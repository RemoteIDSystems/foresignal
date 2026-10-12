package ai.foresignal.inbound.edp;

import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.path.PathType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Vets and compiles EDP payload schemas (EDP §7), and validates documents against the exported
 * protocol schemas ({@code edp/schemas/{name}.schema.json} on the classpath).
 *
 * <p>Every payload schema is checked <em>before</em> compilation: at most 64 KB serialized, only
 * local {@code $ref}s, no {@code $dynamicRef} or {@code $recursiveRef}, no foreign {@code $schema},
 * and every {@code pattern} and {@code patternProperties} key at most 256 characters and accepted by
 * RE2/J. The validator evaluates patterns with {@link Re2RegularExpressionFactory} and its schema
 * loader never fetches remote resources, so a schema can neither reach the network nor run an
 * unbounded pattern (invariant EXT-6).
 */
@Component
public class EdpSchemaCompiler {

    /** The only {@code $schema} a payload schema may declare. */
    public static final String DIALECT = "https://json-schema.org/draft/2020-12/schema";

    private static final String PAYLOAD_BASE = "urn:foresignal:edp:payload-schema:";
    private static final String EXPORTED_BASE = "https://foresignal.ai/protocol/edp-1.0/";

    private final SchemaRegistry registry;
    private final JsonMapper json = JsonMapper.builder().build();
    private final Map<String, Schema> exported = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    /** Creates the compiler. */
    public EdpSchemaCompiler() {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder()
                .regularExpressionFactory(Re2RegularExpressionFactory.INSTANCE)
                .pathType(PathType.JSON_POINTER)
                .build();
        this.registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12, builder -> builder
                .schemaRegistryConfig(config)
                .schemaLoader(loader -> loader.fetchRemoteResources(false)));
    }

    /**
     * Vets and compiles one payload schema.
     *
     * @throws EdpSchemaException with every problem found, paths relative to the schema root
     */
    public CompiledPayloadSchema compile(JsonNode schema) {
        List<EdpSchemaError> problems = vet(schema);
        if (!problems.isEmpty()) {
            throw new EdpSchemaException(problems);
        }
        try {
            Schema compiled = registry.getSchema(
                    SchemaLocation.of(PAYLOAD_BASE + sequence.incrementAndGet()), schema);
            compiled.initializeValidators();
            return new CompiledPayloadSchema(compiled);
        } catch (RuntimeException e) {
            throw new EdpSchemaException(List.of(new EdpSchemaError("", "cannot be compiled: " + e.getMessage())));
        }
    }

    /**
     * The §7 problems of {@code schema}, without compiling it.
     *
     * @return empty when the schema is acceptable
     */
    public List<EdpSchemaError> vet(JsonNode schema) {
        List<EdpSchemaError> problems = new ArrayList<>();
        if (schema == null || !schema.isObject()) {
            problems.add(new EdpSchemaError("", "must be a JSON object"));
            return problems;
        }
        int size = json.writeValueAsString(schema).getBytes(StandardCharsets.UTF_8).length;
        if (size > EdpProtocol.MAX_SCHEMA_BYTES) {
            problems.add(new EdpSchemaError("", "is " + size + " bytes; the maximum is " + EdpProtocol.MAX_SCHEMA_BYTES));
            return problems;
        }
        JsonNode dialect = schema.get("$schema");
        if (dialect != null && !(dialect.isString() && DIALECT.equals(dialect.asString()))) {
            problems.add(new EdpSchemaError("/$schema", "must be absent or " + DIALECT));
        }
        walk(schema, "", problems);
        return problems;
    }

    private static void walk(JsonNode node, String path, List<EdpSchemaError> problems) {
        if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                walk(node.get(i), path + "/" + i, problems);
            }
            return;
        }
        if (!node.isObject()) {
            return;
        }
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            String child = path + "/" + escape(key);
            switch (key) {
                case "$ref" -> {
                    if (!value.isString() || !value.asString().startsWith("#")) {
                        problems.add(new EdpSchemaError(child, "only local references (starting with #) are allowed"));
                    }
                }
                case "$dynamicRef", "$recursiveRef" ->
                        problems.add(new EdpSchemaError(child, key + " is not allowed"));
                case "pattern" -> {
                    if (value.isString()) {
                        checkPattern(value.asString(), child, problems);
                    }
                }
                case "patternProperties" -> {
                    if (value.isObject()) {
                        for (Map.Entry<String, JsonNode> pattern : value.properties()) {
                            checkPattern(pattern.getKey(), child + "/" + escape(pattern.getKey()), problems);
                        }
                    }
                }
                default -> {
                    // Not a keyword the rules govern.
                }
            }
            walk(value, child, problems);
        }
    }

    private static void checkPattern(String regex, String path, List<EdpSchemaError> problems) {
        try {
            Re2RegularExpressionFactory.compile(regex);
        } catch (IllegalArgumentException e) {
            problems.add(new EdpSchemaError(path, e.getMessage()));
        }
    }

    private static String escape(String key) {
        return key.replace("~", "~0").replace("/", "~1");
    }

    /**
     * Validates {@code doc} against the exported protocol schema {@code schemaName}
     * ({@code manifest}, {@code batch}, {@code item}, {@code feature}, {@code event},
     * {@code observation}, {@code document}, {@code forecast}, {@code delete}, {@code status}).
     *
     * <p>{@code format} keywords are annotations in 2020-12; callers check time strings themselves.
     *
     * @return every problem, with JSON Pointer paths into {@code doc}
     * @throws IllegalArgumentException for an unknown schema name
     */
    public List<EdpSchemaError> validateShape(String schemaName, JsonNode doc) {
        Schema schema = exported.computeIfAbsent(schemaName, this::loadExported);
        return CompiledPayloadSchema.errors(schema.validate(doc));
    }

    private Schema loadExported(String name) {
        if (!EdpProtocol.SCHEMA_NAMES.contains(name)) {
            throw new IllegalArgumentException("Unknown EDP schema '" + name + "'");
        }
        String resource = "edp/schemas/" + name + ".schema.json";
        try (InputStream in = EdpSchemaCompiler.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("The EDP schema " + resource + " is not on the classpath");
            }
            JsonNode node = json.readTree(in);
            Schema schema = registry.getSchema(SchemaLocation.of(EXPORTED_BASE + name + ".schema.json"), node);
            schema.initializeValidators();
            return schema;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + resource, e);
        }
    }
}
