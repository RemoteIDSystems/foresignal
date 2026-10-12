package ai.foresignal.api.support;

import ai.foresignal.security.Scope;

/**
 * Who is reading, as far as the read endpoints care.
 *
 * @param scope the token's scope, or null for an anonymous {@code openReads} caller
 * @param exposeObserver the {@code security.exposeObserver} setting
 * @param openReads the {@code security.openReads} setting (drives {@code Cache-Control})
 */
public record ReadCaller(Scope scope, boolean exposeObserver, boolean openReads) {

    /** An anonymous caller with the shipped settings. */
    public static ReadCaller anonymous() {
        return new ReadCaller(null, false, true);
    }

    /** True when no token was presented. */
    public boolean isAnonymous() {
        return scope == null;
    }

    /**
     * True when observers may be returned: agent or admin tokens, or anyone while
     * {@code security.exposeObserver} is on (Core-Data-Model §14).
     */
    public boolean mayReadObservers() {
        return exposeObserver || (scope != null && scope.includes(Scope.AGENT));
    }

    /** A stable text form, for entity tags. */
    public String canonical() {
        return (scope == null ? "anon" : scope.wireValue()) + "/" + exposeObserver;
    }
}
