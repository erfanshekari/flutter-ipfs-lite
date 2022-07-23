package threads.lite.utils;

import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import threads.lite.bitswap.BitSwap;
import threads.lite.cid.Cid;
import threads.lite.core.Closeable;
import threads.lite.dag.BlockService;
import threads.lite.dag.NodeService;
import threads.lite.format.BlockStore;
import threads.lite.format.Node;


public class Resolver {

    @NonNull
    public static Node resolveNode(@NonNull Closeable closeable, @NonNull BlockStore blockStore,
                                   @NonNull BitSwap bitSwap, @NonNull Cid root,
                                   @NonNull List<String> path) throws InterruptedException {

        BlockService blockservice = BlockService.createBlockService(blockStore, bitSwap);
        NodeService nodeService = NodeService.createNodeService(blockservice);
        Pair<Cid, List<String>> resolved = resolveToLastNode(closeable, nodeService, root, path);
        Cid cid = resolved.first;
        Objects.requireNonNull(cid);
        return resolveNode(closeable, nodeService, cid);
    }

    @NonNull
    public static Node resolveNode(@NonNull Closeable closeable,
                                   @NonNull NodeService nodeGetter,
                                   @NonNull Cid cid) throws InterruptedException {
        return nodeGetter.getNode(closeable, cid);
    }

    @NonNull
    private static Pair<Cid, List<String>> resolveToLastNode(@NonNull Closeable closeable,
                                                             @NonNull NodeService nodeService,
                                                             @NonNull Cid root,
                                                             @NonNull List<String> path)
            throws InterruptedException {

        if (path.size() == 0) {
            return Pair.create(root, Collections.emptyList());
        }

        Node node = nodeService.getNode(closeable, root);

        for (String name : path) {
            Cid cid = node.getLinkCid(name);
            node = nodeService.getNode(closeable, cid);
        }

        return Pair.create(node.getCid(), Collections.emptyList());

    }

}
