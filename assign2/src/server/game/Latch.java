package server.game;

public class Latch {

    private int count;

    public Latch(int c) {
        count = c;
    }

    public synchronized void await() throws InterruptedException {
        while (count > 0) {
            wait();
        }
    }

    public synchronized void countDown() {
        if (count == 0) {
            return;
        }

        count--;
        if (count == 0) {
            notifyAll();
        }
    }

}
