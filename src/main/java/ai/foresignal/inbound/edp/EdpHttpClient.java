package ai.foresignal.inbound.edp;

import ai.foresignal.collect.UpstreamClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import org.springframework.stereotype.Component;

/**
 * Fetches provider URLs through the {@link EdpUrlGuard} (docs/External-Data-Protocol.md §7,
 * invariant EXT-6).
 *
 * <p>The host is resolved once by the guard and the connection goes only to that pinned address,
 * so a second DNS answer can never redirect it (DNS rebinding). For {@code http} the URI's host is
 * replaced by the address and the original {@code Host} header is sent. For {@code https} the client
 * connects to the address with SNI set to the original host and checks the certificate chain with the
 * platform trust store and the certificate's DNS names against the original host (RFC 6125).
 * Redirects are followed manually, at most three, to the same host only, and each hop is vetted
 * again. Bodies are capped; connect and request timeouts apply.
 */
@Component
public class EdpHttpClient {

    /** Redirects followed per request. */
    public static final int MAX_REDIRECTS = 3;

    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    private static final Set<Integer> REDIRECTS = Set.of(301, 302, 303, 307, 308);

    static {
        UpstreamClient.allowHostHeaderForPinnedRequests();
    }

    private final EdpUrlGuard guard;
    private final EdpProperties properties;
    private final HttpClient plain = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(CONNECT_TIMEOUT)
            .build();

    /** Creates the client. */
    public EdpHttpClient(EdpUrlGuard guard, EdpProperties properties) {
        this.guard = guard;
        this.properties = properties;
    }

    /**
     * A response from a provider.
     *
     * @param status the HTTP status (below 400; errors are thrown)
     * @param finalUri the URL that answered, after redirects
     * @param headers the response headers
     * @param body the body, at most the requested cap
     */
    public record EdpHttpResponse(int status, URI finalUri, Map<String, List<String>> headers, byte[] body) {

        /** The body decoded as UTF-8. */
        public String bodyText() {
            return new String(body, StandardCharsets.UTF_8);
        }

        /** The first value of a header (case-insensitive), or null. */
        public String header(String name) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(name) && !entry.getValue().isEmpty()) {
                    return entry.getValue().get(0);
                }
            }
            return null;
        }
    }

    /**
     * GETs {@code uri}.
     *
     * @param headers extra request headers (credentials, {@code Accept}); never logged
     * @param allowPrivateNetwork true when the operator allowed private networks for this source
     * @param maxBytes the body cap
     * @throws EdpFetchException when the URL is refused, unreachable, redirects elsewhere or too
     *     often, answers 400 or more (with {@link EdpFetchException#status()}), or sends more than
     *     {@code maxBytes}
     */
    public EdpHttpResponse get(URI uri, Map<String, String> headers, boolean allowPrivateNetwork, long maxBytes)
            throws EdpFetchException {
        URI current = uri;
        for (int hop = 0; ; hop++) {
            EdpUrlGuard.VettedUrl vetted;
            try {
                vetted = guard.vet(current, allowPrivateNetwork);
            } catch (EdpUrlRefusedException e) {
                throw new EdpFetchException(e.getMessage(), e);
            }
            HttpResponse<InputStream> response = send(vetted, headers);
            int status = response.statusCode();
            if (REDIRECTS.contains(status)) {
                String location = response.headers().firstValue("Location").orElse(null);
                close(response);
                if (location == null) {
                    throw new EdpFetchException("Redirect " + status + " without a Location", status);
                }
                if (hop >= MAX_REDIRECTS) {
                    throw new EdpFetchException("More than " + MAX_REDIRECTS + " redirects", status);
                }
                URI next = current.resolve(location);
                if (!EdpUrlGuard.sameHost(current, next)) {
                    throw new EdpFetchException("A redirect to another host (" + next.getHost() + ") was refused",
                            status);
                }
                current = next;
                continue;
            }
            byte[] body = read(response, maxBytes);
            if (status >= 400) {
                String snippet = new String(body, 0, Math.min(body.length, 500), StandardCharsets.UTF_8).strip();
                throw new EdpFetchException("The provider answered HTTP " + status
                        + (snippet.isEmpty() ? "" : ": " + snippet), status);
            }
            return new EdpHttpResponse(status, current, response.headers().map(), body);
        }
    }

    private HttpResponse<InputStream> send(EdpUrlGuard.VettedUrl vetted, Map<String, String> headers)
            throws EdpFetchException {
        URI original = vetted.uri();
        boolean https = "https".equalsIgnoreCase(original.getScheme());
        URI pinned = pinnedUri(original, vetted.address());
        HttpRequest.Builder request = HttpRequest.newBuilder(pinned)
                .GET()
                .timeout(properties.manifestFetchTimeout());
        String hostHeader = hostHeader(original);
        try {
            request.header("Host", hostHeader);
        } catch (IllegalArgumentException e) {
            // The JVM refused the restricted header; the pinned address is the protection, and the
            // TLS name check still uses the original host.
        }
        if (headers != null) {
            headers.forEach(request::header);
        }
        HttpClient client = https ? tlsClient(vetted.host()) : plain;
        try {
            return client.send(request.build(), HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException e) {
            throw new EdpFetchException("Could not reach the provider at " + original.getHost() + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EdpFetchException("Interrupted while fetching " + original.getHost(), e);
        }
    }

    /** {@code original} with its host replaced by the literal of {@code address}. */
    static URI pinnedUri(URI original, InetAddress address) throws EdpFetchException {
        String literal = address instanceof Inet6Address ? "[" + address.getHostAddress() + "]" : address.getHostAddress();
        String path = original.getRawPath() == null || original.getRawPath().isEmpty() ? "/" : original.getRawPath();
        String text = original.getScheme() + "://" + literal + (original.getPort() > 0 ? ":" + original.getPort() : "")
                + path + (original.getRawQuery() == null ? "" : "?" + original.getRawQuery());
        try {
            return new URI(text);
        } catch (URISyntaxException e) {
            throw new EdpFetchException("Could not pin " + original.getHost() + ": " + e.getMessage(), e);
        }
    }

    private static String hostHeader(URI original) {
        String host = original.getHost();
        return original.getPort() > 0 ? host + ":" + original.getPort() : host;
    }

    private HttpClient tlsClient(String host) throws EdpFetchException {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] {new HostCheckingTrustManager(defaultTrustManager(), host)}, null);
            SSLParameters parameters = new SSLParameters();
            if (!isIpLiteral(host)) {
                parameters.setServerNames(List.of(new SNIHostName(host)));
            }
            return HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .connectTimeout(CONNECT_TIMEOUT)
                    .sslContext(context)
                    .sslParameters(parameters)
                    .build();
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new EdpFetchException("Could not set up TLS for " + host + ": " + e.getMessage(), e);
        }
    }

    private static X509ExtendedTrustManager defaultTrustManager() throws GeneralSecurityException {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init((KeyStore) null);
        for (TrustManager manager : factory.getTrustManagers()) {
            if (manager instanceof X509ExtendedTrustManager extended) {
                return extended;
            }
        }
        throw new GeneralSecurityException("No X509ExtendedTrustManager in the platform trust store");
    }

    private static byte[] read(HttpResponse<InputStream> response, long maxBytes) throws EdpFetchException {
        try (InputStream in = response.body()) {
            byte[] body = in.readNBytes((int) Math.min(Integer.MAX_VALUE - 8, maxBytes + 1));
            if (body.length > maxBytes) {
                throw new EdpFetchException("The provider's response is larger than " + maxBytes + " bytes", (Integer) null);
            }
            return body;
        } catch (IOException e) {
            throw new EdpFetchException("Could not read the provider's response: " + e.getMessage(), e);
        }
    }

    private static void close(HttpResponse<InputStream> response) {
        try {
            response.body().close();
        } catch (IOException e) {
            // Nothing to do: the connection is discarded either way.
        }
    }

    static boolean isIpLiteral(String host) {
        return host.indexOf(':') >= 0 || host.matches("^\\d{1,3}(\\.\\d{1,3}){3}$");
    }

    /**
     * True when the leaf certificate names {@code host} (RFC 6125): a DNS subject alternative name
     * equal to it (case-insensitive), or a wildcard in the left-most label only covering exactly one
     * label; an IP literal host must match an IP subject alternative name.
     */
    static boolean certificateMatches(X509Certificate certificate, String host) {
        Collection<List<?>> names;
        try {
            names = certificate.getSubjectAlternativeNames();
        } catch (CertificateParsingException e) {
            return false;
        }
        if (names == null) {
            return false;
        }
        boolean ip = isIpLiteral(host);
        for (List<?> entry : names) {
            if (entry.size() < 2 || !(entry.get(0) instanceof Integer type) || !(entry.get(1) instanceof String value)) {
                continue;
            }
            if (ip && type == 7 && ipEquals(value, host)) {
                return true;
            }
            if (!ip && type == 2 && dnsNameMatches(value, host)) {
                return true;
            }
        }
        return false;
    }

    /** RFC 6125 DNS name matching with a left-most-label wildcard. */
    static boolean dnsNameMatches(String pattern, String host) {
        String p = pattern.toLowerCase(Locale.ROOT);
        String h = host.toLowerCase(Locale.ROOT);
        if (p.endsWith(".")) {
            p = p.substring(0, p.length() - 1);
        }
        if (h.endsWith(".")) {
            h = h.substring(0, h.length() - 1);
        }
        if (!p.startsWith("*.")) {
            return p.equals(h);
        }
        String suffix = p.substring(1);
        if (suffix.indexOf('*') >= 0 || suffix.chars().filter(c -> c == '.').count() < 2) {
            // "*.com" style patterns and further wildcards are never accepted.
            return false;
        }
        int dot = h.indexOf('.');
        return dot > 0 && h.substring(dot).equals(suffix);
    }

    private static boolean ipEquals(String a, String b) {
        try {
            return InetAddress.getByName(a).equals(InetAddress.getByName(b));
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Validates the chain with the platform trust manager, then the leaf's names against the
     * original host (the connection itself targets the pinned address).
     */
    static final class HostCheckingTrustManager extends X509ExtendedTrustManager {

        private final X509TrustManager delegate;
        private final String host;

        HostCheckingTrustManager(X509TrustManager delegate, String host) {
            this.delegate = delegate;
            this.host = host;
        }

        private void checkServer(X509Certificate[] chain, String authType) throws CertificateException {
            delegate.checkServerTrusted(chain, authType);
            if (chain == null || chain.length == 0 || !certificateMatches(chain[0], host)) {
                throw new CertificateException("The provider's certificate does not name " + host);
            }
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            checkServer(chain, authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
                throws CertificateException {
            checkServer(chain, authType);
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
                throws CertificateException {
            checkServer(chain, authType);
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            throw new CertificateException("Client certificates are not accepted");
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
                throws CertificateException {
            throw new CertificateException("Client certificates are not accepted");
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
                throws CertificateException {
            throw new CertificateException("Client certificates are not accepted");
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return delegate.getAcceptedIssuers();
        }
    }
}
