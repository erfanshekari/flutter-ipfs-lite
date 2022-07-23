package threads.lite.dht;

import android.annotation.SuppressLint;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;

import net.luminis.quic.QuicConnection;
import net.luminis.quic.QuicStream;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import dht.pb.Dht;
import threads.lite.IPFS;
import threads.lite.LogUtils;
import threads.lite.cid.Cid;
import threads.lite.cid.Multiaddr;
import threads.lite.cid.Peer;
import threads.lite.cid.PeerId;
import threads.lite.cid.Protocol;
import threads.lite.core.Closeable;
import threads.lite.core.RecordIssue;
import threads.lite.host.DnsResolver;
import threads.lite.host.LiteHost;
import threads.lite.ipns.Ipns;
import threads.lite.ipns.Validator;
import threads.lite.utils.DataHandler;
import threads.lite.utils.ReaderHandler;


public class KadDht implements Routing {

    private static final String TAG = KadDht.class.getSimpleName();
    public final LiteHost host;
    public final PeerId self;

    @NonNull
    public final RoutingTable routingTable;
    private final Validator validator;

    @NonNull
    private final ReentrantLock lock = new ReentrantLock();

    public KadDht(@NonNull LiteHost host, @NonNull Validator validator) {
        this.host = host;
        this.validator = validator;
        this.self = host.self();

        this.routingTable = new RoutingTable(ID.convertPeerID(self));
    }

    void bootstrap() {
        // Fill routing table with currently connected peers that are DHT servers
        if (routingTable.isEmpty()) {
            try {
                lock.lock();

                try {
                    Set<String> addresses = new HashSet<>(IPFS.DHT_BOOTSTRAP_NODES);

                    for (String multiAddress : addresses) {
                        try {
                            Multiaddr multiaddr = new Multiaddr(multiAddress);
                            String name = multiaddr.getStringComponent(Protocol.P2P);
                            Objects.requireNonNull(name);
                            PeerId peerId = PeerId.fromBase58(name);
                            Objects.requireNonNull(peerId);

                            Set<Multiaddr> result = DnsResolver.resolveDnsAddress(multiaddr);
                            peerFound(new Peer(peerId, result), false);

                        } catch (Throwable throwable) {
                            LogUtils.error(TAG, throwable);
                        }
                    }
                } catch (Throwable throwable) {
                    LogUtils.error(TAG, throwable);
                }

            } finally {
                lock.unlock();
            }
        }
    }

    void peerFound(Peer p, boolean isReplaceable) {
        try {
            routingTable.addPeer(p, isReplaceable);
        } catch (Throwable throwable) {
            LogUtils.error(TAG, throwable);
        }
    }


    @NonNull
    private Set<Peer> evalClosestPeers(@NonNull Dht.Message pms) {
        Set<Peer> peers = new HashSet<>();
        List<Dht.Message.Peer> list = pms.getCloserPeersList();
        for (Dht.Message.Peer entry : list) {
            PeerId peerId = new PeerId(entry.getId().toByteArray());


            Set<Multiaddr> multiAddresses = new HashSet<>();
            List<ByteString> addresses = entry.getAddrsList();
            for (ByteString address : addresses) {
                Multiaddr multiaddr = preFilter(address);
                if (multiaddr != null) {
                    if (multiaddr.isSupported(host.protocol.get())) {
                        multiAddresses.add(multiaddr);
                    }
                }
            }

            if (!multiAddresses.isEmpty()) {
                peers.add(new Peer(peerId, multiAddresses));
            } else {
                LogUtils.info(TAG, "Ignore evalClosestPeers : " + multiAddresses);
            }
        }
        return peers;
    }


    private void getClosestPeers(@NonNull Closeable closeable, @NonNull byte[] key,
                                 @NonNull Consumer<Peer> channel) throws InterruptedException {
        if (key.length == 0) {
            throw new RuntimeException("can't lookup empty key");
        }

        runQuery(closeable, key, (ctx1, p) -> {

            Dht.Message pms = findPeerSingle(ctx1, p, key);

            Set<Peer> peers = evalClosestPeers(pms);

            for (Peer peer : peers) {
                channel.accept(peer);
            }

            return peers;
        });


    }

    @Override
    public void putValue(@NonNull Closeable ctx, @NonNull byte[] key, @NonNull byte[] value) throws InterruptedException {

        bootstrap();

        // don't allow local users to put bad values.
        try {
            Ipns.Entry entry = validator.validate(key, value);
            Objects.requireNonNull(entry);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        long start = System.currentTimeMillis();

        @SuppressLint("SimpleDateFormat") String format = new SimpleDateFormat(
                IPFS.TIME_FORMAT_IPFS).format(new Date());
        Dht.Message.Record rec = Dht.Message.Record.newBuilder().setKey(ByteString.copyFrom(key))
                .setValue(ByteString.copyFrom(value))
                .setTimeReceived(format).build();

        ConcurrentSkipListSet<PeerId> handled = new ConcurrentSkipListSet<>();

        try {
            getClosestPeers(ctx, key, peer -> {
                if (!handled.contains(peer.getPeerId())) {
                    handled.add(peer.getPeerId());
                    putValueToPeer(ctx, peer, rec);
                }
            });
        } finally {
            LogUtils.verbose(TAG, "Finish putValue at " + (System.currentTimeMillis() - start));
        }

    }

    private void putValueToPeer(@NonNull Closeable ctx, @NonNull Peer peer,
                                @NonNull Dht.Message.Record rec) {

        try {
            Dht.Message pms = Dht.Message.newBuilder()
                    .setType(Dht.Message.MessageType.PUT_VALUE)
                    .setKey(rec.getKey())
                    .setRecord(rec)
                    .setClusterLevelRaw(0).build();

            Dht.Message rimes = sendRequest(ctx, peer, pms);

            if (!Arrays.equals(rimes.getRecord().getValue().toByteArray(),
                    pms.getRecord().getValue().toByteArray())) {
                throw new RuntimeException("value not put correctly put-message  " +
                        pms + " get-message " + rimes);
            }
            LogUtils.verbose(TAG, "PutValue Success to " + peer.getPeerId().toBase58());
        } catch (InterruptedException | ConnectException ignore) {
        } catch (Throwable throwable) {
            LogUtils.error(TAG, throwable);
        }

    }

    @Override
    public void findProviders(@NonNull Closeable closeable, @NonNull Consumer<Peer> providers,
                              @NonNull Cid cid) throws InterruptedException {
        if (!cid.isDefined()) {
            throw new RuntimeException("Cid invalid");
        }

        bootstrap();

        byte[] key = cid.getHash();

        long start = System.currentTimeMillis();

        try {

            runQuery(closeable, key, (ctx, p) -> {

                Dht.Message pms = findProvidersSingle(ctx, p, key);
                Set<Peer> result = evalClosestPeers(pms);

                List<Dht.Message.Peer> list = pms.getProviderPeersList();
                for (Dht.Message.Peer entry : list) {

                    PeerId peerId = new PeerId(entry.getId().toByteArray());

                    Set<Multiaddr> multiAddresses = new HashSet<>();
                    List<ByteString> addresses = entry.getAddrsList();
                    for (ByteString address : addresses) {
                        Multiaddr multiaddr = preFilter(address);
                        if (multiaddr != null) {
                            if (multiaddr.isSupported(host.protocol.get())) {
                                multiAddresses.add(multiaddr);
                            }
                        }
                    }

                    LogUtils.debug(TAG, "findProviders " + peerId.toBase58() +
                            " Cid Version : " + cid.getVersion() + " addr " +
                            multiAddresses);


                    providers.accept(new Peer(peerId, multiAddresses));

                }

                return result;

            });
        } finally {
            LogUtils.debug(TAG, "Finish findProviders at " +
                    (System.currentTimeMillis() - start));
        }
    }


    public void removeFromRouting(Peer p) {
        boolean result = routingTable.removePeer(p);
        if (result) {
            LogUtils.debug(TAG, "Remove from routing " + p.getPeerId().toBase58());
        }
    }


    private Dht.Message makeProvRecord(@NonNull byte[] key) {

        List<Multiaddr> addresses = host.listenAddresses(false);

        if (addresses.isEmpty()) {
            throw new RuntimeException("no known addresses for self, cannot put provider");
        }

        Dht.Message.Builder builder = Dht.Message.newBuilder()
                .setType(Dht.Message.MessageType.ADD_PROVIDER)
                .setKey(ByteString.copyFrom(key))
                .setClusterLevelRaw(0);

        Dht.Message.Peer.Builder peerBuilder = Dht.Message.Peer.newBuilder()
                .setId(ByteString.copyFrom(self.getBytes()));
        for (Multiaddr ma : addresses) {
            peerBuilder.addAddrs(ByteString.copyFrom(ma.getBytes()));
        }
        builder.addProviderPeers(peerBuilder.build());

        return builder.build();
    }

    @Override
    public void provide(@NonNull Closeable closeable, @NonNull Cid cid) throws InterruptedException {

        if (!cid.isDefined()) {
            throw new RuntimeException("invalid cid: undefined");
        }

        bootstrap();

        byte[] key = cid.getHash();

        final Dht.Message mes = makeProvRecord(key);

        ConcurrentSkipListSet<PeerId> handled = new ConcurrentSkipListSet<>();
        getClosestPeers(closeable, key, peer -> {
            if (!handled.contains(peer.getPeerId())) {
                handled.add(peer.getPeerId());
                sendMessage(closeable, peer, mes);
            }
        });

    }

    private void sendMessage(@NonNull Closeable closeable, @NonNull Peer peer,
                             @NonNull Dht.Message message) {


        QuicConnection conn = null;
        try {
            if (closeable.isClosed()) {
                return;
            }
            conn = connect(peer);

            if (closeable.isClosed()) {
                return;
            }
            sendMessage(conn, message);
        } catch (ExecutionException | InterruptedException | TimeoutException | IOException ignore) {
            // ignore any exception
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    private void sendMessage(@NonNull QuicConnection conn, @NonNull Dht.Message message)
            throws ExecutionException, InterruptedException, TimeoutException, IOException {
        long time = System.currentTimeMillis();
        boolean success = false;
        try {

            QuicStream quicStream = conn.createStream(true);

            OutputStream outputStream = quicStream.getOutputStream();
            outputStream.write(DataHandler.writeToken(IPFS.STREAM_PROTOCOL, IPFS.DHT_PROTOCOL));

            CompletableFuture<Boolean> done = new CompletableFuture<>();

            ReaderHandler.reading(quicStream,
                    (token) -> {
                        if (!Arrays.asList(IPFS.STREAM_PROTOCOL, IPFS.DHT_PROTOCOL).contains(token)) {
                            throw new RuntimeException("Token " + token + " not supported");
                        }
                        try {
                            if (Objects.equals(token, IPFS.DHT_PROTOCOL)) {
                                outputStream.write(DataHandler.encode(message));
                                outputStream.close();
                                done.complete(true);
                            }
                        } catch (Throwable throwable) {
                            throw new RuntimeException(throwable);
                        }
                    },
                    (data) -> done.complete(true),
                    (fin) -> done.complete(true),
                    done::completeExceptionally);

            success = done.get(IPFS.DHT_SEND_READ_TIMEOUT, TimeUnit.SECONDS);
        } finally {
            LogUtils.debug(TAG, "Send " + success + " took " +
                    (System.currentTimeMillis() - time));
        }
    }

    private QuicConnection connect(@NonNull Peer peer) throws ConnectException, InterruptedException {

        return host.connect(peer, IPFS.CONNECT_TIMEOUT, IPFS.GRACE_PERIOD,
                0, 20480);

    }

    private Dht.Message sendRequest(@NonNull Closeable closeable, @NonNull Peer peer,
                                    @NonNull Dht.Message message)
            throws InterruptedException, ConnectException {


        long time = System.currentTimeMillis();
        boolean success = false;

        if (closeable.isClosed()) {
            throw new InterruptedException();
        }

        QuicConnection conn = connect(peer);

        if (closeable.isClosed()) {
            throw new InterruptedException();
        }

        try {

            time = System.currentTimeMillis();

            QuicStream quicStream = conn.createStream(true);

            OutputStream outputStream = quicStream.getOutputStream();

            outputStream.write(DataHandler.writeToken(IPFS.STREAM_PROTOCOL, IPFS.DHT_PROTOCOL));

            CompletableFuture<Dht.Message> store = new CompletableFuture<>();

            ReaderHandler.reading(quicStream, (token) -> {
                if (!Arrays.asList(IPFS.STREAM_PROTOCOL, IPFS.DHT_PROTOCOL).contains(token)) {
                    throw new RuntimeException("Token " + token + " not supported");
                }
                try {
                    if (Objects.equals(token, IPFS.DHT_PROTOCOL)) {
                        outputStream.write(DataHandler.encode(message));
                        outputStream.close();
                    }
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }, (data) -> {
                try {
                    store.complete(Dht.Message.parseFrom(data));
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }, store::completeExceptionally);

            Dht.Message msg = store.get(IPFS.DHT_REQUEST_READ_TIMEOUT, TimeUnit.SECONDS);
            Objects.requireNonNull(msg);
            success = true;
            peer.setLatency(System.currentTimeMillis() - time);

            return msg;
        } catch (ExecutionException | TimeoutException | IOException exception) {
            LogUtils.debug(TAG, "Request " + exception.getClass().getSimpleName() +
                    " : " + exception.getMessage());
            throw new ConnectException(exception.getClass().getSimpleName());

        } finally {
            conn.close();
            LogUtils.debug(TAG, "Request " + success + " took " +
                    (System.currentTimeMillis() - time));
        }
    }


    private Dht.Message getValueSingle(@NonNull Closeable ctx, @NonNull Peer p, @NonNull byte[] key)
            throws InterruptedException, ConnectException {
        Dht.Message pms = Dht.Message.newBuilder()
                .setType(Dht.Message.MessageType.GET_VALUE)
                .setKey(ByteString.copyFrom(key))
                .setClusterLevelRaw(0).build();
        return sendRequest(ctx, p, pms);
    }

    private Dht.Message findPeerSingle(@NonNull Closeable ctx, @NonNull Peer p, @NonNull byte[] key)
            throws InterruptedException, ConnectException {
        Dht.Message pms = Dht.Message.newBuilder()
                .setType(Dht.Message.MessageType.FIND_NODE)
                .setKey(ByteString.copyFrom(key))
                .setClusterLevelRaw(0).build();

        return sendRequest(ctx, p, pms);
    }

    private Dht.Message findProvidersSingle(@NonNull Closeable ctx, @NonNull Peer p, @NonNull byte[] key)
            throws InterruptedException, ConnectException {
        Dht.Message pms = Dht.Message.newBuilder()
                .setType(Dht.Message.MessageType.GET_PROVIDERS)
                .setKey(ByteString.copyFrom(key))
                .setClusterLevelRaw(0).build();
        return sendRequest(ctx, p, pms);
    }


    @Nullable
    private Multiaddr preFilter(@NonNull ByteString address) {
        try {
            return new Multiaddr(address.toByteArray());
        } catch (Throwable ignore) {
            LogUtils.error(TAG, address.toStringUtf8());
        }
        return null;
    }

    @Override
    public void findPeer(@NonNull Closeable closeable, @NonNull Consumer<Peer> consumer, @NonNull PeerId id) throws InterruptedException {

        bootstrap();

        byte[] key = id.getBytes();
        long start = System.currentTimeMillis();
        try {
            runQuery(closeable, key, (ctx, p) -> {

                Dht.Message pms = findPeerSingle(ctx, p, key);

                Set<Peer> peers = evalClosestPeers(pms);
                for (Peer peer : peers) {
                    if (Objects.equals(peer.getPeerId(), id)) {
                        LogUtils.debug(TAG, "findPeer " + peer.getPeerId().toBase58() + " " +
                                peer.getMultiaddrs());
                        consumer.accept(peer);
                    }
                }

                return peers;

            });
        } finally {
            LogUtils.debug(TAG, "Finish findPeer " + id.toBase58() +
                    " at " + (System.currentTimeMillis() - start));
        }
    }

    private void runQuery(@NonNull Closeable ctx, @NonNull byte[] target,
                          @NonNull QueryFunc queryFn) throws InterruptedException {
        // pick the K closest peers to the key in our Routing table.
        ID targetKadID = ID.convertKey(target);
        List<Peer> seedPeers = routingTable.NearestPeers(targetKadID, IPFS.DHT_BUCKET_SIZE);
        if (seedPeers.size() == 0) {
            return;
        }

        Query q = new Query(this);

        q.runQuery(ctx, target, seedPeers, queryFn);

    }

    private Pair<Ipns.Entry, Set<Peer>> getValueOrPeers(
            @NonNull Closeable ctx, @NonNull Peer p, @NonNull byte[] key)
            throws InterruptedException, ConnectException {


        Dht.Message pms = getValueSingle(ctx, p, key);

        Set<Peer> peers = evalClosestPeers(pms);

        if (pms.hasRecord()) {

            Dht.Message.Record rec = pms.getRecord();
            try {
                byte[] record = rec.getValue().toByteArray();
                if (record != null && record.length > 0) {
                    Ipns.Entry entry = validator.validate(rec.getKey().toByteArray(), record);
                    return Pair.create(entry, peers);
                }
            } catch (RecordIssue issue) {
                LogUtils.debug(TAG, issue.getMessage());
            } catch (Throwable throwable) {
                LogUtils.error(TAG, throwable);
            }
        }

        if (peers.size() > 0) {
            return Pair.create(null, peers);
        }
        return Pair.create(null, Collections.emptySet());
    }

    private void getValues(@NonNull Closeable ctx, @NonNull Consumer<Ipns.Entry> consumer,
                           @NonNull byte[] key) throws InterruptedException {


        runQuery(ctx, key, (ctx1, p) -> {

            Pair<Ipns.Entry, Set<Peer>> result = getValueOrPeers(ctx1, p, key);
            Ipns.Entry entry = result.first;
            Set<Peer> peers = result.second;

            if (entry != null) {
                consumer.accept(entry);
            }

            return peers;
        });

    }


    private void processValues(@Nullable Ipns.Entry best,
                               @NonNull Ipns.Entry current,
                               @NonNull Consumer<Ipns.Entry> reporter) {

        if (best != null) {
            int value = validator.compare(best, current);
            if (value == -1) { // "current" is newer entry
                reporter.accept(current);
            }
        } else {
            reporter.accept(current);
        }
    }


    @Override
    public void searchValue(@NonNull Closeable closeable, @NonNull Consumer<Ipns.Entry> consumer,
                            @NonNull byte[] key) throws InterruptedException {

        bootstrap();

        AtomicReference<Ipns.Entry> best = new AtomicReference<>();
        long start = System.currentTimeMillis();
        try {
            getValues(closeable, entry -> processValues(best.get(), entry, (current) -> {
                consumer.accept(current);
                best.set(current);
            }), key);
        } finally {
            LogUtils.info(TAG, "Finish searchValue at " + (System.currentTimeMillis() - start));
        }
    }


    public interface QueryFunc {
        @NonNull
        Set<Peer> query(@NonNull Closeable closeable, @NonNull Peer peer)
                throws InterruptedException, ConnectException;
    }


}
