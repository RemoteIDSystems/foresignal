package ai.foresignal.source.adapters.gbfs;

import java.net.URI;
import java.util.Objects;

/**
 * One docked bikeshare system of the reviewed list ({@link GbfsSystems#REVIEWED}). Never built from a URL an API
 * caller supplied.
 *
 * @param id the system id, kebab-case; the source id is {@code gbfs-{id}} ({@link #sourceId()})
 * @param city where the system operates, for example {@code New York, NY}
 * @param provider the system's public name, for example {@code Citi Bike}
 * @param stationInformationUrl the {@code station_information.json} URL
 * @param stationStatusUrl the {@code station_status.json} URL
 * @param gbfsVersion the document shape the system publishes: {@link #V2} (GBFS 1.x and 2.x, which share the station
 *     fields) or {@link #V3}; the parser reads both shapes either way, so this is the reviewed expectation, not a switch
 * @param license the data license as the operator states it in {@code system_information} ({@code license_id} or
 *     {@code license_url}), or {@link GbfsSystems#OPERATOR_TERMS} when the feed declares none (the descriptor field
 *     is named {@code licence})
 * @param licenseUrl where the license or the operator's data terms are published
 * @param attribution the credit line, text only
 */
public record GbfsSystem(
        String id,
        String city,
        String provider,
        URI stationInformationUrl,
        URI stationStatusUrl,
        String gbfsVersion,
        String license,
        URI licenseUrl,
        String attribution) {

    /** The prefix of every source id of this family. */
    public static final String SOURCE_PREFIX = "gbfs-";

    /** GBFS 1.x and 2.x documents (epoch-second times, string names, {@code num_bikes_available}). */
    public static final String V2 = "2.x";

    /** GBFS 3.0 documents (RFC 3339 times, localized names, {@code num_vehicles_available}). */
    public static final String V3 = "3.0";

    /** Checks that every component is present. */
    public GbfsSystem {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(city, "city");
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(stationInformationUrl, "stationInformationUrl");
        Objects.requireNonNull(stationStatusUrl, "stationStatusUrl");
        Objects.requireNonNull(gbfsVersion, "gbfsVersion");
        Objects.requireNonNull(license, "license");
        Objects.requireNonNull(licenseUrl, "licenseUrl");
        Objects.requireNonNull(attribution, "attribution");
    }

    /** The source id, {@code gbfs-{id}}. */
    public String sourceId() {
        return SOURCE_PREFIX + id;
    }

    /**
     * This system with both URLs moved under {@code baseUrl} (tests only): the new path is {@code /{host}{path}}, so
     * systems that share a path on different hosts (every {@code *.publicbikesystem.net} system) stay distinct.
     *
     * @param baseUrl a scheme and authority, for example a WireMock base URL
     */
    GbfsSystem rebased(String baseUrl) {
        return new GbfsSystem(id, city, provider, rebase(stationInformationUrl, baseUrl),
                rebase(stationStatusUrl, baseUrl), gbfsVersion, license, licenseUrl, attribution);
    }

    /** {@code url} moved under {@code baseUrl} as {@code /{host}{path}}; unchanged when {@code baseUrl} is blank. */
    static URI rebase(URI url, String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return url;
        }
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = url.getRawPath() == null ? "" : url.getRawPath();
        return URI.create(base + "/" + url.getHost() + path);
    }
}
