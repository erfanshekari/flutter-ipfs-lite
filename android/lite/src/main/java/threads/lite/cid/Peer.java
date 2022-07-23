package threads.lite.cid;

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public final class Peer {
    private final PeerId peerId;
    private final Set<Multiaddr> multiaddrs;
    private long latency = Long.MAX_VALUE;


    public Peer(@NonNull PeerId peerId, @NonNull Multiaddr multiaddr) {
        this.peerId = peerId;
        this.multiaddrs = new TreeSet<>();
        this.multiaddrs.add(multiaddr);
    }

    public Peer(@NonNull PeerId peerId, @NonNull Set<Multiaddr> multiaddrs) {
        this.peerId = peerId;
        this.multiaddrs = multiaddrs;
    }

    public long getLatency() {
        return latency;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public PeerId getPeerId() {
        return peerId;
    }

    public Set<Multiaddr> getMultiaddrs() {
        return multiaddrs;
    }

    public boolean hasAddresses() {
        return !multiaddrs.isEmpty();
    }

    @NonNull
    @Override
    public String toString() {
        return "Peer{" +
                "peerId=" + peerId +
                ", multiaddrs=" + multiaddrs +
                ", latency=" + latency +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peer peer = (Peer) o;
        return peerId.equals(peer.peerId) && multiaddrs.equals(peer.multiaddrs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peerId, multiaddrs);
    }
}
