package ai.foresignal.source.adapters.spc;

import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Geometry;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses one SPC outlook layer ({@code day{d}otlk_{type}.lyr.geojson}) into {@code spc-outlook}
 * records (prompt 047).
 *
 * <p>A pure function of the bytes, the day, the layer type and the fetch instant. Each feature is one
 * risk area: {@code externalId} = {@code day{d}:{type}:{LABEL}} (for example {@code day1:cat:MDT},
 * {@code day1:torn:0.10}, {@code day1:sigtorn:SIGN}); {@code observedAt} = {@code ISSUE},
 * {@code eventTime} = {@code VALID}, {@code validUntil} = {@code EXPIRE}, all {@code yyyyMMddHHmm}
 * UTC. Payload {@code dn} is SPC's numeric rank (categorical: TSTM 2, MRGL 3, SLGT 4, ENH 5, MDT 6,
 * HIGH 8), so a rule can say {@code dn >= 6}.
 *
 * <p>Verified 2026-10-12: a layer with no risk area holds one placeholder feature with an empty
 * {@code GeometryCollection} (label {@code "Less Than 2% All Areas"} or empty), and an unused
 * significant-severe layer may keep a months-old feature (day 3 {@code sigprob} still carried a March
 * 2026 polygon). Features with no area, and features that expired before the fetch, are skipped, so
 * they count as absent and their previous records are tombstoned.
 */
public final class SpcOutlookParser {

    /** The record kind. */
    public static final String KIND = "spc-outlook";

    /** The {@code ISSUE}/{@code VALID}/{@code EXPIRE} format (UTC). */
    static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final GeoJsonReader GEOJSON = new GeoJsonReader();

    private SpcOutlookParser() {}

    /** The external id of one risk area. */
    public static String externalId(int day, String type, String label) {
        return "day" + day + ":" + type + ":" + label;
    }

    /**
     * Parses a layer.
     *
     * @param json the file
     * @param day 1, 2 or 3
     * @param type the layer type ({@code cat}, {@code torn}, {@code prob}, …)
     * @param now the fetch instant; features expired before it are skipped
     * @return the risk areas and counts
     * @throws UpstreamFormatException when the file is not a FeatureCollection
     */
    public static ParseResult parse(byte[] json, int day, String type, Instant now) {
        if (json == null || json.length == 0) {
            throw new UpstreamFormatException("SPC layer is empty", "");
        }
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("SPC layer is not valid JSON: " + e.getOriginalMessage(), text(json), e);
        }
        if (root == null || !root.isObject() || !"FeatureCollection".equals(string(root.get("type")))) {
            throw new UpstreamFormatException("SPC layer is not a GeoJSON FeatureCollection", text(json));
        }
        JsonNode features = root.get("features");
        if (features == null || !features.isArray()) {
            throw new UpstreamFormatException("SPC FeatureCollection has no 'features' array", text(json));
        }
        Map<String, NormalizedRecord> byLabel = new LinkedHashMap<>();
        Map<String, Geometry> geometries = new LinkedHashMap<>();
        int empty = 0;
        int expired = 0;
        int malformed = 0;
        for (JsonNode feature : features) {
            JsonNode p = feature == null ? null : feature.get("properties");
            if (p == null || !p.isObject()) {
                malformed++;
                continue;
            }
            String label = string(p.get("LABEL"));
            Geometry geometry;
            try {
                JsonNode g = feature.get("geometry");
                geometry = g == null || g.isNull() ? null : GEOJSON.read(g);
            } catch (RuntimeException e) {
                malformed++;
                continue;
            }
            if (geometry == null || geometry.isEmpty() || label == null || label.isBlank()) {
                empty++;
                continue;
            }
            Instant issue = stamp(p.get("ISSUE"));
            Instant valid = stamp(p.get("VALID"));
            Instant expire = stamp(p.get("EXPIRE"));
            if (issue == null) {
                malformed++;
                continue;
            }
            if (expire != null && now != null && !expire.isAfter(now)) {
                expired++;
                continue;
            }
            String id = externalId(day, type, label.strip());
            Geometry merged = geometries.containsKey(id) ? geometries.get(id).union(geometry) : geometry;
            merged.setSRID(4326);
            geometries.put(id, merged);
            byLabel.put(id, record(id, day, type, label.strip(), p, issue, valid, expire, merged));
        }
        return new ParseResult(List.copyOf(byLabel.values()),
                new ParseDiagnostics(features.size(), byLabel.size(), empty, expired, malformed));
    }

    private static NormalizedRecord record(String id, int day, String type, String label, JsonNode p, Instant issue,
            Instant valid, Instant expire, Geometry geometry) {
        String label2 = string(p.get("LABEL2"));
        String title = "Day " + day + " " + (label2 == null || label2.isBlank() ? label : label2.strip());
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND)
                .observedAt(issue)
                .eventTime(valid)
                .validUntil(expire)
                .geometry(geometry);
        b.put("day", (long) day);
        b.put("type", type);
        b.put("label", label);
        b.put("label2", label2 == null || label2.isBlank() ? null : label2.strip());
        JsonNode dn = p.get("DN");
        b.put("dn", dn != null && dn.isNumber() ? dn.longValue() : null);
        b.put("issue", issue.toString());
        b.put("valid", valid == null ? null : valid.toString());
        b.put("expire", expire == null ? null : expire.toString());
        b.put("title", title);
        return b.build();
    }

    /** {@code yyyyMMddHHmm} (UTC) as an instant, or null. */
    static Instant stamp(JsonNode node) {
        String value = string(node);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.strip(), STAMP).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String string(JsonNode node) {
        return node != null && node.isString() ? node.stringValue() : null;
    }

    private static String text(byte[] json) {
        int length = Math.min(json.length, UpstreamFormatException.MAX_SNIPPET_LENGTH * 4);
        return new String(json, 0, length, StandardCharsets.UTF_8);
    }

    /**
     * One parsed layer.
     *
     * @param records the risk areas, one per label
     * @param diagnostics counts
     */
    public record ParseResult(List<NormalizedRecord> records, ParseDiagnostics diagnostics) {

        /** The ids of the risk areas. */
        public Set<String> ids() {
            Set<String> ids = new LinkedHashSet<>();
            records.forEach(r -> ids.add(r.externalId()));
            return ids;
        }

        /** The records as a mutable list. */
        public List<NormalizedRecord> mutableRecords() {
            return new ArrayList<>(records);
        }
    }

    /**
     * What one parse kept.
     *
     * @param features features in the file
     * @param kept risk areas kept
     * @param empty placeholder features without an area or label
     * @param expired features whose {@code EXPIRE} was not after the fetch
     * @param malformed features without properties, {@code ISSUE} or a readable geometry
     */
    public record ParseDiagnostics(int features, int kept, int empty, int expired, int malformed) {}
}
