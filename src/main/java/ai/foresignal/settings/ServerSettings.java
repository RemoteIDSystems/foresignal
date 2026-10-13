package ai.foresignal.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Server-wide settings stored under {@link SettingKeys#SERVER} (OpenAPI schema
 * {@code ServerSettings}).
 *
 * <p>Prompt 016 reads {@code publicBaseUrl} for TileJSON tile URLs; prompt 018 reads it for
 * notification links and reads {@code timezone} as the default channel timezone. Prompt 023 owns
 * the {@code GET/PUT /v1/settings/server} endpoints ({@code ServerSettingsService}) and adds
 * {@code clientLinks}; prompt 031 adds {@code push} ({@link PushSettings}); prompt 047 adds
 * {@code contactEmail}, which {@code InstallIdService} appends to the User-Agent; {@code warnings} are
 * computed, never stored. Unknown stored keys are ignored.
 *
 * @param publicBaseUrl the base URL clients reach Foresignal at, for example
 *     {@code http://foresignal.local:8080}; null or blank means "use the request's scheme, host and
 *     port"
 * @param timezone the server's IANA timezone, or null for UTC (the default channel timezone, 018)
 * @param clientLinks the deep-link templates the operator set, or null for the defaults
 * @param push the push settings (031), or null for the defaults
 * @param contactEmail an operator contact appended to the upstream User-Agent (prompt 047; NWS asks
 *     for a contact), or null for none
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ServerSettings(String publicBaseUrl, String timezone, ClientLinks clientLinks, PushSettings push,
        String contactEmail) {

    /** The default timezone. */
    public static final String DEFAULT_TIMEZONE = "UTC";

    /** A setting without a contact email. */
    public ServerSettings(String publicBaseUrl, String timezone, ClientLinks clientLinks, PushSettings push) {
        this(publicBaseUrl, timezone, clientLinks, push, null);
    }

    /** A setting without push settings. */
    public ServerSettings(String publicBaseUrl, String timezone, ClientLinks clientLinks) {
        this(publicBaseUrl, timezone, clientLinks, null, null);
    }

    /** A setting with a public base URL and a timezone. */
    public ServerSettings(String publicBaseUrl, String timezone) {
        this(publicBaseUrl, timezone, null, null, null);
    }

    /** A setting with only a public base URL. */
    public ServerSettings(String publicBaseUrl) {
        this(publicBaseUrl, null, null, null, null);
    }

    /** No public base URL. */
    public static ServerSettings defaults() {
        return new ServerSettings(null, null, null, null, null);
    }

    /** The base URL without trailing slashes, or null when unset or blank. */
    public String normalizedPublicBaseUrl() {
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            return null;
        }
        String value = publicBaseUrl.strip();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value.isEmpty() ? null : value;
    }

    /** The deep-link templates in force: the operator's, completed with the defaults. */
    public ClientLinks effectiveClientLinks() {
        return clientLinks == null ? ClientLinks.defaults() : clientLinks.withDefaults();
    }

    /** The push settings in force: the stored ones, or the defaults. */
    public PushSettings effectivePush() {
        return push == null ? PushSettings.defaults() : push;
    }

    /** The contact email, stripped, or null when unset or blank. */
    public String normalizedContactEmail() {
        return contactEmail == null || contactEmail.isBlank() ? null : contactEmail.strip();
    }
}
