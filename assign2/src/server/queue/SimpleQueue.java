package server.queue;

import java.util.Collection;
import java.util.Queue;
import java.util.LinkedList;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class SimpleQueue<E> implements BarrierQueue<E> {

    private final Queue<E> queue = new LinkedList<>();
    private final int required;

    private final Lock lock = new ReentrantLock(true);
    private final Condition condition = lock.newCondition();

    public SimpleQueue(int parties) {
        required = parties;
    }

    @Override
    public void put(E element) {
        lock.lock();

        try {
            queue.add(element);
            condition.signal();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void drainTo(Collection<E> collection) throws InterruptedException {
        lock.lock();

        try {
            while (queue.size() < required) {
                condition.await();
            }

            for (int i = 0; i < required; ++i) {
                collection.add(queue.remove());
            }
        }
        finally {
            lock.unlock();
        }
    }

}
