package threads.lite.cid;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public enum Protocol {

    IP4(4, 32, "ip4"),
    TCP(6, 16, "tcp"),
    DCCP(33, 16, "dccp"),
    IP6(41, 128, "ip6"),
    DNS(53, -1, "dns"),
    DNS4(54, -1, "dns4"),
    DNS6(55, -1, "dns6"),
    DNSADDR(56, -1, "dnsaddr"),
    SCTP(132, 16, "sctp"),
    UDP(273, 16, "udp"),
    UTP(301, 0, "utp"),
    UDT(302, 0, "udt"),
    UNIX(400, -1, "unix"),
    P2P(421, -1, "p2p"),
    IPFS(421, -1, "ipfs"),
    HTTPS(443, 0, "https"),
    ONION(444, 80, "onion"),
    ONION3(445, 296, "onion3"),
    QUIC(460, 0, "quic"),
    WS(477, 0, "ws"),
    WSS(478, 0, "wss"),
    P2PCIRCUIT(290, 0, "p2p-circuit"),
    HTTP(480, 0, "http");

    public static final String IPV4_REGEX = "\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z";
    private static final Map<String, Protocol> byName = new HashMap<>();
    private static final Map<Integer, Protocol> byCode = new HashMap<>();

    static {
        for (Protocol t : Protocol.values()) {
            byName.put(t.getType(), t);
            byCode.put(t.code(), t);
        }

    }

    private final int code, size;
    private final String type;
    private final byte[] encoded;

    Protocol(int code, int size, String type) {
        this.code = code;
        this.size = size;
        this.type = type;
        this.encoded = encode(code);
    }

    public static boolean has(String name) {
        return byName.containsKey(name);
    }

    public static Protocol get(String name) {
        if (byName.containsKey(name))
            return byName.get(name);
        throw new IllegalStateException("No protocol with name: " + name);
    }

    public static Protocol get(int code) {
        if (byCode.containsKey(code))
            return byCode.get(code);
        throw new IllegalStateException("No protocol with code: " + code);
    }

    static byte[] encode(int code) {
        byte[] varint = new byte[(32 - Integer.numberOfLeadingZeros(code) + 6) / 7];
        putUvarint(varint, code);
        return varint;
    }

    private static void read(InputStream in, byte[] b) throws IOException {
        read(in, b, b.length);
    }

    private static void read(InputStream in, byte[] b, int len) throws IOException {
        int total = 0, r = 0;
        while (total < len && r != -1) {
            r = in.read(b, total, len - total);
            if (r >= 0)
                total += r;
        }
    }

    static void putUvarint(byte[] buf, long x) {
        int i = 0;
        while (x >= 0x80) {
            buf[i] = (byte) (x | 0x80);
            x >>= 7;
            i++;
        }
        buf[i] = (byte) x;
    }

    static long readVarint(InputStream in) throws IOException {
        long x = 0;
        int s = 0;
        for (int i = 0; i < 10; i++) {
            int b = in.read();
            if (b == -1)
                throw new EOFException();
            if (b < 0x80) {
                if (i == 9 && b > 1) {
                    throw new IllegalStateException("Overflow reading varint" + (-(i + 1)));
                }
                return x | (((long) b) << s);
            }
            x |= ((long) b & 0x7f) << s;
            s += 7;
        }
        throw new IllegalStateException("Varint too long!");
    }

    public void appendCode(OutputStream out) throws IOException {
        out.write(encoded);
    }

    public boolean isTerminal() {
        return this == Protocol.UNIX;
    }

    public int size() {
        return size;
    }

    public int code() {
        return code;
    }

    public String getType() {
        return type;
    }

    @NonNull
    @Override
    public String toString() {
        return name();
    }

    public byte[] addressToBytes(String addr) {
        try {
            switch (this) {
                case IP4:
                    if (!addr.matches(IPV4_REGEX))
                        throw new IllegalStateException("Invalid IPv4 address: " + addr);
                    return Inet4Address.getByName(addr).getAddress();
                case IP6:
                    return Inet6Address.getByName(addr).getAddress();
                case TCP:
                case UDP:
                case DCCP:
                case SCTP:
                    int x = Integer.parseInt(addr);
                    if (x > 65535)
                        throw new IllegalStateException("Failed to parse " + name() + " address " + addr + " (> 65535");
                    return new byte[]{(byte) (x >> 8), (byte) x};
                case P2P:
                case IPFS: {
                    byte[] hashBytes = PeerId.fromBase58(addr).getBytes();

                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    byte[] varint = new byte[(32 - Integer.numberOfLeadingZeros(hashBytes.length) + 6) / 7];
                    putUvarint(varint, hashBytes.length);
                    bout.write(varint);
                    bout.write(hashBytes);
                    return bout.toByteArray();
                }
                case ONION: {
                    String[] split = addr.split(":");
                    if (split.length != 2)
                        throw new IllegalStateException("Onion address needs a port: " + addr);

                    // onion address without the ".onion" substring
                    if (split[0].length() != 16)
                        throw new IllegalStateException("failed to parse " + name() + " addr: " + addr + " not a Tor onion address.");

                    byte[] onionHostBytes = Multibase.decode(Multibase.Base.Base32.prefix + split[0]);
                    if (onionHostBytes.length != 10)
                        throw new IllegalStateException("Invalid onion address host: " + split[0]);
                    int port = Integer.parseInt(split[1]);
                    if (port > 65535)
                        throw new IllegalStateException("Port is > 65535: " + port);

                    if (port < 1)
                        throw new IllegalStateException("Port is < 1: " + port);

                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    DataOutputStream dout = new DataOutputStream(b);
                    dout.write(onionHostBytes);
                    dout.writeShort(port);
                    dout.flush();
                    return b.toByteArray();
                }
                case ONION3: {
                    String[] split = addr.split(":");
                    if (split.length != 2)
                        throw new IllegalStateException("Onion3 address needs a port: " + addr);

                    // onion3 address without the ".onion" substring
                    if (split[0].length() != 56)
                        throw new IllegalStateException("failed to parse " + name() + " addr: " + addr + " not a Tor onion3 address.");

                    byte[] onionHostBytes = Multibase.decode(Multibase.Base.Base32.prefix + split[0]);
                    if (onionHostBytes.length != 35)
                        throw new IllegalStateException("Invalid onion3 address host: " + split[0]);
                    int port = Integer.parseInt(split[1]);
                    if (port > 65535)
                        throw new IllegalStateException("Port is > 65535: " + port);

                    if (port < 1)
                        throw new IllegalStateException("Port is < 1: " + port);

                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    DataOutputStream dout = new DataOutputStream(b);
                    dout.write(onionHostBytes);
                    dout.writeShort(port);
                    dout.flush();
                    return b.toByteArray();
                }

                case UNIX: {
                    if (addr.startsWith("/"))
                        addr = addr.substring(1);
                    byte[] path = addr.getBytes();
                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    DataOutputStream dout = new DataOutputStream(b);
                    byte[] length = new byte[(32 - Integer.numberOfLeadingZeros(path.length) + 6) / 7];
                    putUvarint(length, path.length);
                    dout.write(length);
                    dout.write(path);
                    dout.flush();
                    return b.toByteArray();
                }
                case DNS4:
                case DNS6:
                case DNSADDR: {
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    byte[] hashBytes = addr.getBytes();
                    byte[] varint = new byte[(32 - Integer.numberOfLeadingZeros(hashBytes.length) + 6) / 7];
                    putUvarint(varint, hashBytes.length);
                    bout.write(varint);
                    bout.write(hashBytes);
                    return bout.toByteArray();
                }
                default:
                    throw new IllegalStateException("Unknown multiaddr type: " + name());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readAddress(@NonNull InputStream in) throws IOException {
        int sizeForAddress = sizeForAddress(in);
        byte[] buf;
        switch (this) {
            case IP4:
            case IP6:
                buf = new byte[sizeForAddress];
                read(in, buf);
                return InetAddress.getByAddress(buf).toString().substring(1);
            case TCP:
            case UDP:
            case DCCP:
            case SCTP:
                return Integer.toString((in.read() << 8) | (in.read()));
            case P2P:
            case IPFS:
                buf = new byte[sizeForAddress];
                read(in, buf);
                return new PeerId(buf).toBase58();
            case ONION: {
                byte[] host = new byte[10];
                read(in, host);
                String port = Integer.toString((in.read() << 8) | (in.read()));
                return Multibase.encode(Multibase.Base.Base32, host).substring(1) + ":" + port;
            }
            case ONION3: {
                byte[] host = new byte[35];
                read(in, host);
                String port = Integer.toString((in.read() << 8) | (in.read()));
                return Multibase.encode(Multibase.Base.Base32, host).substring(1) + ":" + port;
            }
            case UNIX:
            case DNS:
            case DNS4:
            case DNS6:
            case DNSADDR:
                buf = new byte[sizeForAddress];
                read(in, buf);
                return new String(buf);
        }
        throw new IllegalStateException("Unimplemented protocol type: " + type);
    }

    public int sizeForAddress(InputStream in) throws IOException {
        if (size > 0)
            return size / 8;
        if (size == 0)
            return 0;
        return (int) readVarint(in);
    }
}
