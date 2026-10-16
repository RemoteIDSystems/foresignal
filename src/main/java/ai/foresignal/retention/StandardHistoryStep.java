package ai.foresignal.retention;

import ai.foresignal.store.RetentionStateEntity;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
 * <p>The scopes and cutoffs come from {@link RetentionPlanner#standard}, the same plan the 111 hold
 * copy and cold archive wrote out before this step: when every kind of a source resolves to the same
 * window the kind predicate is omitted; otherwise each declared kind is deleted with its own window,
 * and records of undeclared kinds with the source-level window. On completion the floors are
 * recorded (never lowered) and the source becomes a head-purge candidate; on failure, or when an
 * earlier step skipped the source (a failed hold copy or archive write), the source is skipped for
 * this run (no HeadPurge). Rows of source ids no longer registered use {@code retention.default}.
 * Retention only deletes versions below a cutoff taken from the past; it never writes heads or live
 * rows (I1).
 */
@Component
public class StandardHistoryStep implements RetentionStep {

    private static final Logger log = LoggerFactory.getLogger(StandardHistoryStep.class);

    static final String DELETE_CHUNK = """
            DELETE FROM record_version WHERE history_tier = 0 AND seq < :cutoff
               AND seq IN (SELECT seq FROM record_version
                            WHERE history_tier = 0 AND source_id = :source%s AND seq < :cutoff LIMIT :limit)
            """;

    private final RetentionPlanner planner;
    private final RetentionStateStore state;
    private final RetentionProperties properties;

    /** Creates the step. */
    public StandardHistoryStep(RetentionPlanner planner, RetentionStateStore state, RetentionProperties properties) {
        this.planner = planner;
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
        for (Map.Entry<String, List<RetentionPlanner.StandardScope>> source : planner.standard(run).scopes().entrySet()) {
            process(run, source.getKey(), source.getValue());
        }
    }

    private void process(RetentionRun run, String sourceId, List<RetentionPlanner.StandardScope> scopes) {
        if (run.skipped(sourceId)) {
            // A prior step (hold copy or cold archive, prompt 111) failed for this source: keep its
            // rows this run so nothing is deleted before it was written out.
            return;
        }
        Map<String, Long> floors = new LinkedHashMap<>();
        List<RetentionRun.PurgeCandidate> candidates = new ArrayList<>();
        Set<Duration> windows = new LinkedHashSet<>();
        try {
            for (RetentionPlanner.StandardScope scope : scopes) {
                windows.add(scope.window());
                delete(run, scope);
                if (scope.wholeSource()) {
                    long floor = Math.max(scope.cutoff(), state.storedFloor(sourceId, null));
                    floors.put(RetentionStateEntity.ALL_KINDS, floor);
                    candidates.add(new RetentionRun.PurgeCandidate(sourceId, null, floor, scope.window()));
                } else if (scope.kind() != null) {
                    long floor = Math.max(scope.cutoff(), state.storedFloor(sourceId, scope.kind()));
                    floors.put(scope.kind(), floor);
                    candidates.add(new RetentionRun.PurgeCandidate(sourceId, scope.kind(), floor, scope.window()));
                } else {
                    floors.put(RetentionStateEntity.ALL_KINDS, scope.cutoff());
                }
            }
        } catch (RuntimeException e) {
            log.warn("Retention could not delete old versions of {}; skipping it this run", sourceId, e);
            run.skip(sourceId);
            run.error("standard history of " + sourceId + ": " + e.getMessage());
            return;
        }
        for (Duration window : windows) {
            run.standardWindow(window);
        }
        floors.forEach((kind, floor) -> run.floor(sourceId, kind, floor));
        candidates.forEach(run::candidate);
    }

    private void delete(RetentionRun run, RetentionPlanner.StandardScope scope) {
        if (scope.cutoff() <= 0) {
            run.versionsDeleted(scope.sourceId(), 0);
            return;
        }
        String sql = DELETE_CHUNK.formatted(scope.kindClause(null));
        MapSqlParameterSource params = scope.kindParams().addValue("cutoff", scope.cutoff())
                .addValue("source", scope.sourceId()).addValue("limit", properties.chunkSize());
        int deleted;
        do {
            deleted = run.jdbc().update(sql, params);
            run.versionsDeleted(scope.sourceId(), deleted);
        } while (deleted >= properties.chunkSize());
    }
}
