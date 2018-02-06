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

    void writeShort(short s);

    void writeShortLe(short s);

    void writeInt(int i);

    void writeIntLe(int i);

    void writeLong(long l);

    void writeLongLe(long l);
}
