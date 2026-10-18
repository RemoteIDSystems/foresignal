package ai.foresignal.stix;

import ai.foresignal.media.MediaService;
import ai.foresignal.store.query.RecordRow;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Maps cyber records to STIX 2.1 objects (prompt 141). Pure: no I/O, no clock.
 *
 * <ul>
 *   <li><b>{@value #VULNERABILITY_KIND}</b> ({@code cisa-kev}, {@code nvd-cves}) → {@code vulnerability}: the CVE id
 *       as {@code name} and as an {@code external_references} entry {@code {source_name: "cve", external_id}}, the
 *       description ({@code shortDescription} or {@code description}), label {@code kev} for KEV entries.
 *   <li><b>{@value #URL_KIND}</b> (abuse.ch URLhaus) → {@code indicator} with pattern {@code [url:value = '…']}.
 *   <li><b>{@value #C2_KIND}</b> (abuse.ch Feodo Tracker, id {@code ip:port}) → {@code indicator} with pattern
 *       {@code [network-traffic:dst_ref.type = 'ipv4-addr' AND network-traffic:dst_ref.value = '…' AND
 *       network-traffic:dst_port = …]} ({@code ipv6-addr} for an IPv6 address), or {@code [ipv4-addr:value = '…']}
 *       without a port.
 * </ul>
 *
 * <p>Every object has {@code spec_version: "2.1"}, {@code created_by_ref} the Foresignal {@link #identity()}, and
 * millisecond UTC timestamps: {@code created} is the record's event time (first seen, date added) when it is not after
 * its observed time, else the observed time; {@code modified} is the observed time. {@code x_foresignal_ref} carries
 * the record's object reference. Indicators have {@code indicator_types: ["malicious-activity"]} and
 * {@code valid_from} = {@code created}. Pattern literals escape {@code \} and {@code '}.
 */
public final class StixMapper {

    /** The vulnerability kind (076). */
    public static final String VULNERABILITY_KIND = "vulnerability";

    /** The URLhaus malware URL kind (076). */
    public static final String URL_KIND = "malware-url";

    /** The Feodo Tracker C2 kind (076). */
    public static final String C2_KIND = "malware-c2";

    /** The kinds this mapper understands. */
    public static final Set<String> KINDS = Set.of(VULNERABILITY_KIND, URL_KIND, C2_KIND);

    /** The STIX media type. */
    public static final String STIX_MEDIA_TYPE = "application/stix+json;version=2.1";

    /** The fixed creation time of the Foresignal identity (the product's naming date). */
    static final Instant IDENTITY_CREATED = Instant.parse("2026-10-11T00:00:00Z");

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    private static final Pattern IPV4 = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private StixMapper() {}

    /** The id of the Foresignal identity. */
    public static String identityId() {
        return StixIds.id("identity", "foresignal");
    }

    /** The Foresignal {@code identity} object, the creator of every exported object. */
    public static Map<String, Object> identity() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", "identity");
        out.put("spec_version", "2.1");
        out.put("id", identityId());
        out.put("created", timestamp(IDENTITY_CREATED));
        out.put("modified", timestamp(IDENTITY_CREATED));
        out.put("name", "Foresignal");
        out.put("description", "Foresignal intelligence broker; objects are derived from the records of the named"
                + " upstream sources.");
        out.put("identity_class", "system");
        return out;
    }

    /** The STIX id a record exports under: UUIDv5 of its object reference, typed by its kind. */
    public static String idOf(RecordRow row) {
        String type = VULNERABILITY_KIND.equals(row.kind()) ? "vulnerability" : "indicator";
        return StixIds.id(type, MediaService.recordRef(row.sourceId(), row.externalId()));
    }

    /**
     * The STIX object of {@code row}, or null for a tombstone, an unknown kind, or a record whose IOC cannot be
     * expressed (no URL, an unreadable address).
     */
    public static Map<String, Object> toStix(RecordRow row) {
        if (row == null || row.tombstone() || !KINDS.contains(row.kind())) {
            return null;
        }
        JsonNode payload = payload(row);
        return switch (row.kind()) {
            case VULNERABILITY_KIND -> vulnerability(row, payload);
            case URL_KIND -> urlIndicator(row, payload);
            case C2_KIND -> c2Indicator(row, payload);
            default -> null;
        };
    }

    private static Map<String, Object> vulnerability(RecordRow row, JsonNode payload) {
        String cve = text(payload, "cveId");
        if (cve == null) {
            cve = row.externalId();
        }
        Map<String, Object> out = common("vulnerability", row);
        out.put("name", cve);
        String description = text(payload, "shortDescription");
        if (description == null) {
            description = text(payload, "description");
        }
        if (description != null) {
            out.put("description", description);
        }
        List<Map<String, Object>> references = new ArrayList<>();
        Map<String, Object> cveRef = new LinkedHashMap<>();
        cveRef.put("source_name", "cve");
        cveRef.put("external_id", cve);
        references.add(cveRef);
        out.put("external_references", references);
        if (payload.path("kev").asBoolean(false)) {
            out.put("labels", List.of("kev"));
        }
        JsonNode score = payload.get("cvssScore");
        if (score != null && score.isNumber()) {
            out.put("x_foresignal_cvss_score", score.asDouble());
        }
        out.put("x_foresignal_ref", MediaService.recordRef(row.sourceId(), row.externalId()));
        return out;
    }

    private static Map<String, Object> urlIndicator(RecordRow row, JsonNode payload) {
        String url = text(payload, "url");
        if (url == null) {
            return null;
        }
        Map<String, Object> out = common("indicator", row);
        String defanged = text(payload, "urlDefanged");
        out.put("name", "Malware URL " + (defanged == null ? url : defanged));
        String threat = text(payload, "threat");
        if (threat != null) {
            out.put("description", "URLhaus threat: " + threat);
        }
        out.put("indicator_types", List.of("malicious-activity"));
        out.put("pattern", "[url:value = '" + literal(url) + "']");
        out.put("pattern_type", "stix");
        out.put("pattern_version", "2.1");
        out.put("valid_from", out.get("created"));
        List<String> labels = strings(payload.get("tags"));
        if (!labels.isEmpty()) {
            out.put("labels", labels);
        }
        String reference = text(payload, "reference");
        if (reference != null) {
            Map<String, Object> ref = new LinkedHashMap<>();
            ref.put("source_name", "urlhaus");
            ref.put("url", reference);
            out.put("external_references", List.of(ref));
        }
        out.put("x_foresignal_ref", MediaService.recordRef(row.sourceId(), row.externalId()));
        return out;
    }

    private static Map<String, Object> c2Indicator(RecordRow row, JsonNode payload) {
        String ip = text(payload, "ip");
        Integer port = payload.path("port").isNumber() ? payload.get("port").asInt() : null;
        if (ip == null) {
            String[] parts = splitHostPort(row.externalId());
            ip = parts[0];
            if (port == null && parts[1] != null) {
                try {
                    port = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {
                    // no usable port
                }
            }
        }
        if (ip == null || ip.isBlank()) {
            return null;
        }
        ip = ip.strip();
        if (ip.startsWith("[") && ip.endsWith("]")) {
            ip = ip.substring(1, ip.length() - 1);
        }
        String addressType = IPV4.matcher(ip).matches() ? "ipv4-addr" : ip.contains(":") ? "ipv6-addr" : null;
        if (addressType == null) {
            return null;
        }
        Map<String, Object> out = common("indicator", row);
        String malware = text(payload, "malware");
        out.put("name", "C2 server " + row.externalId() + (malware == null ? "" : " (" + malware + ")"));
        if (malware != null) {
            out.put("description", "Feodo Tracker botnet C2 server for " + malware);
        }
        out.put("indicator_types", List.of("malicious-activity"));
        String pattern = port == null
                ? "[" + addressType + ":value = '" + literal(ip) + "']"
                : "[network-traffic:dst_ref.type = '" + addressType + "' AND network-traffic:dst_ref.value = '"
                        + literal(ip) + "' AND network-traffic:dst_port = " + port + "]";
        out.put("pattern", pattern);
        out.put("pattern_type", "stix");
        out.put("pattern_version", "2.1");
        out.put("valid_from", out.get("created"));
        if (malware != null) {
            out.put("labels", List.of(malware.toLowerCase(java.util.Locale.ROOT)));
        }
        out.put("x_foresignal_ref", MediaService.recordRef(row.sourceId(), row.externalId()));
        return out;
    }

    /** {@code type}, {@code spec_version}, {@code id}, {@code created_by_ref}, {@code created}, {@code modified}. */
    private static Map<String, Object> common(String type, RecordRow row) {
        Instant observed = row.observedAt() != null ? row.observedAt() : row.persistedObservedAt();
        Instant created = row.eventTime() != null && (observed == null || !row.eventTime().isAfter(observed))
                ? row.eventTime() : observed;
        Instant modified = observed == null ? created : observed;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", type);
        out.put("spec_version", "2.1");
        out.put("id", idOf(row));
        out.put("created_by_ref", identityId());
        out.put("created", timestamp(created));
        out.put("modified", timestamp(modified));
        return out;
    }

    /** A STIX timestamp: UTC with exactly three fractional digits and {@code Z}. */
    public static String timestamp(Instant instant) {
        return TIMESTAMP.format(instant);
    }

    /** A STIX pattern string literal body: {@code \} and {@code '} escaped with a backslash. */
    static String literal(String text) {
        return text.replace("\\", "\\\\").replace("'", "\\'");
    }

    /** {@code [host, port]} of {@code host:port} or {@code [v6]:port}; the port is null when absent. */
    static String[] splitHostPort(String value) {
        if (value == null) {
            return new String[] {null, null};
        }
        if (value.startsWith("[")) {
            int close = value.indexOf(']');
            if (close > 0) {
                String host = value.substring(1, close);
                String rest = value.substring(close + 1);
                return new String[] {host, rest.startsWith(":") ? rest.substring(1) : null};
            }
        }
        int colon = value.lastIndexOf(':');
        if (colon > 0 && value.indexOf(':') == colon) {
            return new String[] {value.substring(0, colon), value.substring(colon + 1)};
        }
        return new String[] {value, null};
    }

    private static JsonNode payload(RecordRow row) {
        if (row.payload() == null) {
            return JSON.createObjectNode();
        }
        try {
            JsonNode node = JSON.readTree(row.payload());
            return node == null || !node.isObject() ? JSON.createObjectNode() : node;
        } catch (RuntimeException e) {
            return JSON.createObjectNode();
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.isContainer()) {
            return null;
        }
        String text = value.asString();
        return text == null || text.isBlank() ? null : text.strip();
    }

    private static List<String> strings(JsonNode array) {
        List<String> out = new ArrayList<>();
        if (array != null && array.isArray()) {
            for (JsonNode item : array) {
                if (item.isString() && !item.asString().isBlank()) {
                    out.add(item.asString().strip());
                }
            }
        }
        return out;
    }
}
