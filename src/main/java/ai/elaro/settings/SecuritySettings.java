package ai.elaro.settings;

/**
 * Whether data reads need a token (Architecture §15).
 *
 * <p>Open by default, because Elaro is expected to sit on a trusted network and serve whatever
 * frontends are on it. Turning it off makes every data GET require at least the {@code read} scope.
 *
 * <p>Stored under {@link SettingKeys#SECURITY}. The {@code GET/PUT /v1/settings/security} endpoints
 * come in a later prompt; this record and its default exist now because the token filter cannot
 * decide anything without them.
 *
 * @param openReads true when data GETs are served without a token
 */
public record SecuritySettings(boolean openReads) {

    public static final boolean DEFAULT_OPEN_READS = true;

    /** The shipped default: data reads are open. */
    public static SecuritySettings defaults() {
        return new SecuritySettings(DEFAULT_OPEN_READS);
    }
}
