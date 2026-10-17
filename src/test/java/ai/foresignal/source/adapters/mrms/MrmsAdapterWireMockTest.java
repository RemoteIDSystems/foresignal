package ai.foresignal.source.adapters.mrms;

import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
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
import ai.foresignal.collect.BudgetExceededException;
import ai.foresignal.collect.BudgetGovernor;
import ai.foresignal.collect.InBackoffException;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.collect.UpstreamHttpException;
import ai.foresignal.collect.UsageMeter;
import ai.foresignal.config.ForesignalProperties;
import ai.foresignal.raster.decode.Grib2Decoder;
import ai.foresignal.security.InstallIdService;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.raster.DecoderFixtures;
import ai.foresignal.testsupport.raster.ProductAdapterHarness;
import ai.foresignal.testsupport.raster.ProductAdapterHarness.Committed;
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
 * The {@code mrms} adapter against a stubbed S3 (prompt 046): ListObjectsV2 with {@code start-after},
 * the UTC day rollover, the 15-frame cap, PrecipRate/PrecipFlag pairing and its 4-minute fallback, the
 * {@code mbPerDay} advance charge, a 503 with {@code Retry-After}, the probe and a silent format change.
 *
 * <p>Uses the real {@link UpstreamClient} (budget, backoff, usage) pointed at WireMock path-style, and
 * the context's {@link Grib2Decoder}; the raster pipeline is a recording sink.
 */
@IntegrationTest
class MrmsAdapterWireMockTest extends WireMockSupport {

    static final String BUCKET = MrmsAdapter.BUCKET;
    static final Instant NOW = Instant.parse("2026-10-12T12:10:00Z");

    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper jsonMapper;
    @Autowired ForesignalProperties properties;
    @Autowired InstallIdService install;
    @Autowired Grib2Decoder decoder;

    MutableClock clock;
    SourceService sources;
    UsageMeter usage;
    BackoffRegistry backoff;
    BudgetGovernor budgets;
    MrmsAdapter adapter;
    ProductAdapterHarness.MapState state;

    static final byte[] MESH_BYTES = DecoderFixtures.mrmsBytes(DecoderFixtures.MRMS_MESH);

    @BeforeEach
    void setUp() {
        clock = new MutableClock(NOW);
        sources = mock(SourceService.class);
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(30, 12_000, 4_000));
        when(sources.effectiveParams(anyString())).thenReturn(ValidatedParams.empty());
        usage = new UsageMeter(jdbc, clock);
        backoff = new BackoffRegistry(jdbc, clock);
        budgets = new BudgetGovernor(sources, usage, clock);
        adapter = new MrmsAdapter(decoder, baseUrl());
        state = new ProductAdapterHarness.MapState();
        // Anything not stubbed more specifically lists empty.
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/")).atPriority(10)
                .willReturn(aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(ProductAdapterHarness.listing(BUCKET, "", Map.of(), false))));
    }

    private ProductAdapterHarness.Context context(RecordingSink sink) {
        CredentialsAccess credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of());
        UpstreamClient client = new UpstreamClient(budgets, backoff, usage, install, credentials, sources, jdbc, jsonMapper,
                properties, clock);
        client.setS3Endpoint(baseUrl());
        SourceDescriptor descriptor = adapter.descriptor();
        return new ProductAdapterHarness.Context(descriptor, client.forSource(MrmsAdapter.ID, descriptor), clock, state, sink);
    }

    private static void stubListing(String prefix, String startAfter, Map<String, Long> objects) {
        MappingBuilder mapping = get(urlPathEqualTo("/" + BUCKET + "/"))
                .withQueryParam("list-type", equalTo("2"))
                .withQueryParam("prefix", equalTo(prefix))
                .withQueryParam("start-after", startAfter == null ? absent() : equalTo(startAfter));
        WIREMOCK.stubFor(mapping.willReturn(aResponse().withHeader("Content-Type", "application/xml")
                .withBody(ProductAdapterHarness.listing(BUCKET, prefix, objects, false))));
    }

    private static void stubObject(String key, byte[] body) {
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + key)).willReturn(aResponse().withBody(body)));
    }

    private static Map<String, Long> meshFrames(Instant... times) {
        Map<String, Long> objects = new LinkedHashMap<>();
        for (Instant t : times) {
            String key = MrmsAdapter.MESH_UPSTREAM.key(t);
            objects.put(key, (long) MESH_BYTES.length);
            stubObject(key, MESH_BYTES);
        }
        return objects;
    }

    private static Instant t(String time) {
        return Instant.parse(time);
    }

    @Test
    @DisplayName("lists after the last committed key with start-after, commits each frame and advances the cursor")
    void listsAfterTheLastKey() throws Exception {
        String last = MrmsAdapter.MESH_UPSTREAM.key(t("2026-10-12T12:00:00Z"));
        state.put("lastKey:" + MrmsAdapter.MESH, last);
        stubListing("CONUS/MESH_Max_60min_00.50/20261012/", last,
                meshFrames(t("2026-10-12T12:02:00Z"), t("2026-10-12T12:04:00Z"), t("2026-10-12T12:06:00Z")));
        RecordingSink sink = new RecordingSink().disable(MrmsAdapter.REFLECTIVITY, MrmsAdapter.PRECIP_RATE);

        FetchResult result = adapter.fetch(context(sink));

        assertThat(result.records()).hasSize(3).allSatisfy(r -> assertThat(r.kind()).isEqualTo(ProductSpec.RECORD_KIND));
        List<Committed> committed = sink.committed(MrmsAdapter.MESH);
        assertThat(committed).extracting(c -> c.frame().validTime())
                .containsExactly(t("2026-10-12T12:02:00Z"), t("2026-10-12T12:04:00Z"), t("2026-10-12T12:06:00Z"));
        assertThat(committed.get(0).frame().upstreamKey())
                .isEqualTo("CONUS/MESH_Max_60min_00.50/20261012/MRMS_MESH_Max_60min_00.50_20261012-120200.grib2.gz");
        assertThat(committed.get(0).frame().grid().nx()).isEqualTo(7000);
        assertThat(committed.get(0).frame().grid().params()).containsEntry("la1", 54.995).containsEntry("lo1", -129.995);
        assertThat(committed.get(0).frame().variable()).isEqualTo("MESH_Max_60min");
        assertThat(state.get("lastKey:" + MrmsAdapter.MESH))
                .contains(MrmsAdapter.MESH_UPSTREAM.key(t("2026-10-12T12:06:00Z")));
        WIREMOCK.verify(getRequestedFor(urlPathEqualTo("/" + BUCKET + "/")).withQueryParam("start-after", equalTo(last)));
    }

    @Test
    @DisplayName("after UTC midnight it lists yesterday's prefix after the cursor, then today's, in time order")
    void dayRollover() throws Exception {
        clock.setInstant(t("2026-10-13T00:03:00Z"));
        String last = MrmsAdapter.MESH_UPSTREAM.key(t("2026-10-12T23:56:00Z"));
        state.put("lastKey:" + MrmsAdapter.MESH, last);
        stubListing("CONUS/MESH_Max_60min_00.50/20261012/", last, meshFrames(t("2026-10-12T23:58:00Z")));
        stubListing("CONUS/MESH_Max_60min_00.50/20261013/", null,
                meshFrames(t("2026-10-13T00:00:00Z"), t("2026-10-13T00:02:00Z")));
        RecordingSink sink = new RecordingSink().disable(MrmsAdapter.REFLECTIVITY, MrmsAdapter.PRECIP_RATE);

        adapter.fetch(context(sink));

        assertThat(sink.committed(MrmsAdapter.MESH)).extracting(c -> c.frame().validTime())
                .containsExactly(t("2026-10-12T23:58:00Z"), t("2026-10-13T00:00:00Z"), t("2026-10-13T00:02:00Z"));
        assertThat(state.get("lastKey:" + MrmsAdapter.MESH))
                .contains(MrmsAdapter.MESH_UPSTREAM.key(t("2026-10-13T00:02:00Z")));

        // Caught up: the next run lists today's prefix only.
        clock.setInstant(t("2026-10-13T00:05:00Z"));
        WIREMOCK.resetRequests();
        adapter.fetch(context(sink));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/"))
                .withQueryParam("prefix", equalTo("CONUS/MESH_Max_60min_00.50/20261012/")));
    }

    @Test
    @DisplayName("commits at most 15 new frames per product per run, skipping frames older than the 6-hour rung")
    void capsAtFifteen() throws Exception {
        clock.setInstant(t("2026-10-12T12:40:00Z"));
        Map<String, Long> objects = new LinkedHashMap<>(meshFrames(t("2026-10-12T05:00:00Z"), t("2026-10-12T06:00:00Z")));
        for (int i = 0; i < 20; i++) {
            objects.putAll(meshFrames(t("2026-10-12T12:00:00Z").plus(Duration.ofMinutes(2L * i))));
        }
        stubListing("CONUS/MESH_Max_60min_00.50/20261012/", null, objects);
        RecordingSink sink = new RecordingSink().disable(MrmsAdapter.REFLECTIVITY, MrmsAdapter.PRECIP_RATE);

        adapter.fetch(context(sink));

        List<Committed> committed = sink.committed(MrmsAdapter.MESH);
        assertThat(committed).hasSize(MrmsAdapter.MAX_FRAMES_PER_RUN);
        assertThat(committed.get(0).frame().validTime()).isEqualTo(t("2026-10-12T12:00:00Z"));
        assertThat(committed.get(14).frame().validTime()).isEqualTo(t("2026-10-12T12:28:00Z"));
        assertThat(state.get("lastKey:" + MrmsAdapter.MESH))
                .contains(MrmsAdapter.MESH_UPSTREAM.key(t("2026-10-12T12:28:00Z")));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/"
                + MrmsAdapter.MESH_UPSTREAM.key(t("2026-10-12T05:00:00Z")))));
    }

    @Test
    @DisplayName("PrecipRate and PrecipFlag pair by timestamp; an unpaired rate waits 4 minutes, then commits with band 2 missing")
    void precipPairing() throws Exception {
        clock.setInstant(t("2026-10-12T12:05:00Z"));
        byte[] rate = DecoderFixtures.mrmsBytes(DecoderFixtures.MRMS_PRECIP_RATE);
        byte[] flag = DecoderFixtures.mrmsBytes(DecoderFixtures.MRMS_PRECIP_FLAG);
        Map<String, Long> rates = new LinkedHashMap<>();
        for (String time : List.of("2026-10-12T12:00:00Z", "2026-10-12T12:02:00Z", "2026-10-12T12:04:00Z")) {
            String key = MrmsAdapter.PRECIP_RATE_UPSTREAM.key(t(time));
            rates.put(key, (long) rate.length);
            stubObject(key, rate);
        }
        String flagKey = MrmsAdapter.PRECIP_FLAG_UPSTREAM.key(t("2026-10-12T12:00:00Z"));
        stubObject(flagKey, flag);
        stubListing("CONUS/PrecipRate_00.00/20261012/", null, rates);
        stubListing("CONUS/PrecipFlag_00.00/20261012/", null, Map.of(flagKey, (long) flag.length));
        RecordingSink sink = new RecordingSink().decoding().disable(MrmsAdapter.REFLECTIVITY, MrmsAdapter.MESH);

        adapter.fetch(context(sink));

        List<Committed> first = sink.committed(MrmsAdapter.PRECIP_RATE);
        assertThat(first).as("12:00 paired; 12:02 has no flag and is only 3 minutes old").hasSize(1);
        assertThat(first.get(0).frame().validTime()).isEqualTo(t("2026-10-12T12:00:00Z"));
        assertThat(first.get(0).frame().extraStats()).doesNotContainKey("flagMissing");
        DecodedBands bands = first.get(0).bands();
        assertThat(bands.bandCount()).isEqualTo(2);
        long missingRate = 0;
        long missingFlag = 0;
        for (int i = 0; i < DecoderFixtures.MRMS_CELLS; i++) {
            if (bands.codes().get(0)[i] == MrmsCodec.U16_MISSING) {
                missingRate++;
            }
            if (bands.codes().get(1)[i] == MrmsCodec.U16_MISSING) {
                missingFlag++;
            }
        }
        assertThat(missingRate).as("PrecipRate -3 cells (fixture README)").isEqualTo(8_335_467L);
        assertThat(missingFlag).as("PrecipFlag -3 cells").isEqualTo(8_335_467L);
        assertThat(state.get("lastKey:" + MrmsAdapter.PRECIP_RATE))
                .contains(MrmsAdapter.PRECIP_RATE_UPSTREAM.key(t("2026-10-12T12:00:00Z")));

        // 90 seconds later 12:02 is 4.5 minutes old: it commits without its flag; 12:04 still waits.
        clock.setInstant(t("2026-10-12T12:06:30Z"));
        String rateCursor = MrmsAdapter.PRECIP_RATE_UPSTREAM.key(t("2026-10-12T12:00:00Z"));
        Map<String, Long> newer = new LinkedHashMap<>(rates);
        newer.remove(rateCursor);
        stubListing("CONUS/PrecipRate_00.00/20261012/", rateCursor, newer);
        stubListing("CONUS/PrecipFlag_00.00/20261012/", flagKey, Map.of());
        adapter.fetch(context(sink));

        List<Committed> all = sink.committed(MrmsAdapter.PRECIP_RATE);
        assertThat(all).hasSize(2);
        Committed fallback = all.get(1);
        assertThat(fallback.frame().validTime()).isEqualTo(t("2026-10-12T12:02:00Z"));
        assertThat(fallback.frame().extraStats()).containsEntry("flagMissing", true);
        for (int code : fallback.bands().codes().get(1)) {
            assertThat(code).isEqualTo(MrmsCodec.U16_MISSING);
        }
        assertThat(state.get("lastKey:" + MrmsAdapter.PRECIP_RATE))
                .contains(MrmsAdapter.PRECIP_RATE_UPSTREAM.key(t("2026-10-12T12:02:00Z")));
    }

    @Test
    @DisplayName("the listing's Size is charged against mbPerDay before the download: an object the day cannot cover is never fetched")
    void chargesBytesInAdvance() throws Exception {
        when(sources.effectiveBudget(anyString())).thenReturn(Budget.of(30, 12_000, 1));
        budgets = new BudgetGovernor(sources, usage, clock);
        byte[] reflectivity = DecoderFixtures.mrmsBytes(DecoderFixtures.MRMS_REFLECTIVITY);
        String key = MrmsAdapter.REFLECTIVITY_UPSTREAM.key(t("2026-10-12T12:00:37Z"));
        stubObject(key, reflectivity);
        stubListing("CONUS/MergedReflectivityQCComposite_00.50/20261012/", null, Map.of(key, (long) reflectivity.length));
        RecordingSink sink = new RecordingSink().disable(MrmsAdapter.PRECIP_RATE, MrmsAdapter.MESH);

        assertThatThrownBy(() -> adapter.fetch(context(sink))).isInstanceOf(BudgetExceededException.class);

        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/" + key)));
        assertThat(state.get("lastKey:" + MrmsAdapter.REFLECTIVITY)).isEmpty();
        assertThat(sink.committed()).isEmpty();

        // A small MESH file fits the same 1 MB day; its bytes are metered once.
        stubListing("CONUS/MESH_Max_60min_00.50/20261012/", null, meshFrames(t("2026-10-12T12:00:00Z")));
        RecordingSink meshOnly = new RecordingSink().disable(MrmsAdapter.REFLECTIVITY, MrmsAdapter.PRECIP_RATE);
        adapter.fetch(context(meshOnly));
        assertThat(meshOnly.committed()).hasSize(1);
        assertThat(usage.bytesToday(MrmsAdapter.ID)).isGreaterThanOrEqualTo(MESH_BYTES.length);
    }

    @Test
    @DisplayName("a 503 with Retry-After backs the source off for that long and leaves the cursor alone")
    void backoffOn503() throws Exception {
        String key = MrmsAdapter.MESH_UPSTREAM.key(t("2026-10-12T12:00:00Z"));
        stubListing("CONUS/MESH_Max_60min_00.50/20261012/", null, Map.of(key, (long) MESH_BYTES.length));
        WIREMOCK.stubFor(get(urlPathEqualTo("/" + BUCKET + "/" + key))
                .willReturn(aResponse().withStatus(503).withHeader("Retry-After", "120")));
        RecordingSink sink = new RecordingSink().disable(MrmsAdapter.REFLECTIVITY, MrmsAdapter.PRECIP_RATE);

        assertThatThrownBy(() -> adapter.fetch(context(sink))).isInstanceOf(UpstreamHttpException.class);

        assertThat(backoff.isInBackoff(MrmsAdapter.ID)).isTrue();
        assertThat(backoff.backoffUntil(MrmsAdapter.ID)).isEqualTo(NOW.plusSeconds(120));
        assertThat(state.get("lastKey:" + MrmsAdapter.MESH)).isEmpty();
        assertThatThrownBy(() -> adapter.fetch(context(sink))).isInstanceOf(InBackoffException.class);
    }

    @Test
    @DisplayName("the probe lists the newest reflectivity object, downloads it, reads the header only and writes nothing")
    void probe() throws Exception {
        byte[] reflectivity = DecoderFixtures.mrmsBytes(DecoderFixtures.MRMS_REFLECTIVITY);
        String older = MrmsAdapter.REFLECTIVITY_UPSTREAM.key(t("2026-10-12T11:58:38Z"));
        String newest = MrmsAdapter.REFLECTIVITY_UPSTREAM.key(t("2026-10-12T12:00:37Z"));
        stubObject(newest, reflectivity);
        Map<String, Long> objects = new LinkedHashMap<>();
        objects.put(older, (long) reflectivity.length);
        objects.put(newest, (long) reflectivity.length);
        stubListing("CONUS/MergedReflectivityQCComposite_00.50/20261012/", null, objects);
        RecordingSink sink = new RecordingSink().asProbe();

        FetchResult result = adapter.fetch(context(sink));

        assertThat(result.records()).hasSize(1);
        assertThat(sink.committed()).singleElement().satisfies(c -> {
            assertThat(c.frame().upstreamKey()).isEqualTo(newest);
            assertThat(c.frame().grid().nx()).isEqualTo(7000);
            assertThat(c.bands()).as("nothing decoded").isNull();
        });
        assertThat(state.asMap()).isEmpty();
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/" + older)));
    }

    @Test
    @DisplayName("a file without the expected field reports a silent format change, writes no frame and is not retried")
    void silentFormatChange() throws Exception {
        String key = MrmsAdapter.REFLECTIVITY_UPSTREAM.key(t("2026-10-12T12:00:37Z"));
        stubObject(key, MESH_BYTES);
        stubListing("CONUS/MergedReflectivityQCComposite_00.50/20261012/", null, Map.of(key, (long) MESH_BYTES.length));
        RecordingSink sink = new RecordingSink().disable(MrmsAdapter.PRECIP_RATE, MrmsAdapter.MESH);

        FetchResult result = adapter.fetch(context(sink));

        assertThat(result.records()).isEmpty();
        assertThat(sink.committed()).isEmpty();
        assertThat(sink.formatChanges()).containsEntry(MrmsAdapter.REFLECTIVITY,
                "field MergedReflectivityQCComposite missing at level 500 m");
        assertThat(state.get("lastKey:" + MrmsAdapter.REFLECTIVITY)).contains(key);
    }

    @Test
    @DisplayName("disabled products make no request at all")
    void disabledProducts() throws Exception {
        RecordingSink sink = new RecordingSink().disable(MrmsAdapter.REFLECTIVITY, MrmsAdapter.PRECIP_RATE, MrmsAdapter.MESH,
                MrmsAdapter.ROTATION, MrmsAdapter.QPE_1H, MrmsAdapter.QPE_24H, MrmsAdapter.FFG_RATIO);
        assertThat(adapter.fetch(context(sink)).records()).isEmpty();
        WIREMOCK.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    @DisplayName("second wave (053): QPE 1 h at takeEvery 5 commits one file per 10 minutes and passes the rest without"
            + " downloading them; rotation is reduced to the 0.01° grid")
    void secondWaveThinningAndReduction() throws Exception {
        clock.setInstant(t("2026-10-13T04:40:00Z"));
        byte[] qpe = DecoderFixtures.mrmsBytes(DecoderFixtures.MRMS_QPE_1H);
        Map<String, Long> objects = new LinkedHashMap<>();
        for (int i = 0; i < 10; i++) {
            String key = MrmsAdapter.QPE_1H_UPSTREAM.key(t("2026-10-13T04:20:00Z").plus(Duration.ofMinutes(2L * i)));
            objects.put(key, (long) qpe.length);
            stubObject(key, qpe);
        }
        stubListing("CONUS/RadarOnly_QPE_01H_00.00/20261013/", null, objects);
        byte[] rotation = DecoderFixtures.mrmsBytes(DecoderFixtures.MRMS_ROTATION);
        String rotationKey = MrmsAdapter.ROTATION_UPSTREAM.key(t("2026-10-13T04:32:00Z"));
        stubObject(rotationKey, rotation);
        stubListing("CONUS/RotationTrack60min_00.50/20261013/", null, Map.of(rotationKey, (long) rotation.length));
        RecordingSink sink = new RecordingSink().disable(MrmsAdapter.REFLECTIVITY, MrmsAdapter.PRECIP_RATE, MrmsAdapter.MESH,
                MrmsAdapter.QPE_24H, MrmsAdapter.FFG_RATIO);

        adapter.fetch(context(sink));

        assertThat(sink.committed(MrmsAdapter.QPE_1H)).extracting(c -> c.frame().validTime())
                .containsExactly(t("2026-10-13T04:20:00Z"), t("2026-10-13T04:30:00Z"));
        WIREMOCK.verify(0, getRequestedFor(urlPathEqualTo("/" + BUCKET + "/"
                + MrmsAdapter.QPE_1H_UPSTREAM.key(t("2026-10-13T04:22:00Z")))));
        assertThat(state.get("lastKey:" + MrmsAdapter.QPE_1H)).as("the cursor passes the thinned-out files")
                .contains(MrmsAdapter.QPE_1H_UPSTREAM.key(t("2026-10-13T04:38:00Z")));
        Committed rot = sink.committed(MrmsAdapter.ROTATION).get(0);
        assertThat(rot.frame().grid().nx()).isEqualTo(7000);
        assertThat(rot.frame().grid().ny()).isEqualTo(3500);
        assertThat(rot.frame().grid().params()).containsEntry("la1", 54.995).containsEntry("lo1", -129.995)
                .containsEntry("dx", 0.01);
        assertThat(rot.frame().extraStats()).containsEntry("reducedFrom", "14000x7000");
        assertThat(rot.frame().variable()).isEqualTo("RotationTrack60min");
    }

    @Test
    @DisplayName("the descriptor: budgets, cadence, response cap, kinds, retention and the seven products (053 adds four)")
    void descriptor() {
        SourceDescriptor d = adapter.descriptor();
        assertThat(d.id()).isEqualTo("mrms");
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(30, 12_000, 4_500)); // raised by 106 for the extended products
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(2));
        assertThat(d.minInterval()).isEqualTo(Duration.ofMinutes(2));
        assertThat(d.maxResponseBytes()).isEqualTo(8 * 1024 * 1024);
        assertThat(d.kinds()).containsExactly(ProductSpec.RECORD_KIND);
        assertThat(d.kindRetention()).containsEntry(ProductSpec.RECORD_KIND, Duration.ofDays(7));
        assertThat(d.zeroResultsNormal()).isTrue();
        assertThat(d.licence()).isEqualTo("US-PD");
        assertThat(d.products()).extracting(ProductSpec::id)
                .containsExactly(MrmsAdapter.REFLECTIVITY, MrmsAdapter.PRECIP_RATE, MrmsAdapter.MESH, MrmsAdapter.ROTATION,
                        MrmsAdapter.QPE_1H, MrmsAdapter.QPE_24H, MrmsAdapter.FFG_RATIO,
                        // prompt 106 adds three more
                        "mrms-lightning-prob", "mrms-vil", "mrms-echo-top-50");
        ProductSpec reflectivity = d.products().get(0);
        assertThat(reflectivity.codes()).containsEntry(0, "noCoverage").containsEntry(1, "noEcho");
        assertThat(reflectivity.physical(166)).isEqualTo(50.0);
        assertThat(reflectivity.spatial().nativeMaxZoom()).isEqualTo(8);
        assertThat(reflectivity.spatial().maxZoomForAnimation()).isEqualTo(7);
        assertThat(reflectivity.temporal().maxGap()).isEqualTo(Duration.ofMinutes(6));
        assertThat(reflectivity.retention().summary()).isEqualTo("every frame for 6h, then 10m frames to 48h");
        assertThat(d.products().get(1).bands()).containsExactly("rate", "flag");
        assertThat(new MrmsAdapter(decoder, "").objectUri("CONUS/x.grib2.gz").toString())
                .isEqualTo("https://noaa-mrms-pds.s3.amazonaws.com/CONUS/x.grib2.gz");
        assertThat(MrmsAdapter.parseKeyTime("20261010-000041")).isEqualTo(t("2026-10-10T00:00:41Z"));
    }
}
