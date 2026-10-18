package ai.foresignal.cycle;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.briefing.BriefingValidator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/** Parses and validates {@code AnalysisCycleInput} and {@code SituationAssessmentRequest} bodies (prompt 128). */
public final class CycleInputs {

    /** The longest name. */
    public static final int MAX_NAME = 200;

    /** The most entries of any scope list. */
    public static final int MAX_LIST = 200;

    private static final Pattern REGION = Pattern.compile("^[A-Z]{2}(-[A-Z0-9]{1,3})?$");

    private static final Set<String> FIELDS = Set.of("name", "cadence", "schedule", "scope", "analystProfileId",
            "roles", "forecasterProviders", "samples", "maxQuestions", "maxTokensPerCycle", "channelIds",
            "notifyMinPriority", "enabled");

    private static final Set<String> SCOPE_FIELDS = Set.of("regionCodes", "themes", "situationIds",
            "hypothesisSetIds", "sourceIds", "categories");

    private CycleInputs() {}

    /**
     * A validated cycle definition.
     *
     * @param name the name
     * @param cadence daily, weekly or monthly
     * @param cron the 5-field cron
     * @param timezone the IANA timezone
     * @param scope the scope
     * @param analystProfileId the 102 profile, or null
     * @param roles the LLM roles
     * @param forecasterProviders the forecaster providers
     * @param samples forecaster samples per question
     * @param maxQuestions the question cap
     * @param maxTokensPerCycle the token cap
     * @param channelIds the delivery channels
     * @param notifyMinPriority the lowest priority delivered to channels
     * @param enabled whether the cycle is scheduled
     */
    public record CycleDraft(String name, String cadence, String cron, String timezone, CycleScope scope,
            String analystProfileId, List<String> roles, List<String> forecasterProviders, int samples,
            int maxQuestions, long maxTokensPerCycle, List<String> channelIds, String notifyMinPriority,
            boolean enabled) {}

    /**
     * An on-demand request.
     *
     * @param roles the roles (default analyst and red-team)
     * @param effort the effort, or null
     */
    public record SituationRequest(List<String> roles, String effort) {}

    /**
     * Parses a cycle body.
     *
     * @throws ValidationException 400 listing every field error
     */
    public static CycleDraft cycle(JsonNode body) {
        List<FieldError> errors = new ArrayList<>();
        if (body == null || !body.isObject()) {
            throw new ValidationException("The body must be an AnalysisCycleInput object.",
                    List.of(new FieldError("body", "must be an object")));
        }
        for (String field : body.propertyNames()) {
            if (!FIELDS.contains(field)) {
                errors.add(new FieldError(field, "is not an AnalysisCycleInput field"));
            }
        }
        String name = text(body, "name");
        if (name == null || name.isBlank() || name.length() > MAX_NAME) {
            errors.add(new FieldError("name", "is required, 1 to " + MAX_NAME + " characters"));
        }
        String cadence = text(body, "cadence");
        if (cadence == null || !CycleVocabulary.CADENCES.contains(cadence)) {
            errors.add(new FieldError("cadence", "must be daily, weekly or monthly"));
            cadence = CycleVocabulary.DAILY;
        }
        String cron = null;
        String timezone = "UTC";
        JsonNode schedule = body.get("schedule");
        if (schedule == null || !schedule.isObject()) {
            errors.add(new FieldError("schedule", "is required: {cron, timezone}"));
        } else {
            cron = text(schedule, "cron");
            timezone = text(schedule, "timezone");
            try {
                BriefingValidator.cron(cron);
            } catch (IllegalArgumentException e) {
                errors.add(new FieldError("schedule.cron", e.getMessage()));
            }
            if (timezone == null || timezone.isBlank()) {
                errors.add(new FieldError("schedule.timezone", "is required (an IANA timezone)"));
            } else {
                try {
                    BriefingValidator.zone(timezone);
                } catch (IllegalArgumentException e) {
                    errors.add(new FieldError("schedule.timezone", e.getMessage()));
                }
            }
        }
        CycleScope scope = scope(body.get("scope"), errors);
        String profile = text(body, "analystProfileId");
        if (profile != null && (profile.isBlank() || profile.length() > 26)) {
            errors.add(new FieldError("analystProfileId", "must be an analyst profile id"));
        }
        List<String> roles = roles(body.get("roles"), "roles", errors, List.of());
        List<String> providers = stringList(body.get("forecasterProviders"), "forecasterProviders", errors, 64);
        int samples = integer(body, "samples", CycleVocabulary.DEFAULT_SAMPLES, 1, CycleVocabulary.MAX_SAMPLES, errors);
        int maxQuestions = integer(body, "maxQuestions", CycleVocabulary.defaultMaxQuestions(cadence), 0, 1000, errors);
        long maxTokens = longValue(body, "maxTokensPerCycle", CycleVocabulary.defaultMaxTokens(cadence), errors);
        List<String> channels = stringList(body.get("channelIds"), "channelIds", errors, 64);
        String priority = text(body, "notifyMinPriority");
        if (priority == null) {
            priority = CycleVocabulary.priority(cadence);
        } else if (!CycleVocabulary.PRIORITIES.contains(priority)) {
            errors.add(new FieldError("notifyMinPriority", "must be low, normal or high"));
        }
        JsonNode enabledNode = body.get("enabled");
        if (enabledNode != null && !enabledNode.isNull() && !enabledNode.isBoolean()) {
            errors.add(new FieldError("enabled", "must be a boolean"));
        }
        boolean enabled = enabledNode == null || enabledNode.isNull()
                || (enabledNode.isBoolean() && enabledNode.asBoolean());
        if (!errors.isEmpty()) {
            throw new ValidationException("The analysis cycle is not valid.", errors);
        }
        return new CycleDraft(name.strip(), cadence, cron.strip(), timezone.strip(), scope, profile, roles, providers,
                samples, maxQuestions, maxTokens, channels, priority, enabled);
    }

    /**
     * Parses an on-demand request (an absent body is the defaults).
     *
     * @throws ValidationException 400
     */
    public static SituationRequest situation(JsonNode body) {
        List<FieldError> errors = new ArrayList<>();
        if (body == null || body.isNull() || body.isMissingNode()) {
            return new SituationRequest(List.of(CycleVocabulary.ANALYST, CycleVocabulary.RED_TEAM), null);
        }
        if (!body.isObject()) {
            throw ValidationException.forField("body", "must be a SituationAssessmentRequest object");
        }
        for (String field : body.propertyNames()) {
            if (!Set.of("roles", "effort").contains(field)) {
                errors.add(new FieldError(field, "is not a SituationAssessmentRequest field"));
            }
        }
        List<String> roles = roles(body.get("roles"), "roles", errors,
                List.of(CycleVocabulary.ANALYST, CycleVocabulary.RED_TEAM));
        String effort = text(body, "effort");
        if (effort != null && !List.of("low", "medium", "high").contains(effort)) {
            errors.add(new FieldError("effort", "must be low, medium or high"));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException("The situation assessment request is not valid.", errors);
        }
        return new SituationRequest(roles, effort);
    }

    private static CycleScope scope(JsonNode node, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return CycleScope.ALL;
        }
        if (!node.isObject()) {
            errors.add(new FieldError("scope", "must be a CycleScope object"));
            return CycleScope.ALL;
        }
        for (String field : node.propertyNames()) {
            if (!SCOPE_FIELDS.contains(field)) {
                errors.add(new FieldError("scope." + field, "is not a CycleScope field"));
            }
        }
        List<String> regions = stringList(node.get("regionCodes"), "scope.regionCodes", errors, 16);
        for (String region : regions) {
            if (!REGION.matcher(region).matches()) {
                errors.add(new FieldError("scope.regionCodes", "'" + region + "' is not an ISO 3166 code"));
            }
        }
        JsonNode sets = node.get("hypothesisSetIds");
        boolean match = sets != null && sets.isString() && CycleScope.CADENCE_MATCH.equals(sets.asString());
        List<String> setIds = List.of();
        if (sets != null && !sets.isNull() && !match) {
            if (sets.isString()) {
                errors.add(new FieldError("scope.hypothesisSetIds", "must be a list of set ids or 'cadence-match'"));
            } else {
                setIds = stringList(sets, "scope.hypothesisSetIds", errors, 26);
            }
        }
        return new CycleScope(regions, stringList(node.get("themes"), "scope.themes", errors, 64),
                stringList(node.get("situationIds"), "scope.situationIds", errors, 26), setIds, match,
                stringList(node.get("sourceIds"), "scope.sourceIds", errors, 64),
                stringList(node.get("categories"), "scope.categories", errors, 64), false);
    }

    private static List<String> roles(JsonNode node, String field, List<FieldError> errors, List<String> fallback) {
        if (node == null || node.isNull()) {
            return fallback;
        }
        List<String> raw = stringList(node, field, errors, 32);
        Set<String> seen = new LinkedHashSet<>();
        for (String role : raw) {
            if (!CycleVocabulary.ROLES.contains(role)) {
                errors.add(new FieldError(field, "'" + role + "' is not analyst, red-team, forecaster or proposer"));
            } else if (!seen.add(role)) {
                errors.add(new FieldError(field, "'" + role + "' is listed twice"));
            }
        }
        List<String> ordered = new ArrayList<>();
        for (String role : CycleVocabulary.ROLES) {
            if (seen.contains(role)) {
                ordered.add(role);
            }
        }
        return ordered;
    }

    private static List<String> stringList(JsonNode node, String field, List<FieldError> errors, int maxLength) {
        List<String> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isArray()) {
            errors.add(new FieldError(field, "must be an array of strings"));
            return out;
        }
        if (node.size() > MAX_LIST) {
            errors.add(new FieldError(field, "has more than " + MAX_LIST + " entries"));
        }
        for (JsonNode item : node) {
            if (!item.isString() || item.asString().isBlank() || item.asString().length() > maxLength) {
                errors.add(new FieldError(field, "entries must be strings of 1 to " + maxLength + " characters"));
            } else if (!out.contains(item.asString().strip())) {
                out.add(item.asString().strip());
            }
        }
        return out;
    }

    private static int integer(JsonNode body, String field, int fallback, int min, int max, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return fallback;
        }
        if (!node.isIntegralNumber() || node.asLong() < min || node.asLong() > max) {
            errors.add(new FieldError(field, "must be an integer from " + min + " to " + max));
            return fallback;
        }
        return node.asInt();
    }

    private static long longValue(JsonNode body, String field, long fallback, List<FieldError> errors) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            return fallback;
        }
        if (!node.isIntegralNumber() || node.asLong() < 0) {
            errors.add(new FieldError(field, "must be a non-negative integer"));
            return fallback;
        }
        return node.asLong();
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() || !value.isString() ? null : value.asString();
    }

    /** The schedule map of a draft. */
    public static Map<String, Object> schedule(CycleDraft draft) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("cron", draft.cron());
        out.put("timezone", draft.timezone());
        return out;
    }
}
