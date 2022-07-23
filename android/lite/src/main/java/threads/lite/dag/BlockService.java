package threads.lite.dag;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import threads.lite.bitswap.BitSwap;
import threads.lite.cid.Cid;
import threads.lite.core.Closeable;
import threads.lite.format.Block;
import threads.lite.format.BlockStore;

public interface BlockService {

    static BlockService createBlockService(@NonNull final BlockStore bs,
                                           @NonNull final BitSwap bitSwap) {
        return (closeable, cid) -> {
            Block block = bs.getBlock(cid);
            if (block != null) {
                return block;
            }
            return bitSwap.getBlock(closeable, cid);
        };
    }

    @Nullable
    Block getBlock(@NonNull Closeable closeable, @NonNull Cid cid) throws InterruptedException;


}
