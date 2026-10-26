package ai.foresignal.api.mapper;

import ai.foresignal.llm.LlmBudgetGovernor;
import ai.foresignal.llm.LlmHealthTracker;
import ai.foresignal.llm.LlmPricing;
import ai.foresignal.llm.LlmProviderBalances;
import ai.foresignal.llm.LlmProviderConfig;
import ai.foresignal.llm.LlmProviderEntity;
import ai.foresignal.llm.LlmProviderService;
import ai.foresignal.llm.LlmProviderType;
import ai.foresignal.llm.LlmSettings;
import ai.foresignal.llm.LlmSettingsService;
import ai.foresignal.llm.LlmSpendCaps;
import ai.foresignal.llm.LlmSpendWindow;
import ai.foresignal.llm.LlmUsagePoint;
import ai.foresignal.llm.client.LlmModelInfo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Maps the LLM subsystem to its API schemas: {@code LlmProvider} (never the key, {@code apiKeySet}
 * only), {@code LlmSettings}, {@code LlmModelInfo}, {@code LlmUsageSeries}, and (API 1.5.0)
 * {@code LlmPricing}, {@code LlmProviderBudget}, {@code LlmProviderBalance} and {@code LlmProbeResult}.
 */
@Component
public class LlmMapper {

    private final LlmSettingsService settings;
    private final LlmProviderService providers;
    private final LlmBudgetGovernor budget;

    /** Creates the mapper. */
    public LlmMapper(LlmSettingsService settings, LlmProviderService providers, LlmBudgetGovernor budget) {
        this.settings = settings;
        this.providers = providers;
        this.budget = budget;
    }

    /** One {@code LlmProvider}. */
    public Map<String, Object> provider(LlmProviderEntity p) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", p.getId());
        out.put("type", p.getType());
        out.put("baseUrl", p.getBaseUrl());
        out.put("chatModel", p.getChatModel());
        out.put("embeddingModel", p.getEmbeddingModel());
        out.put("embeddingDimensions", p.getEmbeddingDimensions());
        out.put("apiKeySet", LlmProviderService.apiKeySet(p));
        out.put("options", p.getOptions() == null ? Map.of() : p.getOptions());
        out.put("requestsPerMinute", p.getRequestsPerMinute());
        out.put("tokensPerDay", p.getTokensPerDay());
        out.put("enabled", p.isEnabled());
        out.put("roles", settings.roles(p.getId()));
        out.put("status", LlmHealthTracker.status(p));
        out.put("tokensToday", budget.usedToday(providers.config(p)));
        out.put("lastError", p.getLastError());
        out.put("updatedAt", p.getUpdatedAt() == null ? null : p.getUpdatedAt().toString());
        out.put("supportsTools", p.getSupportsTools());
        out.put("knowledgeCutoff", p.getKnowledgeCutoff() == null ? null : p.getKnowledgeCutoff().toString());
        out.put("lastSuccessAt", p.getLastSuccessAt() == null ? null : p.getLastSuccessAt().toString());
        out.put("lastErrorAt", p.getLastErrorAt() == null ? null : p.getLastErrorAt().toString());
        out.put("consecutiveFailures", p.getConsecutiveFailures());
        // R2-011: a personal provider is never local, as LlmProviderConfig#local() decides for egress.
        out.put("local", p.isShared() && (LlmProviderType.OLLAMA.wireValue().equals(p.getType())
                || Boolean.TRUE.equals(p.getOptions() == null ? null : p.getOptions().get("local"))));
        putMoney(out, p);
        out.put("shared", p.isShared());
        out.put("ownerPrincipalId", p.getOwnerPrincipalId());
        LlmSpendCaps perUser = p.getPerUserSpendCaps();
        out.put("perUserHourlyCapUsd", plain(perUser.hourlyUsd()));
        out.put("perUserDailyCapUsd", plain(perUser.dailyUsd()));
        out.put("perUserMonthlyCapUsd", plain(perUser.monthlyUsd()));
        return out;
    }

    /** {@code pricing}, {@code hourlyCapUsd}, {@code dailyCapUsd} and {@code monthlyCapUsd} (API 1.5.0). */
    private static void putMoney(Map<String, Object> out, LlmProviderEntity p) {
        out.put("pricing", pricing(p.getPricing()));
        LlmSpendCaps caps = p.getSpendCaps();
        out.put("hourlyCapUsd", plain(caps.hourlyUsd()));
        out.put("dailyCapUsd", plain(caps.dailyUsd()));
        out.put("monthlyCapUsd", plain(caps.monthlyUsd()));
    }

    /** {@code LlmPricing}. */
    public static Map<String, Object> pricing(LlmPricing pricing) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("inputUsdPerMillionTokens", plain(pricing.inputUsdPerMillionTokens()));
        out.put("outputUsdPerMillionTokens", plain(pricing.outputUsdPerMillionTokens()));
        out.put("cachedInputUsdPerMillionTokens", plain(pricing.cachedInputUsdPerMillionTokens()));
        out.put("embeddingUsdPerMillionTokens", plain(pricing.embeddingUsdPerMillionTokens()));
        return out;
    }

    /**
     * {@code LlmProviderBudget}: each window's spend against its cap, the token and request limits, and
     * the provider-reported balance, when the provider reports one.
     */
    public Map<String, Object> budget(LlmProviderEntity p, Map<LlmSpendWindow, LlmBudgetGovernor.WindowSpend> spend,
            Optional<LlmProviderBalances.Balance> balance) {
        LlmProviderConfig config = providers.config(p);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("providerId", p.getId());
        out.put("currency", "USD");
        out.put("priced", config.pricing().isSet());
        out.put("pricing", pricing(config.pricing()));
        Map<String, Object> windows = new LinkedHashMap<>();
        for (LlmSpendWindow window : LlmSpendWindow.values()) {
            LlmBudgetGovernor.WindowSpend w = spend.get(window);
            BigDecimal cap = config.spendCaps().of(window);
            BigDecimal spent = money(w.spentUsd());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("window", window.wireValue());
            item.put("start", w.start().toString());
            item.put("resetsAt", w.resetsAt().toString());
            item.put("spentUsd", spent);
            item.put("reservedUsd", money(w.reservedUsd()));
            item.put("capUsd", plain(cap));
            item.put("remainingUsd", cap == null ? null : money(cap.subtract(w.spentUsd()).max(BigDecimal.ZERO)));
            item.put("exhausted", cap != null && w.spentUsd().add(w.reservedUsd()).compareTo(cap) >= 0);
            windows.put(window.wireValue(), item);
        }
        out.put("windows", windows);
        Map<String, Object> limits = new LinkedHashMap<>();
        limits.put("requestsPerMinute", p.getRequestsPerMinute());
        limits.put("tokensPerDay", p.getTokensPerDay());
        limits.put("tokensToday", budget.usedToday(config));
        out.put("rateLimits", limits);
        out.put("providerBalance", balance.map(LlmMapper::balance).orElse(null));
        return out;
    }

    /** {@code LlmProviderBalance}. */
    public static Map<String, Object> balance(LlmProviderBalances.Balance balance) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("provider", balance.provider());
        out.put("kind", balance.kind());
        List<Map<String, Object>> amounts = new ArrayList<>();
        for (LlmProviderBalances.Amount amount : balance.amounts()) {
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("amount", plain(amount.amount()));
            a.put("currency", amount.currency());
            amounts.add(a);
        }
        out.put("amounts", amounts);
        out.put("fetchedAt", balance.fetchedAt().toString());
        out.put("error", balance.error());
        return out;
    }

    /** {@code LlmProbeResult}. */
    public static Map<String, Object> probe(Map<String, Object> result) {
        Map<String, Object> out = new LinkedHashMap<>(result);
        Object models = result.get("models");
        if (models instanceof List<?> list) {
            out.put("models", list.stream().map(m -> model((LlmModelInfo) m)).toList());
        }
        return out;
    }

    /** USD rounded to 6 decimal places, without trailing zeros. */
    private static BigDecimal money(BigDecimal value) {
        return plain(value == null ? BigDecimal.ZERO : value.setScale(6, RoundingMode.HALF_UP));
    }

    /** {@code value} without trailing zeros (and never in exponent form), or null. */
    private static BigDecimal plain(BigDecimal value) {
        if (value == null) {
            return null;
        }
        BigDecimal stripped = value.stripTrailingZeros();
        return stripped.scale() < 0 ? stripped.setScale(0) : stripped;
    }

    /**
     * A provider as an {@code LlmProviderInput} for the settings export: no key, no output-only
     * members.
     */
    public Map<String, Object> providerInput(LlmProviderEntity p) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", p.getId());
        out.put("type", p.getType());
        out.put("baseUrl", p.getBaseUrl());
        out.put("chatModel", p.getChatModel());
        out.put("embeddingModel", p.getEmbeddingModel());
        out.put("embeddingDimensions", p.getEmbeddingDimensions());
        out.put("options", p.getOptions() == null ? Map.of() : p.getOptions());
        out.put("requestsPerMinute", p.getRequestsPerMinute());
        out.put("tokensPerDay", p.getTokensPerDay());
        out.put("enabled", p.isEnabled());
        out.put("knowledgeCutoff", p.getKnowledgeCutoff() == null ? null : p.getKnowledgeCutoff().toString());
        putMoney(out, p);
        LlmSpendCaps perUser = p.getPerUserSpendCaps();
        out.put("perUserHourlyCapUsd", plain(perUser.hourlyUsd()));
        out.put("perUserDailyCapUsd", plain(perUser.dailyUsd()));
        out.put("perUserMonthlyCapUsd", plain(perUser.monthlyUsd()));
        return out;
    }

    /** {@code LlmSettings}. */
    public static Map<String, Object> settings(LlmSettings s) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("chatProvider", s.chatProvider());
        out.put("embeddingProvider", s.embeddingProvider());
        out.put("askEnabled", s.askEnabled());
        out.put("enrichmentEnabled", s.enrichmentEnabled());
        out.put("logContent", s.logContent());
        out.put("askMaxTokens", s.askMaxTokens());
        out.put("allowPrivateLocal", s.allowPrivateLocal());
        out.put("ttsProvider", s.ttsProvider());
        return out;
    }

    /** {@code LlmModelInfo}. */
    public static Map<String, Object> model(LlmModelInfo m) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", m.id());
        out.put("displayName", m.displayName());
        out.put("contextWindow", m.contextWindow());
        out.put("supportsTools", m.supportsTools());
        out.put("supportsEmbeddings", m.supportsEmbeddings());
        return out;
    }

    /** {@code LlmUsageSeries}: the non-empty buckets and their totals (timed at {@code from}). */
    public static Map<String, Object> usage(String providerId, String resolution, Instant from,
            List<LlmUsagePoint> points) {
        LlmUsagePoint total = new LlmUsagePoint(from, 0, 0, 0, 0, 0, 0, BigDecimal.ZERO);
        List<Map<String, Object>> items = new ArrayList<>();
        for (LlmUsagePoint point : points) {
            items.add(point(point));
            total = total.plus(point);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("provider", providerId);
        out.put("resolution", resolution);
        out.put("points", items);
        out.put("totals", point(total.at(from)));
        return out;
    }

    private static Map<String, Object> point(LlmUsagePoint p) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("t", p.t().toString());
        out.put("requests", p.requests());
        out.put("inputTokens", p.inputTokens());
        out.put("outputTokens", p.outputTokens());
        out.put("cachedTokens", p.cachedTokens());
        out.put("errors", p.errors());
        out.put("refused", p.refused());
        out.put("costUsd", money(p.costUsd()));
        return out;
    }
}
