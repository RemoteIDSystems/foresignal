package ai.foresignal.source.adapters.airplaneslive;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.HealthAssessment;
import ai.foresignal.collect.HealthEvaluator;
import ai.foresignal.collect.HealthInputs;
import ai.foresignal.collect.HealthSnapshot;
import ai.foresignal.collect.UpstreamAccessDeniedException;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.collect.UsageTotalsWindow;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code airplaneslive-mil} against a stubbed upstream through the real {@link UpstreamClient} (prompt 080): the real
 * approval 403 raises {@link UpstreamAccessDeniedException} carrying "access approval required (contact
 * airplanes.live)", health is {@code failing} with that reason, the retry is hourly and never sooner, nothing is
 * retried with other headers, and the User-Agent is Foresignal's. A 200 (synthetic envelope) maps as readsb.
 */
@IntegrationTest
class AirplanesLiveAdapterTest extends WireMockSupport {

    private static final Instant NOW = Instant.parse("2026-10-13T22:50:12Z");
    private static final String REASON =
            "upstream refused this client (HTTP 403): access approval required (contact airplanes.live)";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private MutableClock clock;
    private BackoffRegistry backoff;
    private UpstreamClient client;
    private AirplanesLiveMilAdapter adapter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        SourceService sources = mock(SourceService.class);
        CredentialsAccess credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(100, 1_000));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        UsageMeter usage = new UsageMeter(jdbc, clock);
        backoff = new BackoffRegistry(jdbc, clock);
        backoff.onSuccess(AirplanesLiveMilAdapter.ID);
        BudgetGovernor budgets = new BudgetGovernor(sources, usage, clock);
        client = new UpstreamClient(
                budgets, backoff, usage, install, credentials, sources, jdbc, jsonMapper, properties, clock);
        adapter = new AirplanesLiveMilAdapter(baseUrl());
    }

    private FetchContext context() {
        SourceDescriptor d = adapter.descriptor();
        return new TestContext(d, client.forSource(d.id(), d), ValidatedParams.empty(), client.stateStore(d.id()),
                clock);
    }

    private static void forbid() {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo("/v2/mil")).willReturn(aResponse().withStatus(403)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("airplaneslive-mil", "forbidden-403.json"))));
    }

    @Test
    @DisplayName("the approval 403 raises UpstreamAccessDeniedException with the approval reason and a one-hour backoff")
    void approvalRefusal() {
        forbid();

        assertThatThrownBy(() -> adapter.fetch(context()))
                .isInstanceOf(UpstreamAccessDeniedException.class)
                .hasMessage(REASON)
                .satisfies(e -> assertThat(((UpstreamAccessDeniedException) e).getDetail())
                        .isEqualTo(AirplanesLiveMilAdapter.ACCESS_REASON));
        assertThat(backoff.backoffUntil(AirplanesLiveMilAdapter.ID)).isEqualTo(NOW.plus(Duration.ofHours(1)));
    }

    @Test
    @DisplayName("health is failing with the approval reason, before and after the backoff runs out")
    void healthReason() {
        HealthSnapshot health = new HealthSnapshot(AirplanesLiveMilAdapter.ID, NOW, null, NOW,
                "UpstreamAccessDeniedException: " + REASON, 403, 1, null, null, List.of(), NOW.plus(Duration.ofHours(1)),
                null, List.of(), null);

        HealthAssessment inBackoff = new HealthEvaluator().evaluate(new HealthInputs(adapter.descriptor(), true, health,
                NOW.plus(Duration.ofHours(1)), UsageTotalsWindow.ZERO, null, null, NOW.plusSeconds(10)));
        assertThat(inBackoff.status()).isEqualTo(HealthStatus.FAILING);
        assertThat(inBackoff.statusReason()).isEqualTo(REASON);

        HealthAssessment later = new HealthEvaluator().evaluate(new HealthInputs(adapter.descriptor(), true, health,
                null, UsageTotalsWindow.ZERO, null, null, NOW.plus(Duration.ofMinutes(61))));
        assertThat(later.status()).isEqualTo(HealthStatus.FAILING);
        assertThat(later.statusReason()).isEqualTo(REASON);
    }

    @Test
    @DisplayName("a refusal without the approval text keeps the generic reason")
    void genericRefusal() {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo("/v2/mil")).willReturn(aResponse().withStatus(451).withBody("no")));

        assertThatThrownBy(() -> adapter.fetch(context()))
                .isInstanceOf(UpstreamAccessDeniedException.class)
                .hasMessage(UpstreamAccessDeniedException.reason(451));
    }

    @Test
    @DisplayName("retries are hourly: nothing is sent during the backoff, one request after it")
    void hourlyRetry() {
        forbid();
        assertThatThrownBy(() -> adapter.fetch(context())).isInstanceOf(UpstreamAccessDeniedException.class);

        clock.advance(Duration.ofMinutes(59));
        assertThatThrownBy(() -> adapter.fetch(context())).isNotInstanceOf(UpstreamHttpException.class);
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo("/v2/mil")));

        clock.advance(Duration.ofMinutes(2));
        assertThatThrownBy(() -> adapter.fetch(context())).isInstanceOf(UpstreamAccessDeniedException.class);
        WIREMOCK.verify(2, getRequestedFor(urlEqualTo("/v2/mil")));
    }

    @Test
    @DisplayName("every request carries Foresignal's User-Agent and no browser or extra headers")
    void userAgent() {
        forbid();
        assertThatThrownBy(() -> adapter.fetch(context())).isInstanceOf(UpstreamAccessDeniedException.class);

        WIREMOCK.verify(getRequestedFor(urlEqualTo("/v2/mil"))
                .withHeader("User-Agent", equalTo(install.userAgent()))
                .withHeader("User-Agent", matching("Foresignal/.+ \\(\\+https://foresignal\\.ai; install=[A-Za-z0-9]+(; contact=[^)]+)?\\)"))
                .withHeader("Referer", absent())
                .withHeader("Origin", absent())
                .withHeader("Authorization", absent())
                .withHeader("Sec-Fetch-Mode", absent()));
    }

    @Test
    @DisplayName("a 200 in the airplanes.live envelope maps through the shared readsb mapper")
    void granted() throws Exception {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo("/v2/mil")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("airplaneslive-mil", "mil-synthetic.json"))));

        FetchResult result = adapter.fetch(context());

        assertThat(result.records()).extracting(r -> r.externalId()).containsExactly("ae57d5", "ae05ac", "ae4ff0");
        assertThat(result.complete()).isTrue();
    }

    @Test
    @DisplayName("the descriptor: refusal reason keyed on the body, 1.1 s pacing, 2/min and 2,880/day, LOCAL_ONLY")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(d.accessDeniedIsTerminal()).isTrue();
        assertThat(d.accessDeniedReasons()).containsEntry("contact@airplanes.live", AirplanesLiveMilAdapter.ACCESS_REASON);
        assertThat(d.hostPacing()).isEqualTo(Duration.ofMillis(1100));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(2, 2_880));
        assertThat(d.credentialFields()).isEmpty();
        assertThat(new AirplanesLiveMilAdapter("").uri().toString()).isEqualTo("https://api.airplanes.live/v2/mil");
    }

    /** The smallest {@link FetchContext} around a real {@link SourceHttp}. */
    private record TestContext(
            SourceDescriptor descriptor, SourceHttp http, ValidatedParams params, AdapterStateStore state, Clock clock)
            implements FetchContext {

        @Override
        public String sourceId() {
            return descriptor.id();
        }

        @Override
        public Map<String, String> credentials() {
            return Map.of();
        }

        @Override
        public Optional<Instant> lastSuccessAt() {
            return Optional.empty();
        }
    }
}
