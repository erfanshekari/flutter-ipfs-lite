package threads.lite.utils;

import androidx.annotation.NonNull;

import java.io.InputStream;

import threads.lite.core.Progress;

public class ReaderInputStream {

    private final InputStream inputStream;
    private final Progress mProgress;
    private final long size;
    private int progress = 0;
    private long totalRead = 0;
    private boolean done;


    public ReaderInputStream(@NonNull InputStream inputStream, @NonNull Progress progress, long size) {
        this.inputStream = inputStream;
        this.mProgress = progress;
        this.size = size;
    }

    public ReaderInputStream(@NonNull InputStream inputStream, long size) {
        this.inputStream = inputStream;
        this.mProgress = null;
        this.size = size;
    }

    public int read(@NonNull byte[] bytes) {

        if (mProgress != null) {
            if (mProgress.isClosed()) {
                throw new RuntimeException("progress closed");
            }
        }
        try {
            int read = inputStream.read(bytes);
            if (read <= 0) {
                done = true;
            } else {
                totalRead += read;
                if (mProgress != null) {
                    if (mProgress.doProgress()) {
                        if (size > 0) {
                            int percent = (int) ((totalRead * 100.0f) / size);
                            if (progress < percent) {
                                progress = percent;
                                mProgress.setProgress(percent);
                            }
                        }
                    }
                }
            }
            return read;
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public boolean done() {
        return done;
    }
}
