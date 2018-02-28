package okio;

import test.TestUtil;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by pc on 2018/1/21.
 */
public class ByteString {

    static final ByteString EMPTY = ByteString.of();

    private byte[] data;

    public ByteString(byte[] bytes) {
        this.data = bytes;
    }

    public ByteString(byte[] bytes, int startIndex, int length) {
        this.data = new byte[length];
        System.arraycopy(bytes, startIndex, this.data, 0, length);
    }

    public String utf8() {
        return new String(data);
    }

    public String toString(Charset charset) {
        return new String(data, charset);
    }

    public static ByteString encodeUtf8(String string) {
        return new ByteString(string.getBytes(TestUtil.UTF_8));
    }

    public byte[] getData() {
        return data;
    }

    public static ByteString of(byte... data) {
        if (data == null) {
            throw new IllegalArgumentException("data == null");
        }
        return new ByteString(data.clone());
    }

    public static ByteString decodeHex(String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("hex == null");
        }
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Unexpected hex string: " + hex);
        }

        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < result.length; i++) {
            int d1 = decodeHexDigit(hex.charAt(i * 2)) << 4;
            int d2 = decodeHexDigit(hex.charAt(i * 2 + 1));
            result[i] = (byte) (d1 + d2);
        }
        return of(result);
    }

    private static int decodeHexDigit(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'f') return c - 'a' + 10;
        if (c >= 'A' && c <= 'F') return c - 'A' + 10;
        throw new IllegalArgumentException("Unexpected hex digit: " + c);
    }

    @Override
    public String toString() {
        return "ByteString{" +
                "data=" + Arrays.toString(data) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ByteString that = (ByteString) o;

        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    public byte getByte(int index) {
        return data[index];
    }

    public static ByteString of(byte[] bytes, int startIndex, int length) {
        return new ByteString(bytes, startIndex, length);
    }

    public static ByteString of(ByteBuffer byteBuffer) {
        return new ByteString(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit() - byteBuffer.position());
    }

    public boolean startsWith(ByteString byteString) {
        for(int i = 0; i < byteString.data.length; i++){
            if(i >= getData().length || getData()[i] != byteString.getData()[i]){
                return false;
            }
        }
        return true;
    }

    public boolean endsWith(ByteString byteString) {
        for(int i = 0; i < byteString.getData().length; i++){
            if(getData().length - 1 - i < 0 || getData()[getData().length - 1 - i] != byteString.getData()[byteString.getData().length - 1 - i]){
                return false;
            }
        }
        return true;
    }

    public ByteString toByteArray() {
        return this;
    }

    public int indexOf(ByteString byteString) {
        return indexOf(byteString, 0);
    }

    public int indexOf(ByteString byteString, int startIndex) {
        startIndex = Math.max(startIndex, 0);
        outer: for(int i = startIndex; i <= getData().length - byteString.getData().length; i++){
            for(int j = 0; j < byteString.data.length; j++){
                if(j >= getData().length || getData()[i + j] != byteString.getData()[j]){
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
