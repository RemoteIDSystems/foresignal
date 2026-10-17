package ai.foresignal.object;

import ai.foresignal.security.Scope;
import java.util.Set;

/**
 * Who asks for objects: the principal behind the token, its scope, whether it may see private objects (039) and the
 * sources hidden from it. Built by {@link ObjectCallers} from a REST request or an MCP caller.
 *
 * @param principal the principal id ({@code token:<id>} for a token without one), or null when anonymous
 * @param actor the actor recorded on writes ({@code admin} or {@code token:<name>})
 * @param scope the token's scope, or null when anonymous (an {@code openReads} caller)
 * @param seesPrivate true when the caller may see private objects
 * @param excludedSources sources the caller may not read (private and restriction-hidden)
 */
public record ObjectCaller(String principal, String actor, Scope scope, boolean seesPrivate,
        Set<String> excludedSources) {

    /** Normalizes the excluded sources. */
    public ObjectCaller {
        excludedSources = excludedSources == null ? Set.of() : Set.copyOf(excludedSources);
    }

    /** A system caller that sees everything (projector, reconcile). */
    public static ObjectCaller system() {
        return new ObjectCaller("system", "system", Scope.ADMIN, true, Set.of());
    }

    /** True when no token was presented. */
    public boolean anonymous() {
        return scope == null;
    }

    /** True when the caller holds at least {@code required} (anonymous counts as read). */
    public boolean allows(Scope required) {
        if (scope == null) {
            return required == Scope.READ;
        }
        return scope.includes(required);
    }

    /** True for an admin token. */
    public boolean admin() {
        return scope == Scope.ADMIN;
    }

    /** True when the caller holds agent scope or more. */
    public boolean agent() {
        return allows(Scope.AGENT) && scope != null;
    }

    /** True when {@code sourceId} is readable by the caller. */
    public boolean canRead(String sourceId) {
        return sourceId == null || !excludedSources.contains(sourceId);
    }
}
