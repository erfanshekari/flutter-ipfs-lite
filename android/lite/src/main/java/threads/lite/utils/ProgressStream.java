package threads.lite.utils;

import androidx.annotation.NonNull;

import threads.lite.core.Progress;

public class ProgressStream extends ReaderStream {

    private final Progress progress;
    private final long size;
    private int remember = 0;
    private long totalRead = 0L;

    public ProgressStream(@NonNull Reader reader, @NonNull Progress progress) {
        super(reader);
        this.progress = progress;
        this.size = reader.getSize();
    }


    @Override
    void loadNextData() throws InterruptedException {
        super.loadNextData();

        if (buffer != null) {
            if (progress.doProgress()) {
                totalRead += buffer.size();
                if (size > 0) {
                    int percent = (int) ((totalRead * 100.0f) / size);
                    if (remember < percent) {
                        remember = percent;
                        progress.setProgress(percent);
                    }
                }
            }
        }
    }

}
