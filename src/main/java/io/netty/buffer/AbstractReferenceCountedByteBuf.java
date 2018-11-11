package io.netty.buffer;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public abstract class AbstractReferenceCountedByteBuf extends AbstractByteBuf  {

    protected AbstractReferenceCountedByteBuf(int maxCapacity) {
        // 设置最大容量
        super(maxCapacity);
        // 初始 refCnt 为 1
        refCntUpdater.set(this, 1);
    }


    /**最大容量*/
    private int maxCapacity;

    /**引用更新器*/
    private static final AtomicIntegerFieldUpdater<AbstractReferenceCountedByteBuf> refCntUpdater = AtomicIntegerFieldUpdater.newUpdater(AbstractReferenceCountedByteBuf.class, "refCnt");


    protected final void setRefCnt(int refCnt) {
        refCntUpdater.set(this, refCnt);
    }

    @Override
    public int maxCapacity() {
        return maxCapacity;
    }

    protected final void maxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

}
