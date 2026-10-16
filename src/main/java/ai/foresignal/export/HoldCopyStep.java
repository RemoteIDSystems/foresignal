package ai.foresignal.export;

import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import ai.foresignal.store.history.HistoryDdl;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * Step 0 of retention (prompt 111): archive-then-delete for legal holds. Before any delete or drop
 * runs, every row that falls inside an active hold is copied into
 * {@link ExportSchemaInitializer#HELD_TABLE} with {@code INSERT … ON CONFLICT DO NOTHING}. Retention
 * still drops leaves and deletes versions; the copies survive independently, so {@code floorSeq} and
 * I7 are unchanged. If a copy fails, the hold's sources are skipped this run (no head purge), the
 * same failure rule as the delete steps.
 */
@Component
public class HoldCopyStep implements RetentionStep {

    private static final Logger log = LoggerFactory.getLogger(HoldCopyStep.class);

    private final HoldService holds;

    /** Creates the step. */
    public HoldCopyStep(HoldService holds) {
        this.holds = holds;
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
        Instant now = run.now();
        List<HoldEntity> active = holds.active(now);
        if (active.isEmpty()) {
            return;
        }
        // Everything below this cutoff is about to be deleted or dropped this run.
        long cutoff = run.rawCutoff(run.largestStandardWindow());
        for (HoldEntity hold : active) {
            try {
                copy(run, hold, cutoff);
            } catch (RuntimeException e) {
                log.warn("Could not copy rows held by {}; skipping its sources this run", hold.getId(), e);
                if (hold.getSources() != null) {
                    for (String source : hold.getSources()) {
                        run.skip(source);
                    }
                }
                run.error("hold copy " + hold.getId() + ": " + e.getMessage());
            }
        }
    }

    private void copy(RetentionRun run, HoldEntity hold, long cutoff) {
        String columns = HistoryDdl.COLUMNS.stream().map(HistoryDdl.Column::name)
                .collect(Collectors.joining(", "));
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(ExportSchemaInitializer.HELD_TABLE)
                .append(" (hold_id, ").append(columns).append(") SELECT :holdId, ").append(columns)
                .append(" FROM record_version v WHERE v.seq < :cutoff");
        MapSqlParameterSource params = new MapSqlParameterSource("holdId", hold.getId())
                .addValue("cutoff", cutoff);
        if (hold.getFromSeq() != null) {
            sql.append(" AND v.seq >= :fromSeq");
            params.addValue("fromSeq", hold.getFromSeq());
        }
        if (hold.getToSeq() != null) {
            sql.append(" AND v.seq <= :toSeq");
            params.addValue("toSeq", hold.getToSeq());
        }
        if (hold.getSources() != null && !hold.getSources().isEmpty()) {
            sql.append(" AND v.source_id = ANY(CAST(:sources AS text[]))");
            params.addValue("sources", ai.foresignal.store.query.PgArrays.text(hold.getSources()));
        }
        if (hold.getKinds() != null && !hold.getKinds().isEmpty()) {
            sql.append(" AND v.kind = ANY(CAST(:kinds AS text[]))");
            params.addValue("kinds", ai.foresignal.store.query.PgArrays.text(hold.getKinds()));
        }
        sql.append(" ON CONFLICT (hold_id, seq) DO NOTHING");
        int copied = run.jdbc().update(sql.toString(), params);
        if (copied > 0) {
            log.info("Hold {} copied {} version rows before retention", hold.getId(), copied);
        }
    }
}
