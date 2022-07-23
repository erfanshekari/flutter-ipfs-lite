package threads.lite;


import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import threads.lite.cid.Cid;
import threads.lite.cid.PeerId;
import threads.lite.core.TimeoutCloseable;
import threads.lite.ipns.Ipns;

@RunWith(AndroidJUnit4.class)
public class IpfsProvideTest {
    private static final String TAG = IpfsProvideTest.class.getSimpleName();
    private static Context context;

    @BeforeClass
    public static void setup() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void test_resolve_provide() throws IOException {
        IPFS ipfs = TestEnv.getTestInstance(context);

        LogUtils.debug(TAG, ipfs.self().toBase58());
        byte[] data = RandomStringUtils.randomAlphabetic(100).getBytes();
        Cid cid = ipfs.storeData(data);
        assertNotNull(cid);

        long start = System.currentTimeMillis();

        ipfs.provide(cid, new TimeoutCloseable(30));

        LogUtils.debug(TAG, "Time provide " + (System.currentTimeMillis() - start));

        long time = System.currentTimeMillis();
        AtomicReference<PeerId> lastPeer = new AtomicReference<>(null);

        ipfs.findProviders((peer) -> lastPeer.set(peer.getPeerId()), cid, new TimeoutCloseable(30));

        LogUtils.debug(TAG, "Time Providers : " + (System.currentTimeMillis() - time) + " [ms]");
        assertEquals(lastPeer.get(), ipfs.self());
    }


    @Test
    public void test_host_provide() {
        IPFS ipfs = TestEnv.getTestInstance(context);

        PeerId self = ipfs.self();

        Cid cid = Cid.decode(self.toBase58());
        assertNotNull(cid);

        boolean result = Arrays.equals(cid.getHash(), self.getBytes());


        assertTrue(result);

        long start = System.currentTimeMillis();

        ipfs.provide(cid, new TimeoutCloseable(30));

        LogUtils.debug(TAG, "Time provide " + (System.currentTimeMillis() - start));

        long time = System.currentTimeMillis();
        AtomicBoolean finished = new AtomicBoolean(false);

        ipfs.findProviders((peer) -> finished.set(true), cid, new TimeoutCloseable(30));

        LogUtils.debug(TAG, "Time Providers : " + (System.currentTimeMillis() - time) + " [ms]");
        assertTrue(finished.get());
    }

    //@Test
    public void test_host_publish_find() {
        IPFS ipfs = TestEnv.getTestInstance(context);

        PeerId self = ipfs.self();

        LogUtils.debug(TAG, self.toBase58());


        long start = System.currentTimeMillis();
        ipfs.publishName("hallo", 0, new TimeoutCloseable(30));

        LogUtils.debug(TAG, "Time provide " + (System.currentTimeMillis() - start));

        long time = System.currentTimeMillis();
        Ipns.Entry res = ipfs.resolveName(self.toBase36(), 0, new TimeoutCloseable(30));
        assertNotNull(res);
        LogUtils.debug(TAG, res.toString());

        LogUtils.debug(TAG, "Time Find Peer : " + (System.currentTimeMillis() - time) + " [ms]");

    }
}
