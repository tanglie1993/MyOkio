package okio;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by pc on 2018/2/25.
 */
public class Timeout {

    public static final Timeout NONE = new Timeout() {};

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

    public final void waitUntilNotified(Object monitor) throws InterruptedIOException {
        try {
            boolean hasDeadline = hasDeadline();
            long timeoutNanos = Timeout.this.timeoutNanos;

            if (!hasDeadline && timeoutNanos == 0L) {
                monitor.wait();
                return;
            }

            long waitNanos;
            long start = System.nanoTime();
            if (hasDeadline && timeoutNanos != 0) {
                long deadlineNanos = deadlineNanoTime - start;
                waitNanos = Math.min(timeoutNanos, deadlineNanos);
            } else if (hasDeadline) {
                waitNanos = deadlineNanoTime - start;
            } else {
                waitNanos = timeoutNanos;
            }

            long elapsedNanos = 0L;
            if (waitNanos > 0L) {
                long waitMillis = waitNanos / 1000000L;
                monitor.wait(waitMillis, (int) (waitNanos - waitMillis * 1000000L));
                elapsedNanos = System.nanoTime() - start;
            }

            // Throw if the timeout elapsed before the monitor was notified.
            if (elapsedNanos >= waitNanos) {
                throw new InterruptedIOException("timeout");
            }
        } catch (InterruptedException e) {
            throw new InterruptedIOException("interrupted");
        }
    }

    public void throwIfReached() throws IOException {
        if (Thread.interrupted()) {
            throw new InterruptedIOException("thread interrupted");
        }

        if (hasDeadline && deadlineNanoTime - System.nanoTime() <= 0) {
            throw new InterruptedIOException("deadline reached");
        }
    }
}
