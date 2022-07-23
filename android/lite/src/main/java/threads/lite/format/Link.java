package threads.lite.format;

import androidx.annotation.NonNull;

import java.util.Objects;

import threads.lite.cid.Cid;

public final class Link {
    public static final int Raw = 3;
    public static final int File = 2;
    public static final int Dir = 1;
    public static final int Unknown = 8;

    @NonNull
    private final Cid cid;
    @NonNull
    private final String name;
    private final long size;
    private final int type;


    private Link(@NonNull Cid cid, @NonNull String name, long size, int type) {
        this.cid = cid;
        this.name = name;
        this.size = size;
        this.type = type;
    }

    public static Link create(@NonNull Cid cid, @NonNull String name, long size, int type) {
        Objects.requireNonNull(cid);
        Objects.requireNonNull(name);
        return new Link(cid, name, size, type);
    }

    @NonNull
    public Cid getCid() {
        return cid;
    }

    public long getSize() {
        return size;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return size == link.size &&
                type == link.type &&
                cid.equals(link.cid) &&
                name.equals(link.name);
    }

    public boolean isDirectory() {
        return type == Dir;
    }


    @NonNull
    @Override
    public String toString() {
        return "Link{" +
                "cid='" + cid.String() + '\'' +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", type=" + type +
                '}';
    }

    public boolean isFile() {
        return type == File;
    }

    public boolean isUnknown() {
        return type == Unknown;
    }

    public boolean isRaw() {
        return type == Raw;
    }
}
