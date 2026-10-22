package ai.foresignal.source.adapters.goes;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.raster.decode.NetcdfDecoder;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import ai.foresignal.testsupport.raster.ProductAdapterHarness;
import ai.foresignal.testsupport.raster.ProductAdapterHarness.RecordingSink;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code goes-east-abi} adapter against a stubbed S3 (prompt 046): band 13 filtering across
 * channels, satellites and scan modes, {@code start-after} within an hour, budget and byte accounting,
 * and a silent format change. The download cases that need a real band 13 file use the
 * {@code downloadTestFixtures} file and skip without it.
 */
@IntegrationTest
class GoesAbiAdapterWireMockTest extends WireMockSupport {

    static final String BUCKET = GoesAbiAdapter.BUCKET;
    static final Instant NOW = Instant.parse("2026-10-12T12:10:00Z");
    static final String HOUR_12 = "ABI-L2-CMIPC/2026/285/12/OR_ABI-L2-CMIPC-M";
    static final String HOUR_11 = "ABI-L2-CMIPC/2026/285/11/OR_ABI-L2-CMIPC-M";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;
    @Autowired NetcdfDecoder decoder;

    MutableClock clock;
    SourceService sources;
    UsageMeter usage;
    BudgetGovernor budgets;
    GoesAbiAdapter adapter;
    ProductAdapterHarness.MapState state;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        sources = mock(SourceService.class);
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(10, 2_000, 2_500));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        usage = new UsageMeter(jdbc, clock);
        budgets = new BudgetGovernor(sources, usage, clock);
        adapter = new GoesAbiAdapter(decoder, baseUrl());
        state = new ProductAdapterHarness.MapState();
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/")).atPriority(10)
                .willReturn(aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(ProductAdapterHarness.listing(BUCKET, "", Map.of(), false))));
    }

    private ai.foresignal.source.spi.FetchContext context(RecordingSink sink) {
        CredentialsAccess credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        UpstreamClient client = new UpstreamClient(budgets, new BackoffRegistry(jdbc, clock), usage, install, credentials,
                sources, jdbc, jsonMapper, properties, clock);
        client.setS3Endpoint(baseUrl());
        SourceDescriptor descriptor = adapter.descriptor();
        // Prompt 106: pin the C13 product so this 046 test stays about band 13 (the default now also
        // enables the water-vapor channels).
        return new ProductAdapterHarness.ParamContext(descriptor, client.forSource(GoesAbiAdapter.ID, descriptor), clock,
                state, sink, ai.foresignal.source.spi.ValidatedParams.of(Map.of("products", List.of(GoesAbiAdapter.IR))));
    }

    private static void stubListing(String prefix, String startAfter, Map<String, Long> objects) {
        MappingBuilder mapping = get(urlPathEqualTo("/" + BUCKET + "/"))
                .withQueryParam("prefix", equalTo(prefix))
                .withQueryParam("start-after", startAfter == null ? absent() : equalTo(startAfter));
        WIREMOCK.stubFor(mapping.willReturn(aResponse().withHeader("Content-Type", "application/xml")
                .withBody(ProductAdapterHarness.listing(BUCKET, prefix, objects, false))));
    }

    /** A CMIPC key of the 12z hour. */
    static String key(String mode, String channel, String sat, String start) {
        return "ABI-L2-CMIPC/2026/285/12/OR_ABI-L2-CMIPC-" + mode + channel + "_" + sat + "_s" + start + "_e" + start
                + "_c" + start + ".nc";
    }

    @Test
    @DisplayName("only GOES-19 band 13 keys count, in any scan mode, oldest first")
    void filtersBand13() throws Exception {
        Map<String, Long> objects = new LinkedHashMap<>();
        objects.put(key("M3", "C13", "G19", "20262851200000"), 3_800_000L);
        objects.put(key("M6", "C01", "G19", "20262851201170"), 5_900_000L);
        objects.put(key("M6", "C02", "G19", "20262851201170"), 9_000_000L);
        objects.put(key("M6", "C13", "G18", "20262851201170"), 3_800_000L);
        objects.put(key("M6", "C13", "G19", "20262851201170"), 3_807_021L);
        objects.put(key("M6", "C13", "G19", "20262851206170"), 3_807_021L);
        objects.put(key("M6", "C14", "G19", "20262851201170"), 3_800_000L);
        stubListing(HOUR_12, null, objects);

        List<GoesAbiAdapter.Frame> frames = adapter.listNew(context(new RecordingSink()), null, null, NOW);

        assertThat(frames).extracting(GoesAbiAdapter.Frame::key).containsExactly(
                key("M3", "C13", "G19", "20262851200000"),
                key("M6", "C13", "G19", "20262851201170"),
                key("M6", "C13", "G19", "20262851206170"));
        assertThat(frames.get(1).time()).isEqualTo(Instant.parse("2026-10-12T12:01:17Z"));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/" + BUCKET + "/")).withQueryParam("prefix", equalTo(HOUR_11)));
    }

    @Test
    @DisplayName("within an hour the listing starts after the last committed key and keeps only newer scans")
    void startAfterWithinHour() throws Exception {
        String last = key("M6", "C13", "G19", "20262851201170");
        Map<String, Long> objects = new LinkedHashMap<>();
        objects.put(key("M6", "C13", "G19", "20262851206170"), 3_807_021L);
        objects.put(key("M6", "C14", "G19", "20262851201170"), 3_800_000L);
        stubListing(HOUR_12, last, objects);

        List<GoesAbiAdapter.Frame> frames = adapter.listNew(context(new RecordingSink()), last,
                GoesAbiAdapter.startTime(last).orElseThrow(), NOW);

        assertThat(frames).extracting(GoesAbiAdapter.Frame::key).containsExactly(key("M6", "C13", "G19", "20262851206170"));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/")).withQueryParam("prefix", equalTo(HOUR_11)));
    }

    @Test
    @DisplayName("each scan costs one request, charged its listed size against mbPerDay, metered once")
    void budgetAccounting() throws Exception {
        byte[] c13 = DecoderFixtures.goesBytesOrSkip();
        String first = key("M6", "C13", "G19", "20262851201170");
        String second = key("M6", "C13", "G19", "20262851206170");
        Map<String, Long> objects = new LinkedHashMap<>();
        objects.put(first, (long) c13.length);
        objects.put(second, (long) c13.length);
        stubListing(HOUR_12, null, objects);
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + first)).willReturn(aResponse().withBody(c13)));
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + second)).willReturn(aResponse().withBody(c13)));
        RecordingSink sink = new RecordingSink();
        long before = budgets.status(GoesAbiAdapter.ID).minuteRemaining();

        adapter.fetch(context(sink));

        assertThat(sink.committed()).hasSize(2);
        GridDefinition grid = sink.committed().get(0).frame().grid();
        assertThat(grid.type()).isEqualTo(GridDefinition.GEOS);
        assertThat(grid.nx()).isEqualTo(2500);
        assertThat(sink.committed().get(0).frame().obsStart()).isEqualTo(Instant.parse("2026-10-12T12:01:17Z"));
        assertThat(sink.committed().get(0).frame().obsEnd()).isEqualTo(Instant.parse("2026-10-12T12:03:55.400Z"));
        assertThat(budgets.status(GoesAbiAdapter.ID).minuteRemaining())
                .as("two hour listings and two downloads").isEqualTo(before - 4);
        assertThat(usage.bytesToday(GoesAbiAdapter.ID)).isGreaterThanOrEqualTo(2L * c13.length)
                .isLessThan(3L * c13.length);
        assertThat(state.get("lastKey:" + GoesAbiAdapter.IR)).contains(second);
    }

    @Test
    @DisplayName("a file without CMI is a silent format change: reported, no frame, not retried")
    void missingCmi() throws Exception {
        byte[] winds = DecoderFixtures.bytes("raster/selftest/goes19-abi-dmwvc-c08-20261012-0001.nc");
        String k = key("M6", "C13", "G19", "20262851201170");
        stubListing(HOUR_12, null, Map.of(k, (long) winds.length));
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + k)).willReturn(aResponse().withBody(winds)));
        RecordingSink sink = new RecordingSink();

        assertThat(adapter.fetch(context(sink)).records()).isEmpty();

        assertThat(sink.formatChanges()).containsEntry(GoesAbiAdapter.IR, "variable CMI missing");
        assertThat(state.get("lastKey:" + GoesAbiAdapter.IR)).contains(k);
    }

    @Test
    @DisplayName("a disabled product makes no request")
    void disabled() throws Exception {
        assertThat(adapter.fetch(context(new RecordingSink().disable(GoesAbiAdapter.IR))).records()).isEmpty();
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("the descriptor and the key grammar")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(10, 2_000, 2_500));
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(5));
        assertThat(d.maxResponseBytes()).isEqualTo(8 * 1024 * 1024);
        assertThat(d.kinds()).containsExactly(ProductSpec.RECORD_KIND);
        ProductSpec ir = d.products().get(0);
        assertThat(ir.id()).isEqualTo("goes-east-ir");
        assertThat(ir.styles()).containsExactly("ir-gray@1", "ir-enhanced@1");
        assertThat(ir.physical(14_733)).isCloseTo(297.33, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(ir.temporal().maxGap()).isEqualTo(Duration.ofMinutes(15));
        assertThat(ir.retention().summary()).isEqualTo("every frame for 24h, then 30m frames to 72h");
        assertThat(GoesAbiAdapter.hourPrefix(NOW)).isEqualTo(HOUR_12);
        assertThat(GoesAbiAdapter.parseStart("20262851201170")).isEqualTo(Instant.parse("2026-10-12T12:01:17Z"));
        assertThat(GoesAbiAdapter.parseStart("20260011200005")).isEqualTo(Instant.parse("2026-01-01T12:00:00.500Z"));
        assertThat(GoesAbiAdapter.startTime("ABI-L2-CMIPC/2026/285/12/OR_ABI-L2-CMIPC-M6C13_G18_s20262851201170_e1_c1.nc"))
                .isEmpty();
        assertThat(new GoesAbiAdapter(decoder, "").objectUri("k").toString()).isEqualTo("https://noaa-goes19.s3.amazonaws.com/k");
    }
}
