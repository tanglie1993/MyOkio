package okio;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

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

    void skip(long count) throws IOException;

    int readInt() throws IOException;

    int readIntLe() throws IOException;

    long readLong() throws IOException;

    long readLongLe() throws IOException;

    Buffer buffer();

    int readAll(Sink sink) throws IOException;

    void readFully(Buffer sink, int length) throws IOException;

    void readFully(byte[] sink) throws IOException;

    int read(byte[] sink) throws IOException;

    byte[] readByteArray() throws IOException;

    int read(byte[] sink, int offset, int byteCount);

    byte[] readByteArray(int count) throws IOException;

    ByteString readByteString() throws IOException;

    ByteString readByteString(int count) throws IOException;

    String readString(int count, Charset charset) throws IOException;

    String readString(Charset charset) throws IOException;

    int indexOf(byte b);

    int indexOf(byte b, int fromIndex);

    int indexOf(byte b, int fromIndex, int toIndex);

    int indexOf(ByteString byteString);

    int indexOf(ByteString byteString, int fromIndex);

    int indexOfElement(ByteString byteString);

    int indexOfElement(ByteString byteString, int index);

    boolean request(long count) throws IOException;

    void require(long count) throws IOException;

    InputStream inputStream();

    long readHexadecimalUnsignedLong() throws IOException;

    long readDecimalLong() throws IOException;

    boolean rangeEquals(int offset, ByteString byteString);

    boolean rangeEquals(int offset, ByteString byteString, int start, int end);
}
