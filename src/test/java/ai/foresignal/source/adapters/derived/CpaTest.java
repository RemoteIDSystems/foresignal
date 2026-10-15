package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.Observation;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.PositionSource;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code derived-cpa} kinematics (prompt 099). */
class CpaTest {

    private static Observation vessel(double lon, double lat, double sog, double cog) {
        return new Observation("aisstream", "m", "vessel", Instant.parse("2026-10-15T10:00:00Z"), Geo.point(lon, lat),
                Map.of("sog", sog, "cog", cog), PositionSource.REPORTED);
    }

    @Test
    @DisplayName("two vessels on a collision course have a small CPA and a short TCPA")
    void converging() {
        Observation a = vessel(-95.0, 32.0, 10, 90);
        Observation b = vessel(-94.8, 32.0, 10, 270);
        double[] result = CpaGenerator.approach(a, b);
        assertThat(result).isNotNull();
        assertThat(result[0]).isLessThan(1.0);
        assertThat(result[1]).isGreaterThan(0);
    }

    @Test
    @DisplayName("a stationary pair has no closing velocity")
    void stationary() {
        assertThat(CpaGenerator.approach(vessel(-95, 32, 0, 0), vessel(-95.1, 32, 0, 0))).isNull();
    }

    @Test
    @DisplayName("the pair key is sorted")
    void key() {
        assertThat(CpaGenerator.pairKey("b", "a")).isEqualTo("a:b");
    }
}
