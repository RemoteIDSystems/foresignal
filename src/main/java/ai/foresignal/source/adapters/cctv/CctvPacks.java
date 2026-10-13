/*
 * The pack list (endpoints, frame-origin pins, bounding boxes, the Digitraffic-User header and TfL's optional
 * app_key) is ported from God's Eye View, server/providers/cctv/constants.js, server/providers/cctv/catalog.js
 * (LIVE_PACKS) and server/providers/cctv/normalize.js (isLikely*Coordinate),
 * https://github.com/bilawalsidhu/gods-eye-view, commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: one source per pack and per Caltrans district; Ontario 511 needs a key (verified 2026-10-13); Austin
 * uses the SODA resource endpoint; Calgary asks for up to 5000 rows; TxDOT (an internal ITS map endpoint) and the
 * Shinjuku pack are not ported; no source cap, round-robin or distance prioritization; boxes added for TfL,
 * Caltrans and Warendorf, which GEV did not check.
 */
package ai.foresignal.source.adapters.cctv;

import ai.foresignal.source.spi.CredentialField;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The static, code-reviewed list of camera-catalog packs (prompt 072): one {@code cctv-*} source each, every one
 * disabled by default.
 *
 * <p>Pack lists are never caller-supplied. {@link #problems} validates the list at startup and
 * {@link CctvCatalogFamily} refuses to start on any problem.
 */
public final class CctvPacks {

    /** The Caltrans districts, one source each. */
    public static final int CALTRANS_DISTRICTS = 12;

    /** A source id. */
    static final Pattern ID = Pattern.compile("^cctv-[a-z0-9]+(-[a-z0-9]+)*$");

    /** The reviewed list. */
    public static final List<CctvPack> REVIEWED = build();

    private CctvPacks() {}

    /**
     * The reviewed pack of {@code id}.
     *
     * @throws IllegalArgumentException when no pack has that id
     */
    public static CctvPack reviewed(String id) {
        return REVIEWED.stream().filter(p -> p.id().equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No reviewed CCTV pack " + id));
    }

    private static List<CctvPack> build() {
        List<CctvPack> packs = new ArrayList<>();
        packs.add(CctvPack.builder("cctv-tfl", CctvJsonParsers::tfl)
                .name("TfL JamCams (London)")
                .provider("Transport for London")
                .homepage("https://api-portal.tfl.gov.uk/")
                .attribution("Powered by TfL Open Data")
                .license("Powered by TfL Open Data (OGL v2 based)")
                .terms("https://api-portal.tfl.gov.uk/")
                .catalog("https://api.tfl.gov.uk/Place/Type/JamCam")
                .origins("https://s3-eu-west-1.amazonaws.com/jamcams.tfl.gov.uk/")
                .bounds(51.2, -0.6, 51.8, 0.4)
                // 1.1 MB, no ETag, Cache-Control max-age=43200 (verified 2026-10-13).
                .interval(CctvPack.MIN_INTERVAL, Duration.ofMinutes(60))
                .credential(CredentialField.optional("appKey", "Optional TfL Unified API app key; raises the"
                        + " anonymous rate limit. Sent as the app_key query parameter."), "app_key")
                .build());
        for (int d = 1; d <= CALTRANS_DISTRICTS; d++) {
            packs.add(CctvPack.builder("cctv-caltrans-d" + d, CctvJsonParsers::caltrans)
                    .name("Caltrans cameras, District " + d)
                    .provider("Caltrans")
                    .homepage("https://cwwp2.dot.ca.gov/documentation/cctv/cctv.htm")
                    .attribution("Caltrans")
                    .license("Caltrans conditions of use")
                    .terms("https://dot.ca.gov/conditions-of-use")
                    .catalog("https://cwwp2.dot.ca.gov/data/d" + d + "/cctv/cctvStatusD" + String.format(Locale.ROOT,
                            "%02d", d) + ".json")
                    .origins("https://cwwp2.dot.ca.gov/", "https://wzmedia.dot.ca.gov/")
                    .bounds(32.4, -124.6, 42.1, -114.0)
                    // Up to 2.4 MB per district (D4, 2026-10-13).
                    .maxResponseBytes(8 * 1024 * 1024)
                    .build());
        }
        packs.add(CctvPack.builder("cctv-ontario511", CctvJsonParsers::ontario)
                .name("Ontario 511 cameras")
                .provider("Ontario 511")
                .homepage("https://511on.ca/developers/doc")
                .attribution("Ontario 511")
                .license("Ontario 511 terms of use")
                .terms("https://511on.ca/about/disclaimer")
                .catalog("https://511on.ca/api/v2/get/cameras?format=json&lang=en")
                .origins(CctvJsonParsers.ONTARIO_IMAGE)
                .bounds(41.0, -95.6, 57.5, -74.0)
                // Published throttle: ten calls every 60 seconds.
                .budget(2, 200)
                .credential(CredentialField.required("key", "Ontario 511 developer key (511on.ca, My 511 > Developer"
                        + " API key). Sent as the key query parameter; without it the API answers 400 Invalid Key."),
                        "key")
                .build());
        packs.add(CctvPack.builder("cctv-digitraffic", CctvJsonParsers::digitraffic)
                .name("Digitraffic road weather cameras (Finland)")
                .provider("Fintraffic")
                .homepage("https://www.digitraffic.fi/en/road-traffic/")
                .attribution("Fintraffic / digitraffic.fi")
                .license("CC BY 4.0")
                .terms("https://www.digitraffic.fi/en/terms-of-service/")
                .catalog("https://tie.digitraffic.fi/api/weathercam/v1/stations")
                .origins(CctvJsonParsers.DIGITRAFFIC_IMAGE)
                .bounds(59.5, 19.0, 70.5, 32.0)
                .header("Digitraffic-User", "Foresignal/{version}")
                .build());
        packs.add(CctvPack.builder("cctv-drivebc", CctvJsonParsers::driveBc)
                .name("DriveBC highway cameras (British Columbia)")
                .provider("DriveBC")
                .homepage("https://www.drivebc.ca/")
                .attribution("DriveBC, Open Government Licence – British Columbia")
                .license("Open Government Licence – British Columbia")
                .terms("https://www2.gov.bc.ca/gov/content/data/policy-standards/open-data/open-government-licence-bc")
                .catalog("https://www.drivebc.ca/api/webcams/")
                .origins(CctvJsonParsers.DRIVEBC_IMAGE)
                .bounds(48.0, -139.5, 60.5, -114.0)
                .build());
        packs.add(CctvPack.builder("cctv-nsw", CctvJsonParsers::nsw)
                .name("Live Traffic NSW cameras")
                .provider("Transport for NSW")
                .homepage("https://opendata.transport.nsw.gov.au/dataset/live-traffic-cameras")
                .attribution("Live Traffic NSW, Transport for NSW")
                .license("CC BY 4.0")
                .terms("https://opendata.transport.nsw.gov.au/dataset/live-traffic-cameras")
                .catalog("https://data.livetraffic.com/cameras/traffic-cam.json")
                .origins("https://webcams.transport.nsw.gov.au/")
                .bounds(-38.0, 140.9, -28.0, 159.2)
                .build());
        packs.add(CctvPack.builder("cctv-calgary", CctvJsonParsers::calgary)
                .name("City of Calgary traffic cameras")
                .provider("The City of Calgary")
                .homepage("https://data.calgary.ca/Transportation-Transit/Traffic-Cameras/k7p9-kppz")
                .attribution("Contains information licensed under the Open Government Licence – City of Calgary")
                .license("Open Government Licence – City of Calgary")
                .terms("https://data.calgary.ca/stories/s/Open-Calgary-Terms-of-Use/u45n-7awa")
                .catalog("https://data.calgary.ca/resource/k7p9-kppz.json?$limit=5000")
                .origins("https://trafficcam.calgary.ca/")
                .bounds(50.8, -114.4, 51.25, -113.8)
                .build());
        packs.add(CctvPack.builder("cctv-austin", CctvJsonParsers::austin)
                .name("City of Austin traffic cameras")
                .provider("Austin Transportation & Public Works")
                .homepage("https://data.austintexas.gov/Transportation-and-Mobility/Traffic-Cameras/b4k4-adkb")
                .attribution("City of Austin, Texas - data.austintexas.gov")
                .license("Public Domain")
                .terms("https://data.austintexas.gov/Transportation-and-Mobility/Traffic-Cameras/b4k4-adkb")
                .catalog("https://data.austintexas.gov/resource/b4k4-adkb.json?$limit=5000")
                .origins(CctvJsonParsers.AUSTIN_IMAGE)
                .bounds(30.02, -98.12, 30.58, -97.4)
                .build());
        packs.add(CctvPack.builder("cctv-deldot", CctvJsonParsers::delDot)
                .name("DelDOT traffic cameras (Delaware)")
                .provider("DelDOT")
                .homepage("https://deldot.gov/")
                .attribution("Delaware Department of Transportation")
                .license("operator terms")
                .terms("https://deldot.gov/")
                .catalog("https://tmc.deldot.gov/json/videocamera.json")
                .origins("https://video.deldot.gov/live/")
                .bounds(38.4, -75.85, 39.9, -75.0)
                .build());
        packs.add(CctvPack.builder("cctv-vegvesen", CctvJsonParsers::vegvesen)
                .name("Statens vegvesen road cameras (Norway)")
                .provider("Statens vegvesen")
                .homepage("https://www.vegvesen.no/en/fag/technology/open-data/")
                .attribution("Contains data under the Norwegian licence for Open Government data (NLOD) distributed"
                        + " by Statens vegvesen")
                .license("NLOD 2.0")
                .terms("https://data.norge.no/nlod/en/2.0")
                .catalog("https://ogckart-sn1.atlas.vegvesen.no/ogc/features/v1/collections/datex_3_1:CctvSimple/items"
                        + "?f=application/geo%2Bjson&limit=5000")
                .origins("https://kamera.atlas.vegvesen.no/api/images/", CctvJsonParsers.VEGVESEN_VIDEO)
                .bounds(57.9, 4.4, 71.3, 31.3)
                .maxResponseBytes(8 * 1024 * 1024)
                .maxPages(5)
                .build());
        packs.add(CctvPack.builder("cctv-estonia", DatexCameraParser::estonia)
                .name("Transpordiamet road weather cameras (Estonia)")
                .provider("Transpordiamet (Tarktee)")
                .homepage("https://tarktee.transpordiamet.ee/")
                .attribution("Transpordiamet / Tarktee")
                .license("operator terms")
                .terms("https://tarktee.transpordiamet.ee/")
                .catalog("https://tarktee.transpordiamet.ee/api/v1/datex/roadCameraLocations")
                .catalog("https://tarktee.transpordiamet.ee/api/v1/datex/roadCameraImages")
                .origins("https://tarktee.transpordiamet.ee/images/")
                .bounds(57.4, 21.5, 59.9, 28.4)
                .budget(4, 400)
                .build());
        packs.add(CctvPack.builder("cctv-tallinn", CctvJsonParsers::bundled)
                .name("City of Tallinn intersection cameras")
                .provider("City of Tallinn")
                .homepage("https://ristmikud.tallinn.ee/")
                .attribution("City of Tallinn (ristmikud.tallinn.ee)")
                .license("operator terms")
                .terms("https://ristmikud.tallinn.ee/")
                .resource("/cctv/packs/tallinn.json")
                .origins("https://ristmikud.tallinn.ee/")
                .bounds(59.2, 24.3, 59.7, 25.4)
                .interval(CctvPack.MIN_INTERVAL, Duration.ofHours(24))
                .build());
        packs.add(CctvPack.builder("cctv-warendorf", CctvJsonParsers::bundled)
                .name("Warendorf municipal webcams")
                .provider("Stadt Warendorf")
                .homepage("https://www.warendorf.de/")
                .attribution("Stadt Warendorf (courtesy)")
                .license("operator terms")
                .terms("https://www.warendorf.de/")
                .resource("/cctv/packs/warendorf.json")
                .origins("http://webcam.warendorf.de/", "https://www.kreis-warendorf.de/")
                .bounds(51.8, 7.8, 52.1, 8.2)
                .interval(CctvPack.MIN_INTERVAL, Duration.ofHours(24))
                .build());
        return List.copyOf(packs);
    }

    /**
     * Everything wrong with {@code packs}; empty when the list may be registered.
     *
     * <p>Checks: kebab-case {@code cctv-} ids, unique; name, provider, license, attribution present; https home and
     * terms pages; either https catalog URLs or a bundled resource that exists, not both; https (or, for a bundled
     * pack, http) frame origins ending in {@code /}, each host listed in {@code cctv/hosts.json}; a bounding box;
     * intervals and budget; a credential with its query parameter; no {@code User-Agent} or {@code Referer} among the
     * extra headers.
     */
    public static List<String> problems(List<CctvPack> packs) {
        List<String> out = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (CctvPack p : packs) {
            String id = p.id();
            if (id == null || !ID.matcher(id).matches()) {
                out.add("invalid id " + id);
                continue;
            }
            if (!ids.add(id)) {
                out.add(id + ": duplicate id");
            }
            if (blank(p.name()) || blank(p.provider()) || blank(p.license()) || blank(p.attribution())) {
                out.add(id + ": name, provider, license and attribution are required");
            }
            if (!https(p.homepage()) || !https(p.termsUrl())) {
                out.add(id + ": homepage and terms must be https URLs");
            }
            if (p.bundled() == !p.catalogUrls().isEmpty()) {
                out.add(id + ": needs either catalog URLs or a bundled resource");
            }
            for (URI url : p.catalogUrls()) {
                if (!"https".equals(url.getScheme()) || url.getHost() == null || url.getRawUserInfo() != null) {
                    out.add(id + ": catalog URL " + url + " must be https without user info");
                }
            }
            if (p.bundled() && CctvPacks.class.getResource(p.resource()) == null) {
                out.add(id + ": missing resource " + p.resource());
            }
            if (p.allowedFrameOrigins().isEmpty()) {
                out.add(id + ": no allowed frame origins");
            }
            for (String origin : p.allowedFrameOrigins()) {
                URI uri = URI.create(origin);
                boolean schemeOk = "https".equals(uri.getScheme()) || (p.bundled() && "http".equals(uri.getScheme()));
                // The mapper compares canonical URLs (lowercase scheme and host, no default port) with these prefixes.
                if (!schemeOk || uri.getHost() == null || !origin.endsWith("/") || uri.getPort() != -1
                        || !origin.startsWith((uri.getScheme() + "://" + uri.getHost() + "/").toLowerCase(Locale.ROOT))) {
                    out.add(id + ": frame origin " + origin + " must be an https URL prefix ending in /");
                }
                if (uri.getHost() != null && CctvFrameHosts.access(uri.getHost()) == null) {
                    out.add(id + ": frame host " + uri.getHost() + " is not in cctv/hosts.json");
                }
            }
            if (p.bounds() == null || p.bounds().south() >= p.bounds().north()
                    || p.bounds().west() >= p.bounds().east()) {
                out.add(id + ": needs a bounding box");
            }
            if (p.minInterval() == null || p.defaultInterval() == null
                    || p.defaultInterval().compareTo(p.minInterval()) < 0) {
                out.add(id + ": the default interval must be at least the minimum");
            }
            if (p.budget() == null || p.budget().perMinute() <= 0 || p.budget().perDay() <= 0) {
                out.add(id + ": needs a budget");
            }
            if ((p.credential() == null) != (p.credentialQueryParam() == null)) {
                out.add(id + ": a credential needs its query parameter");
            }
            for (String header : p.headers().keySet()) {
                String h = header.toLowerCase(Locale.ROOT);
                if (h.equals("user-agent") || h.equals("referer") || h.equals("origin")) {
                    out.add(id + ": header " + header + " is not allowed");
                }
            }
            if (p.maxPages() < 1 || p.maxResponseBytes() <= 0) {
                out.add(id + ": maxPages and maxResponseBytes must be positive");
            }
        }
        return out;
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static boolean https(String s) {
        return s != null && s.startsWith("https://");
    }
}
