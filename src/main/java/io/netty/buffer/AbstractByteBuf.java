package io.netty.buffer;

public abstract class AbstractByteBuf extends ByteBuf {

    /**
     * 读取位置
     */
    int readerIndex;
    /**
     * 写入位置
     */
    int writerIndex;

    /**
     * {@link #readerIndex} 的标记
     */
    private int markedReaderIndex;
    /**
     * {@link #writerIndex} 的标记
     */
    private int markedWriterIndex;

    /**最大容量*/
    private int maxCapacity;

    final void setIndex0(int readerIndex, int writerIndex) {
        this.readerIndex = readerIndex;
        this.writerIndex = writerIndex;
    }

    protected AbstractByteBuf(int maxCapacity) {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException("maxCapacity: " + maxCapacity + " (expected: >= 0)");
        }
        this.maxCapacity = maxCapacity;
    }


    final void discardMarks() {
        markedReaderIndex = markedWriterIndex = 0;
    }
}
