package ai.foresignal.source;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Descriptor validation of the extras from Gap-Analysis §8.A items 1–2 (prompt 008).
 *
 * <p>Uses a dynamic PUSH descriptor with no adapter, which is valid on its own, so each test sees
 * only the problem it introduces.
 */
class DescriptorValidatorTest {

    private static SourceDescriptor.Builder valid() {
        return SourceDescriptor.builder("test-push")
                .kinds("quake", "alert")
                .mode(Mode.PUSH)
                .defaultBudget(60, 10_000);
    }

    private static List<String> problems(SourceDescriptor.Builder builder) {
        return DescriptorValidator.problems(builder.build(), null);
    }

    @Test
    @DisplayName("the baseline is valid, and so is a descriptor using every extra correctly")
    void baselineIsValid() {
        assertThat(problems(valid())).isEmpty();
        assertThat(problems(valid()
                        .titleField("headline")
                        .entityKey("icao24", "hex")
                        .entityKey("tail-code", "tail")
                        .fieldSpec("depth", new FieldSpec(FieldType.NUMBER, "km", "Depth", "Hypocenter depth", "MSL", Set.of("quake")))
                        .fieldSpec("speed", FieldSpec.of(FieldType.NUMBER, "[kn_i]"))
                        .kindDisplay("quake", new KindDisplay("M {mag}", "earthquake", "#D9480F", "SFGPEWRH--MTLH-"))
                        .kindDisplay("alert", new KindDisplay(null, null, null, "10031000001211000000"))
                        .aggBelowZoom(6)
                        .intervalFields("onset", "expires")
                        .privatePrecisionDecimals(7)))
                .isEmpty();
    }

    @Nested
    @DisplayName("titleField, startField, endField")
    class TopLevelNames {

        @Test
        @DisplayName("titleField must be a top-level key")
        void titleField() {
            assertThat(problems(valid().titleField("a.b"))).singleElement().asString().contains("titleField");
        }

        @Test
        @DisplayName("startField and endField come together")
        void bothOrNeither() {
            assertThat(problems(valid().intervalFields("onset", null))).singleElement().asString().contains("together");
            assertThat(problems(valid().intervalFields(null, "expires"))).singleElement().asString().contains("together");
        }

        @Test
        @DisplayName("startField and endField must be top-level keys")
        void topLevel() {
            assertThat(problems(valid().intervalFields("time.start", "time/end")))
                    .hasSize(2)
                    .anySatisfy(p -> assertThat(p).contains("startField"))
                    .anySatisfy(p -> assertThat(p).contains("endField"));
        }
    }

    @Nested
    @DisplayName("entityKeys")
    class EntityKeys {

        @ParameterizedTest
        @ValueSource(strings = {"ICAO24", "icao_24", "-mmsi", "imo-", "a--b", " "})
        @DisplayName("key types must be lowercase kebab-case")
        void keyType(String type) {
            assertThat(problems(valid().entityKey(type, "hex"))).singleElement().asString().contains("entityKeys type");
        }

        @Test
        @DisplayName("fields must be top-level keys")
        void field() {
            assertThat(problems(valid().entityKey("mmsi", "vessel.mmsi"))).singleElement().asString().contains("entityKeys.mmsi");
        }
    }

    @Nested
    @DisplayName("fieldSpecs")
    class FieldSpecs {

        @Test
        @DisplayName("keys must be top-level names")
        void key() {
            assertThat(problems(valid().fieldSpec("a.b", FieldSpec.of(FieldType.STRING, null))))
                    .singleElement().asString().contains("fieldSpecs");
        }

        @Test
        @DisplayName("type is required")
        void type() {
            assertThat(problems(valid().fieldSpec("x", new FieldSpec(null, null, null, null, null, null))))
                    .singleElement().asString().contains("type is required");
        }

        @Test
        @DisplayName("unit must look like a UCUM code")
        void unit() {
            assertThat(problems(valid().fieldSpec("x", FieldSpec.of(FieldType.NUMBER, "km per h"))))
                    .singleElement().asString().contains("UCUM");
            assertThat(problems(valid().fieldSpec("x", FieldSpec.of(FieldType.NUMBER, "")))).hasSize(1);
        }

        @Test
        @DisplayName("datum only for numeric fields in a length unit")
        void datum() {
            assertThat(problems(valid().fieldSpec("x", new FieldSpec(FieldType.STRING, null, null, null, "MSL", null))))
                    .singleElement().asString().contains("datum");
            assertThat(problems(valid().fieldSpec("x", new FieldSpec(FieldType.NUMBER, "s", null, null, "AGL", null))))
                    .singleElement().asString().contains("datum");
            assertThat(problems(valid().fieldSpec("x", new FieldSpec(FieldType.NUMBER, "m", null, null, " ", null))))
                    .singleElement().asString().contains("datum");
            assertThat(problems(valid().fieldSpec("x", new FieldSpec(FieldType.INTEGER, "[ft_i]", null, null, "PRESSURE", null))))
                    .isEmpty();
            assertThat(problems(valid().fieldSpec("x", new FieldSpec(FieldType.NUMBER, null, null, null, "custom datum", null))))
                    .isEmpty();
        }

        @Test
        @DisplayName("kinds must be the source's kinds")
        void kinds() {
            assertThat(problems(valid().fieldSpec("x", new FieldSpec(FieldType.NUMBER, null, null, null, null, Set.of("volcano")))))
                    .singleElement().asString().contains("volcano");
        }

    }

    @Nested
    @DisplayName("kindDisplay")
    class KindDisplays {

        @Test
        @DisplayName("keys must be the source's kinds")
        void key() {
            assertThat(problems(valid().kindDisplay("volcano", new KindDisplay(null, null, null, null))))
                    .singleElement().asString().contains("volcano");
        }

        @ParameterizedTest
        @ValueSource(strings = {"red", "#FFF", "#GGGGGG", "D9480F", "#D9480F0"})
        @DisplayName("colour must be #RRGGBB")
        void color(String color) {
            assertThat(problems(valid().kindDisplay("quake", new KindDisplay(null, null, color, null))))
                    .singleElement().asString().contains("#RRGGBB");
        }

        @ParameterizedTest
        @ValueSource(strings = {"SFGPEWRH--MTLH", "SFGPEWRH--MTLH--", "1003100000121100000", "SFGP EWRH--MTLH"})
        @DisplayName("sidc must be 15 or 20 characters")
        void sidc(String sidc) {
            assertThat(problems(valid().kindDisplay("quake", new KindDisplay(null, null, null, sidc))))
                    .singleElement().asString().contains("sidc");
        }

    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 23})
    @DisplayName("aggBelowZoom is 0–22")
    void aggBelowZoom(int zoom) {
        assertThat(problems(valid().aggBelowZoom(zoom))).singleElement().asString().contains("aggBelowZoom");
        assertThat(problems(valid().aggBelowZoom(0))).isEmpty();
        assertThat(problems(valid().aggBelowZoom(22))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 8})
    @DisplayName("privatePrecisionDecimals is 2–7")
    void privatePrecisionDecimals(int decimals) {
        assertThat(problems(valid().privateSource(true).privatePrecisionDecimals(decimals)))
                .singleElement().asString().contains("privatePrecisionDecimals");
        assertThat(problems(valid().privatePrecisionDecimals(2))).isEmpty();
    }

    @Test
    @DisplayName("defaults: no title field, no keys, not private, 4 decimals")
    void defaults() {
        SourceDescriptor descriptor = valid().build();
        assertThat(descriptor.titleField()).isNull();
        assertThat(descriptor.entityKeys()).isEmpty();
        assertThat(descriptor.fieldSpecs()).isEmpty();
        assertThat(descriptor.kindDisplay()).isEmpty();
        assertThat(descriptor.aggBelowZoom()).isNull();
        assertThat(descriptor.startField()).isNull();
        assertThat(descriptor.endField()).isNull();
        assertThat(descriptor.privateSource()).isFalse();
        assertThat(descriptor.privatePrecisionDecimals()).isEqualTo(4);
    }

    @Test
    @DisplayName("a private source with embedFields gets a warning, not a problem")
    void privateEmbedWarning() {
        SourceDescriptor descriptor = valid().privateSource(true).embedFields("note").build();
        assertThat(DescriptorValidator.problems(descriptor, null)).isEmpty();
        assertThat(DescriptorValidator.warnings(descriptor)).singleElement().asString().contains("private");
        assertThat(DescriptorValidator.warnings(valid().embedFields("note").build())).isEmpty();
    }
}
