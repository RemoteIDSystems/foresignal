package ai.foresignal.mcp;

import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.MOVER;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.MOVER_PRIVATE;
import static ai.foresignal.testsupport.timetravel.TimeTravelTestSources.VEHICLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.visibility.SourceVisibility;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.timetravel.TimeTravelTestSources;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The time-travel MCP tools (prompt 037): {@code get_state_at} and {@code get_track}, their time
 * inputs (ISO, {@code now}, relative durations), caps, the 4 KB short form, scopes and the private
 * gate.
 */
@IntegrationTest
@Import(TimeTravelTestSources.class)
class McpTimeTravelToolsIT {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");

    @Autowired McpToolRegistry registry;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired SourceRegistry sources;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
    }

    private ToolContext context(Scope scope, boolean openReads) {
        if (scope == null) {
            Set<String> excluded = new SourceVisibility(sources, null, null).hiddenFor(null, false, TokenRestrictions.NONE);
            return new ToolContext(new McpCaller(null, new ReadCaller(null, false, openReads), excluded, "127.0.0.1"),
                    "session", McpSessions.STREAMABLE);
        }
        // Token callers here hold the 039 private grant, so the private gate lets them through.
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "bot-" + scope.wireValue(), scope,
                TokenRestrictions.NONE, null, "principal-1", false, true);
        Set<String> excluded = new SourceVisibility(sources, null, null).hiddenFor(scope, true, TokenRestrictions.NONE);
        return new ToolContext(new McpCaller(token, new ReadCaller(scope, false, openReads, null, true), excluded,
                "10.0.0.2"), "session", McpSessions.STREAMABLE);
    }

    private McpSchema.CallToolResult call(String tool, Map<String, Object> args, Scope scope) {
        return registry.call(tool, args, context(scope, true));
    }

    private static JsonNode structured(McpSchema.CallToolResult result) {
        assertThat(result.isError()).as(String.valueOf(result.content())).isFalse();
        // structuredContent is set only for tools that declare an outputSchema (025, MCP); these tools return their
        // JSON as the text block (checkpoint 041).
        if (result.structuredContent() != null) {
            return JSON.valueToTree(result.structuredContent());
        }
        return JSON.readTree(((McpSchema.TextContent) result.content().getFirst()).text());
    }

    private void ingest(Instant at, String source, NormalizedRecord... batch) {
        if (clock.instant().isBefore(at)) {
            clock.setInstant(at);
        }
        records.ingestAt(source, at, batch);
    }

    private static NormalizedRecord mover(String id, int step, Instant at) {
        // A wiggle about 12 km long and 5.5 km wide, so simplification keeps most points.
        return NormalizedRecord.builder(id, VEHICLE).observedAt(at).point(10 + step * 0.01, 50 + 0.05 * Math.sin(step / 3.0))
                .put("callsign", "CS-" + id).put("alt", 1000).build();
    }

    private void seed() {
        ingest(T, MOVER_PRIVATE, mover("secret", 0, T));
        for (int i = 0; i < 300; i++) {
            ingest(T.plusSeconds(i * 20L), MOVER, mover("m1", i, T.plusSeconds(i * 20L)));
        }
        clock.setInstant(T.plusSeconds(6000));
    }

    @Test
    @DisplayName("get_state_at replays a source at ISO, now and relative times, with asOf, watermarkSeq and horizon")
    void stateAt() {
        seed();
        JsonNode iso = structured(call("get_state_at", Map.of("source", List.of(MOVER), "asOf",
                T.plusSeconds(30).toString()), null));
        assertThat(iso.get("asOf").asString()).isEqualTo(T.plusSeconds(30).toString());
        assertThat(iso.get("watermarkSeq").asLong()).isPositive();
        assertThat(iso.get("approximate").asBoolean()).isFalse();
        assertThat(iso.get("horizon").isString()).isTrue();
        assertThat(iso.get("items")).hasSize(1);
        assertThat(iso.get("items").get(0).toString()).contains("m1");

        JsonNode now = structured(call("get_state_at", Map.of("kind", List.of(VEHICLE), "asOf", "now"), null));
        assertThat(now.get("asOf").asString()).isEqualTo(T.plusSeconds(6000).toString());
        // m1's last version was ingested at 5980 s: valid for 5 minutes.
        assertThat(now.get("items")).hasSize(1);
        JsonNode relative = structured(call("get_state_at", Map.of("source", List.of(MOVER), "asOf", "-PT90M"), null));
        assertThat(relative.get("asOf").asString()).isEqualTo(T.plusSeconds(600).toString());
        JsonNode shorthand = structured(call("get_state_at", Map.of("source", List.of(MOVER), "asOf", "-10m"), null));
        assertThat(shorthand.get("asOf").asString()).isEqualTo(T.plusSeconds(5400).toString());
    }

    @Test
    @DisplayName("get_state_at caps, errors and the private gate")
    void stateAtCapsAndErrors() {
        seed();
        assertThat(call("get_state_at", Map.of("source", List.of(MOVER), "asOf", "now", "limit", 201), null).isError())
                .as("limit above 200").isTrue();
        assertThat(call("get_state_at", Map.of("asOf", "now"), null).isError()).as("no source or kind").isTrue();
        assertThat(call("get_state_at", Map.of("source", List.of(MOVER)), null).isError()).as("asOf required").isTrue();
        assertThat(call("get_state_at", Map.of("source", List.of(MOVER), "asOf", "tomorrow-ish"), null).isError())
                .isTrue();
        assertThat(call("get_state_at", Map.of("source", List.of(MOVER), "asOf", T.plusSeconds(9000).toString()),
                null).isError()).as("future").isTrue();
        assertThat(call("get_state_at", Map.of("source", List.of(MOVER), "asOf", "now", "place", List.of("home")),
                null).isError()).as("place needs a token").isTrue();
        assertThat(call("get_state_at", Map.of("source", List.of(MOVER_PRIVATE), "asOf", "now"), null).isError())
                .as("a private source is not found anonymously").isTrue();
        JsonNode agent = structured(call("get_state_at", Map.of("kind", List.of(VEHICLE), "asOf",
                T.plusSeconds(10).toString()), Scope.AGENT));
        assertThat(agent.get("items").toString()).contains("secret");
        JsonNode anonymous = structured(call("get_state_at", Map.of("kind", List.of(VEHICLE), "asOf",
                T.plusSeconds(10).toString()), null));
        assertThat(anonymous.get("items").toString()).doesNotContain("secret");
    }

    @Test
    @DisplayName("get_track: compact segments within 4 KB in short mode, the summary, and the maxPoints cap")
    void track() {
        seed();
        Map<String, Object> args = new HashMap<>(Map.of("sourceId", MOVER, "externalId", "m1",
                "from", T.toString(), "to", "now"));
        McpSchema.CallToolResult shortResult = call("get_track", args, null);
        JsonNode compact = structured(shortResult);
        assertThat(McpOutputShaper.size(compact)).isLessThanOrEqualTo(McpOutputShaper.COMPACT_LIMIT_BYTES);
        JsonNode summary = compact.get("summary");
        assertThat(summary.get("points").asInt()).isLessThanOrEqualTo(100).isGreaterThan(2);
        assertThat(summary.get("pointsScanned").asInt()).isEqualTo(300);
        assertThat(summary.get("distanceKm").asDouble()).isPositive();
        assertThat(summary.get("durationSeconds").asLong()).isPositive();
        assertThat(summary.get("bbox")).hasSize(4);
        assertThat(summary.get("maxSpeedKmh").asDouble()).isPositive();
        assertThat(summary.get("segments").asInt()).isEqualTo(1);
        assertThat(summary.get("approximate").asBoolean()).isFalse();
        assertThat(compact.get("segments").get(0).get(0)).hasSize(3);

        args.put("detail", "long");
        args.put("maxPoints", 500);
        JsonNode full = structured(call("get_track", args, null));
        assertThat(full.get("summary").get("points").asInt()).isGreaterThan(100).isLessThanOrEqualTo(300);

        args.put("maxPoints", 501);
        assertThat(call("get_track", args, null).isError()).as("maxPoints above 500").isTrue();
        assertThat(call("get_track", Map.of("sourceId", MOVER_PRIVATE, "externalId", "secret"), null).isError())
                .as("private source anonymously").isTrue();
        assertThat(call("get_track", Map.of("sourceId", MOVER, "externalId", "nobody"), null).isError()).isTrue();
        assertThat(call("get_track", Map.of("sourceId", MOVER), null).isError()).as("externalId required").isTrue();
    }

    @Test
    @DisplayName("both tools are read tools: listed for anonymous callers only while openReads is on")
    void scope() {
        McpCaller open = context(null, true).caller();
        McpCaller closed = context(null, false).caller();
        assertThat(registry.visible(open)).extracting(McpSchema.Tool::name).contains("get_state_at", "get_track");
        assertThat(registry.visible(closed)).extracting(McpSchema.Tool::name).doesNotContain("get_state_at", "get_track");
        assertThat(registry.call("get_track", Map.of("sourceId", MOVER, "externalId", "m1"), context(null, false))
                .isError()).isTrue();
    }

    @Test
    @DisplayName("McpTimes parses ISO, offsets, now and relative durations, and rejects the rest")
    void times() {
        Instant now = T.plusSeconds(3600);
        assertThat(McpTimes.parse("asOf", "now", now)).isEqualTo(now);
        assertThat(McpTimes.parse("asOf", "2026-03-01T00:10:00Z", now)).isEqualTo(T.plusSeconds(600));
        assertThat(McpTimes.parse("asOf", "2026-03-01T01:10:00+01:00", now)).isEqualTo(T.plusSeconds(600));
        assertThat(McpTimes.parse("asOf", "-PT2H", now)).isEqualTo(now.minusSeconds(7200));
        assertThat(McpTimes.parse("asOf", "PT30M", now)).isEqualTo(now.minusSeconds(1800));
        assertThat(McpTimes.parse("asOf", "-2h", now)).isEqualTo(now.minusSeconds(7200));
        assertThatThrownBy(() -> McpTimes.parse("asOf", "soon", now)).isInstanceOf(McpToolException.class);
    }
}
