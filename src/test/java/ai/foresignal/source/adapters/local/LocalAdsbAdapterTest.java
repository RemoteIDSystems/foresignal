package ai.foresignal.source.adapters.local;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.ParamValidator;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code local-adsb-1} against a WireMock receiver on 127.0.0.1 through the real {@link UpstreamClient}
 * (prompt 069): coverage with one receiver and the positioned aircraft, a stale {@code now} degrading health
 * and a fresh one clearing it, a 978 MHz band, a non-JSON answer as a format error, a non-{@code aircraft.json}
 * path refused at validation, and the descriptor's local defaults.
 */
@IntegrationTest
class LocalAdsbAdapterTest extends WireMockSupport {

    /** The 1090 fixture's {@code now} is 1791900000.5. */
    private static final Instant FRESH = Instant.ofEpochMilli(1_791_900_002_000L);

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;
    @Autowired private ParamValidator paramValidator;

    private MutableClock clock;
    private UpstreamClient client;
    private LocalAdsbAdapter adapter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(FRESH);
        SourceService sources = mock(SourceService.class);
        CredentialsAccess credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(100, 1_000));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        UsageMeter usage = new UsageMeter(jdbc, clock);
        BackoffRegistry backoff = new BackoffRegistry(jdbc, clock);
        backoff.onSuccess("local-adsb-1");
        BudgetGovernor budgets = new BudgetGovernor(sources, usage, clock);
        client = new UpstreamClient(
                budgets, backoff, usage, install, credentials, sources, jdbc, jsonMapper, properties, clock);
        adapter = new LocalAdsbAdapter(1);
        client.stateStore("local-adsb-1").clearDegraded();
    }

    private FetchContext context(String band) {
        SourceDescriptor d = adapter.descriptor();
        Map<String, Object> values = new java.util.LinkedHashMap<>();
        values.put(LocalAdsbAdapter.URL_PARAM, URI.create(url("/data/aircraft.json")));
        values.put(LocalAdsbAdapter.BAND_PARAM, band);
        return new TestContext(d, client.forSource(d.id(), d), ValidatedParams.of(values), client.stateStore(d.id()), clock);
    }

    private static void serve(String fixture) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo("/data/aircraft.json")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("local-receivers", fixture))));
    }

    @Test
    @DisplayName("a fresh document: records with observers, coverage of one receiver and the positioned aircraft")
    void coverage() throws Exception {
        serve("readsb-aircraft-1090.json");

        FetchResult result = adapter.fetch(context("1090"));

        assertThat(result.records()).hasSize(12);
        assertThat(result.coverage()).isEqualTo(new Coverage(1, null, 10, null));
        assertThat(result.records()).allSatisfy(r -> assertThat(r.observers()).singleElement()
                .satisfies(o -> assertThat(o.receiverId()).isEqualTo("local-adsb-1")));
        assertThat(client.stateStore("local-adsb-1").get(AdapterStateStore.DEGRADED_REASON_KEY)).isEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo("/data/aircraft.json")));
    }

    @Test
    @DisplayName("a document whose now is more than 10 s old degrades health; the next fresh one clears it")
    void stale() throws Exception {
        serve("readsb-aircraft-1090.json");
        clock.setInstant(Instant.ofEpochMilli(1_791_900_011_000L));

        adapter.fetch(context("1090"));
        assertThat(client.stateStore("local-adsb-1").get(AdapterStateStore.DEGRADED_REASON_KEY))
                .hasValueSatisfying(reason -> assertThat(reason).startsWith(LocalAdsbAdapter.STALE_REASON));

        clock.setInstant(FRESH);
        adapter.fetch(context("1090"));
        assertThat(client.stateStore("local-adsb-1").get(AdapterStateStore.DEGRADED_REASON_KEY)).isEmpty();
    }

    @Test
    @DisplayName("exactly 10 s old is still fresh, and another adapter's degraded reason is left alone")
    void staleBoundary() {
        AdapterStateStore state = client.stateStore("local-adsb-1");
        Instant now = Instant.parse("2026-10-13T12:00:00Z");
        LocalAdsbAdapter.markStaleness(state, now.minus(Duration.ofSeconds(10)), now);
        assertThat(state.get(AdapterStateStore.DEGRADED_REASON_KEY)).isEmpty();
        state.degrade("something else");
        LocalAdsbAdapter.markStaleness(state, now, now);
        assertThat(state.get(AdapterStateStore.DEGRADED_REASON_KEY)).hasValue("something else");
        state.clearDegraded();
    }

    @Test
    @DisplayName("band 978 adds band to the payload")
    void uat() throws Exception {
        serve("skyaware978-aircraft.json");
        clock.setInstant(Instant.ofEpochSecond(1_791_900_011L));

        FetchResult result = adapter.fetch(context("978"));

        assertThat(result.records()).allSatisfy(r -> assertThat(r.payload()).containsEntry("band", "978"));
    }

    @Test
    @DisplayName("a 200 that is not readsb JSON is a format error")
    void notJson() {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo("/data/aircraft.json"))
                .willReturn(aResponse().withStatus(200).withBody("<html>login</html>")));
        assertThatThrownBy(() -> adapter.fetch(context("1090"))).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("a path not ending in aircraft.json, a query or credentials are refused at validation")
    void pathRefused() {
        for (String bad : new String[] {"/data/receiver.json", "/data/aircraft.json?x=1", "/data/aircraft.jsonx"}) {
            assertThatThrownBy(() -> paramValidator.validate(adapter.descriptor(),
                    Map.of(LocalAdsbAdapter.URL_PARAM, "http://127.0.0.1:" + WIREMOCK.getPort() + bad)))
                    .as(bad)
                    .isInstanceOfSatisfying(ValidationException.class, e -> assertThat(e.getErrors())
                            .extracting(FieldError::field).containsExactly("params." + LocalAdsbAdapter.URL_PARAM));
        }
        ValidatedParams ok = paramValidator.validate(adapter.descriptor(),
                Map.of(LocalAdsbAdapter.URL_PARAM, "http://127.0.0.1:" + WIREMOCK.getPort() + "/tar1090/data/aircraft.json"));
        assertThat(ok.string(LocalAdsbAdapter.BAND_PARAM)).isEqualTo("1090");
        assertThatThrownBy(() -> paramValidator.validate(adapter.descriptor(),
                Map.of(LocalAdsbAdapter.URL_PARAM, "tcp://127.0.0.1:30003"))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> paramValidator.validate(adapter.descriptor(),
                Map.of(LocalAdsbAdapter.URL_PARAM, "http://8.8.8.8/data/aircraft.json")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("the descriptor: TRACK, TTL 5 min, the local SAMPLED policy, 2 s, LOCAL_ONLY and overridable")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(d.id()).isEqualTo("local-adsb-1");
        assertThat(d.historyTier()).isEqualTo(HistoryTier.TRACK);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofMinutes(5));
        assertThat(d.defaultIngestPolicy()).isEqualTo(new IngestPolicy.Sampled(Duration.ofSeconds(10),
                Duration.ofSeconds(120), 300.0, true, Duration.ofSeconds(5)));
        assertThat(d.thresholds()).extracting(t -> t.field() + ":" + t.delta()).containsExactlyInAnyOrder("alt_baro:150.0",
                "track:10.0");
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofSeconds(2));
        assertThat(d.minInterval()).isEqualTo(Duration.ofSeconds(1));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(60, 50_000));
        assertThat(d.fetchTimeout()).isEqualTo(Duration.ofSeconds(2));
        assertThat(d.maxResponseBytes()).isEqualTo(4 * 1024 * 1024);
        assertThat(d.egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(d.egressOverridable()).isTrue();
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
