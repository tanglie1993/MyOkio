package okio;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by pc on 2018/1/18.
 */
public interface BufferedSink extends Sink {
    BufferedSink writeUtf8(String s) throws IOException;

    long writeAll(Source source) throws IOException;

    BufferedSink write(byte[] bytes) throws IOException;

    void writeByte(byte b) throws IOException;

    void writeShort(short s) throws IOException;

    void writeShortLe(short s) throws IOException;

    void writeInt(int i) throws IOException;

    void writeIntLe(int i) throws IOException;

    void writeLong(long l) throws IOException;

    void writeLongLe(long l) throws IOException;

    BufferedSink write(ByteString byteString) throws IOException;

    void writeUtf8(String string, int startIndex, int endIndex) throws IOException;

    void writeString(String string, Charset charset) throws IOException;

    void writeString(String string, int start, int end, Charset charset) throws IOException;

    long write(Source source, long length) throws IOException;

    OutputStream outputStream();

    BufferedSink writeDecimalLong(long value) throws IOException;

    BufferedSink writeHexadecimalUnsignedLong(long value) throws IOException;

    Buffer buffer();
}
