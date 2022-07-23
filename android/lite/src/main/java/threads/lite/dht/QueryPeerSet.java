package threads.lite.dht;

import androidx.annotation.NonNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import threads.lite.IPFS;
import threads.lite.LogUtils;
import threads.lite.cid.Peer;
import threads.lite.cid.PeerId;

public class QueryPeerSet extends ConcurrentHashMap<Peer, QueryPeerState> {

    private final ID key;

    private QueryPeerSet(@NonNull ID key) {
        this.key = key;
    }

    public static QueryPeerSet create(@NonNull byte[] key) {
        return new QueryPeerSet(ID.convertKey(key));
    }

    private BigInteger distanceToKey(@NonNull PeerId peerId) {
        return Util.Distance(ID.convertPeerID(peerId), key);
    }

    // TryAdd adds the peer p to the peer set.
    // If the peer is already present, no action is taken.
    // Otherwise, the peer is added with state set to PeerHeard.
    public void tryAdd(@NonNull Peer peer) {
        QueryPeerState peerSet = get(peer);
        if (peerSet == null) {
            put(peer, new QueryPeerState(peer, distanceToKey(peer.getPeerId())));
        }
    }

    public void setState(@NonNull Peer peer, @NonNull PeerState peerState) {
        Objects.requireNonNull(get(peer)).setState(peerState);
    }


    // GetClosestNInStates returns the closest to the key peers, which are in one of the given states.
    // It returns n peers or less, if fewer peers meet the condition.
    // The returned peers are sorted in ascending order by their distance to the key.
    public List<QueryPeerState> getClosestNInStates(int maxLength, @NonNull List<PeerState> states) {
        if (LogUtils.isDebug()) {
            if (maxLength < 0) {
                throw new RuntimeException("internal state error");
            }
        }
        List<QueryPeerState> list = new ArrayList<>(values());
        Collections.sort(list);

        List<QueryPeerState> peers = new ArrayList<>();
        int count = 0;
        for (QueryPeerState state : list) {
            if (states.contains(state.getState())) {
                peers.add(state);
                count++;
                if (count == maxLength) {
                    break;
                }
            }
        }
        Collections.sort(peers);

        return peers;
    }

    @NonNull
    List<QueryPeerState> getClosestInStates(int maxLength, @NonNull List<PeerState> states) {
        return getClosestNInStates(maxLength, states);
    }

    public int numWaitingOrHeard() {
        return getClosestInStates(size(),
                List.of(PeerState.PeerHeard, PeerState.PeerWaiting)).size();
    }

    public List<Peer> nextHeardPeers() {

        // The peers we query next should be ones that we have only Heard about.
        List<Peer> peersToQuery = new ArrayList<>();
        List<QueryPeerState> peers = getClosestInStates(
                IPFS.DHT_ALPHA, Collections.singletonList(PeerState.PeerHeard));

        for (QueryPeerState p : peers) {
            peersToQuery.add(p.peer);
        }

        return peersToQuery;
    }
}
