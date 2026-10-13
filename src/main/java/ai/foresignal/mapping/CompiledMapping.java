package ai.foresignal.mapping;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import tools.jackson.databind.JsonNode;

/**
 * A validated, immutable {@code RecordMapping} (prompt 059), produced by {@link MappingCompiler} and applied by
 * {@link MappingEngine}.
 *
 * @param source the mapping as written
 * @param select the path producing the items, or null for one item per document
 * @param externalId the external id
 * @param kind the kind value, or null to use {@code defaultKind}
 * @param defaultKind the source's single kind when the mapping names none, else null
 * @param kinds the kinds the source declares, or null when unknown
 * @param observedAt the observation time
 * @param eventTime the event time, or null
 * @param validUntil the upstream expiry, or null
 * @param geometry the geometry, or null for none
 * @param tombstone the tombstone condition, or null
 * @param quality the quality values, or null
 * @param fields the payload fields, in order (a repeated name takes the first value present)
 * @param pairs the name/value flattening, or null
 * @param spread the copied object members, or null
 * @param drop input keys never stored
 * @param filters conditions every item must meet
 * @param referencedKeys the top-level input keys the mapping reads, for the unmapped-key warning
 * @param warnings findings that did not fail compilation (a time-base mismatch in preview)
 */
public record CompiledMapping(
        JsonNode source,
        JsonPath select,
        ValueSpec externalId,
        ValueSpec kind,
        String defaultKind,
        Set<String> kinds,
        TimeSpec observedAt,
        TimeSpec eventTime,
        TimeSpec validUntil,
        GeometrySpec geometry,
        Tombstone tombstone,
        QualitySpec quality,
        List<FieldMapping> fields,
        PairsSpec pairs,
        SpreadSpec spread,
        Set<String> drop,
        List<Filter> filters,
        Set<String> referencedKeys,
        List<MappingWarning> warnings) {

    /** Copies the collections. */
    public CompiledMapping {
        kinds = kinds == null ? null : Set.copyOf(kinds);
        fields = List.copyOf(fields);
        drop = Set.copyOf(drop);
        filters = List.copyOf(filters);
        referencedKeys = Set.copyOf(referencedKeys);
        warnings = List.copyOf(warnings);
    }

    /** The names of the fields this mapping can produce (spread members excluded). */
    public Set<String> fieldNames() {
        java.util.LinkedHashSet<String> out = new java.util.LinkedHashSet<>();
        fields.forEach(f -> out.add(f.name()));
        return out;
    }

    /**
     * One payload field.
     *
     * @param name the top-level payload key
     * @param value where the value is read
     * @param type {@code number}, {@code integer}, {@code string} or {@code boolean}
     * @param conversion the unit conversion, or null
     * @param scale the factor applied after the conversion, or null
     * @param offset the term added after the scale, or null
     * @param defaultValue the value used when the input has none (or holds a sentinel), or null
     * @param sentinels input values that mean "no value"
     */
    public record FieldMapping(String name, ValueSpec value, String type, DoubleUnaryOperator conversion, Double scale,
            Double offset, JsonNode defaultValue, List<JsonNode> sentinels) {

        /** Copies the sentinels. */
        public FieldMapping {
            sentinels = List.copyOf(sentinels);
        }
    }

    /**
     * An item condition.
     *
     * @param value where the compared value is read
     * @param op {@code ==}, {@code !=}, {@code exists} or {@code in}
     * @param operand the comparison value ({@code in}: an array; {@code exists}: true or false, default true)
     */
    public record Filter(ValueSpec value, String op, JsonNode operand) {}

    /**
     * When an item is a tombstone.
     *
     * @param value where the compared value is read
     * @param equals the value meaning "gone", or null
     * @param in values meaning "gone"
     */
    public record Tombstone(ValueSpec value, JsonNode equals, List<JsonNode> in) {

        /** Copies the values. */
        public Tombstone {
            in = in == null ? List.of() : List.copyOf(in);
        }
    }

    /**
     * Quality values.
     *
     * @param accuracyMeters the horizontal accuracy, or null
     * @param extra namespaced scalar extras ({@code lora.rssi}) and where each is read
     */
    public record QualitySpec(ValueSpec accuracyMeters, Map<String, ValueSpec> extra) {

        /** Copies the extras. */
        public QualitySpec {
            extra = extra == null ? Map.of() : java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(extra));
        }
    }

    /**
     * Name/value flattening (airrohr {@code sensordatavalues}).
     *
     * @param path the array of pairs, relative to the item
     * @param key the member holding a pair's name
     * @param value the member holding a pair's value
     */
    public record PairsSpec(JsonPath path, String key, String value) {}

    /**
     * Copies the scalar members of one object into top-level fields (ChirpStack {@code object}, TTS
     * {@code decoded_payload}). Members listed in {@code drop}, members whose name is not a valid field name, and
     * names a field already produced are skipped.
     *
     * @param value where the object is read
     * @param type the type every copied value must have
     * @param max the most members copied
     */
    public record SpreadSpec(ValueSpec value, String type, int max) {}
}
