package ai.foresignal.sink;

import java.util.Map;

/**
 * A sink type (prompt 110; Spring bean per type). {@link #open} starts a running {@link SinkInstance};
 * the supervisor drives it. {@link #validate} rejects an unusable config or secret before persistence.
 */
public interface SinkType {

    /** The type id. */
    String id();

    /** The type metadata. */
    SinkTypeSpec spec();

    /** Validates the config and secrets, throwing {@code ValidationException} on a problem. */
    default void validate(Map<String, Object> config, Map<String, String> secrets) {
    }

    /** Opens a running instance. */
    SinkInstance open(SinkConfig config);
}
