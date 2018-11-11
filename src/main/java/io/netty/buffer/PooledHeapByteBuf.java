package io.netty.buffer;

import io.netty.util.Recycler;

class PooledHeapByteBuf extends PooledByteBuf<byte[]> {

    private static final Recycler<PooledHeapByteBuf> RECYCLER = new Recycler<PooledHeapByteBuf>() {
        @Override
        protected PooledHeapByteBuf newObject(Handle<PooledHeapByteBuf> handle) {
            return new PooledHeapByteBuf(handle, 0); // 真正创建 PooledHeapByteBuf 对象
        }
    };

    static PooledHeapByteBuf newInstance(int maxCapacity) {
        // 从 Recycler 的对象池中获得 PooledHeapByteBuf 对象
        PooledHeapByteBuf buf = RECYCLER.get();
        // 重置 PooledDirectByteBuf 的属性
        buf.reuse(maxCapacity);
        return buf;
    }

    PooledHeapByteBuf(Recycler.Handle<? extends PooledHeapByteBuf> recyclerHandle, int maxCapacity) {
        super(recyclerHandle, maxCapacity);
    }


    @Override
    public int capacity() {
        return 0;
    }

    @Override
    public ByteBuf capacity(int newCapacity) {
        return null;
    }
}
