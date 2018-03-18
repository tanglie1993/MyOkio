package okio;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static okio.Util.checkOffsetAndCount;

/**
 * Created by pc on 2018/3/18.
 */
public class InflaterSource implements Source  {

    private final BufferedSource source;
    private final Inflater inflater;

    public InflaterSource(BufferedSource source, Inflater inflater) {
        this.source = source;
        this.inflater = inflater;
    }

    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        if(source.exhausted()){
            return -1;
        }
        Segment sourceSegment = source.buffer().segmentList.getFirst();
        long inflated = 0;
        while (byteCount > 0) {
            if(sourceSegment == null){
                if(byteCount > 0){
                    throw new EOFException();
                }else{
                    return inflated;
                }
            }
            int toInflate = (int) Math.min(byteCount, sourceSegment.rear - sourceSegment.front);
            inflater.setInput(sourceSegment.data, sourceSegment.front, toInflate);
            sourceSegment.front += toInflate;
            inflate(sink);
            byteCount -= toInflate;
            inflated += toInflate;
            sourceSegment = sourceSegment.next;
        }
        return inflated;
    }

    private void inflate(Buffer sink) throws IOException {
        while (true) {
            try {
                Segment toWrite = sink.writableSegment(1);
                int inflated;
                inflated = inflater.inflate(toWrite.data, toWrite.rear, toWrite.data.length - toWrite.rear);
                if (inflated > 0) {
                    toWrite.rear += inflated;
                }else if(inflater.needsInput()){
                    return;
                }
            }catch (DataFormatException e){

            }

        }
    }

    @Override
    public Timeout timeout() {
        return source.timeout();
    }

    @Override
    public void close() throws IOException {
//        inflate();
        inflater.end();
        source.close();
    }


    @Override public String toString() {
        return getClass().getSimpleName() + "(" + source.toString() + ")";
    }
}
