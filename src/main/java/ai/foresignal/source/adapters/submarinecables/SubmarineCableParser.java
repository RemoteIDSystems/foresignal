package ai.foresignal.source.adapters.submarinecables;

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
 * The public Submarine Cable Map GeoJSON (prompt 094): {@code cable-geo.json} and
 * {@code landing-point-geo.json}.
 *
 * <p>Stylized geometry. {@code externalId} is {@code cable:{id}} for a cable and {@code landing:{id}} for a
 * landing point. A feature without an id is skipped; a malformed geometry keeps the record without geometry and
 * is counted. Features are streamed one at a time ({@link ReferenceStreams}).
 */
public final class SubmarineCableParser {

    /** The cable kind. */
    public static final String CABLE_KIND = "cable";

    /** The landing-point kind. */
    public static final String LANDING_KIND = "cable-landing";

    private SubmarineCableParser() {}

    /** Parses {@code cable-geo.json}. */
    public static List<NormalizedRecord> parseCables(byte[] body) {
        List<NormalizedRecord> out = new ArrayList<>();
        parse(body, CABLE_KIND, "cable:", out::add);
        return out;
    }

    /** Streams {@code cable-geo.json} into {@code sink}. */
    public static ReferenceSupport.ParseCounts parseCables(byte[] body, Consumer<NormalizedRecord> sink) {
        return parse(body, CABLE_KIND, "cable:", sink);
    }

    /** Parses {@code landing-point-geo.json}. */
    public static List<NormalizedRecord> parseLandings(byte[] body) {
        List<NormalizedRecord> out = new ArrayList<>();
        parse(body, LANDING_KIND, "landing:", out::add);
        return out;
    }

    /** Streams {@code landing-point-geo.json} into {@code sink}. */
    public static ReferenceSupport.ParseCounts parseLandings(byte[] body, Consumer<NormalizedRecord> sink) {
        return parse(body, LANDING_KIND, "landing:", sink);
    }

    private static ReferenceSupport.ParseCounts parse(byte[] body, String kind, String idPrefix,
            Consumer<NormalizedRecord> sink) {
        int[] counts = new int[3];
        ReferenceStreams.forEachElement(body, "Submarine Cable Map document", feature -> {
            JsonNode properties = feature.get("properties");
            String id = text(properties, "id");
            if (id == null) {
                counts[1]++;
                return;
            }
            NormalizedRecord.Builder b = NormalizedRecord.builder(idPrefix + id, kind)
                    .put("name", text(properties, "name"))
                    .put("color", text(properties, "color"))
                    .put("isTbd", tbd(properties));
            FeatureGeometry.Result geometry;
            try {
                geometry = FeatureGeometry.fromGeoJson(feature.get("geometry"));
            } catch (RuntimeException e) {
                geometry = FeatureGeometry.Result.NONE;
                counts[2]++;
            }
            sink.accept(b.geometry(geometry.geometry()).build());
            counts[0]++;
        }, "features");
        return new ReferenceSupport.ParseCounts(counts[0], counts[1], counts[2]);
    }

    private static Boolean tbd(JsonNode properties) {
        if (properties == null) {
            return null;
        }
        JsonNode node = properties.get("is_tbd");
        if (node == null || node.isNull()) {
            node = properties.get("isTbd");
        }
        return node != null && node.isBoolean() ? node.booleanValue() : null;
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
