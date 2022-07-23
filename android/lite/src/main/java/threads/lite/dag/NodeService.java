package threads.lite.dag;

import androidx.annotation.NonNull;

import java.util.Objects;

import threads.lite.cid.Cid;
import threads.lite.core.Closeable;
import threads.lite.format.Block;
import threads.lite.format.Coder;
import threads.lite.format.Node;

public interface NodeService {

    static NodeService createNodeService(@NonNull BlockService blockService) {
        return (closeable, cid) -> {

            Block block = blockService.getBlock(closeable, cid);
            Objects.requireNonNull(block, "Block not found");
            return Coder.decode(block);
        };
    }

    @NonNull
    Node getNode(@NonNull Closeable closeable, @NonNull Cid cid) throws InterruptedException;


}
