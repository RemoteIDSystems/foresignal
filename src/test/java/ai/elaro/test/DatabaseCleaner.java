package ai.elaro.test;

import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Empties every Elaro table so each test starts from a known state.
 *
 * <p>Truncating rows rather than recreating the schema is deliberate: the schema and the GiST
 * indexes from {@code SchemaBootstrap} are created once per container and must survive, because
 * several cached Spring contexts share one database.
 *
 * <p>Tables are discovered from {@code information_schema} rather than listed, so an entity added
 * in a later prompt is cleaned without anyone remembering to update this class.
 */
public class DatabaseCleaner {

    /**
     * PostGIS owns this table; truncating it would break every spatial query. It is the only
     * table in the public schema that Elaro does not own.
     */
    static final String POSTGIS_REFERENCE_TABLE = "spatial_ref_sys";

    /** Reset alongside the tables, since {@code record_version.seq} is not an identity column. */
    static final String RECORD_SEQUENCE = "record_seq";

    private final JdbcTemplate jdbc;

    public DatabaseCleaner(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Deletes every row from every Elaro table and restarts the identity and record sequences. */
    public void clean() {
        List<String> tables = elaroTables();
        if (!tables.isEmpty()) {
            // One statement so CASCADE resolves foreign keys across the whole set at once.
            String joined = String.join(", ", tables);
            jdbc.execute("TRUNCATE " + joined + " RESTART IDENTITY CASCADE");
        }
        resetRecordSequence();
    }

    /** The Elaro-owned base tables in the public schema. */
    List<String> elaroTables() {
        return jdbc.queryForList(
                """
                select quote_ident(table_name)
                  from information_schema.tables
                 where table_schema = 'public'
                   and table_type = 'BASE TABLE'
                   and table_name <> ?
                 order by table_name
                """,
                String.class,
                POSTGIS_REFERENCE_TABLE);
    }

    private void resetRecordSequence() {
        // The sequence exists only once Hibernate has created the schema; tolerate its absence so
        // a cleaner can run against a database that has no record_version yet.
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
        public DatabaseCleaner databaseCleaner(JdbcTemplate jdbc) {
            return new DatabaseCleaner(jdbc);
        }
    }
}
