package ai.elaro.config;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bootstrap configuration for Elaro, bound from {@code elaro.*}.
 *
 * <p>Everything else — sources, budgets, credentials, retention, triggers, webhooks, CORS — lives
 * in the database and is managed through the API (Architecture §13).
 *
 * @param dataDir directory holding the secret key file and other runtime state
 * @param inboxDir root for {@code INBOX_PATH} parameters; every inbox read is confined to it
 * @param resetAdminToken regenerate the admin token on the next start
 * @param version the running build's version, supplied from build info
 */
@ConfigurationProperties("elaro")
public record ElaroProperties(Path dataDir, Path inboxDir, boolean resetAdminToken, String version) {

    /** Used when {@code elaro.data-dir} is absent. */
    public static final Path DEFAULT_DATA_DIR = Path.of("./data");

    /** Subdirectory of the data dir used when {@code elaro.inbox-dir} is absent. */
    public static final String DEFAULT_INBOX_SUBDIR = "inbox";

    /** Used when no build info is available, for example when running from an exploded classpath. */
    public static final String UNKNOWN_VERSION = "unknown";

    public ElaroProperties {
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
