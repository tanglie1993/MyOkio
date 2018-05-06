package okio;

/**
 * Created by pc on 2018/1/27.
 */
public class Segment {

    public static final int SIZE = 8192;

    public final byte[] data;

    public int front = 0;

    public int rear;

    public boolean isShared;

    public boolean isOwner = true;

    Segment prev;

    Segment next;

    Segment() {
        this.data = new byte[SIZE];
    }

    public Segment(Segment segment) {
        this.data = segment.data;
        isShared = true;
        segment.isShared = true;
        isOwner = false;
        this.front = segment.front;
        this.rear = segment.rear;
    }
}
