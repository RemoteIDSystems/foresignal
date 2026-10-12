package ai.foresignal.inbound.edp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Builds the EDP components without a Spring context and loads the conformance fixtures. */
final class EdpFixtures {

    static final JsonMapper JSON = JsonMapper.builder().build();

    /** The receipt time the fixtures are written against. */
    static final Instant RECEIVED = Instant.parse("2026-10-11T18:00:00Z");

    static final EdpSchemaCompiler COMPILER = new EdpSchemaCompiler();

    private EdpFixtures() {}

    static EdpManifestValidator manifestValidator() {
        return new EdpManifestValidator(COMPILER, EdpProperties.defaults());
    }

    static EdpItemValidator itemValidator() {
        return new EdpItemValidator(COMPILER, EdpProperties.defaults(), Clock.fixed(RECEIVED, ZoneOffset.UTC));
    }

    static EdpItemMapper mapper() {
        return new EdpItemMapper(JSON);
    }

    /** A classpath resource under {@code edp/conformance/}. */
    static String text(String path) {
        try (InputStream in = EdpFixtures.class.getClassLoader().getResourceAsStream("edp/conformance/" + path)) {
            if (in == null) {
                throw new IllegalStateException("Missing fixture " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static JsonNode json(String path) {
        return JSON.readTree(text(path));
    }

    static JsonNode parse(String json) {
        return JSON.readTree(json);
    }

    /** A validated manifest from {@code manifests/valid/}. */
    static EdpManifestValidator.ManifestCheck manifest(String name) {
        EdpManifestValidator.ManifestCheck check = manifestValidator().validate(json("manifests/valid/" + name));
        if (!check.valid()) {
            throw new IllegalStateException(name + " is not valid: " + check.errors());
        }
        return check;
    }

    /** The items of a batch fixture. */
    static List<JsonNode> items(JsonNode batch) {
        return batch.path("items").valueStream().toList();
    }
}
