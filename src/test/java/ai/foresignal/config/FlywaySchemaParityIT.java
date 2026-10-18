package ai.foresignal.config;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.schema.ApplicationHibernateSettings;
import ai.foresignal.test.schema.PreCutoverSchema;
import ai.foresignal.test.schema.SchemaSnapshot;
import ai.foresignal.test.schema.ScratchDatabase;
import java.util.List;
import java.util.regex.Pattern;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.hibernate.autoconfigure.HibernateProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The Flyway baseline against the schema the pre-1.0 code built (prompt 144): schema A comes from Flyway on an empty
 * database, schema B from the pre-cutover generator ({@link PreCutoverSchema}: the old bootstrap SQL around Hibernate's
 * {@code update}). Columns (name, type, nullability, default, identity, generation), key constraints, indexes and
 * sequences must be equal, apart from the explicit allowlist below.
 *
 * <p>{@link SchemaSnapshot} already leaves out what is not meant to agree: column order, constraint and index names,
 * and the CHECK constraints Hibernate generates for enum columns (the baseline has none, by design).
 */
@IntegrationTest
class FlywaySchemaParityIT {

    /**
     * Known, accepted differences, each a regular expression over a whole difference line ({@code "- "} only in the
     * Flyway schema, {@code "+ "} only in the pre-cutover schema), with the reason.
     */
    static final List<Pattern> ALLOWLIST = List.of(
            // Empty at 1.0.0: nothing is accepted yet. Every remaining difference is a defect in V1__baseline.sql (fix it
            // in a new migration once V1 has been applied anywhere) or an entity; add an entry here only with its reason.
            );

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JpaProperties jpaProperties;
    @Autowired private HibernateProperties hibernateProperties;
    @Autowired private ObjectProvider<HibernatePropertiesCustomizer> customizers;

    @Test
    @DisplayName("the Flyway baseline equals the pre-cutover Hibernate update plus bootstrap SQL")
    void baselineMatchesThePreCutoverSchema() {
        ScratchDatabase flyway = ScratchDatabase.recreate(jdbc, "parity_flyway");
        ScratchDatabase legacy = ScratchDatabase.recreate(jdbc, "parity_pre_cutover");
        try {
            Flyway.configure().dataSource(flyway.dataSource()).locations("classpath:db/migration").load().migrate();
            List<Exception> hibernateErrors = PreCutoverSchema.build(legacy.dataSource(),
                    ApplicationHibernateSettings.of(jpaProperties, hibernateProperties, customizers));
            assertThat(hibernateErrors).as("Hibernate update errors while building the pre-cutover schema").isEmpty();

            SchemaSnapshot a = SchemaSnapshot.read(flyway.jdbc());
            SchemaSnapshot b = SchemaSnapshot.read(legacy.jdbc());

            assertThat(a.columns()).as("the baseline creates tables").isNotEmpty();
            assertThat(a.differences(b, ALLOWLIST))
                    .as("differences between V1__baseline.sql (-) and the pre-cutover schema (+)")
                    .isEmpty();
        } finally {
            flyway.drop(jdbc);
            legacy.drop(jdbc);
        }
    }

    @Test
    @DisplayName("the pre-cutover scripts split into the statements the old initializers ran")
    void preCutoverScriptsSplit() {
        assertThat(PreCutoverSchema.statements(PreCutoverSchema.BEFORE_HIBERNATE))
                .first().asString().contains("CREATE EXTENSION IF NOT EXISTS postgis");
        assertThat(PreCutoverSchema.statements(PreCutoverSchema.BEFORE_HIBERNATE))
                .anyMatch(sql -> sql.contains("PARTITION BY LIST (history_tier)"))
                .anyMatch(sql -> sql.contains("PARTITION BY LIST (chronicle_id)"))
                .anyMatch(sql -> sql.contains("record_version_held"));
        assertThat(PreCutoverSchema.statements(PreCutoverSchema.AFTER_HIBERNATE))
                .hasSize(43)
                .anyMatch(sql -> sql.contains("record_live SET (fillfactor = 70"))
                .anyMatch(sql -> sql.contains("embedding_document ADD COLUMN IF NOT EXISTS tsv"));
    }
}
