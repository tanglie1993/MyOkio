package test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import okio.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static test.TestUtil.repeat;

public final class OkioTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void readWriteFile() throws Exception {
        temporaryFolder.create();
        File file = temporaryFolder.newFile();

        BufferedSink sink = Okio.buffer(Okio.sink(file));
        sink.writeUtf8("Hello, java.io file!");
        sink.close();
        assertTrue(file.exists());
        assertEquals(20, file.length());

        BufferedSource source = Okio.buffer(Okio.source(file));
        assertEquals("Hello, java.io file!", source.readUtf8());
        source.close();
    }

    @Test
    public void appendFile() throws Exception {
        File file = temporaryFolder.newFile();

        BufferedSink sink = Okio.buffer(Okio.appendingSink(file));
        sink.writeUtf8("Hello, ");
        sink.close();
        assertTrue(file.exists());
        assertEquals(7, file.length());

        sink = Okio.buffer(Okio.appendingSink(file));
        sink.writeUtf8("java.io file!");
        sink.close();
        assertEquals(20, file.length());

        BufferedSource source = Okio.buffer(Okio.source(file));
        assertEquals("Hello, java.io file!", source.readUtf8());
        source.close();
    }

    @Test
    public void readWritePath() throws Exception {
        Path path = temporaryFolder.newFile().toPath();

        BufferedSink sink = Okio.buffer(Okio.sink(path));
        sink.writeUtf8("Hello, java.nio file!");
        sink.close();
        assertTrue(Files.exists(path));
        assertEquals(21, Files.size(path));

        BufferedSource source = Okio.buffer(Okio.source(path));
        assertEquals("Hello, java.nio file!", source.readUtf8());
        source.close();
    }

    @Test
    public void sinkFromOutputStream() throws Exception {
        Buffer data = new Buffer();
        data.writeUtf8("a");
        data.writeUtf8(repeat('b', 9998));
        data.writeUtf8("c");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Sink sink = Okio.sink(out);
        sink.write(data, 3);
        assertEquals("abb", out.toString("UTF-8"));
        sink.write(data, data.size());
        assertEquals("a" + repeat('b', 9998) + "c", out.toString("UTF-8"));
    }
}
