package threads.lite.host;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;

import net.luminis.quic.QuicConnection;
import net.luminis.quic.Version;
import net.luminis.quic.log.NullLogger;
import net.luminis.quic.server.ApplicationProtocolConnection;
import net.luminis.quic.server.ApplicationProtocolConnectionFactory;
import net.luminis.quic.server.ServerConnector;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import bitswap.pb.MessageOuterClass;
import circuit.pb.Circuit;
import identify.pb.IdentifyOuterClass;
import threads.lite.IPFS;
import threads.lite.LogUtils;
import threads.lite.bitswap.BitSwap;
import threads.lite.bitswap.BitSwapMessage;
import threads.lite.cid.Cid;
import threads.lite.cid.Multiaddr;
import threads.lite.cid.Peer;
import threads.lite.cid.PeerId;
import threads.lite.cid.Protocol;
import threads.lite.cid.ProtocolSupport;
import threads.lite.core.Closeable;
import threads.lite.crypto.PrivKey;
import threads.lite.crypto.PubKey;
import threads.lite.dht.KadDht;
import threads.lite.dht.Routing;
import threads.lite.format.BlockStore;
import threads.lite.ident.IdentityService;
import threads.lite.ipns.Ipns;
import threads.lite.relay.RelayConnection;
import threads.lite.relay.RelayService;
import threads.lite.relay.Reservation;


public class LiteHost {


    @NonNull
    private static final String TAG = LiteHost.class.getSimpleName();
    @NonNull
    private static final Duration DefaultRecordEOL = Duration.ofHours(24);
    @NonNull
    public final AtomicReference<ProtocolSupport> protocol = new AtomicReference<>(ProtocolSupport.UNKNOWN);
    @NonNull
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    /* NOT YET REQUIRED
    @NonNull

    @NonNull
    private static final TrustManager tm = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String s) {
            try {
                if (IPFS.EVALUATE_PEER) {
                    for (X509Certificate cert : chain) {
                        PubKey pubKey = LiteHostCertificate.extractPublicKey(cert);
                        Objects.requireNonNull(pubKey);
                        PeerId peerId = PeerId.fromPubKey(pubKey);
                        Objects.requireNonNull(peerId);
                    }
                }
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String s) {

            try {
                if (IPFS.EVALUATE_PEER) {
                    for (X509Certificate cert : chain) {
                        PubKey pubKey = LiteHostCertificate.extractPublicKey(cert);
                        Objects.requireNonNull(pubKey);
                        PeerId peerId = PeerId.fromPubKey(pubKey);
                        Objects.requireNonNull(peerId);
                        remotes.put(peerId, pubKey);
                    }
                }
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };*/
    @NonNull
    private final ConcurrentHashMap<PeerId, Reservation> reservations = new ConcurrentHashMap<>();
    @NonNull
    private final ConcurrentSkipListSet<InetAddress> addresses = new ConcurrentSkipListSet<>(
            Comparator.comparing(InetAddress::getHostAddress)
    );
    @NonNull
    private final Routing routing;
    @NonNull
    private final PrivKey privKey;
    @NonNull
    private final BitSwap bitSwap;
    @NonNull
    private final PeerId self;
    private final int port;
    @NonNull
    private final LiteHostCertificate selfSignedCertificate;
    @NonNull
    private final Set<QuicConnection> swarm = ConcurrentHashMap.newKeySet();
    @NonNull
    private final ReentrantLock lock = new ReentrantLock();
    @Nullable
    private Consumer<String> incomingPush;
    @Nullable
    private Consumer<QuicConnection> incomingConnection;
    @Nullable
    private ServerConnector server;

    public LiteHost(@NonNull LiteHostCertificate selfSignedCertificate,
                    @NonNull PrivKey privKey,
                    @NonNull BlockStore blockstore,
                    int port) {
        this.selfSignedCertificate = selfSignedCertificate;
        this.privKey = privKey;

        this.self = PeerId.fromPubKey(privKey.publicKey());

        this.routing = new KadDht(this, new Ipns());

        this.bitSwap = new BitSwap(blockstore, this);

        if (port >= 0 && !isLocalPortFree(port)) {
            this.port = nextFreePort();
        } else {
            this.port = port;
        }
        if (this.port >= 0) {
            try {
                List<Version> supportedVersions = new ArrayList<>();
                supportedVersions.add(Version.IETF_draft_29);
                supportedVersions.add(Version.QUIC_version_1);

                server = new ServerConnector(port,
                        new FileInputStream(selfSignedCertificate.certificate()),
                        new FileInputStream(selfSignedCertificate.privateKey()),
                        supportedVersions, false, new NullLogger());
                server.registerApplicationProtocol(IPFS.APRN, new ApplicationProtocolConnectionFactory() {
                    @Override
                    public ApplicationProtocolConnection createConnection(
                            String protocol, QuicConnection quicConnection) {
                        return new ServerHandler(LiteHost.this, quicConnection);

                    }
                });
                server.start();
            } catch (Throwable throwable) {
                LogUtils.error(TAG, throwable);
            }
        }

        updateListenAddresses();
    }

    public static int nextFreePort() {
        int port = ThreadLocalRandom.current().nextInt(4001, 65535);
        while (true) {
            if (isLocalPortFree(port)) {
                return port;
            } else {
                port = ThreadLocalRandom.current().nextInt(4001, 65535);
            }
        }
    }

    private static boolean isLocalPortFree(int port) {
        try {
            new ServerSocket(port).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @NonNull
    public LiteHostCertificate getSelfSignedCertificate() {
        return selfSignedCertificate;
    }

    @NonNull
    public ConcurrentHashMap<PeerId, Reservation> reservations() {
        return reservations;
    }

    @NonNull
    public Routing getRouting() {
        return routing;
    }

    @NonNull
    public BitSwap getBitSwap() {
        return bitSwap;
    }

    public PeerId self() {
        return self;
    }

    public void message(@NonNull QuicConnection conn, @NonNull MessageOuterClass.Message msg) {
        BitSwapMessage message = BitSwapMessage.newMessageFromProto(msg);
        bitSwap.receiveMessage(conn, message);
    }

    public void findProviders(@NonNull Closeable closeable, @NonNull Consumer<Peer> providers,
                              @NonNull Cid cid) throws InterruptedException {
        routing.findProviders(closeable, providers, cid);
    }

    @NonNull
    private Multiaddr findFirstValid(@NonNull List<Multiaddr> all, @NonNull Protocol type)
            throws Exception {
        if (all.size() == 0) {
            throw new Exception("No default listen addresses");
        }
        Multiaddr multiaddr = all.get(0);
        for (Multiaddr ma : all) {
            if (ma.has(type)) {
                multiaddr = ma;
                break;
            }
        }
        return multiaddr;
    }

    @NonNull
    public Multiaddr defaultListenAddress(boolean enhancePeerId) throws Exception {
        int port = getPort();
        if (port <= 0) {
            throw new Exception("Port is not defined");
        }
        List<Multiaddr> multiaddrs = defaultListenAddresses(enhancePeerId);
        return findFirstValid(multiaddrs, getDefaultProtocol());
    }


    @NonNull
    private List<Multiaddr> prepareAddresses(@NonNull Set<Multiaddr> set) {
        List<Multiaddr> all = new ArrayList<>();
        for (Multiaddr ma : set) {
            try {
                Set<Protocol> protocols = ma.getProtocols();
                if (protocols.contains(Protocol.DNS)) {
                    all.add(DnsResolver.resolveDns(ma));
                } else if (protocols.contains(Protocol.DNS6)) {
                    all.add(DnsResolver.resolveDns6(ma));
                } else if (protocols.contains(Protocol.DNS4)) {
                    all.add(DnsResolver.resolveDns4(ma));
                } else if (protocols.contains(Protocol.DNSADDR)) {
                    all.addAll(DnsResolver.resolveDnsAddress(ma));
                } else {
                    all.add(ma);
                }
            } catch (Throwable throwable) {
                LogUtils.error(TAG, ma.toString() + " prepareAddresses " + throwable);
            }
        }
        return supported(all);
    }

    @NonNull
    public List<Multiaddr> supported(@NonNull List<Multiaddr> all) {
        List<Multiaddr> result = new ArrayList<>();
        for (Multiaddr ma : all) {
            if (ma.isSupported(protocol.get())) {
                result.add(ma);
            }
        }
        return result;
    }


    public void findPeer(@NonNull Closeable closeable,
                         @NonNull Consumer<Peer> consumer,
                         @NonNull PeerId peerId) throws InterruptedException {
        routing.findPeer(closeable, consumer, peerId);
    }

    public void publishName(@NonNull Closeable closable, @NonNull PrivKey privKey,
                            @NonNull String name, @NonNull PeerId id, int sequence)
            throws InterruptedException {


        Date eol = Date.from(new Date().toInstant().plus(DefaultRecordEOL));

        Duration duration = Duration.ofHours(IPFS.IPNS_DURATION);
        ipns.pb.Ipns.IpnsEntry
                record = Ipns.create(privKey, name.getBytes(), sequence, eol, duration);

        PubKey pk = privKey.publicKey();

        record = Ipns.embedPublicKey(pk, record);

        byte[] bytes = record.toByteArray();

        byte[] ipns = IPFS.IPNS_PATH.getBytes();
        byte[] ipnsKey = Bytes.concat(ipns, id.getBytes());
        routing.putValue(closable, ipnsKey, bytes);
    }

    public void swarmReduce(@NonNull QuicConnection connection) {
        swarm.remove(connection);
    }

    public boolean swarmEnhance(@NonNull QuicConnection connection) {
        return swarm.add(connection);
    }

    @NonNull
    public List<QuicConnection> getSwarm() {
        List<QuicConnection> result = new ArrayList<>();
        for (QuicConnection conn : swarm) {
            if (conn.isConnected()) {
                result.add(conn);
            } else {
                swarm.remove(conn);
            }
        }

        return result;
    }


    @NonNull
    public List<Multiaddr> listenAddresses(boolean enhancePeerId) {
        try {
            List<Multiaddr> list = new ArrayList<>();
            if (port > 0) {
                list.addAll(defaultListenAddresses(enhancePeerId));
            }
            return list;
        } catch (Throwable throwable) {
            LogUtils.error(TAG, throwable);
        }
        return Collections.emptyList();

    }

    public List<Multiaddr> defaultListenAddresses(boolean enhancePeerId) {
        List<Multiaddr> result = new ArrayList<>();
        if (port > 0) {
            for (InetAddress inetAddress : getAddresses()) {
                Multiaddr multiaddr = Multiaddr.transform(new InetSocketAddress(inetAddress, port));
                if (enhancePeerId) {
                    result.add(new Multiaddr(
                            multiaddr.toString().concat("/p2p/").concat(self().toBase58())));
                } else {
                    result.add(multiaddr);
                }
            }
        }
        return result;
    }

    public ConcurrentSkipListSet<InetAddress> getAddresses() {
        try {
            evaluateDefaultHost();
        } catch (Throwable throwable) {
            LogUtils.error(TAG, throwable);
        }
        return addresses;
    }

    private void evaluateDefaultHost() throws UnknownHostException {
        if (addresses.isEmpty()) {
            addresses.add(InetAddress.getByName("127.0.0.1"));
            addresses.add(InetAddress.getByName("::1"));
        }
    }

    @NonNull
    public Protocol getDefaultProtocol() {
        if (protocol.get() == ProtocolSupport.IPv6) {
            return Protocol.IP6;
        }
        return Protocol.IP4;
    }

    @Nullable
    public QuicConnection find(@NonNull PeerId peerId, int timeout, int initialMaxStreams,
                               int initialMaxStreamData, @NonNull Closeable closeable) {

        AtomicReference<QuicConnection> found = new AtomicReference<>();
        try {
            findPeer(() -> closeable.isClosed() || found.get() != null, peer -> {
                try {
                    found.set(connect(peer, timeout, IPFS.GRACE_PERIOD, initialMaxStreams,
                            initialMaxStreamData));
                } catch (Throwable throwable) {
                    // ignore exception again
                    found.set(null);
                }
            }, peerId);
        } catch (Throwable throwable) {
            // ignore exception again
        }
        return found.get();

    }


    @NonNull
    public QuicConnection connect(@NonNull Peer peer, int timeout, int maxIdleTimeoutInSeconds,
                                  int initialMaxStreams, int initialMaxStreamData)
            throws ConnectException, InterruptedException {
        return connect(peer.getPeerId(), peer.getMultiaddrs(), timeout, maxIdleTimeoutInSeconds,
                initialMaxStreams, initialMaxStreamData);
    }

    @NonNull
    private QuicConnection connect(@NonNull PeerId peerId, @NonNull Set<Multiaddr> set,
                                   int timeout, int maxIdleTimeoutInSeconds,
                                   int initialMaxStreams, int initialMaxStreamData)
            throws ConnectException, InterruptedException {


        List<Multiaddr> multiaddr = prepareAddresses(set);
        int addresses = multiaddr.size();
        if (addresses == 0) {
            throw new ConnectException("No addresses");
        }

        return connect(peerId, multiaddr, timeout, maxIdleTimeoutInSeconds,
                initialMaxStreams, initialMaxStreamData);
    }

    @NonNull
    public QuicConnection dial(@NonNull PeerId peerId, @NonNull Multiaddr address, int timeout,
                               int maxIdleTimeoutInSeconds, int initialMaxStreams,
                               int initialMaxStreamData)
            throws ConnectException, InterruptedException {
        return Dialer.dial(this, peerId, address, timeout, maxIdleTimeoutInSeconds,
                initialMaxStreams, initialMaxStreamData);
    }

    @NonNull
    private QuicConnection connect(@NonNull PeerId peerId, @NonNull List<Multiaddr> multiaddrs,
                                   int timeout, int maxIdleTimeoutInSeconds,
                                   int initialMaxStreams, int initialMaxStreamData)
            throws ConnectException, InterruptedException {

        CompletableFuture<QuicConnection> done = new CompletableFuture<>();
        if (!multiaddrs.isEmpty()) {
            ExecutorService executor = Executors.newFixedThreadPool(
                    Runtime.getRuntime().availableProcessors());
            for (Multiaddr address : multiaddrs) {
                boolean relayConnection = address.isCircuitAddress();
                if (!relayConnection) {
                    executor.execute(() -> {
                        try {
                            QuicConnection conn = dial(peerId, address, timeout,
                                    maxIdleTimeoutInSeconds, initialMaxStreams,
                                    initialMaxStreamData);
                            done.complete(conn);

                        } catch (Throwable ignore) {
                            // ignore
                        }
                    });
                }
            }
            executor.shutdown();
            try {
                return done.get(timeout, TimeUnit.SECONDS);
            } catch (TimeoutException | ExecutionException exception) {
                throw new ConnectException(exception.getMessage());
            } finally {
                executor.shutdownNow();
            }
        }

        throw new ConnectException("no addresses left");

    }

    public boolean hasReservation(@NonNull PeerId relayId) {
        return reservations.containsKey(relayId);
    }

    public void push(@NonNull byte[] content) {
        try {
            Objects.requireNonNull(content);
            if (incomingPush != null) {
                executor.execute(() -> incomingPush.accept(new String(content)));
            }
        } catch (Throwable throwable) {
            LogUtils.error(TAG, throwable);
        }

    }

    public void incomingConnection(@NonNull QuicConnection connection) {
        try {
            Objects.requireNonNull(connection);
            if (incomingConnection != null) {
                executor.execute(() -> incomingConnection.accept(connection));
            }
        } catch (Throwable throwable) {
            LogUtils.error(TAG, throwable);
        }

    }

    public void setIncomingPush(@Nullable Consumer<String> incomingPush) {
        this.incomingPush = incomingPush;
    }

    public void setIncomingConnection(@Nullable Consumer<QuicConnection> incomingConnection) {
        this.incomingConnection = incomingConnection;
    }

    public IdentifyOuterClass.Identify createIdentity(@Nullable InetSocketAddress inetSocketAddress) {

        IdentifyOuterClass.Identify.Builder builder = IdentifyOuterClass.Identify.newBuilder()
                .setAgentVersion(IPFS.AGENT)
                .setPublicKey(ByteString.copyFrom(privKey.publicKey().bytes()))
                .setProtocolVersion(IPFS.PROTOCOL_VERSION);

        List<Multiaddr> addresses = listenAddresses(false);
        for (Multiaddr addr : addresses) {
            builder.addListenAddrs(ByteString.copyFrom(addr.getBytes()));
        }

        List<String> protocols = getProtocols();
        for (String protocol : protocols) {
            builder.addProtocols(protocol);
        }

        if (inetSocketAddress != null) {
            Multiaddr observed = Multiaddr.transform(inetSocketAddress);
            builder.setObservedAddr(ByteString.copyFrom(observed.getBytes()));
        }

        return builder.build();
    }

    private List<String> getProtocols() {
        return Arrays.asList(IPFS.STREAM_PROTOCOL, IPFS.PUSH_PROTOCOL, IPFS.BITSWAP_PROTOCOL,
                IPFS.IDENTITY_PROTOCOL, IPFS.DHT_PROTOCOL, IPFS.RELAY_PROTOCOL_STOP);
    }

    public void shutdown() {
        try {
            if (server != null) {
                server.shutdown();
            }
        } catch (Throwable throwable) {
            LogUtils.error(TAG, throwable);
        } finally {
            server = null;
        }
    }

    public boolean swarmContains(@NonNull QuicConnection connection) {
        return swarm.contains(connection);
    }

    public void updateNetwork() {
        updateListenAddresses();
    }

    public void updateListenAddresses() {
        lock.lock();
        try {
            List<InetAddress> locals = new ArrayList<>();
            List<InetAddress> externals = new ArrayList<>();
            List<NetworkInterface> interfaces = Collections.list(
                    NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : interfaces) {

                List<InetAddress> addresses =
                        Collections.list(networkInterface.getInetAddresses());
                for (InetAddress inetAddress : addresses) {

                    if (!(inetAddress.isAnyLocalAddress() ||
                            inetAddress.isLinkLocalAddress() ||
                            inetAddress.isLoopbackAddress())) {

                        if (IPFS.PREFER_IPV6_PROTOCOL) {
                            if (inetAddress.isSiteLocalAddress()) {
                                locals.add(inetAddress);
                            } else {
                                externals.add(inetAddress);
                            }
                        } else {
                            externals.add(inetAddress);
                        }
                    }
                }

            }

            if (!externals.isEmpty()) {
                protocol.set(getProtocol(externals));
                addresses.addAll(externals);
            } else {
                protocol.set(getProtocol(locals));
                addresses.addAll(locals);
            }
        } catch (Throwable throwable) {
            LogUtils.error(TAG, throwable);
        } finally {
            lock.unlock();
        }
    }

    @NonNull
    private ProtocolSupport getProtocol(@NonNull List<InetAddress> addresses) {
        boolean ipv4 = false;
        boolean ipv6 = false;
        for (InetAddress inet : addresses) {
            if (inet instanceof Inet6Address) {
                ipv6 = true;
            } else {
                ipv4 = true;
            }
        }

        if (ipv4 && ipv6) {
            return ProtocolSupport.UNKNOWN;
        } else if (ipv4) {
            return ProtocolSupport.IPv4;
        } else if (ipv6) {
            return ProtocolSupport.IPv6;
        } else {
            return ProtocolSupport.UNKNOWN;
        }

    }

    public int getPort() {
        return port;
    }

    @NonNull
    public Reservation doReservation(@NonNull PeerId relayId, @NonNull Multiaddr multiaddr)
            throws Exception {

        if (!multiaddr.isSupported(protocol.get())) {
            throw new Exception("address is not supported");
        }

        QuicConnection conn = Dialer.dial(this, relayId, multiaddr,
                IPFS.CONNECT_TIMEOUT, IPFS.GRACE_PERIOD_RESERVATION,
                IPFS.MAX_STREAMS, IPFS.MESSAGE_SIZE_MAX);
        Objects.requireNonNull(conn);

        // check if RELAY protocols HOP is supported
        PeerInfo peerInfo = IdentityService.getPeerInfo(conn);

        if (!peerInfo.hasProtocol(IPFS.RELAY_PROTOCOL_HOP)) {
            conn.close();
            throw new Exception("does not support relay hop");
        }

        Multiaddr observed = peerInfo.getObserved();
        if (observed == null) {
            conn.close();
            throw new RuntimeException("does not return observed address");
        }

        Circuit.Reservation reservation = RelayService.reserve(conn);
        Reservation done = new Reservation(relayId, conn, multiaddr, observed, reservation);
        reservations.put(relayId, done);
        return done;
    }


    public RelayConnection createRelayConnection(@NonNull PeerId peerId,
                                                 @NonNull Multiaddr multiaddr) throws Exception {
        return RelayService.createRelayConnection(this, peerId, multiaddr,
                IPFS.CONNECT_TIMEOUT, IPFS.GRACE_PERIOD_RESERVATION, IPFS.MAX_STREAMS,
                IPFS.MESSAGE_SIZE_MAX);
    }

    public boolean hasReservations() {
        return reservations.size() > 0;
    }

    public void clearSwarm() {
        swarm.clear();
    }

}


