package ai.foresignal.source.adapters.gvp;

import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.ReferenceStreams;
import ai.foresignal.source.support.ReferenceSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import tools.jackson.databind.JsonNode;

/**
 * The Smithsonian Global Volcanism Program WFS GeoJSON (prompt 094): Holocene volcanoes.
 *
 * <p>{@code externalId} = {@code Volcano_Number}; a point from the feature geometry. {@code lastEruptionYear}
 * is negative for BCE and omitted when the upstream has none; the geologic summary is trimmed to 2 KB. The
 * features are streamed one at a time ({@link ReferenceStreams}); a malformed geometry keeps the record
 * without geometry and is counted.
 */
public final class GvpWfsParser {

    /** The record kind. */
    public static final String KIND = "volcano";

    /** The longest summary kept, characters. */
    public static final int MAX_SUMMARY = 2_048;

    private GvpWfsParser() {}

    /** Parses the WFS GeoJSON FeatureCollection into a list (tests and small documents). */
    public static List<NormalizedRecord> parse(byte[] body) {
        List<NormalizedRecord> out = new ArrayList<>();
        parse(body, out::add);
        return out;
    }

    /**
     * Streams the WFS GeoJSON FeatureCollection into {@code sink}, one feature at a time.
     *
     * @throws UpstreamFormatException when the document does not parse or has no features array
     */
    public static ReferenceSupport.ParseCounts parse(byte[] body, Consumer<NormalizedRecord> sink) {
        int[] counts = new int[3];
        ReferenceStreams.forEachElement(body, "GVP WFS document", feature -> {
            NormalizedRecord record = map(feature, counts);
            if (record == null) {
                counts[1]++;
            } else {
                sink.accept(record);
                counts[0]++;
            }
        }, "features");
        return new ReferenceSupport.ParseCounts(counts[0], counts[1], counts[2]);
    }

    /** One feature's record, or null when it has no volcano number; {@code counts[2]} counts bad geometry. */
    private static NormalizedRecord map(JsonNode feature, int[] counts) {
        JsonNode p = feature.get("properties");
        String number = text(p, "Volcano_Number");
        if (number == null) {
            return null;
        }
        String summary = text(p, "Geologic_Summary");
        if (summary != null && summary.length() > MAX_SUMMARY) {
            summary = summary.substring(0, MAX_SUMMARY);
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(number, KIND)
                .put("name", text(p, "Volcano_Name"))
                .put("landform", text(p, "Volcano_Landform"))
                .put("primaryType", primaryType(p))
                .put("lastEruptionYear", year(p.get("Last_Eruption_Year")))
                .put("country", text(p, "Country"))
                .put("region", text(p, "Region"))
                .put("subregion", text(p, "Subregion"))
                .put("elevationM", number(p.get("Elevation")))
                .put("tectonicSetting", text(p, "Tectonic_Setting"))
                .put("evidenceCategory", text(p, "Evidence_Category"))
                .put("rockType", text(p, "Major_Rock_Type"))
                .put("summary", summary);
        FeatureGeometry.Result geometry;
        try {
            geometry = FeatureGeometry.fromGeoJson(feature.get("geometry"));
        } catch (RuntimeException e) {
            geometry = FeatureGeometry.Result.NONE;
            counts[2]++;
        }
        return b.geometry(geometry.geometry()).build();
    }

    private static String primaryType(JsonNode p) {
        String type = text(p, "Primary_Volcano_Type");
        return type != null ? type : text(p, "Volcano_Type");
    }

    private static Long year(JsonNode node) {
        if (node == null || node.isNull() || node.isContainer()) {
            return null;
        }
        if (node.isIntegralNumber()) {
            return node.longValue();
        }
        Double value = number(node);
        return value == null || value != Math.rint(value) ? null : value.longValue();
    }

    private static Double number(JsonNode node) {
        if (node == null || node.isNull() || node.isContainer()) {
            return null;
        }
        if (node.isNumber()) {
            double value = node.doubleValue();
            return Double.isFinite(value) ? value : null;
        }
        try {
            double value = Double.parseDouble(node.asString().strip());
            return Double.isFinite(value) ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || value.isContainer()) {
            return null;
        }
        String s = value.asString();
        return s == null || s.isBlank() ? null : s.strip();
    }
}
