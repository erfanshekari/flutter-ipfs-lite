package threads.lite;

import static junit.framework.TestCase.assertNotNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import net.luminis.quic.QuicConnection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import threads.lite.cid.Multiaddr;
import threads.lite.cid.Peer;
import threads.lite.cid.PeerId;

@RunWith(AndroidJUnit4.class)
public class IpfsQuicTest {


    private static Context context;

    @BeforeClass
    public static void setup() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void test_1() {

        IPFS ipfs = TestEnv.getTestInstance(context);

        PeerId peerId = PeerId.fromBase58("QmNnooDu7bfjPFoTZYxMNLWUQJyrVwtbZg5gBMjTezGAJN");
        Multiaddr multiaddr = new Multiaddr("/ip4/147.75.109.213/udp/4001/quic");
        Peer peer = new Peer(peerId, multiaddr);
        QuicConnection conn = ipfs.connect(peer, IPFS.CONNECT_TIMEOUT, IPFS.GRACE_PERIOD,
                IPFS.MAX_STREAMS);
        assertNotNull(conn);

        conn.close();

    }

}


