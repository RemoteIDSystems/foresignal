package ai.foresignal.export;

/**
 * The name of {@code record_version_held}, the plain archive-then-delete copy table for legal holds (prompt 111,
 * Architecture §25). It mirrors {@code record_version}'s columns plus {@code hold_id}, with {@code (hold_id, seq)}
 * as the primary key and GiST and {@code (source_id, external_id, seq)} indexes. The Flyway baseline
 * ({@code db/migration/V1__baseline.sql}) creates it; Hibernate does not map it, and every access is native SQL.
 */
public final class HeldVersionsTable {

    /** The held-versions table. */
    public static final String NAME = "record_version_held";

    private HeldVersionsTable() {}
}
