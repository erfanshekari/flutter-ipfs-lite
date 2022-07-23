package threads.lite.dht;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import threads.lite.cid.Peer;

public class PeerDistanceSorter extends ArrayList<PeerDistanceSorter.PeerDistance> {
    private final ID target;

    public PeerDistanceSorter(@NonNull ID target) {
        this.target = target;
    }

    @NonNull
    @Override
    public String toString() {
        return "PeerDistanceSorter{" +
                "target=" + target +
                '}';
    }

    public void appendPeer(@NonNull Peer peer, @NonNull ID id) {
        this.add(new PeerDistance(peer, ID.xor(target, id)));
    }

    public void appendPeersFromList(@NonNull Bucket bucket) {
        for (Bucket.PeerInfo peerInfo : bucket.values()) {
            appendPeer(peerInfo.getPeer(), peerInfo.getID());
        }
    }

    public static class PeerDistance implements Comparable<PeerDistance> {
        private final Peer peer;
        private final ID distance;

        protected PeerDistance(@NonNull Peer peer, @NonNull ID distance) {
            this.peer = peer;
            this.distance = distance;
        }

        @NonNull
        @Override
        public String toString() {
            return "PeerDistance{" +
                    "peerId=" + peer +
                    ", distance=" + distance +
                    '}';
        }

        @Override
        public int compareTo(@NonNull PeerDistance o) {
            return this.distance.compareTo(o.distance);
        }

        @NonNull
        public Peer getPeer() {
            return peer;
        }
    }
}
