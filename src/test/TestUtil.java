package test;

import java.util.Arrays;

/**
 * Created by pc on 2018/1/20.
 */
public class TestUtil {
    static String repeat(char c, int count) {
        char[] array = new char[count];
        Arrays.fill(array, c);
        return new String(array);
    }
}
