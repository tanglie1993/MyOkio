package okio;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.util.logging.Level;

import static okio.Util.checkOffsetAndCount;

/**
 * Created by pc on 2018/1/18.
 */
public class Okio {

    public static Sink sink(File file) throws FileNotFoundException {
        if(file == null){
            throw new NullPointerException("file cannot be null!");
        }
        return sink(new FileOutputStream(file));
    }

    public static Sink sink(Path path) throws FileNotFoundException {
        if(path == null){
            throw new NullPointerException("path cannot be null!");
        }
        return sink(new FileOutputStream(path.toFile()));
    }

    public static Source source(Path path) throws FileNotFoundException {
        if(path == null){
            throw new NullPointerException("path cannot be null!");
        }
        return source(new FileInputStream(path.toFile()));
    }

    public static Sink sink(OutputStream out) {
        if(out == null){
            throw new NullPointerException("outputStream cannot be null!");
        }
        if (out == null) throw new IllegalArgumentException("out == null");
        Timeout timeout = new Timeout();
        return new Sink() {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                Util.checkOffsetAndCount(source.size(), 0, byteCount);
                while (byteCount > 0) {
                    Segment head = source.segmentList.getFirst();
                    int toCopy = (int) Math.min(byteCount, head.rear - head.front);
                    out.write(head.data, head.front, toCopy);
                    source.skip(toCopy);
                    byteCount -= toCopy;
                }
            }

            @Override
            public void flush() throws IOException {
                out.flush();
            }

            @Override
            public void close() throws IOException {
                out.close();
            }

            @Override
            public Timeout timeout() {
                return timeout;
            }

            @Override
            public String toString() {
                return "sink(" + out + ")";
            }
        };
    }

    public static BufferedSink buffer(Sink sink) {
        if(sink == null){
            throw new NullPointerException("sink == null");
        }
        return new RealBufferedSink(sink);
    }

    public static Source source(File file) throws FileNotFoundException {
        if(file == null){
            throw new NullPointerException("file cannot be null!");
        }
        return source(new FileInputStream(file));
    }

    public static Source source(InputStream inputStream) {
        if(inputStream == null){
            throw new NullPointerException("inputStream cannot be null!");
        }
        return new RealBufferedSource(inputStream);
    }

    public static BufferedSource buffer(Source source) {
        if(source == null){
            throw new NullPointerException("source == null");
        }
        return new RealBufferedSource(source);
    }

    public static Sink appendingSink(File file) throws FileNotFoundException {
        if(file == null){
            throw new NullPointerException("file cannot be null!");
        }
        return sink(new FileOutputStream(file, true));
    }

    public static Sink blackhole() {
        return new Sink() {
            @Override public void write(Buffer source, long byteCount) throws IOException {
                source.skip(byteCount);
            }

            @Override public void flush() throws IOException {
            }

            @Override public Timeout timeout() {
                return Timeout.NONE;
            }

            @Override public void close() throws IOException {
            }
        };
    }

    public static Source source(Socket socket) throws IOException {
        if (socket == null) {
            throw new IllegalArgumentException("socket == null");
        }
        AsyncTimeout timeout = timeout(socket);
        Source source = source(socket.getInputStream(), timeout);
        return timeout.source(source);
    }

    private static AsyncTimeout timeout(final Socket socket) {
        return new AsyncTimeout() {
            @Override protected IOException newTimeoutException(IOException cause) {
                InterruptedIOException ioe = new SocketTimeoutException("timeout");
                if (cause != null) {
                    ioe.initCause(cause);
                }
                return ioe;
            }

            @Override protected void timedOut() {
                try {
                    socket.close();
                } catch (Exception e) {
//                    logger.log(Level.WARNING, "Failed to close timed out socket " + socket, e);
                }
            }
        };
    }

    private static Source source(final InputStream in, final Timeout timeout) {
        if (in == null) throw new IllegalArgumentException("in == null");
        if (timeout == null) throw new IllegalArgumentException("timeout == null");

        return new Source() {
            @Override public long read(Buffer sink, long byteCount) throws IOException {
                if (byteCount < 0) {
                    throw new IllegalArgumentException("byteCount < 0: " + byteCount);
                }
                if (byteCount == 0) {
                    return 0;
                }
//                timeout.throwIfReached();
                Segment tail = sink.writableSegment(1);
                int maxToCopy = (int) Math.min(byteCount, Segment.SIZE - tail.rear);
                int bytesRead = in.read(tail.data, tail.rear, maxToCopy);
                if (bytesRead == -1) {
                    return -1;
                }
                tail.rear += bytesRead;
                return bytesRead;
            }

            @Override public void close() throws IOException {
                in.close();
            }

            @Override public Timeout timeout() {
                return timeout;
            }
        };
    }

    public static Sink sink(Socket socket) throws IOException {
        if (socket == null) throw new IllegalArgumentException("socket == null");
        AsyncTimeout timeout = timeout(socket);
        Sink sink = sink(socket.getOutputStream(), timeout);
        return timeout.sink(sink);
    }

    private static Sink sink(final OutputStream out, final Timeout timeout) {
        if (out == null) {
            throw new IllegalArgumentException("out == null");
        }
        if (timeout == null) {
            throw new IllegalArgumentException("timeout == null");
        }

        return new Sink() {
            @Override public void write(Buffer source, long byteCount) throws IOException {
                checkOffsetAndCount(source.size(), 0, byteCount);
                while (byteCount > 0) {
                    timeout.throwIfReached();
                    Segment head = source.segmentList.getFirst();
                    int toCopy = (int) Math.min(byteCount, head.rear - head.front);
                    System.out.println("okio sink write byteCount " + byteCount);
                    System.out.println("okio sink write front " + head.front);
                    System.out.println("okio sink write toCopy " + toCopy);
                    System.out.println("okio sink write source.size() " + source.size());
                    out.write(head.data, head.front, toCopy);
                    byteCount -= toCopy;
                    source.skip(toCopy);
                }
            }

            @Override public void flush() throws IOException {
                out.flush();
            }

            @Override public void close() throws IOException {
                out.close();
            }

            @Override public Timeout timeout() {
                return timeout;
            }

            @Override public String toString() {
                return "sink(" + out + ")";
            }
        };
    }
}
