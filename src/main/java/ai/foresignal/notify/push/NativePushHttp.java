package ai.foresignal.notify.push;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.SSLContext;
import org.springframework.stereotype.Component;

/**
 * The HTTP client of native push (prompt 032): APNs over HTTP/2 with TLS, the FCM HTTP v1 API and
 * Google's OAuth 2.0 token endpoint.
 *
 * <p>One {@link HttpClient} is kept per key (an APNs provider, the FCM host, a token endpoint), so
 * each APNs provider has its own HTTP/2 connection: APNs does not accept provider tokens of several
 * developer accounts on one connection. Clients ask for {@link HttpClient.Version#HTTP_2}; APNs
 * requires it, and a server without HTTP/2 (a test stand-in) falls back to HTTP/1.1. Redirects are
 * never followed. The destinations are Apple's and Google's fixed hosts (configurable only for
 * tests) and the token URI of an admin-configured provider, so no address policy applies here.
 */
@Component
public class NativePushHttp {

    /** The longest response body kept, in characters. */
    static final int MAX_BODY = 4096;

    /**
     * A response.
     *
     * @param status the HTTP status
     * @param headers the response headers
     * @param body the body as text, at most {@value #MAX_BODY} characters
     */
    public record Response(int status, Map<String, List<String>> headers, String body) {

        /** The first value of a header, case-insensitive. */
        public Optional<String> header(String name) {
            for (Map.Entry<String, List<String>> e : headers.entrySet()) {
                if (e.getKey() != null && e.getKey().equalsIgnoreCase(name) && !e.getValue().isEmpty()) {
                    return Optional.of(e.getValue().getFirst());
                }
            }
            return Optional.empty();
        }
    }

    private final NativePushTls tls;
    private final PushProperties properties;
    private final Map<String, HttpClient> clients = new ConcurrentHashMap<>();

    /** Creates the client holder. */
    public NativePushHttp(NativePushTls tls, PushProperties properties) {
        this.tls = tls;
        this.properties = properties;
    }

    /** The production TLS context: the JDK default (platform trust store, no pinning). */
    public static NativePushTls platformTls() {
        return SSLContext::getDefault;
    }

    /**
     * POSTs {@code body} to {@code uri} on the client kept under {@code clientKey}.
     *
     * @throws IOException when the connection fails
     */
    public Response post(String clientKey, URI uri, Map<String, String> headers, byte[] body) throws IOException {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri).timeout(properties.requestTimeout())
                .POST(HttpRequest.BodyPublishers.ofByteArray(body));
        headers.forEach(request::header);
        try {
            HttpResponse<byte[]> response = client(clientKey).send(request.build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            String text = response.body() == null ? "" : new String(response.body(), StandardCharsets.UTF_8);
            if (text.length() > MAX_BODY) {
                text = text.substring(0, MAX_BODY);
            }
            return new Response(response.statusCode(), response.headers().map(), text);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted", e);
        }
    }

    /** Drops the client kept under {@code clientKey} (its provider changed or was deleted). */
    public void evict(String clientKey) {
        HttpClient client = clients.remove(clientKey);
        if (client != null) {
            client.close();
        }
    }

    /** Drops every client (tests). */
    public void reset() {
        clients.keySet().forEach(this::evict);
    }

    private HttpClient client(String key) throws IOException {
        try {
            return clients.computeIfAbsent(key, k -> {
                try {
                    return HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_2)
                            .connectTimeout(properties.connectTimeout())
                            .followRedirects(HttpClient.Redirect.NEVER)
                            .sslContext(tls.context())
                            .build();
                } catch (GeneralSecurityException e) {
                    throw new IllegalStateException(e);
                }
            });
        } catch (IllegalStateException e) {
            throw new IOException("TLS is not available: " + e.getMessage(), e);
        }
    }
}
