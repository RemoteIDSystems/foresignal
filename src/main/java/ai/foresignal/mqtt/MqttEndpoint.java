package ai.foresignal.mqtt;

import java.net.URI;
import java.util.Locale;

/**
 * A broker address parsed from a {@code brokerUrl} (prompt 060): {@code mqtt://}, {@code mqtts://},
 * {@code ws://} or {@code wss://}, a host, an optional port and, for WebSocket, an optional path.
 *
 * @param scheme the scheme, lowercase
 * @param host the host name or literal (IPv6 without brackets)
 * @param port the port: the URL's, else 1883, 8883, 80 or 443 by scheme
 * @param tls true for {@code mqtts} and {@code wss}
 * @param websocket true for {@code ws} and {@code wss}
 * @param path the WebSocket path without the leading slash, empty for the client's default
 */
public record MqttEndpoint(String scheme, String host, int port, boolean tls, boolean websocket, String path) {

    /** Parses {@code url}; the message of the exception says what is wrong. */
    public static MqttEndpoint parse(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("is required");
        }
        URI uri;
        try {
            uri = URI.create(url.strip());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("is not a valid URL");
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        int defaultPort = switch (scheme) {
            case "mqtt" -> 1883;
            case "mqtts" -> 8883;
            case "ws" -> 80;
            case "wss" -> 443;
            default -> throw new IllegalArgumentException("must start with mqtt://, mqtts://, ws:// or wss://");
        };
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("must name a broker host");
        }
        if (uri.getUserInfo() != null) {
            throw new IllegalArgumentException("must not carry credentials; use the username and password secrets");
        }
        if (uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("must not have a query or fragment");
        }
        if (host.startsWith("[") && host.endsWith("]")) {
            host = host.substring(1, host.length() - 1);
        }
        boolean websocket = scheme.startsWith("ws");
        String path = uri.getPath() == null ? "" : uri.getPath();
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (!websocket && !path.isEmpty()) {
            throw new IllegalArgumentException("must not have a path (only ws:// and wss:// URLs do)");
        }
        int port = uri.getPort() < 0 ? defaultPort : uri.getPort();
        return new MqttEndpoint(scheme, host, port, "mqtts".equals(scheme) || "wss".equals(scheme), websocket, path);
    }
}
