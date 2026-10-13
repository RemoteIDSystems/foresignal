// Ported from God's Eye View, src/sources/radioBrowser.js (cleanRadioText, RADIO_UUID_RE,
// normalizeRadioBrowserStation), MIT; see THIRD_PARTY_NOTICES.md. Reworked in Java: the payload allowlist, the
// time base and the null-island rule are Foresignal's, and http stream URLs are kept as links (GEV admits https only
// because it proxies streams; Foresignal never fetches one).
package ai.foresignal.source.adapters.radio;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import ai.foresignal.source.support.MappedRecord;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * One page of Radio Browser's {@code /json/stations/search} as {@code radio-station} records (prompt 075). Pure.
 *
 * <ul>
 *   <li><strong>Identity:</strong> {@code stationuuid} (lower case); a row without a valid UUID is skipped
 *       ({@value MappedRecord#MISSING_ID}).
 *   <li><strong>Time:</strong> {@code observedAt = lastchangetime_iso8601}, the station entry's own change time; a
 *       row without it is skipped ({@value MappedRecord#MISSING_OBSERVED_AT}).
 *   <li><strong>Geometry:</strong> the point {@code geo_long}, {@code geo_lat}; an exact (0,0) (the null island)
 *       or an out-of-range position leaves the record without geometry.
 *   <li><strong>Payload:</strong> {@code name} (control characters such as tabs replaced, whitespace collapsed and
 *       trimmed, at most 200 characters), {@code streamUrl} ({@code url_resolved}), {@code homepage, favicon}
 *       (absolute http(s) URLs only), {@code tags} (split, at most {@value #MAX_TAGS}), {@code countryCode, state,
 *       languages} (split), {@code codec, bitrate, hls} (boolean), {@code online} ({@code lastcheckok == 1}).
 *       The vote, click and check counters and timestamps and {@code changeuuid} are never read (adapter rule 8:
 *       raw counters and churn).
 * </ul>
 * Stream URLs are links for clients: Foresignal never fetches a stream.
 */
public final class RadioBrowserParser {

    /** The record kind. */
    public static final String KIND = "radio-station";

    /** The most tags kept. */
    public static final int MAX_TAGS = 15;

    /** The most languages kept. */
    public static final int MAX_LANGUAGES = 10;

    /** The longest name kept. */
    public static final int MAX_NAME = 200;

    /** Fields that must never reach a payload. */
    public static final Set<String> EXCLUDED = Set.of("votes", "clickcount", "clicktrend", "clicktimestamp",
            "clicktimestamp_iso8601", "lastchecktime", "lastchecktime_iso8601", "lastlocalchecktime",
            "lastlocalchecktime_iso8601", "changeuuid");

    /** A station UUID (GEV {@code RADIO_UUID_RE}). */
    static final Pattern UUID = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", Pattern.CASE_INSENSITIVE);

    private static final Pattern CONTROL = Pattern.compile("[\\u0000-\\u001f\\u007f]");
    private static final Pattern SPACES = Pattern.compile("\\s+");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * One parsed page.
     *
     * @param rows the rows in the page (the crawl stops on a short page)
     * @param records the records, one per id
     * @param skipped skip reason to count
     */
    public record Page(int rows, List<NormalizedRecord> records, Map<String, Integer> skipped) {}

    private RadioBrowserParser() {}

    /**
     * Parses one page.
     *
     * @param body the JSON array
     * @throws UpstreamFormatException when the body is not a JSON array
     */
    public static Page parse(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("Radio Browser page is not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || !root.isArray()) {
            throw new UpstreamFormatException("Radio Browser page is not a JSON array", "");
        }
        List<NormalizedRecord> records = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        Set<String> ids = new HashSet<>();
        for (JsonNode row : root) {
            MappedRecord mapped = station(row);
            if (!mapped.accepted()) {
                skipped.merge(mapped.reason(), 1, Integer::sum);
            } else if (ids.add(mapped.record().externalId())) {
                records.add(mapped.record());
            } else {
                skipped.merge(MappedRecord.DUPLICATE_ID, 1, Integer::sum);
            }
        }
        return new Page(root.size(), records, skipped);
    }

    /**
     * One station row (see the class note).
     *
     * @param row a station object
     */
    public static MappedRecord station(JsonNode row) {
        String uuid = JsonProps.text(row, "stationuuid");
        if (uuid == null || !UUID.matcher(uuid).matches()) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        Instant changed = JsonProps.iso(row, "lastchangetime_iso8601");
        if (changed == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(uuid.toLowerCase(Locale.ROOT), KIND).observedAt(changed);
        Double lat = JsonProps.number(row, "geo_lat");
        Double lon = JsonProps.number(row, "geo_long");
        if (lat != null && lon != null && !(lat == 0.0 && lon == 0.0) && Math.abs(lat) <= 90 && Math.abs(lon) <= 180) {
            b.point(lon, lat);
        }
        Long bitrate = JsonProps.integer(row, "bitrate");
        Long hls = JsonProps.integer(row, "hls");
        Long lastCheckOk = JsonProps.integer(row, "lastcheckok");
        String countryCode = clean(JsonProps.text(row, "countrycode"), 2);
        b.put("name", clean(JsonProps.text(row, "name"), MAX_NAME))
                .put("streamUrl", httpUrl(JsonProps.text(row, "url_resolved")))
                .put("homepage", httpUrl(JsonProps.text(row, "homepage")))
                .put("favicon", httpUrl(JsonProps.text(row, "favicon")))
                .put("tags", list(JsonProps.text(row, "tags"), MAX_TAGS))
                .put("countryCode", countryCode == null ? null : countryCode.toUpperCase(Locale.ROOT))
                .put("state", clean(JsonProps.text(row, "state"), 100))
                .put("languages", list(JsonProps.text(row, "language"), MAX_LANGUAGES))
                .put("codec", clean(JsonProps.text(row, "codec"), 40))
                .put("bitrate", bitrate == null || bitrate <= 0 ? null : bitrate)
                .put("hls", hls == null ? null : hls == 1)
                .put("online", lastCheckOk == null ? null : lastCheckOk == 1);
        return MappedRecord.of(b.build());
    }

    /**
     * {@code value} with control characters replaced by spaces, whitespace collapsed, trimmed and cut to
     * {@code max} characters (GEV {@code cleanRadioText}); null when nothing is left.
     */
    public static String clean(String value, int max) {
        if (value == null) {
            return null;
        }
        String s = SPACES.matcher(CONTROL.matcher(value).replaceAll(" ")).replaceAll(" ").strip();
        if (s.length() > max) {
            s = s.substring(0, max).strip();
        }
        return s.isEmpty() ? null : s;
    }

    /** The comma-separated values of {@code value}, cleaned and de-duplicated, at most {@code max}; null when none. */
    static List<String> list(String value, int max) {
        if (value == null) {
            return null;
        }
        Set<String> out = new LinkedHashSet<>();
        for (String part : value.split(",")) {
            String c = clean(part, 80);
            if (c != null && out.size() < max) {
                out.add(c);
            }
        }
        return out.isEmpty() ? null : List.copyOf(out);
    }

    /** {@code value} when it is an absolute http(s) URL without user info, else null. */
    static String httpUrl(String value) {
        if (value == null) {
            return null;
        }
        try {
            URI uri = new URI(value.strip());
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!(scheme.equals("http") || scheme.equals("https")) || uri.getHost() == null
                    || uri.getRawUserInfo() != null) {
                return null;
            }
            return uri.toString();
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
