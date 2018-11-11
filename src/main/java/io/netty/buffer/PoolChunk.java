package io.netty.buffer;

public class PoolChunk<T> implements PoolChunkMetric {

    private static final int INTEGER_SIZE_MINUS_ONE = Integer.SIZE - 1;

    /**
     * 所属Arena对象
     */
    final PoolArena<T> arena;

    /**
     * 内存空间
     */
    final T memory;

    /**
     * 是否非池化
     */
    final boolean unpooled;

    final int offset;

    /**
     * 分配信息满二叉树
     *
     * index 为节点编号
     */
    private final byte[] memoryMap;
    /**
     * 高度信息满二叉树
     *
     * index 为节点编号
     */
    private final byte[] depthMap;
    /**
     * PoolSubpage 数组
     */
    private final PoolSubpage<T>[] subpages;
    /**
     * 判断分配请求内存是否为 Tiny/Small ，即分配 Subpage 内存块。
     *
     * Used to determine if the requested capacity is equal to or greater than pageSize.
     */
    private final int subpageOverflowMask;
    /**
     * Page 大小，默认 8KB = 8192B
     */
    private final int pageSize;
    /**
     * 从 1 开始左移到 {@link #pageSize} 的位数。默认 13 ，1 << 13 = 8192 。
     *
     * 具体用途，见 {@link #allocateRun(int)} 方法，计算指定容量所在满二叉树的层级。
     */
    private final int pageShifts;
    /**
     * 满二叉树的高度。默认为 11 。
     */
    private final int maxOrder;
    /**
     * Chunk 内存块占用大小。默认为 16M = 16 * 1024  。
     */
    private final int chunkSize;
    /**
     * log2 {@link #chunkSize} 的结果。默认为 log2( 16M ) = 24 。
     */
    private final int log2ChunkSize;
    /**
     * 可分配 {@link #subpages} 的数量，即数组大小。默认为 1 << maxOrder = 1 << 11 = 2048 。
     */
    private final int maxSubpageAllocs;
    /**
     * 标记节点不可用。默认为 maxOrder + 1 = 12 。
     *
     * Used to mark memory as unusable
     */
    private final byte unusable;

    /**
     * 剩余可用字节数
     */
    private int freeBytes;

    /**
     * 所属 PoolChunkList 对象
     */
    PoolChunkList<T> parent;
    /**
     * 上一个 Chunk 对象
     */
    PoolChunk<T> prev;
    /**
     * 下一个 Chunk 对象
     */
    PoolChunk<T> next;



    PoolChunk(PoolArena<T> arena, T memory, int pageSize, int maxOrder, int pageShifts, int chunkSize, int offset) {
        // 池化
        unpooled = false;
        this.arena = arena;
        this.memory = memory;
        this.pageSize = pageSize;
        this.pageShifts = pageShifts;
        this.maxOrder = maxOrder;
        this.chunkSize = chunkSize;
        this.offset = offset;
        unusable = (byte) (maxOrder + 1);
        log2ChunkSize = log2(chunkSize);
        subpageOverflowMask = ~(pageSize - 1);
        freeBytes = chunkSize;

        assert maxOrder < 30 : "maxOrder should be < 30, but is: " + maxOrder;
        maxSubpageAllocs = 1 << maxOrder;

        // 初始化 memoryMap 和 depthMap
        // Generate the memory map.
        memoryMap = new byte[maxSubpageAllocs << 1];
        depthMap = new byte[memoryMap.length];
        int memoryMapIndex = 1;
        for (int d = 0; d <= maxOrder; ++ d) { // move down the tree one level at a time
            int depth = 1 << d;
            for (int p = 0; p < depth; ++ p) {
                // in each level traverse left to right and set value to the depth of subtree
                memoryMap[memoryMapIndex] = (byte) d;
                depthMap[memoryMapIndex] = (byte) d;
                memoryMapIndex ++;
            }
        }

        // 初始化 subpages
        subpages = newSubpageArray(maxSubpageAllocs);
    }

    /** Creates a special chunk that is not pooled. */
    PoolChunk(PoolArena<T> arena, T memory, int size, int offset) {
        // 非池化
        unpooled = true;
        this.arena = arena;
        this.memory = memory;
        this.offset = offset;
        memoryMap = null;
        depthMap = null;
        subpages = null;
        subpageOverflowMask = 0;
        pageSize = 0;
        pageShifts = 0;
        maxOrder = 0;
        unusable = (byte) (maxOrder + 1);
        chunkSize = size;
        log2ChunkSize = log2(chunkSize);
        maxSubpageAllocs = 0;
    }


    private PoolSubpage<T>[] newSubpageArray(int size) {
        return new PoolSubpage[size];
    }

    @Override
    public int usage(){
        final int freeBytes;
        synchronized (arena) {
            freeBytes = this.freeBytes;
        }
        return usage(freeBytes);
    }

    private int usage(int freeBytes){
        if (freeBytes==0){
            return 100;
        }
        /**已经使用的chunkSize大小,按照百分比*/
        int freePercentage = (int) (freeBytes * 100L / chunkSize);

        if(freePercentage==0){
            return 99;
        }
        return 100-freePercentage;
    }

    private byte value(int id){
        return memoryMap[id];
    }

    long allocate(int normCapacity){
        if((normCapacity&subpageOverflowMask)!=0){
            return
        }
    }

    public int allocateNode(int d){
        int id = 1;
        //initial表示由32-d个1 + d个0组成的二进制
        int initial = - (1 << d);

        //获取根节点的值
        byte val = value(id);
        if(val>d){
            return -1;
        }

        //只有达到对应深度 且val>d才会跳出
        while (val<d||(id&initial)==0){/**如果在深度d一下的id循环一直为真*/
            id <<=1;
            val=value(id);
            if(val>d){
                //表示左节点没有符合的节点 检测右节点
                id ^= 1;
                //获取右节点
                val = value(id);
            }
        }

        byte value = value(id);
        assert value == d && (id & initial) == 1 << d : String.format("val = %d, id & initial = %d, d = %d",
                value, id & initial, d);

        setValue(id, unusable);
        updateParentsAlloc(id);
        return id;

    }

    private void updateParentsAlloc(int id) {
        while (id > 1) {
            // 获得父节点的编号
            int parentId = id >>> 1;
            // 获得子节点的值
            byte val1 = value(id);
            // 获得另外一个子节点的
            byte val2 = value(id ^ 1);
            // 获得子节点较小值，并设置到父节点
            byte val = val1 < val2 ? val1 : val2;
            setValue(parentId, val);

            // 跳到父节点
            id = parentId;
        }
    }

    private void setValue(int id, byte val) {
        memoryMap[id] = val;
    }

    /**
     * 需要容量大小
     * @param normCapacity 容量大小
     * normCapacity默认是8kb 放在第11层
     * 8kb结果是2的13次即1后面13个0  所以log2（8kb）-13
     * maxOrder 表示11即默认层级
     * 8KB大小数据快放在第十一层 故pageShifts就是用来消除13次的页大小偏移量
     * 从而d表示相应层
     * 举例:16kb==》14==>14-13==>1==>maxOrder-1==>11-1=10最终16KB大小在第十层满足验证
     * */
    private long allocateRun(int normCapacity){
        //11-
        int d = maxOrder-(log2(normCapacity) - pageShifts);
        int id =allocateNode(d);
        if(id<0){
            return id;
        }
        // 减少剩余可用字节数
        freeBytes -= runLength(id);
        return id;
    }

    /**
     * 记录比有效位小1的个数
     * 3 ==》11 最终结果为1
     * 4 ==> 100 最终结果为2
     * */
    private static int log2(int val) {
        return INTEGER_SIZE_MINUS_ONE - Integer.numberOfLeadingZeros(val);
    }


    private long allocateSubpage(int normCapacity){
        PoolSubpage<T> head = arena.
    }

    private int runLength(int id) {
        // represents the size in #bytes supported by node 'id' in the tree
        return 1 << log2ChunkSize - depth(id);
    }


    private byte depth(int id) {
        return depthMap[id];
    }



}