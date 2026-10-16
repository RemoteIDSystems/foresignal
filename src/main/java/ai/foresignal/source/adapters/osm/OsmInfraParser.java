package ai.foresignal.source.adapters.osm;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Parses RFC 8142 GeoJSON text sequences into {@code osm-infrastructure} records (prompt 104). Only
 * allowlisted tags are copied; the facility kind and the {@code externalId} come from the OSM ids.
 */
public final class OsmInfraParser {

    /** The most vertices kept in a geometry. */
    public static final int MAX_VERTICES = 2_000;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private OsmInfraParser() {}

    /**
     * The outcome of parsing one file.
     *
     * @param records the records
     * @param features the non-blank lines (features) read
     * @param parseErrors the lines that were not valid GeoJSON features
     * @param capExceeded true when the file had more than the feature cap; parsing stopped there
     */
    public record Parsed(List<NormalizedRecord> records, long features, long parseErrors, boolean capExceeded) {}

    /** Parses the lines, keeping power lines at or above {@code minVoltageKv}. */
    public static List<NormalizedRecord> parse(List<String> lines, double minVoltageKv, Instant observedAt) {
        List<NormalizedRecord> out = new ArrayList<>();
        for (String line : lines) {
            parseLine(line, minVoltageKv, observedAt, out);
        }
        return out;
    }

    /**
     * Parses a GeoJSON text sequence line by line without holding the file in memory, stopping once
     * more than {@code maxFeatures} features have been read. RFC 8142 record separators
     * ({@code U+001E}) at the start of a line are ignored.
     */
    public static Parsed parse(BufferedReader reader, double minVoltageKv, Instant observedAt, long maxFeatures)
            throws IOException {
        List<NormalizedRecord> out = new ArrayList<>();
        long features = 0;
        long errors = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank() || line.equals("\u001e")) {
                continue;
            }
            features++;
            if (features > maxFeatures) {
                return new Parsed(List.of(), features, errors, true);
            }
            if (!parseLine(line, minVoltageKv, observedAt, out)) {
                errors++;
            }
        }
        return new Parsed(out, features, errors, false);
    }

    /** Parses one line into {@code out}; false when the line is not a valid GeoJSON feature. */
    private static boolean parseLine(String raw, double minVoltageKv, Instant observedAt, List<NormalizedRecord> out) {
        if (raw == null || raw.isBlank()) {
            return true;
        }
        String line = raw.charAt(0) == '\u001e' ? raw.substring(1) : raw;
        try {
            JsonNode feature = JSON.readTree(line);
            if (feature == null || !feature.isObject()) {
                return false;
            }
            NormalizedRecord record = record(feature, minVoltageKv, observedAt);
            if (record != null) {
                out.add(record);
            }
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    static NormalizedRecord record(JsonNode feature, double minVoltageKv, Instant observedAt) {
        JsonNode properties = feature.get("properties");
        if (properties == null) {
            return null;
        }
        String id = id(feature);
        String kind = kind(properties);
        if (id == null || kind == null) {
            return null;
        }
        Map<String, Object> payload = new java.util.LinkedHashMap<>();
        copy(properties, payload, "name", "name");
        copy(properties, payload, "operator", "operator");
        copy(properties, payload, "operator:wikidata", "operatorWikidata");
        copy(properties, payload, "beds", "beds");
        copy(properties, payload, "emergency", "emergency");
        copy(properties, payload, "iata", "iata");
        copy(properties, payload, "icao", "icao");
        copy(properties, payload, "ref", "ref");
        copy(properties, payload, "osmVersion", "osmVersion");
        Double voltage = maxVoltageKv(properties.get("voltage"));
        if (voltage != null) {
            payload.put("voltageKv", voltage);
        }
        if ("power-line".equals(kind) && voltage != null && voltage < minVoltageKv) {
            return null;
        }
        Double capacity = capacityMw(properties.get("plant:output:electricity"));
        if (capacity != null) {
            payload.put("capacityMw", capacity);
        }
        payload.put("osmType", id.substring(0, 1).equals("n") ? "node" : id.startsWith("w") ? "way" : "relation");
        Geometry geometry = geometry(feature.get("geometry"));
        NormalizedRecord.Builder builder = NormalizedRecord.builder(id, kind).observedAt(observedAt).putAll(payload);
        if (geometry != null) {
            builder.geometry(geometry);
        }
        return builder.build();
    }

    private static String id(JsonNode feature) {
        JsonNode id = feature.get("id");
        if (id == null || id.isNull()) {
            return null;
        }
        return id.isString() ? id.asString() : String.valueOf(id.asLong());
    }

    /** The facility kind from the tags, or null when the feature is not one of them. */
    static String kind(JsonNode properties) {
        if ("substation".equals(text(properties, "power"))) {
            return "substation";
        }
        if ("line".equals(text(properties, "power")) || "cable".equals(text(properties, "power"))) {
            return "power-line";
        }
        if ("plant".equals(text(properties, "power")) || "generator".equals(text(properties, "power"))) {
            return "power-plant-osm";
        }
        if ("hospital".equals(text(properties, "amenity")) || "hospital".equals(text(properties, "healthcare"))) {
            return "hospital";
        }
        if ("data_center".equals(text(properties, "telecom"))) {
            return "data-centre-osm";
        }
        if ("exchange".equals(text(properties, "telecom"))) {
            return "telecom-exchange";
        }
        if ("aerodrome".equals(text(properties, "aeroway"))) {
            return "aerodrome-osm";
        }
        if ("port".equals(text(properties, "landuse")) || "yes".equals(text(properties, "harbour"))) {
            return "port-osm";
        }
        if ("wastewater_plant".equals(text(properties, "man_made")) || "water_works".equals(text(properties, "man_made"))) {
            return "water-facility";
        }
        return null;
    }

    private static void copy(JsonNode properties, Map<String, Object> payload, String from, String to) {
        if (properties.hasNonNull(from)) {
            payload.put(to, properties.get(from).asString());
        }
    }

    /** The maximum voltage of a {@code ;}-separated list, kV. */
    static Double maxVoltageKv(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        double max = -1;
        for (String part : node.asString().split(";")) {
            try {
                double volts = Double.parseDouble(part.trim());
                max = Math.max(max, volts);
            } catch (NumberFormatException e) {
                // skip
            }
        }
        return max < 0 ? null : max / 1000.0;
    }

    /**
     * A capacity in MW from a {@code plant:output:electricity} value: a number with the unit
     * {@code W}, {@code kW}, {@code MW} or {@code GW} ({@code 500 MW}, {@code 1.2 GW},
     * {@code 250 kW}). The OSM key defines no default unit (a plant of unknown output is tagged
     * {@code yes}), so {@code yes}, a bare number or an unknown unit gives null.
     */
    static Double capacityMw(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        java.util.regex.Matcher m = CAPACITY.matcher(node.asString().trim());
        if (!m.matches()) {
            return null;
        }
        double value;
        try {
            value = Double.parseDouble(m.group(1).replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
        return switch (m.group(2).toLowerCase(java.util.Locale.ROOT)) {
            case "w" -> value / 1_000_000.0;
            case "kw" -> value / 1_000.0;
            case "mw" -> value;
            case "gw" -> value * 1_000.0;
            default -> null;
        };
    }

    /** A number, optional spaces, and a power unit. */
    private static final java.util.regex.Pattern CAPACITY =
            java.util.regex.Pattern.compile("([0-9]+(?:[.,][0-9]+)?)\\s*([kKmMgG]?[wW])");

    private static Geometry geometry(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            Geometry geometry = Geo.fromGeoJson(node.toString());
            if (geometry.getNumPoints() > MAX_VERTICES) {
                geometry = DouglasPeuckerSimplifier.simplify(geometry, 0.0005);
            }
            return geometry;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String text(JsonNode properties, String field) {
        return properties.hasNonNull(field) ? properties.get(field).asString() : null;
    }
}
