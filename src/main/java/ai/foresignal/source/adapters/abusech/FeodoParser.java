package ai.foresignal.source.adapters.abusech;

import ai.foresignal.reference.CountryRef;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import ai.foresignal.source.support.MappedRecord;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * A Feodo Tracker IP blocklist ({@code ipblocklist.json} or {@code ipblocklist_recommended.json}) as
 * {@code malware-c2} records (prompt 076). Pure.
 *
 * <ul>
 *   <li><strong>Identity:</strong> {@code ip_address:port}; an entry without an IP literal or a port in 1–65535 is
 *       skipped ({@value MappedRecord#MISSING_ID}).
 *   <li><strong>Time:</strong> {@code observedAt = last_online} (a date, at 00:00Z), else {@code first_seen} when
 *       the server was never seen online; neither skips the entry ({@value MappedRecord#MISSING_OBSERVED_AT}).
 *       {@code eventTime = first_seen} ({@code yyyy-MM-dd HH:mm:ss}, UTC).
 *   <li><strong>Geometry:</strong> the country's label point from the bundled country table (prompt 040
 *       {@code CountryCentroids.byIso2}), with {@code geometryApproximate: true}; an unknown country leaves the
 *       record without geometry.
 *   <li><strong>Payload:</strong> {@code ip, port, status, hostname, asn, asName, country, malware, firstSeen,
 *       lastOnline}.
 * </ul>
 */
public final class FeodoParser {

    /** The record kind. */
    public static final String KIND = "malware-c2";

    /** An IPv4 or IPv6 literal (no host names, so nothing here can trigger a DNS lookup). */
    static final Pattern IP = Pattern.compile("^(\\d{1,3}(\\.\\d{1,3}){3}|[0-9A-Fa-f:.]*:[0-9A-Fa-f:.]*)$");

    /** Feodo's {@code first_seen} format. */
    static final DateTimeFormatter SEEN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * One parsed blocklist.
     *
     * @param records one record per {@code ip:port}
     * @param skipped skip reason to count
     */
    public record Blocklist(List<NormalizedRecord> records, Map<String, Integer> skipped) {}

    private FeodoParser() {}

    /**
     * Parses one blocklist.
     *
     * @param body the JSON array
     * @param countries looks up a country by ISO 3166-1 alpha-2 code
     * @throws UpstreamFormatException when the body is not a JSON array
     */
    public static Blocklist parse(byte[] body, Function<String, Optional<CountryRef>> countries) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("Feodo blocklist is not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || !root.isArray()) {
            throw new UpstreamFormatException("Feodo blocklist is not a JSON array", "");
        }
        List<NormalizedRecord> records = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        Set<String> ids = new HashSet<>();
        for (JsonNode row : root) {
            MappedRecord mapped = entry(row, countries);
            if (!mapped.accepted()) {
                skipped.merge(mapped.reason(), 1, Integer::sum);
            } else if (ids.add(mapped.record().externalId())) {
                records.add(mapped.record());
            } else {
                skipped.merge(MappedRecord.DUPLICATE_ID, 1, Integer::sum);
            }
        }
        return new Blocklist(records, skipped);
    }

    /**
     * One blocklist entry (see the class note).
     *
     * @param row the entry object
     * @param countries looks up a country by ISO 3166-1 alpha-2 code
     */
    public static MappedRecord entry(JsonNode row, Function<String, Optional<CountryRef>> countries) {
        String ip = JsonProps.text(row, "ip_address");
        Long port = JsonProps.integer(row, "port");
        if (ip == null || !IP.matcher(ip).matches() || port == null || port < 1 || port > 65535) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        Instant firstSeen = seen(JsonProps.text(row, "first_seen"));
        Instant lastOnline = date(JsonProps.text(row, "last_online"));
        Instant observed = lastOnline != null ? lastOnline : firstSeen;
        if (observed == null) {
            return MappedRecord.skipped(MappedRecord.MISSING_OBSERVED_AT);
        }
        String id = (ip.contains(":") ? "[" + ip.toLowerCase(Locale.ROOT) + "]" : ip) + ":" + port;
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND).observedAt(observed).eventTime(firstSeen);
        String country = JsonProps.text(row, "country");
        country = country == null || country.length() != 2 ? null : country.toUpperCase(Locale.ROOT);
        Optional<CountryRef> ref = country == null || countries == null ? Optional.empty() : countries.apply(country);
        if (ref.isPresent()) {
            b.point(ref.get().labelLon(), ref.get().labelLat()).put("geometryApproximate", true);
        }
        b.put("ip", ip)
                .put("port", port)
                .put("status", lower(JsonProps.text(row, "status")))
                .put("hostname", cut(JsonProps.text(row, "hostname"), 253))
                .put("asn", JsonProps.integer(row, "as_number"))
                .put("asName", cut(JsonProps.text(row, "as_name"), 200))
                .put("country", country)
                .put("malware", cut(JsonProps.text(row, "malware"), 100))
                .put("firstSeen", firstSeen == null ? null : firstSeen.toString())
                .put("lastOnline", lastOnline == null ? null : JsonProps.text(row, "last_online"));
        return MappedRecord.of(b.build());
    }

    /** Feodo's {@code yyyy-MM-dd HH:mm:ss} (UTC), or null. */
    static Instant seen(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, SEEN).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return date(value);
        }
    }

    /** A {@code yyyy-MM-dd} date at 00:00Z, or null. */
    static Instant date(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String lower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private static String cut(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
