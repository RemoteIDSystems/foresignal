package ai.foresignal.source.adapters.hrrr;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.LccGrid;
import ai.foresignal.raster.RasterPaths;
import ai.foresignal.raster.decode.DecodeRequest;
import ai.foresignal.raster.decode.DecodedGrid;
import ai.foresignal.raster.decode.Grib2Decoder;
import ai.foresignal.raster.model.ModelRunIngest;
import ai.foresignal.raster.model.WindRotation;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import ai.foresignal.testsupport.raster.ProductAdapterHarness;
import ai.foresignal.testsupport.raster.ProductAdapterHarness.Committed;
import ai.foresignal.testsupport.raster.ProductAdapterHarness.RecordingSink;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code hrrr} source against a stubbed {@code noaa-hrrr-bdp-pds} (prompt 051): the f01 {@code .idx}
 * of the 2026-10-12 12z run, ranges answered 206 from captured messages, MASSDEN+UGRD+VGRD coalesced
 * into one range, the earth-relative rotation, the smoke unit conversion, health on a missing field,
 * disabled products and the profile's run cadence.
 *
 * <p><b>Capture procedure</b> (anonymous HTTPS; offsets from {@code hrrr.t12z.wrfsfcf01.grib2.idx}):
 * <pre>
 * H=https://noaa-hrrr-bdp-pds.s3.amazonaws.com/hrrr.20261012/conus/hrrr.t12z.wrfsfcf01.grib2
 * curl -o hrrr.t12z.wrfsfcf01.grib2.idx $H.idx
 * curl -r 0-424136          -o hrrr-f01-refc.grib2      $H   # REFC (msg 1), committed
 * curl -r 3314894-4566989   -o hrrr-f01-gust.grib2      $H   # GUST:surface (msg 9)
 * curl -r 51042719-52027079 -o hrrr-f01-massden.grib2   $H   # MASSDEN:8 m above ground (msg 76)
 * curl -r 52027080-56790309 -o hrrr-f01-ugrd-vgrd.grib2 $H   # UGRD + VGRD 10 m (msgs 77-78)
 * </pre>
 * The last three are 1–4.8 MB, over the commit limit, so {@code ./gradlew downloadTestFixtures}
 * fetches them (sha256-pinned) into {@code build/test-fixtures/hrrr/}; the tests that need them skip
 * with a logged reason when they are absent.
 */
@IntegrationTest
class HrrrAdapterWireMockTest extends WireMockSupport {

    static final String BUCKET = HrrrAdapter.BUCKET;
    static final Instant RUN = Instant.parse("2026-10-12T12:00:00Z");
    static final Instant NOW = Instant.parse("2026-10-12T13:00:00Z");
    static final String KEY = "hrrr.20261012/conus/hrrr.t12z.wrfsfcf01.grib2";
    static final String IDX = new String(DecoderFixtures.hrrrBytes("hrrr.t12z.wrfsfcf01.grib2.idx"),
            StandardCharsets.US_ASCII);
    static final byte[] REFC = DecoderFixtures.hrrrBytes(DecoderFixtures.HRRR_REFC);

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;
    @Autowired Grib2Decoder decoder;
    @Autowired RasterPaths paths;

    MutableClock clock;
    SourceService sources;
    BudgetGovernor budgets;
    UsageMeter usage;
    BackoffRegistry backoff;
    ModelRunIngest ingest;
    HrrrAdapter adapter;
    ProductAdapterHarness.MapState state;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        sources = mock(SourceService.class);
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(60, 4_000, 2_000));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        usage = new UsageMeter(jdbc, clock);
        backoff = new BackoffRegistry(jdbc, clock);
        budgets = new BudgetGovernor(sources, usage, clock);
        ingest = new ModelRunIngest(decoder, paths);
        adapter = new HrrrAdapter(ingest, "small", baseUrl());
        state = new ProductAdapterHarness.MapState();
        // S3 answers 404 for an object that is not published yet (WireMock fails on unmatched requests).
        WIREMOCK.stubFor(get(urlPathMatching("/" + BUCKET + "/.*\\.idx")).atPriority(10)
                .willReturn(aResponse().withStatus(404).withHeader("Content-Type", "application/xml")
                        .withBody("<Error><Code>NoSuchKey</Code></Error>")));
    }

    private ProductAdapterHarness.ParamContext context(RecordingSink sink) {
        CredentialsAccess credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        UpstreamClient client = new UpstreamClient(budgets, backoff, usage, install, credentials, sources, jdbc,
                jsonMapper, properties, clock);
        client.setS3Endpoint(baseUrl());
        SourceDescriptor descriptor = adapter.descriptor();
        return new ProductAdapterHarness.ParamContext(descriptor, client.forSource(HrrrAdapter.ID, descriptor), clock,
                state, sink, ValidatedParams.of(Map.of(ModelRunIngest.STEPS, List.of("1"))));
    }

    private static void stubIdx(String idx) {
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + KEY + ".idx"))
                .willReturn(aResponse().withHeader("Content-Type", "text/plain").withBody(idx)));
    }

    private static void stubRange(long start, long end, byte[] body) {
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + KEY))
                .withHeader("Range", equalTo("bytes=" + start + "-" + end))
                .willReturn(aResponse().withStatus(206).withBody(body)));
    }

    private static int rangeRequests() {
        return WIREMOCK.findAll(getRequestedFor(urlPathEqualTo("/" + BUCKET + "/" + KEY))).size();
    }

    @Test
    @DisplayName("one step: three ranges (MASSDEN+UGRD+VGRD coalesced), rotated winds, smoke in µg/m³, runs complete")
    void fullStep() throws Exception {
        byte[] uv = DecoderFixtures.hrrrDownloadedOrSkip(DecoderFixtures.HRRR_UV);
        byte[] gust = DecoderFixtures.hrrrDownloadedOrSkip(DecoderFixtures.HRRR_GUST);
        byte[] massden = DecoderFixtures.hrrrDownloadedOrSkip(DecoderFixtures.HRRR_MASSDEN);
        byte[] coalesced = new byte[massden.length + uv.length];
        System.arraycopy(massden, 0, coalesced, 0, massden.length);
        System.arraycopy(uv, 0, coalesced, massden.length, uv.length);
        stubIdx(IDX);
        stubRange(0, 424_136, REFC);
        stubRange(3_314_894, 4_566_989, gust);
        stubRange(51_042_719, 56_790_309, coalesced);
        RecordingSink sink = new RecordingSink().decoding();
        adapter.fetch(context(sink));

        assertThat(rangeRequests()).isEqualTo(3);
        assertThat(sink.committed()).extracting(c -> c.frame().productId())
                .containsExactly(HrrrAdapter.WIND, HrrrAdapter.REFC, HrrrAdapter.GUST, HrrrAdapter.SMOKE);
        Committed wind = sink.committed(HrrrAdapter.WIND).get(0);
        GridDefinition grid = wind.frame().grid();
        assertThat(grid.type()).isEqualTo(GridDefinition.LCC);
        assertThat(grid.nx() * grid.ny()).isEqualTo(1_905_141);
        assertThat(wind.frame().upstreamKey()).isEqualTo(KEY + "#77,78");

        // The stored winds are the decoded grid-relative winds rotated by α = n·(λ − λ₀).
        Path raw = Files.createTempFile(paths.tmp(), "hrrr-uv-check-", ".grib2");
        try {
            Files.write(raw, uv);
            DecodedGrid u = decoder.decode(raw, DecodeRequest.grib2("hrrr", "UGRD", 0, 2, 2, 103, 10.0, "10 m"));
            DecodedGrid v = decoder.decode(raw, DecodeRequest.grib2("hrrr", "VGRD", 0, 2, 3, 103, 10.0, "10 m"));
            assertThat(u.metadata()).containsEntry("gridRelativeWinds", true);
            LccGrid lcc = (LccGrid) GridDef.of(grid);
            DecodedBands bands = wind.bands();
            int sw = (grid.ny() - 1) * grid.nx();
            double alpha = WindRotation.angle(lcc.n(), lcc.lov(), lcc.indexToLonLat(0, grid.ny() - 1)[0]);
            double[] expected = WindRotation.rotate(u.floatData()[sw], v.floatData()[sw], alpha);
            assertThat(bands.physical().get(0)[sw]).isCloseTo((float) expected[0], within(1e-4f));
            assertThat(bands.physical().get(1)[sw]).isCloseTo((float) expected[1], within(1e-4f));
            assertThat(Math.toDegrees(alpha)).isCloseTo(-15.6995, within(1e-3));
            int center = (grid.ny() / 2) * grid.nx() + grid.nx() / 2;
            assertThat(Math.hypot(bands.physical().get(0)[center], bands.physical().get(1)[center]))
                    .isCloseTo(Math.hypot(u.floatData()[center], v.floatData()[center]), within(1e-4));
        } finally {
            Files.deleteIfExists(raw);
        }
        float max = 0;
        for (float f : sink.committed(HrrrAdapter.SMOKE).get(0).bands().physical().get(0)) {
            max = Math.max(max, f);
        }
        assertThat(max).as("9.09e-8 kg/m³ is 90.9 µg/m³").isCloseTo(90.9f, within(0.1f));
        assertThat(sink.completedRuns()).containsExactly(HrrrAdapter.WIND + "@" + RUN, HrrrAdapter.REFC + "@" + RUN,
                HrrrAdapter.GUST + "@" + RUN, HrrrAdapter.SMOKE + "@" + RUN);
    }

    @Test
    @DisplayName("disabled products are skipped: only REFC is read, decoded and completed")
    void disabledProducts() throws Exception {
        stubIdx(IDX);
        stubRange(0, 424_136, REFC);
        RecordingSink sink = new RecordingSink().decoding().disable(HrrrAdapter.WIND, HrrrAdapter.GUST, HrrrAdapter.SMOKE);
        adapter.fetch(context(sink));
        assertThat(rangeRequests()).isEqualTo(1);
        assertThat(sink.committed()).extracting(c -> c.frame().productId()).containsExactly(HrrrAdapter.REFC);
        Committed refc = sink.committed().get(0);
        assertThat(refc.frame().validTime()).isEqualTo(Instant.parse("2026-10-12T13:00:00Z"));
        assertThat(refc.frame().upstreamKey()).isEqualTo(KEY + "#1");
        assertThat(refc.bands().physical().get(0)).hasSize(1_905_141);
        assertThat(sink.completedRuns()).containsExactly(HrrrAdapter.REFC + "@" + RUN);
        assertThat(state.asMap()).containsEntry("complete:hrrr-refc", "2026101212Z")
                .doesNotContainKey("complete:hrrr");
    }

    @Test
    @DisplayName("a missing wind field degrades health and writes no frame for any product")
    void missingWind() throws Exception {
        stubIdx(IDX.replace(":VGRD:10 m above ground:", ":VGRD:12 m above ground:"));
        RecordingSink sink = new RecordingSink();
        adapter.fetch(context(sink));
        assertThat(sink.formatChanges()).containsEntry(HrrrAdapter.WIND,
                "possible format change: HRRR idx missing VGRD:10 m above ground");
        assertThat(sink.committed()).isEmpty();
        assertThat(rangeRequests()).isZero();
    }

    @Test
    @DisplayName("the small profile defaults to 6-hourly runs, the large to 3-hourly; the engine reports the cadence")
    void profileCadence() {
        assertThat(adapter.runEvery()).isEqualTo(Duration.ofHours(6));
        assertThat(adapter.descriptor().params()).anySatisfy(p -> {
            assertThat(p.name()).isEqualTo("runEvery");
            assertThat(p.defaultValue()).isEqualTo("6h");
        });
        assertThat(ingest.runCadence(HrrrAdapter.ID)).contains(Duration.ofHours(6));
        ModelRunIngest other = new ModelRunIngest(decoder, paths);
        HrrrAdapter large = new HrrrAdapter(other, "large", "");
        assertThat(large.runEvery()).isEqualTo(Duration.ofHours(3));
        assertThat(large.model().defaultRunEvery()).isEqualTo(Duration.ofHours(3));
        assertThat(other.runCadence(HrrrAdapter.ID)).contains(Duration.ofHours(3));
        assertThat(large.objectUri("k").toString()).isEqualTo("https://noaa-hrrr-bdp-pds.s3.amazonaws.com/k");
    }

    @Test
    @DisplayName("descriptor: four CONUS forecast products on the native grid, budgets and the 10-minute check")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(60, 4_000, 2_000));
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(10));
        assertThat(d.products()).extracting(p -> p.id()).containsExactly("hrrr", "hrrr-refc", "hrrr-gust", "hrrr-smoke");
        assertThat(d.products()).allSatisfy(p -> {
            assertThat(p.isForecast()).isTrue();
            assertThat(p.spatial().nativeMaxZoom()).isEqualTo(8);
        });
        assertThat(d.products().get(1).defaultStyle()).isEqualTo("nws-reflectivity@1");
        assertThat(d.products().get(3).units()).isEqualTo("ug/m3");
        assertThat(adapter.decoderRequirements()).hasSize(4);
    }
}
