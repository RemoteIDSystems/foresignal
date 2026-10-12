package ai.foresignal.net;

import ai.foresignal.collect.UpstreamClient;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Connect-time pinning for {@code java.net.http} senders (prompt 035): the technique of the pinned-IP
 * connector of prompt 005 ({@code BoundSourceHttp.PinnedTarget}), the EDP client of 024 and the Web
 * Push sender of 031, shared by the channel sender and the LLM provider clients.
 *
 * <p>The request URI's host is replaced by the address {@link OutboundUrlPolicy} checked and the
 * original {@code Host} header is sent. For {@code https} the TLS SNI is the original host and
 * {@link PinnedTls} checks the certificate against it. Clients never follow redirects. One client is
 * kept per scheme, host and settings, so connections are reused without being shared across hosts.
 */
@Component
public class PinnedHttp {

    private static final Logger log = LoggerFactory.getLogger(PinnedHttp.class);

    static {
        UpstreamClient.allowHostHeaderForPinnedRequests();
    }

    /**
     * The settings of a pinned client.
     *
     * @param connectTimeout the connect timeout
     * @param version the preferred HTTP version; use {@code HTTP_1_1}, because over HTTP/2 the JDK
     *     takes {@code :authority} from the pinned (literal) URI instead of the {@code Host} header
     */
    public record ClientSpec(Duration connectTimeout, HttpClient.Version version) {}

    private final PinnedTls tls;
    private final Map<String, HttpClient> clients = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /** Creates the factory. */
    public PinnedHttp(PinnedTls tls) {
        this.tls = tls;
    }

    /**
     * The client for requests to {@code original}'s host: redirects off, and for {@code https} the
     * TLS name check and SNI of that host.
     *
     * @throws IOException when TLS cannot be set up
     */
    public HttpClient client(URI original, ClientSpec spec) throws IOException {
        boolean https = "https".equalsIgnoreCase(original.getScheme());
        String host = host(original);
        String key = (https ? "https://" + host : "http://") + "|" + spec.connectTimeout().toMillis() + "|"
                + spec.version();
        HttpClient existing = clients.get(key);
        if (existing != null) {
            return existing;
        }
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(spec.version())
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(spec.connectTimeout())
                .executor(executor);
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

    /**
     * {@code request} copied to connect to {@code address}, with the original {@code Host}.
     *
     * @throws IOException when the URI cannot be rewritten
     */
    public static HttpRequest pin(HttpRequest request, InetAddress address) throws IOException {
        URI original = request.uri();
        HttpRequest.Builder builder = HttpRequest.newBuilder(request, (name, value) -> !"host".equalsIgnoreCase(name))
                .uri(pinnedUri(original, address));
        setHostHeader(builder, original);
        return builder.build();
    }

    /** Sets {@code Host} to {@code original}'s authority, or carries on without it (see below). */
    public static void setHostHeader(HttpRequest.Builder builder, URI original) {
        try {
            builder.header("Host", hostHeader(original));
        } catch (IllegalArgumentException e) {
            // The JVM read its restricted-header property before Foresignal set it. The pinned address
            // (the protection) and the TLS name check still hold; only multi-name receivers notice.
            log.debug("Could not send Host for {}; connecting to the pinned address regardless", original.getHost());
        }
    }

    /** {@code original} with its host replaced by the literal of {@code address}. */
    public static URI pinnedUri(URI original, InetAddress address) throws IOException {
        String literal = address instanceof Inet6Address ? "[" + address.getHostAddress() + "]" : address.getHostAddress();
        String path = original.getRawPath() == null || original.getRawPath().isEmpty() ? "/" : original.getRawPath();
        String text = original.getScheme() + "://" + literal + (original.getPort() > 0 ? ":" + original.getPort() : "")
                + path + (original.getRawQuery() == null ? "" : "?" + original.getRawQuery());
        try {
            return new URI(text);
        } catch (URISyntaxException e) {
            throw new IOException("could not pin " + original.getHost() + ": " + e.getMessage(), e);
        }
    }

    /** The {@code Host} header of {@code original}: its host, plus the port when one is given. */
    public static String hostHeader(URI original) {
        String host = original.getHost();
        return original.getPort() > 0 ? host + ":" + original.getPort() : host;
    }

    /** Forgets the cached clients (tests that change TLS or DNS between cases). */
    public void reset() {
        clients.clear();
    }

    private static String host(URI uri) {
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        return host.startsWith("[") && host.endsWith("]") ? host.substring(1, host.length() - 1) : host;
    }
}
