package ai.foresignal.llm;

import com.anthropic.core.RequestOptions;
import com.anthropic.core.Timeout;
import com.anthropic.core.http.Headers;
import com.anthropic.core.http.HttpClient;
import com.anthropic.core.http.HttpRequest;
import com.anthropic.core.http.HttpRequestBody;
import com.anthropic.core.http.HttpResponse;
import com.anthropic.errors.AnthropicIoException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * The transport of the Anthropic SDK client (prompt 035): the SDK builds the request, sets the
 * credentials and retries; this sends it over {@code java.net.http} through {@link LlmHttp}, checked
 * by the outbound URL policy ({@code LAN_ALLOWED}) and connected to the checked address with the
 * original {@code Host} and TLS name, redirects never followed.
 *
 * <p>The SDK's own OkHttp transport follows redirects and has no DNS hook, so it could not be pinned.
 * Connection failures surface as {@link AnthropicIoException}, which the SDK retries; a policy refusal
 * is an {@code LlmProviderException} ({@code INVALID_REQUEST}), which it does not.
 */
final class PinnedSdkHttpClient implements HttpClient {

    /** Headers the JDK client sets itself and refuses from callers. */
    static final Set<String> RESTRICTED = Set.of("connection", "content-length", "expect", "host", "upgrade");

    private final String providerId;
    private final LlmHttp http;

    /** A transport for {@code providerId}'s requests. */
    PinnedSdkHttpClient(String providerId, LlmHttp http) {
        this.providerId = providerId;
        this.http = http;
    }

    @Override
    public HttpResponse execute(HttpRequest request, RequestOptions options) {
        java.net.http.HttpRequest jdk = toJdk(request, options);
        try {
            java.net.http.HttpRequest pinned = http.pin(providerId, jdk);
            return new Response(http.clientFor(jdk.uri()).send(pinned,
                    java.net.http.HttpResponse.BodyHandlers.ofInputStream()));
        } catch (IOException e) {
            throw new AnthropicIoException("Request failed: " + e.getClass().getSimpleName(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AnthropicIoException("Request interrupted", e);
        }
    }

    @Override
    public CompletableFuture<HttpResponse> executeAsync(HttpRequest request, RequestOptions options) {
        try {
            java.net.http.HttpRequest jdk = toJdk(request, options);
            java.net.http.HttpRequest pinned = http.pin(providerId, jdk);
            return http.clientFor(jdk.uri())
                    .sendAsync(pinned, java.net.http.HttpResponse.BodyHandlers.ofInputStream())
                    .handle((response, failure) -> {
                        if (failure != null) {
                            Throwable cause = failure instanceof CompletionException && failure.getCause() != null
                                    ? failure.getCause() : failure;
                            throw new CompletionException(cause instanceof IOException
                                    ? new AnthropicIoException("Request failed: " + cause.getClass().getSimpleName(), cause)
                                    : cause);
                        }
                        return (HttpResponse) new Response(response);
                    });
        } catch (IOException e) {
            return CompletableFuture.failedFuture(
                    new AnthropicIoException("Request failed: " + e.getClass().getSimpleName(), e));
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public void close() {
        // The pinned clients are shared and cached by PinnedHttp; nothing to release per SDK client.
    }

    /** The SDK request as a {@code java.net.http} request to the original URL. */
    static java.net.http.HttpRequest toJdk(HttpRequest request, RequestOptions options) {
        java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder(URI.create(request.url()))
                .timeout(timeout(options));
        Headers headers = request.headers();
        boolean contentType = false;
        for (String name : headers.names()) {
            String lower = name.toLowerCase(Locale.ROOT);
            if (RESTRICTED.contains(lower)) {
                continue;
            }
            contentType |= lower.equals("content-type");
            for (String value : headers.values(name)) {
                builder.header(name, value);
            }
        }
        HttpRequestBody body = request.body();
        java.net.http.HttpRequest.BodyPublisher publisher = java.net.http.HttpRequest.BodyPublishers.noBody();
        if (body != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            body.writeTo(out);
            publisher = java.net.http.HttpRequest.BodyPublishers.ofByteArray(out.toByteArray());
            if (!contentType && body.contentType() != null) {
                builder.header("Content-Type", body.contentType());
            }
        }
        builder.method(request.method().name(), publisher);
        return builder.build();
    }

    private static Duration timeout(RequestOptions options) {
        Timeout timeout = options == null ? null : options.getTimeout();
        Duration request = timeout == null ? null : timeout.request();
        return request == null || request.isZero() || request.isNegative() ? LlmHttp.ANTHROPIC_TIMEOUT : request;
    }

    /** A {@code java.net.http} response as the SDK's {@link HttpResponse}. */
    static final class Response implements HttpResponse {

        private final int status;
        private final Headers headers;
        private final InputStream body;

        Response(java.net.http.HttpResponse<InputStream> response) {
            this.status = response.statusCode();
            Headers.Builder builder = Headers.builder();
            for (Map.Entry<String, List<String>> entry : response.headers().map().entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }
                for (String value : entry.getValue()) {
                    builder.put(entry.getKey(), value);
                }
            }
            this.headers = builder.build();
            this.body = response.body() == null ? InputStream.nullInputStream() : response.body();
        }

        @Override
        public int statusCode() {
            return status;
        }

        @Override
        public Headers headers() {
            return headers;
        }

        @Override
        public InputStream body() {
            return body;
        }

        @Override
        public void close() {
            try {
                body.close();
            } catch (IOException e) {
                // The connection is discarded either way.
            }
        }
    }
}
