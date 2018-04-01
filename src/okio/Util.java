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

    public static boolean arrayRangeEquals(byte[] a, int aOffset, byte[] b, int bOffset, int byteCount) {
        for (int i = 0; i < byteCount; i++) {
            if (a[i + aOffset] != b[i + bOffset]) return false;
        }
        return true;
    }

    /**
     * Throws {@code t}, even if the declared throws clause doesn't permit it.
     * This is a terrible – but terribly convenient – hack that makes it easy to
     * catch and rethrow exceptions after cleanup. See Java Puzzlers #43.
     */
    public static void sneakyRethrow(Throwable t) {
        Util.<Error>sneakyThrow2(t);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow2(Throwable t) throws T {
        throw (T) t;
    }
}
