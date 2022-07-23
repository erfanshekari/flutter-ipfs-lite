package threads.lite.dht;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import threads.lite.cid.Peer;


public class Bucket {

    private final ConcurrentHashMap<Peer, PeerInfo> peers = new ConcurrentHashMap<>();
    @NonNull
    private final ReentrantLock lock = new ReentrantLock();

    public boolean containsPeer(@NonNull Peer peer) {
        return peers.containsKey(peer);
    }


    @Nullable
    public Peer weakest() {
        if (size() == 0) {
            return null;
        }
        long latency = 0;
        Peer found = null;
        for (Map.Entry<Peer, PeerInfo> entry : peers.entrySet()) {
            PeerInfo info = entry.getValue();
            Peer peer = entry.getKey();
            if (info.isReplaceable()) {
                long tmp = peer.getLatency();

                if (tmp >= latency) {
                    latency = tmp;
                    found = peer;
                }

                if (tmp == Long.MAX_VALUE) {
                    break;
                }
            }
        }
        return found;
    }


    @NonNull
    @Override
    public String toString() {
        return "Bucket{" +
                "peers=" + peers.size() +
                '}';
    }

    public void addPeer(@NonNull Peer peer, boolean isReplaceable) {
        lock.lock();
        try {
            if (!peers.containsKey(peer)) {
                Bucket.PeerInfo peerInfo = new Bucket.PeerInfo(peer, isReplaceable);
                peers.put(peer, peerInfo);
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean removePeer(@NonNull Peer p) {
        PeerInfo peerInfo = peers.get(p);
        if (peerInfo != null) {
            if (peerInfo.isReplaceable()) {
                return peers.remove(p) != null;
            }
        }
        return false;
    }

    public int size() {
        return peers.size();
    }


    @NonNull
    public Collection<PeerInfo> values() {
        return peers.values();
    }


    public static class PeerInfo {
        @NonNull
        private final Peer peer;
        @NonNull
        private final ID id;
        // if a bucket is full, this peer can be replaced to make space for a new peer.
        private final boolean replaceable;

        public PeerInfo(@NonNull Peer peer, boolean replaceable) {
            this.peer = peer;
            this.id = ID.convertPeerID(peer.getPeerId());
            this.replaceable = replaceable;
        }

        public boolean isReplaceable() {
            return replaceable;
        }

        @NonNull
        @Override
        public String toString() {
            return "PeerInfo{" +
                    "peer=" + peer +
                    ", replaceable=" + replaceable +
                    '}';
        }

        @NonNull
        public Peer getPeer() {
            return peer;
        }

        @NonNull
        public ID getID() {
            return id;
        }

    }
}
