package ai.foresignal.source.adapters.ais;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The receiving statistics of one AIS source (prompt 085), for its health details: messages in the last minute,
 * distinct MMSIs in the last 10 minutes and the time of the last message. Time comes from the caller (the receive time
 * of each message). Thread-safe and bounded: at most {@link #MAX_TRACKED} MMSIs and {@link #MAX_TRACKED} message times
 * are kept.
 */
final class AisStationStats {

    /** The window of the message rate. */
    static final Duration RATE_WINDOW = Duration.ofMinutes(1);

    /** The window of the distinct-MMSI count. */
    static final Duration DISTINCT_WINDOW = Duration.ofMinutes(10);

    /** The most MMSIs and message times held. */
    static final int MAX_TRACKED = 200_000;

    private final Deque<Instant> recent = new ArrayDeque<>();
    private final LinkedHashMap<String, Instant> lastByMmsi = new LinkedHashMap<>(1024, 0.75f, true);
    private Instant lastMessageAt;

    /** Records one vessel message from {@code mmsi} received at {@code at}. */
    synchronized void message(String mmsi, Instant at) {
        if (lastMessageAt == null || at.isAfter(lastMessageAt)) {
            lastMessageAt = at;
        }
        recent.addLast(at);
        if (recent.size() > MAX_TRACKED) {
            recent.pollFirst();
        }
        lastByMmsi.put(mmsi, at);
        if (lastByMmsi.size() > MAX_TRACKED) {
            Iterator<String> eldest = lastByMmsi.keySet().iterator();
            eldest.next();
            eldest.remove();
        }
        prune(at);
    }

    /** The messages received within the minute before {@code now}. */
    synchronized int messagesPerMinute(Instant now) {
        prune(now);
        Instant cutoff = now.minus(RATE_WINDOW);
        int n = 0;
        for (Instant t : recent) {
            if (t.isAfter(cutoff)) {
                n++;
            }
        }
        return n;
    }

    /** The distinct MMSIs heard within the 10 minutes before {@code now}. */
    synchronized int distinctMmsi(Instant now) {
        prune(now);
        return lastByMmsi.size();
    }

    /** When the last message arrived, or null. */
    synchronized Instant lastMessageAt() {
        return lastMessageAt;
    }

    /** The health details: {@code messagesPerMin}, {@code distinctMmsi10m} and {@code lastMessageAt}. */
    synchronized Map<String, Object> details(Instant now) {
        Map<String, Object> out = new HashMap<>();
        out.put("messagesPerMin", messagesPerMinute(now));
        out.put("distinctMmsi10m", distinctMmsi(now));
        if (lastMessageAt != null) {
            out.put("lastMessageAt", lastMessageAt.toString());
        }
        return out;
    }

    private void prune(Instant now) {
        Instant rateCutoff = now.minus(RATE_WINDOW);
        while (!recent.isEmpty() && !recent.peekFirst().isAfter(rateCutoff)) {
            recent.pollFirst();
        }
        Instant distinctCutoff = now.minus(DISTINCT_WINDOW);
        Iterator<Map.Entry<String, Instant>> it = lastByMmsi.entrySet().iterator();
        while (it.hasNext()) {
            if (!it.next().getValue().isAfter(distinctCutoff)) {
                it.remove();
            } else {
                break;
            }
        }
    }
}
