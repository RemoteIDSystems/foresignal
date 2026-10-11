package ai.elaro.collect;

import ai.elaro.api.error.InvalidInputException;
import java.time.Duration;

/** Bucket size for a usage series. */
public enum UsageResolution {
    MINUTE("minute", Duration.ofMinutes(1)),
    HOUR("hour", Duration.ofHours(1)),
    DAY("day", Duration.ofDays(1));

    private final String sqlField;
    private final Duration bucketSize;

    UsageResolution(String sqlField, Duration bucketSize) {
        this.sqlField = sqlField;
        this.bucketSize = bucketSize;
    }

    /**
     * The {@code date_trunc} field name.
     *
     * <p>Comes from this closed enum rather than from a caller, so it can be interpolated into the
     * query without becoming an injection point.
     */
    public String sqlField() {
        return sqlField;
    }

    public Duration bucketSize() {
        return bucketSize;
    }

    public String wireValue() {
        return name().toLowerCase();
    }

    /** Parses a wire value. */
    public static UsageResolution fromWireValue(String value) {
        if (value == null || value.isBlank()) {
            return MINUTE;
        }
        return switch (value.trim().toLowerCase()) {
            case "minute" -> MINUTE;
            case "hour" -> HOUR;
            case "day" -> DAY;
            default ->
                    throw new InvalidInputException(
                            "Unknown resolution '" + value + "'. Expected minute, hour or day.");
        };
    }
}
