package ai.foresignal.forecast;

import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.forecast.resolve.ResolverSpec;
import ai.foresignal.security.Ulids;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/**
 * Parses and validates the forecast ledger's request bodies (prompt 122): {@code ForecastQuestionInput},
 * {@code ForecastQuestionPatch}, {@code ForecastInput}, {@code ForecastResolutionInput}, {@code ForecastAnnulInput},
 * {@code ForecasterUpdate} and {@code ForecastEvaluationRequest}. Pure and static, so the Postman contract test can
 * run the same checks the API runs. Resolvers are checked by {@link ResolverSpec} ({@code RESOLVER_INVALID}); forecasts
 * and resolutions that do not fit their question are {@code FORECAST_INVALID}; everything else is
 * {@code VALIDATION_FAILED}.
 */
public final class ForecastInputs {

    /** The longest resolution criteria. */
    public static final int MAX_CRITERIA = 2_000;

    /** The longest rationale. */
    public static final int MAX_RATIONALE = 20_000;

    /** The most citations on one forecast. */
    public static final int MAX_CITATIONS = 100;

    /** The most outcome labels of a multiple or ordered question. */
    public static final int MAX_LABELS = 50;

    /** How far a distribution may stray from summing to one. */
    public static final double SUM_TOLERANCE = 1e-6;

    /** The canonical object-reference grammar ({@code {type}:{id}}). */
    public static final Pattern OBJECT_REF = Pattern.compile("^[a-z][a-z-]*:.+$");

    /** The forecaster id grammar. */
    public static final Pattern FORECASTER_ID = Pattern.compile("^[a-z0-9][a-z0-9._:-]{0,127}$");

    private static final Set<String> QUESTION_FIELDS = Set.of("title", "type", "outcomes", "resolutionCriteria",
            "resolver", "opensAt", "closesAt", "resolveBy", "category", "tags", "links", "dedupKey", "private");

    private static final Set<String> PATCH_FIELDS = Set.of("title", "tags", "closesAt", "resolutionCriteria",
            "resolver");

    private static final Set<String> FORECAST_FIELDS = Set.of("probability", "distribution", "quantiles", "rationale",
            "citations", "forecasterId");

    private static final Set<String> LINK_FIELDS = Set.of("situationId", "hypothesisSetId", "precursorId",
            "findingId", "seriesRef", "outlookId");

    private ForecastInputs() {}

    /**
     * A validated question.
     *
     * @param title the title
     * @param type the type
     * @param outcomes the outcome space
     * @param criteria the resolution criteria
     * @param resolver the validated resolver (with its version)
     * @param opensAt when it opens
     * @param closesAt when it closes
     * @param resolveBy when it resolves
     * @param category the category
     * @param tags the tags
     * @param links the links ({@code QuestionLinks} members)
     * @param dedupKey the dedup key, or null
     * @param privateQuestion whether it is private
     */
    public record QuestionDraft(String title, String type, Map<String, Object> outcomes, String criteria,
            Map<String, Object> resolver, Instant opensAt, Instant closesAt, Instant resolveBy, String category,
            List<String> tags, Map<String, String> links, String dedupKey, boolean privateQuestion) {}

    /**
     * A forecast's value: exactly one of the three forms.
     *
     * @param probability binary
     * @param distribution multiple or ordered
     * @param quantiles numeric
     */
    public record ForecastValue(Double probability, Map<String, Double> distribution, Map<String, Double> quantiles) {

        /** A binary value. */
        public static ForecastValue binary(double p) {
            return new ForecastValue(p, null, null);
        }

        /** A distribution. */
        public static ForecastValue distribution(Map<String, Double> d) {
            return new ForecastValue(null, d, null);
        }

        /** Quantiles. */
        public static ForecastValue quantiles(Map<String, Double> q) {
            return new ForecastValue(null, null, q);
        }
    }

    /**
     * A parsed forecast submission.
     *
     * @param value the value
     * @param rationale the rationale, or null
     * @param citations the citations
     * @param forecasterId the named forecaster (agent scope), or null
     */
    public record ForecastSubmission(ForecastValue value, String rationale, List<Map<String, Object>> citations,
            String forecasterId) {}

    /**
     * A parsed manual resolution.
     *
     * @param outcome the outcome label, or null (numeric)
     * @param value the value, or null
     * @param evidence the evidence refs
     * @param note the note, or null
     */
    public record ResolutionSubmission(String outcome, Double value, List<String> evidence, String note) {}

    /**
     * A parsed evaluation request.
     *
     * @param forecasterId the forecaster
     * @param from the start of the range
     * @param to the end of the range
     * @param categories the categories (empty for all)
     * @param maxQuestions the cap, or null
     */
    public record EvaluationRequest(String forecasterId, Instant from, Instant to, List<String> categories,
            Integer maxQuestions) {}

    // ------------------------------------------------------------------------- questions

    /**
     * Parses a {@code ForecastQuestionInput}.
     *
     * @param body the body
     * @param now the server time (the default {@code opensAt})
     * @throws ValidationException 400 {@code VALIDATION_FAILED} or {@code RESOLVER_INVALID}
     */
    public static QuestionDraft question(JsonNode body, Instant now) {
        requireObject(body, "ForecastQuestionInput");
        List<FieldError> errors = new ArrayList<>();
        unknown(body, QUESTION_FIELDS, errors);
        String title = text(body, "title", 1, 300, true, errors);
        String type = text(body, "type", 1, 16, true, errors);
        if (type != null && !Ledger.TYPES.contains(type)) {
            errors.add(new FieldError("type", "must be one of " + Ledger.TYPES));
            type = null;
        }
        Map<String, Object> outcomes = outcomes(body.get("outcomes"), type, errors);
        String criteria = text(body, "resolutionCriteria", 1, MAX_CRITERIA, true, errors);
        Instant opensAt = instant(body, "opensAt", false, errors);
        Instant closesAt = instant(body, "closesAt", true, errors);
        Instant resolveBy = instant(body, "resolveBy", true, errors);
        String category = text(body, "category", 1, 64, true, errors);
        List<String> tags = tags(body.get("tags"), errors);
        Map<String, String> links = links(body.get("links"), errors);
        String dedupKey = text(body, "dedupKey", 1, 200, false, errors);
        boolean privateQuestion = false;
        if (body.hasNonNull("private")) {
            if (!body.get("private").isBoolean()) {
                errors.add(new FieldError("private", "must be a boolean"));
            } else {
                privateQuestion = body.get("private").asBoolean();
            }
        }
        Instant opens = opensAt == null ? now : opensAt;
        if (closesAt != null && !closesAt.isAfter(opens)) {
            errors.add(new FieldError("closesAt", "must be after opensAt"));
        }
        if (closesAt != null && resolveBy != null && resolveBy.isBefore(closesAt)) {
            errors.add(new FieldError("resolveBy", "must not be before closesAt"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The question is not valid.", errors);
        }
        if (!body.hasNonNull("resolver") || !body.get("resolver").isObject()) {
            throw ResolverSpec.invalid("resolver", "is required and must be an object");
        }
        Map<String, Object> resolver = ResolverSpec.validate(JsonValues.map(body.get("resolver")), type);
        return new QuestionDraft(title, type, outcomes, criteria, resolver, opens, closesAt, resolveBy, category, tags,
                links, dedupKey, privateQuestion);
    }

    /**
     * Parses a {@code ForecastQuestionPatch} (JSON merge patch) into the members present.
     *
     * @param body the body
     * @param questionType the question's type (for the resolver)
     * @throws ValidationException 400 {@code VALIDATION_FAILED} or {@code RESOLVER_INVALID}
     */
    public static Map<String, Object> patch(JsonNode body, String questionType) {
        requireObject(body, "ForecastQuestionPatch");
        List<FieldError> errors = new ArrayList<>();
        unknown(body, PATCH_FIELDS, errors);
        Map<String, Object> out = new LinkedHashMap<>();
        if (body.has("title")) {
            out.put("title", text(body, "title", 1, 300, true, errors));
        }
        if (body.has("tags")) {
            out.put("tags", tags(body.get("tags"), errors));
        }
        if (body.has("closesAt")) {
            out.put("closesAt", instant(body, "closesAt", true, errors));
        }
        if (body.has("resolutionCriteria")) {
            out.put("resolutionCriteria", text(body, "resolutionCriteria", 1, MAX_CRITERIA, true, errors));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The question patch is not valid.", errors);
        }
        if (body.has("resolver")) {
            if (!body.get("resolver").isObject()) {
                throw ResolverSpec.invalid("resolver", "must be an object");
            }
            out.put("resolver", ResolverSpec.validate(JsonValues.map(body.get("resolver")), questionType));
        }
        return out;
    }

    /** The {@code reason} of a {@code ForecastAnnulInput}. */
    public static String annulReason(JsonNode body) {
        requireObject(body, "ForecastAnnulInput");
        List<FieldError> errors = new ArrayList<>();
        unknown(body, Set.of("reason"), errors);
        String reason = text(body, "reason", 1, 2_000, true, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("The annulment is not valid.", errors);
        }
        return reason;
    }

    private static Map<String, Object> outcomes(JsonNode node, String type, List<FieldError> errors) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (node != null && !node.isNull() && !node.isObject()) {
            errors.add(new FieldError("outcomes", "must be an object"));
            return out;
        }
        boolean labeled = Ledger.MULTIPLE.equals(type) || Ledger.ORDERED.equals(type);
        if (node != null && node.isObject()) {
            for (String field : node.propertyNames()) {
                if (!Set.of("labels", "unit", "lower", "upper").contains(field)) {
                    errors.add(new FieldError("outcomes." + field, "is not a QuestionOutcomes member"));
                }
            }
            if (node.hasNonNull("labels")) {
                JsonNode labels = node.get("labels");
                Set<String> seen = new LinkedHashSet<>();
                if (!labels.isArray()) {
                    errors.add(new FieldError("outcomes.labels", "must be an array of strings"));
                } else {
                    for (JsonNode l : labels) {
                        if (!l.isString() || l.asString().isBlank() || l.asString().length() > 100) {
                            errors.add(new FieldError("outcomes.labels", "labels are non-blank strings of at most 100"
                                    + " characters"));
                        } else if (!seen.add(l.asString())) {
                            errors.add(new FieldError("outcomes.labels", "'" + l.asString() + "' is repeated"));
                        }
                    }
                }
                out.put("labels", List.copyOf(seen));
            }
            if (node.hasNonNull("unit")) {
                out.put("unit", node.get("unit").isString() ? node.get("unit").asString() : null);
            }
            for (String bound : List.of("lower", "upper")) {
                if (node.hasNonNull(bound)) {
                    if (!node.get(bound).isNumber()) {
                        errors.add(new FieldError("outcomes." + bound, "must be a number"));
                    } else {
                        out.put(bound, node.get(bound).asDouble());
                    }
                }
            }
        }
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) out.getOrDefault("labels", List.of());
        if (labeled && (labels.size() < 2 || labels.size() > MAX_LABELS)) {
            errors.add(new FieldError("outcomes.labels", "a " + type + " question needs 2 to " + MAX_LABELS
                    + " labels"));
        }
        if (!labeled && !labels.isEmpty()) {
            errors.add(new FieldError("outcomes.labels", "only multiple and ordered questions have labels"));
        }
        if (out.get("lower") instanceof Double lo && out.get("upper") instanceof Double hi && lo >= hi) {
            errors.add(new FieldError("outcomes.upper", "must be greater than lower"));
        }
        if ((out.containsKey("lower") || out.containsKey("upper") || out.containsKey("unit"))
                && type != null && !Ledger.NUMERIC.equals(type)) {
            errors.add(new FieldError("outcomes", "unit and bounds belong to numeric questions"));
        }
        return out;
    }

    private static List<String> tags(JsonNode node, List<FieldError> errors) {
        List<String> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isArray() || node.size() > 32) {
            errors.add(new FieldError("tags", "must be an array of at most 32 strings"));
            return out;
        }
        for (JsonNode t : node) {
            if (!t.isString() || t.asString().isBlank() || t.asString().length() > 64) {
                errors.add(new FieldError("tags", "tags are non-blank strings of at most 64 characters"));
            } else if (!out.contains(t.asString())) {
                out.add(t.asString());
            }
        }
        return out;
    }

    private static Map<String, String> links(JsonNode node, List<FieldError> errors) {
        Map<String, String> out = new LinkedHashMap<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isObject()) {
            errors.add(new FieldError("links", "must be an object"));
            return out;
        }
        for (String field : node.propertyNames()) {
            JsonNode v = node.get(field);
            if (!LINK_FIELDS.contains(field)) {
                errors.add(new FieldError("links." + field, "is not a QuestionLinks member"));
                continue;
            }
            if (v == null || v.isNull()) {
                continue;
            }
            if (!v.isString()) {
                errors.add(new FieldError("links." + field, "must be a string"));
                continue;
            }
            String value = v.asString();
            if (field.equals("seriesRef")) {
                try {
                    SeriesRef.parse(value);
                } catch (RuntimeException e) {
                    errors.add(new FieldError("links.seriesRef", "is not a series reference"));
                    continue;
                }
            } else if (!Ulids.isUlid(value)) {
                errors.add(new FieldError("links." + field, "must be a ULID"));
                continue;
            }
            out.put(field, value);
        }
        return out;
    }

    // ------------------------------------------------------------------------- forecasts

    /**
     * Parses a {@code ForecastInput} for a question.
     *
     * @param body the body
     * @param question the question
     * @throws ValidationException 400 {@code FORECAST_INVALID}
     */
    public static ForecastSubmission forecast(JsonNode body, ForecastQuestionEntity question) {
        if (body == null || !body.isObject()) {
            throw invalidForecast("body", "must be a ForecastInput object");
        }
        for (String field : body.propertyNames()) {
            if (!FORECAST_FIELDS.contains(field)) {
                throw invalidForecast(field, "is not a ForecastInput member");
            }
        }
        Double probability = null;
        Map<String, Double> distribution = null;
        Map<String, Double> quantiles = null;
        if (body.hasNonNull("probability")) {
            if (!body.get("probability").isNumber()) {
                throw invalidForecast("probability", "must be a number");
            }
            probability = body.get("probability").asDouble();
        }
        if (body.hasNonNull("distribution")) {
            distribution = numbers(body.get("distribution"), "distribution");
        }
        if (body.hasNonNull("quantiles")) {
            quantiles = numbers(body.get("quantiles"), "quantiles");
        }
        ForecastValue value = new ForecastValue(probability, distribution, quantiles);
        validateValue(value, question.getType(), question.labels());
        String rationale = null;
        if (body.hasNonNull("rationale")) {
            if (!body.get("rationale").isString() || body.get("rationale").asString().length() > MAX_RATIONALE) {
                throw invalidForecast("rationale", "must be a string of at most " + MAX_RATIONALE + " characters");
            }
            rationale = body.get("rationale").asString();
        }
        List<Map<String, Object>> citations = new ArrayList<>();
        if (body.hasNonNull("citations")) {
            JsonNode list = body.get("citations");
            if (!list.isArray() || list.size() > MAX_CITATIONS) {
                throw invalidForecast("citations", "must be an array of at most " + MAX_CITATIONS + " citations");
            }
            for (JsonNode c : list) {
                if (!c.isObject() || !c.hasNonNull("ref") || !c.get("ref").isString()
                        || !OBJECT_REF.matcher(c.get("ref").asString()).matches()) {
                    throw invalidForecast("citations", "each citation needs a ref of the form {type}:{id}");
                }
                Map<String, Object> citation = new LinkedHashMap<>();
                citation.put("ref", c.get("ref").asString());
                citation.put("quote", c.hasNonNull("quote") && c.get("quote").isString() ? c.get("quote").asString()
                        : null);
                citation.put("claim", c.hasNonNull("claim") && c.get("claim").isString() ? c.get("claim").asString()
                        : null);
                citations.add(citation);
            }
        }
        String forecasterId = null;
        if (body.hasNonNull("forecasterId")) {
            if (!body.get("forecasterId").isString()
                    || !FORECASTER_ID.matcher(body.get("forecasterId").asString()).matches()) {
                throw invalidForecast("forecasterId", "must be a forecaster id");
            }
            forecasterId = body.get("forecasterId").asString();
        }
        return new ForecastSubmission(value, rationale, citations, forecasterId);
    }

    /**
     * Checks that a value fits a question: exactly one form, the one of the question's type; a probability in [0, 1];
     * a distribution over the question's labels summing to 1 ± 1e-6; quantiles with p05, p25, p50, p75 and p95,
     * finite and non-decreasing.
     *
     * @throws ValidationException 400 {@code FORECAST_INVALID}
     */
    public static void validateValue(ForecastValue value, String type, List<String> labels) {
        int forms = (value.probability() != null ? 1 : 0) + (value.distribution() != null ? 1 : 0)
                + (value.quantiles() != null ? 1 : 0);
        if (forms != 1) {
            throw invalidForecast("probability", "give exactly one of probability, distribution or quantiles");
        }
        switch (type) {
            case Ledger.BINARY -> {
                if (value.probability() == null) {
                    throw invalidForecast("probability", "a binary question takes a probability");
                }
                double p = value.probability();
                if (!Double.isFinite(p) || p < 0 || p > 1) {
                    throw invalidForecast("probability", "must be between 0 and 1");
                }
            }
            case Ledger.MULTIPLE, Ledger.ORDERED -> {
                if (value.distribution() == null) {
                    throw invalidForecast("distribution", "a " + type + " question takes a distribution");
                }
                double sum = 0;
                for (Map.Entry<String, Double> e : value.distribution().entrySet()) {
                    if (!labels.contains(e.getKey())) {
                        throw invalidForecast("distribution", "'" + e.getKey() + "' is not an outcome label");
                    }
                    double p = e.getValue();
                    if (!Double.isFinite(p) || p < 0 || p > 1) {
                        throw invalidForecast("distribution", "probabilities must be between 0 and 1");
                    }
                    sum += p;
                }
                if (Math.abs(sum - 1.0) > SUM_TOLERANCE) {
                    throw invalidForecast("distribution", "must sum to 1 (it sums to " + sum + ")");
                }
            }
            case Ledger.NUMERIC -> {
                if (value.quantiles() == null) {
                    throw invalidForecast("quantiles", "a numeric question takes quantiles");
                }
                for (String key : value.quantiles().keySet()) {
                    if (!Ledger.QUANTILES.contains(key)) {
                        throw invalidForecast("quantiles", "'" + key + "' is not one of " + Ledger.QUANTILES);
                    }
                }
                for (String key : Ledger.REQUIRED_QUANTILES) {
                    if (value.quantiles().get(key) == null) {
                        throw invalidForecast("quantiles", "needs " + Ledger.REQUIRED_QUANTILES);
                    }
                }
                double previous = Double.NEGATIVE_INFINITY;
                for (String key : Ledger.QUANTILES) {
                    Double q = value.quantiles().get(key);
                    if (q == null) {
                        continue;
                    }
                    if (!Double.isFinite(q)) {
                        throw invalidForecast("quantiles", key + " must be finite");
                    }
                    if (q < previous) {
                        throw invalidForecast("quantiles", "must be non-decreasing from p05 to p95");
                    }
                    previous = q;
                }
            }
            default -> throw invalidForecast("type", "unknown question type " + type);
        }
    }

    private static Map<String, Double> numbers(JsonNode node, String field) {
        if (!node.isObject()) {
            throw invalidForecast(field, "must be an object of numbers");
        }
        Map<String, Double> out = new LinkedHashMap<>();
        for (String key : node.propertyNames()) {
            JsonNode v = node.get(key);
            if (v == null || !v.isNumber()) {
                throw invalidForecast(field + "." + key, "must be a number");
            }
            out.put(key, v.asDouble());
        }
        return out;
    }

    // ------------------------------------------------------------------------- resolutions

    /**
     * Parses a {@code ForecastResolutionInput} for a question.
     *
     * @throws ValidationException 400 {@code FORECAST_INVALID} when the outcome does not fit, else
     *     {@code VALIDATION_FAILED}
     */
    public static ResolutionSubmission resolution(JsonNode body, ForecastQuestionEntity question) {
        requireObject(body, "ForecastResolutionInput");
        List<FieldError> errors = new ArrayList<>();
        unknown(body, Set.of("outcome", "value", "evidence", "note"), errors);
        List<String> evidence = new ArrayList<>();
        JsonNode list = body.get("evidence");
        if (list == null || !list.isArray() || list.isEmpty()) {
            errors.add(new FieldError("evidence", "at least one evidence ref is required"));
        } else {
            for (JsonNode e : list) {
                if (!e.isString() || !OBJECT_REF.matcher(e.asString()).matches()) {
                    errors.add(new FieldError("evidence", "each ref must have the form {type}:{id}"));
                } else {
                    evidence.add(e.asString());
                }
            }
        }
        String note = text(body, "note", 0, 2_000, false, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("The resolution is not valid.", errors);
        }
        String outcome = body.hasNonNull("outcome") && body.get("outcome").isString() ? body.get("outcome").asString()
                : null;
        Double value = body.hasNonNull("value") && body.get("value").isNumber() ? body.get("value").asDouble() : null;
        if (Ledger.NUMERIC.equals(question.getType())) {
            if (value == null || !Double.isFinite(value) || outcome != null) {
                throw invalidForecast("value", "a numeric question resolves with a finite value");
            }
        } else {
            List<String> allowed = Ledger.BINARY.equals(question.getType()) ? Ledger.YES_NO : question.labels();
            if (outcome == null || !allowed.contains(outcome) || value != null) {
                throw invalidForecast("outcome", "must be one of " + allowed);
            }
        }
        return new ResolutionSubmission(outcome, value, evidence, note);
    }

    // ------------------------------------------------------------------------- forecasters and evaluations

    /**
     * Parses a {@code ForecasterUpdate}.
     *
     * @return {@code active} and {@code config} (null when absent)
     */
    public static Map<String, Object> forecasterUpdate(JsonNode body) {
        requireObject(body, "ForecasterUpdate");
        List<FieldError> errors = new ArrayList<>();
        unknown(body, Set.of("active", "config", "name"), errors);
        if (!body.hasNonNull("active") || !body.get("active").isBoolean()) {
            errors.add(new FieldError("active", "is required and must be a boolean"));
        }
        if (body.has("config") && !body.get("config").isNull() && !body.get("config").isObject()) {
            errors.add(new FieldError("config", "must be an object"));
        }
        if (body.hasNonNull("name") && (!body.get("name").isString() || body.get("name").asString().isBlank()
                || body.get("name").asString().length() > 200)) {
            errors.add(new FieldError("name", "must be 1 to 200 characters"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The forecaster update is not valid.", errors);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("active", body.get("active").asBoolean());
        out.put("config", body.hasNonNull("config") ? JsonValues.map(body.get("config")) : null);
        // Prompt 155: an operator may rename an external forecaster (display name).
        out.put("name", body.hasNonNull("name") ? body.get("name").asString() : null);
        return out;
    }

    /**
     * Parses an {@code ExternalForecasterRegistration} (prompt 155): names and configures a first-class external
     * forecaster before any forecast arrives.
     *
     * @return {@code id}, {@code name} (null when absent) and {@code config} (null when absent)
     */
    public static Map<String, Object> externalForecasterRegistration(JsonNode body) {
        requireObject(body, "ExternalForecasterRegistration");
        List<FieldError> errors = new ArrayList<>();
        unknown(body, Set.of("id", "name", "config"), errors);
        String id = text(body, "id", 1, 128, true, errors);
        if (id != null && !FORECASTER_ID.matcher(id).matches()) {
            // The contract's pattern: one path segment, so updateForecaster can address it.
            errors.add(new FieldError("id", "must match " + FORECASTER_ID.pattern()));
        }
        if (body.hasNonNull("name") && (!body.get("name").isString() || body.get("name").asString().isBlank()
                || body.get("name").asString().length() > 200)) {
            errors.add(new FieldError("name", "must be 1 to 200 characters"));
        }
        if (body.has("config") && !body.get("config").isNull() && !body.get("config").isObject()) {
            errors.add(new FieldError("config", "must be an object"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The external forecaster registration is not valid.", errors);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", id);
        out.put("name", body.hasNonNull("name") ? body.get("name").asString() : null);
        out.put("config", body.hasNonNull("config") ? JsonValues.map(body.get("config")) : null);
        return out;
    }

    /** Parses a {@code ForecastEvaluationRequest}. */
    public static EvaluationRequest evaluation(JsonNode body) {
        requireObject(body, "ForecastEvaluationRequest");
        List<FieldError> errors = new ArrayList<>();
        unknown(body, Set.of("forecasterId", "from", "to", "categories", "maxQuestions"), errors);
        String forecasterId = text(body, "forecasterId", 1, 128, true, errors);
        Instant from = instant(body, "from", true, errors);
        Instant to = instant(body, "to", true, errors);
        if (from != null && to != null && !to.isAfter(from)) {
            errors.add(new FieldError("to", "must be after from"));
        }
        List<String> categories = new ArrayList<>();
        if (body.hasNonNull("categories")) {
            if (!body.get("categories").isArray()) {
                errors.add(new FieldError("categories", "must be an array of strings"));
            } else {
                for (JsonNode c : body.get("categories")) {
                    if (c.isString() && !c.asString().isBlank()) {
                        categories.add(c.asString());
                    } else {
                        errors.add(new FieldError("categories", "categories are non-blank strings"));
                    }
                }
            }
        }
        Integer maxQuestions = null;
        if (body.hasNonNull("maxQuestions")) {
            JsonNode m = body.get("maxQuestions");
            if (!m.isIntegralNumber() || m.asInt() < 1 || m.asInt() > 10_000) {
                errors.add(new FieldError("maxQuestions", "must be an integer from 1 to 10000"));
            } else {
                maxQuestions = m.asInt();
            }
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The evaluation request is not valid.", errors);
        }
        return new EvaluationRequest(forecasterId, from, to, categories, maxQuestions);
    }

    // ------------------------------------------------------------------------- helpers

    /** A {@code FORECAST_INVALID} problem. */
    public static ValidationException invalidForecast(String field, String message) {
        return new ValidationException(ErrorCodes.FORECAST_INVALID, "The forecast is not valid: " + field + " "
                + message + ".", List.of(new FieldError(field, message)));
    }

    private static void requireObject(JsonNode body, String schema) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a " + schema + " object.");
        }
    }

    private static void unknown(JsonNode body, Set<String> allowed, List<FieldError> errors) {
        for (String field : body.propertyNames()) {
            if (!allowed.contains(field)) {
                errors.add(new FieldError(field, "is not a member of this schema"));
            }
        }
    }

    private static String text(JsonNode body, String field, int min, int max, boolean required,
            List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            if (required) {
                errors.add(new FieldError(field, "is required"));
            }
            return null;
        }
        if (!node.isString()) {
            errors.add(new FieldError(field, "must be a string"));
            return null;
        }
        String value = node.asString();
        if (value.strip().length() < min || value.length() > max) {
            errors.add(new FieldError(field, "must be " + min + " to " + max + " characters"));
            return null;
        }
        return value;
    }

    private static Instant instant(JsonNode body, String field, boolean required, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            if (required) {
                errors.add(new FieldError(field, "is required"));
            }
            return null;
        }
        try {
            return JsonValues.instant(node.asString());
        } catch (RuntimeException e) {
            errors.add(new FieldError(field, "must be an ISO-8601 date-time"));
            return null;
        }
    }
}
