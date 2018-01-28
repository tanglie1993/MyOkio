package okio;

import java.util.*;

import static okio.Segment.SIZE;

/**
 * Created by pc on 2018/1/28.
 */
public class SegmentList implements Cloneable {

    private LinkedList<Segment> segmentList = new LinkedList<>();

    SegmentList(SegmentList segmentList) {
        this.segmentList = segmentList.segmentList;
    }

    SegmentList() {

    }

    private SegmentList(LinkedList<Segment> newSegmentList) {
        this.segmentList = newSegmentList;
    }

    public SegmentList clone(){
        LinkedList<Segment> newSegmentList = new LinkedList<>();
        for(Segment segment : segmentList){
            add(newSegmentList, new Segment(segment));
        }
        return new SegmentList(newSegmentList);
    }

    private void add(LinkedList<Segment> segmentList, Segment toAdd) {
        if(segmentList.size() > 0){
            toAdd.prev = segmentList.getLast();
            segmentList.getLast().next = toAdd;
        }
        segmentList.add(toAdd);
    }

    void write(byte[] bytes) {
        int nextWrite = 0;
        while(nextWrite < bytes.length){
            Segment toWrite;
            if(segmentList.size() == 0 || segmentList.getLast().front >= segmentList.getLast().rear || segmentList.getLast().rear >= Segment.SIZE){
                toWrite = new Segment();
                add(segmentList, toWrite);
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

    void remove(int length) {
        int removed = 0;
        while(removed < length){
            if(segmentList.size() == 0){
                return;
            }
            Segment first = segmentList.getFirst();
            int canRemove = first.rear - first.front;
            if(canRemove >= length - removed){
                first.front += length - removed;
                return;
            }
            remove();
            removed += canRemove;
        }
    }

    byte read() {
        while(true){
            if(segmentList.size() == 0){
                return -1;
            }
            Segment first = segmentList.getFirst();
            if(first.rear <= first.front){
                remove();
                continue;
            }
            byte result = first.data[first.front++];
            if(first.front >= first.rear || first.front >= Segment.SIZE){
                remove();
            }
            return result;
        }
    }

    boolean has(int count) {
        int counted = 0;
        for(Segment segment : segmentList){
            counted += segment.rear - segment.front;
            if(counted >= count){
                return true;
            }
        }
        return false;
    }

    int available() {
        int result = 0;
        for(Segment segment : segmentList){
            result += segment.rear - segment.front;
        }
        return result;
    }

    public int read(byte[] sink) {
        int removed = 0;
        while(removed < sink.length){
            if(segmentList.size() == 0){
                return removed;
            }
            Segment first = segmentList.getFirst();
            int canRemove = first.rear - first.front;
            if(canRemove >= sink.length - removed){
                System.arraycopy(first.data, first.front, sink, removed, sink.length - removed);
                first.front += sink.length - removed;
                return sink.length;
            }else{
                System.arraycopy(first.data, first.front, sink, removed, canRemove);
                removed += canRemove;
                remove();
            }
        }
        return sink.length;
    }

    public int read(byte[] sink, final int offset, final int byteCount) {
        if(offset + byteCount > sink.length){
            throw new ArrayIndexOutOfBoundsException();
        }
        int sinkWriteIndex = offset;
        while(true){
            if(sinkWriteIndex >= sink.length || sinkWriteIndex - offset >= byteCount || segmentList.size() == 0){
                return sinkWriteIndex - offset;
            }
            Segment first = segmentList.getFirst();
            if(first.rear - first.front >= byteCount - sinkWriteIndex){
                System.arraycopy(first.data, first.front, sink, sinkWriteIndex, byteCount - (sinkWriteIndex - offset));
                first.front += byteCount - sinkWriteIndex;
                return byteCount;
            }else{
                System.arraycopy(first.data, first.front, sink, sinkWriteIndex, first.rear - first.front);
                sinkWriteIndex += first.rear - first.front;
                remove();
            }
        }
    }

    private void remove() {
        if(segmentList.size() == 0){
            return;
        }
        segmentList.removeFirst();
        if(segmentList.size() > 0){
            segmentList.getFirst().prev = null;
        }
    }

    public int indexOf(byte target, int fromIndex, int toIndex) {
        if(fromIndex < 0){
            throw new IllegalArgumentException("fromIndex < 0");
        }
        if(fromIndex > toIndex){
            throw new IllegalArgumentException("Expected failure: fromIndex > toIndex");
        }
        if(fromIndex == toIndex){
            return -1;
        }
        int currentSearchIndex = 0;
        for (Segment segment : segmentList) {
            for (int i = segment.front; i < segment.rear; i++) {
                if(segment.rear - segment.front + currentSearchIndex < fromIndex){
                    currentSearchIndex += segment.rear - segment.front;
                    continue;
                }
                if (segment.data[i] == target && currentSearchIndex >= fromIndex) {
                    return currentSearchIndex;
                }
                currentSearchIndex++;
            }
        }
        return -1;
    }

    public int indexOf(ByteString byteString, int fromIndex) {
        if(byteString == null || byteString.getData().length == 0){
            throw new IllegalArgumentException("bytes is empty");
        }
        if(fromIndex < 0){
            throw new IllegalArgumentException("fromIndex < 0");
        }
        int currentSearchIndex = 0;
        for (Segment segment : segmentList) {
            if(segment.rear - segment.front + currentSearchIndex < fromIndex){
                currentSearchIndex += segment.rear - segment.front;
                continue;
            }
            for (int i = segment.front; i < segment.rear; i++) {
                if (segment.data[i] == byteString.getData()[0] && currentSearchIndex >= fromIndex) {
                    if (match(segment, i, byteString, 0)) {
                        return currentSearchIndex;
                    }
                }
                currentSearchIndex++;
            }
        }
        return -1;
    }

    private boolean match(Segment segment, final int startIndex, ByteString byteString, final int byteStringStartIndex) {
        int matchedCount = 0;
        for (int i = startIndex; i < segment.rear && byteString.getData().length > i - startIndex + byteStringStartIndex; i++) {
            if (segment.data[i] != byteString.getData()[i - startIndex + byteStringStartIndex]) {
                return false;
            }else{
                matchedCount++;
            }
        }
        if(matchedCount + byteStringStartIndex == byteString.getData().length){
            return true;
        }
        if(byteString.getData().length == byteStringStartIndex + segment.rear - startIndex){
            return true;
        }
        if(segment.next == null){
            return false;
        }
        return match(segment.next, segment.next.front, byteString, byteStringStartIndex + matchedCount);
    }

    public int indexOfElement(ByteString byteString, int fromIndex) {
        if(byteString == null || byteString.getData().length == 0){
            return -1;
        }
        Set<Byte> set = new HashSet<>();
        for (byte b : byteString.getData()){
            set.add(b);
        }
        int cumulativeIndex = 0;
        for (Segment segment : segmentList) {
            if(segment.rear - segment.front + cumulativeIndex < fromIndex){
                cumulativeIndex += segment.rear - segment.front;
                continue;
            }
            for (int i = segment.front; i < segment.rear; i++) {
                if (cumulativeIndex >= fromIndex && set.contains(segment.data[i])) {
                    return cumulativeIndex;
                }
                cumulativeIndex++;
            }
        }
        return -1;
    }
}
