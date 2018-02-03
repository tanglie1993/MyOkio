package okio;

import java.io.IOException;

/**
 * Created by pc on 2018/1/18.
 */
public interface Source {

    long read(Buffer sink, long length) throws IOException;

    void close() throws IOException;
}
