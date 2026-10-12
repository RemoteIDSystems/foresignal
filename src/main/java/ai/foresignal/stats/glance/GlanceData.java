package ai.foresignal.stats.glance;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What the glance shows, before it is fitted into 2,048 bytes ({@link GlanceAssembler}).
 *
 * @param serverTime the clock reading
 * @param status {@code ok}, {@code degraded} or {@code failing}
 * @param open open alerts per severity, or null when counts are not shown
 * @param latest the latest open alerts, newest first, at most 3
 * @param sources source health tallies, or null when counts are not shown
 * @param briefing the latest briefing run, or null
 * @param versionsLastHour versions ingested in the last hour, or null when counts are not shown
 * @param lastSeq the ChangeBus watermark
 * @param links deep links, or null
 * @param sections contributor sections in order (already within their budgets), or null
 */
public record GlanceData(Instant serverTime, String status, Map<String, Long> open, List<Latest> latest,
        Sources sources, Briefing briefing, Long versionsLastHour, long lastSeq, Map<String, String> links,
        LinkedHashMap<String, Object> sections) {

    /** Copies the collections. */
    public GlanceData {
        latest = latest == null ? List.of() : List.copyOf(latest);
    }

    /**
     * One latest alert.
     *
     * @param id the alert id
     * @param severity its severity
     * @param shortTitle the rule name or message, at most {@value GlanceAssembler#MAX_TITLE} characters
     * @param firedAt when it fired
     * @param near a place text such as "12 km NNE of Austin, TX" (prompt 040), or null
     */
    public record Latest(long id, String severity, String shortTitle, Instant firedAt, String near) {}

    /**
     * Source health tallies.
     *
     * @param healthy healthy sources
     * @param degraded degraded sources
     * @param failing failing sources
     */
    public record Sources(long healthy, long degraded, long failing) {}

    /**
     * The latest briefing run.
     *
     * @param title the briefing's name
     * @param at when the run started
     * @param url its HTML rendition
     */
    public record Briefing(String title, Instant at, String url) {}
}
