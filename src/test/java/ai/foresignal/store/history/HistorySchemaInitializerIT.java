package ai.foresignal.store.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.PostgisContainers;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Runs {@link HistorySchemaInitializer} against a scratch database in the shared container, so it
 * can start from layouts the application database never has: empty, and the prompt-002 plain table.
 */
@IntegrationTest
class HistorySchemaInitializerIT {

    private static final String SCRATCH_DB = "history_init_it";

    /** The prompt-002 shape of the two history tables, trimmed to what matters here. */
    private static final String LEGACY_DDL =
            """
            CREATE SEQUENCE IF NOT EXISTS record_seq INCREMENT BY 1;
            CREATE TABLE record_version (
                seq bigint PRIMARY KEY, source_id varchar(64) NOT NULL, external_id varchar(256) NOT NULL,
                version int NOT NULL, kind varchar(64) NOT NULL, observed_at timestamptz NOT NULL,
                ingested_at timestamptz NOT NULL, valid_until timestamptz, geom geometry(Geometry,4326),
                payload jsonb NOT NULL, content_hash char(64) NOT NULL, tombstone boolean NOT NULL,
                CONSTRAINT record_version_source_external_version_key UNIQUE (source_id, external_id, version));
            CREATE TABLE record_head (
                source_id varchar(64) NOT NULL, external_id varchar(256) NOT NULL, seq bigint NOT NULL,
                observed_at timestamptz NOT NULL, content_hash char(64) NOT NULL,
                PRIMARY KEY (source_id, external_id));
            """;

    @Autowired private JdbcTemplate applicationJdbc;

    private JdbcTemplate jdbc;
    private HistorySchemaInitializer initializer;

    @BeforeEach
    void createScratchDatabase() {
        applicationJdbc.execute("DROP DATABASE IF EXISTS " + SCRATCH_DB + " WITH (FORCE)");
        applicationJdbc.execute("CREATE DATABASE " + SCRATCH_DB);
        var dataSource = new DriverManagerDataSource(
                "jdbc:postgresql://" + PostgisContainers.POSTGIS.getHost() + ":"
                        + PostgisContainers.POSTGIS.getMappedPort(5432) + "/" + SCRATCH_DB,
                PostgisContainers.POSTGIS.getUsername(),
                PostgisContainers.POSTGIS.getPassword());
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE EXTENSION IF NOT EXISTS postgis");
        var partitions = new PartitionManager(
                dataSource,
                new SimpleMeterRegistry(),
                new HistoryProperties(1000, 1000, new HistoryProperties.Maintenance(false, Duration.ofMinutes(10))));
        initializer = new HistorySchemaInitializer(dataSource, partitions);
    }

    @AfterEach
    void dropScratchDatabase() {
        applicationJdbc.execute("DROP DATABASE IF EXISTS " + SCRATCH_DB + " WITH (FORCE)");
    }

    @Test
    @DisplayName("an empty database gets the partitioned layout, its indexes and leaves ahead")
    void createsFromNothing() {
        initializer.initialize();

        assertPartitionedLayout();
    }

    @Test
    @DisplayName("an empty plain record_version and record_head are replaced by the partitioned layout")
    void convertsEmptyLegacyTables() {
        jdbc.execute(LEGACY_DDL);

        initializer.afterPropertiesSet();

        assertPartitionedLayout();
        // The head is dropped too, so Hibernate recreates it without the old NOT NULL columns.
        assertThat(jdbc.queryForObject("select to_regclass('record_head') is null", Boolean.class)).isTrue();
        assertThat(jdbc.queryForObject(
                        "select count(*) from pg_constraint where conname = 'record_version_source_external_version_key'",
                        Long.class))
                .isZero();
    }

    @Test
    @DisplayName("a plain record_version without a record_head is converted too")
    void convertsLegacyVersionWithoutHead() {
        jdbc.execute(LEGACY_DDL);
        jdbc.execute("DROP TABLE record_head");

        initializer.initialize();

        assertPartitionedLayout();
    }

    @Test
    @DisplayName("a non-empty plain record_version stops startup with the documented message")
    void refusesPopulatedLegacyTable() {
        jdbc.execute(LEGACY_DDL);
        jdbc.update(
                """
                insert into record_version (seq, source_id, external_id, version, kind, observed_at,
                    ingested_at, payload, content_hash, tombstone)
                values (1, 's', 'e', 1, 'k', now(), now(), '{}'::jsonb, repeat('a', 64), false)
                """);

        assertThatThrownBy(initializer::initialize)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("record_version holds pre-v1.2 data; run docker compose down -v and restart");
        assertThat(relkind()).isEqualTo("r");
    }

    @Test
    @DisplayName("a populated legacy record_head alone also stops startup")
    void refusesPopulatedLegacyHead() {
        jdbc.execute(LEGACY_DDL);
        jdbc.update("insert into record_head values ('s', 'e', 1, now(), repeat('a', 64))");

        assertThatThrownBy(initializer::initialize)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("pre-v1.2 data");
    }

    @Test
    @DisplayName("a second run changes nothing")
    void secondRunIsANoOp() {
        initializer.initialize();
        List<String> before = schemaSnapshot();

        initializer.initialize();

        assertThat(schemaSnapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("ADD COLUMN IF NOT EXISTS restores a dropped nullable column in place")
    void restoresDroppedColumn() {
        initializer.initialize();
        jdbc.execute("ALTER TABLE record_version DROP COLUMN valid_from");
        assertThat(columns()).doesNotContain("valid_from");

        initializer.initialize();

        assertThat(columns()).contains("valid_from");
        // The restored column reaches every leaf.
        String leaf = PartitionManager.leafName(HistoryTier.TRACK, 0);
        assertThat(jdbc.queryForList(
                        "select column_name from information_schema.columns where table_name = ?",
                        String.class,
                        leaf))
                .contains("valid_from");
    }

    // ---------------------------------------------------------------- helpers

    private void assertPartitionedLayout() {
        assertThat(relkind()).isEqualTo("p");
        assertThat(jdbc.queryForObject("select pg_get_partkeydef('record_version'::regclass)", String.class))
                .isEqualTo("LIST (history_tier)");
        assertThat(jdbc.queryForObject("select pg_get_partkeydef('record_version_std'::regclass)", String.class))
                .isEqualTo("RANGE (seq)");
        assertThat(jdbc.queryForObject("select pg_get_partkeydef('record_version_trk'::regclass)", String.class))
                .isEqualTo("RANGE (seq)");
        assertThat(jdbc.queryForList(
                        "select indexname from pg_indexes where tablename = 'record_version'", String.class))
                .containsExactlyInAnyOrder(
                        "record_version_pkey",
                        "record_version_source_external_seq_idx",
                        "record_version_source_observed_idx",
                        "record_version_geom_gist");
        assertThat(columns()).containsExactlyElementsOf(
                HistoryDdl.COLUMNS.stream().map(HistoryDdl.Column::name).toList());
        // Blocks of 1000 from last_value 1: [0,1000) and [1000,2000) per tier, i.e. >= 1.5 blocks.
        assertThat(leafNames("record_version_trk")).containsExactly("rv_t_0", "rv_t_1000");
        assertThat(leafNames("record_version_std")).containsExactly("rv_s_0", "rv_s_1000");
        assertThat(jdbc.queryForObject(
                        "select increment_by from pg_sequences where sequencename = 'record_seq'", Long.class))
                .isEqualTo(1L);
    }

    private String relkind() {
        return jdbc.queryForObject("select relkind::text from pg_class where relname = 'record_version'", String.class);
    }

    private List<String> columns() {
        return jdbc.queryForList(
                "select column_name from information_schema.columns where table_name = 'record_version'"
                        + " order by ordinal_position",
                String.class);
    }

    private List<String> leafNames(String parent) {
        return jdbc.queryForList(
                """
                select c.relname from pg_inherits i
                  join pg_class c on c.oid = i.inhrelid
                  join pg_class p on p.oid = i.inhparent
                 where p.relname = ?
                 order by c.relname
                """,
                String.class,
                parent);
    }

    private List<String> schemaSnapshot() {
        return jdbc.queryForList(
                """
                select c.relname || ':' || c.relkind::text || ':' || coalesce(pg_get_expr(c.relpartbound, c.oid), '')
                  from pg_class c join pg_namespace n on n.oid = c.relnamespace
                 where n.nspname = 'public' and c.relname like 'r%'
                 union all
                select table_name || '.' || column_name || ':' || udt_name || ':' || is_nullable
                  from information_schema.columns where table_schema = 'public' and table_name like 'r%'
                 order by 1
                """,
                String.class);
    }
}
