package ai.foresignal.source.adapters.wikidata;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.ReferenceStreams;
import ai.foresignal.source.support.ReferenceSupport;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import tools.jackson.databind.JsonNode;

/**
 * The Wikidata SPARQL result for nuclear power plants (prompt 094).
 *
 * <p>Property ids: {@code P31} instance of, {@code Q134447} nuclear power plant, {@code P625} coordinate
 * location, {@code P17} country, {@code P2109} nominal power capacity, {@code P5817} state of use, {@code P571}
 * inception. {@code externalId} = {@code wikidata:{Qid}}.
 *
 * <p><strong>One row per plant, deterministically</strong> (2026-10 review). Country, capacity, state of use,
 * inception and even the coordinate can be multi-valued on Wikidata; unconstrained OPTIONALs would return one
 * binding per combination and an arbitrary one would win. The query therefore groups by item and aggregates each
 * property with a fixed rule: the alphabetically first English label of the country and state of use, the
 * earliest inception, the alphabetically first coordinate, and the largest best-rank capacity. Capacity is read
 * from the statement's <em>normalized</em> value ({@code psn:P2109}), which Wikibase converts to the SI unit
 * (watts) whatever unit the editor used (MW, kW, GW), and divided by 10<sup>6</sup> here, so {@code capacityMw} is
 * always megawatts. The parser also drops a repeated Qid, keeping the first, should the endpoint ever return one.
 */
public final class WikidataNuclearParser {

    /** The record kind. */
    public static final String KIND = "nuclear-plant";

    /** Watts per megawatt. */
    static final double WATTS_PER_MW = 1_000_000d;

    /** The SPARQL query, fixed in code (the operator cannot change it). */
    public static final String QUERY =
            """
            SELECT ?item (MIN(STR(?label)) AS ?itemLabel) (MIN(STR(?coord)) AS ?location)
                   (MIN(STR(?countryName)) AS ?countryLabel) (MAX(?capacityW) AS ?capacityWatts)
                   (MIN(STR(?stateName)) AS ?stateLabel) (MIN(?inceptionDate) AS ?inception)
            WHERE {
              ?item wdt:P31/wdt:P279* wd:Q134447 .
              ?item wdt:P625 ?coord .
              OPTIONAL { ?item rdfs:label ?label . FILTER(LANG(?label) = "en") }
              OPTIONAL { ?item wdt:P17 ?country . ?country rdfs:label ?countryName . FILTER(LANG(?countryName) = "en") }
              OPTIONAL { ?item p:P2109 ?capacityStatement .
                         ?capacityStatement a wikibase:BestRank ;
                                            psn:P2109/wikibase:quantityAmount ?capacityW . }
              OPTIONAL { ?item wdt:P5817 ?state . ?state rdfs:label ?stateName . FILTER(LANG(?stateName) = "en") }
              OPTIONAL { ?item wdt:P571 ?inceptionDate . }
            }
            GROUP BY ?item
            ORDER BY ?item
            """;

    private WikidataNuclearParser() {}

    /** Parses a SPARQL JSON results document into a list. */
    public static List<NormalizedRecord> parse(byte[] body) {
        List<NormalizedRecord> out = new ArrayList<>();
        parse(body, out::add);
        return out;
    }

    /**
     * Streams a SPARQL JSON results document into {@code sink}, one binding at a time; a binding repeating a Qid
     * already handed over is skipped and counted.
     *
     * @throws UpstreamFormatException when the document does not parse or has no {@code results.bindings} array
     */
    public static ReferenceSupport.ParseCounts parse(byte[] body, Consumer<NormalizedRecord> sink) {
        int[] counts = new int[3];
        Set<String> seen = new HashSet<>();
        ReferenceStreams.forEachElement(body, "Wikidata SPARQL response", binding -> {
            NormalizedRecord record = map(binding);
            if (record == null || !seen.add(record.externalId())) {
                counts[1]++;
            } else {
                sink.accept(record);
                counts[0]++;
            }
        }, "results", "bindings");
        return new ReferenceSupport.ParseCounts(counts[0], counts[1], counts[2]);
    }

    /** One binding, or null without an item. */
    static NormalizedRecord map(JsonNode binding) {
        String item = value(binding, "item");
        if (item == null) {
            return null;
        }
        String qid = qid(item);
        NormalizedRecord.Builder b = NormalizedRecord.builder("wikidata:" + qid, KIND)
                .put("name", value(binding, "itemLabel"))
                .put("country", value(binding, "countryLabel"))
                .put("capacityMw", capacityMw(binding))
                .put("stateOfUse", value(binding, "stateLabel"))
                .put("inception", isoInstant(value(binding, "inception")));
        String location = value(binding, "location");
        double[] point = point(location != null ? location : value(binding, "coord"));
        if (point != null) {
            b.point(point[0], point[1]);
        }
        return b.build();
    }

    /**
     * The capacity in megawatts: the normalized {@code capacityWatts} (SI watts) divided by 10<sup>6</sup>, or, for
     * a result in the pre-review shape, a bare {@code capacity} taken as megawatts.
     */
    static Double capacityMw(JsonNode binding) {
        Double watts = number(value(binding, "capacityWatts"));
        if (watts != null) {
            return watts / WATTS_PER_MW;
        }
        return number(value(binding, "capacity"));
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
