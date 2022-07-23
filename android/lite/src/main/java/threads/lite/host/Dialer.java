package threads.lite.host;

import androidx.annotation.NonNull;

import net.luminis.quic.QuicClientConnectionImpl;
import net.luminis.quic.QuicConnection;
import net.luminis.quic.TransportParameters;
import net.luminis.quic.Version;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import threads.lite.IPFS;
import threads.lite.LogUtils;
import threads.lite.cid.Multiaddr;
import threads.lite.cid.PeerId;

public class Dialer {
    private static final String TAG = Dialer.class.getSimpleName();
    private static final AtomicInteger failure = new AtomicInteger(0);
    private static final AtomicInteger success = new AtomicInteger(0);


    @NonNull
    public static QuicConnection dial(@NonNull LiteHost host, @NonNull PeerId peerId,
                                      @NonNull Multiaddr address, int timeout,
                                      int maxIdleTimeoutInSeconds, int initialMaxStreams,
                                      int initialMaxStreamData)
            throws ConnectException, InterruptedException {


        LiteHostCertificate selfSignedCertificate = host.getSelfSignedCertificate();

        boolean relayConnection = address.isCircuitAddress();
        if (relayConnection) {
            throw new RuntimeException("Relays can not be dialed here");
        }

        long start = System.currentTimeMillis();
        boolean run = false;
        try {
            QuicClientConnectionImpl conn = QuicClientConnectionImpl.newBuilder()
                    .version(Version.IETF_draft_29) // in the future switch to version 1
                    .noServerCertificateCheck()
                    .clientCertificate(selfSignedCertificate.cert())
                    .clientCertificateKey(selfSignedCertificate.key())
                    .host(address.getHost())
                    .port(address.getPort())
                    .build();

            Objects.requireNonNull(conn);

            conn.connect(timeout, IPFS.APRN, new TransportParameters(
                    maxIdleTimeoutInSeconds, initialMaxStreamData,
                    initialMaxStreams, 0), null);

            if (initialMaxStreams > 0) {
                conn.setPeerInitiatedStreamCallback(
                        (quicStream) -> new StreamHandler(quicStream, host));
            }

            run = true;
            return conn;
        } catch (IOException e) {
            throw new ConnectException(e.getMessage());
        } finally {
            if (LogUtils.isDebug()) {
                if (run) {
                    success.incrementAndGet();
                } else {
                    failure.incrementAndGet();
                }
                LogUtils.debug(TAG, "Run dialClient " + run + " Success " + success.get() + " " +
                        "Failure " + failure.get() +
                        " Peer " + peerId.toBase58() + " " +
                        address + " " + (System.currentTimeMillis() - start));
            }
        }
    }

}
