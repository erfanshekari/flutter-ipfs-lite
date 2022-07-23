package threads.lite.dht;

import androidx.annotation.NonNull;

import java.util.function.Consumer;

import threads.lite.cid.Cid;
import threads.lite.cid.Peer;
import threads.lite.cid.PeerId;
import threads.lite.core.Closeable;
import threads.lite.ipns.Ipns;

public interface Routing {
    void putValue(@NonNull Closeable closable,
                  @NonNull byte[] key,
                  @NonNull byte[] data) throws InterruptedException;


    void findPeer(@NonNull Closeable closeable,
                  @NonNull Consumer<Peer> consumer,
                  @NonNull PeerId peerID) throws InterruptedException;


    void searchValue(@NonNull Closeable closeable,
                     @NonNull Consumer<Ipns.Entry> consumer,
                     @NonNull byte[] key) throws InterruptedException;


    void findProviders(@NonNull Closeable closeable,
                       @NonNull Consumer<Peer> providers,
                       @NonNull Cid cid) throws InterruptedException;

    void provide(@NonNull Closeable closeable, @NonNull Cid cid) throws InterruptedException;

}
