package okio;

/**
 * Created by pc on 2018/2/4.
 */
public class Util {

    public static void checkOffsetAndCount(long size, long offset, long byteCount) {
        if ((offset | byteCount) < 0 || offset > size || size - offset < byteCount) {
            throw new ArrayIndexOutOfBoundsException(
                    String.format("size=%s offset=%s byteCount=%s", size, offset, byteCount));
        }
    }

    public static long reverseBytesLong(long v) {
        return (v & 0xff00000000000000L) >>> 56
                |  (v & 0x00ff000000000000L) >>> 40
                |  (v & 0x0000ff0000000000L) >>> 24
                |  (v & 0x000000ff00000000L) >>>  8
                |  (v & 0x00000000ff000000L)  <<  8
                |  (v & 0x0000000000ff0000L)  << 24
                |  (v & 0x000000000000ff00L)  << 40
                |  (v & 0x00000000000000ffL)  << 56;
    }
}
