package threads.lite.host;

import androidx.annotation.NonNull;

import net.luminis.quic.QuicConnection;
import net.luminis.quic.QuicStream;
import net.luminis.quic.server.ApplicationProtocolConnection;

import java.util.function.Consumer;

public class ServerHandler extends ApplicationProtocolConnection implements Consumer<QuicStream> {

    private final LiteHost liteHost;

    public ServerHandler(@NonNull LiteHost liteHost, @NonNull QuicConnection quicConnection) {
        this.liteHost = liteHost;

        quicConnection.setPeerInitiatedStreamCallback(this);
        liteHost.incomingConnection(quicConnection);
    }

    @Override
    public void accept(QuicStream quicStream) {
        new StreamHandler(quicStream, liteHost);
    }
}
