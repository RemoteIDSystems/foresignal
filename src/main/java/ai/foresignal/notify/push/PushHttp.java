package ai.foresignal.notify.push;

import ai.foresignal.collect.UpstreamClient;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import org.springframework.stereotype.Component;

/**
 * Sends one request to a push service over a connection pinned to the address the
 * {@link PushAddressPolicy} just checked (prompt 031), the same technique as the pinned-IP
 * connector of prompt 005 and the EDP client of 024.
 *
 * <p>The URI's host is replaced by the checked address and the original {@code Host} header is
 * sent; for {@code https} the TLS SNI is the original host and {@link PushTls} checks the
 * certificate against it. Redirects are never followed. One {@link HttpClient} is kept per host so
 * connections are reused without ever being shared across hosts with different names.
 */
@Component
public class PushHttp {

    static {
        UpstreamClient.allowHostHeaderForPinnedRequests();
    }

    /**
     * A push service response.
     *
     * @param status the HTTP status
     * @param headers the response headers
     * @param body the body, at most 2 KB, as text
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

    private final PushTls tls;
    private final PushProperties properties;
    private final Map<String, HttpClient> clients = new ConcurrentHashMap<>();

    /** Creates the sender. */
    public PushHttp(PushTls tls, PushProperties properties) {
        this.tls = tls;
        this.properties = properties;
    }

    /**
     * POSTs {@code body} to {@code endpoint}, connecting to {@code pinned}.
     *
     * @throws IOException when the connection or TLS handshake fails
     */
    public Response post(URI endpoint, InetAddress pinned, Map<String, String> headers, byte[] body) throws IOException {
        String host = endpoint.getHost().toLowerCase(Locale.ROOT);
        if (host.startsWith("[") && host.endsWith("]")) {
            host = host.substring(1, host.length() - 1);
        }
        HttpRequest.Builder request = HttpRequest.newBuilder(pinnedUri(endpoint, pinned))
                .timeout(properties.requestTimeout())
                .POST(HttpRequest.BodyPublishers.ofByteArray(body));
        try {
            request.header("Host", endpoint.getPort() > 0 ? host + ":" + endpoint.getPort() : host);
        } catch (IllegalArgumentException e) {
            // The JVM refused the restricted header; the pinned address and the TLS name check still hold.
        }
        headers.forEach(request::header);
        HttpClient client = client(host, "https".equalsIgnoreCase(endpoint.getScheme()));
        try {
            HttpResponse<byte[]> response = client.send(request.build(), HttpResponse.BodyHandlers.ofByteArray());
            byte[] bytes = response.body() == null ? new byte[0] : response.body();
            String text = new String(bytes, 0, Math.min(bytes.length, 2048), java.nio.charset.StandardCharsets.UTF_8);
            return new Response(response.statusCode(), response.headers().map(), text);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted", e);
        }
    }

    /** Forgets the cached clients (tests that change TLS or DNS between cases). */
    public void reset() {
        clients.clear();
    }

    private HttpClient client(String host, boolean https) throws IOException {
        String key = (https ? "https://" : "http://") + host;
        HttpClient existing = clients.get(key);
        if (existing != null) {
            return existing;
        }
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(properties.connectTimeout());
        if (https) {
            try {
                builder.sslContext(tls.context(host));
            } catch (GeneralSecurityException e) {
                throw new IOException("could not set up TLS for " + host + ": " + e.getMessage(), e);
            }
            SSLParameters parameters = new SSLParameters();
            if (Cidr.literal(host).isEmpty()) {
                parameters.setServerNames(List.of(new SNIHostName(host)));
            }
            builder.sslParameters(parameters);
        }
        HttpClient client = builder.build();
        HttpClient raced = clients.putIfAbsent(key, client);
        return raced == null ? client : raced;
    }

    /** {@code original} with its host replaced by the literal of {@code address}. */
    static URI pinnedUri(URI original, InetAddress address) throws IOException {
        String literal = address instanceof Inet6Address ? "[" + address.getHostAddress() + "]" : address.getHostAddress();
        String path = original.getRawPath() == null || original.getRawPath().isEmpty() ? "/" : original.getRawPath();
        String text = original.getScheme() + "://" + literal + (original.getPort() > 0 ? ":" + original.getPort() : "")
                + path + (original.getRawQuery() == null ? "" : "?" + original.getRawQuery());
        try {
            return new URI(text);
        } catch (URISyntaxException e) {
            throw new IOException("could not pin the endpoint: " + e.getMessage(), e);
        }
    }
}
