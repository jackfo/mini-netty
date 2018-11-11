package io.netty.buffer;


/**
 *      ByteBuf是netty提供的基于字节的数据容器，与java nio的ByteBuffer类似，都是字节缓冲区。
 *
 *      ByteBuf提供了两个指针变量来支持顺序读写操作——读操作的readerIndex和写操作的writerIndex。
 *      下图显示了两个指针如何将缓冲区划分为三个区域:
 *      +-------------------+------------------+------------------+
 *      | discardable bytes |  readable bytes  |  writable bytes  |
 *      |                   |     (CONTENT)    |                  |
 *      +-------------------+------------------+------------------+
 *      |                   |                  |                  |
 *      0      <=      readerIndex   <=   writerIndex    <=    capacity
 *
 *      Readable bytes:
 *      这个段是实际数据存储的地方。
 *      任何名称以read或skip开头的操作都将获取或跳过当前readerIndex上的数据，并增加读取字节数。
 *      如果读操作的参数也是ByteBuf，并且没有指定目标索引，那么指定缓冲区的writerIndex将一起增加。
 *      如果没有足够的内容剩下，IndexOutOfBoundsException就会被抛出。新分配、包装或复制的缓冲区的readerIndex的默认值为0。
 *
 *      Writable bytes:
 *      这个段是一个未定义的空间，需要填充。
 *      任何以write开头的操作都将在当前writerIndex上写入数据，并增加写入字节的数量。
 *      如果写操作的参数也是ByteBuf，并且没有指定源索引，那么指定缓冲区的readerIndex将一起增加。
 *      如果没有足够的可写字节，就会引发IndexOutOfBoundsException。新分配的缓冲区的writerIndex的默认值是0。包装或复制缓冲区的writerIndex的默认值是缓冲区的容量。
 *
 *      Discardable bytes:
 *      此段包含已由读操作读取的字节。最初，这个段的大小是0，但是当执行读操作时，它的大小会增加到writerIndex。
 *      可以通过调用discardReadBytes()来回收未使用的区域来丢弃读字节
 */

public abstract class ByteBuf {

    /**返回这个缓冲区可以包含的字节数*/
    public abstract int capacity();

    /**
     * 调整该缓冲区的容量。
     * @param newCapacity 如果小于当前容量，此缓冲区的内容被截断。如果更大与当前容量相比，缓冲区附加了长度为的未指定数据。
     * */
    public abstract ByteBuf capacity(int newCapacity);

    /***/
    public abstract int maxCapacity();




}
