package ai.foresignal.situation;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One situation stream event (prompt 118) on the {@code situations} SSE topic: {@code situation.created},
 * {@code .updated}, {@code .activated}, {@code .escalated}, {@code .dormant}, {@code .closed} or {@code .merged}.
 * {@code cursor} is the {@code situation_log} id that announced it, strictly increasing.
 *
 * @param cursor the topic cursor
 * @param event the SSE event name
 * @param situationId the situation
 * @param version the situation's version (for upsert)
 * @param situation the {@code SituationSummary}
 * @param previousStatus the status before, or null
 * @param previousSeverity the severity before, or null
 * @param mergedInto the surviving situation of a merge, or null
 * @param at when it happened
 */
public record SituationEvent(long cursor, String event, String situationId, int version,
        Map<String, Object> situation, String previousStatus, String previousSeverity, String mergedInto,
        Instant at) {

    /** A situation was created. */
    public static final String CREATED = "situation.created";

    /** A situation changed (members, title, theme, brief…). */
    public static final String UPDATED = "situation.updated";

    /** A situation became active. */
    public static final String ACTIVATED = "situation.activated";

    /** A situation's severity was raised. */
    public static final String ESCALATED = "situation.escalated";

    /** A situation went dormant. */
    public static final String DORMANT = "situation.dormant";

    /** A situation was closed. */
    public static final String CLOSED = "situation.closed";

    /** A situation was merged into another. */
    public static final String MERGED = "situation.merged";

    /** The {@code SituationEvent} payload. */
    public Map<String, Object> payload() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("event", event);
        out.put("situation", situation);
        out.put("version", version);
        out.put("previousStatus", previousStatus);
        out.put("previousSeverity", previousSeverity);
        out.put("mergedInto", mergedInto);
        out.put("at", at == null ? null : at.toString());
        return out;
    }

    /** True when the situation is private. */
    public boolean privateSituation() {
        return situation != null && Boolean.TRUE.equals(situation.get("private"));
    }
}
