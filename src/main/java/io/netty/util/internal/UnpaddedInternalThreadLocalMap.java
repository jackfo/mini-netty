package io.netty.util.internal;

import io.netty.util.concurrent.FastThreadLocal;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程局部变量数据结构
 * */
class UnpaddedInternalThreadLocalMap {


    static final ThreadLocal<InternalThreadLocalMap> slowThreadLocalMap = new ThreadLocal<InternalThreadLocalMap>();


    static final AtomicInteger nextIndex = new AtomicInteger();

    /** FastThreadLocal使用*/
    Object[] indexedVariables;

    // Core thread-locals
    int futureListenerStackDepth;
    int localChannelReaderStackDepth;
    Map<Class<?>, Boolean> handlerSharableCache;

    // String-related thread-locals
    StringBuilder stringBuilder;
    Map<Charset, CharsetEncoder> charsetEncoderCache;
    Map<Charset, CharsetDecoder> charsetDecoderCache;

    // ArrayList-related thread-locals
    ArrayList<Object> arrayList;

    UnpaddedInternalThreadLocalMap(Object[] indexedVariables) {
        this.indexedVariables = indexedVariables;
    }



}
