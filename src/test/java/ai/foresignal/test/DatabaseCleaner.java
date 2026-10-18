package ai.foresignal.test;

import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.settings.AppSettingService;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Empties every Foresignal table so each test starts from a known state.
 *
 * <p>Truncating rows rather than recreating the schema is deliberate: Flyway builds the schema once per container
 * (prompt 144) and it must survive, because several cached Spring contexts share one database. Flyway's own
 * {@code flyway_schema_history} is never truncated: emptying it would make the next context's Flyway see a non-empty
 * schema with no history and adopt it at version 1 instead of recognizing it as migrated.
 *
 * <p>Tables are discovered from {@code information_schema} rather than listed, so an entity added
 * in a later prompt is cleaned without anyone remembering to update this class. Partitions of
 * {@code record_version} (the tier parents and every seq-range leaf) are left out of that list:
 * {@code TRUNCATE record_version} recurses into them, and the leaves themselves must survive.
 *
 * <p>After truncating, every {@link IngestStateReset} bean reloads its cached state, because the
 * ingest writer's allocator and clock state would otherwise point at rows that no longer exist.
 *
 * <p>The {@link AppSettingService} cache is invalidated too: it is an in-memory copy of
 * {@code app_setting} rows that the truncate removes, and a setting a previous test wrote (for
 * example {@code security.openReads}) would otherwise leak into the next test in a shared context.
 */
public class DatabaseCleaner {

    /**
     * PostGIS owns this table; truncating it would break every spatial query. It is the only
     * table in the public schema that Foresignal does not own.
     */
    static final String POSTGIS_REFERENCE_TABLE = "spatial_ref_sys";

    /** Flyway's history table (the default {@code spring.flyway.table}); its rows must survive every clean. */
    static final String FLYWAY_HISTORY_TABLE = "flyway_schema_history";

    /** Reset alongside the tables, since {@code record_version.seq} is not an identity column. */
    static final String RECORD_SEQUENCE = "record_seq";

    private final JdbcTemplate jdbc;
    private final ObjectProvider<IngestStateReset> stateResets;
    private final AppSettingService appSettings;

    /** Creates a cleaner that resets the given {@link IngestStateReset} beans after each clean. */
    public DatabaseCleaner(JdbcTemplate jdbc, ObjectProvider<IngestStateReset> stateResets,
            AppSettingService appSettings) {
        this.jdbc = jdbc;
        this.stateResets = stateResets;
        this.appSettings = appSettings;
    }

    /**
     * Deletes every row from every Foresignal table, restarts the identity and record sequences,
     * then resets every {@link IngestStateReset} bean.
     */
    public void clean() {
        List<String> tables = foresignalTables();
        if (!tables.isEmpty()) {
            // One statement so CASCADE resolves foreign keys across the whole set at once.
            String joined = String.join(", ", tables);
            truncate("TRUNCATE " + joined + " RESTART IDENTITY CASCADE");
        }
        resetRecordSequence();
        // Drop the cached settings and let dependent filters reload from the truncated table.
        appSettings.invalidateCache();
        stateResets.orderedStream().forEach(IngestStateReset::resetState);
    }

    /**
     * Runs the TRUNCATE, retrying when PostgreSQL picks it as a deadlock victim (40P01). Background workers of the
     * cached contexts (the region tag worker of prompt 040, the trigger and embedding workers) read these tables
     * between tests, and TRUNCATE's ACCESS EXCLUSIVE locks taken table by table can cycle with their share locks
     * (checkpoint 041: an AskActionsIT clean failed with "deadlock detected").
     */
    private void truncate(String sql) {
        for (int attempt = 1; ; attempt++) {
            try {
                jdbc.execute(sql);
                return;
            } catch (org.springframework.dao.PessimisticLockingFailureException e) {
                if (attempt >= 5) {
                    throw e;
                }
                try {
                    Thread.sleep(50L * attempt);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
    }

    /**
     * The Foresignal-owned base tables in the public schema, excluding Flyway's history table and every table that
     * is a partition of another ({@code pg_inherits} child).
     */
    List<String> foresignalTables() {
        return jdbc.queryForList(
                """
                select quote_ident(t.table_name)
                  from information_schema.tables t
                 where t.table_schema = 'public'
                   and t.table_type = 'BASE TABLE'
                   and t.table_name not in (?, ?)
                   and not exists (
                       select 1
                         from pg_inherits i
                         join pg_class c on c.oid = i.inhrelid
                         join pg_namespace n on n.oid = c.relnamespace
                        where n.nspname = t.table_schema
                          and c.relname = t.table_name)
                 order by t.table_name
                """,
                String.class,
                POSTGIS_REFERENCE_TABLE,
                FLYWAY_HISTORY_TABLE);
    }

    private void resetRecordSequence() {
        // The Flyway baseline creates the sequence; tolerate its absence so a cleaner can run against a database
        // that has not been migrated.
        Boolean present =
                jdbc.queryForObject(
                        "select exists (select 1 from pg_sequences where schemaname = 'public' and sequencename = ?)",
                        Boolean.class,
                        RECORD_SEQUENCE);
        if (Boolean.TRUE.equals(present)) {
            jdbc.execute("ALTER SEQUENCE " + RECORD_SEQUENCE + " RESTART WITH 1");
        }
    }

    /**
     * Supplies the cleaner to an integration-test context. Imported by {@link IntegrationTest}
     * rather than component-scanned, so there is exactly one definition.
     */
    @TestConfiguration
    public static class TestConfig {

        @Bean
        public DatabaseCleaner databaseCleaner(
                JdbcTemplate jdbc, ObjectProvider<IngestStateReset> stateResets, AppSettingService appSettings) {
            return new DatabaseCleaner(jdbc, stateResets, appSettings);
        }
    }
}
