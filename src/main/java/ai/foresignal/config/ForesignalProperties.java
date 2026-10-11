package ai.foresignal.config;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bootstrap configuration for Foresignal, bound from {@code foresignal.*}.
 *
 * <p>Everything else — sources, budgets, credentials, retention, triggers, webhooks, CORS — lives
 * in the database and is managed through the API (Architecture §13).
 *
 * @param dataDir directory holding the secret key file and other runtime state
 * @param inboxDir root for {@code INBOX_PATH} parameters; every inbox read is confined to it
 * @param resetAdminToken regenerate the admin token on the next start
 * @param version the running build's version, supplied from build info
 */
@ConfigurationProperties("foresignal")
public record ForesignalProperties(Path dataDir, Path inboxDir, boolean resetAdminToken, String version) {

    /** Used when {@code foresignal.data-dir} is absent. */
    public static final Path DEFAULT_DATA_DIR = Path.of("./data");

    /** Subdirectory of the data dir used when {@code foresignal.inbox-dir} is absent. */
    public static final String DEFAULT_INBOX_SUBDIR = "inbox";

    /** Used when no build info is available, for example when running from an exploded classpath. */
    public static final String UNKNOWN_VERSION = "unknown";

    public ForesignalProperties {
        if (dataDir == null) {
            dataDir = DEFAULT_DATA_DIR;
        }
        if (inboxDir == null) {
            inboxDir = dataDir.resolve(DEFAULT_INBOX_SUBDIR);
        }
        if (version == null || version.isBlank()) {
            version = UNKNOWN_VERSION;
        }
    }
}
