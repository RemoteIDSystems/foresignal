package ai.foresignal.inbound.edp;

import static ai.foresignal.inbound.edp.EdpFixtures.RECEIVED;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The exported JSON Schemas in {@code docs/protocol/edp-1.0/} match the {@code Edp*} components of
 * {@code docs/OpenAPI.json} (the same transform as {@code scripts/export-edp-schemas.py}), and every
 * conformance fixture validates as labeled.
 */
class EdpSchemaExportTest {

    private static final Map<String, String> ROOTS = Map.of(
            "manifest", "EdpManifest", "batch", "EdpBatch", "item", "EdpItem", "feature", "EdpFeatureItem",
            "event", "EdpEventItem", "observation", "EdpObservationItem", "document", "EdpDocumentItem",
            "forecast", "EdpForecastItem", "delete", "EdpDeleteItem", "status", "EdpStatusItem");

    private static final Path DOCS = Path.of("docs");

    @Test
    @DisplayName("each exported schema equals its spec component after the export transform")
    void exportMatchesSpec() throws IOException {
        JsonNode schemas = EdpFixtures.JSON.readTree(Files.readString(DOCS.resolve("OpenAPI.json")))
                .path("components").path("schemas");

        for (Map.Entry<String, String> root : ROOTS.entrySet()) {
            JsonNode exported = EdpFixtures.JSON.readTree(
                    Files.readString(DOCS.resolve("protocol/edp-1.0/" + root.getKey() + ".schema.json")));
            ObjectNode expected = EdpFixtures.JSON.createObjectNode();
            expected.put("$schema", "https://json-schema.org/draft/2020-12/schema");
            expected.put("$id", "https://foresignal.ai/protocol/edp-1.0/" + root.getKey() + ".schema.json");
            expected.put("title", "Foresignal EDP 1.0 " + root.getKey());
            expected.setAll((ObjectNode) rewrite(schemas.get(root.getValue())));
            Set<String> referenced = new TreeSet<>();
            refs(schemas.get(root.getValue()), schemas, referenced);
            referenced.remove(root.getValue());
            if (!referenced.isEmpty()) {
                ObjectNode defs = expected.putObject("$defs");
                for (String name : referenced) {
                    defs.set(name, rewrite(schemas.get(name)));
                }
            }

            assertThat(exported).as(root.getKey() + ".schema.json (regenerate with scripts/export-edp-schemas.py)")
                    .isEqualTo(expected);
        }
    }

    @Test
    @DisplayName("valid manifests register and invalid ones are refused")
    void manifestFixtures() throws IOException {
        EdpManifestValidator validator = EdpFixtures.manifestValidator();
        for (Path file : fixtures("manifests/valid")) {
            assertThat(validator.validate(EdpFixtures.JSON.readTree(Files.readString(file))).errors())
                    .as(file.getFileName().toString()).isEmpty();
        }
        for (Path file : fixtures("manifests/invalid")) {
            assertThat(validator.validate(EdpFixtures.JSON.readTree(Files.readString(file))).valid())
                    .as(file.getFileName().toString()).isFalse();
        }
    }

    @Test
    @DisplayName("every item of a valid batch is accepted; each invalid batch's item is rejected with x-expect")
    void batchFixtures() throws IOException {
        for (Path file : fixtures("batches/valid")) {
            if (!file.toString().endsWith(".json")) {
                continue;
            }
            JsonNode batch = EdpFixtures.JSON.readTree(Files.readString(file));
            EdpItemValidator.ItemCheck check = validate(batch);
            assertThat(check.rejected()).as(file.getFileName().toString()).isEmpty();
            assertThat(check.valid()).hasSize(batch.path("items").size());
            for (JsonNode item : EdpFixtures.items(batch)) {
                String type = item.path("type").asString();
                assertThat(EdpFixtures.COMPILER.validateShape(type, item)).as(file + " " + type).isEmpty();
                assertThat(EdpFixtures.COMPILER.validateShape("item", item)).as(file + " item").isEmpty();
            }
            assertThat(EdpFixtures.COMPILER.validateShape("batch", batch)).as(file + " batch").isEmpty();
        }
        List<String> seen = new ArrayList<>();
        for (Path file : fixtures("batches/invalid")) {
            JsonNode batch = EdpFixtures.JSON.readTree(Files.readString(file));
            EdpItemValidator.ItemCheck check = validate(batch);
            assertThat(check.valid()).as(file.getFileName().toString()).isEmpty();
            assertThat(check.rejected()).as(file.getFileName().toString()).singleElement()
                    .satisfies(r -> assertThat(r.reason()).isEqualTo(batch.path("x-expect").asString()));
            seen.add(batch.path("items").get(0).path("type").asString());
        }
        assertThat(seen).contains("feature", "event", "observation", "document", "forecast", "delete", "status")
                .as("every item type has an invalid example");
    }

    private static EdpItemValidator.ItemCheck validate(JsonNode batch) {
        EdpManifestValidator.ManifestCheck manifest = EdpFixtures.manifest(batch.path("x-manifest").asString());
        return EdpFixtures.itemValidator().validate(EdpFixtures.items(batch), manifest.model(),
                manifest.payloadSchemas(), RECEIVED);
    }

    private static List<Path> fixtures(String dir) throws IOException {
        try (Stream<Path> files = Files.list(Path.of("src/test/resources/edp/conformance").resolve(dir))) {
            return files.sorted().toList();
        }
    }

    /** {@code #/components/schemas/} refs become {@code #/$defs/}; {@code discriminator} is dropped. */
    private static JsonNode rewrite(JsonNode node) {
        if (node.isObject()) {
            ObjectNode out = EdpFixtures.JSON.createObjectNode();
            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                if (entry.getKey().equals("discriminator")) {
                    continue;
                }
                if (entry.getKey().equals("$ref") && entry.getValue().isString()) {
                    out.put("$ref", entry.getValue().asString().replace("#/components/schemas/", "#/$defs/"));
                } else {
                    out.set(entry.getKey(), rewrite(entry.getValue()));
                }
            }
            return out;
        }
        if (node.isArray()) {
            var out = EdpFixtures.JSON.createArrayNode();
            for (Iterator<JsonNode> it = node.values().iterator(); it.hasNext(); ) {
                out.add(rewrite(it.next()));
            }
            return out;
        }
        return node;
    }

    private static void refs(JsonNode node, JsonNode schemas, Set<String> acc) {
        if (node.isObject()) {
            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                if (entry.getKey().equals("$ref") && entry.getValue().isString()
                        && entry.getValue().asString().startsWith("#/components/schemas/")) {
                    String name = entry.getValue().asString().substring("#/components/schemas/".length());
                    if (acc.add(name)) {
                        refs(schemas.get(name), schemas, acc);
                    }
                } else {
                    refs(entry.getValue(), schemas, acc);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node.values()) {
                refs(child, schemas, acc);
            }
        }
    }
}
