package ai.foresignal.source.adapters.acled;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.collect.FetchTrace;
import ai.foresignal.collect.RecordedExchange;
import ai.foresignal.collect.RequestRecorder;
import ai.foresignal.collect.UpstreamAccessDeniedException;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link AcledAdapter} and {@link AcledTokens} against a stubbed ACLED (prompt 081): the password grant, token reuse,
 * the refresh grant before expiry, a new login after a restart, a 401 on data refreshing once, a refused login, the
 * 403 access-level refusal, the cursors, and tokens and the password never reaching traces, recorded exchanges, the
 * log output or the adapter state.
 */
@IntegrationTest
@ExtendWith(OutputCaptureExtension.class)
class AcledAuthTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-13T20:00:00Z");
    static final String TOKEN = "/acleddata.com/oauth/token";
    static final String EVENTS = "/acleddata.com/api/acled/read";
    static final String DELETED = "/acleddata.com/api/deleted/read";
    static final String EMAIL = "analyst@example.org";
    static final String PASSWORD = "pw-0123456789-SECRET";
    static final String ACCESS_1 = "SYNTHETIC-ACCESS-TOKEN-1";
    static final String ACCESS_2 = "SYNTHETIC-ACCESS-TOKEN-2";
    static final String REFRESH_1 = "SYNTHETIC-REFRESH-TOKEN-1";
    static final Map<String, Object> PARAMS = Map.of("countries", List.of("804"));

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    AdapterHttpHarness harness;
    AcledAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOW),
                Budget.of(100, 1000));
        adapter = new AcledAdapter(baseUrl(), new CountryCentroids());
        harness.reset(AcledAdapter.ID, AcledAdapter.CURSOR_KEY, AcledAdapter.DELETED_CURSOR_KEY);
        harness.credentials.clear();
        harness.credentials.put(AcledAdapter.EMAIL, EMAIL);
        harness.credentials.put(AcledAdapter.PASSWORD, PASSWORD);
    }

    @AfterEach
    void tearDown() {
        harness.backoff.onSuccess(AcledAdapter.ID);
    }

    static void serveToken(String file) {
        WIREMOCK.stubFor(post(urlPathEqualTo(TOKEN)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(AdapterFixtures.bytes("acled", file))));
    }

    static void serveData(String events, String deleted) {
        WIREMOCK.stubFor(get(urlPathEqualTo(EVENTS)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(AdapterFixtures.bytes("acled", events))));
        WIREMOCK.stubFor(get(urlPathEqualTo(DELETED)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(AdapterFixtures.bytes("acled", deleted))));
    }

    @Test
    @DisplayName("password grant, then the bearer token on the data requests; the first run reads an event_date window")
    void passwordGrant() throws Exception {
        serveToken("token.json");
        serveData("events.json", "deleted.json");

        FetchResult result = adapter.fetch(harness.context(adapter, PARAMS));

        WIREMOCK.verify(1, postRequestedFor(urlPathEqualTo(TOKEN))
                .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
                .withRequestBody(containing("username=analyst%40example.org"))
                .withRequestBody(containing("grant_type=password"))
                .withRequestBody(containing("client_id=acled"))
                .withRequestBody(containing("scope=authenticated")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(EVENTS)).withHeader("Authorization", equalTo("Bearer " + ACCESS_1))
                .withQueryParam("event_date", equalTo("2026-09-13|2026-10-13"))
                .withQueryParam("event_date_where", equalTo("BETWEEN")).withQueryParam("iso", equalTo("804"))
                .withQueryParam("limit", equalTo("5000")).withQueryParam("page", equalTo("1"))
                .withQueryParam("_format", equalTo("json")));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(DELETED))
                .withQueryParam("deleted_timestamp", equalTo(Long.toString(NOW.minus(Duration.ofDays(30))
                        .getEpochSecond()))));
        assertThat(result.records()).filteredOn(r -> !r.tombstone()).hasSize(3);
        assertThat(result.records()).filteredOn(NormalizedRecord::tombstone).extracting(NormalizedRecord::externalId)
                .as("only the configured country's deletions").containsExactly("UKR099001", "UKR099002");
        var state = harness.client.stateStore(AcledAdapter.ID);
        assertThat(state.get(AcledAdapter.CURSOR_KEY)).contains("1791821000");
        assertThat(state.get(AcledAdapter.DELETED_CURSOR_KEY)).contains("1791720000");
    }

    @Test
    @DisplayName("a later run reuses the token and reads from the timestamp cursor")
    void reuseAndCursor() throws Exception {
        serveToken("token.json");
        serveData("events.json", "deleted-empty.json");
        adapter.fetch(harness.context(adapter, PARAMS));
        WIREMOCK.resetRequests();

        harness.clock.setInstant(NOW.plus(Duration.ofHours(6)));
        adapter.fetch(harness.context(adapter, PARAMS));

        WIREMOCK.verify(0, postRequestedFor(urlPathEqualTo(TOKEN)));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(EVENTS)).withQueryParam("timestamp", equalTo("1791821000"))
                .withQueryParam("timestamp_where", equalTo(">=")).withHeader("Authorization",
                        equalTo("Bearer " + ACCESS_1)));
    }

    @Test
    @DisplayName("within five minutes of expiry the refresh grant is used, then the new access token")
    void refreshBeforeExpiry() throws Exception {
        serveToken("token.json");
        serveData("events-empty.json", "deleted-empty.json");
        adapter.fetch(harness.context(adapter, PARAMS));
        WIREMOCK.resetAll();
        serveToken("token-refreshed.json");
        serveData("events-empty.json", "deleted-empty.json");

        harness.clock.setInstant(NOW.plus(Duration.ofHours(24)).minus(Duration.ofMinutes(4)));
        adapter.fetch(harness.context(adapter, PARAMS));

        WIREMOCK.verify(1, postRequestedFor(urlPathEqualTo(TOKEN)).withRequestBody(containing("grant_type=refresh_token"))
                .withRequestBody(containing("refresh_token=" + REFRESH_1)).withRequestBody(containing("client_id=acled")));
        WIREMOCK.verify(0, postRequestedFor(urlPathEqualTo(TOKEN)).withRequestBody(containing("grant_type=password")));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(EVENTS)).withHeader("Authorization", equalTo("Bearer " + ACCESS_2)));
    }

    @Test
    @DisplayName("after a restart (a new adapter, nothing in memory) the adapter logs in with the password again")
    void reloginAfterRestart() throws Exception {
        serveToken("token.json");
        serveData("events-empty.json", "deleted-empty.json");
        adapter.fetch(harness.context(adapter, PARAMS));
        assertThat(adapter.holdsTokens(AcledAdapter.ID)).isTrue();

        AcledAdapter restarted = new AcledAdapter(baseUrl(), new CountryCentroids());
        assertThat(restarted.holdsTokens(AcledAdapter.ID)).isFalse();
        restarted.fetch(harness.context(restarted, PARAMS));

        WIREMOCK.verify(2, postRequestedFor(urlPathEqualTo(TOKEN)).withRequestBody(containing("grant_type=password")));
    }

    @Test
    @DisplayName("a 401 on data invalidates the access token and refreshes once")
    void unauthorizedRefreshes() throws Exception {
        serveToken("token.json");
        WIREMOCK.stubFor(get(urlPathEqualTo(EVENTS)).inScenario("401").whenScenarioStateIs(
                com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED).willReturn(aResponse().withStatus(401)
                        .withBody("{\"message\":\"The resource owner or authorization server denied the request.\"}"))
                .willSetStateTo("retried"));
        WIREMOCK.stubFor(get(urlPathEqualTo(EVENTS)).inScenario("401").whenScenarioStateIs("retried")
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes("acled", "events-empty.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(DELETED)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("acled", "deleted-empty.json"))));

        adapter.fetch(harness.context(adapter, PARAMS));

        WIREMOCK.verify(1, postRequestedFor(urlPathEqualTo(TOKEN)).withRequestBody(containing("grant_type=password")));
        WIREMOCK.verify(1, postRequestedFor(urlPathEqualTo(TOKEN)).withRequestBody(containing("grant_type=refresh_token")));
        WIREMOCK.verify(2, getRequestedFor(urlPathEqualTo(EVENTS)));
    }

    @Test
    @DisplayName("a refused login is CredentialsRejectedException; missing credentials send nothing")
    void refusedLogin() {
        WIREMOCK.stubFor(post(urlPathEqualTo(TOKEN)).willReturn(aResponse().withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"error\":\"invalid_grant\",\"error_description\":\"The user credentials were incorrect.\"}")));
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, PARAMS)))
                .isInstanceOf(CredentialsRejectedException.class).hasMessageNotContaining(PASSWORD);
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo(EVENTS)));

        WIREMOCK.resetRequests();
        harness.credentials.remove(AcledAdapter.PASSWORD);
        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, PARAMS)))
                .isInstanceOf(CredentialsRejectedException.class);
        WIREMOCK.verify(0, postRequestedFor(urlPathEqualTo(TOKEN)));
    }

    @Test
    @DisplayName("a 403 after a successful login: a refusal with the access-level reason, a one-hour backoff, no cursor")
    void accessLevel() {
        serveToken("token.json");
        WIREMOCK.stubFor(get(urlPathEqualTo(EVENTS)).willReturn(aResponse().withStatus(403)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("acled", "forbidden-403.json"))));

        assertThatThrownBy(() -> adapter.fetch(harness.context(adapter, PARAMS)))
                .isInstanceOfSatisfying(UpstreamAccessDeniedException.class, e -> assertThat(e.getMessage())
                        .isEqualTo("upstream refused this client (HTTP 403): " + AcledAdapter.ACCESS_LEVEL_REASON));
        assertThat(harness.backoff.isInBackoff(AcledAdapter.ID)).isTrue();
        assertThat(harness.client.stateStore(AcledAdapter.ID).get(AcledAdapter.CURSOR_KEY)).isEmpty();
    }

    @Test
    @DisplayName("tokens and the password never reach traces, recorded exchanges, the log output or the state")
    void tokensNeverStored(CapturedOutput output) throws Exception {
        serveToken("token.json");
        serveData("events.json", "deleted.json");
        RequestRecorder recorder = new RequestRecorder();
        DefaultFetchContext ctx = harness.context(adapter, PARAMS, recorder);

        adapter.fetch(ctx);

        List<String> secrets = List.of(ACCESS_1, REFRESH_1, PASSWORD);
        assertThat(ctx.traces()).isNotEmpty().extracting(FetchTrace::toString)
                .allSatisfy(t -> secrets.forEach(s -> assertThat(t).doesNotContain(s)));
        assertThat(recorder.exchanges()).isNotEmpty();
        for (RecordedExchange exchange : recorder.exchanges()) {
            secrets.forEach(s -> assertThat(exchange.toString()).doesNotContain(s));
        }
        secrets.forEach(s -> assertThat(output.getAll()).doesNotContain(s));
        harness.client.stateStore(AcledAdapter.ID).all().values()
                .forEach(v -> secrets.forEach(s -> assertThat(v).doesNotContain(s)));
        for (LoggedRequest request : WIREMOCK.findAll(getRequestedFor(urlPathEqualTo(EVENTS)))) {
            assertThat(request.getUrl()).doesNotContain(ACCESS_1).doesNotContain(PASSWORD);
        }
    }
}
