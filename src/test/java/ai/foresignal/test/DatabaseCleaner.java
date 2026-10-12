package ai.foresignal.test;

import ai.foresignal.ingest.IngestStateReset;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Empties every Foresignal table so each test starts from a known state.
 *
 * <p>Truncating rows rather than recreating the schema is deliberate: the schema and the GiST
 * indexes from {@code SchemaBootstrap} are created once per container and must survive, because
 * several cached Spring contexts share one database.
 *
 * <p>Tables are discovered from {@code information_schema} rather than listed, so an entity added
 * in a later prompt is cleaned without anyone remembering to update this class. Partitions of
 * {@code record_version} (the tier parents and every seq-range leaf) are left out of that list:
 * {@code TRUNCATE record_version} recurses into them, and the leaves themselves must survive.
 *
 * <p>After truncating, every {@link IngestStateReset} bean reloads its cached state, because the
 * ingest writer's allocator and clock state would otherwise point at rows that no longer exist.
 */
public class DatabaseCleaner {

    /**
     * PostGIS owns this table; truncating it would break every spatial query. It is the only
     * table in the public schema that Foresignal does not own.
     */
    static final String POSTGIS_REFERENCE_TABLE = "spatial_ref_sys";

    /** Reset alongside the tables, since {@code record_version.seq} is not an identity column. */
    static final String RECORD_SEQUENCE = "record_seq";

    private final JdbcTemplate jdbc;
    private final ObjectProvider<IngestStateReset> stateResets;

    /** Creates a cleaner that resets the given {@link IngestStateReset} beans after each clean. */
    public DatabaseCleaner(JdbcTemplate jdbc, ObjectProvider<IngestStateReset> stateResets) {
        this.jdbc = jdbc;
        this.stateResets = stateResets;
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
     * The Foresignal-owned base tables in the public schema, excluding every table that is a
     * partition of another ({@code pg_inherits} child).
     */
    List<String> foresignalTables() {
        return jdbc.queryForList(
                """
                select quote_ident(t.table_name)
                  from information_schema.tables t
                 where t.table_schema = 'public'
                   and t.table_type = 'BASE TABLE'
                   and t.table_name <> ?
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
                POSTGIS_REFERENCE_TABLE);
    }

    private void resetRecordSequence() {
        // The sequence exists once the history bootstrap has run; tolerate its absence so a cleaner
        // can run against a database that has no record_version yet.
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
                JdbcTemplate jdbc, ObjectProvider<IngestStateReset> stateResets) {
            return new DatabaseCleaner(jdbc, stateResets);
        }
    }
}
