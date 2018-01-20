package okio;

import java.io.*;

/**
 * Created by pc on 2018/1/18.
 */
public class Okio {

    public static Sink sink(File file) throws FileNotFoundException {
        return sink(new FileOutputStream(file));
    }

    private static Sink sink(OutputStream outputStream) {
        return new Sink() {
            @Override
            public void writeUtf8(String s) throws IOException {
                outputStream.write(s.getBytes());
            }

            @Override
            public void close() throws IOException {
                outputStream.close();
            }
        };
    }

    public static BufferedSink buffer(Sink sink) {
        return new BufferedSink(){
            @Override
            public void writeUtf8(String s) throws IOException {
                sink.writeUtf8(s);
            }

            @Override
            public void close() throws IOException {
                sink.close();
            }
        };
    }

    public static Source source(File file) throws FileNotFoundException {
        return source(new FileInputStream(file));
    }

    private static Source source(FileInputStream inputStream) {
        return new Source() {
            @Override
            public String readUtf8() throws IOException {
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                return new String(bytes);
            }

            @Override
            public void close() throws IOException {
                inputStream.close();
            }
        };
    }

    public static BufferedSource buffer(Source source) {
        return new BufferedSource(){
            @Override
            public String readUtf8() throws IOException {
                return source.readUtf8();
            }

            @Override
            public void close() throws IOException {
                source.close();
            }
        };
    }

    public static Sink appendingSink(File file) throws FileNotFoundException {
        return sink(new FileOutputStream(file, true));
    }
}
