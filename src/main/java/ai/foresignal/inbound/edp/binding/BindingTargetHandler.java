package ai.foresignal.inbound.edp.binding;

import ai.foresignal.api.error.FieldError;
import java.util.List;
import java.util.Map;

/**
 * One binding target (prompt 138, docs/External-Data-Protocol.md §5). The service validates a binding's
 * configuration against the stream with {@link #validate}; the router calls {@link #apply} for each matching record
 * version (at most once per binding and version), and a preview calls {@link #plan}, which never has side effects.
 */
public interface BindingTargetHandler {

    /** The target name ({@link BindingVocabulary#TARGETS}). */
    String target();

    /**
     * Checks the target configuration for {@code stream} (the item type was checked already).
     *
     * @param config the configuration
     * @param stream the stream
     * @return the problems found (empty when valid)
     */
    List<FieldError> validate(Map<String, Object> config, BoundStream stream);

    /** What applying {@code item} would do; never changes anything. */
    BindingEffect plan(ExternalBindingEntity binding, BoundStream stream, BoundItem item);

    /** Applies {@code item}. */
    BindingEffect apply(ExternalBindingEntity binding, BoundStream stream, BoundItem item);

    /**
     * Sets up what the binding needs once, when it is created or replaced (the chronicle layer of a {@code chronicle}
     * binding).
     *
     * @return the reference of what was set up, or null
     */
    default String setUp(ExternalBindingEntity binding, BoundStream stream) {
        return null;
    }
}
