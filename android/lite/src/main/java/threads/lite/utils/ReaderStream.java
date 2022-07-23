package threads.lite.utils;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.io.InputStream;


public class ReaderStream extends InputStream {

    private final Reader reader;
    protected ByteString buffer = null;
    private int positionBuffer = 0;

    public ReaderStream(@NonNull Reader reader) {
        this.reader = reader;
    }


    @Override
    public int available() {
        long size = reader.getSize();
        return (int) size;
    }

    void loadNextData() throws InterruptedException {
        positionBuffer = 0;
        buffer = reader.loadNextData();
    }

    @Override
    public int read() throws IOException {

        try {
            if (buffer == null) {
                loadNextData();
            }
            if (buffer == null || buffer.size() <= 0) {
                return -1;
            }
            if (positionBuffer < buffer.size()) {
                return buffer.byteAt(positionBuffer++) & 0xFF;
            } else {
                loadNextData();
                if (buffer == null || buffer.size() <= 0) {
                    return -1;
                }
                if (positionBuffer < buffer.size()) {
                    return buffer.byteAt(positionBuffer++) & 0xFF;
                }
                return -1;
            }
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }
}
