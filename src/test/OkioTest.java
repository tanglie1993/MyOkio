package test;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class OkioTest {
    @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test public void readWriteFile() throws Exception {
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
}
