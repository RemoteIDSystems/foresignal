package ai.foresignal.source.adapters.spacetrack;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.collect.FetchTrace;
import ai.foresignal.collect.RecordedExchange;
import ai.foresignal.collect.RequestRecorder;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/** {@code spacetrack-tip} against a stubbed Space-Track (prompt 064): the cookie session, re-login, refusals, redaction. */
@IntegrationTest
class SpaceTrackTipAdapterTest extends WireMockSupport {

    private static final String ID = SpaceTrackTipAdapter.ID;
    private static final String LOGIN = "/ajaxauth/login";
    private static final String QUERY = "/basicspacedata/query/class/tip/INSERT_EPOCH/%3Enow-0.042/format/json";
    private static final String PASSWORD = "p@ss w0rd&secret";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private SpaceTrackTipAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install,
                new MutableClock(Instant.parse("2026-10-13T13:00:00Z")), Budget.of(100, 1000));
        harness.reset(ID);
        harness.credentials.put(SpaceTrackTipAdapter.IDENTITY, "operator@example.org");
        harness.credentials.put(SpaceTrackTipAdapter.PASSWORD, PASSWORD);
        adapter = new SpaceTrackTipAdapter(baseUrl());
    }

    private DefaultFetchContext context() {
        return harness.context(adapter, Map.of());
    }

    private static void serveLogin(String cookie) {
        WIREMOCK.stubFor(post(urlEqualTo(LOGIN)).willReturn(aResponse().withStatus(200)
                .withHeader("Set-Cookie", "chocolatechip=" + cookie + "; path=/; secure; HttpOnly").withBody("\"\"")));
    }

    private static void serveQuery(String cookie) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(QUERY)).withHeader("Cookie", equalTo("chocolatechip=" + cookie))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes(ID, "tip.json"))));
    }

    @Test
    @DisplayName("one login, then the session cookie is reused for later queries")
    void cookieReused() throws Exception {
        serveLogin("c1");
        serveQuery("c1");

        FetchResult first = adapter.fetch(context());
        adapter.fetch(context());

        assertThat(first.records()).hasSize(3);
        WIREMOCK.verify(1, postRequestedFor(urlEqualTo(LOGIN))
                .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                .withRequestBody(containing("identity=operator%40example.org")));
        WIREMOCK.verify(2, getRequestedFor(urlEqualTo(QUERY)));
        assertThat(adapter.hasSession(ID)).isTrue();
    }

    @Test
    @DisplayName("a 401 on the query logs in once more and retries")
    void reloginOn401() throws Exception {
        WIREMOCK.stubFor(post(urlEqualTo(LOGIN)).inScenario("login").whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(200).withHeader("Set-Cookie", "chocolatechip=old; path=/"))
                .willSetStateTo("again"));
        WIREMOCK.stubFor(post(urlEqualTo(LOGIN)).inScenario("login").whenScenarioStateIs("again")
                .willReturn(aResponse().withStatus(200).withHeader("Set-Cookie", "chocolatechip=new; path=/")));
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(QUERY)).withHeader("Cookie", equalTo("chocolatechip=old"))
                .willReturn(aResponse().withStatus(401)));
        serveQuery("new");

        assertThat(adapter.fetch(context()).records()).hasSize(3);
        WIREMOCK.verify(2, postRequestedFor(urlEqualTo(LOGIN)));
        WIREMOCK.verify(2, getRequestedFor(urlEqualTo(QUERY)));
    }

    @Test
    @DisplayName("refused credentials are CredentialsRejectedException, by body or by status")
    void badCredentials() {
        WIREMOCK.stubFor(post(urlEqualTo(LOGIN)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody("{\"Login\":\"Failed\"}")));
        assertThatThrownBy(() -> adapter.fetch(context())).isInstanceOf(CredentialsRejectedException.class);
        WIREMOCK.resetAll();
        harness.reset(ID);
        WIREMOCK.stubFor(post(urlEqualTo(LOGIN)).willReturn(aResponse().withStatus(401)));
        assertThatThrownBy(() -> adapter.fetch(context())).isInstanceOf(CredentialsRejectedException.class);
        WIREMOCK.verify(0, getRequestedFor(urlEqualTo(QUERY)));
        harness.credentials.remove(SpaceTrackTipAdapter.PASSWORD);
        assertThatThrownBy(() -> adapter.fetch(context())).isInstanceOf(CredentialsRejectedException.class);
    }

    @Test
    @DisplayName("the password and the cookie never appear in traces or recorded exchanges")
    void redacted() throws Exception {
        serveLogin("c1");
        serveQuery("c1");
        RequestRecorder recorder = new RequestRecorder();
        DefaultFetchContext ctx = harness.context(adapter, Map.of(), recorder);

        adapter.fetch(ctx);

        assertThat(ctx.traces()).extracting(FetchTrace::redactedUrl).noneMatch(u -> u.contains("w0rd"));
        for (RecordedExchange exchange : recorder.exchanges()) {
            assertThat(exchange.toString()).doesNotContain("w0rd").doesNotContain("chocolatechip=c1");
        }
    }

    @Test
    @DisplayName("TIP messages: externalId, MSG_EPOCH, DECAY_EPOCH, window, optional fields and the geometry note")
    void records() {
        TipParser.Parsed parsed = TipParser.parse(AdapterFixtures.bytes(ID, "tip.json"));
        assertThat(parsed.records()).hasSize(3);
        NormalizedRecord r = parsed.records().get(1);
        assertThat(r.externalId()).isEqualTo("60123");
        assertThat(r.kind()).isEqualTo("reentry-prediction");
        assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-13T12:00:00Z"));
        assertThat(r.eventTime()).isEqualTo(Instant.parse("2026-10-14T02:58:00Z"));
        assertThat(r.payload()).containsEntry("window", 120).containsEntry("direction", "descending")
                .containsEntry("highInterest", true).containsEntry("rev", 8127).containsEntry("nextReport", 3)
                .containsEntry("geometryNote", TipParser.GEOMETRY_NOTE);
        assertThat(((Point) r.geometry()).getX()).isEqualTo(101.2);
        NormalizedRecord noPosition = parsed.records().get(2);
        assertThat(noPosition.externalId()).isEqualTo("100777");
        assertThat(noPosition.geometry()).isNull();
        assertThat(noPosition.payload()).doesNotContainKey("geometryNote");
    }

    @Test
    @DisplayName("LOCAL_ONLY, both credentials required, hourly")
    void descriptor() {
        assertThat(adapter.descriptor().egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(adapter.descriptor().requiredCredentialFields()).hasSize(2);
        assertThat(adapter.descriptor().defaultBudget()).isEqualTo(Budget.of(2, 60));
        assertThat(ai.foresignal.source.DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
    }
}
