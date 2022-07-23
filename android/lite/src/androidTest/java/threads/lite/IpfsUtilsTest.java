package threads.lite;


import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.util.Collections;
import java.util.TreeSet;

import threads.lite.cid.Cid;
import threads.lite.cid.Peer;
import threads.lite.cid.PeerId;
import threads.lite.dht.ID;
import threads.lite.dht.PeerDistanceSorter;
import threads.lite.dht.Util;


@RunWith(AndroidJUnit4.class)
public class IpfsUtilsTest {

    private static Context context;

    @BeforeClass
    public static void setup() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void decode_name() {

        IPFS ipfs = TestEnv.getTestInstance(context);

        // test of https://docs.ipfs.io/how-to/address-ipfs-on-web/#http-gateways
        PeerId test = PeerId.fromBase58("QmNnooDu7bfjPFoTZYxMNLWUQJyrVwtbZg5gBMjTezGAJN");
        assertEquals("k2k4r8jl0yz8qjgqbmc2cdu5hkqek5rj6flgnlkyywynci20j0iuyfuj", test.toBase36());

        Cid cid = Cid.decode("QmbWqxBEKC3P8tqsKc98xmWNzrzDtRLMiMPL8wBuTGsMnR");

        assertEquals(cid.String(),
                "QmbWqxBEKC3P8tqsKc98xmWNzrzDtRLMiMPL8wBuTGsMnR");
        assertEquals(Cid.newCidV1(cid.getType(), cid.bytes()).String(),
                "bafybeigdyrzt5sfp7udm7hu76uh7y26nf3efuylqabf3oclgtqy55fbzdi");

        String self = ipfs.self().toBase58();
        assertEquals(self, ipfs.decodeName(ipfs.self().toBase36()));
    }

    @Test
    public void cat_utils() {

        IPFS ipfs = TestEnv.getTestInstance(context);

        PeerId peerId = ipfs.self();

        Peer peer = new Peer(peerId, new TreeSet<>());

        ID a = ID.convertPeerID(peerId);
        ID b = ID.convertPeerID(peerId);


        BigInteger dist = Util.Distance(a, b);
        assertEquals(dist.longValue(), 0L);

        int res = Util.CommonPrefixLen(a, b);
        assertEquals(res, (a.data.length * 8));

        int cmp = a.compareTo(b);
        assertEquals(0, cmp);


        PeerId randrom = PeerId.random();
        Peer randomPeer = new Peer(peerId, new TreeSet<>());
        ID r1 = ID.convertPeerID(randrom);
        ID r2 = ID.convertPeerID(randrom);

        BigInteger distCmp = Util.Distance(a, r1);
        assertNotEquals(distCmp.longValue(), 0L);

        int rres = Util.CommonPrefixLen(r1, r2);
        assertEquals(rres, (r1.data.length * 8));

        int rcmp = r1.compareTo(r2);
        assertEquals(0, rcmp);

        PeerDistanceSorter pds = new PeerDistanceSorter(a);
        pds.appendPeer(peer, a);
        pds.appendPeer(randomPeer, r1);
        Collections.sort(pds);
        assertEquals(pds.get(0).getPeer(), peer);
        assertEquals(pds.get(1).getPeer(), randomPeer);


        PeerDistanceSorter pds2 = new PeerDistanceSorter(a);
        pds2.appendPeer(randomPeer, r1);
        pds2.appendPeer(peer, a);
        Collections.sort(pds2);
        assertEquals(pds2.get(0).getPeer(), peer);
        assertEquals(pds2.get(1).getPeer(), randomPeer);


        PeerDistanceSorter pds3 = new PeerDistanceSorter(r1);
        pds3.appendPeer(peer, a);
        pds3.appendPeer(randomPeer, r1);
        Collections.sort(pds3);
        assertEquals(pds3.get(0).getPeer(), randomPeer);
        assertEquals(pds3.get(1).getPeer(), peer);

    }
}
