/*
 * The reviewed system list (ids, cities, providers and feed URLs), the BCycle URL builder and the host and path
 * allowlist rules are ported from God's Eye View, src/layers/bikeshare/registry.js (RAW_GBFS_CITY_REGISTRY,
 * buildBcycleUrls, normalizeRegistryEntry) and src/data/gbfsSource.js (isAllowedGbfsHost, isAllowedGbfsPath),
 * https://github.com/bilawalsidhu/gods-eye-view, commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java records; the allowlist is derived from the list instead of being kept by hand; license, license URL
 * and attribution added from each system's system_information.json (captured 2026-10-13); Tucson (Tugo) and Fort
 * Worth (Trinity Metro) moved to their publicbikesystem.net feeds and Buffalo (Reddy) to its socialbicycles.com feed,
 * as the BCycle URLs answer 404; Fort Lauderdale (Broward B-cycle, 404) and Columbus (CoGo, the host no longer
 * resolves) dropped; no map centers or load radii (every system is its own source, enabled by the operator).
 */
package ai.foresignal.source.adapters.gbfs;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * The static, code-reviewed list of docked bikeshare systems (prompt 071; Architecture §4.2 rule 5: families come
 * from a static list, never from caller URLs).
 *
 * <p><strong>Only reviewed URLs are requested.</strong> There is no {@code gbfs.json} auto-discovery at runtime,
 * because discovery would let upstream data choose URLs. The host allowlist ({@link #ALLOWED_HOSTS}) is derived from
 * the list, and every URL must be https, on an allowlisted host, and end in {@code station_information.json} or
 * {@code station_status.json} ({@link #isAllowedHost}, {@link #isAllowedPath}); {@link #problems} checks this at
 * startup. Adding a system is a code change reviewed like any other.
 *
 * <p><strong>Licenses:</strong> no system in the list declares {@code license_id} or {@code license_url} in its
 * {@code system_information.json} (checked 2026-10-13), so every entry carries {@link #OPERATOR_TERMS} and points at
 * the operator's data page, or at its home page where it publishes none.
 */
public final class GbfsSystems {

    /** The license line of a system whose feed declares no license. */
    public static final String OPERATOR_TERMS = "operator terms";

    /** A system id: lower-case letters, digits and single hyphens. */
    public static final Pattern ID_PATTERN = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    /** The two endpoint file names a reviewed URL may end in. */
    static final Pattern PATH = Pattern.compile("/station_(information|status)\\.json$", Pattern.CASE_INSENSITIVE);

    /** The reviewed systems, in GEV's order. */
    public static final List<GbfsSystem> REVIEWED = List.of(
            lyft("nyc-citibike", "New York, NY", "bkn", "Citi Bike", "https://citibikenyc.com/data-sharing-policy"),
            lyft("chicago-divvy", "Chicago, IL", "chi", "Divvy", "https://divvybikes.com/data-license-agreement"),
            lyft("dc-capital-bikeshare", "Washington, DC", "dca-cabi", "Capital Bikeshare",
                    "https://capitalbikeshare.com/data-license-agreement"),
            lyft("sf-bay-wheels", "San Francisco, CA", "bay", "Bay Wheels",
                    "https://www.lyft.com/bikes/bay-wheels/system-data"),
            direct("boston-bluebikes", "Boston, MA", "Blue Bikes", "https://gbfs.bluebikes.com/gbfs/en/",
                    "https://bluebikes.com/data-license-agreement"),
            bcycle("philadelphia-indego", "Philadelphia, PA", "bcycle_indego", "Indego",
                    "https://www.rideindego.com/about/data/"),
            direct("portland-biketown", "Portland, OR", "BIKETOWN", "https://gbfs.biketownpdx.com/gbfs/2.3/en/",
                    "https://biketownpdx.com/system-data"),
            bcycle("la-metro-bike", "Los Angeles, CA", "bcycle_lametro", "Metro Bike",
                    "https://bikeshare.metro.net/about/data/"),
            publicBikeSystem("austin-capmetro", "Austin, TX", "austin", "CapMetro", "https://www.capmetro.org/bikeshare"),
            publicBikeSystem("honolulu-biki", "Honolulu, HI", "hon", "Biki", "https://gobiki.org/"),
            publicBikeSystem("chattanooga-bikechatt", "Chattanooga, TN", "chat", "Bike Chattanooga",
                    "https://bikechattanooga.com/"),
            bcycle("boulder-bcycle", "Boulder, CO", "bcycle_boulder", "Boulder BCycle", "https://boulder.bcycle.com"),
            bcycle("milwaukee-bublr", "Milwaukee, WI", "bcycle_bublr", "Bublr", "https://bublrbikes.org"),
            bcycle("madison-bcycle", "Madison, WI", "bcycle_madison", "Madison BCycle", "https://madison.bcycle.com"),
            bcycle("nashville-bcycle", "Nashville, TN", "bcycle_nashville", "Nashville BCycle",
                    "https://nashville.bcycle.com"),
            bcycle("salt-lake-greenbike", "Salt Lake City, UT", "bcycle_greenbikeslc", "GREENbike",
                    "https://www.greenbikeutah.org"),
            bcycle("san-antonio-bcycle", "San Antonio, TX", "bcycle_sanantonio", "San Antonio BCycle",
                    "https://sanantonio.bcycle.com"),
            bcycle("cincinnati-red-bike", "Cincinnati, OH", "bcycle_cincyredbike", "Red Bike",
                    "https://redbike.bcycle.com"),
            bcycle("el-paso-bcycle", "El Paso, TX", "bcycle_elpaso", "El Paso B-cycle", "https://elpaso.bcycle.com"),
            bcycle("indianapolis-pacers", "Indianapolis, IN", "bcycle_pacersbikeshare", "Pacers Bikeshare",
                    "https://pacersbikeshare.org"),
            bcycle("memphis-bcycle", "Memphis, TN", "bcycle_memphis", "Explore Bike Share",
                    "https://www.explorebikeshare.com"),
            bcycle("des-moines-bcycle", "Des Moines, IA", "bcycle_desmoines", "Des Moines BCycle",
                    "https://desmoines.bcycle.com"),
            publicBikeSystem("tucson-tugo", "Tucson, AZ", "tucson", "Tugo", "https://tugobikeshare.com/"),
            publicBikeSystem("fort-worth-trinity", "Fort Worth, TX", "fortworth", "Trinity Metro",
                    "https://ridetrinitymetro.org/bikes-fort-worth/"),
            bcycle("omaha-heartland", "Omaha, NE", "bcycle_heartland", "Heartland B-cycle",
                    "https://heartland.bcycle.com"),
            bcycle("lincoln-bikelnk", "Lincoln, NE", "bcycle_bikelnk", "BikeLNK", "https://bikelnk.com/"),
            bcycle("greenville-sc-bcycle", "Greenville, SC", "bcycle_greenville", "Greenville BCycle",
                    "https://greenville.bcycle.com"),
            direct("buffalo-reddy", "Buffalo, NY", "Reddy Bikeshare",
                    "https://reddybikeshare.socialbicycles.com/opendata/",
                    "https://reddybikeshare.socialbicycles.com"),
            bcycle("las-vegas-rtc-bike-share", "Las Vegas, NV", "bcycle_rtcbikeshare", "RTC Bike Share",
                    "https://rtcbikeshare.bcycle.com"),
            bcycle("santa-barbara-bcycle", "Santa Barbara, CA", "bcycle_santabarbara", "Santa Barbara BCycle",
                    "https://santabarbara.bcycle.com"));

    /** Every host a reviewed URL names, lower-cased: the allowlist. */
    public static final Set<String> ALLOWED_HOSTS = hosts(REVIEWED);

    private GbfsSystems() {}

    /**
     * The BCycle feed URLs of {@code systemId} (GEV {@code buildBcycleUrls}).
     *
     * @param systemId the BCycle system id, for example {@code bcycle_boulder}
     * @return the {@code station_information.json} and {@code station_status.json} URLs, in that order
     */
    public static List<URI> bcycleUrls(String systemId) {
        return List.of(URI.create("https://gbfs.bcycle.com/" + systemId + "/station_information.json"),
                URI.create("https://gbfs.bcycle.com/" + systemId + "/station_status.json"));
    }

    /** True when {@code host} is in {@link #ALLOWED_HOSTS} (GEV {@code isAllowedGbfsHost}, without a wildcard). */
    public static boolean isAllowedHost(String host) {
        return host != null && ALLOWED_HOSTS.contains(host.strip().toLowerCase(Locale.ROOT));
    }

    /** True when {@code path} ends in {@code station_information.json} or {@code station_status.json}. */
    public static boolean isAllowedPath(String path) {
        return path != null && PATH.matcher(path).find();
    }

    /** True when {@code url} is https, on an allowlisted host, with an allowlisted path and no query or fragment. */
    public static boolean isAllowed(URI url) {
        return url != null && "https".equalsIgnoreCase(url.getScheme()) && isAllowedHost(url.getHost())
                && isAllowedPath(url.getPath()) && url.getRawQuery() == null && url.getRawFragment() == null
                && url.getRawUserInfo() == null && url.getPort() == -1;
    }

    /** The reviewed system {@code id}. */
    public static GbfsSystem reviewed(String id) {
        return REVIEWED.stream().filter(s -> s.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No reviewed GBFS system " + id));
    }

    /**
     * What is wrong with {@code systems}: duplicate or malformed ids, URLs that are not allowlisted, an information
     * URL that does not end in {@code station_information.json} (or a status URL in {@code station_status.json}), and
     * an unknown version.
     *
     * @return the problems, empty when the list is valid
     */
    public static List<String> problems(List<GbfsSystem> systems) {
        List<String> problems = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Set<String> hosts = hosts(systems);
        for (GbfsSystem s : systems) {
            if (!ID_PATTERN.matcher(s.id()).matches()) {
                problems.add(s.id() + ": the id is not kebab-case");
            }
            if (!ids.add(s.id())) {
                problems.add(s.id() + ": duplicate id");
            }
            check(problems, s, s.stationInformationUrl(), "station_information.json", hosts);
            check(problems, s, s.stationStatusUrl(), "station_status.json", hosts);
            if (!GbfsSystem.V2.equals(s.gbfsVersion()) && !GbfsSystem.V3.equals(s.gbfsVersion())) {
                problems.add(s.id() + ": unknown GBFS version " + s.gbfsVersion());
            }
            if (s.license().isBlank() || s.attribution().isBlank() || s.city().isBlank() || s.provider().isBlank()) {
                problems.add(s.id() + ": city, provider, license and attribution are required");
            }
            if (!"https".equalsIgnoreCase(s.licenseUrl().getScheme())) {
                problems.add(s.id() + ": the license URL must be https");
            }
        }
        return problems;
    }

    private static void check(List<String> problems, GbfsSystem s, URI url, String file, Set<String> hosts) {
        boolean shape = "https".equalsIgnoreCase(url.getScheme()) && url.getHost() != null
                && hosts.contains(url.getHost().toLowerCase(Locale.ROOT)) && isAllowedPath(url.getPath())
                && url.getRawQuery() == null && url.getRawFragment() == null && url.getRawUserInfo() == null
                && url.getPort() == -1;
        if (!shape || !url.getPath().toLowerCase(Locale.ROOT).endsWith("/" + file)) {
            problems.add(s.id() + ": " + url + " is not an allowlisted https " + file + " URL");
        }
    }

    private static Set<String> hosts(List<GbfsSystem> systems) {
        Set<String> hosts = new TreeSet<>();
        for (GbfsSystem s : systems) {
            for (URI url : List.of(s.stationInformationUrl(), s.stationStatusUrl())) {
                if (url.getHost() != null) {
                    hosts.add(url.getHost().toLowerCase(Locale.ROOT));
                }
            }
        }
        return Collections.unmodifiableSet(hosts);
    }

    private static GbfsSystem lyft(String id, String city, String system, String provider, String licenseUrl) {
        return direct(id, city, provider, "https://gbfs.lyft.com/gbfs/2.3/" + system + "/en/", licenseUrl);
    }

    private static GbfsSystem publicBikeSystem(String id, String city, String subdomain, String provider,
            String licenseUrl) {
        return direct(id, city, provider, "https://" + subdomain + ".publicbikesystem.net/customer/gbfs/v2/en/",
                licenseUrl);
    }

    private static GbfsSystem bcycle(String id, String city, String systemId, String provider, String licenseUrl) {
        List<URI> urls = bcycleUrls(systemId);
        return entry(id, city, provider, urls.get(0), urls.get(1), licenseUrl);
    }

    private static GbfsSystem direct(String id, String city, String provider, String base, String licenseUrl) {
        return entry(id, city, provider, URI.create(base + "station_information.json"),
                URI.create(base + "station_status.json"), licenseUrl);
    }

    private static GbfsSystem entry(String id, String city, String provider, URI information, URI status,
            String licenseUrl) {
        return new GbfsSystem(id, city, provider, information, status, GbfsSystem.V2, OPERATOR_TERMS,
                URI.create(licenseUrl), provider + " GBFS feed (" + information.getHost() + ")");
    }
}
