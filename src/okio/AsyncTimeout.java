package okio;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by pc on 2018/2/25.
 */
public class AsyncTimeout extends Timeout {

    private static final long IDLE_TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(60);

    private static PriorityQueue<AsyncTimeout> queue;

    private boolean inQueue;
    private long timeoutAt;

    protected void timedOut() {

    }

    public final void enter() {
        long timeoutNanos = getTimeoutNanos();
        boolean hasDeadline = hasDeadline();
        if (timeoutNanos == 0 && !hasDeadline) {
            return;
        }
        inQueue = true;
        scheduleTimeout(this, timeoutNanos, hasDeadline);
    }

    private static synchronized void scheduleTimeout(AsyncTimeout node, long timeoutNanos, boolean hasDeadline) {
        if (queue == null) {
            queue = new PriorityQueue<>((o1, o2) -> {
                if(o1.timeoutAt > o2.timeoutAt){
                    return 1;
                }else if(o1.timeoutAt < o2.timeoutAt){
                    return -1;
                }else{
                    return 0;
                }
            });
            new Watchdog().start();
        }

        long now = System.nanoTime();
        if (timeoutNanos != 0 && hasDeadline) {
            node.timeoutAt = now + Math.min(timeoutNanos, node.getDeadlineNanoTime() - now);
        } else if (timeoutNanos != 0) {
            node.timeoutAt = now + timeoutNanos;
        } else if (hasDeadline) {
            node.timeoutAt = node.getDeadlineNanoTime();
        }

        queue.add(node);
        AsyncTimeout.class.notify();
    }

    public final boolean exit() {
        if (!inQueue) {
            return false;
        }
        inQueue = false;
        return cancelScheduledTimeout(this);
    }

    private static synchronized boolean cancelScheduledTimeout(AsyncTimeout node) {
        if(queue == null){
            return true;
        }
        if(queue.contains(node)){
            queue.remove(node);
            return false;
        }
        return true;
    }

    private static final class Watchdog extends Thread {
        Watchdog() {
            super("Okio Watchdog");
            setDaemon(true);
        }

        public void run() {
            while (true) {
                try {
                    AsyncTimeout timedOut;
                    synchronized (AsyncTimeout.class) {
                        timedOut = awaitTimeout();
                        if (timedOut == null) {
                            continue;
                        }else{
                            timedOut.timedOut();
                        }
                        if (queue.isEmpty()) {
                            queue = null;
                            return;
                        }
                    }
                } catch (InterruptedException e) {
                }
            }
        }
    }

    static AsyncTimeout awaitTimeout() throws InterruptedException {
        if(queue == null){
            return null;
        }
        if (queue.isEmpty()) {
            AsyncTimeout.class.wait(IDLE_TIMEOUT_MILLIS);
            return queue.isEmpty() ? null : queue.peek(); // The situation has changed.
        }

        AsyncTimeout node = queue.peek();

        long waitNanos = node.timeoutAt - System.nanoTime();

        if (waitNanos > 0) {
            long waitMillis = waitNanos / 1000000L;
            waitNanos -= (waitMillis * 1000000L);
            AsyncTimeout.class.wait(waitMillis, (int) waitNanos);
            return null;
        }

        queue.poll();
        return node;
    }
}
