package threads.lite.relay;

import androidx.annotation.NonNull;

import net.luminis.quic.QuicConnection;
import net.luminis.quic.QuicConstants;
import net.luminis.quic.QuicStream;
import net.luminis.quic.Statistics;
import net.luminis.quic.Version;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import threads.lite.IPFS;
import threads.lite.cid.PeerId;

public class RelayConnection implements QuicConnection {

    private final PeerId peerId;
    private final QuicConnection conn;

    private RelayConnection(@NonNull QuicConnection relay,
                            @NonNull PeerId peerId) {
        this.conn = relay;
        this.peerId = peerId;
    }

    public static RelayConnection createRelayConnection(@NonNull QuicConnection conn,
                                                        @NonNull PeerId peerId) {
        return new RelayConnection(conn, peerId);

    }

    @Override
    public Version getQuicVersion() {
        return conn.getQuicVersion();
    }

    @Override
    public void setMaxAllowedBidirectionalStreams(int max) {
        conn.setMaxAllowedBidirectionalStreams(max);
    }

    @Override
    public void setMaxAllowedUnidirectionalStreams(int max) {
        conn.setMaxAllowedUnidirectionalStreams(max);
    }

    @Override
    public void setDefaultStreamReceiveBufferSize(long size) {
        conn.setDefaultStreamReceiveBufferSize(size);
    }

    @Override
    public QuicStream createStream(boolean bidirectional) {
        try {
            return RelayService.getStream(conn, peerId, IPFS.RELAY_TIMEOUT, TimeUnit.SECONDS);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    @Override
    public void setPeerInitiatedStreamCallback(Consumer<QuicStream> streamConsumer) {
        throw new RuntimeException("not allowed");
    }

    @Override
    public void close() {
    }

    @Override
    public void close(QuicConstants.TransportErrorCode applicationError, String errorReason) {
    }

    @Override
    public Statistics getStats() {
        return conn.getStats();
    }

    @Override
    public boolean isConnected() {
        return conn.isConnected();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return conn.getRemoteAddress();
    }

    @NonNull
    @Override
    public String toString() {
        return "RelayConnection{" +
                "peerId=" + peerId +
                ", conn=" + conn +
                '}';
    }
}
