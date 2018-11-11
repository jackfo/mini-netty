package io.netty.util.concurrent;


import io.netty.util.internal.ObjectUtil;

final class FastThreadLocalRunnable implements Runnable {

    private final Runnable runnable;

    private FastThreadLocalRunnable(Runnable runnable) {
        this.runnable = ObjectUtil.checkNotNull(runnable, "runnable");
    }

    /**
     * 当前线程运行完,需要清除其线程局部变量
     * */
    @Override
    public void run() {
        try {
            runnable.run();
        } finally {
            FastThreadLocal.removeAll();
        }
    }

    static Runnable wrap(Runnable runnable) {
        return runnable instanceof FastThreadLocalRunnable ? runnable : new FastThreadLocalRunnable(runnable);
    }
}
