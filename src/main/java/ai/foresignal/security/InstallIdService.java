package ai.foresignal.security;

import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import java.security.SecureRandom;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * This installation's stable identifier, and the User-Agent built from it.
 *
 * <p>Every upstream request identifies Foresignal and this install, so an upstream operator seeing
 * unusual traffic can tell one deployment from another and get in touch. That matters here: the
 * project exists partly in reaction to ShadowBroker being called out for generating far more
 * traffic than typical clients, so being identifiable is part of being a good citizen.
 *
 * <p>The id is random, not derived from anything about the host, so it reveals nothing.
 */
@Service
public class InstallIdService implements InitializingBean {

    /** Long enough to be unique in practice, short enough to read in a log line. */
    static final int ID_LENGTH = 8;

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";

    private final AppSettingService settings;
    private final ForesignalVersion version;
    private final SecureRandom random = new SecureRandom();

    private volatile String installId;

    public InstallIdService(AppSettingService settings, ForesignalVersion version) {
        this.settings = settings;
        this.version = version;
    }

    @Override
    public void afterPropertiesSet() {
        installId = loadOrCreate();
    }

    /** The 8-character install id, generated on first start and stable afterwards. */
    public String installId() {
        String cached = installId;
        if (cached == null) {
            cached = loadOrCreate();
            installId = cached;
        }
        return cached;
    }

    /**
     * The User-Agent for every upstream request.
     *
     * <p>It always carries a contact: the project URL, plus the operator's {@code server.contactEmail}
     * when set (prompt 047; the NWS API asks for a contact in the User-Agent). It never imitates a
     * browser.
     */
    public String userAgent() {
        String contact = contactEmail();
        return "Foresignal/" + version.version() + " (+https://foresignal.ai; install=" + installId()
                + (contact == null ? "" : "; contact=" + contact) + ")";
    }

    /** The operator's contact email from the server settings, or null. */
    String contactEmail() {
        try {
            ServerSettings server = settings.getOrDefault(SettingKeys.SERVER, ServerSettings.class, ServerSettings::defaults);
            return server == null ? null : server.normalizedContactEmail();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String loadOrCreate() {
        return settings
                .get(SettingKeys.INSTALL_ID, String.class)
                .filter(id -> !id.isBlank())
                .orElseGet(
                        () -> {
                            String generated = generate();
                            settings.put(SettingKeys.INSTALL_ID, generated);
                            return generated;
                        });
    }

    private String generate() {
        StringBuilder builder = new StringBuilder(ID_LENGTH);
        for (int i = 0; i < ID_LENGTH; i++) {
            builder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return builder.toString();
    }
}
