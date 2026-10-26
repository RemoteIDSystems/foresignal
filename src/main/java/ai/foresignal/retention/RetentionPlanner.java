package ai.foresignal.retention;

import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.history.LeafInfo;
import ai.foresignal.store.history.PartitionManager;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * What one retention run is about to remove (Core-Data-Model §13; prompt 111): the TRACK leaves step
 * 1 will drop and the STANDARD (source, kind) scopes and cutoffs step 2 will delete below.
 *
 * <p>Each plan is computed once per run and kept on the {@link RetentionRun} ({@link RetentionRun#memo}),
 * so the steps that write rows out before they go (the 111 hold copy and cold archive) and the
 * steps that remove them ({@link TrackPartitionStep}, {@link StandardHistoryStep}) act on exactly the
 * same leaves and cutoffs, consumer clamp included.
 */
@Component
public class RetentionPlanner {

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

    /**
     * The TRACK plan of a run.
     *
     * @param rawCutoff {@code cutoffSeq(now − trackWindow)}
     * @param cutoff the cutoff after the consumer clamp (applied only when some leaf is droppable)
     * @param leaves every attached TRACK leaf, by lower bound
     * @param droppable the leading leaves whose upper bound is at or below {@code cutoff}, in order
     */
    public record TrackPlan(long rawCutoff, long cutoff, List<LeafInfo> leaves, List<LeafInfo> droppable) {}

    /**
     * One STANDARD deletion scope: the rows of {@code sourceId} below {@code cutoff}, restricted to
     * {@code kind}, or to the kinds outside {@code otherThan}, or not restricted.
     *
     * @param sourceId the source
     * @param kind the one kind of this scope, or null
     * @param otherThan the declared kinds this scope excludes ("every other kind"), or empty
     * @param window the window the cutoff was taken for
     * @param cutoff every row of the scope with a lower seq is deleted this run
     */
    public record StandardScope(String sourceId, String kind, List<String> otherThan, Duration window, long cutoff) {

        /** Copies the list. */
        public StandardScope {
            otherThan = otherThan == null ? List.of() : List.copyOf(otherThan);
        }

        /** True when the scope is the whole source. */
        public boolean wholeSource() {
            return kind == null && otherThan.isEmpty();
        }

        /** The kind predicate, {@code " AND kind = :kind"}, {@code " AND kind NOT IN (:kinds)"} or empty. */
        public String kindClause(String alias) {
            String column = alias == null || alias.isEmpty() ? "kind" : alias + ".kind";
            if (kind != null) {
                return " AND " + column + " = :kind";
            }
            if (!otherThan.isEmpty()) {
                return " AND " + column + " NOT IN (:kinds)";
            }
            return "";
        }

        /** The parameters of {@link #kindClause}. */
        public MapSqlParameterSource kindParams() {
            MapSqlParameterSource params = new MapSqlParameterSource();
            if (kind != null) {
                params.addValue("kind", kind);
            } else if (!otherThan.isEmpty()) {
                params.addValue("kinds", otherThan);
            }
            return params;
        }

        /** A stable name of the scope: {@code *}, the kind, or {@code !k1,k2}. */
        public String scopeKey() {
            if (kind != null) {
                return kind;
            }
            return otherThan.isEmpty() ? "*" : "!" + String.join(",", otherThan);
        }
    }

    /** The STANDARD plan of a run: scopes per source, sources in processing order. */
    public record StandardPlan(Map<String, List<StandardScope>> scopes) {

        /** Copies the map, keeping its order. */
        public StandardPlan {
            scopes = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(scopes));
        }
    }

    private final PartitionManager partitions;
    private final RetentionResolver resolver;

    /** Creates the planner. */
    public RetentionPlanner(PartitionManager partitions, RetentionResolver resolver) {
        this.partitions = partitions;
        this.resolver = resolver;
    }

    /** The TRACK plan of {@code run}, computed on first use. */
    public TrackPlan track(RetentionRun run) {
        return run.memo(TrackPlan.class, () -> computeTrack(run));
    }

    /** The STANDARD plan of {@code run}, computed on first use. */
    public StandardPlan standard(RetentionRun run) {
        return run.memo(StandardPlan.class, () -> computeStandard(run));
    }

    private TrackPlan computeTrack(RetentionRun run) {
        Duration window = run.settings().effectiveTrackWindow();
        partitions.refresh();
        List<LeafInfo> leaves = partitions.leaves(HistoryTier.TRACK).stream()
                .sorted(Comparator.comparingLong(LeafInfo::fromSeq)).toList();
        // The consumer clamp is applied only when some leaf could be dropped at all. Otherwise there
        // is nothing to hold back, and clamping would still judge every consumer against the short
        // track window and report it as lagging (and release it with an alarm) for data this step
        // never touches (prompt 022).
        long raw = run.rawCutoff(window);
        boolean droppable = !leaves.isEmpty() && leaves.getFirst().toSeq() <= raw;
        long cutoff = droppable ? run.clamp(raw, window) : raw;
        List<LeafInfo> drop = new ArrayList<>();
        for (LeafInfo leaf : leaves) {
            if (leaf.toSeq() > cutoff) {
                break;
            }
            drop.add(leaf);
        }
        return new TrackPlan(raw, cutoff, leaves, List.copyOf(drop));
    }

    private StandardPlan computeStandard(RetentionRun run) {
        Map<String, Map<String, Duration>> windows = new LinkedHashMap<>(resolver.standardWindows(run.settings()));
        Set<String> registered = new HashSet<>(windows.keySet());
        Set<String> track = new HashSet<>();
        for (String id : run.jdbc().getJdbcTemplate().queryForList(DISTINCT_SOURCES, String.class)) {
            if (!registered.contains(id) && !resolver.isRegistered(id)) {
                Map<String, Duration> fallback = new LinkedHashMap<>();
                fallback.put(null, run.settings().capped(run.settings().defaultRetention()));
                windows.put(id, fallback);
            } else if (!registered.contains(id)) {
                track.add(id);
            }
        }
        Map<String, List<StandardScope>> out = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Duration>> source : windows.entrySet()) {
            if (!track.contains(source.getKey())) {
                out.put(source.getKey(), scopes(run, source.getKey(), source.getValue()));
            }
        }
        return new StandardPlan(out);
    }

    private List<StandardScope> scopes(RetentionRun run, String sourceId, Map<String, Duration> windows) {
        Duration sourceLevel = windows.get(null);
        Map<String, Duration> kinds = new LinkedHashMap<>(windows);
        kinds.remove(null);
        Set<Duration> distinct = new HashSet<>(kinds.values());
        distinct.add(sourceLevel);
        List<StandardScope> scopes = new ArrayList<>();
        if (distinct.size() == 1) {
            StandardScope whole = new StandardScope(sourceId, null, List.of(), sourceLevel, 0);
            scopes.add(withCutoff(run, whole));
            return scopes;
        }
        for (Map.Entry<String, Duration> kind : kinds.entrySet()) {
            scopes.add(withCutoff(run, new StandardScope(sourceId, kind.getKey(), List.of(), kind.getValue(), 0)));
        }
        scopes.add(withCutoff(run, new StandardScope(sourceId, null, List.copyOf(kinds.keySet()), sourceLevel, 0)));
        return scopes;
    }

    /**
     * The scope with its cutoff: {@code cutoffSeq(now − window)}, clamped by the consumers only when
     * the scope has a version below it. With nothing to delete there is nothing to hold back, and
     * clamping would still judge every consumer against this scope's window and release it as
     * lagging, with an alarm, over data it never covered (prompt 022). The floor is the raw cutoff
     * then, which is true: no version of the scope is below it.
     */
    private StandardScope withCutoff(RetentionRun run, StandardScope scope) {
        long raw = run.rawCutoff(scope.window());
        long cutoff = raw;
        if (raw > 0) {
            MapSqlParameterSource query = scope.kindParams().addValue("source", scope.sourceId()).addValue("raw", raw);
            Boolean any = run.jdbc().queryForObject(ANY_BELOW.formatted(scope.kindClause(null)), query, Boolean.class);
            cutoff = Boolean.TRUE.equals(any) ? run.clamp(raw, scope.window()) : raw;
        }
        return new StandardScope(scope.sourceId(), scope.kind(), scope.otherThan(), scope.window(), cutoff);
    }
}
