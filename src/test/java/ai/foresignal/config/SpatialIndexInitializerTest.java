package ai.foresignal.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pins the statement list of {@link SpatialIndexInitializer} (Core-Data-Model §8). */
class SpatialIndexInitializerTest {

    @Test
    @DisplayName("the statement list is the v1.2 set plus the 017 alert, 036 place and 040 reference statements, in order")
    void statementList() {
        // Checkpoint 041: 036 (places, saved views) and 040 (reference geography, record_region) added their
        // indexes to the initializer without updating this pin.
        assertThat(SpatialIndexInitializer.STATEMENTS)
                .containsExactly(
                        "DROP INDEX IF EXISTS record_head_geom_gist",
                        "CREATE INDEX IF NOT EXISTS record_live_geom_gist ON record_live USING GIST (geom)",
                        "ALTER TABLE record_live SET (fillfactor = 70, autovacuum_vacuum_scale_factor = 0.02,"
                                + " autovacuum_vacuum_cost_limit = 2000)",
                        "CREATE INDEX IF NOT EXISTS trigger_rule_geofence_gist ON trigger_rule USING GIST (geofence)",
                        "CREATE INDEX IF NOT EXISTS place_area_gist ON place USING GIST (area)",
                        "CREATE INDEX IF NOT EXISTS saved_view_place_ids_idx ON saved_view USING GIN (place_ids)",
                        "CREATE SEQUENCE IF NOT EXISTS alert_rev INCREMENT BY 1",
                        "CREATE INDEX IF NOT EXISTS alert_open_idx ON alert (rule_id, source_id, external_id)"
                                + " WHERE state <> 'resolved'",
                        "CREATE INDEX IF NOT EXISTS ref_country_geom_gist ON ref_country USING GIST (geom)",
                        "CREATE INDEX IF NOT EXISTS ref_country_geom_simple_gist ON ref_country USING GIST (geom_simple)",
                        "CREATE INDEX IF NOT EXISTS ref_admin1_geom_gist ON ref_admin1 USING GIST (geom)",
                        "CREATE INDEX IF NOT EXISTS ref_admin1_geom_simple_gist ON ref_admin1 USING GIST (geom_simple)",
                        "CREATE INDEX IF NOT EXISTS ref_admin1_gn_code_idx ON ref_admin1 (gn_code)",
                        "CREATE INDEX IF NOT EXISTS ref_admin2_geom_gist ON ref_admin2 USING GIST (geom)",
                        "CREATE INDEX IF NOT EXISTS ref_sea_geom_gist ON ref_sea USING GIST (geom)",
                        "CREATE INDEX IF NOT EXISTS gazetteer_place_geog_gist ON gazetteer_place USING GIST (geog)",
                        "CREATE INDEX IF NOT EXISTS record_region_iso2_idx ON record_region (iso2)",
                        "CREATE INDEX IF NOT EXISTS record_region_iso2_admin1_idx ON record_region (iso2, admin1)");
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
