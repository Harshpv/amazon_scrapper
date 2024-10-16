package com.carrot.thread;

public class DynamicCountDownLatch {
    private int count;
    private final Object lock = new Object();

    public DynamicCountDownLatch(int initialCount) {
        this.count = initialCount;
    }

    public void countDown() {
        synchronized (lock) {
            count--;
            if (count == 0) {
                lock.notifyAll();
            }
        }
    }

    public void countUp() {
        synchronized (lock) {
            count++;
        }
    }

    public void await() throws InterruptedException {
        synchronized (lock) {
            while (count > 0) {
                lock.wait();
            }
        }
    }
}
