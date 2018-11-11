package io.netty.util.internal;

import java.nio.ByteBuffer;

interface Cleaner {
    void freeDirectBuffer(ByteBuffer buffer);
}
