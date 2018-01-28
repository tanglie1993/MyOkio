package okio;

/**
 * Created by pc on 2018/1/27.
 */
public class Segment {



    public static final int SIZE = 8192;

    final byte[] data;

    int front = 0;

    int rear;

    Segment prev;

    Segment next;

    Segment() {
        this.data = new byte[SIZE];
    }

    public Segment(Segment segment) {
        this();
        System.arraycopy(segment.data, 0, this.data, 0, segment.front);
    }
}
