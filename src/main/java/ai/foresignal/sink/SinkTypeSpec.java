package ai.foresignal.sink;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A sink type's metadata (prompt 110; OpenAPI {@code SinkType}).
 *
 * @param id the type id
 * @param name a human name
 * @param description what it produces
 * @param kind CHANGE_FEED or STATE_MIRROR
 * @param modes the supported modes
 * @param configSchema JSON Schema 2020-12 of the type's {@code config}
 * @param secretFields write-only secret setting names
 * @param defaultMaxPerSecond 50 for PUSH, null (unlimited) for SERVE
 */
public record SinkTypeSpec(String id, String name, String description, SinkKind kind, Set<SinkMode> modes,
        Map<String, Object> configSchema, List<String> secretFields, Integer defaultMaxPerSecond) {

    /** Validates and copies. */
    public SinkTypeSpec {
        modes = modes == null ? Set.of() : Set.copyOf(modes);
        configSchema = configSchema == null ? Map.of() : Map.copyOf(configSchema);
        secretFields = secretFields == null ? List.of() : List.copyOf(secretFields);
    }
}
