package threads.lite.format;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.Stack;

import threads.lite.cid.Cid;
import threads.lite.core.Closeable;
import threads.lite.dag.NodeService;
import unixfs.pb.Unixfs;

public class Walker {

    private final NodeService nodeService;
    private final Node root;

    private Walker(@NonNull Node node, @NonNull NodeService nodeService) {
        this.root = node;
        this.nodeService = nodeService;
    }

    public static Walker createWalker(@NonNull Node node, @NonNull NodeService nodeService) {
        return new Walker(node, nodeService);
    }


    @Nullable
    public Node next(@NonNull Closeable closeable, @NonNull Visitor visitor) throws InterruptedException {


        if (!visitor.isRootVisited(true)) {
            Stage stage = visitor.peekStage();
            Objects.requireNonNull(stage);
            if (stage.getNode().equals(root)) {
                return root;
            }
        }
        if (visitor.isPresent()) {

            boolean success = down(closeable, visitor);
            if (success) {
                Stage stage = visitor.peekStage();
                Objects.requireNonNull(stage);
                return stage.getNode();
            }

            success = up(visitor);

            if (success) {
                return next(closeable, visitor);
            }
        }
        return null;
    }

    private boolean up(@NonNull Visitor visitor) {

        if (visitor.isPresent()) {
            visitor.popStage();
        } else {
            return false;
        }
        if (visitor.isPresent()) {
            boolean result = nextChild(visitor);
            if (result) {
                return true;
            } else {
                return up(visitor);
            }
        } else {
            return false;
        }
    }


    private boolean nextChild(@NonNull Visitor visitor) {
        Stage stage = visitor.peekStage();
        Node activeNode = stage.getNode();

        if (stage.index() + 1 < activeNode.numLinks()) {
            stage.incrementIndex();
            return true;
        }

        return false;
    }


    public boolean down(@NonNull Closeable closeable, @NonNull Visitor visitor) throws InterruptedException {

        Node child = fetchChild(closeable, visitor);
        if (child != null) {
            visitor.pushActiveNode(child);
            return true;
        }
        return false;
    }


    @Nullable
    private Node fetchChild(@NonNull Closeable closeable, @NonNull Visitor visitor) throws InterruptedException {
        Stage stage = visitor.peekStage();
        Node activeNode = stage.getNode();
        int index = stage.index();
        Objects.requireNonNull(activeNode);

        if (index >= activeNode.numLinks()) {
            return null;
        }

        return nodeService.getNode(closeable, getChild(activeNode, index));
    }

    @NonNull
    public Node getRoot() {
        return root;
    }

    public Pair<Stack<Stage>, Long> seek(@NonNull Closeable closeable,
                                         @NonNull Stack<Stage> stack,
                                         long offset) throws InterruptedException {

        if (offset < 0) {
            throw new RuntimeException("invalid offset");
        }

        if (offset == 0) {
            return Pair.create(stack, 0L);
        }

        long left = offset;

        Node node = stack.peek().getNode();

        if (node.numLinks() > 0) {
            // Internal node, should be a `mdag.ProtoNode` containing a
            // `unixfs.FSNode` (see the `balanced` package for more details).
            Unixfs.Data unixData = node.getData();

            // If there aren't enough size hints don't seek
            // (see the `io.EOF` handling error comment below).
            if (unixData.getBlocksizesCount() != node.numLinks()) {
                throw new RuntimeException("ErrSeekNotSupported");
            }


            // Internal nodes have no data, so just iterate through the
            // sizes of its children (advancing the child index of the
            // `dagWalker`) to find where we need to go down to next in
            // the search
            for (int i = 0; i < unixData.getBlocksizesCount(); i++) {

                long childSize = unixData.getBlocksizes(i);

                if (childSize > left) {
                    stack.peek().setIndex(i);

                    Node fetched = nodeService.getNode(closeable, getChild(node, i));
                    stack.push(new Stage(fetched));

                    return seek(closeable, stack, left);
                }
                left -= childSize;
            }
        }

        return Pair.create(stack, left);
    }

    @NonNull
    public Cid getChild(@NonNull Node node, int index) {
        return new Cid(node.getLink(index).getHash().toByteArray());
    }

    public Pair<Stack<Stage>, Long> seek(@NonNull Closeable closeable, long offset) throws InterruptedException {

        Stack<Stage> stack = new Stack<>();
        stack.push(new Stage(getRoot()));

        return seek(closeable, stack, offset);

    }
}

