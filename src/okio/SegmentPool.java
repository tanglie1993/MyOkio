package okio;

public class SegmentPool {

    private static final int TOTAL_COUNT = 100;

    static Segment segment;

    static int count = 0;

    static synchronized Segment getSegment(){
        if(segment == null){
            return new Segment();
        }else{
            Segment result = segment;
            result.next = null;
            segment = segment.next;
            count--;
            return result;
        }
    }

    static synchronized void recycle(Segment segment){
        if(count >= TOTAL_COUNT || segment.isShared){
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

    public static synchronized Segment getSegment(Segment next) {
        if(segment == null){
            return new Segment(next);
        }else{
            Segment result = segment;
            result.next = null;
            segment = segment.next;
            count--;
            return result;
        }
    }
}