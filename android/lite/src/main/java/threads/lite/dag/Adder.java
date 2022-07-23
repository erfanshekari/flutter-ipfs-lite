package threads.lite.dag;


import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import merkledag.pb.Merkledag;
import threads.lite.cid.Cid;
import threads.lite.format.Block;
import threads.lite.format.BlockStore;
import threads.lite.format.Link;
import threads.lite.format.Node;
import threads.lite.utils.ReaderInputStream;
import threads.lite.utils.Splitter;

public class Adder {

    @NonNull
    private final BlockStore blockStore;

    private Adder(@NonNull BlockStore blockStore) {
        this.blockStore = blockStore;
    }

    public static Adder createAdder(@NonNull BlockStore dagService) {
        return new Adder(dagService);
    }


    public Cid createDirectory(@NonNull List<Link> links) {
        Block block = Directory.createDirectory(links);
        blockStore.putBlock(block);
        return block.getCid();
    }

    public Cid createEmptyDirectory() {
        Block block = Directory.createDirectory();
        blockStore.putBlock(block);
        return block.getCid();
    }

    public Cid addChild(@NonNull Node dirNode, @NonNull Link child) {

        if (!Directory.isDirectory(dirNode)) {
            throw new RuntimeException("not a directory");
        }
        List<Merkledag.PBLink> originals = dirNode.getLinks();
        List<Link> links = new ArrayList<>();
        for (Merkledag.PBLink link : originals) {
            if (!Objects.equals(link.getName(), child.getName())) {
                links.add(Link.create(
                        new Cid(link.getHash().toByteArray()),
                        link.getName(), link.getTsize(), Link.Unknown));
            }
        }
        links.add(child);
        return createDirectory(links);
    }


    public Cid removeChild(@NonNull Node dirNode, @NonNull String name) {

        if (!Directory.isDirectory(dirNode)) {
            throw new RuntimeException("not a directory");
        }
        List<Merkledag.PBLink> originals = dirNode.getLinks();
        List<Link> links = new ArrayList<>();
        for (Merkledag.PBLink link : originals) {
            if (!Objects.equals(link.getName(), name)) {
                links.add(Link.create(
                        new Cid(link.getHash().toByteArray()),
                        link.getName(), link.getTsize(), Link.Unknown));
            }
        }
        return createDirectory(links);
    }


    @NonNull
    public Cid createFromStream(@NonNull final ReaderInputStream reader) {

        Splitter splitter = new Splitter() {

            @Override
            public int nextBytes(@NonNull byte[] bytes) {
                return reader.read(bytes);
            }

            @Override
            public boolean done() {
                return reader.done();
            }
        };

        DagWriter db = new DagWriter(blockStore, splitter);

        return db.trickle();
    }

}
