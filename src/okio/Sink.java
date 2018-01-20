package okio;

import java.io.IOException;

/**
 * Created by pc on 2018/1/18.
 */
public interface Sink {

    void close() throws IOException;


    void write(Buffer data, int length) throws IOException;

    void flush() throws IOException;
}
