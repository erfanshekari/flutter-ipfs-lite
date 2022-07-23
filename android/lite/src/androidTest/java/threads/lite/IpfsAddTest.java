package threads.lite;


import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.common.io.Files;
import com.google.protobuf.ByteString;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import threads.lite.cid.Cid;
import threads.lite.core.Progress;
import threads.lite.core.TimeoutCloseable;
import threads.lite.format.Link;
import threads.lite.utils.Reader;

@RunWith(AndroidJUnit4.class)
public class IpfsAddTest {

    private static final String TAG = IpfsAddTest.class.getSimpleName();
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
        return File.createTempFile("temp", ".io.ipfs.cid", context.getCacheDir());
    }

    @Test(expected = InterruptedException.class)
    public void add_and_remove() throws Exception {

        IPFS ipfs = TestEnv.getTestInstance(context);
        ipfs.clearDatabase();
        String content = "Hallo dfsadf";
        Cid text = ipfs.storeText(content);
        assertNotNull(text);
        assertTrue(ipfs.has(text));
        ipfs.rm(text);
        assertFalse(ipfs.has(text));

        ipfs.getText(text, new TimeoutCloseable(() -> false, 10)); // closed exception expected


    }

    @Test
    public void add_dir() throws Exception {

        IPFS ipfs = TestEnv.getTestInstance(context);
        Cid dir = ipfs.createEmptyDirectory();
        assertNotNull(dir);
        assertTrue(ipfs.isDir(dir, new TimeoutCloseable(1)));

        String content = "Hallo";
        Cid text = ipfs.storeText(content);
        assertNotNull(text);
        assertFalse(ipfs.isDir(text, new TimeoutCloseable(1)));

        byte[] data = ipfs.getData(text, new TimeoutCloseable(1));
        assertEquals(content, new String(data));

        dir = ipfs.addLinkToDirectory(dir, Link.create(text, "text.txt", data.length, Link.File));
        assertNotNull(dir);

        boolean exists = ipfs.hasLink(dir, "text.txt", () -> false);
        assertTrue(exists);

        exists = ipfs.hasLink(dir, "text2.txt", () -> false);
        assertFalse(exists);

        List<Link> links = ipfs.getLinks(dir, false, new TimeoutCloseable(1));
        assertNotNull(links);
        assertEquals(links.size(), 1);

        dir = ipfs.removeFromDirectory(dir, "text.txt");
        assertNotNull(dir);

        links = ipfs.getLinks(dir, false, new TimeoutCloseable(1));
        assertNotNull(links);
        assertEquals(links.size(), 0);

    }


    @Test
    public void create_dir() throws Exception {

        IPFS ipfs = TestEnv.getTestInstance(context);


        String content1 = "Hallo 1";
        Cid text1 = ipfs.storeText(content1);
        assertNotNull(text1);
        assertFalse(ipfs.isDir(text1, new TimeoutCloseable(1)));

        String content2 = "Hallo 12";
        Cid text2 = ipfs.storeText(content2);
        assertNotNull(text2);
        assertFalse(ipfs.isDir(text2, new TimeoutCloseable(1)));

        Cid dir = ipfs.createDirectory(
                List.of(
                        Link.create(text1, "b.txt", content1.length(), Link.File),
                        Link.create(text2, "a.txt", content2.length(), Link.File))
        );

        assertNotNull(dir);
        assertTrue(ipfs.isDir(dir, new TimeoutCloseable(1)));

        List<Link> links = ipfs.getLinks(dir, false, new TimeoutCloseable(1));
        assertNotNull(links);
        assertEquals(links.size(), 2);

        assertEquals(links.get(0).getCid().String(), text1.String());
        assertEquals(links.get(1).getCid().String(), text2.String());

    }

    @Test
    public void add_wrap_test() throws Exception {

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

        LogUtils.debug(TAG, "Bytes : " + inputFile.length() / 1000 + "[kb]");

        Cid hash58Base = ipfs.storeFile(inputFile);
        assertNotNull(hash58Base);

        List<Link> links = ipfs.ls(hash58Base, true, () -> false);
        assertNotNull(links);
        assertEquals(links.size(), 4);

        byte[] bytes = ipfs.getData(hash58Base, () -> false);
        assertNotNull(bytes);
        assertEquals(bytes.length, size);
        File outputFile = createCacheFile();
        Files.write(bytes, outputFile);
        assertTrue(Files.equal(outputFile, inputFile));


    }

    @Test
    public void add_dir_test() throws Exception {
        IPFS ipfs = TestEnv.getTestInstance(context);


        File inputFile = new File(context.getCacheDir(), UUID.randomUUID().toString());
        assertTrue(inputFile.createNewFile());
        try (OutputStream outputStream = new FileOutputStream(inputFile)) {
            for (int i = 0; i < 10; i++) {
                byte[] randomBytes = getRandomBytes(1000);
                outputStream.write(randomBytes);
            }
        }

        Cid hash58Base = ipfs.storeFile(inputFile);
        assertNotNull(hash58Base);

        List<Link> links = ipfs.getLinks(hash58Base, true, () -> false);
        assertNotNull(links);


        assertEquals(links.size(), 0);
    }


    @Test
    public void add_test() throws Exception {

        int packetSize = 1000;
        long maxData = 1000;
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

        Cid hash58Base = ipfs.storeFile(inputFile);
        assertNotNull(hash58Base);

        List<Link> links = ipfs.ls(hash58Base, true, () -> false);
        assertNotNull(links);
        assertEquals(links.size(), 4);
        Link link = links.get(0);
        assertNotEquals(link.getCid(), hash58Base);
        assertFalse(link.isDirectory());
        assertFalse(link.isFile());
        assertFalse(link.isUnknown());
        assertTrue(link.isRaw());

        byte[] bytes = ipfs.getData(hash58Base, () -> false);
        assertNotNull(bytes);
        assertEquals(bytes.length, size);

        File outputFile = createCacheFile();
        Files.write(bytes, outputFile);
        assertTrue(Files.equal(outputFile, inputFile));

    }


    @Test
    public void add_wrap_small_test() throws Exception {

        int packetSize = 200;
        long maxData = 1000;
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

        Cid hash58Base = ipfs.storeFile(inputFile);
        assertNotNull(hash58Base);

        List<Link> links = ipfs.getLinks(hash58Base, true, () -> false);
        assertNotNull(links);
        assertEquals(links.size(), 0);

        byte[] bytes = ipfs.getData(hash58Base, () -> false);
        assertNotNull(bytes);
        assertEquals(bytes.length, size);

        File outputFile = createCacheFile();
        Files.write(bytes, outputFile);
        assertTrue(Files.equal(outputFile, inputFile));


    }

    @Test
    public void add_small_test() throws Exception {

        int packetSize = 200;
        long maxData = 1000;
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

        Cid hash58Base = ipfs.storeFile(inputFile);
        assertNotNull(hash58Base);

        List<Link> links = ipfs.getLinks(hash58Base, true, () -> false);
        assertNotNull(links);
        assertEquals(links.size(), 0);

        byte[] bytes = ipfs.getData(hash58Base, () -> false);
        assertNotNull(bytes);
        assertEquals(bytes.length, size);

        File outputFile = createCacheFile();
        Files.write(bytes, outputFile);
        assertTrue(Files.equal(outputFile, inputFile));

    }


    @Test
    public void test_inputStream() throws Exception {


        IPFS ipfs = TestEnv.getTestInstance(context);

        String text = "moin zehn";
        Cid cid = ipfs.storeText(text);
        assertTrue(ipfs.has(cid));

        byte[] bytes = ipfs.getData(cid, () -> false);
        assertNotNull(bytes);
        assertEquals(bytes.length, text.length());

        InputStream stream = ipfs.getInputStream(cid, () -> false);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IPFS.copy(stream, outputStream);
        assertEquals(text, outputStream.toString());
    }


    @Test
    public void test_inputStreamBig() throws Exception {


        IPFS ipfs = TestEnv.getTestInstance(context);

        String text = RandomStringUtils.randomAlphabetic((IPFS.CHUNK_SIZE * 2) - 50);
        Cid cid = ipfs.storeText(text);

        byte[] bytes = ipfs.getData(cid, () -> false);
        assertNotNull(bytes);
        assertEquals(bytes.length, text.length());

        AtomicInteger percent = new AtomicInteger(0);
        InputStream stream = ipfs.getInputStream(cid, new Progress() {
            @Override
            public void setProgress(int progress) {
                percent.set(progress);
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


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IPFS.copy(stream, outputStream);
        assertEquals(text, outputStream.toString());
        assertEquals(100, percent.get());
    }

    @Test
    public void test_reader() throws Exception {


        IPFS ipfs = TestEnv.getTestInstance(context);

        String text = "0123456789 jjjjjjjj";
        Cid cid = ipfs.storeText(text);
        assertTrue(ipfs.has(cid));

        Reader reader = ipfs.getReader(cid, () -> false);
        reader.seek(0);
        ByteString buffer = reader.loadNextData();
        assertNotNull(buffer);
        assertEquals(text, new String(buffer.toByteArray()));

        int pos = 11;
        reader.seek(pos);
        buffer = reader.loadNextData();
        assertNotNull(buffer);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(buffer.toByteArray());
        assertEquals(text.substring(pos), stream.toString());

        pos = 5;
        reader.seek(pos);
        buffer = reader.loadNextData();
        assertNotNull(buffer);
        stream = new ByteArrayOutputStream();
        stream.write(buffer.toByteArray());
        assertEquals(text.substring(pos), stream.toString());
    }

    @Test
    public void test_readerBig() throws Exception {


        IPFS ipfs = TestEnv.getTestInstance(context);

        String text = RandomStringUtils.randomAlphabetic((IPFS.CHUNK_SIZE * 2) - 50);
        Cid cid = ipfs.storeText(text);
        assertTrue(ipfs.has(cid));

        Reader reader = ipfs.getReader(cid, () -> false);
        reader.seek(0);
        ByteString buffer = reader.loadNextData();
        assertNotNull(buffer);
        assertEquals(IPFS.CHUNK_SIZE, buffer.size());
        buffer = reader.loadNextData();
        assertNotNull(buffer);
        assertEquals(IPFS.CHUNK_SIZE - 50, buffer.size());

        int pos = IPFS.CHUNK_SIZE + 50;
        reader.seek(pos);
        buffer = reader.loadNextData();
        assertNotNull(buffer);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(buffer.toByteArray());

        assertEquals(IPFS.CHUNK_SIZE - 100, stream.toString().length());

        pos = IPFS.CHUNK_SIZE - 50;
        reader.seek(pos);
        buffer = reader.loadNextData();
        assertNotNull(buffer);
        stream = new ByteArrayOutputStream();
        stream.write(buffer.toByteArray());
        assertEquals(50, stream.toString().length());
    }
}
