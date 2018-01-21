package okio;

import java.io.IOException;

/**
 * Created by pc on 2018/1/18.
 */
public interface BufferedSource extends Source {

    String readUtf8() throws IOException;
    String readUtf8(int length) throws IOException;

    byte readByte() throws IOException;

    boolean exhausted();

    short readShort() throws IOException;
}
