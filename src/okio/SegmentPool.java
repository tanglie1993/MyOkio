package okio;

public class SegmentPool {

    private static final int TOTAL_COUNT = 0;

    static Segment segment;

    static int count = 0;

    static synchronized Segment getSegment(){
        if(segment == null){
            return new Segment();
        }else{
            Segment result = segment;
            segment = null;
            result.next = null;
            segment = segment.next;
            count--;
            return result;
        }
    }

    static synchronized void recycle(Segment segment){
        if(count >= TOTAL_COUNT){
            return;
        }
        segment.front = 0;
        segment.rear = 0;
        segment.next = null;
        segment.next = SegmentPool.segment;
        segment.prev = null;
        SegmentPool.segment = segment;
        count++;
    }
}