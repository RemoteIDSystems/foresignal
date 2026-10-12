package ai.foresignal.ask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.mcp.McpToolException;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/** {@link ScopeInjector}: defaults injected, narrowing kept, widening clamped or refused. */
class ScopeInjectorTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static final JsonNode LIST_TOOL = JSON.readTree("""
            {"type":"object","properties":{"source":{"type":"array"},"kind":{"type":"array"},"bbox":{"type":"string"},
             "since":{"type":"string"},"until":{"type":"string"}}}""");

    static final JsonNode ONE_SOURCE_TOOL = JSON.readTree("""
            {"type":"object","properties":{"source":{"type":"string"},"externalId":{"type":"string"}}}""");

    static final Instant SINCE = Instant.parse("2026-10-01T00:00:00Z");
    static final Instant UNTIL = Instant.parse("2026-10-10T00:00:00Z");

    static final AskScope SCOPE = new AskScope(List.of("a", "b"), List.of("quake"), "130,30,150,40", SINCE, UNTIL,
            List.of(), null);

    static ObjectNode args(String json) {
        return (ObjectNode) JSON.readTree(json);
    }

    @Test
    @DisplayName("absent filters take the scope's values")
    void defaults() {
        ObjectNode out = ScopeInjector.apply(SCOPE, LIST_TOOL, args("{}"));

        assertThat(out.get("source").toString()).isEqualTo("[\"a\",\"b\"]");
        assertThat(out.get("kind").toString()).isEqualTo("[\"quake\"]");
        assertThat(out.get("bbox").asString()).isEqualTo("130,30,150,40");
        assertThat(out.get("since").asString()).isEqualTo(SINCE.toString());
        assertThat(out.get("until").asString()).isEqualTo(UNTIL.toString());
    }

    @Test
    @DisplayName("narrower filters are kept; wider ones are intersected or clamped")
    void narrowAndClamp() {
        ObjectNode out = ScopeInjector.apply(SCOPE, LIST_TOOL, args("""
                {"source":["b","z"],"bbox":"140,35,170,60","since":"2026-09-01T00:00:00Z","until":"2026-10-05T00:00:00Z"}"""));

        assertThat(out.get("source").toString()).isEqualTo("[\"b\"]");
        assertThat(out.get("bbox").asString()).isEqualTo("140,35,150,40");
        assertThat(out.get("since").asString()).isEqualTo(SINCE.toString());
        assertThat(out.get("until").asString()).isEqualTo("2026-10-05T00:00:00Z");
    }

    @Test
    @DisplayName("a source, box or window entirely outside the scope is refused")
    void refusals() {
        assertThatThrownBy(() -> ScopeInjector.apply(SCOPE, LIST_TOOL, args("{\"source\":[\"z\"]}")))
                .isInstanceOf(McpToolException.class).hasMessageContaining("limited to sources");
        assertThatThrownBy(() -> ScopeInjector.apply(SCOPE, LIST_TOOL, args("{\"bbox\":\"-130,30,-110,40\"}")))
                .isInstanceOf(McpToolException.class).hasMessageContaining("area");
        assertThatThrownBy(() -> ScopeInjector.apply(SCOPE, LIST_TOOL, args("{\"since\":\"2026-11-01T00:00:00Z\"}")))
                .isInstanceOf(McpToolException.class).hasMessageContaining("time window");
        assertThatThrownBy(() -> ScopeInjector.apply(SCOPE, ONE_SOURCE_TOOL, args("{\"source\":\"z\",\"externalId\":\"1\"}")))
                .isInstanceOf(McpToolException.class);
    }

    @Test
    @DisplayName("parameters the tool does not declare are left alone; an empty scope changes nothing")
    void untouched() {
        ObjectNode one = ScopeInjector.apply(SCOPE, ONE_SOURCE_TOOL, args("{\"source\":\"a\",\"externalId\":\"1\"}"));
        assertThat(one.toString()).isEqualTo("{\"source\":\"a\",\"externalId\":\"1\"}");

        ObjectNode none = ScopeInjector.apply(AskScope.NONE, LIST_TOOL, args("{\"bbox\":\"-180,-80,180,80\"}"));
        assertThat(none.toString()).isEqualTo("{\"bbox\":\"-180,-80,180,80\"}");
    }

    @Test
    @DisplayName("an antimeridian box falls back to the scope's own box")
    void antimeridian() {
        AskScope pacific = new AskScope(List.of(), List.of(), "170,-20,-170,20", null, null, List.of(), null);
        ObjectNode out = ScopeInjector.apply(pacific, LIST_TOOL, args("{\"bbox\":\"175,-10,179,10\"}"));

        assertThat(out.get("bbox").asString()).isEqualTo("170,-20,-170,20");
    }
}
