package ai.foresignal.source.spi;

/**
 * How much a numeric payload field must change to be worth a new version
 * (Core-Data-Model §5.2).
 *
 * <p>Compared against the last persisted value, not the last seen one, so a field drifting in small
 * steps still produces a version once the total drift crosses the threshold.
 *
 * <p>Threshold fields must not be listed as significant: if every change already mattered, the
 * threshold would never be consulted.
 *
 * @param field top-level payload key
 * @param delta the change required
 * @param angular true for degrees, where 359 to 1 is a change of 2 rather than 358
 */
public record FieldThreshold(String field, double delta, boolean angular) {

    public FieldThreshold {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("threshold.field is required");
        }
        if (!(delta > 0)) {
            throw new IllegalArgumentException(
                    "threshold.delta for '" + field + "' must be greater than zero, but was " + delta);
        }
    }

    public static FieldThreshold of(String field, double delta) {
        return new FieldThreshold(field, delta, false);
    }

    public static FieldThreshold angular(String field, double degrees) {
        return new FieldThreshold(field, degrees, true);
    }

    /** True when {@code candidate} differs from {@code persisted} by at least {@link #delta}. */
    public boolean exceeded(double persisted, double candidate) {
        double difference = Math.abs(candidate - persisted);
        if (angular) {
            difference = difference % 360.0;
            if (difference > 180.0) {
                difference = 360.0 - difference;
            }
        }
        return difference >= delta;
    }
}
