package okio;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Created by pc on 2018/1/20.
 */
public class RealBufferedSink implements BufferedSink {

    private Sink sink;
    private Buffer buffer;

    public RealBufferedSink(Sink sink) {
        buffer = new Buffer();
        this.sink = sink;
    }

    public RealBufferedSink(OutputStream outputStream) {
        buffer = new Buffer();
        sink = new Sink() {
            @Override
            public void close() throws IOException {
                outputStream.close();
            }

            @Override
            public void write(Buffer data, long length) throws IOException {
                outputStream.write(data.pop(length).getBytes());
            }

            @Override
            public void flush() throws IOException {
                outputStream.flush();
            }
        };
    }


    @Override
    public void close() throws IOException {
        flush();
        sink.close();
    }

    @Override
    public void write(Buffer data, long length) throws IOException {
        buffer.write(data, length);
        flush();
    }

    @Override
    public void flush() throws IOException {
        sink.write(buffer, buffer.size());
    }

    @Override
    public Buffer writeUtf8(String s) throws IOException {
        return buffer.writeUtf8(s);
    }

    @Override
    public long writeAll(Source source) throws IOException {
        long result = 0;
        while(true){
            long increment = source.read(buffer, Segment.SIZE);
            if(increment > 0){
                result += increment;
            }
            if(increment < Segment.SIZE){
                return result;
            }
        }
    }

    @Override
    public void write(byte[] bytes) {
        buffer.write(bytes);
    }

    @Override
    public void writeByte(byte b) {
        buffer.writeByte(b);
    }

    @Override
    public void writeShort(short s) {
        buffer.writeShort(s);
    }

    @Override
    public void writeShortLe(short s) {
        buffer.writeShortLe(s);
    }

    @Override
    public void writeInt(int i) {
        buffer.writeInt(i);
    }

    @Override
    public void writeIntLe(int i) {
        buffer.writeIntLe(i);
    }

    @Override
    public void writeLong(long l) {
        buffer.writeLong(l);
    }

    @Override
    public void writeLongLe(long l) {
        buffer.writeLongLe(l);
    }

    @Override
    public void write(ByteString byteString) {
        buffer.write(byteString.getData());
    }

    @Override
    public void writeUtf8(String string, int startIndex, int endIndex) {
        buffer.writeUtf8(string, startIndex, endIndex);
    }

    @Override
    public void writeString(String string, Charset charset) {
        buffer.write(string.getBytes(charset));
    }

    @Override
    public void writeString(String string, int start, int end, Charset charset) {
        buffer.write(string.substring(start, end).getBytes(charset));
    }

    @Override
    public long write(Source source, long length) throws IOException {
        return buffer.write(source, length);
    }
}
