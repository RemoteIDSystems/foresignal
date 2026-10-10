package ai.elaro.test;

import static org.assertj.core.api.Assertions.assertThat;

import ai.elaro.store.AppSettingEntity;
import ai.elaro.store.AppSettingRepository;
import ai.elaro.store.SystemSecretEntity;
import ai.elaro.store.SystemSecretRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Verifies the guarantee {@link IntegrationTest} advertises: every test method starts with empty
 * tables, while the schema and its indexes survive.
 *
 * <p>Method order is fixed because two of these tests are a pair: the first writes rows, the second
 * asserts they are gone.
 */
@IntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseCleanerIT {

    @Autowired private DatabaseCleaner cleaner;
    @Autowired private AppSettingRepository appSettings;
    @Autowired private SystemSecretRepository systemSecrets;
    @Autowired private JdbcTemplate jdbc;

    @Test
    @Order(1)
    @DisplayName("a test can leave rows behind")
    void leavesRowsBehind() {
        appSettings.saveAndFlush(new AppSettingEntity("leftover", "{\"a\":1}", Instant.now()));
        systemSecrets.saveAndFlush(new SystemSecretEntity("leftover", new byte[] {1}));
        jdbc.update("select nextval('record_seq')");

        assertThat(appSettings.count()).isEqualTo(1);
    }

    @Test
    @Order(2)
    @DisplayName("the next test sees none of them")
    void startsFromEmptyTables() {
        assertThat(appSettings.count()).isZero();
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
                        "record_head_geom_gist",
                        "trigger_rule_geofence_gist");

        List<String> tables =
                jdbc.queryForList(
                        """
                        select table_name from information_schema.tables
                         where table_schema = 'public' and table_type = 'BASE TABLE'
                        """,
                        String.class);
        assertThat(tables).contains("record_version", "record_head", "app_setting");
    }

    @Test
    @Order(6)
    @DisplayName("PostGIS's own table is never truncated")
    void leavesSpatialRefSysAlone() {
        long before = countSpatialRefSys();

        cleaner.clean();

        assertThat(countSpatialRefSys()).isEqualTo(before).isGreaterThan(0);
        assertThat(cleaner.elaroTables())
                .isNotEmpty()
                .noneMatch(name -> name.contains(DatabaseCleaner.POSTGIS_REFERENCE_TABLE));
    }

    @Test
    @Order(7)
    @DisplayName("cleaning an already-clean database is a no-op")
    void cleaningTwiceIsSafe() {
        cleaner.clean();
        cleaner.clean();

        assertThat(appSettings.count()).isZero();
    }

    @Test
    @Order(8)
    @DisplayName("the discovered table list covers the data model")
    void discoversEveryElaroTable() {
        assertThat(cleaner.elaroTables())
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

    private long countSpatialRefSys() {
        Long count = jdbc.queryForObject("select count(*) from spatial_ref_sys", Long.class);
        return count == null ? 0 : count;
    }
}
