package ai.foresignal.config;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/** Verifies the schema Hibernate cannot create for itself (Core-Data-Model §9). */
@IntegrationTest
class SchemaBootstrapIT {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private SpatialIndexInitializer spatialIndexInitializer;

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Test
    @DisplayName("the EntityManagerFactory depends on the PostGIS and history-schema initializers, so they run first")
    void entityManagerFactoryWaitsForTheInitializers() {
        String[] dependsOn = beanFactory.getBeanDefinition("entityManagerFactory").getDependsOn();

        assertThat(dependsOn)
                .contains(SchemaBootstrap.POSTGIS_INITIALIZER_BEAN, SchemaBootstrap.HISTORY_INITIALIZER_BEAN);
    }

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
                        "record_live_geom_gist",
                        "trigger_rule_geofence_gist")
                .doesNotContain("record_head_geom_gist");
    }

    @Test
    @DisplayName("the alert revision sequence and the partial index on open alerts exist (017)")
    void alertStatements() {
        assertThat(indexNames()).contains("alert_open_idx", "alert_rule_entity_observed_idx", "alert_rev_idx");
        String definition = jdbc.queryForObject(
                "select indexdef from pg_indexes where schemaname = 'public' and indexname = 'alert_open_idx'", String.class);
        assertThat(definition).contains("WHERE").contains("resolved");
        assertThat(jdbc.queryForObject(
                "select count(*) from pg_sequences where schemaname = 'public' and sequencename = 'alert_rev'", Integer.class))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("record_version owns its three partitioned indexes and nothing else")
    void recordVersionIndexes() {
        List<String> parentIndexes =
                jdbc.queryForList(
                        "select indexname from pg_indexes where schemaname = 'public' and tablename = 'record_version'",
                        String.class);

        assertThat(parentIndexes)
                .containsExactlyInAnyOrder(
                        "record_version_pkey",
                        "record_version_source_external_seq_idx",
                        "record_version_source_observed_idx",
                        "record_version_geom_gist");
    }

    @Test
    @DisplayName("record_version is partitioned by tier, then by seq range, with no DEFAULT partition")
    void recordVersionIsPartitioned() {
        assertThat(jdbc.queryForObject(
                        "select relkind::text from pg_class where relname = 'record_version'", String.class))
                .isEqualTo("p");
        assertThat(jdbc.queryForObject(
                        "select pg_get_partkeydef('record_version'::regclass)", String.class))
                .isEqualTo("LIST (history_tier)");
        for (String parent : List.of("record_version_std", "record_version_trk")) {
            assertThat(jdbc.queryForObject(
                            "select pg_get_partkeydef(?::regclass)", String.class, parent))
                    .isEqualTo("RANGE (seq)");
            assertThat(jdbc.queryForObject(
                            "select partdefid from pg_partitioned_table where partrelid = ?::regclass",
                            Long.class,
                            parent))
                    .isZero();
        }
        assertThat(jdbc.queryForObject(
                        "select pg_get_expr(relpartbound, oid) from pg_class where relname = 'record_version_trk'",
                        String.class))
                .isEqualTo("FOR VALUES IN ('1')");
    }

    @Test
    @DisplayName("startup attached at least 1.5 blocks of leaves per tier")
    void leavesExistAhead() {
        for (var tier : ai.foresignal.source.spi.HistoryTier.values()) {
            var leaves = ai.foresignal.test.TestPartitions.leaves(jdbc, tier);
            long block = ai.foresignal.store.history.HistoryProperties.defaults().block(tier);
            assertThat(leaves).isNotEmpty();
            long covered = leaves.get(leaves.size() - 1).toSeq() - leaves.get(0).fromSeq();
            assertThat(covered).isGreaterThanOrEqualTo(block + block / 2);
        }
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
                        "record_version_std",
                        "record_version_trk",
                        "record_head",
                        "record_live",
                        "fetch_log",
                        "seq_clock",
                        "trigger_rule",
                        "alert",
                        // 018 replaced webhook/webhook_delivery with the channel tables; 019 added
                        // retention_state (prompt 022 brought this list up to date).
                        "notification_channel",
                        "notification_delivery",
                        "notification",
                        "retention_state",
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
