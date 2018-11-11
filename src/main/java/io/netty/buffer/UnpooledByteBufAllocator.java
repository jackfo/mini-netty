package io.netty.buffer;

import io.netty.util.internal.LongCounter;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;

public class UnpooledByteBufAllocator extends AbstractByteBufAllocator implements ByteBufAllocatorMetricProvider  {

    /**创建非池化度量器*/
    private final UnpooledByteBufAllocatorMetric metric = new UnpooledByteBufAllocatorMetric();

    private final boolean disableLeakDetector;

    private final boolean noCleaner;

    public static final UnpooledByteBufAllocator DEFAULT =
            new UnpooledByteBufAllocator(PlatformDependent.directBufferPreferred());



    private static final class UnpooledByteBufAllocatorMetric implements ByteBufAllocatorMetric{
        final LongCounter directCounter = PlatformDependent.newLongCounter();
        final LongCounter heapCounter = PlatformDependent.newLongCounter();
        @Override
        public long usedHeapMemory() {
            return heapCounter.value();
        }

        @Override
        public long usedDirectMemory() {
            return directCounter.value();
        }

        @Override
        public String toString() {
            return StringUtil.simpleClassName(this) +
                    "(usedHeapMemory: " + usedHeapMemory() + "; usedDirectMemory: " + usedDirectMemory() + ')';
        }
    }

}
