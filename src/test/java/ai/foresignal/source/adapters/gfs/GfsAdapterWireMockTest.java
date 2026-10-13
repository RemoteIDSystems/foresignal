package ai.foresignal.source.adapters.gfs;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
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
import ai.foresignal.raster.RasterPaths;
import ai.foresignal.raster.decode.Grib2Decoder;
import ai.foresignal.raster.model.ModelRunIngest;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.FetchResult;
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
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code gfs} source against a stubbed {@code noaa-gfs-bdp-pds} (prompt 051): the {@code .idx},
 * coalesced byte ranges answered 206 from captured messages, field matching by variable and level,
 * run completion, health on a missing field and on a missing {@code .idx}, and the probe.
 *
 * <p>The fixtures come from the 1° file of the 2026-10-12 12z run, reached by overriding the
 * {@code pathTemplate} param (the GFSv17 escape hatch) so the messages stay small; the 0.25° file has
 * the same fields and the same packing. <b>Capture procedure</b> (anonymous HTTPS, offsets from the
 * step's {@code .idx}: a message ends one byte before the next one starts):
 * <pre>
 * B=https://noaa-gfs-bdp-pds.s3.amazonaws.com/gfs.20261012/12/atmos/gfs.t12z.pgrb2.1p00.f006
 * curl -o gfs.t12z.pgrb2.1p00.f006.idx $B.idx
 * curl -r 586148-641118     -o gfs-1p00-f006-gust.grib2      $B   # GUST:surface (msg 14)
 * curl -r 35230937-35279061 -o gfs-1p00-f006-tmp2m.grib2     $B   # TMP:2 m above ground (msg 581)
 * curl -r 35666549-35825559 -o gfs-1p00-f006-ugrd-vgrd.grib2 $B   # UGRD + VGRD 10 m (msgs 588-589)
 * curl -r 36127136-36163721 -o gfs-1p00-f006-apcp.grib2      $B   # APCP:surface:0-6 hour acc (msg 596)
 * </pre>
 * The real {@link UpstreamClient} (budget, backoff, usage) is pointed at WireMock path-style; the
 * pipeline is a recording sink that decodes what it is given.
 */
@IntegrationTest
class GfsAdapterWireMockTest extends WireMockSupport {

    static final String BUCKET = GfsAdapter.BUCKET;
    static final String TEMPLATE = "gfs.{yyyyMMdd}/{HH}/atmos/gfs.t{HH}z.pgrb2.1p00.f{FFF}";
    static final Instant RUN = Instant.parse("2026-10-12T12:00:00Z");
    static final Instant NOW = Instant.parse("2026-10-12T16:00:00Z");
    static final String KEY6 = "gfs.20261012/12/atmos/gfs.t12z.pgrb2.1p00.f006";
    static final String KEY3 = "gfs.20261012/12/atmos/gfs.t12z.pgrb2.1p00.f003";

    static final String IDX = new String(DecoderFixtures.gfsBytes("gfs.t12z.pgrb2.1p00.f006.idx"),
            StandardCharsets.US_ASCII);
    static final byte[] UV = DecoderFixtures.gfsBytes("gfs-1p00-f006-ugrd-vgrd.grib2");
    static final byte[] GUST = DecoderFixtures.gfsBytes("gfs-1p00-f006-gust.grib2");
    static final byte[] TMP = DecoderFixtures.gfsBytes("gfs-1p00-f006-tmp2m.grib2");
    static final byte[] APCP = DecoderFixtures.gfsBytes("gfs-1p00-f006-apcp.grib2");

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;
    @Autowired Grib2Decoder decoder;
    @Autowired RasterPaths paths;

    MutableClock clock;
    SourceService sources;
    UsageMeter usage;
    BackoffRegistry backoff;
    BudgetGovernor budgets;
    GfsAdapter adapter;
    ProductAdapterHarness.MapState state;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        sources = mock(SourceService.class);
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(60, 3_000, 1_000));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        usage = new UsageMeter(jdbc, clock);
        backoff = new BackoffRegistry(jdbc, clock);
        budgets = new BudgetGovernor(sources, usage, clock);
        adapter = new GfsAdapter(new ModelRunIngest(decoder, paths), baseUrl());
        state = new ProductAdapterHarness.MapState();
        // S3 answers 404 for an object that is not published yet (WireMock fails on unmatched requests).
        WIREMOCK.stubFor(get(urlPathMatching("/" + BUCKET + "/.*\\.idx")).atPriority(10)
                .willReturn(aResponse().withStatus(404).withHeader("Content-Type", "application/xml")
                        .withBody("<Error><Code>NoSuchKey</Code></Error>")));
    }

    private ProductAdapterHarness.ParamContext context(RecordingSink sink, List<String> steps) {
        CredentialsAccess credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        UpstreamClient client = new UpstreamClient(budgets, backoff, usage, install, credentials, sources, jdbc,
                jsonMapper, properties, clock);
        client.setS3Endpoint(baseUrl());
        SourceDescriptor descriptor = adapter.descriptor();
        ValidatedParams params = ValidatedParams.of(Map.of(ModelRunIngest.PATH_TEMPLATE, TEMPLATE,
                ModelRunIngest.STEPS, steps));
        return new ProductAdapterHarness.ParamContext(descriptor, client.forSource(GfsAdapter.ID, descriptor), clock,
                state, sink, params);
    }

    /** Stubs one step: its {@code .idx} and the four planned ranges. */
    private static void stubStep(String key, String idx) {
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + key + ".idx"))
                .willReturn(aResponse().withHeader("Content-Type", "text/plain").withBody(idx)));
        stubRange(key, 586_148, 641_118, GUST);
        stubRange(key, 35_230_937, 35_279_061, TMP);
        stubRange(key, 35_666_549, 35_825_559, UV);
        stubRange(key, 36_127_136, 36_163_721, APCP);
    }

    private static void stubRange(String key, long start, long end, byte[] body) {
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + key))
                .withHeader("Range", equalTo("bytes=" + start + "-" + end))
                .willReturn(aResponse().withStatus(206)
                        .withHeader("Content-Range", "bytes " + start + "-" + end + "/45489175").withBody(body)));
    }

    private static int rangeRequests() {
        return WIREMOCK.findAll(getRequestedFor(urlPathMatching("/" + BUCKET + "/.*\\.f00[36]"))).size();
    }

    @Test
    @DisplayName("one step: the .idx, four coalesced ranges, a decoded frame per product, and every run completes")
    void oneStepRun() throws Exception {
        stubStep(KEY6, IDX);
        RecordingSink sink = new RecordingSink().decoding();
        FetchResult result = adapter.fetch(context(sink, List.of("6")));

        assertThat(rangeRequests()).as("UGRD+VGRD coalesced, GUST, TMP, APCP").isEqualTo(4);
        assertThat(sink.committed()).extracting(c -> c.frame().productId())
                .containsExactly(GfsAdapter.WIND, GfsAdapter.GUST, GfsAdapter.T2M, GfsAdapter.APCP);
        Committed wind = sink.committed(GfsAdapter.WIND).get(0);
        assertThat(wind.frame().referenceTime()).isEqualTo(RUN);
        assertThat(wind.frame().forecastStep()).isEqualTo(6);
        assertThat(wind.frame().validTime()).isEqualTo(Instant.parse("2026-10-12T18:00:00Z"));
        assertThat(wind.frame().upstreamKey()).isEqualTo(KEY6 + "#588,589");
        GridDefinition grid = wind.frame().grid();
        assertThat(grid.type()).isEqualTo(GridDefinition.LATLON);
        assertThat(grid.nx()).isEqualTo(360);
        assertThat(grid.ny()).isEqualTo(181);
        DecodedBands bands = wind.bands();
        assertThat(bands.bandCount()).isEqualTo(2);
        // Ranges read with NetCDF-Java from the captured messages.
        assertThat(range(bands.physical().get(0))[0]).isCloseTo(-20.4525f, within(1e-3f));
        assertThat(range(bands.physical().get(0))[1]).isCloseTo(24.6875f, within(1e-3f));
        assertThat(range(bands.physical().get(1))[0]).isCloseTo(-26.5272f, within(1e-3f));
        assertThat(range(sink.committed(GfsAdapter.T2M).get(0).bands().physical().get(0))[0])
                .isCloseTo(213.1902f, within(1e-3f));
        assertThat(range(sink.committed(GfsAdapter.APCP).get(0).bands().physical().get(0))[1])
                .isCloseTo(85.875f, within(1e-3f));
        assertThat(sink.completedRuns()).containsExactly(GfsAdapter.WIND + "@" + RUN, GfsAdapter.GUST + "@" + RUN,
                GfsAdapter.T2M + "@" + RUN, GfsAdapter.APCP + "@" + RUN);
        assertThat(result.records()).hasSize(4 + 4);
        assertThat(state.asMap()).containsEntry("done:2026101212Z", "6").containsEntry("complete:gfs", "2026101212Z")
                .containsEntry("steps:gfs-apcp:2026101212Z", "6");
        assertThat(sink.formatChanges()).isEmpty();

        WIREMOCK.resetRequests();
        adapter.fetch(context(sink, List.of("6")));
        assertThat(WIREMOCK.findAll(anyRequestedFor(anyUrl()))).as("a complete run is never fetched again").isEmpty();
        assertThat(paths.tmp().toFile().list((d, name) -> name.startsWith("gfs-2026101212Z")))
                .as("every staged message is deleted").isEmpty();
    }

    @Test
    @DisplayName("two steps over two fetches: the run completes only when its last step has a frame")
    void runAcrossSteps() throws Exception {
        stubStep(KEY3, IDX.replace("0-6 hour acc", "0-3 hour acc").replace(":6 hour fcst", ":3 hour fcst"));
        RecordingSink sink = new RecordingSink();
        adapter.fetch(context(sink, List.of("3", "6")));
        assertThat(sink.committed()).hasSize(4);
        assertThat(sink.completedRuns()).as("f006 is not published yet").isEmpty();
        assertThat(state.asMap()).containsEntry("done:2026101212Z", "3").doesNotContainKey("complete:gfs");

        stubStep(KEY6, IDX);
        adapter.fetch(context(sink, List.of("3", "6")));
        assertThat(sink.committed()).hasSize(8);
        assertThat(sink.committed(GfsAdapter.APCP)).extracting(c -> c.frame().forecastStep()).containsExactly(3, 6);
        assertThat(sink.completedRuns()).hasSize(4);
        assertThat(state.asMap()).containsEntry("done:2026101212Z", "3,6");
    }

    @Test
    @DisplayName("an optional field missing from the .idx drops only its product and degrades health")
    void optionalFieldMissing() throws Exception {
        stubStep(KEY6, IDX.replace(":GUST:surface:", ":GUSTX:surface:"));
        RecordingSink sink = new RecordingSink();
        adapter.fetch(context(sink, List.of("6")));
        assertThat(sink.formatChanges()).containsEntry(GfsAdapter.GUST,
                "possible format change: GFS idx missing GUST:surface");
        assertThat(sink.committed(GfsAdapter.GUST)).as("no empty frame").isEmpty();
        assertThat(sink.committed()).hasSize(3);
        assertThat(sink.completedRuns()).doesNotContain(GfsAdapter.GUST + "@" + RUN).hasSize(3);
        assertThat(rangeRequests()).isEqualTo(3);
    }

    @Test
    @DisplayName("a required wind field missing writes no frame at all and reads no range")
    void requiredFieldMissing() throws Exception {
        stubStep(KEY6, IDX.replace(":UGRD:10 m above ground:", ":UGRD:11 m above ground:"));
        RecordingSink sink = new RecordingSink();
        FetchResult result = adapter.fetch(context(sink, List.of("6")));
        assertThat(sink.formatChanges()).containsEntry(GfsAdapter.WIND,
                "possible format change: GFS idx missing UGRD:10 m above ground");
        assertThat(sink.committed()).isEmpty();
        assertThat(result.records()).isEmpty();
        assertThat(rangeRequests()).isZero();
        assertThat(state.asMap()).containsEntry("done:2026101212Z", "6");
    }

    @Test
    @DisplayName("a missing .idx is 'not published yet'; 2 h past the lag it degrades the source")
    void missingIdx() throws Exception {
        RecordingSink sink = new RecordingSink();
        adapter.fetch(context(sink, List.of("6")));
        assertThat(state.get(AdapterStateStore.DEGRADED_REASON_KEY)).isEmpty();
        clock.setInstant(RUN.plus(GfsAdapter.AVAILABILITY_LAG).plus(Duration.ofHours(2)).plusSeconds(60));
        adapter.fetch(context(sink, List.of("6")));
        assertThat(state.get(AdapterStateStore.DEGRADED_REASON_KEY)).hasValueSatisfying(reason -> assertThat(reason)
                .startsWith("GFS: no .idx for run 2026101212Z f006").contains(KEY6 + ".idx"));
        stubStep(KEY6, IDX);
        adapter.fetch(context(sink, List.of("6")));
        assertThat(state.get(AdapterStateStore.DEGRADED_REASON_KEY)).as("cleared once the .idx appears").isEmpty();
    }

    @Test
    @DisplayName("an HTML body where the .idx should be is an error, never an empty inventory")
    void htmlIdx() {
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + KEY6 + ".idx"))
                .willReturn(aResponse().withHeader("Content-Type", "text/html").withBody("<html>maintenance</html>")));
        RecordingSink sink = new RecordingSink();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> adapter.fetch(context(sink, List.of("6"))))
                .hasMessageContaining("malformed GRIB2 index");
        assertThat(sink.committed()).isEmpty();
    }

    @Test
    @DisplayName("the probe reads the newest .idx and one message, returns the grid summary and writes nothing")
    void probe() throws Exception {
        stubStep(KEY6, IDX);
        stubRange(KEY6, 35_666_549, 35_745_833, Arrays.copyOf(UV, 79_285));
        RecordingSink sink = new RecordingSink().asProbe();
        FetchResult result = adapter.fetch(context(sink, List.of("6")));
        assertThat(result.records()).hasSize(1);
        assertThat(sink.committed().get(0).frame().grid().nx()).isEqualTo(360);
        assertThat(rangeRequests()).isEqualTo(1);
        assertThat(state.asMap()).as("a probe writes no adapter state").isEmpty();
    }

    @Test
    @DisplayName("descriptor: POLLING product source, budgets, params, four forecast products")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(d.id()).isEqualTo("gfs");
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(60, 3_000, 1_000));
        assertThat(d.licence()).isEqualTo("US-PD");
        assertThat(d.zeroResultsNormal()).isTrue();
        assertThat(d.kindRetention()).containsEntry("raster-frame", Duration.ofDays(7));
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(15));
        assertThat(d.params()).extracting(p -> p.name()).containsExactly("pathTemplate", "steps", "runEvery",
                "availabilityLag");
        assertThat(d.products()).extracting(p -> p.id()).containsExactly("gfs", "gfs-gust", "gfs-t2m", "gfs-apcp");
        assertThat(d.products()).allSatisfy(p -> assertThat(p.isForecast()).isTrue());
        assertThat(d.products().get(0).kind()).isEqualTo("field");
        assertThat(d.products().get(0).bands()).containsExactly("u", "v");
        assertThat(ai.foresignal.raster.model.ForecastSteps.parse(GfsAdapter.DEFAULT_STEPS)).hasSize(29);
        assertThat(adapter.decoderRequirements()).containsOnlyKeys("gfs", "gfs-gust", "gfs-t2m", "gfs-apcp");
        assertThat(adapter.objectUri("k").toString()).isEqualTo(baseUrl() + "/" + BUCKET + "/k");
        assertThat(new GfsAdapter(new ModelRunIngest(decoder, paths), "").objectUri("k").toString())
                .isEqualTo("https://noaa-gfs-bdp-pds.s3.amazonaws.com/k");
    }

    private static float[] range(float[] values) {
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        for (float v : values) {
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        return new float[] {min, max};
    }
}
