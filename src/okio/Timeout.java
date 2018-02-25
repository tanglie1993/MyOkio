package okio;

import java.util.concurrent.TimeUnit;

/**
 * Created by pc on 2018/2/25.
 */
public class Timeout {

    private boolean hasDeadline;
    private long deadlineNanoTime;
    private long timeoutNanos;

    public void timeout(int length, TimeUnit timeUnit) {
        this.timeoutNanos = timeUnit.toNanos(length);
    }

    public boolean hasDeadline() {
        return hasDeadline;
    }

    public long getDeadlineNanoTime() {
        return deadlineNanoTime;
    }

    public long getTimeoutNanos() {
        return timeoutNanos;
    }

    public void deadline(int length, TimeUnit timeUnit) {
        this.deadlineNanoTime = timeUnit.toNanos(length);
        this.hasDeadline = true;
    }

    public void setDeadlineNanoTime(long deadlineNanoTime) {
        this.deadlineNanoTime = deadlineNanoTime;
        this.hasDeadline = true;
    }
}
