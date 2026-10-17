package ai.foresignal.config;

import org.springframework.core.env.Environment;

/**
 * The deployment profile (prompt 113): {@code foresignal.mode} ({@code FORESIGNAL_MODE}) is {@value #FULL} (the
 * default) or {@value #EDGE}, a lightweight relay node (a Raspberry Pi at a receiver site) that collects locally
 * and forwards to a hub over intermittent links.
 *
 * <p>Edge mode keeps collection (local receivers 069, mesh 083, keyless AIS 085, decoders 086), the writer,
 * triggers and channels, and leaves out, at bean creation, embeddings, LLM features (ask, briefings), raster
 * products and rendering, OGC, exports and federation serving: those beans carry
 * {@code @ConditionalOnProperty(name = EdgeMode.PROPERTY, havingValue = EdgeMode.FULL, matchIfMissing = true)}.
 * {@code /v1/system} reports {@code mode} and turns the matching {@code features} off. The documented heap target
 * is 256 MB; retention defaults to a 6 h track window and a 1 d default window.
 */
public final class EdgeMode {

    /** The property. */
    public static final String PROPERTY = "foresignal.mode";

    /** The full deployment (default). */
    public static final String FULL = "full";

    /** The edge relay deployment. */
    public static final String EDGE = "edge";

    /** The {@code features} flags edge mode turns off. */
    public static final java.util.List<String> DISABLED_FEATURES = java.util.List.of("embeddings", "llm", "search",
            "rasters", "briefings", "ogc", "exports", "federationServing", "ask");

    private EdgeMode() {}

    /** True when {@code environment} runs in edge mode. */
    public static boolean edge(Environment environment) {
        return environment != null && EDGE.equalsIgnoreCase(environment.getProperty(PROPERTY, FULL).strip());
    }

    /** The mode wire value of {@code environment}: {@value #FULL} or {@value #EDGE}. */
    public static String of(Environment environment) {
        return edge(environment) ? EDGE : FULL;
    }
}
