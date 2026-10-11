package ai.foresignal.security;

/**
 * What a request must present to be allowed through.
 *
 * <p>Kept as a small closed set rather than a bare {@link Scope} so the two cases that are not a
 * scope — fully open endpoints, and ingest which accepts a per-source key — are explicit instead of
 * being special-cased inside the filter.
 */
public sealed interface AccessRequirement {

    /** No credential needed, ever. */
    record Open() implements AccessRequirement {}

    /** A token of at least {@code scope}. {@code READ} is waived while {@code openReads} is on. */
    record RequiresScope(Scope scope) implements AccessRequirement {}

    /**
     * A per-source ingest key, or an admin token.
     *
     * <p>Ingest keys arrive with custom sources in prompt 020 (Architecture §16); until then only
     * the admin token satisfies this.
     */
    record IngestKeyOrAdmin() implements AccessRequirement {}

    AccessRequirement OPEN = new Open();
    AccessRequirement READ = new RequiresScope(Scope.READ);
    AccessRequirement AGENT = new RequiresScope(Scope.AGENT);
    AccessRequirement ADMIN = new RequiresScope(Scope.ADMIN);
    AccessRequirement INGEST_KEY_OR_ADMIN = new IngestKeyOrAdmin();
}
