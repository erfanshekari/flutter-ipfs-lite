package threads.lite.host;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import threads.lite.IPFS;
import threads.lite.cid.Multiaddr;

public class PeerInfo {

    @NonNull
    private final String agent;
    @NonNull
    private final String version;
    @NonNull
    private final List<Multiaddr> addresses;
    @NonNull
    private final List<String> protocols;
    @Nullable
    private final Multiaddr observed;

    public PeerInfo(@NonNull String agent,
                    @NonNull String version,
                    @NonNull List<Multiaddr> addresses,
                    @NonNull List<String> protocols,
                    @Nullable Multiaddr observed) {
        this.agent = agent;
        this.version = version;
        this.addresses = addresses;
        this.protocols = protocols;
        this.observed = observed;
    }

    @NonNull
    public String getVersion() {
        return version;
    }

    @NonNull
    public List<String> getProtocols() {
        return protocols;
    }

    @Nullable
    public Multiaddr getObserved() {
        return observed;
    }

    @NonNull
    public List<Multiaddr> getAddresses() {
        return addresses;
    }

    @NonNull
    @Override
    public String toString() {
        return "PeerInfo{" +
                ", agent='" + agent + '\'' +
                ", version='" + version + '\'' +
                ", addresses=" + addresses +
                ", protocols=" + protocols +
                ", observed=" + observed +
                '}';
    }

    @NonNull
    public String getAgent() {
        return agent;
    }

    public boolean hasProtocol(@NonNull String protocol) {
        return protocols.contains(protocol);
    }

    public boolean hasRelayHop() {
        return hasProtocol(IPFS.RELAY_PROTOCOL_HOP);
    }
}
