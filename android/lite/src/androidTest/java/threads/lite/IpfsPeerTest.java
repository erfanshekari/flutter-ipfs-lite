package threads.lite;


import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import junit.framework.TestCase;

import net.luminis.quic.QuicConnection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import threads.lite.cid.Multiaddr;
import threads.lite.cid.Peer;
import threads.lite.cid.PeerId;
import threads.lite.core.TimeoutCloseable;
import threads.lite.relay.Reservation;

@RunWith(AndroidJUnit4.class)
public class IpfsPeerTest {
    private static final String TAG = IpfsPeerTest.class.getSimpleName();
    private static Context context;

    @BeforeClass
    public static void setup() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void test_connectAndFindPeer() {

        IPFS ipfs = TestEnv.getTestInstance(context);

        Multiaddr multiaddr = new Multiaddr("/ip4/139.178.68.145/udp/4001/quic");
        assertTrue(multiaddr.isIP4());
        Peer peer = new Peer(PeerId.decodeName("12D3KooWSzPeHsfxULJwFiLeq6Qsx6TruezAwjZ619qsLhqC7cUR"),
                multiaddr);

        LogUtils.debug(TAG, peer.toString());
        QuicConnection conn = ipfs.connect(peer, IPFS.RESOLVE_TIMEOUT,
                IPFS.GRACE_PERIOD, IPFS.MAX_STREAMS);
        assertNotNull(conn);
        TestCase.assertTrue(conn.isConnected());
        AtomicBoolean found = new AtomicBoolean(false);
        ipfs.findPeer(peer.getPeerId(), peer1 -> {
            LogUtils.debug(TAG, peer1.toString());
            found.set(true);
        }, new TimeoutCloseable(found::get, 30));
        assertTrue(found.get());
    }

    @Test
    public void test_findPeer() {

        IPFS ipfs = TestEnv.getTestInstance(context);

        ConcurrentHashMap<PeerId, Reservation> reservations = ipfs.reservations();
        assertFalse(reservations.isEmpty());
        for (Reservation reservation : reservations.values()) {
            PeerId relayId = reservation.getPeerId();
            LogUtils.debug(TAG, relayId.toString());
            QuicConnection conn = reservation.getConnection();
            assertNotNull(conn);
            TestCase.assertTrue(conn.isConnected());
            AtomicBoolean found = new AtomicBoolean(false);
            ipfs.findPeer(relayId, peer -> {
                LogUtils.debug(TAG, peer.toString());
                found.set(true);
            }, new TimeoutCloseable(found::get, 30));
            // assertTrue(found.get());
        }
    }
}