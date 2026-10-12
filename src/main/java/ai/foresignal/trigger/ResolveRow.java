package ai.foresignal.trigger;

import org.springframework.jdbc.core.RowMapper;

/**
 * One version where an entity stops satisfying a rule ({@link RuleSqlBuilder#inverse}).
 *
 * @param seq the version's seq
 * @param sourceId the source
 * @param externalId the entity
 * @param tombstone true when the version is a tombstone
 */
public record ResolveRow(long seq, String sourceId, String externalId, boolean tombstone) {

    /** Reads the columns of {@link RuleSqlBuilder#inverse}. */
    public static final RowMapper<ResolveRow> MAPPER = (rs, row) ->
            new ResolveRow(rs.getLong("seq"), rs.getString("source_id"), rs.getString("external_id"), rs.getBoolean("tombstone"));

    /** How the alert is resolved: {@code tombstone} or {@code inverse_edge}. */
    public String resolvedBy() {
        return tombstone ? "tombstone" : "inverse_edge";
    }
}
