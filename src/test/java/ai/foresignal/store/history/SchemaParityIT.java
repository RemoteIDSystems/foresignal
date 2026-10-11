package ai.foresignal.store.history;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.store.RecordVersionEntity;
import ai.foresignal.test.IntegrationTest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.sql.DataSource;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.tool.schema.internal.ExceptionHandlerCollectingImpl;
import org.hibernate.tool.schema.spi.ContributableMatcher;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.SchemaManagementTool;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
import org.hibernate.tool.schema.spi.ScriptTargetOutput;
import org.hibernate.tool.schema.spi.TargetDescriptor;
import org.hibernate.type.SqlTypes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.hibernate.autoconfigure.HibernateProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.hibernate.autoconfigure.HibernateSettings;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Proves the bootstrap DDL and the {@link RecordVersionEntity} mapping agree, and that Hibernate
 * leaves the partitioned table alone (Core-Data-Model §17).
 */
@IntegrationTest
class SchemaParityIT {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private DataSource dataSource;
    @Autowired private JpaProperties jpaProperties;
    @Autowired private HibernateProperties hibernateProperties;
    @Autowired private ObjectProvider<HibernatePropertiesCustomizer> customizers;

    @Test
    @DisplayName("record_version is a partitioned table (relkind 'p')")
    void recordVersionIsPartitioned() {
        assertThat(jdbc.queryForObject(
                        "select relkind::text from pg_class c join pg_namespace n on n.oid = c.relnamespace"
                                + " where n.nspname = 'public' and c.relname = 'record_version'",
                        String.class))
                .isEqualTo("p");
    }

    @Test
    @DisplayName("the table's columns match the entity's mapped columns by name, nullability and type")
    void columnsMatchTheEntity() {
        Map<String, String> expected = expectedColumns();
        Map<String, String> actual = new TreeMap<>();
        jdbc.query(
                """
                select column_name, is_nullable, udt_name, character_maximum_length
                  from information_schema.columns
                 where table_schema = 'public' and table_name = 'record_version'
                """,
                rs -> {
                    Object length = rs.getObject(4);
                    actual.put(
                            rs.getString(1),
                            describe(rs.getString(3), length == null ? null : ((Number) length).intValue(),
                                    "NO".equals(rs.getString(2))));
                });

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("the column list in the DDL constants matches the entity, in order")
    void ddlConstantsFollowEntityOrder() {
        List<String> entityOrder = new ArrayList<>();
        for (Field field : RecordVersionEntity.class.getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column != null) {
                entityOrder.add(column.name());
            }
        }

        assertThat(HistoryDdl.COLUMNS).extracting(HistoryDdl.Column::name).containsExactlyElementsOf(entityOrder);
    }

    @Test
    @DisplayName("the prompt-002 unique constraint is gone")
    void noLegacyUniqueConstraint() {
        assertThat(jdbc.queryForObject(
                        "select count(*) from pg_constraint where conname = 'record_version_source_external_version_key'",
                        Long.class))
                .isZero();
    }

    @Test
    @DisplayName("a second Hibernate schema update emits nothing for record_version")
    void secondSchemaUpdateLeavesRecordVersionAlone() {
        Map<String, Object> settings = applicationHibernateSettings();
        assertThat(settings).containsEntry("hibernate.hbm2ddl.extra_physical_table_types", "PARTITIONED TABLE");

        List<String> statements = schemaUpdateScript(settings);

        assertThat(statements).noneMatch(sql -> sql.toLowerCase().contains("record_version"));
    }

    @Test
    @DisplayName("Hibernate's PostgreSQL dialect also recognizes partitioned tables on its own")
    void dialectRecognizesPartitionedTablesWithoutTheSetting() {
        // Hibernate 7.4's PostgreSQLDialect adds PARTITIONED TABLE to the physical table types
        // itself, so the setting is a belt-and-braces declaration rather than the only guard. This
        // pins that behavior: if a dialect change ever dropped it, the setting above still holds.
        Map<String, Object> settings = applicationHibernateSettings();
        settings.remove("hibernate.hbm2ddl.extra_physical_table_types");

        List<String> statements = schemaUpdateScript(settings);

        assertThat(statements).noneMatch(sql -> sql.toLowerCase().contains("record_version"));
    }

    // ---------------------------------------------------------------- helpers

    /** Expected columns derived from the entity's {@code @Column} and {@code @JdbcTypeCode}. */
    private static Map<String, String> expectedColumns() {
        Map<String, String> expected = new TreeMap<>();
        for (Field field : RecordVersionEntity.class.getDeclaredFields()) {
            Column column = field.getAnnotation(Column.class);
            if (column == null) {
                continue;
            }
            boolean notNull = !column.nullable() || field.getType().isPrimitive();
            String udt;
            Integer length = null;
            JdbcTypeCode code = field.getAnnotation(JdbcTypeCode.class);
            if (column.columnDefinition().startsWith("geometry")) {
                udt = "geometry";
            } else if (code != null && code.value() == SqlTypes.SMALLINT) {
                udt = "int2";
            } else if (code != null && code.value() == SqlTypes.JSON) {
                udt = "jsonb";
            } else if (code != null && code.value() == SqlTypes.TIMESTAMP_UTC) {
                udt = "timestamptz";
            } else if (field.getType() == Long.class || field.getType() == long.class) {
                udt = "int8";
            } else if (field.getType() == Integer.class || field.getType() == int.class) {
                udt = "int4";
            } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                udt = "bool";
            } else if (field.getType() == String.class) {
                udt = "varchar";
                length = column.length();
            } else {
                throw new AssertionError("No expected SQL type for " + field);
            }
            expected.put(column.name(), describe(udt, length, notNull));
        }
        return expected;
    }

    private static String describe(String udt, Integer length, boolean notNull) {
        return udt + (length == null ? "" : "(" + length + ")") + (notNull ? " not null" : " null");
    }

    /** The Hibernate settings the application's EntityManagerFactory was built with. */
    private Map<String, Object> applicationHibernateSettings() {
        Map<String, Object> settings = new HashMap<>(hibernateProperties.determineHibernateProperties(
                jpaProperties.getProperties(),
                new HibernateSettings()
                        .ddlAuto(() -> "none")
                        .hibernatePropertiesCustomizers(customizers.orderedStream().toList())));
        settings.remove(AvailableSettings.HBM2DDL_AUTO);
        settings.put(AvailableSettings.JAKARTA_NON_JTA_DATASOURCE, dataSource);
        return settings;
    }

    /** Runs a schema update against the live database, capturing the statements as a script. */
    private static List<String> schemaUpdateScript(Map<String, Object> settings) {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().applySettings(settings).build();
        try {
            MetadataSources sources = new MetadataSources(registry);
            entityClasses().forEach(sources::addAnnotatedClass);
            Metadata metadata = sources.buildMetadata();

            List<String> statements = new ArrayList<>();
            ScriptTargetOutput output = new ScriptTargetOutput() {
                @Override
                public void prepare() {}

                @Override
                public void accept(String command) {
                    statements.add(command);
                }

                @Override
                public void release() {}
            };
            TargetDescriptor target = new TargetDescriptor() {
                @Override
                public EnumSet<TargetType> getTargetTypes() {
                    return EnumSet.of(TargetType.SCRIPT);
                }

                @Override
                public ScriptTargetOutput getScriptTargetOutput() {
                    return output;
                }
            };
            Map<String, Object> configuration = new HashMap<>(settings);
            ExecutionOptions options = SchemaManagementToolCoordinator.buildExecutionOptions(
                    configuration, new ExceptionHandlerCollectingImpl());
            registry.requireService(SchemaManagementTool.class)
                    .getSchemaMigrator(configuration)
                    .doMigration(metadata, options, ContributableMatcher.ALL, target);
            return statements;
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private static List<Class<?>> entityClasses() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        List<Class<?>> classes = new ArrayList<>();
        for (BeanDefinition definition : scanner.findCandidateComponents("ai.foresignal")) {
            try {
                classes.add(Class.forName(definition.getBeanClassName()));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        return classes;
    }
}
