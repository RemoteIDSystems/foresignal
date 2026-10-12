package ai.foresignal.llm;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.llm.client.LlmProviderException;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.net.PinnedHttp;
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
 * The one factory for every LLM provider HTTP client (prompt 026): the {@code java.net.http} clients
 * of the OpenAI-compatible and Ollama implementations, and the Anthropic SDK client.
 *
 * <p>Routing every provider through here is what lets prompt 035 apply its outbound URL policy
 * (profile {@code LAN_ALLOWED}, {@link LlmProviderValidator#PROFILE}) in one place:
 * {@link #checkBaseUrl} refuses a base URL whose host is link-local, metadata or otherwise blocked,
 * and every request is checked again just before it is sent and connects to the checked address with
 * the original {@code Host} and TLS name ({@link PinnedHttp}), so a DNS answer that changed since the
 * provider was saved cannot redirect it (DNS rebinding). The Anthropic SDK client keeps the SDK's
 * request building, retries and credentials, but its transport is replaced by
 * {@link PinnedSdkHttpClient}, which does the same check and pinning (the SDK's own OkHttp transport
 * follows redirects and offers no DNS hook). LLM calls are not upstream fetches, so they do not pass
 * through {@code UpstreamClient}; their budgets are {@link LlmBudgetGovernor}'s. Redirects are never
 * followed, so a provider cannot bounce a request (and its key) elsewhere.
 */
@Component
public class LlmHttp {

    /** Connection timeout of the provider clients. */
    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    /** Request timeout of the Anthropic SDK client (its own default; streaming scales it up). */
    static final Duration ANTHROPIC_TIMEOUT = Duration.ofMinutes(10);

    /** SDK retries on 408/409/429/5xx and connection errors (prompt 026: {@code maxRetries(2)}). */
    static final int ANTHROPIC_MAX_RETRIES = 2;

    /** The settings of every provider client. */
    static final PinnedHttp.ClientSpec SPEC = new PinnedHttp.ClientSpec(CONNECT_TIMEOUT, HttpClient.Version.HTTP_1_1);

    private final OutboundUrlPolicy policy;
    private final PinnedHttp pinned;

    /** Creates the factory. */
    public LlmHttp(OutboundUrlPolicy policy, PinnedHttp pinned) {
        this.policy = policy;
        this.pinned = pinned;
    }

    /**
     * Checks {@code baseUrl} before any client uses it: its shape, then the outbound URL policy.
     *
     * @throws LlmProviderException ({@code INVALID_REQUEST}) when the URL is not acceptable
     */
    public void checkBaseUrl(String providerId, String baseUrl) {
        List<FieldError> errors = new ArrayList<>();
        LlmProviderValidator.validateBaseUrl(baseUrl, errors);
        if (!errors.isEmpty()) {
            throw new LlmProviderException(LlmProviderException.Kind.INVALID_REQUEST, providerId,
                    "baseUrl " + errors.getFirst().message());
        }
        // 035: OutboundUrlPolicy (LAN_ALLOWED). A host that does not resolve yet fails at send time.
        if (policy.check(baseUrl, LlmProviderValidator.PROFILE, false) instanceof OutboundUrlPolicy.Denied denied
                && !denied.unresolved()) {
            throw notAllowed(providerId, denied);
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
     * @throws LlmProviderException ({@code INVALID_REQUEST}) when the outbound URL policy refuses the
     *     destination; ({@code CONNECTION}) when the provider cannot be reached or times out
     */
    public HttpResponse<String> send(String providerId, HttpRequest request) {
        return exchange(providerId, request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Sends a request and streams the body as lines (NDJSON or server-sent events). The caller closes
     * the stream.
     *
     * @throws LlmProviderException ({@code INVALID_REQUEST}) when the outbound URL policy refuses the
     *     destination; ({@code CONNECTION}) when the provider cannot be reached or times out
     */
    public HttpResponse<Stream<String>> sendLines(String providerId, HttpRequest request) {
        return exchange(providerId, request, HttpResponse.BodyHandlers.ofLines());
    }

    /**
     * Builds an Anthropic SDK client for one provider configuration: the decrypted key, the base URL,
     * two retries, and the pinned transport. A provider without a key is refused here, so the SDK
     * never looks for credentials on the host. Built once per configuration by
     * {@link LlmClientFactory} and closed when the configuration changes.
     */
    public AnthropicClient anthropic(String providerId, String baseUrl, String apiKey) {
        checkBaseUrl(providerId, baseUrl);
        if (apiKey == null || apiKey.isBlank()) {
            // Never let the SDK fall back to host credentials (environment, profile files).
            throw new LlmProviderException(LlmProviderException.Kind.AUTHENTICATION, providerId,
                    "no API key is set; set one with PUT /v1/llm/providers/" + providerId);
        }
        AnthropicClient sdk = AnthropicOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .maxRetries(ANTHROPIC_MAX_RETRIES)
                .timeout(ANTHROPIC_TIMEOUT)
                .build();
        // Keep the SDK's credentials, headers and retries; replace only its transport.
        return sdk.withOptions(options -> options.httpClient(new PinnedSdkHttpClient(providerId, this)));
    }

    /** The checked, pinned exchange shared by {@link #send} and {@link #sendLines}. */
    private <T> HttpResponse<T> exchange(String providerId, HttpRequest request, HttpResponse.BodyHandler<T> handler) {
        try {
            return clientFor(request.uri()).send(pin(providerId, request), handler);
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
     * The pinned client for requests to {@code uri}'s host.
     *
     * @throws IOException when TLS cannot be set up
     */
    HttpClient clientFor(URI uri) throws IOException {
        return pinned.client(uri, SPEC);
    }

    /**
     * {@code request} checked under {@code LAN_ALLOWED} and rewritten to the checked address.
     *
     * @throws LlmProviderException ({@code INVALID_REQUEST}) when the policy refuses the destination,
     *     ({@code CONNECTION}) when its host does not resolve
     * @throws IOException when the URI cannot be rewritten
     */
    HttpRequest pin(String providerId, HttpRequest request) throws IOException {
        OutboundUrlPolicy.Result result = policy.check(request.uri(), LlmProviderValidator.PROFILE);
        if (result instanceof OutboundUrlPolicy.Denied denied) {
            if (denied.unresolved()) {
                throw new LlmProviderException(LlmProviderException.Kind.CONNECTION, providerId,
                        "could not be reached (" + denied.reason() + ")");
            }
            throw notAllowed(providerId, denied);
        }
        return PinnedHttp.pin(request, ((OutboundUrlPolicy.Allowed) result).pinned());
    }

    private static LlmProviderException notAllowed(String providerId, OutboundUrlPolicy.Denied denied) {
        return new LlmProviderException(LlmProviderException.Kind.INVALID_REQUEST, providerId,
                "baseUrl is not allowed by the outbound URL policy (" + denied.code() + "): " + denied.reason());
    }
}
