package io.netty.buffer;


/**
 * 提供相应的内存分配度量
 * */
public interface ByteBufAllocatorMetricProvider {

    /**返回相应内存分配度量器*/
    ByteBufAllocatorMetric metric();
}
