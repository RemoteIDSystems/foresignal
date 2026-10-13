package ai.foresignal.source.adapters.cisa;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import ai.foresignal.source.support.MappedRecord;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
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
 * The CISA KEV catalog JSON as {@code vulnerability} records (prompt 076). Pure.
 *
 * <ul>
 *   <li><strong>Identity:</strong> {@code cveID} (upper case, {@code CVE-yyyy-n}); an entry without a valid id is
 *       skipped ({@value MappedRecord#MISSING_ID}), a repeated id is skipped ({@value MappedRecord#DUPLICATE_ID}).
 *   <li><strong>Time:</strong> {@code observedAt} is the catalog's {@code dateReleased} for every record (the
 *       publication time, so it is not hashed and a new catalog release is a touch); {@code eventTime} is the
 *       entry's {@code dateAdded} at 00:00Z.
 *   <li><strong>Payload:</strong> {@code cveId, vendorProject, product, vulnerabilityName} (trimmed: upstream has
 *       leading and trailing spaces), {@code shortDescription, requiredAction, dueDate} (a date string),
 *       {@code knownRansomwareCampaignUse} ({@code Known|Unknown}), {@code cwes}, {@code notes} (at most
 *       {@value #MAX_NOTES} characters) and {@code kev: true}.
 * </ul>
 * A catalog without {@code dateReleased} or without a {@code vulnerabilities} array is a format error, because it
 * cannot be compared with the previous one.
 */
public final class KevParser {

    /** The record kind. */
    public static final String KIND = "vulnerability";

    /** The longest {@code notes} kept. */
    public static final int MAX_NOTES = 2048;

    /** The longest of the other text fields kept. */
    public static final int MAX_TEXT = 4096;

    /** A CVE id. */
    static final Pattern CVE = Pattern.compile("^CVE-\\d{4}-\\d{4,}$");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * One parsed catalog.
     *
     * @param catalogVersion the catalog's {@code catalogVersion}, or null
     * @param released the catalog's {@code dateReleased}
     * @param records one record per entry
     * @param skipped skip reason to count
     */
    public record Catalog(String catalogVersion, Instant released, List<NormalizedRecord> records,
            Map<String, Integer> skipped) {}

    private KevParser() {}

    /**
     * Parses the catalog.
     *
     * @param body the catalog JSON
     * @throws UpstreamFormatException when the body is not the catalog object, or {@code dateReleased} is missing
     */
    public static Catalog parse(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("CISA KEV catalog is not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || !root.isObject() || !root.path("vulnerabilities").isArray()) {
            throw new UpstreamFormatException("CISA KEV catalog has no vulnerabilities array", "");
        }
        Instant released = instant(JsonProps.text(root, "dateReleased"));
        if (released == null) {
            throw new UpstreamFormatException("CISA KEV catalog has no readable dateReleased", "");
        }
        List<NormalizedRecord> records = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        Set<String> ids = new HashSet<>();
        for (JsonNode entry : root.get("vulnerabilities")) {
            MappedRecord mapped = entry(entry, released);
            if (!mapped.accepted()) {
                skipped.merge(mapped.reason(), 1, Integer::sum);
            } else if (ids.add(mapped.record().externalId())) {
                records.add(mapped.record());
            } else {
                skipped.merge(MappedRecord.DUPLICATE_ID, 1, Integer::sum);
            }
        }
        return new Catalog(JsonProps.text(root, "catalogVersion"), released, records, skipped);
    }

    /**
     * One catalog entry (see the class note).
     *
     * @param entry the entry object
     * @param released the catalog's {@code dateReleased}, every record's {@code observedAt}
     */
    public static MappedRecord entry(JsonNode entry, Instant released) {
        String id = JsonProps.text(entry, "cveID");
        if (id == null || !CVE.matcher(id.toUpperCase(Locale.ROOT)).matches()) {
            return MappedRecord.skipped(MappedRecord.MISSING_ID);
        }
        id = id.toUpperCase(Locale.ROOT);
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND).observedAt(released)
                .eventTime(date(JsonProps.text(entry, "dateAdded")));
        b.put("cveId", id)
                .put("vendorProject", cut(JsonProps.text(entry, "vendorProject"), 200))
                .put("product", cut(JsonProps.text(entry, "product"), 200))
                .put("vulnerabilityName", cut(JsonProps.text(entry, "vulnerabilityName"), 500))
                .put("shortDescription", cut(JsonProps.text(entry, "shortDescription"), MAX_TEXT))
                .put("requiredAction", cut(JsonProps.text(entry, "requiredAction"), MAX_TEXT))
                .put("dueDate", dateText(JsonProps.text(entry, "dueDate")))
                .put("knownRansomwareCampaignUse", ransomware(JsonProps.text(entry, "knownRansomwareCampaignUse")))
                .put("cwes", cwes(entry.get("cwes")))
                .put("notes", cut(JsonProps.text(entry, "notes"), MAX_NOTES))
                .put("kev", true);
        return MappedRecord.of(b.build());
    }

    /** {@code value} cut to {@code max} characters (and trimmed again), or null. */
    static String cut(String value, int max) {
        if (value == null) {
            return null;
        }
        String s = value.length() > max ? value.substring(0, max).strip() : value;
        return s.isEmpty() ? null : s;
    }

    /** {@code Known} or {@code Unknown} (case-insensitive), else null. */
    static String ransomware(String value) {
        if (value == null) {
            return null;
        }
        if (value.equalsIgnoreCase("known")) {
            return "Known";
        }
        return value.equalsIgnoreCase("unknown") ? "Unknown" : null;
    }

    /** The CWE ids of the {@code cwes} array, at most 10; null when there are none. */
    static List<String> cwes(JsonNode array) {
        if (array == null || !array.isArray()) {
            return null;
        }
        List<String> out = new ArrayList<>();
        for (JsonNode cwe : array) {
            if (cwe.isString() && !cwe.asString().isBlank() && out.size() < 10 && !out.contains(cwe.asString().strip())) {
                out.add(cwe.asString().strip());
            }
        }
        return out.isEmpty() ? null : List.copyOf(out);
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

    /** {@code value} when it is a {@code yyyy-MM-dd} date, else null. */
    static String dateText(String value) {
        return date(value) == null ? null : value;
    }

    /** An ISO-8601 instant with an offset ({@code 2026-10-13T19:10:43.6568Z}), else a bare date at 00:00Z. */
    static Instant instant(String value) {
        if (value == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException e) {
            return date(value);
        }
    }
}
