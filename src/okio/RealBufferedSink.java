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

            @Override
            public Timeout timeout() {
                return Timeout.NONE;
            }
        };
    }


    @Override
    public void close() throws IOException {
        IOException toThrow = null;
        try {
            flush();
        }catch (IOException e){
            toThrow = e;
        }
        if(toThrow != null){
            try{
                sink.close();
            }catch (IOException e){

            }
            throw toThrow;
        }else{
            sink.close();
        }

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

    @Override public Timeout timeout() {
        return sink.timeout();
    }

    long totalWriteTime = 0;

    @Override
    public BufferedSink writeUtf8(String s) throws IOException {
        buffer.writeUtf8(s);
        writeCompletedSegmentsToSink();
        return this;
    }

    private void writeCompletedSegmentsToSink() throws IOException {
        long byteCount = buffer.completeSegmentByteCount();
        if (byteCount > 0) {
            sink.write(buffer, byteCount);
        }
    }

    public long getTotalWriteTime() {
        return totalWriteTime;
    }

    public long getSegmentListWriteTime() {
        return buffer.getSegmentListWriteTime();
    }

    public long getBufferWriteTime() {
        return buffer.getTotalWriteTime();
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
    public BufferedSink write(byte[] bytes) throws IOException {
        buffer.write(bytes);
        writeCompletedSegmentsToSink();
        return this;
    }

    @Override
    public void writeByte(byte b) throws IOException {
        buffer.writeByte(b);
        writeCompletedSegmentsToSink();
    }

    @Override
    public void writeShort(short s) throws IOException {
        buffer.writeShort(s);
        writeCompletedSegmentsToSink();
    }

    @Override
    public void writeShortLe(short s) throws IOException {
        buffer.writeShortLe(s);
        writeCompletedSegmentsToSink();
    }

    @Override
    public void writeInt(int i) throws IOException {
        buffer.writeInt(i);
        writeCompletedSegmentsToSink();
    }

    @Override
    public void writeIntLe(int i) throws IOException {
        buffer.writeIntLe(i);
        writeCompletedSegmentsToSink();
    }

    @Override
    public void writeLong(long l) throws IOException {
        buffer.writeLong(l);
        writeCompletedSegmentsToSink();
    }

    @Override
    public void writeLongLe(long l) throws IOException {
        buffer.writeLongLe(l);
        writeCompletedSegmentsToSink();
    }

    @Override
    public BufferedSink write(ByteString byteString) throws IOException {
        buffer.write(byteString.getData());
        writeCompletedSegmentsToSink();
        return this;
    }

    @Override
    public void writeUtf8(String string, int startIndex, int endIndex) throws IOException {
        buffer.writeUtf8(string, startIndex, endIndex);
        writeCompletedSegmentsToSink();
    }

    @Override
    public void writeString(String string, Charset charset) throws IOException {
        buffer.write(string.getBytes(charset));
        writeCompletedSegmentsToSink();
    }

    @Override
    public void writeString(String string, int start, int end, Charset charset) throws IOException {
        buffer.write(string.substring(start, end).getBytes(charset));
        writeCompletedSegmentsToSink();
    }

    @Override
    public long write(Source source, long length) throws IOException {
        long result = buffer.write(source, length);
        writeCompletedSegmentsToSink();
        return result;
    }

    @Override
    public OutputStream outputStream() {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                buffer.writeByte((byte) b);
            }

            @Override
            public void flush() throws IOException {
                sink.write(buffer, buffer.size());
            }

            @Override
            public void write(byte b[], int off, int len) throws IOException {
                if (b == null) {
                    throw new NullPointerException();
                } else if ((off < 0) || (off > b.length) || (len < 0) ||
                        ((off + len) > b.length) || ((off + len) < 0)) {
                    throw new ArrayIndexOutOfBoundsException();
                } else if (len == 0) {
                    return;
                }
                for (int i = 0 ; i < len ; i++) {
                    write(b[off + i]);
                }
            }
        };
    }

    @Override
    public BufferedSink writeDecimalLong(long value) throws IOException {
        buffer.writeDecimalLong(value);
        writeCompletedSegmentsToSink();
        return this;
    }


    @Override
    public BufferedSink writeHexadecimalUnsignedLong(long value) throws IOException {
        buffer.writeHexadecimalUnsignedLong(value);
        writeCompletedSegmentsToSink();
        return this;
    }

    @Override
    public Buffer buffer() {
        return buffer;
    }

    public long getBufferWriteByteTime() {
        return buffer.writeByteTime;
    }
}
