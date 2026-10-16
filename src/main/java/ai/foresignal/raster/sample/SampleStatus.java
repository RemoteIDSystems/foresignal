package ai.foresignal.raster.sample;

/**
 * The status of one sampled value (prompt 052; OpenAPI {@code SampleStatus}). Anything but {@link #OK}
 * means the value is unknown, never that the weather is clear; a non-ok value always carries
 * {@code statusText}.
 */
public enum SampleStatus {

    /** A value from a current frame. */
    OK("ok", null),

    /** Outside the grid or footprint, or a no-coverage code (MRMS code 0). */
    NO_COVERAGE("no-coverage", "outside product coverage; this is not evidence of clear weather"),

    /** Inside coverage, but the sample is missing (nodata). */
    NO_DATA("no-data", "inside product coverage but no data at this point; this is not evidence of clear weather"),

    /** The frame is older than the product's {@code maxGap} relative to the requested time. */
    STALE("stale", "the newest frame is older than the product's maximum gap; the value may be out of date"),

    /** The product is disabled, its source is failing, it has no frame, or the frame expired. */
    UNAVAILABLE("unavailable", "the product has no usable frame; this is not evidence of clear weather"),

    /** The position sample's entity was last seen more than 15 minutes ago (prompt 106). */
    STALE_POSITION("stale-position", "the sampled entity's position is more than 15 minutes old; the location may be out of date");

    private final String wire;
    private final String defaultText;

    SampleStatus(String wire, String defaultText) {
        this.wire = wire;
        this.defaultText = defaultText;
    }

    /** The wire value ({@code ok}, {@code no-coverage}, …). */
    public String wireValue() {
        return wire;
    }

    /** The standard explanation of a non-ok status, or null for {@link #OK}. */
    public String defaultText() {
        return defaultText;
    }

    /** True for {@link #OK}. */
    public boolean ok() {
        return this == OK;
    }
}
