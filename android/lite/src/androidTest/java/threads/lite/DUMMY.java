package threads.lite;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protobuf.ByteString;

import net.luminis.quic.QuicConnection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import threads.lite.cid.Cid;
import threads.lite.cid.PeerId;
import threads.lite.core.Closeable;
import threads.lite.core.Progress;
import threads.lite.crypto.PrivKey;
import threads.lite.crypto.Rsa;
import threads.lite.format.Block;
import threads.lite.format.BlockStore;
import threads.lite.host.LiteHost;
import threads.lite.host.LiteHostCertificate;
import threads.lite.host.PeerInfo;
import threads.lite.ident.IdentityService;
import threads.lite.utils.Reader;
import threads.lite.utils.ReaderStream;

public class DUMMY {

    private static final String TAG = DUMMY.class.getSimpleName();
    @NonNull
    private final LiteHost host;
    @NonNull
    private final BlockStore blockstore;

    private DUMMY(@NonNull Context context) throws Exception {
        KeyPair keypair = getKeyPair();
        PrivKey privateKey = new Rsa.RsaPrivateKey(keypair.getPrivate(), keypair.getPublic());
        LiteHostCertificate selfSignedCertificate = new LiteHostCertificate(context,
                privateKey, keypair);

        this.blockstore = new BlockStore() {
            private final ConcurrentHashMap<Cid, Block> blocks = new ConcurrentHashMap<>();

            @Override
            public boolean hasBlock(@NonNull Cid cid) {
                return blocks.containsKey(cid);
            }

            @Override
            public Block getBlock(@NonNull Cid cid) {
                return blocks.get(cid);
            }

            @Override
            public void deleteBlock(@NonNull Cid cid) {
                blocks.remove(cid);
            }

            @Override
            public void deleteBlocks(@NonNull List<Cid> cids) {
                for (Cid cid : cids) {
                    deleteBlock(cid);
                }
            }

            @Override
            public void putBlock(@NonNull Block block) {
                blocks.put(block.getCid(), block);
            }

            @Override
            public int getSize(@NonNull Cid cid) {
                Block block = getBlock(cid);
                if (block != null) {
                    return block.getData().length;
                }
                return -1;
            }

            @Override
            public void clear() {
                blocks.clear();
            }
        };

        this.host = new LiteHost(selfSignedCertificate, privateKey, blockstore,
                LiteHost.nextFreePort());

    }


    @NonNull
    public static DUMMY getInstance(@NonNull Context context) {

        synchronized (DUMMY.class) {

            try {
                return new DUMMY(context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

    }

    private KeyPair getKeyPair() throws NoSuchAlgorithmException {

        String algorithm = "RSA";
        final KeyPair keypair;

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm);
        keyGen.initialize(2048, LiteHostCertificate.ThreadLocalInsecureRandom.current());
        keypair = keyGen.generateKeyPair();

        return keypair;
    }


    @Nullable
    public String getText(@NonNull Cid cid, @NonNull Closeable closeable) throws IOException, InterruptedException {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            getToOutputStream(outputStream, cid, closeable);
            return outputStream.toString();
        }
    }

    public void storeToOutputStream(@NonNull OutputStream outputStream, @NonNull Cid cid,
                                    @NonNull Progress progress) throws InterruptedException, IOException {

        long totalRead = 0L;
        int remember = 0;

        Reader reader = getReader(cid, progress);
        long size = reader.getSize();

        do {
            if (progress.isClosed()) {
                throw new InterruptedException();
            }

            ByteString buffer = reader.loadNextData();
            if (buffer == null || buffer.size() <= 0) {
                return;
            }
            outputStream.write(buffer.toByteArray());

            // calculate progress
            totalRead += buffer.size();
            if (progress.doProgress()) {
                if (size > 0) {
                    int percent = (int) ((totalRead * 100.0f) / size);
                    if (remember < percent) {
                        remember = percent;
                        progress.setProgress(percent);
                    }
                }
            }
        } while (true);
    }

    public void storeToOutputStream(@NonNull OutputStream outputStream,
                                    @NonNull Cid cid,
                                    @NonNull Closeable closeable)
            throws IOException, InterruptedException {

        Reader reader = getReader(cid, closeable);

        do {
            ByteString buffer = reader.loadNextData();
            if (buffer == null || buffer.size() <= 0) {
                return;
            }
            outputStream.write(buffer.toByteArray());
        } while (true);
    }

    @NonNull
    public byte[] getData(@NonNull Cid cid, @NonNull Progress progress) throws IOException, InterruptedException {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            storeToOutputStream(outputStream, cid, progress);
            return outputStream.toByteArray();
        }
    }

    @NonNull
    public byte[] getData(@NonNull Cid cid, @NonNull Closeable closeable) throws IOException, InterruptedException {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            storeToOutputStream(outputStream, cid, closeable);
            return outputStream.toByteArray();
        }
    }

    @NonNull
    public PeerId getPeerID() {
        return host.self();
    }

    @NonNull
    public PeerInfo getPeerInfo(@NonNull QuicConnection conn)
            throws Exception {
        return IdentityService.getPeerInfo(conn);
    }

    public void shutdown() {
        try {
            host.shutdown();
        } catch (Throwable throwable) {
            LogUtils.error(TAG, throwable);
        }
    }

    @NonNull
    public Reader getReader(@NonNull Cid cid, @NonNull Closeable closeable) throws InterruptedException {
        return Reader.getReader(closeable, blockstore, host.getBitSwap(), cid);
    }

    private void getToOutputStream(@NonNull OutputStream outputStream, @NonNull Cid cid,
                                   @NonNull Closeable closeable) throws IOException, InterruptedException {
        try (InputStream inputStream = getInputStream(cid, closeable)) {
            IPFS.copy(inputStream, outputStream);
        }
    }

    @NonNull
    public InputStream getInputStream(@NonNull Cid cid, @NonNull Closeable closeable) throws InterruptedException {
        Reader reader = getReader(cid, closeable);
        return new ReaderStream(reader);
    }

    @NonNull
    public LiteHost getHost() {
        return host;
    }


}
