package ai.foresignal.source.adapters.wx;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.DefaultDerivedContext;
import ai.foresignal.raster.model.EcmwfDefinition;
import ai.foresignal.raster.sample.FrameView;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.testsupport.raster.SampleFixtures;
import ai.foresignal.testsupport.weather.FakeDerived;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The place-weather model-disagreement flag (prompt 105 §4, post-review; AIFS from prompt 106): like is compared
 * with like over the next 24 h, a run that does not reach now + 24 h (HRRR's 18-hour runs) is left out, AIFS
 * joins as an extra model, IFS precipitation is the 24-hour total ending near now + 24 h, and the IFS section
 * carries the ECMWF "changes made" attribution.
 */
class PlaceDisagreementTest {

    static final Instant RUN = Instant.parse("2026-10-15T00:00:00Z");
    static final Instant NOW = RUN.plus(Duration.ofHours(1));

    /** A run of constant {@code value} steps at {@code 0, every, 2·every, … ≤ lastHour}. */
    private static List<FrameView> run(String productId, int every, int lastHour, float value) {
        float[] field = new float[SampleFixtures.TEXAS.nx() * SampleFixtures.TEXAS.ny()];
        Arrays.fill(field, value);
        List<FrameView> steps = new ArrayList<>();
        for (int h = 0; h <= lastHour; h += every) {
            steps.add(SampleFixtures.physical(SampleFixtures.gusts(productId), SampleFixtures.TEXAS,
                    SampleFixtures.row(productId, RUN.plus(Duration.ofHours(h)), RUN, h), field.clone()));
        }
        return steps;
    }

    /** A run total growing by {@code perHour} mm each hour. */
    private static List<FrameView> total(String productId, int every, int lastHour, float perHour) {
        List<FrameView> steps = new ArrayList<>();
        for (int h = 0; h <= lastHour; h += every) {
            float[] field = new float[SampleFixtures.TEXAS.nx() * SampleFixtures.TEXAS.ny()];
            Arrays.fill(field, perHour * h);
            steps.add(SampleFixtures.physical(SampleFixtures.gusts(productId), SampleFixtures.TEXAS,
                    SampleFixtures.row(productId, RUN.plus(Duration.ofHours(h)), RUN, h), field));
        }
        return steps;
    }

    private static FakeDerived world() {
        FakeDerived world = new FakeDerived(NOW);
        world.addPlace("home", "Home", -98.0, 30.5, 20, 1);
        world.addRun("gfs-gust", RUN, run("gfs-gust", 1, 48, 15f));
        world.addRun("ifs-gust", RUN, run("ifs-gust", 3, 48, 26f));
        return world;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> forecast(FakeDerived world) {
        WxPlacesGenerator g = new WxPlacesGenerator();
        DefaultDerivedContext ctx = world.context(g.descriptor(), ValidatedParams.empty());
        g.reconcile(List.of(), ctx);
        NormalizedRecord r = world.collect(ctx).stream().filter(x -> x.externalId().equals("place:home")).findFirst()
                .orElseThrow();
        return (Map<String, Object>) r.payload().get("forecast");
    }

    @Test
    @DisplayName("an 18-hour HRRR run is left out; GFS and IFS gusts 11 m/s apart disagree")
    void hrrrBeyondItsHorizonIsLeftOut() {
        FakeDerived world = world();
        world.addRun("hrrr-gust", RUN, run("hrrr-gust", 1, 18, 40f));
        Map<String, Object> disagreement = asMap(forecast(world).get("disagreement"));
        assertThat(disagreement.get("fields")).isEqualTo(List.of("gust"));
        assertThat(disagreement.get("models")).isEqualTo(List.of("gfs", "ifs"));
        assertThat(asMap(disagreement.get("spread"))).containsEntry("gust", 11.0);
    }

    @Test
    @DisplayName("a 48-hour HRRR run covers the window and joins the comparison")
    void longHrrrJoins() {
        FakeDerived world = world();
        world.addRun("hrrr-gust", RUN, run("hrrr-gust", 1, 48, 40f));
        Map<String, Object> disagreement = asMap(forecast(world).get("disagreement"));
        assertThat(disagreement.get("models")).isEqualTo(List.of("gfs", "hrrr", "ifs"));
        assertThat(asMap(disagreement.get("spread"))).containsEntry("gust", 25.0);
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("AIFS joins t2m; next-24-hour totals are compared (GFS and AIFS run totals, IFS 24-hour product)")
    void aifsAndPrecipitation() {
        FakeDerived world = world();
        world.addRun("gfs-t2m", RUN, run("gfs-t2m", 1, 48, 300f));
        world.addRun("aifs-t2m", RUN, run("aifs-t2m", 6, 48, 306f));
        world.addRun("gfs-apcp", RUN, total("gfs-apcp", 1, 48, 1f));
        world.addRun("aifs-tp", RUN, total("aifs-tp", 6, 48, 2f));
        world.addRun("ifs-precip-24h", RUN, run("ifs-precip-24h", 3, 48, 24f));
        Map<String, Object> forecast = forecast(world);
        Map<String, Object> disagreement = asMap(forecast.get("disagreement"));
        assertThat((List<String>) disagreement.get("fields")).contains("t2mMax", "precip24h");
        assertThat((List<String>) disagreement.get("models")).contains("aifs");
        // GFS 25 − 1 = 24 mm, IFS 24 mm (the f024 24-hour amount), AIFS 48 − 0 = 48 mm: spread 24 over the
        // max(15 mm, half the 32 mm mean) threshold.
        double precipSpread = ((Number) asMap(disagreement.get("spread")).get("precip24h")).doubleValue();
        assertThat(precipSpread).isGreaterThanOrEqualTo(15.0);
        Map<String, Object> ifs = asMap(forecast.get("ifs"));
        assertThat(ifs).containsEntry("precip24hMm", 24L).containsEntry("attribution", EcmwfDefinition.CHANGES_MADE);
    }

    @Test
    @DisplayName("coversWindow and next24h pick the run reach and the step ending at or before now + 24 h")
    void helpers() {
        FakeDerived world = world();
        world.addRun("hrrr-gust", RUN, run("hrrr-gust", 1, 18, 40f));
        WxPlacesGenerator g = new WxPlacesGenerator();
        DefaultDerivedContext ctx = world.context(g.descriptor(), ValidatedParams.empty());
        assertThat(WxPlacesGenerator.coversWindow(ctx, "hrrr-gust", RUN, NOW)).isFalse();
        assertThat(WxPlacesGenerator.coversWindow(ctx, "gfs-gust", RUN, NOW)).isTrue();
        assertThat(WxPlacesGenerator.coversWindow(ctx, "gfs-gust", RUN, RUN.plus(Duration.ofHours(30)))).isFalse();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o) {
        assertThat(o).isInstanceOf(Map.class);
        return (Map<String, Object>) o;
    }
}
