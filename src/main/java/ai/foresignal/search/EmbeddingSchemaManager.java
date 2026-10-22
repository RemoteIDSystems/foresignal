package ai.foresignal.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.OptionalInt;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The vector-slot DDL of {@code embedding_document} (prompt 027), the one part of that table that stays dynamic
 * (Architecture §25): its width follows the configured embedding model, so the reindex creates, resets and drops slots
 * at runtime. The rest of the table's native DDL (the generated {@code tsv} column and its GIN index, the GiST index
 * on {@code geom} and the btree on {@code (source_id, effective_time)}) is in the Flyway baseline since prompt 144.
 *
 * <p>Vector slots ({@link VectorSlot}): {@code embedding_<s> vector(N)}, {@code model_<s> varchar(200)} and the HNSW
 * index {@code embedding_document_hnsw_<s>} with {@code vector_cosine_ops}. Only created when pgvector is available
 * ({@link SemanticCapability}). Resetting a slot drops and re-adds its nullable columns, a metadata-only change; the
 * HNSW index is built with {@code CREATE INDEX CONCURRENTLY} on an autocommit connection, so writes continue during
 * the build.
 */
@Component
public class EmbeddingSchemaManager {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingSchemaManager.class);

    /** The table. */
    public static final String TABLE = "embedding_document";

    static final String SELECT_DIMENSIONS = """
            SELECT a.atttypmod FROM pg_attribute a
             WHERE a.attrelid = 'embedding_document'::regclass AND a.attname = ? AND NOT a.attisdropped
            """;

    static final String SELECT_INDEX_VALID = """
            SELECT i.indisvalid FROM pg_class c JOIN pg_index i ON i.indexrelid = c.oid
             WHERE c.relname = ? AND c.relnamespace = (SELECT oid FROM pg_namespace WHERE nspname = current_schema())
            """;

    private final DataSource dataSource;
    private final SemanticCapability capability;

    /** Creates the manager. */
    public EmbeddingSchemaManager(DataSource dataSource, SemanticCapability capability) {
        this.dataSource = dataSource;
        this.capability = capability;
    }

    /** True when pgvector is available, so slots can exist. */
    public boolean vectorsAvailable() {
        return capability.available();
    }

    /**
     * The statements that create {@code slot} at {@code dimensions} from scratch: drop and re-add its
     * columns. The HNSW index is separate ({@link #indexStatement}), because it is built concurrently.
     */
    public static List<String> slotStatements(VectorSlot slot, int dimensions) {
        if (dimensions < 1) {
            throw new IllegalArgumentException("dimensions must be positive");
        }
        return List.of(
                "ALTER TABLE embedding_document DROP COLUMN IF EXISTS " + slot.embeddingColumn()
                        + ", DROP COLUMN IF EXISTS " + slot.modelColumn(),
                "ALTER TABLE embedding_document ADD COLUMN " + slot.embeddingColumn() + " vector(" + dimensions
                        + "), ADD COLUMN " + slot.modelColumn() + " varchar(200)");
    }

    /** The HNSW index of {@code slot}, built concurrently. */
    public static String indexStatement(VectorSlot slot) {
        return "CREATE INDEX CONCURRENTLY IF NOT EXISTS " + slot.indexName() + " ON embedding_document USING hnsw ("
                + slot.embeddingColumn() + " vector_cosine_ops)";
    }

    /**
     * Makes sure {@code slot} exists at {@code dimensions}: a no-op when it already does, otherwise
     * a {@linkplain #resetSlot reset}.
     *
     * @return true when the slot was (re)created, emptying it
     */
    public boolean ensureSlot(VectorSlot slot, int dimensions) {
        requireVectors();
        OptionalInt current = dimensions(slot);
        if (current.isPresent() && current.getAsInt() == dimensions && modelColumnExists(slot)) {
            return false;
        }
        resetSlot(slot, dimensions);
        return true;
    }

    /**
     * Recreates {@code slot} empty at {@code dimensions}: {@code DROP COLUMN IF EXISTS} and re-add,
     * which also drops its index. Metadata-only on nullable columns.
     */
    public void resetSlot(VectorSlot slot, int dimensions) {
        requireVectors();
        execute(slotStatements(slot, dimensions));
        log.info("Vector slot {} reset to vector({})", slot, dimensions);
    }

    /** Drops {@code slot}'s index and columns (after a swap, or when a reindex is canceled). */
    public void dropSlot(VectorSlot slot) {
        execute(List.of("DROP INDEX IF EXISTS " + slot.indexName(),
                "ALTER TABLE embedding_document DROP COLUMN IF EXISTS " + slot.embeddingColumn()
                        + ", DROP COLUMN IF EXISTS " + slot.modelColumn()));
        log.info("Vector slot {} dropped", slot);
    }

    /** The declared width of {@code slot}'s vector column, or empty when it does not exist. */
    public OptionalInt dimensions(VectorSlot slot) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_DIMENSIONS)) {
            statement.setString(1, slot.embeddingColumn());
            try (ResultSet rows = statement.executeQuery()) {
                if (rows.next() && rows.getInt(1) > 0) {
                    return OptionalInt.of(rows.getInt(1));
                }
                return OptionalInt.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read the vector slot " + slot + ": " + e.getMessage(), e);
        }
    }

    /** True when {@code slot}'s valid HNSW index exists. */
    public boolean hasValidIndex(VectorSlot slot) {
        Boolean valid = indexValidity(slot);
        return valid != null && valid;
    }

    /**
     * Builds {@code slot}'s HNSW index with {@code CREATE INDEX CONCURRENTLY}, outside any transaction
     * and without a statement timeout. An invalid index left by an interrupted build is dropped first.
     */
    public void buildIndex(VectorSlot slot) {
        requireVectors();
        Boolean valid = indexValidity(slot);
        if (Boolean.TRUE.equals(valid)) {
            return;
        }
        try (Connection connection = dataSource.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(true);
            try (Statement statement = connection.createStatement()) {
                statement.execute("SET statement_timeout = 0");
                if (Boolean.FALSE.equals(valid)) {
                    statement.execute("DROP INDEX CONCURRENTLY IF EXISTS " + slot.indexName());
                }
                statement.execute(indexStatement(slot));
            } finally {
                try (Statement reset = connection.createStatement()) {
                    reset.execute("RESET statement_timeout");
                }
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not build the HNSW index of slot " + slot + ": " + e.getMessage(), e);
        }
        log.info("HNSW index {} built", slot.indexName());
    }

    private Boolean indexValidity(VectorSlot slot) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_INDEX_VALID)) {
            statement.setString(1, slot.indexName());
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? rows.getBoolean(1) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read the index of slot " + slot + ": " + e.getMessage(), e);
        }
    }

    private boolean modelColumnExists(VectorSlot slot) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(SELECT_DIMENSIONS)) {
            statement.setString(1, slot.modelColumn());
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not read the vector slot " + slot + ": " + e.getMessage(), e);
        }
    }

    private void requireVectors() {
        if (!capability.available()) {
            throw new IllegalStateException("pgvector is not available: " + capability.reason());
        }
    }

    private void execute(List<String> statements) {
        try (Connection connection = dataSource.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(true);
            try (Statement statement = connection.createStatement()) {
                for (String sql : statements) {
                    statement.execute(sql);
                }
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("embedding_document DDL failed: " + e.getMessage(), e);
        }
    }
}
