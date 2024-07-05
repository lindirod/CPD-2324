package server.queue;

import java.util.Collection;

public interface BarrierQueue<E> {

    void put(E element);

    void drainTo(Collection<E> collection) throws InterruptedException;

}
