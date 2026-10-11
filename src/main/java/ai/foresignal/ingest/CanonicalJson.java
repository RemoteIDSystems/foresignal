package ai.foresignal.ingest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Canonical JSON for hashing (Core-Data-Model §4).
 *
 * <p>Deterministic across JVMs and runs: object keys are sorted recursively by their UTF-16 code
 * units, there is no whitespace, and numbers are written in plain form with trailing zeros
 * stripped, so {@code 5}, {@code 5L} and {@code 5.0} are all written {@code 5} and {@code 1e3} is
 * written {@code 1000}. Strings are escaped minimally ({@code "}, {@code \}, and control
 * characters as {@code \}{@code uXXXX}).
 *
 * <p>Accepts the JSON value types {@link JsonValues#normalize} produces — maps with string keys,
 * collections, strings, booleans, numbers and null — and rejects anything else, and every NaN or
 * infinite number, with {@link IllegalArgumentException}.
 */
public final class CanonicalJson {

    private CanonicalJson() {}

    /** The canonical form of {@code value}. */
    public static String write(Object value) {
        StringBuilder out = new StringBuilder(128);
        append(out, value);
        return out.toString();
    }

    /** The canonical form's UTF-8 bytes. */
    public static byte[] bytes(Object value) {
        return write(value).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * A number's canonical text: plain notation, trailing zeros stripped, integral values without
     * a fraction.
     *
     * @throws IllegalArgumentException for NaN or an infinity
     */
    public static String number(Number number) {
        BigDecimal decimal = toBigDecimal(number);
        if (decimal == null) {
            return number instanceof BigInteger big ? big.toString() : Long.toString(number.longValue());
        }
        BigDecimal stripped = decimal.stripTrailingZeros();
        if (stripped.signum() == 0) {
            return "0";
        }
        if (stripped.scale() <= 0) {
            return stripped.toBigIntegerExact().toString();
        }
        return stripped.toPlainString();
    }

    private static BigDecimal toBigDecimal(Number number) {
        if (number instanceof Double d) {
            checkFinite(d);
            return BigDecimal.valueOf(d);
        }
        if (number instanceof Float f) {
            checkFinite(f);
            // Float.toString gives the shortest decimal that round-trips the float, so 5.1f is
            // written 5.1 rather than its widened double expansion.
            return new BigDecimal(Float.toString(f));
        }
        if (number instanceof BigDecimal bd) {
            return bd;
        }
        if (number instanceof Long
                || number instanceof Integer
                || number instanceof Short
                || number instanceof Byte
                || number instanceof BigInteger) {
            return null;
        }
        double d = number.doubleValue();
        checkFinite(d);
        return new BigDecimal(number.toString());
    }

    private static void checkFinite(double d) {
        if (!Double.isFinite(d)) {
            throw new IllegalArgumentException("JSON has no representation for " + d);
        }
    }

    private static void append(StringBuilder out, Object value) {
        switch (value) {
            case null -> out.append("null");
            case String s -> appendString(out, s);
            case Boolean b -> out.append(b ? "true" : "false");
            case Number n -> out.append(number(n));
            case Map<?, ?> map -> appendObject(out, map);
            case Collection<?> collection -> {
                out.append('[');
                boolean first = true;
                for (Object element : collection) {
                    if (!first) {
                        out.append(',');
                    }
                    first = false;
                    append(out, element);
                }
                out.append(']');
            }
            default ->
                    throw new IllegalArgumentException(
                            "Not a JSON value: " + value.getClass().getName());
        }
    }

    private static void appendObject(StringBuilder out, Map<?, ?> map) {
        TreeMap<String, Object> sorted = new TreeMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException("JSON object keys must be strings: " + entry.getKey());
            }
            sorted.put(key, entry.getValue());
        }
        out.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> entry : sorted.entrySet()) {
            if (!first) {
                out.append(',');
            }
            first = false;
            appendString(out, entry.getKey());
            out.append(':');
            append(out, entry.getValue());
        }
        out.append('}');
    }

    private static void appendString(StringBuilder out, String s) {
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        out.append('"');
    }
}
