package threads.lite.format;

import androidx.annotation.NonNull;

import java.security.MessageDigest;

import threads.lite.cid.Cid;

public class Block {

    @NonNull
    private final Cid cid;
    @NonNull
    private final byte[] data;

    private Block(@NonNull Cid cid, @NonNull byte[] data) {
        this.cid = cid;
        this.data = data;
    }

    @NonNull
    public static Block createBlockWithCid(@NonNull Cid cid, @NonNull byte[] data) {
        return new Block(cid, data);
    }

    @NonNull
    public static Block createBlock(@NonNull byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            Cid cid = Cid.newCidV0(hash);
            return createBlockWithCid(cid, data);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @NonNull
    public byte[] getData() {
        return data;
    }

    @NonNull
    public Cid getCid() {
        return cid;
    }

}
