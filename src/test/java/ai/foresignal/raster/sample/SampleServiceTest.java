package ai.foresignal.raster.sample;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.raster.ElgCodec;
import ai.foresignal.raster.EncodedFrame;
import ai.foresignal.raster.FrameEncoder;
import ai.foresignal.raster.FrameResolver;
import ai.foresignal.raster.FrameStore;
import ai.foresignal.raster.GridDef;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.ProductStatusTracker;
import ai.foresignal.raster.RasterFrameQueries;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.lightning.FlashStore;
import ai.foresignal.raster.render.RasterRenderService;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.DecodedBands;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.raster.SampleFixtures;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;

/**
 * {@link SampleService} without a database (prompt 052): every status, nearest versus bilinear, MRMS code 0 as
 * {@code no-coverage}, relative times, {@code localTime} in the place's zone, and u/v derivation.
 */
class SampleServiceTest {

    static final Instant T = Instant.parse("2026-10-10T14:00:00Z");
    static final GridDefinition GRID = GridDefinition.latLon(4, 3, -100.0, 28.0, -94.0, 34.0);
    static final String OBS = "obs";
    static final String GUST = "gust";
    static final String WIND = "wind";

    MutableClock clock;
    ProductRegistry registry;
    ProductStatusTracker status;
    RasterFrameQueries frames;
    FrameStore store;
    RasterRenderService renderer;
    HealthService health;
    SampleService service;
    final Map<String, ElgCodec.Decoded> files = new HashMap<>();

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        clock = new MutableClock(T.plusSeconds(60));
        registry = mock(ProductRegistry.class);
        status = new ProductStatusTracker(mock(ApplicationEventPublisher.class));
        frames = mock(RasterFrameQueries.class);
        store = mock(FrameStore.class);
        renderer = mock(RasterRenderService.class);
        health = mock(HealthService.class);
        ObjectProvider<HealthService> healthProvider = mock(ObjectProvider.class);
        when(healthProvider.getIfAvailable()).thenReturn(health);
        ObjectProvider<FlashStore> flashes = mock(ObjectProvider.class);
        SourceHealthView healthy = view(HealthStatus.HEALTHY);
        when(health.health(anyString())).thenReturn(healthy);
        when(renderer.projection(anyString())).thenReturn(GridDef.of(GRID));
        when(store.exists(anyString())).thenAnswer(i -> files.containsKey(i.getArgument(0, String.class)));
        when(store.read(anyString())).thenAnswer(i -> files.get(i.getArgument(0, String.class)));
        FrameResolver resolver = new FrameResolver(new FrameResolver.FrameLookup() {
            @Override
            public Optional<RasterFrameRow> newest(String productId, boolean includePartial) {
                return frames.newest(productId, includePartial);
            }

            @Override
            public Optional<RasterFrameRow> newestAtOrBefore(String productId, Instant at, boolean includePartial) {
                return frames.newestAtOrBefore(productId, at, includePartial);
            }

            @Override
            public List<RasterFrameRow> atValidTime(String productId, Instant validTime) {
                return frames.atValidTime(productId, validTime);
            }

            @Override
            public Optional<Instant> earliest(String productId) {
                return Optional.empty();
            }

            @Override
            public List<RasterFrameQueries.RunSummary> runs(String productId) {
                return frames.runs(productId);
            }

            @Override
            public List<RasterFrameRow> runFrames(String productId, Instant run, boolean includePartial) {
                return frames.runFrames(productId, run, includePartial);
            }
        });
        register(SampleFixtures.reflectivity(OBS), "src-obs");
        register(SampleFixtures.gusts(GUST), "src-model");
        register(SampleFixtures.wind(WIND), "src-model");
        service = new SampleService(registry, status, frames, store, resolver, renderer, healthProvider, flashes, clock);
    }

    private void register(ProductSpec spec, String source) {
        when(registry.find(spec.id())).thenReturn(Optional.of(new ProductRegistry.Registered(spec, source, false)));
    }

    private static SourceHealthView view(HealthStatus s) {
        SourceHealthView v = mock(SourceHealthView.class);
        when(v.status()).thenReturn(s);
        return v;
    }

    /** Stores an observation frame of codes at {@code t}. */
    private RasterFrameRow observation(Instant t, int[] codes) {
        ProductSpec spec = SampleFixtures.reflectivity(OBS);
        EncodedFrame e = FrameEncoder.encode(spec, GRID, DecodedBands.codes(codes), false);
        String path = "frames/" + OBS + "/" + t.toEpochMilli() + ".fsg";
        files.put(path, new ElgCodec.Decoded(e.header(), e.decoded()));
        return row(OBS, t, null, null, path);
    }

    private RasterFrameRow forecast(String id, ProductSpec spec, Instant run, int step, float[]... bands) {
        EncodedFrame e = FrameEncoder.encode(spec, GRID, DecodedBands.physical(bands), false);
        String path = "frames/" + id + "/" + run.toEpochMilli() + "-" + step + ".fsg";
        files.put(path, new ElgCodec.Decoded(e.header(), e.decoded()));
        return row(id, run.plusSeconds(3600L * step), run, step, path);
    }

    private static RasterFrameRow row(String id, Instant valid, Instant run, Integer step, String path) {
        return new RasterFrameRow(id, run == null ? "o" + valid.toEpochMilli() : "f" + step, valid, run, step, null, null,
                "g1", path, 10, "0123456789abcdef0123456789abcdef", "u8", 1, Map.of(), null, RasterFrameRow.COMPLETE,
                false, run != null, valid, null);
    }

    private static final int[] CODES = {0, 0, 0, 0, 1, SampleFixtures.dbz(52.5), 86, 106, 1, 1, 126, 2};

    private SampleValue sample(String product, double lon, double lat, TimeSpec time) {
        return service.value(registry.find(product).orElseThrow(),
                new SampleService.Point(lon, lat, null, ZoneOffset.UTC), time, null, false, clock.instant());
    }

    @Test
    @DisplayName("ok: the nearest cell's physical value, units, label, frame key, age and attribution")
    void okNearest() {
        RasterFrameRow frame = observation(T, CODES);
        when(frames.newest(OBS, false)).thenReturn(Optional.of(frame));
        // Row 1, column 1 of the 4 × 3 grid: center −97.75, 31.0.
        SampleValue v = sample(OBS, -97.75, 31.0, TimeSpec.LATEST);
        assertThat(v.status()).isEqualTo(SampleStatus.OK);
        assertThat(v.value()).isEqualTo(52.5);
        assertThat(v.units()).isEqualTo("dBZ");
        assertThat(v.label()).isEqualTo("52.5 dBZ");
        assertThat(v.method()).isEqualTo("nearest");
        assertThat(v.frameKey()).isEqualTo(frame.frameKey());
        assertThat(v.validTime()).isEqualTo(T);
        assertThat(v.ageMinutes()).isEqualTo(1.0);
        assertThat(v.statusText()).isNull();
        assertThat(v.attribution()).containsEntry("licence", "US-PD").containsKey("requiredDisplay");
        assertThat(v.toMap()).containsEntry("status", "ok").containsEntry("coverage", "CONUS");
    }

    @Test
    @DisplayName("a named special code is a value with its meaning as the label (no echo)")
    void namedCode() {
        when(frames.newest(OBS, false)).thenReturn(Optional.of(observation(T, CODES)));
        SampleValue v = sample(OBS, -99.25, 31.0, TimeSpec.LATEST);
        assertThat(v.status()).isEqualTo(SampleStatus.OK);
        assertThat(v.label()).isEqualTo("no echo");
    }

    @Test
    @DisplayName("MRMS code 0 is no-coverage with the not-clear text; off the grid too")
    void noCoverage() {
        when(frames.newest(OBS, false)).thenReturn(Optional.of(observation(T, CODES)));
        SampleValue code0 = sample(OBS, -99.25, 33.0, TimeSpec.LATEST);
        assertThat(code0.status()).isEqualTo(SampleStatus.NO_COVERAGE);
        assertThat(code0.value()).isNull();
        assertThat(code0.statusText()).isEqualTo("outside product coverage; this is not evidence of clear weather");
        SampleValue off = sample(OBS, -80.0, 31.0, TimeSpec.LATEST);
        assertThat(off.status()).isEqualTo(SampleStatus.NO_COVERAGE);
        assertThat(off.statusText()).contains("not evidence of clear weather");
    }

    @Test
    @DisplayName("nodata inside coverage is no-data")
    void noData() {
        // The bilinear gust product: its nodata code (-32768) comes from NaN samples.
        float[] gust = new float[12];
        java.util.Arrays.fill(gust, Float.NaN);
        Instant run = T.minusSeconds(3600);
        RasterFrameRow step = forecast(GUST, SampleFixtures.gusts(GUST), run, 1, gust);
        when(frames.runs(GUST)).thenReturn(List.of(new RasterFrameQueries.RunSummary(run, true, List.of(1), T, T, 0)));
        when(frames.runFrames(eq(GUST), eq(run), anyBoolean())).thenReturn(List.of(step));
        SampleValue v = sample(GUST, -97.75, 31.0, TimeSpec.LATEST);
        assertThat(v.status()).isEqualTo(SampleStatus.NO_DATA);
        assertThat(v.statusText()).isNotBlank();
    }

    @Test
    @DisplayName("stale: older than maxGap at the requested time, value withheld")
    void stale() {
        when(frames.newest(OBS, false)).thenReturn(Optional.of(observation(T, CODES)));
        clock.setInstant(T.plusSeconds(7 * 60));
        SampleValue v = sample(OBS, -97.75, 31.0, TimeSpec.LATEST);
        assertThat(v.status()).isEqualTo(SampleStatus.STALE);
        assertThat(v.value()).isNull();
        assertThat(v.statusText()).contains("maximum gap");
        assertThat(v.frameKey()).isNotNull();
    }

    @Test
    @DisplayName("unavailable: no frame, product disabled, source failing, frame expired")
    void unavailable() {
        when(frames.newest(OBS, false)).thenReturn(Optional.empty());
        assertThat(sample(OBS, -97.75, 31.0, TimeSpec.LATEST).status()).isEqualTo(SampleStatus.UNAVAILABLE);

        RasterFrameRow frame = observation(T, CODES);
        when(frames.newest(OBS, false)).thenReturn(Optional.of(frame));
        status.disable(OBS, "src-obs", "decoder self-test failed", T);
        SampleValue disabled = sample(OBS, -97.75, 31.0, TimeSpec.LATEST);
        assertThat(disabled.status()).isEqualTo(SampleStatus.UNAVAILABLE);
        assertThat(disabled.statusText()).contains("disabled");
        status.enable(OBS, "src-obs");

        SourceHealthView failing = view(HealthStatus.FAILING);
        when(health.health("src-obs")).thenReturn(failing);
        service.invalidateHealth();
        assertThat(sample(OBS, -97.75, 31.0, TimeSpec.LATEST).statusText()).contains("failing");
        SourceHealthView degraded = view(HealthStatus.DEGRADED);
        when(health.health("src-obs")).thenReturn(degraded);
        service.invalidateHealth();
        assertThat(sample(OBS, -97.75, 31.0, TimeSpec.LATEST).status()).isEqualTo(SampleStatus.OK);

        files.clear();
        SampleValue expired = sample(OBS, -97.75, 31.0, TimeSpec.LATEST);
        assertThat(expired.status()).isEqualTo(SampleStatus.UNAVAILABLE);
        assertThat(expired.statusText()).contains("expired");
    }

    @Test
    @DisplayName("relative times: a past offset picks the frame at or before it; a future offset on an observation product is unavailable")
    void relativeTimes() {
        RasterFrameRow old = observation(T.minusSeconds(3600), CODES);
        when(frames.newestAtOrBefore(eq(OBS), any(), eq(false))).thenReturn(Optional.of(old));
        SampleValue past = sample(OBS, -97.75, 31.0, TimeSpec.parse("time", "-PT1H"));
        assertThat(past.status()).isEqualTo(SampleStatus.OK);
        assertThat(past.validTime()).isEqualTo(T.minusSeconds(3600));
        SampleValue future = sample(OBS, -97.75, 31.0, TimeSpec.parse("time", "+PT6H"));
        assertThat(future.status()).isEqualTo(SampleStatus.UNAVAILABLE);
        assertThat(future.statusText()).contains("observation product");
    }

    @Test
    @DisplayName("a future offset on a forecast product uses the newest complete run's step at or before it; bilinear")
    void forecastBilinear() {
        Instant run = T.minusSeconds(3600);
        float[] g0 = {10, 20, 30, 40, 10, 20, 30, 40, 10, 20, 30, 40};
        float[] g1 = {11, 21, 31, 41, 11, 21, 31, 41, 11, 21, 31, 41};
        RasterFrameRow s0 = forecast(GUST, SampleFixtures.gusts(GUST), run, 0, g0);
        RasterFrameRow s6 = forecast(GUST, SampleFixtures.gusts(GUST), run, 6, g1);
        when(frames.runs(GUST)).thenReturn(List.of(new RasterFrameQueries.RunSummary(run, true, List.of(0, 6),
                run, run.plusSeconds(6 * 3600), 0)));
        when(frames.runFrames(eq(GUST), eq(run), anyBoolean())).thenReturn(List.of(s0, s6));
        // Halfway between columns 1 (20) and 2 (30) of step 6 (21, 31): 26.
        SampleValue v = sample(GUST, -97.0, 31.0, TimeSpec.parse("time", "+PT6H"));
        assertThat(v.status()).isEqualTo(SampleStatus.OK);
        assertThat(v.method()).isEqualTo("bilinear");
        assertThat(v.step()).isEqualTo(6);
        assertThat(v.run()).isNotNull();
        assertThat(v.value()).isCloseTo(26.0, org.assertj.core.data.Offset.offset(0.01));
        assertThat(v.ageMinutes()).isNegative();
    }

    @Test
    @DisplayName("localTime is the valid time in the place's zone, else UTC")
    void localTime() {
        when(frames.newest(OBS, false)).thenReturn(Optional.of(observation(T, CODES)));
        SampleValue chicago = service.value(registry.find(OBS).orElseThrow(),
                new SampleService.Point(-97.75, 31.0, "home", ZoneId.of("America/Chicago")), TimeSpec.LATEST, null,
                false, clock.instant());
        assertThat(chicago.localTime()).isEqualTo("2026-10-10T09:00-05:00");
        SampleValue utc = sample(OBS, -97.75, 31.0, TimeSpec.LATEST);
        assertThat(utc.localTime()).isEqualTo("2026-10-10T14:00Z");
    }

    @Test
    @DisplayName("u/v products return u, v and derived speed, direction and compass, with no value")
    void windDerivation() {
        Instant run = T.minusSeconds(3600);
        float[] u = new float[12];
        float[] v = new float[12];
        java.util.Arrays.fill(u, 10f);
        java.util.Arrays.fill(v, 0f);
        RasterFrameRow s1 = forecast(WIND, SampleFixtures.wind(WIND), run, 1, u, v);
        when(frames.runs(WIND)).thenReturn(List.of(new RasterFrameQueries.RunSummary(run, true, List.of(1), T, T, 0)));
        when(frames.runFrames(eq(WIND), eq(run), anyBoolean())).thenReturn(List.of(s1));
        SampleValue w = sample(WIND, -97.75, 31.0, TimeSpec.NOW);
        assertThat(w.status()).isEqualTo(SampleStatus.OK);
        assertThat(w.value()).isNull();
        assertThat(w.u()).isEqualTo(10.0);
        assertThat(w.v()).isEqualTo(0.0);
        // A westerly: blowing from 270°.
        assertThat(w.derived()).containsEntry("speedMs", 10.0).containsEntry("fromDeg", 270).containsEntry("compass", "W");
    }

    @Test
    @DisplayName("imperial units convert values and labels")
    void imperial() {
        Instant run = T.minusSeconds(3600);
        float[] g = new float[12];
        java.util.Arrays.fill(g, 10f);
        RasterFrameRow s1 = forecast(GUST, SampleFixtures.gusts(GUST), run, 1, g);
        when(frames.runs(GUST)).thenReturn(List.of(new RasterFrameQueries.RunSummary(run, true, List.of(1), T, T, 0)));
        when(frames.runFrames(eq(GUST), eq(run), anyBoolean())).thenReturn(List.of(s1));
        SampleValue v = service.value(registry.find(GUST).orElseThrow(),
                new SampleService.Point(-97.75, 31.0, null, ZoneOffset.UTC), TimeSpec.NOW, null, true, clock.instant());
        assertThat(v.units()).isEqualTo("mph");
        assertThat(v.value()).isCloseTo(22.369, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("time expressions parse; bad ones are 400")
    void timeSpecs() {
        assertThat(TimeSpec.parse("time", null)).isEqualTo(TimeSpec.LATEST);
        assertThat(TimeSpec.parse("time", "now")).isEqualTo(TimeSpec.NOW);
        assertThat(TimeSpec.parse("time", "-PT1H").target(T)).isEqualTo(T.minusSeconds(3600));
        assertThat(TimeSpec.parse("time", "+PT6H").future(T)).isTrue();
        assertThat(TimeSpec.parse("time", "2026-10-10T12:00:00Z").target(T)).isEqualTo(Instant.parse("2026-10-10T12:00:00Z"));
        assertThatThrownBy(() -> TimeSpec.parse("time", "yesterday")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> TimeSpec.parse("time", "+P30D")).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("products are resolved in order; unknown and hidden ones are 404; too many are 400")
    void products() {
        assertThat(service.resolveProducts(List.of(OBS, GUST, OBS), Set.of())).extracting(ProductRegistry.Registered::id)
                .containsExactly(OBS, GUST);
        assertThatThrownBy(() -> service.resolveProducts(List.of(OBS), Set.of("src-obs")))
                .hasMessageContaining(OBS);
        when(registry.find("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.resolveProducts(List.of("nope"), Set.of())).hasMessageContaining("nope");
        List<String> many = new java.util.ArrayList<>();
        for (int i = 0; i < 21; i++) {
            many.add("p" + i);
        }
        assertThatThrownBy(() -> service.resolveProducts(many, Set.of())).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("the compass has 16 points")
    void compass() {
        assertThat(SampleService.compass(0)).isEqualTo("N");
        assertThat(SampleService.compass(247.5)).isEqualTo("WSW");
        assertThat(SampleService.compass(359)).isEqualTo("N");
        assertThat(SampleService.compass(90)).isEqualTo("E");
    }
}
