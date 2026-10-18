package ai.foresignal.cli.edp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.foresignal.cli.CliUsageException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The bundled EDP 1.0 schemas and the input formats {@code edp validate} accepts. */
class EdpLocalValidatorTest {

    @TempDir
    Path tmp;

    private final JsonMapper json = JsonMapper.builder().build();
    private final EdpLocalValidator validator = new EdpLocalValidator(json);

    @Test
    void everyExportedSchemaIsBundled() {
        for (String name : List.of("manifest", "item", "batch", "feature", "event", "observation", "document",
                "forecast", "delete", "status")) {
            assertTrue(EdpLocalValidator.class.getClassLoader()
                    .getResource("edp/schemas/" + name + ".schema.json") != null, name);
        }
    }

    @Test
    void anItemOfAnUnknownTypeIsRejected() {
        JsonNode item = json.readTree("{\"stream\":\"s\",\"type\":\"nope\",\"id\":\"1\"}");
        EdpLocalValidator.Report report = validator.validate(null, List.of(Map.entry("item 1", item)));
        assertEquals(null, report.manifestValid());
        assertEquals(1, report.itemsRejected());
        assertFalse(report.valid());
        assertEquals("item 1", report.problems().get(0).location());
    }

    @Test
    void itemsMayComeAsAJsonArrayOrBatch() throws Exception {
        Path manifest = Files.writeString(tmp.resolve("m.json"), "{}");
        Path array = Files.writeString(tmp.resolve("items.json"), "[{\"a\":1},{\"b\":2}]");
        Path batch = Files.writeString(tmp.resolve("batch.json"), "{\"items\":[{\"a\":1}]}");
        Path neither = Files.writeString(tmp.resolve("bad.json"), "{\"x\":1}");
        assertEquals(2, validator.read(manifest, array).items().size());
        assertEquals("items.json item 2", validator.read(manifest, array).items().get(1).getKey());
        assertEquals(1, validator.read(manifest, batch).items().size());
        assertThrows(CliUsageException.class, () -> validator.read(manifest, neither));
        assertThrows(CliUsageException.class, () -> validator.read(tmp.resolve("missing.json"), null));
    }

    @Test
    void anEmptyManifestIsInvalid() {
        EdpLocalValidator.Report report = validator.validate(json.readTree("{}"), List.of());
        assertFalse(report.manifestValid());
        assertTrue(report.problems().stream().anyMatch(p -> p.message().contains("protocol")), report.toString());
    }
}
