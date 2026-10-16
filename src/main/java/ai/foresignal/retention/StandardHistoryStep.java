package ai.foresignal.retention;

import ai.foresignal.store.RetentionStateEntity;
import java.time.Duration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * Step 2 (Core-Data-Model §13): chunked deletes of STANDARD history per (source, kind) window.
 *
 * <p>Each chunk is its own transaction (autocommit) of at most {@code chunkSize} rows, with the
 * outer seq bound so the planner prunes leaves:
 *
 * <pre>
 * DELETE FROM record_version WHERE history_tier = 0 AND seq &lt; :cutoff
 *   AND seq IN (SELECT seq FROM record_version WHERE history_tier = 0 AND source_id = :source
 *               AND kind = :kind AND seq &lt; :cutoff LIMIT :limit)
 * </pre>
 *
 * <p>When every kind of a source resolves to the same window the kind predicate is omitted. Otherwise
 * each declared kind is deleted with its own window, and records of undeclared kinds with the
 * source-level window. On completion the floors are recorded (never lowered) and the source becomes
 * a head-purge candidate; on failure the source is skipped for this run (no HeadPurge). Rows of
 * source ids no longer registered use {@code retention.default}. Retention only deletes versions
 * below a cutoff taken from the past; it never writes heads or live rows (I1).
 */
@Component
public class StandardHistoryStep implements RetentionStep {

    private static final Logger log = LoggerFactory.getLogger(StandardHistoryStep.class);

    static final String DELETE_CHUNK = """
            DELETE FROM record_version WHERE history_tier = 0 AND seq < :cutoff
               AND seq IN (SELECT seq FROM record_version
                            WHERE history_tier = 0 AND source_id = :source%s AND seq < :cutoff LIMIT :limit)
            """;

    /** Whether a scope has a version below a raw cutoff (the clamp is needed only then). */
    static final String ANY_BELOW = """
            SELECT EXISTS (SELECT 1 FROM record_version
                            WHERE history_tier = 0 AND source_id = :source%s AND seq < :raw)
            """;

    /** Distinct STANDARD source ids by a loose index scan on {@code (source_id, external_id, seq)}. */
    static final String DISTINCT_SOURCES = """
            WITH RECURSIVE s(id) AS (
                SELECT min(source_id) FROM record_version WHERE history_tier = 0
                UNION ALL
                SELECT (SELECT min(source_id) FROM record_version WHERE history_tier = 0 AND source_id > s.id)
                  FROM s WHERE s.id IS NOT NULL)
            SELECT id FROM s WHERE id IS NOT NULL
            """;

    private final RetentionResolver resolver;
    private final RetentionStateStore state;
    private final RetentionProperties properties;

    /** Creates the step. */
    public StandardHistoryStep(RetentionResolver resolver, RetentionStateStore state, RetentionProperties properties) {
        this.resolver = resolver;
        this.state = state;
        this.properties = properties;
    }

    @Override
    public String name() {
        return "standard-history";
    }

    @Override
    public int order() {
        return 200;
    }

    @Override
    public void run(RetentionRun run) {
        Map<String, Map<String, Duration>> windows = new LinkedHashMap<>(resolver.standardWindows());
        Set<String> registered = new HashSet<>(windows.keySet());
        Set<String> track = new HashSet<>();
        for (String id : run.jdbc().getJdbcTemplate().queryForList(DISTINCT_SOURCES, String.class)) {
            if (!registered.contains(id) && !resolver.isRegistered(id)) {
                Map<String, Duration> fallback = new LinkedHashMap<>();
                fallback.put(null, run.settings().defaultRetention());
                windows.put(id, fallback);
            } else if (!registered.contains(id)) {
                track.add(id);
            }
        }
        for (Map.Entry<String, Map<String, Duration>> source : windows.entrySet()) {
            if (!track.contains(source.getKey())) {
                process(run, source.getKey(), source.getValue());
            }
        }
    }

    private void process(RetentionRun run, String sourceId, Map<String, Duration> windows) {
        if (run.skipped(sourceId)) {
            // A prior step (hold copy or cold archive, prompt 111) failed for this source: keep its
            // rows this run so nothing is deleted before it was written out.
            return;
        }
        Duration sourceLevel = windows.get(null);
        Map<String, Duration> kinds = new LinkedHashMap<>(windows);
        kinds.remove(null);
        Set<Duration> distinct = new HashSet<>(kinds.values());
        distinct.add(sourceLevel);
        Map<String, Long> floors = new LinkedHashMap<>();
        List<RetentionRun.PurgeCandidate> candidates = new java.util.ArrayList<>();
        try {
            if (distinct.size() == 1) {
                long cutoff = cutoff(run, sourceId, "", new MapSqlParameterSource(), sourceLevel);
                delete(run, sourceId, "", new MapSqlParameterSource(), cutoff);
                long floor = Math.max(cutoff, state.storedFloor(sourceId, null));
                floors.put(RetentionStateEntity.ALL_KINDS, floor);
                candidates.add(new RetentionRun.PurgeCandidate(sourceId, null, floor, sourceLevel));
            } else {
                for (Map.Entry<String, Duration> kind : kinds.entrySet()) {
                    long cutoff = cutoff(run, sourceId, " AND kind = :kind",
                            new MapSqlParameterSource("kind", kind.getKey()), kind.getValue());
                    delete(run, sourceId, " AND kind = :kind", new MapSqlParameterSource("kind", kind.getKey()), cutoff);
                    long floor = Math.max(cutoff, state.storedFloor(sourceId, kind.getKey()));
                    floors.put(kind.getKey(), floor);
                    candidates.add(new RetentionRun.PurgeCandidate(sourceId, kind.getKey(), floor, kind.getValue()));
                }
                long cutoff = cutoff(run, sourceId, " AND kind NOT IN (:kinds)",
                        new MapSqlParameterSource("kinds", List.copyOf(kinds.keySet())), sourceLevel);
                delete(run, sourceId, " AND kind NOT IN (:kinds)",
                        new MapSqlParameterSource("kinds", List.copyOf(kinds.keySet())), cutoff);
                floors.put(RetentionStateEntity.ALL_KINDS, cutoff);
            }
        } catch (RuntimeException e) {
            log.warn("Retention could not delete old versions of {}; skipping it this run", sourceId, e);
            run.skip(sourceId);
            run.error("standard history of " + sourceId + ": " + e.getMessage());
            return;
        }
        for (Duration window : distinct) {
            run.standardWindow(window);
        }
        floors.forEach((kind, floor) -> run.floor(sourceId, kind, floor));
        candidates.forEach(run::candidate);
    }

    /**
     * The cutoff of one scope (a source, or some of its kinds): {@code cutoffSeq(now − window)},
     * clamped by the consumers only when the scope has a version below it. With nothing to delete
     * there is nothing to hold back, and clamping would still judge every consumer against this
     * scope's window and release it as lagging, with an alarm, over data it never covered (prompt
     * 022). The floor is the raw cutoff then, which is true: no version of the scope is below it.
     */
    private long cutoff(RetentionRun run, String sourceId, String kindClause, MapSqlParameterSource params,
            Duration window) {
        long raw = run.rawCutoff(window);
        if (raw <= 0) {
            return raw;
        }
        MapSqlParameterSource query = new MapSqlParameterSource(params.getValues())
                .addValue("source", sourceId).addValue("raw", raw);
        Boolean any = run.jdbc().queryForObject(ANY_BELOW.formatted(kindClause), query, Boolean.class);
        return Boolean.TRUE.equals(any) ? run.clamp(raw, window) : raw;
    }

    private void delete(RetentionRun run, String sourceId, String kindClause, MapSqlParameterSource params,
            long cutoff) {
        if (cutoff <= 0) {
            run.versionsDeleted(sourceId, 0);
            return;
        }
        String sql = DELETE_CHUNK.formatted(kindClause);
        params.addValue("cutoff", cutoff).addValue("source", sourceId).addValue("limit", properties.chunkSize());
        int deleted;
        do {
            deleted = run.jdbc().update(sql, params);
            run.versionsDeleted(sourceId, deleted);
        } while (deleted >= properties.chunkSize());
    }
}
