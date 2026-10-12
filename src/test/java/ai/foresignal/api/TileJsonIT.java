package ai.foresignal.api;

import static ai.foresignal.testsupport.tiles.TileTestSources.FIRE_KIND;
import static ai.foresignal.testsupport.tiles.TileTestSources.FIRE_PRIVATE;
import static ai.foresignal.testsupport.tiles.TileTestSources.PLANE;
import static ai.foresignal.testsupport.tiles.TileTestSources.QUAKE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.tiles.TileTestSources;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code GET /v1/tiles.json} and {@code GET /v1/tiles/{layer}.json} (TileJSON 3.0.0). */
@IntegrationTest
@AutoConfigureMockMvc
@Import(TileTestSources.class)
class TileJsonIT {

    /** The keys TileJSON 3.0.0 requires (tilejson, tiles) plus those vector tiles need (vector_layers). */
    static final List<String> REQUIRED = List.of("tilejson", "tiles", "vector_layers");

    @Autowired MockMvc mvc;
    @Autowired JsonMapper json;
    @Autowired AppSettingService settings;
    @Autowired AdminTokenService adminTokens;

    @BeforeEach
    @AfterEach
    void resetSettings() {
        settings.invalidateCache();
    }

    private JsonNode read(String url, String token) throws Exception {
        var request = get(url);
        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return json.readTree(mvc.perform(request).andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString());
    }

    private static List<String> ids(JsonNode index) {
        List<String> ids = new ArrayList<>();
        index.get("layers").forEach(layer -> ids.add(layer.get("id").asString()));
        return ids;
    }

    @Test
    @DisplayName("the index lists source ids and kinds with TileJSON URLs and aggBelowZoom; private sources need a token")
    void index() throws Exception {
        mvc.perform(get("/v1/tiles.json"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=300"))
                .andExpect(header().exists(HttpHeaders.ETAG))
                .andExpect(jsonPath("$.tilejson").value("3.0.0"))
                .andExpect(jsonPath("$.layers[?(@.id == '" + QUAKE + "')].type").value("source"))
                .andExpect(jsonPath("$.layers[?(@.id == '" + QUAKE + "')].tilejson")
                        .value("http://localhost/v1/tiles/" + QUAKE + ".json"))
                .andExpect(jsonPath("$.layers[?(@.id == '" + PLANE + "')].aggBelowZoom").value(6))
                .andExpect(jsonPath("$.layers[?(@.id == '" + FIRE_KIND + "')].type").value("kind"))
                .andExpect(jsonPath("$.layers[?(@.id == '" + FIRE_KIND + "')].aggBelowZoom").value(5));

        JsonNode anonymous = read("/v1/tiles.json", null);
        assertThat(ids(anonymous)).contains(QUAKE, PLANE, FIRE_KIND).doesNotContain(FIRE_PRIVATE);
        JsonNode quake = null;
        for (JsonNode layer : anonymous.get("layers")) {
            if (QUAKE.equals(layer.get("id").asString())) {
                quake = layer;
            }
        }
        assertThat(quake).isNotNull();
        assertThat(quake.has("aggBelowZoom")).isTrue();
        assertThat(quake.get("aggBelowZoom").isNull()).isTrue();

        assertThat(ids(read("/v1/tiles.json", adminTokens.rotate()))).contains(FIRE_PRIVATE);
    }

    @Test
    @DisplayName("a per-layer document carries every TileJSON 3.0.0 key, field descriptions from fieldSpecs and the extensions")
    void perLayerDocument() throws Exception {
        JsonNode doc = read("/v1/tiles/" + QUAKE + ".json", null);
        for (String key : REQUIRED) {
            assertThat(doc.has(key)).as(key).isTrue();
        }
        assertThat(doc.get("tilejson").asString()).isEqualTo("3.0.0");
        assertThat(doc.get("name").asString()).isEqualTo("Tile test source " + QUAKE);
        assertThat(doc.get("description").asString()).isEqualTo("Test earthquakes for tiles");
        assertThat(doc.get("version").asString()).isNotBlank();
        assertThat(doc.get("attribution").asString()).isEqualTo("Attribution of " + QUAKE);
        assertThat(doc.get("scheme").asString()).isEqualTo("xyz");
        assertThat(doc.get("tiles").get(0).asString()).isEqualTo("http://localhost/v1/tiles/" + QUAKE + "/{z}/{x}/{y}.mvt");
        assertThat(doc.get("minzoom").asInt()).isZero();
        assertThat(doc.get("maxzoom").asInt()).isEqualTo(16);
        assertThat(doc.get("bounds").size()).isEqualTo(4);
        assertThat(doc.get("bounds").get(1).asDouble()).isEqualTo(-85.0511);
        JsonNode vectorLayer = doc.get("vector_layers").get(0);
        assertThat(vectorLayer.get("id").asString()).isEqualTo(QUAKE);
        assertThat(vectorLayer.get("minzoom").asInt()).isZero();
        assertThat(vectorLayer.get("maxzoom").asInt()).isEqualTo(16);
        JsonNode fields = vectorLayer.get("fields");
        assertThat(fields.get("mag").asString()).isEqualTo("Magnitude (1)");
        assertThat(fields.get("place").asString()).isEqualTo("String");
        assertThat(fields.get("observedAt").asString()).isEqualTo("Number");
        assertThat(fields.get("live").asString()).isEqualTo("Boolean");
        assertThat(fields.get("title").asString()).isEqualTo("String");
        assertThat(fields.has("bad-name")).isFalse();
        assertThat(doc.has("foresignal:aggBelowZoom")).isTrue();
        assertThat(doc.get("foresignal:aggBelowZoom").isNull()).isTrue();
        List<String> properties = new ArrayList<>();
        doc.get("foresignal:properties").forEach(p -> properties.add(p.asString()));
        assertThat(properties).containsExactly(
                "source", "externalId", "kind", "title", "observedAt", "version", "live", "mag", "place");
    }

    @Test
    @DisplayName("a kind document unions the visible sources: fields, attribution and the lowest aggBelowZoom")
    void kindDocument() throws Exception {
        JsonNode doc = read("/v1/tiles/" + FIRE_KIND + ".json", null);
        assertThat(doc.get("name").asString()).isEqualTo(FIRE_KIND);
        assertThat(doc.get("attribution").asString())
                .isEqualTo("Attribution of tile-fire-a · Attribution of tile-fire-b");
        JsonNode fields = doc.get("vector_layers").get(0).get("fields");
        assertThat(fields.get("frp").asString()).isEqualTo("Fire radiative power (MW)");
        assertThat(fields.get("confidence").asString()).isEqualTo("String");
        assertThat(fields.has("owner")).isFalse();
        assertThat(doc.get("foresignal:aggBelowZoom").asInt()).isEqualTo(5);

        JsonNode withToken = read("/v1/tiles/" + FIRE_KIND + ".json", adminTokens.rotate());
        assertThat(withToken.get("vector_layers").get(0).get("fields").has("owner")).isTrue();
        assertThat(withToken.get("foresignal:aggBelowZoom").asInt()).isEqualTo(3);
    }

    @Test
    @DisplayName("tile URLs use server.publicBaseUrl when it is set")
    void publicBaseUrl() throws Exception {
        settings.put(SettingKeys.SERVER, new ServerSettings("https://maps.example.org/"));
        JsonNode doc = read("/v1/tiles/" + QUAKE + ".json", null);
        assertThat(doc.get("tiles").get(0).asString())
                .isEqualTo("https://maps.example.org/v1/tiles/" + QUAKE + "/{z}/{x}/{y}.mvt");
        JsonNode index = read("/v1/tiles.json", null);
        assertThat(index.get("layers").get(0).get("tilejson").asString()).startsWith("https://maps.example.org/v1/tiles/");
    }

    @Test
    @DisplayName("without publicBaseUrl the request's scheme, host and port are used; X-Forwarded headers are ignored")
    void requestBase() throws Exception {
        String body = mvc.perform(get("https://tiles.local:8443/v1/tiles/" + QUAKE + ".json")
                        .header("X-Forwarded-Host", "evil.example"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(json.readTree(body).get("tiles").get(0).asString())
                .isEqualTo("https://tiles.local:8443/v1/tiles/" + QUAKE + "/{z}/{x}/{y}.mvt");
    }

    @Test
    @DisplayName("ETag and 304 on both documents")
    void etags() throws Exception {
        for (String url : List.of("/v1/tiles.json", "/v1/tiles/" + QUAKE + ".json")) {
            String etag = mvc.perform(get(url)).andReturn().getResponse().getHeader(HttpHeaders.ETAG);
            assertThat(etag).startsWith("\"");
            mvc.perform(get(url).header(HttpHeaders.IF_NONE_MATCH, etag))
                    .andExpect(status().isNotModified())
                    .andExpect(header().string(HttpHeaders.ETAG, etag));
        }
    }

    @Test
    @DisplayName("404 UNKNOWN_LAYER for an unknown layer and for a private source asked anonymously")
    void unknownLayer() throws Exception {
        mvc.perform(get("/v1/tiles/no-such-layer.json"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNKNOWN_LAYER"));
        mvc.perform(get("/v1/tiles/" + FIRE_PRIVATE + ".json"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNKNOWN_LAYER"));
    }
}
