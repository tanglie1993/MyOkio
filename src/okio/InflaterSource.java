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
    private int bufferBytesHeldByInflater;

    public InflaterSource(BufferedSource source, Inflater inflater) {
        this.source = source;
        this.inflater = inflater;
    }

    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        boolean exhausted = false;
        if(source.exhausted()){
            exhausted = true;
        }
        Segment sourceSegment = source.buffer().segmentList.getFirst();
        long totalInflated = 0;
        while (byteCount > 0) {
            if(sourceSegment == null){
                exhausted = true;
            }
            int toInflate = (int) Math.min(byteCount, sourceSegment.rear - sourceSegment.front);
            if(inflater.needsInput()){
                bufferBytesHeldByInflater = sourceSegment.rear - sourceSegment.front;
                inflater.setInput(sourceSegment.data, sourceSegment.front, toInflate);
            }
            long inflated = inflate(sink);
//            sourceSegment.front += inflated;
            if(inflated > 0){
                return inflated;
            }
            if (inflater.finished()) {
                return -1;
            }
            byteCount -= inflated;
            totalInflated += inflated;
            sourceSegment = sourceSegment.next;
            if(exhausted){
                throw new EOFException();
            }
        }
        return totalInflated;
    }

    private long inflate(Buffer sink) throws IOException {
        long result = 0;
        while (true) {
            try {
                Segment toWrite = sink.writableSegment(1);
                int inflated;
                inflated = inflater.inflate(toWrite.data, toWrite.rear, toWrite.data.length - toWrite.rear);
                if (inflated > 0) {
                    toWrite.rear += inflated;
                    result += inflated;
                    return result;
                }else if(inflater.needsInput() || inflater.finished()){
                    releaseInflatedBytes();
                    return result;
                }
            }catch (DataFormatException e){

            }

        }
    }

    private void releaseInflatedBytes() throws IOException {
        int toRelease = bufferBytesHeldByInflater - inflater.getRemaining();
        source.skip(toRelease);
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
