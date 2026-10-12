package ai.foresignal.net;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Detects a JVM or environment HTTP proxy (prompt 035).
 *
 * <p>Through a proxy, the proxy resolves and connects, so Foresignal's address check describes the
 * view from Foresignal rather than from the proxy. {@link OutboundUrlPolicy} therefore refuses the
 * {@code LAN_ONLY} profile when a proxy is configured, and logs once that address checks are
 * bypassed for proxied requests in the other profiles.
 */
public final class OutboundProxies {

    /** JVM system properties that configure a proxy. */
    static final List<String> PROPERTIES = List.of("http.proxyHost", "https.proxyHost", "socksProxyHost");

    /** Environment variables that configure a proxy (curl, OkHttp and most tools honor them). */
    static final List<String> ENVIRONMENT = List.of("HTTP_PROXY", "http_proxy", "HTTPS_PROXY", "https_proxy",
            "ALL_PROXY", "all_proxy");

    private OutboundProxies() {}

    /**
     * The first configured proxy setting, named (never its value, which may carry credentials), or
     * empty when none is configured.
     *
     * @param environment the process environment
     * @param properties the JVM system properties
     */
    public static Optional<String> configured(Map<String, String> environment, Properties properties) {
        for (String name : PROPERTIES) {
            String value = properties.getProperty(name);
            if (value != null && !value.isBlank()) {
                return Optional.of("system property " + name);
            }
        }
        if ("true".equalsIgnoreCase(properties.getProperty("java.net.useSystemProxies"))) {
            return Optional.of("system property java.net.useSystemProxies=true");
        }
        for (String name : ENVIRONMENT) {
            String value = environment.get(name);
            if (value != null && !value.isBlank()) {
                return Optional.of("environment variable " + name);
            }
        }
        return Optional.empty();
    }

    /** {@link #configured(Map, Properties)} of this process. */
    public static Optional<String> configured() {
        return configured(System.getenv(), System.getProperties());
    }
}
