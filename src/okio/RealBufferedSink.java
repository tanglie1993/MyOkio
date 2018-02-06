package okio;

import java.io.IOException;
import java.io.OutputStream;

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
    public void writeAll(Source source) throws IOException {

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
}
