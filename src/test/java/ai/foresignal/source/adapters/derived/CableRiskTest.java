package ai.foresignal.source.adapters.derived;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.Geo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.LineString;

/** {@code derived-cable-risk} proximity (prompt 099). */
class CableRiskTest {

    @Test
    @DisplayName("distance to a cable route is measured and vessels near it are flagged")
    void distance() {
        LineString route = Geo.factory().createLineString(new org.locationtech.jts.geom.Coordinate[] {
            new org.locationtech.jts.geom.Coordinate(-95.0, 27.0), new org.locationtech.jts.geom.Coordinate(-95.0, 27.5)});
        ReferenceData.Cable cable = new ReferenceData.Cable("c1", route);
        assertThat(CableRiskGenerator.pointDistanceKm(cable, -95.0, 27.25)).isLessThan(0.5);
        assertThat(CableRiskGenerator.pointDistanceKm(cable, -94.5, 27.25)).isGreaterThan(2);
    }

    @Test
    @DisplayName("service vessel types are excluded")
    void excludedTypes() {
        assertThat(CableRiskGenerator.EXCLUDED).contains(31, 32, 52, 53, 55);
    }
}
