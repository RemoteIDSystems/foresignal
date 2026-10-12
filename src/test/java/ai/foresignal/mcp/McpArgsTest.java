package ai.foresignal.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Typed argument reads and the schema builders (prompt 025). */
class McpArgsTest {

    private final JsonMapper json = JsonMapper.builder().build();

    private JsonNode args(Map<String, Object> values) {
        return json.valueToTree(values);
    }

    @Test
    @DisplayName("strings, numbers, booleans and arrays are read with defaults")
    void reads() {
        JsonNode a = args(Map.of("s", " text ", "blank", " ", "n", 7, "b", true, "list", List.of("x", " y ", ""),
                "one", "solo"));
        assertThat(McpArgs.text(a, "s")).isEqualTo("text");
        assertThat(McpArgs.text(a, "blank")).isNull();
        assertThat(McpArgs.text(a, "missing")).isNull();
        assertThat(McpArgs.integer(a, "n", 1)).isEqualTo(7);
        assertThat(McpArgs.integer(a, "missing", 3)).isEqualTo(3);
        assertThat(McpArgs.optionalLong(a, "n")).isEqualTo(7L);
        assertThat(McpArgs.optionalLong(a, "missing")).isNull();
        assertThat(McpArgs.numberText(a, "n")).isEqualTo("7");
        assertThat(McpArgs.bool(a, "b", false)).isTrue();
        assertThat(McpArgs.optionalBool(a, "missing")).isNull();
        assertThat(McpArgs.strings(a, "list")).containsExactly("x", "y");
        assertThat(McpArgs.strings(a, "one")).containsExactly("solo");
        assertThat(McpArgs.strings(a, "missing")).isEmpty();
    }

    @Test
    @DisplayName("required members, date-times and UUIDs report the argument by name")
    void typed() {
        UUID id = UUID.randomUUID();
        JsonNode a = args(Map.of("at", "2026-10-12T08:00:00+02:00", "z", "2026-10-12T06:00:00Z", "id", id.toString(),
                "bad", "yesterday"));
        assertThat(McpArgs.instant(a, "at")).isEqualTo(Instant.parse("2026-10-12T06:00:00Z"));
        assertThat(McpArgs.instant(a, "z")).isEqualTo(Instant.parse("2026-10-12T06:00:00Z"));
        assertThat(McpArgs.instant(a, "missing")).isNull();
        assertThat(McpArgs.requiredUuid(a, "id")).isEqualTo(id);
        assertThatThrownBy(() -> McpArgs.instant(a, "bad")).isInstanceOf(McpToolException.class)
                .hasMessageContaining("'bad'");
        assertThatThrownBy(() -> McpArgs.uuid(a, "bad")).isInstanceOf(McpToolException.class)
                .hasMessageContaining("UUID");
        assertThatThrownBy(() -> McpArgs.requiredText(a, "missing")).hasMessageContaining("'missing' is required");
        assertThatThrownBy(() -> McpArgs.requiredLong(a, "missing")).hasMessageContaining("required");
        assertThatThrownBy(() -> McpArgs.requiredUuid(a, "missing")).hasMessageContaining("required");
    }

    @Test
    @DisplayName("schema builders produce closed objects with the declared constraints")
    void schemas() {
        Map<String, Object> schema = JsonSchemas.object(JsonSchemas.props(
                "a", JsonSchemas.integer("A.", 1, 5, 2L),
                "b", JsonSchemas.enumArray("B.", "x", "y")), "a");
        assertThat(schema).containsEntry("additionalProperties", false).containsEntry("required", List.of("a"));
        assertThat(schema.get("properties").toString()).contains("maximum=5").contains("uniqueItems=true");
        assertThat(JsonSchemas.listOutput("L").get("required")).isEqualTo(List.of("items", "total", "truncated"));
        assertThatThrownBy(() -> JsonSchemas.props("odd")).isInstanceOf(IllegalArgumentException.class);
    }
}
