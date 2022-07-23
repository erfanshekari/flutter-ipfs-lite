package net.luminis.quic;

import androidx.annotation.NonNull;


public class StreamData {
    public byte[] data;
    public boolean fin;

    @NonNull
    @Override
    public String toString() {
        return "StreamData{" +
                "data=" + new String(data) +
                ", fin=" + fin +
                '}';
    }
}
