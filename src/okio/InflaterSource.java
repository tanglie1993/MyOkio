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
        if (byteCount < 0) throw new IllegalArgumentException("byteCount < 0: " + byteCount);
//        if (closed) throw new IllegalStateException("closed");
        if (byteCount == 0) return 0;

        while (true) {
            boolean sourceExhausted = refill();

            // Decompress the inflater's compressed data into the sink.
            try {
                Segment tail = sink.writableSegment(1);
                int bytesInflated = inflater.inflate(tail.data, tail.rear, Segment.SIZE - tail.rear);
                if (bytesInflated > 0) {
                    tail.rear += bytesInflated;
//                    sink.size += bytesInflated;
                    return bytesInflated;
                }
                if (inflater.finished() || inflater.needsDictionary()) {
                    releaseInflatedBytes();
//                    if (tail.front == tail.rear) {
//                        // We allocated a tail segment, but didn't end up needing it. Recycle!
//                        sink.head = tail.pop();
//                        SegmentPool.recycle(tail);
//                    }
                    return -1;
                }
                if (sourceExhausted) throw new EOFException("source exhausted prematurely");
            } catch (DataFormatException e) {
                throw new IOException(e);
            }
        }
    }

    public boolean refill() throws IOException {
        if (!inflater.needsInput()) return false;

        releaseInflatedBytes();
        if (inflater.getRemaining() != 0) throw new IllegalStateException("?"); // TODO: possible?

        // If there are compressed bytes in the source, assign them to the inflater.
        if (source.exhausted()) return true;

        // Assign buffer bytes to the inflater.
        Segment head = source.buffer().segmentList.getFirst();
        bufferBytesHeldByInflater = head.rear - head.front;
        inflater.setInput(head.data, head.front, head.rear - head.front);
        return false;
    }

    private void releaseInflatedBytes() throws IOException {
        if (bufferBytesHeldByInflater == 0) return;
        int toRelease = bufferBytesHeldByInflater - inflater.getRemaining();
        bufferBytesHeldByInflater -= toRelease;
        source.skip(toRelease);
    }

//    private long inflate(Buffer sink) throws IOException {
//        long result = 0;
//        while (true) {
//            try {
//                Segment toWrite = sink.writableSegment(1);
//                int inflated;
//                inflated = inflater.inflate(toWrite.data, toWrite.rear, toWrite.data.length - toWrite.rear);
//                if (inflated > 0) {
//                    toWrite.rear += inflated;
//                    result += inflated;
//                    return result;
//                }else if(inflater.finished() || inflater.needsDictionary()){
//                    return result;
//                }
//            }catch (DataFormatException e){
//
//            }
//
//        }
//    }

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
