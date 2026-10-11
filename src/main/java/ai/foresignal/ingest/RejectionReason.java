package ai.foresignal.ingest;

/**
 * Why the normalizer refused one record (Core-Data-Model §3, §14). A rejection never fails the
 * batch.
 */
public enum RejectionReason {
    /** {@code externalId} is blank or longer than 256 characters. */
    EXTERNAL_ID_INVALID,
    /** {@code kind} is not one of the descriptor's kinds. */
    UNKNOWN_KIND,
    /** {@code observedAtMode=UPSTREAM} and the record has no {@code observedAt}. */
    MISSING_OBSERVED_AT,
    /** {@code observed_at} is before 1970-01-02. */
    OBSERVED_AT_TOO_OLD,
    /** The serialized payload is larger than the limit. */
    PAYLOAD_TOO_LARGE,
    /** The payload nests deeper than the limit. */
    PAYLOAD_TOO_DEEP,
    /** The payload has more keys, counted at every level, than the limit. */
    PAYLOAD_TOO_MANY_KEYS,
    /** A payload string is longer than the limit. */
    PAYLOAD_STRING_TOO_LONG,
    /** A payload number is NaN or infinite. */
    PAYLOAD_NON_FINITE_NUMBER,
    /** A payload value is not representable as JSON. */
    PAYLOAD_UNSUPPORTED_TYPE,
    /** More observers than the limit. */
    TOO_MANY_OBSERVERS,
    /** More geometry vertices than the limit, counted before repair. */
    GEOMETRY_TOO_MANY_VERTICES,
    /** The geometry carries an SRID other than 4326. */
    GEOMETRY_WRONG_SRID,
    /** A coordinate is outside ±180 / ±90, or not finite. */
    COORDINATES_OUT_OF_RANGE,
    /** Quality fails §14 validation. */
    QUALITY_INVALID,
    /** {@code validFrom} is after {@code validUntil}. */
    VALID_RANGE_INVERTED
}
