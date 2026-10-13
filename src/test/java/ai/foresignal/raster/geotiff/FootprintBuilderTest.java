package ai.foresignal.raster.geotiff;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * {@link FootprintBuilder} (prompt 054): the alpha-mask outline (leftmost and rightmost valid pixel per
 * row), simplification within its tolerance, separate parts across empty rows, and the antimeridian
 * split, both for a grid that runs past 180° and for a global grid whose swath wraps at its edges.
 */
class FootprintBuilderTest {

    static final double SCALE = 0.25;

    /** An image whose valid pixels are {@code [left(y), right(y)]} per row (both inclusive), or none. */
    static int[] mask(int w, int h, java.util.function.IntFunction<int[]> extent) {
        int[] rgba = new int[w * h];
        for (int y = 0; y < h; y++) {
            int[] e = extent.apply(y);
            if (e == null) {
                continue;
            }
            for (int x = e[0]; x <= e[1]; x++) {
                rgba[y * w + (x % w)] = 0x112233FF;
            }
        }
        return rgba;
    }

    @Test
    @DisplayName("a rectangle of valid pixels gives its corner-exact outline")
    void rectangle() {
        GeoReference ref = new GeoReference(4326, new double[] {-100, SCALE, 0, 35, 0, -SCALE}, false);
        int[] rgba = mask(20, 10, y -> y >= 2 && y < 8 ? new int[] {4, 11} : null);
        MultiPolygon footprint = FootprintBuilder.build(20, 10, rgba, ref, FootprintBuilder.DEFAULT_TOLERANCE);
        assertThat(footprint.getNumGeometries()).isEqualTo(1);
        assertThat(footprint.isValid()).isTrue();
        assertThat(footprint.getSRID()).isEqualTo(4326);
        Envelope env = footprint.getEnvelopeInternal();
        assertThat(env.getMinX()).isEqualTo(-99.0);
        assertThat(env.getMaxX()).isEqualTo(-97.0);
        assertThat(env.getMaxY()).isEqualTo(34.5);
        assertThat(env.getMinY()).isEqualTo(33.0);
        assertThat(footprint.getArea()).isCloseTo(2.0 * 1.5, within(1e-9));
        assertThat(footprint.getNumPoints()).as("simplified to the corners").isLessThanOrEqualTo(6);
    }

    @Test
    @DisplayName("a slanted swath: left edge down, right edge up; simplification stays within its tolerance")
    void slantedSwath() {
        GeoReference ref = new GeoReference(4326, new double[] {-110, 0.01, 0, 42, 0, -0.01}, false);
        int w = 400;
        int h = 600;
        int[] rgba = mask(w, h, y -> new int[] {50 + y / 3, 150 + y / 3});
        MultiPolygon fine = FootprintBuilder.build(w, h, rgba, ref, 0.0);
        MultiPolygon coarse = FootprintBuilder.build(w, h, rgba, ref, 0.05);
        assertThat(coarse.getNumPoints()).isLessThan(fine.getNumPoints());
        assertThat(coarse.getNumPoints()).isLessThan(40);
        assertThat(coarse.isValid()).isTrue();
        // Every simplified vertex lies within the tolerance of the exact outline, and the area barely moves.
        assertThat(coarse.getArea()).isCloseTo(fine.getArea(), within(fine.getArea() * 0.02));
        assertThat(coarse.getEnvelopeInternal().getMinX()).isCloseTo(-109.5, within(0.06));
        assertThat(coarse.getEnvelopeInternal().getMaxY()).isCloseTo(42.0, within(1e-9));
    }

    @Test
    @DisplayName("rows without a valid pixel split the outline into parts; an empty mask gives nothing")
    void gapsAndEmpty() {
        GeoReference ref = new GeoReference(4326, new double[] {0, 1, 0, 10, 0, -1}, false);
        int[] rgba = mask(10, 10, y -> y < 3 || y > 6 ? new int[] {2, 5} : null);
        assertThat(FootprintBuilder.build(10, 10, rgba, ref, 0.02).getNumGeometries()).isEqualTo(2);
        assertThat(FootprintBuilder.build(10, 10, new int[100], ref, 0.02).isEmpty()).isTrue();
    }

    @Test
    @DisplayName("a grid running past 180°: the footprint splits into a part on each side of the antimeridian")
    void antimeridianPastEdge() {
        GeoReference ref = new GeoReference(4326, new double[] {170, SCALE, 0, 66, 0, -SCALE}, false);
        int[] rgba = mask(80, 40, y -> new int[] {10 + y, 30 + y});
        MultiPolygon footprint = FootprintBuilder.build(80, 40, rgba, ref, 0.02);
        assertThat(footprint.getNumGeometries()).isEqualTo(2);
        assertThat(footprint.isValid()).isTrue();
        for (int i = 0; i < 2; i++) {
            Envelope env = footprint.getGeometryN(i).getEnvelopeInternal();
            assertThat(env.getMinX()).isGreaterThanOrEqualTo(-180);
            assertThat(env.getMaxX()).isLessThanOrEqualTo(180);
        }
        double[] bounds = Antimeridian.bounds(footprint);
        assertThat(bounds[0]).as("west of the antimeridian").isCloseTo(172.5, within(1e-6));
        assertThat(bounds[2]).as("east of it").isCloseTo(-172.5, within(1e-6));
        assertThat(bounds[0]).isGreaterThan(bounds[2]);
        // Area is preserved by the split: 21 px × 40 rows of 0.25°.
        assertThat(footprint.getArea()).isCloseTo(21 * 40 * SCALE * SCALE, within(1e-6));
    }

    @Test
    @DisplayName("a global grid whose swath leaves the right edge and re-enters at the left stays one swath, split at 180°")
    void antimeridianWrappedGlobal() {
        GeoReference ref = new GeoReference(4326, new double[] {-180, 1, 0, 70, 0, -1}, false);
        int w = 360;
        // Columns 350..369 (wrapping to 0..9) on every row: 170°E..170°W.
        int[] rgba = mask(w, 20, y -> new int[] {350, 369});
        MultiPolygon footprint = FootprintBuilder.build(w, 20, rgba, ref, 0.02);
        assertThat(footprint.getNumGeometries()).isEqualTo(2);
        assertThat(footprint.getArea()).isCloseTo(20 * 20, within(1e-6));
        double[] bounds = Antimeridian.bounds(footprint);
        assertThat(bounds[0]).isEqualTo(170.0);
        assertThat(bounds[2]).isEqualTo(-170.0);
    }

    @Test
    @DisplayName("EPSG:3857 pixels are outlined in degrees")
    void mercator() {
        GeoReference ref = new GeoReference(3857, new double[] {-11_131_949.08, 10_000, 0, 4_000_000, 0, -10_000}, false);
        int[] rgba = mask(10, 10, y -> new int[] {0, 9});
        MultiPolygon footprint = FootprintBuilder.build(10, 10, rgba, ref, 0.0);
        Envelope env = footprint.getEnvelopeInternal();
        assertThat(env.getMinX()).isCloseTo(-100.0, within(1e-4));
        assertThat(env.getMaxX()).isCloseTo(-100.0 + Math.toDegrees(100_000 / GeoReference.MERCATOR_RADIUS), within(1e-4));
    }
}
