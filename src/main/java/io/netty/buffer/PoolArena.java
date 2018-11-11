package io.netty.buffer;

import io.netty.util.internal.PlatformDependent;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PoolArena<T> implements PoolArenaMetric {

     static final boolean HAS_UNSAFE = PlatformDependent.hasUnsafe();


     enum SizeClass{
         Tiny,
         Small,
         Normal
     }

     /**数组的大小*/
     static final int numTinySubpagePools = 512>>>4;

    /**PoolArena 被多少线程引用的计数器*/
    final AtomicInteger numThreadCaches = new AtomicInteger();


    final PooledByteBufAllocator parent;

    private final int maxOrder;

    /**Page大小,默认8KB=8192B*/
    final int pageSize;

    /**从1开始左移到pageSize的位数 默认13 1《《13 = 8192*/
    final int pageShifts;

    /**chunk内存块占用大小 默认16M = 16*1024*/
    final int chunkSize;

    /**判断分配请求内存是否为 Tiny/Small ，即分配 Subpage 内存块。*/
    final int subpageOverflowMask;

    /**
     * 数组的大小
     *
     * 默认为 4
     */
    final int numSmallSubpagePools;

    /***/
    private final PoolSubpage<T>[] smallSubpagePools;

    /**
     * 对齐基准
     */
    final int directMemoryCacheAlignment;
    /**
     * {@link #directMemoryCacheAlignment} 掩码
     */
    final int directMemoryCacheAlignmentMask;

    /**数组的每个元素都是双向链表*/
    private final PoolSubpage<T>[] tinySubpagePools;

    private final PoolChunkList<T> q050;
    private final PoolChunkList<T> q025;
    private final PoolChunkList<T> q000;
    private final PoolChunkList<T> qInit;
    private final PoolChunkList<T> q075;
    private final PoolChunkList<T> q100;

    protected PoolArena(PooledByteBufAllocator parent, int pageSize,
                        int maxOrder, int pageShifts, int chunkSize, int cacheAlignment) {
        this.parent = parent;
        this.pageSize = pageSize;
        this.maxOrder = maxOrder;
        this.pageShifts = pageShifts;
        this.chunkSize = chunkSize;
        directMemoryCacheAlignment = cacheAlignment;
        directMemoryCacheAlignmentMask = cacheAlignment - 1;
        subpageOverflowMask = ~(pageSize - 1);

        // 初始化 tinySubpagePools 数组
        tinySubpagePools = newSubpagePoolArray(numTinySubpagePools);
        for (int i = 0; i < tinySubpagePools.length; i ++) {
            tinySubpagePools[i] = newSubpagePoolHead(pageSize);
        }

        // 初始化 smallSubpagePools 数组
        numSmallSubpagePools = pageShifts - 9;
        smallSubpagePools = newSubpagePoolArray(numSmallSubpagePools);
        for (int i = 0; i < smallSubpagePools.length; i ++) {
            smallSubpagePools[i] = newSubpagePoolHead(pageSize);
        }

        // PoolChunkList 之间的双向链表，初始化

        q100 = new PoolChunkList<T>(this, null, 100, Integer.MAX_VALUE, chunkSize);
        q075 = new PoolChunkList<T>(this, q100, 75, 100, chunkSize);
        q050 = new PoolChunkList<T>(this, q075, 50, 100, chunkSize);
        q025 = new PoolChunkList<T>(this, q050, 25, 75, chunkSize);
        q000 = new PoolChunkList<T>(this, q025, 1, 50, chunkSize);
        qInit = new PoolChunkList<T>(this, q000, Integer.MIN_VALUE, 25, chunkSize);

        q100.prevList(q075);
        q075.prevList(q050);
        q050.prevList(q025);
        q025.prevList(q000);
        q000.prevList(null); // 无前置节点
        qInit.prevList(qInit); // 前置节点为自己

        // 创建 PoolChunkListMetric 数组
        List<PoolChunkListMetric> metrics = new ArrayList<PoolChunkListMetric>(6);
        metrics.add(qInit);
        metrics.add(q000);
        metrics.add(q025);
        metrics.add(q050);
        metrics.add(q075);
        metrics.add(q100);
        chunkListMetrics = Collections.unmodifiableList(metrics);
    }

    PooledByteBuf<T> allocate(PoolThreadCache cache, int reqCapacity, int maxCapacity) {

        //创建相应的PoolByteBuf
        PooledByteBuf<T> buf = newByteBuf(maxCapacity);


    }

    abstract boolean isDirect();

    protected abstract PooledByteBuf<T> newByteBuf(int maxCapacity);

    private void allocate(PoolThreadCache cache, PooledByteBuf<T> buf, final int reqCapacity){

    }

    private PoolSubpage<T> newSubpagePoolHead(int pageSize) {
        PoolSubpage<T> head = new PoolSubpage<T>(pageSize);
        head.prev = head;
        head.next = head;
        return head;
    }

    private PoolSubpage<T>[] newSubpagePoolArray(int size){
        return new PoolSubpage[size];
    }

    PoolSubpage<T> findSubpagePoolHead(int elemSize){
        int tableIdx;
        PoolSubpage<T>[] table;

        if(isTiny(elemSize)){
            tableIdx = elemSize>>>4;
            table = tinySubpagePools;
        }else{
            tableIdx=0;
            elemSize>>>=10;
            while (elemSize!=0){
                elemSize>>>=1;
                tableIdx++;
            }
            table = smallSubpagePools;
        }
        return table[tableIdx];
    }

    static boolean isTiny(int normCapacity) {
        return (normCapacity & 0xFFFFFE00) == 0;
    }


    static final class HeapArena extends PoolArena<byte[]> { // 管理 byte[] 数组

        HeapArena(PooledByteBufAllocator parent, int pageSize, int maxOrder, int pageShifts, int chunkSize, int directMemoryCacheAlignment) {
            super(parent, pageSize, maxOrder, pageShifts, chunkSize, directMemoryCacheAlignment);
        }

        // 创建 byte[] 数组,即内存大小
        private static byte[] newByteArray(int size) {
            return PlatformDependent.allocateUninitializedArray(size);
        }

        @Override
        boolean isDirect() {
            return false;
        }

        @Override
        protected PooledByteBuf<byte[]> newByteBuf(int maxCapacity) {
            return HAS_UNSAFE ? PooledUnsafeHeapByteBuf.newUnsafeInstance(maxCapacity)
                    : PooledHeapByteBuf.newInstance(maxCapacity);
        }

    }

    static final class DirectArena extends PoolArena<ByteBuffer> { // 管理 Direct ByteBuffer 对象

        DirectArena(PooledByteBufAllocator parent, int pageSize, int maxOrder,
                    int pageShifts, int chunkSize, int directMemoryCacheAlignment) {
            super(parent, pageSize, maxOrder, pageShifts, chunkSize, directMemoryCacheAlignment);
        }

        @Override
        protected PooledByteBuf<ByteBuffer> newByteBuf(int maxCapacity) {
            if (HAS_UNSAFE) {
                return PooledUnsafeDirectByteBuf.newInstance(maxCapacity);
            } else {
                return PooledDirectByteBuf.newInstance(maxCapacity);
            }
        }

    }


}
