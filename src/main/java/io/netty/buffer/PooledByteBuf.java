package io.netty.buffer;

import io.netty.util.Recycler;

public abstract class PooledByteBuf<T> extends AbstractReferenceCountedByteBuf  {

    private final Recycler.Handle<PooledByteBuf<T>> recyclerHandle;

    @SuppressWarnings("unchecked")
    protected PooledByteBuf(Recycler.Handle<? extends PooledByteBuf<T>> recyclerHandle, int maxCapacity) {
        super(maxCapacity);
        this.recyclerHandle = (Recycler.Handle<PooledByteBuf<T>>) recyclerHandle;
    }

    final void reuse(int maxCapacity) {
        // 设置最大容量
        maxCapacity(maxCapacity);
        // 设置引用数量为 0
        setRefCnt(1);
        // 重置读写索引为 0
        setIndex0(0, 0);
        // 重置读写标记位为 0
        discardMarks();
    }


}
