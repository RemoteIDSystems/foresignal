package ai.foresignal.ask;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.BboxParser;
import ai.foresignal.source.SourceRegistry;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * Reads an {@code AskRequest} body, reporting every problem at once ({@code 400 VALIDATION_FAILED}
 * with field errors).
 *
 * <p>Unknown members are refused, so a misspelled {@code allowActions} cannot silently leave actions
 * pending. A scope source must be registered and visible to the caller; a private source the caller
 * may not see is reported exactly like an unknown one.
 */
public final class AskRequestParser {

    static final Set<String> MEMBERS = Set.of("question", "threadId", "scope", "allowActions");

    static final Set<String> SCOPE_MEMBERS = Set.of("sources", "kinds", "bbox", "since", "until", "placeIds", "viewId");

    /** The most values of a scope list. */
    static final int MAX_LIST = 50;

    private AskRequestParser() {}

    /**
     * Parses {@code body}.
     *
     * @param body the request body
     * @param registry the registered sources
     * @param excludedSources the sources the caller may not see
     * @throws ValidationException listing every member error
     */
    public static AskInput parse(JsonNode body, SourceRegistry registry, Set<String> excludedSources) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be a JSON object.");
        }
        List<FieldError> errors = new ArrayList<>();
        body.propertyNames().forEach(name -> {
            if (!MEMBERS.contains(name)) {
                errors.add(new FieldError(name, "is not a member of AskRequest"));
            }
        });

        String question = null;
        JsonNode q = body.get("question");
        if (q == null || q.isNull()) {
            errors.add(new FieldError("question", "is required"));
        } else if (!q.isString() || q.asString().isBlank()) {
            errors.add(new FieldError("question", "must be a non-blank string"));
        } else if (q.asString().length() > AskInput.MAX_QUESTION) {
            errors.add(new FieldError("question", "must be at most " + AskInput.MAX_QUESTION + " characters"));
        } else {
            question = q.asString().strip();
        }

        UUID threadId = null;
        JsonNode t = body.get("threadId");
        if (t != null && !t.isNull()) {
            try {
                threadId = UUID.fromString(t.isString() ? t.asString() : "");
            } catch (IllegalArgumentException e) {
                errors.add(new FieldError("threadId", "must be a UUID"));
            }
        }

        boolean allowActions = false;
        JsonNode a = body.get("allowActions");
        if (a != null && !a.isNull()) {
            if (!a.isBoolean()) {
                errors.add(new FieldError("allowActions", "must be a boolean"));
            } else {
                allowActions = a.asBoolean();
            }
        }

        AskScope scope = scope(body.get("scope"), registry, excludedSources, errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("The question is not valid.", errors);
        }
        return new AskInput(question, threadId, scope, allowActions);
    }

    private static AskScope scope(JsonNode node, SourceRegistry registry, Set<String> excluded, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return AskScope.NONE;
        }
        if (!node.isObject()) {
            errors.add(new FieldError("scope", "must be an object"));
            return AskScope.NONE;
        }
        node.propertyNames().forEach(name -> {
            if (!SCOPE_MEMBERS.contains(name)) {
                errors.add(new FieldError("scope." + name, "is not a member of AskScope"));
            }
        });
        List<String> sources = strings(node.get("sources"), "scope.sources", errors);
        for (String source : sources) {
            if (!registry.contains(source) || excluded.contains(source)) {
                errors.add(new FieldError("scope.sources", "unknown source '" + source + "'"));
            }
        }
        List<String> kinds = strings(node.get("kinds"), "scope.kinds", errors);
        String bbox = null;
        JsonNode b = node.get("bbox");
        if (b != null && !b.isNull()) {
            if (!b.isString()) {
                errors.add(new FieldError("scope.bbox", "must be minLon,minLat,maxLon,maxLat"));
            } else {
                try {
                    BboxParser.parse(b.asString());
                    bbox = b.asString();
                } catch (ValidationException e) {
                    errors.add(new FieldError("scope.bbox", e.getMessage()));
                }
            }
        }
        Instant since = instant(node.get("since"), "scope.since", errors);
        Instant until = instant(node.get("until"), "scope.until", errors);
        if (since != null && until != null && !since.isBefore(until)) {
            errors.add(new FieldError("scope.until", "must be after since"));
        }
        List<String> placeIds = strings(node.get("placeIds"), "scope.placeIds", errors);
        String viewId = null;
        JsonNode v = node.get("viewId");
        if (v != null && !v.isNull()) {
            if (!v.isString() || v.asString().isBlank()) {
                errors.add(new FieldError("scope.viewId", "must be a non-blank string"));
            } else {
                viewId = v.asString();
            }
        }
        return new AskScope(sources, kinds, bbox, since, until, placeIds, viewId);
    }

    private static List<String> strings(JsonNode node, String field, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            errors.add(new FieldError(field, "must be an array of strings"));
            return List.of();
        }
        if (node.size() > MAX_LIST) {
            errors.add(new FieldError(field, "must have at most " + MAX_LIST + " values"));
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (JsonNode value : node) {
            if (!value.isString() || value.asString().isBlank()) {
                errors.add(new FieldError(field, "must be an array of non-blank strings"));
                return List.of();
            }
            if (!out.contains(value.asString())) {
                out.add(value.asString());
            }
        }
        return out;
    }

    private static Instant instant(JsonNode node, String field, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString()) {
            errors.add(new FieldError(field, "must be an RFC 3339 date-time"));
            return null;
        }
        try {
            return OffsetDateTime.parse(node.asString()).toInstant();
        } catch (DateTimeParseException e) {
            errors.add(new FieldError(field, "must be an RFC 3339 date-time"));
            return null;
        }
    }
}
