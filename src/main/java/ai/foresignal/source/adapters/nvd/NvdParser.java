package ai.foresignal.source.adapters.nvd;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
 * One page of the NVD CVE API 2.0 as {@code vulnerability} records (prompt 076). Pure.
 *
 * <ul>
 *   <li><strong>Identity and time:</strong> {@code externalId = cve.id}, {@code observedAt = lastModified} (not
 *       hashed, so a record whose only change is {@code lastModified} is a touch), {@code eventTime = published};
 *       NVD timestamps carry no zone and are UTC.
 *   <li><strong>Metric selection:</strong> per CVSS version the {@code Primary} entry (NVD's own), else the first.
 *       {@code cvssScore}, the score filtered and ranked on, is the v4.0 base score when present, else v3.1.
 *   <li><strong>Filter</strong> ({@link Filter}): a CVE whose record carries {@code cisaExploitAdd} is always kept;
 *       otherwise its {@code cvssScore} must reach {@code minCvss} (a CVE without a score passes only when
 *       {@code minCvss <= 0}) and, when vendors are configured, one of its CPE vendors must be among them.
 *   <li><strong>Rejected:</strong> {@code vulnStatus = Rejected} is never a record; its id is reported so the adapter
 *       can tombstone it if it was ingested before.
 *   <li><strong>Payload:</strong> {@code cveId, vulnStatus, description} (English, at most {@value #MAX_DESCRIPTION}
 *       characters), {@code cvssV40} and {@code cvssV31} ({@code baseScore, baseSeverity, vectorString}),
 *       {@code cvssScore}, {@code cwes} (at most 10), {@code vendors} and {@code products} (from the vulnerable CPE
 *       match criteria, at most {@value #MAX_CPE} each), {@code references} (at most 10 URLs), {@code kev},
 *       {@code cisaActionDue, cisaRequiredAction}.
 * </ul>
 */
public final class NvdParser {

    /** The record kind (the same kind as {@code cisa-kev}; a different source). */
    public static final String KIND = "vulnerability";

    /** The longest description kept. */
    public static final int MAX_DESCRIPTION = 4096;

    /** The most vendors and products kept. */
    public static final int MAX_CPE = 20;

    /** The most CWEs and references kept. */
    public static final int MAX_LIST = 10;

    /** The status of a withdrawn CVE. */
    public static final String REJECTED = "Rejected";

    static final Pattern CVE = Pattern.compile("^CVE-\\d{4}-\\d{4,}$");
    private static final Pattern CPE_SPLIT = Pattern.compile("(?<!\\\\):");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * The filter parameters.
     *
     * @param minCvss the lowest {@code cvssScore} kept
     * @param vendors CPE vendor names (lower case); empty means every vendor
     */
    public record Filter(double minCvss, Set<String> vendors) {

        /** Lower-cases and copies the vendors. */
        public Filter {
            Set<String> v = new LinkedHashSet<>();
            if (vendors != null) {
                for (String s : vendors) {
                    if (s != null && !s.isBlank()) {
                        v.add(s.strip().toLowerCase(Locale.ROOT));
                    }
                }
            }
            vendors = Set.copyOf(v);
        }
    }

    /**
     * One parsed page.
     *
     * @param totalResults the API's {@code totalResults}
     * @param startIndex the API's {@code startIndex}
     * @param count the CVEs on this page
     * @param records the CVEs kept by the filter
     * @param rejected the ids of rejected CVEs
     * @param skipped reasons CVEs were left out ({@code below-min-cvss}, {@code vendor}, {@code missing-id}, …)
     */
    public record Page(int totalResults, int startIndex, int count, List<NormalizedRecord> records,
            List<String> rejected, Map<String, Integer> skipped) {}

    private NvdParser() {}

    /**
     * Parses one page.
     *
     * @param body the API response
     * @param filter the filter
     * @throws UpstreamFormatException when the body is not an NVD CVE response
     */
    public static Page parse(byte[] body, Filter filter) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("NVD response is not JSON: " + e.getOriginalMessage(), "", e);
        }
        if (root == null || !root.isObject() || !root.path("vulnerabilities").isArray()
                || JsonProps.integer(root, "totalResults") == null) {
            throw new UpstreamFormatException("NVD response has no vulnerabilities array or totalResults", "");
        }
        List<NormalizedRecord> records = new ArrayList<>();
        List<String> rejected = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        for (JsonNode item : root.get("vulnerabilities")) {
            JsonNode cve = item.get("cve");
            String id = cve == null ? null : JsonProps.text(cve, "id");
            if (id == null || !CVE.matcher(id).matches()) {
                skipped.merge("missing-id", 1, Integer::sum);
                continue;
            }
            if (REJECTED.equalsIgnoreCase(JsonProps.text(cve, "vulnStatus"))) {
                rejected.add(id);
                continue;
            }
            String reason = filterReason(cve, filter);
            if (reason != null) {
                skipped.merge(reason, 1, Integer::sum);
                continue;
            }
            NormalizedRecord record = record(cve);
            if (record == null) {
                skipped.merge("missing-observed-at", 1, Integer::sum);
            } else {
                records.add(record);
            }
        }
        return new Page(JsonProps.integer(root, "totalResults").intValue(),
                JsonProps.integer(root, "startIndex") == null ? 0 : JsonProps.integer(root, "startIndex").intValue(),
                root.get("vulnerabilities").size(), records, rejected, skipped);
    }

    /**
     * Why {@code cve} is left out, or null when the filter keeps it.
     *
     * @param cve the {@code cve} object
     * @param filter the filter
     */
    static String filterReason(JsonNode cve, Filter filter) {
        if (cve.hasNonNull("cisaExploitAdd")) {
            return null;
        }
        Double score = cvssScore(cve);
        if (score == null ? filter.minCvss() > 0 : score < filter.minCvss()) {
            return "below-min-cvss";
        }
        if (!filter.vendors().isEmpty()) {
            Set<String> vendors = new LinkedHashSet<>(cpe(cve, 3, Integer.MAX_VALUE));
            vendors.retainAll(filter.vendors());
            if (vendors.isEmpty()) {
                return "vendor";
            }
        }
        return null;
    }

    /**
     * The record of a kept CVE, or null without {@code lastModified}.
     *
     * @param cve the {@code cve} object
     */
    public static NormalizedRecord record(JsonNode cve) {
        Instant lastModified = time(JsonProps.text(cve, "lastModified"));
        if (lastModified == null) {
            return null;
        }
        String id = JsonProps.text(cve, "id");
        Map<String, Object> v40 = metric(cve.path("metrics").get("cvssMetricV40"));
        Map<String, Object> v31 = metric(cve.path("metrics").get("cvssMetricV31"));
        boolean kev = cve.hasNonNull("cisaExploitAdd");
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND).observedAt(lastModified)
                .eventTime(time(JsonProps.text(cve, "published")));
        b.put("cveId", id)
                .put("vulnStatus", JsonProps.text(cve, "vulnStatus"))
                .put("description", description(cve.get("descriptions")))
                .put("cvssV40", v40)
                .put("cvssV31", v31)
                .put("cvssScore", cvssScore(cve))
                .put("cwes", cwes(cve.get("weaknesses")))
                .put("vendors", nullIfEmpty(cpe(cve, 3, MAX_CPE)))
                .put("products", nullIfEmpty(cpe(cve, 4, MAX_CPE)))
                .put("references", references(cve.get("references")))
                .put("kev", kev)
                .put("cisaActionDue", kev ? dateText(JsonProps.text(cve, "cisaActionDue")) : null)
                .put("cisaRequiredAction", kev ? cut(JsonProps.text(cve, "cisaRequiredAction"), 1000) : null);
        return b.build();
    }

    /**
     * The score filtered and ranked on: the selected v4.0 base score, else the selected v3.1 one; null when neither.
     *
     * @param cve the {@code cve} object
     */
    public static Double cvssScore(JsonNode cve) {
        JsonNode metrics = cve.path("metrics");
        JsonNode v40 = select(metrics.get("cvssMetricV40"));
        if (v40 != null && JsonProps.number(v40.get("cvssData"), "baseScore") != null) {
            return JsonProps.number(v40.get("cvssData"), "baseScore");
        }
        JsonNode v31 = select(metrics.get("cvssMetricV31"));
        return v31 == null ? null : JsonProps.number(v31.get("cvssData"), "baseScore");
    }

    /** The {@code Primary} entry of a metric array, else the first; null when empty. */
    static JsonNode select(JsonNode array) {
        if (array == null || !array.isArray() || array.isEmpty()) {
            return null;
        }
        for (JsonNode m : array) {
            if ("Primary".equals(JsonProps.text(m, "type"))) {
                return m;
            }
        }
        return array.get(0);
    }

    /** {@code baseScore, baseSeverity, vectorString} of the selected entry; null when there is none. */
    static Map<String, Object> metric(JsonNode array) {
        JsonNode m = select(array);
        if (m == null || m.get("cvssData") == null) {
            return null;
        }
        JsonNode data = m.get("cvssData");
        Map<String, Object> out = new LinkedHashMap<>();
        Double score = JsonProps.number(data, "baseScore");
        if (score != null) {
            out.put("baseScore", score);
        }
        String severity = JsonProps.text(data, "baseSeverity");
        if (severity == null) {
            severity = JsonProps.text(m, "baseSeverity");
        }
        if (severity != null) {
            out.put("baseSeverity", severity);
        }
        String vector = JsonProps.text(data, "vectorString");
        if (vector != null) {
            out.put("vectorString", vector);
        }
        return out.isEmpty() ? null : out;
    }

    /** The English description, cut to {@value #MAX_DESCRIPTION}; null when there is none. */
    static String description(JsonNode array) {
        if (array == null || !array.isArray()) {
            return null;
        }
        for (JsonNode d : array) {
            if ("en".equalsIgnoreCase(JsonProps.text(d, "lang"))) {
                return cut(JsonProps.text(d, "value"), MAX_DESCRIPTION);
            }
        }
        return null;
    }

    /** The {@code CWE-n} ids of the weaknesses, de-duplicated, at most {@value #MAX_LIST}; null when none. */
    static List<String> cwes(JsonNode weaknesses) {
        Set<String> out = new LinkedHashSet<>();
        if (weaknesses != null && weaknesses.isArray()) {
            for (JsonNode w : weaknesses) {
                for (JsonNode d : w.path("description")) {
                    String v = JsonProps.text(d, "value");
                    if (v != null && v.startsWith("CWE-") && out.size() < MAX_LIST) {
                        out.add(v);
                    }
                }
            }
        }
        return out.isEmpty() ? null : List.copyOf(out);
    }

    /**
     * The distinct values of CPE 2.3 component {@code index} (3 vendor, 4 product) of the vulnerable match criteria,
     * in order, at most {@code max}.
     */
    static List<String> cpe(JsonNode cve, int index, int max) {
        Set<String> out = new LinkedHashSet<>();
        for (JsonNode config : cve.path("configurations")) {
            for (JsonNode node : config.path("nodes")) {
                for (JsonNode match : node.path("cpeMatch")) {
                    if (match.path("vulnerable").isBoolean() && !match.path("vulnerable").booleanValue()) {
                        continue;
                    }
                    String criteria = JsonProps.text(match, "criteria");
                    if (criteria == null) {
                        continue;
                    }
                    String[] parts = CPE_SPLIT.split(criteria);
                    if (parts.length > index && !parts[index].isEmpty() && !"*".equals(parts[index])
                            && !"-".equals(parts[index]) && out.size() < max) {
                        out.add(parts[index].replace("\\", "").toLowerCase(Locale.ROOT));
                    }
                }
            }
        }
        return List.copyOf(out);
    }

    /** The reference URLs (http and https only), de-duplicated, at most {@value #MAX_LIST}; null when none. */
    static List<String> references(JsonNode array) {
        Set<String> out = new LinkedHashSet<>();
        if (array != null && array.isArray()) {
            for (JsonNode r : array) {
                String url = JsonProps.text(r, "url");
                if (url != null && (url.startsWith("https://") || url.startsWith("http://")) && url.length() <= 500
                        && out.size() < MAX_LIST) {
                    out.add(url);
                }
            }
        }
        return out.isEmpty() ? null : List.copyOf(out);
    }

    /** An NVD timestamp ({@code 2026-10-12T21:17:06.410}, UTC; an offset is accepted too), or null. */
    static Instant time(String value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            try {
                return OffsetDateTime.parse(value).toInstant();
            } catch (DateTimeParseException e2) {
                return null;
            }
        }
    }

    private static String dateText(String value) {
        if (value == null) {
            return null;
        }
        try {
            LocalDate.parse(value);
            return value;
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String cut(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max).strip();
    }

    private static List<String> nullIfEmpty(List<String> values) {
        return values.isEmpty() ? null : values;
    }
}
