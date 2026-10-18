package ai.foresignal.api.mapper;

import ai.foresignal.llm.LlmBudgetGovernor;
import ai.foresignal.llm.LlmHealthTracker;
import ai.foresignal.llm.LlmProviderEntity;
import ai.foresignal.llm.LlmProviderService;
import ai.foresignal.llm.LlmProviderType;
import ai.foresignal.llm.LlmSettings;
import ai.foresignal.llm.LlmSettingsService;
import ai.foresignal.llm.LlmUsagePoint;
import ai.foresignal.llm.client.LlmModelInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Maps the LLM subsystem to its API schemas: {@code LlmProvider} (never the key, {@code apiKeySet}
 * only), {@code LlmSettings}, {@code LlmModelInfo} and {@code LlmUsageSeries}.
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
        out.put("local", LlmProviderType.OLLAMA.wireValue().equals(p.getType())
                || Boolean.TRUE.equals(p.getOptions() == null ? null : p.getOptions().get("local")));
        return out;
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
        LlmUsagePoint total = new LlmUsagePoint(from, 0, 0, 0, 0, 0, 0);
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
        return out;
    }
}
