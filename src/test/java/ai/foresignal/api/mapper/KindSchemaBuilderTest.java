package ai.foresignal.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class KindSchemaBuilderTest {

    private static SourceDescriptor.Builder source(String id, String... kinds) {
        return SourceDescriptor.builder(id).name(id).kinds(kinds).mode(Mode.PUSH);
    }

    @Test
    void unknownKindIsEmpty() {
        assertThat(KindSchemaBuilder.build("nope", List.of(source("a", "k").build()))).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void mergesAgreeingSpecs() {
        SourceDescriptor a = source("a", "quake")
                .fieldSpec("mag", new FieldSpec(FieldType.NUMBER, "1", "Magnitude", "How big", null, Set.of()))
                .fieldSpec("time", new FieldSpec(FieldType.TIMESTAMP, "ms", null, null, null, Set.of()))
                .fieldSpec("elsewhere", new FieldSpec(FieldType.STRING, null, null, null, null, Set.of("other")))
                .kinds("quake", "other")
                .kindDisplay("quake", new KindDisplay("M {mag}", "earthquake", "#D9480F", null))
                .build();
        SourceDescriptor b = source("b", "quake")
                .fieldSpec("mag", new FieldSpec(FieldType.NUMBER, "1", "Magnitude", null, null, Set.of()))
                .fieldSpec("depth", new FieldSpec(FieldType.NUMBER, "km", null, null, "WGS84", Set.of()))
                .build();
        Map<String, Object> schema = KindSchemaBuilder.build("quake", List.of(b, a)).orElseThrow();
        assertThat(schema.get("$schema")).isEqualTo(KindSchemaBuilder.DIALECT);
        assertThat(schema.get("title")).isEqualTo("quake");
        assertThat(schema.get("type")).isEqualTo("object");
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        assertThat(properties).containsOnlyKeys("depth", "mag", "time");
        Map<String, Object> mag = (Map<String, Object>) properties.get("mag");
        assertThat(mag).containsEntry("type", "number").containsEntry("x-unit", "1").containsEntry("x-label", "Magnitude")
                .containsEntry("description", "How big");
        assertThat((Map<String, Object>) properties.get("time"))
                .containsEntry("type", List.of("string", "integer")).containsEntry("x-format", "timestamp");
        assertThat((Map<String, Object>) properties.get("depth")).containsEntry("x-datum", "WGS84");
        assertThat(schema.get("x-conflicts")).isEqualTo(List.of());
        assertThat(((KindDisplay) schema.get("x-display")).icon()).isEqualTo("earthquake");
    }

    @Test
    @SuppressWarnings("unchecked")
    void conflictingTypesBecomeOneOf() {
        SourceDescriptor a = source("a", "quake")
                .fieldSpec("mag", FieldSpec.of(FieldType.NUMBER, "1")).build();
        SourceDescriptor b = source("b", "quake")
                .fieldSpec("mag", FieldSpec.of(FieldType.STRING, null)).build();
        SourceDescriptor c = source("c", "quake")
                .fieldSpec("mag", FieldSpec.of(FieldType.NUMBER, "1")).build();
        Map<String, Object> schema = KindSchemaBuilder.build("quake", List.of(a, b, c)).orElseThrow();
        Map<String, Object> mag = (Map<String, Object>) ((Map<String, Object>) schema.get("properties")).get("mag");
        List<Map<String, Object>> variants = (List<Map<String, Object>>) mag.get("oneOf");
        assertThat(variants).hasSize(2);
        assertThat(variants.get(0)).containsEntry("type", "number").containsEntry("x-sources", List.of("a", "c"));
        assertThat(variants.get(1)).containsEntry("type", "string").containsEntry("x-sources", List.of("b"));
        assertThat(schema.get("x-conflicts")).isEqualTo(List.of("a", "b", "c"));
        assertThat(schema.get("x-display")).isNull();
    }

    @Test
    void everyTypeMaps() {
        SourceDescriptor a = source("a", "k")
                .fieldSpec("s", FieldSpec.of(FieldType.STRING, null))
                .fieldSpec("i", FieldSpec.of(FieldType.INTEGER, null))
                .fieldSpec("b", FieldSpec.of(FieldType.BOOLEAN, null))
                .fieldSpec("o", FieldSpec.of(FieldType.OBJECT, null))
                .fieldSpec("a", FieldSpec.of(FieldType.ARRAY, null))
                .build();
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> properties = (Map<String, Map<String, Object>>)
                KindSchemaBuilder.build("k", List.of(a)).orElseThrow().get("properties");
        assertThat(properties.get("s").get("type")).isEqualTo("string");
        assertThat(properties.get("i").get("type")).isEqualTo("integer");
        assertThat(properties.get("b").get("type")).isEqualTo("boolean");
        assertThat(properties.get("o").get("type")).isEqualTo("object");
        assertThat(properties.get("a").get("type")).isEqualTo("array");
    }
}
