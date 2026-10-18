package ai.foresignal.cli.edp;

import ai.foresignal.cli.CliUsageException;
import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.path.PathType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Validates an EDP 1.0 manifest and items offline, against the JSON Schemas exported to
 * {@code docs/protocol/edp-1.0/} and bundled with the CLI under {@code edp/schemas/}. This checks the shape only;
 * the server's {@code POST /v1/external/validate} ({@code edp validate --remote}) also checks registration rules,
 * payload schemas and breaking changes.
 */
public final class EdpLocalValidator {

    /** The base URI the exported schemas declare in their {@code $id}. */
    public static final String SCHEMA_BASE = "https://foresignal.ai/protocol/edp-1.0/";

    /**
     * One problem.
     *
     * @param location {@code manifest}, or {@code item <n>} (1-based line or array position)
     * @param path     the JSON Pointer inside that document ({@code ""} for the root)
     * @param message  what is wrong
     */
    public record Problem(String location, String path, String message) {
    }

    /**
     * The result of a validation.
     *
     * @param manifestValid  whether the manifest conforms (null when no manifest was given)
     * @param itemsValid     how many items conform
     * @param itemsRejected  how many items do not
     * @param problems       every problem, manifest first
     */
    public record Report(Boolean manifestValid, int itemsValid, int itemsRejected, List<Problem> problems) {

        /**
         * Whether everything conforms.
         *
         * @return true when there is no problem
         */
        public boolean valid() {
            return problems.isEmpty();
        }
    }

    /**
     * The inputs read from files.
     *
     * @param manifest the manifest, or null
     * @param items    the items with their location labels
     */
    public record Input(JsonNode manifest, List<Map.Entry<String, JsonNode>> items) {
    }

    private final JsonMapper json;
    private final SchemaRegistry registry;
    private final Map<String, Schema> schemas = new HashMap<>();

    /**
     * Creates the validator.
     *
     * @param json the JSON mapper
     */
    public EdpLocalValidator(JsonMapper json) {
        this.json = json;
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().pathType(PathType.JSON_POINTER).build();
        this.registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12, builder -> builder
                .schemaRegistryConfig(config)
                .schemaLoader(loader -> loader.fetchRemoteResources(false)));
    }

    /**
     * Reads a manifest file and an optional items file.
     *
     * <p>The manifest file may be a bare manifest or a provider-kit example ({@code {"manifest": …, "batch":
     * {"items": […]}}}); in the latter case its batch items are used when no items file is given. The items file is
     * NDJSON (one item per line, blank lines ignored), or, when its name ends in {@code .json}, a JSON array or a
     * batch object with {@code items}.
     *
     * @param manifestFile the manifest file
     * @param itemsFile    the items file, or null
     * @return the parsed input
     * @throws CliUsageException when a file cannot be read or is not JSON
     */
    public Input read(Path manifestFile, Path itemsFile) {
        JsonNode doc = readJson(manifestFile);
        JsonNode manifest = doc;
        List<Map.Entry<String, JsonNode>> items = new ArrayList<>();
        if (doc.isObject() && !doc.has("protocol") && doc.path("manifest").isObject()) {
            manifest = doc.get("manifest");
            if (itemsFile == null && doc.path("batch").path("items").isArray()) {
                addArray(items, doc.path("batch").path("items"), manifestFile.getFileName() + " batch item ");
            }
        }
        if (itemsFile != null) {
            String name = itemsFile.getFileName().toString();
            if (name.endsWith(".json")) {
                JsonNode whole = readJson(itemsFile);
                JsonNode array = whole.isArray() ? whole : whole.path("items");
                if (!array.isArray()) {
                    throw new CliUsageException(itemsFile + " is neither a JSON array nor a batch with \"items\"");
                }
                addArray(items, array, name + " item ");
            } else {
                readNdjson(itemsFile, items);
            }
        }
        return new Input(manifest, items);
    }

    /**
     * Validates a manifest and items.
     *
     * @param manifest the manifest, or null to check items only
     * @param items    the items with their location labels
     * @return the report
     */
    public Report validate(JsonNode manifest, List<Map.Entry<String, JsonNode>> items) {
        List<Problem> problems = new ArrayList<>();
        Boolean manifestValid = null;
        if (manifest != null) {
            List<Problem> found = check("manifest", manifest, "manifest");
            manifestValid = found.isEmpty();
            problems.addAll(found);
        }
        int valid = 0;
        int rejected = 0;
        for (Map.Entry<String, JsonNode> item : items) {
            if (item.getValue() == null) {
                rejected++;
                continue;
            }
            List<Problem> found = check("item", item.getValue(), item.getKey());
            if (found.isEmpty()) {
                valid++;
            } else {
                rejected++;
                problems.addAll(found);
            }
        }
        return new Report(manifestValid, valid, rejected, problems);
    }

    /**
     * Validates one document against one bundled schema.
     *
     * @param schemaName the schema ({@code manifest}, {@code item}, {@code batch} …)
     * @param doc        the document
     * @param location   the location label for problems
     * @return the problems, empty when valid
     */
    public List<Problem> check(String schemaName, JsonNode doc, String location) {
        Schema schema = schemas.computeIfAbsent(schemaName, this::load);
        List<Problem> problems = new ArrayList<>();
        for (Error error : schema.validate(doc)) {
            String path = error.getInstanceLocation() == null ? "" : error.getInstanceLocation().toString();
            if ("$".equals(path) || "/".equals(path)) {
                path = "";
            }
            problems.add(new Problem(location, path, error.getMessage()));
        }
        return problems;
    }

    private Schema load(String name) {
        String resource = "edp/schemas/" + name + ".schema.json";
        try (InputStream in = EdpLocalValidator.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("The bundled EDP schema " + resource + " is missing");
            }
            JsonNode node = json.readTree(in);
            Schema schema = registry.getSchema(SchemaLocation.of(SCHEMA_BASE + name + ".schema.json"), node);
            schema.initializeValidators();
            return schema;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read the bundled EDP schema " + resource, e);
        }
    }

    private JsonNode readJson(Path file) {
        try {
            return json.readTree(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new CliUsageException("Cannot read " + file + ": " + e.getMessage(), e);
        } catch (JacksonException e) {
            throw new CliUsageException(file + " is not valid JSON: " + e.getOriginalMessage(), e);
        }
    }

    private void readNdjson(Path file, List<Map.Entry<String, JsonNode>> items) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CliUsageException("Cannot read " + file + ": " + e.getMessage(), e);
        }
        String name = file.getFileName().toString();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).strip();
            if (line.isEmpty()) {
                continue;
            }
            String label = name + ":" + (i + 1);
            try {
                items.add(Map.entry(label, json.readTree(line)));
            } catch (JacksonException e) {
                throw new CliUsageException(label + " is not valid JSON: " + e.getOriginalMessage(), e);
            }
        }
    }

    private static void addArray(List<Map.Entry<String, JsonNode>> items, JsonNode array, String prefix) {
        for (int i = 0; i < array.size(); i++) {
            items.add(Map.entry(prefix + (i + 1), array.get(i)));
        }
    }
}
