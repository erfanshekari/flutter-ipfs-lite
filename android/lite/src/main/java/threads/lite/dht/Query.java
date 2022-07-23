package threads.lite.dht;

import androidx.annotation.NonNull;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import threads.lite.LogUtils;
import threads.lite.cid.Peer;
import threads.lite.core.Closeable;

public class Query {

    private static final String TAG = Query.class.getSimpleName();

    @NonNull
    private final KadDht dht;

    public Query(@NonNull KadDht dht) {
        this.dht = dht;
    }

    public void runQuery(@NonNull Closeable closeable, @NonNull byte[] key,
                         @NonNull List<Peer> seedPeers, @NonNull KadDht.QueryFunc queryFn)
            throws InterruptedException {

        QueryPeerSet queryPeers = QueryPeerSet.create(key);

        ExecutorService executor =
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        QueryUpdate update = new QueryUpdate();
        update.addAll(seedPeers);

        LinkedBlockingQueue<QueryUpdate> queue = new LinkedBlockingQueue<>();
        queue.offer(update);

        try {
            while (true) {

                QueryUpdate current = queue.take();

                if (closeable.isClosed()) {
                    throw new InterruptedException();
                }

                // the peers in query update are added to the queryPeers
                for (Peer peer : current) {
                    if (Objects.equals(peer.getPeerId(), dht.self)) { // don't add self.
                        continue;
                    }
                    queryPeers.tryAdd(peer);  // set initial state to PeerHeard
                }

                boolean result = queryPeers.numWaitingOrHeard() == 0 && queue.isEmpty();
                if (result) {
                    LogUtils.error(TAG, "Starvation Termination " + queryPeers.size());
                    break;
                }

                List<Peer> nextPeersToQuery = queryPeers.nextHeardPeers();

                // try spawning the queries, if there are no available peers to query then we won't spawn them
                for (Peer queryPeer : nextPeersToQuery) {
                    queryPeers.setState(queryPeer, PeerState.PeerWaiting);
                    executor.execute(() -> {
                        try {
                            Set<Peer> newPeers = queryFn.query(closeable, queryPeer);

                            // query successful, try to add to routing table
                            dht.peerFound(queryPeer, true);
                            queryPeers.setState(queryPeer, PeerState.PeerQueried);

                            QueryUpdate queryUpdate = new QueryUpdate();
                            queryUpdate.addAll(newPeers);
                            queue.offer(queryUpdate);

                        } catch (InterruptedException interruptedException) {
                            queue.clear();
                            queue.offer(new QueryUpdate());
                        } catch (ConnectException ignore) {
                            dht.removeFromRouting(queryPeer);
                            queryPeers.setState(queryPeer, PeerState.PeerUnreachable);
                            queue.offer(new QueryUpdate());
                        } catch (Throwable throwable) {
                            LogUtils.error(TAG, throwable);
                            dht.removeFromRouting(queryPeer);
                            queryPeers.setState(queryPeer, PeerState.PeerUnreachable);
                            queue.offer(new QueryUpdate());
                        }
                    });
                }
            }
        } finally {
            executor.shutdown();
            executor.shutdownNow();
        }
    }


    public static class QueryUpdate extends ArrayList<Peer> {
    }

}
