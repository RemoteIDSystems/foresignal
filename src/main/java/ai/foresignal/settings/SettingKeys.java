package ai.foresignal.settings;

import java.util.List;

/** Keys under which Foresignal stores its settings in {@code app_setting} (Architecture §7.1). */
public final class SettingKeys {

    /** {@link RetentionSettings}: how long records, alerts and usage are kept. */
    public static final String RETENTION = "retention";

    /** {@link CorsSettings}: which origins may read and write. */
    public static final String CORS = "cors";

    /** The durable {@code seq} cursor the trigger engine has evaluated up to. */
    public static final String TRIGGER_CURSOR = "trigger.cursor";

    /** Limits applied to SSE subscribers (buffer size, replay cap). */
    public static final String STREAM_LIMITS = "stream.limits";

    /** {@link SecuritySettings}: whether data reads require a token. */
    public static final String SECURITY = "security";

    /**
     * {@link ServerSettings}: the public base URL used in generated links (TileJSON from 016;
     * prompt 023 adds the rest of the setting and its endpoints).
     */
    public static final String SERVER = "server";

    /** This installation's generated identifier. */
    public static final String INSTALL_ID = "install.id";

    /**
     * Every key above. {@link AppSettingService#invalidateCache()} announces each of them, so a
     * component holding its own copy of a setting drops it too. A new key belongs in this list.
     */
    public static final List<String> ALL =
            List.of(RETENTION, CORS, TRIGGER_CURSOR, STREAM_LIMITS, SECURITY, SERVER, INSTALL_ID);

    private SettingKeys() {}
}
