package threads.lite.dht;

import androidx.annotation.NonNull;

import java.math.BigInteger;

import threads.lite.cid.Peer;

public class QueryPeerState implements Comparable<QueryPeerState> {

    @NonNull
    public final Peer peer;
    @NonNull
    public final BigInteger distance;
    @NonNull
    private PeerState state;

    public QueryPeerState(@NonNull Peer peer, @NonNull BigInteger distance) {
        this.peer = peer;
        this.distance = distance;
        this.state = PeerState.PeerHeard;
    }

    @NonNull
    public PeerState getState() {
        return state;
    }

    public void setState(@NonNull PeerState state) {
        this.state = state;
    }

    @NonNull
    @Override
    public String toString() {
        return "QueryPeerState{" +
                "id=" + peer +
                ", distance=" + distance +
                ", state=" + state +
                '}';
    }

    @Override
    public int compareTo(QueryPeerState o) {
        return distance.compareTo(o.distance);
    }

}
