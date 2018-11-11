package io.netty.buffer;

public interface ByteBufAllocator {

    ByteBuf heapBuffer(int initialCapacity, int maxCapacity);
}
