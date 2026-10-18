package ai.foresignal.plugin;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The single host import of ABI v1, {@code env.foresignal_log(level, ptr, len)} (prompt 141), with its rate limits.
 *
 * <p>Levels: 0 debug, 1 info, 2 warn, 3 or more error (a negative level is debug). A message is decoded as UTF-8,
 * cut to {@link PluginLimits#maxLogMessageBytes()}, and every control character (line breaks included) becomes a
 * space, so a plugin cannot forge log lines. At most {@link PluginLimits#maxLogMessagesPerCall()} messages are kept per
 * call and {@link PluginLimits#maxLogMessagesPerMinute()} per plugin per minute; the rest are counted and reported
 * once at the end of the call. The import never fails the call: an out-of-bounds pointer drops the message.
 */
public final class PluginLog {

    /** Receives the messages that pass the limits. */
    @FunctionalInterface
    public interface Sink {

        /**
         * Writes one message.
         *
         * @param level the normalized level, 0 (debug) to 3 (error)
         * @param message the sanitized message
         */
        void write(int level, String message);
    }

    private final String plugin;
    private final PluginLimits limits;
    private final Clock clock;
    private final Sink sink;

    private long windowMinute = Long.MIN_VALUE;
    private int windowCount;

    /**
     * Creates the log of one plugin.
     *
     * @param plugin the plugin name
     * @param limits the limits
     * @param clock the clock of the per-minute window
     * @param sink where messages go, or null for the logger {@code foresignal.plugin.<name>}
     */
    public PluginLog(String plugin, PluginLimits limits, Clock clock, Sink sink) {
        this.plugin = Objects.requireNonNull(plugin);
        this.limits = Objects.requireNonNull(limits);
        this.clock = clock == null ? Clock.systemUTC() : clock;
        this.sink = sink == null ? loggerSink(plugin) : sink;
    }

    /** A sink writing to the logger {@code foresignal.plugin.<name>}. */
    public static Sink loggerSink(String plugin) {
        Logger log = LoggerFactory.getLogger("foresignal.plugin." + plugin);
        return (level, message) -> {
            switch (level) {
                case 0 -> log.debug("{}", message);
                case 1 -> log.info("{}", message);
                case 2 -> log.warn("{}", message);
                default -> log.error("{}", message);
            }
        };
    }

    /** Starts the counters of one call. */
    public Call startCall() {
        return new Call();
    }

    /** The counters of one call. Not thread-safe; one call runs on one thread. */
    public final class Call {

        private int kept;
        private int dropped;

        private Call() {}

        /**
         * Handles one {@code foresignal_log} call.
         *
         * @param level the plugin's level
         * @param bytes the message bytes as read from memory, or null when the pointer was out of bounds
         */
        public void log(long level, byte[] bytes) {
            if (bytes == null) {
                dropped++;
                return;
            }
            if (kept >= limits.maxLogMessagesPerCall() || !takeMinuteSlot()) {
                dropped++;
                return;
            }
            kept++;
            sink.write(normalize(level), sanitize(bytes, limits.maxLogMessageBytes()));
        }

        /** Messages kept in this call. */
        public int kept() {
            return kept;
        }

        /** Messages dropped in this call (limits or bad pointers). */
        public int dropped() {
            return dropped;
        }

        /** Reports the dropped count once, at warn, when anything was dropped. */
        public void finish() {
            if (dropped > 0) {
                LoggerFactory.getLogger("foresignal.plugin." + plugin)
                        .warn("{} log message(s) from plugin {} were dropped by the rate limit", dropped, plugin);
            }
        }
    }

    private synchronized boolean takeMinuteSlot() {
        long minute = Instant.now(clock).getEpochSecond() / 60;
        if (minute != windowMinute) {
            windowMinute = minute;
            windowCount = 0;
        }
        if (windowCount >= limits.maxLogMessagesPerMinute()) {
            return false;
        }
        windowCount++;
        return true;
    }

    /** The level clamped to 0 to 3. */
    static int normalize(long level) {
        if (level <= 0) {
            return 0;
        }
        return level >= 3 ? 3 : (int) level;
    }

    /**
     * The message as UTF-8 text cut to {@code maxBytes} and with every control character replaced by a space.
     *
     * @param bytes the raw bytes
     * @param maxBytes the cap
     */
    static String sanitize(byte[] bytes, int maxBytes) {
        int n = Math.min(bytes.length, maxBytes);
        String text = new String(bytes, 0, n, StandardCharsets.UTF_8);
        StringBuilder out = new StringBuilder(text.length());
        text.codePoints().forEach(cp -> {
            if (Character.isISOControl(cp) || cp == 0x2028 || cp == 0x2029) {
                out.append(' ');
            } else {
                out.appendCodePoint(cp);
            }
        });
        return out.toString();
    }
}
