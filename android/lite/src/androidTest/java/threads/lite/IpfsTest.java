package threads.lite;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import threads.lite.cid.Cid;
import threads.lite.cid.Multiaddr;
import threads.lite.core.TimeoutCloseable;
import threads.lite.format.Link;
import threads.lite.host.DnsResolver;
import threads.lite.host.PeerInfo;


@RunWith(AndroidJUnit4.class)
public class IpfsTest {
    private static final String TAG = IpfsTest.class.getSimpleName();
    private static Context context;

    @BeforeClass
    public static void setup() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void test_listenAddresses() throws Exception {
        IPFS ipfs = TestEnv.getTestInstance(context);
        Thread.sleep(5000);

        assertTrue(ipfs.hasReservations());


        List<Multiaddr> result = ipfs.listenAddresses();
        assertNotNull(result);
        for (Multiaddr ma : result) {
            LogUtils.debug(TAG, "Listen Address : " + ma.toString());
        }
        assertTrue(result.size() >= 1);


        PeerInfo info = ipfs.getIdentity();
        Objects.requireNonNull(info);

        assertNotNull(info.getVersion());
        assertFalse(info.hasRelayHop());
        assertEquals(info.getAgent(), IPFS.AGENT);

        List<String> protocols = info.getProtocols();
        for (String protocol : protocols) {
            assertNotNull(protocol);
        }

        LogUtils.debug(TAG, info.toString());

        Multiaddr ma = ipfs.defaultListenAddress();
        assertNotNull(ma);
        LogUtils.debug(TAG, ma.toString());
    }


    @Test
    public void test_dns_addr() {

        if (TestEnv.isConnected(context)) {

            Set<String> result = DnsResolver.resolveDnsAddress(IPFS.LIB2P_DNS);
            assertNotNull(result);

            assertEquals(result.size(), 30);

            for (String address : result) {
                LogUtils.debug(TAG, address);
            }
        }
    }

    @Test
    public void streamTest() throws IOException, InterruptedException {
        IPFS ipfs = TestEnv.getTestInstance(context);

        String test = "Moin";
        Cid cid = ipfs.storeText(test);
        assertNotNull(cid);
        byte[] bytes = ipfs.getData(cid, () -> false);
        assertNotNull(bytes);
        assertEquals(test, new String(bytes));


        try {
            Cid fault = Cid.decode(ipfs.self().toBase58());
            ipfs.getData(fault, new TimeoutCloseable(10));
            fail();
        } catch (Exception ignore) {
            // ok
        }


    }

    @Test
    public void test_timeout_cat() {

        Cid notValid = Cid.decode("QmaFuc7VmzwT5MAx3EANZiVXRtuWtTwALjgaPcSsZ2Jdip");
        IPFS ipfs = TestEnv.getTestInstance(context);

        try {
            ipfs.getData(notValid, new TimeoutCloseable(10));
            fail();
        } catch (Exception ignore) {
            // ok
        }

    }


    private byte[] getRandomBytes() {
        return RandomStringUtils.randomAlphabetic(400000).getBytes();
    }

    @Test
    public void test_add_cat() throws IOException, InterruptedException {
        IPFS ipfs = TestEnv.getTestInstance(context);

        byte[] content = getRandomBytes();

        Cid hash58Base = ipfs.storeData(content);
        assertNotNull(hash58Base);
        LogUtils.debug(TAG, hash58Base.String());

        byte[] fileContents = ipfs.getData(hash58Base, () -> false);
        assertNotNull(fileContents);
        assertEquals(content.length, fileContents.length);
        assertEquals(new String(content), new String(fileContents));

        ipfs.rm(hash58Base);

    }


    @Test
    public void test_ls_timeout() {

        IPFS ipfs = TestEnv.getTestInstance(context);

        try {
            ipfs.getLinks(Cid.decode("QmXm3f7uKuFKK3QUL1V1oJZnpJSYX8c3vdhd94evSQUPCH"),
                    true, new TimeoutCloseable(20));
            fail();
        } catch (InterruptedException closedException) {
            return;
        }
        fail();

    }

    @Test
    public void test_ls_small() throws IOException, InterruptedException {

        IPFS ipfs = TestEnv.getTestInstance(context);

        Cid cid = ipfs.storeText("hallo");
        assertNotNull(cid);
        List<Link> links = ipfs.getLinks(cid, true, () -> false);
        assertNotNull(links);
        assertEquals(links.size(), 0);
        links = ipfs.getLinks(cid, true, new TimeoutCloseable(20));
        assertNotNull(links);
        assertEquals(links.size(), 0);
    }
}
