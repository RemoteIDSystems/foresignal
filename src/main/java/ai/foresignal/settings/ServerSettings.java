package ai.foresignal.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Server-wide settings stored under {@link SettingKeys#SERVER} (OpenAPI schema
 * {@code ServerSettings}).
 *
 * <p>Prompt 016 reads only {@code publicBaseUrl}, for TileJSON tile URLs. Prompt 023 owns the
 * {@code GET/PUT /v1/settings/server} endpoints and adds {@code timezone}, {@code clientLinks},
 * {@code push} and the warnings; unknown stored keys are ignored until then.
 *
 * @param publicBaseUrl the base URL clients reach Foresignal at, for example
 *     {@code http://foresignal.local:8080}; null or blank means "use the request's scheme, host and
 *     port"
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ServerSettings(String publicBaseUrl) {

    /** No public base URL. */
    public static ServerSettings defaults() {
        return new ServerSettings(null);
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
