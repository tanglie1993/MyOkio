package okio;

import java.io.EOFException;
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

    short readShortLe() throws IOException;

    void skip(int count) throws IOException;

    int readInt() throws IOException;

    int readIntLe() throws IOException;

    long readLong() throws IOException;

    long readLongLe() throws IOException;

    Buffer buffer();

    int readAll(Sink sink) throws IOException;

    void readFully(Buffer sink, int length) throws IOException;

    void readFully(byte[] sink) throws EOFException;

    int read(byte[] sink) throws IOException;

    byte[] readByteArray();

    int read(byte[] sink, int offset, int byteCount);

    byte[] readByteArray(int count);
}
