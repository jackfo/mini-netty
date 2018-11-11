package io.netty.buffer;

import java.util.Collections;
import java.util.Iterator;

import static java.lang.Math.max;

final class PoolChunkList<T> implements PoolChunkListMetric {

    private static final Iterator<PoolChunkMetric> EMPTY_METRICS = Collections.<PoolChunkMetric>emptyList().iterator();

    /**所属PoolArena对象*/
    private final PoolArena<T> arena;

    /**下一个PoolChunkList对象*/
    private final PoolChunkList<T> nextList;

    /**Chunk最小内存利用率*/
    private final int minUsage;

    /**Chunk最大内存利用率*/
    private final int maxUsage;

    /**每个Chunk最大可分配的容量*/
    private final int maxCapacity;

    private PoolChunk<T> head;

    /**前一个PoolChunkList对象*/
    private PoolChunkList<T> prevList;

    PoolChunkList(PoolArena<T> arena, PoolChunkList<T> nextList, int minUsage, int maxUsage, int chunkSize) {
        assert minUsage <= maxUsage;
        this.arena = arena;
        this.nextList = nextList;
        this.minUsage = minUsage;
        this.maxUsage = maxUsage;
        // 计算 maxUsage 属性
        maxCapacity = calculateMaxCapacity(minUsage, chunkSize);
    }

    /**保证最小值为1*/
    private static int minUsage0(int value) {
        return max(1, value);
    }


    /**
     * 计算当前poolChunk的最大容量
     * */
    private static int calculateMaxCapacity(int minUsage,int chunkSize){
        //计算最小minUsage值
        minUsage = minUsage0(minUsage);

        /**如果最小使用率*/
        if (minUsage == 100) {
            // If the minUsage is 100 we can not allocate anything out of this list.
            return 0;
        }
        return  (int) (chunkSize * (100L - minUsage) / 100L);
    }

    void prevList(PoolChunkList<T> prevList) {
        assert this.prevList == null;
        this.prevList = prevList;
    }


}
