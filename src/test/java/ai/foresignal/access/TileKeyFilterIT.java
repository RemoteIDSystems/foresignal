package ai.foresignal.access;

import static ai.foresignal.testsupport.records.RecordsTestSources.OTHER_KIND;
import static ai.foresignal.testsupport.records.RecordsTestSources.PRIVATE;
import static ai.foresignal.testsupport.records.RecordsTestSources.PRIVATE_KIND;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_A;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_B;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.security.ratelimit.AuthFailureBackoff;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The tile-key carrier through the real filter chain (prompt 050): {@code openReads} is off, so the key
 * is what authenticates. A valid key forwards to the canonical path and returns the same body and ETag
 * as a bearer request; a wrong prefix is {@code 403 INSUFFICIENT_SCOPE}; POST and PATCH are 405;
 * unknown, revoked and expired keys are {@code 401 INVALID_TILE_KEY} and count against the
 * auth-failure backoff; a key's restriction (sources, kinds, bbox) clamps the MVT; private sources are
 * never returned. Also the agent-scoped management API.
 *
 * <p>The fixture is 035's leak-matrix fixture: {@code in-a1} is the only record inside
 * {@code sources=[test-schema-a], kinds=[test-quake], bbox=[0,0,10,10]}; every other external id
 * starts with {@code out-}.
 */
@IntegrationTest
@Import(TriggerTestSources.class)
class TileKeyFilterIT {

    static final Instant T = Instant.parse("2026-06-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String TILE = "/tiles/" + SCHEMA_KIND + "/0/0/0.mvt?agg=none";

    @Autowired MockMvc mvc;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired AppSettingService settings;
    @Autowired AdminTokenService adminTokens;
    @Autowired TokenService tokens;
    @Autowired TileKeyService tileKeys;
    @Autowired AuthFailureBackoff backoff;
    @Autowired MeterRegistry meters;

    String admin;
    String reader;
    String open;
    String narrow;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        clock.setInstant(T);
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        admin = "Bearer " + adminTokens.rotate();
        reader = TestTokens.bearer(TestTokens.create(tokens, "tiles-reader", Scope.READ));
        open = tileKeys.create(JSON.readTree("{\"name\":\"open\",\"prefixes\":[\"/v1/tiles/\",\"/v1/changes/stream\"],"
                + "\"ttl\":\"7d\"}")).key();
        narrow = tileKeys.create(JSON.readTree("{\"name\":\"narrow\",\"prefixes\":[\"/v1/tiles/\"],\"ttl\":\"7d\","
                + "\"restrictions\":{\"sources\":[\"" + SCHEMA_A + "\"],\"kinds\":[\"" + SCHEMA_KIND + "\"],"
                + "\"bbox\":[0,0,10,10]}}")).key();
        records.ingestAt(SCHEMA_A, T, rec("in-a1", SCHEMA_KIND, 5, 5), rec("out-a2", SCHEMA_KIND, 50, 50));
        records.ingestAt(SCHEMA_B, T, rec("out-b1", SCHEMA_KIND, 5, 5), rec("out-b2", OTHER_KIND, 6, 6));
        records.ingestAt(PRIVATE, T, rec("out-p1", PRIVATE_KIND, 5, 5));
        backoff.reset();
    }

    @AfterEach
    void tearDown() {
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
        backoff.reset();
    }

    private static NormalizedRecord rec(String id, String kind, double lon, double lat) {
        return NormalizedRecord.builder(id, kind).observedAt(T).point(lon, lat).put("mag", 5.0).build();
    }

    private MockHttpServletResponse perform(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request).andReturn().getResponse();
    }

    private static String latin1(MockHttpServletResponse response) {
        return new String(response.getContentAsByteArray(), StandardCharsets.ISO_8859_1);
    }

    @Test
    @DisplayName("a valid key forwards to the canonical path: same body and ETag as a bearer request, no-referrer")
    void forwardsLikeABearerRequest() throws Exception {
        MockHttpServletResponse keyed = perform(get("/v1/k/" + open + TILE));
        MockHttpServletResponse bearer = perform(get("/v1" + TILE).header(HttpHeaders.AUTHORIZATION, reader));
        assertThat(keyed.getStatus()).isEqualTo(200);
        assertThat(bearer.getStatus()).isEqualTo(200);
        assertThat(keyed.getContentAsByteArray()).isEqualTo(bearer.getContentAsByteArray());
        assertThat(keyed.getHeader(HttpHeaders.ETAG)).isNotBlank().isEqualTo(bearer.getHeader(HttpHeaders.ETAG));
        assertThat(keyed.getHeader("Referrer-Policy")).isEqualTo("no-referrer");
        assertThat(keyed.getHeader(HttpHeaders.CACHE_CONTROL)).startsWith("private");
        assertThat(latin1(keyed)).contains("in-a1").contains("out-b1");
        assertThat(perform(get("/v1" + TILE)).getStatus()).as("openReads is off").isEqualTo(401);
        assertThat(perform(head("/v1/k/" + open + TILE)).getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("a path outside the key's prefixes is 403 INSUFFICIENT_SCOPE")
    void wrongPrefix() throws Exception {
        MockHttpServletResponse records = perform(get("/v1/k/" + open + "/records"));
        assertThat(records.getStatus()).isEqualTo(403);
        assertThat(records.getContentAsString()).contains("INSUFFICIENT_SCOPE").doesNotContain(open);
        assertThat(perform(get("/v1/k/" + narrow + "/changes/stream")).getStatus()).isEqualTo(403);
        assertThat(perform(get("/v1/k/" + open + "/tiles.json")).getStatus())
                .as("/v1/tiles.json is not under /v1/tiles/").isEqualTo(403);
    }

    @Test
    @DisplayName("POST and PATCH through a key are 405 with Allow: GET, HEAD")
    void writesAreRefused() throws Exception {
        MockHttpServletResponse posted = perform(post("/v1/k/" + open + TILE).contentType(MediaType.APPLICATION_JSON)
                .content("{}"));
        assertThat(posted.getStatus()).isEqualTo(405);
        assertThat(posted.getHeader(HttpHeaders.ALLOW)).isEqualTo("GET, HEAD");
        assertThat(perform(patch("/v1/k/" + open + "/changes/stream").contentType(MediaType.APPLICATION_JSON)
                .content("{}")).getStatus()).isEqualTo(405);
    }

    @Test
    @DisplayName("unknown, revoked and expired keys are 401 INVALID_TILE_KEY and count as failed credentials")
    void invalidKeys() throws Exception {
        double before = meters.get(AuthFailureBackoff.FAILURES_METER).counter().count();
        MockHttpServletResponse unknown = perform(get("/v1/k/fsk_unknownunknownunknown" + TILE));
        assertThat(unknown.getStatus()).isEqualTo(401);
        assertThat(unknown.getContentAsString()).contains("INVALID_TILE_KEY").doesNotContain("fsk_unknownunknownunknown");
        assertThat(backoff.trackedAddresses()).isEqualTo(1);

        JsonNode created = JSON.readTree(perform(post("/v1/tile-keys").header(HttpHeaders.AUTHORIZATION, admin)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"soon-revoked\",\"prefixes\":[\"/v1/tiles/\"],\"ttl\":\"1d\"}"))
                .getContentAsString());
        String key = created.get("key").asString();
        assertThat(perform(get("/v1/k/" + key + TILE)).getStatus()).isEqualTo(200);
        assertThat(backoff.trackedAddresses()).as("a valid key resets the address").isZero();
        assertThat(perform(delete("/v1/tile-keys/" + created.get("id").asString()).header(HttpHeaders.AUTHORIZATION, admin))
                .getStatus()).isEqualTo(204);
        MockHttpServletResponse revoked = perform(get("/v1/k/" + key + TILE));
        assertThat(revoked.getStatus()).isEqualTo(401);
        assertThat(revoked.getContentAsString()).contains("INVALID_TILE_KEY");

        clock.advance(Duration.ofDays(7).plusSeconds(1));
        MockHttpServletResponse expired = perform(get("/v1/k/" + open + TILE));
        assertThat(expired.getStatus()).isEqualTo(401);
        assertThat(expired.getContentAsString()).contains("INVALID_TILE_KEY");
        assertThat(meters.get(AuthFailureBackoff.FAILURES_METER).counter().count() - before).isEqualTo(3.0);
        assertThat(backoff.trackedAddresses()).isEqualTo(1);
    }

    @Test
    @DisplayName("the key's restriction (sources, kinds, bbox) clamps the MVT, which stays private")
    void restrictionsClampTiles() throws Exception {
        MockHttpServletResponse tile = perform(get("/v1/k/" + narrow + TILE));
        assertThat(tile.getStatus()).isEqualTo(200);
        assertThat(latin1(tile)).contains("in-a1").doesNotContain("out-a2").doesNotContain("out-b1");
        assertThat(tile.getHeader(HttpHeaders.CACHE_CONTROL)).startsWith("private");
        assertThat(perform(get("/v1/k/" + narrow + "/tiles/" + OTHER_KIND + ".json")).getStatus())
                .as("a kind outside the restriction").isEqualTo(404);
    }

    @Test
    @DisplayName("private-source records are never returned through a tile key, even an unrestricted one")
    void privateSourcesNever() throws Exception {
        MockHttpServletResponse asAdmin = perform(get("/v1/tiles/" + PRIVATE_KIND + "/0/0/0.mvt?agg=none")
                .header(HttpHeaders.AUTHORIZATION, admin));
        assertThat(latin1(asAdmin)).as("the fixture has a private record").contains("out-p1");
        MockHttpServletResponse keyed = perform(get("/v1/k/" + open + "/tiles/" + PRIVATE_KIND + "/0/0/0.mvt?agg=none"));
        assertThat(keyed.getStatus()).isIn(200, 404);
        assertThat(latin1(keyed)).doesNotContain("out-p1");
        MockHttpServletResponse layer = perform(get("/v1/k/" + open + "/tiles/" + PRIVATE_KIND + ".json"));
        assertThat(layer.getContentAsString()).doesNotContain(PRIVATE);
    }

    @Test
    @DisplayName("the management API is agent scope: create shows the key and urlBase once, list never does")
    void managementApi() throws Exception {
        String agent = TestTokens.bearer(TestTokens.create(tokens, "map-agent", Scope.AGENT));
        MockHttpServletResponse created = perform(post("/v1/tile-keys").header(HttpHeaders.AUTHORIZATION, agent)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"tablet\",\"prefixes\":[\"/v1/products/\",\"/v1/styles\"],\"ttl\":\"P7D\"}"));
        assertThat(created.getStatus()).as(created.getContentAsString()).isEqualTo(201);
        JsonNode body = JSON.readTree(created.getContentAsString());
        String key = body.get("key").asString();
        assertThat(key).startsWith("fsk_");
        assertThat(body.get("urlBase").asString()).endsWith("/v1/k/" + key);
        assertThat(body.get("keyPrefix").asString()).isEqualTo(key.substring(0, 8));
        assertThat(body.has("restrictions") && !body.get("restrictions").isNull()).isFalse();

        MockHttpServletResponse list = perform(get("/v1/tile-keys?limit=10").header(HttpHeaders.AUTHORIZATION, agent));
        assertThat(list.getStatus()).isEqualTo(200);
        assertThat(list.getContentAsString()).contains(body.get("id").asString()).doesNotContain(key)
                .doesNotContain("urlBase");
        assertThat(JSON.readTree(list.getContentAsString()).get("items").size()).isEqualTo(3);

        assertThat(perform(get("/v1/tile-keys").header(HttpHeaders.AUTHORIZATION, reader)).getStatus()).isEqualTo(403);
        assertThat(perform(post("/v1/tile-keys").header(HttpHeaders.AUTHORIZATION, agent)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"x\",\"prefixes\":[\"/v1/records\"],\"ttl\":\"40d\"}")).getStatus()).isEqualTo(400);
        assertThat(perform(delete("/v1/tile-keys/not-a-uuid").header(HttpHeaders.AUTHORIZATION, agent)).getStatus())
                .isEqualTo(404);
        assertThat(perform(get("/v1/k/" + key + "/tile-keys")).getStatus()).as("a tile key cannot manage keys")
                .isEqualTo(403);
    }
}
