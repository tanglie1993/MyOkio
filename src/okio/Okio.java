package okio;

import java.io.*;
import java.nio.file.Path;

/**
 * Created by pc on 2018/1/18.
 */
public class Okio {

    public static Sink sink(File file) throws FileNotFoundException {
        return sink(new FileOutputStream(file));
    }

    public static Sink sink(Path path) throws FileNotFoundException {
        return sink(new FileOutputStream(path.toFile()));
    }

    public static Source source(Path path) throws FileNotFoundException {
        return source(new FileInputStream(path.toFile()));
    }

    public static Sink sink(OutputStream outputStream) {
        return new RealBufferedSink(outputStream);
    }

    public static BufferedSink buffer(Sink sink) {
        return new RealBufferedSink(sink);
    }

    public static Source source(File file) throws FileNotFoundException {
        return source(new FileInputStream(file));
    }

    private static Source source(InputStream inputStream) {
        return new RealBufferedSource(inputStream);
    }

    public static BufferedSource buffer(Source source) {
        return new RealBufferedSource(source);
    }

    public static Sink appendingSink(File file) throws FileNotFoundException {
        return sink(new FileOutputStream(file, true));
    }
}
