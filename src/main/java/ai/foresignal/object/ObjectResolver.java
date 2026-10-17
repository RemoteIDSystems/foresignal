package ai.foresignal.object;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The SPI through which an owning package makes its objects resolvable (prompt 129): one Spring bean per object
 * type, discovered by {@link ObjectService}. A resolver returns the envelope without {@code actions} (computed
 * centrally by {@link ObjectActions}), or empty when the object does not exist, did not exist at {@code asOf}, or is
 * invisible to the caller (private objects are invisible, not forbidden).
 */
public interface ObjectResolver {

    /** The object type this resolver owns, one of {@link ObjectTypes#ALL}. */
    String type();

    /**
     * The envelope of {@code ref}.
     *
     * @param ref a reference of {@link #type()}
     * @param context the caller and {@code asOf}
     * @return the envelope, or empty when missing or invisible
     */
    Optional<ObjectEnvelope> resolve(ObjectRef ref, ResolveContext context);

    /**
     * Envelopes for several references of {@link #type()}; missing ones are left out. Resolvers with a cheaper batch
     * read override it.
     */
    default Map<ObjectRef, ObjectEnvelope> resolveAll(List<ObjectRef> refs, ResolveContext context) {
        Map<ObjectRef, ObjectEnvelope> out = new LinkedHashMap<>();
        for (ObjectRef ref : refs) {
            resolve(ref, context).ifPresent(e -> out.put(ref, e));
        }
        return out;
    }

    /** True when {@code asOf} is honored for this type (otherwise the current state is returned). */
    default boolean asOfAware() {
        return false;
    }
}
