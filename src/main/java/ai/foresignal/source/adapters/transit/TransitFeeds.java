/*
 * The reviewed feed list (ids, names, operators, regions, VehiclePositions URLs, the Entur header, licenses, license
 * URLs, attributions and default modes) is ported from God's Eye View, src/data/transitFeeds.js
 * (TRANSIT_FEED_REGISTRY and its admission rules), https://github.com/bilawalsidhu/gods-eye-view,
 * commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java records; ServiceAlerts URLs added (verified 2026-10-13); Entur's ET-Client-Name carries the install
 * id instead of a fixed name; a per-feed time base chosen from captured feeds; the TransLink path uses SEQ as the
 * prompt's verified table gives it; no coverage circles (every feed is its own source, enabled by the operator).
 */
package ai.foresignal.source.adapters.transit;

import ai.foresignal.source.spi.Egress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The static, code-reviewed list of keyless GTFS-RT feeds (prompt 070; Architecture §4.2 rule 5: families come from
 * a static list, never from caller URLs).
 *
 * <p>Admission rules (GEV): no key, token or registration (identify-yourself headers are fine); an open license that
 * permits display with attribution; real coordinates in {@code VehiclePosition.position}. Endpoints verified
 * 2026-10-13. Time bases were chosen from feeds captured that day: VEHICLE where at least 95% of entities carried a
 * vehicle timestamp (MBTA 555/555, CapMetro 384/384, HSL 788/788, OVapi 3836/3836, Entur 8667/8667, TransLink 7/7),
 * HEADER for Metro Transit (423/754, 56%).
 *
 * <p>The only header a feed may add is {@code ET-Client-Name} ({@link #ALLOWED_HEADERS}); the {@code User-Agent} is
 * always Foresignal's.
 */
public final class TransitFeeds {

    /** The request headers a feed entry may add. */
    public static final Set<String> ALLOWED_HEADERS = Set.of("ET-Client-Name");

    /** A feed id: lower-case letters, digits and single hyphens. */
    public static final Pattern ID_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    /** Entur's identification header, built from the install id. */
    public static final Map<String, String> ENTUR_HEADERS =
            Map.of("ET-Client-Name", "foresignal-" + TransitFeed.INSTALL_ID_PLACEHOLDER);

    /** The reviewed feeds, in presentation order. */
    public static final List<TransitFeed> REVIEWED = List.of(
            new TransitFeed("mbta", "MBTA", "Massachusetts Bay Transportation Authority", "Boston, MA",
                    URI.create("https://cdn.mbta.com/realtime/VehiclePositions.pb"),
                    URI.create("https://cdn.mbta.com/realtime/Alerts.pb"), Map.of(),
                    "MassDOT Developers License Agreement",
                    "https://cdn.mbta.com/sites/default/files/2023-08/mbta-massdot-develop-license-agreement.pdf",
                    "MBTA / MassDOT", TimeBase.VEHICLE, TransitModes.BUS, TransitModes.MBTA, Egress.OPEN, false),
            new TransitFeed("capmetro-austin", "CapMetro", "Capital Metropolitan Transportation Authority", "Austin, TX",
                    URI.create("https://data.texas.gov/download/eiei-9rpf/application%2Foctet-stream"), null, Map.of(),
                    "CapMetro Developer Tools license", "https://www.capmetro.org/developertools",
                    "Capital Metropolitan Transportation Authority — data.texas.gov", TimeBase.VEHICLE, TransitModes.BUS,
                    null, Egress.OPEN, false),
            new TransitFeed("metrotransit-msp", "Metro Transit", "Metro Transit (Metropolitan Council)",
                    "Minneapolis–St Paul, MN", URI.create("https://svc.metrotransit.org/mtgtfs/vehiclepositions.pb"),
                    URI.create("https://svc.metrotransit.org/mtgtfs/alerts.pb"), Map.of(),
                    "Public Metro Transit vehicle position data", "https://svc.metrotransit.org/",
                    "Metro Transit — Metropolitan Council", TimeBase.HEADER, TransitModes.BUS,
                    TransitModes.METRO_TRANSIT, Egress.OPEN, false),
            new TransitFeed("hsl-helsinki", "HSL", "Helsinki Region Transport (HSL)", "Helsinki, Finland",
                    URI.create("https://realtime.hsl.fi/realtime/vehicle-positions/v2/hsl"),
                    URI.create("https://realtime.hsl.fi/realtime/service-alerts/v2/hsl"), Map.of(), "CC BY 4.0",
                    "https://www.hsl.fi/en/hsl/open-data", "HSL (Helsinki Region Transport)", TimeBase.VEHICLE,
                    TransitModes.BUS, TransitModes.HSL, Egress.OPEN, false),
            new TransitFeed("ovapi-nl", "OVapi", "Stichting OpenGeo (NDOV data)", "Netherlands",
                    URI.create("https://gtfs.ovapi.nl/nl/vehiclePositions.pb"),
                    URI.create("https://gtfs.ovapi.nl/nl/alerts.pb"), Map.of(),
                    "Free to use per the OVapi README (best effort, no SLA; terms otherwise unverified)",
                    "https://gtfs.ovapi.nl/README", "OVapi / Stichting OpenGeo — Dutch integrated real-time transit data",
                    TimeBase.VEHICLE, TransitModes.BUS, null, Egress.OPEN, false),
            new TransitFeed("entur-norway", "Entur", "Entur AS (Norwegian national transit data)", "Norway",
                    URI.create("https://api.entur.io/realtime/v1/gtfs-rt/vehicle-positions"),
                    URI.create("https://api.entur.io/realtime/v1/gtfs-rt/alerts"), ENTUR_HEADERS,
                    "Norwegian Licence for Open Government Data (NLOD)",
                    "https://developer.entur.org/pages-intro-authentication", "Entur — data under NLOD",
                    TimeBase.VEHICLE, TransitModes.BUS, TransitModes.ENTUR, Egress.OPEN, false),
            new TransitFeed("translink-seq", "TransLink", "TransLink (Queensland Government)",
                    "South East Queensland, Australia",
                    URI.create("https://gtfsrt.api.translink.com.au/api/realtime/SEQ/VehiclePositions"),
                    URI.create("https://gtfsrt.api.translink.com.au/api/realtime/SEQ/alerts"), Map.of(),
                    "CC BY 4.0 (unverified)", "https://translink.com.au/about-translink/open-data",
                    "TransLink — Queensland Government (CC BY 4.0)", TimeBase.VEHICLE, TransitModes.BUS, null,
                    Egress.OPEN, false));

    private TransitFeeds() {}

    /** The reviewed feed with {@code id}, or null. */
    public static TransitFeed reviewed(String id) {
        for (TransitFeed feed : REVIEWED) {
            if (feed.id().equals(id)) {
                return feed;
            }
        }
        return null;
    }

    /**
     * The problems of a feed list: ids that are not kebab-case or repeat, URLs that are not absolute https, a feed
     * with neither URL, headers outside {@link #ALLOWED_HEADERS}, and modes outside {@link TransitModes#MODES}.
     *
     * @return one line per problem; empty when the list is valid
     */
    public static List<String> problems(List<TransitFeed> feeds) {
        List<String> problems = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (TransitFeed feed : feeds) {
            if (!ID_PATTERN.matcher(feed.id()).matches()) {
                problems.add(feed.id() + ": the id is not kebab-case");
            }
            if (!ids.add(feed.id())) {
                problems.add(feed.id() + ": the id repeats");
            }
            if (feed.vehiclePositionsUrl() == null && feed.alertsUrl() == null) {
                problems.add(feed.id() + ": the feed has neither a VehiclePositions nor an alerts URL");
            }
            for (URI url : new URI[] {feed.vehiclePositionsUrl(), feed.alertsUrl()}) {
                if (url != null && (!url.isAbsolute() || !"https".equalsIgnoreCase(url.getScheme())
                        || url.getRawUserInfo() != null)) {
                    problems.add(feed.id() + ": " + url + " is not an absolute https URL without user info");
                }
            }
            for (String header : feed.headers().keySet()) {
                boolean allowed = ALLOWED_HEADERS.stream().anyMatch(h -> h.equalsIgnoreCase(header));
                if (!allowed) {
                    problems.add(feed.id() + ": the header " + header + " is not allowed; only " + ALLOWED_HEADERS);
                }
            }
            if (!TransitModes.MODES.contains(feed.defaultMode().toLowerCase(Locale.ROOT))) {
                problems.add(feed.id() + ": the default mode " + feed.defaultMode() + " is unknown");
            }
        }
        return problems;
    }
}
