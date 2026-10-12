package ai.foresignal.inbound.edp;

import static ai.foresignal.inbound.edp.EdpFixtures.RECEIVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/** Payload schemas cannot reach the network or run unbounded patterns (EDP §7, invariant EXT-6). */
class EdpSchemaSafetyTest {

    private final EdpSchemaCompiler compiler = new EdpSchemaCompiler();

    private static List<EdpSchemaError> errors(Runnable action) {
        try {
            action.run();
        } catch (EdpSchemaException e) {
            return e.errors();
        }
        throw new AssertionError("expected the schema to be refused");
    }

    @Test
    @DisplayName("a remote $ref is refused before anything is compiled or fetched")
    void remoteRef() {
        List<EdpSchemaError> errors = errors(() -> compiler.compile(EdpFixtures.parse(
                "{\"type\":\"object\",\"properties\":{\"a\":{\"$ref\":\"https://evil.example.org/s.json\"}}}")));

        assertThat(errors).singleElement().satisfies(e -> {
            assertThat(e.path()).isEqualTo("/properties/a/$ref");
            assertThat(e.message()).contains("local");
        });
    }

    @Test
    @DisplayName("local $refs compile and validate")
    void localRef() {
        CompiledPayloadSchema schema = compiler.compile(EdpFixtures.parse("""
                {"type":"object","properties":{"a":{"$ref":"#/$defs/code"}},"$defs":{"code":{"type":"string"}}}"""));

        assertThat(schema.validate(EdpFixtures.parse("{\"a\":\"x\"}"))).isEmpty();
        assertThat(schema.validate(EdpFixtures.parse("{\"a\":1}"))).isNotEmpty();
    }

    @Test
    @DisplayName("$dynamicRef, $recursiveRef and a foreign $schema are refused")
    void dynamicRefsAndDialect() {
        assertThat(errors(() -> compiler.compile(EdpFixtures.parse("{\"$dynamicRef\":\"#meta\"}"))))
                .extracting(EdpSchemaError::path).contains("/$dynamicRef");
        assertThat(errors(() -> compiler.compile(EdpFixtures.parse("{\"items\":{\"$recursiveRef\":\"#\"}}"))))
                .extracting(EdpSchemaError::path).contains("/items/$recursiveRef");
        assertThat(errors(() -> compiler.compile(EdpFixtures.parse(
                "{\"$schema\":\"https://evil.example.org/meta\",\"type\":\"object\"}"))))
                .extracting(EdpSchemaError::path).contains("/$schema");
        assertThat(compiler.vet(EdpFixtures.parse(
                "{\"$schema\":\"https://json-schema.org/draft/2020-12/schema\",\"type\":\"object\"}"))).isEmpty();
    }

    @Test
    @DisplayName("a schema over 64 KB is refused")
    void oversized() {
        ObjectNode schema = EdpFixtures.JSON.createObjectNode();
        ObjectNode properties = schema.putObject("properties");
        for (int i = 0; i < 3000; i++) {
            properties.putObject("field_" + i).put("type", "string").put("description", "x".repeat(20));
        }

        assertThat(errors(() -> compiler.compile(schema))).singleElement()
                .satisfies(e -> assertThat(e.message()).contains("bytes"));
    }

    @Test
    @DisplayName("a pattern or patternProperties key over 256 characters, or one RE2 cannot run, is refused")
    void longOrUnsupportedPatterns() {
        assertThat(errors(() -> compiler.compile(EdpFixtures.parse(
                "{\"properties\":{\"a\":{\"pattern\":\"" + "a".repeat(257) + "\"}}}"))))
                .extracting(EdpSchemaError::path).contains("/properties/a/pattern");
        assertThat(errors(() -> compiler.compile(EdpFixtures.parse(
                "{\"patternProperties\":{\"" + "b".repeat(300) + "\":{}}}"))))
                .isNotEmpty();
        // Backreferences need backtracking, which RE2 does not do.
        assertThat(errors(() -> compiler.compile(EdpFixtures.parse("{\"pattern\":\"(a)\\\\1\"}")))).isNotEmpty();
    }

    @Test
    @Timeout(10)
    @DisplayName("a catastrophic-backtracking pattern runs in linear time on a long input")
    void catastrophicPattern() {
        CompiledPayloadSchema schema = compiler.compile(EdpFixtures.parse(
                "{\"type\":\"object\",\"properties\":{\"s\":{\"type\":\"string\",\"pattern\":\"^(a+)+$\"}}}"));
        String hostile = "a".repeat(2000) + "!";

        long started = System.nanoTime();
        List<EdpSchemaError> errors = schema.validate(EdpFixtures.JSON.createObjectNode().put("s", hostile));
        long elapsedMs = (System.nanoTime() - started) / 1_000_000;

        assertThat(errors).isNotEmpty();
        assertThat(elapsedMs).isLessThan(2000);
        assertThat(Re2RegularExpressionFactory.INSTANCE.getRegularExpression("^(a+)+$").matches("a".repeat(50))).isTrue();
    }

    @Test
    @DisplayName("once the validation budget is spent, the remaining items are rejected VALIDATION_BUDGET_EXCEEDED")
    void validationBudget() {
        EdpManifestValidator.ManifestCheck manifest = EdpFixtures.manifest("hormuz-example.json");
        EdpProperties noBudget = new EdpProperties(0, Duration.ofMinutes(30), 0.5, Duration.ZERO, Duration.ofSeconds(30));
        EdpItemValidator validator = new EdpItemValidator(compiler, noBudget, Clock.fixed(RECEIVED, ZoneOffset.UTC));
        List<JsonNode> items = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            items.add(EdpFixtures.parse("{\"stream\":\"incidents\",\"type\":\"delete\",\"id\":\"d" + i + "\"}"));
        }

        EdpItemValidator.ItemCheck check = validator.validate(items, manifest.model(), manifest.payloadSchemas(), RECEIVED);

        assertThat(check.valid()).isEmpty();
        assertThat(check.rejected()).hasSize(3)
                .allSatisfy(r -> assertThat(r.reason()).isEqualTo("VALIDATION_BUDGET_EXCEEDED"));
    }

    @Test
    @DisplayName("an unsafe payload schema fails manifest registration with paths under the stream")
    void manifestRegistration() {
        EdpManifestValidator.ManifestCheck remote = EdpFixtures.manifestValidator()
                .validate(EdpFixtures.json("manifests/invalid/remote-ref.json"));
        EdpManifestValidator.ManifestCheck longPattern = EdpFixtures.manifestValidator()
                .validate(EdpFixtures.json("manifests/invalid/long-pattern.json"));

        assertThat(remote.valid()).isFalse();
        assertThat(remote.errors()).extracting(EdpSchemaError::path).contains("/streams/0/payloadSchema/$ref");
        assertThat(longPattern.valid()).isFalse();
        assertThat(longPattern.errors()).extracting(EdpSchemaError::path)
                .contains("/streams/0/payloadSchema/properties/code/pattern");
    }

    @Test
    @DisplayName("a non-object schema is refused")
    void notAnObject() {
        assertThatThrownBy(() -> compiler.compile(EdpFixtures.parse("true"))).isInstanceOf(EdpSchemaException.class);
    }
}
