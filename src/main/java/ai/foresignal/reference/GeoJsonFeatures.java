package ai.foresignal.reference;

import ai.foresignal.source.formats.geojson.GeoJsonReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.util.GeometryFixer;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Streams the features of a large GeoJSON FeatureCollection (prompt 040): Jackson's streaming parser walks
 * to the {@code features} array and reads one feature tree at a time, so a 40 MB Natural Earth file never
 * sits in memory whole. Geometries are read with Foresignal's own {@link GeoJsonReader} (Conventions,
 * "GeoJSON reading") and repaired with JTS {@link GeometryFixer}.
 */
public final class GeoJsonFeatures {

    /** The most vertices one feature may have (Natural Earth's largest country has a few hundred thousand). */
    public static final int MAX_FEATURE_VERTICES = 5_000_000;

    private static final GeometryFactory WGS84 = new GeometryFactory(new PrecisionModel(), 4326);
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private GeoJsonFeatures() {}

    /**
     * Calls {@code consumer} with each feature's properties and polygonal geometry (as a valid
     * MultiPolygon, SRID 4326); features without a polygonal geometry are skipped.
     *
     * @return the number of features read
     * @throws IllegalArgumentException when the document is not a FeatureCollection
     */
    public static int forEachPolygonal(InputStream in, BiConsumer<JsonNode, MultiPolygon> consumer) {
        GeoJsonReader reader = new GeoJsonReader(MAX_FEATURE_VERTICES, 4);
        int[] count = {0};
        forEach(in, feature -> {
            JsonNode geometry = feature.get("geometry");
            if (geometry == null || geometry.isNull()) {
                return;
            }
            MultiPolygon polygonal = polygonal(GeometryFixer.fix(reader.read(geometry)));
            if (polygonal != null && !polygonal.isEmpty()) {
                consumer.accept(feature.path("properties"), polygonal);
                count[0]++;
            }
        });
        return count[0];
    }

    /** Calls {@code consumer} with each feature object of the collection, in document order. */
    public static void forEach(InputStream in, java.util.function.Consumer<JsonNode> consumer) {
        try (JsonParser parser = JSON.createParser(in)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IllegalArgumentException("not a GeoJSON object");
            }
            boolean sawFeatures = false;
            while (parser.nextToken() == JsonToken.PROPERTY_NAME) {
                String name = parser.currentName();
                JsonToken value = parser.nextToken();
                if ("features".equals(name) && value == JsonToken.START_ARRAY) {
                    sawFeatures = true;
                    while (parser.nextToken() == JsonToken.START_OBJECT) {
                        consumer.accept(JSON.readTree(parser));
                    }
                } else {
                    parser.skipChildren();
                }
            }
            if (!sawFeatures) {
                throw new IllegalArgumentException("not a GeoJSON FeatureCollection (no features array)");
            }
        }
    }

    /** The polygons of {@code geometry} as one MultiPolygon, or null when it has none. */
    public static MultiPolygon polygonal(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        if (geometry instanceof MultiPolygon multi) {
            multi.setSRID(4326);
            return multi;
        }
        List<Polygon> polygons = new ArrayList<>();
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry part = geometry.getGeometryN(i);
            if (part instanceof Polygon polygon && !polygon.isEmpty()) {
                polygons.add(polygon);
            } else if (part instanceof MultiPolygon nested) {
                for (int j = 0; j < nested.getNumGeometries(); j++) {
                    polygons.add((Polygon) nested.getGeometryN(j));
                }
            }
        }
        if (polygons.isEmpty()) {
            return null;
        }
        MultiPolygon out = WGS84.createMultiPolygon(polygons.toArray(Polygon[]::new));
        out.setSRID(4326);
        return out;
    }
}
