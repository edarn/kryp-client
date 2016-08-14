package se.tna.commonloggerservice;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by thomas on 2014-10-17.
 */
public class ConcurrentMaxSizeArray<E> {
    private ArrayBlockingQueue<E> theList;
    private int maxSize = 0;

    public ConcurrentMaxSizeArray() {
        initiate(100);
    }

    public ConcurrentMaxSizeArray(int maxSize) {
        initiate(maxSize);
    }

    private void initiate(int max) {
        theList = new ArrayBlockingQueue<E>(max);
        maxSize = max;
    }

    public synchronized void add(E object) {
        if (theList.size() >= maxSize) {
            theList.poll(); //Queue is full, remove one item.
        }
        theList.add(object);
    }

    public E getMostRecentlyAddedObject() {
        E[] arrayList = (E[]) theList.toArray();

        return arrayList[arrayList.length - 1];
    }

    public synchronized int size() {
        return theList.size();
    }

    public synchronized void clear() {
        theList.clear();
    }

    public Object[] getArray() {

        return theList.toArray();
    }
}
