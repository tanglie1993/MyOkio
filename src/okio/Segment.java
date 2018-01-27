package okio;

/**
 * Created by pc on 2018/1/27.
 */
public class Segment {

    public static final int SIZE = 8192;

    final byte[] data;

    /** The next byte of application data byte to read in this segment. */
    int pos;

    Segment prev;

    Segment next;

    Segment() {
        this.data = new byte[SIZE];
    }

    public Segment pop() {
        Segment result = next != this ? next : null;
        prev.next = next;
        next.prev = prev;
        next = null;
        prev = null;
        return result;
    }

    public Segment push(Segment segment) {
        segment.prev = this;
        segment.next = next;
        next.prev = segment;
        next = segment;
        return segment;
    }
}
