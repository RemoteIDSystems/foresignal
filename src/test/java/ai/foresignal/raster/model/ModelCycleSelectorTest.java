package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.raster.RasterProfile;
import ai.foresignal.source.adapters.gfs.GfsAdapter;
import ai.foresignal.source.adapters.hrrr.HrrrAdapter;
import ai.foresignal.test.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Candidate cycles, object keys and cycle keys (prompt 051; God's Eye View {@code catalog.js}). */
class ModelCycleSelectorTest {

    static final Duration SIX = Duration.ofHours(6);

    @Test
    @DisplayName("God's Eye View case: 04z with a 5 h lag selects the previous day's 18z cycle")
    void gevCase() {
        assertThat(ModelCycleSelector.latest(Instant.parse("2026-09-15T04:00:00Z"), SIX, Duration.ofHours(5)))
                .isEqualTo(Instant.parse("2026-09-14T18:00:00Z"));
    }

    @Test
    @DisplayName("the GFS lag of 3.5 h decides which cycle is a candidate, and candidates go back by the cadence")
    void lag() {
        MutableClock clock = new MutableClock(Instant.parse("2026-10-12T15:29:00Z"));
        assertThat(ModelCycleSelector.candidates(clock.instant(), GfsAdapter.RUN_EVERY, GfsAdapter.AVAILABILITY_LAG, 2))
                .containsExactly(Instant.parse("2026-10-12T06:00:00Z"), Instant.parse("2026-10-12T00:00:00Z"));
        clock.setInstant(Instant.parse("2026-10-12T15:30:00Z"));
        assertThat(ModelCycleSelector.latest(clock.instant(), GfsAdapter.RUN_EVERY, GfsAdapter.AVAILABILITY_LAG))
                .isEqualTo(Instant.parse("2026-10-12T12:00:00Z"));
    }

    @Test
    @DisplayName("cycles roll over at 00z into the previous day")
    void rollover() {
        Instant now = Instant.parse("2026-10-13T00:40:00Z");
        assertThat(ModelCycleSelector.candidates(now, Duration.ofHours(3), Duration.ofMinutes(50), 3)).containsExactly(
                Instant.parse("2026-10-12T21:00:00Z"), Instant.parse("2026-10-12T18:00:00Z"),
                Instant.parse("2026-10-12T15:00:00Z"));
        assertThat(ModelCycleSelector.latest(Instant.parse("2026-10-13T00:50:00Z"), Duration.ofHours(3),
                Duration.ofMinutes(50))).isEqualTo(Instant.parse("2026-10-13T00:00:00Z"));
    }

    @Test
    @DisplayName("the small profile runs HRRR every 6 h, the large every 3 h")
    void hrrrProfileCadence() {
        assertThat(RasterProfile.SMALL.hrrrRunEvery()).isEqualTo(SIX);
        assertThat(RasterProfile.LARGE.hrrrRunEvery()).isEqualTo(Duration.ofHours(3));
        Instant now = Instant.parse("2026-10-12T16:00:00Z");
        assertThat(ModelCycleSelector.latest(now, RasterProfile.SMALL.hrrrRunEvery(), HrrrAdapter.AVAILABILITY_LAG))
                .isEqualTo(Instant.parse("2026-10-12T12:00:00Z"));
        assertThat(ModelCycleSelector.latest(now, RasterProfile.LARGE.hrrrRunEvery(), HrrrAdapter.AVAILABILITY_LAG))
                .isEqualTo(Instant.parse("2026-10-12T15:00:00Z"));
    }

    @Test
    @DisplayName("cadences must be whole hours dividing 24")
    void cadence() {
        assertThat(ModelCycleSelector.requireCadence(Duration.ofHours(1))).isEqualTo(1);
        for (Duration bad : List.of(Duration.ofHours(5), Duration.ofMinutes(90), Duration.ZERO, Duration.ofHours(48))) {
            assertThatThrownBy(() -> ModelCycleSelector.requireCadence(bad)).isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> ModelCycleSelector.requireCadence(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("object keys fill {yyyyMMdd}, {HH}, {FFF} and {FF} (God's Eye View gfsObjectKey)")
    void objectKeys() {
        Instant cycle = Instant.parse("2026-09-14T06:00:00Z");
        assertThat(ModelCycleSelector.objectKey(GfsAdapter.DEFAULT_TEMPLATE, cycle, 3))
                .isEqualTo("gfs.20260914/06/atmos/gfs.t06z.pgrb2.0p25.f003");
        assertThat(ModelCycleSelector.objectKey(HrrrAdapter.DEFAULT_TEMPLATE, Instant.parse("2026-10-12T12:00:00Z"), 1))
                .isEqualTo("hrrr.20261012/conus/hrrr.t12z.wrfsfcf01.grib2");
        assertThat(ModelCycleSelector.objectKey("v17/{yyyyMMdd}{HH}/f{FFF}", cycle, 120)).isEqualTo("v17/2026091406/f120");
    }

    @Test
    @DisplayName("cycle keys round-trip; anything else parses to null")
    void cycleKeys() {
        Instant cycle = Instant.parse("2026-10-12T18:00:00Z");
        assertThat(ModelCycleSelector.cycleKey(cycle)).isEqualTo("2026101218Z");
        assertThat(ModelCycleSelector.parseCycleKey("2026101218Z")).isEqualTo(cycle);
        assertThat(ModelCycleSelector.parseCycleKey("2026101299Z")).isNull();
        assertThat(ModelCycleSelector.parseCycleKey("garbage")).isNull();
        assertThat(ModelCycleSelector.parseCycleKey(null)).isNull();
    }
}
