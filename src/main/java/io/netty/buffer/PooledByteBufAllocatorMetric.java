package io.netty.buffer;

public final class PooledByteBufAllocatorMetric implements ByteBufAllocatorMetric{

    /**池化内存分配*/
    private final PooledByteBufAllocator allocator;

    PooledByteBufAllocatorMetric(PooledByteBufAllocator allocator) {
        this.allocator = allocator;
    }




}
