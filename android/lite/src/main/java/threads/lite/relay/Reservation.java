package threads.lite.relay;

import androidx.annotation.NonNull;

import net.luminis.quic.QuicConnection;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import circuit.pb.Circuit;
import circuit.pb.Voucher;
import threads.lite.LogUtils;
import threads.lite.cid.Multiaddr;
import threads.lite.cid.PeerId;

public class Reservation {
    private static final String TAG = Reservation.class.getSimpleName();
    @NonNull
    private final PeerId peerId;
    @NonNull
    private final QuicConnection conn;
    @NonNull
    private final Multiaddr observed;
    @NonNull
    private final Multiaddr multiaddr;
    @NonNull
    private final Circuit.Reservation reservation;

    public Reservation(@NonNull PeerId peerId,
                       @NonNull QuicConnection conn,
                       @NonNull Multiaddr multiaddr,
                       @NonNull Multiaddr observed,
                       @NonNull Circuit.Reservation reservation) {
        this.peerId = peerId;
        this.conn = conn;
        this.multiaddr = multiaddr;
        this.observed = observed;
        this.reservation = reservation;
        if (LogUtils.isDebug()) {
            // currently no reason to get the voucher
            voucherDebug();
        }
    }

    @NonNull
    public Multiaddr getMultiaddr() {
        return multiaddr;
    }

    @NonNull
    public Multiaddr getObserved() {
        return observed;
    }

    @NonNull
    public Circuit.Reservation getReservation() {
        return reservation;
    }

    public long expireInMinutes() {
        Date expire = new Date(reservation.getExpire() * 1000);
        Date now = new Date();
        long duration = expire.getTime() - now.getTime();
        return TimeUnit.MILLISECONDS.toMinutes(duration);
    }

    @NonNull
    @Override
    public String toString() {
        return "Reservation{" +
                "observed=" + observed +
                ", multiaddr=" + multiaddr +
                '}';
    }

    private void voucherDebug() {
        if (reservation.hasVoucher()) {
            try {
                Voucher.ReservationVoucher voucher =
                        Voucher.ReservationVoucher.parseFrom(reservation.getVoucher());

                LogUtils.error(TAG, "hasExpiration " + voucher.hasExpiration());
                LogUtils.error(TAG, "Expiration " + voucher.getExpiration());

                LogUtils.error(TAG, new String(voucher.getPeer().toByteArray()));
                LogUtils.error(TAG, new String(voucher.getRelay().toByteArray()));

                LogUtils.error(TAG, "Relay " + new Multiaddr(voucher.getRelay().toByteArray()));
                LogUtils.error(TAG, "Peer " + new Multiaddr(voucher.getPeer().toByteArray()));

            } catch (Throwable throwable) {
                LogUtils.debug(TAG, "hasVoucher " + throwable.getMessage());
            }
        }
    }

    @NonNull
    public PeerId getPeerId() {
        return peerId;
    }

    @NonNull
    public QuicConnection getConnection() {
        return conn;
    }
}
