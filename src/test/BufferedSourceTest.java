package test;

import okio.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static test.TestUtil.*;

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

    @Test
    public void readFullyTooShortThrows() throws IOException {
        sink.writeUtf8("Hi");
        Buffer sink = new Buffer();
        try {
            source.readFully(sink, 5);
            fail();
        } catch (EOFException ignored) {
        }

        // Verify we read all that we could from the source.
        assertEquals("Hi", sink.readUtf8());
    }

    @Test
    public void readFullyByteArray() throws IOException {
        Buffer data = new Buffer();
        data.writeUtf8("Hello").writeUtf8(repeat('e', SEGMENT_SIZE));

        byte[] expected = data.clone().readByteArray();
        sink.write(data, data.size());

        byte[] sink = new byte[SEGMENT_SIZE + 5];
        source.readFully(sink);
        assertByteArraysEquals(expected, sink);
    }

    @Test
    public void readFullyByteArrayTooShortThrows() throws IOException {
        sink.writeUtf8("Hello");

        byte[] sink = new byte[6];
        try {
            source.readFully(sink);
            fail();
        } catch (EOFException ignored) {
        }

        // Verify we read all that we could from the source.
        assertByteArraysEquals(new byte[] { 'H', 'e', 'l', 'l', 'o', 0 }, sink);
    }

    @Test
    public void readIntoByteArray() throws IOException {
        sink.writeUtf8("abcd");

        byte[] sink = new byte[3];
        int read = source.read(sink);
        assertEquals(3, read);
        byte[] expected = { 'a', 'b', 'c' };
        assertByteArraysEquals(expected, sink);
    }

    @Test
    public void readIntoByteArrayNotEnough() throws IOException {
        sink.writeUtf8("abcd");

        byte[] sink = new byte[5];
        int read = source.read(sink);
        assertEquals(4, read);
        byte[] expected = { 'a', 'b', 'c', 'd', 0 };
        assertByteArraysEquals(expected, sink);
    }

    @Test
    public void readIntoByteArrayOffsetAndCount() throws IOException {
        sink.writeUtf8("abcd");

        byte[] sink = new byte[7];
        int read = source.read(sink, 2, 3);
        assertEquals(3, read);
        byte[] expected = { 0, 0, 'a', 'b', 'c', 0, 0 };
        assertByteArraysEquals(expected, sink);
    }

    @Test
    public void readByteArray() throws IOException {
        String string = "abcd" + repeat('e', SEGMENT_SIZE);
        sink.writeUtf8(string);
        assertByteArraysEquals(string.getBytes(UTF_8), source.readByteArray());
    }

    @Test
    public void readByteArrayPartial() throws IOException {
        sink.writeUtf8("abcd");
        assertEquals("[97, 98, 99]", Arrays.toString(source.readByteArray(3)));
        assertEquals("d", source.readUtf8(1));
    }

    @Test
    public void readByteString() throws IOException {
        sink.writeUtf8("abcd").writeUtf8(repeat('e', SEGMENT_SIZE));
        assertEquals("abcd" + repeat('e',SEGMENT_SIZE), source.readByteString().utf8());
    }

    @Test
    public void readByteStringPartial() throws IOException {
        sink.writeUtf8("abcd").writeUtf8(repeat('e', SEGMENT_SIZE));
        assertEquals("abc", source.readByteString(3).utf8());
        assertEquals("d", source.readUtf8(1));
    }

    @Test
    public void readSpecificCharsetPartial() throws Exception {
        sink.write(
                ByteString.decodeHex("0000007600000259000002c80000006c000000e40000007300000259"
                        + "000002cc000000720000006100000070000000740000025900000072"));
        assertEquals("vəˈläsə", source.readString(7 * 4, Charset.forName("utf-32")));
    }

    @Test
    public void readSpecificCharset() throws Exception {
        sink.write(
                ByteString.decodeHex("0000007600000259000002c80000006c000000e40000007300000259"
                        + "000002cc000000720000006100000070000000740000025900000072"));
        assertEquals("vəˈläsəˌraptər", source.readString(Charset.forName("utf-32")));
    }

    @Test
    public void readUtf8SpansSegments() throws Exception {
        sink.writeUtf8(repeat('a', SEGMENT_SIZE * 2));
        source.skip(SEGMENT_SIZE - 1);
        assertEquals("aa", source.readUtf8(2));
    }

    @Test
    public void readUtf8Segment() throws Exception {
        sink.writeUtf8(repeat('a', SEGMENT_SIZE));
        assertEquals(repeat('a', SEGMENT_SIZE), source.readUtf8(SEGMENT_SIZE));
    }

    @Test
    public void readUtf8PartialBuffer() throws Exception {
        sink.writeUtf8(repeat('a', SEGMENT_SIZE + 20));
        assertEquals(repeat('a', SEGMENT_SIZE + 10), source.readUtf8(SEGMENT_SIZE + 10));
    }

    @Test
    public void readUtf8EntireBuffer() throws Exception {
        sink.writeUtf8(repeat('a', SEGMENT_SIZE * 2));
        assertEquals(repeat('a', SEGMENT_SIZE * 2), source.readUtf8());
    }

    @Test
    public void skip() throws Exception {
        sink.writeUtf8("a");
        sink.writeUtf8(repeat('b', SEGMENT_SIZE));
        sink.writeUtf8("c");
        source.skip(1);
        assertEquals('b', source.readByte() & 0xff);
        source.skip(SEGMENT_SIZE - 2);
        assertEquals('b', source.readByte() & 0xff);
        source.skip(1);
        assertTrue(source.exhausted());
    }

    @Test
    public void skipInsufficientData() throws Exception {
        sink.writeUtf8("a");

        try {
            source.skip(2);
            fail();
        } catch (EOFException ignored) {
        }
    }

    @Test
    public void indexOf() throws Exception {
        // The segment is empty.
        assertEquals(-1, source.indexOf((byte) 'a'));

        // The segment has one value.
        sink.writeUtf8("a"); // a
        assertEquals(0, source.indexOf((byte) 'a'));
        assertEquals(-1, source.indexOf((byte) 'b'));

        // The segment has lots of data.
        sink.writeUtf8(repeat('b', SEGMENT_SIZE - 2)); // ab...b
        assertEquals(0, source.indexOf((byte) 'a'));
        assertEquals(1, source.indexOf((byte) 'b'));
        assertEquals(-1, source.indexOf((byte) 'c'));

        // The segment doesn't start at 0, it starts at 2.
        source.skip(2); // b...b
        assertEquals(-1, source.indexOf((byte) 'a'));
        assertEquals(0, source.indexOf((byte) 'b'));
        assertEquals(-1, source.indexOf((byte) 'c'));

        // The segment is full.
        sink.writeUtf8("c"); // b...bc
        assertEquals(-1, source.indexOf((byte) 'a'));
        assertEquals(0, source.indexOf((byte) 'b'));
        assertEquals(SEGMENT_SIZE - 3, source.indexOf((byte) 'c'));

        // The segment doesn't start at 2, it starts at 4.
        source.skip(2); // b...bc
        assertEquals(-1, source.indexOf((byte) 'a'));
        assertEquals(0, source.indexOf((byte) 'b'));
        assertEquals(SEGMENT_SIZE - 5, source.indexOf((byte) 'c'));

        // Two segments.
        sink.writeUtf8("d"); // b...bcd, d is in the 2nd segment.
        assertEquals(SEGMENT_SIZE - 4, source.indexOf((byte) 'd'));
        assertEquals(-1, source.indexOf((byte) 'e'));
    }

    @Test
    public void indexOfByteWithStartOffset() throws IOException {
        sink.writeUtf8("a").writeUtf8(repeat('b', SEGMENT_SIZE)).writeUtf8("c");
        assertEquals(-1, source.indexOf((byte) 'a', 1));
        assertEquals(15, source.indexOf((byte) 'b', 15));
    }


    @Test
    public void indexOfByteWithBothOffsets() throws IOException {
        byte a = (byte) 'a';
        byte c = (byte) 'c';

        int size = SEGMENT_SIZE * 5;
        byte[] bytes = new byte[size];
        Arrays.fill(bytes, a);

        // These are tricky places where the buffer
        // starts, ends, or segments come together.
        int[] points = {
                0,                       1,                   2,
                SEGMENT_SIZE - 1,        SEGMENT_SIZE,        SEGMENT_SIZE + 1,
                size / 2 - 1,            size / 2,            size / 2 + 1,
                size - SEGMENT_SIZE - 1, size - SEGMENT_SIZE, size - SEGMENT_SIZE + 1,
                size - 3,                size - 2,            size - 1
        };

        // In each iteration, we write c to the known point and then search for it using different
        // windows. Some of the windows don't overlap with c's position, and therefore a match shouldn't
        // be found.
        for (int p : points) {
            bytes[p] = c;
            sink.write(bytes);

            assertEquals( p, source.indexOf(c, 0,      size     ));
            assertEquals( p, source.indexOf(c, 0,      p + 1    ));
            assertEquals( p, source.indexOf(c, p,      size     ));
            assertEquals( p, source.indexOf(c, p,      p + 1    ));
            assertEquals( p, source.indexOf(c, p / 2,  p * 2 + 1));
            assertEquals(-1, source.indexOf(c, 0,      p / 2    ));
            assertEquals(-1, source.indexOf(c, 0,      p        ));
            assertEquals(-1, source.indexOf(c, 0,      0        ));
            assertEquals(-1, source.indexOf(c, p,      p        ));

            // Reset.
            source.readUtf8();
            bytes[p] = a;
        }
    }

    @Test
    public void indexOfByteInvalidBoundsThrows() throws IOException {
        sink.writeUtf8("abc");

        try {
            source.indexOf((byte) 'a', -1);
            fail("Expected failure: fromIndex < 0");
        } catch (IllegalArgumentException expected) {
        }

        try {
            source.indexOf((byte) 'a', 10, 0);
            fail("Expected failure: fromIndex > toIndex");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void indexOfByteString() throws IOException {
        assertEquals(-1, source.indexOf(ByteString.encodeUtf8("flop")));

        sink.writeUtf8("flip flop");
        assertEquals(5, source.indexOf(ByteString.encodeUtf8("flop")));
        source.readUtf8(); // Clear stream.

        // Make sure we backtrack and resume searching after partial match.
        sink.writeUtf8("hi hi hi hey");
        assertEquals(3, source.indexOf(ByteString.encodeUtf8("hi hi hey")));
    }

    @Test 
    public void indexOfByteStringAtSegmentBoundary() throws IOException {
        sink.writeUtf8(repeat('a', SEGMENT_SIZE - 1));
        sink.writeUtf8("bcd");
        assertEquals(SEGMENT_SIZE - 3, source.indexOf(ByteString.encodeUtf8("aabc"), SEGMENT_SIZE - 4));
        assertEquals(SEGMENT_SIZE - 3, source.indexOf(ByteString.encodeUtf8("aabc"), SEGMENT_SIZE - 3));
        assertEquals(SEGMENT_SIZE - 2, source.indexOf(ByteString.encodeUtf8("abcd"), SEGMENT_SIZE - 2));
        assertEquals(SEGMENT_SIZE - 2, source.indexOf(ByteString.encodeUtf8("abc"),  SEGMENT_SIZE - 2));
        assertEquals(SEGMENT_SIZE - 2, source.indexOf(ByteString.encodeUtf8("abc"),  SEGMENT_SIZE - 2));
        assertEquals(SEGMENT_SIZE - 2, source.indexOf(ByteString.encodeUtf8("ab"),   SEGMENT_SIZE - 2));
        assertEquals(SEGMENT_SIZE - 2, source.indexOf(ByteString.encodeUtf8("a"),    SEGMENT_SIZE - 2));
        assertEquals(SEGMENT_SIZE - 1, source.indexOf(ByteString.encodeUtf8("bc"),   SEGMENT_SIZE - 2));
        assertEquals(SEGMENT_SIZE - 1, source.indexOf(ByteString.encodeUtf8("b"),    SEGMENT_SIZE - 2));
        assertEquals(SEGMENT_SIZE,     source.indexOf(ByteString.encodeUtf8("c"),    SEGMENT_SIZE - 2));
        assertEquals(SEGMENT_SIZE,     source.indexOf(ByteString.encodeUtf8("c"),    SEGMENT_SIZE    ));
        assertEquals(SEGMENT_SIZE + 1, source.indexOf(ByteString.encodeUtf8("d"),    SEGMENT_SIZE - 2));
        assertEquals(SEGMENT_SIZE + 1, source.indexOf(ByteString.encodeUtf8("d"),    SEGMENT_SIZE + 1));
    }

    @Test
    public void indexOfDoesNotWrapAround() throws IOException {
        sink.writeUtf8(repeat('a', SEGMENT_SIZE - 1));
        sink.writeUtf8("bcd");
        assertEquals(-1, source.indexOf(ByteString.encodeUtf8("abcda"), SEGMENT_SIZE - 3));
    }

    @Test
    public void indexOfByteStringWithOffset() throws IOException {
        assertEquals(-1, source.indexOf(ByteString.encodeUtf8("flop"), 1));

        sink.writeUtf8("flop flip flop");
        assertEquals(10, source.indexOf(ByteString.encodeUtf8("flop"), 1));
        source.readUtf8(); // Clear stream

        // Make sure we backtrack and resume searching after partial match.
        sink.writeUtf8("hi hi hi hi hey");
        assertEquals(6, source.indexOf(ByteString.encodeUtf8("hi hi hey"), 1));
    }
}


