package ai.foresignal.api.mapper;

import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * Builds the JSON Schema 2020-12 document of one kind ({@code GET /v1/kinds/{kind}/schema}) by
 * merging the {@code fieldSpecs} of every source that emits the kind.
 *
 * <p>Each payload field becomes a property with {@code type}, {@code description}, {@code x-unit}
 * (UCUM code), {@code x-label} and, for vertical values, {@code x-datum}. {@code TIMESTAMP} fields
 * are {@code ["string", "integer"]} with {@code x-format: timestamp}, because upstreams send either
 * ISO-8601 text or epoch counts. When sources disagree on a field's type, the property becomes a
 * {@code oneOf} of the variants (each with {@code x-sources}) and the disagreeing source ids are
 * listed in the document's {@code x-conflicts}. {@code x-display} is the first declared
 * {@code kindDisplay} for the kind, in source-id order.
 *
 * <p>Sources are visited in id order and fields sorted by name, so the document, and the entity tag
 * computed from it, is stable.
 */
public final class KindSchemaBuilder {

    /** The JSON Schema dialect. */
    public static final String DIALECT = "https://json-schema.org/draft/2020-12/schema";

    private KindSchemaBuilder() {}

    /**
     * The schema of {@code kind}, or empty when no source emits it.
     *
     * @param kind the kind
     * @param descriptors every registered descriptor
     */
    public static Optional<Map<String, Object>> build(String kind, List<SourceDescriptor> descriptors) {
        List<SourceDescriptor> emitting = descriptors.stream()
                .filter(d -> d.kinds().contains(kind))
                .sorted(Comparator.comparing(SourceDescriptor::id))
                .toList();
        if (emitting.isEmpty()) {
            return Optional.empty();
        }
        Map<String, List<Declared>> byField = new TreeMap<>();
        KindDisplay display = null;
        for (SourceDescriptor descriptor : emitting) {
            for (Map.Entry<String, FieldSpec> entry : descriptor.fieldSpecs().entrySet()) {
                if (entry.getValue() != null && entry.getValue().appliesTo(kind)) {
                    byField.computeIfAbsent(entry.getKey(), k -> new ArrayList<>())
                            .add(new Declared(descriptor.id(), entry.getValue()));
                }
            }
            if (display == null && descriptor.kindDisplay() != null) {
                display = descriptor.kindDisplay().get(kind);
            }
        }
        Map<String, Object> properties = new LinkedHashMap<>();
        Set<String> conflicts = new LinkedHashSet<>();
        for (Map.Entry<String, List<Declared>> field : byField.entrySet()) {
            Map<FieldType, List<Declared>> byType = new LinkedHashMap<>();
            for (Declared declared : field.getValue()) {
                byType.computeIfAbsent(declared.spec().type(), t -> new ArrayList<>()).add(declared);
            }
            if (byType.size() == 1) {
                properties.put(field.getKey(), property(field.getValue().getFirst().spec()));
            } else {
                List<Map<String, Object>> variants = new ArrayList<>();
                for (List<Declared> group : byType.values()) {
                    Map<String, Object> variant = property(group.getFirst().spec());
                    List<String> sources = group.stream().map(Declared::sourceId).toList();
                    variant.put("x-sources", sources);
                    variants.add(variant);
                    conflicts.addAll(sources);
                }
                Map<String, Object> property = new LinkedHashMap<>();
                property.put("oneOf", variants);
                properties.put(field.getKey(), property);
            }
        }
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("$schema", DIALECT);
        schema.put("title", kind);
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("x-display", display);
        schema.put("x-conflicts", List.copyOf(new java.util.TreeSet<>(conflicts)));
        return Optional.of(schema);
    }

    private static Map<String, Object> property(FieldSpec spec) {
        Map<String, Object> property = new LinkedHashMap<>();
        switch (spec.type()) {
            case STRING -> property.put("type", "string");
            case INTEGER -> property.put("type", "integer");
            case NUMBER -> property.put("type", "number");
            case BOOLEAN -> property.put("type", "boolean");
            case OBJECT -> property.put("type", "object");
            case ARRAY -> property.put("type", "array");
            case TIMESTAMP -> {
                property.put("type", List.of("string", "integer"));
                property.put("x-format", "timestamp");
            }
        }
        if (spec.description() != null) {
            property.put("description", spec.description());
        }
        if (spec.unit() != null) {
            property.put("x-unit", spec.unit());
        }
        if (spec.label() != null) {
            property.put("x-label", spec.label());
        }
        if (spec.datum() != null) {
            property.put("x-datum", spec.datum());
        }
        return property;
    }

    private record Declared(String sourceId, FieldSpec spec) {}
}
