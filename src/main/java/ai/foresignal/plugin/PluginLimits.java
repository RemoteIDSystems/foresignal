package ai.foresignal.plugin;

import java.time.Duration;

/**
 * The sandbox limits of one plugin call (prompt 141, {@code docs/Plugin-Design.md} §4).
 *
 * @param maxMemoryPages the most 64 KiB pages a module's memory may reach; 1024 pages is 64 MB
 * @param timeout the wall-clock budget of one call, instantiation included
 * @param maxInputBytes the largest input handed to a plugin
 * @param maxOutputBytes the largest output (the length prefix's value) accepted from a plugin
 * @param maxRecords the most records one output may carry
 * @param maxLogMessagesPerCall the most {@code foresignal_log} messages kept from one call
 * @param maxLogMessagesPerMinute the most {@code foresignal_log} messages kept from one plugin per minute
 * @param maxLogMessageBytes the longest log message kept; longer ones are cut
 */
public record PluginLimits(int maxMemoryPages, Duration timeout, int maxInputBytes, int maxOutputBytes,
        int maxRecords, int maxLogMessagesPerCall, int maxLogMessagesPerMinute, int maxLogMessageBytes) {

    /** The size of one WebAssembly memory page. */
    public static final int PAGE_BYTES = 65_536;

    /** The limits of ABI v1: 64 MB, 5 s, 16 MB in and out, 50,000 records, 20 log lines a call and 60 a minute. */
    public static final PluginLimits DEFAULTS = new PluginLimits(1024, Duration.ofSeconds(5), 16 * 1024 * 1024,
            16 * 1024 * 1024, 50_000, 20, 60, 1024);

    /** Checks every limit is positive. */
    public PluginLimits {
        if (maxMemoryPages < 1 || maxMemoryPages > 65_536) {
            throw new IllegalArgumentException("maxMemoryPages must be 1 to 65536");
        }
        if (timeout == null || timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        if (maxInputBytes < 1 || maxOutputBytes < 1 || maxRecords < 1 || maxLogMessagesPerCall < 0
                || maxLogMessagesPerMinute < 0 || maxLogMessageBytes < 1) {
            throw new IllegalArgumentException("limits must be positive");
        }
    }

    /** These limits with another timeout (tests). */
    public PluginLimits withTimeout(Duration value) {
        return new PluginLimits(maxMemoryPages, value, maxInputBytes, maxOutputBytes, maxRecords,
                maxLogMessagesPerCall, maxLogMessagesPerMinute, maxLogMessageBytes);
    }

    /** The memory cap in bytes. */
    public long maxMemoryBytes() {
        return (long) maxMemoryPages * PAGE_BYTES;
    }
}
