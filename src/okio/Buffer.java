package okio;

import java.io.IOException;

/**
 * Created by pc on 2018/1/20.
 */
public class Buffer implements BufferedSource, BufferedSink {

    private StringBuffer buffer = new StringBuffer();

    public void writeUtf8(String a) {
        buffer.append(a);
    }

    public void write(Source source, int length) throws IOException {
        source.read(this, length);
    }

    @Override
    public void writeAll(Source source) throws IOException {
        if (source == null) throw new IllegalArgumentException("source == null");
        while(true){
            if(!source.read(this, 20)){
                break;
            }
        }
    }
    @Override
    public String readUtf8() throws IOException {
        return buffer.toString();
    }

    @Override
    public boolean read(Buffer data, int length) throws IOException {
        if(length > buffer.length()){
            length = buffer.length();
        }
        data.writeUtf8(buffer.substring(0, length));
        buffer.delete(0, length);
        return buffer.length() > 0;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void write(Buffer data, int i) {
        buffer.append(data.pop(i));
    }

    @Override
    public void flush() throws IOException {

    }

    public int size() {
        return buffer.length();
    }

    public String pop(int length) {
        if(length > buffer.length()){
            length = buffer.length();
        }
        String result = buffer.substring(0, length);
        buffer.delete(0, length);
        return result;
    }
}
