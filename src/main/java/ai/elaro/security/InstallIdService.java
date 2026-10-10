package ai.elaro.security;

import ai.elaro.config.ElaroVersion;
import ai.elaro.settings.AppSettingService;
import ai.elaro.settings.SettingKeys;
import java.security.SecureRandom;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * This installation's stable identifier, and the User-Agent built from it.
 *
 * <p>Every upstream request identifies Elaro and this install, so an upstream operator seeing
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
    private final ElaroVersion version;
    private final SecureRandom random = new SecureRandom();

    private volatile String installId;

    public InstallIdService(AppSettingService settings, ElaroVersion version) {
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

    /** The User-Agent for every upstream request. */
    public String userAgent() {
        return "Elaro/" + version.version() + " (+https://elaro.ai; install=" + installId() + ")";
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
