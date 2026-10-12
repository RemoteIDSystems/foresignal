package ai.foresignal.inbound.edp;

import ai.foresignal.net.HostResolver;
import ai.foresignal.net.OutboundUrlPolicy;
import java.net.InetAddress;
import java.net.URI;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The security boundary for every provider URL: {@code manifestUrl}, {@code pull.itemsUrl},
 * {@code search.url} and media (docs/External-Data-Protocol.md §7, invariant EXT-6).
 *
 * <p>Since prompt 035 this delegates to {@link OutboundUrlPolicy}: profile {@code PUBLIC_ONLY}
 * (https only) by default, {@code LAN_ALLOWED} (http allowed) when the operator allowed private
 * networks for the source. The host is resolved <em>once</em>, every address must pass, and the caller
 * connects only to the returned pinned address (no DNS rebinding). Link-local, cloud metadata and the
 * other always-blocked addresses are refused even when private networks are allowed; the outbound
 * overrides ({@code app_setting['outbound']}) apply.
 */
@Component
public class EdpUrlGuard {

    /**
     * A vetted URL and the address to connect to.
     *
     * @param uri the URL as given
     * @param host its host, lower case, without IPv6 brackets
     * @param port the effective port
     * @param address the vetted address the connection must use
     */
    public record VettedUrl(URI uri, String host, int port, InetAddress address) {}

    private final OutboundUrlPolicy policy;

    /** A guard over the application's outbound URL policy. */
    @Autowired
    public EdpUrlGuard(OutboundUrlPolicy policy) {
        this.policy = policy;
    }

    /** A guard over a policy without overrides that resolves through {@code resolver}, for tests. */
    EdpUrlGuard(HostResolver resolver) {
        this(OutboundUrlPolicy.withoutOverrides(resolver));
    }

    /** The profile of a source's provider URLs. */
    static OutboundUrlPolicy.Profile profile(boolean allowPrivateNetwork) {
        return allowPrivateNetwork ? OutboundUrlPolicy.Profile.LAN_ALLOWED : OutboundUrlPolicy.Profile.PUBLIC_ONLY;
    }

    /**
     * Vets {@code uri}.
     *
     * @param allowPrivateNetwork true when the operator allowed private networks for this source
     * @return the URL with the pinned address
     * @throws EdpUrlRefusedException with the reason when the URL may not be fetched
     */
    public VettedUrl vet(URI uri, boolean allowPrivateNetwork) throws EdpUrlRefusedException {
        if (uri == null || !uri.isAbsolute()) {
            throw new EdpUrlRefusedException("A provider URL must be absolute");
        }
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("https") && !scheme.equals("http")) {
            throw new EdpUrlRefusedException("A provider URL must be http or https, not " + scheme);
        }
        if (scheme.equals("http") && !allowPrivateNetwork) {
            throw new EdpUrlRefusedException("A provider URL must use https unless private networks are allowed"
                    + " for this source");
        }
        OutboundUrlPolicy.Result result = policy.check(uri, profile(allowPrivateNetwork));
        if (result instanceof OutboundUrlPolicy.Denied denied) {
            String hint = !allowPrivateNetwork && !denied.unresolved() && denied.reason().contains("PUBLIC_ONLY")
                    ? "; allow private networks for this source to use it" : "";
            throw new EdpUrlRefusedException("A provider URL is not allowed: " + denied.reason() + hint);
        }
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        if (host.startsWith("[") && host.endsWith("]")) {
            host = host.substring(1, host.length() - 1);
        }
        int port = uri.getPort() > 0 ? uri.getPort() : scheme.equals("https") ? 443 : 80;
        return new VettedUrl(uri, host, port, ((OutboundUrlPolicy.Allowed) result).pinned());
    }

    /**
     * True when a redirect from {@code from} to {@code to} stays on the same host (scheme changes
     * are judged by {@link #vet} on the next hop, which applies the policy again).
     */
    static boolean sameHost(URI from, URI to) {
        return from.getHost() != null && to.getHost() != null
                && from.getHost().equalsIgnoreCase(to.getHost());
    }
}
