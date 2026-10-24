package ai.foresignal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import ai.foresignal.store.history.HistoryProperties;
import ai.foresignal.store.history.PartitionManager;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.schema.ApplicationHibernateSettings;
import ai.foresignal.test.schema.PreCutoverSchema;
import ai.foresignal.test.schema.ScratchDatabase;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.tool.schema.internal.ExceptionHandlerLoggedImpl;
import org.hibernate.tool.schema.spi.ContributableMatcher;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.SchemaManagementTool;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.hibernate.autoconfigure.HibernateProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The Flyway migrations on an empty database (prompt 144, Architecture §25): {@code V1__baseline.sql} and every
 * later migration apply in order, Hibernate's {@code validate} accepts the result, {@code record_version} is the
 * tiered partitioned table with its three partitioned indexes and no leaf, the {@link PartitionManager} attaches
 * leaves afterwards, and there is never a DEFAULT partition.
 *
 * <p>Runs against a scratch database in the shared container, migrated once for the class; the application's own
 * database is checked only for its Flyway history.
 */
@IntegrationTest
class FlywayBaselineIT {

    private static ScratchDatabase scratch;
    private static JdbcTemplate admin;
    private static MigrateResult migration;

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JpaProperties jpaProperties;
    @Autowired private HibernateProperties hibernateProperties;
    @Autowired private ObjectProvider<HibernatePropertiesCustomizer> customizers;

    /** Migrates a fresh scratch database once for the whole class. */
    @BeforeEach
    void migrateScratchOnce() {
        if (scratch == null) {
            admin = jdbc;
            scratch = ScratchDatabase.recreate(jdbc, "flyway_baseline_it");
            migration = Flyway.configure()
                    .dataSource(scratch.dataSource())
                    .locations("classpath:db/migration")
                    .load()
                    .migrate();
        }
    }

    /** Drops the scratch database. */
    @AfterAll
    static void dropScratch() {
        if (scratch != null) {
            scratch.drop(admin);
            scratch = null;
        }
    }

    /**
     * The versions of the migrations on the classpath ({@code V1__baseline.sql} and the ones after it), in order.
     */
    static List<Integer> migrationVersions() {
        try {
            Pattern name = Pattern.compile("V(\\d+)__.+\\.sql");
            List<Integer> versions = new ArrayList<>();
            for (Resource r : new PathMatchingResourcePatternResolver().getResources("classpath:db/migration/V*__*.sql")) {
                Matcher m = name.matcher(r.getFilename());
                if (m.matches()) {
                    versions.add(Integer.parseInt(m.group(1)));
                }
            }
            versions.sort(null);
            return versions;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    @DisplayName("the application's database was built by Flyway: every migration applied successfully")
    void applicationDatabaseIsMigrated() {
        List<Integer> versions = migrationVersions();
        assertThat(versions).first().isEqualTo(1);
        for (int version : versions) {
            assertThat(jdbc.queryForObject("select count(*) from flyway_schema_history where version = ? and success",
                    Long.class, String.valueOf(version))).as("V%d", version).isEqualTo(1L);
        }
    }

    @Test
    @DisplayName("an empty database migrates to the latest version by running V1__baseline.sql and every later "
            + "migration in order")
    void emptyDatabaseMigrates() {
        List<Integer> versions = migrationVersions();
        assertThat(versions).as("migrations V1..Vn without gaps")
                .containsExactlyElementsOf(java.util.stream.IntStream.rangeClosed(1, versions.size()).boxed().toList());
        assertThat(migration.success).isTrue();
        assertThat(migration.migrationsExecuted).isEqualTo(versions.size());
        assertThat(migration.targetSchemaVersion).isEqualTo(String.valueOf(versions.getLast()));
        assertThat(scratch.jdbc().queryForList("select extname from pg_extension", String.class))
                .contains("postgis", "vector");
    }

    @Test
    @DisplayName("Hibernate validate accepts the migrated schema for every entity")
    void hibernateValidatePasses() {
        Map<String, Object> settings = new HashMap<>(
                ApplicationHibernateSettings.of(jpaProperties, hibernateProperties, customizers));
        settings.put(AvailableSettings.JAKARTA_NON_JTA_DATASOURCE, scratch.dataSource());
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().applySettings(settings).build();
        try {
            MetadataSources sources = new MetadataSources(registry);
            PreCutoverSchema.entityClasses().forEach(sources::addAnnotatedClass);
            Metadata metadata = sources.buildMetadata();
            ExecutionOptions options =
                    SchemaManagementToolCoordinator.buildExecutionOptions(settings, ExceptionHandlerLoggedImpl.INSTANCE);
            assertThatCode(() -> registry.requireService(SchemaManagementTool.class)
                            .getSchemaValidator(settings)
                            .doValidation(metadata, options, ContributableMatcher.ALL))
                    .doesNotThrowAnyException();
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    @DisplayName("record_version is partitioned by tier into two range-partitioned children, with no leaf yet")
    void recordVersionIsTiered() {
        JdbcTemplate db = scratch.jdbc();
        assertThat(relkind(db, "record_version")).isEqualTo("p");
        List<String> children = db.queryForList("""
                select c.relname || ' ' || c.relkind::text || ' ' || pg_get_expr(c.relpartbound, c.oid)
                  from pg_inherits i
                  join pg_class c on c.oid = i.inhrelid
                 where i.inhparent = 'record_version'::regclass
                 order by c.relname
                """, String.class);
        assertThat(children).containsExactly(
                "record_version_std p FOR VALUES IN ('0')", "record_version_trk p FOR VALUES IN ('1')");
        assertThat(childCount(db, "record_version_std")).isZero();
        assertThat(childCount(db, "record_version_trk")).isZero();
        assertThat(db.queryForObject("""
                select pg_get_constraintdef(oid) from pg_constraint
                 where conrelid = 'record_version'::regclass and contype = 'p'
                """, String.class)).isEqualTo("PRIMARY KEY (seq, history_tier)");
    }

    @Test
    @DisplayName("record_version has exactly the three partitioned indexes besides its primary key")
    void recordVersionHasThreePartitionedIndexes() {
        List<String> indexes = scratch.jdbc().queryForList("""
                select c.relname from pg_index x
                  join pg_class c on c.oid = x.indexrelid
                 where x.indrelid = 'record_version'::regclass and not x.indisprimary and c.relkind = 'I'
                 order by c.relname
                """, String.class);
        assertThat(indexes).containsExactly(
                "record_version_geom_gist",
                "record_version_source_external_seq_idx",
                "record_version_source_observed_idx");
    }

    @Test
    @DisplayName("ensureAhead creates leaves for both tiers after the migration, and never a DEFAULT partition")
    void partitionManagerCreatesLeavesAfterwards() {
        // Own scratch: ensureAhead() attaches leaves, which must not pollute the shared `scratch` that
        // recordVersionIsTiered() asserts is leaf-free (the class shares one migrated scratch, in any method order).
        ScratchDatabase leaves = ScratchDatabase.recreate(jdbc, "flyway_baseline_leaves");
        Flyway.configure().dataSource(leaves.dataSource()).locations("classpath:db/migration").load().migrate();
        JdbcTemplate db = leaves.jdbc();
        PartitionManager partitions =
                new PartitionManager(leaves.dataSource(), new SimpleMeterRegistry(), HistoryProperties.defaults());

        partitions.ensureAhead();

        assertThat(childCount(db, "record_version_std")).isPositive();
        assertThat(childCount(db, "record_version_trk")).isPositive();
        assertThat(db.queryForObject("""
                select count(*) from pg_class c
                 where c.relispartition and pg_get_expr(c.relpartbound, c.oid) = 'DEFAULT'
                """, Long.class)).isZero();
    }

    @Test
    @DisplayName("chronicle_version is a LIST-partitioned parent with no leaf; record_version_held is a plain table")
    void otherHistoryTables() {
        JdbcTemplate db = scratch.jdbc();
        assertThat(relkind(db, "chronicle_version")).isEqualTo("p");
        assertThat(childCount(db, "chronicle_version")).isZero();
        assertThat(relkind(db, "record_version_held")).isEqualTo("r");
    }

    @Test
    @DisplayName("record_live carries the HOT-friendly storage parameters")
    void recordLiveStorageParameters() {
        String options = scratch.jdbc().queryForObject(
                "select array_to_string(reloptions, ',') from pg_class where relname = 'record_live'", String.class);
        assertThat(options).contains("fillfactor=70", "autovacuum_vacuum_scale_factor=0.02",
                "autovacuum_vacuum_cost_limit=2000");
    }

    private static String relkind(JdbcTemplate db, String table) {
        return db.queryForObject(
                "select relkind::text from pg_class c join pg_namespace n on n.oid = c.relnamespace"
                        + " where n.nspname = current_schema() and c.relname = ?",
                String.class, table);
    }

    private static long childCount(JdbcTemplate db, String parent) {
        Long n = db.queryForObject(
                "select count(*) from pg_inherits where inhparent = to_regclass(?)", Long.class, parent);
        return n == null ? 0 : n;
    }
}
