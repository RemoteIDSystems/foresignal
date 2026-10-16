package ai.foresignal.ogc.cql2;

import ai.foresignal.source.spi.FieldType;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The properties a CQL2 filter may name for one collection: the built-in record properties
 * ({@code title}, {@code sourceId}, {@code observedAt}, {@code eventTime}, {@code version} and the
 * spatial property {@code geometry}) plus the top-level payload keys the kind's {@code fieldSpecs}
 * declare (prompt 008).
 *
 * @param payload the declared payload keys and their types, in declaration order
 */
public record Cql2Queryables(Map<String, FieldType> payload) {

    /** The spatial queryable (the record's geometry). */
    public static final String GEOMETRY = "geometry";

    /** The built-in queryables and their types; they win over a payload key of the same name. */
    public static final Map<String, Type> BUILT_IN;

    static {
        Map<String, Type> m = new LinkedHashMap<>();
        m.put("title", Type.STRING);
        m.put("sourceId", Type.STRING);
        m.put("observedAt", Type.TIMESTAMP);
        m.put("eventTime", Type.TIMESTAMP);
        m.put("version", Type.INTEGER);
        m.put(GEOMETRY, Type.GEOMETRY);
        BUILT_IN = java.util.Collections.unmodifiableMap(m);
    }

    /** The type of a queryable as the translator sees it. */
    public enum Type {
        /** A string. */
        STRING,
        /** A number. */
        NUMBER,
        /** An integer. */
        INTEGER,
        /** A boolean. */
        BOOLEAN,
        /** An RFC 3339 instant. */
        TIMESTAMP,
        /** A JSON object (only {@code IS NULL} applies). */
        OBJECT,
        /** A JSON array (only {@code IS NULL} applies). */
        ARRAY,
        /** The record geometry. */
        GEOMETRY;

        /** The queryable type of a field spec type. */
        public static Type of(FieldType type) {
            return switch (type) {
                case STRING -> STRING;
                case INTEGER -> INTEGER;
                case NUMBER -> NUMBER;
                case BOOLEAN -> BOOLEAN;
                case TIMESTAMP -> TIMESTAMP;
                case OBJECT -> OBJECT;
                case ARRAY -> ARRAY;
            };
        }
    }

    /** Copies the map, keeping its order. */
    public Cql2Queryables {
        payload = java.util.Collections.unmodifiableMap(new LinkedHashMap<>(payload == null ? Map.of() : payload));
    }

    /** True when {@code name} is a built-in queryable. */
    public static boolean isBuiltIn(String name) {
        return BUILT_IN.containsKey(name);
    }

    /** The type of {@code name}, or null when it is not a queryable of this collection. */
    public Type type(String name) {
        Type builtIn = BUILT_IN.get(name);
        if (builtIn != null) {
            return builtIn;
        }
        FieldType field = payload.get(name);
        return field == null ? null : Type.of(field);
    }
}
