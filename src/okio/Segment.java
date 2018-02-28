package okio;

/**
 * Created by pc on 2018/1/27.
 */
public class Segment {



    public static final int SIZE = 8192;

    public final byte[] data;

    public int front = 0;

    public int rear;

    Segment prev;

    Segment next;

    Segment() {
        this.data = new byte[SIZE];
    }

    public Segment(Segment segment) {
        this();
        System.arraycopy(segment.data, 0, this.data, 0, Segment.SIZE);
        this.front = segment.front;
        this.rear = segment.rear;
    }
}
