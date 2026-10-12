package ai.foresignal.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Admin overrides of the outbound URL policy (prompt 035), stored under {@link SettingKeys#OUTBOUND}
 * (OpenAPI schema {@code OutboundSettings}).
 *
 * <p>Host entries match a host name exactly (case-insensitive, a trailing dot ignored), or every
 * subdomain when written {@code *.example.org} (not the apex itself). CIDR entries are
 * {@code a.b.c.d/n} or {@code x::y/n}, or a bare address. Deny entries win over allow entries, and
 * allow entries never unblock the always-blocked set (link-local, cloud metadata and the other
 * not-forwardable special-purpose blocks).
 *
 * @param allowHosts hosts allowed whatever their addresses' class (except always-blocked)
 * @param allowCidrs address blocks allowed in every profile (except always-blocked)
 * @param denyHosts hosts refused in every profile
 * @param denyCidrs address blocks refused in every profile
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OutboundSettings(List<String> allowHosts, List<String> allowCidrs, List<String> denyHosts,
        List<String> denyCidrs) {

    /** Replaces missing lists with empty ones. */
    public OutboundSettings {
        allowHosts = allowHosts == null ? List.of() : List.copyOf(allowHosts);
        allowCidrs = allowCidrs == null ? List.of() : List.copyOf(allowCidrs);
        denyHosts = denyHosts == null ? List.of() : List.copyOf(denyHosts);
        denyCidrs = denyCidrs == null ? List.of() : List.copyOf(denyCidrs);
    }

    /** No overrides. */
    public static OutboundSettings defaults() {
        return new OutboundSettings(List.of(), List.of(), List.of(), List.of());
    }
}
