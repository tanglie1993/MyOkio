package okio;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by pc on 2018/1/18.
 */
public interface BufferedSink extends Sink {
    BufferedSink writeUtf8(String s) throws IOException;

    long writeAll(Source source) throws IOException;

    BufferedSink write(byte[] bytes);

    void writeByte(byte b);

    void writeShort(short s);

    void writeShortLe(short s);

    void writeInt(int i);

    void writeIntLe(int i);

    void writeLong(long l);

    void writeLongLe(long l);

    BufferedSink write(ByteString byteString);

    void writeUtf8(String string, int startIndex, int endIndex);

    void writeString(String string, Charset charset);

    void writeString(String string, int start, int end, Charset charset);

    long write(Source source, long length) throws IOException;

    OutputStream outputStream();

    BufferedSink writeDecimalLong(long value) throws IOException;

    BufferedSink writeHexadecimalUnsignedLong(long value);
}
