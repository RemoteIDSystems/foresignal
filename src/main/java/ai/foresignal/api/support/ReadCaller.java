package ai.foresignal.api.support;

import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.ReadRestriction;

/**
 * Who is reading, as far as the read endpoints care.
 *
 * @param scope the token's scope, or null for an anonymous {@code openReads} caller
 * @param exposeObserver the {@code security.exposeObserver} setting
 * @param openReads the {@code security.openReads} setting (drives {@code Cache-Control})
 * @param restriction the caller's effective read restriction (prompt 035; never null)
 */
public record ReadCaller(Scope scope, boolean exposeObserver, boolean openReads, ReadRestriction restriction) {

    /** A null restriction becomes {@link ReadRestriction#NONE}. */
    public ReadCaller {
        restriction = restriction == null ? ReadRestriction.NONE : restriction;
    }

    /** An unrestricted caller (before prompt 035's restrictions). */
    public ReadCaller(Scope scope, boolean exposeObserver, boolean openReads) {
        this(scope, exposeObserver, openReads, ReadRestriction.NONE);
    }

    /** An anonymous caller with the shipped settings. */
    public static ReadCaller anonymous() {
        return new ReadCaller(null, false, true);
    }

    /**
     * True when the caller's restriction narrows what it reads; such responses must be
     * {@code Cache-Control: private} and must not share a cache entry with other callers.
     */
    public boolean restricted() {
        return restriction.hasDataRestrictions();
    }

    /**
     * True when responses may carry {@code Cache-Control: public}: reads are open and the caller is
     * not restricted (a restricted response is always {@code private}, prompt 035).
     */
    public boolean publicCache() {
        return openReads && !restricted();
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
        String r = restriction.canonical();
        return (scope == null ? "anon" : scope.wireValue()) + "/" + exposeObserver + (r.isEmpty() ? "" : "/" + r);
    }
}
