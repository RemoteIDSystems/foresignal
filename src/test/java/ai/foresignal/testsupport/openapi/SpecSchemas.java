package ai.foresignal.testsupport.openapi;

import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Validates JSON instances against schemas of {@code docs/OpenAPI.json} with a JSON Schema 2020-12
 * validator (networknt), as OpenAPI 3.1 defines its schema dialect.
 *
 * <p>Each target is wrapped in a small root document that carries the spec's {@code components},
 * so every {@code $ref: "#/components/schemas/…"} inside the spec resolves within the same
 * document, exactly as it does in the spec itself.
 */
public final class SpecSchemas {

    /** The base IRI of the wrapper documents; never fetched. */
    private static final String BASE = "https://foresignal.ai/v1/openapi.json";

    private final SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
    private final JsonNode components;
    private final Map<String, Schema> cache = new ConcurrentHashMap<>();

    /** Validator over the authored document. */
    public SpecSchemas() {
        this(OpenApiSpec.document());
    }

    /** Validator over {@code document}. */
    public SpecSchemas(JsonNode document) {
        this.components = document.get("components");
    }

    /**
     * The errors of {@code instance} against the component schema named {@code name}; empty when it
     * conforms.
     */
    public List<String> validateComponent(String name, JsonNode instance) {
        return validateRef("#/components/schemas/" + name, instance);
    }

    /**
     * The errors of {@code instance} against the schema at {@code ref} (a local JSON pointer such as
     * {@code #/components/schemas/Change}); empty when it conforms.
     */
    public List<String> validateRef(String ref, JsonNode instance) {
        Schema schema = cache.computeIfAbsent(ref, r -> {
            ObjectNode root = OpenApiSpec.JSON.createObjectNode();
            root.put("$schema", "https://json-schema.org/draft/2020-12/schema");
            root.put("$ref", r);
            root.set("components", components);
            return registry.getSchema(SchemaLocation.of(BASE + "?ref=" + Integer.toHexString(r.hashCode())), root);
        });
        return messages(schema.validate(instance));
    }

    /**
     * The errors of {@code instance} against an inline {@code schema} object taken from the spec (a
     * request body's {@code schema}, which may itself be a {@code $ref}).
     */
    public List<String> validateInline(JsonNode schema, JsonNode instance) {
        ObjectNode root = OpenApiSpec.JSON.createObjectNode();
        root.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        root.set("allOf", OpenApiSpec.JSON.createArrayNode().add(schema));
        root.set("components", components);
        Schema compiled = registry.getSchema(SchemaLocation.of(BASE + "?inline=" + Integer.toHexString(schema.hashCode())), root);
        return messages(compiled.validate(instance));
    }

    private static List<String> messages(List<Error> errors) {
        return errors.stream().map(e -> e.getInstanceLocation() + ": " + e.getMessage()).toList();
    }
}
