package ai.foresignal.source.adapters.transit;

import ai.foresignal.source.spi.Egress;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * One GTFS-RT feed: a reviewed entry of {@link TransitFeeds} or a row of the vendored Mobility Database snapshot
 * enabled by id ({@link MobilityCatalog}). Never built from a URL an API caller supplied.
 *
 * @param id the feed id, kebab-case; the source ids are built from it ({@link #vehicleSourceId()},
 *     {@link #alertsSourceId()})
 * @param name a short display name, for example {@code MBTA}
 * @param operator the operating agency
 * @param region where the feed covers
 * @param vehiclePositionsUrl the VehiclePositions URL, or null when the feed publishes none
 * @param alertsUrl the ServiceAlerts URL, or null when the feed publishes none (CapMetro's alerts are JSON only)
 * @param headers extra request headers; the only one a feed may add is {@code ET-Client-Name}
 *     ({@link TransitFeeds#ALLOWED_HEADERS}), and {@value #INSTALL_ID_PLACEHOLDER} in a value is replaced by the
 *     install id, so each installation is identifiable
 * @param license the data license as the operator states it (the descriptor field is named {@code licence})
 * @param licenseUrl where the license or terms are published
 * @param attribution the credit line, text only
 * @param timeBase where a vehicle's {@code observedAt} comes from, fixed per feed
 * @param defaultMode the mode of a vehicle whose route id does not tell, one of {@link TransitModes#MODES}
 * @param routeMode the operator's route-id rule, or null; it returns {@link TransitModes#UNKNOWN} when it cannot tell
 * @param egress where the records may be served; {@link Egress#OPEN} unless the license says otherwise
 * @param catalog true for a Mobility Database feed, false for a reviewed entry
 */
public record TransitFeed(
        String id,
        String name,
        String operator,
        String region,
        URI vehiclePositionsUrl,
        URI alertsUrl,
        Map<String, String> headers,
        String license,
        String licenseUrl,
        String attribution,
        TimeBase timeBase,
        String defaultMode,
        UnaryOperator<String> routeMode,
        Egress egress,
        boolean catalog) {

    /** The placeholder of the install id in a header value. */
    public static final String INSTALL_ID_PLACEHOLDER = "{installId}";

    /** The prefix of a reviewed feed's vehicle source id. */
    public static final String VEHICLE_PREFIX = "transit-";

    /** The prefix of a reviewed feed's alerts source id. */
    public static final String ALERTS_PREFIX = "transit-alerts-";

    /** The infix of a catalog feed's source ids: {@code transit-mdb-{id}} and {@code transit-alerts-mdb-{id}}. */
    public static final String CATALOG_INFIX = "mdb-";

    /** Copies the headers and checks the required components. */
    public TransitFeed {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(timeBase, "timeBase");
        headers = headers == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(headers));
        defaultMode = defaultMode == null ? TransitModes.UNKNOWN : defaultMode;
        egress = egress == null ? Egress.OPEN : egress;
    }

    /** {@code transit-{id}}, or {@code transit-mdb-{id}} for a catalog feed. */
    public String vehicleSourceId() {
        return VEHICLE_PREFIX + (catalog ? CATALOG_INFIX : "") + id;
    }

    /** {@code transit-alerts-{id}}, or {@code transit-alerts-mdb-{id}} for a catalog feed. */
    public String alertsSourceId() {
        return ALERTS_PREFIX + (catalog ? CATALOG_INFIX : "") + id;
    }

    /**
     * The mode of a vehicle on {@code routeId}: the operator's rule when it tells, else {@link #defaultMode()}
     * (GEV {@code transitModeFor}).
     */
    public String modeFor(String routeId) {
        String hinted = routeMode == null ? null : routeMode.apply(routeId);
        String mode = hinted != null && !hinted.equals(TransitModes.UNKNOWN) ? hinted : defaultMode;
        return TransitModes.MODES.contains(mode) ? mode : TransitModes.UNKNOWN;
    }

    /** The request headers with {@value #INSTALL_ID_PLACEHOLDER} replaced by {@code installId}. */
    public Map<String, String> resolvedHeaders(String installId) {
        if (headers.isEmpty()) {
            return Map.of();
        }
        Map<String, String> out = new LinkedHashMap<>();
        headers.forEach((key, value) -> out.put(key, value.replace(INSTALL_ID_PLACEHOLDER,
                installId == null ? "unknown" : installId)));
        return out;
    }

    /** This feed with both URLs moved to {@code baseUrl} (scheme and authority), keeping path and query (tests). */
    TransitFeed rebased(String baseUrl) {
        return new TransitFeed(id, name, operator, region, rebase(vehiclePositionsUrl, baseUrl),
                rebase(alertsUrl, baseUrl), headers, license, licenseUrl, attribution, timeBase, defaultMode, routeMode,
                egress, catalog);
    }

    private static URI rebase(URI url, String baseUrl) {
        if (url == null || baseUrl == null || baseUrl.isBlank()) {
            return url;
        }
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = url.getRawPath() == null ? "" : url.getRawPath();
        String query = url.getRawQuery() == null ? "" : "?" + url.getRawQuery();
        return URI.create(base + path + query);
    }
}
