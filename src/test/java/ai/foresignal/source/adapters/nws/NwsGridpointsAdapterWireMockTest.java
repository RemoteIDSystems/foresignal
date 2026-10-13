package ai.foresignal.source.adapters.nws;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.ForecastSeriesSink;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code nws-gridpoints} against a stubbed {@code api.weather.gov} (prompt 053): the {@code /points} resolution
 * is cached, {@code If-Modified-Since} and its 304 keep the record, a 500 keeps the last forecast and degrades
 * after three in a row, a moved place is resolved again, places leaving the selection are tombstoned, and every
 * request carries Foresignal's User-Agent.
 *
 * <p>Uses the real {@code UpstreamClient} (budget, backoff, conditional GET, User-Agent) pointed at WireMock, an
 * in-memory {@link PlacePoints} and a recording {@link ForecastSeriesSink}.
 */
@IntegrationTest
class NwsGridpointsAdapterWireMockTest extends WireMockSupport {

    static final Instant NOW = Instant.parse("2026-10-12T20:10:00Z");
    static final double LAT = 37.6856;
    static final double LON = -117.3638;
    static final String POINTS = "/points/37.6856,-117.3638";
    static final String GRID = "/gridpoints/VEF/55,177";
    static final String LAST_MODIFIED = "Mon, 12 Oct 2026 19:46:56 GMT";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;

    MutableClock clock;
    WeatherAdapterHarness.Upstream upstream;
    TestPlaces places;
    RecordingSeries series;
    NwsGridpointsAdapter adapter;

    /** Places a test sets. */
    static final class TestPlaces implements PlacePoints {

        final Map<String, PlacePoints.Summary> byId = new LinkedHashMap<>();

        void put(String id, double lon, double lat, List<String> products) {
            byId.put(id, new PlacePoints.Summary(id, id.substring(0, 1).toUpperCase() + id.substring(1), lon, lat, products,
                    byId.containsKey(id) ? byId.get(id).version() + 1 : 1));
        }

        @Override
        public Optional<double[]> point(String placeId) {
            PlacePoints.Summary s = byId.get(placeId);
            return s == null ? Optional.empty() : Optional.of(new double[] {s.lon(), s.lat()});
        }

        @Override
        public List<PlacePoints.Summary> summaries() {
            return new ArrayList<>(byId.values());
        }
    }

    /** A series sink that remembers what it was given. */
    static final class RecordingSeries implements ForecastSeriesSink {

        final Map<String, List<Map<String, Object>>> rows = new LinkedHashMap<>();
        final List<String> removed = new CopyOnWriteArrayList<>();

        @Override
        public void put(String placeId, String provider, Instant updatedAt, Instant validFrom, List<Map<String, Object>> items) {
            rows.put(placeId + "|" + provider, items);
        }

        @Override
        public void remove(String placeId, String provider) {
            rows.remove(placeId + "|" + provider);
            removed.add(placeId + "|" + provider);
        }
    }

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        upstream = WeatherAdapterHarness.upstream(jdbc, jsonMapper, properties, install, clock, Budget.of(100, 3_000), null);
        places = new TestPlaces();
        series = new RecordingSeries();
        adapter = new NwsGridpointsAdapter(() -> places, () -> series, baseUrl());
        places.put("home", LON, LAT, List.of());
    }

    @AfterEach
    void tearDown() {
        upstream.backoff().onSuccess(NwsGridpointsAdapter.ID);
    }

    private WeatherAdapterHarness.Context context(Map<String, Object> params) {
        SourceDescriptor d = adapter.descriptor();
        return new WeatherAdapterHarness.Context(d, upstream.client().forSource(NwsGridpointsAdapter.ID, d),
                ValidatedParams.of(params), state(), clock);
    }

    private AdapterStateStore state() {
        return upstream.client().stateStore(NwsGridpointsAdapter.ID);
    }

    private static void servePoints(String path) {
        WIREMOCK.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/geo+json")
                .withBody(AdapterFixtures.bytes("nws-gridpoints", "points-VEF-55-177.json"))));
    }

    private static void serveGrid() {
        WIREMOCK.stubFor(get(urlPathEqualTo(GRID)).withHeader("If-Modified-Since", absent())
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/geo+json")
                        .withHeader("Last-Modified", LAST_MODIFIED).withHeader("Cache-Control", "public, max-age=3600")
                        .withBody(AdapterFixtures.bytes("nws-gridpoints", "gridpoints-VEF-55-177.json"))));
        WIREMOCK.stubFor(get(urlPathEqualTo(GRID)).withHeader("If-Modified-Since", equalTo(LAST_MODIFIED))
                .willReturn(aResponse().withStatus(304).withHeader("Last-Modified", LAST_MODIFIED)));
    }

    private void atNextDue(String placeId) {
        clock.setInstant(NwsGridpointsAdapter.nextDue(placeId, clock.instant()));
    }

    private static List<NormalizedRecord> live(FetchResult result) {
        return result.records().stream().filter(r -> !r.tombstone()).toList();
    }

    @Test
    @DisplayName("the descriptor is valid: kind nws-forecast, retention 7 d, budget 10/min and 3,000/day, no TTL")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.kinds()).containsExactly(NwsGridpointsAdapter.KIND);
        assertThat(d.kindRetention()).containsEntry(NwsGridpointsAdapter.KIND, Duration.ofDays(7));
        assertThat(d.defaultBudget().perMinute()).isEqualTo(10);
        assertThat(d.defaultBudget().perDay()).isEqualTo(3_000);
        assertThat(d.recordTtl()).isNull();
        assertThat(d.licence()).isEqualTo("US-PD");
        assertThat(d.titleField()).isEqualTo("place");
    }

    @Test
    @DisplayName("first run: /points then the gridpoint; one record with the 24-hour summaries and a cached series")
    void firstRun() throws Exception {
        servePoints(POINTS);
        serveGrid();

        FetchResult result = adapter.fetch(context(Map.of()));

        assertThat(result.records()).singleElement().satisfies(r -> {
            assertThat(r.externalId()).isEqualTo("place:home");
            assertThat(r.kind()).isEqualTo("nws-forecast");
            assertThat(r.observedAt()).isEqualTo(Instant.parse("2026-10-12T19:46:56Z"));
            assertThat(r.eventTime()).isNull();
            assertThat(r.geometry()).as("a place position is sensitive").isNull();
            assertThat(r.payload()).containsEntry("place", "Home").containsEntry("wfo", "VEF").containsEntry("gridX", 55)
                    .containsEntry("gridY", 177).containsEntry("popMax24h", 60).containsEntry("qpf24hMm", 1.4)
                    .containsEntry("tMaxC24h", 6.7).containsEntry("tMinC24h", 2.2)
                    .containsEntry("forecastUpdated", "2026-10-12T19:46:56Z").containsKey("gustMaxMs24h");
            assertThat((List<?>) r.payload().get("hazards")).hasSize(1);
        });
        assertThat(series.rows.get("home|nws")).hasSize(72);
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(POINTS)).withHeader("Accept", equalTo("application/geo+json"))
                .withHeader("User-Agent", containing("+https://foresignal.ai")));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(GRID)).withHeader("User-Agent", containing("Foresignal/")));
        assertThat(state().get("point:home")).hasValueSatisfying(v -> assertThat(v).contains("\"gridId\":\"VEF\""));
    }

    @Test
    @DisplayName("the resolution is cached; the hourly re-fetch sends If-Modified-Since and a 304 keeps the record")
    void cachedResolutionAndNotModified() throws Exception {
        servePoints(POINTS);
        serveGrid();
        adapter.fetch(context(Map.of()));

        clock.setInstant(NOW.plus(Duration.ofMinutes(5)));
        assertThat(adapter.fetch(context(Map.of())).records()).as("not due yet: no request").isEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(GRID)));

        atNextDue("home");
        FetchResult second = adapter.fetch(context(Map.of()));

        assertThat(second.records()).as("304: the record stays as it is").isEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(POINTS)));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo(GRID)).withHeader("If-Modified-Since", equalTo(LAST_MODIFIED)));
        assertThat(series.rows).containsKey("home|nws");
        assertThat(series.removed).isEmpty();
    }

    @Test
    @DisplayName("a 500 keeps the last forecast and retries in 10 minutes; three in a row degrade the source")
    void serverErrorsDegrade() throws Exception {
        servePoints(POINTS);
        serveGrid();
        adapter.fetch(context(Map.of()));
        WIREMOCK.stubFor(get(urlPathEqualTo(GRID)).atPriority(1)
                .willReturn(aResponse().withStatus(500).withBody("{\"title\":\"Unexpected Problem\"}")));

        atNextDue("home");
        for (int i = 1; i <= 3; i++) {
            FetchResult result = adapter.fetch(context(Map.of()));
            assertThat(result.records()).isEmpty();
            assertThat(NwsGridpointsAdapter.failures(state(), "home")).isEqualTo(i);
            if (i < 3) {
                assertThat(state().get(AdapterStateStore.DEGRADED_REASON_KEY)).isEmpty();
            }
            clock.setInstant(clock.instant().plus(NwsGridpointsAdapter.RETRY_AFTER_FAILURE));
        }
        assertThat(state().get(AdapterStateStore.DEGRADED_REASON_KEY)).hasValueSatisfying(reason -> assertThat(reason)
                .contains("place home").contains("failed 3 times in a row").contains("last good forecast is kept"));
        assertThat(series.rows).as("the last good series is kept").containsKey("home|nws");
        assertThat(series.removed).isEmpty();

        // Recovery clears the count and the warning.
        WIREMOCK.resetAll();
        servePoints(POINTS);
        serveGrid();
        state().remove("lastmod:grid:home");
        assertThat(live(adapter.fetch(context(Map.of())))).hasSize(1);
        assertThat(NwsGridpointsAdapter.failures(state(), "home")).isZero();
        assertThat(state().get(AdapterStateStore.DEGRADED_REASON_KEY)).isEmpty();
    }

    @Test
    @DisplayName("a moved place is resolved again and its old gridpoint validators are dropped")
    void movedPlaceReResolves() throws Exception {
        servePoints(POINTS);
        serveGrid();
        adapter.fetch(context(Map.of()));

        places.put("home", -117.36, 37.69, List.of());
        servePoints("/points/37.69,-117.36");
        atNextDue("home");
        FetchResult result = adapter.fetch(context(Map.of()));

        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/points/37.69,-117.36")));
        assertThat(live(result)).as("no validators, so a full 200").hasSize(1);
        assertThat(state().get("point:home")).hasValueSatisfying(v -> assertThat(v).contains("\"lat\":\"37.69\""));
    }

    @Test
    @DisplayName("a 404 from the gridpoint drops the resolution, so the next attempt asks /points again")
    void gridpointGoneReResolves() throws Exception {
        servePoints(POINTS);
        serveGrid();
        adapter.fetch(context(Map.of()));
        WIREMOCK.stubFor(get(urlPathEqualTo(GRID)).atPriority(1).willReturn(aResponse().withStatus(404)));

        atNextDue("home");
        adapter.fetch(context(Map.of()));
        assertThat(state().get("point:home")).isEmpty();

        WIREMOCK.resetAll();
        servePoints(POINTS);
        serveGrid();
        clock.setInstant(clock.instant().plus(NwsGridpointsAdapter.RETRY_AFTER_FAILURE));
        assertThat(live(adapter.fetch(context(Map.of())))).hasSize(1);
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo(POINTS)));
    }

    @Test
    @DisplayName("a place outside NWS coverage (404 from /points) is named in the health warning, never retried hourly")
    void outsideCoverage() throws Exception {
        places.byId.clear();
        places.put("abroad", 2.35, 48.86, List.of());
        WIREMOCK.stubFor(get(urlPathEqualTo("/points/48.86,2.35")).willReturn(aResponse().withStatus(404)));

        assertThat(adapter.fetch(context(Map.of())).records()).isEmpty();
        assertThat(state().get(AdapterStateStore.DEGRADED_REASON_KEY)).hasValueSatisfying(
                reason -> assertThat(reason).contains("place abroad is outside NWS gridpoint coverage"));

        atNextDue("abroad");
        adapter.fetch(context(Map.of()));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/points/48.86,2.35")));
    }

    @Test
    @DisplayName("a deleted or deselected place is tombstoned and its series deleted; allPlaces=false needs product nws")
    void selectionAndTombstones() throws Exception {
        servePoints(POINTS);
        serveGrid();
        adapter.fetch(context(Map.of()));

        FetchResult deselected = adapter.fetch(context(Map.of(NwsGridpointsAdapter.ALL_PLACES_PARAM, false)));

        assertThat(deselected.records()).singleElement().satisfies(r -> {
            assertThat(r.tombstone()).isTrue();
            assertThat(r.externalId()).isEqualTo("place:home");
        });
        assertThat(series.removed).containsExactly("home|nws");
        assertThat(state().get("point:home")).isEmpty();

        places.put("home", LON, LAT, List.of("nws"));
        FetchResult selected = adapter.fetch(context(Map.of(NwsGridpointsAdapter.ALL_PLACES_PARAM, false)));
        assertThat(live(selected)).hasSize(1);

        places.byId.clear();
        FetchResult deleted = adapter.fetch(context(Map.of()));
        assertThat(deleted.records()).extracting(NormalizedRecord::externalId).containsExactly("place:home");
        assertThat(deleted.records().get(0).tombstone()).isTrue();
    }

    @Test
    @DisplayName("more places than maxPlaces: only the first by id are forecast, with a health warning; at most 4 a run")
    void maxPlacesAndPerRunCap() throws Exception {
        places.byId.clear();
        for (String id : List.of("a1", "a2", "a3", "a4", "a5", "a6")) {
            places.put(id, LON, LAT, List.of());
        }
        servePoints(POINTS);
        serveGrid();

        FetchResult first = adapter.fetch(context(Map.of(NwsGridpointsAdapter.MAX_PLACES_PARAM, 5)));

        assertThat(first.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("place:a1", "place:a2", "place:a3", "place:a4");
        assertThat(state().get(AdapterStateStore.DEGRADED_REASON_KEY)).hasValueSatisfying(
                reason -> assertThat(reason).contains("6 places selected; only the first 5"));
        FetchResult second = adapter.fetch(context(Map.of(NwsGridpointsAdapter.MAX_PLACES_PARAM, 5)));
        assertThat(second.records()).extracting(NormalizedRecord::externalId).containsExactly("place:a5");
    }

    @Test
    @DisplayName("helpers: 4-decimal coordinates, the gridpoint URL allow-list, slot minutes and next due times")
    void helpers() {
        assertThat(NwsGridpointsAdapter.coordinate(32.776712)).isEqualTo("32.7767");
        assertThat(NwsGridpointsAdapter.coordinate(-96.79700)).isEqualTo("-96.797");
        assertThat(NwsGridpointsAdapter.coordinate(30.0)).isEqualTo("30");
        assertThat(adapter.gridUri("https://api.weather.gov/gridpoints/FWD/89,104")).hasValueSatisfying(
                u -> assertThat(u.toString()).isEqualTo(baseUrl() + "/gridpoints/FWD/89,104"));
        assertThat(adapter.gridUri("https://evil.example/gridpoints/FWD/89,104")).isEmpty();
        assertThat(adapter.gridUri("https://api.weather.gov/gridpoints/FWD/89,104/forecast?x=1")).isEmpty();
        int slot = NwsGridpointsAdapter.slotMinute("home");
        assertThat(slot).isBetween(0, 59);
        Instant next = NwsGridpointsAdapter.nextDue("home", NOW);
        assertThat(Duration.between(NOW, next)).isBetween(Duration.ofMinutes(45), Duration.ofMinutes(105));
        assertThat(next.atZone(java.time.ZoneOffset.UTC).getMinute()).isEqualTo(slot);
        assertThat(NwsGridpointsAdapter.nextDue("home", next)).isEqualTo(next.plus(Duration.ofHours(1)));
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(NwsGridpointsAdapter.MAX_PLACES_PARAM, 0))))
                .containsKey(NwsGridpointsAdapter.MAX_PLACES_PARAM);
    }
}
