package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.derive.Observation;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.PositionSource;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code derived-felt-locally} intensity (prompt 099). */
class FeltLocallyTest {

    private static Observation quake(double lon, double lat, double mag) {
        return new Observation("usgs", "e", "earthquake", Instant.parse("2026-10-15T10:00:00Z"), Geo.point(lon, lat),
                Map.of("mag", mag), PositionSource.UNKNOWN);
    }

    @Test
    @DisplayName("without a ShakeMap the IPE is used and bands to 0.5")
    void ipe() {
        FeltLocallyGenerator.Estimate estimate =
                FeltLocallyGenerator.estimate(quake(-95, 32, 6.0), null, -95.1, 32.1, 6.0, 10.0);
        assertThat(estimate.method()).isEqualTo("ipe-aw2007");
        assertThat(estimate.mmi()).isFinite();
        assertThat(FeltLocallyGenerator.band(4.7)).isEqualTo(4.5);
        assertThat(FeltLocallyGenerator.band(4.0)).isEqualTo(4.0);
    }

    @Test
    @DisplayName("a ShakeMap contour is preferred when present")
    void shakemapPreferred() {
        Observation contour = new Observation("usgs-shakemap", "e:mmi6", "shakemap-mmi",
                Instant.parse("2026-10-15T10:00:00Z"), Geo.point(-95.1, 32.1), Map.of("mmi", 6), PositionSource.UNKNOWN);
        FeltLocallyGenerator.Estimate estimate =
                FeltLocallyGenerator.estimate(quake(-95, 32, 6.0), java.util.List.of(contour), -95.1, 32.1, 6.0, 10.0);
        assertThat(estimate.method()).isEqualTo("shakemap");
        assertThat(estimate.mmi()).isEqualTo(6.0);
    }
}
