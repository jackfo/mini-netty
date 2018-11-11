package io.netty.util.internal.logging.slf4;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.LocationAwareSlf4JLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

public class Slf4JLoggerFactory extends InternalLoggerFactory {

    @SuppressWarnings("deprecation")
    public static final InternalLoggerFactory INSTANCE = new Slf4JLoggerFactory();


    public Slf4JLoggerFactory() {
    }

    public Slf4JLoggerFactory(boolean failIfNOP) {
        assert failIfNOP;
        if (LoggerFactory.getILoggerFactory() instanceof NOPLoggerFactory) {
            throw new NoClassDefFoundError("NOPLoggerFactory not supported");
        }
    }

    @Override
    public InternalLogger newInstance(String name) {
        return wrapLogger(LoggerFactory.getLogger(name));
    }

    // package-private for testing.
    static InternalLogger wrapLogger(Logger logger) {
        return logger instanceof LocationAwareLogger ?
                new LocationAwareSlf4JLogger((LocationAwareLogger) logger) : new Slf4JLogger(logger);
    }
}
