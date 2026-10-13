package ai.foresignal.mapping;

/**
 * A non-fatal finding about a mapping or its result (OpenAPI {@code MappingWarning}, prompt 059).
 *
 * @param code a stable code, one of the constants below
 * @param message what was found
 * @param path where, such as {@code mapping.fields[3]}, or null
 */
public record MappingWarning(String code, String message, String path) {

    /** A top-level input key that no path reads and {@code drop} does not list. */
    public static final String UNMAPPED_INPUT_KEY = "UNMAPPED_INPUT_KEY";

    /** A produced field the source's {@code fieldSpecs} does not declare. */
    public static final String FIELD_NOT_IN_FIELD_SPECS = "FIELD_NOT_IN_FIELD_SPECS";

    /** A SAMPLED or LATEST source's significant field that the mapping does not produce. */
    public static final String SIGNIFICANT_FIELD_MISSING = "SIGNIFICANT_FIELD_MISSING";

    /** {@code observedAt} disagrees with the source's {@code observedAtMode}. */
    public static final String TIME_BASE_MISMATCH = "TIME_BASE_MISMATCH";

    /** A field value that could not be coerced to its type, or was NaN or infinite; the field was left out. */
    public static final String FIELD_REJECTED = "FIELD_REJECTED";

    /** A coordinate outside the valid range (an AIS sentinel such as lat 91); the geometry was left out. */
    public static final String GEOMETRY_OUT_OF_RANGE = "GEOMETRY_OUT_OF_RANGE";

    /** A kind the mapping produces that the source does not declare. */
    public static final String UNKNOWN_KIND = "UNKNOWN_KIND";
}
