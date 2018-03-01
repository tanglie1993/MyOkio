package test;

import okio.Buffer;
import okio.ByteString;
import okio.Segment;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
            byteCount = (Segment.SIZE / 2) + dice.nextInt(Segment.SIZE / 2);
            if (byteCount > data.length - pos) byteCount = data.length - pos;
            int offset = dice.nextInt(Segment.SIZE - byteCount);

            Buffer segment = new Buffer();
            segment.write(new byte[offset]);
            segment.write(data, pos, byteCount);
            segment.skip(offset);

            result.write(segment, byteCount);
        }

        return result;
    }

    public static void assertEquivalent(ByteString b1, ByteString b2) {
        // Equals.
        assertTrue(b1.equals(b2));
        assertTrue(b1.equals(b1));
        assertTrue(b2.equals(b1));

        // Hash code.
        assertEquals(b1.hashCode(), b2.hashCode());
        assertEquals(b1.hashCode(), b1.hashCode());
        assertEquals(b1.toString(), b2.toString());

        // Content.
        assertEquals(b1.getData().length, b2.getData().length);
        byte[] b2Bytes = b2.toByteArray();
        for (int i = 0; i < b2Bytes.length; i++) {
            byte b = b2Bytes[i];
            assertEquals(b, b1.getByte(i));
        }
        assertByteArraysEquals(b1.toByteArray(), b2Bytes);

        // Doesn't equal a different byte string.
        assertFalse(b1.equals(null));
        assertFalse(b1.equals(new Object()));
        if (b2Bytes.length > 0) {
            byte[] b3Bytes = b2Bytes.clone();
            b3Bytes[b3Bytes.length - 1]++;
            ByteString b3 = new ByteString(b3Bytes);
            assertFalse(b1.equals(b3));
            assertFalse(b1.hashCode() == b3.hashCode());
        } else {
            ByteString b3 = ByteString.encodeUtf8("a");
            assertFalse(b1.equals(b3));
            assertFalse(b1.hashCode() == b3.hashCode());
        }
    }

    public static <T extends Serializable> T reserialize(T original) throws Exception {
        Buffer buffer = new Buffer();
        ObjectOutputStream out = new ObjectOutputStream(buffer.outputStream());
        out.writeObject(original);
        ObjectInputStream in = new ObjectInputStream(buffer.inputStream());
        return (T) in.readObject();
    }
}
