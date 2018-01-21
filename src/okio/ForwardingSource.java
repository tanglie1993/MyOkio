package okio;

import java.io.IOException;

/**
 * Created by pc on 2018/1/21.
 */
public class ForwardingSource implements Source {

    private final Source source;

    public ForwardingSource(Source source) {
        if (source == null) throw new IllegalArgumentException("source == null");
        this.source = source;
    }

    @Override
    public int read(Buffer sink, int length) throws IOException {
        return source.read(sink, length);
    }

    @Override
    public void close() throws IOException {
        source.close();
    }
}
