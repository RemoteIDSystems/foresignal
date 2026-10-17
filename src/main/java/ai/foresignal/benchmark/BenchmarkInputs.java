package ai.foresignal.benchmark;

import ai.foresignal.api.error.ValidationException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses and validates the benchmark request bodies (prompt 123): {@code BenchmarkRunInput} and
 * {@code BenchmarkCaseInput}. Pure and static, so the Postman contract test runs the same checks as the API; every
 * failure is a 400 {@code VALIDATION_FAILED}.
 */
public final class BenchmarkInputs {

    /** The suites. */
    public static final List<String> SUITES = List.of("synthetic", "historical", "all");

    /** The case sources. */
    public static final List<String> SOURCES = List.of("operator", "agent", "auto");

    /** The benchmarked components (each a {@code ComponentBenchmark}). */
    public static final List<String> COMPONENTS = List.of("change-point", "anomaly", "event-study", "co-occurrence",
            "evidence", "canonical-linking", "precursor", "situation-linker", "forecaster:base-rate",
            "forecaster:analog", "forecaster:precursor", "forecaster:ensemble");

    /** A 040 region code. */
    public static final Pattern REGION = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");

    private static final Set<String> RUN_FIELDS = Set.of("suite", "components", "seed");
    private static final Set<String> CASE_FIELDS = Set.of("name", "description", "windowFrom", "windowTo",
            "regionCodes", "expectations");
    private static final Set<String> SEVERITIES = Set.of("info", "notable", "significant");
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private BenchmarkInputs() {}

    /**
     * A validated run request.
     *
     * @param suite the suite
     * @param components the components (every built component when the request named none)
     * @param seed the seed, or null for the suite's fixed seed
     */
    public record RunRequest(String suite, List<String> components, Long seed) {}

    /**
     * Parses a {@code BenchmarkRunInput}.
     *
     * @throws ValidationException 400 for an unknown suite, component or member
     */
    public static RunRequest run(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a BenchmarkRunInput object.");
        }
        for (String field : body.propertyNames()) {
            if (!RUN_FIELDS.contains(field)) {
                throw ValidationException.forField(field, "is not a BenchmarkRunInput member");
            }
        }
        if (!body.hasNonNull("suite") || !body.get("suite").isString()
                || !SUITES.contains(body.get("suite").asString())) {
            throw ValidationException.forField("suite", "must be one of " + SUITES);
        }
        List<String> components = new ArrayList<>(COMPONENTS);
        if (body.hasNonNull("components")) {
            JsonNode list = body.get("components");
            if (!list.isArray() || list.isEmpty()) {
                throw ValidationException.forField("components", "must be a non-empty array of component names");
            }
            Set<String> chosen = new LinkedHashSet<>();
            for (JsonNode c : list) {
                if (!c.isString() || !COMPONENTS.contains(c.asString())) {
                    throw ValidationException.forField("components", "'" + (c.isString() ? c.asString() : c)
                            + "' is not a benchmarked component; one of " + COMPONENTS);
                }
                chosen.add(c.asString());
            }
            components = new ArrayList<>(chosen);
        }
        Long seed = null;
        if (body.hasNonNull("seed")) {
            if (!body.get("seed").isIntegralNumber() || !body.get("seed").canConvertToLong()) {
                throw ValidationException.forField("seed", "must be a 64-bit integer");
            }
            seed = body.get("seed").asLong();
        }
        return new RunRequest(body.get("suite").asString(), components, seed);
    }

    /**
     * A validated case.
     *
     * @param name the name
     * @param description the description, or null
     * @param windowFrom the window start
     * @param windowTo the window end
     * @param regionCodes the region codes
     * @param expectations the expectations
     */
    public record CaseInput(String name, String description, Instant windowFrom, Instant windowTo,
            List<String> regionCodes, Map<String, Object> expectations) {}

    /**
     * Parses a {@code BenchmarkCaseInput}.
     *
     * @throws ValidationException 400 for a missing or invalid member
     */
    public static CaseInput caseInput(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a BenchmarkCaseInput object.");
        }
        for (String field : body.propertyNames()) {
            if (!CASE_FIELDS.contains(field)) {
                throw ValidationException.forField(field, "is not a BenchmarkCaseInput member");
            }
        }
        String name = text(body, "name", true);
        if (name.isBlank() || name.length() > 200) {
            throw ValidationException.forField("name", "must be 1 to 200 characters");
        }
        String description = text(body, "description", false);
        if (description != null && description.length() > 4000) {
            throw ValidationException.forField("description", "must be at most 4000 characters");
        }
        Instant from = instant(body, "windowFrom");
        Instant to = instant(body, "windowTo");
        if (!from.isBefore(to)) {
            throw ValidationException.forField("windowTo", "must be after windowFrom");
        }
        List<String> regions = new ArrayList<>();
        if (body.hasNonNull("regionCodes")) {
            if (!body.get("regionCodes").isArray()) {
                throw ValidationException.forField("regionCodes", "must be an array of 040 region codes");
            }
            for (JsonNode r : body.get("regionCodes")) {
                if (!r.isString() || !REGION.matcher(r.asString()).matches()) {
                    throw ValidationException.forField("regionCodes", "'" + r + "' is not a 040 region code");
                }
                regions.add(r.asString());
            }
        }
        if (!body.hasNonNull("expectations") || !body.get("expectations").isObject()) {
            throw ValidationException.forField("expectations", "must be an object");
        }
        JsonNode e = body.get("expectations");
        checkFindings(e.get("findings"));
        checkPrecursors(e.get("precursors"));
        checkQuestions(e.get("questions"));
        if (e.hasNonNull("notes") && !e.get("notes").isString()) {
            throw ValidationException.forField("expectations.notes", "must be a string");
        }
        Map<String, Object> expectations = JSON.convertValue(e, MAP);
        return new CaseInput(name, description, from, to, List.copyOf(regions), expectations);
    }

    private static void checkFindings(JsonNode findings) {
        if (findings == null || findings.isNull()) {
            return;
        }
        if (!findings.isArray()) {
            throw ValidationException.forField("expectations.findings", "must be an array");
        }
        for (JsonNode f : findings) {
            if (!f.isObject() || !f.hasNonNull("findingType") || !f.get("findingType").isString()
                    || f.get("findingType").asString().isBlank()) {
                throw ValidationException.forField("expectations.findings", "each needs a findingType");
            }
            if (!f.hasNonNull("present") || !f.get("present").isBoolean()) {
                throw ValidationException.forField("expectations.findings", "each needs present (a boolean)");
            }
            if (f.hasNonNull("seriesRef") && (!f.get("seriesRef").isString()
                    || !f.get("seriesRef").asString().matches("^(series|rollup|rule|social):.+$"))) {
                throw ValidationException.forField("expectations.findings", "seriesRef must be a series reference");
            }
            if (f.hasNonNull("minSeverity") && !SEVERITIES.contains(f.get("minSeverity").asString())) {
                throw ValidationException.forField("expectations.findings", "minSeverity must be one of "
                        + SEVERITIES);
            }
            for (String t : List.of("from", "to")) {
                if (f.hasNonNull(t)) {
                    instant(f, t);
                }
            }
            if (f.hasNonNull("regionCodes")) {
                for (JsonNode r : f.get("regionCodes")) {
                    if (!r.isString() || !REGION.matcher(r.asString()).matches()) {
                        throw ValidationException.forField("expectations.findings", "'" + r
                                + "' is not a 040 region code");
                    }
                }
            }
        }
    }

    private static void checkPrecursors(JsonNode precursors) {
        if (precursors == null || precursors.isNull()) {
            return;
        }
        if (!precursors.isArray()) {
            throw ValidationException.forField("expectations.precursors", "must be an array");
        }
        for (JsonNode p : precursors) {
            if (!p.isObject() || !p.hasNonNull("expected")
                    || !List.of("validated", "not-validated").contains(p.get("expected").asString())) {
                throw ValidationException.forField("expectations.precursors",
                        "each needs expected: validated or not-validated");
            }
        }
    }

    private static void checkQuestions(JsonNode questions) {
        if (questions == null || questions.isNull()) {
            return;
        }
        if (!questions.isArray()) {
            throw ValidationException.forField("expectations.questions", "must be an array");
        }
        for (JsonNode q : questions) {
            if (!q.isObject() || !q.hasNonNull("text") || !q.get("text").isString()) {
                throw ValidationException.forField("expectations.questions", "each needs text");
            }
            if (!q.hasNonNull("type") || !List.of("binary", "numeric").contains(q.get("type").asString())) {
                throw ValidationException.forField("expectations.questions", "type must be binary or numeric");
            }
            if (!q.has("outcome") || q.get("outcome").isNull()) {
                throw ValidationException.forField("expectations.questions", "each needs an outcome");
            }
            if (q.hasNonNull("resolvedAt")) {
                instant(q, "resolvedAt");
            }
        }
    }

    private static String text(JsonNode body, String field, boolean required) {
        if (!body.hasNonNull(field)) {
            if (required) {
                throw ValidationException.forField(field, "is required");
            }
            return null;
        }
        if (!body.get(field).isString()) {
            throw ValidationException.forField(field, "must be a string");
        }
        return body.get(field).asString();
    }

    private static Instant instant(JsonNode body, String field) {
        String value = text(body, field, true);
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw ValidationException.forField(field, "must be an RFC 3339 date-time");
        }
    }
}
