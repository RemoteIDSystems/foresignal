package ai.foresignal.source.adapters.wikidata;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The Wikidata SPARQL result for nuclear power plants (prompt 094).
 *
 * <p>Property ids: {@code P31} instance of, {@code Q134447} nuclear power plant, {@code P625} coordinate
 * location, {@code P17} country, {@code P2109} nominal power capacity, {@code P5817} state of use, {@code P571}
 * inception. {@code externalId} = {@code wikidata:{Qid}}.
 */
public final class WikidataNuclearParser {

    /** The record kind. */
    public static final String KIND = "nuclear-plant";

    /** The SPARQL query, fixed in code (the operator cannot change it). */
    public static final String QUERY =
            """
            SELECT ?item ?itemLabel ?coord ?countryLabel ?capacity ?stateLabel ?inception WHERE {
              ?item wdt:P31/wdt:P279* wd:Q134447 .
              ?item wdt:P625 ?coord .
              OPTIONAL { ?item wdt:P17 ?country . }
              OPTIONAL { ?item wdt:P2109 ?capacity . }
              OPTIONAL { ?item wdt:P5817 ?state . }
              OPTIONAL { ?item wdt:P571 ?inception . }
              SERVICE wikibase:label { bd:serviceParam wikibase:language "en". }
            }
            """;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private WikidataNuclearParser() {}

    /** Parses a SPARQL JSON results document. */
    public static List<NormalizedRecord> parse(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (RuntimeException e) {
            throw new UpstreamFormatException("Wikidata SPARQL response could not be parsed",
                    new String(body, 0, Math.min(500, body.length), StandardCharsets.UTF_8), e);
        }
        JsonNode bindings = root == null ? null : root.path("results").path("bindings");
        if (bindings == null || !bindings.isArray()) {
            throw new UpstreamFormatException("Wikidata SPARQL response has no results.bindings array", "");
        }
        List<NormalizedRecord> out = new ArrayList<>(bindings.size());
        for (JsonNode binding : bindings) {
            NormalizedRecord record = map(binding);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    /** One binding, or null without an item or a coordinate. */
    static NormalizedRecord map(JsonNode binding) {
        String item = value(binding, "item");
        if (item == null) {
            return null;
        }
        String qid = qid(item);
        NormalizedRecord.Builder b = NormalizedRecord.builder("wikidata:" + qid, KIND)
                .put("name", value(binding, "itemLabel"))
                .put("country", value(binding, "countryLabel"))
                .put("capacityMw", number(value(binding, "capacity")))
                .put("stateOfUse", value(binding, "stateLabel"))
                .put("inception", isoInstant(value(binding, "inception")));
        double[] point = point(value(binding, "coord"));
        if (point != null) {
            b.point(point[0], point[1]);
        }
        return b.build();
    }

    /** The {@code Q…} id from a Wikidata entity URI. */
    static String qid(String item) {
        int slash = item.lastIndexOf('/');
        return slash >= 0 ? item.substring(slash + 1) : item;
    }

    /** A WKT {@code Point(lon lat)} as {@code [lon, lat]}, or null. */
    static double[] point(String wkt) {
        if (wkt == null) {
            return null;
        }
        int open = wkt.indexOf('(');
        int close = wkt.lastIndexOf(')');
        if (open < 0 || close <= open) {
            return null;
        }
        String[] parts = wkt.substring(open + 1, close).strip().split("\\s+");
        if (parts.length < 2) {
            return null;
        }
        try {
            double lon = Double.parseDouble(parts[0]);
            double lat = Double.parseDouble(parts[1]);
            if (!Double.isFinite(lon) || !Double.isFinite(lat) || Math.abs(lat) > 90 || Math.abs(lon) > 180) {
                return null;
            }
            return new double[] {lon, lat};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String isoInstant(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value).toString();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static Double number(String value) {
        if (value == null) {
            return null;
        }
        try {
            double parsed = Double.parseDouble(value.strip());
            return Double.isFinite(parsed) ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String value(JsonNode binding, String key) {
        JsonNode node = binding.get(key);
        if (node == null || !node.isObject()) {
            return null;
        }
        JsonNode value = node.get("value");
        if (value == null || value.isNull()) {
            return null;
        }
        String s = value.asString();
        return s == null || s.isBlank() ? null : s.strip();
    }
}
