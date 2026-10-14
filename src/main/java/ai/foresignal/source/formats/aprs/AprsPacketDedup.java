package ai.foresignal.source.formats.aprs;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The packets heard first-hand on RF in the last {@value #WINDOW_SECONDS} seconds (prompt 082), so {@code aprs-is}
 * can drop its copy of a packet a {@code local-aprs-{n}} TNC already delivered: the local copy always wins, because
 * RF is first-hand.
 *
 * <ul>
 *   <li><strong>Key:</strong> SHA-256 of {@code source + ":" + info}, so the path (which differs between the RF copy
 *       and the igated copy) is not part of it.
 *   <li><strong>Bound:</strong> at most {@value #MAX_ENTRIES} keys, least recently registered evicted, so memory
 *       stays bounded however busy the frequency is.
 *   <li><strong>Time:</strong> callers pass the receive instants; the class reads no clock.
 * </ul>
 * Thread-safe: one shared instance serves every APRS source.
 */
@Component
public class AprsPacketDedup {

    /** The window in seconds. */
    public static final int WINDOW_SECONDS = 30;

    /** The window. */
    public static final Duration WINDOW = Duration.ofSeconds(WINDOW_SECONDS);

    /** The most keys held. */
    public static final int MAX_ENTRIES = 50_000;

    private final Map<String, Instant> seen = new LinkedHashMap<>(1024, 0.75f, true) {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Instant> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    /** Creates an empty filter. */
    public AprsPacketDedup() {}

    /**
     * Records a packet heard first-hand.
     *
     * @param source the sender's call with SSID
     * @param info the information field
     * @param at when it was received
     */
    public void register(String source, String info, Instant at) {
        if (source == null || info == null || at == null) {
            return;
        }
        String key = key(source, info);
        synchronized (seen) {
            seen.put(key, at);
        }
    }

    /**
     * True when the same packet was registered within {@link #WINDOW} before (or at) {@code at}.
     *
     * @param source the sender's call with SSID
     * @param info the information field
     * @param at when the second copy was received
     */
    public boolean seenRecently(String source, String info, Instant at) {
        if (source == null || info == null || at == null) {
            return false;
        }
        String key = key(source, info);
        Instant registered;
        synchronized (seen) {
            registered = seen.get(key);
        }
        if (registered == null) {
            return false;
        }
        Duration age = Duration.between(registered, at);
        return !age.isNegative() ? age.compareTo(WINDOW) <= 0 : age.abs().compareTo(WINDOW) <= 0;
    }

    /** The number of keys held. */
    public int size() {
        synchronized (seen) {
            return seen.size();
        }
    }

    /** The dedup key: the hex SHA-256 of {@code source + ":" + info}. */
    public static String key(String source, String info) {
        return AprsRecords.sha256(source + ":" + info);
    }
}
