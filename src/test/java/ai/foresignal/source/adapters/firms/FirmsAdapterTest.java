package ai.foresignal.source.adapters.firms;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.collect.DefaultFetchContext;
import ai.foresignal.collect.FetchTrace;
import ai.foresignal.collect.RecordedExchange;
import ai.foresignal.collect.RequestRecorder;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.AdapterHttpHarness;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code nasa-firms} against a stubbed FIRMS (prompt 065): sequential products, the area segment, the yesterday
 * catch-up, the high-water mark and its overlap, the window, refused keys, non-CSV bodies and key redaction.
 */
@IntegrationTest
@ExtendWith(OutputCaptureExtension.class)
class FirmsAdapterTest extends WireMockSupport {

    private static final String ID = FirmsAdapter.ID;
    private static final String KEY = "0f1e2d3c4b5a69788796a5b4c3d2e1f0";
    private static final Instant NOON = Instant.parse("2026-10-13T12:00:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private ForesignalProperties properties;
    @Autowired private InstallIdService install;

    private AdapterHttpHarness harness;
    private FirmsAdapter adapter;

    @BeforeEach
    void setUp() {
        harness = new AdapterHttpHarness(jdbc, jsonMapper, properties, install, new MutableClock(NOON),
                Budget.of(100, 1000));
        List<String> keys = new ArrayList<>();
        for (String source : FirmsAdapter.ALL_SOURCES) {
            keys.add(FirmsAdapter.HW_PREFIX + source);
            keys.add(FirmsAdapter.PENDING_PREFIX + source);
        }
        harness.reset(ID, keys.toArray(String[]::new));
        harness.credentials.put(FirmsAdapter.MAP_KEY, KEY);
        adapter = new FirmsAdapter(baseUrl());
    }

    private static String path(String source, String area) {
        return "/api/area/csv/" + KEY + "/" + source + "/" + area + "/1";
    }

    private static void serve(String urlRegex, int status, String body) {
        WIREMOCK.stubFor(WireMock.get(urlPathMatching(urlRegex)).willReturn(aResponse().withStatus(status)
                .withHeader("Content-Type", "text/plain;charset=UTF-8").withBody(body)));
    }

    private static void serveAll(String fixture) {
        serve("/api/area/csv/.*", 200, AdapterFixtures.text(ID, fixture));
    }

    private DefaultFetchContext context(Map<String, Object> params, Instant lastSuccess) {
        return harness.context(adapter, params, null, lastSuccess);
    }

    private static Map<String, Object> only(String source) {
        return Map.of(FirmsAdapter.SOURCES_PARAM, List.of(source));
    }

    @Test
    @DisplayName("products are fetched one after another, in the configured order, with the world area")
    void sequential() throws Exception {
        WIREMOCK.stubFor(WireMock.get(urlPathMatching("/api/area/csv/.*")).willReturn(aResponse().withStatus(200)
                .withFixedDelay(150).withBody(AdapterFixtures.text(ID, "header-only.csv"))));
        List<String> order = List.of("MODIS_NRT", "VIIRS_SNPP_NRT", "VIIRS_NOAA21_NRT", "VIIRS_NOAA20_NRT");

        adapter.fetch(context(Map.of(FirmsAdapter.SOURCES_PARAM, order), null));

        List<ServeEvent> events = WIREMOCK.getAllServeEvents().stream()
                .sorted(Comparator.comparing(e -> e.getRequest().getLoggedDate())).toList();
        assertThat(events).extracting(e -> e.getRequest().getUrl())
                .containsExactly(order.stream().map(s -> path(s, "world")).toArray(String[]::new));
        for (int i = 1; i < events.size(); i++) {
            long previous = events.get(i - 1).getRequest().getLoggedDate().getTime();
            long start = events.get(i).getRequest().getLoggedDate().getTime();
            assertThat(start - previous).as("request %d waited for request %d", i, i - 1).isGreaterThanOrEqualTo(150);
        }
    }

    @Test
    @DisplayName("the default products are the three VIIRS satellites and MODIS")
    void defaultProducts() throws Exception {
        serveAll("header-only.csv");
        FetchResult result = adapter.fetch(context(Map.of(), null));
        assertThat(result.records()).isEmpty();
        for (String source : FirmsAdapter.DEFAULT_SOURCES) {
            WIREMOCK.verify(1, getRequestedFor(urlEqualTo(path(source, "world"))));
        }
        WIREMOCK.verify(4, anyRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("a configured area is the west,south,east,north path segment")
    void area() throws Exception {
        serveAll("header-only.csv");
        Map<String, Object> params = new HashMap<>(only("VIIRS_NOAA20_NRT"));
        params.put(FirmsAdapter.AREA_PARAM, new Envelope(-125, -66, 24, 50));

        adapter.fetch(context(params, null));

        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(path("VIIRS_NOAA20_NRT", "-125,24,-66,50"))));
        assertThat(FirmsAdapter.areaSegment(ValidatedParams.of(Map.of(FirmsAdapter.AREA_PARAM,
                new Envelope(-180, 180, -90, 90))))).isEqualTo("world");
        assertThat(FirmsAdapter.areaSegment(ValidatedParams.of(Map.of(FirmsAdapter.AREA_PARAM,
                new Envelope(-10.5, 20.25, 0, 1))))).isEqualTo("-10.5,0,20.25,1");
        assertThat(FirmsAdapter.areaSegment(ValidatedParams.empty())).isEqualTo("world");
    }

    @Test
    @DisplayName("yesterday is fetched as well only between 00:00 and 06:00 UTC")
    void yesterdayCatchUp() throws Exception {
        serveAll("header-only.csv");
        harness.clock.setInstant(Instant.parse("2026-10-13T03:00:00Z"));
        adapter.fetch(context(only("VIIRS_NOAA20_NRT"), null));
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(path("VIIRS_NOAA20_NRT", "world"))));
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(path("VIIRS_NOAA20_NRT", "world") + "/2026-10-12")));

        WIREMOCK.resetRequests();
        harness.clock.setInstant(Instant.parse("2026-10-13T05:59:00Z"));
        adapter.fetch(context(only("VIIRS_NOAA20_NRT"), null));
        WIREMOCK.verify(2, anyRequestedFor(anyUrl()));

        WIREMOCK.resetRequests();
        harness.clock.setInstant(Instant.parse("2026-10-13T06:00:00Z"));
        adapter.fetch(context(only("VIIRS_NOAA20_NRT"), null));
        WIREMOCK.verify(1, anyRequestedFor(anyUrl()));
        WIREMOCK.verify(1, getRequestedFor(urlEqualTo(path("VIIRS_NOAA20_NRT", "world"))));
    }

    @Test
    @DisplayName("today and yesterday may repeat a detection; it is emitted once")
    void dedupAcrossDays() throws Exception {
        serveAll("unpadded-times.csv");
        harness.clock.setInstant(Instant.parse("2026-10-13T05:00:00Z"));
        FetchResult result = adapter.fetch(context(only("VIIRS_NOAA20_NRT"), null));
        // 00:05 and 00:45 are in the window; 13:05 is more than 2 h ahead of 05:00.
        assertThat(result.records()).extracting(NormalizedRecord::observedAt).containsExactly(
                Instant.parse("2026-10-13T00:05:00Z"), Instant.parse("2026-10-13T00:45:00Z"));
    }

    @Test
    @DisplayName("high-water: after a successful run only rows from hw − 3 h are emitted")
    void highWater() throws Exception {
        serveAll("viirs.csv");
        FetchResult first = adapter.fetch(context(only("VIIRS_NOAA20_NRT"), null));
        assertThat(first.records()).hasSize(50);
        assertThat(harness.client.stateStore(ID).get(FirmsAdapter.PENDING_PREFIX + "VIIRS_NOAA20_NRT"))
                .contains("2026-10-13T11:31:00Z@2026-10-13T12:00:00Z");
        assertThat(harness.client.stateStore(ID).get(FirmsAdapter.HW_PREFIX + "VIIRS_NOAA20_NRT")).isEmpty();

        harness.clock.advance(Duration.ofMinutes(30));
        FetchResult second = adapter.fetch(context(only("VIIRS_NOAA20_NRT"), NOON));

        assertThat(harness.client.stateStore(ID).get(FirmsAdapter.HW_PREFIX + "VIIRS_NOAA20_NRT"))
                .contains("2026-10-13T11:31:00Z");
        // hw 11:31 − 3 h = 08:31: the rows at 08:31 and later (13 of them) are re-emitted, identical.
        assertThat(second.records()).hasSize(13);
        assertThat(second.records()).allSatisfy(r ->
                assertThat(r.observedAt()).isAfterOrEqualTo(Instant.parse("2026-10-13T08:31:00Z")));
        assertThat(second.records()).containsAll(first.records().subList(37, 50));
    }

    @Test
    @DisplayName("a run whose batch was not ingested never advances the high-water mark")
    void failedHandOff() throws Exception {
        serveAll("viirs.csv");
        adapter.fetch(context(only("VIIRS_NOAA20_NRT"), null));

        harness.clock.advance(Duration.ofMinutes(30));
        FetchResult second = adapter.fetch(context(only("VIIRS_NOAA20_NRT"), NOON.minus(Duration.ofHours(1))));

        assertThat(second.records()).hasSize(50);
        assertThat(harness.client.stateStore(ID).get(FirmsAdapter.HW_PREFIX + "VIIRS_NOAA20_NRT")).isEmpty();
    }

    @Test
    @DisplayName("the window keeps only acquisitions within it of now")
    void window() throws Exception {
        serveAll("viirs.csv");
        Map<String, Object> params = new HashMap<>(only("VIIRS_NOAA20_NRT"));
        params.put(FirmsAdapter.WINDOW_PARAM, Duration.ofHours(2));

        FetchResult result = adapter.fetch(context(params, null));

        assertThat(result.records()).hasSize(7);
        assertThat(result.records()).allSatisfy(r ->
                assertThat(r.observedAt()).isAfterOrEqualTo(Instant.parse("2026-10-13T10:00:00Z")));
        assertThat(FirmsAdapter.window(ValidatedParams.of(Map.of(FirmsAdapter.WINDOW_PARAM, Duration.ofHours(72)))))
                .isEqualTo(Duration.ofHours(48));
        assertThat(FirmsAdapter.window(ValidatedParams.empty())).isEqualTo(Duration.ofHours(24));
    }

    @Test
    @DisplayName("Invalid MAP_KEY, as a 400 or a 200 body, is CredentialsRejectedException")
    void invalidKey() {
        serve("/api/area/csv/.*", 400, AdapterFixtures.text(ID, "invalid-key.txt"));
        assertThatThrownBy(() -> adapter.fetch(context(only("VIIRS_NOAA20_NRT"), null)))
                .isInstanceOf(CredentialsRejectedException.class).hasMessageNotContaining(KEY);

        WIREMOCK.resetAll();
        harness.reset(ID);
        serve("/api/area/csv/.*", 200, "  invalid map_key.\n");
        assertThatThrownBy(() -> adapter.fetch(context(only("VIIRS_NOAA20_NRT"), null)))
                .isInstanceOf(CredentialsRejectedException.class);
    }

    @Test
    @DisplayName("an HTML 200 is UpstreamFormatException with a snippet; a key in the body is masked")
    void html() {
        String page = AdapterFixtures.text(ID, "html-error.html").replace("Please", "Request for " + KEY + ". Please");
        serve("/api/area/csv/.*", 200, page);

        assertThatThrownBy(() -> adapter.fetch(context(only("MODIS_NRT"), null)))
                .isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("MODIS_NRT")
                .hasMessageContaining("Error in processing")
                .hasMessageContaining("***")
                .hasMessageNotContaining(KEY)
                .satisfies(e -> assertThat(((UpstreamFormatException) e).getSnippet().length())
                        .isLessThanOrEqualTo(UpstreamFormatException.MAX_SNIPPET_LENGTH + "… (truncated)".length()));
    }

    @Test
    @DisplayName("the MAP_KEY never appears in traces, recorded exchanges, exception messages or the log")
    void redacted(CapturedOutput output) throws Exception {
        serveAll("viirs.csv");
        RequestRecorder recorder = new RequestRecorder();
        DefaultFetchContext ctx = harness.context(adapter, only("VIIRS_NOAA20_NRT"), recorder);

        adapter.fetch(ctx);

        assertThat(ctx.traces()).isNotEmpty();
        assertThat(ctx.traces()).extracting(FetchTrace::redactedUrl).allSatisfy(url ->
                assertThat(url).doesNotContain(KEY).contains("/api/area/csv/***/VIIRS_NOAA20_NRT/world/1"));
        assertThat(recorder.exchanges()).isNotEmpty();
        for (RecordedExchange exchange : recorder.exchanges()) {
            assertThat(exchange.toString()).doesNotContain(KEY);
        }

        WIREMOCK.resetAll();
        harness.reset(ID);
        serve("/api/area/csv/.*", 500, "internal error for " + KEY);
        DefaultFetchContext failing = harness.context(adapter, only("VIIRS_NOAA20_NRT"), new RequestRecorder());
        assertThatThrownBy(() -> adapter.fetch(failing)).satisfies(e ->
                assertThat(String.valueOf(failing.traces())).doesNotContain(KEY));
        harness.reset(ID);
        assertThat(output.getAll()).doesNotContain(KEY);
    }

    @Test
    @DisplayName("no key, or an unusable one, means no request at all")
    void keyless() {
        harness.credentials.clear();
        assertThatThrownBy(() -> adapter.fetch(context(Map.of(), null)))
                .isInstanceOf(CredentialsRejectedException.class).hasMessageContaining("no request");
        harness.credentials.put(FirmsAdapter.MAP_KEY, "short");
        assertThatThrownBy(() -> adapter.fetch(context(Map.of(), null)))
                .isInstanceOf(CredentialsRejectedException.class);
        harness.credentials.put(FirmsAdapter.MAP_KEY, "has/slash/and/enough");
        assertThatThrownBy(() -> adapter.fetch(context(Map.of(), null)))
                .isInstanceOf(CredentialsRejectedException.class);
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("descriptor: valid defaults, required mapKey, STANDARD/ALL, TTL 24 h anchored on observedAt, 7 d")
    void descriptor() {
        var d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.id()).isEqualTo("nasa-firms");
        assertThat(d.kinds()).containsExactly("fire-detection");
        assertThat(d.requiredCredentialFields()).extracting(f -> f.name()).containsExactly("mapKey");
        assertThat(d.historyTier()).isEqualTo(HistoryTier.STANDARD);
        assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.OBSERVED);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofHours(24));
        assertThat(d.kindRetention()).containsEntry("fire-detection", Duration.ofDays(7));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(6, 600, 1500));
        assertThat(d.rankField()).isEqualTo("frpMw");
        assertThat(d.tileProperties()).containsExactly("frpMw", "confidenceClass", "satellite", "daynight");
        assertThat(d.zeroResultsNormal()).isFalse();
        assertThat(d.fetchTimeout()).isEqualTo(Duration.ofSeconds(120));
        assertThat(d.maxResponseBytes()).isEqualTo(64 * 1024 * 1024);
        assertThat(d.param(FirmsAdapter.SOURCES_PARAM).defaultValue()).isEqualTo(FirmsAdapter.DEFAULT_SOURCES);
    }

    @Test
    @DisplayName("checkParams and checkCredentials")
    void checks() {
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(FirmsAdapter.SOURCES_PARAM, List.of()))))
                .containsKey(FirmsAdapter.SOURCES_PARAM);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(FirmsAdapter.SOURCES_PARAM,
                List.of("MODIS_NRT", "MODIS_NRT"))))).containsKey(FirmsAdapter.SOURCES_PARAM);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(FirmsAdapter.WINDOW_PARAM, Duration.ofMinutes(30)))))
                .containsKey(FirmsAdapter.WINDOW_PARAM);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(FirmsAdapter.WINDOW_PARAM, Duration.ofHours(49)))))
                .containsKey(FirmsAdapter.WINDOW_PARAM);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(FirmsAdapter.AREA_PARAM, new Envelope(1, 1, 2, 3)))))
                .containsKey(FirmsAdapter.AREA_PARAM);
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of(FirmsAdapter.WINDOW_PARAM, Duration.ofHours(48),
                FirmsAdapter.SOURCES_PARAM, List.of("LANDSAT_NRT"))))).isEmpty();
        assertThat(adapter.checkCredentials(Map.of(FirmsAdapter.MAP_KEY, KEY))).isEmpty();
        assertThat(adapter.checkCredentials(Map.of(FirmsAdapter.MAP_KEY, "a b c d e f"))).containsKey("mapKey");
        assertThat(FirmsAdapter.mask("x" + KEY + "y", KEY)).isEqualTo("x***y");
    }
}
