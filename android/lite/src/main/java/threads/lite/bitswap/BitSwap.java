package threads.lite.bitswap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.luminis.quic.QuicConnection;
import net.luminis.quic.QuicStream;

import java.io.OutputStream;
import java.util.List;

import bitswap.pb.MessageOuterClass;
import threads.lite.IPFS;
import threads.lite.LogUtils;
import threads.lite.cid.Cid;
import threads.lite.core.Closeable;
import threads.lite.format.Block;
import threads.lite.format.BlockStore;
import threads.lite.host.LiteHost;
import threads.lite.utils.DataHandler;


public class BitSwap {

    private static final String TAG = BitSwap.class.getSimpleName();

    @NonNull
    private final BitSwapManager bitSwapManager;
    @NonNull
    private final BitSwapEngine engine;

    public BitSwap(@NonNull BlockStore blockstore, @NonNull LiteHost host) {
        bitSwapManager = new BitSwapManager(this, blockstore, host);
        engine = new BitSwapEngine(blockstore);
    }

    private static void writeMessage(@NonNull QuicConnection conn, @NonNull BitSwapMessage message) {

        if (IPFS.BITSWAP_REQUEST_ACTIVE) {
            try {
                QuicStream quicStream = conn.createStream(true);

                OutputStream outputStream = quicStream.getOutputStream();
                outputStream.write(DataHandler.writeToken(
                        IPFS.STREAM_PROTOCOL, IPFS.BITSWAP_PROTOCOL));
                outputStream.write(DataHandler.encode(message.toProtoV1()));
                outputStream.close();

            } catch (Throwable throwable) {
                LogUtils.error(TAG, throwable.getClass().getSimpleName() +
                        " : " + throwable.getMessage());
            }
        }
    }

    @Nullable
    public Block getBlock(@NonNull Closeable closeable, @NonNull Cid cid) throws InterruptedException {
        return bitSwapManager.getBlock(closeable, cid);
    }

    public void reset() {
        bitSwapManager.reset();
    }

    public void receiveMessage(@NonNull QuicConnection conn, @NonNull BitSwapMessage bsm) {

        receiveConnMessage(conn, bsm);

        if (IPFS.BITSWAP_ENGINE_ACTIVE) {
            try {
                BitSwapMessage msg = engine.messageReceived(bsm);
                if (msg != null) {
                    QuicStream stream = conn.createStream(true);
                    OutputStream outputStream = stream.getOutputStream();

                    outputStream.write(DataHandler.writeToken(
                            IPFS.STREAM_PROTOCOL, IPFS.BITSWAP_PROTOCOL));
                    outputStream.write(DataHandler.encode(msg.toProtoV1()));
                    outputStream.close();
                }
            } catch (Throwable throwable) {
                LogUtils.error(TAG, throwable);
            }
        }


    }

    private void receiveConnMessage(@NonNull QuicConnection conn, @NonNull BitSwapMessage bsm) {

        List<Block> wanted = bsm.blocks();
        List<Cid> haves = bsm.haves();
        if (wanted.size() > 0 || haves.size() > 0) {
            for (Block block : wanted) {
                LogUtils.info(TAG, "Block Received " + block.getCid().String());
                bitSwapManager.blockReceived(block);
            }

            bitSwapManager.haveReceived(conn, haves);
        }

    }

    void sendHaveMessage(@NonNull QuicConnection conn, @NonNull List<Cid> haves) {
        sendHaves(conn, haves);
    }

    private void sendHaves(@NonNull QuicConnection conn, @NonNull List<Cid> haves) {
        if (haves.size() == 0) {
            return;
        }

        int priority = Integer.MAX_VALUE;

        BitSwapMessage message = BitSwapMessage.create(false);

        for (Cid c : haves) {

            // Broadcast wants are sent as want-have
            MessageOuterClass.Message.Wantlist.WantType wantType =
                    MessageOuterClass.Message.Wantlist.WantType.Have;

            message.entry(c, priority, wantType, false);

            priority--;
        }

        if (message.empty()) {
            return;
        }

        writeMessage(conn, message);

    }

    void sendWantsMessage(@NonNull QuicConnection conn, @NonNull List<Cid> wants) {
        sendWants(conn, wants);
    }

    public void sendWants(@NonNull QuicConnection conn, @NonNull List<Cid> wants) {

        if (wants.size() == 0) {
            return;
        }
        BitSwapMessage message = BitSwapMessage.create(false);

        int priority = Integer.MAX_VALUE;

        for (Cid c : wants) {

            message.entry(c, priority,
                    MessageOuterClass.Message.Wantlist.WantType.Block, true);

            priority--;
        }

        if (message.empty()) {
            return;
        }

        writeMessage(conn, message);

    }
}

