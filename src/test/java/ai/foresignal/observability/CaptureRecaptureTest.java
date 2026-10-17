package ai.foresignal.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SplittableRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Capture–recapture and the presence proxy (prompt 116, OBS-2): planted detection rates are recovered within
 * the interval, fewer than 20 reference events give no estimate, and the estimate valid at a time is the one
 * whose period ended by then, never a later one.
 */
class CaptureRecaptureTest {

    private static final Instant T0 = Instant.parse("2026-03-01T00:00:00Z");

    @Test
    @DisplayName("planted detection rates are recovered within the 95% interval")
    void plantedRatesRecovered() {
        SplittableRandom random = new SplittableRandom(116);
        for (double planted : new double[] {0.3, 0.7, 0.95}) {
            int reference = 400;
            long hits = 0;
            for (int i = 0; i < reference; i++) {
                if (random.nextDouble() < planted) {
                    hits++;
                }
            }
            Optional<CaptureRecapture.Estimate> estimate = CaptureRecapture.estimate(hits, reference);
            assertThat(estimate).isPresent();
            assertThat(estimate.get().lo()).as("lo for " + planted).isLessThanOrEqualTo(planted);
            assertThat(estimate.get().hi()).as("hi for " + planted).isGreaterThanOrEqualTo(planted);
            assertThat(estimate.get().p()).isCloseTo(planted, org.assertj.core.data.Offset.offset(0.07));
            assertThat(estimate.get().n()).isEqualTo(reference);
            assertThat(estimate.get().confidence()).isEqualTo("high");
        }
    }

    @Test
    @DisplayName("fewer than 20 reference events give no estimate; 20 to 99 are moderate confidence")
    void minimumReference() {
        assertThat(CaptureRecapture.estimate(10, 19)).isEmpty();
        assertThat(CaptureRecapture.estimate(10, 20)).map(CaptureRecapture.Estimate::confidence).contains("moderate");
        assertThat(CaptureRecapture.estimate(30, 20)).map(CaptureRecapture.Estimate::p).contains(1.0);
    }

    private static ObservabilityService.SourceEstimate row(String source, Instant periodEnd, double p) {
        return new ObservabilityService.SourceEstimate(source, "seismic", "US", null, null,
                periodEnd.minusSeconds(365L * 86_400), periodEnd, p, p - 0.05, p + 0.05, 50, CaptureRecapture.METHOD,
                "moderate", periodEnd.plusSeconds(3_600), null);
    }

    @Test
    @DisplayName("time-versioned estimates: the one valid at `at` is the newest whose period ended by then")
    void selectedByAt() {
        List<ObservabilityService.SourceEstimate> rows = List.of(row("usgs", T0, 0.6),
                row("usgs", T0.plusSeconds(7L * 86_400), 0.7), row("usgs", T0.plusSeconds(14L * 86_400), 0.9));
        List<ObservabilityService.SourceEstimate> before = ObservabilityService.validAt(rows, T0.minusSeconds(1));
        assertThat(before).isEmpty();
        assertThat(ObservabilityService.validAt(rows, T0.plusSeconds(86_400))).singleElement()
                .extracting(ObservabilityService.SourceEstimate::p).isEqualTo(0.6);
        assertThat(ObservabilityService.validAt(rows, T0.plusSeconds(10L * 86_400))).singleElement()
                .extracting(ObservabilityService.SourceEstimate::p).isEqualTo(0.7);
        assertThat(ObservabilityService.validAt(rows, T0.plusSeconds(30L * 86_400))).singleElement()
                .extracting(ObservabilityService.SourceEstimate::p).isEqualTo(0.9);
        assertThat(ObservabilityService.validAt(rows, T0.plusSeconds(500L * 86_400))).isEmpty();
    }

    @Test
    @DisplayName("presence proxy: share of active hours over the 90th-percentile share, capped at 1")
    void presenceProxy() {
        Map<String, Long> hours = new LinkedHashMap<>();
        for (int i = 0; i < 10; i++) {
            hours.put("cell" + i, (long) (i + 1) * 10);
        }
        Map<String, PresenceProxy.CellEstimate> estimates = PresenceProxy.estimate(hours, 200);
        assertThat(estimates.get("cell8").p()).isEqualTo(1.0);
        assertThat(estimates.get("cell9").p()).isEqualTo(1.0);
        assertThat(estimates.get("cell0").p()).isCloseTo(10.0 / 90.0, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(PresenceProxy.estimate(hours, 0)).isEmpty();
        Map<String, PresenceProxy.CellEstimate> range = PresenceProxy.receiverRange(hours, 200);
        assertThat(range).containsOnlyKeys("cell1", "cell2", "cell3", "cell4", "cell5", "cell6", "cell7", "cell8",
                "cell9");
        assertThat(PresenceProxy.percentile(new ArrayList<>(List.of(1.0, 2.0, 3.0)), 0.9)).isEqualTo(3.0);
    }

    @Test
    @DisplayName("the estimator runs once per ISO week, on Mondays from 04:30 UTC")
    void weeklySchedule() {
        Instant mondayEarly = Instant.parse("2026-10-12T04:00:00Z");
        Instant mondayLate = Instant.parse("2026-10-12T04:30:00Z");
        Instant thursday = Instant.parse("2026-10-15T10:00:00Z");
        assertThat(ObservabilityEstimator.due(mondayEarly, null)).isFalse();
        assertThat(ObservabilityEstimator.due(mondayLate, null)).isTrue();
        assertThat(ObservabilityEstimator.due(thursday, null)).isTrue();
        assertThat(ObservabilityEstimator.due(thursday, "2026-W42")).isFalse();
    }

    @Test
    @DisplayName("the band SQL buckets magnitudes like CanonicalRules.Bands")
    void bandExpression() {
        String sql = ObservabilityEstimator.bandExpression(new ai.foresignal.evidence.CanonicalRules.Bands("M", 2.5,
                0.5, 7.0));
        assertThat(sql).contains("c.band_value < 2.5").contains("'M'").contains("'+'");
        assertThat(ObservabilityEstimator.bandExpression(null)).isEqualTo("CAST(NULL AS text)");
    }
}
