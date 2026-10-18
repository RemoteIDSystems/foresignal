package ai.foresignal.search;

/**
 * Whether the database can store vectors: the pgvector extension exists, as checked at start
 * ({@code VectorExtensionProbe}; the Flyway baseline creates the extension, Architecture §25).
 *
 * <p>A database migrated from the baseline always has it. A development database adopted with
 * {@code baseline-on-migrate} (V1 not run) may not; then {@link #available()} is false and {@link #reason()}
 * says why, text search keeps working and {@code GET /v1/search/status} reports the reason.
 */
public class SemanticCapability {

    private volatile boolean available;
    private volatile String reason = "The pgvector extension has not been checked yet";

    /** True when the {@code vector} extension exists in the database. */
    public boolean available() {
        return available;
    }

    /** Why vectors are unavailable, or null when they are available. */
    public String reason() {
        return available ? null : reason;
    }

    /** Records that the extension exists. */
    public void markAvailable() {
        this.available = true;
        this.reason = null;
    }

    /**
     * Records that the extension is missing.
     *
     * @param reason the explanation shown in the search status
     */
    public void markUnavailable(String reason) {
        this.available = false;
        this.reason = reason;
    }
}
