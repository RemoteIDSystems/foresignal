package ai.foresignal.source.adapters.internet;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * An IPv4 or IPv6 prefix in CIDR form (prompt 092, {@code ris-live}), compared by bits rather than by text, so
 * {@code 192.168.10.0/24} is not taken for a more-specific of {@code 192.168.1.0/24} and IPv6 prefixes compare
 * correctly whatever their spelling.
 *
 * <p>Only address literals are accepted: the text is checked against a literal pattern before
 * {@link InetAddress#getByName} sees it, so no name is ever resolved.
 */
final class Cidr {

    private static final Pattern IPV4 = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3}");
    private static final Pattern IPV6 = Pattern.compile("[0-9A-Fa-f:.]*:[0-9A-Fa-f:.]*");

    private final byte[] network;
    private final int length;
    private final String text;

    private Cidr(byte[] network, int length, String text) {
        this.network = network;
        this.length = length;
        this.text = text;
    }

    /**
     * Parses {@code value} ({@code "193.0.0.0/21"}, {@code "2001:db8::/32"}), masking any host bits.
     *
     * @return the prefix, or empty when {@code value} is not a literal CIDR prefix
     */
    static Optional<Cidr> parse(String value) {
        if (value == null) {
            return Optional.empty();
        }
        String trimmed = value.strip();
        int slash = trimmed.indexOf('/');
        if (slash <= 0 || slash == trimmed.length() - 1) {
            return Optional.empty();
        }
        String address = trimmed.substring(0, slash);
        boolean v4 = IPV4.matcher(address).matches();
        if (!v4 && !IPV6.matcher(address).matches()) {
            return Optional.empty();
        }
        if (v4) {
            // An out-of-range octet is not a literal, and getByName would then try to resolve it as a name.
            for (String octet : address.split("\\.")) {
                if (Integer.parseInt(octet) > 255) {
                    return Optional.empty();
                }
            }
        }
        int length;
        try {
            length = Integer.parseInt(trimmed.substring(slash + 1));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        byte[] bytes;
        try {
            bytes = InetAddress.getByName(address).getAddress();
        } catch (UnknownHostException | SecurityException e) {
            return Optional.empty();
        }
        if (length < 0 || length > bytes.length * 8) {
            return Optional.empty();
        }
        byte[] masked = mask(bytes, length);
        String canonical;
        try {
            canonical = InetAddress.getByAddress(masked).getHostAddress() + "/" + length;
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
        return Optional.of(new Cidr(masked, length, canonical));
    }

    private static byte[] mask(byte[] bytes, int length) {
        byte[] out = bytes.clone();
        for (int i = 0; i < out.length; i++) {
            int bitsHere = Math.max(0, Math.min(8, length - i * 8));
            out[i] = (byte) (out[i] & (0xFF << (8 - bitsHere)));
        }
        return out;
    }

    /** True when {@code other} lies inside this prefix (same family, at least as long, same leading bits). */
    boolean contains(Cidr other) {
        if (other.network.length != network.length || other.length < length) {
            return false;
        }
        return Arrays.equals(mask(other.network, length), network);
    }

    /** True when {@code other} is a strictly more-specific prefix inside this one. */
    boolean strictlyContains(Cidr other) {
        return other.length > length && contains(other);
    }

    /** The prefix length. */
    int length() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Cidr other && other.length == length && Arrays.equals(other.network, network);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(network) + length;
    }

    /** The canonical text form ({@code 2001:db8:0:0:0:0:0:0/32} for IPv6, as the JDK spells it). */
    @Override
    public String toString() {
        return text;
    }
}
