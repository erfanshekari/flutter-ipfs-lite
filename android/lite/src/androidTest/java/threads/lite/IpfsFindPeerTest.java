package threads.lite;


import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import threads.lite.cid.Multiaddr;
import threads.lite.cid.PeerId;
import threads.lite.core.TimeoutCloseable;
import threads.lite.ipns.Ipns;


@RunWith(AndroidJUnit4.class)
public class IpfsFindPeerTest {
    private static final String TAG = IpfsFindPeerTest.class.getSimpleName();


    private static Context context;

    @BeforeClass
    public static void setup() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void find_peer_test0() {

        IPFS ipfs = TestEnv.getTestInstance(context);

        PeerId relay = PeerId.fromBase58("QmW9m57aiBDHAkKj9nmFSEn7ZqrcF1fZS4bipsTCHburei");

        AtomicBoolean found = new AtomicBoolean(false);
        ipfs.findPeer(relay, peer -> {

            assertTrue(peer.hasAddresses());

            for (Multiaddr addr : peer.getMultiaddrs()) {
                LogUtils.debug(TAG, addr.toString());
            }
            found.set(true);
        }, new TimeoutCloseable(10));

        assertTrue(found.get());


    }

    @Test
    public void find_peer_corbett() {
        IPFS ipfs = TestEnv.getTestInstance(context);

        //CorbettReport ipns://k2k4r8jllj4k33jxoa4vaeleqkrwu8b7tqz7tgczhptbfkhqr2i280fm

        String key = "k2k4r8jllj4k33jxoa4vaeleqkrwu8b7tqz7tgczhptbfkhqr2i280fm";
        Ipns.Entry res = ipfs.resolveName(key, 0, new TimeoutCloseable(30));
        assertNotNull(res);
        LogUtils.debug(TAG, res.getPeerId().toBase58());
        LogUtils.debug(TAG, res.getKeyType().toString());
        LogUtils.debug(TAG, res.getValue());
        LogUtils.debug(TAG, res.toString());
    }

    @Test
    public void find_peer_freedom() {
        IPFS ipfs = TestEnv.getTestInstance(context);

        //FreedomsPhoenix.com ipns://k2k4r8magsykrprepvtuvd1h8wonxy7rbdkxd09aalsvclqh7wpb28m1

        String key = "k2k4r8magsykrprepvtuvd1h8wonxy7rbdkxd09aalsvclqh7wpb28m1";
        Ipns.Entry res = ipfs.resolveName(key, 0, new TimeoutCloseable(30));
        assertNotNull(res);
        LogUtils.debug(TAG, res.getPeerId().toBase58());
        LogUtils.debug(TAG, res.getKeyType().toString());
        LogUtils.debug(TAG, res.getValue());
        LogUtils.debug(TAG, res.toString());
    }

    @Test
    public void find_peer_pirates() {
        IPFS ipfs = TestEnv.getTestInstance(context);

        //PiratesWithoutBorders.com ipns://k2k4r8l8zgv45qm2sjt7p16l7pvy69l4jr1o50cld4s98wbnanl0zn6t

        String key = "k2k4r8l8zgv45qm2sjt7p16l7pvy69l4jr1o50cld4s98wbnanl0zn6t";
        Ipns.Entry res = ipfs.resolveName(key, 0, new TimeoutCloseable(30));
        assertNotNull(res);
        LogUtils.debug(TAG, res.getPeerId().toBase58());
        LogUtils.debug(TAG, res.getKeyType().toString());
        LogUtils.debug(TAG, res.getValue());
        LogUtils.debug(TAG, res.toString());
    }
}
