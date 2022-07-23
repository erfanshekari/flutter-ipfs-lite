package threads.lite.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Room;

import com.google.common.io.BaseEncoding;

import java.util.List;

import threads.lite.cid.Cid;
import threads.lite.format.BlockStore;


public class BLOCKS implements BlockStore {

    private static volatile BLOCKS INSTANCE = null;
    private final BlocksDatabase blocksDatabase;

    private BLOCKS(BLOCKS.Builder builder) {
        this.blocksDatabase = builder.blocksDatabase;
    }

    @NonNull
    private static BLOCKS createBlocks(@NonNull BlocksDatabase blocksDatabase) {

        return new BLOCKS.Builder()
                .blocksDatabase(blocksDatabase)
                .build();
    }

    public static BLOCKS getInstance(@NonNull Context context) {

        if (INSTANCE == null) {
            synchronized (BLOCKS.class) {
                if (INSTANCE == null) {
                    BlocksDatabase blocksDatabase = Room.databaseBuilder(context, BlocksDatabase.class,
                                    BlocksDatabase.class.getSimpleName()).
                            allowMainThreadQueries().
                            fallbackToDestructiveMigration().build();

                    INSTANCE = BLOCKS.createBlocks(blocksDatabase);
                }
            }
        }
        return INSTANCE;
    }

    @Nullable
    public byte[] getData(@NonNull String id) {
        Block block = getBlock(id);
        if (block != null) {
            return block.getData();
        }
        return null;
    }

    @Override
    public void clear() {
        getBlocksDatabase().clearAllTables();
    }


    @NonNull
    public BlocksDatabase getBlocksDatabase() {
        return blocksDatabase;
    }


    @NonNull
    private Block createBlock(@NonNull String id, @NonNull byte[] data) {
        //LogUtils.error(TAG, "createBlock " +  id);
        return Block.createBlock(id, data);
    }

    private void storeBlock(@NonNull Block block) {
        getBlocksDatabase().blockDao().insertBlock(block);
    }

    public void deleteBlock(@NonNull String id) {
        //LogUtils.error(TAG, "deleteBlock " +  id);
        getBlocksDatabase().blockDao().deleteBlock(id);
    }

    public int sizeBlock(@NonNull String id) {
        return (int) getBlockSize(id);
    }

    public void insertBlock(@NonNull String id, @NonNull byte[] bytes) {
        //LogUtils.error(TAG, "insertBlock " +  id);
        storeBlock(createBlock(id, bytes));
    }

    public boolean hasBlock(@NonNull String id) {
        return getBlocksDatabase().blockDao().hasBlock(id);
    }

    public long getBlockSize(@NonNull String id) {
        //LogUtils.error(TAG, "getBlockSize " +  id);
        return getBlocksDatabase().blockDao().getBlockSize(id);
    }

    @Nullable
    public Block getBlock(@NonNull String id) {
        //LogUtils.error(TAG, "getBlock " +  id);
        return getBlocksDatabase().blockDao().getBlock(id);
    }

    @Override
    public boolean hasBlock(@NonNull Cid cid) {
        String key = getKey(cid);
        return hasBlock(key);
    }

    @Override
    public threads.lite.format.Block getBlock(@NonNull Cid cid) {

        String key = getKey(cid);
        byte[] data = getData(key);
        if (data == null) {
            return null;
        }
        return threads.lite.format.Block.createBlockWithCid(cid, data);
    }

    @Override
    public void putBlock(@NonNull threads.lite.format.Block block) {
        String key = getKey(block.getCid());
        insertBlock(key, block.getData());
    }

    @Override
    public int getSize(@NonNull Cid cid) {
        String key = getKey(cid);
        return sizeBlock(key);
    }

    @Override
    public void deleteBlock(@NonNull Cid cid) {
        String key = getKey(cid);
        deleteBlock(key);
    }

    @Override
    public void deleteBlocks(@NonNull List<Cid> cids) {
        for (Cid cid : cids) {
            deleteBlock(cid);
        }
    }


    public String getKey(@NonNull Cid cid) {
        return BaseEncoding.base32().encode(cid.bytes());
    }

    static class Builder {
        BlocksDatabase blocksDatabase = null;

        BLOCKS build() {

            return new BLOCKS(this);
        }

        Builder blocksDatabase(@NonNull BlocksDatabase blocksDatabase) {

            this.blocksDatabase = blocksDatabase;
            return this;
        }
    }
}
