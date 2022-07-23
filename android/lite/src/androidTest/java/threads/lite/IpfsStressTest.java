package threads.lite;


import static junit.framework.TestCase.assertNotNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import threads.lite.cid.Cid;
import threads.lite.core.TimeoutProgress;


@RunWith(AndroidJUnit4.class)
public class IpfsStressTest {
    private static final String TAG = IpfsStressTest.class.getSimpleName();


    private static Context context;

    @BeforeClass
    public static void setup() {
        context = ApplicationProvider.getApplicationContext();
    }


    @Test
    public void stress_test0() throws IOException, InterruptedException {

        IPFS ipfs = TestEnv.getTestInstance(context);

        byte[] data = ipfs.getData(Cid.decode("QmcniBv7UQ4gGPQQW2BwbD4ZZHzN3o3tPuNLZCbBchd1zh"),
                new TimeoutProgress(TimeUnit.MINUTES.toSeconds(5)) {
                    @Override
                    public void setProgress(int progress) {
                        LogUtils.debug(TAG, "Progress " + progress);
                    }

                    @Override
                    public boolean doProgress() {
                        return true;
                    }
                });
        assertNotNull(data);


    }

}
