package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code derived-dark-vessel-match} (prompt 099): a SAR detection inside the vessel's reachable area and
 * the gap window is matched; one outside either is not; without a SAR source the generator is idle with a
 * health note.
 */
class DarkVesselMatchTest {

    private static final Instant T0 = Instant.parse("2026-10-15T10:00:00Z");
    private static final double LON = -95.0;
    private static final double LAT = 27.0;

    @Test
    @DisplayName("the reachable radius grows with the time since the last fix and is capped at 200 km")
    void reachable() {
        Instant start = Instant.parse("2026-10-15T10:00:00Z");
        assertThat(DarkVesselMatchGenerator.reachableKm(start, start.plusSeconds(3600))).isCloseTo(46.3,
                org.assertj.core.data.Offset.offset(0.01));
        assertThat(DarkVesselMatchGenerator.reachableKm(start, start.plusSeconds(2 * 3600))).isCloseTo(92.6,
                org.assertj.core.data.Offset.offset(0.01));
        assertThat(DarkVesselMatchGenerator.reachableKm(start, start.plusSeconds(30 * 24 * 3600)))
                .isEqualTo(DarkVesselMatchGenerator.REACH_KM);
    }

    private static LayerHarness gap(String gapEnd) {
        LayerHarness harness = new LayerHarness(T0);
        if (gapEnd == null) {
            harness.put(DerivedSources.DERIVED_AIS_GAP, "366123456:" + T0.getEpochSecond(), "ais-gap", T0, LON, LAT,
                    "gapStart", T0.toString(), "open", true);
        } else {
            harness.put(DerivedSources.DERIVED_AIS_GAP, "366123456:" + T0.getEpochSecond(), "ais-gap", T0, LON, LAT,
                    "gapStart", T0.toString(), "gapEnd", gapEnd, "open", false);
        }
        return harness;
    }

    @Test
    @DisplayName("without a SAR detection source the generator is idle and says why")
    void idleWithoutSar() {
        LayerHarness harness = gap(null);
        DarkVesselMatchGenerator generator = new DarkVesselMatchGenerator(harness.registry());
        assertThat(harness.tick(generator, T0.plus(Duration.ofHours(1)))).isEmpty();
        assertThat(harness.last.state().get(AdapterStateStore.DEGRADED_REASON_KEY))
                .hasValueSatisfying(reason -> assertThat(reason).contains("gfw-sar-detections"));
    }

    @Test
    @DisplayName("a detection 60 km away two hours after the last fix (reach 92.6 km) is a candidate")
    void insideReachAndWindowMatches() {
        LayerHarness harness = gap(null);
        harness.put(DerivedSources.SAR_DETECTIONS, "det-1", "sar-vessel-detection", T0.plus(Duration.ofHours(2)), LON,
                LayerHarness.north(LAT, 60.0));
        DarkVesselMatchGenerator generator = new DarkVesselMatchGenerator(harness.registry());
        List<NormalizedRecord> out = harness.tick(generator, T0.plus(Duration.ofHours(3)));
        assertThat(out).singleElement().satisfies(record -> {
            assertThat(record.externalId()).endsWith(":det-1").startsWith("366123456:");
            assertThat(record.payload()).containsEntry("distanceKm", 60.0).containsEntry("timeOffsetH", 2.0)
                    .containsEntry("candidate", true);
        });
    }

    @Test
    @DisplayName("reach is measured to the detection time, not to the tick: 120 km at +2 h is out of reach")
    void outsideReachDoesNotMatch() {
        LayerHarness harness = gap(null);
        harness.put(DerivedSources.SAR_DETECTIONS, "det-1", "sar-vessel-detection", T0.plus(Duration.ofHours(2)), LON,
                LayerHarness.north(LAT, 120.0));
        DarkVesselMatchGenerator generator = new DarkVesselMatchGenerator(harness.registry());
        // Ten hours after the gap started the vessel could reach 200 km, but not by the detection at +2 h.
        assertThat(harness.tick(generator, T0.plus(Duration.ofHours(10)))).isEmpty();
    }

    @Test
    @DisplayName("a detection more than six hours after the gap ended is outside the window")
    void outsideWindowDoesNotMatch() {
        LayerHarness harness = gap(T0.plus(Duration.ofHours(1)).toString());
        harness.put(DerivedSources.SAR_DETECTIONS, "det-1", "sar-vessel-detection", T0.plus(Duration.ofHours(8)), LON,
                LayerHarness.north(LAT, 30.0));
        DarkVesselMatchGenerator generator = new DarkVesselMatchGenerator(harness.registry());
        assertThat(harness.tick(generator, T0.plus(Duration.ofHours(9)))).isEmpty();
    }
}
