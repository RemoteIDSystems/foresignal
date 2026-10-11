package ai.foresignal.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

/**
 * The single source of the running build's version.
 *
 * <p>Resolution order, most to least reliable:
 *
 * <ol>
 *   <li>{@link BuildProperties#getVersion()} from {@code META-INF/build-info.properties}, which the
 *       Gradle {@code buildInfo()} task generates on every build.
 *   <li>{@code foresignal.version}, which {@code application.yml} fills from
 *       {@code spring.application.version} (the jar manifest).
 *   <li>{@code "dev"}.
 * </ol>
 *
 * <p>The 001 report flagged that the second source is empty when running from exploded classes,
 * because there is no manifest. Every consumer — the upstream User-Agent, {@code GET /v1/system},
 * settings export — must read this bean rather than {@code ForesignalProperties.version()} so they all
 * agree.
 */
@Component
public class ForesignalVersion {

    /** Used when neither build info nor a manifest is available. */
    public static final String FALLBACK = "dev";

    private final String version;

    public ForesignalVersion(ObjectProvider<BuildProperties> buildProperties, ForesignalProperties properties) {
        this.version = resolve(buildProperties.getIfAvailable(), properties.version());
    }

    static String resolve(BuildProperties buildProperties, String configuredVersion) {
        if (buildProperties != null && isUsable(buildProperties.getVersion())) {
            return buildProperties.getVersion();
        }
        if (isUsable(configuredVersion)) {
            return configuredVersion;
        }
        return FALLBACK;
    }

    /** {@code ForesignalProperties} normalizes an absent version to "unknown", which is not a version. */
    private static boolean isUsable(String candidate) {
        return candidate != null
                && !candidate.isBlank()
                && !ForesignalProperties.UNKNOWN_VERSION.equals(candidate);
    }

    /** For example {@code 0.1.0-SNAPSHOT}, or {@code dev}. */
    public String version() {
        return version;
    }

    @Override
    public String toString() {
        return version;
    }
}
