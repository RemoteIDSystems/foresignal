package ai.foresignal.source;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

/**
 * Decides whether an address belongs to the local network.
 *
 * <p>Prompt 004 used this for {@link ai.foresignal.source.spi.ParamType#LAN_URL} validation; since
 * prompt 035 that check is {@link ai.foresignal.net.OutboundUrlPolicy} profile {@code LAN_ONLY}, and
 * every outbound URL check goes through that policy. This class remains a classification helper for
 * non-outbound decisions, such as whether {@code publicBaseUrl} looks private
 * ({@code SystemWarnings}). Do not use it to vet a destination.
 */
public final class LocalAddresses {

    private LocalAddresses() {}

    /**
     * True for loopback, RFC 1918 private, link-local or IPv6 unique-local addresses.
     *
     * <p>Carrier-grade NAT (100.64/10) and IPv4-mapped IPv6 are handled too: the first is not the
     * operator's LAN, and the second would otherwise let {@code ::ffff:169.254.169.254} slip past an
     * IPv6-only check.
     */
    public static boolean isPrivate(InetAddress address) {
        if (address == null) {
            return false;
        }
        if (address.isLoopbackAddress() || address.isLinkLocalAddress() || address.isAnyLocalAddress()) {
            return true;
        }
        if (address instanceof Inet4Address) {
            return address.isSiteLocalAddress();
        }
        if (address instanceof Inet6Address ipv6) {
            // An IPv4-mapped address is really an IPv4 address; judge it as one.
            byte[] bytes = ipv6.getAddress();
            if (isIpv4Mapped(bytes)) {
                return isPrivateIpv4(bytes[12], bytes[13]);
            }
            // fc00::/7 — unique local.
            return (bytes[0] & 0xFE) == 0xFC;
        }
        return false;
    }

    /** The reason {@code address} is not acceptable, or null when it is. */
    public static String rejectionReason(InetAddress address) {
        if (address == null) {
            return "the host did not resolve to any address";
        }
        if (isCarrierGradeNat(address)) {
            return "resolves to "
                    + address.getHostAddress()
                    + ", which is carrier-grade NAT (100.64.0.0/10) rather than a local network";
        }
        if (!isPrivate(address)) {
            return "resolves to the public address "
                    + address.getHostAddress()
                    + "; only loopback, private (RFC 1918), link-local and unique-local addresses are"
                    + " allowed";
        }
        return null;
    }

    /** True for 100.64.0.0/10, which is routable-but-not-yours and must not count as local. */
    public static boolean isCarrierGradeNat(InetAddress address) {
        byte[] bytes = address.getAddress();
        if (bytes.length == 16 && isIpv4Mapped(bytes)) {
            return isCgnat(bytes[12], bytes[13]);
        }
        return bytes.length == 4 && isCgnat(bytes[0], bytes[1]);
    }

    private static boolean isCgnat(byte first, byte second) {
        return (first & 0xFF) == 100 && (second & 0xFF) >= 64 && (second & 0xFF) <= 127;
    }

    private static boolean isIpv4Mapped(byte[] bytes) {
        if (bytes.length != 16) {
            return false;
        }
        for (int i = 0; i < 10; i++) {
            if (bytes[i] != 0) {
                return false;
            }
        }
        return (bytes[10] & 0xFF) == 0xFF && (bytes[11] & 0xFF) == 0xFF;
    }

    private static boolean isPrivateIpv4(byte first, byte second) {
        int a = first & 0xFF;
        int b = second & 0xFF;
        return a == 10
                || a == 127
                || (a == 172 && b >= 16 && b <= 31)
                || (a == 192 && b == 168)
                || (a == 169 && b == 254);
    }
}
