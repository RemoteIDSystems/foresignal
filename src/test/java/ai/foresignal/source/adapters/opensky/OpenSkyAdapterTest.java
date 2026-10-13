package ai.foresignal.source.adapters.opensky;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.collect.FetchTrace;
import ai.foresignal.collect.RecordedExchange;
import ai.foresignal.collect.RequestRecorder;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamQuotaDeferredException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code opensky-states} against a stubbed API and identity provider through the real {@link UpstreamClient}
 * (prompt 062): the token cache and its refresh, anonymous mode, credit costs, the quota deferral, the
 * 429 backoff and redaction of the client secret.
 */
@IntegrationTest
class OpenSkyAdapterTest extends WireMockSupport {

    private static final String ID = OpenSkyStatesAdapter.ID;
    private static final Instant NOW = Instant.parse("2026-10-13T12:23:26Z");
    private static final String CLIENT_ID = "foresignal-test-client";
    private static final String SECRET = "s3cr3t-Value_with+chars";
    private static final String TOKEN_PATH = "/auth/realms/opensky-network/protocol/openid-connect/token";
    private static final String WORLD = "/api/states/all?extended=1";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private MutableClock clock;
    private BackoffRegistry backoff;
    private UpstreamClient client;
    private CredentialsAccess credentials;
    private OpenSkyStatesAdapter adapter;
    private final Map<String, String> stored = new HashMap<>();

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        stored.clear();
        SourceService sources = mock(SourceService.class);
        credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenAnswer(i -> Map.copyOf(stored));
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(100, 1_000));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        UsageMeter usage = new UsageMeter(jdbc, clock);
        backoff = new BackoffRegistry(jdbc, clock);
        backoff.onSuccess(ID);
        BudgetGovernor budgets = new BudgetGovernor(sources, usage, clock);
        client = new UpstreamClient(
                budgets, backoff, usage, install, credentials, sources, jdbc, jsonMapper, properties, clock);
        client.stateStore(ID).remove(OpenSkyCredits.REMAINING_KEY);
        client.stateStore(ID).remove(OpenSkyCredits.AT_KEY);
        adapter = new OpenSkyStatesAdapter(baseUrl(), baseUrl());
    }

    private DefaultFetchContext context(Map<String, Object> params, RequestRecorder recorder) {
        SourceDescriptor d = adapter.descriptor();
        return new DefaultFetchContext(d, client, recorder, () -> ValidatedParams.of(params), credentials, clock,
                Optional::empty, client.stateStore(ID));
    }

    private DefaultFetchContext context() {
        return context(Map.of(), null);
    }

    private void withCredentials() {
        stored.put(OpenSkyStatesAdapter.CLIENT_ID, CLIENT_ID);
        stored.put(OpenSkyStatesAdapter.CLIENT_SECRET, SECRET);
    }

    private static void serveToken(String token) {
        WIREMOCK.stubFor(post(urlEqualTo(TOKEN_PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"access_token\":\"" + token + "\",\"expires_in\":1800,\"token_type\":\"Bearer\"}")));
    }

    private static void serveStates(String url, String remaining) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(url)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withHeader("X-Rate-Limit-Remaining", remaining)
                .withBody(AdapterFixtures.bytes(ID, "states-bbox.json"))));
    }

    @Nested
    @DisplayName("OAuth2 client credentials")
    class Auth {

        @Test
        @DisplayName("the token is fetched once with a client_credentials form and reused as a Bearer header")
        void tokenReused() throws Exception {
            withCredentials();
            serveToken("tok-1");
            serveStates(WORLD, "3990");

            FetchResult first = adapter.fetch(context());
            adapter.fetch(context());

            assertThat(first.records()).hasSize(30);
            assertThat(first.complete()).isTrue();
            WIREMOCK.verify(1, postRequestedFor(urlEqualTo(TOKEN_PATH))
                    .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
                    .withRequestBody(containing("grant_type=client_credentials"))
                    .withRequestBody(containing("client_id=" + CLIENT_ID)));
            WIREMOCK.verify(2, getRequestedFor(urlEqualTo(WORLD)).withHeader("Authorization", equalTo("Bearer tok-1")));
        }

        @Test
        @DisplayName("the token is refreshed 60 s before its 30-minute expiry")
        void refreshedAfterExpiry() throws Exception {
            withCredentials();
            serveToken("tok-1");
            serveStates(WORLD, "3990");

            adapter.fetch(context());
            clock.advance(Duration.ofMinutes(28));
            adapter.fetch(context());
            WIREMOCK.verify(1, postRequestedFor(urlEqualTo(TOKEN_PATH)));

            clock.advance(Duration.ofMinutes(1).plusSeconds(1));
            adapter.fetch(context());
            WIREMOCK.verify(2, postRequestedFor(urlEqualTo(TOKEN_PATH)));
        }

        @Test
        @DisplayName("a 401 from /states/all refreshes the token once and retries")
        void unauthorizedRefreshesOnce() throws Exception {
            withCredentials();
            WIREMOCK.stubFor(post(urlEqualTo(TOKEN_PATH)).inScenario("tokens").whenScenarioStateIs(Scenario.STARTED)
                    .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                            .withBody("{\"access_token\":\"tok-1\",\"expires_in\":1800}"))
                    .willSetStateTo("second"));
            WIREMOCK.stubFor(post(urlEqualTo(TOKEN_PATH)).inScenario("tokens").whenScenarioStateIs("second")
                    .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                            .withBody("{\"access_token\":\"tok-2\",\"expires_in\":1800}")));
            WIREMOCK.stubFor(WireMock.get(urlEqualTo(WORLD)).withHeader("Authorization", equalTo("Bearer tok-1"))
                    .willReturn(aResponse().withStatus(401)));
            WIREMOCK.stubFor(WireMock.get(urlEqualTo(WORLD)).withHeader("Authorization", equalTo("Bearer tok-2"))
                    .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                            .withBody(AdapterFixtures.bytes(ID, "states-bbox.json"))));

            FetchResult result = adapter.fetch(context());

            assertThat(result.records()).hasSize(30);
            WIREMOCK.verify(2, postRequestedFor(urlEqualTo(TOKEN_PATH)));
            WIREMOCK.verify(2, getRequestedFor(urlEqualTo(WORLD)));
        }

        @Test
        @DisplayName("a second 401 after the refresh fails the fetch instead of looping")
        void secondUnauthorizedFails() {
            withCredentials();
            serveToken("tok-1");
            WIREMOCK.stubFor(WireMock.get(urlEqualTo(WORLD)).willReturn(aResponse().withStatus(401)));

            assertThatThrownBy(() -> adapter.fetch(context())).isInstanceOf(UpstreamHttpException.class);
            WIREMOCK.verify(2, getRequestedFor(urlEqualTo(WORLD)));
        }

        @Test
        @DisplayName("a 400 or 401 from the token endpoint is CredentialsRejectedException")
        void credentialsRejected() {
            withCredentials();
            WIREMOCK.stubFor(post(urlEqualTo(TOKEN_PATH)).willReturn(aResponse().withStatus(401)
                    .withBody("{\"error\":\"unauthorized_client\"}")));

            assertThatThrownBy(() -> adapter.fetch(context())).isInstanceOf(CredentialsRejectedException.class);
            WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/api/states/all")));
        }

        @Test
        @DisplayName("anonymous mode sends no Authorization header and asks for no token")
        void anonymous() throws Exception {
            serveStates(WORLD, "395");

            adapter.fetch(context());

            WIREMOCK.verify(1, getRequestedFor(urlEqualTo(WORLD)).withHeader("Authorization", absent()));
            WIREMOCK.verify(0, postRequestedFor(urlEqualTo(TOKEN_PATH)));
        }

        @Test
        @DisplayName("the client secret never appears in a FetchTrace or a recorded exchange")
        void secretRedacted() throws Exception {
            withCredentials();
            serveToken("tok-1");
            serveStates(WORLD, "3990");
            RequestRecorder recorder = new RequestRecorder();
            DefaultFetchContext ctx = context(Map.of(), recorder);

            adapter.fetch(ctx);

            List<FetchTrace> traces = ctx.traces();
            assertThat(traces).hasSize(2);
            assertThat(traces).extracting(FetchTrace::redactedUrl).noneMatch(url -> url.contains(SECRET));
            for (RecordedExchange exchange : recorder.exchanges()) {
                assertThat(exchange.toString()).doesNotContain(SECRET);
                if (exchange.requestHeaders().containsKey("Authorization")) {
                    assertThat(exchange.requestHeaders().get("Authorization")).containsExactly("***");
                }
            }
        }

        @Test
        @DisplayName("clientId and clientSecret are stored together or not at all")
        void credentialsTogether() {
            assertThat(adapter.checkCredentials(Map.of("clientId", "a"))).containsOnlyKeys("clientSecret");
            assertThat(adapter.checkCredentials(Map.of("clientSecret", "b"))).containsOnlyKeys("clientId");
            assertThat(adapter.checkCredentials(Map.of("clientId", "a", "clientSecret", "b"))).isEmpty();
            assertThat(adapter.checkCredentials(Map.of())).isEmpty();
        }
    }

    @Nested
    @DisplayName("credits")
    class Credits {

        @Test
        @DisplayName("cost by bbox area: ≤ 25 sq° 1, ≤ 100 2, ≤ 400 3, larger or world 4")
        void costTiers() {
            assertThat(OpenSkyStatesAdapter.cost(null)).isEqualTo(4);
            assertThat(OpenSkyStatesAdapter.cost(new Envelope(5, 10, 45, 48))).isEqualTo(1);
            assertThat(OpenSkyStatesAdapter.cost(new Envelope(0, 5, 0, 5))).isEqualTo(1);
            assertThat(OpenSkyStatesAdapter.cost(new Envelope(0, 10, 0, 10))).isEqualTo(2);
            assertThat(OpenSkyStatesAdapter.cost(new Envelope(0, 20, 0, 20))).isEqualTo(3);
            assertThat(OpenSkyStatesAdapter.cost(new Envelope(0, 20, 0, 20.5))).isEqualTo(4);
            assertThat(OpenSkyStatesAdapter.cost(new Envelope(-180, 180, -90, 90))).isEqualTo(4);
        }

        @Test
        @DisplayName("a bbox adds lamin, lomin, lamax and lomax")
        void bboxUrl() throws Exception {
            String url = "/api/states/all?extended=1&lamin=45&lomin=5&lamax=48&lomax=10";
            serveStates(url, "395");

            adapter.fetch(context(Map.of(OpenSkyStatesAdapter.BBOX_PARAM, new Envelope(5, 10, 45, 48)), null));

            WIREMOCK.verify(1, getRequestedFor(urlEqualTo(url)));
        }

        @Test
        @DisplayName("X-Rate-Limit-Remaining and its time are stored after every response")
        void remainingStored() throws Exception {
            serveStates(WORLD, "395");

            adapter.fetch(context());

            AdapterStateStore state = client.stateStore(ID);
            assertThat(state.get(OpenSkyCredits.REMAINING_KEY)).contains("395");
            assertThat(state.get(OpenSkyCredits.AT_KEY)).contains(NOW.toString());
        }

        @Test
        @DisplayName("remaining below cost + reserve defers until the next UTC midnight and sends nothing")
        void deferred() {
            serveStates(WORLD, "43");
            AdapterStateStore state = client.stateStore(ID);
            state.put(OpenSkyCredits.REMAINING_KEY, "43");
            state.put(OpenSkyCredits.AT_KEY, NOW.minusSeconds(600).toString());

            assertThatThrownBy(() -> adapter.fetch(context()))
                    .isInstanceOf(UpstreamQuotaDeferredException.class)
                    .satisfies(e -> assertThat(((UpstreamQuotaDeferredException) e).retryAt())
                            .isEqualTo(Instant.parse("2026-10-14T00:00:00Z")));
            WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/api/states/all")));
            WIREMOCK.verify(0, postRequestedFor(urlEqualTo(TOKEN_PATH)));
        }

        @Test
        @DisplayName("exactly cost + reserve left, a smaller reserve, or yesterday's reading lets the fetch go")
        void notDeferred() throws Exception {
            serveStates(WORLD, "40");
            AdapterStateStore state = client.stateStore(ID);
            state.put(OpenSkyCredits.REMAINING_KEY, "44");
            state.put(OpenSkyCredits.AT_KEY, NOW.toString());
            adapter.fetch(context());

            state.put(OpenSkyCredits.REMAINING_KEY, "10");
            state.put(OpenSkyCredits.AT_KEY, NOW.toString());
            adapter.fetch(context(Map.of(OpenSkyStatesAdapter.RESERVE_PARAM, 5), null));

            state.put(OpenSkyCredits.REMAINING_KEY, "0");
            state.put(OpenSkyCredits.AT_KEY, Instant.parse("2026-10-12T23:59:00Z").toString());
            adapter.fetch(context());

            WIREMOCK.verify(3, getRequestedFor(urlEqualTo(WORLD)));
        }

        @Test
        @DisplayName("a 429 with X-Rate-Limit-Retry-After-Seconds goes to backoff")
        void throttled() {
            WIREMOCK.stubFor(WireMock.get(urlEqualTo(WORLD)).willReturn(aResponse().withStatus(429)
                    .withHeader("X-Rate-Limit-Retry-After-Seconds", "900")));

            assertThatThrownBy(() -> adapter.fetch(context())).isInstanceOf(UpstreamHttpException.class);

            assertThat(backoff.backoffUntil(ID)).isEqualTo(NOW.plusSeconds(900));
        }
    }

    @Test
    @DisplayName("the descriptor: TRACK, LOCAL_ONLY, 15-minute TTL, 90 s floor, 300 s default, 2/min and 1,500/day")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.historyTier()).isEqualTo(HistoryTier.TRACK);
        assertThat(d.egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofMinutes(15));
        assertThat(d.minInterval()).isEqualTo(Duration.ofSeconds(90));
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofSeconds(300));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(2, 1_500));
        assertThat(d.licence()).isEqualTo("OpenSky Network terms (non-commercial research)");
        assertThat(d.termsUrl()).isEqualTo("https://opensky-network.org/about/terms-of-use");
        assertThat(d.attribution()).contains("IPSN 2014");
        assertThat(d.credentialFields()).extracting(f -> f.name()).containsExactly("clientId", "clientSecret");
        assertThat(d.requiresCredentials()).isFalse();
        assertThat(d.param("creditReserve").defaultValue()).isEqualTo(40);
        assertThat(d.accessDeniedIsTerminal()).isFalse();
        assertThat(adapter.tokenUri().toString()).endsWith(TOKEN_PATH);
        assertThat(new OpenSkyStatesAdapter("", "").tokenUri().toString())
                .isEqualTo("https://auth.opensky-network.org/auth/realms/opensky-network/protocol/openid-connect/token");
        assertThat(new OpenSkyStatesAdapter("", "").statesUri(null).toString())
                .isEqualTo("https://opensky-network.org/api/states/all?extended=1");
    }
}
