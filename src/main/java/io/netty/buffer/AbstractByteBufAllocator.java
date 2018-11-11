package io.netty.buffer;

import io.netty.util.internal.PlatformDependent;

public abstract class AbstractByteBufAllocator implements ByteBufAllocator{


    /**倾向于创建堆外内存*/
    private final boolean directByDefault;

    /**空 ByteBuf 缓存*/
    private final ByteBuf emptyBuf;

    protected AbstractByteBufAllocator(boolean preferDirect) {
        directByDefault = preferDirect && PlatformDependent.hasUnsafe(); // 支持 Unsafe
        emptyBuf = new EmptyByteBuf(this);
    }


    @Override
    public ByteBuf heapBuffer(int initialCapacity, int maxCapacity) {
        /**
         * 如果初始容量和最大容量都为0 则返回对应空buf
         * */
        if(initialCapacity==0&&maxCapacity==0){
            return emptyBuf;
        }
        validate(initialCapacity, maxCapacity); // 校验容量的参数
        return newDirectBuffer(initialCapacity,maxCapacity);
    }

    private static void validate(int initialCapacity, int maxCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity: " + initialCapacity + " (expected: 0+)");
        }
        if (initialCapacity > maxCapacity) {
            throw new IllegalArgumentException(String.format(
                    "initialCapacity: %d (expected: not greater than maxCapacity(%d)",
                    initialCapacity, maxCapacity));
        }
    }

    /**
     * 使用给定的初始容量和maxCapacity创建堆ByteBuf。
     */
    protected abstract ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity);

    /**
     * 使用给定的初始容量和maxCapacity创建直接ByteBuf。
     */
    protected abstract ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity);
}
