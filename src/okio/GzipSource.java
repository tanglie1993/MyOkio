package okio;

import java.io.IOException;

/**
 * Created by pc on 2018/1/21.
 */
public class GzipSource implements Source {

    private final Source source;

    public GzipSource(Source source) {
        if (source == null) throw new IllegalArgumentException("source == null");
        this.source = source;
    }

    @Override
    public long read(Buffer sink, long length) throws IOException {
        return source.read(sink, length);
    }

    @Override
    public void close() throws IOException {
        source.close();
    }

    @Override
    public Timeout timeout() {
        return Timeout.NONE;
    }
}
