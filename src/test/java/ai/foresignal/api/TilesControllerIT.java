package ai.foresignal.api;

import static ai.foresignal.testsupport.tiles.TileTestSources.PLANE;
import static ai.foresignal.testsupport.tiles.TileTestSources.PLANE_KIND;
import static ai.foresignal.testsupport.tiles.TileTestSources.QUAKE;
import static ai.foresignal.testsupport.tiles.TileTestSources.QUAKE_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.tiles.TileCoordinates;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.tiles.MvtDecoder;
import ai.foresignal.testsupport.tiles.TileMath;
import ai.foresignal.testsupport.tiles.TileTestSources;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** {@code GET /v1/tiles/{layer}/{z}/{x}/{y}.mvt}: media type, cache headers, ETag, 400 and 404. */
@IntegrationTest
@AutoConfigureMockMvc
@Import(TileTestSources.class)
class TilesControllerIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final double LON = -79.38;
    static final double LAT = 43.65;

    @Autowired MockMvc mvc;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired AppSettingService settings;
    @Autowired AdminTokenService adminTokens;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(T);
    }

    @AfterEach
    void tearDown() {
        settings.invalidateCache();
    }

    private static String url(String layer, TileCoordinates tile) {
        return "/v1/tiles/" + layer + "/" + tile.z() + "/" + tile.x() + "/" + tile.y() + ".mvt";
    }

    private void seed() {
        records.ingestAt(QUAKE, T,
                NormalizedRecord.builder("q1", QUAKE_KIND).observedAt(T).point(LON, LAT).put("mag", 4.5).build());
        records.ingestAt(PLANE, T,
                NormalizedRecord.builder("p1", PLANE_KIND).observedAt(T).point(LON, LAT).put("callsign", "A").build());
    }

    @Test
    @DisplayName("a raw tile: the MVT media type, public max-age=5, a strong ETag, Vary, and {y} bound before .mvt")
    void rawTile() throws Exception {
        seed();
        TileCoordinates tile = TileMath.tileFor(LON, LAT, 10);
        MvcResult result = mvc.perform(get(url(QUAKE, tile)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.mapbox-vector-tile"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=5"))
                .andExpect(header().string(HttpHeaders.VARY, "Accept-Encoding, Authorization"))
                .andExpect(header().doesNotExist(TilesController.TRUNCATED_HEADER))
                .andReturn();
        assertThat(result.getResponse().getHeader(HttpHeaders.ETAG)).startsWith("\"b-");
        assertThat(MvtDecoder.features(result.getResponse().getContentAsByteArray()))
                .singleElement()
                .satisfies(f -> assertThat(f.get("externalId")).isEqualTo("q1"));
    }

    @Test
    @DisplayName("an aggregated tile is cached for aggMaxAge (60 s)")
    void aggregatedCacheHeader() throws Exception {
        seed();
        mvc.perform(get(url(PLANE, TileMath.tileFor(LON, LAT, 3))))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=60"));
        mvc.perform(get(url(QUAKE, TileMath.tileFor(LON, LAT, 12))).param("agg", "grid"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=60"));
        mvc.perform(get(url(PLANE, TileMath.tileFor(LON, LAT, 3))).param("agg", "none"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "public, max-age=5"));
    }

    @Test
    @DisplayName("Cache-Control is private when openReads is off (and a token is then required)")
    void privateWhenReadsClosed() throws Exception {
        seed();
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        String token = adminTokens.rotate();
        TileCoordinates tile = TileMath.tileFor(LON, LAT, 10);
        mvc.perform(get(url(QUAKE, tile))).andExpect(status().isUnauthorized());
        mvc.perform(get(url(QUAKE, tile)).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "private, max-age=5"));
    }

    @Test
    @DisplayName("ETag and 304 on If-None-Match; the tag changes when the tile changes")
    void etag() throws Exception {
        seed();
        TileCoordinates tile = TileMath.tileFor(LON, LAT, 10);
        String etag = mvc.perform(get(url(QUAKE, tile))).andReturn().getResponse().getHeader(HttpHeaders.ETAG);
        mvc.perform(get(url(QUAKE, tile)).header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified())
                .andExpect(header().string(HttpHeaders.ETAG, etag));
        clock.setInstant(T.plus(Duration.ofMinutes(1)));
        records.ingestAt(QUAKE, clock.instant(), NormalizedRecord.builder("q2", QUAKE_KIND)
                .observedAt(clock.instant()).point(LON, LAT).put("mag", 2.0).build());
        String changed = mvc.perform(get(url(QUAKE, tile)).header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isOk())
                .andReturn().getResponse().getHeader(HttpHeaders.ETAG);
        assertThat(changed).isNotEqualTo(etag);
    }

    @Test
    @DisplayName("an empty tile is 200 with an empty body")
    void emptyTile() throws Exception {
        MvcResult result = mvc.perform(get("/v1/tiles/" + QUAKE + "/4/3/5.mvt"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.mapbox-vector-tile"))
                .andReturn();
        assertThat(result.getResponse().getContentAsByteArray()).isEmpty();
    }

    @ParameterizedTest(name = "400 for {0}")
    @ValueSource(strings = {
        "/v1/tiles/tile-quake/17/0/0.mvt",
        "/v1/tiles/tile-quake/3/8/0.mvt",
        "/v1/tiles/tile-quake/3/0/8.mvt",
        "/v1/tiles/tile-quake/3/-1/0.mvt",
        "/v1/tiles/tile-quake/a/0/0.mvt",
        "/v1/tiles/tile-quake/3/0/0.mvt?agg=cluster",
    })
    @DisplayName("400 VALIDATION_FAILED for out-of-range z/x/y, malformed numbers and a bad agg")
    void badRequests(String url) throws Exception {
        mvc.perform(get(url))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("404 UNKNOWN_LAYER for an unknown layer")
    void unknownLayer() throws Exception {
        mvc.perform(get("/v1/tiles/no-such-layer/3/0/0.mvt"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("UNKNOWN_LAYER"));
    }

    @Test
    @DisplayName("the TileJSON index is a read: open while openReads is on")
    void indexIsOpen() throws Exception {
        mvc.perform(get("/v1/tiles.json")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("the request base omits default ports and brackets IPv6 hosts")
    void requestBaseFormatting() {
        assertThat(TilesController.requestBase("http", "example.org", 80)).isEqualTo("http://example.org");
        assertThat(TilesController.requestBase("https", "example.org", 443)).isEqualTo("https://example.org");
        assertThat(TilesController.requestBase("http", "example.org", 8080)).isEqualTo("http://example.org:8080");
        assertThat(TilesController.requestBase("http", "::1", 8080)).isEqualTo("http://[::1]:8080");
        assertThat(TilesController.cacheControl(true, Duration.ofSeconds(60))).isEqualTo("public, max-age=60");
        assertThat(TilesController.cacheControl(false, Duration.ofSeconds(5))).isEqualTo("private, max-age=5");
    }
}
