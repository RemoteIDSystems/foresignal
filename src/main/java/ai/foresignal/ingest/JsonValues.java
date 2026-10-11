package ai.foresignal.ingest;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.Duration;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Converts adapter payloads into plain JSON values and reads values by path.
 *
 * <p>After {@link #normalize}, a payload contains only {@code Map<String,Object>},
 * {@code List<Object>}, {@code String}, {@code Boolean}, {@code Long}, {@code Double},
 * {@code BigInteger} and {@code BigDecimal}, so hashing ({@link CanonicalJson}) and storage
 * (Jackson, prompt 009) see the same values. Null map values are dropped at every level, because
 * payloads carry no nulls (Core-Data-Model §3, adapter rule 8).
 */
public final class JsonValues {

    private JsonValues() {}

    /** A value that has no JSON representation. */
    public static final class UnsupportedValueException extends IllegalArgumentException {

        private static final long serialVersionUID = 1L;

        UnsupportedValueException(String message) {
            super(message);
        }
    }

    /**
     * A JSON-typed, unmodifiable copy of {@code payload} with null map values removed recursively.
     *
     * <p>Strings stand in for enums, characters, UUIDs, URIs, durations and {@code java.time}
     * values ({@code toString()}); integral numbers become {@code Long}, floats become the
     * {@code Double} of their shortest decimal form.
     *
     * @throws UnsupportedValueException when a value has no JSON representation or a map key is
     *     not a string
     */
    public static Map<String, Object> normalize(Map<String, ?> payload) {
        if (payload == null || payload.isEmpty()) {
            return Map.of();
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) convert(payload);
        return result;
    }

    private static Object convert(Object value) {
        return switch (value) {
            case null -> null;
            case String s -> s;
            case Boolean b -> b;
            case Long l -> l;
            case Integer i -> i.longValue();
            case Short s -> s.longValue();
            case Byte b -> b.longValue();
            case Double d -> d;
            case Float f -> Double.parseDouble(Float.toString(f));
            case BigInteger big -> big;
            case BigDecimal big -> big;
            case Number n -> new BigDecimal(n.toString());
            case CharSequence cs -> cs.toString();
            case Character c -> c.toString();
            case Enum<?> e -> e.name();
            case UUID u -> u.toString();
            case URI u -> u.toString();
            case Duration d -> d.toString();
            case TemporalAccessor t -> t.toString();
            case Map<?, ?> map -> {
                Map<String, Object> copy = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (!(entry.getKey() instanceof String key)) {
                        throw new UnsupportedValueException(
                                "Payload keys must be strings, but found " + entry.getKey());
                    }
                    Object converted = convert(entry.getValue());
                    if (converted != null) {
                        copy.put(key, converted);
                    }
                }
                yield Collections.unmodifiableMap(copy);
            }
            case Collection<?> collection -> {
                List<Object> copy = new ArrayList<>(collection.size());
                for (Object element : collection) {
                    copy.add(convert(element));
                }
                yield Collections.unmodifiableList(copy);
            }
            default -> {
                if (value.getClass().isArray()) {
                    int length = Array.getLength(value);
                    List<Object> copy = new ArrayList<>(length);
                    for (int i = 0; i < length; i++) {
                        copy.add(convert(Array.get(value, i)));
                    }
                    yield Collections.unmodifiableList(copy);
                }
                throw new UnsupportedValueException(
                        "Payload value of type " + value.getClass().getName() + " has no JSON form");
            }
        };
    }

    /**
     * The value at a dot-separated path, such as {@code position.lat}, or null when any step is
     * missing or not an object.
     */
    public static Object at(Map<String, ?> root, String path) {
        if (root == null || path == null || path.isEmpty()) {
            return null;
        }
        Object current = root;
        int start = 0;
        while (true) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            int dot = path.indexOf('.', start);
            String step = dot < 0 ? path.substring(start) : path.substring(start, dot);
            current = map.get(step);
            if (dot < 0 || current == null) {
                return current;
            }
            start = dot + 1;
        }
    }

    /** {@code value} as a double when it is a finite number, else null. */
    public static Double finiteNumber(Object value) {
        if (value instanceof Number number) {
            double d = number.doubleValue();
            return Double.isFinite(d) ? d : null;
        }
        return null;
    }

    /** {@code value} rendered as text for titles and search: strings as-is, numbers canonical. */
    public static String text(Object value) {
        return switch (value) {
            case null -> null;
            case String s -> s;
            case Number n -> {
                try {
                    yield CanonicalJson.number(n);
                } catch (IllegalArgumentException e) {
                    yield null;
                }
            }
            case Boolean b -> b.toString();
            default -> null;
        };
    }
}
