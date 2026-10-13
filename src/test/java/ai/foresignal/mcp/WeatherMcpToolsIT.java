package ai.foresignal.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.tools.ProductLegendResource;
import ai.foresignal.mcp.tools.ProductsResource;
import ai.foresignal.place.PlaceService;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.source.adapters.mrms.ProbSevereAdapter;
import ai.foresignal.source.adapters.nws.NwsAlertsAdapter;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.raster.RasterApiSupport;
import ai.foresignal.testsupport.raster.RasterTestSources;
import io.modelcontextprotocol.spec.McpSchema;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;

/**
 * The weather MCP tools (prompt 052; Weather-Imagery-Design §6.9): compact output within 4 KB and
 * {@code detail=long} within 50 KB, every value with {@code validTime}, {@code localTime}, {@code ageMinutes},
 * {@code run}, {@code coverage} and {@code status} (with the no-coverage text), nearby hazards with their
 * {@code automated} flag, places only with a token, agent scope for the place write tools, and the
 * {@code foresignal://products} resource and {@code foresignal://products/{id}/legend} template.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class WeatherMcpToolsIT extends RasterApiSupport {

    @Autowired McpToolRegistry registry;
    @Autowired McpProtocol protocol;
    @Autowired PlaceService places;
    @Autowired TestRecords records;
    @Autowired ProductsResource productsResource;
    @Autowired ProductLegendResource legendResource;

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

    McpSchema.CallToolResult call(String tool, Map<String, Object> args, Scope scope) {
        return registry.call(tool, args, context(scope));
    }

    JsonNode structured(McpSchema.CallToolResult result) {
        return json.valueToTree(result.structuredContent());
    }

    static int size(McpSchema.CallToolResult result) {
        StringBuilder out = new StringBuilder();
        result.content().forEach(c -> {
            if (c instanceof McpSchema.TextContent t) {
                out.append(t.text());
            }
        });
        return out.toString().getBytes(StandardCharsets.UTF_8).length;
    }

    @BeforeEach
    void setUp() {
        if (places.find("mcp-home").isEmpty()) {
            places.create(json.readTree("{\"id\":\"mcp-home\",\"name\":\"Home\",\"shape\":\"point\",\"geometry\":"
                    + "{\"type\":\"Point\",\"coordinates\":[-97.75,31.0]},\"radiusKm\":20,\"timezone\":\"America/Chicago\"}"));
        }
    }

    @AfterEach
    void tearDown() {
        places.delete("mcp-home", true);
    }

    @Test
    @DisplayName("sample_weather: every value carries validTime, localTime, ageMinutes, run, coverage and status; 4 KB")
    void sampleWeather() throws Exception {
        commitObservation(T, 0);
        McpSchema.CallToolResult result = call("sample_weather",
                Map.of("lon", -97.75, "lat", 31.0, "products", List.of(OBS)), null);
        assertThat(result.isError()).isFalse();
        assertThat(size(result)).isLessThanOrEqualTo(McpOutputShaper.COMPACT_LIMIT_BYTES);
        JsonNode v = structured(result).get("values").get(0);
        assertThat(v.get("status").asString()).isEqualTo("ok");
        for (String key : List.of("validTime", "localTime", "ageMinutes", "run", "coverage", "status")) {
            assertThat(v.has(key)).as(key).isTrue();
        }
        JsonNode off = structured(call("sample_weather", Map.of("lon", -99.25, "lat", 33.0, "products", List.of(OBS)),
                null)).get("values").get(0);
        assertThat(off.get("status").asString()).isEqualTo("no-coverage");
        assertThat(off.get("statusText").asString())
                .isEqualTo("outside product coverage; this is not evidence of clear weather");
        assertThat(registry.tool("sample_weather").orElseThrow().description())
                .startsWith(ForesignalMcpTool.UNTRUSTED_NOTE);
    }

    @Test
    @DisplayName("sample_weather lists nearby hazards with the automated flag: NWS alerts false, ProbSevere true")
    void hazards() {
        clock.setInstant(T);
        records.ingest(NwsAlertsAdapter.ID, NormalizedRecord.builder("urn:test:alert", NwsAlertsAdapter.KIND)
                .observedAt(T).geometry(Geo.point(-97.75, 31.0).buffer(0.2)).put("event", "Severe Thunderstorm Warning")
                .put("headline", "Test warning").build());
        records.ingest(ProbSevereAdapter.ID, NormalizedRecord.builder("123456", ProbSevereAdapter.KIND).observedAt(T)
                .point(-97.6, 31.1).put("probSevere", 44).put("automated", true).put("notAWarning", true).build());
        JsonNode out = structured(call("sample_weather", Map.of("lon", -97.75, "lat", 31.0, "products", List.of(OBS),
                "detail", "long"), Scope.READ));
        JsonNode alerts = out.get("hazards").get("nwsAlerts");
        JsonNode probSevere = out.get("hazards").get("probSevere");
        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).get("automated").asBoolean()).isFalse();
        assertThat(probSevere).hasSize(1);
        assertThat(probSevere.get(0).get("automated").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("a place needs a token; anonymous callers get a tool error, readers get placeId without coordinates")
    void placeNeedsToken() throws Exception {
        commitObservation(T, 0);
        assertThat(call("sample_weather", Map.of("place", "mcp-home", "products", List.of(OBS)), null).isError()).isTrue();
        McpSchema.CallToolResult read = call("sample_weather", Map.of("place", "mcp-home", "products", List.of(OBS)),
                Scope.READ);
        assertThat(read.isError()).isFalse();
        JsonNode doc = structured(read);
        assertThat(doc.get("placeId").asString()).isEqualTo("mcp-home");
        assertThat(doc.has("lon")).isFalse();
        assertThat(doc.get("values").get(0).get("localTime").asString()).endsWith("-05:00");
        assertThat(call("weather_forecast", Map.of("place", "mcp-home"), null).isError()).isTrue();
        assertThat(call("area_weather_stats", Map.of("product", OBS, "place", "mcp-home"), null).isError()).isTrue();
    }

    @Test
    @DisplayName("list_products, weather_forecast and area_weather_stats: compact sizes and status fields")
    void otherTools() throws Exception {
        commitObservation(T, 0);
        McpSchema.CallToolResult list = call("list_products", Map.of(), null);
        assertThat(list.isError()).isFalse();
        assertThat(size(list)).isLessThanOrEqualTo(McpOutputShaper.COMPACT_LIMIT_BYTES);
        McpSchema.CallToolResult longList = call("list_products", Map.of("detail", "long"), null);
        assertThat(size(longList)).isLessThanOrEqualTo(McpOutputShaper.LONG_LIMIT_BYTES);
        assertThat(structured(longList).get("items").toString()).contains(OBS).contains("\"health\"");

        McpSchema.CallToolResult forecast = call("weather_forecast", Map.of("lon", -97.75, "lat", 31.0), null);
        assertThat(forecast.isError()).isFalse();
        assertThat(size(forecast)).isLessThanOrEqualTo(McpOutputShaper.COMPACT_LIMIT_BYTES);
        for (JsonNode p : structured(forecast).get("providers")) {
            assertThat(p.has("run")).isTrue();
            assertThat(p.has("status")).isTrue();
            assertThat(p.has("coverage")).isTrue();
        }

        McpSchema.CallToolResult stats = call("area_weather_stats", Map.of("product", OBS, "bbox", "-100,28,-94,34",
                "thresholds", List.of(10)), null);
        assertThat(stats.isError()).isFalse();
        JsonNode s = structured(stats);
        assertThat(s.get("status").asString()).isEqualTo("ok");
        for (String key : List.of("validTime", "localTime", "ageMinutes", "run", "coverage", "status")) {
            assertThat(s.has(key)).as(key).isTrue();
        }
        assertThat(size(stats)).isLessThanOrEqualTo(McpOutputShaper.COMPACT_LIMIT_BYTES);
        assertThat(call("area_weather_stats", Map.of("product", OBS), null).isError()).isTrue();
    }

    @Test
    @DisplayName("place writes stay agent-scoped: create_place and delete_place refuse read tokens")
    void placeToolScopes() {
        assertThat(registry.tool("create_place").orElseThrow().requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(registry.tool("delete_place").orElseThrow().requiredScope()).isEqualTo(Scope.AGENT);
        assertThat(call("delete_place", Map.of("placeId", "mcp-home"), Scope.READ).isError()).isTrue();
        for (String tool : List.of("list_products", "sample_weather", "weather_forecast", "area_weather_stats")) {
            assertThat(registry.tool(tool).orElseThrow().requiredScope()).as(tool).isEqualTo(Scope.READ);
        }
    }

    @Test
    @DisplayName("foresignal://products lists products; foresignal://products/{id}/legend is a listed template")
    void resources() throws Exception {
        assertThat(protocol.resources()).containsKey("foresignal://products");
        assertThat(protocol.templates()).containsKey("foresignal://products/{id}/legend");
        assertThat(productsResource.read(context(null))).contains(OBS);
        assertThat(legendResource.matches("foresignal://products/" + OBS + "/legend")).isTrue();
        assertThat(legendResource.matches("foresignal://products/" + OBS)).isFalse();
        JsonNode legend = json.readTree(legendResource.read(context(null), "foresignal://products/" + OBS + "/legend"));
        assertThat(legend.isObject()).isTrue();
        assertThat(protocol.listTemplates(new McpCaller(null, new ReadCaller(null, false, true), Set.of(), "1.2.3.4")))
                .extracting(McpSchema.ResourceTemplate::uriTemplate).contains("foresignal://products/{id}/legend");
    }
}
