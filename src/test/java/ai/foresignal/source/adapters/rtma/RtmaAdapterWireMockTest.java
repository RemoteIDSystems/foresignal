package ai.foresignal.source.adapters.rtma;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
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
import ai.foresignal.raster.ProductEnablement;
import ai.foresignal.raster.RasterPaths;
import ai.foresignal.raster.decode.Grib2Decoder;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import ai.foresignal.testsupport.raster.GridStats;
import ai.foresignal.testsupport.raster.ProductAdapterHarness;
import ai.foresignal.testsupport.raster.ProductAdapterHarness.Committed;
import ai.foresignal.testsupport.raster.ProductAdapterHarness.RecordingSink;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code rtma} source end to end against a stubbed {@code noaa-rtma-pds}: the captured 2026-10-17 11:00Z
 * {@code .idx}, the GUST message answered 206 from the downloaded fixture, and the frame committed. Before
 * 2026-10-22 the downloads were keyed by message and looked up by field, so every product failed to decode
 * and nothing was ever committed.
 */
@IntegrationTest
class RtmaAdapterWireMockTest extends WireMockSupport {

    static final String KEY = "rtma2p5_ru.20261017/rtma2p5_ru.t1100z.2dvaranl_ndfd.grb2";
    static final Instant ANALYSIS = Instant.parse("2026-10-17T11:00:00Z");

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;
    @Autowired Grib2Decoder decoder;
    @Autowired RasterPaths paths;

    MutableClock clock;
    SourceService sources;
    RtmaAdapter adapter;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(ANALYSIS.plusSeconds(5 * 60));
        sources = mock(SourceService.class);
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(60, 3_000, 4_500));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        adapter = new RtmaAdapter(decoder, paths, baseUrl(), "small");
        // Earlier analyses are not published (S3 answers 404; WireMock fails on unmatched requests).
        WIREMOCK.stubFor(get(urlPathMatching("/" + RtmaAdapter.BUCKET + "/.*\\.idx")).atPriority(10)
                .willReturn(aResponse().withStatus(404).withBody("<Error><Code>NoSuchKey</Code></Error>")));
    }

    private ProductAdapterHarness.ParamContext context(RecordingSink sink) {
        CredentialsAccess credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        UsageMeter usage = new UsageMeter(jdbc, clock);
        UpstreamClient client = new UpstreamClient(new BudgetGovernor(sources, usage, clock),
                new BackoffRegistry(jdbc, clock), usage, install, credentials, sources, jdbc, jsonMapper, properties,
                clock);
        client.setS3Endpoint(baseUrl());
        SourceDescriptor descriptor = adapter.descriptor();
        return new ProductAdapterHarness.ParamContext(descriptor, client.forSource(RtmaAdapter.ID, descriptor), clock,
                new ProductAdapterHarness.MapState(), sink,
                ValidatedParams.of(Map.of(ProductEnablement.PRODUCTS_PARAM, List.of(RtmaAdapter.GUST))));
    }

    @Test
    @DisplayName("a published analysis is downloaded, decoded and committed as a frame")
    void fetchCommitsFrame() throws Exception {
        Assumptions.assumeTrue(Files.isRegularFile(RtmaAdapterContractTest.GUST_FILE),
                "RTMA-RU fixture absent: run ./gradlew downloadTestFixtures");
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + RtmaAdapter.BUCKET + "/" + KEY + ".idx"))
                .willReturn(aResponse().withHeader("Content-Type", "text/plain")
                        .withBody(DecoderFixtures.bytes(RtmaAdapterContractTest.IDX))));
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + RtmaAdapter.BUCKET + "/" + KEY))
                .withHeader("Range", equalTo("bytes=57580533-63198168"))
                .willReturn(aResponse().withStatus(206)
                        .withBody(Files.readAllBytes(RtmaAdapterContractTest.GUST_FILE))));
        RecordingSink sink = new RecordingSink().decoding();

        adapter.fetch(context(sink));

        assertThat(sink.committed()).extracting(c -> c.frame().productId()).containsExactly(RtmaAdapter.GUST);
        Committed gust = sink.committed(RtmaAdapter.GUST).get(0);
        assertThat(gust.frame().validTime()).isEqualTo(ANALYSIS);
        assertThat(gust.frame().upstreamKey()).isEqualTo(KEY);
        assertThat(GridStats.of(gust.bands().physical().get(0)).max()).isCloseTo(32.37, within(1e-3));
    }
}
