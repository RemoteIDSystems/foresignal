package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.testsupport.raster.SyntheticRasterAdapter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The retention rules as pure functions (prompt 042, Weather-Imagery-Design §2.4). */
class FrameThinningTest {

    static final Instant NOW = Instant.parse("2026-10-10T14:00:00Z");
    static final ProductSpec OBS = SyntheticRasterAdapter.observationSpec();
    static final ProductSpec FCST = SyntheticRasterAdapter.forecastSpec();

    static RasterFrameRow obs(Instant t) {
        return new RasterFrameRow(OBS.id(), FrameKeys.observationFrameId(t), t, null, null, null, null, "g", "p", 1,
                "0".repeat(64), "u8", 1, Map.of(), null, RasterFrameRow.COMPLETE, false, false, t, null);
    }

    static RasterFrameRow fcst(Instant run, boolean complete) {
        return new RasterFrameRow(FCST.id(), FrameKeys.forecastFrameId(run, 0), run, run, 0, null, null, "g", "p", 1,
                "0".repeat(64), "i16", 1, Map.of(), null, RasterFrameRow.COMPLETE, false, complete, run, null);
    }

    @Test
    @DisplayName("a new frame expires at the first rung; forecasts and unladdered products never expire")
    void initialExpiry() {
        assertThat(FrameThinning.initialExpiry(OBS, NOW)).isEqualTo(NOW.plus(Duration.ofHours(6)));
        assertThat(FrameThinning.initialExpiry(FCST, NOW)).isNull();
        ProductSpec maxAgeOnly = ProductSpec.builder("m").retention(ProductSpec.RetentionLadder.maxAge(Duration.ofHours(72)))
                .build();
        assertThat(FrameThinning.initialExpiry(maxAgeOnly, NOW)).isEqualTo(NOW.plus(Duration.ofHours(72)));
        assertThat(FrameThinning.initialExpiry(ProductSpec.builder("n").build(), NOW)).isNull();
    }

    @Test
    @DisplayName("the MRMS ladder: every 2-min frame younger than 6 h stays; older ones thin to 10 min; past 48 h nothing")
    void mrmsLadder() {
        List<RasterFrameRow> frames = new ArrayList<>();
        for (Instant t = NOW.minus(Duration.ofHours(50)); t.isBefore(NOW); t = t.plus(Duration.ofMinutes(2))) {
            frames.add(obs(t));
        }
        FrameThinning.Plan plan = FrameThinning.plan(OBS, frames, NOW);
        List<RasterFrameRow> kept = frames.stream().filter(f -> !plan.delete().contains(f)).toList();
        assertThat(kept).allMatch(f -> !f.validTime().isBefore(NOW.minus(Duration.ofHours(48))));
        List<RasterFrameRow> young = kept.stream().filter(f -> !f.validTime().isBefore(NOW.minus(Duration.ofHours(6))))
                .toList();
        assertThat(young).hasSize(180);
        Instant settled = NOW.minus(Duration.ofHours(6)).minus(Duration.ofMinutes(5));
        List<RasterFrameRow> old = kept.stream().filter(f -> f.validTime().isBefore(NOW.minus(Duration.ofHours(6))))
                .toList();
        List<RasterFrameRow> settledOld = old.stream().filter(f -> f.validTime().isBefore(settled)).toList();
        assertThat(settledOld).allMatch(f -> f.validTime().getEpochSecond() % 600 == 0);
        assertThat(settledOld).hasSize(42 * 6);
        // Just past the rung, the frame nearest the next boundary survives until that boundary's frame ages in.
        assertThat(old).hasSize(42 * 6 + 1);
        assertThat(plan.thinned().keySet()).containsExactlyInAnyOrderElementsOf(old);
        assertThat(plan.thinned().values()).allMatch(e -> e != null);
    }

    @Test
    @DisplayName("forecasts keep the newest two complete runs and a run in progress newer than them")
    void forecastRuns() {
        List<RasterFrameRow> frames = List.of(fcst(NOW.minus(Duration.ofHours(18)), true),
                fcst(NOW.minus(Duration.ofHours(15)), false), fcst(NOW.minus(Duration.ofHours(12)), true),
                fcst(NOW.minus(Duration.ofHours(6)), true), fcst(NOW, false));
        FrameThinning.Plan plan = FrameThinning.plan(FCST, frames, NOW);
        assertThat(plan.delete()).extracting(RasterFrameRow::referenceTime)
                .containsExactlyInAnyOrder(NOW.minus(Duration.ofHours(18)), NOW.minus(Duration.ofHours(15)));
        assertThat(plan.thinned()).isEmpty();
    }

    @Test
    @DisplayName("ties go to the earlier frame, and a product without a ladder keeps everything")
    void tiesAndNoLadder() {
        RasterFrameRow before = obs(NOW.minus(Duration.ofHours(7)).minusSeconds(180));
        RasterFrameRow after = obs(NOW.minus(Duration.ofHours(7)).plusSeconds(180));
        assertThat(FrameThinning.nearestPerBoundary(List.of(before, after), Duration.ofMinutes(10)))
                .containsExactly(before);
        assertThat(FrameThinning.plan(ProductSpec.builder("n").build(), List.of(before), NOW).isEmpty()).isTrue();
    }
}
