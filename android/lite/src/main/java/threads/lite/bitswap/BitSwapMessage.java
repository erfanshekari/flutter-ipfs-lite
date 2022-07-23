package threads.lite.bitswap;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bitswap.pb.MessageOuterClass;
import threads.lite.cid.Cid;
import threads.lite.cid.Prefix;
import threads.lite.format.Block;


public interface BitSwapMessage {


    static BitSwapMessage create(boolean full) {
        return new BitSwapMessageImpl(full);
    }

    static BitSwapMessage newMessageFromProto(MessageOuterClass.Message pbm) {
        BitSwapMessageImpl m = new BitSwapMessageImpl(pbm.getWantlist().getFull());
        for (MessageOuterClass.Message.Wantlist.Entry e :
                pbm.getWantlist().getEntriesList()) {
            Cid cid = new Cid(e.getBlock().toByteArray());
            if (!cid.isDefined()) {
                throw new RuntimeException("errCidMissing");
            }
            m.addEntry(cid, e.getPriority(), e.getCancel(), e.getWantType(), e.getSendDontHave());
        }
        // deprecated
        for (ByteString data : pbm.getBlocksList()) {
            // CIDv0, sha256, protobuf only
            Block block = Block.createBlock(data.toByteArray());
            m.addBlock(block);
        }
        for (MessageOuterClass.Message.Block b : pbm.getPayloadList()) {
            ByteString prefix = b.getPrefix();
            Prefix pref = Prefix.getPrefixFromBytes(prefix.toByteArray());
            Cid cid = pref.sum(b.getData().toByteArray());
            Block block = Block.createBlockWithCid(cid, b.getData().toByteArray());
            m.addBlock(block);
        }

        for (MessageOuterClass.Message.BlockPresence bi : pbm.getBlockPresencesList()) {
            Cid cid = new Cid(bi.getCid().toByteArray());
            if (!cid.isDefined()) {
                throw new RuntimeException("errCidMissing");
            }
            m.AddBlockPresence(cid, bi.getType());
        }


        m.pendingBytes = pbm.getPendingBytes();

        return m;
    }

    MessageOuterClass.Message toProtoV1();

    // Wantlist returns a slice of unique keys that represent data wanted by
    // the sender.
    List<Entry> wantlist();

    // Blocks returns a slice of unique blocks.
    List<Block> blocks();

    // Haves returns the Cids for each HAVE
    List<Cid> haves();

    // AddEntry adds an entry to the Wantlist.
    void entry(@NonNull Cid key, int priority, @NonNull MessageOuterClass.Message.Wantlist.WantType wantType, boolean sendDontHave);

    // Empty indicates whether the message has any information
    boolean empty();

    // AddBlock adds a block to the message
    void addBlock(@NonNull Block block);

    // AddHave adds a HAVE for the given Cid to the message
    void addHave(@NonNull Cid cid);

    // AddDontHave adds a DONT_HAVE for the given Cid to the message
    void addDontHave(@NonNull Cid cid);

    // SetPendingBytes sets the number of bytes of data that are yet to be sent
    // to the client (because they didn't fit in this message)
    void setPendingBytes(int pendingBytes);


    // Entry is a wantlist entry in a Bitswap message, with flags indicating
    // - whether message is a cancel
    // - whether requester wants a DONT_HAVE message
    // - whether requester wants a HAVE message (instead of the block)
    class Entry {
        public Cid cid;
        public int priority;
        public MessageOuterClass.Message.Wantlist.WantType WantType;
        public boolean Cancel;
        public boolean SendDontHave;

        // Get the entry in protobuf form
        public MessageOuterClass.Message.Wantlist.Entry ToPB() {

            return MessageOuterClass.Message.Wantlist.Entry.newBuilder().setBlock(
                            ByteString.copyFrom(cid.bytes())
                    ).setPriority(priority).
                    setCancel(Cancel).
                    setWantType(WantType).
                    setSendDontHave(SendDontHave).build();

        }
    }


    class BitSwapMessageImpl implements BitSwapMessage {

        final HashMap<Cid, Entry> wantlist = new HashMap<>();
        final HashMap<Cid, Block> blocks = new HashMap<>();
        final HashMap<Cid, MessageOuterClass.Message.BlockPresenceType> blockPresences = new HashMap<>();
        final boolean full;
        int pendingBytes;

        public BitSwapMessageImpl(boolean full) {
            this.full = full;
        }

        public void addEntry(@NonNull Cid c,
                             int priority, boolean cancel,
                             @NonNull MessageOuterClass.Message.Wantlist.WantType wantType,
                             boolean sendDontHave) {
            Entry entry = wantlist.get(c);
            if (entry != null) {
                // Only change priority if want is of the same type
                if (entry.WantType == wantType) {
                    entry.priority = priority;
                }
                // Only change from "dont cancel" to "do cancel"
                if (cancel) {
                    entry.Cancel = true;
                }
                // Only change from "dont send" to "do send" DONT_HAVE
                if (sendDontHave) {
                    entry.SendDontHave = true;
                }
                // want-block overrides existing want-have
                if (wantType == MessageOuterClass.Message.Wantlist.WantType.Block
                        && entry.WantType == MessageOuterClass.Message.Wantlist.WantType.Have) {
                    entry.WantType = wantType;
                }
                wantlist.put(c, entry);
                return;
            }

            entry = new Entry();
            entry.cid = c;
            entry.priority = priority;
            entry.WantType = wantType;
            entry.SendDontHave = sendDontHave;
            entry.Cancel = cancel;

            wantlist.put(c, entry);

        }

        @Override
        public List<Entry> wantlist() {
            return new ArrayList<>(wantlist.values());
        }

        @Override
        public List<Block> blocks() {
            return new ArrayList<>(blocks.values());
        }

        private List<Cid> getBlockPresenceByType() {

            List<Cid> cids = new ArrayList<>();
            for (Map.Entry<Cid, MessageOuterClass.Message.BlockPresenceType> entry :
                    blockPresences.entrySet()) {
                if (entry.getValue() == MessageOuterClass.Message.BlockPresenceType.Have) {
                    cids.add(entry.getKey());
                }
            }
            return cids;
        }

        @Override
        public List<Cid> haves() {
            return getBlockPresenceByType();
        }

        private int PendingBytes() {
            return pendingBytes;
        }

        @Override
        public void entry(@NonNull Cid key, int priority, @NonNull MessageOuterClass.Message.Wantlist.WantType wantType, boolean sendDontHave) {
            addEntry(key, priority, false, wantType, sendDontHave);
        }

        @Override
        public boolean empty() {
            return blocks.size() == 0 && wantlist.size() == 0 && blockPresences.size() == 0;
        }

        @Override
        public void addBlock(@NonNull Block block) {
            blockPresences.remove(block.getCid());
            blocks.put(block.getCid(), block);
        }

        private void AddBlockPresence(@NonNull Cid cid,
                                      @NonNull MessageOuterClass.Message.BlockPresenceType type) {
            if (blocks.containsKey(cid)) {
                return;
            }
            blockPresences.put(cid, type);
        }

        @Override
        public void addHave(@NonNull Cid cid) {
            AddBlockPresence(cid, MessageOuterClass.Message.BlockPresenceType.Have);
        }

        @Override
        public void addDontHave(@NonNull Cid cid) {
            AddBlockPresence(cid, MessageOuterClass.Message.BlockPresenceType.DontHave);
        }

        @Override
        public void setPendingBytes(int pendingBytes) {
            this.pendingBytes = pendingBytes;
        }


        public MessageOuterClass.Message toProtoV1() {

            MessageOuterClass.Message.Builder builder = MessageOuterClass.Message.newBuilder();

            MessageOuterClass.Message.Wantlist.Builder wantBuilder =
                    MessageOuterClass.Message.Wantlist.newBuilder();


            for (Entry entry : wantlist.values()) {
                wantBuilder.addEntries(entry.ToPB());
            }
            wantBuilder.setFull(full);
            builder.setWantlist(wantBuilder.build());


            for (Block block : blocks()) {
                builder.addPayload(MessageOuterClass.Message.Block.newBuilder()
                        .setData(ByteString.copyFrom(block.getData()))
                        .setPrefix(ByteString.copyFrom(block.getCid().getPrefix().bytes())).build());
            }


            for (Map.Entry<Cid, MessageOuterClass.Message.BlockPresenceType> mapEntry :
                    blockPresences.entrySet()) {
                builder.addBlockPresences(MessageOuterClass.Message.BlockPresence.newBuilder()
                        .setType(mapEntry.getValue())
                        .setCid(ByteString.copyFrom(mapEntry.getKey().bytes())));
            }

            builder.setPendingBytes(PendingBytes());

            return builder.build();

        }

    }
}
