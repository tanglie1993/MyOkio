package okio;


import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by pc on 2018/1/20.
 */
public class RealBufferedSource implements BufferedSource {

    private Source source;
    private Buffer buffer;

    public RealBufferedSource(Source source) {
        buffer = new Buffer();
        this.source = source;
    }

    public RealBufferedSource(InputStream inputStream) {
        buffer = new Buffer();
        source = new Source() {
            @Override
            public int read(Buffer data, int length) throws IOException {
                int size = length;
                if(inputStream.available() < 0){
                    return -1;
                }
                if(length > inputStream.available()){
                    size = inputStream.available();
                }
                byte[] bytes = new byte[size];
                inputStream.read(bytes);
                String string = new String(bytes);
                if(length > string.length()){
                    length = string.length();
                }
                data.writeUtf8(string.substring(0, length));
                return buffer.size();
            }

            @Override
            public void close() throws IOException {
                inputStream.close();
            }
        };
    }


    @Override
    public int read(Buffer data, int length) throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + length);
        }
        int result = buffer.write(source, length);
        buffer.read(data, length);
        return result;
    }

    @Override
    public void close() throws IOException {
        source.close();
    }

    @Override
    public String readUtf8() throws IOException {
        buffer.writeAll(source);
        return buffer.readUtf8();
    }

    @Override
    public String readUtf8(int length) throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + length);
        }
        buffer.write(source, length);
        return buffer.readUtf8(length);
    }

    @Override
    public byte readByte() throws IOException {
        buffer.write(source, 1);
        return buffer.readByte();
    }

    @Override
    public boolean exhausted() {
        return buffer.size() == 0;
    }

    @Override
    public short readShort() throws IOException {
        buffer.write(source, 2);
        return buffer.readShort();
    }

    @Override
    public short readShortLe() throws IOException {
        buffer.write(source, 2);
        return buffer.readShortLe();
    }

    @Override
    public void skip(int count) throws IOException {
        buffer.write(source, count);
        buffer.pop(count);
    }

    @Override
    public int readInt() throws IOException {
        buffer.write(source, 4);
        return buffer.readInt();
    }

    @Override
    public int readIntLe() throws IOException {
        buffer.write(source, 4);
        return buffer.readIntLe();
    }

    @Override
    public long readLong() throws IOException {
        buffer.write(source, 8);
        return buffer.readLong();
    }

    @Override
    public long readLongLe() throws IOException {
        buffer.write(source, 8);
        return buffer.readLongLe();
    }

    @Override
    public Buffer buffer() {
        return buffer;
    }

    @Override
    public int readAll(Sink sink) throws IOException {
        int result = buffer.size();
        sink.write(buffer, buffer.size());
        return result;
    }

    @Override
    public void readFully(Buffer sink, int length) throws IOException {
        buffer.readFully(sink, length);
    }

    @Override
    public void readFully(byte[] sink) throws EOFException {
        buffer.readFully(sink);
    }

    @Override
    public int read(byte[] sink) throws IOException {
        return 0;
    }

    @Override
    public byte[] readByteArray() {
        return buffer.readByteArray();
    }

    @Override
    public int read(byte[] sink, int offset, int byteCount) {
        return buffer.read(sink, offset, byteCount);
    }

    @Override
    public byte[] readByteArray(int count) {
        return new byte[0];
    }

    @Override
    public ByteString readByteString() {
        return new ByteString(readByteArray());
    }

    @Override
    public ByteString readByteString(int count) {
        return new ByteString(readByteArray(count));
    }

    @Override
    public String readString(int count, Charset charset) {
        return new ByteString(readByteArray(count)).toString(charset);
    }

    @Override
    public String readString(Charset charset) {
        return new ByteString(readByteArray()).toString(charset);
    }

    @Override
    public int indexOf(byte b) {
        return buffer.indexOf(b);
    }

    @Override
    public int indexOf(byte b, int fromIndex) {
        return buffer.indexOf(b, fromIndex);
    }

    @Override
    public int indexOf(byte b, int fromIndex, int toIndex) {
        return buffer.indexOf(b, fromIndex, toIndex);
    }

    @Override
    public int indexOf(ByteString byteString) {
        return buffer.indexOf(byteString);
    }

    @Override
    public int indexOf(ByteString byteString, int fromIndex) {
        return buffer.indexOf(byteString, fromIndex);
    }
}
