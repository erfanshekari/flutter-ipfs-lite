package threads.lite;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.io.Files;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import threads.lite.cid.Cid;
import threads.lite.core.Progress;
import threads.lite.utils.ReaderProgress;

@RunWith(AndroidJUnit4.class)
public class IpfsPerformanceTest {
    private static final String TAG = IpfsPerformanceTest.class.getSimpleName();
    private static Context context;

    @BeforeClass
    public static void setup() {
        context = ApplicationProvider.getApplicationContext();
    }


    private byte[] getRandomBytes(int number) {
        return RandomStringUtils.randomAlphabetic(number).getBytes();
    }

    @NonNull
    public File createCacheFile() throws IOException {
        return File.createTempFile("temp", ".cid", context.getCacheDir());
    }

    @Test
    public void test_add_cat_small() throws Exception {

        int packetSize = 1000;
        long maxData = 100;

        IPFS ipfs = TestEnv.getTestInstance(context);


        File inputFile = createCacheFile();
        try (OutputStream outputStream = new FileOutputStream(inputFile)) {
            for (int i = 0; i < maxData; i++) {
                byte[] randomBytes = getRandomBytes(packetSize);
                outputStream.write(randomBytes);
            }
        }
        AtomicInteger counter = new AtomicInteger(0);
        try (OutputStream outputStream = new ByteArrayOutputStream()) {
            try (InputStream inputStream = new FileInputStream(inputFile)) {
                IPFS.copy(inputStream, outputStream, new ReaderProgress() {
                    @Override
                    public long getSize() {
                        return inputFile.length();
                    }

                    @Override
                    public void setProgress(int progress) {
                        counter.set(progress);
                    }

                    @Override
                    public boolean doProgress() {
                        return true;
                    }

                    @Override
                    public boolean isClosed() {
                        return false;
                    }
                });

                assertEquals(100, counter.get());
            }
        }
        long size = inputFile.length();


        LogUtils.debug(TAG, "Bytes : " + inputFile.length() / 1000 + "[kb]");
        long now = System.currentTimeMillis();
        Cid cid = ipfs.storeFile(inputFile);
        assertNotNull(cid);
        LogUtils.debug(TAG, "Add : " + cid +
                " Time : " + ((System.currentTimeMillis() - now) / 1000) + "[s]");

        File file = createCacheFile();
        file.deleteOnExit();
        assertTrue(file.exists());
        assertTrue(file.delete());

        now = System.currentTimeMillis();

        byte[] data = ipfs.getData(cid, () -> false);
        Objects.requireNonNull(data);

        LogUtils.debug(TAG, "Cat : " + cid +
                " Time : " + ((System.currentTimeMillis() - now) / 1000) + "[s]");

        assertEquals(data.length, size);

        File temp = createCacheFile();
        ipfs.storeToFile(temp, cid, () -> false);

        assertEquals(temp.length(), size);

        assertTrue(temp.delete());
        assertTrue(inputFile.delete());


        ipfs.rm(cid);


    }


    @Test
    public void test_cmp_files() throws Exception {

        int packetSize = 10000;
        long maxData = 5000;


        IPFS ipfs = TestEnv.getTestInstance(context);

        File inputFile = createCacheFile();
        try (OutputStream outputStream = new FileOutputStream(inputFile)) {
            for (int i = 0; i < maxData; i++) {
                byte[] randomBytes = getRandomBytes(packetSize);
                outputStream.write(randomBytes);
            }
        }
        long size = inputFile.length();


        LogUtils.debug(TAG, "Bytes : " + inputFile.length() / 1000 + "[kb]");

        Cid cid = ipfs.storeFile(inputFile);
        assertNotNull(cid);
        File file = createCacheFile();
        ipfs.storeToFile(file, cid, () -> false);

        assertEquals(file.length(), size);

        assertTrue(Files.equal(inputFile, file));

        assertTrue(file.delete());
        assertTrue(inputFile.delete());


        ipfs.rm(cid);


    }

    @Test
    public void test_add_cat() throws Exception {


        IPFS ipfs = TestEnv.getTestInstance(context);

        int packetSize = 10000;
        long maxData = 25000;


        File inputFile = createCacheFile();

        try (OutputStream outputStream = new FileOutputStream(inputFile)) {
            for (int i = 0; i < maxData; i++) {
                byte[] randomBytes = getRandomBytes(packetSize);
                outputStream.write(randomBytes);
            }
        }

        long size = inputFile.length();
        assertEquals(packetSize * maxData, size);

        LogUtils.debug(TAG, "Bytes : " + inputFile.length() / 1000 + "[kb]");


        long now = System.currentTimeMillis();
        Cid cid = ipfs.storeFile(inputFile);
        assertNotNull(cid);
        LogUtils.debug(TAG, "Add : " + cid.String() +
                " Time : " + ((System.currentTimeMillis() - now) / 1000) + "[s]");


        File temp = createCacheFile();
        ipfs.storeToFile(temp, cid, new Progress() {
            @Override
            public void setProgress(int percent) {
                LogUtils.debug(TAG, "Progress : " + percent);
            }

            @Override
            public boolean doProgress() {
                return true;
            }

            @Override
            public boolean isClosed() {
                return false;
            }

        });

        assertEquals(temp.length(), size);


        now = System.currentTimeMillis();
        Cid cid_2 = ipfs.storeFile(inputFile);
        assertNotNull(cid_2);
        LogUtils.debug(TAG, "Add : " + cid_2.String() +
                " Time : " + ((System.currentTimeMillis() - now) / 1000) + "[s]");

        assertEquals(cid, cid_2);


        now = System.currentTimeMillis();
        File outputFile1 = createCacheFile();
        ipfs.storeToFile(outputFile1, cid, () -> false);
        LogUtils.debug(TAG, "Cat : " + cid.String() +
                " Time : " + ((System.currentTimeMillis() - now) / 1000) + "[s]");


        now = System.currentTimeMillis();
        File outputFile2 = createCacheFile();
        ipfs.storeToFile(outputFile2, cid, () -> false);
        LogUtils.debug(TAG, "Cat : " + cid.String() +
                " Time : " + ((System.currentTimeMillis() - now) / 1000) + "[s]");


        assertEquals(outputFile1.length(), size);
        assertEquals(outputFile2.length(), size);
        assertTrue(outputFile2.delete());
        assertTrue(outputFile1.delete());
        assertTrue(inputFile.delete());

        ipfs.rm(cid);


    }

    @Test
    public void test_add_cat_abort() throws Exception {


        IPFS ipfs = TestEnv.getTestInstance(context);

        int packetSize = 1000;
        long maxData = 1000;


        File inputFile = createCacheFile();
        try (OutputStream outputStream = new FileOutputStream(inputFile)) {
            for (int i = 0; i < maxData; i++) {
                byte[] randomBytes = getRandomBytes(packetSize);
                outputStream.write(randomBytes);
            }
        }
        long size = inputFile.length();
        assertEquals(packetSize * maxData, size);

        LogUtils.debug(TAG, "Bytes : " + inputFile.length() / 1000 + "[kb]");


        long now = System.currentTimeMillis();
        Cid cid = ipfs.storeFile(inputFile);
        assertNotNull(cid);
        LogUtils.debug(TAG, "Add : " + cid +
                " Time : " + ((System.currentTimeMillis() - now) / 1000) + "[s]");


        File abort = createCacheFile();
        AtomicBoolean closed = new AtomicBoolean(false);
        try {
            ipfs.storeToFile(abort, cid, new Progress() {
                @Override
                public void setProgress(int percent) {
                    if (percent > 50) {
                        closed.set(true);
                    }
                    LogUtils.debug(TAG, "Progress : " + percent);
                }

                @Override
                public boolean doProgress() {
                    return true;
                }

                @Override
                public boolean isClosed() {
                    return closed.get();
                }

            });
        } catch (Throwable ignore) {
            //
        }

        assertTrue(closed.get());


    }


    @Test
    public void test_storeInputStream() throws Exception {

        int packetSize = 1000;
        long maxData = 100;

        IPFS ipfs = TestEnv.getTestInstance(context);


        File inputFile = createCacheFile();
        try (OutputStream outputStream = new FileOutputStream(inputFile)) {
            for (int i = 0; i < maxData; i++) {
                byte[] randomBytes = getRandomBytes(packetSize);
                outputStream.write(randomBytes);
            }
        }
        long size = inputFile.length();

        AtomicInteger counter = new AtomicInteger(0);
        try (InputStream inputStream = new FileInputStream(inputFile)) {
            Cid cid = ipfs.storeInputStream(inputStream, new Progress() {
                @Override
                public void setProgress(int progress) {
                    counter.set(progress);
                }

                @Override
                public boolean doProgress() {
                    return true;
                }

                @Override
                public boolean isClosed() {
                    return false;
                }
            }, size);
            assertNotNull(cid);

            assertEquals(counter.get(), 100);

            File temp = createCacheFile();
            ipfs.storeToFile(temp, cid, () -> false);

            assertTrue(Files.equal(temp, inputFile));

            assertTrue(temp.delete());
        }
        assertTrue(inputFile.delete());

    }
}
