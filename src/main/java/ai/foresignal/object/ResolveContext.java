package ai.foresignal.object;

import java.time.Instant;

/**
 * What a resolver needs besides the reference: the caller and the optional {@code asOf} (transaction time,
 * docs/Client-Contract.md §7).
 *
 * @param caller who asks
 * @param asOf the knowledge cutoff, or null for now
 */
public record ResolveContext(ObjectCaller caller, Instant asOf) {

    /** A context for the system caller at the present time. */
    public static ResolveContext system() {
        return new ResolveContext(ObjectCaller.system(), null);
    }

    /** True when the caller may see private objects. */
    public boolean seesPrivate() {
        return caller.seesPrivate();
    }

    /** True when an object created at {@code createdAt} existed at {@code asOf} (always true without asOf). */
    public boolean existedAt(Instant createdAt) {
        return asOf == null || createdAt == null || !createdAt.isAfter(asOf);
    }
}
