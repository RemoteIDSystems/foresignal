package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.Observation;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.PositionSource;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code derived-ais-gap} coverage awareness (prompt 099). */
class AisGapCoverageTest {

    private static Observation vessel(double lon, double lat, double sog, int navStatus) {
        return new Observation("aisstream", "m1", "vessel", Instant.parse("2026-10-15T10:00:00Z"), Geo.point(lon, lat),
                Map.of("mmsi", "m1", "sog", sog, "navStatus", navStatus), PositionSource.REPORTED);
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
    @DisplayName("the coverage test needs three nearby vessels")
    void densityThreshold() {
        assertThat(AisGapGenerator.DENSITY_MIN).isEqualTo(3);
        assertThat(AisGapGenerator.DENSITY_RADIUS_KM).isEqualTo(50);
    }
}
