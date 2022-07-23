package threads.lite.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;

import java.util.Objects;

import threads.lite.bitswap.BitSwap;
import threads.lite.cid.Cid;
import threads.lite.core.Closeable;
import threads.lite.dag.BlockService;
import threads.lite.dag.DagReader;
import threads.lite.dag.NodeService;
import threads.lite.format.BlockStore;
import threads.lite.format.Node;

public class Reader {

    private final DagReader dagReader;
    private final Closeable closeable;


    private Reader(@NonNull Closeable closeable, @NonNull DagReader dagReader) {
        this.closeable = closeable;
        this.dagReader = dagReader;
    }

    public static Reader getReader(@NonNull Closeable closeable, @NonNull BlockStore blockstore,
                                   @NonNull BitSwap bitSwap, @NonNull Cid cid) throws InterruptedException {
        BlockService blockservice = BlockService.createBlockService(blockstore, bitSwap);
        NodeService dags = NodeService.createNodeService(blockservice);
        Node top = Resolver.resolveNode(closeable, dags, cid);
        Objects.requireNonNull(top);
        DagReader dagReader = DagReader.create(top, dags);

        return new Reader(closeable, dagReader);
    }

    @Nullable
    public ByteString loadNextData() throws InterruptedException {
        return dagReader.loadNextData(closeable);
    }

    public void seek(long position) throws InterruptedException {
        dagReader.seek(closeable, position);
    }

    public long getSize() {
        return this.dagReader.getSize();
    }
}
