package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code derived-deception} (prompt 099): implied-speed math, and the boundaries the prompt names, driven
 * through ticks: 1,399 vs 1,401 kn between ADS-B fixes, MLAT never checked, and a duplicate MMSI 49 vs 51 km
 * apart within 120 s.
 */
class DeceptionTest {

    private static final Instant T0 = Instant.parse("2026-10-15T10:00:00Z");

    @Test
    @DisplayName("implied speed is distance over time in knots")
    void implied() {
        double knots = DeceptionGenerator.impliedKnots(32.0, -95.0, 32.1, -95.0, 60);
        assertThat(knots).isGreaterThan(300);
        assertThat(DeceptionGenerator.impliedKnots(32.0, -95.0, 32.0, -95.0, 60)).isZero();
        assertThat(DeceptionGenerator.impliedKnots(32.0, -95.0, 32.1, -95.0, 0)).isZero();
    }

    /** Two ADS-B fixes 60 s apart at {@code knots}; the records emitted by the second tick. */
    private static List<NormalizedRecord> aircraftAt(double knots, String posSource) {
        LayerHarness harness = new LayerHarness(T0, "adsblol-area-1");
        DeceptionGenerator generator = new DeceptionGenerator(harness.registry());
        harness.put("adsblol-area-1", "abc123", "aircraft", T0, 10.0, 0.0, "hex", "abc123", "posSource", posSource);
        harness.tick(generator, T0.plusSeconds(30));
        double km = knots * 1.852 * 60 / 3600.0;
        harness.put("adsblol-area-1", "abc123", "aircraft", T0.plusSeconds(60), 10.0, LayerHarness.north(0.0, km),
                "hex", "abc123", "posSource", posSource);
        return harness.tick(generator, T0.plusSeconds(90));
    }

    @Test
    @DisplayName("an aircraft at 1,401 kn implied between ADS-B fixes is flagged; at 1,399 kn it is not")
    void aircraftSpeedBoundary() {
        assertThat(aircraftAt(1_401, "adsb")).singleElement().satisfies(record -> {
            assertThat(record.externalId()).isEqualTo("abc123");
            assertThat(record.payload().get("checks"))
                    .isEqualTo(List.of(Map.of("check", "implied-speed", "count", 1)));
        });
        assertThat(aircraftAt(1_399, "adsb")).isEmpty();
    }

    @Test
    @DisplayName("MLAT and TIS-B positions are never checked for implied speed")
    void calculatedPositionsSkipped() {
        assertThat(aircraftAt(5_000, "mlat")).isEmpty();
        assertThat(aircraftAt(5_000, "tisb")).isEmpty();
    }

    /** The same MMSI from two sources 60 s apart, {@code km} apart; the records the tick emits. */
    private static List<NormalizedRecord> duplicateAt(double km) {
        LayerHarness harness = new LayerHarness(T0, "aisstream", "aishub");
        DeceptionGenerator generator = new DeceptionGenerator(harness.registry());
        harness.put("aisstream", "366123456", "vessel", T0, -95.0, 30.0, "mmsi", "366123456", "sog", 10.0);
        harness.put("aishub", "366123456", "vessel", T0.plusSeconds(60), -95.0, LayerHarness.north(30.0, km),
                "mmsi", "366123456", "sog", 10.0);
        return harness.tick(generator, T0.plus(Duration.ofMinutes(2)));
    }

    @Test
    @DisplayName("a duplicate MMSI 51 km apart within 120 s is flagged; 49 km apart is not")
    void duplicateMmsiBoundary() {
        assertThat(duplicateAt(51)).singleElement().satisfies(record -> assertThat(record.payload().get("checks"))
                .isEqualTo(List.of(Map.of("check", "duplicate-mmsi", "count", 1))));
        assertThat(duplicateAt(49)).isEmpty();
    }
}
