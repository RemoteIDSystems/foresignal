package ai.foresignal.source.adapters.satdump;

import ai.foresignal.source.spi.Budget;
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
 * 60 a minute and 50,000 a day (each inbox scan counts as one request).
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

    /** {@code location}: {@code lat,lon[,altM]} or a place id. */
    public static final String LOCATION = "location";

    /** {@code satNames}: a JSON object overriding the satellite name table. */
    public static final String SAT_NAMES = "satNames";

    /** {@code apiInterval}: the {@code /api} poll period. */
    public static final String API_INTERVAL = "apiInterval";

    /** {@code scanInterval}: the inbox scan period. */
    public static final String SCAN_INTERVAL = "scanInterval";

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
                        + " geostationary frames. Foresignal talks to SatDump only through HTTP and files.")
                .kinds(GroundStationRecords.KIND, PassBook.KIND, ProductSpec.RECORD_KIND)
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
