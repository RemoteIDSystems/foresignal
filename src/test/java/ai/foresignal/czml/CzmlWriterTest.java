package ai.foresignal.czml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link CzmlWriter} (prompt 141) against the golden document {@code src/test/resources/czml/golden.czml}: the document
 * packet first with the clock and attribution, a sampled entity, a constant one-sample entity with the default color,
 * an entity without samples left out, and the caps and helpers.
 */
class CzmlWriterTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant T0 = Instant.parse("2026-03-01T00:00:00Z");

    private static String golden() throws IOException {
        try (InputStream in = CzmlWriterTest.class.getResourceAsStream("/czml/golden.czml")) {
            assertThat(in).as("golden file").isNotNull();
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /** Serializes and parses back, so number node types match the parsed golden file. */
    private static JsonNode tree(List<Map<String, Object>> packets) {
        return JSON.readTree(JSON.writeValueAsString(packets));
    }

    @Test
    @DisplayName("the packets equal the golden document")
    void golden_document() throws IOException {
        CzmlWriter.Document document = new CzmlWriter.Document("Foresignal history", T0, T0.plusSeconds(3600),
                "Attribution A · Attribution B", false);
        CzmlWriter.Entity sampled = new CzmlWriter.Entity("tt-mover:m1", "CS-1", "tt-vehicle", "#FF8800", List.of(
                new CzmlWriter.Sample(T0, 10.0, 50.0, 0),
                new CzmlWriter.Sample(T0.plusSeconds(60), 10.123456789, 50.5, 1000.456),
                new CzmlWriter.Sample(T0.plusMillis(90_500), 10.2, 50.25, 0)));
        CzmlWriter.Entity single = new CzmlWriter.Entity("tt-mover:m2", null, null, "bogus", List.of(
                new CzmlWriter.Sample(T0.plusSeconds(30), -120.5, 35.25, 12.0)));
        CzmlWriter.Entity empty = new CzmlWriter.Entity("tt-mover:m3", "Nothing", "tt-vehicle", "#000000", List.of());

        List<Map<String, Object>> packets = CzmlWriter.packets(document, List.of(sampled, single, empty));

        assertThat(tree(packets)).isEqualTo(JSON.readTree(golden()));
        assertThat(packets.getFirst().get("id")).as("document packet first").isEqualTo("document");
    }

    @Test
    @DisplayName("at most 5,000 entity packets; a truncated document says so in its description")
    void entityCap() {
        List<CzmlWriter.Entity> many = new ArrayList<>();
        for (int i = 0; i < CzmlWriter.MAX_ENTITIES + 7; i++) {
            many.add(new CzmlWriter.Entity("s:" + i, "E" + i, "k", null,
                    List.of(new CzmlWriter.Sample(T0, i % 180, 0, 0))));
        }
        List<Map<String, Object>> packets = CzmlWriter.packets(
                new CzmlWriter.Document(null, T0, T0, null, true), many);
        assertThat(packets).hasSize(CzmlWriter.MAX_ENTITIES + 1);
        assertThat(packets.getFirst().get("name")).isEqualTo("Foresignal");
        assertThat((String) packets.getFirst().get("description"))
                .isEqualTo("Data: Foresignal (truncated: at most 5000 entities)");
    }

    @Test
    @DisplayName("colors: #RRGGBB and RRGGBB parse; null and malformed fall back to the default")
    void colors() {
        assertThat(CzmlWriter.rgba("#00FF7f")).containsExactly(0, 255, 127, 255);
        assertThat(CzmlWriter.rgba("102030")).containsExactly(16, 32, 48, 255);
        assertThat(CzmlWriter.rgba(null)).containsExactly(51, 136, 255, 255);
        assertThat(CzmlWriter.rgba("#12")).containsExactly(51, 136, 255, 255);
        assertThat(CzmlWriter.rgba(" #ABCDEF ")).containsExactly(171, 205, 239, 255);
    }

    @Test
    @DisplayName("clock multiplier shows the range in about two minutes and is at least 1")
    void multiplier() {
        assertThat(CzmlWriter.multiplier(T0, T0)).isEqualTo(1);
        assertThat(CzmlWriter.multiplier(T0, T0.plusSeconds(60))).isEqualTo(1);
        assertThat(CzmlWriter.multiplier(T0, T0.plusSeconds(86_400))).isEqualTo(720);
        assertThat(CzmlWriter.interval(T0, T0.plusSeconds(1))).isEqualTo("2026-03-01T00:00:00Z/2026-03-01T00:00:01Z");
    }

    @Test
    @DisplayName("a document whose end precedes its start, or without a range, is refused")
    void documentValidation() {
        assertThatThrownBy(() -> new CzmlWriter.Document("x", T0, T0.minusSeconds(1), null, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new CzmlWriter.Document("x", null, T0, null, false))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new CzmlWriter.Sample(null, 0, 0, 0)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new CzmlWriter.Entity(null, "t", "k", null, List.of()))
                .isInstanceOf(NullPointerException.class);
        assertThat(new CzmlWriter.Entity("a", "t", "k", null, null).samples()).isEmpty();
    }

    @Test
    @DisplayName("a blank attribution reads 'Data: Foresignal'; a title that is blank falls back to the id")
    void fallbacks() {
        Map<String, Object> doc = CzmlWriter.documentPacket(new CzmlWriter.Document("n", T0, T0, "  ", false));
        assertThat(doc.get("description")).isEqualTo("Data: Foresignal");
        Map<String, Object> packet = CzmlWriter.entityPacket(new CzmlWriter.Entity("src:id", " ", "k", null,
                List.of(new CzmlWriter.Sample(T0, 1, 2, 3))));
        assertThat(packet.get("name")).isEqualTo("src:id");
        assertThat(packet.get("properties")).isEqualTo(Map.of("kind", "k"));
    }
}
