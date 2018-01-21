package test;

import okio.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by pc on 2018/1/21.
 */
public class BufferedSourceTest {

    private BufferedSink sink;
    private BufferedSource source;

    private static class Pipe {
        BufferedSink sink;
        BufferedSource source;
    }

    @Before
    public void setUp() {
        Buffer buffer = new Buffer();
        Pipe pipe = new Pipe();
        pipe.sink = buffer;
        pipe.source = buffer;

        sink = pipe.sink;
        source = pipe.source;
    }

    @Test
    public void readBytes() throws Exception {
        sink.write(new byte[] { (byte) 0xab, (byte) 0xcd });
        assertEquals(0xab, source.readByte() & 0xff);
        assertEquals(0xcd, source.readByte() & 0xff);
        assertTrue(source.exhausted());
    }

    @Test public void readShort() throws Exception {
        sink.write(new byte[] {
                (byte) 0xab, (byte) 0xcd, (byte) 0xef, (byte) 0x01
        });
        assertEquals((short) 0xabcd, source.readShort());
        assertEquals((short) 0xef01, source.readShort());
        assertTrue(source.exhausted());
    }

    @Test public void readShortLe() throws Exception {
        sink.write(new byte[] {
                (byte) 0xab, (byte) 0xcd, (byte) 0xef, (byte) 0x10
        });
        assertEquals((short) 0xcdab, source.readShortLe());
        assertEquals((short) 0x10ef, source.readShortLe());
        assertTrue(source.exhausted());
    }
}


