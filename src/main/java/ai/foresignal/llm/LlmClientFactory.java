package ai.foresignal.llm;

import ai.foresignal.llm.client.AnthropicLlmClient;
import ai.foresignal.llm.client.LlmClient;
import ai.foresignal.llm.client.OllamaLlmClient;
import ai.foresignal.llm.client.OpenAiCompatibleLlmClient;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Builds the raw {@link LlmClient} for a provider configuration, once per configuration: an equal
 * {@link LlmProviderConfig} reuses the client, a changed one (model, URL, key, options) rebuilds it
 * and closes the old Anthropic SDK client.
 */
@Component
public class LlmClientFactory {

    private static final Logger log = LoggerFactory.getLogger(LlmClientFactory.class);

    private record Built(LlmProviderConfig config, LlmClient client) {}

    private final LlmHttp http;
    private final JsonMapper json;
    private final Map<String, Built> clients = new ConcurrentHashMap<>();

    /** Creates the factory. */
    public LlmClientFactory(LlmHttp http, JsonMapper json) {
        this.http = http;
        this.json = json;
    }

    /** The client for {@code config}, built on first use or when the configuration changed. */
    public LlmClient client(LlmProviderConfig config) {
        Built built = clients.compute(config.id(), (id, existing) -> {
            if (existing != null && existing.config().equals(config)) {
                return existing;
            }
            if (existing != null) {
                close(existing.client());
            }
            return new Built(config, build(config));
        });
        return built.client();
    }

    /** Drops (and closes) the client of a deleted provider. */
    public void evict(String providerId) {
        Built removed = clients.remove(providerId);
        if (removed != null) {
            close(removed.client());
        }
    }

    private LlmClient build(LlmProviderConfig config) {
        return switch (config.type()) {
            case ANTHROPIC -> new AnthropicLlmClient(config,
                    http.anthropic(config.id(), config.baseUrl(), config.apiKey()), json);
            case OPENAI_COMPATIBLE -> new OpenAiCompatibleLlmClient(config, http, json);
            case OLLAMA -> new OllamaLlmClient(config, http, json);
        };
    }

    private static void close(LlmClient client) {
        if (client instanceof AnthropicLlmClient anthropic) {
            try {
                anthropic.sdkClient().close();
            } catch (RuntimeException e) {
                log.debug("Closing the Anthropic client of {} failed", anthropic.providerId(), e);
            }
        }
    }

    /** Closes every client on shutdown. */
    @PreDestroy
    public void closeAll() {
        clients.values().forEach(built -> close(built.client()));
        clients.clear();
    }
}
