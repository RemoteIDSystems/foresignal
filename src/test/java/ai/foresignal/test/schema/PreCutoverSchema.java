package ai.foresignal.test.schema;

import jakarta.persistence.Entity;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.tool.schema.TargetType;
import org.hibernate.tool.schema.internal.ExceptionHandlerCollectingImpl;
import org.hibernate.tool.schema.spi.ContributableMatcher;
import org.hibernate.tool.schema.spi.ExecutionOptions;
import org.hibernate.tool.schema.spi.SchemaManagementTool;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
import org.hibernate.tool.schema.spi.ScriptTargetOutput;
import org.hibernate.tool.schema.spi.TargetDescriptor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The pre-1.0 schema generator, kept as a test-only utility (prompt 144): what {@code ddl-auto=update} plus the
 * bootstrap code built before Flyway owned the schema. {@code FlywaySchemaParityIT} compares its result with the
 * Flyway baseline.
 *
 * <p>Three steps, in the order the old startup ran them: {@code schema/pre-cutover/before-hibernate.sql} (extensions,
 * the partitioned history tables, chronicle and held tables), Hibernate's schema migrator over every {@link Entity} in
 * {@code ai.foresignal} with the application's own Hibernate settings, then {@code schema/pre-cutover/after-hibernate.sql}
 * (the indexes and storage parameters JPA cannot express).
 */
public final class PreCutoverSchema {

    /** The SQL that ran before the {@code EntityManagerFactory}. */
    public static final String BEFORE_HIBERNATE = "schema/pre-cutover/before-hibernate.sql";

    /** The SQL that ran after Hibernate's DDL. */
    public static final String AFTER_HIBERNATE = "schema/pre-cutover/after-hibernate.sql";

    /** The separator between statements in those files. */
    static final String SEPARATOR = "\n--;";

    private PreCutoverSchema() {}

    /**
     * Builds the pre-cutover schema in an empty database.
     *
     * @param target the empty database
     * @param hibernateSettings the application's Hibernate settings (naming strategies, dialect options); the data
     *     source and {@code hbm2ddl.auto} are replaced
     * @return the Hibernate migrator's collected exceptions, empty when every statement succeeded
     */
    public static List<Exception> build(DataSource target, Map<String, Object> hibernateSettings) {
        JdbcTemplate jdbc = new JdbcTemplate(target);
        statements(BEFORE_HIBERNATE).forEach(jdbc::execute);
        List<Exception> errors = hibernateUpdate(target, hibernateSettings);
        statements(AFTER_HIBERNATE).forEach(jdbc::execute);
        return errors;
    }

    /**
     * The statements of a classpath script, split on lines holding only {@code --;}.
     *
     * @param resource the classpath location
     * @return the non-blank statements, in order
     */
    public static List<String> statements(String resource) {
        String text;
        try (InputStream in = new ClassPathResource(resource).getInputStream()) {
            text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + resource, e);
        }
        List<String> out = new ArrayList<>();
        for (String chunk : text.split(SEPARATOR)) {
            String statement = chunk.strip();
            if (!statement.isEmpty() && !onlyComments(statement)) {
                out.add(statement);
            }
        }
        return out;
    }

    private static boolean onlyComments(String chunk) {
        return chunk.lines().allMatch(line -> line.isBlank() || line.strip().startsWith("--"));
    }

    /** Runs Hibernate's schema migrator ({@code update}) against {@code target}. */
    private static List<Exception> hibernateUpdate(DataSource target, Map<String, Object> hibernateSettings) {
        Map<String, Object> settings = new HashMap<>(hibernateSettings);
        settings.remove(AvailableSettings.HBM2DDL_AUTO);
        settings.put(AvailableSettings.JAKARTA_NON_JTA_DATASOURCE, target);
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder().applySettings(settings).build();
        try {
            MetadataSources sources = new MetadataSources(registry);
            entityClasses().forEach(sources::addAnnotatedClass);
            Metadata metadata = sources.buildMetadata();
            TargetDescriptor database = new TargetDescriptor() {
                @Override
                public EnumSet<TargetType> getTargetTypes() {
                    return EnumSet.of(TargetType.DATABASE);
                }

                @Override
                public ScriptTargetOutput getScriptTargetOutput() {
                    return null;
                }
            };
            ExceptionHandlerCollectingImpl handler = new ExceptionHandlerCollectingImpl();
            ExecutionOptions options = SchemaManagementToolCoordinator.buildExecutionOptions(settings, handler);
            registry.requireService(SchemaManagementTool.class)
                    .getSchemaMigrator(settings)
                    .doMigration(metadata, options, ContributableMatcher.ALL, database);
            return new ArrayList<>(handler.getExceptions());
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    /**
     * Every {@link Entity} class under {@code ai.foresignal}.
     *
     * @return the classes, in classpath-scan order
     */
    public static List<Class<?>> entityClasses() {
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
