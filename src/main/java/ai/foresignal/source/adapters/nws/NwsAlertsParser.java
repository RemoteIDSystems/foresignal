package ai.foresignal.source.adapters.nws;

import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.CountyGeometries;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Geometry;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses the NWS {@code /alerts/active} GeoJSON into {@code nws-alert} records (prompt 047).
 *
 * <p>A pure function of the response bytes, the county lookup and the severity floor (adapter rule
 * 3), so it is tested against recorded fixtures with no Spring context.
 *
 * <p><strong>Time base</strong> (Core-Data-Model §2, NWS row): {@code observedAt} = {@code sent},
 * {@code eventTime} = {@code onset ?? effective}, {@code validFrom} = {@code onset}. {@code validUntil}
 * is the later of {@code expires} and {@code ends}: verified 2026-10-13, 46 of 217 active alerts had an
 * {@code expires} already in the past while {@code ends} was still ahead, and 40 had an
 * {@code onset} after {@code expires}, which the normalizer would reject as an inverted range. When
 * {@code onset} is still after the chosen expiry, {@code validFrom} is left out rather than losing the
 * alert.
 *
 * <p><strong>Geometry:</strong> the polygon when there is one ({@code geometrySource: "polygon"});
 * otherwise, when every UGC code is a county ({@code C}) and {@code ref_admin2} is loaded, the union of
 * the counties named by the SAME codes, simplified to 0.005° ({@code "county-union"}); otherwise none
 * ({@code "none"}, zones stay in {@code ugc}).
 *
 * <p><strong>Supersession:</strong> {@code messageType} {@code Update} and {@code Cancel} name the
 * alerts they replace in {@code references[].identifier}; those ids are reported for tombstoning, and a
 * referenced alert still listed in the same response is dropped from it.
 *
 * <p><strong>Untrusted text:</strong> {@code description} and {@code instruction} are upstream prose.
 * They are embedded and searchable, but are data in any LLM context (prompt 028's citation rules).
 * Every string is capped at {@value #MAX_STRING_BYTES} UTF-8 bytes and ends with {@code …} when cut.
 */
public final class NwsAlertsParser {

    /** The record kind. */
    public static final String KIND = "nws-alert";

    /** The longest string kept, in UTF-8 bytes (the record limit). */
    public static final int MAX_STRING_BYTES = 8 * 1024;

    /** Payload {@code geometrySource} for an upstream polygon. */
    public static final String GEOMETRY_POLYGON = "polygon";

    /** Payload {@code geometrySource} for a county union. */
    public static final String GEOMETRY_COUNTY_UNION = "county-union";

    /** Payload {@code geometrySource} when there is no geometry. */
    public static final String GEOMETRY_NONE = "none";

    /** CAP severities from most to least severe. */
    public static final List<String> SEVERITIES = List.of("Extreme", "Severe", "Moderate", "Minor", "Unknown");

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final GeoJsonReader GEOJSON = new GeoJsonReader();
    private static final Pattern LEADING_NUMBER = Pattern.compile("^\\s*([0-9]+(?:\\.[0-9]+)?)");
    private static final Pattern UGC = Pattern.compile("^[A-Z]{2}[CZ]\\d{3}$");
    private static final Pattern SAME = Pattern.compile("^\\d{6}$");

    private NwsAlertsParser() {}

    /**
     * Parses a response.
     *
     * @param json the response body
     * @param counties county outlines for alerts without a polygon
     * @param minSeverity the lowest CAP severity kept, or null for every alert
     * @return the alerts, the superseded ids and counts
     * @throws UpstreamFormatException when the body is not a GeoJSON FeatureCollection
     */
    public static ParseResult parse(byte[] json, CountyGeometries counties, String minSeverity) {
        if (json == null || json.length == 0) {
            throw new UpstreamFormatException("NWS response is empty", "");
        }
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("NWS response is not valid JSON: " + e.getOriginalMessage(), text(json), e);
        }
        if (root == null || !root.isObject() || !"FeatureCollection".equals(string(root.get("type")))) {
            throw new UpstreamFormatException("NWS response is not a GeoJSON FeatureCollection", text(json));
        }
        JsonNode features = root.get("features");
        if (features == null || !features.isArray()) {
            throw new UpstreamFormatException("NWS FeatureCollection has no 'features' array", text(json));
        }
        CountyGeometries lookup = counties == null ? CountyGeometries.none() : counties;
        int floor = minSeverity == null ? SEVERITIES.size() - 1 : SEVERITIES.indexOf(minSeverity);
        if (floor < 0) {
            floor = SEVERITIES.size() - 1;
        }

        Map<String, NormalizedRecord> alerts = new LinkedHashMap<>();
        Map<String, Instant> superseded = new LinkedHashMap<>();
        int skippedMalformed = 0;
        int skippedSeverity = 0;
        int countyUnions = 0;
        int noGeometry = 0;
        for (JsonNode feature : features) {
            JsonNode p = feature == null ? null : feature.get("properties");
            if (p == null || !p.isObject()) {
                skippedMalformed++;
                continue;
            }
            String id = string(p.get("id"));
            Instant sent = instant(p.get("sent"));
            if (id == null || id.isBlank() || sent == null) {
                skippedMalformed++;
                continue;
            }
            String messageType = string(p.get("messageType"));
            if ("Update".equals(messageType) || "Cancel".equals(messageType)) {
                for (JsonNode ref : iterable(p.get("references"))) {
                    String refId = string(ref.get("identifier"));
                    if (refId != null && !refId.isBlank() && !refId.equals(id)) {
                        superseded.merge(refId, sent, (a, b) -> a.isAfter(b) ? a : b);
                    }
                }
            }
            String severity = string(p.get("severity"));
            int rank = severity == null ? SEVERITIES.size() - 1 : SEVERITIES.indexOf(severity);
            if (rank < 0) {
                rank = SEVERITIES.size() - 1;
            }
            if (rank > floor) {
                skippedSeverity++;
                continue;
            }
            Alert alert = alert(feature, p, id, sent, lookup);
            if (alert == null) {
                skippedMalformed++;
                continue;
            }
            if (GEOMETRY_COUNTY_UNION.equals(alert.geometrySource())) {
                countyUnions++;
            } else if (GEOMETRY_NONE.equals(alert.geometrySource())) {
                noGeometry++;
            }
            alerts.put(id, alert.record());
        }
        int droppedSuperseded = 0;
        for (String refId : superseded.keySet()) {
            if (alerts.remove(refId) != null) {
                droppedSuperseded++;
            }
        }
        return new ParseResult(
                List.copyOf(alerts.values()),
                Collections.unmodifiableMap(superseded),
                new ParseDiagnostics(features.size(), alerts.size(), skippedMalformed, skippedSeverity,
                        droppedSuperseded, countyUnions, noGeometry));
    }

    private static Alert alert(JsonNode feature, JsonNode p, String id, Instant sent, CountyGeometries counties) {
        Instant effective = instant(p.get("effective"));
        Instant onset = instant(p.get("onset"));
        Instant expires = instant(p.get("expires"));
        Instant ends = instant(p.get("ends"));
        Instant validUntil = later(expires, ends);
        Instant validFrom = onset != null && validUntil != null && onset.isAfter(validUntil) ? null : onset;

        Geometry geometry = null;
        String geometrySource = GEOMETRY_NONE;
        JsonNode geometryNode = feature.get("geometry");
        if (geometryNode != null && geometryNode.isObject()) {
            try {
                geometry = GEOJSON.read(geometryNode);
                geometrySource = GEOMETRY_POLYGON;
            } catch (RuntimeException e) {
                return null;
            }
        }
        JsonNode geocode = p.path("geocode");
        List<String> ugc = strings(geocode.get("UGC"));
        List<String> same = strings(geocode.get("SAME"));
        if (geometry == null && !ugc.isEmpty() && counties.available() && allCounties(ugc)) {
            Set<String> fips = new LinkedHashSet<>();
            for (String code : same) {
                if (SAME.matcher(code).matches()) {
                    fips.add(code.substring(1));
                }
            }
            Optional<Geometry> union = counties.union(fips);
            if (union.isPresent()) {
                geometry = union.get();
                geometrySource = GEOMETRY_COUNTY_UNION;
            }
        }

        JsonNode parameters = p.path("parameters");
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND)
                .observedAt(sent)
                .eventTime(onset != null ? onset : effective)
                .validFrom(validFrom)
                .validUntil(validUntil)
                .geometry(geometry);
        b.put("event", text(p.get("event")));
        b.put("eventCode", eventCode(p.get("eventCode")));
        b.put("headline", text(p.get("headline")));
        b.put("severity", text(p.get("severity")));
        b.put("certainty", text(p.get("certainty")));
        b.put("urgency", text(p.get("urgency")));
        b.put("messageType", text(p.get("messageType")));
        b.put("category", text(p.get("category")));
        b.put("response", text(p.get("response")));
        b.put("senderName", text(p.get("senderName")));
        b.put("areaDesc", text(p.get("areaDesc")));
        b.put("sent", sent.toString());
        b.put("effective", iso(effective));
        b.put("onset", iso(onset));
        b.put("expires", iso(expires));
        b.put("ends", iso(ends));
        b.put("ugc", ugc.isEmpty() ? null : ugc);
        b.put("same", same.isEmpty() ? null : same);
        List<String> vtec = strings(parameters.get("VTEC"));
        b.put("vtec", vtec.isEmpty() ? null : vtec);
        b.put("motion", text(first(parameters.get("eventMotionDescription"))));
        b.put("maxHailSizeIn", number(first(parameters.get("maxHailSize"))));
        b.put("maxWindGustMph", number(first(parameters.get("maxWindGust"))));
        b.put("tornadoDetection", text(first(parameters.get("tornadoDetection"))));
        b.put("thunderstormDamageThreat", text(first(parameters.get("thunderstormDamageThreat"))));
        b.put("description", text(p.get("description")));
        b.put("instruction", text(p.get("instruction")));
        b.put("web", text(p.get("web")));
        b.put("geometrySource", geometrySource);
        return new Alert(b.build(), geometrySource);
    }

    /** True when every UGC code names a county ({@code SSC###}). */
    static boolean allCounties(List<String> ugc) {
        for (String code : ugc) {
            if (!UGC.matcher(code).matches() || code.charAt(2) != 'C') {
                return false;
            }
        }
        return true;
    }

    /** {@code {same, nws}} from {@code eventCode.SAME[0]} and {@code eventCode.NationalWeatherService[0]}. */
    private static Map<String, Object> eventCode(JsonNode node) {
        if (node == null || !node.isObject()) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        String same = text(first(node.get("SAME")));
        String nws = text(first(node.get("NationalWeatherService")));
        if (same != null) {
            out.put("same", same);
        }
        if (nws != null) {
            out.put("nws", nws);
        }
        return out.isEmpty() ? null : out;
    }

    /**
     * Caps a string at {@value #MAX_STRING_BYTES} UTF-8 bytes, ending a cut string with {@code …}; null
     * for a blank or missing value.
     */
    static String truncate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.getBytes(StandardCharsets.UTF_8).length <= MAX_STRING_BYTES) {
            return value;
        }
        int budget = MAX_STRING_BYTES - "…".getBytes(StandardCharsets.UTF_8).length;
        StringBuilder out = new StringBuilder();
        int used = 0;
        for (int i = 0; i < value.length(); ) {
            int cp = value.codePointAt(i);
            int size = new String(Character.toChars(cp)).getBytes(StandardCharsets.UTF_8).length;
            if (used + size > budget) {
                break;
            }
            out.appendCodePoint(cp);
            used += size;
            i += Character.charCount(cp);
        }
        return out.append('…').toString();
    }

    /** The leading number of a parameter such as {@code "1.00"} or {@code "60 MPH"}, or null. */
    static Double number(JsonNode node) {
        String value = string(node);
        if (value == null) {
            return null;
        }
        Matcher m = LEADING_NUMBER.matcher(value);
        return m.find() ? Double.valueOf(m.group(1)) : null;
    }

    private static Instant later(Instant a, Instant b) {
        if (a == null) {
            return b;
        }
        return b == null || a.isAfter(b) ? a : b;
    }

    /** An ISO-8601 timestamp with offset as an instant, or null. */
    static Instant instant(JsonNode node) {
        String value = string(node);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String iso(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    private static JsonNode first(JsonNode node) {
        return node != null && node.isArray() && !node.isEmpty() ? node.get(0) : null;
    }

    private static List<String> strings(JsonNode node) {
        List<String> out = new ArrayList<>();
        for (JsonNode item : iterable(node)) {
            String value = string(item);
            if (value != null && !value.isBlank()) {
                out.add(truncate(value.strip()));
            }
        }
        return out;
    }

    private static Iterable<JsonNode> iterable(JsonNode node) {
        return node != null && node.isArray() ? node : List.of();
    }

    private static String text(JsonNode node) {
        String value = string(node);
        return value == null ? null : truncate(value.strip());
    }

    private static String string(JsonNode node) {
        return node != null && node.isString() ? node.stringValue() : null;
    }

    private static String text(byte[] json) {
        int length = Math.min(json.length, UpstreamFormatException.MAX_SNIPPET_LENGTH * 4);
        return new String(json, 0, length, StandardCharsets.UTF_8);
    }

    private record Alert(NormalizedRecord record, String geometrySource) {}

    /**
     * One parsed response.
     *
     * @param alerts the kept alerts, in feed order, never tombstones
     * @param superseded ids named by {@code Update} or {@code Cancel} references, with the newest
     *     referencing {@code sent}
     * @param diagnostics counts
     */
    public record ParseResult(List<NormalizedRecord> alerts, Map<String, Instant> superseded,
            ParseDiagnostics diagnostics) {

        /** The ids of the kept alerts. */
        public Set<String> ids() {
            Set<String> ids = new LinkedHashSet<>();
            alerts.forEach(r -> ids.add(r.externalId()));
            return ids;
        }
    }

    /**
     * What one parse kept and skipped.
     *
     * @param features features in the document
     * @param alerts alerts kept
     * @param skippedMalformed features without properties, id or {@code sent}, or with an unreadable
     *     geometry
     * @param skippedSeverity alerts below {@code minSeverity}
     * @param droppedSuperseded alerts dropped because another alert in the response superseded them
     * @param countyUnions alerts drawn as a county union
     * @param noGeometry alerts kept without geometry
     */
    public record ParseDiagnostics(int features, int alerts, int skippedMalformed, int skippedSeverity,
            int droppedSuperseded, int countyUnions, int noGeometry) {}
}
