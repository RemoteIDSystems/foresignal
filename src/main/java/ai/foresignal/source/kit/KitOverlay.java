package ai.foresignal.source.kit;

import ai.foresignal.net.OutboundUrlPolicy;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The operator overlay of the kits (prompt 073), off by default.
 *
 * <p>When {@value #ENABLED_PROPERTY} is true, the families also load {@code ${FORESIGNAL_DATA_DIR}/kits/features}
 * and {@code ${FORESIGNAL_DATA_DIR}/kits/feeds}. An overlay spec id must start with {@value #ID_PREFIX}, and every
 * URL it names must pass the {@link OutboundUrlPolicy} ({@code PUBLIC_ONLY}): the overlay is operator-supplied, so it
 * gets the same SSRF guard as every other operator URL. Without the policy the overlay is refused and the reason
 * logged. Specs are never accepted over the API.
 */
public final class KitOverlay {

    private static final Logger log = LoggerFactory.getLogger(KitOverlay.class);

    /** The bootstrap property enabling the overlay. */
    public static final String ENABLED_PROPERTY = "foresignal.kits.operator-specs.enabled";

    /** The prefix every overlay spec id must have. */
    public static final String ID_PREFIX = "op-";

    /** The overlay folder under the data directory. */
    public static final String FOLDER = "kits";

    private final boolean enabled;
    private final Path dataDir;
    private final Supplier<OutboundUrlPolicy> policy;

    /**
     * Creates the overlay settings.
     *
     * @param enabled the value of {@value #ENABLED_PROPERTY}
     * @param dataDir Foresignal's data directory, or null
     * @param policy supplies the outbound URL policy, or returns null when it is unavailable
     */
    public KitOverlay(boolean enabled, Path dataDir, Supplier<OutboundUrlPolicy> policy) {
        this.enabled = enabled;
        this.dataDir = dataDir;
        this.policy = policy == null ? () -> null : policy;
    }

    /** An overlay that is off. */
    public static KitOverlay disabled() {
        return new KitOverlay(false, null, null);
    }

    /** True when the overlay is enabled. */
    public boolean enabled() {
        return enabled;
    }

    /**
     * The overlay directory of one kit ({@code features} or {@code feeds}), or null when the overlay is off or
     * refused (logged).
     *
     * @param kit {@code features} or {@code feeds}
     */
    public Path directory(String kit) {
        if (!enabled) {
            return null;
        }
        if (dataDir == null) {
            log.warn("The kit operator overlay is enabled but no data directory is configured; it is ignored");
            return null;
        }
        if (policy.get() == null) {
            log.warn("The kit operator overlay is refused: the outbound URL policy (OutboundUrlPolicy) is"
                    + " unavailable, so overlay URLs cannot be checked");
            return null;
        }
        return dataDir.resolve(FOLDER).resolve(kit);
    }

    /**
     * Checks an overlay URL against the outbound URL policy.
     *
     * @param field the field path, for messages
     * @param url the URL
     * @param problems receives the problem when the URL is refused
     */
    public void checkUrl(String field, String url, List<String> problems) {
        if (url == null || url.isBlank()) {
            return;
        }
        OutboundUrlPolicy p = policy.get();
        if (p == null) {
            problems.add(field + ": the outbound URL policy is unavailable, so the overlay URL cannot be checked");
            return;
        }
        OutboundUrlPolicy.Result result;
        try {
            result = p.check(URI.create(url.strip()), OutboundUrlPolicy.Profile.PUBLIC_ONLY);
        } catch (IllegalArgumentException e) {
            problems.add(field + ": is not a URL");
            return;
        }
        if (result instanceof OutboundUrlPolicy.Denied denied) {
            problems.add(field + ": refused by the outbound URL policy: " + denied.reason());
        }
    }
}
