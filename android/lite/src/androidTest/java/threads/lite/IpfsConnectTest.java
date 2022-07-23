package threads.lite;


import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.luminis.quic.QuicConnection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.ConnectException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import threads.lite.cid.Multiaddr;
import threads.lite.cid.Peer;
import threads.lite.cid.PeerId;
import threads.lite.core.TimeoutCloseable;
import threads.lite.host.PeerInfo;
import threads.lite.relay.Reservation;


@RunWith(AndroidJUnit4.class)
public class IpfsConnectTest {
    private static final String TAG = IpfsConnectTest.class.getSimpleName();

    private static final String DUMMY_PID = "QmVLnkyetpt7JNpjLmZX21q9F8ZMnhBts3Q53RcAGxWH6V";

    private static Context context;

    @BeforeClass
    public static void setup() {
        context = ApplicationProvider.getApplicationContext();
    }


    @Test(expected = ConnectException.class)
    public void swarm_connect() throws InterruptedException, ConnectException {

        IPFS ipfs = TestEnv.getTestInstance(context);
        PeerId pc = PeerId.fromBase58("QmRxoQNy1gNGMM1746Tw8UBNBF8axuyGkzcqb2LYFzwuXd");


        // multiaddress is just a fiction
        ipfs.dial(pc, new Multiaddr("/ip4/139.178.68.146/udp/4001/quic"),
                IPFS.CONNECT_TIMEOUT, IPFS.GRACE_PERIOD,
                IPFS.MAX_STREAMS, IPFS.MESSAGE_SIZE_MAX);

        fail(); // exception is thrown


    }

    @Test
    public void test_swarm_connect() {
        IPFS ipfs = TestEnv.getTestInstance(context);

        PeerId relay = ipfs.getPeerId("QmchgNzyUFyf2wpfDMmpGxMKHA3PkC1f3H2wUgbs21vXoz");


        LogUtils.debug(TAG, "Stage 1");

        QuicConnection result = ipfs.connect(new Peer(relay, new HashSet<>()),
                IPFS.CONNECT_TIMEOUT, IPFS.GRACE_PERIOD,
                IPFS.MAX_STREAMS);
        assertNull(result);

        LogUtils.debug(TAG, "Stage 2");

        result = ipfs.find(relay, IPFS.CONNECT_TIMEOUT,
                IPFS.MAX_STREAMS, IPFS.MESSAGE_SIZE_MAX, new TimeoutCloseable(10));
        assertNull(result);

        LogUtils.debug(TAG, "Stage 3");

        relay = ipfs.getPeerId(DUMMY_PID);
        result = ipfs.find(relay, IPFS.CONNECT_TIMEOUT,
                IPFS.MAX_STREAMS, IPFS.MESSAGE_SIZE_MAX, new TimeoutCloseable(10));
        assertNull(result);

        LogUtils.debug(TAG, "Stage 4");

    }


    @Test
    public void test_print_swarm_peers() {
        IPFS ipfs = TestEnv.getTestInstance(context);

        ConcurrentHashMap<PeerId, Reservation> reservations = ipfs.reservations();
        for (Reservation reservation : reservations.values()) {
            Peer peer = new Peer(reservation.getPeerId(),
                    reservation.getMultiaddr());
            QuicConnection conn = ipfs.connect(peer, IPFS.CONNECT_TIMEOUT, IPFS.GRACE_PERIOD,
                    IPFS.MAX_STREAMS);
            assertNotNull(conn);
            assertFalse(ipfs.swarmContains(conn));
            assertTrue(ipfs.swarmEnhance(conn));
        }

        List<QuicConnection> quicConnections = ipfs.getSwarm();

        assertNotNull(quicConnections);
        LogUtils.debug(TAG, "Peers : " + quicConnections.size());
        for (QuicConnection conn : quicConnections) {

            try {
                assertNotNull(conn.getRemoteAddress());
                PeerInfo peerInfo = ipfs.getPeerInfo(conn);


                LogUtils.debug(TAG, peerInfo.toString());
                assertNotNull(peerInfo.getAddresses());
                assertNotNull(peerInfo.getAgent());

                Multiaddr observed = peerInfo.getObserved();
                if (observed != null) {
                    LogUtils.debug(TAG, observed.toString());
                }

            } catch (Throwable throwable) {
                LogUtils.debug(TAG, "" + throwable.getClass().getName());
            } finally {
                ipfs.swarmReduce(conn);
            }

        }

    }

}
