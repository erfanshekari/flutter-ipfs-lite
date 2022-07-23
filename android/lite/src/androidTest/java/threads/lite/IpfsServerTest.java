package threads.lite;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.luminis.quic.QuicConnection;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import threads.lite.cid.Cid;
import threads.lite.cid.Multiaddr;
import threads.lite.cid.PeerId;
import threads.lite.core.Progress;
import threads.lite.core.TimeoutCloseable;
import threads.lite.host.Dialer;
import threads.lite.host.PeerInfo;
import threads.lite.push.PushService;


@RunWith(AndroidJUnit4.class)
public class IpfsServerTest {

    private static final String TAG = IpfsServerTest.class.getSimpleName();
    private static Context context;

    @BeforeClass
    public static void setup() {
        context = ApplicationProvider.getApplicationContext();
    }


    @Test
    public void server_test() throws Exception {

        IPFS ipfs = TestEnv.getTestInstance(context);

        Thread.sleep(5000);
        DUMMY dummy = DUMMY.getInstance(context);
        try {
            AtomicBoolean incomingConnection = new AtomicBoolean(false);
            ipfs.setIncomingConnection(connection -> incomingConnection.set(true));
            Multiaddr multiaddr = ipfs.defaultListenAddress();

            PeerId host = ipfs.self();
            assertNotNull(host);

            QuicConnection conn = Dialer.dial(dummy.getHost(), ipfs.self(), multiaddr,
                    IPFS.CONNECT_TIMEOUT, IPFS.GRACE_PERIOD,
                    IPFS.MAX_STREAMS, IPFS.MESSAGE_SIZE_MAX);
            Objects.requireNonNull(conn);
            dummy.getHost().swarmEnhance(conn);
            assertTrue(dummy.getHost().swarmContains(conn));


            PeerInfo info = dummy.getPeerInfo(conn);
            assertNotNull(info);
            assertEquals(info.getAgent(), IPFS.AGENT);
            assertNotNull(info.getObserved());

            // simple push test
            String data = "moin";
            AtomicBoolean notified = new AtomicBoolean(false);
            ipfs.setIncomingPush((content) -> notified.set(content.equals(data)));
            PushService.notify(conn, data);


            Thread.sleep(1000);
            assertTrue(notified.get());
            assertTrue(incomingConnection.get());

            // simple data test
            String text = "Hallo das ist ein Test";
            Cid cid = ipfs.storeText(text);
            assertNotNull(cid);
            String cmpText = dummy.getText(cid, new TimeoutCloseable(10));
            assertEquals(text, cmpText);
        } finally {
            dummy.shutdown();
        }

    }

    @Test
    public void server_stress_test() throws Exception {

        IPFS ipfs = TestEnv.getTestInstance(context);

        Thread.sleep(5000);
        DUMMY dummy = DUMMY.getInstance(context);
        try {

            PeerId host = ipfs.self();
            assertNotNull(host);
            Multiaddr multiaddr = ipfs.defaultListenAddress();

            byte[] input = RandomStringUtils.randomAlphabetic(1000000).getBytes();

            Cid cid = ipfs.storeData(input);
            assertNotNull(cid);

            byte[] cmp = ipfs.getData(cid, new TimeoutCloseable(30));
            assertArrayEquals(input, cmp);

            List<Cid> cids = ipfs.getBlocks(cid);
            LogUtils.debug(TAG, "Links " + cids.size());


            QuicConnection conn = Dialer.dial(dummy.getHost(), host, multiaddr,
                    IPFS.CONNECT_TIMEOUT, IPFS.GRACE_PERIOD,
                    IPFS.MAX_STREAMS, IPFS.MESSAGE_SIZE_MAX);
            Objects.requireNonNull(conn);
            dummy.getHost().swarmEnhance(conn);
            assertTrue(dummy.getHost().swarmContains(conn));

            PeerInfo info = dummy.getPeerInfo(conn);
            assertNotNull(info);
            assertEquals(info.getAgent(), IPFS.AGENT);
            assertNotNull(info.getObserved());


            byte[] output = dummy.getData(cid, new Progress() {
                @Override
                public void setProgress(int progress) {
                    LogUtils.debug(TAG, "" + progress);
                }

                @Override
                public boolean doProgress() {
                    return true;
                }

                @Override
                public boolean isClosed() {
                    return false;
                }
            });
            assertArrayEquals(input, output);
        } finally {
            dummy.shutdown();
        }
    }


    @Test
    public void server_multiple_dummy_conn() throws Exception {

        IPFS ipfs = TestEnv.getTestInstance(context);
        Thread.sleep(5000);

        for (int i = 0; i < 5; i++) {
            DUMMY dummy = DUMMY.getInstance(context);


            PeerId server = ipfs.self();
            LogUtils.debug(TAG, "Server :" + server.toBase58());
            PeerId client = dummy.getPeerID();
            LogUtils.debug(TAG, "Client :" + client.toBase58());

            Multiaddr multiaddr = ipfs.defaultListenAddress();


            QuicConnection conn = Dialer.dial(
                    dummy.getHost(), server, multiaddr,
                    IPFS.CONNECT_TIMEOUT, IPFS.GRACE_PERIOD, IPFS.MAX_STREAMS,
                    IPFS.MESSAGE_SIZE_MAX);
            assertNotNull(conn);
            dummy.getHost().swarmEnhance(conn);

            PeerInfo info = dummy.getPeerInfo(conn);
            assertNotNull(info);
            assertEquals(info.getAgent(), IPFS.AGENT);
            assertNotNull(info.getObserved());

            byte[] input = RandomStringUtils.randomAlphabetic(500).getBytes();

            Cid cid = ipfs.storeData(input);
            assertNotNull(cid);

            byte[] output = dummy.getData(cid, new TimeoutCloseable(10));
            assertArrayEquals(input, output);

            dummy.shutdown();
        }

    }


}