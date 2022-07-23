package threads.lite.utils;


import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import merkledag.pb.Merkledag;
import threads.lite.bitswap.BitSwap;
import threads.lite.cid.Cid;
import threads.lite.core.Closeable;
import threads.lite.dag.Adder;
import threads.lite.dag.BlockService;
import threads.lite.dag.Directory;
import threads.lite.dag.NodeService;
import threads.lite.format.Block;
import threads.lite.format.BlockStore;
import threads.lite.format.Coder;
import threads.lite.format.Link;
import threads.lite.format.Node;
import unixfs.pb.Unixfs;


public class Stream {


    private static Adder getFileAdder(@NonNull BlockStore blockStore) {
        return Adder.createAdder(blockStore);
    }

    public static boolean isDir(@NonNull Closeable closeable,
                                @NonNull BlockStore blockstore,
                                @NonNull BitSwap exchange,
                                @NonNull Cid cid) throws InterruptedException {


        BlockService blockservice = BlockService.createBlockService(blockstore, exchange);
        NodeService nodeService = NodeService.createNodeService(blockservice);

        Node node = Resolver.resolveNode(closeable, nodeService, cid);
        Objects.requireNonNull(node);
        return Directory.isDirectory(node);
    }

    @NonNull
    public static Cid createEmptyDirectory(@NonNull BlockStore storage) {
        Adder fileAdder = getFileAdder(storage);
        return fileAdder.createEmptyDirectory();
    }

    @NonNull
    public static Cid addLinkToDirectory(@NonNull BlockStore storage,
                                         @NonNull Cid directory,
                                         @NonNull Link link) {

        Adder fileAdder = getFileAdder(storage);
        Block block = storage.getBlock(directory);
        Objects.requireNonNull(block, "Block not local available");
        Node dirNode = Coder.decode(block);
        Objects.requireNonNull(dirNode);
        return fileAdder.addChild(dirNode, link);

    }

    @NonNull
    public static Cid createDirectory(@NonNull BlockStore storage, @NonNull List<Link> links) {
        Adder fileAdder = getFileAdder(storage);
        return fileAdder.createDirectory(links);
    }

    @NonNull
    public static Cid removeFromDirectory(@NonNull BlockStore storage,
                                          @NonNull Cid directory,
                                          @NonNull String name) {

        Adder fileAdder = getFileAdder(storage);
        Block block = storage.getBlock(directory);
        Objects.requireNonNull(block, "Block not local available");
        Node dirNode = Coder.decode(block);
        Objects.requireNonNull(dirNode);
        return fileAdder.removeChild(dirNode, name);
    }

    @NonNull
    public static List<Cid> getBlocks(@NonNull BlockStore blockstore, @NonNull Cid cid) {
        List<Cid> result = new ArrayList<>();

        Block block = blockstore.getBlock(cid);
        Node node = Coder.decode(block);
        List<Merkledag.PBLink> links = node.getLinks();

        for (Merkledag.PBLink link : links) {
            Cid child = new Cid(link.getHash().toByteArray());
            result.add(child);
            result.addAll(getBlocks(blockstore, child));
        }

        return result;
    }

    public static void ls(@NonNull Closeable closeable,
                          @NonNull Consumer<Link> consumer,
                          @NonNull BlockStore blockstore,
                          @NonNull BitSwap bitSwap,
                          @NonNull Cid cid, boolean resolveChildren) throws InterruptedException {

        BlockService blockservice = BlockService.createBlockService(blockstore, bitSwap);
        NodeService nodeService = NodeService.createNodeService(blockservice);


        Node node = Resolver.resolveNode(closeable, nodeService, cid);
        Objects.requireNonNull(node);
        List<Merkledag.PBLink> links = node.getLinks();
        for (Merkledag.PBLink link : links) {
            processLink(closeable, consumer, nodeService, link, resolveChildren);
        }
    }

    public static boolean hasLink(@NonNull Closeable closeable,
                                  @NonNull BlockStore blockstore,
                                  @NonNull BitSwap bitSwap,
                                  @NonNull Cid cid,
                                  @NonNull String name) throws InterruptedException {

        BlockService blockservice = BlockService.createBlockService(blockstore, bitSwap);
        NodeService nodeService = NodeService.createNodeService(blockservice);

        Node node = Resolver.resolveNode(closeable, nodeService, cid);
        Objects.requireNonNull(node);
        Merkledag.PBLink link = node.getLinkByName(name);
        return link != null;
    }

    @NonNull
    public static Cid readInputStream(@NonNull BlockStore storage,
                                      @NonNull ReaderInputStream readerInputStream) {

        Adder fileAdder = getFileAdder(storage);
        return fileAdder.createFromStream(readerInputStream);
    }

    private static void processLink(@NonNull Closeable closeable,
                                    @NonNull Consumer<Link> consumer,
                                    @NonNull NodeService nodeService,
                                    @NonNull Merkledag.PBLink link, boolean resolveChildren)
            throws InterruptedException {

        String name = link.getName();
        long size = link.getTsize();
        Cid cid = new Cid(link.getHash().toByteArray());


        if (!resolveChildren) {
            consumer.accept(Link.create(cid, name, size, Link.Unknown));
        } else {

            Node linkNode = nodeService.getNode(closeable, cid);

            Unixfs.Data data = linkNode.getData();
            int type;
            switch (data.getType()) {
                case File:
                    type = Link.File;
                    break;
                case Raw:
                    type = Link.Raw;
                    break;
                case Directory:
                    type = Link.Dir;
                    break;
                case Symlink:
                case HAMTShard:
                case Metadata:
                default:
                    type = Link.Unknown;
            }
            size = data.getFilesize();
            consumer.accept(Link.create(cid, name, size, type));


        }

    }
}
