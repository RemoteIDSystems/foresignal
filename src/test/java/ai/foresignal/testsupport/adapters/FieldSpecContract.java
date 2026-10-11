package ai.foresignal.testsupport.adapters;

import static org.assertj.core.api.Assertions.fail;

import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The fieldSpecs contract (Gap-Analysis §8.A item 3; Adapter Guide): every payload key an adapter
 * emits is declared in its descriptor's {@code fieldSpecs}, and every declared key holds the JSON
 * type its {@link FieldType} says.
 *
 * <p>Every adapter's parser contract test calls {@link #assertPayloadKeysDeclared} on the records
 * parsed from each of its fixtures. An undeclared key would be published by the kind schema endpoint
 * (prompt 013) without a type, unit or label; a key whose type contradicts its spec would be
 * mislabeled or mis-converted by every client.
 */
public final class FieldSpecContract {

    private FieldSpecContract() {}

    /**
     * Fails, listing every problem at once, when a payload key is not declared for its record's kind
     * or a declared key's value contradicts the declared type.
     *
     * <p>A spec applies to a record when its {@link FieldSpec#kinds()} is empty or contains the
     * record's kind. Tombstones are skipped; their payload is empty by definition.
     *
     * <p>Type rules: STRING is a string; INTEGER an integral number; NUMBER any finite number;
     * BOOLEAN a boolean; TIMESTAMP an ISO-8601 instant string or an integral epoch count; OBJECT a
     * map; ARRAY a list.
     *
     * @param descriptor the adapter's descriptor
     * @param records records the adapter's parser produced
     */
    public static void assertPayloadKeysDeclared(SourceDescriptor descriptor, List<NormalizedRecord> records) {
        Map<String, FieldSpec> specs = descriptor.fieldSpecs();
        Set<String> undeclared = new TreeSet<>();
        List<String> mistyped = new ArrayList<>();
        for (NormalizedRecord record : records) {
            if (record.tombstone()) {
                continue;
            }
            for (Map.Entry<String, Object> entry : record.payload().entrySet()) {
                String key = entry.getKey();
                FieldSpec spec = specs.get(key);
                if (spec == null || !spec.appliesTo(record.kind())) {
                    undeclared.add(record.kind() + "." + key);
                    continue;
                }
                Object value = entry.getValue();
                if (!matches(spec.type(), value)) {
                    mistyped.add(
                            record.externalId()
                                    + "."
                                    + key
                                    + " is declared "
                                    + spec.type()
                                    + " but holds "
                                    + describe(value));
                }
            }
        }
        if (!undeclared.isEmpty() || !mistyped.isEmpty()) {
            StringBuilder message = new StringBuilder(descriptor.id()).append(" violates the fieldSpecs contract.");
            if (!undeclared.isEmpty()) {
                message.append("\nUndeclared payload keys (kind.key): ").append(undeclared);
            }
            if (!mistyped.isEmpty()) {
                message.append("\nValues contradicting their FieldType:");
                mistyped.forEach(problem -> message.append("\n  ").append(problem));
            }
            fail(message.toString());
        }
    }

    /** True when {@code value} is a JSON value of {@code type}; null is never emitted (rule 8). */
    static boolean matches(FieldType type, Object value) {
        if (value == null || type == null) {
            return false;
        }
        return switch (type) {
            case STRING -> value instanceof String;
            case INTEGER -> isIntegral(value);
            case NUMBER -> isFiniteNumber(value);
            case BOOLEAN -> value instanceof Boolean;
            case TIMESTAMP -> isIntegral(value) || (value instanceof String text && isInstant(text));
            case OBJECT -> value instanceof Map<?, ?>;
            case ARRAY -> value instanceof List<?>;
        };
    }

    private static boolean isIntegral(Object value) {
        return value instanceof Long
                || value instanceof Integer
                || value instanceof Short
                || value instanceof Byte
                || value instanceof BigInteger;
    }

    private static boolean isFiniteNumber(Object value) {
        if (value instanceof Double d) {
            return Double.isFinite(d);
        }
        if (value instanceof Float f) {
            return Float.isFinite(f);
        }
        return isIntegral(value) || value instanceof BigDecimal;
    }

    private static boolean isInstant(String text) {
        try {
            Instant.parse(text);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private static String describe(Object value) {
        return value == null ? "null" : value.getClass().getSimpleName() + " " + value;
    }
}
