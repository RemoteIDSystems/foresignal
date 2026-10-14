package ai.foresignal.source.spi;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A fixed public {@code host:port} a source may reach over raw TCP (prompt 082), listed in the descriptor's
 * {@code allowedHosts} and opened only through {@link SocketTransport#tcpPublic}.
 *
 * <p>The host is a DNS name (lower case) or an IPv4 literal; it is code-reviewed, never operator input.
 *
 * @param host the host name, lower case
 * @param port the TCP port, 1 to 65535
 */
public record HostPort(String host, int port) {

    /** A DNS name or dotted-quad: letters, digits, dots and hyphens. */
    private static final Pattern HOST = Pattern.compile("^[a-z0-9]([a-z0-9-]*[a-z0-9])?(\\.[a-z0-9]([a-z0-9-]*[a-z0-9])?)*$");

    /** Normalizes the host to lower case and checks both parts. */
    public HostPort {
        Objects.requireNonNull(host, "host");
        host = host.strip().toLowerCase(Locale.ROOT);
        if (host.isEmpty() || host.length() > 253 || !HOST.matcher(host).matches()) {
            throw new IllegalArgumentException("'" + host + "' is not a host name");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("port " + port + " is out of range");
        }
    }

    /**
     * Parses {@code host:port}.
     *
     * @throws IllegalArgumentException when the text is not {@code host:port}
     */
    public static HostPort parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("no host:port given");
        }
        int colon = text.lastIndexOf(':');
        if (colon <= 0 || colon == text.length() - 1) {
            throw new IllegalArgumentException("'" + text + "' is not host:port");
        }
        try {
            return new HostPort(text.substring(0, colon), Integer.parseInt(text.substring(colon + 1)));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + text + "' has no numeric port", e);
        }
    }

    /** {@code host:port}. */
    @Override
    public String toString() {
        return host + ":" + port;
    }
}
