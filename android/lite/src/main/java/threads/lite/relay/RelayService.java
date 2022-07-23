package threads.lite.relay;

import androidx.annotation.NonNull;

import com.google.protobuf.ByteString;

import net.luminis.quic.QuicConnection;
import net.luminis.quic.QuicStream;

import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import circuit.pb.Circuit;
import threads.lite.IPFS;
import threads.lite.LogUtils;
import threads.lite.cid.Multiaddr;
import threads.lite.cid.PeerId;
import threads.lite.cid.Protocol;
import threads.lite.host.Dialer;
import threads.lite.host.LiteHost;
import threads.lite.utils.DataHandler;
import threads.lite.utils.ReaderHandler;

public class RelayService {

    public static final String TAG = RelayService.class.getSimpleName();

    @NonNull
    public static RelayConnection createRelayConnection(
            @NonNull LiteHost liteHost, @NonNull PeerId peerId, @NonNull Multiaddr multiaddr,
            int timeout, int maxIdleTimeoutInSeconds, int initialMaxStreams,
            int initialMaxStreamData)
            throws Exception {

        boolean hasCircuit = multiaddr.isCircuitAddress();
        if (!hasCircuit) {
            throw new RuntimeException("usage error");
        }

        boolean hasIpfsType = multiaddr.has(Protocol.IPFS);
        if (!hasIpfsType) {
            throw new RuntimeException("wrong format");
        }

        PeerId relayId = PeerId.fromBase58(
                multiaddr.getStringComponent(Protocol.IPFS));
        String host = multiaddr.getHost();
        int port = multiaddr.getPort();

        Multiaddr relayAddr = Multiaddr.transform(new InetSocketAddress(host, port));

        // keepAlive is set to false, only for own relays we are
        // keeping the connection
        QuicConnection conn = Dialer.dial(liteHost, relayId, relayAddr, timeout,
                maxIdleTimeoutInSeconds, initialMaxStreams,
                initialMaxStreamData);
        Objects.requireNonNull(conn);


        return RelayConnection.createRelayConnection(conn, peerId);
    }

    @NonNull
    public static Circuit.Reservation reserve(@NonNull QuicConnection conn) throws Exception {

        Circuit.HopMessage message = Circuit.HopMessage.newBuilder()
                .setType(Circuit.HopMessage.Type.RESERVE).build();

        long time = System.currentTimeMillis();

        QuicStream quicStream = conn.createStream(true);
        OutputStream outputStream = quicStream.getOutputStream();

        outputStream.write(DataHandler.writeToken(IPFS.STREAM_PROTOCOL, IPFS.RELAY_PROTOCOL_HOP));


        CompletableFuture<Circuit.HopMessage> store = new CompletableFuture<>();

        ReaderHandler.reading(quicStream,
                (token) -> {
                    if (!Arrays.asList(IPFS.STREAM_PROTOCOL, IPFS.RELAY_PROTOCOL_HOP).contains(token)) {
                        throw new RuntimeException("Token " + token + " not supported");
                    }
                    try {
                        if (Objects.equals(token, IPFS.RELAY_PROTOCOL_HOP)) {
                            outputStream.write(DataHandler.encode(message));
                            outputStream.close();
                        }
                    } catch (Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                },
                (data) -> {
                    try {
                        store.complete(Circuit.HopMessage.parseFrom(data));
                    } catch (Throwable throwable) {
                        store.completeExceptionally(throwable);
                    }

                }, store::completeExceptionally);

        Circuit.HopMessage msg = store.get(IPFS.CONNECT_TIMEOUT, TimeUnit.SECONDS);

        LogUtils.info(TAG, "Request took " + (System.currentTimeMillis() - time));
        Objects.requireNonNull(msg);

        if (msg.getType() == Circuit.HopMessage.Type.STATUS) {
            if (msg.getStatus() == Circuit.Status.OK) {
                return msg.getReservation();
            }
        }
        throw new Exception("Reservation failed");

    }


    @NonNull
    public static QuicStream getStream(@NonNull QuicConnection conn, @NonNull PeerId peerId,
                                       long timeout, @NonNull TimeUnit timeoutUnit)
            throws Exception {

        Circuit.Peer dest = Circuit.Peer.newBuilder()
                .setId(ByteString.copyFrom(peerId.getBytes())).build();

        Circuit.HopMessage message = Circuit.HopMessage.newBuilder()
                .setType(Circuit.HopMessage.Type.CONNECT)
                .setPeer(dest)
                .build();

        long time = System.currentTimeMillis();

        QuicStream quicStream = conn.createStream(true);

        OutputStream outputStream = quicStream.getOutputStream();
        outputStream.write(DataHandler.writeToken(IPFS.STREAM_PROTOCOL, IPFS.RELAY_PROTOCOL_HOP));

        CompletableFuture<Boolean> done = new CompletableFuture<>();
        ReaderHandler.reading(quicStream,
                (token) -> {
                    if (!Arrays.asList(IPFS.STREAM_PROTOCOL, IPFS.RELAY_PROTOCOL_HOP).contains(token)) {
                        throw new RuntimeException("Token " + token + " not supported");
                    }
                    try {
                        if (Objects.equals(token, IPFS.RELAY_PROTOCOL_HOP)) {
                            outputStream.write(DataHandler.encode(message));
                        }
                    } catch (Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                },
                (data) -> {
                    try {
                        Circuit.HopMessage msg = Circuit.HopMessage.parseFrom(data);

                        LogUtils.info(TAG, "Request took " + (System.currentTimeMillis() - time));
                        Objects.requireNonNull(msg);


                        if (msg.getType() != Circuit.HopMessage.Type.STATUS) {
                            outputStream.close();
                            throw new ConnectException(msg.getType().name());
                        }

                        if (msg.getStatus() != Circuit.Status.OK) {
                            outputStream.close();
                            throw new ConnectException(msg.getStatus().name());
                        }

                        if (msg.hasLimit()) {
                            Circuit.Limit limit = msg.getLimit();
                            if (limit.hasData()) {
                                LogUtils.debug(TAG, "Relay Limit Data " + limit.getData());
                            }
                            if (limit.hasDuration()) {
                                LogUtils.debug(TAG, "Relay Limit Duration " +
                                        limit.getDuration());
                            }
                        }
                        LogUtils.debug(TAG, "Success Relay Stream to " + peerId.toBase58());
                        done.complete(true);
                    } catch (Throwable throwable) {
                        done.completeExceptionally(throwable);
                    }

                },
                done::completeExceptionally);

        done.get(timeout, timeoutUnit);

        return quicStream;

    }

}
