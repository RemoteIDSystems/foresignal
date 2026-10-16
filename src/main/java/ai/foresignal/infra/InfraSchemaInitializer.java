package ai.foresignal.infra;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * The indexes of the infrastructure graph (prompt 104) that JPA cannot express: GiST on
 * {@code infra_node.geom} and {@code infra_node.point}, the facility-type and build lookups, and the
 * edge uniqueness and traversal indexes. Every statement is {@code IF NOT EXISTS}, so a restart is a
 * no-op; a statement that fails (for example a unique index over rows an older build left behind)
 * is logged and skipped rather than stopping startup, and the next successful rebuild removes the
 * old rows.
 *
 * <p>Runs as an {@link ApplicationRunner} after Hibernate's DDL, like {@code SpatialIndexInitializer}.
 * Prompt 144 moves these statements into the Flyway baseline.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class InfraSchemaInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InfraSchemaInitializer.class);

    /** The statements, in order. */
    static final List<String> STATEMENTS = List.of(
            "CREATE INDEX IF NOT EXISTS infra_node_geom_gist ON infra_node USING GIST (geom)",
            "CREATE INDEX IF NOT EXISTS infra_node_point_gist ON infra_node USING GIST (point)",
            "CREATE INDEX IF NOT EXISTS infra_node_type_idx ON infra_node (facility_type)",
            "CREATE INDEX IF NOT EXISTS infra_node_build_type_idx ON infra_node (build_id, facility_type, id)",
            "CREATE UNIQUE INDEX IF NOT EXISTS infra_node_build_record_idx ON infra_node"
                    + " (build_id, source_id, external_id)",
            "CREATE UNIQUE INDEX IF NOT EXISTS infra_edge_unique_idx ON infra_edge"
                    + " (from_node, to_node, relation, rule_id)",
            "CREATE INDEX IF NOT EXISTS infra_edge_build_from_idx ON infra_edge (build_id, from_node)",
            "CREATE INDEX IF NOT EXISTS infra_edge_build_to_idx ON infra_edge (build_id, to_node)");

    private final DataSource dataSource;

    /** Creates the initializer. */
    public InfraSchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        execute();
    }

    /** Applies every statement; returns how many succeeded. */
    public int execute() {
        int applied = 0;
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            for (String sql : STATEMENTS) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(sql);
                    applied++;
                } catch (SQLException e) {
                    log.warn("Infrastructure index statement skipped ({}): {}", sql, e.getMessage());
                }
            }
        } catch (SQLException e) {
            log.warn("Could not create the infrastructure graph indexes: {}", e.getMessage());
        }
        return applied;
    }
}
