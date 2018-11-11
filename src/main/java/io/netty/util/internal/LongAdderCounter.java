package io.netty.util.internal;

import java.util.concurrent.atomic.LongAdder;

public class LongAdderCounter extends LongAdder implements LongCounter{
    @Override
    public long value() {
        return longValue();
    }
}
