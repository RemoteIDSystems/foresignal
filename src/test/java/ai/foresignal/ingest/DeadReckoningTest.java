package ai.foresignal.ingest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.source.spi.MotionSpec.SpeedUnit;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

/** Dead reckoning and the deviation used by SAMPLED row 10. */
class DeadReckoningTest {

    private static final Coordinate ORIGIN = new Coordinate(0.5, 0.5);

    /** One degree of arc on the mean-radius sphere, in meters. */
    private static final double DEGREE = Math.toRadians(1) * DeadReckoning.EARTH_RADIUS_METERS;

    @Test
    @DisplayName("predicts the position along course at speed")
    void predictsPosition() {
        // 100 m/s due north for 1,000 s is 100 km.
        Coordinate predicted = DeadReckoning.destination(ORIGIN, 0.0, 100_000);

        assertThat(predicted.x).isCloseTo(0.5, within(1e-9));
        assertThat(predicted.y).isCloseTo(0.5 + 100_000 / DEGREE, within(1e-9));
    }

    @Test
    @DisplayName("an entity exactly at its predicted position deviates by ~0")
    void onTrackIsZero() {
        Coordinate predicted = DeadReckoning.destination(ORIGIN, 45.0, 5_000);

        double deviation =
                DeadReckoning.deviationMeters(
                        ORIGIN, 5.0, SpeedUnit.METERS_PER_SECOND, 45.0, Duration.ofSeconds(1000), predicted);

        assertThat(deviation).isLessThan(0.01);
    }

    @Test
    @DisplayName("speed is converted from the declared unit")
    void convertsUnits() {
        Coordinate tenMinutesAt100Knots = DeadReckoning.destination(ORIGIN, 90.0, 100 * 0.514444 * 600);
        Coordinate tenMinutesAt100Kmh = DeadReckoning.destination(ORIGIN, 90.0, 100 * 0.277778 * 600);
        Coordinate tenMinutesAt100Mph = DeadReckoning.destination(ORIGIN, 90.0, 100 * 0.44704 * 600);
        Duration tenMinutes = Duration.ofMinutes(10);

        assertThat(DeadReckoning.deviationMeters(ORIGIN, 100.0, SpeedUnit.KNOTS, 90.0, tenMinutes, tenMinutesAt100Knots))
                .isLessThan(0.01);
        assertThat(DeadReckoning.deviationMeters(
                        ORIGIN, 100.0, SpeedUnit.KILOMETERS_PER_HOUR, 90.0, tenMinutes, tenMinutesAt100Kmh))
                .isLessThan(0.01);
        assertThat(DeadReckoning.deviationMeters(
                        ORIGIN, 100.0, SpeedUnit.MILES_PER_HOUR, 90.0, tenMinutes, tenMinutesAt100Mph))
                .isLessThan(0.01);
        // Knots read as m/s would put the prediction about 29 km further east.
        assertThat(DeadReckoning.deviationMeters(
                        ORIGIN, 100.0, SpeedUnit.METERS_PER_SECOND, 90.0, tenMinutes, tenMinutesAt100Knots))
                .isGreaterThan(25_000);
    }

    @Test
    @DisplayName("a null unit means knots")
    void nullUnitIsKnots() {
        Coordinate predicted = DeadReckoning.destination(ORIGIN, 90.0, 10 * 0.514444 * 60);
        assertThat(DeadReckoning.deviationMeters(ORIGIN, 10.0, null, 90.0, Duration.ofSeconds(60), predicted))
                .isLessThan(0.01);
    }

    @Test
    @DisplayName("crossing the antimeridian keeps longitude in range and the distance short")
    void antimeridian() {
        Coordinate east = new Coordinate(179.9, 10.0);

        Coordinate predicted = DeadReckoning.destination(east, 90.0, 50_000);

        assertThat(predicted.x).isBetween(-180.0, -179.0);
        Coordinate candidate = new Coordinate(-179.95, 10.0);
        double deviation =
                DeadReckoning.deviationMeters(east, 50.0, SpeedUnit.METERS_PER_SECOND, 90.0, Duration.ofSeconds(1000), candidate);
        assertThat(deviation).isLessThan(50_000);
        assertThat(DeadReckoning.haversineMeters(east, candidate)).isLessThan(20_000);
    }

    @Test
    @DisplayName("without motion the deviation is the plain distance")
    void noMotionFallsBackToHaversine() {
        Coordinate candidate = new Coordinate(0.5, 1.5);

        assertThat(DeadReckoning.deviationMeters(ORIGIN, null, SpeedUnit.KNOTS, 90.0, Duration.ofSeconds(60), candidate))
                .isCloseTo(DEGREE, within(1.0));
        assertThat(DeadReckoning.deviationMeters(ORIGIN, 10.0, SpeedUnit.KNOTS, null, Duration.ofSeconds(60), candidate))
                .isCloseTo(DEGREE, within(1.0));
        assertThat(DeadReckoning.deviationMeters(ORIGIN, Double.NaN, SpeedUnit.KNOTS, 0.0, Duration.ofSeconds(60), candidate))
                .isCloseTo(DEGREE, within(1.0));
        assertThat(DeadReckoning.deviationMeters(ORIGIN, 10.0, SpeedUnit.KNOTS, 0.0, null, candidate))
                .isCloseTo(DEGREE, within(1.0));
    }

    @Test
    @DisplayName("a missing geometry on either side deviates by 0")
    void nullGeometryIsZero() {
        assertThat(DeadReckoning.deviationMeters(null, 10.0, SpeedUnit.KNOTS, 0.0, Duration.ofSeconds(60), ORIGIN))
                .isZero();
        assertThat(DeadReckoning.deviationMeters(ORIGIN, 10.0, SpeedUnit.KNOTS, 0.0, Duration.ofSeconds(60), null))
                .isZero();
    }

    @Test
    @DisplayName("a negative Δt does not reckon backwards")
    void negativeElapsedIsZero() {
        Coordinate candidate = new Coordinate(0.5, 0.5);
        assertThat(DeadReckoning.deviationMeters(
                        ORIGIN, 100.0, SpeedUnit.METERS_PER_SECOND, 0.0, Duration.ofSeconds(-60), candidate))
                .isZero();
    }

    @Test
    @DisplayName("haversine matches one degree of latitude")
    void haversine() {
        assertThat(DeadReckoning.haversineMeters(new Coordinate(0, 0), new Coordinate(0, 1)))
                .isCloseTo(DEGREE, within(1e-6));
        assertThat(DeadReckoning.haversineMeters(ORIGIN, ORIGIN)).isZero();
    }

    @Test
    @DisplayName("longitudes wrap into [-180, 180)")
    void normalizesLongitude() {
        assertThat(DeadReckoning.normalizeLongitude(181.0)).isCloseTo(-179.0, within(1e-9));
        assertThat(DeadReckoning.normalizeLongitude(-181.0)).isCloseTo(179.0, within(1e-9));
        assertThat(DeadReckoning.normalizeLongitude(180.0)).isCloseTo(-180.0, within(1e-9));
        assertThat(DeadReckoning.normalizeLongitude(12.5)).isCloseTo(12.5, within(1e-9));
    }
}
