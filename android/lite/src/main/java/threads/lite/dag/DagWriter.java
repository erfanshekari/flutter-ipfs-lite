package threads.lite.dag;

import android.util.Pair;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;

import merkledag.pb.Merkledag;
import threads.lite.IPFS;
import threads.lite.cid.Cid;
import threads.lite.format.Block;
import threads.lite.format.BlockStore;
import threads.lite.format.Coder;
import threads.lite.utils.Splitter;
import unixfs.pb.Unixfs;


public class DagWriter {
    public static final int depthRepeat = 4;
    private final BlockStore blockStore;
    private final Splitter splitter;


    public DagWriter(@NonNull BlockStore blockStore, @NonNull Splitter splitter) {
        this.blockStore = blockStore;
        this.splitter = splitter;
    }

    public NodeWrapper createWrapper(@NonNull Unixfs.Data.DataType dataType) {
        return new NodeWrapper(dataType);
    }

    public void fillNodeLayer(@NonNull NodeWrapper node) {
        byte[] bytes = new byte[IPFS.CHUNK_SIZE];
        int numChildren = 0;
        while ((numChildren < IPFS.LINKS_PER_BLOCK) && !isDone()) {
            int read = splitter.nextBytes(bytes);
            if (read > 0) {
                Unixfs.Data.Builder data = Unixfs.Data.newBuilder().setType(Unixfs.Data.DataType.Raw)
                        .setFilesize(read)
                        .setData(ByteString.copyFrom(bytes, 0, read));

                Merkledag.PBNode.Builder pbn = Merkledag.PBNode.newBuilder();
                pbn.setData(ByteString.copyFrom(data.build().toByteArray()));

                Merkledag.PBNode child = pbn.build();

                byte[] encoded = child.toByteArray();

                Block block = Coder.encode(encoded);
                blockStore.putBlock(block);
                node.addChild(block.getCid(), read);
            }
            numChildren++;
        }
        node.commit();
    }


    public Cid trickle() {
        NodeWrapper newRoot = createWrapper(Unixfs.Data.DataType.File);
        Pair<Cid, Long> result = fillTrickleRec(newRoot, -1);
        return result.first;
    }


    private Pair<Cid, Long> fillTrickleRec(@NonNull NodeWrapper node, int maxDepth) {
        // Always do this, even in the base case
        fillNodeLayer(node);


        for (int depth = 1; maxDepth == -1 || depth < maxDepth; depth++) {
            if (isDone()) {
                break;
            }

            for (int repeatIndex = 0; repeatIndex < depthRepeat && !isDone(); repeatIndex++) {

                Pair<Cid, Long> result = fillTrickleRec(createWrapper(
                        Unixfs.Data.DataType.File), depth);

                node.addChild(result.first, result.second);
            }
        }
        Block block = node.commit();
        long fileSize = node.getFileSize();
        blockStore.putBlock(block);
        return Pair.create(block.getCid(), fileSize);
    }

    public boolean isDone() {
        return splitter.done();
    }

    public static class NodeWrapper {
        private final Unixfs.Data.Builder data;
        private final Merkledag.PBNode.Builder pbn = Merkledag.PBNode.newBuilder();
        private long size = 0L;

        private NodeWrapper(@NonNull Unixfs.Data.DataType dataType) {
            this.data = Unixfs.Data.newBuilder().setType(dataType).setFilesize(size);
        }

        public void addChild(@NonNull Cid cid, long fileSize) {
            Merkledag.PBLink.Builder lnb = Merkledag.PBLink.newBuilder()
                    .setName("")
                    .setTsize(fileSize);

            if (cid.isDefined()) {
                lnb.setHash(ByteString.copyFrom(cid.bytes()));
            }

            pbn.addLinks(lnb.build());
            data.addBlocksizes(fileSize);
            size = size + fileSize;
        }

        public Block commit() {
            data.setFilesize(size);
            pbn.setData(ByteString.copyFrom(data.build().toByteArray()));
            Merkledag.PBNode node = pbn.build();
            byte[] encoded = node.toByteArray();
            return Coder.encode(encoded);
        }

        public long getFileSize() {
            return size;
        }
    }
}
