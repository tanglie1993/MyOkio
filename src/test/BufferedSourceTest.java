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
import static test.TestUtil.SEGMENT_SIZE;
import static test.TestUtil.repeat;

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

    @Test
    public void readShort() throws Exception {
        sink.write(new byte[] {
                (byte) 0xab, (byte) 0xcd, (byte) 0xef, (byte) 0x01
        });
        assertEquals((short) 0xabcd, source.readShort());
        assertEquals((short) 0xef01, source.readShort());
        assertTrue(source.exhausted());
    }

    @Test
    public void readShortLe() throws Exception {
        sink.write(new byte[] {
                (byte) 0xab, (byte) 0xcd, (byte) 0xef, (byte) 0x10
        });
        assertEquals((short) 0xcdab, source.readShortLe());
        assertEquals((short) 0x10ef, source.readShortLe());
        assertTrue(source.exhausted());
    }

    @Test
    public void readShortSplitAcrossMultipleSegments() throws Exception {
        sink.writeUtf8(repeat('a', SEGMENT_SIZE - 1));
        sink.write(new byte[] { (byte) 0xab, (byte) 0xcd });
        source.skip(SEGMENT_SIZE - 1);
        assertEquals((short) 0xabcd, source.readShort());
        assertTrue(source.exhausted());
    }

    @Test
    public void readInt() throws Exception {
        sink.write(new byte[] {
                (byte) 0xab, (byte) 0xcd, (byte) 0xef, (byte) 0x01, (byte) 0x87, (byte) 0x65, (byte) 0x43,
                (byte) 0x21
        });
        assertEquals(0xabcdef01, source.readInt());
        assertEquals(0x87654321, source.readInt());
        assertTrue(source.exhausted());
    }

    @Test
    public void readIntLe() throws Exception {
        sink.write(new byte[] {
                (byte) 0xab, (byte) 0xcd, (byte) 0xef, (byte) 0x10, (byte) 0x87, (byte) 0x65, (byte) 0x43,
                (byte) 0x21
        });
        assertEquals(0x10efcdab, source.readIntLe());
        assertEquals(0x21436587, source.readIntLe());
        assertTrue(source.exhausted());
    }

    @Test
    public void readIntSplitAcrossMultipleSegments() throws Exception {
        sink.writeUtf8(repeat('a', SEGMENT_SIZE - 3));
        sink.write(new byte[] {
                (byte) 0xab, (byte) 0xcd, (byte) 0xef, (byte) 0x01
        });
        source.skip(SEGMENT_SIZE - 3);
        assertEquals(0xabcdef01, source.readInt());
        assertTrue(source.exhausted());
    }

    @Test
    public void readLong() throws Exception {
        sink.write(new byte[] {
                (byte) 0xab, (byte) 0xcd, (byte) 0xef, (byte) 0x10, (byte) 0x87, (byte) 0x65, (byte) 0x43,
                (byte) 0x21, (byte) 0x36, (byte) 0x47, (byte) 0x58, (byte) 0x69, (byte) 0x12, (byte) 0x23,
                (byte) 0x34, (byte) 0x45
        });
        assertEquals(0xabcdef1087654321L, source.readLong());
        assertEquals(0x3647586912233445L, source.readLong());
        assertTrue(source.exhausted());
    }

    @Test
    public void readLongLe() throws Exception {
        sink.write(new byte[] {
                (byte) 0xab, (byte) 0xcd, (byte) 0xef, (byte) 0x10, (byte) 0x87, (byte) 0x65, (byte) 0x43,
                (byte) 0x21, (byte) 0x36, (byte) 0x47, (byte) 0x58, (byte) 0x69, (byte) 0x12, (byte) 0x23,
                (byte) 0x34, (byte) 0x45
        });
        assertEquals(0x2143658710efcdabL, source.readLongLe());
        assertEquals(0x4534231269584736L, source.readLongLe());
        assertTrue(source.exhausted());
    }

    @Test
    public void readLongSplitAcrossMultipleSegments() throws Exception {
        sink.writeUtf8(repeat('a', SEGMENT_SIZE - 7));
        sink.write(new byte[] {
                (byte) 0xab, (byte) 0xcd, (byte) 0xef, (byte) 0x01, (byte) 0x87, (byte) 0x65, (byte) 0x43,
                (byte) 0x21,
        });
        source.skip(SEGMENT_SIZE - 7);
        assertEquals(0xabcdef0187654321L, source.readLong());
        assertTrue(source.exhausted());
    }

    @Test
    public void readAll() throws IOException {
        source.buffer().writeUtf8("abc");
        sink.writeUtf8("def");

        Buffer sink = new Buffer();
        assertEquals(6, source.readAll(sink));
        assertEquals("abcdef", sink.readUtf8());
        assertTrue(source.exhausted());
    }

    @Test
    public void readAllExhausted() throws IOException {
        MockSink mockSink = new MockSink();
        assertEquals(0, source.readAll(mockSink));
        assertTrue(source.exhausted());
        mockSink.assertLog();
    }

    @Test
    public void readExhaustedSource() throws Exception {
        Buffer sink = new Buffer();
        sink.writeUtf8(repeat('a', 10));
        assertEquals(-1, source.read(sink, 10));
        assertEquals(10, sink.size());
        assertTrue(source.exhausted());
    }

    @Test
    public void readZeroBytesFromSource() throws Exception {
        Buffer sink = new Buffer();
        sink.writeUtf8(repeat('a', 10));

        // Either 0 or -1 is reasonable here. For consistency with Android's
        // ByteArrayInputStream we return 0.
        assertEquals(-1, source.read(sink, 0));
        assertEquals(10, sink.size());
        assertTrue(source.exhausted());
    }

    @Test
    public void readFully() throws Exception {
        sink.writeUtf8(repeat('a', 10000));
        Buffer sink = new Buffer();
        source.readFully(sink, 9999);
        assertEquals(repeat('a', 9999), sink.readUtf8());
        assertEquals("a", source.readUtf8());
    }
}


