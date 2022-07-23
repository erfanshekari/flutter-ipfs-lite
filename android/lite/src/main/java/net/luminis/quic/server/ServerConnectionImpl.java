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
import net.luminis.quic.cid.ConnectionIdManager;
import net.luminis.quic.frame.*;
import net.luminis.quic.log.Logger;
import net.luminis.quic.packet.*;
import net.luminis.quic.send.SenderImpl;
import net.luminis.quic.stream.FlowControl;
import net.luminis.quic.stream.StreamManager;
import net.luminis.quic.tls.QuicTransportParametersExtension;
import net.luminis.tls.NewSessionTicket;
import net.luminis.tls.TlsProtocolException;
import net.luminis.tls.alert.MissingExtensionAlert;
import net.luminis.tls.alert.NoApplicationProtocolAlert;
import net.luminis.tls.extension.ApplicationLayerProtocolNegotiationExtension;
import net.luminis.tls.extension.Extension;
import net.luminis.tls.handshake.*;
import net.luminis.tls.util.ByteUtils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.luminis.quic.QuicConnectionImpl.Status.Connected;
import static net.luminis.quic.QuicConstants.TransportErrorCode.INVALID_TOKEN;
import static net.luminis.quic.QuicConstants.TransportErrorCode.TRANSPORT_PARAMETER_ERROR;


public class ServerConnectionImpl extends QuicConnectionImpl implements ServerConnection, TlsStatusEventHandler {

    private static final int TOKEN_SIZE = 37;
    private final Random random;
    private final SenderImpl sender;
    private final InetSocketAddress initialClientAddress;
    private final boolean retryRequired;
    private final GlobalAckGenerator ackGenerator;
    private final List<FrameProcessor2<AckFrame>> ackProcessors = new CopyOnWriteArrayList<>();
    private final TlsServerEngine tlsEngine;
    private final ApplicationProtocolRegistry applicationProtocolRegistry;
    private final Consumer<ServerConnectionImpl> closeCallback;
    private final StreamManager streamManager;
    private final int initialMaxStreamData;
    private final int maxOpenStreamsUni;
    private final int maxOpenStreamsBidi;
    private final byte[] token;
    private final ConnectionIdManager connectionIdManager;
    private volatile String negotiatedApplicationProtocol;
    private int maxIdleTimeoutInSeconds;
    private volatile long bytesReceived;
    private volatile boolean addressValidated;
    private boolean acceptEarlyData = true;
    private boolean acceptedEarlyData = false;
    private int allowedClientConnectionIds = 3;


    /**
     * Creates a server connection implementation.
     * @param quicVersion  quic version used for this connection
     * @param serverSocket  the socket that is used for sending packets
     * @param initialClientAddress  the initial client address (after handshake, clients can move to different address)
     * @param peerCid  the connection id of the client
     * @param originalDcid  the original destination connection id used by the client
     * @param connectionIdLength  length of the connection id's generated and used by this connection (used as its source)
     * @param tlsServerEngineFactory  factory for creating tls engine
     * @param retryRequired  whether or not a retry is required for address validation
     * @param applicationProtocolRegistry  the registry for application protocols this server supports
     * @param initialRtt  the initial rtt
     * @param connectionRegistry
     * @param closeCallback  callback for notifying interested parties this connection is closed
     * @param log  logger
     */
    protected ServerConnectionImpl(Version quicVersion, DatagramSocket serverSocket, InetSocketAddress initialClientAddress,
                                   byte[] peerCid, byte[] originalDcid, int connectionIdLength, TlsServerEngineFactory tlsServerEngineFactory,
                                   boolean retryRequired, ApplicationProtocolRegistry applicationProtocolRegistry,
                                   Integer initialRtt, ServerConnectionRegistry connectionRegistry, Consumer<ServerConnectionImpl> closeCallback, Logger log) {
        super(quicVersion, Role.Server, null, log);
        this.initialClientAddress = initialClientAddress;
        this.retryRequired = retryRequired;
        this.applicationProtocolRegistry = applicationProtocolRegistry;
        this.closeCallback = closeCallback;

        tlsEngine = tlsServerEngineFactory.createServerEngine(new TlsMessageSender(), this);

        idleTimer = new IdleTimer(this, log);
        sender = new SenderImpl(quicVersion, getMaxPacketSize(), serverSocket, initialClientAddress,this, initialRtt, this.log);
        if (! retryRequired) {
            sender.setAntiAmplificationLimit(0);
        }
        idleTimer.setPtoSupplier(sender::getPto);

        BiConsumer<Integer, String> closeWithErrorFunction = (error, reason) -> {
            immediateCloseWithError(EncryptionLevel.App, error, reason);
        };
        connectionIdManager = new ConnectionIdManager(peerCid, originalDcid, connectionIdLength, allowedClientConnectionIds, connectionRegistry, sender, closeWithErrorFunction, log);

        ackGenerator = sender.getGlobalAckGenerator();
        registerProcessor(ackGenerator);

        if (retryRequired) {
            random = new SecureRandom();
            token = new byte[TOKEN_SIZE];
            random.nextBytes(token);
        }
        else {
            random = null;
            token = null;
        }
        connectionSecrets.computeInitialKeys(originalDcid);
        sender.start(connectionSecrets);

        maxIdleTimeoutInSeconds = 30;
        initialMaxStreamData = 1_000_000;
        maxOpenStreamsUni = 10;
        maxOpenStreamsBidi = 100;
        streamManager = new StreamManager(this, Role.Server, log, maxOpenStreamsUni, maxOpenStreamsBidi);

        //this.log.getQLog().emitConnectionCreatedEvent(Instant.now());
    }

    @Override
    public void abortConnection(Throwable error) {
        log.error(this.toString() + " aborted due to internal error", error);
        closeCallback.accept(this);
    }

    @Override
    protected SenderImpl getSender() {
        return sender;
    }

    @Override
    protected TlsEngine getTlsEngine() {
        return tlsEngine;
    }

    @Override
    protected GlobalAckGenerator getAckGenerator() {
        return ackGenerator;
    }

    @Override
    protected StreamManager getStreamManager() {
        return streamManager;
    }

    @Override
    public long getInitialMaxStreamData() {
        return initialMaxStreamData;
    }

    @Override
    public int getMaxShortHeaderPacketOverhead() {
        return 1  // flag byte
                + connectionIdManager.getCurrentPeerConnectionId().length
                + 4  // max packet number size, in practice this will be mostly 1
                + 16; // encryption overhead
    }

    @Override
    protected int getSourceConnectionIdLength() {
        return connectionIdManager.getInitialConnectionId().length;
    }

    public byte[] getInitialConnectionId() {
        return connectionIdManager.getInitialConnectionId();
    }

    @Override
    public byte[] getSourceConnectionId() {
        return connectionIdManager.getInitialConnectionId();
    }

    @Override
    public byte[] getDestinationConnectionId() {
        return connectionIdManager.getCurrentPeerConnectionId();
    }

    @Override
    public void registerProcessor(FrameProcessor2<AckFrame> ackProcessor) {
        ackProcessors.add(ackProcessor);
    }

    @Override
    public void earlySecretsKnown() {
        connectionSecrets.computeEarlySecrets(tlsEngine);
    }

    @Override
    public void handshakeSecretsKnown() {
        connectionSecrets.computeHandshakeSecrets(tlsEngine, tlsEngine.getSelectedCipher());
    }

    @Override
    public void handshakeFinished() {
        connectionSecrets.computeApplicationSecrets(tlsEngine);
        sender.enableAppLevel();
        // https://tools.ietf.org/html/draft-ietf-quic-tls-32#section-4.9.2
        // "An endpoint MUST discard its handshake keys when the TLS handshake is confirmed"
        // https://tools.ietf.org/html/draft-ietf-quic-tls-32#section-4.1.2
        // "the TLS handshake is considered confirmed at the server when the handshake completes"
        getSender().discard(PnSpace.Handshake, "tls handshake confirmed");
        // TODO: discard keys too
        // https://tools.ietf.org/html/draft-ietf-quic-tls-32#section-4.9.2
        // "The server MUST send a HANDSHAKE_DONE frame as soon as it completes the handshake."
        sendHandshakeDone(new HandshakeDoneFrame(quicVersion));
        connectionState = Connected;

        synchronized (handshakeStateLock) {
            if (handshakeState.transitionAllowed(HandshakeState.Confirmed)) {
                handshakeState = HandshakeState.Confirmed;
                handshakeStateListeners.forEach(l -> l.handshakeStateChangedEvent(handshakeState));
            }
            else {
                log.debug("Handshake state cannot be set to Confirmed");
            }
        }

        if (!acceptedEarlyData) {
            applicationProtocolRegistry.startApplicationProtocolConnection(negotiatedApplicationProtocol, this);
        }
        connectionIdManager.handshakeFinished();
    }

    private void sendHandshakeDone(QuicFrame frame) {
        send(frame, this::sendHandshakeDone);
    }

    @Override
    public void newSessionTicketReceived(NewSessionTicket ticket) {
    }

    @Override
    public void extensionsReceived(List<Extension> extensions) throws TlsProtocolException {
        // https://tools.ietf.org/html/draft-ietf-quic-tls-32#section-8.1
        // "Unless another mechanism is used for agreeing on an application protocol, endpoints MUST use ALPN for this purpose."
        Optional<Extension> alpnExtension = extensions.stream()
                .filter(ext -> ext instanceof ApplicationLayerProtocolNegotiationExtension)
                .findFirst();
        if (!alpnExtension.isPresent()) {
            throw new MissingExtensionAlert("missing application layer protocol negotiation extension");
        }
        else {
            // "When using ALPN, endpoints MUST immediately close a connection (...) if an application protocol is not negotiated."
            List<String> requestedProtocols = ((ApplicationLayerProtocolNegotiationExtension) alpnExtension.get()).getProtocols();
            Optional<String> applicationProtocol = applicationProtocolRegistry.selectSupportedApplicationProtocol(requestedProtocols);
            applicationProtocol
                    .map(protocol -> {
                        // Add negotiated protocol to TLS response (Encrypted Extensions message)
                        tlsEngine.addServerExtensions(new ApplicationLayerProtocolNegotiationExtension(protocol));
                        return protocol; })
                    .map(selectedProtocol -> negotiatedApplicationProtocol = selectedProtocol)
                    .orElseThrow(() -> new NoApplicationProtocolAlert(requestedProtocols));
        }

        // https://tools.ietf.org/html/draft-ietf-quic-tls-32#section-8.2
        // "endpoints that receive ClientHello or EncryptedExtensions messages without the quic_transport_parameters extension
        //  MUST close the connection with an error of type 0x16d (equivalent to a fatal TLS missing_extension alert"
        Optional<Extension> tpExtension = extensions.stream()
                .filter(ext -> ext instanceof QuicTransportParametersExtension)
                .findFirst();
        if (!tpExtension.isPresent()) {
            throw new MissingExtensionAlert("missing quic transport parameters extension");
        }
        else {
            try {
                validateAndProcess(((QuicTransportParametersExtension) tpExtension.get()).getTransportParameters());
            } catch (TransportError transportParameterError) {
                throw new TlsProtocolException("transport parameter error", transportParameterError);
            }
        }

        TransportParameters serverTransportParams = new TransportParameters(maxIdleTimeoutInSeconds, initialMaxStreamData, maxOpenStreamsBidi, maxOpenStreamsUni);
        serverTransportParams.setActiveConnectionIdLimit(allowedClientConnectionIds);
        serverTransportParams.setDisableMigration(true);
        serverTransportParams.setInitialSourceConnectionId(connectionIdManager.getInitialConnectionId());
        serverTransportParams.setOriginalDestinationConnectionId(connectionIdManager.getOriginalDestinationConnectionId());
        if (retryRequired) {
            serverTransportParams.setRetrySourceConnectionId(connectionIdManager.getInitialConnectionId());
        }
        tlsEngine.setSelectedApplicationLayerProtocol(negotiatedApplicationProtocol);
        tlsEngine.addServerExtensions(new QuicTransportParametersExtension(quicVersion, serverTransportParams, Role.Server));
    }

    @Override
    public boolean isEarlyDataAccepted() {
        if (acceptEarlyData) {
            // Remember that server connection actually accepted early data
            acceptedEarlyData = true;
            applicationProtocolRegistry.startApplicationProtocolConnection(negotiatedApplicationProtocol, this);

            log.info("Server accepted early data");
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    protected QuicPacket parsePacket(ByteBuffer data) throws MissingKeysException, DecryptionException, InvalidPacketException {
        try {
            return super.parsePacket(data);
        }
        catch (DecryptionException decryptionException) {
            if (retryRequired && (data.get(0) & 0b1111_0000) == 0b1100_0000) {
                // If retry packet has been sent, but lost, client will send another initial with keys based on odcid
                try {
                    data.rewind();
                    connectionSecrets.computeInitialKeys(connectionIdManager.getOriginalDestinationConnectionId());
                    return super.parsePacket(data);
                }
                finally {
                    connectionSecrets.computeInitialKeys(connectionIdManager.getInitialConnectionId());
                }
            }
            else {
                throw decryptionException;
            }
        }
    }

    @Override
    public void parseAndProcessPackets(int datagram, Instant timeReceived, ByteBuffer data, QuicPacket parsedPacket) {
        if (InitialPacket.isInitial(data) && data.limit() < 1200) {
            // https://tools.ietf.org/html/draft-ietf-quic-transport-34#section-14.1
            // "A server MUST discard an Initial packet that is carried in a UDP datagram with a payload that is smaller
            //  than the smallest allowed maximum datagram size of 1200 bytes."
            return;
        }

        // https://tools.ietf.org/html/draft-ietf-quic-transport-34#section-8
        // "Therefore, after receiving packets from an address that is not yet validated, an endpoint MUST limit the
        //  amount of data it sends to the unvalidated address to three times the amount of data received from that address."
        // https://tools.ietf.org/html/draft-ietf-quic-transport-34#section-8.1
        // "For the purposes of avoiding amplification prior to address validation, servers MUST count all of the
        //  payload bytes received in datagrams that are uniquely attributed to a single connection. This includes
        //  datagrams that contain packets that are successfully processed and datagrams that contain packets that
        //  are all discarded."
        bytesReceived += data.remaining();
        if (! addressValidated) {
            sender.setAntiAmplificationLimit(3 * (int) bytesReceived);
        }

        super.parseAndProcessPackets(datagram, timeReceived, data, parsedPacket);
    }

    @Override
    public ProcessResult process(InitialPacket packet, Instant time) {
        assert(Arrays.equals(packet.getDestinationConnectionId(), connectionIdManager.getInitialConnectionId()) || Arrays.equals(packet.getDestinationConnectionId(), connectionIdManager.getOriginalDestinationConnectionId()));

        if (retryRequired) {
            if (packet.getToken() == null) {
                sendRetry();
                connectionSecrets.computeInitialKeys(connectionIdManager.getInitialConnectionId());
                return ProcessResult.Abort;  // No further packet processing (e.g. ack generation).
            }
            else if (!Arrays.equals(packet.getToken(), token)) {
                // https://tools.ietf.org/html/draft-ietf-quic-transport-33#section-8.1.2
                // "If a server receives a client Initial that can be unprotected but contains an invalid Retry token,
                // (...), the server SHOULD immediately close (Section 10.2) the connection with an INVALID_TOKEN error."
                immediateCloseWithError(EncryptionLevel.Initial, INVALID_TOKEN.value, null);
                return ProcessResult.Abort;
            }
            else {
                // Receiving a valid token implies address is validated.
                addressValidated = true;
                sender.unsetAntiAmplificationLimit();
                // Valid token, proceed as usual.
                processFrames(packet, time);
                return ProcessResult.Continue;
            }
        }
        else {
            processFrames(packet, time);
            return ProcessResult.Continue;
        }
    }

    private void sendRetry() {
        RetryPacket retry = new RetryPacket(quicVersion, connectionIdManager.getInitialConnectionId(), getDestinationConnectionId(), getOriginalDestinationConnectionId(), token);
        sender.send(retry);
    }

    @Override
    public ProcessResult process(ShortHeaderPacket packet, Instant time) {
        connectionIdManager.registerConnectionIdInUse(packet.getDestinationConnectionId());
        processFrames(packet, time);
        return ProcessResult.Continue;
    }

    @Override
    public ProcessResult process(VersionNegotiationPacket packet, Instant time) {
        // Intentionally discarding packet without any action (clients should not send Version Negotiation packets).
        return ProcessResult.Abort;
    }

    @Override
    public ProcessResult process(HandshakePacket packet, Instant time) {
        if (! addressValidated) {
            // https://tools.ietf.org/html/draft-ietf-quic-transport-34#section-8.1
            // "In particular, receipt of a packet protected with Handshake keys confirms that the peer successfully processed
            //  an Initial packet. Once an endpoint has successfully processed a Handshake packet from the peer, it can consider
            //  the peer address to have been validated."
            addressValidated = true;
            sender.unsetAntiAmplificationLimit();
        }
        // https://tools.ietf.org/html/draft-ietf-quic-transport-32#section-17.2.2.1
        // "A server stops sending and processing Initial packets when it receives its first Handshake packet. "
        sender.discard(PnSpace.Initial, "first handshake packet received");  // Only discards when not yet done.
        processFrames(packet, time);
        // https://tools.ietf.org/html/draft-ietf-quic-tls-32#section-4.9.1
        // "a server MUST discard Initial keys when it first successfully processes a Handshake packet"
        // TODO: discard keys too

        return ProcessResult.Continue;
    }

    @Override
    public ProcessResult process(RetryPacket packet, Instant time) {
        // Intentionally discarding packet without any action (clients should not send Retry packets).
        return ProcessResult.Abort;
    }

    @Override
    public ProcessResult process(ZeroRttPacket packet, Instant time) {
       if (acceptedEarlyData) {
            processFrames(packet, time);
        }
        else {
            log.warn("Ignoring 0-RTT packet because server connection does not accept early data.");
        }
        return ProcessResult.Continue;
    }

    @Override
    public void process(AckFrame ackFrame, QuicPacket packet, Instant timeReceived) {
        ackProcessors.forEach(processor -> processor.process(ackFrame, packet.getPnSpace(), timeReceived));
    }

    @Override
    public void process(HandshakeDoneFrame handshakeDoneFrame, QuicPacket packet, Instant timeReceived) {
    }

    @Override
    public void process(NewConnectionIdFrame newConnectionIdFrame, QuicPacket packet, Instant timeReceived) {
        connectionIdManager.process(newConnectionIdFrame);
    }

    @Override
    public void process(NewTokenFrame newTokenFrame, QuicPacket packet, Instant timeReceived) {
    }

    @Override
    public void process(RetireConnectionIdFrame retireConnectionIdFrame, QuicPacket packet, Instant timeReceived) {
        connectionIdManager.process(retireConnectionIdFrame, packet.getDestinationConnectionId());
    }

    @Override
    protected void terminate() {
        super.terminate();
        //log.getQLog().emitConnectionTerminatedEvent();
        String statsSummary = getStats().toString().replace("\n", "    ");
        log.info(String.format("Stats for connection %s: %s", ByteUtils.bytesToHex(connectionIdManager.getInitialConnectionId()), statsSummary));
        closeCallback.accept(this);
    }

    private void validateAndProcess(TransportParameters transportParameters) throws TransportError {
        if (transportParameters.getInitialMaxStreamsBidi() > 0x1000000000000000l) {
            throw new TransportError(TRANSPORT_PARAMETER_ERROR);
        }
        if (transportParameters.getMaxUdpPayloadSize() < 1200) {
            throw new TransportError(TRANSPORT_PARAMETER_ERROR);
        }
        if (transportParameters.getAckDelayExponent() > 20) {
            throw new TransportError(TRANSPORT_PARAMETER_ERROR);
        }
        if (transportParameters.getMaxAckDelay() > 16384) {
            throw new TransportError(TRANSPORT_PARAMETER_ERROR);
        }
        if (transportParameters.getActiveConnectionIdLimit() < 2) {
            throw new TransportError(TRANSPORT_PARAMETER_ERROR);
        }
        if (!connectionIdManager.validateInitialPeerConnectionId(transportParameters.getInitialSourceConnectionId())) {
            // https://tools.ietf.org/html/draft-ietf-quic-transport-32#section-7.3
            // "An endpoint MUST treat absence of the initial_source_connection_id transport parameter from either
            //  endpoint (...) as a connection error of type TRANSPORT_PARAMETER_ERROR."
            // "An endpoint MUST treat the following as a connection error of type TRANSPORT_PARAMETER_ERROR or
            //  PROTOCOL_VIOLATION: a mismatch between values received from a peer in these transport parameters and the
            //  value sent in the corresponding Destination or Source Connection ID fields of Initial packets."
            throw new TransportError(TRANSPORT_PARAMETER_ERROR);
        }

        determineIdleTimeout(maxIdleTimeoutInSeconds * 1000, transportParameters.getMaxIdleTimeout());

        connectionIdManager.registerPeerCidLimit(transportParameters.getActiveConnectionIdLimit());

        flowController = new FlowControl(Role.Server, transportParameters.getInitialMaxData(),
                transportParameters.getInitialMaxStreamDataBidiLocal(), transportParameters.getInitialMaxStreamDataBidiRemote(),
                transportParameters.getInitialMaxStreamDataUni(), log);
        streamManager.setFlowController(flowController);

        streamManager.setInitialMaxStreamsBidi(transportParameters.getInitialMaxStreamsBidi());
        streamManager.setInitialMaxStreamsUni(transportParameters.getInitialMaxStreamsUni());
    }

    @Override
    public InetAddress getInitialClientAddress() {
        return initialClientAddress.getAddress();
    }

    private class TlsMessageSender implements ServerMessageSender {
        @Override
        public void send(ServerHello sh) {
            CryptoStream cryptoStream = getCryptoStream(EncryptionLevel.Initial);
            cryptoStream.write(sh, false);
            log.sentPacketInfo(cryptoStream.toStringSent());
        }

        @Override
        public void send(EncryptedExtensions ee) {
            getCryptoStream(EncryptionLevel.Handshake).write(ee, false);
        }

        @Override
        public void send(CertificateMessage cm) throws IOException {
            getCryptoStream(EncryptionLevel.Handshake).write(cm, false);
        }

        @Override
        public void send(CertificateVerifyMessage cv) throws IOException {
            getCryptoStream(EncryptionLevel.Handshake).write(cv, false);
        }

        @Override
        public void send(FinishedMessage finished) throws IOException {
            CryptoStream cryptoStream = getCryptoStream(EncryptionLevel.Handshake);
            cryptoStream.write(finished, false);
            log.sentPacketInfo(cryptoStream.toStringSent());
        }

        @Override
        public void send(NewSessionTicketMessage newSessionTicket) throws IOException {
            CryptoStream cryptoStream = getCryptoStream(EncryptionLevel.App);
            cryptoStream.write(newSessionTicket, true);
        }
    }

    public boolean isClosed() {
        return connectionState == Status.Closed;
    }

    public byte[] getOriginalDestinationConnectionId() {
        return connectionIdManager.getOriginalDestinationConnectionId();
    }

    public List<byte[]> getActiveConnectionIds() {
        return connectionIdManager.getActiveConnectionIds();
    }

    @Override
    public void setMaxAllowedBidirectionalStreams(int max) {
    }

    @Override
    public void setMaxAllowedUnidirectionalStreams(int max) {
    }

    @Override
    public void setDefaultStreamReceiveBufferSize(long size) {
    }

    @Override
    public void setPeerInitiatedStreamCallback(Consumer<QuicStream> streamConsumer) {
        streamManager.setPeerInitiatedStreamCallback(streamConsumer);
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return initialClientAddress;
    }

    @Override
    public String toString() {
        return "ServerConnection[" + ByteUtils.bytesToHex(connectionIdManager.getOriginalDestinationConnectionId()) + "]";
    }
}
