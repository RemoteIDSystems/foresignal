package ai.foresignal.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link EmbeddingText}: the embedded text and its hash (prompt 027). */
class EmbeddingTextTest {

    @Test
    @DisplayName("text is kind | source name | embed field values, in declaration order, empties left out")
    void format() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("headline", "Flood warning");
        payload.put("summary", "");
        payload.put("areas", List.of("North", "South"));
        payload.put("detail", Map.of("level", 3));
        assertThat(EmbeddingText.of("alert", "Weather", payload, List.of("headline", "summary", "areas", "detail", "x")))
                .isEqualTo("alert | Weather | Flood warning | North South | 3");
    }

    @Test
    @DisplayName("no embed field with a value means no text")
    void noText() {
        assertThat(EmbeddingText.of("k", "S", Map.of("other", "x"), List.of("headline"))).isNull();
        assertThat(EmbeddingText.of("k", "S", Map.of("headline", "x"), List.of())).isNull();
        assertThat(EmbeddingText.of("k", "S", null, List.of("headline"))).isNull();
    }

    @Test
    @DisplayName("text is cut to 2,000 characters")
    void cut() {
        String text = EmbeddingText.of("k", "S", Map.of("body", "x".repeat(5_000)), List.of("body"));
        assertThat(text).hasSize(EmbeddingText.MAX_CHARS).startsWith("k | S | x");
        assertThat(EmbeddingText.cut("short")).isEqualTo("short");
        String surrogate = "a".repeat(EmbeddingText.MAX_CHARS - 1) + "😀";
        assertThat(EmbeddingText.cut(surrogate)).hasSize(EmbeddingText.MAX_CHARS - 1);
    }

    @Test
    @DisplayName("the hash is stable and differs for different text")
    void hash() {
        assertThat(EmbeddingText.hash("abc")).isEqualTo(EmbeddingText.hash("abc"));
        assertThat(EmbeddingText.hash("abc")).isNotEqualTo(EmbeddingText.hash("abd"));
        assertThat(new DocInput("record", "s:1", "s", "k", 1L, null, null, null, "abc").textHash())
                .isEqualTo(EmbeddingText.hash("abc"));
    }

    @Test
    @DisplayName("vector literals are pgvector text; non-finite components are refused")
    void literal() {
        assertThat(EmbeddingStore.literal(new float[] {1f, 0.5f, -2f})).isEqualTo("[1.0,0.5,-2.0]");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> EmbeddingStore.literal(new float[] {Float.NaN}))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("vector slots name their columns and index, and alternate")
    void slots() {
        assertThat(VectorSlot.A.embeddingColumn()).isEqualTo("embedding_a");
        assertThat(VectorSlot.B.modelColumn()).isEqualTo("model_b");
        assertThat(VectorSlot.B.indexName()).isEqualTo("embedding_document_hnsw_b");
        assertThat(VectorSlot.A.other()).isEqualTo(VectorSlot.B);
        assertThat(VectorSlot.B.other()).isEqualTo(VectorSlot.A);
    }
}
