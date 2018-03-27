package okio;

import java.io.*;
import java.nio.file.Path;

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
}
