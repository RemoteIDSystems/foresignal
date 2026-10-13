package ai.foresignal.raster.sample;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Percentage.withPercentage;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.raster.GridDef;
import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.GridDefinition;
import ai.foresignal.testsupport.raster.SampleFixtures;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

/**
 * Spherical cell and polygon areas (prompt 052), and the area scan behind {@code /stats}: a latlon and an
 * {@code lcc} grid agree with the exact spherical area within 1%.
 */
class CellAreasTest {

    /** The exact spherical area of a lon/lat box, km². */
    static double boxKm2(double west, double south, double east, double north) {
        double r = Geo.EARTH_RADIUS_KM;
        return r * r * Math.toRadians(east - west) * (Math.sin(Math.toRadians(north)) - Math.sin(Math.toRadians(south)));
    }

    @Test
    @DisplayName("a latlon grid's cells sum to the exact box area; a polygon's area matches too")
    void latlonExact() {
        GridDefinition d = GridDefinition.latLon(100, 80, -100, 28, -95, 32);
        GridDef g = GridDef.of(d);
        double sum = 0;
        for (int r = 0; r < 80; r++) {
            for (int c = 0; c < 100; c++) {
                sum += CellAreas.cellAreaKm2(g, c, r);
            }
        }
        double exact = boxKm2(-100, 28, -95, 32);
        assertThat(sum).isCloseTo(exact, withPercentage(0.01));
        Geometry box = Geo.factory().toGeometry(new Envelope(-100, -95, 28, 32));
        assertThat(CellAreas.geometryAreaKm2(box)).isCloseTo(exact, withPercentage(1));
    }

    @Test
    @DisplayName("an lcc grid (HRRR's projection, 3 km cells) gives a box's area within 1%")
    void lccWithinOnePercent() {
        GridDefinition d = new GridDefinition(GridDefinition.LCC, 400, 300, -110, 20, -80, 45, Map.of(
                "lat1", 38.5, "lat2", 38.5, "lov", -97.5, "la1", 29.0, "lo1", -103.0, "dx", 3000.0, "dy", 3000.0));
        GridDef g = GridDef.of(d);
        Geometry box = Geo.factory().toGeometry(new Envelope(-100, -98, 30, 32));
        float[] ones = new float[400 * 300];
        java.util.Arrays.fill(ones, 1f);
        FrameView view = SampleFixtures.physical(SampleFixtures.gusts("g"), d, null, ones);
        AreaScan.Result result = AreaScan.scan(view, box, new double[] {0.5}, false);
        double exact = boxKm2(-100, 30, -98, 32);
        assertThat(result.areaKm2()).isCloseTo(exact, withPercentage(1));
        assertThat(result.areaAboveKm2()[0]).isCloseTo(exact, withPercentage(1));
        assertThat(result.coveragePct()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("a scan reports min, max, mean, p95, coverage and the area above thresholds")
    void scanStatistics() {
        GridDefinition d = GridDefinition.latLon(10, 10, -100, 30, -99, 31);
        float[] v = new float[100];
        for (int i = 0; i < 100; i++) {
            v[i] = i;
        }
        FrameView view = SampleFixtures.physical(SampleFixtures.gusts("g"), d, null, v);
        Geometry all = Geo.factory().toGeometry(new Envelope(-100, -99, 30, 31));
        AreaScan.Result r = AreaScan.scan(view, all, new double[] {50, 95}, true);
        assertThat(r.cellsInArea()).isEqualTo(100);
        assertThat(r.min()).isEqualTo(0.0);
        assertThat(r.max()).isEqualTo(99.0);
        assertThat(r.mean()).isCloseTo(49.5, org.assertj.core.data.Offset.offset(1e-6));
        assertThat(r.p95()).isEqualTo(94.0);
        assertThat(r.areaAboveKm2()[0] / r.areaKm2()).isCloseTo(0.5, org.assertj.core.data.Offset.offset(0.02));
        assertThat(r.maskCells()).hasSize(50);
        Geometry outside = Geo.factory().toGeometry(new Envelope(10, 11, 10, 11));
        assertThat(AreaScan.scan(view, outside, new double[0], false).cellsInArea()).isZero();
    }

    @Test
    @DisplayName("stats geometry limits: vertices, dimension and 2,000,000 km²")
    void limits() {
        assertThatThrownBy(() -> AreaStatsService.checkGeometry(Geo.point(0, 0))).isInstanceOf(ValidationException.class);
        Geometry huge = Geo.factory().toGeometry(new Envelope(-120, -70, 20, 50));
        assertThatThrownBy(() -> AreaStatsService.checkGeometry(huge)).isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors().get(0).field()).isEqualTo("geometry"));
        Geometry many = Geo.point(-98, 30).buffer(0.5, 3000);
        assertThat(many.getNumPoints()).isGreaterThan(10_000);
        assertThatThrownBy(() -> AreaStatsService.checkGeometry(many)).isInstanceOf(ValidationException.class);
        assertThat(AreaStatsService.checkGeometry(Geo.factory().toGeometry(new Envelope(-99, -98, 30, 31))))
                .isCloseTo(boxKm2(-99, 30, -98, 31), withPercentage(1));
    }
}
