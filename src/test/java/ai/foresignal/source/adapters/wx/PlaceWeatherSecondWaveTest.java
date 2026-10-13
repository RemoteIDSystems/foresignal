package ai.foresignal.source.adapters.wx;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.DefaultDerivedContext;
import ai.foresignal.derive.HeadSnapshot;
import ai.foresignal.raster.sample.FrameView;
import ai.foresignal.raster.sample.PlaceGridIndex;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.adapters.mrms.MrmsAdapter;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.raster.SampleFixtures;
import ai.foresignal.testsupport.weather.FakeDerived;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The prompt 053 parts of {@code wx-places} and {@code wx-areas} without a database: the precip section's merged
 * status, a disabled input leaving the section, {@code forecast.nws} from the NWS head (ok, stale, unavailable),
 * restoring the parts from a head, and the new {@code wx-areas} fields.
 */
class PlaceWeatherSecondWaveTest {

    static final Instant T = Instant.parse("2026-10-12T21:00:00Z");
    static final double LON = -98.0;
    static final double LAT = 30.5;

    FakeDerived world;
    WxPlacesGenerator generator;
    Map<String, ProductSpec> specs;

    @BeforeEach
    void setUp() {
        world = new FakeDerived(T.plusSeconds(30));
        world.addPlace("home", "Home", LON, LAT, 20, 1);
        generator = new WxPlacesGenerator();
        PlaceGridIndex.clearCache();
        specs = new LinkedHashMap<>();
        for (ProductSpec s : new MrmsAdapter(null, "").descriptor().products()) {
            specs.put(s.id(), s);
        }
    }

    private FrameView frame(String productId, Instant t, int code) {
        int[] codes = new int[SampleFixtures.TEXAS.nx() * SampleFixtures.TEXAS.ny()];
        Arrays.fill(codes, code);
        ProductSpec spec = specs.get(productId);
        if (spec.bands().size() > 1) {
            // mrms-precip-rate: band 2 is the PrecipFlag (0, no precipitation type).
            return SampleFixtures.bands(spec, SampleFixtures.TEXAS, SampleFixtures.row(productId, t, null, null), codes,
                    new int[codes.length]);
        }
        return SampleFixtures.codes(spec, SampleFixtures.TEXAS, codes, SampleFixtures.row(productId, t, null, null));
    }

    private NormalizedRecord reconcile(List<HeadSnapshot> heads) {
        DefaultDerivedContext ctx = world.context(generator.descriptor(), ValidatedParams.empty());
        generator.reconcile(heads, ctx);
        List<NormalizedRecord> out = world.collect(ctx);
        return out.get(out.size() - 1);
    }

    private NormalizedRecord tick(Instant now) {
        world.clock.setInstant(now);
        DefaultDerivedContext ctx = world.context(generator.descriptor(), ValidatedParams.empty());
        generator.onTick(now, ctx);
        List<NormalizedRecord> out = world.collect(ctx);
        return out.isEmpty() ? null : out.get(out.size() - 1);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> section(NormalizedRecord r, String... path) {
        Object o = r.payload();
        for (String p : path) {
            o = ((Map<String, Object>) o).get(p);
        }
        return (Map<String, Object>) o;
    }

    private static HeadSnapshot nwsHead(Instant updated) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("place", "Home");
        payload.put("popMax24h", 70);
        payload.put("qpf24hMm", 18.0);
        payload.put("gustMaxMs24h", 14.0);
        payload.put("forecastUpdated", updated.toString());
        return new HeadSnapshot("nws-gridpoints", "place:home", "nws-forecast", 7, 1, updated, false, payload, null);
    }

    @Test
    @DisplayName("precip: rate plus the radius maxima of QPE 1 h, QPE 24 h and the FFG ratio, whole units, all ok")
    void precipParts() {
        world.addFrame(MrmsAdapter.PRECIP_RATE, frame(MrmsAdapter.PRECIP_RATE, T, 50));
        world.addFrame(MrmsAdapter.QPE_1H, frame(MrmsAdapter.QPE_1H, T, 123));
        world.addFrame(MrmsAdapter.QPE_24H, frame(MrmsAdapter.QPE_24H, T, 456));
        world.addFrame(MrmsAdapter.FFG_RATIO, frame(MrmsAdapter.FFG_RATIO, T, 1050));

        Map<String, Object> precip = section(reconcile(List.of()), "precip");

        assertThat(precip).containsEntry("status", "ok").containsEntry("rateMmH", 5L).containsEntry("qpe1hMmMax", 12L)
                .containsEntry("qpe24hMmMax", 46L).containsEntry("ffgRatio1hPctMax", 105L).doesNotContainKey("asOf");
    }

    @Test
    @DisplayName("a stale part makes precip stale with that part's asOf and value; the 24-hour QPE is judged against 150 min")
    void stalePart() {
        world.addFrame(MrmsAdapter.PRECIP_RATE, frame(MrmsAdapter.PRECIP_RATE, T, 50));
        world.addFrame(MrmsAdapter.QPE_24H, frame(MrmsAdapter.QPE_24H, T.minus(Duration.ofMinutes(100)), 456));
        world.addFrame(MrmsAdapter.FFG_RATIO, frame(MrmsAdapter.FFG_RATIO, T, 1050));
        assertThat(section(reconcile(List.of()), "precip")).containsEntry("status", "ok");

        // 40 minutes on, with a fresh rate frame: the 24-hour QPE (140 min old) is still within its 150-minute gap,
        // the FFG ratio (40 min old) is past its 30.
        Instant later = T.plus(Duration.ofMinutes(40));
        world.addFrame(MrmsAdapter.PRECIP_RATE, frame(MrmsAdapter.PRECIP_RATE, later, 60));
        Map<String, Object> precip = section(tick(later.plusSeconds(30)), "precip");

        assertThat(precip).containsEntry("status", "stale").containsEntry("asOf", T.toString())
                .containsEntry("ffgRatio1hPctMax", 105L).containsEntry("qpe24hMmMax", 46L).containsEntry("rateMmH", 6L)
                .doesNotContainKey("qpe1hMmMax");
        assertThat(precip.get("statusText")).isNotNull();
    }

    @Test
    @DisplayName("an input whose product is disabled leaves the section: metric and status")
    void disabledPartLeaves() {
        world.addFrame(MrmsAdapter.PRECIP_RATE, frame(MrmsAdapter.PRECIP_RATE, T, 50));
        world.addFrame(MrmsAdapter.QPE_1H, frame(MrmsAdapter.QPE_1H, T, 123));
        assertThat(section(reconcile(List.of()), "precip")).containsEntry("qpe1hMmMax", 12L);

        world.unavailable.put(MrmsAdapter.QPE_1H, "product mrms-qpe-1h is disabled: by the operator");
        NormalizedRecord r = tick(T.plusSeconds(60));

        assertThat(section(r, "precip")).containsEntry("status", "ok").doesNotContainKey("qpe1hMmMax");
    }

    @Test
    @DisplayName("a failing source keeps the part's value and makes precip unavailable, which outranks stale")
    void severity() {
        assertThat(PlaceWeatherState.severity(WxSection.UNAVAILABLE)).isGreaterThan(PlaceWeatherState.severity(WxSection.NO_COVERAGE));
        assertThat(PlaceWeatherState.severity(WxSection.NO_COVERAGE)).isGreaterThan(PlaceWeatherState.severity(WxSection.STALE));
        assertThat(PlaceWeatherState.severity(WxSection.STALE)).isGreaterThan(PlaceWeatherState.severity(WxSection.OK));

        world.addFrame(MrmsAdapter.PRECIP_RATE, frame(MrmsAdapter.PRECIP_RATE, T, 50));
        world.addFrame(MrmsAdapter.FFG_RATIO, frame(MrmsAdapter.FFG_RATIO, T, 1050));
        reconcile(List.of());
        world.unavailable.put(MrmsAdapter.FFG_RATIO, "source mrms is failing");
        NormalizedRecord r = tick(T.plusSeconds(60));

        assertThat(section(r, "precip")).containsEntry("status", "unavailable").containsEntry("ffgRatio1hPctMax", 105L)
                .containsEntry("asOf", T.toString());
    }

    @Test
    @DisplayName("forecast.nws: unavailable without a record or with the source disabled, ok when fresh, stale after 6 h")
    void nwsSection() {
        assertThat(section(reconcile(List.of()), "forecast", "nws")).containsEntry("status", "unavailable")
                .doesNotContainKey("popMax24h");

        Instant updated = T.minus(Duration.ofHours(1));
        world.heads.put("nws-gridpoints", List.of(nwsHead(updated)));
        Map<String, Object> ok = section(tick(T.plusSeconds(60)), "forecast", "nws");
        assertThat(ok).containsEntry("status", "ok").containsEntry("popMax24h", 70).containsEntry("qpf24hMm", 18.0)
                .containsEntry("gustMaxMs24h", 14.0).doesNotContainKey("asOf");

        Map<String, Object> stale = section(tick(updated.plus(Duration.ofHours(6)).plusSeconds(60)), "forecast", "nws");
        assertThat(stale).containsEntry("status", "stale").containsEntry("popMax24h", 70)
                .containsEntry("asOf", updated.toString());

        world.statuses.put("nws-gridpoints", HealthStatus.DISABLED);
        Map<String, Object> off = section(tick(updated.plus(Duration.ofHours(7))), "forecast", "nws");
        assertThat(off).containsEntry("status", "unavailable").containsEntry("popMax24h", 70);
    }

    @Test
    @DisplayName("reconcile restores the precip parts and forecast.nws from the head")
    void restore() {
        Map<String, Object> precip = new LinkedHashMap<>();
        precip.put("status", "stale");
        precip.put("asOf", T.minus(Duration.ofHours(1)).toString());
        precip.put("rateMmH", 3);
        precip.put("qpe24hMmMax", 40);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("precip", precip);
        payload.put("forecast", Map.of("nws", Map.of("status", "ok", "popMax24h", 20)));
        PlaceWeatherState s = PlaceWeatherState.restore("home", "Home", 20, LON, LAT, 1, payload, T);

        assertThat(s.precip.metric("rateMmH")).isEqualTo(3);
        assertThat(s.precip.metrics()).doesNotContainKey("qpe24hMmMax");
        assertThat(s.qpe24h.metric("qpe24hMmMax")).isEqualTo(40);
        assertThat(s.qpe1h.metrics()).as("never had data").isNull();
        assertThat(s.nws.metric("popMax24h")).isEqualTo(20);
        assertThat(s.payload().get("precip")).isInstanceOf(Map.class);
        assertThat(PlaceWeatherState.precipPart(null, PlaceWeatherState.QPE_1H_KEY, false)).isNull();
    }

    @Test
    @DisplayName("wx-areas accepts rotation, qpe1h and ffg with their products, gaps and rounding")
    void areaFields() {
        assertThat(WxAreasGenerator.AreaDef.parse("rotation:0.01:aoi")).hasValueSatisfying(d -> {
            assertThat(d.productId()).isEqualTo("mrms-rotation-60m");
            assertThat(d.externalId()).isEqualTo("rotation:0.01:aoi");
            assertThat(d.maxGap()).isEqualTo(Duration.ofMinutes(6));
            assertThat(d.roundMax(0.01234)).isEqualTo(0.012);
        });
        assertThat(WxAreasGenerator.AreaDef.parse("qpe1h:25:home")).hasValueSatisfying(d -> {
            assertThat(d.productId()).isEqualTo("mrms-qpe-1h");
            assertThat(d.maxGap()).isEqualTo(Duration.ofMinutes(30));
            assertThat(d.roundMax(25.6)).isEqualTo(26L);
        });
        assertThat(WxAreasGenerator.AreaDef.parse("ffg:100:aoi")).hasValueSatisfying(d -> {
            assertThat(d.productId()).isEqualTo("mrms-ffg-ratio-1h");
            assertThat(d.maxGap()).isEqualTo(Duration.ofMinutes(30));
        });
        assertThat(new WxAreasGenerator().checkParams(ValidatedParams.of(Map.of(WxAreasGenerator.AREAS_PARAM,
                List.of("rotation:0.01:aoi", "qpe1h:25:aoi", "ffg:100:aoi"))))).isEmpty();
    }

    @Test
    @DisplayName("the generator subscribes to the new products and to nws-forecast records of nws-gridpoints")
    void inputs() {
        var in = generator.inputs(ValidatedParams.empty());
        assertThat(in.products()).contains(MrmsAdapter.QPE_1H, MrmsAdapter.QPE_24H, MrmsAdapter.FFG_RATIO);
        assertThat(in.sources()).contains("nws-gridpoints");
        assertThat(in.kinds()).contains("nws-forecast");
    }
}
