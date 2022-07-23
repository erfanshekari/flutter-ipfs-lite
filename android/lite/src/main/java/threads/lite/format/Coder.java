package threads.lite.format;

import androidx.annotation.NonNull;

import java.security.MessageDigest;

import threads.lite.cid.Cid;
import threads.lite.cid.Multihash;

public class Coder {
    @NonNull
    public static Node decode(@NonNull Block block) {
        Cid cid = block.getCid();
        if (cid.getType() != Cid.DagProtobuf) {
            throw new RuntimeException("only protobuf nodes supported");
        }

        return Node.createNode(cid, block.getData());
    }

    @NonNull
    public static Block encode(@NonNull byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = Cid.encode(digest.digest(data), Multihash.Type.sha2_256.index);
            Cid cid = Cid.newCidV0(hash);
            return Block.createBlockWithCid(cid, data);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

}
