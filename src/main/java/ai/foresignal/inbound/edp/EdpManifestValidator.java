package ai.foresignal.inbound.edp;

import ai.foresignal.settings.Durations;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Validates an EDP manifest at registration (docs/External-Data-Protocol.md §2, §3.1, §7).
 *
 * <p>Checks the manifest schema, the protocol minor against {@link EdpProperties#maxMinor()}, that
 * at least one delivery mechanism is declared, unique stream ids, that layer streams carry
 * {@code feature} or {@code event} items with a geometry, slugs, the pull and search transport,
 * field roles, presentation hints, TTLs, and compiles every {@code payloadSchema} through
 * {@link EdpSchemaCompiler}. Every problem is reported, not just the first.
 */
@Component
public class EdpManifestValidator {

    static final Pattern SLUG = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
    static final Pattern FIELD = Pattern.compile("^[A-Za-z0-9_]{1,64}$");
    static final Pattern COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");
    static final int MAX_ZOOM = 22;

    private final EdpSchemaCompiler compiler;
    private final EdpProperties properties;

    /** Creates the validator. */
    public EdpManifestValidator(EdpSchemaCompiler compiler, EdpProperties properties) {
        this.compiler = compiler;
        this.properties = properties;
    }

    /**
     * The result of validating one manifest.
     *
     * @param protocolUnsupported true when the protocol minor is above what this instance supports
     * @param errors every problem; empty when valid
     * @param model the parsed manifest, or null when invalid
     * @param payloadSchemas the compiled payload schema per stream id (streams without one are absent)
     */
    public record ManifestCheck(
            boolean protocolUnsupported,
            List<EdpSchemaError> errors,
            EdpManifestModel model,
            Map<String, CompiledPayloadSchema> payloadSchemas) {

        /** Copies the collections. */
        public ManifestCheck {
            errors = errors == null ? List.of() : List.copyOf(errors);
            payloadSchemas = payloadSchemas == null ? Map.of() : Map.copyOf(payloadSchemas);
        }

        /** True when the manifest may be registered. */
        public boolean valid() {
            return errors.isEmpty() && model != null;
        }
    }

    /** Validates {@code manifest}. Never throws for invalid input; the problems are in the result. */
    public ManifestCheck validate(JsonNode manifest) {
        List<EdpSchemaError> errors = new ArrayList<>();
        if (manifest == null || !manifest.isObject()) {
            errors.add(new EdpSchemaError("", "the manifest must be a JSON object"));
            return new ManifestCheck(false, errors, null, Map.of());
        }

        boolean protocolUnsupported = false;
        JsonNode protocolNode = manifest.get("protocol");
        int minor = EdpProtocol.minor(protocolNode != null && protocolNode.isString() ? protocolNode.asString() : null);
        if (minor < 0) {
            errors.add(new EdpSchemaError("/protocol", "must be foresignal-edp/1.N"));
        } else if (minor > properties.maxMinor()) {
            protocolUnsupported = true;
            errors.add(new EdpSchemaError("/protocol", "foresignal-edp/1." + minor
                    + " is newer than this instance supports (highest minor " + properties.maxMinor() + ")"));
        }

        errors.addAll(compiler.validateShape("manifest", manifest));

        checkTransport(manifest.path("transport"), errors);
        checkSource(manifest.path("source"), errors);

        Map<String, CompiledPayloadSchema> schemas = new LinkedHashMap<>();
        JsonNode streams = manifest.path("streams");
        Set<String> ids = new HashSet<>();
        if (streams.isArray()) {
            for (int i = 0; i < streams.size(); i++) {
                checkStream(streams.get(i), "/streams/" + i, ids, schemas, errors);
            }
        }

        EdpManifestModel model = null;
        if (errors.isEmpty()) {
            try {
                model = EdpManifestModel.parse(manifest);
            } catch (RuntimeException e) {
                errors.add(new EdpSchemaError("", e.getMessage()));
            }
        }
        return new ManifestCheck(protocolUnsupported, dedupe(errors), model, model == null ? Map.of() : schemas);
    }

    private static void checkSource(JsonNode source, List<EdpSchemaError> errors) {
        if (!source.isObject()) {
            return;
        }
        String id = EdpManifestModel.text(source, "id");
        if (id != null && (!SLUG.matcher(id).matches() || id.length() > 48)) {
            errors.add(new EdpSchemaError("/source/id", "must be a slug of at most 48 characters"));
        }
        String originator = EdpManifestModel.text(source.path("originator"), "id");
        if (originator != null && !SLUG.matcher(originator).matches()) {
            errors.add(new EdpSchemaError("/source/originator/id", "must be a slug"));
        }
    }

    private static void checkTransport(JsonNode transport, List<EdpSchemaError> errors) {
        if (!transport.isObject()) {
            errors.add(new EdpSchemaError("/transport", "is required"));
            return;
        }
        boolean push = transport.path("push").asBoolean(false);
        JsonNode pull = transport.get("pull");
        boolean hasPull = pull != null && pull.isObject();
        if (!push && !hasPull) {
            errors.add(new EdpSchemaError("/transport",
                    "must declare at least one delivery mechanism: push: true or a pull endpoint"));
        }
        if (hasPull) {
            checkEndpoint(pull, "itemsUrl", "/transport/pull", errors);
            JsonNode interval = pull.get("intervalSeconds");
            if (interval != null && !interval.isNull() && (!interval.isIntegralNumber() || interval.asInt() < 60)) {
                errors.add(new EdpSchemaError("/transport/pull/intervalSeconds", "must be an integer of at least 60"));
            }
        }
        JsonNode search = transport.get("search");
        if (search != null && search.isObject()) {
            checkEndpoint(search, "url", "/transport/search", errors);
        }
    }

    private static void checkEndpoint(JsonNode endpoint, String urlField, String path, List<EdpSchemaError> errors) {
        String url = EdpManifestModel.text(endpoint, urlField);
        if (url == null || !isHttpUrl(url)) {
            errors.add(new EdpSchemaError(path + "/" + urlField, "must be an absolute http or https URL"));
        }
        String auth = EdpManifestModel.text(endpoint, "auth");
        if (auth != null && !List.of("none", "bearer", "header", "basic").contains(auth)) {
            errors.add(new EdpSchemaError(path + "/auth", "must be none, bearer, header or basic"));
        }
        if ("header".equals(auth) && EdpManifestModel.text(endpoint, "header") == null) {
            errors.add(new EdpSchemaError(path + "/header", "is required when auth is header"));
        }
    }

    static boolean isHttpUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            return uri.isAbsolute() && uri.getHost() != null
                    && ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private void checkStream(JsonNode stream, String path, Set<String> ids, Map<String, CompiledPayloadSchema> schemas,
            List<EdpSchemaError> errors) {
        if (!stream.isObject()) {
            return;
        }
        String id = EdpManifestModel.text(stream, "id");
        if (id != null) {
            if (!SLUG.matcher(id).matches()) {
                errors.add(new EdpSchemaError(path + "/id", "must be a slug"));
            }
            if (!ids.add(id)) {
                errors.add(new EdpSchemaError(path + "/id", "duplicate stream id '" + id + "'"));
            }
        }
        String kind = EdpManifestModel.text(stream, "kind");
        if (kind != null && !SLUG.matcher(kind).matches()) {
            errors.add(new EdpSchemaError(path + "/kind", "must be a slug"));
        }
        String mode = EdpManifestModel.text(stream, "mode");
        String itemType = EdpManifestModel.text(stream, "itemType");
        String geometry = EdpManifestModel.text(stream, "geometry");
        if ("layer".equals(mode)) {
            if (itemType != null && !"feature".equals(itemType) && !"event".equals(itemType)) {
                errors.add(new EdpSchemaError(path + "/itemType", "a layer stream carries feature or event items"));
            }
            if ("none".equals(geometry)) {
                errors.add(new EdpSchemaError(path + "/geometry", "a layer stream must have a geometry"));
            }
        }
        String ttl = EdpManifestModel.text(stream, "ttl");
        if (ttl != null) {
            try {
                Durations.parse(ttl);
            } catch (RuntimeException e) {
                errors.add(new EdpSchemaError(path + "/ttl", "must be an ISO-8601 duration"));
            }
        }
        if (EdpManifestModel.text(stream, "snapshotScope") != null
                && !"snapshot".equals(EdpManifestModel.text(stream, "update"))) {
            errors.add(new EdpSchemaError(path + "/snapshotScope", "only snapshot streams have a snapshot scope"));
        }
        checkFields(stream.path("fields"), path + "/fields", errors);
        checkPresentation(stream.path("presentation"), path + "/presentation", errors);

        JsonNode schema = stream.get("payloadSchema");
        if (schema != null && !schema.isNull()) {
            try {
                CompiledPayloadSchema compiled = compiler.compile(schema);
                if (id != null) {
                    schemas.put(id, compiled);
                }
            } catch (EdpSchemaException e) {
                for (EdpSchemaError error : e.errors()) {
                    errors.add(new EdpSchemaError(path + "/payloadSchema" + error.path(), error.message()));
                }
            }
        }
    }

    private static void checkFields(JsonNode fields, String path, List<EdpSchemaError> errors) {
        if (!fields.isObject()) {
            return;
        }
        String title = EdpManifestModel.text(fields, "title");
        if (title != null && !FIELD.matcher(title).matches()) {
            errors.add(new EdpSchemaError(path + "/title", "must be a top-level payload name"));
        }
        List<String> significant = EdpManifestModel.strings(fields.path("significant"));
        List<String> volatileFields = EdpManifestModel.strings(fields.path("volatile"));
        checkNames(significant, path + "/significant", errors);
        checkNames(volatileFields, path + "/volatile", errors);
        for (String name : significant) {
            if (volatileFields.contains(name)) {
                errors.add(new EdpSchemaError(path, "'" + name + "' is both significant and volatile"));
            }
        }
    }

    private static void checkNames(List<String> names, String path, List<EdpSchemaError> errors) {
        for (int i = 0; i < names.size(); i++) {
            if (!FIELD.matcher(names.get(i)).matches()) {
                errors.add(new EdpSchemaError(path + "/" + i, "must be a top-level payload name"));
            }
        }
    }

    private static void checkPresentation(JsonNode presentation, String path, List<EdpSchemaError> errors) {
        if (!presentation.isObject()) {
            return;
        }
        String color = EdpManifestModel.text(presentation, "color");
        if (color != null && !COLOR.matcher(color).matches()) {
            errors.add(new EdpSchemaError(path + "/color", "must be #RRGGBB"));
        }
        for (String zoom : List.of("minZoom", "maxZoom")) {
            JsonNode value = presentation.get(zoom);
            if (value != null && !value.isNull()
                    && (!value.isIntegralNumber() || value.asInt() < 0 || value.asInt() > MAX_ZOOM)) {
                errors.add(new EdpSchemaError(path + "/" + zoom, "must be an integer from 0 to " + MAX_ZOOM));
            }
        }
        String label = EdpManifestModel.text(presentation, "labelField");
        if (label != null && !FIELD.matcher(label).matches()) {
            errors.add(new EdpSchemaError(path + "/labelField", "must be a top-level payload name"));
        }
        checkNames(EdpManifestModel.strings(presentation.path("tileProperties")), path + "/tileProperties", errors);
    }

    private static List<EdpSchemaError> dedupe(List<EdpSchemaError> errors) {
        return new ArrayList<>(new java.util.LinkedHashSet<>(errors));
    }
}
