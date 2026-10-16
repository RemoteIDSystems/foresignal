package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.Observation;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code derived-ais-gap} coverage awareness (prompt 099), driven tick by tick: no gap without three
 * nearby vessels or with a degraded window, a gap that opens and closes with its displacement fields,
 * the six-hour tombstone, the expiry of a gap whose vessel never returns, and the state cap.
 */
class AisGapCoverageTest {

    private static final Instant T0 = Instant.parse("2026-10-15T10:00:00Z");
    private static final String AIS = "aisstream";
    private static final String MMSI = "366123456";

    private LayerHarness harness;
    private AisGapGenerator generator;

    @BeforeEach
    void setUp() {
        harness = new LayerHarness(T0, AIS);
        generator = new AisGapGenerator(harness.registry(), harness.reference());
    }

    private static Observation vessel(double lon, double lat, double sog, int navStatus) {
        return new Observation(AIS, "m1", "vessel", T0, Geo.point(lon, lat),
                Map.of("mmsi", "m1", "sog", sog, "navStatus", navStatus), PositionSource.REPORTED);
    }

    private void underWay(String mmsi, Instant at, double lat) {
        harness.put(AIS, mmsi, "vessel", at, -95.0, lat, "mmsi", mmsi, "sog", 8.0, "cog", 90.0, "navStatus", 0);
    }

    @Test
    @DisplayName("a moving vessel away from port is eligible for a gap")
    void movingOutsidePort() {
        assertThat(AisGapGenerator.eligible(vessel(-95, 32, 8, 0), List.of(), -95, 32)).isTrue();
    }

    @Test
    @DisplayName("a slow, moored or in-port vessel is not")
    void ineligible() {
        assertThat(AisGapGenerator.eligible(vessel(-95, 32, 0.5, 0), List.of(), -95, 32)).isFalse();
        assertThat(AisGapGenerator.eligible(vessel(-95, 32, 8, 1), List.of(), -95, 32)).isFalse();
        List<ReferenceData.Port> ports = List.of(new ReferenceData.Port("1", "Houston", -95, 32, 6));
        assertThat(AisGapGenerator.eligible(vessel(-95.01, 32.01, 8, 0), ports, -95.01, 32.01)).isFalse();
    }

    @Test
    @DisplayName("two nearby vessels are not coverage evidence: no gap opens; three are, and it opens")
    void densityBelowThreeOpensNothing() {
        underWay(MMSI, T0, 32.0);
        harness.world.densities.put(AIS, AisGapGenerator.DENSITY_MIN - 1);
        assertThat(harness.tick(generator, T0.plus(Duration.ofMinutes(31)))).isEmpty();

        harness.world.densities.put(AIS, AisGapGenerator.DENSITY_MIN);
        List<NormalizedRecord> opened = harness.tick(generator, T0.plus(Duration.ofMinutes(36)));
        assertThat(opened).singleElement().satisfies(record -> {
            assertThat(record.externalId()).isEqualTo(MMSI + ":" + T0.getEpochSecond());
            assertThat(record.payload()).containsEntry("open", true).containsEntry("gapStart", T0.toString())
                    .containsEntry("lastSog", 8.0).containsEntry("lastCog", 90.0).containsEntry("nearby", 3);
        });
    }

    @Test
    @DisplayName("no gap opens before 30 minutes without a fix")
    void notBeforeThirtyMinutes() {
        underWay(MMSI, T0, 32.0);
        harness.world.densities.put(AIS, 10);
        assertThat(harness.tick(generator, T0.plus(Duration.ofMinutes(29)))).isEmpty();
    }

    @Test
    @DisplayName("a degraded window (a bus gap or live drop) opens no gap, whatever the density")
    void degradedInputOpensNothing() {
        underWay(MMSI, T0, 32.0);
        harness.world.densities.put(AIS, 10);
        harness.world.degradedAt = T0.plus(Duration.ofMinutes(20));
        assertThat(harness.tick(generator, T0.plus(Duration.ofMinutes(31)))).isEmpty();
    }

    @Test
    @DisplayName("a failing input source opens no gap")
    void failingSourceOpensNothing() {
        underWay(MMSI, T0, 32.0);
        harness.world.densities.put(AIS, 10);
        harness.world.statuses.put(AIS, HealthStatus.FAILING);
        assertThat(harness.tick(generator, T0.plus(Duration.ofMinutes(31)))).isEmpty();
    }

    @Test
    @DisplayName("the vessel's reappearance closes the gap with gapEnd, minutes, displacement and implied speed;"
            + " six hours later it is tombstoned")
    void reappearanceClosesTheGap() {
        underWay(MMSI, T0, 32.0);
        harness.world.densities.put(AIS, 5);
        harness.tick(generator, T0.plus(Duration.ofMinutes(31)));

        Instant back = T0.plus(Duration.ofMinutes(90));
        underWay(MMSI, back, LayerHarness.north(32.0, 20.0));
        List<NormalizedRecord> closed = harness.tick(generator, back.plus(Duration.ofMinutes(5)));
        assertThat(closed).singleElement().satisfies(record -> {
            assertThat(record.externalId()).isEqualTo(MMSI + ":" + T0.getEpochSecond());
            assertThat(record.payload()).containsEntry("open", false).containsEntry("gapEnd", back.toString())
                    .containsEntry("gapMinutes", 90L).containsEntry("displacementKm", 20.0)
                    .containsEntry("impliedKn", 7.2); // 20 km in 1.5 h = 7.2 kn
        });

        // The vessel keeps reporting, so no new gap opens while the closed one waits for its tombstone.
        underWay(MMSI, back.plus(Duration.ofHours(5)).minusSeconds(60), LayerHarness.north(32.0, 20.0));
        harness.tick(generator, back.plus(Duration.ofHours(5)));
        assertThat(harness.tombstones).isEmpty();
        Instant later = back.plus(Duration.ofHours(6)).plus(Duration.ofMinutes(5));
        underWay(MMSI, later.minusSeconds(60), LayerHarness.north(32.0, 20.0));
        harness.tick(generator, later);
        assertThat(harness.tombstones).containsExactly("ais-gap/" + MMSI + ":" + T0.getEpochSecond());
        assertThat(generator.stateSize()).isZero();
    }

    @Test
    @DisplayName("a gap whose vessel never returns expires after 12 hours with an unknown end, then is tombstoned")
    void neverReturningVesselExpires() {
        underWay(MMSI, T0, 32.0);
        harness.world.densities.put(AIS, 5);
        harness.tick(generator, T0.plus(Duration.ofMinutes(31)));
        assertThat(generator.stateSize()).isEqualTo(1);

        assertThat(harness.tick(generator, T0.plus(Duration.ofHours(11)))).isEmpty();
        List<NormalizedRecord> expired = harness.tick(generator, T0.plus(AisGapGenerator.FORGET).plusSeconds(60));
        assertThat(expired).singleElement().satisfies(record -> {
            assertThat(record.payload()).containsEntry("open", false).containsEntry("expired", true)
                    .containsEntry("status", "insufficient-data").doesNotContainKey("gapEnd");
        });

        harness.tick(generator, T0.plus(AisGapGenerator.FORGET).plus(Duration.ofHours(6)).plus(Duration.ofMinutes(5)));
        assertThat(harness.tombstones).containsExactly("ais-gap/" + MMSI + ":" + T0.getEpochSecond());
        assertThat(generator.stateSize()).isZero();
    }

    @Test
    @DisplayName("at the state cap no further gap opens and the drop is counted")
    void capCountsDrops() {
        underWay("366000001", T0, 32.0);
        underWay("366000002", T0, 33.0);
        harness.world.densities.put(AIS, 5);
        List<NormalizedRecord> opened = harness.tick(generator, T0.plus(Duration.ofMinutes(31)),
                ValidatedParams.of(Map.of("maxEntities", 1)));
        assertThat(opened).hasSize(1);
        assertThat(generator.dropped()).isEqualTo(1);
    }
}
