package ai.foresignal.source.adapters.nexrad;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
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
import ai.foresignal.raster.decode.SafeNetcdf;
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
import ai.foresignal.testsupport.raster.ProductAdapterHarness.RecordingSink;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
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
 * The {@code nexrad-l3} adapter against a stubbed {@code unidata-nexrad-level3} (prompt 106 task 4,
 * post-review fix): day-prefix listing with {@code start-after}, download, NIDS decode, resampling and the
 * cursor. The objects served are the committed real fixtures ({@code fixtures/nexrad-l3/README.md}).
 */
@IntegrationTest
class NexradL3AdapterTest extends WireMockSupport {

    static final String BUCKET = NexradL3Adapter.BUCKET;
    static final Instant NOW = Instant.parse("2026-10-16T19:20:00Z");
    static final String N0B_PREFIX = "FWS_N0B_2026_10_16_";
    static final String N0G_PREFIX = "FWS_N0G_2026_10_16_";
    static final String N0B_KEY = "FWS_N0B_2026_10_16_19_15_25";
    static final String N0G_KEY = "FWS_N0G_2026_10_16_19_01_16";

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;
    @Autowired SafeNetcdf safe;

    MutableClock clock;
    BudgetGovernor budgets;
    UsageMeter usage;
    SourceService sources;
    NexradL3Adapter adapter;
    ProductAdapterHarness.MapState state;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        sources = mock(SourceService.class);
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(10, 2_000, 2_000));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        usage = new UsageMeter(jdbc, clock);
        budgets = new BudgetGovernor(sources, usage, clock);
        adapter = new NexradL3Adapter(baseUrl(), null, safe);
        state = new ProductAdapterHarness.MapState();
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/")).atPriority(10)
                .willReturn(aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(ProductAdapterHarness.listing(BUCKET, "", Map.of(), false))));
    }

    private ai.foresignal.source.spi.FetchContext context(RecordingSink sink, List<String> products) {
        CredentialsAccess credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        UpstreamClient client = new UpstreamClient(budgets, new BackoffRegistry(jdbc, clock), usage, install, credentials,
                sources, jdbc, jsonMapper, properties, clock);
        client.setS3Endpoint(baseUrl());
        SourceDescriptor descriptor = adapter.descriptor();
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(NexradL3Adapter.SITES_PARAM, List.of("FWS"));
        params.put("products", products);
        return new ProductAdapterHarness.ParamContext(descriptor, client.forSource(NexradL3Adapter.ID, descriptor), clock,
                state, sink, ValidatedParams.of(params));
    }

    private static void stubListing(String prefix, String startAfter, Map<String, Long> objects) {
        MappingBuilder mapping = get(urlPathEqualTo("/" + BUCKET + "/"))
                .withQueryParam("prefix", equalTo(prefix))
                .withQueryParam("start-after", startAfter == null ? absent() : equalTo(startAfter));
        WIREMOCK.stubFor(mapping.willReturn(aResponse().withHeader("Content-Type", "application/xml")
                .withBody(ProductAdapterHarness.listing(BUCKET, prefix, objects, false))));
    }

    @Test
    @DisplayName("lists the day prefix, decodes the newest scans and commits 0.005° latlon u8 frames")
    void ingestsN0b() throws Exception {
        byte[] n0b = DecoderFixtures.bytes("fixtures/nexrad-l3/" + N0B_KEY);
        Map<String, Long> objects = new LinkedHashMap<>();
        objects.put("FWS_N0B_2026_10_16_17_00_00", (long) n0b.length);
        objects.put(N0B_KEY, (long) n0b.length);
        stubListing(N0B_PREFIX, null, objects);
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + N0B_KEY)).willReturn(aResponse().withBody(n0b)));
        RecordingSink sink = new RecordingSink();

        adapter.fetch(context(sink, List.of(NexradL3Adapter.N0B)));

        // The 17:00 scan is older than the 60-minute lookback and is never downloaded.
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/FWS_N0B_2026_10_16_17_00_00")));
        assertThat(sink.committed(NexradL3Adapter.N0B)).hasSize(1);
        var frame = sink.committed(NexradL3Adapter.N0B).get(0).frame();
        assertThat(frame.validTime()).isEqualTo(Instant.parse("2026-10-16T19:15:25Z"));
        assertThat(frame.grid().type()).isEqualTo(GridDefinition.LATLON);
        assertThat(frame.grid().nx()).isEqualTo(981);
        assertThat(frame.upstreamKey()).isEqualTo(N0B_KEY);
        DecodedBands bands = sink.committed(NexradL3Adapter.N0B).get(0).bands();
        assertThat(bands).isNotNull();
        assertThat(state.get("lastKey:FWS_N0B")).contains(N0B_KEY);
    }

    @Test
    @DisplayName("the next run lists with start-after the last key and commits nothing new")
    void startAfter() throws Exception {
        state.put("lastKey:FWS_N0B", N0B_KEY);
        stubListing(N0B_PREFIX, N0B_KEY, Map.of());
        RecordingSink sink = new RecordingSink();

        adapter.fetch(context(sink, List.of(NexradL3Adapter.N0B)));

        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/" + BUCKET + "/")).withQueryParam("prefix", equalTo(N0B_PREFIX))
                .withQueryParam("start-after", equalTo(N0B_KEY)));
        assertThat(sink.committed()).isEmpty();
    }

    @Test
    @DisplayName("N0G is decoded as velocity on its own product")
    void ingestsN0g() throws Exception {
        clock.setInstant(Instant.parse("2026-10-16T19:05:00Z"));
        byte[] n0g = DecoderFixtures.bytes("fixtures/nexrad-l3/" + N0G_KEY);
        stubListing(N0G_PREFIX, null, Map.of(N0G_KEY, (long) n0g.length));
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + N0G_KEY)).willReturn(aResponse().withBody(n0g)));
        RecordingSink sink = new RecordingSink();

        adapter.fetch(context(sink, List.of(NexradL3Adapter.N0G)));

        assertThat(sink.committed(NexradL3Adapter.N0G)).hasSize(1);
        assertThat(sink.committed(NexradL3Adapter.N0G).get(0).frame().variable()).isEqualTo("BaseVelocityDV");
        assertThat(sink.committed(NexradL3Adapter.N0B)).isEmpty();
    }

    @Test
    @DisplayName("a file that is not NIDS is skipped with a warning and the cursor moves past it")
    void undecodableSkipped() throws Exception {
        String key = "FWS_N0B_2026_10_16_19_10_00";
        byte[] junk = "not a radar product".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        stubListing(N0B_PREFIX, null, Map.of(key, (long) junk.length));
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + key)).willReturn(aResponse().withBody(junk)));
        RecordingSink sink = new RecordingSink();

        adapter.fetch(context(sink, List.of(NexradL3Adapter.N0B)));

        assertThat(sink.committed()).isEmpty();
        assertThat(state.get("lastKey:FWS_N0B")).contains(key);
    }
}
