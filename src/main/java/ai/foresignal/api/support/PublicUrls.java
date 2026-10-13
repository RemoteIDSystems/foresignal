package ai.foresignal.api.support;

import ai.foresignal.security.restrict.PathKeyFilter;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The URLs that generated documents point at (prompt 058: the MapLibre style, Atom and JSON Feed links,
 * KML NetworkLinks and icon hrefs).
 *
 * <p>The base is {@code server.publicBaseUrl} when set; otherwise the request's scheme, host and port,
 * which Tomcat's {@code RemoteIpValve} has already corrected from {@code X-Forwarded-*} only for the
 * trusted proxies of prompt 023 (a raw forwarded header is never read here). The API root is
 * {@code /v1}, or {@code /v1/k/{key}} when the request itself came through a key-in-path URL
 * ({@link PathKeyFilter#keyBase}), so a header-less client stays on its key.
 *
 * @param base {@code scheme://host[:port]} without a trailing slash
 * @param apiRoot {@code /v1} or {@code /v1/k/{key}}
 */
public record PublicUrls(String base, String apiRoot) {

    /** The API root of an ordinary request. */
    public static final String V1 = "/v1";

    /** The URLs of {@code request}. */
    public static PublicUrls of(AppSettingService settings, HttpServletRequest request) {
        String keyBase = PathKeyFilter.keyBase(request);
        return new PublicUrls(base(settings, request), keyBase == null ? V1 : keyBase);
    }

    /** The public base URL: {@code server.publicBaseUrl}, else the request's own base. */
    public static String base(AppSettingService settings, HttpServletRequest request) {
        ServerSettings server = settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults);
        String configured = server.normalizedPublicBaseUrl();
        if (configured != null) {
            return configured;
        }
        return requestBase(request.getScheme(), request.getServerName(), request.getServerPort());
    }

    /** {@code scheme://host[:port]}, bracketing an IPv6 literal and omitting 80/443 for http/https. */
    public static String requestBase(String scheme, String host, int port) {
        String h = host.contains(":") && !host.startsWith("[") ? "[" + host + "]" : host;
        boolean defaultPort = port <= 0
                || ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        return scheme + "://" + h + (defaultPort ? "" : ":" + port);
    }

    /** True when the request came through a key-in-path URL (the generated URLs carry the key). */
    public boolean keyed() {
        return !V1.equals(apiRoot);
    }

    /**
     * The absolute URL of a canonical API path.
     *
     * @param canonicalPath a path starting with {@code /v1/}, for example {@code /v1/tiles/quakes.json}
     */
    public String api(String canonicalPath) {
        if (!canonicalPath.startsWith(V1 + "/")) {
            throw new IllegalArgumentException("not a /v1/ path: " + canonicalPath);
        }
        return base + apiRoot + canonicalPath.substring(V1.length());
    }

    /** The absolute, never keyed URL of a canonical API path (links meant for people, such as feed entries). */
    public String plain(String canonicalPath) {
        return base + canonicalPath;
    }
}
