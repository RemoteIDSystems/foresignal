package ai.foresignal.source.adapters.mrms;

import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.AreaOfInterest;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses one MRMS ProbSevere v3 file ({@code MRMS_PROBSEVERE_yyyyMMdd_HHmmss.json}) into
 * {@code probsevere-object} records (prompt 047).
 *
 * <p>A pure function of the bytes and the AOI envelope. The header's {@code validTime}
 * ({@code "20261012_235841 UTC"}) is every record's {@code observedAt}; {@code eventTime} is null
 * (continuous state, adapter rule 10). <strong>Every feature property is a string</strong> (verified
 * 2026-10-12, 86 features), so each is converted to a number here.
 *
 * <p>Only features whose polygon's bounding box intersects the AOI envelope are kept. Values are
 * quantized so noise does not mint versions: MESH to 0.25 in, composite reflectivity to 1 dBZ, motion
 * speed to 5 km/h and bearing to 10°. The environmental fields that drift every scan ({@code muCape},
 * {@code mlCape}, {@code ebShearKt}, {@code srh01km}, {@code flashRate}) are declared volatile by the
 * adapter. Every payload carries {@code automated: true} and {@code notAWarning: true}: ProbSevere is
 * automated guidance, and NWS warnings stay authoritative.
 */
public final class ProbSevereParser {

    /** The record kind. */
    public static final String KIND = "probsevere-object";

    /** The header time format, without the {@code " UTC"} suffix. */
    static final DateTimeFormatter VALID_TIME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final GeoJsonReader GEOJSON = new GeoJsonReader();

    private ProbSevereParser() {}

    /**
     * Parses a file.
     *
     * @param json the file
     * @param envelope the AOI {@code [minLon, minLat, maxLon, maxLat]}
     * @return the valid time, the kept objects and counts
     * @throws UpstreamFormatException when the file is not a ProbSevere FeatureCollection with a valid
     *     time
     */
    public static ParseResult parse(byte[] json, double[] envelope) {
        if (json == null || json.length == 0) {
            throw new UpstreamFormatException("ProbSevere file is empty", "");
        }
        JsonNode root;
        try {
            root = JSON.readTree(json);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("ProbSevere file is not valid JSON: " + e.getOriginalMessage(), text(json), e);
        }
        if (root == null || !root.isObject() || !"FeatureCollection".equals(string(root.get("type")))) {
            throw new UpstreamFormatException("ProbSevere file is not a GeoJSON FeatureCollection", text(json));
        }
        Instant validTime = validTime(string(root.get("validTime")));
        if (validTime == null) {
            throw new UpstreamFormatException("ProbSevere file has no readable validTime", text(json));
        }
        JsonNode features = root.get("features");
        if (features == null || !features.isArray()) {
            throw new UpstreamFormatException("ProbSevere FeatureCollection has no 'features' array", text(json));
        }
        Map<String, NormalizedRecord> kept = new LinkedHashMap<>();
        int outside = 0;
        int malformed = 0;
        for (JsonNode feature : features) {
            JsonNode p = feature == null ? null : feature.get("properties");
            String id = p == null ? null : string(p.get("ID"));
            if (id == null || id.isBlank() || feature.get("geometry") == null || !feature.get("geometry").isObject()) {
                malformed++;
                continue;
            }
            Geometry geometry;
            try {
                geometry = GEOJSON.read(feature.get("geometry"));
            } catch (RuntimeException e) {
                malformed++;
                continue;
            }
            Envelope box = geometry.getEnvelopeInternal();
            if (box.isNull() || !AreaOfInterest.intersects(envelope, box.getMinX(), box.getMinY(), box.getMaxX(), box.getMaxY())) {
                outside++;
                continue;
            }
            kept.put(id.strip(), record(id.strip(), p, geometry, validTime));
        }
        return new ParseResult(validTime, List.copyOf(kept.values()),
                new ParseDiagnostics(features.size(), kept.size(), outside, malformed));
    }

    private static NormalizedRecord record(String id, JsonNode p, Geometry geometry, Instant validTime) {
        Long probSevere = integer(p.get("ProbSevere"));
        Long probTor = integer(p.get("ProbTor"));
        NormalizedRecord.Builder b = NormalizedRecord.builder(id, KIND)
                .observedAt(validTime)
                .geometry(geometry);
        b.put("probSevere", probSevere);
        b.put("probTor", probTor);
        b.put("probHail", integer(p.get("ProbHail")));
        b.put("probWind", integer(p.get("ProbWind")));
        Double mesh = number(p.get("MESH"));
        b.put("meshIn", mesh == null ? null : Math.round(mesh * 4.0) / 4.0);
        Double compRef = number(p.get("COMPREF"));
        b.put("compRefDbz", compRef == null ? null : Math.round(compRef));
        b.put("vil", number(p.get("VIL")));
        b.put("echoTop50Kft", number(p.get("EchoTop_50")));
        b.put("flashRate", number(p.get("FLASH_RATE")));
        b.put("muCape", number(p.get("MUCAPE")));
        b.put("mlCape", number(p.get("MLCAPE")));
        b.put("ebShearKt", number(p.get("EBSHEAR")));
        b.put("srh01km", number(p.get("SRH01KM")));
        b.put("motion", motion(number(p.get("MOTION_EAST")), number(p.get("MOTION_SOUTH"))));
        b.put("label", label(probSevere, probTor));
        b.put("automated", true);
        b.put("notAWarning", true);
        return b.build();
    }

    /**
     * {@code {speedKmh, bearingDeg}} from the eastward and southward components in m/s: speed to the
     * nearest 5 km/h, the bearing the object moves toward (0 = north, clockwise) to the nearest 10°.
     * The bearing is left out when the rounded speed is 0.
     */
    static Map<String, Object> motion(Double eastMs, Double southMs) {
        if (eastMs == null || southMs == null) {
            return null;
        }
        double north = -southMs;
        double speedKmh = Math.hypot(eastMs, north) * 3.6;
        long speed = Math.round(speedKmh / 5.0) * 5L;
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("speedKmh", speed);
        if (speed > 0) {
            double bearing = Math.toDegrees(Math.atan2(eastMs, north));
            if (bearing < 0) {
                bearing += 360.0;
            }
            long rounded = (Math.round(bearing / 10.0) * 10L) % 360L;
            out.put("bearingDeg", rounded);
        }
        return out;
    }

    /** {@code "ProbSevere 44% (tor 26%)"}. */
    static String label(Long probSevere, Long probTor) {
        if (probSevere == null) {
            return null;
        }
        return "ProbSevere " + probSevere + "%" + (probTor == null ? "" : " (tor " + probTor + "%)");
    }

    /** {@code "20261010_000041 UTC"} as an instant, or null. */
    static Instant validTime(String value) {
        if (value == null) {
            return null;
        }
        String stamp = value.strip();
        if (stamp.endsWith("UTC")) {
            stamp = stamp.substring(0, stamp.length() - 3).strip();
        }
        try {
            return LocalDateTime.parse(stamp, VALID_TIME).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** A string property as a finite number, or null when absent, blank or not numeric. */
    static Double number(JsonNode node) {
        String value = node == null ? null : node.isString() ? node.stringValue() : node.isNumber() ? node.asString() : null;
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            double d = Double.parseDouble(value.strip());
            return Double.isFinite(d) ? d : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** A string property rounded to an integer, or null. */
    static Long integer(JsonNode node) {
        Double d = number(node);
        return d == null ? null : Math.round(d);
    }

    private static String string(JsonNode node) {
        return node != null && node.isString() ? node.stringValue() : null;
    }

    private static String text(byte[] json) {
        int length = Math.min(json.length, UpstreamFormatException.MAX_SNIPPET_LENGTH * 4);
        return new String(json, 0, length, StandardCharsets.UTF_8);
    }

    /**
     * One parsed file.
     *
     * @param validTime the header's valid time
     * @param records the objects inside the AOI
     * @param diagnostics counts
     */
    public record ParseResult(Instant validTime, List<NormalizedRecord> records, ParseDiagnostics diagnostics) {

        /** The kept ids. */
        public Set<String> ids() {
            Set<String> ids = new LinkedHashSet<>();
            records.forEach(r -> ids.add(r.externalId()));
            return ids;
        }
    }

    /**
     * What one parse kept.
     *
     * @param features features in the file
     * @param kept objects inside the AOI
     * @param outsideAoi objects outside it
     * @param malformed features without an id or a readable polygon
     */
    public record ParseDiagnostics(int features, int kept, int outsideAoi, int malformed) {}
}
