package io.netty.util.internal.logging;

import io.netty.util.internal.logging.slf4.Slf4JLoggerFactory;

public abstract class InternalLoggerFactory {

    private static volatile InternalLoggerFactory defaultFactory;

    @SuppressWarnings("UnusedCatchParameter")
    private static InternalLoggerFactory newDefaultFactory(String name) {
        InternalLoggerFactory f;
        try {
            f = new Slf4JLoggerFactory(true);
            f.newInstance(name).debug("Using SLF4J as the default logging framework");
        } catch (Throwable ignore1) {
          throw new RuntimeException("创建Slf4JLoggerFactory失败");
        }
        return f;
    }

    /**
     * Returns the default factory.  The initial default factory is
     */
    public static InternalLoggerFactory getDefaultFactory() {
        if (defaultFactory == null) {
            defaultFactory = newDefaultFactory(InternalLoggerFactory.class.getName());
        }
        return defaultFactory;
    }

    /**
     * Changes the default factory.
     */
    public static void setDefaultFactory(InternalLoggerFactory defaultFactory) {
        if (defaultFactory == null) {
            throw new NullPointerException("defaultFactory");
        }
        InternalLoggerFactory.defaultFactory = defaultFactory;
    }

    /**
     * Creates a new logger instance with the name of the specified class.
     */
    public static InternalLogger getInstance(Class<?> clazz) {
        return getInstance(clazz.getName());
    }

    /**
     * Creates a new logger instance with the specified name.
     */
    public static InternalLogger getInstance(String name) {
        return getDefaultFactory().newInstance(name);
    }

    /**
     * Creates a new logger instance with the specified name.
     */
    protected abstract InternalLogger newInstance(String name);
}
