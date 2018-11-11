package io.netty.buffer;

import io.netty.util.Recycler;
import io.netty.util.Recycler.Handle;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.nio.ByteBuffer;
import java.util.Map;


/**
 * 内存分配的线程缓存
 */
final class PoolThreadCache {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(PoolThreadCache.class);

    /**
     * 对应的 Heap PoolArena 对象
     */
    final PoolArena<byte[]> heapArena;
    /**
     * 对应的 Direct PoolArena 对象
     */
    final PoolArena<ByteBuffer> directArena;

    // Hold the caches for the different size classes, which are tiny, small and normal.
    /**
     * Heap 类型的 tiny Subpage 内存块缓存数组
     */
    private final MemoryRegionCache<byte[]>[] tinySubPageHeapCaches;
    /**
     * Heap 类型的 small Subpage 内存块缓存数组
     */
    private final MemoryRegionCache<byte[]>[] smallSubPageHeapCaches;
    /**
     * Heap 类型的 normal 内存块缓存数组
     */
    private final MemoryRegionCache<byte[]>[] normalHeapCaches;
    /**
     * Direct 类型的 tiny Subpage 内存块缓存数组
     */
    private final MemoryRegionCache<ByteBuffer>[] tinySubPageDirectCaches;
    /**
     * Direct 类型的 small Subpage 内存块缓存数组
     */
    private final MemoryRegionCache<ByteBuffer>[] smallSubPageDirectCaches;
    /**
     * Direct 类型的 normal 内存块缓存数组
     */
    private final MemoryRegionCache<ByteBuffer>[] normalDirectCaches;

    // Used for bitshifting when calculate the index of normal caches later
    /**
     * 用于计算请求分配的 normal 类型的内存块，在 {@link #normalDirectCaches} 数组中的位置
     *
     * 默认为 log2(pageSize) = log2(8192) = 13
     */
    private final int numShiftsNormalDirect;
    /**
     * 用于计算请求分配的 normal 类型的内存块，在 {@link #normalHeapCaches} 数组中的位置
     *
     * 默认为 log2(pageSize) = log2(8192) = 13
     */
    private final int numShiftsNormalHeap;

    /**
     * 分配次数
     */
    private int allocations;

    private final int freeSweepAllocationThreshold;

    PoolThreadCache(PoolArena<byte[]> heapArena, PoolArena<ByteBuffer> directArena,
                    int tinyCacheSize, int smallCacheSize, int normalCacheSize,
                    int maxCachedBufferCapacity, int freeSweepAllocationThreshold) {

        if (maxCachedBufferCapacity < 0) {
            throw new IllegalArgumentException("maxCachedBufferCapacity: "
                    + maxCachedBufferCapacity + " (expected: >= 0)");
        }
        this.freeSweepAllocationThreshold = freeSweepAllocationThreshold;
        this.heapArena = heapArena;
        this.directArena = directArena;



    }


    private static <T> MemoryRegionCache<T>[] createSubPageCaches(int cacheSize, int numCaches, PoolArena.SizeClass sizeClass){

        if(cacheSize>0&&numCaches>0){

        }
    }

    private static <T> MemoryRegionCache

    private abstract static class MemoryRegionCache<T>{
        /**队列大小*/
        private final int size;

        private final Queue<Entry<>>
    }

    /**
     * 采用代理模式,来对对象进行回收
     * */
    static final class Entry<T>{
        /**用于回收Entry对象*/
        final Handle<Entry<?>> recyclerHandle;

        PoolChunk<T> chunk;

        long handle = -1;

        Entry(Handle<Entry<?>> recyclerHandle){
            this.recyclerHandle = recyclerHandle;
        }

        void recycle(){
            chunk = null;
            handle = -1;
            recyclerHandle.recycle(this);
        }
    }


    @SuppressWarnings("rawtypes")
    private static Entry newEntry(PoolChunk<?> chunk, long handle) {
        // 从 Recycler 对象中，获得 Entry 对象
        Entry entry = RECYCLER.get();
        // 初始化属性
        entry.chunk = chunk;
        entry.handle = handle;
        return entry;
    }

    @SuppressWarnings("rawtypes")
    private static final Recycler<Entry> RECYCLER = new Recycler<Entry>() {

        @SuppressWarnings("unchecked")
        @Override
        protected Entry newObject(Handle<Entry> handle) {
            return new Entry(handle); // 创建 Entry 对象
        }

    };
}
