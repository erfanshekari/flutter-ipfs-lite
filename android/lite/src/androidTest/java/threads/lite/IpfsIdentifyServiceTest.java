package threads.lite;


import static org.junit.Assert.assertFalse;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import threads.lite.cid.Multiaddr;

@RunWith(AndroidJUnit4.class)
public class IpfsIdentifyServiceTest {
    private static final String TAG = IpfsIdentifyServiceTest.class.getSimpleName();
    private static Context context;

    @BeforeClass
    public static void setup() {
        context = ApplicationProvider.getApplicationContext();
    }


    @Test
    public void identify_test() {

        IPFS ipfs = TestEnv.getTestInstance(context);

        List<Multiaddr> list = ipfs.listenAddresses(false);
        assertFalse(list.isEmpty());
        for (Multiaddr addr : list) {
            LogUtils.info(TAG, addr.toString());
        }
    }
}
