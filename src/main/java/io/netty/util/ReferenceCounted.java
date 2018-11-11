package io.netty.util;

/**
 * 引用计数对象
 * */
public interface ReferenceCounted {

    /**返回当前对象引用次数,如果是0意味着这个对象没有被引用*/
    int refCnt();

    /**增加当前对象被引用的次数*/
    ReferenceCounted retain();

    /**通过指定的增量增加引用计数*/
    ReferenceCounted retain(int increment);

    /**记录此对象当前访问位置*/
    ReferenceCounted touch();

    /**记录此对象的当前访问位置，以及用于调试的其他任意信息。*/
    ReferenceCounted touch(Object hint);

    /**将引用计数减少1并在引用计数达到0时释放该对象*/
    boolean release();

    /**通过指定的减量减少引用计数，并在引用计数达到0时释放该对象*/
    boolean release(int decrement);


}
