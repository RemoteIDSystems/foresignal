package ai.foresignal.mcp;

import static ai.foresignal.testsupport.reference.RegionTestSources.POINTS;
import static ai.foresignal.testsupport.reference.RegionTestSources.POINT_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.reference.RegionAttribution;
import ai.foresignal.reference.RegionTagWorker;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.reference.ReferenceFixtures;
import ai.foresignal.testsupport.reference.RegionTestSources;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * The MCP side of prompt 040: {@code reverse_geocode} (read) returns the {@code RegionTags} of a point and
 * refuses coordinates out of range, and short record items carry the {@code near} text of a tagged record.
 */
@IntegrationTest
@Import(RegionTestSources.class)
class McpReverseGeocodeToolIT {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant T = Instant.parse("2026-06-05T12:00:00Z");

    @Autowired McpToolRegistry registry;
    @Autowired ApplicationContext context;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired RegionTagWorker worker;
    @Autowired ChangeBus changeBus;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        ReferenceFixtures.loadAll(context);
        await().atMost(Duration.ofSeconds(15)).until(() -> !worker.retagging());
    }

    static ToolContext context(Scope scope) {
        if (scope == null) {
            return new ToolContext(new McpCaller(null, new ReadCaller(null, false, true), Set.of(), "127.0.0.1"),
                    "session", McpSessions.STREAMABLE);
        }
        AuthenticatedToken token = new AuthenticatedToken(UUID.randomUUID(), "bot-" + scope.wireValue(), scope,
                TokenRestrictions.NONE, null, "principal-1", false);
        return new ToolContext(new McpCaller(token, new ReadCaller(scope, false, true), Set.of(), "10.0.0.2"),
                "session", McpSessions.STREAMABLE);
    }

    static String text(McpSchema.CallToolResult result) {
        StringBuilder out = new StringBuilder();
        result.content().forEach(c -> {
            if (c instanceof McpSchema.TextContent t) {
                out.append(t.text());
            }
        });
        if (result.structuredContent() != null) {
            out.append(JSON.writeValueAsString(result.structuredContent()));
        }
        return out.toString();
    }

    @Test
    @DisplayName("reverse_geocode returns the RegionTags of a point, with the GeoNames credit")
    void reverseGeocode() {
        McpSchema.CallToolResult result = registry.call("reverse_geocode", Map.of("lon", -97.70, "lat", 30.37),
                context(Scope.READ));
        assertThat(result.isError()).isFalse();
        assertThat(text(result)).contains("\"iso2\":\"US\"").contains("\"admin1\":\"US-TX\"")
                .contains("12 km NNE of Austin, TX").contains(RegionAttribution.GEONAMES);
        assertThat(registry.call("reverse_geocode", Map.of("lon", -85.0, "lat", 25.0), context(null)).isError())
                .as("read tools are open to anonymous callers under openReads").isFalse();
    }

    @Test
    @DisplayName("out-of-range or missing coordinates are refused")
    void invalid() {
        assertThat(registry.call("reverse_geocode", Map.of("lon", 200, "lat", 0), context(Scope.READ)).isError())
                .isTrue();
        assertThat(registry.call("reverse_geocode", Map.of("lon", 0), context(Scope.READ)).isError()).isTrue();
    }

    @Test
    @DisplayName("a tagged record's short MCP item carries near")
    void shortRecordNear() {
        records.ingest(POINTS, NormalizedRecord.builder("mcp1", POINT_KIND).observedAt(T).point(-97.70, 30.37)
                .put("name", "MCP point").build());
        await().atMost(Duration.ofSeconds(15)).until(() -> worker.cursor() >= changeBus.lastPublishedSeq());
        McpSchema.CallToolResult result = registry.call("get_record", Map.of("source", POINTS, "externalId", "mcp1"),
                context(Scope.READ));
        assertThat(result.isError()).isFalse();
        assertThat(text(result)).contains("\"near\":\"12 km NNE of Austin, TX\"");
    }
}
