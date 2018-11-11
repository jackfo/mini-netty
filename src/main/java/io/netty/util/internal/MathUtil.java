package io.netty.util.internal;

public class MathUtil {

    private MathUtil() {
    }

    /**快速找到下一个 大于当前数切是2的幂次*/
    public static int findNextPositivePowerOfTwo(final int value) {

        assert value > Integer.MIN_VALUE && value < 0x40000000;
        return 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
    }
}
