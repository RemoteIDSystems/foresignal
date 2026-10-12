package ai.foresignal.api.mapper;

import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.tiles.TileCoordinates;
import ai.foresignal.store.tiles.TileLayer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Builds the TileJSON documents of prompt 016 as ordered maps (OpenAPI schemas
 * {@code TileLayerIndex} and {@code TileJson}; TileJSON 3.0.0 keys are snake_case by that spec).
 *
 * <p>Maps rather than records, so {@code foresignal:aggBelowZoom} is written as an explicit
 * {@code null} when a layer never aggregates, and the extension keys keep their colons.
 */
@Component
public class TileJsonMapper {

    /** The TileJSON version written. */
    public static final String TILEJSON_VERSION = "3.0.0";

    /** Web Mercator bounds: the whole world between the latitudes Web Mercator can show. */
    public static final List<Double> BOUNDS = List.of(-180.0, -85.0511, 180.0, 85.0511);

    /** Field descriptions of the base feature properties. */
    static final Map<String, String> BASE_FIELDS;

    static {
        Map<String, String> base = new LinkedHashMap<>();
        base.put("source", "String");
        base.put("externalId", "String");
        base.put("kind", "String");
        base.put("title", "String");
        base.put("observedAt", "Number");
        base.put("version", "Number");
        base.put("live", "Boolean");
        BASE_FIELDS = Map.copyOf(base);
    }

    private final ForesignalVersion version;

    /** Creates the mapper. */
    public TileJsonMapper(ForesignalVersion version) {
        this.version = version;
    }

    /** {@code GET /v1/tiles.json}: every visible layer with its TileJSON URL. */
    public Map<String, Object> index(List<TileLayer> layers, String baseUrl) {
        return index(layers, baseUrl, null);
    }

    /**
     * {@code GET /v1/tiles.json?asOf=}: the index whose TileJSON URLs carry the canonical
     * {@code asOf} (prompt 037); a null {@code asOf} gives the current index.
     */
    public Map<String, Object> index(List<TileLayer> layers, String baseUrl, String asOf) {
        List<Map<String, Object>> entries = new ArrayList<>();
        for (TileLayer layer : layers) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", layer.id());
            entry.put("type", layer.type().wireValue());
            entry.put("name", layer.name());
            entry.put("tilejson", baseUrl + "/v1/tiles/" + layer.id() + ".json" + (asOf == null ? "" : "?asOf=" + asOf));
            entry.put("aggBelowZoom", layer.aggBelowZoom());
            entries.add(entry);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tilejson", TILEJSON_VERSION);
        body.put("layers", entries);
        return body;
    }

    /** {@code GET /v1/tiles/{layer}.json}: the TileJSON 3.0.0 document of one layer. */
    public Map<String, Object> tileJson(TileLayer layer, String baseUrl) {
        return tileJson(layer, baseUrl, baseUrl + "/v1/tiles/" + layer.id() + "/{z}/{x}/{y}.mvt");
    }

    /**
     * {@code GET /v1/tiles/{layer}.json?asOf=}: the TileJSON of the layer as known at {@code asOf}
     * (prompt 037). The tile template is concrete, with the canonical {@code asOf} baked in, and
     * {@code live} is not among the properties (asOf features come from stored versions);
     * {@code foresignal:asOf} echoes the instant.
     */
    public Map<String, Object> asOfTileJson(TileLayer layer, String baseUrl, String asOf) {
        Map<String, Object> body = tileJson(layer, baseUrl,
                baseUrl + "/v1/tiles/" + layer.id() + "/{z}/{x}/{y}.mvt?asOf=" + asOf);
        List<String> properties = new ArrayList<>(layer.properties());
        properties.remove("live");
        body.put("foresignal:properties", List.copyOf(properties));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> vectorLayers = (List<Map<String, Object>>) body.get("vector_layers");
        for (Map<String, Object> vectorLayer : vectorLayers) {
            Object fields = vectorLayer.get("fields");
            if (fields instanceof Map<?, ?> map) {
                Map<Object, Object> copy = new LinkedHashMap<>(map);
                copy.remove("live");
                vectorLayer.put("fields", copy);
            }
        }
        body.put("foresignal:asOf", asOf);
        return body;
    }

    /**
     * The TileJSON 3.0.0 document of one layer with an explicit tile template (prompt 036: a saved
     * view's template carries the view version, so caches key on it).
     */
    public Map<String, Object> tileJson(TileLayer layer, String baseUrl, String tilesTemplate) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (String property : layer.properties()) {
            fields.put(property, fieldDescription(property, layer.sources()));
        }
        Map<String, Object> vectorLayer = new LinkedHashMap<>();
        vectorLayer.put("id", layer.id());
        vectorLayer.put("description", layer.description());
        vectorLayer.put("minzoom", 0);
        vectorLayer.put("maxzoom", TileCoordinates.MAX_ZOOM);
        vectorLayer.put("fields", fields);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tilejson", TILEJSON_VERSION);
        body.put("name", layer.name());
        body.put("description", layer.description());
        body.put("version", version.version());
        String attribution = layer.attribution();
        if (attribution != null) {
            body.put("attribution", attribution);
        }
        body.put("scheme", "xyz");
        body.put("tiles", List.of(tilesTemplate));
        body.put("minzoom", 0);
        body.put("maxzoom", TileCoordinates.MAX_ZOOM);
        body.put("bounds", BOUNDS);
        body.put("vector_layers", List.of(vectorLayer));
        body.put("foresignal:aggBelowZoom", layer.aggBelowZoom());
        body.put("foresignal:properties", layer.properties());
        Map<String, Object> presentation = presentation(layer);
        if (presentation != null) {
            body.put("foresignal:presentation", presentation);
        }
        return body;
    }

    /**
     * The layer presentation hints of a kind layer (EDP {@code presentation}, prompt 024): the first
     * declaring source's hints for the kind, or null when none declares any.
     */
    static Map<String, Object> presentation(TileLayer layer) {
        if (layer.type() != ai.foresignal.store.tiles.TileLayerType.KIND) {
            return null;
        }
        for (ai.foresignal.source.spi.SourceDescriptor source : layer.sources()) {
            Map<String, Object> hints = source.kindPresentation().get(layer.id());
            if (hints != null && !hints.isEmpty()) {
                return hints;
            }
        }
        return null;
    }

    /**
     * The description of one feature property: the base type for base properties; for a tile
     * property, the first declaring source's {@code fieldSpecs} label and unit ({@code "Magnitude (1)"}),
     * else its type ({@code "Number"}, {@code "Boolean"}, {@code "String"}), else {@code "String"}.
     */
    static String fieldDescription(String property, List<SourceDescriptor> sources) {
        String base = BASE_FIELDS.get(property);
        if (base != null) {
            return base;
        }
        for (SourceDescriptor source : sources) {
            FieldSpec spec = source.fieldSpecs().get(property);
            if (spec == null) {
                continue;
            }
            String label = spec.label() != null && !spec.label().isBlank() ? spec.label().strip() : typeName(spec);
            return spec.unit() != null && !spec.unit().isBlank() ? label + " (" + spec.unit().strip() + ")" : label;
        }
        return "String";
    }

    private static String typeName(FieldSpec spec) {
        if (spec.type() == null) {
            return "String";
        }
        return switch (spec.type()) {
            case NUMBER, INTEGER -> "Number";
            case BOOLEAN -> "Boolean";
            default -> "String";
        };
    }
}
