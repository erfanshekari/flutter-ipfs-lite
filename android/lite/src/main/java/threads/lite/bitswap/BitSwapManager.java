package threads.lite.bitswap;

import androidx.annotation.NonNull;

import net.luminis.quic.QuicConnection;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import threads.lite.IPFS;
import threads.lite.LogUtils;
import threads.lite.cid.Cid;
import threads.lite.cid.Peer;
import threads.lite.core.Closeable;
import threads.lite.format.Block;
import threads.lite.format.BlockStore;
import threads.lite.host.LiteHost;


public class BitSwapManager {

    private static final String TAG = BitSwapManager.class.getSimpleName();

    @NonNull
    private final LiteHost host;
    @NonNull
    private final BlockStore blockStore;
    @NonNull
    private final BitSwap bitSwap;
    @NonNull
    private final Set<QuicConnection> connections = ConcurrentHashMap.newKeySet();
    @NonNull
    private final ConcurrentHashMap<Cid, ConcurrentLinkedDeque<QuicConnection>> matches = new ConcurrentHashMap<>();


    public BitSwapManager(@NonNull BitSwap bitSwap, @NonNull BlockStore blockStore, @NonNull LiteHost host) {
        this.bitSwap = bitSwap;
        this.blockStore = blockStore;
        this.host = host;
    }

    public void haveReceived(@NonNull QuicConnection conn, @NonNull List<Cid> cids) {

        for (Cid cid : cids) {
            ConcurrentLinkedDeque<QuicConnection> res = matches.get(cid);
            if (res != null) {
                res.add(conn);
            }
        }
    }

    public void reset() {

        LogUtils.debug(TAG, "Reset");
        try {
            connections.clear();
            matches.clear();
        } catch (Throwable throwable) {
            LogUtils.error(TAG, throwable);
        }
    }

    private void connectPeer(@NonNull Closeable closeable, @NonNull Peer peer) {
        try {
            synchronized (peer.getPeerId().toBase58().intern()) {
                if (closeable.isClosed()) {
                    return;
                }

                QuicConnection conn = host.connect(peer, IPFS.CONNECT_TIMEOUT, IPFS.GRACE_PERIOD,
                        IPFS.MAX_STREAMS, IPFS.MESSAGE_SIZE_MAX);

                LogUtils.debug(TAG, "New connection " + peer.getPeerId().toBase58());

                if (closeable.isClosed()) {
                    return;
                }

                connections.add(conn);
            }

        } catch (Throwable ignore) {
            // ignore
        }
    }

    public Block runWantHaves(@NonNull Closeable closeable, @NonNull Cid cid) throws InterruptedException {

        ExecutorService executor =
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            if (IPFS.BITSWAP_SUPPORT_FIND_PROVIDERS) {

                executor.execute(() -> {

                    long start = System.currentTimeMillis();
                    try {
                        LogUtils.debug(TAG, "Load Provider Start " + cid.String());

                        if (closeable.isClosed()) {
                            return;
                        }
                        Set<Peer> handled = ConcurrentHashMap.newKeySet();
                        host.findProviders(closeable, (peer) -> {
                            if (peer.hasAddresses()) {
                                if (!handled.contains(peer)) {
                                    handled.add(peer);
                                    if (!executor.isShutdown()) {
                                        executor.execute(() -> connectPeer(closeable, peer));
                                    }
                                }
                            }
                        }, cid);
                    } catch (InterruptedException ignore) {
                        // nothing to do here
                    } catch (Throwable throwable) {
                        LogUtils.error(TAG, throwable);
                    } finally {
                        LogUtils.info(TAG, "Load Provider Finish " + cid.String() +
                                " onStart [" + (System.currentTimeMillis() - start) + "]...");
                    }
                });
            }

            Set<QuicConnection> haves = new HashSet<>();

            while (matches.containsKey(cid)) {

                if (closeable.isClosed()) {
                    throw new InterruptedException();
                }

                for (QuicConnection conn : host.getSwarm()) {
                    if (!haves.contains(conn)) {
                        haves.add(conn);
                        bitSwap.sendHaveMessage(conn, Collections.singletonList(cid));
                    }
                }

                for (QuicConnection conn : connections) {
                    if (!haves.contains(conn)) {
                        haves.add(conn);
                        bitSwap.sendHaveMessage(conn, Collections.singletonList(cid));
                    }
                }

                ConcurrentLinkedDeque<QuicConnection> set = matches.get(cid);
                if (set != null) {
                    QuicConnection conn = set.poll();
                    if (conn != null) {
                        long start = System.currentTimeMillis();
                        try {
                            bitSwap.sendWantsMessage(conn, Collections.singletonList(cid));
                        } catch (Throwable throwable) {
                            LogUtils.error(TAG, throwable);
                        } finally {
                            LogUtils.debug(TAG, "Match CID " + cid.String() +
                                    " took " + (System.currentTimeMillis() - start));
                        }
                    }

                }

                if (closeable.isClosed()) {
                    throw new InterruptedException();
                }

            }
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }

        return blockStore.getBlock(cid);
    }


    public void blockReceived(@NonNull Block block) {

        Cid cid = block.getCid();
        if (matches.containsKey(cid)) {
            blockStore.putBlock(block);
            matches.remove(cid);
        }
    }

    public Block getBlock(@NonNull Closeable closeable, @NonNull Cid cid) throws InterruptedException {

        try {
            Block block = blockStore.getBlock(cid);
            if (block == null) {
                AtomicBoolean done = new AtomicBoolean(false);
                LogUtils.info(TAG, "Block Get " + cid.String());

                try {
                    matches.put(cid, new ConcurrentLinkedDeque<>());
                    return runWantHaves(() -> closeable.isClosed() || done.get(), cid);
                } finally {
                    done.set(true);
                    matches.remove(cid); // just safety
                }
            }
            return block;

        } finally {
            LogUtils.info(TAG, "Block Release  " + cid.String());
        }
    }
}
