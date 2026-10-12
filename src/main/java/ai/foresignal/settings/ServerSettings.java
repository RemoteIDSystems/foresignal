package ai.foresignal.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Server-wide settings stored under {@link SettingKeys#SERVER} (OpenAPI schema
 * {@code ServerSettings}).
 *
 * <p>Prompt 016 reads {@code publicBaseUrl}, for TileJSON tile URLs; prompt 018 reads it for
 * notification links and reads {@code timezone} as the default channel timezone. Prompt 023 owns the
 * {@code GET/PUT /v1/settings/server} endpoints and adds {@code clientLinks}, {@code push} and the
 * warnings; unknown stored keys are ignored until then.
 *
 * @param publicBaseUrl the base URL clients reach Foresignal at, for example
 *     {@code http://foresignal.local:8080}; null or blank means "use the request's scheme, host and
 *     port"
 * @param timezone the server's IANA timezone, or null for UTC (the default channel timezone, 018)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ServerSettings(String publicBaseUrl, String timezone) {

    /** A setting with only a public base URL. */
    public ServerSettings(String publicBaseUrl) {
        this(publicBaseUrl, null);
    }

    /** No public base URL. */
    public static ServerSettings defaults() {
        return new ServerSettings(null, null);
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
}
