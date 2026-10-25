package ai.foresignal.llm;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.llm.client.ChatMessage;
import ai.foresignal.llm.client.ChatRequest;
import ai.foresignal.llm.client.ChatResponse;
import ai.foresignal.llm.client.EmbeddingModelSpec;
import ai.foresignal.llm.client.EmbeddingResponse;
import ai.foresignal.llm.client.LlmClient;
import ai.foresignal.llm.client.LlmModelInfo;
import ai.foresignal.llm.client.LlmProviderException;
import ai.foresignal.llm.client.Purpose;
import ai.foresignal.llm.client.StopReason;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The front door of the LLM subsystem: governed clients for a provider or a role.
 *
 * <p>A governed client resolves the provider's current configuration, reserves the call's estimated
 * tokens and one request with {@link LlmBudgetGovernor} (refusing with
 * {@link LlmBudgetExceededException}), calls the raw client, then records usage
 * ({@link LlmUsageMeter}), settles the reservation with the reported tokens, updates provider health
 * ({@link LlmHealthTracker}, including {@code supportsTools=false} when the model rejects tools) and
 * logs the call: purpose, provider, model, tokens (cached included), duration and stop reason.
 * Prompt and response bodies are logged only when {@code logContent} is on, at DEBUG; API keys never.
 *
 * <p>No call happens without an explicit trigger (NFR-13): nothing here runs on its own.
 */
@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    /** The test endpoint's prompt. */
    public static final String TEST_PROMPT = "Reply with the single word: ok";

    /** The test endpoint's output cap. */
    public static final int TEST_MAX_TOKENS = 16;

    private final LlmProviderService providers;
    private final LlmSettingsService settings;
    private final LlmClientFactory factory;
    private final LlmBudgetGovernor budget;
    private final LlmUsageMeter usage;
    private final LlmHealthTracker health;
    private final Clock clock;

    /** Creates the service. */
    public LlmService(LlmProviderService providers, LlmSettingsService settings, LlmClientFactory factory,
            LlmBudgetGovernor budget, LlmUsageMeter usage, LlmHealthTracker health, Clock clock) {
        this.providers = providers;
        this.settings = settings;
        this.factory = factory;
        this.budget = budget;
        this.usage = usage;
        this.health = health;
        this.clock = clock;
    }

    /** True when a chat provider is assigned. */
    public boolean chatConfigured() {
        return settings.get().chatProvider() != null;
    }

    /** The governed client of the chat role, when assigned (Ask, briefings, enrichment). */
    public Optional<LlmClient> chatClient() {
        return Optional.ofNullable(settings.get().chatProvider()).map(this::client);
    }

    /** The governed client of the embedding role, when assigned (search). */
    public Optional<LlmClient> embeddingClient() {
        return Optional.ofNullable(settings.get().embeddingProvider()).map(this::client);
    }

    /** The governed client of the chat role, or {@code 409 LLM_NOT_CONFIGURED}. */
    public LlmClient requireChatClient() {
        return chatClient().orElseThrow(() -> new ConflictException(ErrorCodes.LLM_NOT_CONFIGURED,
                "No chat provider is configured; add one in /v1/llm/providers and assign it in PUT /v1/llm/settings."));
    }

    /** The configuration snapshot of a provider (for the caller's {@code EgressPolicy} destination). */
    public LlmProviderConfig config(String providerId) {
        return providers.config(providerId);
    }

    /** A governed client for provider {@code providerId}; it re-reads the configuration on every call. */
    public LlmClient client(String providerId) {
        return new GovernedClient(providerId);
    }

    /**
     * {@code testLlmProvider}: a tiny chat ("Reply with the single word: ok", 16 tokens, purpose
     * {@code test}) and, when the provider has an embedding model, an embedding of "test". Consumes
     * budget. A budget refusal propagates (429); a provider failure is reported in the result.
     */
    public Map<String, Object> test(String providerId) {
        return test(providers.config(providerId), client(providerId));
    }

    /**
     * The {@code LlmTestResult} of {@code config}, calling {@code client}: the governed client of a saved
     * provider, or a raw one for {@link #probe}.
     */
    private Map<String, Object> test(LlmProviderConfig config, LlmClient client) {
        Map<String, Object> out = new LinkedHashMap<>();
        boolean success = true;
        String error = null;
        String refusalCategory = null;
        if (config.chatModel() != null) {
            Instant start = clock.instant();
            try {
                ChatResponse response = client.chat(ChatRequest.simple(Purpose.TEST, "", TEST_PROMPT,
                        TEST_MAX_TOKENS));
                Map<String, Object> chat = new LinkedHashMap<>();
                chat.put("model", response.model());
                chat.put("latencyMs", Duration.between(start, clock.instant()).toMillis());
                chat.put("inputTokens", response.usage().inputTokens());
                chat.put("outputTokens", response.usage().outputTokens());
                chat.put("cachedTokens", response.usage().cachedTokens());
                chat.put("text", response.content());
                chat.put("stopReason", response.stopReason().wireValue());
                out.put("chat", chat);
                if (response.stopReason() == StopReason.REFUSAL) {
                    success = false;
                    refusalCategory = response.refusalCategory();
                    error = "The model refused the test prompt"
                            + (refusalCategory == null ? "" : " (category " + refusalCategory + ")");
                } else if (response.stopReason() == StopReason.ERROR) {
                    success = false;
                    error = "The provider reported an error stop reason";
                }
            } catch (LlmProviderException e) {
                success = false;
                error = e.getMessage();
            }
        }
        if (config.embeddingSpec().isPresent()) {
            Instant start = clock.instant();
            try {
                EmbeddingResponse response = client.embed(List.of("test"));
                Map<String, Object> embedding = new LinkedHashMap<>();
                embedding.put("model", response.model());
                embedding.put("dimensions", response.dimensions());
                embedding.put("latencyMs", Duration.between(start, clock.instant()).toMillis());
                out.put("embedding", embedding);
            } catch (LlmProviderException e) {
                success = false;
                error = error == null ? e.getMessage() : error + "; " + e.getMessage();
            }
        } else {
            out.put("embedding", null);
        }
        if (config.chatModel() == null && config.embeddingSpec().isEmpty()) {
            success = false;
            error = "The provider has neither a chatModel nor an embedding model to test";
        }
        out.put("success", success);
        out.put("error", error);
        out.put("refusalCategory", refusalCategory);
        return out;
    }

    /**
     * {@code probeLlmProvider}: tests an unsaved configuration and lists its models in one round trip,
     * persisting nothing. The client is built for this call alone (never the cached client of a saved
     * provider) and closed after it; the key is used for these requests and then dropped. Nothing is
     * metered, budgeted or recorded as health, because there is no provider to charge it to; the outbound
     * URL policy applies exactly as to a saved provider ({@link LlmHttp}).
     *
     * <p>The models are listed first. With a chat or embedding model the {@link #test} probes run as
     * well; without one, the listing is the connection test. A type that cannot list its models answers
     * an empty list with {@code modelsError} and, when the chat probe works, a successful test.
     *
     * @return the {@code LlmProbeResult}: {@code test}, {@code models}, {@code modelsError}
     */
    public Map<String, Object> probe(LlmProviderConfig config) {
        LlmClient client;
        try {
            client = factory.detached(config);
        } catch (LlmProviderException e) {
            // A refused base URL, or an Anthropic provider without a key: nothing was sent.
            return probeResult(failedTest(e.getMessage()), List.of(), e.getMessage());
        }
        try {
            List<LlmModelInfo> models = List.of();
            String modelsError = null;
            try {
                models = client.models();
            } catch (LlmProviderException e) {
                modelsError = e.getMessage();
            } catch (UnsupportedOperationException e) {
                modelsError = "this provider type does not list its models";
            }
            Map<String, Object> test;
            if (config.chatModel() != null || config.embeddingSpec().isPresent()) {
                test = test(config, client);
            } else if (modelsError == null) {
                test = new LinkedHashMap<>(failedTest(null));
                test.put("success", true);
            } else {
                test = failedTest(modelsError);
            }
            return probeResult(test, models, modelsError);
        } finally {
            factory.close(client);
        }
    }

    private static Map<String, Object> failedTest(String error) {
        Map<String, Object> test = new LinkedHashMap<>();
        test.put("chat", null);
        test.put("embedding", null);
        test.put("success", false);
        test.put("error", error);
        test.put("refusalCategory", null);
        return test;
    }

    private static Map<String, Object> probeResult(Map<String, Object> test, List<LlmModelInfo> models,
            String modelsError) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("test", test);
        out.put("models", models);
        out.put("modelsError", modelsError);
        return out;
    }

    /** Forgets a deleted provider's client and budget state. */
    public void forget(String providerId) {
        factory.evict(providerId);
        budget.invalidate(providerId);
    }

    /** {@code listLlmProviderModels}: the provider's models; a provider failure is 502. */
    public List<LlmModelInfo> models(String providerId) {
        LlmProviderConfig config = providers.config(providerId);
        return factory.client(config).models();
    }

    /** A client that budgets, meters, tracks health and logs every call. */
    private final class GovernedClient implements LlmClient {

        private final String providerId;

        GovernedClient(String providerId) {
            this.providerId = providerId;
        }

        @Override
        public String providerId() {
            return providerId;
        }

        @Override
        public ChatResponse chat(ChatRequest request) {
            LlmProviderConfig config = providers.config(providerId);
            int maxTokens = request.maxTokens() != null ? request.maxTokens() : config.effectiveMaxTokens();
            long promptTokens = LlmBudgetGovernor.estimate(request.promptCharacters(), 0);
            LlmBudgetGovernor.Reservation reservation = budget.reserve(config, promptTokens + Math.max(0, maxTokens),
                    config.pricing().chatEstimate(promptTokens, maxTokens));
            Instant start = clock.instant();
            try {
                ChatResponse response = factory.client(config).chat(request);
                long ms = Duration.between(start, clock.instant()).toMillis();
                BigDecimal cost = config.pricing().chat(response.usage(), config.type());
                usage.recordCall(providerId, response.usage(), cost);
                budget.settle(reservation, response.usage().total(), cost);
                health.success(providerId);
                log.info("LLM call purpose={} provider={} model={} inputTokens={} outputTokens={} cachedTokens={}"
                                + " durationMs={} stopReason={}", request.purpose().wireValue(), providerId,
                        response.model(), response.usage().inputTokens(), response.usage().outputTokens(),
                        response.usage().cachedTokens(), ms, response.stopReason().wireValue());
                if (settings.get().logContent() && log.isDebugEnabled()) {
                    log.debug("LLM call content provider={} system={} messages={} response={}", providerId,
                            request.system(), describe(request.messages()), response.content());
                }
                return response;
            } catch (LlmProviderException e) {
                failed(config, reservation, request.purpose(), start, e);
                throw e;
            }
        }

        @Override
        public EmbeddingResponse embed(List<String> texts) {
            LlmProviderConfig config = providers.config(providerId);
            return governedEmbed(config, texts, () -> factory.client(config).embed(texts));
        }

        @Override
        public EmbeddingResponse embed(List<String> texts, EmbeddingModelSpec spec) {
            LlmProviderConfig config = providers.config(providerId);
            return governedEmbed(config, texts, () -> factory.client(config).embed(texts, spec));
        }

        private EmbeddingResponse governedEmbed(LlmProviderConfig config, List<String> texts,
                java.util.function.Supplier<EmbeddingResponse> call) {
            if (!config.type().supportsEmbeddings()) {
                return factory.client(config).embed(texts); // throws UnsupportedOperationException
            }
            long chars = texts.stream().mapToLong(String::length).sum();
            long tokens = LlmBudgetGovernor.estimate(chars, 0);
            LlmBudgetGovernor.Reservation reservation = budget.reserve(config, tokens,
                    config.pricing().embedding(tokens));
            Instant start = clock.instant();
            try {
                EmbeddingResponse response = call.get();
                BigDecimal cost = config.pricing().embedding(response.usage());
                usage.recordCall(providerId, response.usage(), cost);
                budget.settle(reservation, response.usage().total(), cost);
                health.success(providerId);
                log.info("LLM call purpose=embedding provider={} model={} inputTokens={} texts={} durationMs={}",
                        providerId, response.model(), response.usage().inputTokens(), texts.size(),
                        Duration.between(start, clock.instant()).toMillis());
                return response;
            } catch (LlmProviderException e) {
                failed(config, reservation, null, start, e);
                throw e;
            }
        }

        private void failed(LlmProviderConfig config, LlmBudgetGovernor.Reservation reservation, Purpose purpose,
                Instant start, LlmProviderException e) {
            usage.recordError(providerId);
            budget.settle(reservation, 0);
            if (e.kind() == LlmProviderException.Kind.TOOLS_UNSUPPORTED) {
                // The provider works; its model does not take tools. Ask switches to retrieval-only.
                health.supportsTools(providerId, false);
            } else {
                health.failure(providerId, e.getMessage());
            }
            log.warn("LLM call failed purpose={} provider={} model={} durationMs={} kind={}: {}",
                    purpose == null ? "embedding" : purpose.wireValue(), providerId, config.chatModel(),
                    Duration.between(start, clock.instant()).toMillis(), e.kind(), e.getMessage());
        }

        @Override
        public List<LlmModelInfo> models() {
            return LlmService.this.models(providerId);
        }
    }

    private static String describe(List<ChatMessage> messages) {
        StringBuilder out = new StringBuilder("[");
        for (ChatMessage message : messages) {
            if (out.length() > 1) {
                out.append(", ");
            }
            switch (message) {
                case ChatMessage.User user -> out.append("user: ").append(user.text());
                case ChatMessage.Assistant assistant -> out.append("assistant: ").append(assistant.text())
                        .append(assistant.toolCalls().isEmpty() ? "" : " " + assistant.toolCalls());
                case ChatMessage.ToolResults results -> out.append("tool results: ").append(results.results());
            }
        }
        return out.append(']').toString();
    }
}
