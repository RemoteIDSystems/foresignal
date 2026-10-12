package ai.foresignal.settings;

/**
 * Whether data reads need a token (Architecture §15).
 *
 * <p>Open by default, because Foresignal is expected to sit on a trusted network and serve whatever
 * frontends are on it. Turning it off makes every data GET require at least the {@code read} scope.
 *
 * <p>Stored under {@link SettingKeys#SECURITY}; {@code GET/PUT /v1/settings/security} (prompt 023,
 * {@code SecuritySettingsService}) read and replace it. Members later prompts own
 * ({@code anonymous}, {@code metricsOpen} 035, {@code exposeMlat} 069) are ignored when stored.
 *
 * @param openReads true when data GETs are served without a token
 * @param exposeObserver true when record observers are returned with {@code include=observer} to
 *     read-scope and anonymous callers too; otherwise only agent and admin tokens see them
 *     (Core-Data-Model §14, prompt 014)
 */
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public record SecuritySettings(boolean openReads, Boolean exposeObserver) {

    public static final boolean DEFAULT_OPEN_READS = true;

    public static final boolean DEFAULT_EXPOSE_OBSERVER = false;

    /** A value stored before {@code exposeObserver} existed reads as false. */
    public SecuritySettings {
        if (exposeObserver == null) {
            exposeObserver = DEFAULT_EXPOSE_OBSERVER;
        }
    }

    /** Only {@code openReads}; observers stay gated. */
    public SecuritySettings(boolean openReads) {
        this(openReads, DEFAULT_EXPOSE_OBSERVER);
    }

    /** The shipped default: data reads are open, observers gated. */
    public static SecuritySettings defaults() {
        return new SecuritySettings(DEFAULT_OPEN_READS, DEFAULT_EXPOSE_OBSERVER);
    }
}
