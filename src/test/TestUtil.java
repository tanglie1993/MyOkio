package test;

import okio.Buffer;
import okio.Segment;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Created by pc on 2018/1/20.
 */
public class TestUtil {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    public static String repeat(char c, int count) {
        char[] array = new char[count];
        Arrays.fill(array, c);
        return new String(array);
    }

    static void assertByteArraysEquals(byte[] a, byte[] b) {
        assertEquals(Arrays.toString(a), Arrays.toString(b));
    }

    static void assertByteArrayEquals(String expectedUtf8, byte[] b) {
        assertEquals(expectedUtf8, new String(b, UTF_8));
    }

    public static Buffer bufferWithRandomSegmentLayout(Random dice, byte[] data) throws IOException {
        Buffer result = new Buffer();

        // Writing to result directly will yield packed segments. Instead, write to
        // other buffers, then write those buffers to result.
        for (int pos = 0, byteCount; pos < data.length; pos += byteCount) {
            byteCount = (4 / 2) + dice.nextInt(4 / 2);
            if (byteCount > data.length - pos) byteCount = data.length - pos;
            int offset = dice.nextInt(4 - byteCount);

            Buffer segment = new Buffer();
            segment.write(new byte[offset]);
            segment.write(data, pos, byteCount);
            segment.skip(offset);

            result.write(segment, byteCount);
        }

        return result;
    }
}
