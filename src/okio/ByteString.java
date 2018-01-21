package okio;

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
}
