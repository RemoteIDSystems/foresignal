package ai.foresignal.briefing;

import ai.foresignal.api.support.BboxFilter;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Where the digest statistics come from (prompt 029). {@link SqlDigestStatsProvider} reads
 * {@code record_version}, {@code record_live ⋈ record_head} and {@code alert} directly; prompt 038
 * swaps in a rollup-backed implementation ({@code activity_rollup}) for the counts.
 *
 * <p>Every method takes the caller's template so one digest runs in one read transaction with the
 * read API's statement timeout.
 */
public interface DigestStatsProvider {

    /**
     * Version counts between the seq bounds, per {@code (source, kind)}.
     *
     * @param fromSeq exclusive lower bound
     * @param toSeq inclusive upper bound
     */
    Map<Key, ReasonCounts> counts(NamedParameterJdbcTemplate jdbc, Filter filter, long fromSeq, long toSeq);

    /** Entities observed at or after {@code since} per {@code (source, kind)}, tombstoned heads excluded. */
    Map<Key, Long> active(NamedParameterJdbcTemplate jdbc, Filter filter, Instant since);

    /**
     * The notable current records whose head seq lies between the bounds, at most {@code perKind} per
     * kind, ranked by the source's {@code rankField} (descending), else by effective time; sources in
     * {@link Filter#hiddenSources()} are left out.
     */
    List<Row> notable(NamedParameterJdbcTemplate jdbc, Filter filter, long fromSeq, long toSeq, int perKind);

    /**
     * The alerts with {@code sinceAlertId < id <= untilAlertId} in scope (suppressed ones excluded):
     * group counts over all of them, and up to {@code limit} ids (sources in
     * {@link Filter#hiddenSources()} left out), critical first, then newest.
     */
    AlertWindow alerts(NamedParameterJdbcTemplate jdbc, Filter filter, long sinceAlertId, long untilAlertId,
            int limit);

    /** The largest alert id, 0 when there is none. */
    long maxAlertId(NamedParameterJdbcTemplate jdbc);

    /**
     * The alert cursor for "alerts fired at or after {@code t}": one less than the first such alert's
     * id, or {@link #maxAlertId} when none fired since.
     */
    long alertCursorAt(NamedParameterJdbcTemplate jdbc, Instant t);

    /**
     * The filters of one digest; empty collections mean "all".
     *
     * @param sources source ids
     * @param kinds kinds
     * @param bbox the area, or null
     * @param ruleIds rules for the alert section
     * @param hiddenSources sources whose records and alerts may be counted but not listed
     */
    record Filter(List<String> sources, List<String> kinds, BboxFilter bbox, Set<UUID> ruleIds,
            Set<String> hiddenSources, ai.foresignal.security.restrict.ReadRestriction restriction) {

        /** Copies the collections; a null restriction becomes none. */
        public Filter {
            sources = sources == null ? List.of() : List.copyOf(sources);
            kinds = kinds == null ? List.of() : List.copyOf(kinds);
            ruleIds = ruleIds == null ? Set.of() : Set.copyOf(ruleIds);
            hiddenSources = hiddenSources == null ? Set.of() : Set.copyOf(hiddenSources);
            restriction = restriction == null ? ai.foresignal.security.restrict.ReadRestriction.NONE : restriction;
        }

        /** An unrestricted filter (the shape before prompt 035). */
        public Filter(List<String> sources, List<String> kinds, BboxFilter bbox, Set<UUID> ruleIds,
                Set<String> hiddenSources) {
            this(sources, kinds, bbox, ruleIds, hiddenSources, null);
        }
    }

    /**
     * A {@code (source, kind)} pair.
     *
     * @param sourceId the source
     * @param kind the kind
     */
    record Key(String sourceId, String kind) {}

    /**
     * Per-reason version counts and distinct entities of one {@code (source, kind)}.
     *
     * @param byCode versions indexed by persist reason code (index 0 unused, length 10)
     * @param entitiesNew distinct entities with FIRST or REAPPEAR
     * @param entitiesChanged distinct entities with SIGNIFICANT or CHANGE
     * @param entitiesRemoved distinct entities with TOMBSTONE
     * @param entitiesEdges distinct entities with TRIGGER_EDGE
     */
    record ReasonCounts(long[] byCode, long entitiesNew, long entitiesChanged, long entitiesRemoved,
            long entitiesEdges) {}

    /**
     * One notable record.
     *
     * @param sourceId the source
     * @param externalId the external id
     * @param kind the kind
     * @param seq the head seq
     * @param title the stored title, or null
     * @param live {@code record_live.dirty}
     * @param observedAt the live observation time
     * @param effectiveTime {@code coalesce(event_time, observed_at)}
     * @param rankValue the rank field's numeric value, or null
     */
    record Row(String sourceId, String externalId, String kind, long seq, String title, boolean live,
            Instant observedAt, Instant effectiveTime, Double rankValue) {}

    /**
     * Alert group counts and the ids to list.
     *
     * @param groups one row per (severity, state, rule)
     * @param itemIds the ids to list, in order
     */
    record AlertWindow(List<AlertGroup> groups, List<Long> itemIds) {}

    /**
     * Alerts of one severity, state and rule.
     *
     * @param severity the severity
     * @param state the lifecycle state
     * @param ruleId the rule
     * @param ruleName its name, or null when deleted
     * @param count the alerts
     */
    record AlertGroup(String severity, String state, UUID ruleId, String ruleName, long count) {}
}
