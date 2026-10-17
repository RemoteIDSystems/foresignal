package ai.foresignal.source.adapters.ecmwf;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.raster.RasterPaths;
import ai.foresignal.raster.decode.DecodeRequest;
import ai.foresignal.raster.decode.Grib2Decoder;
import ai.foresignal.raster.model.EcmwfDefinition;
import ai.foresignal.raster.model.EcmwfRunIngest;
import ai.foresignal.raster.model.ModelCycleSelector;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import ai.foresignal.testsupport.raster.ProductAdapterHarness;
import ai.foresignal.testsupport.raster.ProductAdapterHarness.RecordingSink;
import java.net.URI;
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
 * The ECMWF engine against a stubbed {@code ecmwf-forecasts} mirror (prompt 105 post-review): per-step
 * {@code .index} → planned ranges → commits → run complete; a step is marked done only when every product
 * that has it committed it, and is retried otherwise (a missing reference message, a missing field), at
 * most {@value EcmwfRunIngest#MAX_STEP_ATTEMPTS} times; a 503 fails the fetch without marking anything.
 *
 * <p><b>Fixture:</b> every message is the 046 self-test file {@code ecmwf-ifs-0p25-lsm-0h.grib2} (the IFS
 * 0.25° land-sea mask, CCSDS-packed, 187,781 bytes; see {@code raster/selftest/README.md}), served as the
 * only field of each step's object. The test model reads it as {@code lsm} so the real decoder runs on real
 * IFS bytes; the difference product ({@code lsm} at a step minus {@code lsm} three hours earlier) is
 * therefore exactly zero, which also proves the clamp leaves no negative values.
 */
@IntegrationTest
class EcmwfAdapterTest extends WireMockSupport {

    static final String BUCKET = EcmwfAdapter.BUCKET;
    static final Instant RUN = Instant.parse("2026-10-15T00:00:00Z");
    static final Instant NOW = Instant.parse("2026-10-15T08:00:00Z");
    static final String CYCLE = ModelCycleSelector.cycleKey(RUN);
    static final String FIXTURE = "raster/selftest/ecmwf-ifs-0p25-lsm-0h.grib2";
    static final byte[] LSM = DecoderFixtures.bytes(FIXTURE);
    static final String INDEX = "{\"param\": \"lsm\", \"levtype\": \"sfc\", \"step\": \"0\", \"_offset\": 0, \"_length\": "
            + LSM.length + "}\n";
    static final DecodeRequest LSM_REQUEST = DecodeRequest.grib2("t-lsm", "lsm", 2, 0, 0, null, null, null);

    /** A two-product test model on the default IFS layout: the field and its 3-hour difference. */
    static final EcmwfDefinition MODEL = new EcmwfDefinition("ecmwf-ifs", "IFS", BUCKET,
            EcmwfAdapter.DEFAULT_TEMPLATE, List.of("0-3/3"), Duration.ofHours(12), Duration.ofHours(7), "0p25",
            List.of(new EcmwfDefinition.Product("t-lsm", List.of(new EcmwfDefinition.Component("lsm", "sfc", null,
                            LSM_REQUEST)), true, 1.0, 0, null),
                    new EcmwfDefinition.Product("t-lsm-3h", List.of(new EcmwfDefinition.Component("lsm", "sfc", null,
                            LSM_REQUEST)), false, 1.0, 3, step -> step >= 3)),
            "t-lsm");

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;
    @Autowired Grib2Decoder decoder;
    @Autowired RasterPaths paths;

    MutableClock clock;
    EcmwfRunIngest ingest;
    ProductAdapterHarness.MapState state;
    UpstreamClient client;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        SourceService sources = mock(SourceService.class);
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(60, 4_000, 2_000));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        UsageMeter usage = new UsageMeter(jdbc, clock);
        BackoffRegistry backoff = new BackoffRegistry(jdbc, clock);
        BudgetGovernor budgets = new BudgetGovernor(sources, usage, clock);
        CredentialsAccess credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        client = new UpstreamClient(budgets, backoff, usage, install, credentials, sources, jdbc, jsonMapper,
                properties, clock);
        ingest = new EcmwfRunIngest(decoder, paths);
        state = new ProductAdapterHarness.MapState();
        // The mirror answers 404 for an index that is not published yet.
        WIREMOCK.stubFor(get(urlPathMatching("/" + BUCKET + "/.*\\.index")).atPriority(10)
                .willReturn(aResponse().withStatus(404).withBody("<Error><Code>NoSuchKey</Code></Error>")));
    }

    private ProductAdapterHarness.ParamContext context(RecordingSink sink, String... steps) {
        return context(EcmwfAdapter.ID, sink, steps);
    }

    /** A context whose HTTP budget and backoff belong to {@code httpSourceId} (the 503 test uses its own). */
    private ProductAdapterHarness.ParamContext context(String httpSourceId, RecordingSink sink, String... steps) {
        EcmwfAdapter adapter = new EcmwfAdapter(ingest, baseUrl());
        return new ProductAdapterHarness.ParamContext(adapter.descriptor(),
                client.forSource(httpSourceId, adapter.descriptor()), clock, state, sink,
                ValidatedParams.of(Map.of(EcmwfRunIngest.STEPS, List.of(steps))));
    }

    private static URI objectUri(String key) {
        return URI.create(baseUrl() + "/" + BUCKET + "/" + key);
    }

    private static String key(int step) {
        return ModelCycleSelector.objectKey(EcmwfAdapter.DEFAULT_TEMPLATE.replace("{res}", "0p25"), RUN, step);
    }

    private static void stubStep(int step, String index) {
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + EcmwfRunIngest.indexKey(key(step))))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(index)));
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + key(step)))
                .withHeader("Range", equalTo("bytes=0-" + (LSM.length - 1)))
                .willReturn(aResponse().withStatus(206).withBody(LSM)));
    }

    @Test
    @DisplayName("index → ranges → commits → run complete; the difference product is clamped at zero")
    void completeRun() throws Exception {
        stubStep(0, INDEX);
        stubStep(3, INDEX);
        RecordingSink sink = new RecordingSink().decoding();
        ingest.fetch(context(sink, "0", "3"), MODEL, EcmwfAdapterTest::objectUri);

        assertThat(sink.committed()).extracting(c -> c.frame().productId() + "@" + c.frame().forecastStep())
                .containsExactly("t-lsm@0", "t-lsm@3", "t-lsm-3h@3");
        float[] difference = sink.committed("t-lsm-3h").get(0).bands().physical().get(0);
        assertThat(difference).hasSize(1440 * 721);
        for (float v : difference) {
            assertThat(v).isEqualTo(0f);
        }
        assertThat(sink.completedRuns()).containsExactlyInAnyOrder("t-lsm@" + RUN, "t-lsm-3h@" + RUN);
        assertThat(state.asMap()).containsEntry("done:" + CYCLE, "0,3").doesNotContainKey("tries:" + CYCLE);
    }

    @Test
    @DisplayName("a missing reference message leaves the step undone, and a later fetch completes it")
    void missingReferenceRetries() throws Exception {
        stubStep(3, INDEX);
        RecordingSink first = new RecordingSink().decoding();
        ingest.fetch(context(first, "3"), MODEL, EcmwfAdapterTest::objectUri);
        assertThat(first.committed()).extracting(c -> c.frame().productId()).containsExactly("t-lsm");
        assertThat(state.asMap()).doesNotContainKey("done:" + CYCLE).containsEntry("tries:" + CYCLE, "3:1");
        assertThat(first.completedRuns()).containsExactly("t-lsm@" + RUN);

        stubStep(0, INDEX);
        RecordingSink second = new RecordingSink().decoding();
        ingest.fetch(context(second, "3"), MODEL, EcmwfAdapterTest::objectUri);
        assertThat(second.committed()).extracting(c -> c.frame().productId()).containsExactly("t-lsm-3h");
        assertThat(second.completedRuns()).containsExactly("t-lsm-3h@" + RUN);
        assertThat(state.asMap()).containsEntry("done:" + CYCLE, "3").doesNotContainKey("tries:" + CYCLE);
    }

    @Test
    @DisplayName("a missing required field is retried, then skipped after the attempt limit; the run never completes")
    void missingFieldGivesUp() throws Exception {
        String other = "{\"param\": \"2t\", \"levtype\": \"sfc\", \"_offset\": 0, \"_length\": 10}\n";
        stubStep(0, other);
        for (int attempt = 1; attempt <= EcmwfRunIngest.MAX_STEP_ATTEMPTS; attempt++) {
            RecordingSink sink = new RecordingSink().decoding();
            ingest.fetch(context(sink, "0"), MODEL, EcmwfAdapterTest::objectUri);
            assertThat(sink.committed()).isEmpty();
            assertThat(sink.formatChanges()).containsKey("t-lsm");
        }
        assertThat(state.asMap()).containsEntry("done:" + CYCLE, "0").doesNotContainKey("tries:" + CYCLE)
                .doesNotContainKey("complete:t-lsm");
        int before = WIREMOCK.findAll(getRequestedFor(urlPathEqualTo("/" + BUCKET + "/" + EcmwfRunIngest.indexKey(key(0))))).size();
        ingest.fetch(context(new RecordingSink(), "0"), MODEL, EcmwfAdapterTest::objectUri);
        assertThat(WIREMOCK.findAll(getRequestedFor(urlPathEqualTo("/" + BUCKET + "/" + EcmwfRunIngest.indexKey(key(0))))))
                .as("a step given up on is not fetched again").hasSize(before);
    }

    @Test
    @DisplayName("a 503 on the index fails the fetch and marks no step done")
    void serviceUnavailable() {
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + EcmwfRunIngest.indexKey(key(0))))
                .willReturn(aResponse().withStatus(503).withHeader("Retry-After", "120")));
        RecordingSink sink = new RecordingSink();
        assertThatThrownBy(() -> ingest.fetch(context("ecmwf-ifs-503-test", sink, "0"), MODEL,
                EcmwfAdapterTest::objectUri))
                .isInstanceOf(AdapterException.class);
        assertThat(state.asMap()).doesNotContainKey("done:" + CYCLE);
        assertThat(sink.committed()).isEmpty();
    }

    @Test
    @DisplayName("object URLs: the S3 mirror by default, path style under a base URL; the fallback host constant")
    void objectUrls() {
        EcmwfAdapter real = new EcmwfAdapter(new EcmwfRunIngest(decoder, paths), "");
        assertThat(real.objectUri("a/b.index").toString())
                .isEqualTo("https://ecmwf-forecasts.s3.amazonaws.com/a/b.index");
        EcmwfAdapter stubbed = new EcmwfAdapter(new EcmwfRunIngest(decoder, paths), "http://localhost:1/");
        assertThat(stubbed.objectUri("a/b").toString()).isEqualTo("http://localhost:1/ecmwf-forecasts/a/b");
        assertThat(EcmwfAdapter.FALLBACK_HOST).isEqualTo("https://data.ecmwf.int/forecasts");
    }
}
