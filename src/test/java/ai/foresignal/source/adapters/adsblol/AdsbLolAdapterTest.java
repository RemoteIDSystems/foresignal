package ai.foresignal.source.adapters.adsblol;

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

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
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
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.ParamValidator;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
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
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The adsb.lol adapters against a stubbed upstream through the real {@link UpstreamClient} request path
 * (prompt 062): the mil URL, the area URL built from validated parameters, {@code Retry-After}, the
 * non-JSON 200, the 451/403 refusal with its one-hour backoff and health reason, and the User-Agent.
 *
 * <p>As in {@code UsgsEarthquakesAdapterTest}, the client's collaborators are built here so the test owns
 * the clock and the backoff registry.
 */
@IntegrationTest
class AdsbLolAdapterTest extends WireMockSupport {

    private static final Instant NOW = Instant.parse("2026-10-13T12:23:14Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;
    @Autowired private ParamValidator paramValidator;
    @Autowired private SourceRegistry registry;

    private MutableClock clock;
    private BackoffRegistry backoff;
    private UpstreamClient client;
    private AdsbLolMilAdapter mil;
    private AdsbLolAreaAdapter area;

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
        backoff.onSuccess(AdsbLolMilAdapter.ID);
        backoff.onSuccess("adsblol-area-1");
        BudgetGovernor budgets = new BudgetGovernor(sources, usage, clock);
        client = new UpstreamClient(
                budgets, backoff, usage, install, credentials, sources, jdbc, jsonMapper, properties, clock);
        mil = new AdsbLolMilAdapter(baseUrl());
        area = (AdsbLolAreaAdapter) new AdsbLolAreaFamily(baseUrl()).adapters().getFirst();
    }

    private FetchContext context(SourceAdapter adapter, Map<String, Object> params) {
        SourceDescriptor d = adapter.descriptor();
        return new TestContext(d, client.forSource(d.id(), d), ValidatedParams.of(params), client.stateStore(d.id()),
                clock);
    }

    private static void serve(String path, String fixtureDir, String fixture) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes(fixtureDir, fixture))));
    }

    @Nested
    @DisplayName("requests")
    class Requests {

        @Test
        @DisplayName("adsblol-mil GETs /v2/mil and returns every aircraft as a complete result")
        void milUrl() throws Exception {
            serve("/v2/mil", "adsblol-mil", "mil.json");

            FetchResult result = mil.fetch(context(mil, Map.of()));

            assertThat(result.records()).hasSize(30);
            assertThat(result.complete()).isTrue();
            assertThat(result.records()).extracting(r -> r.payload().get("mil")).containsOnly(true);
            WIREMOCK.verify(1, getRequestedFor(urlEqualTo("/v2/mil")));
        }

        @Test
        @DisplayName("an area slot builds /v2/point/{lat}/{lon}/{radiusNm} from its parameters, with coverage")
        void areaUrl() throws Exception {
            serve("/v2/point/41.98/-87.9/250", "adsblol-area", "point-41.98--87.90-250.json");

            FetchResult result = area.fetch(context(area, Map.of("lat", 41.98, "lon", -87.9, "radiusNm", 250)));

            assertThat(result.records()).hasSize(30);
            assertThat(result.records()).extracting(r -> r.externalId()).contains("~2ab098", "~2ab131");
            assertThat(result.coverage().actual()).isEqualTo(30);
            Envelope region = result.coverage().region();
            assertThat(region.getMinY()).isCloseTo(41.98 - 250 / 60.0, org.assertj.core.data.Offset.offset(1e-9));
            assertThat(region.getMaxY()).isCloseTo(41.98 + 250 / 60.0, org.assertj.core.data.Offset.offset(1e-9));
            assertThat(region.getMinX()).isLessThan(-87.9 - 250 / 60.0);
            WIREMOCK.verify(1, getRequestedFor(urlEqualTo("/v2/point/41.98/-87.9/250")));
        }

        @Test
        @DisplayName("the radius defaults to 250 nm and small coordinates are written without an exponent")
        void defaults() {
            assertThat(area.uri(0.0001, -0.5, 250).toString()).endsWith("/v2/point/0.0001/-0.5/250");
            assertThat(area.descriptor().param(AdsbLolAreaAdapter.RADIUS_PARAM).defaultValue()).isEqualTo(250);
        }

        @Test
        @DisplayName("every request carries Foresignal's identifiable User-Agent and no browser headers")
        void userAgent() throws Exception {
            serve("/v2/mil", "adsblol-mil", "mil.json");

            mil.fetch(context(mil, Map.of()));

            WIREMOCK.verify(getRequestedFor(urlEqualTo("/v2/mil"))
                    .withHeader("User-Agent", equalTo(install.userAgent()))
                    .withHeader("User-Agent", matching("Foresignal/.+ \\(\\+https://foresignal\\.ai; install=[A-Za-z0-9]+\\)"))
                    .withHeader("Referer", absent())
                    .withHeader("Origin", absent())
                    .withHeader("Sec-Fetch-Mode", absent()));
        }
    }

    @Nested
    @DisplayName("parameters")
    class Parameters {

        @Test
        @DisplayName("radiusNm 251 is rejected by ParamValidator")
        void radiusTooLarge() {
            SourceDescriptor d = registry.descriptor("adsblol-area-1");
            assertThatThrownBy(() -> paramValidator.validate(d, Map.of("lat", 41.98, "lon", -87.9, "radiusNm", 251)))
                    .isInstanceOf(ValidationException.class)
                    .extracting(e -> ((ValidationException) e).getErrors().stream().map(FieldError::field).toList())
                    .isEqualTo(List.of("params.radiusNm"));
        }

        @Test
        @DisplayName("lat and lon are required numbers in range; a valid set passes with the default radius")
        void center() {
            SourceDescriptor d = registry.descriptor("adsblol-area-2");
            assertThatThrownBy(() -> paramValidator.validate(d, Map.of())).isInstanceOf(ValidationException.class);
            assertThatThrownBy(() -> paramValidator.validate(d, Map.of("lat", 91, "lon", 0)))
                    .isInstanceOf(ValidationException.class);
            assertThatThrownBy(() -> paramValidator.validate(d, Map.of("lat", 0, "lon", "http://evil")))
                    .isInstanceOf(ValidationException.class);
            ValidatedParams ok = paramValidator.validate(d, Map.of("lat", 51.47, "lon", -0.45));
            assertThat(ok.integer(AdsbLolAreaAdapter.RADIUS_PARAM)).isEqualTo(250);
        }

        @Test
        @DisplayName("checkParams reports each out-of-range value")
        void checkParams() {
            assertThat(area.checkParams(ValidatedParams.of(Map.of("lat", -90.5, "lon", 181.0, "radiusNm", 0))))
                    .containsOnlyKeys("lat", "lon", "radiusNm");
            assertThat(area.checkParams(ValidatedParams.of(Map.of("lat", 90.0, "lon", -180.0, "radiusNm", 1))))
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("etiquette and failures")
    class Failures {

        @Test
        @DisplayName("Retry-After on a 429 is honored")
        void retryAfter() {
            WIREMOCK.stubFor(WireMock.get(urlEqualTo("/v2/mil"))
                    .willReturn(aResponse().withStatus(429).withHeader("Retry-After", "120")));

            assertThatThrownBy(() -> mil.fetch(context(mil, Map.of()))).isInstanceOf(UpstreamHttpException.class);

            assertThat(backoff.backoffUntil(AdsbLolMilAdapter.ID)).isEqualTo(NOW.plusSeconds(120));
        }

        @Test
        @DisplayName("Retry-After on another 4xx/5xx is honored too")
        void retryAfterOnServerError() {
            WIREMOCK.stubFor(WireMock.get(urlEqualTo("/v2/mil"))
                    .willReturn(aResponse().withStatus(502).withHeader("Retry-After", "60")));

            assertThatThrownBy(() -> mil.fetch(context(mil, Map.of()))).isInstanceOf(UpstreamHttpException.class);

            assertThat(backoff.backoffUntil(AdsbLolMilAdapter.ID)).isEqualTo(NOW.plusSeconds(60));
        }

        @Test
        @DisplayName("a 429 without Retry-After backs off 30 s")
        void throttleDefault() {
            WIREMOCK.stubFor(WireMock.get(urlEqualTo("/v2/mil")).willReturn(aResponse().withStatus(429)));

            assertThatThrownBy(() -> mil.fetch(context(mil, Map.of()))).isInstanceOf(UpstreamHttpException.class);

            assertThat(backoff.backoffUntil(AdsbLolMilAdapter.ID)).isEqualTo(NOW.plusSeconds(30));
        }

        @Test
        @DisplayName("an HTML 200 is UpstreamFormatException: non-JSON 200, treated as throttling")
        void htmlBody() {
            WIREMOCK.stubFor(WireMock.get(urlEqualTo("/v2/mil")).willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "text/html; charset=utf-8")
                    .withBody(AdapterFixtures.bytes("adsblol-mil", "adsblol-html-200.html"))));

            assertThatThrownBy(() -> mil.fetch(context(mil, Map.of())))
                    .isInstanceOf(UpstreamFormatException.class)
                    .hasMessageContaining("non-JSON 200, treated as throttling");
        }

        @Test
        @DisplayName("a JSON content type whose body does not start with { is refused the same way")
        void jsonTypeHtmlBody() {
            WIREMOCK.stubFor(WireMock.get(urlEqualTo("/v2/mil")).willReturn(aResponse().withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("<!DOCTYPE html><p>wait</p>")));

            assertThatThrownBy(() -> mil.fetch(context(mil, Map.of())))
                    .isInstanceOf(UpstreamFormatException.class)
                    .hasMessageContaining("non-JSON 200");
        }

        @ParameterizedTest
        @ValueSource(ints = {451, 403})
        @DisplayName("451 and 403 refuse this client: a one-hour backoff and the failing health reason")
        void accessDenied(int status) {
            WIREMOCK.stubFor(WireMock.get(urlEqualTo("/v2/mil"))
                    .willReturn(aResponse().withStatus(status).withHeader("Retry-After", "10").withBody("blocked")));

            assertThatThrownBy(() -> mil.fetch(context(mil, Map.of())))
                    .isInstanceOf(UpstreamAccessDeniedException.class)
                    .hasMessage("upstream refused this client (HTTP " + status + "); see Operator Guide → adsb.lol access");
            assertThat(backoff.backoffUntil(AdsbLolMilAdapter.ID)).isEqualTo(NOW.plus(Duration.ofHours(1)));

            HealthSnapshot health = new HealthSnapshot(AdsbLolMilAdapter.ID, NOW, null, NOW,
                    "UpstreamAccessDeniedException: " + UpstreamAccessDeniedException.reason(status), status, 1,
                    null, null, List.of(), NOW.plus(Duration.ofHours(1)), null, List.of(), null);
            HealthAssessment assessed = new HealthEvaluator().evaluate(new HealthInputs(mil.descriptor(), true, health,
                    NOW.plus(Duration.ofHours(1)), UsageTotalsWindow.ZERO, null, null, NOW.plusSeconds(10)));
            assertThat(assessed.status()).isEqualTo(HealthStatus.FAILING);
            assertThat(assessed.statusReason())
                    .isEqualTo("upstream refused this client (HTTP " + status + "); see Operator Guide → adsb.lol access");

            // Still failing after the backoff ran out but before the hourly retry succeeds.
            HealthAssessment later = new HealthEvaluator().evaluate(new HealthInputs(mil.descriptor(), true, health,
                    null, UsageTotalsWindow.ZERO, null, null, NOW.plus(Duration.ofMinutes(61))));
            assertThat(later.status()).isEqualTo(HealthStatus.FAILING);
        }

        @Test
        @DisplayName("while in backoff nothing is sent")
        void noTighterLoop() {
            WIREMOCK.stubFor(WireMock.get(urlEqualTo("/v2/mil")).willReturn(aResponse().withStatus(451)));
            assertThatThrownBy(() -> mil.fetch(context(mil, Map.of()))).isInstanceOf(UpstreamAccessDeniedException.class);

            clock.advance(Duration.ofMinutes(59));
            assertThatThrownBy(() -> mil.fetch(context(mil, Map.of()))).isNotInstanceOf(UpstreamHttpException.class);

            WIREMOCK.verify(1, getRequestedFor(urlEqualTo("/v2/mil")));
        }
    }

    @Nested
    @DisplayName("descriptors")
    class Descriptors {

        @Test
        @DisplayName("mil and the three area slots pass registry validation with the prompt's values")
        void values() {
            SourceDescriptor m = mil.descriptor();
            assertThat(DescriptorValidator.problems(m, mil)).isEmpty();
            assertThat(m.minInterval()).isEqualTo(Duration.ofSeconds(15));
            assertThat(m.defaultInterval()).isEqualTo(Duration.ofSeconds(30));
            assertThat(m.defaultBudget()).isEqualTo(Budget.of(4, 3_000));
            assertThat(m.licence()).isEqualTo("ODbL-1.0");
            assertThat(m.termsUrl()).isEqualTo("https://github.com/adsblol/api");
            assertThat(m.attribution()).isEqualTo("adsb.lol");
            assertThat(m.egress()).isEqualTo(Egress.OPEN);
            assertThat(m.accessDeniedIsTerminal()).isTrue();
            assertThat(m.historyTier()).isEqualTo(HistoryTier.TRACK);
            assertThat(m.zeroResultsNormal()).isFalse();
            assertThat(m.credentialFields()).isEmpty();

            List<SourceAdapter> slots = new AdsbLolAreaFamily("").adapters();
            assertThat(slots).extracting(SourceAdapter::id)
                    .containsExactly("adsblol-area-1", "adsblol-area-2", "adsblol-area-3");
            for (SourceAdapter slot : slots) {
                SourceDescriptor d = slot.descriptor();
                assertThat(DescriptorValidator.problems(d, slot)).isEmpty();
                assertThat(d.minInterval()).isEqualTo(Duration.ofSeconds(30));
                assertThat(d.defaultInterval()).isEqualTo(Duration.ofSeconds(60));
                assertThat(d.defaultBudget()).isEqualTo(Budget.of(2, 1_500));
                assertThat(d.zeroResultsNormal()).isTrue();
                assertThat(d.param("lat").required()).isTrue();
                assertThat(d.param("lon").required()).isTrue();
            }
        }

        @Test
        @DisplayName("the circle envelope widens to the whole longitude range at a pole or the antimeridian")
        void envelope() {
            Envelope polar = AdsbLolAreaAdapter.envelope(89.0, 10.0, 250);
            assertThat(polar.getMinX()).isEqualTo(-180.0);
            assertThat(polar.getMaxY()).isEqualTo(90.0);
            Envelope dateline = AdsbLolAreaAdapter.envelope(0.0, 179.0, 250);
            assertThat(dateline.getMinX()).isEqualTo(-180.0);
            assertThat(dateline.getMaxX()).isEqualTo(180.0);
        }
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
