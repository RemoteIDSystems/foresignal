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

    /**
     * A token of at least {@code scope}.
     *
     * <p>{@code READ} is waived while {@code security.openReads} is on, unless
     * {@code alwaysAuthenticated} is set: cost-bearing operations (LLM calls, search, Web Push,
     * ticket minting) need a token even then (prompt 023, invariant SEC-5). The flag mirrors the
     * spec's {@code x-foresignal-always-authenticated}; on {@code agent} and {@code admin}
     * requirements it changes nothing, because those always need a token.
     *
     * @param scope the least scope that satisfies the requirement
     * @param alwaysAuthenticated true when a token is required even under {@code openReads}
     */
    record RequiresScope(Scope scope, boolean alwaysAuthenticated) implements AccessRequirement {

        /** A requirement that follows {@code openReads} (not always authenticated). */
        public RequiresScope(Scope scope) {
            this(scope, false);
        }
    }

    /**
     * A per-source ingest key, or an admin token.
     *
     * <p>Ingest keys arrive with custom sources in prompt 024 (Architecture §16); until then only
     * an admin token satisfies this.
     */
    record IngestKeyOrAdmin() implements AccessRequirement {}

    /**
     * A federation peer ingest key ({@code fsp_…}, prompt 113) sent as a Bearer token: the instance-to-instance
     * endpoints ({@code x-foresignal-scope: peer}). No API token satisfies it, not even admin.
     */
    record PeerKey() implements AccessRequirement {}

    AccessRequirement OPEN = new Open();
    AccessRequirement READ = new RequiresScope(Scope.READ);
    AccessRequirement AGENT = new RequiresScope(Scope.AGENT);
    AccessRequirement ADMIN = new RequiresScope(Scope.ADMIN);

    /** {@code read}, but a token is required even while {@code openReads} is on. */
    AccessRequirement READ_ALWAYS_AUTHENTICATED = new RequiresScope(Scope.READ, true);

    /** {@code agent}, marked always authenticated to agree with the spec. */
    AccessRequirement AGENT_ALWAYS_AUTHENTICATED = new RequiresScope(Scope.AGENT, true);

    /** {@code admin}, marked always authenticated to agree with the spec. */
    AccessRequirement ADMIN_ALWAYS_AUTHENTICATED = new RequiresScope(Scope.ADMIN, true);

    AccessRequirement INGEST_KEY_OR_ADMIN = new IngestKeyOrAdmin();

    /** A federation peer ingest key (113). */
    AccessRequirement PEER = new PeerKey();

    /** True when this requirement needs a token even while {@code openReads} is on. */
    default boolean alwaysAuthenticated() {
        return this instanceof RequiresScope required && required.alwaysAuthenticated();
    }
}
