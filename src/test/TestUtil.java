package test;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by pc on 2018/1/20.
 */
public class TestUtil {

    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final int SEGMENT_SIZE = 8192;

    static String repeat(char c, int count) {
        char[] array = new char[count];
        Arrays.fill(array, c);
        return new String(array);
    }
}
