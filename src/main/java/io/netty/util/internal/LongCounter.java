package io.netty.util.internal;

public interface LongCounter {
    void add(long delta);
    void increment();
    void decrement();
    long value();
}
