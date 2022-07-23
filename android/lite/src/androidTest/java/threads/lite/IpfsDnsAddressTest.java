package threads.lite;


import static org.junit.Assert.assertNotNull;

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
public class IpfsDnsAddressTest {

    private static Context context;

    @BeforeClass
    public static void setup() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void test_dnsAddress() {
        IPFS ipfs = TestEnv.getTestInstance(context);

        PeerId peerId = PeerId.fromBase58("QmNnooDu7bfjPFoTZYxMNLWUQJyrVwtbZg5gBMjTezGAJN");
        Peer peer = new Peer(peerId, new Multiaddr(
                "/dnsaddr/bootstrap.libp2p.io/p2p/QmNnooDu7bfjPFoTZYxMNLWUQJyrVwtbZg5gBMjTezGAJN"));

        QuicConnection result = ipfs.connect(peer, IPFS.CONNECT_TIMEOUT, IPFS.GRACE_PERIOD,
                IPFS.MAX_STREAMS);
        assertNotNull(result);


    }
}
