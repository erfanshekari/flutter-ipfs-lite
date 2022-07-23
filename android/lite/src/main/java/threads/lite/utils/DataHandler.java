package threads.lite.utils;

import androidx.annotation.NonNull;

import com.google.protobuf.MessageLite;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import threads.lite.LogUtils;
import threads.lite.cid.Multihash;

public class DataHandler {
    private static final String TAG = DataHandler.class.getSimpleName();


    public static byte[] encode(@NonNull MessageLite message) {
        return encode(message.toByteArray());
    }

    public static byte[] encode(@NonNull byte[] data) {
        try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            Multihash.putUvarint(buf, data.length);
            buf.write(data);
            return buf.toByteArray();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public static byte[] writeToken(String... tokens) {

        try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            for (String token : tokens) {
                byte[] data = token.getBytes(StandardCharsets.UTF_8);
                Multihash.putUvarint(buf, data.length + 1);
                buf.write(data);
                buf.write('\n');
            }
            return buf.toByteArray();
        } catch (Throwable throwable) {
            LogUtils.error(TAG, throwable);
            throw new RuntimeException(throwable);
        }
    }
}
