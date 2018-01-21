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
            public void write(Buffer data, int i) throws IOException {
                outputStream.write(data.pop(i).getBytes());
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
    public void write(Buffer data, int i) throws IOException {
        buffer.write(data, i);
        flush();
    }

    @Override
    public void flush() throws IOException {
        sink.write(buffer, buffer.size());
    }

    @Override
    public void writeUtf8(String s) throws IOException {
        buffer.writeUtf8(s);
    }

    @Override
    public void writeAll(Source source) throws IOException {

    }

    @Override
    public void write(byte[] bytes) {
        buffer.write(bytes);
    }

    @Override
    public void write(Buffer clone, boolean result) {

    }
}
