package io.netty.buffer;

import io.netty.util.internal.StringUtil;

import java.nio.ByteOrder;

public class EmptyByteBuf extends ByteBuf {


    private final ByteBufAllocator alloc;
    private final ByteOrder order;
    private final String str;

    public EmptyByteBuf(ByteBufAllocator alloc) {
        this(alloc, ByteOrder.BIG_ENDIAN);
    }

    private EmptyByteBuf(ByteBufAllocator alloc, ByteOrder order) {
        if (alloc == null) {
            throw new NullPointerException("alloc");
        }

        this.alloc = alloc;
        this.order = order;
        str = StringUtil.simpleClassName(this) + (order == ByteOrder.BIG_ENDIAN? "BE" : "LE");
    }

    @Override
    public int capacity() {
        return 0;
    }

    @Override
    public ByteBuf capacity(int newCapacity) {
        return null;
    }

    @Override
    public int maxCapacity() {
        return 0;
    }
}
