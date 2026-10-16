package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.Observation;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PositionSource;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code derived-hf-blackout} (prompt 099): the baseline math, and through ticks a planted spot drop on a
 * sunlit path against 14 days of the same hour (flagged while the source is healthy, not when it fails,
 * not at night), with several ticks per hour never polluting the baseline.
 */
class HfBlackoutTest {

    private static final String PSK = DerivedSources.PSKREPORTER;
    private static final Instant TODAY_NOON = Instant.parse("2026-10-15T12:00:00Z");

    @Test
    @DisplayName("the median is the middle value, or the mean of the two middle values")
    void median() {
        assertThat(HfBlackoutGenerator.median(List.of(1.0, 3.0, 2.0))).isEqualTo(2.0);
        assertThat(HfBlackoutGenerator.median(List.of(1.0, 2.0, 3.0, 4.0))).isEqualTo(2.5);
        assertThat(HfBlackoutGenerator.median(List.of())).isNaN();
    }

    @Test
    @DisplayName("the path class is the sender's Maidenhead field and the DXCC")
    void path() {
        Observation spot = new Observation("pskreporter-ft8", "s|r|14074|1", "radio-spot", Instant.EPOCH, Geo.point(0, 0),
                Map.of("band", "20m", "senderLocator", "EM13", "dxcc", "291", "snr", -12), PositionSource.UNKNOWN);
        assertThat(HfBlackoutGenerator.pathClass(spot)).isEqualTo("EM:291");
    }

    /** Replaces the current spots with {@code count} 20 m spots on the EM:291 path, ten minutes before {@code now}. */
    private static void spots(LayerHarness harness, Instant now, int count) {
        harness.clear(PSK);
        for (int i = 0; i < count; i++) {
            harness.put(PSK, "spot-" + now.getEpochSecond() + "-" + i, "radio-spot", now.minus(Duration.ofMinutes(10)),
                    -97.0, 30.0, "band", "20m", "senderLocator", "EM13", "dxcc", "291", "snr", -10);
        }
    }

    /**
     * Fourteen whole days of 20 spots every hour, ticked at half past each hour, before {@code today}'s
     * date. Ticking around the clock is what made the old 14-entry history forget the same hour of
     * earlier days.
     */
    private static void baseline(LayerHarness harness, HfBlackoutGenerator generator, Instant today) {
        Instant midnight = today.truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        for (int day = 14; day >= 1; day--) {
            for (int hour = 0; hour < 24; hour++) {
                Instant at = midnight.minus(Duration.ofDays(day)).plus(Duration.ofHours(hour)).plus(Duration.ofMinutes(30));
                spots(harness, at, 20);
                harness.tick(generator, at);
            }
        }
    }

    @Test
    @DisplayName("a 75% spot drop at noon against the 14-day noon baseline is flagged while the source is healthy")
    void plantedDropFlagged() {
        LayerHarness harness = new LayerHarness(TODAY_NOON);
        HfBlackoutGenerator generator = new HfBlackoutGenerator(harness.registry());
        baseline(harness, generator, TODAY_NOON);

        Instant now = TODAY_NOON.plus(Duration.ofMinutes(30));
        spots(harness, now, 5);
        List<NormalizedRecord> out = harness.tick(generator, now);
        assertThat(out).singleElement().satisfies(record -> {
            assertThat(record.externalId()).isEqualTo("20m:EM:291:12");
            assertThat(record.payload()).containsEntry("spots", 5).containsEntry("baselineSpots", 20L)
                    .containsEntry("spotDrop", true).containsEntry("sunlit", true);
        });
    }

    @Test
    @DisplayName("several ticks within the same hour compare against earlier days only")
    void ticksWithinTheHourDoNotPolluteTheBaseline() {
        LayerHarness harness = new LayerHarness(TODAY_NOON);
        HfBlackoutGenerator generator = new HfBlackoutGenerator(harness.registry());
        baseline(harness, generator, TODAY_NOON);

        Instant early = TODAY_NOON.plus(Duration.ofMinutes(10));
        spots(harness, early, 20);
        assertThat(harness.tick(generator, early)).isEmpty();
        Instant late = TODAY_NOON.plus(Duration.ofMinutes(50));
        spots(harness, late, 5);
        assertThat(harness.tick(generator, late)).hasSize(1);
    }

    @Test
    @DisplayName("the same drop with the source failing is not flagged")
    void failingSourceNotFlagged() {
        LayerHarness harness = new LayerHarness(TODAY_NOON);
        HfBlackoutGenerator generator = new HfBlackoutGenerator(harness.registry());
        baseline(harness, generator, TODAY_NOON);

        harness.world.statuses.put(PSK, HealthStatus.FAILING);
        Instant now = TODAY_NOON.plus(Duration.ofMinutes(30));
        spots(harness, now, 5);
        assertThat(harness.tick(generator, now)).isEmpty();
    }

    @Test
    @DisplayName("the same drop at night (02 UTC) is not flagged: only sunlit paths count")
    void nightNotFlagged() {
        Instant night = Instant.parse("2026-10-15T02:00:00Z");
        LayerHarness harness = new LayerHarness(night);
        HfBlackoutGenerator generator = new HfBlackoutGenerator(harness.registry());
        baseline(harness, generator, night);

        Instant now = night.plus(Duration.ofMinutes(30));
        spots(harness, now, 5);
        assertThat(harness.tick(generator, now)).isEmpty();
    }
}
