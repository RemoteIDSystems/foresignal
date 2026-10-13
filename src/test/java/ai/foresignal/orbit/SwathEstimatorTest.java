package ai.foresignal.orbit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.raster.geotiff.Antimeridian;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.MultiPolygon;

/**
 * {@link SwathEstimator} (prompt 054): a Meteor-M2-3 TLE (CelesTrak GP, NORAD 57166, epoch
 * 2026-10-12T22:16Z, fetched 2026-10-13) propagates offline with the bundled Orekit data, the swath
 * width comes from {@code corr_swath} or the instrument table, and a swath over the Bering Sea is split
 * at ±180°.
 */
class SwathEstimatorTest {

    static final String LINE1 = "1 57166U 23091A   26285.92798396  .00000015  00000+0  25251-4 0  9990";
    static final String LINE2 = "2 57166  98.5949 338.4155 0004029 171.9444 188.1801 14.24054110171272";

    @Test
    @DisplayName("the width: corr_swath when positive, else MSU-MR 2800, AVHRR 2900, MHS 2200 km, else unknown")
    void width() {
        assertThat(SwathEstimator.widthKm(2750.0, "msu_mr")).contains(2750.0);
        assertThat(SwathEstimator.widthKm(null, "msu_mr")).contains(2800.0);
        assertThat(SwathEstimator.widthKm(0.0, "MSU-MR")).contains(2800.0);
        assertThat(SwathEstimator.widthKm(null, "avhrr_3")).contains(2900.0);
        assertThat(SwathEstimator.widthKm(null, "mhs")).contains(2200.0);
        assertThat(SwathEstimator.widthKm(null, "kmss")).isEmpty();
        assertThat(SwathEstimator.widthKm(Double.NaN, null)).isEmpty();
    }

    @Test
    @DisplayName("the ground track propagates offline: 61 points over 10 minutes at 10-s steps, about 7 km/s apart")
    void groundTrack() {
        Instant start = Instant.parse("2026-10-13T03:00:00Z");
        List<double[]> track = SwathEstimator.groundTrack(LINE1, LINE2, start, start.plusSeconds(600));
        assertThat(track).hasSize(61);
        for (int i = 1; i < track.size(); i++) {
            double[] a = track.get(i - 1);
            double[] b = track.get(i);
            double km = Math.toDegrees(Math.acos(Math.sin(Math.toRadians(a[1])) * Math.sin(Math.toRadians(b[1]))
                    + Math.cos(Math.toRadians(a[1])) * Math.cos(Math.toRadians(b[1]))
                            * Math.cos(Math.toRadians(b[0] - a[0])))) * 111.2;
            assertThat(km).as("ground distance of one 10-s step").isBetween(60.0, 75.0);
            assertThat(b[1]).isBetween(-90.0, 90.0);
        }
    }

    @Test
    @DisplayName("a mid-latitude swath: one part, valid, about 2,800 km wide, around the track")
    void swath() {
        Instant start = Instant.parse("2026-10-13T03:00:00Z");
        SwathEstimator.Swath swath = SwathEstimator.estimate(LINE1, LINE2, start, start.plusSeconds(600), 2800)
                .orElseThrow();
        assertThat(swath.points()).isEqualTo(61);
        assertThat(swath.widthKm()).isEqualTo(2800.0);
        MultiPolygon footprint = swath.footprint();
        assertThat(footprint.isValid()).isTrue();
        assertThat(footprint.getSRID()).isEqualTo(4326);
        assertThat(footprint.getNumGeometries()).isEqualTo(1);
        List<double[]> track = SwathEstimator.groundTrack(LINE1, LINE2, start, start.plusSeconds(600));
        double[] mid = track.get(30);
        assertThat(footprint.contains(Antimeridian.GEOMETRY.createPoint(
                new org.locationtech.jts.geom.Coordinate(mid[0], mid[1])))).isTrue();
        // About 4,300 km of track × 2,800 km: roughly 1,100–1,900 square degrees at mid-latitudes.
        assertThat(footprint.getArea()).isBetween(800.0, 2500.0);
    }

    @Test
    @DisplayName("a swath over the Bering Sea crosses ±180°: two parts, one on each side, bounds west > east")
    void antimeridian() {
        Instant start = Instant.parse("2026-10-12T21:05:00Z");
        MultiPolygon footprint = SwathEstimator.estimate(LINE1, LINE2, start, start.plusSeconds(240), 2800)
                .orElseThrow().footprint();
        assertThat(footprint.getNumGeometries()).isGreaterThanOrEqualTo(2);
        assertThat(footprint.isValid()).isTrue();
        boolean touchesEast = false;
        boolean touchesWest = false;
        for (int i = 0; i < footprint.getNumGeometries(); i++) {
            Envelope env = footprint.getGeometryN(i).getEnvelopeInternal();
            assertThat(env.getMinX()).isGreaterThanOrEqualTo(-180);
            assertThat(env.getMaxX()).isLessThanOrEqualTo(180);
            touchesEast |= env.getMaxX() == 180;
            touchesWest |= env.getMinX() == -180;
        }
        assertThat(touchesEast).isTrue();
        assertThat(touchesWest).isTrue();
        double[] bounds = Antimeridian.bounds(footprint);
        assertThat(bounds[0]).isGreaterThan(bounds[2]);
        assertThat(bounds[1]).isBetween(40.0, 60.0);
        assertThat(bounds[3]).isBetween(65.0, 80.0);
        assertThat(footprint.getArea()).isCloseTo(715, within(150.0));
    }

    @Test
    @DisplayName("no TLE, a bad TLE, a zero width or a reversed window: empty, or a one-point window widened by nothing")
    void degenerate() {
        Instant start = Instant.parse("2026-10-13T03:00:00Z");
        assertThat(SwathEstimator.estimate(null, LINE2, start, start, 2800)).isEmpty();
        assertThat(SwathEstimator.estimate("1 garbage", "2 garbage", start, start.plusSeconds(60), 2800)).isEmpty();
        assertThat(SwathEstimator.estimate(LINE1, LINE2, start, start.plusSeconds(60), 0)).isEmpty();
        assertThat(SwathEstimator.estimate(LINE1, LINE2, start, start.minusSeconds(60), 2800)).as("one point")
                .isEmpty();
        assertThat(SwathEstimator.estimate(LINE1, LINE2, start, start.plusSeconds(3 * 3600), 2800)).hasValueSatisfying(
                s -> assertThat(s.points()).as("capped at 30 minutes").isEqualTo(181));
    }
}
