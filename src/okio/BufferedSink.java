package okio;

import java.io.*;

/**
 * Created by pc on 2018/1/18.
 */
public interface BufferedSink extends Sink {
    void writeUtf8(String s) throws IOException;

    void writeAll(Source source) throws IOException;
}
