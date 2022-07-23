package threads.lite;


import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import threads.lite.cid.Cid;
import threads.lite.core.TimeoutCloseable;
import threads.lite.ipns.Ipns;


@RunWith(AndroidJUnit4.class)
public class IpfsLoadContentTest {
    private static final String TAG = IpfsLoadContentTest.class.getSimpleName();


    private static Context context;

    @BeforeClass
    public static void setup() {
        context = ApplicationProvider.getApplicationContext();
    }


    @Test
    public void find_peer_corbett() throws IOException, InterruptedException {

        IPFS ipfs = TestEnv.getTestInstance(context);

        //CorbettReport ipns://k2k4r8jllj4k33jxoa4vaeleqkrwu8b7tqz7tgczhptbfkhqr2i280fm

        String key = "k2k4r8jllj4k33jxoa4vaeleqkrwu8b7tqz7tgczhptbfkhqr2i280fm";

        Ipns.Entry res = ipfs.resolveName(key, 0, new TimeoutCloseable(30));
        assertNotNull(res);
        LogUtils.debug(TAG, res.toString());

        assertTrue(ipfs.isValid(res.getHash()));
        Cid cid = Cid.decode(res.getHash());
        assertEquals(cid.getVersion(), 0);
        assertEquals(res.getHash(), cid.String());

        Cid node = ipfs.resolve(cid, List.of(IPFS.INDEX_HTML), new TimeoutCloseable(60));
        assertNotNull(node);

        String text = ipfs.getText(node, new TimeoutCloseable(30));

        assertNotNull(text);
        TestCase.assertFalse(text.isEmpty());
    }

    @Test
    public void find_peer_freedom() throws IOException, InterruptedException {

        IPFS ipfs = TestEnv.getTestInstance(context);

        //FreedomsPhoenix.com ipns://k2k4r8magsykrprepvtuvd1h8wonxy7rbdkxd09aalsvclqh7wpb28m1

        String key = "k2k4r8magsykrprepvtuvd1h8wonxy7rbdkxd09aalsvclqh7wpb28m1";

        Ipns.Entry res = ipfs.resolveName(key, 0, new TimeoutCloseable(30));
        assertNotNull(res);
        LogUtils.debug(TAG, res.toString());

        assertTrue(ipfs.isValid(res.getHash()));
        Cid cid = Cid.decode(res.getHash());
        assertEquals(cid.getVersion(), 0);
        assertEquals(res.getHash(), cid.String());

        Cid node = ipfs.resolve(cid, List.of(IPFS.INDEX_HTML), new TimeoutCloseable(60));
        assertNotNull(node);

        String text = ipfs.getText(node, new TimeoutCloseable(30));

        assertNotNull(text);
        TestCase.assertFalse(text.isEmpty());
    }

    @Test
    public void find_peer_pirates() throws IOException, InterruptedException {

        IPFS ipfs = TestEnv.getTestInstance(context);


        //PiratesWithoutBorders.com ipns://k2k4r8l8zgv45qm2sjt7p16l7pvy69l4jr1o50cld4s98wbnanl0zn6t

        String key = "k2k4r8l8zgv45qm2sjt7p16l7pvy69l4jr1o50cld4s98wbnanl0zn6t";

        Ipns.Entry res = ipfs.resolveName(key, 0, new TimeoutCloseable(30));
        assertNotNull(res);
        LogUtils.debug(TAG, res.toString());

        assertTrue(ipfs.isValid(res.getHash()));
        Cid cid = Cid.decode(res.getHash());
        assertEquals(cid.getVersion(), 0);
        assertEquals(res.getHash(), cid.String());

        Cid node = ipfs.resolve(cid, List.of(IPFS.INDEX_HTML), new TimeoutCloseable(60));
        assertNotNull(node);

        String text = ipfs.getText(node, new TimeoutCloseable(30));

        assertNotNull(text);
        TestCase.assertFalse(text.isEmpty());
    }
}
