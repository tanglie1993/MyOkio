package okio;

/**
 * Created by pc on 2018/4/15.
 */
public class SegmentPool {
    static Segment segment;


    static synchronized Segment getSegment(){
        if(segment == null){
            return new Segment();
        }else{
            Segment result = segment;
            segment = null;
            return result;
        }
    }

    static synchronized void recycle(Segment segment){
        segment.front = 0;
        segment.rear = 0;
        segment.next = null;
        segment.prev = null;
        SegmentPool.segment = segment;
    }
}
