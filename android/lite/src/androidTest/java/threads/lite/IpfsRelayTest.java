package threads.lite;


import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import threads.lite.cid.Cid;
import threads.lite.cid.Multiaddr;
import threads.lite.cid.PeerId;
import threads.lite.core.TimeoutCloseable;
import threads.lite.host.PeerInfo;
import threads.lite.ident.IdentityService;
import threads.lite.relay.RelayConnection;
import threads.lite.relay.Reservation;


@RunWith(AndroidJUnit4.class)
public class IpfsRelayTest {
    private static final String TAG = IpfsRelayTest.class.getSimpleName();
    private static Context context;

    @BeforeClass
    public static void setup() {
        context = ApplicationProvider.getApplicationContext();
    }


    @Test
    public void test_relay_reserve_and_connect() throws Throwable {
        IPFS ipfs = TestEnv.getTestInstance(context);


        Thread.sleep(5000);
        DUMMY dummy = DUMMY.getInstance(context);
        try {

            assertTrue(ipfs.hasReservations());

            PeerId peerId = ipfs.self();

            ConcurrentHashMap<PeerId, Reservation> reservations = ipfs.reservations();
            assertFalse(reservations.isEmpty());
            Map.Entry<PeerId, Reservation> entry = reservations.entrySet().stream().
                    findAny().orElseThrow((Supplier<Throwable>) () ->
                            new RuntimeException("at least one present"));


            PeerId relayID = entry.getKey();
            assertNotNull(relayID);
            Multiaddr relayAddr = entry.getValue().getMultiaddr();
            assertNotNull(relayAddr);
            assertNotNull(entry.getValue().getObserved());
            assertNotNull(entry.getValue().getReservation());

            Multiaddr multiaddr = new Multiaddr(relayAddr.toString().
                    replace("/p2p/", "/ipfs/") + "/p2p-circuit"
            );

            RelayConnection conn = dummy.getHost().createRelayConnection(peerId, multiaddr);
            Objects.requireNonNull(conn);

            // TEST 1
            PeerInfo peerInfo = IdentityService.getPeerInfo(conn);
            assertNotNull(peerInfo);
            Multiaddr observed = peerInfo.getObserved();
            assertNotNull(observed);
            assertEquals(peerInfo.getAgent(), IPFS.AGENT);
            LogUtils.debug(TAG, peerInfo.toString());


            // TEST 2
            AtomicBoolean success = new AtomicBoolean(false);
            String data = "moin";
            ipfs.setIncomingPush((ctx) -> success.set(ctx.equals(data)));
            ipfs.notify(conn, data);
            Thread.sleep(1000);
            Assert.assertTrue(success.get());

            // TEST 3
            peerInfo = IdentityService.getPeerInfo(conn);
            assertNotNull(peerInfo);

            // TEST 4
            success.set(false);
            String test = "zehn";
            ipfs.setIncomingPush((ctx) -> success.set(ctx.equals(test)));
            ipfs.notify(conn, test);
            Thread.sleep(1000);
            Assert.assertTrue(success.get());

            assertTrue(ipfs.hasReservations());

        } finally {
            dummy.shutdown();
        }
    }


    //@Test todo
    public void test_findRelays() {
        IPFS ipfs = TestEnv.getTestInstance(context);


        long start = System.currentTimeMillis();

        try {
            AtomicBoolean find = new AtomicBoolean(false);

            ipfs.findProviders((peer) -> find.set(true), Cid.nsToCid(IPFS.RELAY_RENDEZVOUS),
                    new TimeoutCloseable(180));

            assertTrue(find.get());
        } catch (Throwable throwable) {
            fail();
        } finally {
            LogUtils.info(TAG, "Time " + (System.currentTimeMillis() - start));
        }
    }

}
