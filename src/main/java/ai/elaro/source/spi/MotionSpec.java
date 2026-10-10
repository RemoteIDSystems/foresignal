package ai.elaro.source.spi;

/**
 * Where to find speed and course in a payload, so sampling can dead-reckon
 * (Core-Data-Model §5.2).
 *
 * <p>Dead reckoning from the last persisted position is what makes a distance threshold meaningful
 * for a moving entity: an aircraft that has travelled exactly where it was predicted to has not
 * told us anything new, however far it moved.
 *
 * <p>These fields must not be listed as significant — a changing speed reading is precisely the
 * thing sampling exists to suppress.
 *
 * @param speedField top-level payload key holding speed
 * @param speedUnit the unit that field is in
 * @param courseField top-level payload key holding course in degrees
 */
public record MotionSpec(String speedField, SpeedUnit speedUnit, String courseField) {

    public MotionSpec {
        if (speedField == null || speedField.isBlank()) {
            throw new IllegalArgumentException("motion.speedField is required");
        }
        if (courseField == null || courseField.isBlank()) {
            throw new IllegalArgumentException("motion.courseField is required");
        }
        if (speedUnit == null) {
            speedUnit = SpeedUnit.KNOTS;
        }
    }

    /** Units a feed may report speed in. */
    public enum SpeedUnit {
        KNOTS(0.514444),
        METERS_PER_SECOND(1.0),
        KILOMETERS_PER_HOUR(0.277778),
        MILES_PER_HOUR(0.44704);

        private final double metersPerSecond;

        SpeedUnit(double metersPerSecond) {
            this.metersPerSecond = metersPerSecond;
        }

        public double toMetersPerSecond(double value) {
            return value * metersPerSecond;
        }

        public String wireValue() {
            return name().toLowerCase();
        }
    }
}
