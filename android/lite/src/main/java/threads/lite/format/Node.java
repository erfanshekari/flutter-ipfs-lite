package threads.lite.format;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Objects;

import merkledag.pb.Merkledag;
import threads.lite.cid.Cid;
import unixfs.pb.Unixfs;

public class Node {
    private final Merkledag.PBNode node;
    private final Cid cid;
    private Unixfs.Data data = null;

    private Node(@NonNull Cid cid, @NonNull Merkledag.PBNode node) {
        this.cid = cid;
        this.node = node;
    }

    @NonNull
    public static Node createNode(@NonNull Cid cid, byte[] encoded) {
        try {
            return new Node(cid, Merkledag.PBNode.parseFrom(encoded));
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @NonNull
    public Cid getLinkCid(@NonNull String name) {

        Merkledag.PBLink lnk = getLinkByName(name);
        if (lnk == null) {
            throw new RuntimeException("no link with the name " + name + " resolved");
        }

        return new Cid(lnk.getHash().toByteArray());
    }

    @Nullable
    public Merkledag.PBLink getLinkByName(@NonNull String name) {

        for (Merkledag.PBLink link : getLinks()) {
            if (Objects.equals(link.getName(), name)) {
                return link;
            }
        }
        return null;

    }

    @NonNull
    public Merkledag.PBLink getLink(int index) {
        return node.getLinks(index);
    }

    @NonNull
    public List<Merkledag.PBLink> getLinks() {
        return node.getLinksList();
    }

    public int numLinks() {
        return node.getLinksCount();
    }

    @NonNull
    public Cid getCid() {
        return cid;
    }

    @NonNull
    public Unixfs.Data getData() {
        try {
            if (data == null) {
                data = Unixfs.Data.parseFrom(node.getData().toByteArray());
            }
        } catch (Throwable throwable) {
            throw new RuntimeException();
        }
        return data;
    }

}
