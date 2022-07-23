package threads.lite.format;

import androidx.annotation.NonNull;

public class Stage {
    private final Node node;
    private int index;

    Stage(@NonNull Node node) {
        this.node = node;
        this.index = 0;
    }

    public Node getNode() {
        return node;
    }

    public void incrementIndex() {
        index = index + 1;
    }

    public int index() {
        return index;
    }

    public void setIndex(int value) {
        index = value;
    }

    @NonNull
    @Override
    public String toString() {
        return node + " " + index + " ";
    }
}
