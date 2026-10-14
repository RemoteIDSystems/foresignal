package ai.foresignal.notify.mesh;

import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.source.spi.LanEndpoint;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

/**
 * Validates and pins the LAN address of a mesh channel (prompt 084) exactly like a {@code LAN_URL} source parameter
 * (prompts 069 and 083): {@code scheme://host:port} only, an explicit port, no user info, path, query or fragment,
 * and a host that resolves under {@link OutboundUrlPolicy.Profile#LAN_ONLY} (loopback, RFC 1918 or unique-local).
 * The returned {@link LanEndpoint} carries the pinned address, so the companion connection never resolves the name
 * again. This is the one place outside {@code ParamValidator} that builds a {@link LanEndpoint}, for channel config.
 */
public final class MeshLanUrls {

    /** The outcome of {@link #pin}. */
    public sealed interface Outcome permits Pinned, Refused {}

    /**
     * The URL is valid and its host is on the LAN.
     *
     * @param endpoint the pinned endpoint
     */
    public record Pinned(LanEndpoint endpoint) implements Outcome {}

    /**
     * The URL is refused.
     *
     * @param reason why
     * @param notLan true when the host resolved but not to a LAN address (a public-channel rule for Meshtastic)
     */
    public record Refused(String reason, boolean notLan) implements Outcome {}

    private MeshLanUrls() {}

    /**
     * Checks and pins {@code url}.
     *
     * @param schemes the accepted schemes, lower case
     * @param policy the outbound policy that resolves and classifies the host
     */
    public static Outcome pin(String url, Set<String> schemes, OutboundUrlPolicy policy) {
        if (url == null || url.isBlank()) {
            return new Refused("is required", false);
        }
        URI uri;
        try {
            uri = new URI(url.strip());
        } catch (URISyntaxException e) {
            return new Refused("'" + url + "' is not a URL", false);
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!schemes.contains(scheme) || uri.getHost() == null) {
            return new Refused("must be " + String.join(" or ", schemes.stream().sorted().map(s -> s + "://host:port")
                    .toList()), false);
        }
        if (uri.getPort() < 1 || uri.getPort() > 65_535) {
            return new Refused("needs an explicit port, for example " + scheme + "://192.168.1.20:"
                    + (scheme.equals("tcp") ? "5000" : scheme.equals("mqtts") ? "8883" : "1883"), false);
        }
        if (uri.getRawUserInfo() != null) {
            return new Refused("must not carry user info", false);
        }
        String path = uri.getRawPath();
        if ((path != null && !path.isEmpty() && !path.equals("/")) || uri.getRawQuery() != null
                || uri.getRawFragment() != null) {
            return new Refused("must be only scheme://host:port, without a path, query or fragment", false);
        }
        if (policy == null) {
            return new Refused("the outbound URL policy is not available", false);
        }
        OutboundUrlPolicy.Result result = policy.checkHost(uri.getHost(), OutboundUrlPolicy.Profile.LAN_ONLY);
        if (result instanceof OutboundUrlPolicy.Denied denied) {
            if (denied.unresolved()) {
                return new Refused("the host '" + uri.getHost() + "' could not be resolved", false);
            }
            return new Refused("the host '" + uri.getHost() + "' is not on the LAN: " + denied.reason(), true);
        }
        LanEndpoint.Scheme s = switch (scheme) {
            case "tcp" -> LanEndpoint.Scheme.TCP;
            case "mqtts" -> LanEndpoint.Scheme.MQTTS;
            default -> LanEndpoint.Scheme.MQTT;
        };
        return new Pinned(new LanEndpoint(s, uri.getHost(), uri.getPort(),
                ((OutboundUrlPolicy.Allowed) result).pinned()));
    }
}
