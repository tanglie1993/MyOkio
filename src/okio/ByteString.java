package okio;

import test.TestUtil;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by pc on 2018/1/21.
 */
public class ByteString {

    private byte[] data;

    public ByteString(byte[] bytes) {
        this.data = bytes;
    }

    public String utf8() {
        return new String(data);
    }

    public static byte[] decodeHex(String s) {
        return DatatypeConverter.parseHexBinary(s);
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
        if (data == null) throw new IllegalArgumentException("data == null");
        return new ByteString(data.clone());
    }

    @Override
    public String toString() {
        return "ByteString{" +
                "data=" + Arrays.toString(data) +
                '}';
    }
}
