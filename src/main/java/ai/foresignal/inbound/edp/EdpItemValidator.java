package ai.foresignal.inbound.edp;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Validates the items of an EDP batch, each on its own, in the protocol's order
 * (docs/External-Data-Protocol.md §3.1).
 *
 * <p>Per item: a known {@code type}; the stream exists (a source-level {@code status} may omit it);
 * the type matches the stream's {@code itemType} ({@code delete} and {@code status} fit any
 * stream); the item schema; string and media limits; {@code properties} against the stream's
 * {@code payloadSchema}; geometry (§3.3); the observation period and one granularity per series;
 * forecast values; times, including {@code observedAt} no more than 5 minutes in the future. Invalid
 * items are rejected individually; valid items in the same batch are accepted. Validation of a batch
 * is bounded by {@link EdpProperties#validationBudget()}: once it is spent, every remaining item is
 * rejected {@code VALIDATION_BUDGET_EXCEEDED}.
 */
@Component
public class EdpItemValidator {

    /** How far ahead of receipt an {@code observedAt} may be. */
    public static final Duration MAX_FUTURE = Duration.ofMinutes(5);

    private static final String[] QUANTILES = {"p05", "p10", "p25", "p50", "p75", "p90", "p95"};
    private static final String[] REQUIRED_QUANTILES = {"p05", "p25", "p50", "p75", "p95"};
    private static final String[] TIMES = {"observedAt", "eventTime", "validUntil", "knownAt", "publishedAt", "madeAt"};

    private final EdpSchemaCompiler compiler;
    private final EdpProperties properties;
    private final Clock clock;

    /** Creates the validator. */
    public EdpItemValidator(EdpSchemaCompiler compiler, EdpProperties properties, Clock clock) {
        this.compiler = compiler;
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * The result of validating a batch's items.
     *
     * @param valid the accepted items, in batch order
     * @param rejected the refused items, in batch order
     */
    public record ItemCheck(List<EdpValidItem> valid, List<EdpItemRejection> rejected) {

        /** Copies the lists. */
        public ItemCheck {
            valid = valid == null ? List.of() : List.copyOf(valid);
            rejected = rejected == null ? List.of() : List.copyOf(rejected);
        }
    }

    /**
     * Validates {@code items}.
     *
     * @param items the batch's items
     * @param manifest the registered manifest
     * @param schemas the compiled payload schema per stream id
     * @param receivedAt when Foresignal received the batch
     */
    public ItemCheck validate(List<JsonNode> items, EdpManifestModel manifest,
            Map<String, CompiledPayloadSchema> schemas, Instant receivedAt) {
        List<EdpValidItem> valid = new ArrayList<>();
        List<EdpItemRejection> rejected = new ArrayList<>();
        Map<String, String> granularity = new HashMap<>();
        Instant deadline = clock.instant().plus(properties.validationBudget());
        long deadlineNanos = System.nanoTime() + properties.validationBudget().toNanos();
        for (int index = 0; index < items.size(); index++) {
            JsonNode item = items.get(index);
            if (System.nanoTime() >= deadlineNanos || !clock.instant().isBefore(deadline)) {
                rejected.add(EdpItemRejection.of(index, text(item, "stream"), text(item, "id"),
                        "VALIDATION_BUDGET_EXCEEDED", "the batch's validation time budget of "
                                + properties.validationBudget().toMillis() + " ms was spent"));
                continue;
            }
            Object result = check(index, item, manifest, schemas == null ? Map.of() : schemas, receivedAt, granularity);
            if (result instanceof EdpValidItem ok) {
                valid.add(ok);
            } else {
                rejected.add((EdpItemRejection) result);
            }
        }
        return new ItemCheck(valid, rejected);
    }

    private Object check(int index, JsonNode item, EdpManifestModel manifest,
            Map<String, CompiledPayloadSchema> schemas, Instant receivedAt, Map<String, String> granularity) {
        String streamId = text(item, "stream");
        String id = text(item, "id");
        if (item == null || !item.isObject()) {
            return EdpItemRejection.of(index, null, null, "SCHEMA_INVALID", "an item must be a JSON object");
        }
        String type = text(item, "type");
        if (type == null || !EdpProtocol.ITEM_TYPES.contains(type)) {
            return EdpItemRejection.of(index, streamId, id, "SCHEMA_INVALID",
                    "type must be one of " + EdpProtocol.ITEM_TYPES);
        }
        EdpManifestModel.Stream stream = null;
        if (streamId != null || !"status".equals(type)) {
            stream = manifest.stream(streamId);
            if (stream == null) {
                return EdpItemRejection.of(index, streamId, id, "UNKNOWN_STREAM",
                        "the manifest declares no stream '" + streamId + "'");
            }
            if (!"delete".equals(type) && !"status".equals(type) && !type.equals(stream.itemType())) {
                return EdpItemRejection.of(index, streamId, id, "ITEM_TYPE_MISMATCH",
                        "stream '" + streamId + "' carries " + stream.itemType() + " items, not " + type);
            }
        }

        List<EdpSchemaError> shape = compiler.validateShape(type, item);
        if (!shape.isEmpty()) {
            return new EdpItemRejection(index, streamId, id, "SCHEMA_INVALID", shape);
        }

        List<EdpSchemaError> limits = new ArrayList<>();
        checkStrings(item, "", limits);
        JsonNode media = item.get("media");
        if (media != null && media.isArray() && media.size() > EdpProtocol.MAX_MEDIA) {
            limits.add(new EdpSchemaError("/media", "has " + media.size() + " entries; the maximum is "
                    + EdpProtocol.MAX_MEDIA));
        }
        if (!limits.isEmpty()) {
            return new EdpItemRejection(index, streamId, id, "LIMIT_EXCEEDED", limits);
        }

        for (String name : TIMES) {
            JsonNode value = item.get(name);
            if (value != null && !value.isNull() && EdpTimes.parse(item, name) == null) {
                return new EdpItemRejection(index, streamId, id, "SCHEMA_INVALID",
                        List.of(new EdpSchemaError("/" + name, "must be an RFC 3339 date-time with an offset")));
            }
        }
        Instant observedAt = EdpTimes.parse(item, "observedAt");
        if (observedAt != null && observedAt.isAfter(receivedAt.plus(MAX_FUTURE))) {
            return EdpItemRejection.of(index, streamId, id, "OBSERVED_AT_IN_FUTURE",
                    "observedAt " + observedAt + " is more than 5 minutes after receipt (" + receivedAt + ")");
        }
        boolean needsObservedAt = List.of("feature", "event", "observation", "document").contains(type);
        if (needsObservedAt && observedAt == null && (stream == null || !stream.observedAtReceived())) {
            return EdpItemRejection.of(index, streamId, id, "MISSING_OBSERVED_AT",
                    "observedAt is required unless the stream declares observedAtMode: received");
        }

        if (stream != null && !"status".equals(type) && !"delete".equals(type)) {
            CompiledPayloadSchema schema = schemas.get(stream.id());
            JsonNode properties = item.get("properties");
            if (schema != null) {
                List<EdpSchemaError> payload = schema.validate(
                        properties == null || properties.isNull()
                                ? tools.jackson.databind.node.JsonNodeFactory.instance.objectNode()
                                : properties);
                if (!payload.isEmpty()) {
                    List<EdpSchemaError> prefixed = new ArrayList<>();
                    payload.forEach(e -> prefixed.add(new EdpSchemaError("/properties" + e.path(), e.message())));
                    return new EdpItemRejection(index, streamId, id, "PAYLOAD_INVALID", prefixed);
                }
            }
            List<EdpSchemaError> geometry = EdpGeometryValidator.validate(item.get("geometry"), stream.geometry(),
                    "/geometry");
            if (!geometry.isEmpty()) {
                return new EdpItemRejection(index, streamId, id, "GEOMETRY_INVALID", geometry);
            }
        }

        if ("observation".equals(type)) {
            String t = text(item, "t");
            EdpPeriods.Period period = EdpPeriods.parse(t);
            if (period == null) {
                return EdpItemRejection.of(index, streamId, id, "PERIOD_INVALID",
                        "t '" + t + "' is not YYYY, YYYY-Qn, YYYY-MM, YYYY-Www, YYYY-MM-DD or an RFC 3339 timestamp");
            }
            String series = streamId + "\u0000" + text(item, "seriesKey");
            String previous = granularity.putIfAbsent(series, period.granularity());
            if (previous != null && !previous.equals(period.granularity())) {
                return EdpItemRejection.of(index, streamId, id, "GRANULARITY_MIXED",
                        "series '" + text(item, "seriesKey") + "' already uses " + previous + " periods, not "
                                + period.granularity());
            }
        }
        if ("forecast".equals(type)) {
            String problem = forecastProblem(item);
            if (problem != null) {
                return EdpItemRejection.of(index, streamId, id, "FORECAST_INVALID", problem);
            }
        }
        return new EdpValidItem(index, item, type, stream);
    }

    /** The problem with a forecast's question and values, or null. */
    static String forecastProblem(JsonNode item) {
        JsonNode question = item.get("question");
        JsonNode questionRef = item.get("questionRef");
        boolean hasQuestion = question != null && question.isObject();
        boolean hasRef = questionRef != null && questionRef.isString() && !questionRef.asString().isBlank();
        if (!hasQuestion && !hasRef) {
            return "a forecast needs a question or a questionRef";
        }
        JsonNode probability = item.get("probability");
        JsonNode quantiles = item.get("quantiles");
        boolean hasProbability = probability != null && probability.isNumber();
        boolean hasQuantiles = quantiles != null && quantiles.isObject();
        if (!hasProbability && !hasQuantiles) {
            return "a forecast needs a probability or quantiles";
        }
        if (hasProbability && (probability.doubleValue() < 0 || probability.doubleValue() > 1)) {
            return "probability must be between 0 and 1";
        }
        if (hasQuantiles) {
            for (String name : REQUIRED_QUANTILES) {
                JsonNode value = quantiles.get(name);
                if (value == null || !value.isNumber()) {
                    return "quantiles need p05, p25, p50, p75 and p95";
                }
            }
            Double last = null;
            String lastName = null;
            for (String name : QUANTILES) {
                JsonNode value = quantiles.get(name);
                if (value == null || value.isNull()) {
                    continue;
                }
                if (!value.isNumber() || !Double.isFinite(value.doubleValue())) {
                    return "quantile " + name + " must be a finite number";
                }
                if (last != null && value.doubleValue() < last) {
                    return "quantiles must be non-decreasing, but " + name + " < " + lastName;
                }
                last = value.doubleValue();
                lastName = name;
            }
        }
        return null;
    }

    /** Collects strings over the limits: {@code text} 20,000 characters, everything else 2,048. */
    static void checkStrings(JsonNode node, String path, List<EdpSchemaError> errors) {
        if (node.isString()) {
            int max = "/text".equals(path) ? EdpProtocol.MAX_TEXT_LENGTH : EdpProtocol.MAX_STRING_LENGTH;
            if (node.asString().length() > max) {
                errors.add(new EdpSchemaError(path, "is " + node.asString().length()
                        + " characters; the maximum is " + max));
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                checkStrings(node.get(i), path + "/" + i, errors);
            }
        } else if (node.isObject()) {
            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                if (entry.getKey().length() > EdpProtocol.MAX_STRING_LENGTH) {
                    errors.add(new EdpSchemaError(path, "has a member name longer than "
                            + EdpProtocol.MAX_STRING_LENGTH + " characters"));
                }
                checkStrings(entry.getValue(), path + "/" + entry.getKey().replace("~", "~0").replace("/", "~1"),
                        errors);
            }
        }
    }

    private static String text(JsonNode node, String name) {
        if (node == null || !node.isObject()) {
            return null;
        }
        JsonNode value = node.get(name);
        return value == null || value.isNull() || !value.isValueNode() ? null : value.asString();
    }
}
