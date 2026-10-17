package ai.foresignal.precursor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.backtest.PrecursorTester;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The pure rules of the precursor library (prompt 120): validation and staleness, the lifecycle, the shrunk live
 * probability, the deterministic description, candidate keys, pre-whitening and the miner's helpers.
 */
class PrecursorRulesTest {

    static PrecursorTester.Result result(long n, long hits, double base, double lift, double p, String verdict,
            boolean insufficient) {
        double rate = n == 0 ? 0 : (double) hits / n;
        PrecursorTester.Direction reverse = new PrecursorTester.Direction(n, n, 0, 0, base, 0, 1, List.of());
        return new PrecursorTester.Result(n, hits, rate, ai.foresignal.analytics.math.Wilson.interval(hits, n), 60,
                600, 1440, 2000, 2880, 1500, base, lift, p, verdict, false, insufficient, PrecursorTester.NOTE, n,
                reverse, 1L);
    }

    @Test
    @DisplayName("event validation: notable, lift ≥ 1.5 and the Wilson lower bound above the base rate")
    void eventValidation() {
        assertThat(PrecursorRules.eventValidationFailure(result(20, 18, 0.2, 4.5, 0.001, "notable", false))).isNull();
        assertThat(PrecursorRules.eventValidationFailure(result(20, 18, 0.2, 4.5, 0.2, "weak", false)))
                .contains("weak");
        assertThat(PrecursorRules.eventValidationFailure(result(20, 7, 0.25, 1.4, 0.01, "notable", false)))
                .contains("lift");
        assertThat(PrecursorRules.eventValidationFailure(result(10, 5, 0.3, 1.67, 0.04, "notable", false)))
                .contains("interval");
        assertThat(PrecursorRules.eventValidationFailure(result(5, 5, 0.1, 10, 0.01, "insufficient", true)))
                .contains("insufficient");
    }

    @Test
    @DisplayName("event staleness: lift below 1.2 or the interval including the base rate")
    void eventStale() {
        assertThat(PrecursorRules.eventStaleReason(result(20, 18, 0.2, 4.5, 0.001, "notable", false))).isNull();
        assertThat(PrecursorRules.eventStaleReason(result(20, 4, 0.2, 1.0, 0.5, "no-evidence-of-precedence", false)))
                .contains("lift");
        assertThat(PrecursorRules.eventStaleReason(result(10, 4, 0.3, 1.33, 0.2, "no-evidence-of-precedence", false)))
                .contains("interval");
    }

    @Test
    @DisplayName("series validation and staleness: the same sign and p ≤ 0.05")
    void series() {
        LeadLagCcf.Result positive = new LeadLagCcf.Result(200, 14, 7, new LeadLagCcf.Direction(3, 0.5, 0.5, 0.002),
                new LeadLagCcf.Direction(1, 0.1, 0.1, 0.4), false, false);
        LeadLagCcf.Result negative = new LeadLagCcf.Result(100, 14, 7, new LeadLagCcf.Direction(3, -0.4, 0.4, 0.01),
                new LeadLagCcf.Direction(1, 0.1, 0.1, 0.4), false, false);
        LeadLagCcf.Result weak = new LeadLagCcf.Result(100, 14, 7, new LeadLagCcf.Direction(3, 0.2, 0.2, 0.3),
                new LeadLagCcf.Direction(1, 0.1, 0.1, 0.4), false, false);
        assertThat(PrecursorRules.seriesValidationFailure(positive, positive)).isNull();
        assertThat(PrecursorRules.seriesValidationFailure(positive, negative)).contains("sign");
        assertThat(PrecursorRules.seriesValidationFailure(positive, weak)).contains("p-value");
        assertThat(PrecursorRules.seriesStaleReason("positive", positive)).isNull();
        assertThat(PrecursorRules.seriesStaleReason("positive", negative)).contains("sign");
        assertThat(PrecursorRules.seriesStaleReason("positive", weak)).contains("p-value");
    }

    @Test
    @DisplayName("lifecycle: validated → stale → retired; a stale pattern that validates again returns")
    void lifecycle() {
        assertThat(PrecursorRules.afterRetest("validated", null, true)).isEqualTo("validated");
        assertThat(PrecursorRules.afterRetest("validated", "lift fell", false)).isEqualTo("stale");
        assertThat(PrecursorRules.afterRetest("stale", "lift fell", false)).isEqualTo("retired");
        assertThat(PrecursorRules.afterRetest("stale", null, true)).isEqualTo("validated");
        assertThat(PrecursorRules.afterRetest("stale", null, false)).isEqualTo("stale");
        assertThat(PrecursorRules.afterRetest("retired", "x", false)).isEqualTo("retired");
    }

    @Test
    @DisplayName("probability: (hits + 1) / (n + 2) shrunk toward the base rate by n / (n + 20)")
    void probability() {
        assertThat(PrecursorRules.probability(8, 10, 0.2)).isCloseTo(0.75 / 3 + 0.2 * 2 / 3, within(1e-12));
        assertThat(PrecursorRules.probability(0, 0, 0.3)).isCloseTo(0.3, within(1e-12));
        assertThat(PrecursorRules.probability(1000, 1000, 0.1)).isGreaterThan(0.95);
        assertThat(PrecursorRules.shrinkWeight(20)).isEqualTo(0.5);
        assertThat(PrecursorRules.probability(5, 10, Double.NaN)).isCloseTo(0.5, within(1e-12));
    }

    @Test
    @DisplayName("durations and the deterministic description")
    void text() {
        assertThat(PrecursorRules.iso(Duration.ofDays(3))).isEqualTo("P3D");
        assertThat(PrecursorRules.iso(Duration.ZERO)).isEqualTo("P0D");
        assertThat(PrecursorRules.iso(Duration.ofHours(6))).isEqualTo("PT6H");
        assertThat(PrecursorText.lag(Duration.ofDays(14))).isEqualTo("2 weeks");
        assertThat(PrecursorText.lag(Duration.ofDays(1))).isEqualTo("1 day");
        assertThat(PrecursorText.lag(Duration.ofHours(6))).isEqualTo("6 hours");
        String d = PrecursorText.event(List.of("YE"), "GNSS interference alerts", "maritime disruption events",
                Duration.ofDays(3), 12, 30, new double[] {0.25, 0.58}, 0.11, Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-05-01T00:00:00Z"), true);
        assertThat(d).isEqualTo("In YE, GNSS interference alerts was followed by maritime disruption events within 3"
                + " days in 12/30 cases (40%, 95% interval 25%–58%) vs 11% expected by chance; validated on 2026-01-01"
                + " to 2026-05-01.");
        assertThat(PrecursorText.series(List.of(), "oil", "transits", Duration.ofDays(2), -0.31, 0.004,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-05-01T00:00:00Z"), false))
                .contains("opposite direction").contains("2 days later").contains("tested on");
    }

    @Test
    @DisplayName("candidate keys ignore labels and map order but not lags")
    void keys() {
        Map<String, Object> a1 = new LinkedHashMap<>();
        a1.put("selector", Map.of("intelEventType", "x", "regionCodes", List.of("YE")));
        a1.put("label", "one");
        Map<String, Object> a2 = new LinkedHashMap<>();
        a2.put("label", "two");
        a2.put("selector", Map.of("regionCodes", List.of("YE"), "intelEventType", "x"));
        Map<String, Object> c = Map.of("selector", Map.of("alertRuleTag", "t"));
        Candidate first = new Candidate("event", a1, c, Map.of("rule", "none"), Duration.ZERO, Duration.ofDays(1),
                List.of(), "auto", null, 0, 0, null);
        Candidate second = new Candidate("event", a2, c, Map.of("rule", "none"), Duration.ZERO, Duration.ofDays(1),
                List.of(), "operator", "x", 5, 5, null);
        Candidate longer = new Candidate("event", a1, c, Map.of("rule", "none"), Duration.ZERO, Duration.ofDays(3),
                List.of(), "auto", null, 0, 0, null);
        assertThat(first.key()).isEqualTo(second.key()).hasSize(64);
        assertThat(first.key()).isNotEqualTo(longer.key());
    }

    @Test
    @DisplayName("pre-whitening: count residuals against the slot baseline; standardized seasonal residuals")
    void prewhitening() {
        double[] counts = new double[40];
        java.util.Arrays.fill(counts, 5);
        counts[35] = 50;
        double[] r = Prewhitening.residuals(counts, true, 1);
        assertThat(r[0]).isNaN();
        assertThat(r[10]).isCloseTo(0, within(1e-9));
        assertThat(r[35]).isGreaterThan(5);
        double[] level = new double[28];
        for (int i = 0; i < level.length; i++) {
            level[i] = 100 + (i % 7) * 10 + (i == 20 ? 30 : 0);
        }
        double[] s = Prewhitening.residuals(level, false, 7);
        assertThat(s[20]).isGreaterThan(s[13]);
        double[] z = Prewhitening.standardize(new double[] {0, 1, -1, 0.5, -0.5, 8, Double.NaN});
        assertThat(z[5]).isGreaterThan(PrecursorRules.LIVE_THRESHOLD);
        assertThat(z[6]).isNaN();
    }

    @Test
    @DisplayName("miner helpers: hits within the lag window, the split instant and the spatial mapping")
    void minerHelpers() {
        Instant t = Instant.parse("2026-03-01T00:00:00Z");
        PrecursorTester.Occurrence a = new PrecursorTester.Occurrence(t, null, null, "YE", null, null);
        List<PrecursorTester.Occurrence> c = List.of(new PrecursorTester.Occurrence(t.plus(Duration.ofDays(1)), null,
                null, "YE", null, null));
        assertThat(PrecursorMiner.hit(a, c, Duration.ZERO, Duration.ofDays(2), PrecursorTester.Spatial.sameRegion(),
                t.plus(Duration.ofDays(10)))).isTrue();
        assertThat(PrecursorMiner.hit(a, c, Duration.ZERO, Duration.ofHours(12), PrecursorTester.Spatial.none(),
                t.plus(Duration.ofDays(10)))).isFalse();
        assertThat(PrecursorMiner.split(t, t.plus(Duration.ofDays(30)), 2.0 / 3)).isEqualTo(t.plus(Duration.ofDays(20)));
        assertThat(PrecursorMiner.spatial(Map.of("rule", "sameRegion", "regionLevel", "admin1")).regionLevel())
                .isEqualTo("admin1");
        assertThat(PrecursorMiner.spatial(Map.of("rule", "withinKm", "withinKm", 50)).withinKm()).isEqualTo(50.0);
        assertThat(PrecursorMiner.spatial(null).kind()).isEqualTo("none");
        assertThat(PrecursorOccurrences.regionOfList(List.of("YE-HD", "YE"))).containsExactly("YE", "YE-HD");
        assertThat(PrecursorOccurrences.regionOfCode("group-1")).containsExactly(null, null);
        assertThat(PrecursorOccurrences.familyKey(Map.of("changePoint", Map.of("family", "conflict"))))
                .isEqualTo("changePoint:conflict");
        assertThat(PrecursorLiveWatch.selectorKey(Map.of("situationActivated", Map.of()))).isEqualTo(
                "situationActivated:*");
        assertThat(PrecursorLiveWatch.selectorKey(Map.of("timestamps", List.of()))).isNull();
    }
}
