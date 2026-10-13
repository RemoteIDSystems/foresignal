package ai.foresignal.source.support;

import ai.foresignal.source.spi.NormalizedRecord;

/**
 * What a pure per-feature mapper made of one upstream entry (prompt 075): a record, or the reason it was skipped.
 *
 * @param record the record, or null when skipped
 * @param reason the skip reason ({@code missing-id}, {@code missing-observed-at}, {@code bad-geometry}, …), or null
 *     when accepted
 */
public record MappedRecord(NormalizedRecord record, String reason) {

    /** The skip reason of an entry without an identifier. */
    public static final String MISSING_ID = "missing-id";

    /** The skip reason of an entry without the time its source's time base names. */
    public static final String MISSING_OBSERVED_AT = "missing-observed-at";

    /** The skip reason of an entry whose geometry could not be read. */
    public static final String BAD_GEOMETRY = "bad-geometry";

    /** The skip reason of a repeated identifier. */
    public static final String DUPLICATE_ID = "duplicate-id";

    /** An accepted entry. */
    public static MappedRecord of(NormalizedRecord record) {
        return new MappedRecord(record, null);
    }

    /** A skipped entry. */
    public static MappedRecord skipped(String reason) {
        return new MappedRecord(null, reason);
    }

    /** True when the entry became a record. */
    public boolean accepted() {
        return record != null;
    }
}
