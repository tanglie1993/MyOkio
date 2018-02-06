package okio;

import java.io.*;

/**
 * Created by pc on 2018/1/18.
 */
public interface BufferedSink extends Sink {
    Buffer writeUtf8(String s) throws IOException;

    void writeAll(Source source) throws IOException;

    void write(byte[] bytes);

    void writeByte(byte b);
}
