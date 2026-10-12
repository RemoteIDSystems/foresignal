package ai.foresignal.notify.push;

import ai.foresignal.source.LocalAddresses;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Decides whether a push endpoint may be contacted (prompt 031): at subscription time and again
 * before every send.
 *
 * <p>The endpoint host is resolved and <em>every</em> address must be globally routable. Refused:
 * loopback, RFC 1918, carrier-grade NAT (100.64/10), link-local, unique-local (fc00::/7),
 * site-local, IPv4-mapped (the JDK hands those over as IPv4 addresses, judged as such) and
 * IPv4-compatible IPv6, NAT64 (64:ff9b::/96, 64:ff9b:1::/48), 6to4
 * (2002::/16), Teredo (2001::/32), multicast, 0.0.0.0/8, the reserved 240/4 block, the IETF and
 * benchmarking blocks, documentation ranges, and cloud metadata addresses ({@code LocalAddresses}
 * from prompt 004 classifies the private part). The one exception is the operator's
 * {@code push.allowedPrivateHosts}: an endpoint whose host is listed exactly, or whose every address
 * lies in a listed CIDR, may resolve to non-global addresses (a LAN UnifiedPush distributor).
 * Metadata addresses stay refused even then. There is no global "allow private" switch.
 *
 * <p>The returned {@link Decision#pinned()} address is the one the sender must connect to, so a
 * second DNS answer cannot redirect the request (DNS rebinding). Prompt 035 later replaces this
 * class with {@code OutboundUrlPolicy} profile {@code PUSH_ENDPOINT}.
 */
@Component
public class PushAddressPolicy {

    /** Cloud metadata addresses, refused even when allowlisted. */
    static final Set<String> METADATA = Set.of("169.254.169.254", "fd00:ec2:0:0:0:0:0:254", "100.100.100.200",
            "169.254.170.2");

    /** Blocks that are not globally routable beyond what {@link LocalAddresses} covers. */
    static final List<Cidr> BLOCKED = List.of(
            cidr("0.0.0.0/8"), cidr("100.64.0.0/10"), cidr("192.0.0.0/24"), cidr("192.0.2.0/24"),
            cidr("198.18.0.0/15"), cidr("198.51.100.0/24"), cidr("203.0.113.0/24"), cidr("224.0.0.0/4"),
            cidr("240.0.0.0/4"),
            cidr("::/96"), cidr("64:ff9b::/96"), cidr("64:ff9b:1::/48"), cidr("100::/64"),
            cidr("2001::/32"), cidr("2001:db8::/32"), cidr("2002::/16"), cidr("fc00::/7"), cidr("fe80::/10"),
            cidr("fec0::/10"), cidr("ff00::/8"));

    private final PushHostResolver resolver;

    /** Creates the policy. */
    public PushAddressPolicy(PushHostResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * The outcome of a check.
     *
     * @param allowed whether the endpoint may be contacted
     * @param host the endpoint host, lower case, without brackets
     * @param addresses every resolved address
     * @param pinned the address to connect to, or null when refused
     * @param reason why it was refused, or null
     */
    public record Decision(boolean allowed, String host, List<InetAddress> addresses, InetAddress pinned, String reason) {

        static Decision refuse(String host, List<InetAddress> addresses, String reason) {
            return new Decision(false, host, addresses, null, reason);
        }
    }

    /** Checks the host of {@code endpoint} against {@code allowedPrivateHosts}. */
    public Decision check(URI endpoint, List<String> allowedPrivateHosts) {
        String host = endpoint == null ? null : endpoint.getHost();
        return check(host, allowedPrivateHosts);
    }

    /** Checks {@code rawHost} against {@code allowedPrivateHosts}. */
    public Decision check(String rawHost, List<String> allowedPrivateHosts) {
        if (rawHost == null || rawHost.isBlank()) {
            return Decision.refuse(rawHost, List.of(), "the endpoint has no host");
        }
        String host = rawHost.toLowerCase(Locale.ROOT);
        if (host.startsWith("[") && host.endsWith("]")) {
            host = host.substring(1, host.length() - 1);
        }
        InetAddress[] resolved;
        try {
            resolved = resolver.resolve(host);
        } catch (UnknownHostException e) {
            return Decision.refuse(host, List.of(), "the host " + host + " does not resolve");
        }
        if (resolved == null || resolved.length == 0) {
            return Decision.refuse(host, List.of(), "the host " + host + " does not resolve");
        }
        List<InetAddress> addresses = List.copyOf(Arrays.asList(resolved));
        for (InetAddress address : addresses) {
            if (isMetadata(address)) {
                return Decision.refuse(host, addresses, "the host " + host + " resolves to the cloud metadata address "
                        + address.getHostAddress());
            }
        }
        List<String> allowed = allowedPrivateHosts == null ? List.of() : allowedPrivateHosts;
        if (hostAllowed(host, allowed) || everyAddressAllowed(addresses, allowed)) {
            return new Decision(true, host, addresses, addresses.getFirst(), null);
        }
        for (InetAddress address : addresses) {
            String reason = blockedReason(address);
            if (reason != null) {
                return Decision.refuse(host, addresses, "the host " + host + " resolves to " + address.getHostAddress()
                        + ", " + reason + "; add it to push.allowedPrivateHosts to use it");
            }
        }
        return new Decision(true, host, addresses, addresses.getFirst(), null);
    }

    /** Why {@code address} is not globally routable, or null when it is. */
    public static String blockedReason(InetAddress address) {
        if (address == null) {
            return "no address";
        }
        if (isMetadata(address)) {
            return "a cloud metadata address";
        }
        if (address.isLoopbackAddress()) {
            return "a loopback address";
        }
        if (address.isAnyLocalAddress()) {
            return "the unspecified address";
        }
        if (address.isMulticastAddress()) {
            return "a multicast address";
        }
        if (LocalAddresses.isCarrierGradeNat(address)) {
            return "a carrier-grade NAT address (100.64.0.0/10)";
        }
        if (address instanceof Inet6Address && isIpv4MappedOrCompatible(address.getAddress())) {
            return "an IPv4-mapped or IPv4-compatible IPv6 address";
        }
        if (LocalAddresses.isPrivate(address)) {
            return "a private, link-local or unique-local address";
        }
        for (Cidr block : BLOCKED) {
            if (block.contains(address)) {
                return "in the non-global block " + block;
            }
        }
        if (address instanceof Inet4Address && (address.getAddress()[0] & 0xFF) == 255) {
            return "a broadcast address";
        }
        return null;
    }

    static boolean hostAllowed(String host, List<String> allowed) {
        for (String entry : allowed) {
            String value = entry.strip().toLowerCase(Locale.ROOT);
            if (!value.contains("/") && Cidr.literal(value).isEmpty() && value.equals(host)) {
                return true;
            }
        }
        return false;
    }

    static boolean everyAddressAllowed(List<InetAddress> addresses, List<String> allowed) {
        List<Cidr> blocks = new ArrayList<>();
        for (String entry : allowed) {
            Cidr.parse(entry).ifPresent(blocks::add);
        }
        if (blocks.isEmpty() || addresses.isEmpty()) {
            return false;
        }
        for (InetAddress address : addresses) {
            if (blocks.stream().noneMatch(b -> b.contains(address))) {
                return false;
            }
        }
        return true;
    }

    static boolean isMetadata(InetAddress address) {
        InetAddress effective = address;
        byte[] bytes = address.getAddress();
        if (bytes.length == 16 && isIpv4MappedOrCompatible(bytes)) {
            try {
                effective = InetAddress.getByAddress(Arrays.copyOfRange(bytes, 12, 16));
            } catch (UnknownHostException e) {
                return true;
            }
        }
        return METADATA.contains(effective.getHostAddress());
    }

    private static boolean isIpv4MappedOrCompatible(byte[] bytes) {
        if (bytes.length != 16) {
            return false;
        }
        for (int i = 0; i < 10; i++) {
            if (bytes[i] != 0) {
                return false;
            }
        }
        boolean mapped = (bytes[10] & 0xFF) == 0xFF && (bytes[11] & 0xFF) == 0xFF;
        boolean compatible = bytes[10] == 0 && bytes[11] == 0;
        return mapped || compatible;
    }

    private static Cidr cidr(String text) {
        return Cidr.parse(text).orElseThrow(() -> new IllegalStateException("bad built-in CIDR " + text));
    }
}
