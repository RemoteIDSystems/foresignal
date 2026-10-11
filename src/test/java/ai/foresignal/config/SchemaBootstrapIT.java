package ai.foresignal.config;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/** Verifies the two things Hibernate cannot create for itself (Architecture §7.3). */
@IntegrationTest
class SchemaBootstrapIT {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private SpatialIndexInitializer spatialIndexInitializer;

    @Test
    @DisplayName("the PostGIS extension is installed")
    void postgisExtensionExists() {
        Boolean present =
                jdbc.queryForObject(
                        "select exists (select 1 from pg_extension where extname = 'postgis')",
                        Boolean.class);

        assertThat(present).isTrue();
    }

    @Test
    @DisplayName("PostGIS functions are actually callable, not just registered")
    void postgisFunctionsWork() {
        Integer srid =
                jdbc.queryForObject(
                        "select ST_SRID(ST_SetSRID(ST_MakePoint(1, 2), 4326))", Integer.class);

        assertThat(srid).isEqualTo(4326);
    }

    @Test
    @DisplayName("every GiST index from the bootstrap list exists")
    void gistIndexesExist() {
        assertThat(indexNames())
                .contains(
                        "record_version_geom_gist",
                        "record_head_geom_gist",
                        "trigger_rule_geofence_gist");
    }

    @Test
    @DisplayName("the spatial indexes really are GiST")
    void spatialIndexesUseGist() {
        List<String> definitions =
                jdbc.queryForList(
                        "select indexdef from pg_indexes where schemaname = 'public' and indexname like '%_gist'",
                        String.class);

        assertThat(definitions).isNotEmpty().allMatch(def -> def.toLowerCase().contains("using gist"));
    }

    @Test
    @DisplayName("running the bootstrap again changes nothing")
    void secondRunIsANoOp() {
        List<String> before = indexNames();

        spatialIndexInitializer.execute();
        spatialIndexInitializer.execute();

        assertThat(indexNames()).isEqualTo(before);
    }

    @Test
    @DisplayName("record_seq exists and increments by one")
    void recordSequenceIncrementsByOne() {
        Long increment =
                jdbc.queryForObject(
                        "select increment_by from pg_sequences where schemaname = 'public' and sequencename = 'record_seq'",
                        Long.class);

        assertThat(increment).isEqualTo(1L);
    }

    @Test
    @DisplayName("every table from the data model was created")
    void allTablesExist() {
        List<String> tables =
                jdbc.queryForList(
                        """
                        select table_name from information_schema.tables
                         where table_schema = 'public' and table_type = 'BASE TABLE'
                        """,
                        String.class);

        assertThat(tables)
                .contains(
                        "source_settings",
                        "source_credential",
                        "source_health",
                        "source_usage",
                        "record_version",
                        "record_head",
                        "trigger_rule",
                        "alert",
                        "webhook",
                        "webhook_delivery",
                        "app_setting",
                        "system_secret");
    }

    @Test
    @DisplayName("no migration tool created a history table")
    void noMigrationHistoryTable() {
        List<String> tables =
                jdbc.queryForList(
                        "select table_name from information_schema.tables where table_schema = 'public'",
                        String.class);

        assertThat(tables)
                .doesNotContain("flyway_schema_history", "databasechangelog", "databasechangeloglock");
    }

    private List<String> indexNames() {
        return jdbc.queryForList(
                "select indexname from pg_indexes where schemaname = 'public' order by indexname",
                String.class);
    }
}
