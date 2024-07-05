package server.queue;

import java.util.Collection;
import java.util.LinkedList;

import java.util.function.Function;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class RankedQueue<E> implements BarrierQueue<E> {

    private final LinkedList<E> queue = new LinkedList<>();
    private final int required;

    private final Function<E, Integer> evaluationFunction;
    private int tolerance;
    private final int toleranceIncrement;
    private final int toleranceIncrementInterval;
    private final TimeUnit toleranceIncrementIntervalUnit;

    private final Lock lock = new ReentrantLock(true);
    private final Condition condition = lock.newCondition();

    public RankedQueue(int parties, Function<E, Integer> ef, int initialTolerance, int time, TimeUnit unit) {
        required = parties;

        evaluationFunction = ef;
        tolerance = initialTolerance;
        toleranceIncrement = initialTolerance;
        toleranceIncrementInterval = time;
        toleranceIncrementIntervalUnit = unit;

        trackTolerance();
    }

    private void trackTolerance() {
        Thread.startVirtualThread(() -> {
            while (true) {
                if (queue.isEmpty()) {
                    await();
                }
                else {
                    incrementTolerance();
                }
            }
        });
    }

    private void await() {
        lock.lock();

        try {
            while (queue.isEmpty()) {
                try {
                    condition.await();
                }
                catch (InterruptedException ignored) {

                }
            }
        }
        finally {
            lock.unlock();
        }
    }

    private void incrementTolerance() {
        try {
            toleranceIncrementIntervalUnit.sleep(toleranceIncrementInterval);
        }
        catch (InterruptedException ignored) {

        }

        lock.lock();

        try {
            tolerance += toleranceIncrement;
            condition.signal();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void put(E element) {
        lock.lock();

        try {
            queue.add(element);
            condition.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public void drainTo(Collection<E> collection) throws InterruptedException {
        lock.lock();

        try {
            while (!getElements(collection)) {
                condition.await();
            }
        }
        finally {
            lock.unlock();
        }
    }

    private boolean getElements(Collection<E> collection) {
        if (queue.size() < required) return false;

        for (int i = 0; i < queue.size(); ++i) {
            collection.clear();

            E element = queue.get(i);
            int eval = evaluationFunction.apply(element);

            collection.add(element);

            for (int j = i + 1; j < queue.size(); ++j) {
                E elem = queue.get(j);
                int e = evaluationFunction.apply(elem);

                if (Math.abs(eval - e) < tolerance && collection.size() < required) {
                    collection.add(elem);
                }
            }

            if (collection.size() == required) {
                for (E elem : collection) {
                    queue.remove(elem);
                }
                if (tolerance > toleranceIncrement) {
                    tolerance -= toleranceIncrement;
                }
                return true;
            }
        }

        return false;
    }

}