package ai.foresignal.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.store.AlertEntity;
import ai.foresignal.store.AlertRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.stats.StatsItSupport;
import ai.foresignal.testsupport.stats.StatsTestSources;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * EGR-1 (prompt 039, the prompt's {@code PrivateEnforcementIT}; named after {@code docs/Invariants.md}):
 * a matrix of {anonymous, read, read + privateAccess, agent, admin} × every read path of §2. Under
 * {@code openReads=true} a caller that may not see private sources finds a private-source entity in
 * no list and gets {@code 404} on every direct read; admin and holders of the private grant see it.
 * A plain agent token no longer sees private sources (prompt 039 tightened the 023 gate).
 *
 * <p>Records are seeded only through {@code TestRecords}; alerts, which are not records, through their
 * repository. The shared 038 context ({@link StatsTestSources}) carries the private {@value
 * StatsTestSources#SECRET}.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(StatsTestSources.class)
class PrivateEgressIT extends StatsItSupport {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String SECRET = StatsTestSources.SECRET;
    private static final String POINTS = StatsTestSources.POINTS;
    private static final Instant AT = T0.plusSeconds(600);

    /** The callers of the matrix. */
    enum Caller {
        ANONYMOUS(false), READ(false), READ_PRIVATE(true), AGENT(false), ADMIN(true);

        final boolean seesPrivate;

        Caller(boolean seesPrivate) {
            this.seesPrivate = seesPrivate;
        }
    }

    @Autowired MockMvc mvc;
    @Autowired TokenService tokens;
    @Autowired AlertRepository alerts;
    @Autowired GlanceController glance;

    Map<Caller, String> bearer;
    long secretAlertId;

    @BeforeEach
    void setUp() throws Exception {
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
        glance.clearCache();
        bearer = new java.util.EnumMap<>(Caller.class);
        bearer.put(Caller.READ, TestTokens.bearer(TestTokens.create(tokens, "egr-read", Scope.READ)));
        bearer.put(Caller.READ_PRIVATE, TestTokens.bearer(TestTokens.create(tokens, "egr-read-private", Scope.READ, true)));
        bearer.put(Caller.AGENT, TestTokens.bearer(TestTokens.create(tokens, "egr-agent", Scope.AGENT)));
        bearer.put(Caller.ADMIN, TestTokens.bearer(TestTokens.create(tokens, "egr-admin", Scope.ADMIN)));
        seed(POINTS, AT, point("pub1", StatsTestSources.POINT_KIND, 10, 50, AT, 1));
        seed(SECRET, AT, point("sec1", StatsTestSources.SECRET_KIND, 10.01, 50.01, AT, 7));
        seed(SECRET, AT.plusSeconds(60), point("sec1", StatsTestSources.SECRET_KIND, 10.02, 50.02,
                AT.plusSeconds(60), 8));
        clock.setInstant(AT.plusSeconds(120));
        secretAlertId = alert(SECRET, "critical").getId();
        alert(POINTS, "info");
        call(Caller.ADMIN, put("/v1/settings/metrics").contentType(MediaType.APPLICATION_JSON)
                .content("{\"projections\": [{\"name\": \"egr_secret_value\", \"source\": \"" + SECRET
                        + "\", \"path\": \"value\", \"allowPrivate\": true}]}"));
    }

    @AfterEach
    void restore() throws Exception {
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
        call(Caller.ADMIN, put("/v1/settings/metrics").contentType(MediaType.APPLICATION_JSON)
                .content("{\"projections\": []}"));
        glance.clearCache();
    }

    private AlertEntity alert(String source, String severity) {
        AlertEntity a = new AlertEntity(UUID.randomUUID(), 1L, source, source.equals(SECRET) ? "sec1" : "pub1",
                clock.instant());
        a.setKind(source.equals(SECRET) ? StatsTestSources.SECRET_KIND : StatsTestSources.POINT_KIND);
        a.setSeverity(severity);
        a.setMessage("Alert " + severity + " from " + source);
        a.setRecordSnapshot(Map.of("geometry", Map.of("type", "Point", "coordinates", List.of(10.0, 50.0))));
        return alerts.saveAndFlush(a);
    }

    private MockHttpServletResponse call(Caller caller, MockHttpServletRequestBuilder request) throws Exception {
        String token = bearer.get(caller);
        if (token != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, token);
        }
        return mvc.perform(request).andReturn().getResponse();
    }

    private String body(Caller caller, MockHttpServletRequestBuilder request) throws Exception {
        MockHttpServletResponse response = call(caller, request);
        assertThat(response.getStatus()).as(caller + " " + response.getContentAsString()).isEqualTo(200);
        return response.getContentAsString(StandardCharsets.UTF_8);
    }

    /** A list read: the private source appears only for callers that see it. */
    private void listed(Caller caller, MockHttpServletRequestBuilder request, String marker) throws Exception {
        String text = body(caller, request);
        if (caller.seesPrivate) {
            assertThat(text).as(caller + " sees it").contains(marker);
        } else {
            assertThat(text).as(caller + " must not see it").doesNotContain(marker);
        }
    }

    /** A direct read: 404 when hidden (its existence does not leak), not 404 when visible. */
    private void direct(Caller caller, MockHttpServletRequestBuilder request) throws Exception {
        int status = call(caller, request).getStatus();
        if (caller.seesPrivate) {
            assertThat(status).as(caller + " direct read").isNotEqualTo(404).isLessThan(500);
        } else {
            assertThat(status).as(caller + " direct read").isEqualTo(404);
        }
    }

    @ParameterizedTest
    @EnumSource(Caller.class)
    @DisplayName("records: list, direct get, versions, track, asOf and count")
    void records(Caller caller) throws Exception {
        listed(caller, get("/v1/records").param("limit", "500"), SECRET);
        listed(caller, get("/v1/records").param("asOf", AT.plusSeconds(90).toString()).param("limit", "500"), SECRET);
        direct(caller, get("/v1/records/" + SECRET + "/sec1"));
        direct(caller, get("/v1/records/" + SECRET + "/sec1/versions"));
        direct(caller, get("/v1/records/" + SECRET + "/sec1/track").param("from", T0.toString()));
        direct(caller, get("/v1/records").param("source", SECRET));

        long all = JSON.readTree(body(Caller.ADMIN, get("/v1/records").param("count", "true"))).get("count").asLong();
        long mine = JSON.readTree(body(caller, get("/v1/records").param("count", "true"))).get("count").asLong();
        assertThat(mine).as(caller + " count").isEqualTo(caller.seesPrivate ? all : all - 1);
    }

    @ParameterizedTest
    @EnumSource(Caller.class)
    @DisplayName("history, frames, timeline, changes and kinds")
    void history(Caller caller) throws Exception {
        listed(caller, get("/v1/history").param("since", T0.toString()).param("limit", "500"), SECRET);
        long all = JSON.readTree(body(Caller.ADMIN, get("/v1/history").param("since", T0.toString())
                .param("count", "true"))).get("count").asLong();
        long mine = JSON.readTree(body(caller, get("/v1/history").param("since", T0.toString())
                .param("count", "true"))).get("count").asLong();
        assertThat(mine).as(caller + " history count").isEqualTo(caller.seesPrivate ? all : all - 2);
        direct(caller, get("/v1/history/frames").param("source", SECRET).param("from", AT.toString())
                .param("to", AT.plusSeconds(120).toString()).param("step", "60s"));
        direct(caller, get("/v1/timeline").param("source", SECRET).param("from", T0.toString())
                .param("to", AT.plusSeconds(120).toString()));
        listed(caller, get("/v1/changes").param("after", "0").param("limit", "500"), SECRET);
        listed(caller, get("/v1/kinds"), StatsTestSources.SECRET_KIND);
    }

    @ParameterizedTest
    @EnumSource(Caller.class)
    @DisplayName("sources: list and direct get")
    void sources(Caller caller) throws Exception {
        listed(caller, get("/v1/sources"), SECRET);
        listed(caller, get("/v1/sources/health"), SECRET);
        direct(caller, get("/v1/sources/" + SECRET));
    }

    @ParameterizedTest
    @EnumSource(Caller.class)
    @DisplayName("tiles: the private layer is 404, indexes omit it, and private data is never publicly cached")
    void tiles(Caller caller) throws Exception {
        direct(caller, get("/v1/tiles/" + SECRET + "/0/0/0.mvt"));
        direct(caller, get("/v1/tiles/" + StatsTestSources.SECRET_KIND + "/0/0/0.mvt"));
        direct(caller, get("/v1/tiles/" + SECRET + ".json"));
        listed(caller, get("/v1/tiles.json"), SECRET);
        if (caller.seesPrivate) {
            assertThat(call(caller, get("/v1/tiles/" + SECRET + "/0/0/0.mvt")).getHeader(HttpHeaders.CACHE_CONTROL))
                    .startsWith("private");
            assertThat(call(caller, get("/v1/tiles.json")).getHeader(HttpHeaders.CACHE_CONTROL)).startsWith("private");
        }
        if (caller == Caller.ANONYMOUS) {
            assertThat(call(caller, get("/v1/tiles/" + POINTS + "/0/0/0.mvt")).getHeader(HttpHeaders.CACHE_CONTROL))
                    .startsWith("public");
        }
    }

    @ParameterizedTest
    @EnumSource(Caller.class)
    @DisplayName("search: always a token; hidden sources are absent and 404 when named")
    void search(Caller caller) throws Exception {
        if (caller == Caller.ANONYMOUS) {
            assertThat(call(caller, get("/v1/search").param("q", "Name")).getStatus()).isEqualTo(401);
            return;
        }
        String text = body(caller, get("/v1/search").param("q", "Name").param("mode", "text"));
        if (!caller.seesPrivate) {
            assertThat(text).doesNotContain(SECRET);
        }
        direct(caller, get("/v1/search").param("q", "Name").param("mode", "text").param("source", SECRET));
    }

    @ParameterizedTest
    @EnumSource(Caller.class)
    @DisplayName("alerts: list and direct get; glance shows the private alert only to allowed callers")
    void alerts(Caller caller) throws Exception {
        listed(caller, get("/v1/alerts"), SECRET);
        direct(caller, get("/v1/alerts/" + secretAlertId));
        String g = body(caller, get("/v1/glance"));
        if (caller.seesPrivate) {
            assertThat(g).contains("critical");
        } else {
            assertThat(g).doesNotContain("Alert critical");
            JsonNode latest = JSON.readTree(g).at("/alerts/latest");
            for (JsonNode item : latest) {
                assertThat(item.path("severity").asString()).isNotEqualTo("critical");
            }
        }
    }

    @ParameterizedTest
    @EnumSource(Caller.class)
    @DisplayName("stats (timeseries, top) and metrics exclude private sources for callers that may not see them")
    void statsAndMetrics(Caller caller) throws Exception {
        String from = T0.toString();
        String to = AT.plus(Duration.ofHours(1)).toString();
        listed(caller, get("/v1/stats/top").param("by", "source").param("from", from).param("to", to), SECRET);
        direct(caller, get("/v1/stats/timeseries").param("source", SECRET).param("from", from).param("to", to)
                .param("bucket", "1h"));
        listed(caller, get("/v1/metrics"), "egr_secret_value");
    }
}
