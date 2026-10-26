package ai.foresignal.llm;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.llm.client.Effort;
import ai.foresignal.net.OutboundUrlPolicy;
import java.math.BigDecimal;
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
 * fragment. {@link #parse} then applies the outbound URL policy (prompt 035) with profile
 * {@code LAN_ALLOWED} ({@link #PROFILE}): private-network URLs stay allowed, because a local Ollama is
 * the point, while link-local, metadata and the other blocked addresses are refused with 400
 * {@code URL_NOT_ALLOWED} on {@code baseUrl}. {@link LlmHttp} checks again before every request.
 */
@Component
public class LlmProviderValidator {

    /**
     * The outbound profile of every provider {@code baseUrl}. {@code PUBLIC_ONLY} is not forced for
     * frontier providers, because the same field points at a local Ollama.
     */
    public static final OutboundUrlPolicy.Profile PROFILE = OutboundUrlPolicy.Profile.LAN_ALLOWED;

    private final OutboundUrlPolicy policy;

    /** Creates the validator. */
    public LlmProviderValidator(OutboundUrlPolicy policy) {
        this.policy = policy;
    }

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
            "knowledgeCutoff", "pricing", "hourlyCapUsd", "dailyCapUsd", "monthlyCapUsd", "shared",
            "perUserHourlyCapUsd", "perUserDailyCapUsd", "perUserMonthlyCapUsd");

    /** Output-only members of {@code LlmProvider}, tolerated (and ignored) so an exported provider imports. */
    private static final Set<String> OUTPUT_ONLY = Set.of("apiKeySet", "roles", "status", "tokensToday", "lastError",
            "updatedAt", "supportsTools", "lastSuccessAt", "lastErrorAt", "consecutiveFailures", "local", "ownerPrincipalId");

    private static final Set<String> PRICING_MEMBERS = Set.of("inputUsdPerMillionTokens", "outputUsdPerMillionTokens",
            "cachedInputUsdPerMillionTokens", "embeddingUsdPerMillionTokens");

    /** The largest price, in USD per million tokens. */
    static final BigDecimal MAX_PRICE = BigDecimal.valueOf(100_000);

    /** The largest USD cap of any window. */
    static final BigDecimal MAX_CAP = BigDecimal.valueOf(10_000_000_000L);

    private static final Set<String> OPTION_MEMBERS = Set.of("effort", "maxTokens", "temperature", "extraHeaders",
            "local", "ttsModel", "ttsCharactersPerDay");

    /** The largest daily character budget of the {@code tts} role (prompt 141). */
    public static final long MAX_TTS_CHARACTERS_PER_DAY = 100_000_000L;

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
        boolean urlRefused = false;
        if (baseUrl != null) {
            int before = errors.size();
            validateBaseUrl(baseUrl, errors);
            if (errors.size() == before) {
                // 035: OutboundUrlPolicy (LAN_ALLOWED); a host that does not resolve yet is accepted.
                String problem = policy.saveProblem(baseUrl, PROFILE, false);
                if (problem != null) {
                    errors.add(new FieldError("baseUrl", problem));
                    urlRefused = true;
                }
            }
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

        LlmPricing pricing = pricing(body.get("pricing"), type, errors);
        LlmSpendCaps caps = new LlmSpendCaps(cap(body, "hourlyCapUsd", errors), cap(body, "dailyCapUsd", errors),
                cap(body, "monthlyCapUsd", errors));
        if (caps.isSet() && !pricing.isSet()) {
            String field = caps.hourlyUsd() != null ? "hourlyCapUsd" : caps.dailyUsd() != null ? "dailyCapUsd"
                    : "monthlyCapUsd";
            errors.add(new FieldError(field, "needs pricing: spend is the metered tokens at the provider's prices,"
                    + " so a cap without prices could never be reached"));
        }
        // R2-011: the caps a shared provider applies to each user's own spend.
        LlmSpendCaps perUser = new LlmSpendCaps(cap(body, "perUserHourlyCapUsd", errors),
                cap(body, "perUserDailyCapUsd", errors), cap(body, "perUserMonthlyCapUsd", errors));
        if (perUser.isSet() && !pricing.isSet()) {
            String field = perUser.hourlyUsd() != null ? "perUserHourlyCapUsd"
                    : perUser.dailyUsd() != null ? "perUserDailyCapUsd" : "perUserMonthlyCapUsd";
            errors.add(new FieldError(field, "needs pricing: spend is the metered tokens at the provider's prices,"
                    + " so a cap without prices could never be reached"));
        }
        Boolean shared = null;
        JsonNode sharedNode = body.get("shared");
        if (sharedNode != null && !sharedNode.isNull()) {
            if (sharedNode.isBoolean()) {
                shared = sharedNode.asBoolean();
            } else {
                errors.add(new FieldError("shared", "must be a boolean"));
            }
        }

        if (!errors.isEmpty()) {
            if (urlRefused) {
                throw OutboundUrlPolicy.notAllowed(errors);
            }
            throw new ValidationException("The LLM provider is not valid.", errors);
        }
        return new LlmProviderInput(id, type, baseUrl, chatModel, embeddingModel, embeddingDimensions, apiKey, options,
                rpm == null ? DEFAULT_REQUESTS_PER_MINUTE : rpm, tpd == null ? DEFAULT_TOKENS_PER_DAY : tpd, enabled,
                cutoff, pricing, caps, perUser, shared);
    }

    /**
     * Checks {@code baseUrl}'s shape: absolute http or https with a host, no user info, no fragment.
     * The address rules are the outbound URL policy's ({@link #PROFILE}), applied by the callers.
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
        // The outbound URL policy (035, LAN_ALLOWED) is applied by parse and by LlmHttp.checkBaseUrl.
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
                            "is not an option (effort, maxTokens, temperature, extraHeaders, local, ttsModel,"
                                    + " ttsCharactersPerDay)"));
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
            // Prompt 141: the speech model and the daily character budget of the tts role.
            JsonNode ttsModel = node.get("ttsModel");
            if (ttsModel != null && !ttsModel.isNull()) {
                if (!ttsModel.isString() || ttsModel.asString().isBlank() || ttsModel.asString().length() > 200) {
                    errors.add(new FieldError("options.ttsModel", "must be a model name of 1 to 200 characters"));
                } else if (type != LlmProviderType.OPENAI_COMPATIBLE) {
                    errors.add(new FieldError("options.ttsModel", "applies only to openai-compatible providers"));
                } else {
                    out.put("ttsModel", ttsModel.asString().strip());
                }
            }
            JsonNode ttsChars = node.get("ttsCharactersPerDay");
            if (ttsChars != null && !ttsChars.isNull()) {
                if (!ttsChars.isIntegralNumber() || ttsChars.asLong() < 1_000
                        || ttsChars.asLong() > MAX_TTS_CHARACTERS_PER_DAY) {
                    errors.add(new FieldError("options.ttsCharactersPerDay",
                            "must be an integer from 1000 to " + MAX_TTS_CHARACTERS_PER_DAY));
                } else {
                    out.put("ttsCharactersPerDay", ttsChars.asLong());
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

    private static LlmPricing pricing(JsonNode node, LlmProviderType type, List<FieldError> errors) {
        if (node == null || node.isNull()) {
            return LlmPricing.NONE;
        }
        if (!node.isObject()) {
            errors.add(new FieldError("pricing", "must be an object of prices in USD per million tokens"));
            return LlmPricing.NONE;
        }
        for (String name : node.propertyNames()) {
            if (!PRICING_MEMBERS.contains(name)) {
                errors.add(new FieldError("pricing." + name, "is not a price (inputUsdPerMillionTokens,"
                        + " outputUsdPerMillionTokens, cachedInputUsdPerMillionTokens, embeddingUsdPerMillionTokens)"));
            }
        }
        BigDecimal embedding = price(node, "embeddingUsdPerMillionTokens", errors);
        if (embedding != null && type == LlmProviderType.ANTHROPIC) {
            errors.add(new FieldError("pricing.embeddingUsdPerMillionTokens",
                    "Anthropic has no embeddings endpoint, so it has no embedding price"));
            embedding = null;
        }
        return new LlmPricing(price(node, "inputUsdPerMillionTokens", errors),
                price(node, "outputUsdPerMillionTokens", errors), price(node, "cachedInputUsdPerMillionTokens", errors),
                embedding);
    }

    private static BigDecimal price(JsonNode pricing, String name, List<FieldError> errors) {
        JsonNode node = pricing.get(name);
        if (node == null || node.isNull()) {
            return null;
        }
        BigDecimal value = node.isNumber() ? node.decimalValue() : null;
        if (value == null || value.signum() < 0 || value.compareTo(MAX_PRICE) > 0 || value.stripTrailingZeros().scale() > 6) {
            errors.add(new FieldError("pricing." + name, "must be a number from 0 to " + MAX_PRICE
                    + " with at most 6 decimal places, in USD per million tokens"));
            return null;
        }
        return value.stripTrailingZeros().scale() < 0 ? value.setScale(0) : value.stripTrailingZeros();
    }

    private static BigDecimal cap(JsonNode body, String name, List<FieldError> errors) {
        JsonNode node = body.get(name);
        if (node == null || node.isNull()) {
            return null;
        }
        BigDecimal value = node.isNumber() ? node.decimalValue() : null;
        if (value == null || value.signum() <= 0 || value.compareTo(MAX_CAP) > 0 || value.stripTrailingZeros().scale() > 4) {
            errors.add(new FieldError(name, "must be a number above 0 and at most " + MAX_CAP.toPlainString()
                    + " with at most 4 decimal places, in USD, or null for no cap"));
            return null;
        }
        return value.stripTrailingZeros().scale() < 0 ? value.setScale(0) : value.stripTrailingZeros();
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
