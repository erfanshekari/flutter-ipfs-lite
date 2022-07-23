package threads.lite.push;

import androidx.annotation.NonNull;

import net.luminis.quic.QuicConnection;
import net.luminis.quic.QuicStream;

import java.io.OutputStream;

import threads.lite.IPFS;
import threads.lite.LogUtils;
import threads.lite.utils.DataHandler;

public class PushService {
    private static final String TAG = PushService.class.getSimpleName();

    public static void notify(@NonNull QuicConnection conn, @NonNull String content) throws Exception {
        long time = System.currentTimeMillis();

        try {
            QuicStream quicStream = conn.createStream(true);

            OutputStream outputStream = quicStream.getOutputStream();
            outputStream.write(DataHandler.writeToken(IPFS.STREAM_PROTOCOL, IPFS.PUSH_PROTOCOL));
            outputStream.write(DataHandler.encode(content.getBytes()));
            outputStream.close();
        } finally {
            LogUtils.debug(TAG, "Send took " + (System.currentTimeMillis() - time));
        }
    }
}
