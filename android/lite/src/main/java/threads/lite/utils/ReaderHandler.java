package threads.lite.utils;

import androidx.annotation.NonNull;

import com.google.common.io.ByteStreams;

import net.luminis.quic.QuicStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import threads.lite.IPFS;
import threads.lite.LogUtils;
import threads.lite.cid.Multihash;

public class ReaderHandler {
    private static final String TAG = ReaderHandler.class.getSimpleName();

    private final ByteArrayOutputStream temp = new ByteArrayOutputStream();
    private int expectedLength;

    public ReaderHandler() {
        this.expectedLength = 0;
    }


    public static void reading(@NonNull QuicStream quicStream,
                               @NonNull Consumer<String> token,
                               @NonNull Consumer<byte[]> consumer,
                               @NonNull Consumer<Void> finished,
                               @NonNull Consumer<Throwable> throwable) {
        ReaderHandler reader = new ReaderHandler();

        quicStream.setConsumer(streamData -> {
            try {
                reader.load(streamData.data, token, consumer);
                if (streamData.fin) {
                    // LogUtils.error(TAG, "stream finished");
                    finished.accept(null);
                    reader.close();
                }
            } catch (Throwable exception) {
                LogUtils.error(TAG, "ExpectedLength " + reader.expectedLength +
                        " Content Length " + reader.temp.size() + " " + exception.getMessage());
                throwable.accept(exception);
                reader.close();
            }
        });
    }

    public static void reading(@NonNull QuicStream quicStream,
                               @NonNull Consumer<String> token,
                               @NonNull Consumer<byte[]> consumer,
                               @NonNull Consumer<Throwable> throwable) {
        reading(quicStream, token, consumer, (fin) -> {
        }, throwable);
    }

    @NonNull
    @Override
    public String toString() {
        return "DataHandler{" +
                ", temp=" + temp +
                ", expectedLength=" + expectedLength +
                '}';
    }

    private void iteration(@NonNull Consumer<String> tokenConsumer,
                           @NonNull Consumer<byte[]> dataConsumer)
            throws IOException {

        // LogUtils.error(TAG, "expected length " + expectedLength + " temp " + temp.size());

        // shortcut
        if (temp.size() < expectedLength) {
            // no reading required
            return;
        }


        try (InputStream inputStream = new ByteArrayInputStream(temp.toByteArray())) {
            expectedLength = (int) Multihash.readVarint(inputStream);
            byte[] tokenData = new byte[expectedLength];
            int read = inputStream.read(tokenData);
            if (read == expectedLength) {
                // expected to be for a token
                if (tokenData[0] == '/' && tokenData[read - 1] == '\n') {
                    String token = new String(tokenData, StandardCharsets.UTF_8);
                    token = token.substring(0, read - 1);
                    tokenConsumer.accept(token);
                } else if (tokenData[0] == 'n' && tokenData[1] == 'a' && tokenData[read - 1] == '\n') {
                    LogUtils.error(TAG, "na token");
                    tokenConsumer.accept(IPFS.NA);
                } else {
                    dataConsumer.accept(tokenData);
                }
                // next iteration
                expectedLength = 0;
                temp.reset();
                long copied = ByteStreams.copy(inputStream, temp);
                if (copied == 0) {
                    temp.reset();
                } else {
                    iteration(tokenConsumer, dataConsumer);
                }
            }
        }
    }

    public void load(@NonNull byte[] data,
                     @NonNull Consumer<String> tokenConsumer,
                     @NonNull Consumer<byte[]> dataConsumer)
            throws IOException {

        temp.write(data);

        iteration(tokenConsumer, dataConsumer);
    }

    private void close() {
        expectedLength = Integer.MAX_VALUE; // make sure no reading
        try {
            temp.close();
        } catch (Throwable throwable) {
            LogUtils.error(TAG, throwable);
        }
    }

}
