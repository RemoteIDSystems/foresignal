package ai.foresignal.llm;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.llm.client.LlmProviderException;
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

/**
 * The one factory for every LLM provider HTTP client (prompt 026): the shared {@code java.net.http}
 * client of the OpenAI-compatible and Ollama implementations, and the Anthropic SDK client.
 *
 * <p>Routing every provider through here is what lets prompt 035 retrofit its outbound URL policy
 * (SSRF guard, {@code LAN_ALLOWED}) onto the provider {@code baseUrl} in one place:
 * {@link #checkBaseUrl}. LLM calls are not upstream fetches, so they do not pass through
 * {@code UpstreamClient}; their budgets are {@link LlmBudgetGovernor}'s. Redirects are never
 * followed, so a provider cannot bounce a request (and its key) elsewhere.
 */
@Component
public class LlmHttp {

    /** Connection timeout of the shared client. */
    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    /** Request timeout of the Anthropic SDK client (its own default; streaming scales it up). */
    static final Duration ANTHROPIC_TIMEOUT = Duration.ofMinutes(10);

    /** SDK retries on 408/409/429/5xx and connection errors (prompt 026: {@code maxRetries(2)}). */
    static final int ANTHROPIC_MAX_RETRIES = 2;

    private final HttpClient http;

    /** Creates the factory and its shared client. */
    public LlmHttp() {
        this.http = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NEVER)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    /**
     * Checks {@code baseUrl} before any client uses it.
     *
     * @throws LlmProviderException ({@code INVALID_REQUEST}) when the URL is not acceptable
     */
    public void checkBaseUrl(String providerId, String baseUrl) {
        List<FieldError> errors = new ArrayList<>();
        LlmProviderValidator.validateBaseUrl(baseUrl, errors);
        // 035: OutboundUrlPolicy — resolve and pin the host under LAN_ALLOWED here.
        if (!errors.isEmpty()) {
            throw new LlmProviderException(LlmProviderException.Kind.INVALID_REQUEST, providerId,
                    "baseUrl " + errors.getFirst().message());
        }
    }

    /** {@code baseUrl} joined with {@code path} (which starts with {@code /}). */
    public URI uri(String providerId, String baseUrl, String path) {
        checkBaseUrl(providerId, baseUrl);
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return URI.create(base + path);
    }

    /**
     * Sends a request and reads the body as a string.
     *
     * @throws LlmProviderException ({@code CONNECTION}) when the provider cannot be reached or times out
     */
    public HttpResponse<String> send(String providerId, HttpRequest request) {
        try {
            return http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException e) {
            throw new LlmProviderException(LlmProviderException.Kind.CONNECTION, providerId, "timed out", null, e);
        } catch (IOException e) {
            throw new LlmProviderException(LlmProviderException.Kind.CONNECTION, providerId,
                    "could not be reached (" + e.getClass().getSimpleName() + ")", null, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LlmProviderException(LlmProviderException.Kind.CONNECTION, providerId, "interrupted", null, e);
        }
    }

    /**
     * Sends a request and streams the body as lines (NDJSON or server-sent events). The caller closes
     * the stream.
     *
     * @throws LlmProviderException ({@code CONNECTION}) when the provider cannot be reached or times out
     */
    public HttpResponse<Stream<String>> sendLines(String providerId, HttpRequest request) {
        try {
            return http.send(request, HttpResponse.BodyHandlers.ofLines());
        } catch (HttpTimeoutException e) {
            throw new LlmProviderException(LlmProviderException.Kind.CONNECTION, providerId, "timed out", null, e);
        } catch (IOException e) {
            throw new LlmProviderException(LlmProviderException.Kind.CONNECTION, providerId,
                    "could not be reached (" + e.getClass().getSimpleName() + ")", null, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LlmProviderException(LlmProviderException.Kind.CONNECTION, providerId, "interrupted", null, e);
        }
    }

    /**
     * Builds an Anthropic SDK client for one provider configuration: the decrypted key, the base URL,
     * two retries. A provider without a key is refused here, so the SDK never looks for credentials
     * on the host. Built once per configuration by {@link LlmClientFactory} and closed when the
     * configuration changes.
     */
    public AnthropicClient anthropic(String providerId, String baseUrl, String apiKey) {
        checkBaseUrl(providerId, baseUrl);
        if (apiKey == null || apiKey.isBlank()) {
            // Never let the SDK fall back to host credentials (environment, profile files).
            throw new LlmProviderException(LlmProviderException.Kind.AUTHENTICATION, providerId,
                    "no API key is set; set one with PUT /v1/llm/providers/" + providerId);
        }
        return AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .maxRetries(ANTHROPIC_MAX_RETRIES)
                .timeout(ANTHROPIC_TIMEOUT)
                .build();
    }
}
