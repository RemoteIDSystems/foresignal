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
import ai.foresignal.raster.decode.NetcdfDecoder;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FrameInput;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.RasterSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.raster.ProductAdapterHarness;
import ai.foresignal.testsupport.weather.WeatherAdapterHarness;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
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
 * {@code goes-fdc} against a stubbed S3 (prompt 048): newest scan only, the AOI and CONUS scopes, a
 * first sighting that stays put, miss counting only on processed scans (a failed download or an
 * unreadable file counts for nothing), the hour rollover and the descriptor.
 */
@IntegrationTest
class GoesFdcAdapterTest extends WireMockSupport {

    static final String BUCKET = GoesFdcAdapter.BUCKET;
    static final String HOUR_21 = "ABI-L2-FDCC/2026/285/21/";
    static final String HOUR_22 = "ABI-L2-FDCC/2026/285/22/";
    static final long SIZE = 524_009L;
    static final long GLM_SIZE = 432_989L;

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;
    @Autowired NetcdfDecoder decoder;

    MutableClock clock;
    SourceService sources;
    BudgetGovernor budgets;
    UsageMeter usage;
    GoesFdcAdapter adapter;
    WeatherAdapterHarness.TestAoi aoi;
    WeatherAdapterHarness.MapState state;

    /** A fetch context with parameters and a raster sink (only {@code probing()} is used). */
    record Context(SourceDescriptor descriptor, SourceHttp http, ValidatedParams params, AdapterStateStore state,
            Clock clock, boolean probe) implements FetchContext {

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

        @Override
        public RasterSink rasters() {
            return new RasterSink() {
                @Override
                public NormalizedRecord commit(FrameInput frame) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public List<NormalizedRecord> completeRun(String productId, Instant referenceTime) {
                    return List.of();
                }

                @Override
                public void reportFormatChange(String productId, String reason) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean probing() {
                    return probe;
                }
            };
        }
    }

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-10-12T21:59:30Z"));
        sources = mock(SourceService.class);
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(100, 10_000, 300));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        usage = new UsageMeter(jdbc, clock);
        budgets = new BudgetGovernor(sources, usage, clock);
        aoi = new WeatherAdapterHarness.TestAoi(-106.65, 25.84, -93.51, 36.50);
        adapter = new GoesFdcAdapter(decoder, aoi, baseUrl());
        state = new WeatherAdapterHarness.MapState();
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/")).atPriority(10)
                .willReturn(aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(ProductAdapterHarness.listing(BUCKET, "", Map.of(), false))));
    }

    private Context context(String scope, boolean probe) {
        CredentialsAccess credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        UpstreamClient client = new UpstreamClient(budgets, new BackoffRegistry(jdbc, clock), usage, install, credentials,
                sources, jdbc, jsonMapper, properties, clock);
        client.setS3Endpoint(baseUrl());
        SourceDescriptor d = adapter.descriptor();
        ValidatedParams params = scope == null ? ValidatedParams.empty()
                : ValidatedParams.of(Map.of(GoesFdcAdapter.SCOPE_PARAM, scope));
        return new Context(d, client.forSource(GoesFdcAdapter.ID, d), params, state, clock, probe);
    }

    /** A G19 FDCC key of {@code hour} with the {@code s} time {@code hhmmss}. */
    static String key(String hhmmss) {
        String hour = hhmmss.substring(0, 2);
        return "ABI-L2-FDCC/2026/285/" + hour + "/OR_ABI-L2-FDCC-M6_G19_s2026285" + hhmmss + "0_e2026285" + hhmmss
                + "9_c2026285" + hhmmss + "9.nc";
    }

    private static void stubListing(String prefix, String startAfter, Map<String, Long> objects) {
        MappingBuilder mapping = get(urlPathEqualTo("/" + BUCKET + "/")).withQueryParam("prefix", equalTo(prefix))
                .withQueryParam("start-after", startAfter == null ? absent() : equalTo(startAfter));
        WIREMOCK.stubFor(mapping.willReturn(aResponse().withHeader("Content-Type", "application/xml")
                .withBody(ProductAdapterHarness.listing(BUCKET, prefix, objects, false))));
    }

    private static void stubScan(String key) {
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + key))
                .willReturn(aResponse().withBody(AdapterFixtures.bytes("fdc", GlmParserTest.FDC))));
    }

    private static List<NormalizedRecord> ofKind(FetchResult result, String kind) {
        return result.records().stream().filter(r -> r.kind().equals(kind)).toList();
    }

    @Test
    @DisplayName("only the newest scan is processed; the Texas AOI keeps two detections and two clusters")
    void newestOnly() throws Exception {
        Map<String, Long> objects = new LinkedHashMap<>();
        objects.put(key("215117"), SIZE);
        objects.put(key("215617"), SIZE);
        stubListing(HOUR_21, null, objects);
        stubScan(key("215617"));

        FetchResult result = adapter.fetch(context(null, false));

        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/" + key("215117"))));
        assertThat(result.complete()).isTrue();
        List<NormalizedRecord> detections = ofKind(result, "fire-detection");
        assertThat(detections).extracting(NormalizedRecord::externalId).containsExactly("g19:608:594", "g19:927:692");
        assertThat(detections).allMatch(r -> r.observedAt().equals(Instant.parse("2026-10-12T21:56:17Z")))
                .allMatch(r -> r.eventTime().equals(Instant.parse("2026-10-12T21:56:17Z")));
        assertThat(ofKind(result, "fire-cluster")).hasSize(2).allMatch(r -> r.externalId().startsWith("fc-"));
        assertThat(state.get(GoesFdcAdapter.LAST_KEY)).contains(key("215617"));
        assertThat(state.get(GoesFdcAdapter.LAST_SCAN)).contains("2026-10-12T21:56:17Z");
        assertThat(state.get(AdapterStateStore.MAX_INPUT_GAP_KEY)).contains("PT15M");
        assertThat(state.get(AdapterStateStore.NEWEST_INPUT_KEY)).contains("2026-10-12T21:56:17.900Z");
        assertThat(aoi.consumers()).contains(GoesFdcAdapter.ID);
    }

    @Test
    @DisplayName("scope conus keeps all 25 detections and 22 clusters")
    void conusScope() throws Exception {
        stubListing(HOUR_21, null, Map.of(key("215617"), SIZE));
        stubScan(key("215617"));

        FetchResult result = adapter.fetch(context("conus", false));

        assertThat(ofKind(result, "fire-detection")).hasSize(25);
        assertThat(ofKind(result, "fire-cluster")).hasSize(22);
    }

    @Test
    @DisplayName("misses count only on processed scans: a failed download or an unreadable file counts for nothing")
    void missesOnlyOnProcessedScans() throws Exception {
        String first = key("215117");
        stubListing(HOUR_21, null, Map.of(first, SIZE));
        stubScan(first);
        adapter.fetch(context(null, false));
        String pixels = state.get(GoesFdcAdapter.PIXELS).orElseThrow();
        assertThat(pixels).contains("608:594|").contains("927:692|").endsWith("|0");

        // The next scan's download fails: nothing is processed.
        String failed = key("215617");
        stubListing(HOUR_21, first, Map.of(failed, SIZE));
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + failed)).willReturn(aResponse().withStatus(500)));
        assertThatThrownBy(() -> adapter.fetch(context(null, false))).isInstanceOf(AdapterException.class);
        assertThat(state.get(GoesFdcAdapter.PIXELS)).contains(pixels);
        assertThat(state.get(GoesFdcAdapter.LAST_KEY)).contains(first);

        // The scan after that is not an FDC file: not processed, the source degraded, misses unchanged.
        String unreadable = key("220117");
        clock.setInstant(Instant.parse("2026-10-12T22:02:30Z"));
        stubListing(HOUR_22, null, Map.of(unreadable, GLM_SIZE));
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + unreadable))
                .willReturn(aResponse().withBody(AdapterFixtures.bytes("glm", GlmParserTest.ACTIVE))));
        FetchResult notProcessed = adapter.fetch(context(null, false));
        assertThat(notProcessed.notModified()).isTrue();
        assertThat(state.get(GoesFdcAdapter.PIXELS)).contains(pixels);
        assertThat(state.get(AdapterStateStore.DEGRADED_REASON_KEY)).hasValueSatisfying(
                r -> assertThat(r).contains("variable Mask missing"));

        // Three processed scans without the pixels: tombstones on the third, not before.
        FdcParser.Result empty = new FdcParser.Result(List.of(), 2500, 1500, 0);
        Context ctx = context(null, false);
        Instant t = Instant.parse("2026-10-12T22:06:17Z");
        assertThat(adapter.process(ctx, empty, t)).noneMatch(NormalizedRecord::tombstone);
        assertThat(adapter.process(ctx, empty, t.plusSeconds(300))).noneMatch(NormalizedRecord::tombstone);
        List<NormalizedRecord> third = adapter.process(ctx, empty, t.plusSeconds(600));
        assertThat(third).allMatch(NormalizedRecord::tombstone);
        assertThat(third).extracting(NormalizedRecord::externalId).contains("g19:608:594", "g19:927:692");
        assertThat(third.stream().filter(r -> r.kind().equals("fire-cluster")).count()).isEqualTo(2);
        assertThat(third).allMatch(r -> r.observedAt().equals(t.plusSeconds(600)));
        assertThat(state.get(GoesFdcAdapter.PIXELS)).hasValue("");
    }

    @Test
    @DisplayName("a pixel seen again keeps its first sighting as eventTime; a returning pixel resets its misses")
    void firstSeenIsStable() throws Exception {
        String first = key("215117");
        stubListing(HOUR_21, null, Map.of(first, SIZE));
        stubScan(first);
        adapter.fetch(context(null, false));

        String second = key("215617");
        stubListing(HOUR_21, first, Map.of(second, SIZE));
        stubScan(second);
        FetchResult result = adapter.fetch(context(null, false));

        NormalizedRecord pixel = ofKind(result, "fire-detection").get(0);
        assertThat(pixel.observedAt()).isEqualTo(Instant.parse("2026-10-12T21:56:17Z"));
        assertThat(pixel.eventTime()).isEqualTo(Instant.parse("2026-10-12T21:51:17Z"));
        NormalizedRecord cluster = ofKind(result, "fire-cluster").get(0);
        assertThat(cluster.payload()).containsEntry("firstSeenAt", "2026-10-12T21:51:17Z");
        assertThat(cluster.eventTime()).isEqualTo(Instant.parse("2026-10-12T21:51:17Z"));
    }

    @Test
    @DisplayName("a scan not newer than the last processed one is ignored")
    void olderScanIgnored() throws Exception {
        state.put(GoesFdcAdapter.LAST_SCAN, "2026-10-12T21:56:17Z");
        stubListing(HOUR_21, null, Map.of(key("215617"), SIZE));

        assertThat(adapter.fetch(context(null, false)).notModified()).isTrue();
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/" + key("215617"))));
    }

    @Test
    @DisplayName("after the hour turns, the previous hour is listed after the cursor until a new-hour key is processed")
    void hourRollover() throws Exception {
        clock.setInstant(Instant.parse("2026-10-12T22:00:30Z"));
        String last = key("215117");
        state.put(GoesFdcAdapter.LAST_KEY, last);
        stubListing(HOUR_22, null, Map.of());
        stubListing(HOUR_21, last, Map.of(key("215617"), SIZE));
        stubScan(key("215617"));

        FetchResult result = adapter.fetch(context(null, false));

        assertThat(ofKind(result, "fire-detection")).hasSize(2);
        assertThat(state.get(GoesFdcAdapter.LAST_KEY)).contains(key("215617"));
        assertThat(adapter.fetch(context(null, false)).notModified()).isTrue();
    }

    @Test
    @DisplayName("a probe previews the newest scan's detections and writes no state")
    void probe() throws Exception {
        stubListing(HOUR_21, null, Map.of(key("215617"), SIZE));
        stubScan(key("215617"));

        FetchResult result = adapter.fetch(context(null, true));

        assertThat(result.records()).hasSize(2);
        assertThat(state.asMap()).isEmpty();
    }

    @Test
    @DisplayName("pixel bookkeeping round-trips through the adapter state")
    void pixelState() {
        Map<String, GoesFdcAdapter.PixelState> pixels = new LinkedHashMap<>();
        pixels.put("1:2", new GoesFdcAdapter.PixelState(1_000L, 0));
        pixels.put("3:4", new GoesFdcAdapter.PixelState(2_000L, 2));
        String text = GoesFdcAdapter.writePixels(pixels);
        assertThat(GoesFdcAdapter.readPixels(text + "\nbad")).isEqualTo(pixels);
        assertThat(GoesFdcAdapter.readPixels(null)).isEmpty();
    }

    @Test
    @DisplayName("the descriptor matches the prompt and validates")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        // SourceDescriptor.kinds is a Set (Set.copyOf), so its iteration order is unspecified.
        assertThat(d.kinds()).containsExactlyInAnyOrder("fire-detection", "fire-cluster");
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(5));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(4, 800, 300));
        assertThat(d.kindRetention()).containsEntry("fire-detection", Duration.ofDays(7))
                .containsEntry("fire-cluster", Duration.ofDays(7));
        assertThat(d.recordTtl()).isNull();
        assertThat(d.licence()).isEqualTo("US-PD");
        assertThat(d.titleField()).isEqualTo("label");
        assertThat(d.params()).anySatisfy(p -> assertThat(p.name()).isEqualTo("scope"));
    }
}
