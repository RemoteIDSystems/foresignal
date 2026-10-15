package ai.foresignal.analyst;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The deterministic change detector (prompt 102): from the profile cursor, decides which in-scope
 * inputs are new or updated, excludes analyst-origin events it produced itself (the loop guard, unless
 * a deterministic detector updated them since), ranks by severity then confidence then recency, and
 * caps the packet.
 */
public final class AnalystChangeDetector {

    /** The most items one packet carries. */
    public static final int CAP = 30;

    private AnalystChangeDetector() {}

    /**
     * One candidate input.
     *
     * @param kind event | anomaly | alert
     * @param id the input id
     * @param title a short title
     * @param severity info | warning | critical, or null
     * @param confidence the confidence, or null
     * @param at when it was observed or updated
     * @param origin recipe | analyst | operator | null
     * @param summary the compact summary
     */
    public record Input(String kind, String id, String title, String severity, Double confidence, Instant at,
            String origin, Map<String, Object> summary) {}

    /** The packet of new inputs. */
    public record Packet(List<Input> items, Map<String, Integer> counts, AnalystCursor nextCursor) {}

    /**
     * Detects the packet.
     *
     * @param cursor the profile's cursor
     * @param candidates the in-scope inputs (already filtered to update time ≥ cursor where cheap)
     * @param loopGuardExcludeAnalystOrigin true to drop analyst-origin events unchanged since the cursor
     */
    public static Packet detect(AnalystCursor cursor, List<Input> candidates, boolean loopGuardExcludeAnalystOrigin) {
        List<Input> fresh = new ArrayList<>();
        for (Input input : candidates) {
            if (!isNew(cursor, input)) {
                continue;
            }
            if (loopGuardExcludeAnalystOrigin && "analyst".equals(input.origin())) {
                continue;
            }
            fresh.add(input);
        }
        fresh.sort(Comparator.comparingInt((Input i) -> -severityRank(i.severity()))
                .thenComparing(i -> -(i.confidence() == null ? 0 : i.confidence()))
                .thenComparing(i -> i.at() == null ? Instant.EPOCH : i.at(), Comparator.reverseOrder()));
        List<Input> capped = fresh.size() > CAP ? fresh.subList(0, CAP) : fresh;

        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("events", 0);
        counts.put("anomalies", 0);
        counts.put("alerts", 0);
        AnalystCursor next = cursor;
        for (Input input : fresh) {
            counts.merge(input.kind(), 1, Integer::sum);
            next = advance(next, input);
        }
        return new Packet(new ArrayList<>(capped), counts, next);
    }

    /** True when the input is new or updated relative to the cursor. */
    static boolean isNew(AnalystCursor cursor, Input input) {
        return switch (input.kind()) {
            case "alert" -> cursor.alertId() == null || longId(input.id()) > cursor.alertId();
            case "anomaly" -> cursor.anomalyId() == null || longId(input.id()) > cursor.anomalyId();
            case "event" -> cursor.eventUpdatedAt() == null
                    || (input.at() != null && input.at().isAfter(cursor.eventUpdatedAt()));
            default -> true;
        };
    }

    /** Advances the cursor by one input. */
    static AnalystCursor advance(AnalystCursor cursor, Input input) {
        return switch (input.kind()) {
            case "alert" -> new AnalystCursor(cursor.seq(), max(cursor.alertId(), longId(input.id())),
                    cursor.eventUpdatedAt(), cursor.anomalyId());
            case "anomaly" -> new AnalystCursor(cursor.seq(), cursor.alertId(), cursor.eventUpdatedAt(),
                    max(cursor.anomalyId(), longId(input.id())));
            case "event" -> new AnalystCursor(cursor.seq(), cursor.alertId(),
                    later(cursor.eventUpdatedAt(), input.at()), cursor.anomalyId());
            default -> cursor;
        };
    }

    private static int severityRank(String severity) {
        if (severity == null) {
            return 0;
        }
        return switch (severity) {
            case "critical" -> 3;
            case "warning" -> 2;
            case "info" -> 1;
            default -> 0;
        };
    }

    private static long longId(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Long max(Long a, long b) {
        return a == null ? b : Math.max(a, b);
    }

    private static Instant later(Instant a, Instant b) {
        if (b == null) {
            return a;
        }
        return a == null || b.isAfter(a) ? b : a;
    }
}
