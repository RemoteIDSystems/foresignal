package ai.foresignal.inbound;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.inbound.format.IngestFormatRegistry;
import ai.foresignal.inbound.format.RecordMapping;
import ai.foresignal.inbound.signature.SignatureVerifiers;
import ai.foresignal.inbound.signature.VerifierConfig;
import ai.foresignal.settings.Durations;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Reads and validates a {@code CustomSourceInput} body (OpenAPI), collecting every field error
 * before failing (prompt 024).
 *
 * <p>Members that later prompts own and enforce ({@code profile} 039, {@code mapping},
 * {@code preset}, {@code presetParams} 059, {@code allowPathKey}, {@code ingestAllowCidrs} 039,
 * {@code captureSamples} 059, {@code licenceTerms} 109) are refused with a field error when set to
 * anything but their default: accepting them unenforced would fail open.
 */
@Component
public class CustomSourceInputReader {

    /** Custom source ids. */
    public static final Pattern ID_PATTERN = Pattern.compile("^custom-[a-z0-9]+(-[a-z0-9]+)*$");

    /** Kinds, like source ids. */
    public static final Pattern KIND_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    /** Top-level payload field names. */
    public static final Pattern FIELD_PATTERN = Pattern.compile("^[A-Za-z0-9_]{1,64}$");

    static final int MAX_ID_LENGTH = 64;
    static final int MAX_RATE_PER_MINUTE = 6_000;
    static final int MAX_RECORDS_PER_MINUTE = 60_000;
    static final int DEFAULT_RATE_PER_MINUTE = 60;
    static final int DEFAULT_RECORDS_PER_MINUTE = 6_000;

    /** Members a later prompt owns, with that prompt. */
    static final Map<String, String> LATER_MEMBERS = Map.of(
            "profile", "039",
            "mapping", "059",
            "preset", "059",
            "presetParams", "059",
            "allowPathKey", "039",
            "ingestAllowCidrs", "039",
            "captureSamples", "059",
            "licenceTerms", "109");

    private final IngestFormatRegistry formats;
    private final SignatureVerifiers verifiers;
    private final JsonMapper json;

    /** Creates the reader. */
    public CustomSourceInputReader(IngestFormatRegistry formats, SignatureVerifiers verifiers, JsonMapper json) {
        this.formats = formats;
        this.verifiers = verifiers;
        this.json = json;
    }

    /**
     * Validates {@code body}.
     *
     * @param body the request body
     * @param existing the stored verifier configuration when updating (secrets may then be omitted),
     *     or null when creating
     * @param secretSet the formats whose secret is already stored, when updating
     * @return the definition
     * @throws ValidationException listing every problem
     */
    public CustomSourceDefinition read(JsonNode body, Map<String, VerifierConfig> existing, Set<String> secretSet) {
        if (body == null || !body.isObject()) {
            throw new InvalidInputException("The body must be a JSON object.");
        }
        List<FieldError> errors = new ArrayList<>();

        String id = text(body, "id");
        if (id == null) {
            errors.add(new FieldError("id", "is required"));
        } else if (id.length() > MAX_ID_LENGTH || !ID_PATTERN.matcher(id).matches()) {
            errors.add(new FieldError("id", "must match ^custom-[a-z0-9]+(-[a-z0-9]+)*$ and be at most 64 characters"));
        }
        String name = text(body, "name");
        if (name == null || name.isBlank()) {
            errors.add(new FieldError("name", "is required"));
        } else if (name.length() > 200) {
            errors.add(new FieldError("name", "must be at most 200 characters"));
        }

        List<String> kinds = strings(body, "kinds", errors);
        if (kinds.isEmpty()) {
            errors.add(new FieldError("kinds", "must list at least one kind"));
        }
        for (int i = 0; i < kinds.size(); i++) {
            if (!KIND_PATTERN.matcher(kinds.get(i)).matches()) {
                errors.add(new FieldError("kinds[" + i + "]", "must match ^[a-z0-9]+(-[a-z0-9]+)*$"));
            }
        }
        if (new LinkedHashSet<>(kinds).size() != kinds.size()) {
            errors.add(new FieldError("kinds", "must not repeat a kind"));
        }

        String recordTtl = text(body, "recordTtl");
        Duration ttl = null;
        if (recordTtl != null) {
            try {
                ttl = Durations.parsePositive(recordTtl);
            } catch (InvalidInputException e) {
                errors.add(new FieldError("recordTtl", e.getMessage()));
            }
        }

        List<String> tileProperties = fields(body, "tileProperties", errors);
        List<String> searchFields = body.has("searchFields") && !body.get("searchFields").isNull()
                ? fields(body, "searchFields", errors)
                : null;
        List<String> embedFields = fields(body, "embedFields", errors);
        List<String> volatileFields = fields(body, "volatileFields", errors);
        List<String> significantFields = fields(body, "significantFields", errors);
        String rankField = field(body, "rankField", errors);
        String titleField = field(body, "titleField", errors);

        int rate = integer(body, "rateLimitPerMinute", DEFAULT_RATE_PER_MINUTE, 1, MAX_RATE_PER_MINUTE, errors);
        int recordsPerMinute =
                integer(body, "recordsPerMinute", DEFAULT_RECORDS_PER_MINUTE, 1, MAX_RECORDS_PER_MINUTE, errors);

        String observedAtMode = enumValue(body, "observedAtMode", "UPSTREAM", Set.of("UPSTREAM", "RECEIVED"), errors);
        String ttlAnchor = enumValue(body, "ttlAnchor", "RECEIVED", Set.of("RECEIVED", "OBSERVED"), errors);

        Map<String, FieldSpec> fieldSpecs = fieldSpecs(body, errors);
        boolean privateSource = bool(body, "private", false, errors);
        String licence = text(body, "licence");
        String attribution = text(body, "attribution");
        String description = text(body, "description");

        ObjectNode formatOptions = formats(body, errors);
        Map<String, VerifierConfig> verifierConfigs = new LinkedHashMap<>();
        Map<String, String> secrets = new LinkedHashMap<>();
        verifiers(body, existing, secretSet, verifierConfigs, secrets, errors);

        for (Map.Entry<String, String> later : LATER_MEMBERS.entrySet()) {
            JsonNode value = body.get(later.getKey());
            if (value != null && !value.isNull() && !(value.isBoolean() && !value.booleanValue())) {
                errors.add(new FieldError(later.getKey(),
                        "arrives with prompt " + later.getValue() + " and is not accepted yet"));
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("The custom source is not valid.", errors);
        }
        return new CustomSourceDefinition(id, name, description, kinds, attribution, recordTtl, ttl, tileProperties,
                searchFields, embedFields, rankField, rate, observedAtMode, ttlAnchor, volatileFields,
                significantFields, titleField, fieldSpecs, privateSource, licence, recordsPerMinute, formatOptions,
                verifierConfigs, secrets);
    }

    private ObjectNode formats(JsonNode body, List<FieldError> errors) {
        JsonNode node = body.get("formats");
        ObjectNode out = JsonNodeFactory.instance.objectNode();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isObject()) {
            errors.add(new FieldError("formats", "must be an object keyed by format id"));
            return out;
        }
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            String format = entry.getKey();
            JsonNode options = entry.getValue();
            if (formats.find(format).isEmpty()) {
                errors.add(new FieldError("formats." + format, "is not a registered ingest format"));
                continue;
            }
            if (options == null || options.isNull()) {
                continue;
            }
            if (!options.isObject()) {
                errors.add(new FieldError("formats." + format, "must be an object"));
                continue;
            }
            if ("mapping".equals(format)) {
                try {
                    RecordMapping.parse(options);
                } catch (ValidationException e) {
                    for (FieldError error : e.getErrors()) {
                        errors.add(error);
                    }
                    if (e.getErrors().isEmpty()) {
                        errors.add(new FieldError("formats.mapping", e.getMessage()));
                    }
                    continue;
                }
            }
            out.set(format, options);
        }
        return out;
    }

    private void verifiers(JsonNode body, Map<String, VerifierConfig> existing, Set<String> secretSet,
            Map<String, VerifierConfig> configs, Map<String, String> secrets, List<FieldError> errors) {
        JsonNode node = body.get("verifiers");
        if (node == null || node.isNull()) {
            return;
        }
        if (!node.isObject()) {
            errors.add(new FieldError("verifiers", "must be an object keyed by format id"));
            return;
        }
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            String format = entry.getKey();
            JsonNode value = entry.getValue();
            String where = "verifiers." + format;
            if (formats.find(format).isEmpty()) {
                errors.add(new FieldError(where, "is not a registered ingest format"));
                continue;
            }
            if (value == null || !value.isObject()) {
                errors.add(new FieldError(where, "must be an object"));
                continue;
            }
            String scheme = text(value, "scheme");
            if (scheme == null || !verifiers.schemes().contains(scheme)) {
                errors.add(new FieldError(where + ".scheme", "must be one of " + verifiers.schemes()));
                continue;
            }
            String header = text(value, "header");
            boolean keyOptional = value.path("keyOptional").asBoolean(false);
            VerifierConfig config = new VerifierConfig(scheme, header, keyOptional);
            String secret = text(value, "secret");
            if (config.active()) {
                boolean stored = secretSet != null && secretSet.contains(format);
                if ((secret == null || secret.isEmpty()) && !stored) {
                    errors.add(new FieldError(where + ".secret", "is required for scheme " + scheme));
                    continue;
                }
                if (secret != null && secret.length() > 512) {
                    errors.add(new FieldError(where + ".secret", "must be at most 512 characters"));
                    continue;
                }
            }
            configs.put(format, config);
            if (secret != null && !secret.isEmpty()) {
                secrets.put(format, secret);
            }
        }
    }

    private Map<String, FieldSpec> fieldSpecs(JsonNode body, List<FieldError> errors) {
        JsonNode node = body.get("fieldSpecs");
        Map<String, FieldSpec> out = new LinkedHashMap<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isObject()) {
            errors.add(new FieldError("fieldSpecs", "must be an object keyed by payload field"));
            return out;
        }
        for (Map.Entry<String, JsonNode> entry : node.properties()) {
            String key = entry.getKey();
            if (!FIELD_PATTERN.matcher(key).matches()) {
                errors.add(new FieldError("fieldSpecs." + key, "keys must match ^[A-Za-z0-9_]{1,64}$"));
                continue;
            }
            JsonNode spec = entry.getValue();
            if (spec == null || !spec.isObject() || text(spec, "type") == null) {
                errors.add(new FieldError("fieldSpecs." + key + ".type", "is required"));
                continue;
            }
            try {
                ObjectNode normalized = (ObjectNode) spec.deepCopy();
                normalized.put("type", text(spec, "type").toUpperCase(Locale.ROOT));
                out.put(key, json.treeToValue(normalized, FieldSpec.class));
            } catch (RuntimeException e) {
                errors.add(new FieldError("fieldSpecs." + key, "is not a valid FieldSpec"));
            }
        }
        return out;
    }

    // ---------- small readers ----------

    static String text(JsonNode node, String name) {
        JsonNode value = node.get(name);
        return value == null || value.isNull() || !value.isValueNode() ? null : value.asString();
    }

    private static List<String> strings(JsonNode body, String name, List<FieldError> errors) {
        JsonNode value = body.get(name);
        List<String> out = new ArrayList<>();
        if (value == null || value.isNull()) {
            return out;
        }
        if (!value.isArray()) {
            errors.add(new FieldError(name, "must be an array of strings"));
            return out;
        }
        for (int i = 0; i < value.size(); i++) {
            JsonNode item = value.get(i);
            if (item == null || !item.isString()) {
                errors.add(new FieldError(name + "[" + i + "]", "must be a string"));
            } else {
                out.add(item.asString());
            }
        }
        return out;
    }

    private static List<String> fields(JsonNode body, String name, List<FieldError> errors) {
        List<String> values = strings(body, name, errors);
        for (int i = 0; i < values.size(); i++) {
            if (!FIELD_PATTERN.matcher(values.get(i)).matches()) {
                errors.add(new FieldError(name + "[" + i + "]", "must be a top-level payload key matching ^[A-Za-z0-9_]{1,64}$"));
            }
        }
        return values;
    }

    private static String field(JsonNode body, String name, List<FieldError> errors) {
        String value = text(body, name);
        if (value != null && !FIELD_PATTERN.matcher(value).matches()) {
            errors.add(new FieldError(name, "must be a top-level payload key matching ^[A-Za-z0-9_]{1,64}$"));
        }
        return value;
    }

    private static int integer(JsonNode body, String name, int fallback, int min, int max, List<FieldError> errors) {
        JsonNode value = body.get(name);
        if (value == null || value.isNull()) {
            return fallback;
        }
        if (!value.isIntegralNumber() || value.asLong() < min || value.asLong() > max) {
            errors.add(new FieldError(name, "must be an integer from " + min + " to " + max));
            return fallback;
        }
        return value.asInt();
    }

    private static boolean bool(JsonNode body, String name, boolean fallback, List<FieldError> errors) {
        JsonNode value = body.get(name);
        if (value == null || value.isNull()) {
            return fallback;
        }
        if (!value.isBoolean()) {
            errors.add(new FieldError(name, "must be a boolean"));
            return fallback;
        }
        return value.booleanValue();
    }

    private static String enumValue(JsonNode body, String name, String fallback, Set<String> allowed,
            List<FieldError> errors) {
        String value = text(body, name);
        if (value == null) {
            return fallback;
        }
        String upper = value.toUpperCase(Locale.ROOT);
        if (!allowed.contains(upper)) {
            errors.add(new FieldError(name, "must be one of " + allowed));
            return fallback;
        }
        return upper;
    }

    /** The default search fields, for callers that need to show them. */
    static List<String> defaultSearchFields() {
        return SourceDescriptor.DEFAULT_SEARCH_FIELDS;
    }
}
