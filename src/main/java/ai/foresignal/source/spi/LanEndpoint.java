package ai.foresignal.source.spi;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Locale;
import java.util.Objects;

/**
 * A validated {@link ParamType#LAN_URL} of scheme {@code tcp} or {@code udp} (prompt 069): where a socket
 * source connects or listens.
 *
 * <ul>
 *   <li><strong>{@code tcp://host:port}:</strong> {@code address} is the loopback, RFC 1918 or unique-local
 *       address the host resolved to when the parameter was validated. The transport connects to it and
 *       never resolves the name again (DNS rebinding).
 *   <li><strong>{@code udp://bindHost:port}:</strong> {@code address} is the local address to bind:
 *       {@code 0.0.0.0}, {@code ::} or an address of one of this machine's interfaces.
 * </ul>
 *
 * <p>The port is always explicit; no default is ever assumed. Only {@code ParamValidator} creates the
 * values this is built from, so an adapter cannot name an arbitrary host.
 *
 * @param scheme {@link Scheme#TCP} or {@link Scheme#UDP}
 * @param host the host as the operator wrote it, for messages
 * @param port the port, 1 to 65535
 * @param address the pinned address (TCP) or bind address (UDP)
 */
public record LanEndpoint(Scheme scheme, String host, int port, InetAddress address) {

    /** The socket schemes. */
    public enum Scheme {
        /** A TCP client connection to a receiver on the LAN. */
        TCP,
        /** A UDP listener bound on this machine. */
        UDP;

        /** The URL scheme, lower case. */
        public String wireValue() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /** Validates the members. */
    public LanEndpoint {
        Objects.requireNonNull(scheme, "scheme");
        Objects.requireNonNull(address, "address");
        if (port < 1 || port > 65_535) {
            throw new IllegalArgumentException("port must be 1 to 65535, but was " + port);
        }
        host = host == null || host.isBlank() ? address.getHostAddress() : host;
    }

    /**
     * The endpoint of a validated {@code tcp://} or {@code udp://} parameter.
     *
     * @throws IllegalStateException when the parameter is absent, has another scheme, or was not pinned
     *     (a validator bug rather than bad configuration)
     */
    public static LanEndpoint fromParams(ValidatedParams params, String name) {
        URI uri = params.lanUrl(name);
        if (uri == null || uri.getScheme() == null) {
            throw new IllegalStateException("Param '" + name + "' has no LAN URL");
        }
        Scheme scheme = switch (uri.getScheme().toLowerCase(Locale.ROOT)) {
            case "tcp" -> Scheme.TCP;
            case "udp" -> Scheme.UDP;
            default -> throw new IllegalStateException(
                    "Param '" + name + "' is a " + uri.getScheme() + " URL, not tcp or udp");
        };
        InetAddress address = params.pinnedAddress(name)
                .orElseThrow(() -> new IllegalStateException("Param '" + name + "' was not pinned"));
        return new LanEndpoint(scheme, uri.getHost(), uri.getPort(), address);
    }

    /** The socket address to connect to (TCP) or bind (UDP). */
    public InetSocketAddress socketAddress() {
        return new InetSocketAddress(address, port);
    }

    /** {@code scheme://host:port}, for logs; no address is resolved. */
    @Override
    public String toString() {
        String h = host.indexOf(':') >= 0 && !host.startsWith("[") ? "[" + host + "]" : host;
        return scheme.wireValue() + "://" + h + ":" + port;
    }
}
