package ai.foresignal.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.config.VectorExtensionInitializer;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.PostgisContainers;
import ai.foresignal.testsupport.search.SearchItSupport;
import ai.foresignal.testsupport.search.SearchTestSources;
import java.util.OptionalInt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * {@link EmbeddingSchemaManager} against the {@code docker/db} image (PostGIS plus pgvector), and the
 * non-fatal path without pgvector against the plain PostGIS image (prompt 027).
 */
@IntegrationTest
@Import(SearchTestSources.class)
class EmbeddingSchemaManagerIT extends SearchItSupport {

    @Autowired SemanticCapability capability;

    @Test
    @DisplayName("the docker/db image has pgvector: the capability is available")
    void capabilityAvailable() {
        assertThat(capability.available()).isTrue();
        assertThat(capability.reason()).isNull();
        assertThat(schema.vectorsAvailable()).isTrue();
    }

    @Test
    @DisplayName("slot A at 8 dimensions, then slot B at 16 beside it; ensureSlot is idempotent")
    void slotsSideBySide() {
        schema.resetSlot(VectorSlot.A, 8);
        assertThat(schema.dimensions(VectorSlot.A)).isEqualTo(OptionalInt.of(8));
        assertThat(schema.ensureSlot(VectorSlot.A, 8)).isFalse();

        schema.resetSlot(VectorSlot.B, 16);
        assertThat(schema.dimensions(VectorSlot.A)).isEqualTo(OptionalInt.of(8));
        assertThat(schema.dimensions(VectorSlot.B)).isEqualTo(OptionalInt.of(16));
        assertThat(schema.ensureSlot(VectorSlot.B, 16)).isFalse();

        assertThat(schema.ensureSlot(VectorSlot.B, 4)).isTrue();
        assertThat(schema.dimensions(VectorSlot.B)).isEqualTo(OptionalInt.of(4));
        assertThat(columnExists("model_b")).isTrue();
    }

    @Test
    @DisplayName("the base DDL is idempotent: tsv, GIN, GiST and btree indexes exist after repeated runs")
    void baseIdempotent() {
        schema.ensureBase();
        schema.ensureBase();
        assertThat(columnExists("tsv")).isTrue();
        assertThat(indexExists("embedding_document_tsv_gin")).isTrue();
        assertThat(indexExists("embedding_document_geom_gist")).isTrue();
        assertThat(indexExists("embedding_document_source_time_idx")).isTrue();
    }

    @Test
    @DisplayName("the HNSW index is built concurrently over existing rows, valid, and a rebuild is a no-op")
    void concurrentIndexBuild() {
        schema.resetSlot(VectorSlot.A, 3);
        jdbc.update("INSERT INTO embedding_document (doc_type, doc_id, text, text_hash, updated_at, embedding_a, model_a)"
                + " VALUES ('briefing', 'b1', 'x', 1, now(), CAST('[1,0,0]' AS vector), 'p:m:3')");
        schema.buildIndex(VectorSlot.A);
        assertThat(schema.hasValidIndex(VectorSlot.A)).isTrue();
        String definition = jdbc.queryForObject("SELECT indexdef FROM pg_indexes WHERE indexname = ?", String.class,
                VectorSlot.A.indexName());
        assertThat(definition).contains("hnsw").contains("vector_cosine_ops").contains("embedding_a");
        schema.buildIndex(VectorSlot.A);
        assertThat(schema.hasValidIndex(VectorSlot.A)).isTrue();

        schema.dropSlot(VectorSlot.A);
        assertThat(schema.dimensions(VectorSlot.A)).isEmpty();
        assertThat(indexExists(VectorSlot.A.indexName())).isFalse();
    }

    @Test
    @DisplayName("slot statements name only the slot's own columns and the requested width")
    void slotStatements() {
        assertThat(EmbeddingSchemaManager.slotStatements(VectorSlot.B, 16)).containsExactly(
                "ALTER TABLE embedding_document DROP COLUMN IF EXISTS embedding_b, DROP COLUMN IF EXISTS model_b",
                "ALTER TABLE embedding_document ADD COLUMN embedding_b vector(16), ADD COLUMN model_b varchar(200)");
        assertThat(EmbeddingSchemaManager.indexStatement(VectorSlot.A)).isEqualTo(
                "CREATE INDEX CONCURRENTLY IF NOT EXISTS embedding_document_hnsw_a ON embedding_document"
                        + " USING hnsw (embedding_a vector_cosine_ops)");
        assertThatThrownBy(() -> EmbeddingSchemaManager.slotStatements(VectorSlot.A, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("without pgvector (plain PostGIS image) the extension check is non-fatal and slots are refused")
    void missingExtensionIsNonFatal() {
        try (PostgreSQLContainer plain = new PostgreSQLContainer(PostgisContainers.POSTGIS_IMAGE)) {
            plain.start();
            DriverManagerDataSource dataSource =
                    new DriverManagerDataSource(plain.getJdbcUrl(), plain.getUsername(), plain.getPassword());

            SemanticCapability missing = new SemanticCapability();
            new VectorExtensionInitializer(dataSource, missing).initialize();

            assertThat(missing.available()).isFalse();
            assertThat(missing.reason()).contains("pgvector").contains("Text search keeps working");
            EmbeddingSchemaManager manager = new EmbeddingSchemaManager(dataSource, missing);
            assertThat(manager.vectorsAvailable()).isFalse();
            assertThatThrownBy(() -> manager.resetSlot(VectorSlot.A, 8))
                    .isInstanceOf(IllegalStateException.class).hasMessageContaining("pgvector");
        }
    }

    private boolean columnExists(String column) {
        Long n = jdbc.queryForObject("SELECT count(*) FROM information_schema.columns"
                + " WHERE table_name = 'embedding_document' AND column_name = ?", Long.class, column);
        return n != null && n > 0;
    }

    private boolean indexExists(String index) {
        Long n = jdbc.queryForObject("SELECT count(*) FROM pg_indexes WHERE indexname = ?", Long.class, index);
        return n != null && n > 0;
    }
}
