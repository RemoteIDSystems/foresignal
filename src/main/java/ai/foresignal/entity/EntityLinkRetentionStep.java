package ai.foresignal.entity;

import ai.foresignal.retention.RetentionRun;
import ai.foresignal.retention.RetentionStep;
import java.sql.Timestamp;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

/**
 * Deletes closed entity links older than {@code retention.entityLinks} (prompt 098). Entity facts are
 * not tied to record retention, so this is their own step; key and alias rows are never deleted.
 */
@Component
public class EntityLinkRetentionStep implements RetentionStep {

    @Override
    public String name() {
        return "entity_links";
    }

    @Override
    public int order() {
        return 650;
    }

    @Override
    public void run(RetentionRun run) {
        long deleted = run.jdbc().update(
                "DELETE FROM entity_link WHERE valid_to IS NOT NULL AND valid_to < :t",
                new MapSqlParameterSource("t", Timestamp.from(run.now().minus(run.settings().entityLinks()))));
        run.deleted("entity_link", deleted);
    }
}
