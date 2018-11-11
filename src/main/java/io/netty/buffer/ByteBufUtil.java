package io.netty.buffer;

import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.Locale;

/**
 * 处理ByteBuf的具体工具类
 * */
public final class ByteBufUtil {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ByteBufUtil.class);

    static final ByteBufAllocator DEFAULT_ALLOCATOR;

    /**
     * 根据io.netty.allocator.type参数值来决定采用池化分配内存方式,还是非池化分配内存方式
     * 默认安卓机器采用非池化
     * 非安卓机器采用池化
     * 目前只支持非安卓机器
     * */



    static {
        //获取相应的分配策略
        String allocType = SystemPropertyUtil.get("io.netty.allocator.type","pooled");
        allocType = allocType.toLowerCase(Locale.US).trim();

        ByteBufAllocator alloc;

        if ("unpooled".equals(allocType)) {
            alloc = UnpooledByteBufAllocator.DEFAULT;
            logger.debug("-Dio.netty.allocator.type: {}", allocType);
        } else if ("pooled".equals(allocType)) {
            alloc = PooledByteBufAllocator.DEFAULT;
            logger.debug("-Dio.netty.allocator.type: {}", allocType);
        } else {
            alloc = PooledByteBufAllocator.DEFAULT;
            logger.debug("-Dio.netty.allocator.type: pooled (unknown: {})", allocType);
        }

    }

}
