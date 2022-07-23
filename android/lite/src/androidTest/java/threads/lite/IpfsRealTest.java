package threads.lite;


import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import threads.lite.cid.Cid;
import threads.lite.core.TimeoutCloseable;
import threads.lite.format.Link;
import threads.lite.host.DnsResolver;
import threads.lite.ipns.Ipns;

@RunWith(AndroidJUnit4.class)
public class IpfsRealTest {
    private static final String TAG = IpfsRealTest.class.getSimpleName();
    private static Context context;

    @BeforeClass
    public static void setup() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void test_corbett() {

        IPFS ipfs = TestEnv.getTestInstance(context);

        //CorbettReport ipns://k2k4r8jllj4k33jxoa4vaeleqkrwu8b7tqz7tgczhptbfkhqr2i280fm
        String key = "k2k4r8jllj4k33jxoa4vaeleqkrwu8b7tqz7tgczhptbfkhqr2i280fm";

        Ipns.Entry res = ipfs.resolveName(key, 0, new TimeoutCloseable(30));
        assertNotNull(res);

        assertTrue(ipfs.isValid(res.getHash()));
        Cid cid = Cid.decode(res.getHash());
        assertEquals(cid.getVersion(), 0);
        assertEquals(res.getHash(), cid.String());

        AtomicBoolean atomicBoolean = new AtomicBoolean(false);


        ipfs.findProviders((peer) -> atomicBoolean.set(true), Cid.decode(res.getHash()),
                new TimeoutCloseable(30));


        assertTrue(atomicBoolean.get());
    }

    @Test
    public void test_blog_ipfs_io() throws InterruptedException, IOException {

        IPFS ipfs = TestEnv.getTestInstance(context);

        String link = DnsResolver.resolveDnsLink("blog.ipfs.io");

        assertNotNull(link);
        assertFalse(link.isEmpty());
        Cid cid = Cid.decode(link.replace(IPFS.IPFS_PATH, ""));

        Cid node = ipfs.resolve(cid, Collections.emptyList(), new TimeoutCloseable(60));
        assertNotNull(node);
        assertEquals(node.getVersion(), 0);

        List<Link> links = ipfs.ls(node, false, () -> false);
        assertNotNull(links);
        for (Link lnk : links) {
            LogUtils.debug(TAG, lnk.toString());
        }

        node = ipfs.resolve(node, List.of(IPFS.INDEX_HTML), new TimeoutCloseable(60));
        assertNotNull(node);
        assertEquals(node.getVersion(), 0);

        String text = ipfs.getText(node, new TimeoutCloseable(30));

        assertNotNull(text);
        assertFalse(text.isEmpty());


    }

    @Test
    public void test_unknown() throws InterruptedException {

        IPFS ipfs = TestEnv.getTestInstance(context);

        Cid node = ipfs.resolve(Cid.decode("QmavE42xtK1VovJFVTVkCR5Jdf761QWtxmvak9Zx718TVr"),
                Collections.emptyList(), new TimeoutCloseable(60));
        assertNotNull(node);
        assertEquals(node.getVersion(), 0);


        List<Link> links = ipfs.getLinks(node, false, new TimeoutCloseable(1));
        assertNotNull(links);
        assertFalse(links.isEmpty());


    }

    @Test
    public void test_unknown_2() throws InterruptedException {

        IPFS ipfs = TestEnv.getTestInstance(context);

        Cid node = ipfs.resolve(Cid.decode("QmfQiLpdBDbSkb2oySwFHzNucvLkHmGFxgK4oA2BUSwi4t"),
                Collections.emptyList(), new TimeoutCloseable(60));
        assertNotNull(node);
        assertEquals(node.getVersion(), 0);


        List<Link> links = ipfs.getLinks(node, false, new TimeoutCloseable(1));
        assertNotNull(links);
        assertFalse(links.isEmpty());
    }

}
