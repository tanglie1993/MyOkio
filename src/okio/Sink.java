package okio;

import java.io.IOException;

/**
 * Created by pc on 2018/1/18.
 */
public interface Sink {
    void writeUtf8(String s) throws IOException;

    void close() throws IOException;
}
