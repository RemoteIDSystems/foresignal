package ai.foresignal.llm;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.llm.client.Effort;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * Parses and validates {@code LlmProviderInput} bodies (OpenAPI), applying the provider defaults:
 * {@code anthropic} gets {@code https://api.anthropic.com}, chat model {@code claude-opus-5-5},
 * effort {@code medium} and {@code maxTokens} 16000; {@code ollama} gets {@code http://ollama:11434};
 * {@code openai-compatible} must name its {@code baseUrl}.
 *
 * <p>{@code baseUrl} is checked here only for shape: http or https, a host, no user info, no
 * fragment. Private-network URLs stay allowed, because a local Ollama is the point. Prompt 035 adds
 * the outbound URL policy (SSRF guard) at the seam marked below and in {@link LlmHttp}.
 */
@Component
public class LlmProviderValidator {

    /** The id pattern of the spec. */
    static final Pattern ID = Pattern.compile("^[a-z0-9-]+$");

    /** The default Anthropic chat model. */
    public static final String DEFAULT_ANTHROPIC_MODEL = "claude-opus-5-5";

    /** The default Anthropic effort. */
    public static final String DEFAULT_ANTHROPIC_EFFORT = "medium";

    /** The default Anthropic output cap. */
    public static final int DEFAULT_ANTHROPIC_MAX_TOKENS = 16_000;

    static final int DEFAULT_REQUESTS_PER_MINUTE = 30;
    static final long DEFAULT_TOKENS_PER_DAY = 2_000_000L;
    static final long MIN_TOKENS_PER_DAY = 1000L;

    private static final Set<String> MEMBERS = Set.of("id", "type", "baseUrl", "chatModel", "embeddingModel",
            "embeddingDimensions", "apiKey", "options", "requestsPerMinute", "tokensPerDay", "enabled",
            "knowledgeCutoff");

    /** Output-only members of {@code LlmProvider}, tolerated (and ignored) so an exported provider imports. */
    private static final Set<String> OUTPUT_ONLY = Set.of("apiKeySet", "roles", "status", "tokensToday", "lastError",
            "updatedAt", "supportsTools", "lastSuccessAt", "lastErrorAt", "consecutiveFailures", "local");

    private static final Set<String> OPTION_MEMBERS = Set.of("effort", "maxTokens", "temperature", "extraHeaders",
            "local");

    /** Headers an operator may not set: Foresignal sets them, or the HTTP client forbids them. */
    private static final Set<String> RESERVED_HEADERS = Set.of("authorization", "x-api-key", "content-type",
            "content-length", "host", "connection", "expect", "upgrade", "anthropic-version", "anthropic-beta");

    /**
     * Parses a create ({@code pathId} null) or replace body.
     *
     * @param body the request body
     * @param pathId the provider id from the path on replace; the body's {@code id}, when present,
     *     must match it
     * @throws ValidationException listing every error
     */
    public LlmProviderInput parse(JsonNode body, String pathId) {
        if (body == null || !body.isObject()) {
            throw new ValidationException("The request body must be an LlmProviderInput object.");
        }
        List<FieldError> errors = new ArrayList<>();
        for (String name : body.propertyNames()) {
            if (!MEMBERS.contains(name) && !OUTPUT_ONLY.contains(name)) {
                errors.add(new FieldError(name, "is not a member of LlmProviderInput"));
            }
        }

        String id = pathId;
        JsonNode idNode = body.get("id");
        if (pathId == null) {
            if (idNode == null || !idNode.isString() || !ID.matcher(idNode.asString()).matches()
                    || idNode.asString().length() > 64) {
                errors.add(new FieldError("id", "is required: lower-case letters, digits and hyphens, at most 64"));
            } else {
                id = idNode.asString();
            }
        } else if (idNode != null && !idNode.isNull() && !(idNode.isString() && pathId.equals(idNode.asString()))) {
            errors.add(new FieldError("id", "must match the provider id in the path ('" + pathId + "')"));
        }

        LlmProviderType type = null;
        JsonNode typeNode = body.get("type");
        if (typeNode == null || !typeNode.isString() || LlmProviderType.fromWire(typeNode.asString()) == null) {
            errors.add(new FieldError("type", "is required: anthropic, openai-compatible or ollama"));
        } else {
            type = LlmProviderType.fromWire(typeNode.asString());
        }

        String baseUrl = optionalString(body, "baseUrl", errors);
        if (baseUrl == null && type != null) {
            baseUrl = type.defaultBaseUrl();
            if (baseUrl == null) {
                errors.add(new FieldError("baseUrl", "is required for openai-compatible providers"));
            }
        }
        if (baseUrl != null) {
            validateBaseUrl(baseUrl, errors);
            baseUrl = stripTrailingSlash(baseUrl);
        }

        String chatModel = optionalString(body, "chatModel", errors);
        if (chatModel == null && type == LlmProviderType.ANTHROPIC) {
            chatModel = DEFAULT_ANTHROPIC_MODEL;
        }
        String embeddingModel = optionalString(body, "embeddingModel", errors);
        Integer embeddingDimensions = optionalInt(body, "embeddingDimensions", 8, 4096, errors);
        if (type == LlmProviderType.ANTHROPIC && (embeddingModel != null || embeddingDimensions != null)) {
            errors.add(new FieldError("embeddingModel",
                    "Anthropic has no embeddings endpoint; configure embeddings on an openai-compatible or ollama provider"));
        }

        String apiKey = null;
        JsonNode keyNode = body.get("apiKey");
        if (keyNode != null && !keyNode.isNull()) {
            if (!keyNode.isString() || keyNode.asString().isBlank()) {
                errors.add(new FieldError("apiKey", "must be a non-empty string; omit it to keep the stored key"));
            } else {
                apiKey = keyNode.asString();
            }
        }

        Map<String, Object> options = options(body.get("options"), type, errors);
        Integer rpm = optionalInt(body, "requestsPerMinute", 1, Integer.MAX_VALUE, errors);
        Long tpd = optionalLong(body, "tokensPerDay", MIN_TOKENS_PER_DAY, errors);
        boolean enabled = true;
        JsonNode enabledNode = body.get("enabled");
        if (enabledNode != null && !enabledNode.isNull()) {
            if (!enabledNode.isBoolean()) {
                errors.add(new FieldError("enabled", "must be a boolean"));
            } else {
                enabled = enabledNode.asBoolean();
            }
        }
        LocalDate cutoff = null;
        String cutoffText = optionalString(body, "knowledgeCutoff", errors);
        if (cutoffText != null) {
            try {
                cutoff = LocalDate.parse(cutoffText);
            } catch (DateTimeParseException e) {
                errors.add(new FieldError("knowledgeCutoff", "must be an ISO date (YYYY-MM-DD)"));
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("The LLM provider is not valid.", errors);
        }
        return new LlmProviderInput(id, type, baseUrl, chatModel, embeddingModel, embeddingDimensions, apiKey, options,
                rpm == null ? DEFAULT_REQUESTS_PER_MINUTE : rpm, tpd == null ? DEFAULT_TOKENS_PER_DAY : tpd, enabled,
                cutoff);
    }

    /**
     * Checks {@code baseUrl}'s shape: absolute http or https with a host, no user info, no fragment.
     * Private and loopback addresses are allowed.
     */
    public static void validateBaseUrl(String baseUrl, List<FieldError> errors) {
        URI uri;
        try {
            uri = new URI(baseUrl);
        } catch (URISyntaxException e) {
            errors.add(new FieldError("baseUrl", "is not a valid URI"));
            return;
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) {
            errors.add(new FieldError("baseUrl", "must use http or https"));
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            errors.add(new FieldError("baseUrl", "must name a host"));
        }
        if (uri.getRawUserInfo() != null) {
            errors.add(new FieldError("baseUrl", "must not carry user info; put the key in apiKey"));
        }
        if (uri.getRawFragment() != null) {
            errors.add(new FieldError("baseUrl", "must not carry a fragment"));
        }
        // 035: OutboundUrlPolicy — the outbound URL policy (LAN_ALLOWED) is enforced here and in LlmHttp.
    }

    private static Map<String, Object> options(JsonNode node, LlmProviderType type, List<FieldError> errors) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (node != null && !node.isNull()) {
            if (!node.isObject()) {
                errors.add(new FieldError("options", "must be an object"));
                return out;
            }
            for (String name : node.propertyNames()) {
                if (!OPTION_MEMBERS.contains(name)) {
                    errors.add(new FieldError("options." + name,
                            "is not an option (effort, maxTokens, temperature, extraHeaders, local)"));
                }
            }
            JsonNode effort = node.get("effort");
            if (effort != null && !effort.isNull()) {
                try {
                    out.put("effort", Effort.fromWire(effort.isString() ? effort.asString() : "?").wireValue());
                } catch (IllegalArgumentException e) {
                    errors.add(new FieldError("options.effort", "must be low, medium, high, xhigh or max"));
                }
            }
            JsonNode maxTokens = node.get("maxTokens");
            if (maxTokens != null && !maxTokens.isNull()) {
                if (!maxTokens.isIntegralNumber() || !maxTokens.canConvertToInt() || maxTokens.asInt() < 256
                        || maxTokens.asInt() > 128_000) {
                    errors.add(new FieldError("options.maxTokens", "must be an integer from 256 to 128000"));
                } else {
                    out.put("maxTokens", maxTokens.asInt());
                }
            }
            JsonNode temperature = node.get("temperature");
            if (temperature != null && !temperature.isNull()) {
                if (!temperature.isNumber() || temperature.asDouble() < 0 || temperature.asDouble() > 2) {
                    errors.add(new FieldError("options.temperature", "must be a number from 0 to 2, or null"));
                } else if (type == LlmProviderType.ANTHROPIC) {
                    errors.add(new FieldError("options.temperature",
                            "is not supported by current Claude models; control depth with effort"));
                } else {
                    out.put("temperature", temperature.asDouble());
                }
            }
            JsonNode headers = node.get("extraHeaders");
            if (headers != null && !headers.isNull()) {
                if (!headers.isObject()) {
                    errors.add(new FieldError("options.extraHeaders", "must be an object of header name to value"));
                } else {
                    Map<String, String> copy = new LinkedHashMap<>();
                    for (String name : headers.propertyNames()) {
                        JsonNode value = headers.get(name);
                        if (!name.matches("^[A-Za-z0-9!#$%&'*+.^_`|~-]+$")) {
                            errors.add(new FieldError("options.extraHeaders." + name, "is not a valid header name"));
                        } else if (RESERVED_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                            errors.add(new FieldError("options.extraHeaders." + name, "is set by Foresignal"));
                        } else if (!value.isString() || value.asString().indexOf('\n') >= 0
                                || value.asString().indexOf('\r') >= 0) {
                            errors.add(new FieldError("options.extraHeaders." + name, "must be a single-line string"));
                        } else {
                            copy.put(name, value.asString());
                        }
                    }
                    if (!copy.isEmpty()) {
                        out.put("extraHeaders", copy);
                    }
                }
            }
            JsonNode local = node.get("local");
            if (local != null && !local.isNull()) {
                if (!local.isBoolean()) {
                    errors.add(new FieldError("options.local", "must be a boolean"));
                } else if (local.asBoolean()) {
                    out.put("local", true);
                }
            }
        }
        if (type == LlmProviderType.ANTHROPIC) {
            out.putIfAbsent("effort", DEFAULT_ANTHROPIC_EFFORT);
            out.putIfAbsent("maxTokens", DEFAULT_ANTHROPIC_MAX_TOKENS);
        }
        return out;
    }

    private static String optionalString(JsonNode body, String name, List<FieldError> errors) {
        JsonNode node = body.get(name);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isString() || node.asString().isBlank()) {
            errors.add(new FieldError(name, "must be a non-empty string or null"));
            return null;
        }
        return node.asString().trim();
    }

    private static Integer optionalInt(JsonNode body, String name, int min, int max, List<FieldError> errors) {
        JsonNode node = body.get(name);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isIntegralNumber() || !node.canConvertToInt() || node.asInt() < min || node.asInt() > max) {
            errors.add(new FieldError(name, "must be an integer from " + min
                    + (max == Integer.MAX_VALUE ? " up" : " to " + max)));
            return null;
        }
        return node.asInt();
    }

    private static Long optionalLong(JsonNode body, String name, long min, List<FieldError> errors) {
        JsonNode node = body.get(name);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isIntegralNumber() || !node.canConvertToLong() || node.asLong() < min) {
            errors.add(new FieldError(name, "must be an integer of at least " + min));
            return null;
        }
        return node.asLong();
    }

    private static String stripTrailingSlash(String url) {
        String out = url;
        while (out.endsWith("/")) {
            out = out.substring(0, out.length() - 1);
        }
        return out;
    }
}
