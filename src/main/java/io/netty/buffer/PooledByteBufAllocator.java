package io.netty.buffer;

import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.FastThreadLocalThread;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PooledByteBufAllocator  extends AbstractByteBufAllocator implements ByteBufAllocatorMetricProvider {


    private static final InternalLogger logger = InternalLoggerFactory.getInstance(PooledByteBufAllocator.class);
    private static final int DEFAULT_NUM_HEAP_ARENA;
    private static final int DEFAULT_NUM_DIRECT_ARENA;

    private static final int DEFAULT_PAGE_SIZE;

    /**满二叉树的高度*/
    private static final int DEFAULT_MAX_ORDER; // 8192 << 11 = 16 MiB per chunk
    private static final int DEFAULT_TINY_CACHE_SIZE;
    private static final int DEFAULT_SMALL_CACHE_SIZE;
    private static final int DEFAULT_NORMAL_CACHE_SIZE;
    private static final int DEFAULT_MAX_CACHED_BUFFER_CAPACITY;
    private static final int DEFAULT_CACHE_TRIM_INTERVAL;
    private static final boolean DEFAULT_USE_CACHE_FOR_ALL_THREADS;
    private static final int DEFAULT_DIRECT_MEMORY_CACHE_ALIGNMENT;

    private static final int MIN_PAGE_SIZE = 4096;
    private static final int MAX_CHUNK_SIZE = (int) (((long) Integer.MAX_VALUE + 1) / 2);


    /**线程局部变量缓存*/
    private final PoolThreadLocalCache threadCache;

    static{
        int defaultPageSize = SystemPropertyUtil.getInt("io.netty.allocator.pageSize",8192);
        Throwable pageSizeFallbackCause = null;
        try{
            validateAndCalculatePageShifts(defaultPageSize);
        }catch (Throwable t){
            pageSizeFallbackCause = t;
            defaultPageSize=8192;
        }
        DEFAULT_PAGE_SIZE = defaultPageSize;

        //初始化默认最大排序
        int defaultMaxOrder = SystemPropertyUtil.getInt("io.netty.allocator.maxOrder", 11);
        Throwable maxOrderFallbackCause = null;
        try{
            validateAndCalculateChunkSize(DEFAULT_PAGE_SIZE, defaultMaxOrder);
        }catch (Throwable t){
            maxOrderFallbackCause = t;
            defaultMaxOrder = 11;
        }

        DEFAULT_MAX_ORDER = defaultMaxOrder;

        final Runtime runtime = Runtime.getRuntime();

        /**一个 EventLoop 一个 Arena ，避免多线程竞争。*/
        final int defaultMinNumArena = NettyRuntime.availableProcessors()*2;

        /**初始化默认Chunk的大小*/
        final int defaultChunkSize = DEFAULT_PAGE_SIZE << DEFAULT_MAX_ORDER;

        // 初始化 DEFAULT_NUM_HEAP_ARENA
        DEFAULT_NUM_HEAP_ARENA = Math.max(0,
                SystemPropertyUtil.getInt(
                        "io.netty.allocator.numHeapArenas",
                        (int) Math.min(
                                defaultMinNumArena,
                                runtime.maxMemory() / defaultChunkSize / 2 / 3)));

        DEFAULT_NUM_DIRECT_ARENA = Math.max(0,
                SystemPropertyUtil.getInt(
                        "io.netty.allocator.numDirectArenas",
                        (int) Math.min(
                                defaultMinNumArena,
                                PlatformDependent.maxDirectMemory() / defaultChunkSize / 2 / 3)));


        // 初始化 DEFAULT_TINY_CACHE_SIZE
        DEFAULT_TINY_CACHE_SIZE = SystemPropertyUtil.getInt("io.netty.allocator.tinyCacheSize", 512);
        // 初始化 DEFAULT_SMALL_CACHE_SIZE
        DEFAULT_SMALL_CACHE_SIZE = SystemPropertyUtil.getInt("io.netty.allocator.smallCacheSize", 256);
        // 初始化 DEFAULT_NORMAL_CACHE_SIZE
        DEFAULT_NORMAL_CACHE_SIZE = SystemPropertyUtil.getInt("io.netty.allocator.normalCacheSize", 64);


        DEFAULT_MAX_CACHED_BUFFER_CAPACITY = SystemPropertyUtil.getInt("io.netty.allocator.maxCachedBufferCapacity", 32 * 1024);

        // 初始化 DEFAULT_CACHE_TRIM_INTERVAL
        // the number of threshold of allocations when cached entries will be freed up if not frequently used
        DEFAULT_CACHE_TRIM_INTERVAL = SystemPropertyUtil.getInt("io.netty.allocator.cacheTrimInterval", 8192);

        // 初始化 DEFAULT_USE_CACHE_FOR_ALL_THREADS
        DEFAULT_USE_CACHE_FOR_ALL_THREADS = SystemPropertyUtil.getBoolean("io.netty.allocator.useCacheForAllThreads", true);

        // 初始化 DEFAULT_DIRECT_MEMORY_CACHE_ALIGNMENT
        DEFAULT_DIRECT_MEMORY_CACHE_ALIGNMENT = SystemPropertyUtil.getInt("io.netty.allocator.directMemoryCacheAlignment", 0);

    }


    public PooledByteBufAllocator() {
        this(false);
    }

    @SuppressWarnings("deprecation")
    public PooledByteBufAllocator(boolean preferDirect) {
        this(preferDirect, DEFAULT_NUM_HEAP_ARENA, DEFAULT_NUM_DIRECT_ARENA, DEFAULT_PAGE_SIZE, DEFAULT_MAX_ORDER);
    }

    @SuppressWarnings("deprecation")
    public PooledByteBufAllocator(int nHeapArena, int nDirectArena, int pageSize, int maxOrder) {
        this(false, nHeapArena, nDirectArena, pageSize, maxOrder);
    }

    @Deprecated
    public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder) {
        this(preferDirect, nHeapArena, nDirectArena, pageSize, maxOrder,
                DEFAULT_TINY_CACHE_SIZE, DEFAULT_SMALL_CACHE_SIZE, DEFAULT_NORMAL_CACHE_SIZE);
    }

    @Deprecated
    public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder,
                                  int tinyCacheSize, int smallCacheSize, int normalCacheSize) {
        this(preferDirect, nHeapArena, nDirectArena, pageSize, maxOrder, tinyCacheSize, smallCacheSize,
                normalCacheSize, DEFAULT_USE_CACHE_FOR_ALL_THREADS, DEFAULT_DIRECT_MEMORY_CACHE_ALIGNMENT);
    }

    public PooledByteBufAllocator(boolean preferDirect, int nHeapArena,
                                  int nDirectArena, int pageSize, int maxOrder, int tinyCacheSize,
                                  int smallCacheSize, int normalCacheSize,
                                  boolean useCacheForAllThreads) {
        this(preferDirect, nHeapArena, nDirectArena, pageSize, maxOrder,
                tinyCacheSize, smallCacheSize, normalCacheSize,
                useCacheForAllThreads, DEFAULT_DIRECT_MEMORY_CACHE_ALIGNMENT);
    }

    private final int chunkSize;

    private final List<PoolArenaMetric> heapArenaMetrics;

    private final List<PoolArenaMetric> directArenaMetrics;

    /**创建内存分配的度量*/
    private final PooledByteBufAllocatorMetric metric;

    /**
     *@param preferDirect 表示是否倾向于使用堆外内存
     *@param nHeapArena   堆的arena的数目
     *@param nDirectArena 堆外arens的数目
     *@param pageSize
     *@param maxOrder
     *@param tinyCacheSize
     *@param smallCacheSize
     *@param normalCacheSize
     *@param useCacheForAllThreads
     *@param directMemoryCacheAlignment
     * */
    public PooledByteBufAllocator(boolean preferDirect, int nHeapArena, int nDirectArena, int pageSize, int maxOrder,
                                  int tinyCacheSize, int smallCacheSize, int normalCacheSize,
                                  boolean useCacheForAllThreads, int directMemoryCacheAlignment) {
        //主要设置是否支持堆外内存
        super(preferDirect);
        threadCache = new PoolThreadLocalCache(useCacheForAllThreads);
        this.tinyCacheSize = tinyCacheSize;
        this.smallCacheSize = smallCacheSize;
        this.normalCacheSize = normalCacheSize;

        //计算chunk的大小 如 pageSize是8KB=8192B maxOrder为11  8KB<11=16MB
        chunkSize = validateAndCalculateChunkSize(pageSize, maxOrder);

        if (nHeapArena < 0) {
            throw new IllegalArgumentException("nHeapArena: " + nHeapArena + " (expected: >= 0)");
        }
        if (nDirectArena < 0) {
            throw new IllegalArgumentException("nDirectArea: " + nDirectArena + " (expected: >= 0)");
        }

        if (directMemoryCacheAlignment < 0) {
            throw new IllegalArgumentException("directMemoryCacheAlignment: "
                    + directMemoryCacheAlignment + " (expected: >= 0)");
        }
        if (directMemoryCacheAlignment > 0 && !isDirectMemoryCacheAlignmentSupported()) {
            throw new IllegalArgumentException("directMemoryCacheAlignment is not supported");
        }

        if ((directMemoryCacheAlignment & -directMemoryCacheAlignment) != directMemoryCacheAlignment) {
            throw new IllegalArgumentException("directMemoryCacheAlignment: "
                    + directMemoryCacheAlignment + " (expected: power of two)");
        }

        //根据pageSize计算位  8192==》2的13次 所以结果是13
        int pageShifts = validateAndCalculatePageShifts(pageSize);

        if(nDirectArena>0){
            //创建相应的堆arena块数
            heapArenas =newArenaArray(nHeapArena);
            //创建PoolArenaMetric相应的度量
            List<PoolArenaMetric> metrics = new ArrayList<PoolArenaMetric>(heapArenas.length);

            for (int i = 0; i < heapArenas.length; i ++) {
                // 创建 HeapArena 对象
                PoolArena.HeapArena arena = new PoolArena.HeapArena(this,
                        pageSize, maxOrder, pageShifts, chunkSize,
                        directMemoryCacheAlignment);
                heapArenas[i] = arena;
                metrics.add(arena);

            }
            heapArenaMetrics = Collections.unmodifiableList(metrics);
        }else{
            heapArenas = null;
            heapArenaMetrics = Collections.emptyList();
        }

        if (nDirectArena > 0) {
            directArenas = newArenaArray(nDirectArena);
            List<PoolArenaMetric> metrics = new ArrayList<PoolArenaMetric>(directArenas.length);
            for (int i = 0; i < directArenas.length; i ++) {
                PoolArena.DirectArena arena = new PoolArena.DirectArena(
                        this, pageSize, maxOrder, pageShifts, chunkSize, directMemoryCacheAlignment);
                directArenas[i] = arena;
                metrics.add(arena);
            }
            directArenaMetrics = Collections.unmodifiableList(metrics);
        } else {
            directArenas = null;
            directArenaMetrics = Collections.emptyList();
        }

        // 创建 PooledByteBufAllocatorMetric
        metric = new PooledByteBufAllocatorMetric(this);
    }

    @SuppressWarnings("unchecked")
    private static <T> PoolArena<T>[] newArenaArray(int size) {
        return new PoolArena[size];
    }

    public static final PooledByteBufAllocator DEFAULT = new PooledByteBufAllocator(PlatformDependent.directBufferPreferred());

    /**堆内内存划分*/
    private final PoolArena<byte[]>[] heapArenas;

    /**堆外内存划分*/
    private final PoolArena<ByteBuffer>[] directArenas;

    /**tiny内存块缓存的大小*/
    private final int tinyCacheSize;

    /**
     * {@link PoolThreadCache} 的 small 内存块缓存数组的大小
     */
    private final int smallCacheSize;
    /**
     * {@link PoolThreadCache} 的 normal 内存块缓存数组的大小
     */
    private final int normalCacheSize;

    public static boolean isDirectMemoryCacheAlignmentSupported() {
        return PlatformDependent.hasUnsafe();
    }

    private static int validateAndCalculatePageShifts(int pageSize) {
        //校验pageSize不能小于最小的页
        if (pageSize < MIN_PAGE_SIZE) {
            throw new IllegalArgumentException("pageSize: " + pageSize + " (expected: " + MIN_PAGE_SIZE + ")");
        }

        // 校验 Page 的内存大小，必须是 2 的指数级
        if ((pageSize & pageSize - 1) != 0) {
            throw new IllegalArgumentException("pageSize: " + pageSize + " (expected: power of 2)");
        }

        //Integer.numberOfLeadingZeros == 32-pageSIze低位1的位数  如 9==》1001 =》4 ==》32-4=28
        return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(pageSize);
    }


    public static int validateAndCalculateChunkSize(int pageSize,int maxOrder){
        if(maxOrder>14){
            throw new IllegalArgumentException("maxOrder: " + maxOrder + " (expected: 0-14)");
        }

        //计算块大小
        int chunkSize = pageSize;
        //扩大chunkSize的大小
        for (int i = maxOrder; i > 0; i --) {
            if (chunkSize > MAX_CHUNK_SIZE / 2) {
                throw new IllegalArgumentException(String.format(
                        "pageSize (%d) << maxOrder (%d) must not exceed %d", pageSize, maxOrder, MAX_CHUNK_SIZE));
            }
            chunkSize <<= 1;
        }
        return chunkSize;
    }

    /**获取池化内存分配器的度量*/
    @Override
    public PooledByteBufAllocatorMetric metric() {
        return metric;
    }

    @Override
    protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity) {
        //获取相应的PoolThreadCache对象
        PoolThreadCache cache = threadCache.get();

        /**获取当前线程对应的heapArena*/
        PoolArena<byte[]> heapArena = cache.heapArena;

        final ByteBuf buf;
        if(heapArena!=null){
            //如果
            buf=heapArena.allocate(cache,initialCapacity,maxCapacity);
        }else{
            //TODO:为什么heapArena为空,则采用非池化的方式？
        }
    }

    /**
     * PoolThreadCache线程局部变量
     * */
    final class PoolThreadLocalCache extends FastThreadLocal<PoolThreadCache>{

        /**是否支持缓存*/
        private final boolean useCacheForAllThreads;

        PoolThreadLocalCache(boolean useCacheForAllThreads) {
            this.useCacheForAllThreads = useCacheForAllThreads;
        }

        @Override
        protected synchronized PoolThreadCache initialValue() {
            final PoolArena<byte[]> heapArena =leastUsedArena(heapArenas);
            final PoolArena<ByteBuffer> directArena = leastUsedArena(directArenas);

            //创建开启缓存的PoolThreadCache
            Thread current = Thread.currentThread();
            if (useCacheForAllThreads || current instanceof FastThreadLocalThread) {
                return new PoolThreadCache(
                        heapArena, directArena, tinyCacheSize, smallCacheSize, normalCacheSize,
                        DEFAULT_MAX_CACHED_BUFFER_CAPACITY, DEFAULT_CACHE_TRIM_INTERVAL);
            }

            // 创建不进行缓存的 PoolThreadCache 对象
            return new PoolThreadCache(heapArena, directArena, 0, 0, 0, 0, 0);
        }

        @Override
        protected void onRemoval(PoolThreadCache threadCache) {
            // 释放缓存
            threadCache.free();
        }

        /**
         * 选择线程使用最少的PoolArena
         * */
        private <T> PoolArena<T> leastUsedArena(PoolArena<T>[] arenas){
            if (arenas == null || arenas.length == 0) {
                return null;
            }

            /**获取第一个PoolArena*/
            PoolArena<T> minArena = arenas[0];

            for (int i = 1; i < arenas.length; i++) {
                PoolArena<T> arena = arenas[i];
                if (arena.numThreadCaches.get() < minArena.numThreadCaches.get()) {
                    minArena = arena;
                }
            }
            return minArena;
        }

    }

}
