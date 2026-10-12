package ai.foresignal.notify.push;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * An address block such as {@code 192.168.1.0/24} or {@code fd12:3456::/48}, used by
 * {@code push.allowedPrivateHosts} (prompt 031) and by {@link PushAddressPolicy}'s block list.
 *
 * <p>A bare address literal is a single-address block ({@code /32} or {@code /128}). Literals are
 * parsed without DNS: only dotted-quad IPv4 and colon-hex IPv6 are accepted.
 *
 * @param network the network address (host bits cleared)
 * @param prefix the prefix length
 */
public record Cidr(InetAddress network, int prefix) {

    private static final Pattern IPV4 = Pattern.compile("^(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}$");
    private static final Pattern IPV6 = Pattern.compile("^[0-9a-fA-F:.]+$");

    /** The smallest IPv4 prefix {@code allowedPrivateHosts} accepts. */
    public static final int MIN_ALLOWED_V4_PREFIX = 8;

    /** The smallest IPv6 prefix {@code allowedPrivateHosts} accepts. */
    public static final int MIN_ALLOWED_V6_PREFIX = 32;

    /**
     * Parses {@code a.b.c.d/n}, {@code x::y/n} or a bare literal.
     *
     * @return the block, or empty when the text is not an address block
     */
    public static Optional<Cidr> parse(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        String value = text.strip();
        int slash = value.indexOf('/');
        String address = slash < 0 ? value : value.substring(0, slash);
        if (address.startsWith("[") && address.endsWith("]")) {
            address = address.substring(1, address.length() - 1);
        }
        Optional<InetAddress> literal = literal(address);
        if (literal.isEmpty()) {
            return Optional.empty();
        }
        int bits = literal.get().getAddress().length * 8;
        int prefix = bits;
        if (slash >= 0) {
            String p = value.substring(slash + 1);
            if (!p.matches("^\\d{1,3}$")) {
                return Optional.empty();
            }
            prefix = Integer.parseInt(p);
            if (prefix > bits) {
                return Optional.empty();
            }
        }
        return Optional.of(new Cidr(mask(literal.get(), prefix), prefix));
    }

    /** The address of a literal, without any DNS lookup; empty when {@code text} is not one. */
    public static Optional<InetAddress> literal(String text) {
        if (text == null) {
            return Optional.empty();
        }
        boolean v4 = IPV4.matcher(text).matches();
        boolean v6 = !v4 && text.indexOf(':') >= 0 && IPV6.matcher(text).matches();
        if (!v4 && !v6) {
            return Optional.empty();
        }
        try {
            // A literal never triggers a lookup.
            return Optional.of(InetAddress.getByName(text));
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }

    /** True when {@code address} lies in this block (IPv4 and IPv6 never match each other). */
    public boolean contains(InetAddress address) {
        if (address == null) {
            return false;
        }
        byte[] a = address.getAddress();
        byte[] n = network.getAddress();
        if (a.length != n.length) {
            return false;
        }
        int full = prefix / 8;
        for (int i = 0; i < full; i++) {
            if (a[i] != n[i]) {
                return false;
            }
        }
        int rest = prefix % 8;
        if (rest == 0) {
            return true;
        }
        int maskByte = (0xFF << (8 - rest)) & 0xFF;
        return (a[full] & maskByte) == (n[full] & maskByte);
    }

    /** True for an IPv4 block. */
    public boolean ipv4() {
        return network instanceof Inet4Address;
    }

    private static InetAddress mask(InetAddress address, int prefix) {
        byte[] bytes = address.getAddress().clone();
        for (int i = 0; i < bytes.length; i++) {
            int bitsHere = Math.max(0, Math.min(8, prefix - i * 8));
            bytes[i] = (byte) (bytes[i] & ((0xFF << (8 - bitsHere)) & 0xFF));
        }
        try {
            return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        return network.getHostAddress() + "/" + prefix;
    }
}
