package okio;

import java.util.LinkedList;

import static okio.Segment.SIZE;

/**
 * Created by pc on 2018/1/28.
 */
public class SegmentList implements Cloneable {

    private LinkedList<Segment> segmentList = new LinkedList<>();

    public SegmentList(SegmentList segmentList) {
        this.segmentList = segmentList.segmentList;
    }

    public SegmentList() {

    }

    public SegmentList(LinkedList<Segment> newSegmentList) {
        this.segmentList = newSegmentList;
    }

    public SegmentList clone(){
        LinkedList<Segment> newSegmentList = new LinkedList<>();
        for(Segment segment : segmentList){
            newSegmentList.add(new Segment(segment));
        }
        return new SegmentList(newSegmentList);
    }

    public void write(byte[] bytes) {
        int nextWrite = 0;
        while(nextWrite < bytes.length){
            Segment toWrite;
            Segment lastSegment = segmentList.getLast();
            if(segmentList.size() == 0 || lastSegment.front >= lastSegment.rear || lastSegment.rear >= Segment.SIZE){
                toWrite = new Segment();
                segmentList.add(toWrite);
            }else{
                toWrite = segmentList.getLast();
            }
            int available = Segment.SIZE - toWrite.rear;
            if(available >= bytes.length - nextWrite){
                System.arraycopy(bytes, nextWrite, toWrite.data, toWrite.rear, bytes.length - nextWrite);
                toWrite.rear += bytes.length - nextWrite;
                break;
            }else{
                System.arraycopy(bytes, nextWrite, toWrite.data, toWrite.rear, available);
                toWrite.rear = Segment.SIZE;
                nextWrite += available;
            }
        }
    }

    public void remove(int length) {
        
    }
}
