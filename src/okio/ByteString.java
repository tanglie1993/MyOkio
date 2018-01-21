package okio;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;

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
}
