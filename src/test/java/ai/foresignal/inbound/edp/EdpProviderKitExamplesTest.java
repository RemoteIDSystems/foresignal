package ai.foresignal.inbound.edp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/**
 * The provider kit of prompt 138 ({@code docs/protocol/edp-1.0/examples/}): every example manifest registers, every
 * item of its example batch is accepted, there is one example per item type, and the reference provider and its CSV
 * are present.
 */
class EdpProviderKitExamplesTest {

    /** The examples directory. */
    static final Path EXAMPLES = Path.of("docs/protocol/edp-1.0/examples");

    /** The receipt time the examples are written against. */
    static final Instant RECEIVED = Instant.parse("2026-10-18T06:00:00Z");

    @Test
    @DisplayName("each example manifest is valid and each example batch is accepted item by item")
    void examplesValidate() throws IOException {
        EdpManifestValidator manifests = EdpFixtures.manifestValidator();
        EdpItemValidator items = new EdpItemValidator(EdpFixtures.COMPILER, EdpProperties.defaults(),
                Clock.fixed(RECEIVED, ZoneOffset.UTC));
        Set<String> types = new LinkedHashSet<>();
        List<Path> files = providers();
        assertThat(files).isNotEmpty();
        for (Path file : files) {
            JsonNode example = EdpFixtures.JSON.readTree(Files.readString(file));
            EdpManifestValidator.ManifestCheck manifest = manifests.validate(example.get("manifest"));
            assertThat(manifest.errors()).as(file.getFileName() + " manifest").isEmpty();
            JsonNode batch = example.get("batch");
            assertThat(EdpFixtures.COMPILER.validateShape("batch", batch)).as(file.getFileName() + " batch").isEmpty();
            List<JsonNode> list = EdpFixtures.items(batch);
            EdpItemValidator.ItemCheck check = items.validate(list, manifest.model(), manifest.payloadSchemas(),
                    RECEIVED);
            assertThat(check.rejected()).as(file.getFileName() + " items").isEmpty();
            assertThat(check.valid()).hasSize(list.size());
            list.forEach(item -> types.add(item.path("type").asString()));
        }
        assertThat(types).as("one minimal provider per item type").contains("feature", "event", "observation",
                "document", "forecast", "delete", "status");
    }

    @Test
    @DisplayName("the example bindings name targets of the protocol and streams of their own manifest")
    void exampleBindingsFit() throws IOException {
        List<String> targets = List.of("series", "canonical-family", "social", "news", "evidence", "indicator",
                "forecast", "place", "situation", "chronicle");
        for (Path file : providers()) {
            JsonNode example = EdpFixtures.JSON.readTree(Files.readString(file));
            Set<String> streams = new LinkedHashSet<>();
            example.path("manifest").path("streams").forEach(s -> streams.add(s.path("id").asString()));
            for (JsonNode binding : example.path("bindings")) {
                assertThat(targets).as(file.getFileName() + " target").contains(binding.path("target").asString());
                assertThat(streams).as(file.getFileName() + " stream").contains(binding.path("streamId").asString());
            }
        }
    }

    @Test
    @DisplayName("the Python reference provider and its sample CSV are shipped, and the provider stays small")
    void referenceProvider() throws IOException {
        Path script = EXAMPLES.resolve("csv_provider.py");
        assertThat(script).exists();
        assertThat(EXAMPLES.resolve("sample.csv")).exists();
        List<String> lines = Files.readAllLines(script);
        assertThat(lines.size()).isLessThanOrEqualTo(70);
        String text = String.join("\n", lines);
        assertThat(text).contains("def push(", "def serve(", "nextCursor", "X-Foresignal-Ingest-Key",
                "foresignal-edp/1.0");
    }

    private static List<Path> providers() throws IOException {
        try (Stream<Path> files = Files.list(EXAMPLES)) {
            return files.filter(p -> p.getFileName().toString().endsWith(".provider.json")).sorted().toList();
        }
    }
}
