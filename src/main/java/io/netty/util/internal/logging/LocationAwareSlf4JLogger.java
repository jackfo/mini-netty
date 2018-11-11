package io.netty.util.internal.logging;

import org.slf4j.spi.LocationAwareLogger;

import static org.slf4j.spi.LocationAwareLogger.*;
import static org.slf4j.spi.LocationAwareLogger.ERROR_INT;
import static org.slf4j.spi.LocationAwareLogger.WARN_INT;

public class LocationAwareSlf4JLogger extends AbstractInternalLogger {

    private static final String FQCN = LocationAwareSlf4JLogger.class.getName();
    private static final long serialVersionUID = -8292030083201538180L;

    private final transient LocationAwareLogger logger;

    public LocationAwareSlf4JLogger(LocationAwareLogger logger) {
        super(logger.getName());
        this.logger = logger;
    }

    private void log(final int level, final String message, final Object... params) {
        logger.log(null, FQCN, level, message, params, null);
    }

    private void log(final int level, final String message, Throwable throwable, final Object... params) {
        logger.log(null, FQCN, level, message, params, throwable);
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        if (isTraceEnabled()) {
            log(TRACE_INT, msg, null);
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (isTraceEnabled()) {
            log(TRACE_INT, format, arg);
        }
    }

    @Override
    public void trace(String format, Object argA, Object argB) {
        if (isTraceEnabled()) {
            log(TRACE_INT, format, argA, argB);
        }
    }

    @Override
    public void trace(String format, Object... argArray) {
        if (isTraceEnabled()) {
            log(TRACE_INT, format, argArray);
        }
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (isTraceEnabled()) {
            log(TRACE_INT, msg, t);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        if (isDebugEnabled()) {
            log(DEBUG_INT, msg);
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (isDebugEnabled()) {
            log(DEBUG_INT, format, arg);
        }
    }

    @Override
    public void debug(String format, Object argA, Object argB) {
        if (isDebugEnabled()) {
            log(DEBUG_INT, format, argA, argB);
        }
    }

    @Override
    public void debug(String format, Object... argArray) {
        if (isDebugEnabled()) {
            log(DEBUG_INT, format, argArray);
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (isDebugEnabled()) {
            log(DEBUG_INT, msg, t);
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        if (isInfoEnabled()) {
            log(INFO_INT, msg);
        }
    }

    @Override
    public void info(String format, Object arg) {
        if (isInfoEnabled()) {
            log(INFO_INT, format, arg);
        }
    }

    @Override
    public void info(String format, Object argA, Object argB) {
        if (isInfoEnabled()) {
            log(INFO_INT, format, argA, argB);
        }
    }

    @Override
    public void info(String format, Object... argArray) {
        if (isInfoEnabled()) {
            log(INFO_INT, format, argArray);
        }
    }

    @Override
    public void info(String msg, Throwable t) {
        if (isInfoEnabled()) {
            log(INFO_INT, msg, t);
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        if (isWarnEnabled()) {
            log(WARN_INT, msg);
        }
    }

    @Override
    public void warn(String format, Object arg) {
        if (isWarnEnabled()) {
            log(WARN_INT, format, arg);
        }
    }

    @Override
    public void warn(String format, Object... argArray) {
        if (isWarnEnabled()) {
            log(WARN_INT, format, argArray);
        }
    }

    @Override
    public void warn(String format, Object argA, Object argB) {
        if (isWarnEnabled()) {
            log(WARN_INT, format, argA, argB);
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (isWarnEnabled()) {
            log(WARN_INT, msg, t);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        if (isErrorEnabled()) {
            log(ERROR_INT, msg);
        }
    }

    @Override
    public void error(String format, Object arg) {
        if (isErrorEnabled()) {
            log(ERROR_INT, format, arg);
        }
    }

    @Override
    public void error(String format, Object argA, Object argB) {
        if (isErrorEnabled()) {
            log(ERROR_INT, format, argA, argB);
        }
    }

    @Override
    public void error(String format, Object... argArray) {
        if (isErrorEnabled()) {
            log(ERROR_INT, format, argArray);
        }
    }

    @Override
    public void error(String msg, Throwable t) {
        if (isErrorEnabled()) {
            log(ERROR_INT, msg, t);
        }
    }

}
