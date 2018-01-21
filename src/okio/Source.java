package okio;

import java.io.IOException;

/**
 * Created by pc on 2018/1/18.
 */
public interface Source {

    int read(Buffer sink, int length) throws IOException;

    void close() throws IOException;
}
