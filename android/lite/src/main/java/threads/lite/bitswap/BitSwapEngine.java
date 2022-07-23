package threads.lite.bitswap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bitswap.pb.MessageOuterClass;
import threads.lite.IPFS;
import threads.lite.LogUtils;
import threads.lite.cid.Cid;
import threads.lite.format.Block;
import threads.lite.format.BlockStore;


public class BitSwapEngine {
    public static final int MaxBlockSizeReplaceHasWithBlock = 1024;
    private static final String TAG = BitSwapEngine.class.getSimpleName();
    private final BlockStore blockstore;


    BitSwapEngine(@NonNull BlockStore bs) {
        this.blockstore = bs;
    }

    private BitSwapMessage createMessage(@NonNull List<Task> tasks) {

        // Create a new message
        BitSwapMessage msg = BitSwapMessage.create(false);

        // Amount of data in the request queue still waiting to be popped
        msg.setPendingBytes(0);

        // Split out want-blocks, want-haves and DONT_HAVEs
        List<Cid> blockCids = new ArrayList<>();
        Map<Cid, TaskData> blockTasks = new HashMap<>();

        for (Task task : tasks) {

            Cid c = task.cid;
            TaskData td = task.data;
            if (td.HaveBlock) {
                if (td.IsWantBlock) {
                    blockCids.add(c);
                    blockTasks.put(c, td);
                } else {
                    // Add HAVES to the message
                    msg.addHave(c);
                }
            } else {
                // Add DONT_HAVEs to the message
                msg.addDontHave(c);
            }
        }

        Map<Cid, Block> blks = getBlocks(blockCids);
        for (Map.Entry<Cid, TaskData> entry : blockTasks.entrySet()) {
            Block blk = blks.get(entry.getKey());
            // If the block was not found (it has been removed)
            if (blk == null) {
                // If the client requested DONT_HAVE, add DONT_HAVE to the message
                if (entry.getValue().SendDontHave) {
                    msg.addDontHave(entry.getKey());
                }
            } else {
                msg.addBlock(blk);
            }
        }
        return msg;

    }

    public List<BitSwapMessage.Entry> filterWantsCancels(
            @NonNull List<BitSwapMessage.Entry> es) {
        List<BitSwapMessage.Entry> wants = new ArrayList<>();
        for (BitSwapMessage.Entry et : es) {
            if (!et.Cancel) {
                wants.add(et);
            }
        }
        return wants;
    }

    @Nullable
    public BitSwapMessage messageReceived(@NonNull BitSwapMessage bsm) {

        List<BitSwapMessage.Entry> entries = bsm.wantlist();

        List<BitSwapMessage.Entry> wants = filterWantsCancels(entries);

        Set<Cid> wantKs = new HashSet<>();
        for (BitSwapMessage.Entry entry : wants) {
            wantKs.add(entry.cid);
        }


        HashMap<Cid, Integer> blockSizes = getBlockSizes(wantKs);

        List<Task> tasks = new ArrayList<>();

        for (BitSwapMessage.Entry entry : wants) {
            // For each want-have / want-block

            Cid c = entry.cid;
            Integer blockSize = blockSizes.get(entry.cid);

            if (blockSize == null) {
                LogUtils.debug(TAG,
                        "Bitswap engine: block not found" + " cid " + entry.cid.String()
                                + " sendDontHave " + entry.SendDontHave);

                // Only add the task to the queue if the requester wants a DONT_HAVE
                if (IPFS.SEND_DONT_HAVES && entry.SendDontHave) {

                    boolean isWantBlock =
                            entry.WantType == MessageOuterClass.Message.Wantlist.WantType.Block;

                    Task task = new Task(c, new TaskData(false,
                            isWantBlock, true));
                    tasks.add(task);

                }
            } else {
                boolean isWantBlock = sendAsBlock(entry.WantType, blockSize);

                LogUtils.debug(TAG,
                        "Bitswap engine: block found" +
                                " cid " + entry.cid.String() + " isWantBlock " + isWantBlock);

                Task task = new Task(c, new TaskData(true, isWantBlock, entry.SendDontHave));
                tasks.add(task);
            }
        }
        BitSwapMessage msg = createMessage(tasks);
        if (!msg.empty()) {
            return msg;
        }
        return null;

    }

    private boolean sendAsBlock(MessageOuterClass.Message.Wantlist.WantType wantType, Integer blockSize) {
        boolean isWantBlock = wantType == MessageOuterClass.Message.Wantlist.WantType.Block;
        return isWantBlock || blockSize <= MaxBlockSizeReplaceHasWithBlock;
    }


    public Map<Cid, Block> getBlocks(@NonNull List<Cid> cids) {
        Map<Cid, Block> blks = new HashMap<>();
        for (Cid c : cids) {
            Block block = blockstore.getBlock(c);
            if (block != null) {
                blks.put(c, block);
            }
        }
        return blks;
    }

    public HashMap<Cid, Integer> getBlockSizes(@NonNull Set<Cid> wantKs) {

        HashMap<Cid, Integer> sizes = new HashMap<>();
        for (Cid cid : wantKs) {
            int size = blockstore.getSize(cid);
            if (size > 0) {
                sizes.put(cid, size);
            }
        }
        return sizes;
    }


    private static class Task {
        // Topic for the task
        public final Cid cid;
        // Arbitrary data associated with this Task by the client
        public final TaskData data;

        public Task(@NonNull Cid cid, @NonNull TaskData data) {
            this.cid = cid;
            this.data = data;
        }

    }


    private static class TaskData {
        // Tasks can be want-have or want-block
        final boolean IsWantBlock;
        // Whether to immediately send a response if the block is not found
        final boolean SendDontHave;
        // Whether the block was found
        final boolean HaveBlock;

        public TaskData(boolean haveBlock, boolean isWantBlock, boolean sendDontHave) {
            this.SendDontHave = sendDontHave;
            this.IsWantBlock = isWantBlock;
            this.HaveBlock = haveBlock;
        }


    }

}
