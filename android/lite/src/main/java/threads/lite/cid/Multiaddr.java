package threads.lite.cid;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;


public final class Multiaddr implements Comparable<Multiaddr> {
    private final String address;

    public Multiaddr(String address) {
        this.address = address;
    }

    public Multiaddr(byte[] raw) {
        this.address = encodeToString(raw); // check validity
    }

    private static byte[] decodeFromString(String addr) {
        while (addr.endsWith("/"))
            addr = addr.substring(0, addr.length() - 1);
        String[] parts = addr.split("/");
        if (parts[0].length() != 0)
            throw new IllegalStateException("MultiAddress must start with a /");

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            for (int i = 1; i < parts.length; ) {
                String part = parts[i++];
                Protocol p = Protocol.get(part);
                p.appendCode(bout);
                if (p.size() == 0)
                    continue;

                String component = p.isTerminal() ?
                        Stream.of(Arrays.copyOfRange(parts, i, parts.length)).reduce("", (a, b) -> a + "/" + b) :
                        parts[i++];
                if (component.length() == 0)
                    throw new IllegalStateException("Protocol requires address, but non provided!");

                bout.write(p.addressToBytes(component));
                if (p.isTerminal())
                    break;
            }
            return bout.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Error decoding multiaddress: " + addr);
        }
    }

    private static String encodeToString(byte[] raw) {
        StringBuilder b = new StringBuilder();
        InputStream in = new ByteArrayInputStream(raw);
        try {
            while (in.available() > 0) {
                int code = (int) Protocol.readVarint(in);
                Protocol p = Protocol.get(code);
                b.append("/").append(p.getType());
                if (p.size() == 0)
                    continue;

                String addr = p.readAddress(in);
                if (addr.length() > 0)
                    b.append("/").append(addr);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return b.toString();
    }

    @NonNull
    public static Multiaddr transform(@NonNull InetSocketAddress inetSocketAddress) {

        InetAddress inetAddress = inetSocketAddress.getAddress();
        boolean ipv6 = inetAddress instanceof Inet6Address;
        int port = inetSocketAddress.getPort();
        String multiaddress = "";
        if (ipv6) {
            multiaddress = multiaddress.concat("/ip6/");
        } else {
            multiaddress = multiaddress.concat("/ip4/");
        }
        multiaddress = multiaddress + inetAddress.getHostAddress() + "/udp/" + port + "/quic";
        return new Multiaddr(multiaddress);

    }

    private boolean isAnyLocalAddress() {
        try {
            InetAddress inetAddress = InetAddress.getByName(getHost());
            if (inetAddress.isAnyLocalAddress() || inetAddress.isLinkLocalAddress()
                    || (inetAddress.isLoopbackAddress())
                    || (inetAddress.isSiteLocalAddress())) {
                return true;
            }
        } catch (Throwable ignore) {
            return false;
        }
        return false;
    }

    public boolean isSupported(@NonNull ProtocolSupport protocolSupport) {
        Set<Protocol> protocols = getProtocols();
        if (protocolSupport == ProtocolSupport.IPv4) {
            if (protocols.contains(Protocol.IP6)) {
                return false;
            }
        }
        if (protocolSupport == ProtocolSupport.IPv6) {
            if (protocols.contains(Protocol.IP4)) {
                return false;
            }
        }
        if (protocols.contains(Protocol.DNSADDR)) {
            return true;
        }
        if (protocols.contains(Protocol.DNS)) {
            return true;
        }
        if (protocols.contains(Protocol.DNS4)) {
            return true;
        }
        if (protocols.contains(Protocol.DNS6)) {
            return true;
        }
        if (protocols.contains(Protocol.P2PCIRCUIT)) {
            return false;
        }
        if (protocols.contains(Protocol.QUIC)) {
            return !isAnyLocalAddress();
        }

        return false;
    }

    public byte[] getBytes() {
        return decodeFromString(address);
    }

    public String getHost() {
        String[] parts = toString().substring(1).split("/");
        if (parts[0].startsWith("ip") || parts[0].startsWith("dns"))
            return parts[1];
        throw new IllegalStateException("This multiaddress doesn't have a host: " + this);
    }

    public int getPort() {
        String[] parts = toString().substring(1).split("/");
        if (parts[2].startsWith("tcp") || parts[2].startsWith("udp"))
            return Integer.parseInt(parts[3]);
        throw new IllegalStateException("This multiaddress doesn't have a port: " + this);
    }

    @NonNull
    @Override
    public String toString() {
        return address;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Multiaddr))
            return false;
        return Objects.equals(address, ((Multiaddr) other).address);
    }

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    @Nullable
    public String getStringComponent(Protocol type) {
        String[] tokens = address.split("/");
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (Objects.equals(token, type.getType())) {
                return tokens[i + 1];
            }
        }
        return null;
    }

    @NonNull
    public Set<Protocol> getProtocols() {
        Set<Protocol> protocols = new HashSet<>();
        String[] tokens = address.split("/");
        for (String token : tokens) {
            if (Protocol.has(token)) {
                protocols.add(Protocol.get(token));
            }
        }
        return protocols;
    }

    public boolean has(@NonNull Protocol type) {
        String[] tokens = address.split("/");
        for (String token : tokens) {
            if (Objects.equals(token, type.getType())) {
                return true;
            }
        }
        return false;
    }

    public boolean isIP4() {
        return address.startsWith("/ip4/");
    }

    public boolean isCircuitAddress() {
        return this.has(Protocol.P2PCIRCUIT);
    }

    @Override
    public int compareTo(Multiaddr multiaddr) {
        return address.compareTo(multiaddr.address);
    }
}
