package ai.foresignal.stats;

import ai.foresignal.api.error.ValidationException;
import java.util.List;
import java.util.Locale;

/**
 * A stats measure (prompt 038). {@link #CHANGES} is internal (the briefing digest's {@code changed});
 * the API accepts the others.
 */
public enum StatsMeasure {

    /** Persisted versions, HEARTBEAT and KEYFRAME excluded. */
    VERSIONS("versions", "count(*)"),
    /** Distinct entities. */
    ENTITIES("entities", "count(DISTINCT v.source_id || chr(31) || v.external_id)"),
    /** FIRST and REAPPEAR versions. */
    FIRSTS("firsts", "count(*) FILTER (WHERE v.persist_reason IN (1, 2))"),
    /** TOMBSTONE versions. */
    TOMBSTONES("tombstones", "count(*) FILTER (WHERE v.persist_reason = 4)"),
    /** TRIGGER_EDGE versions. */
    EDGES("edges", "count(*) FILTER (WHERE v.persist_reason = 9)"),
    /** Alerts fired ({@code alert.fired_at}). */
    ALERTS("alerts", "count(*)"),
    /** SIGNIFICANT and CHANGE versions (internal). */
    CHANGES("changes", "count(*) FILTER (WHERE v.persist_reason IN (3, 5))");

    /** The values the API accepts. */
    public static final List<String> API_VALUES = List.of("versions", "entities", "firsts", "tombstones", "edges",
            "alerts");

    private final String wire;
    private final String rawSql;

    StatsMeasure(String wire, String rawSql) {
        this.wire = wire;
        this.rawSql = rawSql;
    }

    /** The wire value, also the {@code activity_rollup} column (except {@link #ALERTS}). */
    public String wire() {
        return wire;
    }

    /** The aggregate over raw {@code record_version v} rows. */
    public String rawSql() {
        return rawSql;
    }

    /** The rollup column; {@link #ALERTS} has none. */
    public String column() {
        if (this == ALERTS) {
            throw new IllegalStateException("alerts are not rolled up");
        }
        return wire;
    }

    /** Parses an API value (default {@code versions}). */
    public static StatsMeasure parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return VERSIONS;
        }
        String value = raw.strip().toLowerCase(Locale.ROOT);
        if (!API_VALUES.contains(value)) {
            throw ValidationException.forField("measure", "must be one of " + String.join(", ", API_VALUES));
        }
        return valueOf(value.toUpperCase(Locale.ROOT));
    }
}
