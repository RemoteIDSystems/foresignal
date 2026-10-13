package ai.foresignal.source.adapters.abusech;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import ai.foresignal.source.support.MappedRecord;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
 * The URLhaus API {@code /v1/urls/recent/} response as {@code malware-url} records (prompt 076). Pure.
 *
 * <ul>
 *   <li><strong>Identity:</strong> {@code id} (digits); an entry without one is skipped
 *       ({@value MappedRecord#MISSING_ID}).
 *   <li><strong>Time:</strong> {@code observedAt = eventTime = date_added} ({@code yyyy-MM-dd HH:mm:ss UTC}); an
 *       entry without it is skipped ({@value MappedRecord#MISSING_OBSERVED_AT}).
 *   <li><strong>Payload:</strong> {@code url} (hostile: stored as text, never fetched, never a link),
 *       {@code urlDefanged} ({@link #defang}), {@code host, urlStatus, threat, tags, blacklists} (the
 *       {@code spamhaus_dbl}/{@code surbl} statuses), {@code reporter, reference} ({@code urlhaus_reference}).
 * </ul>
 * {@code query_status} {@code no_results} is an empty window; any status other than {@code ok} is a format error.
 */
public final class UrlhausParser {

    /** The record kind. */
    public static final String KIND = "malware-url";

    /** The longest URL kept. */
    public static final int MAX_URL = 2048;

    /** The most tags kept. */
    public static final int MAX_TAGS = 20;

    /** URLhaus's {@code date_added} format. */
    static final DateTimeFormatter ADDED = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

    private static final Pattern DIGITS = Pattern.compile("^\\d{1,19}$");
    private static final Pattern CONTROL = Pattern.compile("[\\u0000-\\u001f\\u007f]");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * One parsed response.
     *
     * @param records one record per id
     * @param skipped skip reason to count
     */
    public record Recent(List<NormalizedRecord> records, Map<String, Integer> skipped) {}

    private UrlhausParser() {}

    /**
     * Parses one response.
     *
     * @param body the JSON object
     * @throws UpstreamFormatException when the body is not the response object or the query did not succeed
     */
    public static Recent parse(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("URLhaus response is not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || !root.isObject()) {
            throw new UpstreamFormatException("URLhaus response is not a JSON object", "");
        }
        String status = JsonProps.text(root, "query_status");
        if ("no_results".equals(status)) {
            return new Recent(List.of(), Map.of());
        }
        if (!"ok".equals(status) || !root.path("urls").isArray()) {
            throw new UpstreamFormatException("URLhaus query_status is " + (status == null ? "missing" : status), "");
        }
        List<NormalizedRecord> records = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        Set<String> ids = new HashSet<>();
        for (JsonNode row : root.get("urls")) {
            MappedRecord mapped = entry(row);
            if (!mapped.accepted()) {
                skipped.merge(mapped.reason(), 1, Integer::sum);
            } else if (ids.add(mapped.record().externalId())) {
                records.add(mapped.record());
            } else {
                skipped.merge(MappedRecord.DUPLICATE_ID, 1, Integer::sum);
            }
        }
        return new Recent(records, skipped);
    }

    /**
     * One URL entry (see the class note).
     *
     * @param row the entry object
     */
    public static MappedRecord entry(JsonNode row) {
        String id = JsonProps.text(row, "id");
        if (id == null || !DIGITS.matcher(id).matches()) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        Instant added = added(JsonProps.text(row, "date_added"));
        if (added == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        String url = clean(JsonProps.text(row, "url"), MAX_URL);
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND).observedAt(added).eventTime(added);
        b.put("url", url)
                .put("urlDefanged", defang(url))
                .put("host", clean(JsonProps.text(row, "host"), 253))
                .put("urlStatus", clean(JsonProps.text(row, "url_status"), 20))
                .put("threat", clean(JsonProps.text(row, "threat"), 50))
                .put("tags", tags(row.get("tags")))
                .put("blacklists", blacklists(row.get("blacklists")))
                .put("reporter", clean(JsonProps.text(row, "reporter"), 100))
                .put("reference", reference(JsonProps.text(row, "urlhaus_reference")));
        return MappedRecord.of(b.build());
    }

    /**
     * {@code url} made unclickable: the {@code http}/{@code https} scheme becomes {@code hxxp}/{@code hxxps} and every
     * dot of the host becomes {@code [.]}; the path and query are left as they are. Null for null.
     *
     * @param url a malware URL
     */
    public static String defang(String url) {
        if (url == null) {
            return null;
        }
        String scheme = "";
        String rest = url;
        int sep = url.indexOf("://");
        if (sep > 0) {
            scheme = url.substring(0, sep);
            rest = url.substring(sep + 3);
            String lower = scheme.toLowerCase(Locale.ROOT);
            if (lower.startsWith("http")) {
                scheme = "hxxp" + scheme.substring(4);
            }
            scheme = scheme + "://";
        }
        int end = rest.length();
        for (char c : new char[] {'/', '?', '#'}) {
            int i = rest.indexOf(c);
            if (i >= 0 && i < end) {
                end = i;
            }
        }
        return scheme + rest.substring(0, end).replace(".", "[.]") + rest.substring(end);
    }

    /** URLhaus's {@code yyyy-MM-dd HH:mm:ss UTC}, or null. */
    static Instant added(String value) {
        if (value == null) {
            return null;
        }
        String s = value.endsWith(" UTC") ? value.substring(0, value.length() - 4).strip() : value;
        try {
            return LocalDateTime.parse(s, ADDED).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** The tags, cleaned, at most {@value #MAX_TAGS}; null when none. */
    static List<String> tags(JsonNode array) {
        if (array == null || !array.isArray()) {
            return null;
        }
        List<String> out = new ArrayList<>();
        for (JsonNode tag : array) {
            String t = tag.isString() ? clean(tag.asString(), 60) : null;
            if (t != null && !out.contains(t) && out.size() < MAX_TAGS) {
                out.add(t);
            }
        }
        return out.isEmpty() ? null : List.copyOf(out);
    }

    /** The blacklist statuses as a map, or null. */
    static Map<String, String> blacklists(JsonNode object) {
        if (object == null || !object.isObject()) {
            return null;
        }
        Map<String, String> out = new LinkedHashMap<>();
        for (String name : List.of("spamhaus_dbl", "surbl")) {
            String v = clean(JsonProps.text(object, name), 40);
            if (v != null) {
                out.put(name, v);
            }
        }
        return out.isEmpty() ? null : out;
    }

    /** {@code value} when it is a URLhaus https link, else null. */
    static String reference(String value) {
        return value != null && value.startsWith("https://urlhaus.abuse.ch/") && value.length() <= 200 ? value : null;
    }

    /** {@code value} with control characters removed, trimmed and cut to {@code max}; null when nothing is left. */
    static String clean(String value, int max) {
        if (value == null) {
            return null;
        }
        String s = CONTROL.matcher(value).replaceAll("").strip();
        if (s.length() > max) {
            s = s.substring(0, max);
        }
        return s.isEmpty() ? null : s;
    }
}
