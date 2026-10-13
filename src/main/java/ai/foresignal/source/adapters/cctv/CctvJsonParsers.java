/*
 * The per-pack field mapping is ported from God's Eye View, server/providers/cctv/sources.js
 * (loadTflSourcesFromOpenData, loadCaltransSourcesFromOpenData, loadOntarioSourcesFromOpenData with
 * normalizeOntarioCctvUrl and pickOntarioCctvView, loadFintrafficSourcesFromOpenData, loadDriveBcSourcesFromOpenData
 * with driveBcImageCredit, nswCameraToSource, normalizeCalgaryImageUrl, calgaryCameraId, calgaryCameraName,
 * loadAustinSourcesFromOpenData, loadDelDOTSourcesFromOpenData, vegvesenCameraToSource, loadTallinnSourcesFromCatalog,
 * loadWarendorfSourcesFromCatalog) and server/providers/cctv/normalize.js (fintrafficCameraName),
 * https://github.com/bilawalsidhu/gods-eye-view, commit 591f299d11f38a612629a274463196d57ae3862e.
 * Copyright (c) 2026 Bilawal Sidhu. MIT License; the full notice is in THIRD_PARTY_NOTICES.md.
 * Changes: Java over Jackson trees; out-of-service cameras are kept with status offline instead of dropped (except
 * entries the upstream marks as not a live camera); no synthetic heading, pitch, field of view, range, mount height
 * or ground elevation; headings only from fields the upstream documents as the camera's direction; Austin reads the
 * SODA resource endpoint instead of rows.json; no distance prioritization or caps; bounding boxes and frame-origin
 * pins are applied by CameraRecordMapper; Caltrans and DelDOT ids no longer depend on list position.
 */
package ai.foresignal.source.adapters.cctv;

import ai.foresignal.source.spi.UpstreamFormatException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The JSON and GeoJSON pack parsers (prompt 072), one static method per upstream format, each a
 * {@link CctvPackParser}.
 *
 * <p>Every parser reads only what its upstream publishes. Where an upstream marks a camera out of service
 * ({@code available=false}, {@code inService=false}, {@code is_on=false}…) the camera is kept with
 * {@code status offline}; where it marks an entry as not a camera at all (Austin {@code DESIRED}, a Digitraffic
 * station that is not {@code GATHERING}, a DriveBC camera that should not appear) the entry is skipped as
 * {@link PackContext#NOT_LISTED}.
 */
public final class CctvJsonParsers {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** Caltrans: the leading camera code of {@code locationName} ("TV102 -- I-580 : …"). */
    static final Pattern CALTRANS_CODE = Pattern.compile("^([A-Za-z0-9_-]+)\\s*--\\s*");

    /** Digitraffic: a preset id is its station id plus a two-digit view number. */
    static final Pattern DIGITRAFFIC_PRESET = Pattern.compile("^C\\d{7}$");

    /** Ontario 511: a view URL path. */
    static final Pattern ONTARIO_VIEW_PATH = Pattern.compile("^/map/Cctv/([A-Za-z0-9_.-]+)$");

    /** DelDOT: the only accepted HLS path. */
    static final Pattern DELDOT_HLS_PATH = Pattern.compile("^/live/[A-Za-z0-9_.-]+/playlist\\.m3u8$");

    /** DelDOT and Vegvesen: an accepted camera id. */
    static final Pattern SAFE_ID = Pattern.compile("^[A-Za-z0-9_-]{1,80}$");

    /** Calgary: the numbered frame file name. */
    static final Pattern CALGARY_FILE = Pattern.compile("(?i)/(loc\\d+)\\.jpg$");

    /** DriveBC: the {@code credit} texts that are image attributions rather than operational notes. */
    static final Pattern DRIVEBC_CREDIT =
            Pattern.compile("(?i)courtesy|provided by|presented in cooperation|city of|parks canada");

    /** The Digitraffic frame origin; frame URLs are built from the preset id. */
    public static final String DIGITRAFFIC_IMAGE = "https://weathercam.digitraffic.fi/";

    /** The DriveBC frame URL prefix; frame URLs are built from the camera id. */
    public static final String DRIVEBC_IMAGE = "https://www.drivebc.ca/images/";

    /** The Austin frame URL prefix; frame URLs are built from the camera id. */
    public static final String AUSTIN_IMAGE = "https://cctv.austinmobility.io/image/";

    /** The Ontario 511 frame URL prefix; frame URLs are built from the view id. */
    public static final String ONTARIO_IMAGE = "https://511on.ca/map/Cctv/";

    /** The Vegvesen HLS manifest URL prefix. */
    public static final String VEGVESEN_VIDEO = "https://kamera.vegvesen.no/public/";

    private CctvJsonParsers() {}

    // ---------------------------------------------------------------- TfL

    /** TfL Unified API {@code /Place/Type/JamCam}: an array of places with {@code additionalProperties}. */
    public static List<CameraEntry> tfl(byte[] body, PackContext pc) {
        JsonNode root = read(body, pc);
        requireArray(root, pc);
        List<CameraEntry> out = new ArrayList<>();
        for (JsonNode place : root) {
            String id = text(place, "id");
            if (id == null) {
                pc.skip(PackContext.MALFORMED);
                continue;
            }
            String available = null;
            String image = null;
            String view = null;
            for (JsonNode p : place.path("additionalProperties")) {
                String key = text(p, "key");
                if ("available".equals(key)) {
                    available = text(p, "value");
                } else if ("imageUrl".equals(key)) {
                    image = text(p, "value");
                } else if ("view".equals(key)) {
                    view = text(p, "value");
                }
            }
            // videoUrl is an MP4 clip, not a stream: deliberately not read. The view label is mostly free text
            // ("Zoom west - Deptford Bridge"); only a label that is wholly a compass direction is a direction.
            Double heading = Compass.heading(view);
            out.add(CameraEntry.builder(id, number(place.get("lat")), number(place.get("lon")))
                    .name(orElse(text(place, "commonName"), "JamCam " + id))
                    .direction(heading == null ? null : view)
                    .headingDeg(heading)
                    .active("true".equalsIgnoreCase(available))
                    .imageUrl(image)
                    .build());
        }
        return out;
    }

    // ---------------------------------------------------------------- Caltrans

    /** Caltrans CWWP2 {@code cctvStatusDNN.json}: {@code data[].cctv}. */
    public static List<CameraEntry> caltrans(byte[] body, PackContext pc) {
        JsonNode root = read(body, pc);
        JsonNode data = root.path("data");
        if (!data.isArray()) {
            throw format(pc, "has no data array");
        }
        List<CameraEntry> out = new ArrayList<>();
        for (JsonNode row : data) {
            JsonNode cctv = row.path("cctv");
            JsonNode loc = cctv.path("location");
            JsonNode images = cctv.path("imageData");
            String image = reported(text(images.path("static"), "currentImageURL"));
            String locationName = orElse(text(loc, "locationName"), "");
            Matcher code = CALTRANS_CODE.matcher(locationName);
            String id = code.find() ? code.group(1) : imageDirectory(image);
            if (id == null) {
                pc.skip(PackContext.MALFORMED);
                continue;
            }
            String label = CALTRANS_CODE.matcher(locationName).replaceFirst("").strip();
            Integer minutes = integer(reported(text(images.path("static"), "currentImageUpdateFrequency")));
            out.add(CameraEntry.builder(id, number(loc.get("latitude")), number(loc.get("longitude")))
                    .name(label.isEmpty() ? "Caltrans " + id : label)
                    .region(text(loc, "nearbyPlace"))
                    .road(text(loc, "route"))
                    // The documented direction of travel of the road the camera is on, not its facing: no heading.
                    .direction(text(loc, "direction"))
                    .active("true".equalsIgnoreCase(text(cctv, "inService")))
                    .imageUrl(image)
                    .videoUrl(reported(text(images, "streamingVideoURL")))
                    .refreshSeconds(minutes != null && minutes >= 1 && minutes <= 1440 ? minutes * 60 : null)
                    .build());
        }
        return out;
    }

    /** The directory name of a Caltrans image URL ({@code …/image/tv102i580westofsr24/tv102….jpg}), or null. */
    static String imageDirectory(String url) {
        if (url == null) {
            return null;
        }
        String[] parts = url.split("/");
        return parts.length >= 2 && !parts[parts.length - 2].isBlank() ? parts[parts.length - 2] : null;
    }

    // ---------------------------------------------------------------- Ontario 511

    /** Ontario 511 {@code /api/v2/get/cameras}: an array of cameras with {@code Views}. */
    public static List<CameraEntry> ontario(byte[] body, PackContext pc) {
        JsonNode root = read(body, pc);
        requireArray(root, pc);
        List<CameraEntry> out = new ArrayList<>();
        for (JsonNode row : root) {
            String id = text(row, "Id");
            if (id == null) {
                pc.skip(PackContext.MALFORMED);
                continue;
            }
            String image = null;
            String fallback = null;
            for (JsonNode view : row.path("Views")) {
                if (!"enabled".equalsIgnoreCase(text(view, "Status"))) {
                    continue;
                }
                String url = ontarioViewUrl(text(view, "Url"));
                String description = orElse(text(view, "Description"), "");
                // GEV pickOntarioCctvView: the first enabled view whose description does not say "down".
                if (!description.matches("(?i).*\\bdown\\b.*")) {
                    image = url;
                    break;
                }
                if (fallback == null) {
                    fallback = url;
                }
            }
            if (image == null) {
                image = fallback;
            }
            String direction = text(row, "Direction");
            if (direction != null && (direction.equalsIgnoreCase("Unknown") || direction.equalsIgnoreCase("None"))) {
                direction = null;
            }
            String location = text(row, "Location");
            String roadway = text(row, "Roadway");
            out.add(CameraEntry.builder(id, number(row.get("Latitude")), number(row.get("Longitude")))
                    .name(orElse(location, orElse(roadway, "Ontario 511 camera " + id)))
                    .road(roadway)
                    .direction(direction)
                    .headingDeg(Compass.heading(direction))
                    .active(image != null)
                    .imageUrl(image)
                    .build());
        }
        return out;
    }

    /**
     * An Ontario 511 view URL on {@code 511on.ca} or a {@code *.traveliq.co} host, rebuilt on
     * {@value #ONTARIO_IMAGE} (GEV {@code normalizeOntarioCctvUrl}); any other URL is returned verbatim and the
     * mapper's origin check drops it.
     */
    static String ontarioViewUrl(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            URI uri = new URI(raw.strip());
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            Matcher path = ONTARIO_VIEW_PATH.matcher(uri.getPath() == null ? "" : uri.getPath());
            if ("https".equalsIgnoreCase(uri.getScheme()) && (host.equals("511on.ca") || host.endsWith(".traveliq.co"))
                    && path.matches()) {
                return ONTARIO_IMAGE + path.group(1);
            }
        } catch (URISyntaxException e) {
            return raw;
        }
        return raw;
    }

    // ---------------------------------------------------------------- Digitraffic

    /** Digitraffic {@code /api/weathercam/v1/stations}: one entry per preset of a {@code GATHERING} station. */
    public static List<CameraEntry> digitraffic(byte[] body, PackContext pc) {
        JsonNode root = read(body, pc);
        JsonNode features = root.path("features");
        if (!features.isArray()) {
            throw format(pc, "is not a FeatureCollection");
        }
        List<CameraEntry> out = new ArrayList<>();
        for (JsonNode feature : features) {
            JsonNode props = feature.path("properties");
            String stationId = text(props, "id");
            if (stationId == null) {
                pc.skip(PackContext.MALFORMED);
                continue;
            }
            if (!"GATHERING".equalsIgnoreCase(text(props, "collectionStatus"))) {
                pc.skip(PackContext.NOT_LISTED);
                continue;
            }
            JsonNode coords = feature.path("geometry").path("coordinates");
            Double lon = number(coords.get(0));
            Double lat = number(coords.get(1));
            for (JsonNode preset : props.path("presets")) {
                String presetId = text(preset, "id");
                if (presetId == null || !DIGITRAFFIC_PRESET.matcher(presetId).matches()
                        || !presetId.startsWith(stationId)) {
                    pc.skip(PackContext.MALFORMED);
                    continue;
                }
                out.add(CameraEntry.builder(stationId + ":" + presetId, lat, lon)
                        .name(digitrafficName(text(props, "name"), stationId, presetId))
                        .active(preset.path("inCollection").isBoolean() && preset.path("inCollection").asBoolean())
                        .imageUrl(DIGITRAFFIC_IMAGE + presetId + ".jpg")
                        .build());
            }
        }
        return out;
    }

    /** GEV {@code fintrafficCameraName}: "kt51 Inkoo (view 01)". */
    static String digitrafficName(String stationName, String stationId, String presetId) {
        String base = stationName == null ? "" : stationName.replace('_', ' ').strip();
        if (base.isEmpty()) {
            base = "Fintraffic " + stationId;
        }
        String view = presetId.length() > stationId.length() ? presetId.substring(stationId.length()) : "";
        return view.isEmpty() ? base : base + " (view " + view + ")";
    }

    // ---------------------------------------------------------------- DriveBC

    /** DriveBC {@code /api/webcams/}: an array of webcams. */
    public static List<CameraEntry> driveBc(byte[] body, PackContext pc) {
        JsonNode root = read(body, pc);
        requireArray(root, pc);
        List<CameraEntry> out = new ArrayList<>();
        for (JsonNode row : root) {
            JsonNode idNode = row.get("id");
            if (idNode == null || !idNode.isIntegralNumber() || idNode.asLong() <= 0) {
                pc.skip(PackContext.MALFORMED);
                continue;
            }
            if (!(row.path("should_appear").isBoolean() && row.path("should_appear").asBoolean())) {
                pc.skip(PackContext.NOT_LISTED);
                continue;
            }
            long id = idNode.asLong();
            JsonNode coords = row.path("location").path("coordinates");
            String orientation = text(row, "orientation");
            out.add(CameraEntry.builder(Long.toString(id), number(coords.get(1)), number(coords.get(0)))
                    .name(orElse(text(row, "name"), "DriveBC camera " + id))
                    .provider(driveBcCredit(text(row, "credit")))
                    .region(text(row, "region_name"))
                    .road(text(row, "highway_display"))
                    .direction(orientation)
                    .headingDeg(Compass.heading(orientation))
                    .active(row.path("is_on").isBoolean() && row.path("is_on").asBoolean())
                    .imageUrl(DRIVEBC_IMAGE + id + ".jpg")
                    .build());
        }
        return out;
    }

    /**
     * GEV {@code driveBcImageCredit}: the {@code credit} with HTML stripped when it is an image attribution
     * ("Images courtesy of TransLink"), else null (operational notes such as "relies on solar power" are dropped).
     */
    static String driveBcCredit(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").strip();
        return !text.isEmpty() && DRIVEBC_CREDIT.matcher(text).find() ? text : null;
    }

    // ---------------------------------------------------------------- NSW

    /** Live Traffic NSW {@code traffic-cam.json}: a FeatureCollection. */
    public static List<CameraEntry> nsw(byte[] body, PackContext pc) {
        JsonNode root = read(body, pc);
        JsonNode features = root.path("features");
        if (!features.isArray()) {
            throw format(pc, "is not a FeatureCollection");
        }
        List<CameraEntry> out = new ArrayList<>();
        for (JsonNode feature : features) {
            String id = text(feature, "id");
            if (id == null) {
                pc.skip(PackContext.MALFORMED);
                continue;
            }
            JsonNode props = feature.path("properties");
            JsonNode coords = feature.path("geometry").path("coordinates");
            String direction = text(props, "direction");
            String region = text(props, "region");
            out.add(CameraEntry.builder(id, strictNumber(coords.get(1)), strictNumber(coords.get(0)))
                    .name(orElse(text(props, "title"), "NSW camera " + id))
                    .region(region == null ? null : region.replace('_', ' '))
                    .direction(direction)
                    .headingDeg(Compass.heading(direction))
                    .imageUrl(text(props, "href"))
                    .build());
        }
        return out;
    }

    // ---------------------------------------------------------------- Calgary

    /** Open Calgary SODA {@code k7p9-kppz}: an array of rows; the frame file name is the id. */
    public static List<CameraEntry> calgary(byte[] body, PackContext pc) {
        JsonNode root = read(body, pc);
        requireArray(root, pc);
        List<CameraEntry> out = new ArrayList<>();
        for (JsonNode row : root) {
            String image = calgaryImageUrl(text(row.path("camera_url"), "url"));
            String id = calgaryId(image);
            if (id == null) {
                pc.skip(PackContext.MALFORMED);
                continue;
            }
            JsonNode coords = row.path("point").path("coordinates");
            out.add(CameraEntry.builder(id, number(coords.get(1)), number(coords.get(0)))
                    .name(orElse(text(row, "camera_location"),
                            orElse(text(row.path("camera_url"), "description"), "Calgary camera " + id)))
                    // The city quadrant of the address grid, never a facing.
                    .region(text(row, "quadrant"))
                    .imageUrl(image)
                    .build());
        }
        return out;
    }

    /**
     * GEV {@code normalizeCalgaryImageUrl}: an {@code http:} frame URL upgraded to {@code https:} (the city's host
     * serves both and redirects to https); other schemes are returned verbatim for the origin check to drop.
     */
    static String calgaryImageUrl(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.strip();
        return text.regionMatches(true, 0, "http://", 0, 7) ? "https://" + text.substring(7) : text;
    }

    /** The {@code locNN} frame file name of a Calgary frame URL, or null. */
    static String calgaryId(String url) {
        if (url == null) {
            return null;
        }
        Matcher m = CALGARY_FILE.matcher(url);
        return m.find() ? m.group(1).toLowerCase(Locale.ROOT) : null;
    }

    // ---------------------------------------------------------------- Austin

    /** Austin SODA {@code b4k4-adkb}: an array of rows; only {@code TURNED_ON} cameras are listed. */
    public static List<CameraEntry> austin(byte[] body, PackContext pc) {
        JsonNode root = read(body, pc);
        requireArray(root, pc);
        List<CameraEntry> out = new ArrayList<>();
        for (JsonNode row : root) {
            String id = text(row, "camera_id");
            if (id == null || !id.matches("\\d+")) {
                pc.skip(PackContext.MALFORMED);
                continue;
            }
            if (!"TURNED_ON".equalsIgnoreCase(text(row, "camera_status"))) {
                pc.skip(PackContext.NOT_LISTED);
                continue;
            }
            JsonNode coords = row.path("location").path("coordinates");
            out.add(CameraEntry.builder(id, number(coords.get(1)), number(coords.get(0)))
                    .name(orElse(text(row, "location_name"), "Austin camera " + id))
                    .road(text(row, "primary_st"))
                    .imageUrl(AUSTIN_IMAGE + id + ".jpg")
                    .build());
        }
        return out;
    }

    // ---------------------------------------------------------------- DelDOT

    /** DelDOT {@code videocamera.json}: {@code videoCameras[]}, live HLS only. */
    public static List<CameraEntry> delDot(byte[] body, PackContext pc) {
        JsonNode root = read(body, pc);
        JsonNode rows = root.path("videoCameras");
        if (!rows.isArray()) {
            throw format(pc, "has no videoCameras array");
        }
        List<CameraEntry> out = new ArrayList<>();
        for (JsonNode row : rows) {
            String id = text(row, "id");
            if (id == null || !SAFE_ID.matcher(id).matches()) {
                pc.skip(PackContext.MALFORMED);
                continue;
            }
            String county = text(row, "county");
            out.add(CameraEntry.builder(id, number(row.get("lat")), number(row.get("lon")))
                    .name(orElse(text(row, "title"), "DelDOT " + id))
                    .region(county == null ? null : county + " County")
                    .active("active".equalsIgnoreCase(text(row, "status")))
                    .videoUrl(delDotStream(text(row.path("urls"), "m3u8s")))
                    .build());
        }
        return out;
    }

    /** GEV's DelDOT pin: the HTTPS HLS URL on {@code video.deldot.gov} with a {@code /live/…/playlist.m3u8} path. */
    static String delDotStream(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            URI uri = new URI(raw.strip());
            if ("https".equalsIgnoreCase(uri.getScheme()) && "video.deldot.gov".equalsIgnoreCase(uri.getHost())
                    && uri.getUserInfo() == null && uri.getPath() != null
                    && DELDOT_HLS_PATH.matcher(uri.getPath()).matches()) {
                return raw.strip();
            }
        } catch (URISyntaxException e) {
            return null;
        }
        return null;
    }

    // ---------------------------------------------------------------- Vegvesen

    /** Statens vegvesen OGC API Features {@code datex_3_1:CctvSimple} items, one page; reports {@code rel=next}. */
    public static List<CameraEntry> vegvesen(byte[] body, PackContext pc) {
        JsonNode root = read(body, pc);
        JsonNode features = root.path("features");
        if (!features.isArray()) {
            throw format(pc, "is not a FeatureCollection");
        }
        pc.nextPage(null);
        for (JsonNode link : root.path("links")) {
            if ("next".equals(text(link, "rel")) && text(link, "href") != null) {
                try {
                    pc.nextPage(new URI(text(link, "href")));
                } catch (URISyntaxException e) {
                    throw format(pc, "has an invalid next link");
                }
            }
        }
        List<CameraEntry> out = new ArrayList<>();
        for (JsonNode feature : features) {
            JsonNode props = feature.path("properties");
            String id = text(props, "cameraId");
            if (id == null || !SAFE_ID.matcher(id).matches()) {
                pc.skip(PackContext.MALFORMED);
                continue;
            }
            String availability = text(props, "status.stillImageAvailability");
            JsonNode coords = feature.path("geometry").path("coordinates");
            String place = orElse(text(props, "description"), "Kamera " + id);
            String towards = text(props, "orientationDescription");
            String video = text(props, "videoUrl");
            boolean hls = "hls".equalsIgnoreCase(text(props, "videoEncodingStandard"))
                    && (VEGVESEN_VIDEO + id + "/manifest.m3u8").equals(video);
            out.add(CameraEntry.builder(id, number(coords.get(1)), number(coords.get(0)))
                    // orientationDescription names the place the camera looks towards, not a bearing.
                    .name(towards != null && !towards.equals(place) ? place + " → " + towards : place)
                    .road(text(props, "roadNumber"))
                    .active(availability == null || availability.equals("videoOrImagesAvailable"))
                    .imageUrl(text(props, "stillImageUrl"))
                    .videoUrl(hls ? video : null)
                    .build());
        }
        return out;
    }

    // ---------------------------------------------------------------- bundled packs

    /**
     * A bundled God's Eye View catalog ({@code config/cctv_sources.{tallinn,warendorf}.json}): an array of
     * {@code {id, name, provider, city, lat, lon, url, headingDeg, headingConfidence}}. Only a curated heading
     * ({@code headingConfidence: high}) is kept; GEV's pose priors (pitch, field of view, range, mount and ground
     * heights) are not.
     */
    public static List<CameraEntry> bundled(byte[] body, PackContext pc) {
        JsonNode root = read(body, pc);
        requireArray(root, pc);
        List<CameraEntry> out = new ArrayList<>();
        for (JsonNode item : root) {
            String id = text(item, "id");
            if (id == null) {
                pc.skip(PackContext.MALFORMED);
                continue;
            }
            Double heading = strictNumber(item.get("headingDeg"));
            boolean curated = "high".equalsIgnoreCase(text(item, "headingConfidence"));
            out.add(CameraEntry.builder(id, strictNumber(item.get("lat")), strictNumber(item.get("lon")))
                    .name(orElse(text(item, "name"), id))
                    .provider(text(item, "provider"))
                    .region(text(item, "city"))
                    .headingDeg(curated && heading != null ? ((heading % 360) + 360) % 360 : null)
                    .imageUrl(orElse(text(item, "url"), text(item, "snapshotUrl")))
                    .build());
        }
        return out;
    }

    // ---------------------------------------------------------------- helpers

    /** Parses {@code body} as JSON, or throws a format error naming the pack. */
    static JsonNode read(byte[] body, PackContext pc) {
        try {
            JsonNode root = JSON.readTree(body);
            if (root == null || root.isMissingNode()) {
                throw format(pc, "is empty");
            }
            return root;
        } catch (JacksonException e) {
            throw format(pc, "is not valid JSON");
        }
    }

    private static void requireArray(JsonNode root, PackContext pc) {
        if (!root.isArray()) {
            throw format(pc, "is not a JSON array");
        }
    }

    static UpstreamFormatException format(PackContext pc, String problem) {
        return new UpstreamFormatException(pc.pack().id() + ": the catalog " + problem, "");
    }

    /** A stripped, non-blank string or number field; null otherwise. */
    static String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || !(value.isString() || value.isNumber())) {
            return null;
        }
        String text = value.asString().strip();
        return text.isEmpty() ? null : text;
    }

    /** A finite number from a number or a numeric string; null otherwise. */
    static Double number(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            double d = node.asDouble();
            return Double.isFinite(d) ? d : null;
        }
        if (node.isString()) {
            try {
                double d = Double.parseDouble(node.asString().strip());
                return Double.isFinite(d) ? d : null;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /** A finite number only from a JSON number (GEV: {@code Number(null)} must not become 0). */
    static Double strictNumber(JsonNode node) {
        return node != null && node.isNumber() && Double.isFinite(node.asDouble()) ? node.asDouble() : null;
    }

    /** An integer from text, or null. */
    static Integer integer(String text) {
        if (text == null) {
            return null;
        }
        try {
            return Integer.valueOf(text.strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Caltrans' "Not Reported" placeholder as null. */
    static String reported(String text) {
        return text == null || text.equalsIgnoreCase("Not Reported") ? null : text;
    }

    static String orElse(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /** The reasons a parser counts, for documentation and tests. */
    static final Map<String, String> REASONS = Map.of(
            PackContext.NO_COORDINATES, "no usable coordinates",
            PackContext.OUT_OF_BOUNDS, "outside the pack's bounding box",
            PackContext.DUPLICATE, "an id already listed in this fetch",
            PackContext.MALFORMED, "no usable id",
            PackContext.NOT_LISTED, "marked by the upstream as not a live camera");
}
