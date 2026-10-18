package ai.foresignal.test;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.store.AppSettingEntity;
import ai.foresignal.store.AppSettingRepository;
import ai.foresignal.store.SystemSecretEntity;
import ai.foresignal.store.SystemSecretRepository;
import ai.foresignal.store.history.LeafInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Verifies the guarantee {@link IntegrationTest} advertises: every test method starts with empty
 * tables, while the schema, its indexes, every partition child and Flyway's history survive.
 *
 * <p>Method order is fixed because two of these tests are a pair: the first writes rows, the second
 * asserts they are gone.
 */
@IntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseCleanerIT {

    @Autowired private DatabaseCleaner cleaner;
    @Autowired private AppSettingRepository appSettings;
    @Autowired private AppSettingService settingService;
    @Autowired private SystemSecretRepository systemSecrets;
    @Autowired private JdbcTemplate jdbc;

    @Test
    @Order(1)
    @DisplayName("a test can leave rows behind")
    void leavesRowsBehind() {
        appSettings.saveAndFlush(new AppSettingEntity("leftover", "{\"a\":1}", Instant.now()));
        systemSecrets.saveAndFlush(new SystemSecretEntity("leftover", new byte[] {1}));
        jdbc.queryForObject("select nextval('record_seq')", Long.class);

        assertThat(appSettings.existsById("leftover")).isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("the next test sees none of them")
    void startsFromEmptyTables() {
        // The cleaner's IngestStateReset beans run after the truncation, and the trigger engine
        // re-seeds its durable cursor (app_setting 'trigger.cursor', Core-Data-Model §13 consumer
        // clamp) when it reloads. So the leftover rows are gone, but app_setting is not empty
        // (prompt 022: these assertions counted every row before 017 added the cursor). Since 040 the region tag
        // worker re-seeds 'region.cursor' the same way (checkpoint 041).
        assertThat(appSettings.findAll()).extracting(AppSettingEntity::getKey).doesNotContain("leftover")
                .allMatch(key -> key.equals(SettingKeys.TRIGGER_CURSOR) || key.equals(SettingKeys.REGION_CURSOR));
        assertThat(systemSecrets.count()).isZero();
    }

    @Test
    @Order(3)
    @DisplayName("record_seq restarts at 1 after cleaning")
    void recordSequenceRestarts() {
        assertThat(jdbc.queryForObject("select nextval('record_seq')", Long.class)).isEqualTo(1L);
        assertThat(jdbc.queryForObject("select nextval('record_seq')", Long.class)).isEqualTo(2L);

        cleaner.clean();

        assertThat(jdbc.queryForObject("select nextval('record_seq')", Long.class)).isEqualTo(1L);
    }

    @Test
    @Order(4)
    @DisplayName("identity columns restart, so alert ids are predictable")
    void identityColumnsRestart() {
        Long first =
                jdbc.queryForObject(
                        """
                        insert into alert (rule_id, seq, source_id, external_id, fired_at)
                        values (gen_random_uuid(), 1, 's', 'e', now()) returning id
                        """,
                        Long.class);

        assertThat(first).isEqualTo(1L);
    }

    @Test
    @Order(5)
    @DisplayName("cleaning leaves the schema and the GiST indexes in place")
    void preservesSchemaAndIndexes() {
        cleaner.clean();

        List<String> indexes =
                jdbc.queryForList(
                        "select indexname from pg_indexes where schemaname = 'public'",
                        String.class);
        assertThat(indexes)
                .contains(
                        "record_version_geom_gist",
                        "record_live_geom_gist",
                        "trigger_rule_geofence_gist");

        List<String> tables =
                jdbc.queryForList(
                        """
                        select table_name from information_schema.tables
                         where table_schema = 'public' and table_type = 'BASE TABLE'
                        """,
                        String.class);
        assertThat(tables).contains("record_version", "record_head", "record_live", "app_setting");
    }

    @Test
    @Order(6)
    @DisplayName("PostGIS's own table is never truncated")
    void leavesSpatialRefSysAlone() {
        long before = countSpatialRefSys();

        cleaner.clean();

        assertThat(countSpatialRefSys()).isEqualTo(before).isGreaterThan(0);
        assertThat(cleaner.foresignalTables())
                .isNotEmpty()
                .noneMatch(name -> name.contains(DatabaseCleaner.POSTGIS_REFERENCE_TABLE));
    }

    @Test
    @Order(7)
    @DisplayName("cleaning an already-clean database is a no-op")
    void cleaningTwiceIsSafe() {
        cleaner.clean();
        cleaner.clean();

        // Only the re-seeded trigger and region cursors remain (see startsFromEmptyTables).
        assertThat(appSettings.findAll()).extracting(AppSettingEntity::getKey)
                .allMatch(key -> key.equals(SettingKeys.TRIGGER_CURSOR) || key.equals(SettingKeys.REGION_CURSOR));
    }

    @Test
    @Order(8)
    @DisplayName("the discovered table list covers the data model")
    void discoversEveryForesignalTable() {
        assertThat(cleaner.foresignalTables())
                .contains(
                        "source_settings",
                        "source_credential",
                        "source_health",
                        "source_usage",
                        "record_version",
                        "record_head",
                        "record_live",
                        "fetch_log",
                        "seq_clock",
                        "trigger_rule",
                        "alert",
                        // 018 replaced webhook/webhook_delivery with the channel tables (prompt 022).
                        "notification_channel",
                        "notification_delivery",
                        "notification",
                        "retention_state",
                        "app_setting",
                        "system_secret");
    }

    private long countSpatialRefSys() {
        Long count = jdbc.queryForObject("select count(*) from spatial_ref_sys", Long.class);
        return count == null ? 0 : count;
    }

    @Test
    @Order(9)
    @DisplayName("partitions are never listed for truncation on their own")
    void leavesAreNotTruncatedIndividually() {
        assertThat(cleaner.foresignalTables())
                .contains("record_version")
                .doesNotContain("record_version_std", "record_version_trk")
                .noneMatch(name -> name.startsWith("rv_"));
    }

    @Test
    @Order(10)
    @DisplayName("cleaning empties record_version through its parent and keeps every partition")
    void partitionsSurviveCleaning() {
        List<LeafInfo> trackBefore = TestPartitions.leaves(jdbc, HistoryTier.TRACK);
        List<LeafInfo> standardBefore = TestPartitions.leaves(jdbc, HistoryTier.STANDARD);
        assertThat(trackBefore).isNotEmpty();
        assertThat(standardBefore).isNotEmpty();
        jdbc.update(
                """
                insert into record_version (seq, ingested_at, observed_at, received_at, version,
                    history_tier, persist_reason, qa_flags, tombstone, source_id, external_id, kind, payload)
                values (nextval('record_seq'), now(), now(), now(), 1, 1, 0, 0, false, 's', 'e', 'k', '{}'::jsonb)
                """);
        assertThat(jdbc.queryForObject("select count(*) from record_version", Long.class)).isEqualTo(1L);

        cleaner.clean();

        assertThat(jdbc.queryForObject("select count(*) from record_version", Long.class)).isZero();
        assertThat(TestPartitions.leaves(jdbc, HistoryTier.TRACK)).isEqualTo(trackBefore);
        assertThat(TestPartitions.leaves(jdbc, HistoryTier.STANDARD)).isEqualTo(standardBefore);
        assertThat(jdbc.queryForObject(
                        "select relkind::text from pg_class where relname = 'record_version'", String.class))
                .isEqualTo("p");
    }

    @Test
    @Order(11)
    @DisplayName("every IngestStateReset bean is reset after the tables are truncated")
    void ingestStateResetBeansAreInvoked() {
        List<String> calls = new ArrayList<>();
        StaticListableBeanFactory beans = new StaticListableBeanFactory();
        beans.addBean("first", (IngestStateReset) () -> calls.add(
                "first:" + jdbc.queryForObject("select count(*) from app_setting", Long.class)));
        beans.addBean("second", (IngestStateReset) () -> calls.add("second"));
        appSettings.saveAndFlush(new AppSettingEntity("pending", "{}", Instant.now()));

        new DatabaseCleaner(jdbc, beans.getBeanProvider(IngestStateReset.class), settingService).clean();

        // The reset runs after truncation: the first bean saw an empty table.
        assertThat(calls).containsExactlyInAnyOrder("first:0", "second");
    }

    @Test
    @Order(12)
    @DisplayName("with no IngestStateReset beans, cleaning still works")
    void cleaningWithoutResetBeans() {
        StaticListableBeanFactory none = new StaticListableBeanFactory();

        new DatabaseCleaner(jdbc, none.getBeanProvider(IngestStateReset.class), settingService).clean();

        assertThat(appSettings.count()).isZero();
    }

    @Test
    @Order(13)
    @DisplayName("Flyway's history table is never listed and its rows survive cleaning (prompt 144)")
    void flywayHistorySurvivesCleaning() {
        Long before = jdbc.queryForObject("select count(*) from " + DatabaseCleaner.FLYWAY_HISTORY_TABLE, Long.class);
        assertThat(before).isNotNull().isGreaterThan(0L);

        cleaner.clean();

        assertThat(cleaner.foresignalTables()).doesNotContain(DatabaseCleaner.FLYWAY_HISTORY_TABLE);
        assertThat(jdbc.queryForObject("select count(*) from " + DatabaseCleaner.FLYWAY_HISTORY_TABLE, Long.class))
                .isEqualTo(before);
        assertThat(jdbc.queryForObject(
                        "select count(*) from " + DatabaseCleaner.FLYWAY_HISTORY_TABLE
                                + " where version = '1' and success",
                        Long.class))
                .isEqualTo(1L);
    }

    @Test
    @Order(14)
    @DisplayName("partition children of chronicle_version are skipped too; only the parent is listed")
    void chroniclePartitionChildrenAreSkipped() {
        assertThat(cleaner.foresignalTables())
                .contains("chronicle_version")
                .noneMatch(name -> name.startsWith("chronicle_version_"));
    }
}
