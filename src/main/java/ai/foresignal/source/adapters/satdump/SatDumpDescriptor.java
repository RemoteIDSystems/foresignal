package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.ValidatedParams;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The descriptor and parameters of a {@code satdump-{station}} source (prompt 049,
 * Weather-Imagery-Design §4.1 and §2.5).
 *
 * <p>STREAMING (two long-running loops under the streaming supervisor), kinds {@code ground-station},
 * {@code satellite-pass} and {@code raster-frame} with 30 d, 365 d and 7 d retention, STANDARD, policy
 * ALL, {@code observedAt} from upstream (the {@code /api} read time for ground-station records, SatDump
 * timestamps for passes and frames), no record TTL, license {@code operator-owned}, a LAN-only budget of
 * 60 a minute and 50,000 a day (each inbox scan counts as one request, and so does each remote push).
 *
 * <p>Prompt 054 adds the {@code polarGlob} parameter (polar GeoTIFF overlays), the optional
 * {@code ingestKey} credential and {@code acceptsRemoteMedia}: a remote station may push images and pass
 * bundles to {@code POST /v1/ingest/{sourceId}/media} and {@code /rasters}.
 */
public final class SatDumpDescriptor {

    /** The source id prefix. */
    public static final String PREFIX = "satdump-";

    /** The license of everything the station produces. */
    public static final String LICENSE = "operator-owned";

    /** {@code apiUrl}: SatDump's legacy CLI web server, {@code LAN_URL}. */
    public static final String API_URL = "apiUrl";

    /** {@code inboxPath}: SatDump's output directory under the inbox, {@code INBOX_PATH}. */
    public static final String INBOX_PATH = "inboxPath";

    /** The catalog numbers whose passes the station predicts (prompt 064). */
    public static final String PREDICT_NORAD = "predictNorad";

    /** The minimum elevation of a predicted pass (prompt 064). */
    public static final String PREDICT_MIN_ELEVATION = "predictMinElevationDeg";

    /** METEOR-M2 3 and METEOR-M2 4. */
    public static final List<String> DEFAULT_PREDICT_NORAD = List.of("57166", "59051");

    /** The default minimum elevation of a predicted pass, degrees. */
    public static final double DEFAULT_PREDICT_MIN_ELEVATION = 10.0;

    /** {@code location}: {@code lat,lon[,altM]} or a place id. */
    public static final String LOCATION = "location";

    /** {@code satNames}: a JSON object overriding the satellite name table. */
    public static final String SAT_NAMES = "satNames";

    /** {@code apiInterval}: the {@code /api} poll period. */
    public static final String API_INTERVAL = "apiInterval";

    /** {@code scanInterval}: the inbox scan period. */
    public static final String SCAN_INTERVAL = "scanInterval";

    /** {@code polarGlob}: which files of a ready pass folder are read as polar GeoTIFF overlays (054). */
    public static final String POLAR_GLOB = "polarGlob";

    /** {@code satdumpVersion}: the installed SatDump version, {@code 1.2} or {@code 2.0} (prompt 107). */
    public static final String SATDUMP_VERSION = "satdumpVersion";

    /** The default {@code polarGlob}. */
    public static final String DEFAULT_POLAR_GLOB = "**/*.tif";

    /**
     * {@code ingestKey}: the credential a remote station presents on {@code POST /v1/ingest/{sourceId}/media}
     * and {@code /rasters} (prompt 054).
     */
    public static final String INGEST_KEY = SourceDescriptor.REMOTE_MEDIA_KEY;

    /** The shortest {@code apiInterval}. */
    public static final Duration MIN_API_INTERVAL = Duration.ofSeconds(2);

    /** The longest {@code apiInterval}. */
    public static final Duration MAX_API_INTERVAL = Duration.ofSeconds(30);

    /** The shortest {@code scanInterval}. */
    public static final Duration MIN_SCAN_INTERVAL = Duration.ofSeconds(5);

    /** The longest {@code scanInterval}. */
    public static final Duration MAX_SCAN_INTERVAL = Duration.ofMinutes(10);

    /** A station name: kebab-case, short enough that the source id stays within 64 characters. */
    public static final Pattern STATION = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");

    /** The longest station name. */
    public static final int MAX_STATION_LENGTH = 64 - PREFIX.length();

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private SatDumpDescriptor() {}

    /** The source id of {@code station}. */
    public static String sourceId(String station) {
        return PREFIX + station;
    }

    /** The descriptor of {@code station}. */
    public static SourceDescriptor build(String station) {
        return SourceDescriptor.builder(sourceId(station))
                .name("SatDump ground station " + station)
                .description("The operator's own SatDump ground station '" + station + "': station state and RF"
                        + " sessions from SatDump's /api (optional), satellite passes joined to their RF statistics,"
                        + " composites and thumbnails from SatDump's output directory, and GOES/Elektro xRIT"
                        + " geostationary frames, plus polar GeoTIFF overlays with real footprints (or an approximate"
                        + " swath). Foresignal talks to SatDump only through HTTP and files.")
                .kinds(GroundStationRecords.KIND, PassBook.KIND, ProductSpec.RECORD_KIND, SatDumpExtras.DCP_KIND,
                        SatDumpExtras.EMWIN_PRODUCT_KIND, SatDumpExtras.EMWIN_WARNING_KIND)
                .homepage("https://github.com/SatDump/SatDump")
                .attribution("Operator-owned SatDump ground station")
                .licence(LICENSE)
                .mode(Mode.STREAMING)
                .defaultBudget(Budget.of(60, 50_000))
                .recordTtl(null)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .zeroResultsNormal(true)
                .titleField("title")
                .volatileFields(SourceDescriptor.RASTER_EXPIRES_AT_FIELD)
                .kindRetention(GroundStationRecords.KIND, Duration.ofDays(30))
                .kindRetention(PassBook.KIND, Duration.ofDays(365))
                .kindRetention(ProductSpec.RECORD_KIND, SourceDescriptor.RASTER_FRAME_RETENTION)
                .kindRetention(SatDumpExtras.DCP_KIND, Duration.ofDays(3))
                .kindRetention(SatDumpExtras.EMWIN_PRODUCT_KIND, Duration.ofDays(3))
                .kindRetention(SatDumpExtras.EMWIN_WARNING_KIND, Duration.ofDays(3))
                // dcp-observation (prompt 107): the address, the data and the failure code are significant
                // and the signal fields are volatile. significantFields is descriptor-wide, so it also names
                // what versions the other kinds: station state and pass status, the searched and tiled
                // fields, and the EMWIN text and warning action (checkpoint 124).
                .significantFields("address", "dataAscii", "dataHex", "failureCode")
                .significantFields("title", "state", "status", "satellite", "currentTarget", "satdumpMode")
                .significantFields("text", "action", "headline", "issuedAt", "duplicateOf")
                .volatileFields("signalDbm", "freqOffsetHz")
                .embedFields("text")
                .kindDisplay(SatDumpExtras.DCP_KIND, new ai.foresignal.source.spi.KindDisplay("DCP {address}", "water",
                        "#1098AD", null, null, null, null))
                .kindDisplay(SatDumpExtras.EMWIN_PRODUCT_KIND, new ai.foresignal.source.spi.KindDisplay("{awipsId}",
                        "antenna", "#0C8599", null, null, null, null))
                .kindDisplay(SatDumpExtras.EMWIN_WARNING_KIND, new ai.foresignal.source.spi.KindDisplay("{phenomena}",
                        "caution", "#E8590C", null, null, null, null))
                .tileProperties("state", "status", "satellite")
                .searchFields("title", "satellite", "currentTarget")
                .param(ParamSpec.optional(API_URL, ParamType.LAN_URL,
                        "SatDump's legacy CLI web server (satdump legacy autotrack|live … --http_server), for example"
                                + " http://192.168.1.60:8081. Local network only. Optional; at least one of apiUrl and"
                                + " inboxPath is required.", null))
                .param(ParamSpec.optional(INBOX_PATH, ParamType.INBOX_PATH,
                        "SatDump's output directory under FORESIGNAL_INBOX_DIR, for example satdump/home. Optional;"
                                + " at least one of apiUrl and inboxPath is required.", null))
                .param(ParamSpec.required(LOCATION, ParamType.STRING,
                        "The station position: lat,lon[,altM] in WGS84 degrees and meters, or a place id."))
                .param(ParamSpec.optional(SAT_NAMES, ParamType.STRING,
                        "A JSON object of satellite name or NORAD number to satKey, overriding the built-in"
                                + " normalization table, for example {\"METEOR-M2 3\":\"meteor-m2-3\"}.", null))
                .param(ParamSpec.optional(API_INTERVAL, ParamType.DURATION,
                        "How often /api is polled, 2s to 30s.", "5s"))
                .param(ParamSpec.optional(SCAN_INTERVAL, ParamType.DURATION,
                        "How often the inbox is fully scanned, 5s to 10m; each scan costs one budget unit.", "30s"))
                .param(ParamSpec.optional(POLAR_GLOB, ParamType.STRING,
                        "Which files of a ready polar pass folder are read as EPSG:4326/3857 GeoTIFF overlays, a glob"
                                + " relative to the pass folder; ** crosses directories.", DEFAULT_POLAR_GLOB))
                .param(ParamSpec.optional(PREDICT_NORAD, ParamType.STRING_LIST,
                        "Catalog numbers whose passes over the station are predicted from celestrak-gp elements (prompt"
                                + " 064) as status predicted; empty turns predictions off.", DEFAULT_PREDICT_NORAD))
                .param(ParamSpec.optional(PREDICT_MIN_ELEVATION, ParamType.NUMBER,
                        "The elevation a predicted pass must rise above, degrees.", DEFAULT_PREDICT_MIN_ELEVATION))
                .param(ParamSpec.enumerated(SATDUMP_VERSION,
                        "The installed SatDump version. 2.0 enables the foresignal-manifest.json hook"
                                + " (contrib/satdump/foresignal_hook.as); 1.2 is the latest stable.",
                        "1.2", List.of("1.2", "2.0")))
                .param(ParamSpec.optional(SatDumpExtras.DCS_ENABLED, ParamType.BOOLEAN,
                        "Ingest SatDump parse_dcs JSON (DCS environmental platforms). Requires an effective bbox"
                                + " (dcsBbox, else the places AOI).", false))
                .param(ParamSpec.optional(SatDumpExtras.DCS_GLOB, ParamType.STRING,
                        "Which files hold DCS JSON, matched relative to inboxPath (or a remote upload folder); processed as each file settles.", SatDumpExtras.DEFAULT_DCS_GLOB))
                .param(ParamSpec.optional(SatDumpExtras.DCS_BBOX, ParamType.BBOX,
                        "The DCS bounding box minLon,minLat,maxLon,maxLat; defaults to the places AOI.", null))
                .param(ParamSpec.optional(SatDumpExtras.DCS_ADDRESSES, ParamType.STRING_LIST,
                        "Only DCP addresses in this list are kept.", null))
                .param(ParamSpec.optional(SatDumpExtras.DCS_AGENCIES, ParamType.STRING_LIST,
                        "Only DCP agencies in this list are kept.", null))
                .param(ParamSpec.optional(SatDumpExtras.DCS_MAX_PER_SCAN, ParamType.INTEGER,
                        "The most DCS messages kept per scan; over the cap, health degrades.",
                        SatDumpExtras.DEFAULT_DCS_MAX_PER_SCAN))
                .param(ParamSpec.optional(SatDumpExtras.EMWIN_ENABLED, ParamType.BOOLEAN,
                        "Ingest EMWIN products and warnings from the GOES HRIT reception.", false))
                .param(ParamSpec.optional(SatDumpExtras.EMWIN_GLOB, ParamType.STRING,
                        "Which files hold EMWIN products, matched relative to inboxPath (or a remote upload folder); processed as each file settles.", SatDumpExtras.DEFAULT_EMWIN_GLOB))
                .param(ParamSpec.optional(SatDumpExtras.EMWIN_PRODUCTS, ParamType.STRING_LIST,
                        "The AWIPS product categories to keep (the first three letters of the AWIPS id).",
                        SatDumpExtras.DEFAULT_EMWIN_PRODUCTS))
                .param(ParamSpec.optional(SatDumpExtras.EMWIN_FALLBACK, ParamType.BOOLEAN,
                        "When nws-alerts is healthy, mark EMWIN warnings as duplicates of it.", true))
                .credential(CredentialField.optional(INGEST_KEY,
                        "Optional. A key you choose (eik_ followed by at least 32 random characters) that a remote"
                                + " station sends, like an ingest key, to POST /v1/ingest/" + sourceId(station)
                                + "/media and /rasters. Without it only an admin token may push."))
                .acceptsRemoteMedia(true)
                .build();
    }

    /**
     * The rules spanning several parameters: at least one of {@code apiUrl} and {@code inboxPath}, the
     * interval ranges, a parseable {@code location} and a {@code satNames} object of strings.
     */
    public static Map<String, String> check(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (!params.has(API_URL) && !params.has(INBOX_PATH)) {
            problems.put(API_URL, "at least one of apiUrl and inboxPath is required: apiUrl for station state and RF"
                    + " statistics, inboxPath for passes, products and xRIT frames");
        }
        Duration api = params.duration(API_INTERVAL);
        if (api != null && (api.compareTo(MIN_API_INTERVAL) < 0 || api.compareTo(MAX_API_INTERVAL) > 0)) {
            problems.put(API_INTERVAL, "must be between 2s and 30s, but was " + api.toSeconds() + "s");
        }
        Duration scan = params.duration(SCAN_INTERVAL);
        if (scan != null && (scan.compareTo(MIN_SCAN_INTERVAL) < 0 || scan.compareTo(MAX_SCAN_INTERVAL) > 0)) {
            problems.put(SCAN_INTERVAL, "must be between 5s and 10m, but was " + scan.toSeconds() + "s");
        }
        String location = params.string(LOCATION);
        if (location != null) {
            try {
                StationLocation.parse(location);
            } catch (IllegalArgumentException e) {
                problems.put(LOCATION, e.getMessage());
            }
        }
        String glob = params.string(POLAR_GLOB);
        if (glob != null && !glob.isBlank()) {
            try {
                PolarGlob.compile(glob);
            } catch (IllegalArgumentException e) {
                problems.put(POLAR_GLOB, e.getMessage());
            }
        }
        for (String norad : predictNorad(params)) {
            if (!norad.matches("\\d{1,9}")) {
                problems.put(PREDICT_NORAD, "'" + norad + "' is not a catalog number of 1 to 9 digits");
                break;
            }
        }
        Double minEl = params.number(PREDICT_MIN_ELEVATION);
        if (minEl != null && (minEl < 0 || minEl >= 90)) {
            problems.put(PREDICT_MIN_ELEVATION, "must be 0 to 89, but was " + minEl);
        }
        String names = params.string(SAT_NAMES);
        if (names != null && !names.isBlank()) {
            try {
                satNames(names);
            } catch (IllegalArgumentException e) {
                problems.put(SAT_NAMES, e.getMessage());
            }
        }
        return problems;
    }

    /**
     * The rules of the DCS and EMWIN parameters (prompt 107): enabling DCS without an effective bbox
     * (a {@code dcsBbox} or a configured places AOI) is refused, and {@code dcsMaxPerScan} must be >= 1.
     *
     * @param hasAoi true when the places AOI is available as the default bbox
     */
    public static Map<String, String> checkExtras(ValidatedParams params, boolean hasAoi) {
        Map<String, String> problems = new LinkedHashMap<>();
        if (params.bool(SatDumpExtras.DCS_ENABLED, false) && !params.has(SatDumpExtras.DCS_BBOX) && !hasAoi) {
            problems.put(SatDumpExtras.DCS_BBOX,
                    "DCS cannot be enabled without an effective bbox: set dcsBbox or configure the places AOI");
        }
        Integer cap = params.integer(SatDumpExtras.DCS_MAX_PER_SCAN);
        if (cap != null && cap < 1) {
            problems.put(SatDumpExtras.DCS_MAX_PER_SCAN, "must be at least 1, but was " + cap);
        }
        return problems;
    }

    /** The {@code predictNorad} list, trimmed; the default when unset (prompt 064). */
    public static List<String> predictNorad(ValidatedParams params) {
        List<String> list = params.has(PREDICT_NORAD) ? params.stringList(PREDICT_NORAD) : null;
        return (list == null ? DEFAULT_PREDICT_NORAD : list).stream().map(String::strip)
                .filter(n -> !n.isEmpty()).toList();
    }

    /**
     * Parses the {@code satNames} parameter.
     *
     * @throws IllegalArgumentException when it is not a JSON object of strings
     */
    public static Map<String, String> satNames(String text) {
        if (text == null || text.isBlank()) {
            return Map.of();
        }
        JsonNode root;
        try {
            root = JSON.readTree(text);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("must be a JSON object of name to satKey: " + e.getOriginalMessage());
        }
        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException("must be a JSON object of name to satKey");
        }
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> entry : root.properties()) {
            if (!entry.getValue().isString() || SatNames.slug(entry.getValue().asString()).isEmpty()) {
                throw new IllegalArgumentException("the value of '" + entry.getKey() + "' must be a satKey string");
            }
            out.put(entry.getKey(), entry.getValue().asString());
        }
        return out;
    }
}
