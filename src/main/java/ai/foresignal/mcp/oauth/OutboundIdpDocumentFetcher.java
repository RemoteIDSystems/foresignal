package ai.foresignal.mcp.oauth;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.net.PinnedHttp;
import ai.foresignal.net.SpecialPurposeAddresses;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * {@link IdpDocumentFetcher} through the outbound URL policy (prompt 035): every URL is checked under
 * {@code LAN_ALLOWED} (an identity provider often runs next to Foresignal, such as {@code http://keycloak:8080}),
 * plain {@code http} is accepted only when every resolved address is loopback, private or shared (tailnet), and the
 * request goes to the checked address with the original {@code Host} and TLS name ({@link PinnedHttp}). Redirects
 * are not followed, no credentials are sent, and a body over {@link #MAX_BYTES} is refused.
 */
@Component
public class OutboundIdpDocumentFetcher implements IdpDocumentFetcher {

    /** The largest document read: 512 KB. */
    static final int MAX_BYTES = 512 * 1024;

    /** The connect and request timeout. */
    static final Duration TIMEOUT = Duration.ofSeconds(10);

    private static final PinnedHttp.ClientSpec CLIENT = new PinnedHttp.ClientSpec(TIMEOUT, HttpClient.Version.HTTP_1_1);

    private final OutboundUrlPolicy policy;
    private final PinnedHttp pinned;

    /** Creates the fetcher. */
    public OutboundIdpDocumentFetcher(OutboundUrlPolicy policy, PinnedHttp pinned) {
        this.policy = policy;
        this.pinned = pinned;
    }

    @Override
    public String fetch(URI uri) throws IOException {
        InetAddress address = check(policy, uri);
        HttpRequest.Builder builder = HttpRequest.newBuilder(PinnedHttp.pinnedUri(uri, address))
                .header("Accept", "application/json")
                .timeout(TIMEOUT)
                .GET();
        PinnedHttp.setHostHeader(builder, uri);
        HttpResponse<InputStream> response;
        try {
            response = pinned.client(uri, CLIENT).send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted while fetching from " + uri.getHost(), e);
        }
        try (InputStream body = response.body()) {
            if (response.statusCode() != 200) {
                throw new IOException(uri.getHost() + " answered " + response.statusCode() + " for " + uri.getPath());
            }
            byte[] bytes = body.readNBytes(MAX_BYTES + 1);
            if (bytes.length > MAX_BYTES) {
                throw new IOException(uri.getHost() + " sent more than " + MAX_BYTES + " bytes for " + uri.getPath());
            }
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    /**
     * Checks {@code uri} for an identity-provider fetch and returns the address to connect to (one resolution, so
     * the address checked is the address used).
     *
     * @throws IOException with the policy's reason when the URL is refused
     */
    static InetAddress check(OutboundUrlPolicy policy, URI uri) throws IOException {
        OutboundUrlPolicy.Result result = policy.check(uri, OutboundUrlPolicy.Profile.LAN_ALLOWED, false);
        if (result instanceof OutboundUrlPolicy.Denied denied) {
            throw new IOException("the outbound URL policy refuses " + uri.getHost() + ": " + denied.reason());
        }
        OutboundUrlPolicy.Allowed allowed = (OutboundUrlPolicy.Allowed) result;
        String plain = plainHttpProblem(uri, allowed);
        if (plain != null) {
            throw new IOException(plain);
        }
        return allowed.pinned();
    }

    /**
     * Why {@code uri} may not be used for an identity-provider document, or null when it may: the
     * {@code LAN_ALLOWED} check, and {@code http} only towards loopback, private or shared addresses (keys fetched
     * over plain http from a public host could be swapped in transit).
     *
     * @param saving true when the URL is being saved: a host that does not resolve now is accepted then
     */
    public static String problem(OutboundUrlPolicy policy, URI uri, boolean saving) {
        OutboundUrlPolicy.Result result = policy.check(uri, OutboundUrlPolicy.Profile.LAN_ALLOWED, false);
        if (result instanceof OutboundUrlPolicy.Denied denied) {
            return saving && denied.unresolved() ? null : denied.reason();
        }
        return plainHttpProblem(uri, (OutboundUrlPolicy.Allowed) result);
    }

    /** The refusal of a plain {@code http} URL that resolves to a public address, or null. */
    static String plainHttpProblem(URI uri, OutboundUrlPolicy.Allowed allowed) {
        boolean plain = uri.getScheme() != null && "http".equals(uri.getScheme().toLowerCase(Locale.ROOT));
        if (!plain) {
            return null;
        }
        for (InetAddress address : allowed.pinnedAddresses()) {
            if (SpecialPurposeAddresses.classify(address).addressClass()
                    == SpecialPurposeAddresses.AddressClass.GLOBAL) {
                return "plain http is accepted only for an identity provider on a local, private or tailnet address; "
                        + uri.getHost() + " resolves to the public address " + address.getHostAddress()
                        + ", so use https";
            }
        }
        return null;
    }
}
