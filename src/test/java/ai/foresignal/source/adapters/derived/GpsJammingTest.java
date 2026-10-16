package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code derived-gps-jamming} thresholds (prompt 099). */
class GpsJammingTest {

    @Test
    @DisplayName("a cell needs three aircraft before a level is asserted")
    void minimumAircraft() {
        assertThat(GpsJammingGenerator.level(2, 2)).isEqualTo("low");
        assertThat(GpsJammingGenerator.level(3, 1)).isEqualTo("medium");
    }

    @Test
    @DisplayName("the boundaries are 30% medium and 50% high, inclusive")
    void boundaries() {
        assertThat(GpsJammingGenerator.level(10, 2)).isEqualTo("low");
        assertThat(GpsJammingGenerator.level(10, 3)).isEqualTo("medium");
        assertThat(GpsJammingGenerator.level(10, 4)).isEqualTo("medium");
        assertThat(GpsJammingGenerator.level(10, 5)).isEqualTo("high");
    }

    @Test
    @DisplayName("after a restart a cell seen 10 minutes ago is kept as insufficient-data; one seen 3 hours ago is"
            + " tombstoned")
    void reconcileAdoptsLiveCells() {
        java.time.Instant now = java.time.Instant.parse("2026-10-15T12:00:00Z");
        String fresh = GpsJammingGenerator.key(-95.5, 32.5, true);
        String stale = GpsJammingGenerator.key(10.5, 50.5, true);
        LayerHarness harness = new LayerHarness(now, "adsblol-mil");
        GpsJammingGenerator generator = new GpsJammingGenerator(harness.registry());
        harness.reconcile(generator, java.util.List.of(
                cellHead(fresh, now.minusSeconds(600)), cellHead(stale, now.minusSeconds(3 * 3600))));

        java.util.List<ai.foresignal.source.spi.NormalizedRecord> out = harness.tick(generator, now.plusSeconds(60));
        assertThat(harness.tombstones).containsExactly("jamming-cell/" + stale);
        assertThat(out).singleElement().satisfies(record -> {
            assertThat(record.externalId()).isEqualTo(fresh);
            assertThat(record.payload()).containsEntry("level", "high").containsEntry("status", "insufficient-data");
        });
    }

    private static ai.foresignal.derive.HeadSnapshot cellHead(String id, java.time.Instant at) {
        return new ai.foresignal.derive.HeadSnapshot(GpsJammingGenerator.ID, id, GpsJammingGenerator.KIND, 1, 1, at,
                false, java.util.Map.of("level", "high", "ratio", 0.6, "total", 5, "bad", 3, "status", "ok"), null);
    }

    @Test
    @DisplayName("H3 and 1-degree keys are stable")
    void keys() {
        String h3 = GpsJammingGenerator.key(-95.5, 32.5, true);
        assertThat(h3).startsWith("h3:");
        assertThat(GpsJammingGenerator.key(-95.5, 32.5, false)).isEqualTo("deg:32:-96");
    }
}
