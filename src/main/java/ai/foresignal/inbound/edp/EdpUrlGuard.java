package ai.foresignal.inbound.edp;

import ai.foresignal.source.LocalAddresses;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The security boundary for every provider URL: {@code manifestUrl}, {@code pull.itemsUrl},
 * {@code search.url} and media (docs/External-Data-Protocol.md §7, invariant EXT-6).
 *
 * <p>Rules: absolute {@code http} or {@code https} without user info; {@code https} unless the source
 * allows private networks; the host is resolved <em>once</em> and every address must be acceptable,
 * and the caller connects only to the returned pinned address (no DNS rebinding). Loopback,
 * link-local, private (RFC 1918, unique-local), carrier-grade NAT, unspecified and multicast
 * addresses are refused unless the operator allowed private networks for that source; cloud
 * metadata addresses are refused always.
 */
@Component
public class EdpUrlGuard {

    /** Cloud metadata endpoints, refused even when private networks are allowed. */
    static final Set<String> METADATA = Set.of("169.254.169.254", "fd00:ec2:0:0:0:0:0:254", "100.100.100.200");

    /** Resolves a host name to its addresses; replaceable in tests. */
    @FunctionalInterface
    public interface HostResolver {

        /** All addresses of {@code host}. */
        InetAddress[] resolve(String host) throws UnknownHostException;
    }

    /**
     * A vetted URL and the address to connect to.
     *
     * @param uri the URL as given
     * @param host its host, lower case, without IPv6 brackets
     * @param port the effective port
     * @param address the vetted address the connection must use
     */
    public record VettedUrl(URI uri, String host, int port, InetAddress address) {}

    private final HostResolver resolver;

    /** A guard using the system resolver. */
    @Autowired
    public EdpUrlGuard() {
        this(InetAddress::getAllByName);
    }

    /** A guard with an explicit resolver, for tests. */
    EdpUrlGuard(HostResolver resolver) {
        this.resolver = resolver;
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
        if (uri.getRawUserInfo() != null) {
            throw new EdpUrlRefusedException("A provider URL must not carry user info");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new EdpUrlRefusedException("A provider URL needs a host");
        }
        host = host.toLowerCase(Locale.ROOT);
        if (host.startsWith("[") && host.endsWith("]")) {
            host = host.substring(1, host.length() - 1);
        }
        InetAddress[] addresses;
        try {
            addresses = resolver.resolve(host);
        } catch (UnknownHostException e) {
            throw new EdpUrlRefusedException("The host " + host + " does not resolve");
        }
        if (addresses == null || addresses.length == 0) {
            throw new EdpUrlRefusedException("The host " + host + " does not resolve");
        }
        for (InetAddress address : addresses) {
            String reason = refusal(address, allowPrivateNetwork);
            if (reason != null) {
                throw new EdpUrlRefusedException("The host " + host + " " + reason);
            }
        }
        int port = uri.getPort() > 0 ? uri.getPort() : scheme.equals("https") ? 443 : 80;
        return new VettedUrl(uri, host, port, addresses[0]);
    }

    /**
     * True when a redirect from {@code from} to {@code to} stays on the same host (scheme changes
     * are judged by {@link #vet} on the next hop).
     */
    static boolean sameHost(URI from, URI to) {
        return from.getHost() != null && to.getHost() != null
                && from.getHost().equalsIgnoreCase(to.getHost());
    }

    /** Why {@code address} is refused, or null when it is acceptable. */
    static String refusal(InetAddress address, boolean allowPrivateNetwork) {
        if (isMetadata(address)) {
            return "resolves to the cloud metadata address " + address.getHostAddress();
        }
        if (allowPrivateNetwork) {
            return null;
        }
        if (address.isAnyLocalAddress() || address.isMulticastAddress()) {
            return "resolves to the non-routable address " + address.getHostAddress();
        }
        if (LocalAddresses.isCarrierGradeNat(address)) {
            return "resolves to the carrier-grade NAT address " + address.getHostAddress();
        }
        if (LocalAddresses.isPrivate(address)) {
            return "resolves to the private, loopback or link-local address " + address.getHostAddress()
                    + "; allow private networks for this source to use it";
        }
        return null;
    }

    private static boolean isMetadata(InetAddress address) {
        InetAddress effective = address;
        if (address instanceof Inet6Address) {
            byte[] bytes = address.getAddress();
            boolean mapped = true;
            for (int i = 0; i < 10; i++) {
                mapped &= bytes[i] == 0;
            }
            mapped &= (bytes[10] & 0xFF) == 0xFF && (bytes[11] & 0xFF) == 0xFF;
            if (mapped) {
                try {
                    effective = Inet4Address.getByAddress(new byte[] {bytes[12], bytes[13], bytes[14], bytes[15]});
                } catch (UnknownHostException e) {
                    return true;
                }
            }
        }
        return METADATA.contains(effective.getHostAddress());
    }
}
