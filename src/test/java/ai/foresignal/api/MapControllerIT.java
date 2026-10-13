package ai.foresignal.api;

import static ai.foresignal.testsupport.tiles.TileTestSources.FIRE_PRIVATE;
import static ai.foresignal.testsupport.tiles.TileTestSources.PLANE;
import static ai.foresignal.testsupport.tiles.TileTestSources.QUAKE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.map.SpriteCatalog;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.tiles.TileTestSources;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code getMapStyle}, {@code getMapSprite} and {@code getMapIcon} over HTTP (prompt 058). */
@IntegrationTest
@Import(TileTestSources.class)
class MapControllerIT {

    @Autowired MockMvc mvc;
    @Autowired JsonMapper json;
    @Autowired AppSettingService settings;
    @Autowired AdminTokenService adminTokens;
    @Autowired SpriteCatalog sprite;

    @BeforeEach
    @AfterEach
    void reset() {
        settings.invalidateCache();
    }

    private JsonNode body(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("the style is version 8 without glyphs, strongly tagged, max-age=300, and 304 on If-None-Match")
    void styleEtag() throws Exception {
        MvcResult result = mvc.perform(get("/v1/map/style.json").param("layers", QUAKE + "," + PLANE))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=300"))
                .andReturn();
        String etag = result.getResponse().getHeader(HttpHeaders.ETAG);
        assertThat(etag).isNotBlank().doesNotStartWith("W/");
        JsonNode style = body(result);
        assertThat(style.get("version").asInt()).isEqualTo(8);
        assertThat(style.has("glyphs")).isFalse();
        assertThat(style.get("sprite").asString()).isEqualTo("http://localhost/v1/map/sprites/" + sprite.spriteName());
        assertThat(style.get("sources").get(QUAKE).get("url").asString())
                .isEqualTo("http://localhost/v1/tiles/" + QUAKE + ".json");
        assertThat(style.get("sources").has(PLANE + "--agg")).isTrue();
        mvc.perform(get("/v1/map/style.json").param("layers", QUAKE + "," + PLANE).header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified())
                .andExpect(header().string(HttpHeaders.ETAG, etag));
        mvc.perform(get("/v1/map/style.json").param("theme", "blue")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("a private layer is omitted for an anonymous caller and present, privately cached, for the admin")
    void privateLayer() throws Exception {
        JsonNode anonymous = body(mvc.perform(get("/v1/map/style.json").param("layers", QUAKE + "," + FIRE_PRIVATE))
                .andExpect(status().isOk()).andReturn());
        assertThat(anonymous.get("sources").has(FIRE_PRIVATE)).isFalse();
        assertThat(anonymous.get("sources").has(QUAKE)).isTrue();
        MvcResult admin = mvc.perform(get("/v1/map/style.json").param("layers", FIRE_PRIVATE)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminTokens.rotate()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "private, max-age=300"))
                .andReturn();
        assertThat(body(admin).get("sources").has(FIRE_PRIVATE)).isTrue();
        // The default selection is every readable source layer.
        JsonNode all = body(mvc.perform(get("/v1/map/style.json")).andReturn());
        assertThat(all.get("sources").has(QUAKE)).isTrue();
        assertThat(all.get("sources").has(FIRE_PRIVATE)).isFalse();
    }

    @Test
    @DisplayName("the current sprite files are immutable for a year, with their media types")
    void spriteHeaders() throws Exception {
        String base = "/v1/map/sprites/" + sprite.spriteName();
        MvcResult index = mvc.perform(get(base + ".json"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/json"))
                .andReturn();
        assertThat(body(index).get("volcano").get("sdf").asBoolean()).isTrue();
        mvc.perform(get(base + ".png"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable"));
        MvcResult retina = mvc.perform(get(base + "@2x.json")).andExpect(status().isOk()).andReturn();
        assertThat(body(retina).get("volcano").get("pixelRatio").asInt()).isEqualTo(2);
        mvc.perform(get(base + "@2x.png")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("a stale sha8 is 404 with a Link to the current sprite")
    void staleSprite() throws Exception {
        String stale = sprite.sha8().equals("00000000") ? "11111111" : "00000000";
        mvc.perform(get("/v1/map/sprites/foresignal-" + stale + ".json"))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.LINK,
                        "<http://localhost/v1/map/sprites/" + sprite.spriteName() + ".json>; rel=\"latest-version\""));
        mvc.perform(get("/v1/map/sprites/other.json")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("a KML icon is a PNG with long-lived caching; an unknown name is 404")
    void icons() throws Exception {
        mvc.perform(get("/v1/map/icons/volcano.png"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=604800"));
        mvc.perform(get("/v1/map/icons/no-such-icon.png")).andExpect(status().isNotFound());
    }
}
