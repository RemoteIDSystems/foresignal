package ai.foresignal.source.adapters.goes;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.place.PlacesAoiService;
import ai.foresignal.raster.decode.NetcdfDecoder;
import ai.foresignal.raster.lightning.FlashArchive;
import ai.foresignal.raster.lightning.FlashStore;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.raster.ProductAdapterHarness;
import ai.foresignal.testsupport.weather.FlashTestSink;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code goes-glm} against a stubbed S3 (prompt 048): granules in key order with the cursor, the 60-minute
 * catch-up cap, the per-minute budget, the freeze while nothing new is processed, a silent format change,
 * a failed download and the probe. The flash store is real, with an AOI covering the whole Earth so the
 * captured granule's 37 clusters reach the ring.
 */
@IntegrationTest
class GoesGlmAdapterTest extends WireMockSupport {

    static final String BUCKET = GoesGlmAdapter.BUCKET;
    static final String HOUR_21 = "GLM-L2-LCFA/2026/285/21/";
    static final String HOUR_20 = "GLM-L2-LCFA/2026/285/20/";
    /** Captured fixture sizes, so the listing matches what is served. */
    static final Map<String, Long> SIZES = Map.of(GlmParserTest.ACTIVE, 432_989L, GlmParserTest.QUIET, 255_210L,
            GlmParserTest.FDC, 524_009L);

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;
    @Autowired NetcdfDecoder decoder;

    @TempDir Path glm;

    MutableClock clock;
    SourceService sources;
    BudgetGovernor budgets;
    UsageMeter usage;
    GoesGlmAdapter adapter;
    ProductAdapterHarness.MapState state;
    FlashStore store;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-10-12T21:58:30Z"));
        sources = mock(SourceService.class);
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(100, 10_000, 3_000));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        usage = new UsageMeter(jdbc, clock);
        budgets = new BudgetGovernor(sources, usage, clock);
        adapter = new GoesGlmAdapter(decoder, baseUrl());
        state = new ProductAdapterHarness.MapState();
        PlacesAoiService world = mock(PlacesAoiService.class);
        when(world.version()).thenReturn(1L);
        when(world.aoi()).thenReturn(box(-180, -90, 180, 90));
        store = new FlashStore(new FlashArchive(glm), world, FlashStore.Scope.AOI, clock);
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/")).atPriority(10)
                .willReturn(aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(ProductAdapterHarness.listing(BUCKET, "", Map.of(), false))));
    }

    private ProductAdapterHarness.Context context(boolean probing) {
        CredentialsAccess credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        UpstreamClient client = new UpstreamClient(budgets, new BackoffRegistry(jdbc, clock), usage, install, credentials,
                sources, jdbc, jsonMapper, properties, clock);
        client.setS3Endpoint(baseUrl());
        SourceDescriptor d = adapter.descriptor();
        return new ProductAdapterHarness.Context(d, client.forSource(GoesGlmAdapter.ID, d), clock, state,
                new FlashTestSink(store, probing));
    }

    /** A G19 LCFA key of the 21z hour starting at {@code hhmmss} (20 s long). */
    static String key(String hhmmss) {
        int h = Integer.parseInt(hhmmss.substring(0, 2));
        int m = Integer.parseInt(hhmmss.substring(2, 4));
        int s = Integer.parseInt(hhmmss.substring(4, 6)) + 20;
        String end = String.format("%02d%02d%02d", h + (m + s / 60) / 60, (m + s / 60) % 60, s % 60);
        return String.format("GLM-L2-LCFA/2026/285/%02d/OR_GLM-L2-LCFA_G19_s2026285%s0_e2026285%s0_c2026285%s1.nc", h,
                hhmmss, end, end);
    }

    /** Lists {@code keys}, each sized as the active granule. */
    private static void stubListing(String prefix, String startAfter, String... keys) {
        Map<String, String> served = new LinkedHashMap<>();
        for (String k : keys) {
            served.put(k, GlmParserTest.ACTIVE);
        }
        stubListing(prefix, startAfter, served);
    }

    /** Lists the keys of {@code served} (key to fixture file), sized as their fixtures. */
    private static void stubListing(String prefix, String startAfter, Map<String, String> served) {
        Map<String, Long> objects = new LinkedHashMap<>();
        served.forEach((k, fixture) -> objects.put(k, SIZES.get(fixture)));
        MappingBuilder mapping = get(urlPathEqualTo("/" + BUCKET + "/")).withQueryParam("prefix", equalTo(prefix))
                .withQueryParam("start-after", startAfter == null ? absent() : equalTo(startAfter));
        WIREMOCK.stubFor(mapping.willReturn(aResponse().withHeader("Content-Type", "application/xml")
                .withBody(ProductAdapterHarness.listing(BUCKET, prefix, objects, false))));
    }

    private static void stubGranule(String key) {
        stubGranule(key, "glm", GlmParserTest.ACTIVE);
    }

    private static void stubGranule(String key, String dir, String fixture) {
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + key))
                .willReturn(aResponse().withBody(AdapterFixtures.bytes(dir, fixture))));
    }

    private static MultiPolygon box(double minLon, double minLat, double maxLon, double maxLat) {
        Polygon p = Geo.factory().createPolygon(new Coordinate[] {new Coordinate(minLon, minLat),
                new Coordinate(maxLon, minLat), new Coordinate(maxLon, maxLat), new Coordinate(minLon, maxLat),
                new Coordinate(minLon, minLat)});
        return Geo.factory().createMultiPolygon(new Polygon[] {p});
    }

    @Test
    @DisplayName("new granules are processed in key order; other satellites and products are ignored")
    void listingOrder() throws Exception {
        String k1 = key("215700");
        String k2 = key("215720");
        Map<String, String> served = new LinkedHashMap<>();
        served.put(k1, GlmParserTest.QUIET);
        served.put(k2, GlmParserTest.ACTIVE);
        served.put(k2.replace("_G19_", "_G18_"), GlmParserTest.ACTIVE);
        served.put("GLM-L2-LCFA/2026/285/21/README.txt", GlmParserTest.ACTIVE);
        stubListing(HOUR_21, null, served);
        // The quiet granule's flashes (03:59) fall outside k1's 5-minute window, so k1 makes no cluster; the
        // active granule (21:57:19-39) leaves the quiet flashes behind the ring window and makes 37 clusters.
        stubGranule(k1, "glm", GlmParserTest.QUIET);
        stubGranule(k2);

        FetchResult result = adapter.fetch(context(false));

        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/" + k1)));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/" + k2)));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/" + BUCKET + "/")).withQueryParam("prefix", equalTo(HOUR_20)));
        assertThat(state.get(GoesGlmAdapter.LAST_KEY)).contains(k2);
        assertThat(state.get(GoesGlmAdapter.LAST_END)).contains("2026-10-12T21:57:40Z");
        assertThat(state.get(AdapterStateStore.NEWEST_INPUT_KEY)).contains("2026-10-12T21:57:40Z");
        assertThat(state.get(AdapterStateStore.MAX_INPUT_GAP_KEY)).contains("PT2M");
        assertThat(store.newestFlashFileTime()).contains(Instant.parse("2026-10-12T21:57:40Z"));
        assertThat(store.ringFlashes()).isEqualTo(420);
        assertThat(result.records()).isNotEmpty().allMatch(r -> r.kind().equals("lightning-cluster"))
                .noneMatch(NormalizedRecord::tombstone);
        assertThat(result.records()).hasSize(37);
        assertThat(state.get(GoesGlmAdapter.CLUSTERS).orElseThrow().split("\n")).hasSize(37);

        // The next run lists after the cursor and finds nothing new: unchanged, clusters frozen.
        stubListing(HOUR_21, k2);
        String clusters = state.get(GoesGlmAdapter.CLUSTERS).orElseThrow();
        clock.advance(Duration.ofSeconds(30));
        assertThat(adapter.fetch(context(false)).notModified()).isTrue();
        assertThat(state.get(GoesGlmAdapter.CLUSTERS)).contains(clusters);
    }

    @Test
    @DisplayName("after an outage the adapter catches up at most 60 minutes")
    void catchUpCap() throws Exception {
        clock.setInstant(Instant.parse("2026-10-12T23:30:00Z"));
        state.put(GoesGlmAdapter.LAST_KEY, key("210000"));
        String hour22 = "GLM-L2-LCFA/2026/285/22/";
        String old22 = key("222000");
        String recent22 = key("224000");
        stubListing(hour22, null, old22, recent22);
        stubGranule(recent22);

        adapter.fetch(context(false));

        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/" + old22)));
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/" + recent22)));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/")).withQueryParam("prefix", equalTo(HOUR_21)));
        assertThat(state.get(GoesGlmAdapter.LAST_KEY)).contains(recent22);
        assertThat(state.get(GoesGlmAdapter.LAST_END)).contains("2026-10-12T22:40:20Z");
    }

    @Test
    @DisplayName("at most four granules a run, and a run that runs out of budget keeps what it processed")
    void budget() throws Exception {
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(3, 10_000, 3_000));
        // A minute of its own, so the usage of the other tests does not count against the 3 requests.
        clock.setInstant(Instant.parse("2026-10-12T21:57:50Z"));
        String last = key("215600");
        state.put(GoesGlmAdapter.LAST_KEY, last);
        String a = key("215620");
        String b = key("215640");
        String c = key("215700");
        stubListing(HOUR_21, last, a, b, c);
        stubGranule(a);
        stubGranule(b);
        stubGranule(c);

        adapter.fetch(context(false));

        // One listing and two downloads use the 3 requests; the third download is refused, and the run keeps
        // the two granules it processed instead of failing.
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/" + c)));
        assertThat(state.get(GoesGlmAdapter.LAST_KEY)).contains(b);
        assertThat(state.get(GoesGlmAdapter.LAST_END)).contains("2026-10-12T21:57:00Z");
        assertThat(GoesGlmAdapter.MAX_FILES_PER_RUN).isEqualTo(4);
    }

    @Test
    @DisplayName("a granule without the flash variables degrades the source and is not processed")
    void formatChange() throws Exception {
        String k = key("215700");
        stubListing(HOUR_21, null, Map.of(k, GlmParserTest.FDC));
        stubGranule(k, "fdc", GlmParserTest.FDC);

        FetchResult result = adapter.fetch(context(false));

        assertThat(result.notModified()).isTrue();
        assertThat(state.get(AdapterStateStore.DEGRADED_REASON_KEY)).hasValueSatisfying(
                r -> assertThat(r).contains("variable flash_lat missing"));
        assertThat(state.get(GoesGlmAdapter.LAST_KEY)).contains(k);
        assertThat(state.get(GoesGlmAdapter.LAST_END)).isEmpty();
        assertThat(store.newestFlashFileTime()).isEmpty();
    }

    @Test
    @DisplayName("a failed download before anything was processed fails the run and moves nothing")
    void downloadFails() {
        String k = key("215700");
        stubListing(HOUR_21, null, k);
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + k)).willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> adapter.fetch(context(false))).isInstanceOf(AdapterException.class);
        assertThat(state.get(GoesGlmAdapter.LAST_KEY)).isEmpty();
        assertThat(state.get(GoesGlmAdapter.LAST_END)).isEmpty();
        assertThat(store.ringFlashes()).isZero();
    }

    @Test
    @DisplayName("a probe parses the newest granule and writes nothing")
    void probe() throws Exception {
        String k = key("215720");
        stubListing(HOUR_21, null, key("215700"), k);
        stubGranule(k);
        clock.setInstant(Instant.parse("2026-10-12T21:59:30Z"));

        FetchResult result = adapter.fetch(context(true));

        assertThat(result.records()).isEmpty();
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/" + k)));
        assertThat(store.ringFlashes()).isZero();
        assertThat(state.asMap()).isEmpty();
    }

    @Test
    @DisplayName("the descriptor matches the prompt and validates")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.kinds()).containsExactly("lightning-cluster");
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofSeconds(30));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(10, 10_000, 3_000));
        assertThat(d.kindRetention()).containsEntry("lightning-cluster", Duration.ofDays(3));
        assertThat(d.recordTtl()).isNull();
        assertThat(d.licence()).isEqualTo("US-PD");
        assertThat(d.zeroResultsNormal()).isTrue();
        assertThat(d.titleField()).isEqualTo("label");
    }
}
