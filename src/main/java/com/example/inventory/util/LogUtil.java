package com.example.inventory.util;

import org.slf4j.Logger;

/**
 * Global logging helper to standardize how we log exceptions with stack traces.
 */
public final class LogUtil {

    private LogUtil() {}

    /**
     * Logs an error with full stack trace using the provided logger.
     *
     * @param log the SLF4J logger (usually from @Slf4j)
     * @param message contextual message to include
     * @param t the throwable to log
     */
    public static void logException(Logger log, String message, Throwable t) {
        if (log == null) {
            return; // nothing we can do
        }
        if (message == null || message.isBlank()) {
            log.error("Unexpected error", t);
        } else {
            log.error(message, t);
        }
    }

    /**
     * Convenience overload when no message is provided.
     */
    public static void logException(Logger log, Throwable t) {
        logException(log, null, t);
    }
}
