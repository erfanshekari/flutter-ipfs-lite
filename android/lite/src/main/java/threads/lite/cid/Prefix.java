package threads.lite.cid;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public final class Prefix {

    public final long version;
    public final long codec;
    private final long mhType;
    private final long mhLength;

    public Prefix(long codec, long mhLength, long mhType, long version) {
        this.version = version;
        this.codec = codec;
        this.mhType = mhType;
        this.mhLength = mhLength;
    }

    public static Prefix getPrefixFromBytes(byte[] buf) {

        try (InputStream inputStream = new ByteArrayInputStream(buf)) {
            long version = Multihash.readVarint(inputStream);
            if (version != 1 && version != 0) {
                throw new Exception("invalid version");
            }
            long codec = Multihash.readVarint(inputStream);
            if (!(codec == Cid.DagProtobuf || codec == Cid.Raw || codec == Cid.Libp2pKey)) {
                throw new Exception("not supported codec");
            }

            long mhtype = Multihash.readVarint(inputStream);

            long mhlen = Multihash.readVarint(inputStream);

            return new Prefix(codec, mhlen, mhtype, version);


        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @NonNull
    public Cid sum(byte[] data) {


        if (version == 0 && (mhType != Multihash.Type.sha2_256.index) ||
                (mhLength != 32 && mhLength != -1)) {

            throw new RuntimeException("invalid v0 prefix");
        }
        if (mhType != Multihash.Type.sha2_256.index) {
            throw new RuntimeException("todo");
        }
        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = Cid.encode(digest.digest(data), mhType);

            switch ((int) version) {
                case 0:
                    return Cid.newCidV0(hash);
                case 1:
                    return Cid.newCidV1(codec, hash);
                default:
                    throw new RuntimeException("invalid cid version");
            }

        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

    }

    public byte[] bytes() {

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Multihash.putUvarint(out, version);
            Multihash.putUvarint(out, codec);
            Multihash.putUvarint(out, mhType);
            Multihash.putUvarint(out, mhLength);
            return out.toByteArray();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
}
