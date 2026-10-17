package ai.foresignal.kinematics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.TokenService;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.RecordsTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The CPA/ETA endpoints (prompt 114): {@code getCpa}, {@code getEta} and {@code cpaBatch} over live aircraft records,
 * the batch's top N, the errors {@code CPA_RANGE}, {@code STATE_TOO_OLD} and validation, staleness, and restricted
 * tokens reading a record they may not see as absent.
 */
@IntegrationTest
@Import(RecordsTestSources.class)
class KinematicsApiIT {

    static final Instant T = Instant.parse("2026-10-16T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final String SRC = "test-kin-trk";
    static final String KIND = "test-aircraft";

    @Autowired MockMvc mvc;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired SourceRegistry registry;
    @Autowired TokenService tokens;

    String read;

    @BeforeEach
    void setUp() {
        cleanUp();
        clock.setInstant(T);
        registry.registerDynamic(SourceDescriptor.builder(SRC)
                .name("Kinematics test aircraft")
                .attribution("Test")
                .licence("CC BY 4.0")
                .kind(KIND)
                .mode(Mode.PUSH)
                .historyTier(HistoryTier.TRACK)
                .recordTtl(Duration.ofMinutes(10))
                .defaultIngestPolicy(IngestPolicy.all(Duration.ofMinutes(2)))
                .defaultBudget(Budget.of(60, 100_000))
                .build());
        read = TestTokens.bearer(TestTokens.create(tokens, "kin-read-" + System.nanoTime(), Scope.READ));
        records.ingestAt(SRC, T,
                // Checkpoint 124: an exact (0,0) is removed by the normalizer (QaFlags.NULL_ISLAND), so "a" starts
                // 0.1 m east of it; every distance in the scenario is unchanged at this precision.
                plane("a", 0.000001, 0.0, 200, 90, 3000),
                plane("b", 0.5, 0.0, 200, 270, 3300),
                plane("c", 0.2, 0.05, 150, 270, 3000),
                plane("far", 6.0, 0.0, 200, 270, 3000),
                NormalizedRecord.builder("still", KIND).observedAt(T).point(0.1, 0.02).build());
    }

    @AfterEach
    void cleanUp() {
        if (registry.contains(SRC)) {
            registry.unregisterDynamic(SRC);
        }
    }

    static NormalizedRecord plane(String id, double lon, double lat, double knots, double track, double alt) {
        return NormalizedRecord.builder(id, KIND).observedAt(T).point(lon, lat).put("gs", knots).put("track", track)
                .put("alt_geom", alt).build();
    }

    JsonNode call(RequestBuilder request, int expected) throws Exception {
        MvcResult result = mvc.perform(request).andExpect(status().is(expected)).andReturn();
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return body.isEmpty() ? null : JSON.readTree(body);
    }

    @Test
    @DisplayName("getCpa: two aircraft head-on close in about 2 minutes with 300 m of vertical separation")
    void cpa() throws Exception {
        JsonNode r = call(get("/v1/kinematics/cpa").param("a", SRC + "/a").param("b", SRC + "/b").param("horizon", "1h")
                .header(HttpHeaders.AUTHORIZATION, read), 200);
        // 0.5° at the equator is about 55.6 km, closed at 2 × 102.9 m/s.
        assertThat(r.get("tcpaSeconds").asDouble()).isCloseTo(270, within(5.0));
        assertThat(r.get("dcpaMeters").asDouble()).isLessThan(10);
        assertThat(r.get("verticalSeparationM").asDouble()).isEqualTo(300.0);
        assertThat(r.get("horizon").asString()).isEqualTo("1h");
        assertThat(r.get("stale").asBoolean()).isFalse();
        assertThat(r.get("a").get("speedMps").asDouble()).isCloseTo(102.89, within(0.01));
        assertThat(r.get("a").get("derived").asBoolean()).isFalse();
        assertThat(r.get("t0").asString()).isEqualTo(T.toString());
    }

    @Test
    @DisplayName("getCpa: more than 500 km apart is 400 CPA_RANGE; an unknown record is 404; a bad ref or horizon is 400")
    void cpaErrors() throws Exception {
        JsonNode range = call(get("/v1/kinematics/cpa").param("a", SRC + "/a").param("b", SRC + "/far")
                .header(HttpHeaders.AUTHORIZATION, read), 400);
        assertThat(range.get("code").asString()).isEqualTo("CPA_RANGE");
        call(get("/v1/kinematics/cpa").param("a", SRC + "/a").param("b", SRC + "/nobody")
                .header(HttpHeaders.AUTHORIZATION, read), 404);
        call(get("/v1/kinematics/cpa").param("a", "no-slash").param("b", SRC + "/b")
                .header(HttpHeaders.AUTHORIZATION, read), 400);
        call(get("/v1/kinematics/cpa").param("a", SRC + "/a").param("b", SRC + "/b").param("horizon", "3d")
                .header(HttpHeaders.AUTHORIZATION, read), 400);
    }

    @Test
    @DisplayName("a state older than 2 × the TTL is stale; older than 1 h is 422 STATE_TOO_OLD")
    void staleness() throws Exception {
        clock.setInstant(T.plus(Duration.ofMinutes(30)));
        JsonNode r = call(get("/v1/kinematics/cpa").param("a", SRC + "/a").param("b", SRC + "/b")
                .header(HttpHeaders.AUTHORIZATION, read), 200);
        assertThat(r.get("stale").asBoolean()).isTrue();
        assertThat(r.get("a").get("stale").asBoolean()).isTrue();
        clock.setInstant(T.plus(Duration.ofMinutes(61)));
        JsonNode old = call(get("/v1/kinematics/cpa").param("a", SRC + "/a").param("b", SRC + "/b")
                .header(HttpHeaders.AUTHORIZATION, read), 422);
        assertThat(old.get("code").asString()).isEqualTo("STATE_TOO_OLD");
        JsonNode eta = call(get("/v1/kinematics/eta").param("record", SRC + "/a").param("to", "1,0")
                .header(HttpHeaders.AUTHORIZATION, read), 422);
        assertThat(eta.get("code").asString()).isEqualTo("STATE_TOO_OLD");
    }

    @Test
    @DisplayName("getEta: approaching a point ahead gives an ETA; a point behind gives none; a place needs an agent token")
    void eta() throws Exception {
        JsonNode ahead = call(get("/v1/kinematics/eta").param("record", SRC + "/a").param("to", "0.3,0")
                .header(HttpHeaders.AUTHORIZATION, read), 200);
        assertThat(ahead.get("approaching").asBoolean()).isTrue();
        assertThat(ahead.get("etaSeconds").asDouble()).isCloseTo(324, within(3.0));
        assertThat(ahead.get("target").get("point").get("coordinates").get(0).asDouble()).isEqualTo(0.3);
        assertThat(ahead.get("etaAt").isString()).isTrue();
        JsonNode behind = call(get("/v1/kinematics/eta").param("record", SRC + "/a").param("to", "-0.3,0")
                .header(HttpHeaders.AUTHORIZATION, read), 200);
        assertThat(behind.get("approaching").asBoolean()).isFalse();
        assertThat(behind.get("etaSeconds").isNull()).isTrue();
        call(get("/v1/kinematics/eta").param("record", SRC + "/a").param("to", "home")
                .header(HttpHeaders.AUTHORIZATION, read), 400);
        call(get("/v1/kinematics/eta").param("record", SRC + "/a").param("to", "200,0")
                .header(HttpHeaders.AUTHORIZATION, read), 400);
        String agent = TestTokens.bearer(TestTokens.create(tokens, "kin-agent-" + System.nanoTime(), Scope.AGENT));
        call(get("/v1/kinematics/eta").param("record", SRC + "/a").param("to", "no-such-place")
                .header(HttpHeaders.AUTHORIZATION, agent), 404);
    }

    @Test
    @DisplayName("a record without speed or course gets one derived from its last two persisted positions")
    void derived() throws Exception {
        clock.setInstant(T.plusSeconds(60));
        records.ingestAt(SRC, T.plusSeconds(60),
                NormalizedRecord.builder("still", KIND).observedAt(T.plusSeconds(60)).point(0.1, 0.03).build());
        JsonNode r = call(get("/v1/kinematics/eta").param("record", SRC + "/still").param("to", "0.1,0.5")
                .header(HttpHeaders.AUTHORIZATION, read), 200);
        assertThat(r.get("record").get("derived").asBoolean()).isTrue();
        assertThat(r.get("record").get("courseDeg").asDouble()).isCloseTo(0, within(0.5));
        // 0.01° of latitude in 60 s.
        assertThat(r.get("record").get("speedMps").asDouble()).isCloseTo(18.5, within(0.5));
        assertThat(r.get("approaching").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("cpaBatch: the closest approaches first, within maxDcpaM, excluding the subject and far records")
    void batch() throws Exception {
        String body = "{\"subject\":\"" + SRC + "/a\",\"filter\":{\"kinds\":[\"" + KIND + "\"]},\"horizon\":\"1h\","
                + "\"maxDcpaM\":20000,\"top\":2}";
        JsonNode r = call(post("/v1/kinematics/cpa/batch").contentType(MediaType.APPLICATION_JSON).content(body)
                .header(HttpHeaders.AUTHORIZATION, read), 200);
        assertThat(r.get("subject").get("externalId").asString()).isEqualTo("a");
        assertThat(r.get("evaluated").asInt()).isEqualTo(4);
        assertThat(r.get("truncated").asBoolean()).isFalse();
        List<String> ids = new java.util.ArrayList<>();
        r.get("items").forEach(i -> ids.add(i.get("b").get("externalId").asString()));
        assertThat(ids).hasSize(2).first().isEqualTo("b");
        assertThat(ids).doesNotContain("a", "far");
        double first = r.get("items").get(0).get("dcpaMeters").asDouble();
        double second = r.get("items").get(1).get("dcpaMeters").asDouble();
        assertThat(first).isLessThanOrEqualTo(second);
    }

    @Test
    @DisplayName("cpaBatch validation: a filter is required and top is bounded")
    void batchErrors() throws Exception {
        call(post("/v1/kinematics/cpa/batch").contentType(MediaType.APPLICATION_JSON)
                .content("{\"subject\":\"" + SRC + "/a\",\"filter\":{}}").header(HttpHeaders.AUTHORIZATION, read), 400);
        call(post("/v1/kinematics/cpa/batch").contentType(MediaType.APPLICATION_JSON)
                .content("{\"subject\":\"" + SRC + "/a\",\"filter\":{\"kinds\":[\"x\"]},\"top\":501}")
                .header(HttpHeaders.AUTHORIZATION, read), 400);
        call(post("/v1/kinematics/cpa/batch").contentType(MediaType.APPLICATION_JSON)
                .content("{\"subject\":\"" + SRC + "/nobody\",\"filter\":{\"kinds\":[\"x\"]}}")
                .header(HttpHeaders.AUTHORIZATION, read), 404);
    }

    @Test
    @DisplayName("a restricted token reads a record outside its restriction as absent")
    void restricted() throws Exception {
        TokenRestrictions only = new TokenRestrictions(List.of(RecordsTestSources.PRIVATE), null, null, null, null, null,
                null, null, null, null);
        String restricted = TestTokens.bearer(TestTokens.create(tokens, "kin-restricted-" + System.nanoTime(), Scope.READ,
                only, null));
        call(get("/v1/kinematics/cpa").param("a", SRC + "/a").param("b", SRC + "/b")
                .header(HttpHeaders.AUTHORIZATION, restricted), 404);
    }
}
