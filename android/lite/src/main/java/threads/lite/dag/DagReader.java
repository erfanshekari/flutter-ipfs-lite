package threads.lite.dag;


import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import threads.lite.core.Closeable;
import threads.lite.format.Node;
import threads.lite.format.Stage;
import threads.lite.format.Visitor;
import threads.lite.format.Walker;
import unixfs.pb.Unixfs;

public class DagReader {

    public final AtomicInteger atomicLeft = new AtomicInteger(0);
    private final long size;
    private final Visitor visitor;
    private final Walker dagWalker;


    public DagReader(@NonNull Walker dagWalker, long size) {
        this.dagWalker = dagWalker;
        this.size = size;
        this.visitor = new Visitor(dagWalker.getRoot());

    }

    public static DagReader create(@NonNull Node node, @NonNull NodeService serv) {
        long size = 0;

        Unixfs.Data unixData = node.getData();

        switch (unixData.getType()) {
            case Raw:
            case File:
                size = unixData.getFilesize();
                break;
        }


        Walker dagWalker = Walker.createWalker(node, serv);
        return new DagReader(dagWalker, size);

    }

    public long getSize() {
        return size;
    }


    public void seek(@NonNull Closeable closeable, long offset) throws InterruptedException {
        Pair<Stack<Stage>, Long> result = dagWalker.seek(closeable, offset);
        this.atomicLeft.set(result.second.intValue());
        this.visitor.reset(result.first);
    }

    @Nullable
    public ByteString loadNextData(@NonNull Closeable closeable) throws InterruptedException {

        int left = atomicLeft.getAndSet(0);
        if (left > 0) {
            Node node = visitor.peekStage().getNode();

            if (node.numLinks() == 0) {
                return readUnixNodeData(node, left);
            }
        }

        while (true) {
            Node node = dagWalker.next(closeable, visitor);
            if (node == null) {
                return null;
            }

            if (node.numLinks() > 0) {
                continue;
            }

            return readUnixNodeData(node, 0);
        }
    }

    private ByteString readUnixNodeData(@NonNull Node node, int position) {

        Unixfs.Data unixData = node.getData();
        switch (unixData.getType()) {
            case Directory:
            case File:
            case Raw:
                return unixData.getData().substring(position);
            default:
                throw new RuntimeException("found %s node in unexpected place " +
                        unixData.getType().name());
        }
    }
}
