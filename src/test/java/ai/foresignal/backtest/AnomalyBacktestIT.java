package ai.foresignal.backtest;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.trigger.window.WindowSpec;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * No look-ahead in the anomaly backtest (prompt 103, invariant INT-2): the replay evaluates buckets
 * in order and builds each baseline from earlier buckets only, so a spike at the end of the range
 * never changes an earlier verdict. Exercises {@link AnomalyReplay}, the replay the rule backtest
 * runs for anomaly rules.
 */
class AnomalyBacktestIT {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    private static final UUID RULE = UUID.fromString("00000000-0000-0000-0000-000000000103");

    private static WindowSpec spec(String sensitivity) {
        return new WindowSpec(Duration.ofHours(1), Duration.ofMinutes(15), "entity", null, 500, null, null,
                Duration.ofHours(1), "hour-of-day", "up", sensitivity, 3, 1, 0.8);
    }

    /** {@code days} of hourly buckets for group {@code g}, alternating counts 9 and 11. */
    private static List<AnomalyReplay.Sample> steady(int days) {
        List<AnomalyReplay.Sample> out = new ArrayList<>();
        for (int h = 0; h < days * 24; h++) {
            out.add(new AnomalyReplay.Sample("g", T0.plus(Duration.ofHours(h)), h % 2 == 0 ? 9 : 11, 1.0, true,
                    false));
        }
        return out;
    }

    @Test
    @DisplayName("a spike at the end of the range does not change any earlier verdict")
    void noLookAhead() {
        Instant from = T0.plus(Duration.ofDays(10));
        Instant to = T0.plus(Duration.ofDays(14));
        List<AnomalyReplay.Sample> base = steady(14);
        List<AnomalyReplay.Sample> spiked = new ArrayList<>(base);
        AnomalyReplay.Sample last = spiked.removeLast();
        spiked.add(new AnomalyReplay.Sample("g", last.bucketStart(), 500, 1.0, true, false));

        AnomalyReplay.Outcome normal = AnomalyReplay.replay(base, spec("medium"), from, to);
        AnomalyReplay.Outcome withSpike = AnomalyReplay.replay(spiked, spec("medium"), from, to);

        int earlier = normal.verdicts().size() - 1;
        assertThat(withSpike.verdicts()).hasSize(normal.verdicts().size());
        assertThat(withSpike.verdicts().subList(0, earlier)).isEqualTo(normal.verdicts().subList(0, earlier));
        assertThat(normal.wouldFire()).isZero();
        assertThat(withSpike.verdicts().getLast().classification()).isEqualTo("activity-up");
        assertThat(withSpike.wouldFire()).isEqualTo(1);
    }

    @Test
    @DisplayName("samples after the range are never evaluated and never feed a baseline")
    void laterSamplesIgnored() {
        Instant from = T0.plus(Duration.ofDays(10));
        Instant to = T0.plus(Duration.ofDays(12));
        List<AnomalyReplay.Sample> base = steady(12);
        List<AnomalyReplay.Sample> withFuture = new ArrayList<>(base);
        for (int h = 0; h < 48; h++) {
            withFuture.add(new AnomalyReplay.Sample("g", to.plus(Duration.ofHours(h)), 1000, 1.0, true, false));
        }
        assertThat(AnomalyReplay.replay(withFuture, spec("medium"), from, to).verdicts())
                .isEqualTo(AnomalyReplay.replay(base, spec("medium"), from, to).verdicts());
    }

    @Test
    @DisplayName("the warm-up consumed is reported while the baseline learns")
    @SuppressWarnings("unchecked")
    void warmUp() {
        AnomalyReplay.Outcome outcome = AnomalyReplay.replay(steady(10), spec("medium"), T0, T0.plus(Duration.ofDays(10)));
        // hour-of-day needs 7 samples per slot for one week: the first 7 days of every hour learn.
        assertThat(outcome.learningBuckets()).isEqualTo(7 * 24);
        assertThat(outcome.byClassification()).containsEntry("learning", 7 * 24);
        Map<String, Object> result = outcome.toMap(Duration.ofHours(1), RULE, Map.of());
        assertThat(result).containsEntry("ruleType", "anomaly");
        assertThat((Map<String, Object>) result.get("anomaly")).containsEntry("warmupConsumed", "PT168H")
                .containsEntry("coverageDrops", 0L).containsKey("byClassification");
    }

    @Test
    @DisplayName("a variant changes only the detector parameters")
    void variantSensitivity() {
        WindowSpec patched = RuleBacktest.patchWindow(spec("medium"), Map.of("name", "low",
                "window", Map.of("sensitivity", "low", "bucket", "PT5M")));
        assertThat(patched.sensitivity()).isEqualTo("low");
        assertThat(patched.bucket()).isEqualTo(Duration.ofHours(1));
        assertThat(RuleBacktest.patchWindow(spec("high"), Map.of("name", "same"))).isEqualTo(spec("high"));
    }

    @Test
    @DisplayName("samples and topEntities carry the RuleBacktestResult fields, with the stored watermark as seq")
    @SuppressWarnings("unchecked")
    void specShapes() {
        Instant from = T0.plus(Duration.ofDays(10));
        Instant to = T0.plus(Duration.ofDays(14));
        List<AnomalyReplay.Sample> spiked = new ArrayList<>(steady(14));
        AnomalyReplay.Sample last = spiked.removeLast();
        spiked.add(new AnomalyReplay.Sample("g", last.bucketStart(), 500, 1.0, true, false));
        AnomalyReplay.Outcome outcome = AnomalyReplay.replay(spiked, spec("medium"), from, to);

        Map<String, Object> withWatermark = outcome.toMap(Duration.ofHours(1), RULE,
                Map.of(AnomalyReplay.key("g", last.bucketStart()), 4242L));
        List<Map<String, Object>> samples = (List<Map<String, Object>>) withWatermark.get("samples");
        assertThat(samples).containsExactly(Map.of("seq", 4242L, "source", "rule-windows",
                "externalId", RULE + ":g", "observedAt", last.bucketStart().toString(), "edge", "window"));
        List<Map<String, Object>> top = (List<Map<String, Object>>) withWatermark.get("topEntities");
        assertThat(top).hasSize(1);
        assertThat(top.getFirst()).containsOnlyKeys("sourceId", "externalId", "entityId", "count")
                .containsEntry("sourceId", "rule-windows").containsEntry("externalId", RULE + ":g")
                .containsEntry("entityId", null).containsEntry("count", 1);

        // Without a stored watermark there is no real seq: the hit is counted but not sampled.
        Map<String, Object> without = outcome.toMap(Duration.ofHours(1), RULE, Map.of());
        assertThat(without).containsEntry("wouldFire", 1L);
        assertThat((List<?>) without.get("samples")).isEmpty();
        assertThat((List<?>) without.get("topEntities")).hasSize(1);
    }
}
