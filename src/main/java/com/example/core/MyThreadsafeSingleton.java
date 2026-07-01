package com.example.core;

/**
 * Thread-safe singleton example.
 */
public final class MyThreadsafeSingleton {
    // volatile instance for double-checked locking
    private static volatile MyThreadsafeSingleton instance;

    // example state
    private int value;

    // private constructor
    private MyThreadsafeSingleton() {
        this.value = 0;
    }

    // get singleton instance
    public static MyThreadsafeSingleton getInstance() {
        if (instance == null) {
            synchronized (MyThreadsafeSingleton.class) {
                if (instance == null) {
                    instance = new MyThreadsafeSingleton();
                }
            }
        }
        return instance;
    }

    // example getter/setter
    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    // reset helper for tests
    public static void reset() {
        synchronized (MyThreadsafeSingleton.class) {
            instance = null;
        }
    }
}
