package io.netty.buffer;

import io.netty.util.Recycler;

/**
 * 通过Unsafe的方式创建内存
 * */
public class PooledUnsafeHeapByteBuf extends PooledHeapByteBuf{

    private static final Recycler<PooledUnsafeHeapByteBuf> RECYCLER = new Recycler<PooledUnsafeHeapByteBuf>() {
        @Override
        protected PooledUnsafeHeapByteBuf newObject(Handle<PooledUnsafeHeapByteBuf> handle) {
            return new PooledUnsafeHeapByteBuf(handle, 0); // 真正创建 PooledUnsafeHeapByteBuf 对象
        }

    };


    static PooledUnsafeHeapByteBuf newUnsafeInstance(int maxCapacity) {
        // 从 Recycler 的对象池中获得 PooledUnsafeHeapByteBuf 对象,如果没有则进行创建
        PooledUnsafeHeapByteBuf buf = RECYCLER.get();
        // 重置 PooledUnsafeHeapByteBuf 的属性
        buf.reuse(maxCapacity);
        return buf;
    }

    private PooledUnsafeHeapByteBuf(Recycler.Handle<PooledUnsafeHeapByteBuf> recyclerHandle, int maxCapacity) {
        super(recyclerHandle, maxCapacity);
    }


}
