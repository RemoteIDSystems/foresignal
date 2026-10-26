package ai.foresignal.egress;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * One row of {@code egress_route} (prompt R2-018): a proxy, WireGuard profile or bound interface the collector can
 * present as its exit.
 *
 * @param id the id
 * @param name the operator's name, unique ignoring case
 * @param type {@value #HTTP}, {@value #SOCKS5}, {@value #WIREGUARD} or {@value #INTERFACE}
 * @param enabled whether the selector may use it
 * @param url the proxy URL ({@code http://host:port} or {@code socks5://host:port}), or null
 * @param boundInterface the host interface name or local IP literal to bind (type {@value #INTERFACE}), or null
 * @param secretsCipher the sealed JSON object of the write-only secrets, or null
 * @param secretsNonce its nonce, or null
 * @param exitIp the public address the last test call observed, or null
 * @param exitIpCheckedAt when that was, or null
 * @param createdAt when it was created
 * @param updatedAt when it last changed
 */
public record EgressRoute(UUID id, String name, String type, boolean enabled, String url, String boundInterface,
        byte[] secretsCipher, byte[] secretsNonce, String exitIp, Instant exitIpCheckedAt, Instant createdAt,
        Instant updatedAt) {

    /** An HTTP proxy, tunneled with {@code CONNECT} for https. */
    public static final String HTTP = "http";

    /** A SOCKS5 proxy (RFC 1928), with optional username and password (RFC 1929). */
    public static final String SOCKS5 = "socks5";

    /** A WireGuard profile; host-level, recorded but not brought up by the server. */
    public static final String WIREGUARD = "wireguard";

    /** A host interface or source IP to bind. */
    public static final String INTERFACE = "interface";

    /** The types. */
    public static final List<String> TYPES = List.of(HTTP, SOCKS5, WIREGUARD, INTERFACE);

    /** The proxy username secret. */
    public static final String USERNAME = "username";

    /** The proxy password secret. */
    public static final String PASSWORD = "password";

    /** The WireGuard profile secret. */
    public static final String WIREGUARD_CONFIG = "wireguardConfig";

    /** The secret names. */
    public static final List<String> SECRETS = List.of(USERNAME, PASSWORD, WIREGUARD_CONFIG);

    /** True when the route is a proxy ({@value #HTTP} or {@value #SOCKS5}). */
    public boolean proxy() {
        return HTTP.equals(type) || SOCKS5.equals(type);
    }

    /** This route with a newly observed exit IP. */
    public EgressRoute withExitIp(String ip, Instant at) {
        return new EgressRoute(id, name, type, enabled, url, boundInterface, secretsCipher, secretsNonce, ip, at,
                createdAt, updatedAt);
    }
}
