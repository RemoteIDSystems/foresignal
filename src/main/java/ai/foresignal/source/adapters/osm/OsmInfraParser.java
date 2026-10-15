package ai.foresignal.source.adapters.osm;

import ai.foresignal.source.spi.Geo;
import ai.foresignal.source.spi.NormalizedRecord;
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

    /** Parses the lines, keeping power lines at or above {@code minVoltageKv}. */
    public static List<NormalizedRecord> parse(List<String> lines, double minVoltageKv, Instant observedAt) {
        List<NormalizedRecord> out = new ArrayList<>();
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            try {
                JsonNode feature = JSON.readTree(line);
                NormalizedRecord record = record(feature, minVoltageKv, observedAt);
                if (record != null) {
                    out.add(record);
                }
            } catch (RuntimeException e) {
                // a malformed line is skipped; the adapter counts parse errors
            }
        }
        return out;
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

    /** A capacity in MW from a value like {@code 500 MW}. */
    static Double capacityMw(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asString().replaceAll("[^0-9.]", "").trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value) / 1_000_000.0;
        } catch (NumberFormatException e) {
            return null;
        }
    }

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
