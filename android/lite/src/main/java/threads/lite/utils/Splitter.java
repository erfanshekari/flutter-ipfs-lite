package threads.lite.utils;

import androidx.annotation.NonNull;

public interface Splitter {

    int nextBytes(@NonNull byte[] bytes);

    boolean done();
}
