package io.netty.buffer;

/**
 * 内存分配度量
 * */
public interface ByteBufAllocatorMetric {

    /**返回由ByteBufAllocator使用的堆内存字节数或-1(如果未知)*/
    long usedHeapMemory();

    /**返回由ByteBufAllocator使用的堆外内存字节数或-1(如果未知)*/
    long usedDirectMemory();
}
