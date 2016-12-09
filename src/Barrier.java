/**
 * Helps to synchronize between two or more threads that one waits for another.
 *
 * Usage :
 *
 * [[waitForDone()]] on the thread that's waiting for the barrier
 * [[done()]] on the thread that other threads are waiting for (when done)
 */
class Barrier {

    private final Object monitor = new Object();
    private volatile boolean open = false;

    Barrier(boolean open) {
        this.open = open;
    }

    void waitForDone() throws InterruptedException {
        synchronized (monitor) {
            while (!open) {
                monitor.wait();
            }
        }
    }

    void done() {
        synchronized (monitor) {
            open = true;
            monitor.notifyAll();
        }
    }
}
