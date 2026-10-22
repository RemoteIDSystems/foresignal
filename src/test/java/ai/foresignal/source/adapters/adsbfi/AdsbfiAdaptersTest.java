package ai.foresignal.source.adapters.adsbfi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.UpstreamAccessDeniedException;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.ParamValidator;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.formats.readsb.AircraftRegion;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The adsb.fi adapters against a stubbed upstream through the real {@link UpstreamClient} request path (prompt 080):
 * the mil and region URLs, {@code distNm} above 250 refused by {@code ParamValidator}, requests serial across every
 * {@code adsbfi-*} source with the 1.1 s host spacing, the 403 refusal, 429 backoff, the non-JSON 200 and the
 * Foresignal User-Agent.
 *
 * <p>As in {@code AdsbLolAdapterTest}, the client's collaborators are built here so the test owns the clock and the
 * backoff registry. All sources use one client, so they share its {@code HostPacer}, as in production.
 */
@IntegrationTest
@Timeout(value = 60, unit = TimeUnit.SECONDS)
class AdsbfiAdaptersTest extends WireMockSupport {

    private static final Instant NOW = Instant.parse("2026-10-13T22:50:15Z");
    private static final String MIL = "/api/v2/mil";
    private static final String REGION = "/api/v3/lat/39.86/lon/-104.67/dist/25";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;
    @Autowired private ParamValidator paramValidator;
    @Autowired private SourceRegistry registry;

    private MutableClock clock;
    private BackoffRegistry backoff;
    private UpstreamClient client;
    private AdsbFiMilAdapter mil;
    private List<AdsbFiRegionAdapter> regions;

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
        BudgetGovernor budgets = new BudgetGovernor(sources, usage, clock);
        client = new UpstreamClient(
                budgets, backoff, usage, install, credentials, sources, jdbc, jsonMapper, properties, clock);
        mil = new AdsbFiMilAdapter(baseUrl());
        regions = new AdsbFiRegionFamily(baseUrl()).adapters().stream().map(a -> (AdsbFiRegionAdapter) a).toList();
        backoff.onSuccess(AdsbFiMilAdapter.ID);
        for (AdsbFiRegionAdapter region : regions) {
            backoff.onSuccess(region.id());
        }
    }

    private FetchContext context(SourceAdapter adapter, Map<String, Object> params) {
        SourceDescriptor d = adapter.descriptor();
        return new TestContext(d, client.forSource(d.id(), d), ValidatedParams.of(params), client.stateStore(d.id()),
                clock);
    }

    private static Map<String, Object> denver() {
        return Map.of("lat", 39.86, "lon", -104.67, "distNm", 25);
    }

    private static void serve(String path, String dir, String fixture, int delayMs) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withFixedDelay(delayMs)
                .withBody(AdapterFixtures.bytes(dir, fixture))));
    }

    @Test
    @DisplayName("adsbfi-mil GETs /api/v2/mil with Foresignal's User-Agent and returns every aircraft")
    void milUrl() throws Exception {
        serve(MIL, "adsbfi-mil", "mil.json", 0);

        FetchResult result = mil.fetch(context(mil, Map.of()));

        assertThat(result.records()).hasSize(30);
        assertThat(result.complete()).isTrue();
        assertThat(mil.uri().toString()).isEqualTo(baseUrl() + MIL);
        assertThat(new AdsbFiMilAdapter("").uri().toString()).isEqualTo("https://opendata.adsb.fi/api/v2/mil");
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(MIL))
                .withHeader("User-Agent", equalTo(install.userAgent()))
                .withHeader("User-Agent", matching("Foresignal/.+")));
    }

    @Test
    @DisplayName("a region slot builds /api/v3/lat/{lat}/lon/{lon}/dist/{distNm} from its parameters, with coverage")
    void regionUrl() throws Exception {
        serve(REGION, "adsbfi-region", "region-39.86--104.67-25.json", 0);
        AdsbFiRegionAdapter region = regions.getFirst();

        FetchResult result = region.fetch(context(region, denver()));

        assertThat(result.records()).hasSize(25);
        assertThat(result.coverage().actual()).isEqualTo(25);
        assertThat(result.coverage().region().getMinY())
                .isCloseTo(39.86 - 25 / 60.0, org.assertj.core.data.Offset.offset(1e-9));
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(REGION)));
        assertThat(region.uri(0.0001, -0.5, 150).toString()).endsWith("/api/v3/lat/0.0001/lon/-0.5/dist/150");
    }

    @Test
    @DisplayName("distNm defaults to 150; 251 is rejected by ParamValidator, as are a missing or out-of-range center")
    void params() {
        SourceDescriptor d = registry.descriptor("adsbfi-region-1");
        assertThatThrownBy(() -> paramValidator.validate(d, Map.of("lat", 39.86, "lon", -104.67, "distNm", 251)))
                .isInstanceOf(ValidationException.class)
                .extracting(e -> ((ValidationException) e).getErrors().stream().map(FieldError::field).toList())
                .isEqualTo(List.of("params.distNm"));
        assertThatThrownBy(() -> paramValidator.validate(d, Map.of())).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> paramValidator.validate(d, Map.of("lat", 91, "lon", 0)))
                .isInstanceOf(ValidationException.class);
        ValidatedParams ok = paramValidator.validate(d, Map.of("lat", 39.86, "lon", -104.67, "label", "DEN"));
        assertThat(ok.integer(AdsbFiRegionAdapter.DIST_PARAM)).isEqualTo(150);
        assertThat(regions.get(3).checkParams(
                ValidatedParams.of(Map.of("lat", -90.5, "lon", 181.0, "distNm", 0)))).containsOnlyKeys("lat", "lon",
                "distNm");
    }

    @Test
    @DisplayName("requests to opendata.adsb.fi never overlap and start at least 1.1 s apart across adsbfi-* sources")
    void serialAcrossSources() throws Exception {
        // Each answer takes 1.5 s, longer than the spacing, so overlapping requests would start less than 1.5 s
        // apart; spaced but overlapping requests would start 1.1 s apart.
        serve(MIL, "adsbfi-mil", "mil.json", 1_500);
        serve(REGION, "adsbfi-region", "region-39.86--104.67-25.json", 1_500);
        serve("/api/v3/lat/60.32/lon/24.96/dist/60", "adsbfi-region", "region-39.86--104.67-25.json", 1_500);

        List<Callable<FetchResult>> fetches = List.of(
                () -> mil.fetch(context(mil, Map.of())),
                () -> regions.get(0).fetch(context(regions.get(0), denver())),
                () -> regions.get(1).fetch(context(regions.get(1),
                        Map.of("lat", 60.32, "lon", 24.96, "distNm", 60))));
        ExecutorService pool = Executors.newFixedThreadPool(3);
        try {
            List<Future<FetchResult>> futures = new ArrayList<>();
            for (Callable<FetchResult> fetch : fetches) {
                futures.add(pool.submit(fetch));
            }
            for (Future<FetchResult> future : futures) {
                assertThat(future.get(30, TimeUnit.SECONDS).records()).isNotEmpty();
            }
        } finally {
            pool.shutdownNow();
        }

        List<Long> received = WIREMOCK.getAllServeEvents().stream()
                .map(ServeEvent::getRequest).map(r -> r.getLoggedDate().getTime())
                .sorted(Comparator.naturalOrder()).toList();
        assertThat(received).hasSize(3);
        for (int i = 1; i < received.size(); i++) {
            assertThat(received.get(i) - received.get(i - 1)).as("gap before request %d", i)
                    .isGreaterThanOrEqualTo(1_400L);
        }
    }

    @Test
    @DisplayName("spacing alone: back-to-back fast requests from two sources start at least 1.1 s apart")
    void spacingAcrossSources() throws Exception {
        serve(MIL, "adsbfi-mil", "mil.json", 0);
        serve(REGION, "adsbfi-region", "region-39.86--104.67-25.json", 0);

        mil.fetch(context(mil, Map.of()));
        regions.getFirst().fetch(context(regions.getFirst(), denver()));

        List<Long> received = WIREMOCK.getAllServeEvents().stream()
                .map(ServeEvent::getRequest).map(r -> r.getLoggedDate().getTime())
                .sorted(Comparator.naturalOrder()).toList();
        assertThat(received.get(1) - received.get(0)).isGreaterThanOrEqualTo(1_050L);
    }

    @Test
    @DisplayName("a 403 refuses this client: UpstreamAccessDeniedException and a one-hour backoff, nothing retried")
    void accessDenied() {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(MIL)).willReturn(aResponse().withStatus(403).withBody("Forbidden")));

        assertThatThrownBy(() -> mil.fetch(context(mil, Map.of()))).isInstanceOf(UpstreamAccessDeniedException.class);

        assertThat(backoff.backoffUntil(AdsbFiMilAdapter.ID)).isEqualTo(NOW.plus(Duration.ofHours(1)));
        clock.advance(Duration.ofMinutes(30));
        assertThatThrownBy(() -> mil.fetch(context(mil, Map.of()))).isNotInstanceOf(UpstreamHttpException.class);
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(MIL)));
    }

    @Test
    @DisplayName("a 429 is throttling: the prompt 005 backoff (Retry-After honored), not a refusal")
    void throttled() {
        WIREMOCK.stubFor(WireMock.get(urlPathMatching("/api/v3/.*"))
                .willReturn(aResponse().withStatus(429).withHeader("Retry-After", "20")));
        AdsbFiRegionAdapter region = regions.getFirst();

        assertThatThrownBy(() -> region.fetch(context(region, denver())))
                .isInstanceOf(UpstreamHttpException.class)
                .isNotInstanceOf(UpstreamAccessDeniedException.class);
        assertThat(backoff.backoffUntil(region.id())).isEqualTo(NOW.plusSeconds(20));
    }

    @Test
    @DisplayName("an HTML 200 is a format error, never an empty layer")
    void htmlBody() {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(MIL)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/html").withBody("<html>busy</html>")));

        assertThatThrownBy(() -> mil.fetch(context(mil, Map.of())))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("non-JSON 200");
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    @DisplayName("adsbfi-blindspots: Shadowbroker's eight 250 nm circles in order, 1.1 s apart, merged by hex")
    void blindSpots() throws Exception {
        List<Duration> pauses = new ArrayList<>();
        AdsbFiBlindSpotAdapter blind = new AdsbFiBlindSpotAdapter(baseUrl(), pauses::add);
        backoff.onSuccess(AdsbFiBlindSpotAdapter.ID);
        List<String> paths = AircraftRegion.BLIND_SPOTS.stream()
                .map(r -> "/api/v3/lat/" + r.latText() + "/lon/" + r.lonText() + "/dist/250").toList();
        serve(paths.getFirst(), "adsbfi-mil", "mil.json", 0);
        for (String path : paths.subList(1, paths.size())) {
            serve(path, "adsbfi-region", "region-39.86--104.67-25.json", 0);
        }

        FetchResult result = blind.fetch(context(blind, Map.of()));

        assertThat(paths.getFirst()).isEqualTo("/api/v3/lat/56.8/lon/60.6/dist/250");
        assertThat(paths.getLast()).isEqualTo("/api/v3/lat/9/lon/38.7/dist/250");
        List<String> order = WIREMOCK.getAllServeEvents().stream()
                .sorted(Comparator.comparing(e -> e.getRequest().getLoggedDate()))
                .map(e -> e.getRequest().getUrl()).toList();
        assertThat(order).containsExactlyElementsOf(paths);
        assertThat(pauses).hasSize(7).containsOnly(Duration.ofMillis(1100));
        assertThat(result.records()).extracting(r -> r.externalId()).doesNotHaveDuplicates();
        assertThat(result.complete()).isTrue();
    }

    @Test
    @DisplayName("adsbfi-blindspots: every 30 min, a 35-minute TTL, on by default; adsbfi-mil is off by default")
    void blindSpotDescriptor() {
        SourceDescriptor d = new AdsbFiBlindSpotAdapter("").descriptor();
        assertThat(ai.foresignal.source.DescriptorValidator.problems(d, new AdsbFiBlindSpotAdapter(""))).isEmpty();
        assertThat(ai.foresignal.source.spi.IngestPolicyValidator.problems(d.defaultIngestPolicy(), d,
                "defaultIngestPolicy")).isEmpty();
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(30));
        assertThat(d.recordTtl()).isEqualTo(Duration.ofMinutes(35));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(10, 400));
        assertThat(d.hostPacing()).isEqualTo(Duration.ofMillis(1100));
        assertThat(d.params()).isEmpty();
        assertThat(d.autoEnable()).isTrue();
        assertThat(mil.descriptor().autoEnable()).isFalse();
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
