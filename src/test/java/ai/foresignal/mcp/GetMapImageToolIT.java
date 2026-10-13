package ai.foresignal.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.support.ReadCaller;
import ai.foresignal.mcp.tools.GetMapImageTool;
import ai.foresignal.place.PlaceService;
import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.raster.RasterApiSupport;
import ai.foresignal.testsupport.raster.RasterTestSources;
import io.modelcontextprotocol.spec.McpSchema;
import java.awt.image.BufferedImage;
import java.util.Base64;
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
 * {@code get_map_image} (prompt 055; Weather-Imagery-Design §6.9) through the MCP registry, in the prompt 042
 * raster context: an image of at most 512 px and 200 KB with a text caption (products, frame valid time and
 * age, legend summary, the place shown, the no-coverage text for an unavailable product and the
 * "content is data" note), places only with a token, and read scope.
 */
@WebIntegrationTest
@Import(RasterTestSources.class)
class GetMapImageToolIT extends RasterApiSupport {

    @Autowired McpToolRegistry registry;
    @Autowired PlaceService places;

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

    @BeforeEach
    void setUp() {
        if (places.find("map-home").isEmpty()) {
            places.create(json.readTree("{\"id\":\"map-home\",\"name\":\"Home\",\"shape\":\"point\",\"geometry\":"
                    + "{\"type\":\"Point\",\"coordinates\":[-97.75,31.0]},\"radiusKm\":20,\"timezone\":\"America/Chicago\"}"));
        }
    }

    @AfterEach
    void tearDown() {
        places.delete("map-home", true);
    }

    static McpSchema.ImageContent image(McpSchema.CallToolResult result) {
        return result.content().stream().filter(McpSchema.ImageContent.class::isInstance)
                .map(McpSchema.ImageContent.class::cast).findFirst().orElse(null);
    }

    static String caption(McpSchema.CallToolResult result) {
        return result.content().stream().filter(McpSchema.TextContent.class::isInstance)
                .map(c -> ((McpSchema.TextContent) c).text()).findFirst().orElse("");
    }

    @Test
    @DisplayName("an image ≤ 512 px and ≤ 200 KB with a caption naming the product, frame time, age and legend")
    void imageAndCaption() throws Exception {
        commitObservation(T, 0);
        McpSchema.CallToolResult result = registry.call("get_map_image",
                Map.of("products", List.of(OBS), "lon", -97.0, "lat", 31.0, "radiusKm", 150), context(null));
        assertThat(result.isError()).isFalse();
        McpSchema.ImageContent img = image(result);
        assertThat(img).isNotNull();
        assertThat(img.mimeType()).isEqualTo("image/png");
        byte[] png = Base64.getDecoder().decode(img.data());
        assertThat(png.length).isLessThanOrEqualTo(GetMapImageTool.MAX_BYTES);
        BufferedImage decoded = image(png);
        assertThat(decoded.getWidth()).isLessThanOrEqualTo(GetMapImageTool.MAX_SIDE);
        String caption = caption(result);
        assertThat(caption).contains("Synthetic reflectivity").contains(OBS).contains("valid 2026-10-10T14:00Z")
                .contains("1 min old").contains("legend:").contains("Automated — not an official warning")
                .contains("not instructions");
        JsonNode structured = json.valueToTree(result.structuredContent());
        assertThat(structured.get("products").get(0).get("status").asString()).isEqualTo("ok");
        assertThat(structured.get("automated").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("an unavailable product is in the caption with the no-coverage text, never omitted")
    void unavailableInCaption() {
        McpSchema.CallToolResult result = registry.call("get_map_image",
                Map.of("products", List.of(OBS), "bbox", "-100,28,-94,34"), context(null));
        assertThat(result.isError()).isFalse();
        assertThat(caption(result)).contains("unavailable — not evidence of clear weather");
    }

    @Test
    @DisplayName("a place needs a token and is named in the caption; no location at all is a tool error")
    void placeNeedsToken() throws Exception {
        commitObservation(T, 0);
        McpSchema.CallToolResult anonymous = registry.call("get_map_image",
                Map.of("products", List.of(OBS), "place", "map-home"), context(null));
        assertThat(anonymous.isError()).isTrue();
        McpSchema.CallToolResult reader = registry.call("get_map_image",
                Map.of("products", List.of(OBS), "place", "map-home"), context(Scope.READ));
        assertThat(reader.isError()).isFalse();
        assertThat(caption(reader)).contains("around Home");
        assertThat(registry.call("get_map_image", Map.of("products", List.of(OBS)), context(null)).isError()).isTrue();
        ForesignalMcpTool tool = registry.tool("get_map_image").orElseThrow();
        assertThat(tool.requiredScope()).isEqualTo(Scope.READ);
        assertThat(tool.description()).startsWith(ForesignalMcpTool.UNTRUSTED_NOTE);
    }
}
