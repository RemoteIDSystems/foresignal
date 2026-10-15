package ai.foresignal.source.adapters.usgs;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The USGS adapter against a stubbed upstream (Architecture §4.2 rule 4): the URL per feed, the
 * conditional GET round trip, error mapping and the User-Agent, through the real
 * {@link UpstreamClient} request path.
 *
 * <p>As in {@code UpstreamClientIT}, the client's collaborators are built here so the test owns the
 * clock and the budget; the adapter state store is the real one, because the stored
 * {@code Last-Modified} is what makes the second request conditional.
 */
@IntegrationTest
class UsgsEarthquakesAdapterTest extends WireMockSupport {

    private static final String ID = UsgsEarthquakesAdapter.ID;
    private static final String LAST_MODIFIED = "Sun, 11 Oct 2026 22:32:45 GMT";
    private static final Instant NOW = Instant.parse("2026-10-11T22:33:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private MutableClock clock;
    private UpstreamClient client;
    private UsgsEarthquakesAdapter adapter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        SourceService sources = mock(SourceService.class);
        CredentialsAccess credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(100, 1_000));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        UsageMeter usage = new UsageMeter(jdbc, clock);
        BackoffRegistry backoff = new BackoffRegistry(jdbc, clock);
        BudgetGovernor budgets = new BudgetGovernor(sources, usage, clock);
        client = new UpstreamClient(
                budgets, backoff, usage, install, credentials, sources, jdbc, jsonMapper, properties, clock);
        adapter = new UsgsEarthquakesAdapter(baseUrl());
    }

    private FetchContext context(String feed) {
        Map<String, Object> params = feed == null ? Map.of() : Map.of(UsgsEarthquakesAdapter.FEED_PARAM, feed);
        return new TestContext(adapter.descriptor(), client.forSource(ID, adapter.descriptor()),
                ValidatedParams.of(params), client.stateStore(ID), clock);
    }

    private static String feedPath(String feed) {
        return "/earthquakes/feed/v1.0/summary/" + feed + ".geojson";
    }

    private static void serveFixture(String feed) {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo(feedPath(feed)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=utf-8")
                        .withHeader("Last-Modified", LAST_MODIFIED)
                        .withHeader("Cache-Control", "public, max-age=60")
                        .withBody(AdapterFixtures.bytes(ID, "all_day.geojson"))));
    }

    @Nested
    @DisplayName("requests")
    class Requests {

        @Test
        @DisplayName("the default feed is all_day")
        void defaultFeed() throws Exception {
            serveFixture("all_day");

            FetchResult result = adapter.fetch(context(null));

            assertThat(result.records()).hasSize(25);
            assertThat(result.complete()).isTrue();
            assertThat(result.notModified()).isFalse();
            WIREMOCK.verify(1, getRequestedFor(urlEqualTo(feedPath("all_day"))));
        }

        @ParameterizedTest
        @ValueSource(strings = {"all_hour", "2.5_week", "4.5_month", "significant_month"})
        @DisplayName("the URL follows the feed parameter")
        void feedSelectsUrl(String feed) throws Exception {
            serveFixture(feed);

            adapter.fetch(context(feed));

            WIREMOCK.verify(1, getRequestedFor(urlEqualTo(feedPath(feed))));
        }

        @Test
        @DisplayName("every request carries Foresignal's identifiable User-Agent")
        void userAgent() throws Exception {
            serveFixture("all_day");

            adapter.fetch(context("all_day"));

            WIREMOCK.verify(getRequestedFor(urlEqualTo(feedPath("all_day")))
                    .withHeader("User-Agent", equalTo(install.userAgent()))
                    .withHeader("User-Agent", matching("Foresignal/.+ \\(\\+https://foresignal\\.ai; install=[A-Za-z0-9]+(; contact=[^)]+)?\\)")));
        }
    }

    @Nested
    @DisplayName("conditional GET")
    class Conditional {

        @Test
        @DisplayName("a 200 stores Last-Modified, the next request sends If-Modified-Since, and a 304 is unchanged")
        void roundTrip() throws Exception {
            WIREMOCK.stubFor(WireMock.get(urlEqualTo(feedPath("all_day")))
                    .withHeader("If-Modified-Since", absent())
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Last-Modified", LAST_MODIFIED)
                            .withBody(AdapterFixtures.bytes(ID, "all_day.geojson"))));
            WIREMOCK.stubFor(WireMock.get(urlEqualTo(feedPath("all_day")))
                    .withHeader("If-Modified-Since", equalTo(LAST_MODIFIED))
                    .willReturn(aResponse().withStatus(304).withHeader("Last-Modified", LAST_MODIFIED)));

            FetchResult first = adapter.fetch(context("all_day"));
            assertThat(first.records()).hasSize(25);
            AdapterStateStore state = client.stateStore(ID);
            assertThat(state.get("lastmod:feed:all_day")).contains(LAST_MODIFIED);

            FetchResult second = adapter.fetch(context("all_day"));
            assertThat(second.notModified()).isTrue();
            assertThat(second.records()).isEmpty();

            WIREMOCK.verify(1, getRequestedFor(urlEqualTo(feedPath("all_day"))).withHeader("If-Modified-Since", absent()));
            WIREMOCK.verify(1, getRequestedFor(urlEqualTo(feedPath("all_day")))
                    .withHeader("If-Modified-Since", equalTo(LAST_MODIFIED)));
        }

        @Test
        @DisplayName("validators are kept per feed")
        void perFeedValidators() throws Exception {
            serveFixture("all_day");
            WIREMOCK.stubFor(WireMock.get(urlEqualTo(feedPath("all_hour")))
                    .willReturn(aResponse().withStatus(200).withBody(AdapterFixtures.bytes(ID, "all_day.geojson"))));

            adapter.fetch(context("all_day"));
            adapter.fetch(context("all_hour"));

            WIREMOCK.verify(1, getRequestedFor(urlEqualTo(feedPath("all_hour"))).withHeader("If-Modified-Since", absent()));
        }
    }

    @Nested
    @DisplayName("failures")
    class Failures {

        @Test
        @DisplayName("a 500 is an UpstreamHttpException with the status")
        void serverError() {
            WIREMOCK.stubFor(WireMock.get(urlEqualTo(feedPath("all_day")))
                    .willReturn(aResponse().withStatus(500).withBody("upstream broke")));

            assertThatThrownBy(() -> adapter.fetch(context("all_day")))
                    .isInstanceOf(UpstreamHttpException.class)
                    .extracting(e -> ((UpstreamHttpException) e).getStatus())
                    .isEqualTo(500);
        }

        @Test
        @DisplayName("a body that is not a FeatureCollection is an UpstreamFormatException")
        void formatChange() {
            WIREMOCK.stubFor(WireMock.get(urlEqualTo(feedPath("all_day")))
                    .willReturn(aResponse().withStatus(200)
                            .withBody(AdapterFixtures.bytes(ID, "not-a-feature-collection.json"))));

            assertThatThrownBy(() -> adapter.fetch(context("all_day"))).isInstanceOf(UpstreamFormatException.class);
        }

        @Test
        @DisplayName("an empty feature list is a complete, empty result")
        void quietHour() throws Exception {
            WIREMOCK.stubFor(WireMock.get(urlEqualTo(feedPath("significant_hour")))
                    .willReturn(aResponse().withStatus(200)
                            .withBody("{\"type\":\"FeatureCollection\",\"metadata\":{\"count\":0},\"features\":[]}")));

            FetchResult result = adapter.fetch(context("significant_hour"));

            assertThat(result.records()).isEmpty();
            assertThat(result.complete()).isTrue();
        }
    }

    @Nested
    @DisplayName("descriptor")
    class Descriptor {

        private final SourceDescriptor d = adapter().descriptor();

        private UsgsEarthquakesAdapter adapter() {
            return new UsgsEarthquakesAdapter("");
        }

        @Test
        @DisplayName("passes registry validation")
        void valid() {
            assertThat(DescriptorValidator.problems(d, adapter())).isEmpty();
        }

        @Test
        @DisplayName("has the values the prompt specifies")
        void values() {
            assertThat(d.id()).isEqualTo("usgs-earthquakes");
            assertThat(d.name()).isEqualTo("USGS Earthquakes");
            assertThat(d.kinds()).containsExactly("earthquake");
            assertThat(d.homepage()).isEqualTo("https://earthquake.usgs.gov/earthquakes/feed/");
            assertThat(d.attribution()).isEqualTo("U.S. Geological Survey, Earthquake Hazards Program");
            assertThat(d.licence()).isEqualTo("US-PD");
            assertThat(d.termsUrl())
                    .isEqualTo("https://www.usgs.gov/information-policies-and-instructions/copyrights-and-credits");
            assertThat(d.mode()).isEqualTo(Mode.POLLING);
            assertThat(d.credentialFields()).isEmpty();
            assertThat(d.minInterval()).isEqualTo(Duration.ofSeconds(60));
            assertThat(d.defaultInterval()).isEqualTo(Duration.ofSeconds(60));
            assertThat(d.defaultBudget()).isEqualTo(Budget.of(4, 2_000));
            assertThat(d.historyTier()).isEqualTo(HistoryTier.STANDARD);
            assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
            assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.RECEIVED);
            assertThat(d.recordTtl()).isNull();
            assertThat(d.defaultIngestPolicy()).isEqualTo(IngestPolicy.all());
            assertThat(d.defaultIngestPolicy().keyframe()).isNull();
            assertThat(d.volatileFields()).containsExactly("updated");
            assertThat(d.significantFields()).isEmpty();
            assertThat(d.titleField()).isEqualTo("title");
            assertThat(d.zeroResultsNormal()).isTrue();
            assertThat(d.tileProperties()).containsExactly("mag", "place", "depth", "tsunami", "alert", "sig");
            assertThat(d.searchFields()).containsExactly("place", "title");
            assertThat(d.rankField()).isEqualTo("mag");
            assertThat(d.embedFields()).isEmpty();
            assertThat(d.defaultRetention()).isEqualTo(Duration.ofDays(1825));
            assertThat(d.kindDisplay().get("earthquake").titleTemplate()).isEqualTo("M {mag} - {place}");
            assertThat(d.kindDisplay().get("earthquake").icon()).isEqualTo("earthquake");
            assertThat(d.kindDisplay().get("earthquake").color()).isEqualTo("#D9480F");
        }

        @Test
        @DisplayName("feed is a string parameter defaulting to all_day with the 20 published feeds")
        void feedParam() {
            ParamSpec feed = d.param("feed");
            assertThat(feed.defaultValue()).isEqualTo("all_day");
            assertThat(feed.required()).isFalse();
            assertThat(feed.allowed()).hasSize(20).contains("all_hour", "2.5_day", "significant_month");
        }

        @Test
        @DisplayName("depth is declared in km with its datum, the time fields in ms")
        void fieldSpecs() {
            assertThat(d.fieldSpecs().get("depth").unit()).isEqualTo("km");
            assertThat(d.fieldSpecs().get("depth").datum()).isEqualTo("network-dependent reference, positive down");
            assertThat(d.fieldSpecs().get("mag").unit()).isEqualTo("1");
            assertThat(d.fieldSpecs().get("time").unit()).isEqualTo("ms");
            assertThat(d.fieldSpecs().get("updated").label()).isEqualTo("Last updated");
            assertThat(d.fieldSpecs().get("nst").unit()).isEqualTo("{station}");
            assertThat(d.fieldSpecs().get("felt").unit()).isEqualTo("{report}");
            assertThat(d.fieldSpecs().get("rms").unit()).isEqualTo("s");
            assertThat(d.fieldSpecs().get("gap").unit()).isEqualTo("deg");
        }

        @Test
        @DisplayName("the base URL defaults to USGS and the test property overrides it")
        void baseUrl() {
            assertThat(new UsgsEarthquakesAdapter(null).baseUrl()).isEqualTo("https://earthquake.usgs.gov");
            assertThat(new UsgsEarthquakesAdapter("  ").feedUri("all_day"))
                    .isEqualTo(URI.create("https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson"));
            assertThat(new UsgsEarthquakesAdapter("http://localhost:1234/").feedUri("4.5_week"))
                    .isEqualTo(URI.create("http://localhost:1234/earthquakes/feed/v1.0/summary/4.5_week.geojson"));
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

    @Test
    @DisplayName("records survive the round trip with their ids")
    void ids() throws Exception {
        serveFixture("all_day");
        List<String> ids = adapter.fetch(context("all_day")).records().stream()
                .map(r -> r.externalId()).toList();
        assertThat(ids).contains("aka2026uekvdg", "us6000u1kv", "us6000u1i6");
    }
}
