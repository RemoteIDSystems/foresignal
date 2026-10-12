package ai.foresignal.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pins the statement list of {@link SpatialIndexInitializer} (Core-Data-Model §8). */
class SpatialIndexInitializerTest {

    @Test
    @DisplayName("the statement list is exactly the v1.2 set plus the 017 alert statements, in order")
    void statementList() {
        assertThat(SpatialIndexInitializer.STATEMENTS)
                .containsExactly(
                        "DROP INDEX IF EXISTS record_head_geom_gist",
                        "CREATE INDEX IF NOT EXISTS record_live_geom_gist ON record_live USING GIST (geom)",
                        "ALTER TABLE record_live SET (fillfactor = 70, autovacuum_vacuum_scale_factor = 0.02,"
                                + " autovacuum_vacuum_cost_limit = 2000)",
                        "CREATE INDEX IF NOT EXISTS trigger_rule_geofence_gist ON trigger_rule USING GIST (geofence)",
                        "CREATE SEQUENCE IF NOT EXISTS alert_rev INCREMENT BY 1",
                        "CREATE INDEX IF NOT EXISTS alert_open_idx ON alert (rule_id, source_id, external_id)"
                                + " WHERE state <> 'resolved'");
    }

    @Test
    @DisplayName("record_version is left to the history initializer, which owns its DDL")
    void noRecordVersionStatement() {
        assertThat(SpatialIndexInitializer.STATEMENTS).noneMatch(sql -> sql.contains("record_version"));
    }

    @Test
    @DisplayName("record_live gets no index on observed_at, so touches stay HOT")
    void noObservedAtIndex() {
        assertThat(SpatialIndexInitializer.STATEMENTS).noneMatch(sql -> sql.contains("observed_at"));
    }

    @Test
    @DisplayName("every statement is safe to repeat")
    void statementsAreIdempotent() {
        assertThat(SpatialIndexInitializer.STATEMENTS)
                .allMatch(sql -> sql.contains("IF NOT EXISTS") || sql.contains("IF EXISTS") || sql.contains(" SET ("));
    }
}
