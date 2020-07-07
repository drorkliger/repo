package bgu.spl.mics.application.passiveObjects;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadCounter implements Serializable {

    private AtomicInteger counter;

    private static class SingletonCounter
    {
        private static final ThreadCounter threadCounter = new ThreadCounter();
    }

    //part 1/3
    private static class SingletonHolder
    {
        private static final SingletonCounter singletonCounter = new SingletonCounter();
    }

    //singleton part 2 of 3

    public ThreadCounter() {
        this.counter=new AtomicInteger(0);
    }


    //singleton part 3 of 3
    /**
     * Retrieves the single instance of this class.
     */
    public static ThreadCounter getInstance()
    {
        return SingletonCounter.threadCounter;
    }

    public int getCounter() {
        return counter.get();
    }

    public void increaseCounter()
    {
        counter.incrementAndGet();
    }
}
