package ai.foresignal.sink;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.LocalAddresses;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.springframework.stereotype.Component;

/**
 * The SERVE binding and peer rules (prompt 110; Gap §4a): a SERVE sink binds only to loopback, RFC 1918,
 * link-local or ULA; {@code 100.64.0.0/10} (a tailnet) only with {@code allowTailnet}; {@code 0.0.0.0} only
 * with {@code allowAllInterfaces}, and then every accepted peer must pass the same check.
 */
@Component
public class ServeGuard {

    /** The default bind. */
    public static final String DEFAULT_BIND = "127.0.0.1";

    /** True for a literal the SERVE sinks may bind to. */
    public boolean allowedBind(InetAddress address, boolean allowAllInterfaces, boolean allowTailnet) {
        if (address == null) {
            return false;
        }
        if (address.isAnyLocalAddress()) {
            return allowAllInterfaces;
        }
        return local(address, allowTailnet);
    }

    /** True when a peer address may connect to a SERVE sink. */
    public boolean allowedPeer(InetAddress address, boolean allowTailnet) {
        return address != null && local(address, allowTailnet);
    }

    private static boolean local(InetAddress address, boolean allowTailnet) {
        if (address.isLoopbackAddress() || address.isLinkLocalAddress()) {
            return true;
        }
        if (LocalAddresses.isPrivate(address)) {
            return true;
        }
        // IPv6 unique-local fc00::/7.
        byte[] raw = address.getAddress();
        if (raw.length == 16 && (raw[0] & 0xFE) == 0xFC) {
            return true;
        }
        return allowTailnet && LocalAddresses.isCarrierGradeNat(address);
    }

    /** Resolves and validates a bind host, throwing {@code 400 VALIDATION_FAILED}. */
    public InetAddress resolveBind(String host, boolean allowAllInterfaces, boolean allowTailnet) {
        if (host == null || host.isBlank()) {
            host = DEFAULT_BIND;
        }
        try {
            InetAddress address = InetAddress.getByName(host.strip());
            if ("0.0.0.0".equals(host.strip()) && !allowAllInterfaces) {
                throw ValidationException.forField("config.bind",
                        "0.0.0.0 needs allowAllInterfaces=true");
            }
            if (!allowedBind(address, allowAllInterfaces, allowTailnet)) {
                throw ValidationException.forField("config.bind",
                        host + " is not loopback, RFC 1918, link-local, ULA or a permitted tailnet address");
            }
            return address;
        } catch (UnknownHostException e) {
            throw ValidationException.forField("config.bind", "cannot resolve " + host);
        }
    }
}
