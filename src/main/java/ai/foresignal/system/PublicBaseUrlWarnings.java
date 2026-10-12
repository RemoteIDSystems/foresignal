package ai.foresignal.system;

import ai.foresignal.settings.ServerSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The {@code publicBaseUrl} warnings of {@code PUT /v1/settings/server} and {@code /v1/system}
 * (prompt 023).
 *
 * <ul>
 *   <li>{@code PUBLIC_BASE_URL_INSECURE}: {@code publicBaseUrl} is {@code http://} and its host is
 *       not loopback, private (RFC 1918, unique-local, link-local), CGNAT 100.64/10 or a
 *       {@code .local} name ({@link SystemWarnings#isLocal}); tokens would cross that network in
 *       clear text.
 *   <li>{@code PUBLIC_BASE_URL_MISMATCH}: requests arrive over HTTPS (directly, or with a trusted
 *       proxy's {@code X-Forwarded-Proto: https}) while {@code publicBaseUrl} is still
 *       {@code http://}, so generated links would downgrade.
 * </ul>
 */
public final class PublicBaseUrlWarnings {

    /** Code of the insecure-URL warning. */
    public static final String INSECURE = "PUBLIC_BASE_URL_INSECURE";

    /** Code of the scheme-mismatch warning. */
    public static final String MISMATCH = "PUBLIC_BASE_URL_MISMATCH";

    private PublicBaseUrlWarnings() {}

    /**
     * The warnings for {@code server}.
     *
     * @param server the settings
     * @param httpsSeen true when requests have arrived over HTTPS
     * @return the warnings, severity {@code warning}
     */
    public static List<SystemWarnings.Warning> of(ServerSettings server, boolean httpsSeen) {
        List<SystemWarnings.Warning> out = new ArrayList<>();
        String base = server == null ? null : server.normalizedPublicBaseUrl();
        if (base == null || !base.toLowerCase(Locale.ROOT).startsWith("http://")) {
            return out;
        }
        String host = SystemWarnings.host(base);
        if (!SystemWarnings.isLocal(host)) {
            out.add(new SystemWarnings.Warning(INSECURE, "warning",
                    "publicBaseUrl is http:// on a host that is not local (" + host + "); tokens and links would"
                            + " cross that network in clear text. Use an https:// URL (Tailscale Serve or Caddy)."));
        }
        if (httpsSeen) {
            out.add(new SystemWarnings.Warning(MISMATCH, "warning",
                    "Requests arrive over https, but publicBaseUrl is http://; generated links would downgrade."
                            + " Set publicBaseUrl to the https:// name."));
        }
        return out;
    }
}
