package okio;


import java.io.IOException;
import java.io.InputStream;

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
            public boolean read(Buffer data, int length) throws IOException {
                int size = length;
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
                return buffer.size() > 0;
            }

            @Override
            public void close() throws IOException {
                inputStream.close();
            }
        };
    }


    @Override
    public boolean read(Buffer data, int length) throws IOException {
        boolean result = buffer.write(source, length);
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
}
