/*
 * Copyright © 2020, 2021, 2022 Peter Doornbosch
 *
 * This file is part of Kwik, an implementation of the QUIC protocol in Java.
 *
 * Kwik is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * Kwik is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.luminis.quic.server;

import net.luminis.quic.*;
import net.luminis.quic.log.Logger;
import net.luminis.quic.log.NullLogger;
import net.luminis.quic.packet.VersionNegotiationPacket;
import net.luminis.tls.handshake.TlsServerEngineFactory;
import net.luminis.tls.util.ByteUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import threads.lite.LogUtils;

/**
 * Listens for QUIC connections on a given port. Requires server certificate and corresponding private key.
 */
public class ServerConnector implements ServerConnectionRegistry {
    private static final String TAG = ServerConnector.class.getSimpleName();
    private static final int MINIMUM_LONG_HEADER_LENGTH = 1 + 4 + 1 + 0 + 1 + 0;
    private static final int CONNECTION_ID_LENGTH = 4;

    private final Receiver receiver;
    private final Logger log;
    private final List<Version> supportedVersions;
    private final List<Integer> supportedVersionIds;
    private final DatagramSocket serverSocket;
    private final boolean requireRetry;
    private Integer initalRtt = 100;
    private Map<ConnectionSource, ServerConnectionProxy> currentConnections;
    private TlsServerEngineFactory tlsEngineFactory;
    private final ServerConnectionFactory serverConnectionFactory;
    private ApplicationProtocolRegistry applicationProtocolRegistry;


    public ServerConnector(int port, InputStream certificateFile, InputStream certificateKeyFile, List<Version> supportedVersions, boolean requireRetry, Logger log) throws Exception {
        this(new DatagramSocket(port), certificateFile, certificateKeyFile, supportedVersions, requireRetry, log);
    }

    public ServerConnector(DatagramSocket socket, InputStream certificateFile, InputStream certificateKeyFile, List<Version> supportedVersions, boolean requireRetry, Logger log) throws Exception {
        serverSocket = socket;
        this.supportedVersions = supportedVersions;
        this.requireRetry = requireRetry;
        this.log = Objects.requireNonNull(log);

        tlsEngineFactory = new TlsServerEngineFactory(certificateFile, certificateKeyFile);
        applicationProtocolRegistry = new ApplicationProtocolRegistry();
        serverConnectionFactory = new ServerConnectionFactory(CONNECTION_ID_LENGTH, serverSocket, tlsEngineFactory,
                this.requireRetry, applicationProtocolRegistry, initalRtt, this, this::removeConnection, log);

        supportedVersionIds = supportedVersions.stream().map(version -> version.getId()).collect(Collectors.toList());
        currentConnections = new ConcurrentHashMap<>();
        receiver = new Receiver(socket, rawPacket -> {
            try {
                process(rawPacket);
            } catch (Throwable throwable) {
                LogUtils.error(TAG, "Server receiver " + throwable.getMessage());
            }
        }, new NullLogger(), throwable -> LogUtils.error(TAG, throwable));
    }


    public void shutdown() {
        receiver.shutdown();
    }

    public void registerApplicationProtocol(String protocol, ApplicationProtocolConnectionFactory protocolConnectionFactory) {
        applicationProtocolRegistry.registerApplicationProtocol(protocol, protocolConnectionFactory);
    }

    public Set<String> getRegisteredApplicationProtocols() {
        return applicationProtocolRegistry.getRegisteredApplicationProtocols();
    }

    public void start() {
        receiver.start();
   }

    protected void process(RawPacket rawPacket) {
        ByteBuffer data = rawPacket.getData();
        int flags = data.get();
        data.rewind();
        if ((flags & 0b1100_0000) == 0b1100_0000) {
            // https://tools.ietf.org/html/draft-ietf-quic-transport-32#section-17.2
            // "Header Form:  The most significant bit (0x80) of byte 0 (the first byte) is set to 1 for long headers."
            processLongHeaderPacket(new InetSocketAddress(rawPacket.getAddress(), rawPacket.getPort()), data);
        } else if ((flags & 0b1100_0000) == 0b0100_0000) {
            // https://tools.ietf.org/html/draft-ietf-quic-transport-32#section-17.3
            // "Header Form:  The most significant bit (0x80) of byte 0 is set to 0 for the short header.
            processShortHeaderPacket(new InetSocketAddress(rawPacket.getAddress(), rawPacket.getPort()), data);
        } else {
            // https://tools.ietf.org/html/draft-ietf-quic-transport-32#section-17.2
            // https://tools.ietf.org/html/draft-ietf-quic-transport-32#section-17.3
            // "The next bit (0x40) of byte 0 is set to 1. Packets containing a zero value for this bit are not valid
            //  packets in this version and MUST be discarded."
            log.warn(String.format("Invalid Quic packet (flags: %02x) is discarded", flags));
        }
    }

    private void processLongHeaderPacket(InetSocketAddress clientAddress, ByteBuffer data) {
        if (data.remaining() >= MINIMUM_LONG_HEADER_LENGTH) {
            data.position(1);
            int version = data.getInt();

            data.position(5);
            int dcidLength = data.get() & 0xff;
            // https://tools.ietf.org/html/draft-ietf-quic-transport-32#section-17.2
            // "In QUIC version 1, this value MUST NOT exceed 20. Endpoints that receive a version 1 long header with a
            //  value larger than 20 MUST drop the packet. In order to properly form a Version Negotiation packet,
            //  servers SHOULD be able to read longer connection IDs from other QUIC versions."
            if (dcidLength > 20) {
                if (initialWithUnspportedVersion(data, version)) {
                    // https://tools.ietf.org/html/draft-ietf-quic-transport-32#section-6
                    // "A server sends a Version Negotiation packet in response to each packet that might initiate a new connection;"
                    sendVersionNegotiationPacket(clientAddress, data, dcidLength);
                }
                return;
            }
            if (data.remaining() >= dcidLength + 1) {  // after dcid at least one byte scid length
                byte[] dcid = new byte[dcidLength];
                data.get(dcid);
                int scidLength = data.get() & 0xff;
                if (data.remaining() >= scidLength) {
                    byte[] scid = new byte[scidLength];
                    data.get(scid);
                    data.rewind();

                    Optional<ServerConnectionProxy> connection = isExistingConnection(clientAddress, dcid);
                    if (!connection.isPresent()) {
                        synchronized (this) {
                            if (mightStartNewConnection(data, version, dcid) && !isExistingConnection(clientAddress, dcid).isPresent()) {
                                connection = Optional.of(createNewConnection(version, clientAddress, scid, dcid));
                            } else if (initialWithUnspportedVersion(data, version)) {
                                log.received(Instant.now(), 0, EncryptionLevel.Initial, dcid, scid);
                                // https://tools.ietf.org/html/draft-ietf-quic-transport-32#section-6
                                // "A server sends a Version Negotiation packet in response to each packet that might initiate a new connection;"
                                sendVersionNegotiationPacket(clientAddress, data, dcidLength);
                            }
                        }
                    }
                    connection.ifPresent(c -> c.parsePackets(0, Instant.now(), data));
                }
            }
        }
    }

    private void processShortHeaderPacket(InetSocketAddress clientAddress, ByteBuffer data) {
        byte[] dcid = new byte[CONNECTION_ID_LENGTH];
        data.position(1);
        data.get(dcid);
        data.rewind();
        Optional<ServerConnectionProxy> connection = isExistingConnection(clientAddress, dcid);
        connection.ifPresent(c -> c.parsePackets(0, Instant.now(), data));
        /*
        connection.ifPresentOrElse(c -> c.parsePackets(0, Instant.now(), data),
                () -> log.warn("Discarding short header packet addressing non existent connection " + ByteUtils.bytesToHex(dcid)));*/
    }

    private boolean mightStartNewConnection(ByteBuffer packetBytes, int version, byte[] dcid) {
        // https://tools.ietf.org/html/draft-ietf-quic-transport-32#section-7.2
        // "This Destination Connection ID MUST be at least 8 bytes in length."
        if (dcid.length >= 8) {
            return supportedVersionIds.contains(version);
        } else {
            return false;
        }
    }

    private boolean initialWithUnspportedVersion(ByteBuffer packetBytes, int version) {
        packetBytes.rewind();
        int flags = packetBytes.get() & 0xff;
        if ((flags & 0b1111_0000) == 0b1100_0000) {
            // https://tools.ietf.org/html/draft-ietf-quic-transport-32#section-14.1
            // "A server MUST discard an Initial packet that is carried in a UDP
            //   datagram with a payload that is smaller than the smallest allowed
            //   maximum datagram size of 1200 bytes. "
            if (packetBytes.limit() >= 1200) {
                return !supportedVersionIds.contains(version);
            }
        }
        return false;
    }

    private ServerConnectionProxy createNewConnection(int versionValue, InetSocketAddress clientAddress, byte[] scid, byte[] dcid) {
        try {
            Version version = Version.parse(versionValue);
            ServerConnectionProxy connectionCandidate = new ServerConnectionCandidate(version, clientAddress, scid, dcid, serverConnectionFactory, this, log);
            // Register new connection now with the original connection id, as retransmitted initial packets with the
            // same original dcid might be received, which should _not_ lead to another connection candidate)
            currentConnections.put(new ConnectionSource(dcid), connectionCandidate);

            return connectionCandidate;
        } catch (UnknownVersionException e) {
            // Impossible, as it only gets here if the given version is supported, so it is a known version.
            throw new RuntimeException();
        }
    }

    private void removeConnection(ServerConnectionImpl connection) {
        ServerConnectionProxy removed = null;
        for (byte[] connectionId: connection.getActiveConnectionIds()) {
            if (removed == null) {
                removed = currentConnections.remove(new ConnectionSource(connectionId));
                if (removed == null) {
                    log.error("Cannot remove connection with cid " + ByteUtils.bytesToHex(connectionId));
                }
            }
            else {
                if (removed != currentConnections.remove(new ConnectionSource(connectionId))) {
                    log.error("Removed connections for set of active cids are not identical");
                }
            }
        }
        currentConnections.remove(new ConnectionSource(connection.getOriginalDestinationConnectionId()));

        if (! removed.isClosed()) {
            log.error("Removed connection with dcid " + ByteUtils.bytesToHex(connection.getOriginalDestinationConnectionId()) + " that is not closed...");
        }
        removed.terminate();
    }


    private Optional<ServerConnectionProxy> isExistingConnection(InetSocketAddress clientAddress, byte[] dcid) {
        return Optional.ofNullable(currentConnections.get(new ConnectionSource(dcid)));
    }
    
    private void sendVersionNegotiationPacket(InetSocketAddress clientAddress, ByteBuffer data, int dcidLength) {
        data.rewind();
        if (data.remaining() >= 1 + 4 + 1 + dcidLength + 1) {
            byte[] dcid = new byte[dcidLength];
            data.position(1 + 4 + 1);
            data.get(dcid);
            int scidLength = data.get() & 0xff;
            byte[] scid = new byte[scidLength];
            if (scidLength > 0) {
                data.get(scid);
            }
            // https://tools.ietf.org/html/draft-ietf-quic-transport-32#section-17.2.1
            // "The server MUST include the value from the Source Connection ID field of the packet it receives in the
            //  Destination Connection ID field. The value for Source Connection ID MUST be copied from the Destination
            //  Connection ID of the received packet, ..."
            VersionNegotiationPacket versionNegotiationPacket = new VersionNegotiationPacket(supportedVersions, dcid, scid);
            byte[] packetBytes = versionNegotiationPacket.generatePacketBytes(null, null);
            DatagramPacket datagram = new DatagramPacket(packetBytes, packetBytes.length, clientAddress.getAddress(), clientAddress.getPort());
            try {
                serverSocket.send(datagram);
                log.sent(Instant.now(), versionNegotiationPacket);
            } catch (IOException e) {
                log.error("Sending version negotiation packet failed", e);
            }
        }
    }

    @Override
    public void registerConnection(ServerConnectionProxy connection, byte[] connectionId) {
        currentConnections.put(new ConnectionSource(connectionId), connection);
    }

    @Override
    public void deregisterConnection(ServerConnectionProxy connection, byte[] connectionId) {
        boolean removed = currentConnections.remove(new ConnectionSource(connectionId), connection);
        if (! removed && currentConnections.containsKey(new ConnectionSource(connectionId))) {
            log.error("Connection " + connection + " not removed, because "
                    + currentConnections.get(new ConnectionSource(connectionId)) + " is registered for "
                    + ByteUtils.bytesToHex(connectionId));
        }
    }

    @Override
    public void registerAdditionalConnectionId(byte[] currentConnectionId, byte[] newConnectionId) {
        ServerConnectionProxy connection = currentConnections.get(new ConnectionSource(currentConnectionId));
        if (connection != null) {
            currentConnections.put(new ConnectionSource(newConnectionId), connection);
        }
        else {
            log.error("Cannot add additional cid to non-existing connection " + ByteUtils.bytesToHex(currentConnectionId));
        }
    }

    @Override
    public void deregisterConnectionId(byte[] connectionId) {
        currentConnections.remove(new ConnectionSource(connectionId));
    }

    /**
     * Logs the entire connection table. For debugging purposed only.
     */
    private void logConnectionTable() {
        log.info("Connection table: \n" +
                currentConnections.entrySet().stream()
                        .sorted(new Comparator<Map.Entry<ConnectionSource, ServerConnectionProxy>>() {
                            @Override
                            public int compare(Map.Entry<ConnectionSource, ServerConnectionProxy> o1, Map.Entry<ConnectionSource, ServerConnectionProxy> o2) {
                                return o1.getValue().toString().compareTo(o2.getValue().toString());
                            }
                        })
                        .map(e -> e.getKey() + "->" + e.getValue())
                        .collect(Collectors.joining("\n")));

    }

}
