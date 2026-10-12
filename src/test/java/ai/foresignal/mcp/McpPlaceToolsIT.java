package ai.foresignal.mcp;

import static ai.foresignal.testsupport.place.PlaceTestSources.EVENTS;
import static ai.foresignal.testsupport.place.PlaceTestSources.EVENT_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.tools.PlacesResource;
import ai.foresignal.place.PlaceService;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.place.PlaceTestSources;
import ai.foresignal.watch.WatchlistService;
import io.modelcontextprotocol.spec.McpSchema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * The place, view and watchlist MCP tools (prompt 036): their scopes (place reads always need a
 * token; writes and watchlists need agent), compact output without any place coordinates for read
 * tokens, coordinates only with {@code detail=long} for agent tokens, and the
 * {@code foresignal://places} resource under the same rule.
 */
@IntegrationTest
@Import(PlaceTestSources.class)
class McpPlaceToolsIT {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant T = Instant.parse("2026-05-01T12:00:00Z");

    /** A distinctive place coordinate that must never reach a read token. */
    static final String SECRET_LAT = "45.6789";

    @Autowired McpToolRegistry registry;
    @Autowired PlaceService places;
    @Autowired WatchlistService watchlists;
    @Autowired PlacesResource placesResource;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        places.create(JSON.readTree("{\"id\":\"home\",\"name\":\"Home\",\"shape\":\"point\",\"geometry\":"
                + "{\"type\":\"Point\",\"coordinates\":[12.3456," + SECRET_LAT + "]},\"radiusKm\":10}"));
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

    McpSchema.CallToolResult call(String tool, Map<String, Object> args, Scope scope) {
        return registry.call(tool, args, context(scope));
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
    @DisplayName("place reads always need a token; read tokens get compact output without coordinates, agents get them with detail=long")
    void placeReads() {
        for (String tool : List.of("list_places", "get_place", "records_near_place")) {
            Map<String, Object> args = tool.equals("list_places") ? Map.of()
                    : tool.equals("get_place") ? Map.of("placeId", "home") : Map.of("placeIds", List.of("home"));
            assertThat(call(tool, args, null).isError()).as(tool + " anonymous").isTrue();
        }
        McpSchema.CallToolResult read = call("list_places", Map.of("detail", "long"), Scope.READ);
        assertThat(read.isError()).isFalse();
        assertThat(text(read)).contains("Home").contains("\"radiusKm\"").doesNotContain(SECRET_LAT)
                .doesNotContain("coordinates");
        assertThat(text(call("get_place", Map.of("placeId", "home"), Scope.READ))).doesNotContain(SECRET_LAT);

        McpSchema.CallToolResult agentShort = call("get_place", Map.of("placeId", "home"), Scope.AGENT);
        assertThat(text(agentShort)).doesNotContain(SECRET_LAT);
        McpSchema.CallToolResult agentLong = call("get_place", Map.of("placeId", "home", "detail", "long"), Scope.AGENT);
        assertThat(text(agentLong)).contains(SECRET_LAT).contains("area");
        assertThat(call("get_place", Map.of("placeId", "nowhere"), Scope.READ).isError()).isTrue();
    }

    @Test
    @DisplayName("records_near_place returns the records inside the place and its name, never its coordinates")
    void recordsNearPlace() {
        records.ingest(EVENTS, NormalizedRecord.builder("close", EVENT_KIND).observedAt(T).point(12.36, 45.69)
                .put("name", "close").build());
        records.ingest(EVENTS, NormalizedRecord.builder("away", EVENT_KIND).observedAt(T).point(13.5, 45.69)
                .put("name", "away").build());
        McpSchema.CallToolResult result = call("records_near_place", Map.of("placeIds", List.of("home")), Scope.READ);
        assertThat(result.isError()).isFalse();
        String out = text(result);
        assertThat(out).contains("close").doesNotContain("\"away\"").contains("Home").doesNotContain(SECRET_LAT);
        assertThat(text(call("records_near_place", Map.of("placeIds", List.of("home"), "radiusKm", 200), Scope.READ)))
                .contains("away");
    }

    @Test
    @DisplayName("writes need agent: create, update and delete a place; create and render a view; watchlists are agent-only")
    void scopes() {
        Map<String, Object> place = Map.of("id", "work", "name", "Work", "shape", "point",
                "geometry", Map.of("type", "Point", "coordinates", List.of(1.0, 2.0)));
        assertThat(call("create_place", Map.of("place", place), Scope.READ).isError()).isTrue();
        McpSchema.CallToolResult created = call("create_place", Map.of("place", place), Scope.AGENT);
        assertThat(created.isError()).isFalse();
        assertThat(text(created)).contains("work").doesNotContain("coordinates");
        Map<String, Object> moved = Map.of("id", "work", "name", "Work", "shape", "point",
                "geometry", Map.of("type", "Point", "coordinates", List.of(1.5, 2.0)));
        assertThat(call("update_place", Map.of("placeId", "work", "place", moved), Scope.AGENT).isError()).isFalse();
        assertThat(call("delete_place", Map.of("placeId", "work"), Scope.READ).isError()).isTrue();
        assertThat(call("delete_place", Map.of("placeId", "work"), Scope.AGENT).isError()).isFalse();

        Map<String, Object> view = Map.of("slug", "home-events", "name", "Home events", "placeIds", List.of("home"));
        assertThat(call("create_view", Map.of("view", view), Scope.READ).isError()).isTrue();
        assertThat(call("create_view", Map.of("view", view), Scope.AGENT).isError()).isFalse();
        assertThat(text(call("list_views", Map.of(), null))).contains("home-events");
        assertThat(call("get_view_records", Map.of("viewId", "home-events"), null).isError())
                .as("a view with a place needs a token").isTrue();
        assertThat(call("get_view_records", Map.of("viewId", "home-events"), Scope.READ).isError()).isFalse();

        assertThat(call("list_watchlists", Map.of(), Scope.READ).isError()).isTrue();
        UUID list = watchlists.create(JSON.readTree("{\"name\":\"Tails\",\"identity\":\"icao24\"}")).getId();
        assertThat(call("add_to_watchlist", Map.of("watchlistId", list.toString(), "items",
                List.of(Map.of("value", "a1b2c3"), Map.of("value", "nothex"))), Scope.AGENT).isError()).isFalse();
        assertThat(text(call("get_watchlist", Map.of("watchlistId", list.toString()), Scope.AGENT))).contains("A1B2C3");
        assertThat(text(call("list_watchlists", Map.of(), Scope.AGENT))).contains("Tails");
        assertThat(call("remove_from_watchlist", Map.of("watchlistId", list.toString(), "values", List.of("A1B2C3")),
                Scope.AGENT).isError()).isFalse();
        assertThat(watchlists.get(list).getItemCount()).isZero();
    }

    @Test
    @DisplayName("foresignal://places needs a token; read tokens see names and radii, agents also coordinates")
    void resource() {
        assertThat(McpProtocol.allowed(placesResource, context(null).caller())).isFalse();
        assertThat(McpProtocol.allowed(placesResource, context(Scope.READ).caller())).isTrue();
        assertThat(placesResource.read(context(Scope.READ))).contains("Home").contains("radiusKm")
                .doesNotContain(SECRET_LAT);
        assertThat(placesResource.read(context(Scope.AGENT))).contains(SECRET_LAT);
    }
}
