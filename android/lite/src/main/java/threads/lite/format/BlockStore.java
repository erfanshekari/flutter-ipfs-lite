package threads.lite.format;

import androidx.annotation.NonNull;

import java.util.List;

import threads.lite.cid.Cid;

public interface BlockStore {

    boolean hasBlock(@NonNull Cid cid);

    Block getBlock(@NonNull Cid cid);

    void deleteBlock(@NonNull Cid cid);

    void deleteBlocks(@NonNull List<Cid> cids);

    void putBlock(@NonNull Block block);

    int getSize(@NonNull Cid cid);

    void clear();
}


