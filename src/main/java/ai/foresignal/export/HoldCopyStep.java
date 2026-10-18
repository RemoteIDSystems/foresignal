package ai.foresignal.export;

import ai.foresignal.retention.RetentionPlanner;
import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.history.HistoryDdl;
import ai.foresignal.store.history.LeafInfo;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * Archive-then-delete for holds (prompt 111): a {@link RetentionStep} ordered before every delete
 * and drop. It copies exactly the rows this run is about to remove ({@link RetentionPlanner}: the
 * TRACK leaves step 1 drops and the STANDARD scopes and cutoffs step 2 deletes below) that fall
 * inside an active hold into {@link HeldVersionsTable#NAME}, with
 * {@code INSERT … ON CONFLICT (hold_id, seq) DO NOTHING}, so a repeated copy is harmless.
 *
 * <p>Retention still drops leaves and deletes versions, and {@code floorSeq} and I7 are unchanged:
 * the copies live outside {@code record_version}. When a copy fails, the rows it would have saved
 * are kept this run, the failure rule of Core-Data-Model §13 step 2: a STANDARD source is skipped
 * ({@link RetentionRun#skip}), a TRACK leaf is blocked ({@link RetentionRun#blockLeaf}).
 */
@Component
public class HoldCopyStep implements RetentionStep {

    private static final Logger log = LoggerFactory.getLogger(HoldCopyStep.class);

    /** The column list of {@code record_version} (and of the held table after {@code hold_id}). */
    static final String COLUMNS = HistoryDdl.COLUMNS.stream().map(HistoryDdl.Column::name)
            .collect(Collectors.joining(", "));

    private final HoldService holds;
    private final RetentionPlanner planner;

    /** Creates the step. */
    public HoldCopyStep(HoldService holds, RetentionPlanner planner) {
        this.holds = holds;
        this.planner = planner;
    }

    @Override
    public String name() {
        return "hold-copy";
    }

    @Override
    public int order() {
        return 90;
    }

    @Override
    public void run(RetentionRun run) {
        List<HoldEntity> active = holds.active(run.now());
        if (active.isEmpty()) {
            return;
        }
        copyTrack(run, active);
        copyStandard(run, active);
    }

    private void copyTrack(RetentionRun run, List<HoldEntity> active) {
        for (LeafInfo leaf : planner.track(run).droppable()) {
            for (HoldEntity hold : active) {
                try {
                    String where = " WHERE v.history_tier = :tier AND v.seq >= :leafFrom AND v.seq < :leafTo";
                    MapSqlParameterSource params = new MapSqlParameterSource("tier", HistoryTier.TRACK.code())
                            .addValue("leafFrom", leaf.fromSeq()).addValue("leafTo", leaf.toSeq());
                    copy(run, hold, where, params);
                } catch (RuntimeException e) {
                    log.warn("Could not copy rows of track partition {} held by {}; keeping the partition this run",
                            leaf.name(), hold.getId(), e);
                    run.blockLeaf(leaf.name());
                    run.error("hold copy " + hold.getId() + " of " + leaf.name() + ": " + e.getMessage());
                    // Later leaves are not dropped either (the TRACK step stops at a blocked leaf).
                    return;
                }
            }
        }
    }

    private void copyStandard(RetentionRun run, List<HoldEntity> active) {
        for (Map.Entry<String, List<RetentionPlanner.StandardScope>> source
                : planner.standard(run).scopes().entrySet()) {
            String sourceId = source.getKey();
            if (run.skipped(sourceId)) {
                continue;
            }
            try {
                for (HoldEntity hold : active) {
                    if (!hold.coversSource(sourceId)) {
                        continue;
                    }
                    for (RetentionPlanner.StandardScope scope : source.getValue()) {
                        if (scope.cutoff() <= 0) {
                            continue;
                        }
                        String where = " WHERE v.history_tier = :tier AND v.source_id = :source AND v.seq < :cutoff"
                                + scope.kindClause("v");
                        MapSqlParameterSource params = scope.kindParams()
                                .addValue("tier", HistoryTier.STANDARD.code())
                                .addValue("source", sourceId).addValue("cutoff", scope.cutoff());
                        copy(run, hold, where, params);
                    }
                }
            } catch (RuntimeException e) {
                log.warn("Could not copy held rows of {}; keeping its versions this run", sourceId, e);
                run.skip(sourceId);
                run.error("hold copy of " + sourceId + ": " + e.getMessage());
            }
        }
    }

    /** Copies the rows matching {@code where} that fall inside {@code hold}. */
    private void copy(RetentionRun run, HoldEntity hold, String where, MapSqlParameterSource params) {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(HeldVersionsTable.NAME)
                .append(" (hold_id, ").append(COLUMNS).append(") SELECT :holdId, ")
                .append(prefixed()).append(" FROM record_version v").append(where);
        params.addValue("holdId", hold.getId());
        Long lower = holds.lowerSeq(hold, run.jdbc());
        Long upper = holds.upperSeq(hold, run.jdbc(), run.now());
        if (lower != null) {
            sql.append(" AND v.seq >= :holdFrom");
            params.addValue("holdFrom", lower);
        }
        if (upper != null) {
            sql.append(" AND v.seq <= :holdTo");
            params.addValue("holdTo", upper);
        }
        if (!hold.getSources().isEmpty()) {
            sql.append(" AND v.source_id IN (:holdSources)");
            params.addValue("holdSources", hold.getSources());
        }
        if (!hold.getKinds().isEmpty()) {
            sql.append(" AND v.kind IN (:holdKinds)");
            params.addValue("holdKinds", hold.getKinds());
        }
        sql.append(" ON CONFLICT (hold_id, seq) DO NOTHING");
        int copied = run.jdbc().update(sql.toString(), params);
        if (copied > 0) {
            log.info("Hold {} copied {} versions before retention removes them", hold.getId(), copied);
        }
    }

    private static String prefixed() {
        return HistoryDdl.COLUMNS.stream().map(c -> "v." + c.name()).collect(Collectors.joining(", "));
    }
}
