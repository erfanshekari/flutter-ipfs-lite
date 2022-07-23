package threads.lite.dag;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;

import java.util.List;

import merkledag.pb.Merkledag;
import threads.lite.format.Block;
import threads.lite.format.Coder;
import threads.lite.format.Link;
import threads.lite.format.Node;
import unixfs.pb.Unixfs;


public interface Directory {


    static Block createDirectory(@NonNull List<Link> links) {
        Unixfs.Data.Builder builder = Unixfs.Data.newBuilder()
                .setType(Unixfs.Data.DataType.Directory);
        Merkledag.PBNode.Builder pbn = Merkledag.PBNode.newBuilder();
        long fileSize = 0;
        for (Link link : links) {
            long size = link.getSize();
            fileSize = fileSize + size;
            builder.addBlocksizes(size);

            Merkledag.PBLink.Builder lnb = Merkledag.PBLink.newBuilder()
                    .setName(link.getName())
                    .setTsize(link.getSize());

            if (link.getCid().isDefined()) {
                lnb.setHash(ByteString.copyFrom(link.getCid().bytes()));
            }

            pbn.addLinks(lnb.build());
        }
        builder.setFilesize(fileSize);
        byte[] unixData = builder.build().toByteArray();

        pbn.setData(ByteString.copyFrom(unixData));
        Merkledag.PBNode node = pbn.build();
        byte[] encoded = node.toByteArray();
        return Coder.encode(encoded);
    }

    static Block createDirectory() {
        byte[] unixData = Unixfs.Data.newBuilder()
                .setType(Unixfs.Data.DataType.Directory)
                .build().toByteArray();
        Merkledag.PBNode.Builder pbn = Merkledag.PBNode.newBuilder();
        pbn.setData(ByteString.copyFrom(unixData));
        Merkledag.PBNode node = pbn.build();
        byte[] encoded = node.toByteArray();
        return Coder.encode(encoded);
    }


    static boolean isDirectory(@NonNull Node node) {
        Unixfs.Data unixData = node.getData();

        return unixData.getType() == unixfs.pb.Unixfs.Data.DataType.Directory ||
                unixData.getType() == unixfs.pb.Unixfs.Data.DataType.HAMTShard;
    }

}
