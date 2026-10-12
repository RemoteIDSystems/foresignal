package ai.foresignal.reference;

import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * The orphan sweep of {@code record_region} (prompt 040, the 019 step mechanism): deletes the tags of
 * records whose head no longer exists, in chunks. Runs after the head purges (order 400); a head purged
 * later is swept on the next run. 039's purge fan-out deletes an entity's row at once.
 */
@Component
public class RegionRetentionStep implements RetentionStep {

    /** Rows deleted per statement. */
    static final int CHUNK = 10_000;

    static final String DELETE_ORPHANS = """
            DELETE FROM record_region WHERE (source_id, external_id) IN (
              SELECT r.source_id, r.external_id FROM record_region r
               WHERE NOT EXISTS (SELECT 1 FROM record_head h
                                  WHERE h.source_id = r.source_id AND h.external_id = r.external_id)
               LIMIT :limit)
            """;

    @Override
    public String name() {
        return "record-region";
    }

    @Override
    public int order() {
        return 460;
    }

    @Override
    public void run(RetentionRun run) {
        long total = 0;
        int deleted;
        do {
            deleted = run.jdbc().update(DELETE_ORPHANS, new MapSqlParameterSource("limit", CHUNK));
            total += deleted;
        } while (deleted == CHUNK);
        run.deleted("record_region", total);
    }
}
