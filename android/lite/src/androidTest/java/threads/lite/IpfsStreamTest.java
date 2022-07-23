package threads.lite;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import threads.lite.cid.Cid;
import threads.lite.format.Link;

@RunWith(AndroidJUnit4.class)
public class IpfsStreamTest {

    private static Context context;

    @BeforeClass
    public static void setup() {
        context = ApplicationProvider.getApplicationContext();
    }


    @Test
    public void test_string() throws IOException, InterruptedException {
        IPFS ipfs = TestEnv.getTestInstance(context);

        String text = "Hello Moin und Zehn Elf";
        Cid hash = ipfs.storeText(text);
        assertNotNull(hash);
        List<Link> links = ipfs.getLinks(hash, true, () -> false);
        assertNotNull(links);
        assertEquals(links.size(), 0);


        byte[] result = ipfs.getData(hash, () -> false);
        assertNotNull(result);
        assertEquals(text, new String(result));


        Cid hash2 = ipfs.storeText("TEST test");
        assertNotNull(hash2);
        links = ipfs.getLinks(hash2, true, () -> false);
        assertNotNull(links);
        assertEquals(links.size(), 0);


    }
}
