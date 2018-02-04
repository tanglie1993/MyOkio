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
}
