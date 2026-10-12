package ai.foresignal.inbound;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * An IPv4 or IPv6 CIDR block, for a custom source's {@code ingestAllowCidrs} (prompt 039).
 *
 * <p>Parsing accepts only address literals (never host names, so it never resolves DNS). A bare
 * address is a single-host block ({@code /32} or {@code /128}). An IPv4-mapped IPv6 sender
 * ({@code ::ffff:a.b.c.d}) is matched as its IPv4 address.
 *
 * @param network the network address, with host bits cleared
 * @param prefix the prefix length
 */
public record Cidr(byte[] network, int prefix) {

    private static final Pattern LITERAL = Pattern.compile("^[0-9A-Fa-f:.]+$");

    /**
     * Parses {@code text} ({@code 192.168.1.0/24}, {@code fd00::/8}, {@code 10.0.0.7}).
     *
     * @throws IllegalArgumentException when it is not a CIDR block
     */
    public static Cidr parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("empty");
        }
        String t = text.strip();
        int slash = t.indexOf('/');
        String address = slash < 0 ? t : t.substring(0, slash);
        byte[] bytes = literal(address);
        if (bytes == null) {
            throw new IllegalArgumentException("not an IP address literal");
        }
        int max = bytes.length * 8;
        int prefix = max;
        if (slash >= 0) {
            try {
                prefix = Integer.parseInt(t.substring(slash + 1));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("bad prefix length");
            }
            if (prefix < 0 || prefix > max) {
                throw new IllegalArgumentException("prefix length must be 0 to " + max);
            }
        }
        return new Cidr(mask(bytes, prefix), prefix);
    }

    /** True when {@code address} (an IP literal; anything else is false) lies inside this block. */
    public boolean contains(String address) {
        byte[] bytes = literal(address);
        if (bytes == null) {
            return false;
        }
        if (bytes.length == 16 && network.length == 4 && isIpv4Mapped(bytes)) {
            bytes = Arrays.copyOfRange(bytes, 12, 16);
        }
        if (bytes.length != network.length) {
            return false;
        }
        return Arrays.equals(mask(bytes, prefix), network);
    }

    /** The bytes of an IP literal, or null when {@code text} is not one. */
    static byte[] literal(String text) {
        if (text == null) {
            return null;
        }
        String t = text.strip();
        if (t.startsWith("[") && t.endsWith("]")) {
            t = t.substring(1, t.length() - 1);
        }
        int zone = t.indexOf('%');
        if (zone >= 0) {
            t = t.substring(0, zone);
        }
        if (t.isEmpty() || !LITERAL.matcher(t).matches()) {
            return null;
        }
        if (!t.contains(":") && (!t.matches("^[0-9]{1,3}(\\.[0-9]{1,3}){3}$"))) {
            // Never let a dotted name reach the resolver.
            return null;
        }
        try {
            return InetAddress.getByName(t).getAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private static boolean isIpv4Mapped(byte[] bytes) {
        for (int i = 0; i < 10; i++) {
            if (bytes[i] != 0) {
                return false;
            }
        }
        return (bytes[10] & 0xFF) == 0xFF && (bytes[11] & 0xFF) == 0xFF;
    }

    private static byte[] mask(byte[] bytes, int prefix) {
        byte[] out = bytes.clone();
        for (int i = 0; i < out.length; i++) {
            int bits = Math.max(0, Math.min(8, prefix - i * 8));
            out[i] = (byte) (out[i] & (bits == 0 ? 0 : (0xFF << (8 - bits))));
        }
        return out;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Cidr c && c.prefix == prefix && Arrays.equals(c.network, network);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(network) + prefix;
    }

    /** The canonical text form, {@code address/prefix}. */
    @Override
    public String toString() {
        try {
            return InetAddress.getByAddress(network).getHostAddress() + "/" + prefix;
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
    }
}
