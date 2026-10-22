package ai.foresignal.map;

import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.tiles.TileLayer;
import ai.foresignal.store.tiles.TileLayerType;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Generates the MapLibre overlay style of prompt 058 ({@code GET /v1/map/style.json}) from each kind's
 * {@code kindDisplay} hints. Overlay only: no basemap, no {@code glyphs} and no text layers, so a
 * client drops the sources and layers onto its own basemap and no glyph is ever requested.
 *
 * <p><strong>Sources.</strong> One {@code vector} source per tile layer, named after the layer, whose
 * {@code url} is the layer's TileJSON (016). Below a layer's {@code aggBelowZoom} a second source
 * {@code <layer>--agg} reads the same tiles with {@code agg=hex} ({@code maxzoom} = {@code aggBelowZoom});
 * its {@code fill} layer ramps on {@code count} and stops at {@code aggBelowZoom}, where the raw layers
 * start ({@code minzoom}), so the two sets hand over at one zoom.
 *
 * <p><strong>Layers per kind</strong>, filtered by geometry type and, when the layer carries several
 * kinds, by {@code ["==", ["get", "kind"], kind]}:
 *
 * <ul>
 *   <li>points with an icon: {@code symbol}, SDF {@code icon-color}, {@code icon-rotate} from
 *       {@code rotateField} with {@code icon-rotation-alignment: map}, {@code icon-allow-overlap};
 *   <li>points without an icon: {@code circle} whose radius interpolates by zoom and, with
 *       {@code sizeField}, grows with the square root of that property;
 *   <li>lines: {@code line};
 *   <li>polygons: {@code fill} at opacity 0.25 plus a {@code line} outline.
 * </ul>
 *
 * <p>The color is the kind's {@code colour} (else the theme default); with a {@code colorField}, or a
 * {@code severity} tile property, it is a {@code match} over the common severity words
 * ({@link #SEVERITY_COLORS}) that falls back to the kind's color.
 *
 * <p>{@code metadata.foresignal.generatedAt} is when this service started, so the document, and its
 * strong ETag, stay the same between restarts unless the registry or the request changes.
 */
@Service
public class MapStyleService {

    /** The style's {@code name}. */
    public static final String NAME = "Foresignal overlay";

    /** The suffix of an aggregation source and layer id. */
    public static final String AGG_SUFFIX = "--agg";

    /** Severity words to colors, highest first; a value outside these falls back to the kind's color. */
    public static final List<Map.Entry<List<String>, String>> SEVERITY_COLORS = List.of(
            Map.entry(List.of("extreme", "critical"), "#C92A2A"),
            Map.entry(List.of("severe", "warning", "high"), "#E8590C"),
            Map.entry(List.of("moderate", "watch", "medium"), "#F08C00"),
            Map.entry(List.of("minor", "advisory", "low", "info"), "#1971C2"));

    /** The property a color match reads when the kind declares no {@code colorField}. */
    public static final String DEFAULT_COLOR_FIELD = "severity";

    /** The visual theme. */
    public enum Theme {
        /** Light basemaps: white halos, darker defaults. */
        LIGHT("#1971C2", "#FFFFFF", List.of("#D0EBFF", "#74C0FC", "#1C7ED6", "#0B4B8C")),
        /** Dark basemaps: dark halos, lighter defaults. */
        DARK("#4DABF7", "#1D1F23", List.of("#1C3D5A", "#1C7ED6", "#74C0FC", "#E7F5FF"));

        private final String defaultColor;
        private final String halo;
        private final List<String> ramp;

        Theme(String defaultColor, String halo, List<String> ramp) {
            this.defaultColor = defaultColor;
            this.halo = halo;
            this.ramp = ramp;
        }

        /** The color of a kind without {@code colour}. */
        public String defaultColor() {
            return defaultColor;
        }

        /** Icon halo and circle stroke color. */
        public String halo() {
            return halo;
        }

        /** The aggregation ramp at counts 1, 10, 100 and 1000. */
        public List<String> ramp() {
            return ramp;
        }

        /** The wire value. */
        public String wireValue() {
            return name().toLowerCase(Locale.ROOT);
        }

        /** Parses {@code light} or {@code dark}; null means light. */
        public static Theme parse(String value) {
            return value == null ? LIGHT : Theme.valueOf(value.toUpperCase(Locale.ROOT));
        }
    }

    private final SpriteCatalog sprite;
    private final Instant generatedAt;

    /** Creates the service. */
    public MapStyleService(SpriteCatalog sprite, Clock clock) {
        this.sprite = sprite;
        this.generatedAt = clock.instant();
    }

    /** When this service started: the style's {@code metadata.foresignal.generatedAt}. */
    public Instant generatedAt() {
        return generatedAt;
    }

    /**
     * The style document for {@code layers}.
     *
     * @param layers the visible tile layers, already filtered for the caller (private sources removed)
     * @param theme the theme
     * @param urls the public URLs of the request (keyed when it came through a tile or feed key)
     * @return the version-8 style as ordered maps
     */
    public Map<String, Object> style(List<TileLayer> layers, Theme theme, PublicUrls urls) {
        Map<String, Object> sources = new LinkedHashMap<>();
        List<Map<String, Object>> styleLayers = new ArrayList<>();
        List<Map<String, Object>> metadataLayers = new ArrayList<>();
        for (TileLayer layer : layers) {
            List<String> kinds = kinds(layer);
            Integer agg = layer.aggBelowZoom();
            Map<String, Object> source = new LinkedHashMap<>();
            source.put("type", "vector");
            source.put("url", urls.api("/v1/tiles/" + layer.id() + ".json"));
            if (layer.attribution() != null) {
                source.put("attribution", layer.attribution());
            }
            sources.put(layer.id(), source);
            if (agg != null && agg > 0) {
                Map<String, Object> aggSource = new LinkedHashMap<>();
                aggSource.put("type", "vector");
                aggSource.put("tiles", List.of(urls.api("/v1/tiles/" + layer.id() + "/{z}/{x}/{y}.mvt") + "?agg=hex"));
                aggSource.put("minzoom", 0);
                aggSource.put("maxzoom", agg);
                if (layer.attribution() != null) {
                    aggSource.put("attribution", layer.attribution());
                }
                sources.put(layer.id() + AGG_SUFFIX, aggSource);
                styleLayers.add(aggLayer(layer, agg, theme));
            }
            int minzoom = agg != null && agg > 0 ? agg : 0;
            for (String kind : kinds) {
                styleLayers.addAll(kindLayers(layer, kind, kinds.size() > 1, minzoom, theme));
            }
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("id", layer.id());
            meta.put("kinds", kinds);
            if (layer.attribution() != null) {
                meta.put("attribution", layer.attribution());
            }
            meta.put("minzoom", 0);
            meta.put("aggBelowZoom", agg);
            metadataLayers.add(meta);
        }
        Map<String, Object> foresignal = new LinkedHashMap<>();
        foresignal.put("layers", metadataLayers);
        foresignal.put("theme", theme.wireValue());
        foresignal.put("generatedAt", generatedAt.toString());
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("foresignal", foresignal);

        Map<String, Object> style = new LinkedHashMap<>();
        style.put("version", 8);
        style.put("name", NAME);
        style.put("metadata", metadata);
        style.put("sources", sources);
        style.put("sprite", urls.api("/v1/map/sprites/" + sprite.spriteName()));
        style.put("layers", styleLayers);
        return style;
    }

    /** The kinds a layer carries: its own id for a kind layer, else its sources' kinds in first-seen order. */
    static List<String> kinds(TileLayer layer) {
        if (layer.type() == TileLayerType.KIND) {
            return List.of(layer.id());
        }
        Set<String> kinds = new LinkedHashSet<>();
        for (SourceDescriptor source : layer.sources()) {
            for (String kind : source.kinds()) {
                if (!source.hidesKind(kind)) {
                    kinds.add(kind);
                }
            }
        }
        return List.copyOf(kinds);
    }

    /** The first declaring source's display hints for {@code kind}, or null. */
    static KindDisplay display(TileLayer layer, String kind) {
        for (SourceDescriptor source : layer.sources()) {
            KindDisplay display = source.kindDisplay().get(kind);
            if (display != null) {
                return display;
            }
        }
        return null;
    }

    private List<Map<String, Object>> kindLayers(TileLayer layer, String kind, boolean filterKind, int minzoom,
            Theme theme) {
        KindDisplay display = display(layer, kind);
        String base = display != null && display.color() != null ? display.color().toUpperCase(Locale.ROOT)
                : theme.defaultColor();
        Object color = color(layer, display, base);
        String prefix = layer.id() + "--" + kind;
        List<Map<String, Object>> out = new ArrayList<>();

        boolean icon = display != null && display.icon() != null && sprite.contains(display.icon());
        if (icon) {
            Map<String, Object> layout = new LinkedHashMap<>();
            layout.put("icon-image", iconImage(display));
            layout.put("icon-allow-overlap", true);
            layout.put("icon-ignore-placement", true);
            if (display.rotateField() != null) {
                layout.put("icon-rotate", List.of("coalesce", List.of("to-number", List.of("get", display.rotateField())), 0));
                layout.put("icon-rotation-alignment", "map");
            }
            Map<String, Object> paint = new LinkedHashMap<>();
            paint.put("icon-color", color);
            paint.put("icon-halo-color", theme.halo());
            paint.put("icon-halo-width", 1);
            out.add(layer(prefix + "--point", "symbol", layer.id(), filter(kind, filterKind, "Point"), minzoom, layout,
                    paint));
        } else {
            Map<String, Object> paint = new LinkedHashMap<>();
            paint.put("circle-radius", radius(display));
            paint.put("circle-color", color);
            paint.put("circle-opacity", 0.9);
            paint.put("circle-stroke-color", theme.halo());
            paint.put("circle-stroke-width", 1);
            out.add(layer(prefix + "--point", "circle", layer.id(), filter(kind, filterKind, "Point"), minzoom, null,
                    paint));
        }

        Map<String, Object> linePaint = new LinkedHashMap<>();
        linePaint.put("line-color", color);
        linePaint.put("line-width", List.of("interpolate", List.of("linear"), List.of("zoom"), 0, 1, 10, 2.5));
        out.add(layer(prefix + "--line", "line", layer.id(), filter(kind, filterKind, "LineString"), minzoom,
                null, linePaint));

        Map<String, Object> fillPaint = new LinkedHashMap<>();
        fillPaint.put("fill-color", color);
        fillPaint.put("fill-opacity", 0.25);
        out.add(layer(prefix + "--fill", "fill", layer.id(), filter(kind, filterKind, "Polygon"), minzoom, null,
                fillPaint));
        Map<String, Object> outlinePaint = new LinkedHashMap<>();
        outlinePaint.put("line-color", color);
        outlinePaint.put("line-width", 1.5);
        out.add(layer(prefix + "--outline", "line", layer.id(), filter(kind, filterKind, "Polygon"), minzoom, null,
                outlinePaint));
        return out;
    }

    /**
     * The icon: a {@code match} on {@code iconField} over {@code iconCases}, falling back to {@code icon}, when
     * the kind chooses its icon by value (cases in sorted order, so the style is the same on every start);
     * otherwise the constant {@code icon}.
     */
    static Object iconImage(KindDisplay display) {
        if (display.iconField() == null || display.iconCases() == null || display.iconCases().isEmpty()) {
            return display.icon();
        }
        List<Object> match = new ArrayList<>();
        match.add("match");
        match.add(valueOf(display.iconField()));
        new java.util.TreeMap<>(display.iconCases()).forEach((value, icon) -> {
            match.add(value);
            match.add(icon);
        });
        match.add(display.icon());
        return match;
    }

    /**
     * The color expression: a {@code match} on {@code colorField}, or on {@code severity} when the layer
     * carries it, over the kind's {@code colorCases} or else {@link #SEVERITY_COLORS}; otherwise the constant
     * {@code base}.
     */
    static Object color(TileLayer layer, KindDisplay display, String base) {
        String field = display != null && display.colorField() != null ? display.colorField()
                : layer.tileProperties().contains(DEFAULT_COLOR_FIELD) ? DEFAULT_COLOR_FIELD : null;
        if (field == null) {
            return base;
        }
        List<Object> match = new ArrayList<>();
        match.add("match");
        match.add(valueOf(field));
        if (display != null && display.colorCases() != null && !display.colorCases().isEmpty()) {
            new java.util.TreeMap<>(display.colorCases()).forEach((value, color) -> {
                match.add(value);
                match.add(color.toUpperCase(Locale.ROOT));
            });
        } else {
            for (Map.Entry<List<String>, String> entry : SEVERITY_COLORS) {
                match.add(entry.getKey());
                match.add(entry.getValue());
            }
        }
        match.add(base);
        return match;
    }

    /** A feature's {@code field} as lowercase text, empty when absent: the input of every value match. */
    private static Object valueOf(String field) {
        return List.of("downcase", List.of("to-string", List.of("coalesce", List.of("get", field), "")));
    }

    /** The circle radius: by zoom, and with {@code sizeField} plus up to three times the square root of it. */
    static Object radius(KindDisplay display) {
        if (display == null || display.sizeField() == null) {
            return List.of("interpolate", List.of("linear"), List.of("zoom"), 0, 2, 8, 4, 14, 6);
        }
        Object size = List.of("min", 6, List.of("sqrt",
                List.of("max", 0, List.of("coalesce", List.of("to-number", List.of("get", display.sizeField())), 0))));
        return List.of("interpolate", List.of("linear"), List.of("zoom"),
                0, List.of("+", 2, List.of("*", 0.5, size)),
                8, List.of("+", 4, List.of("*", 1, size)),
                14, List.of("+", 6, List.of("*", 2, size)));
    }

    /** The geometry filter, and the kind filter when the layer carries several kinds. */
    static Object filter(String kind, boolean filterKind, String geometry) {
        List<String> types = switch (geometry) {
            case "Point" -> List.of("Point", "MultiPoint");
            case "LineString" -> List.of("LineString", "MultiLineString");
            default -> List.of("Polygon", "MultiPolygon");
        };
        Object geom = List.of("match", List.of("geometry-type"), types, true, false);
        if (!filterKind) {
            return geom;
        }
        return List.of("all", List.of("==", List.of("get", "kind"), kind), geom);
    }

    private Map<String, Object> aggLayer(TileLayer layer, int agg, Theme theme) {
        List<String> ramp = theme.ramp();
        Map<String, Object> paint = new LinkedHashMap<>();
        paint.put("fill-color", List.of("interpolate", List.of("linear"), List.of("coalesce", List.of("get", "count"), 0),
                1, ramp.get(0), 10, ramp.get(1), 100, ramp.get(2), 1000, ramp.get(3)));
        paint.put("fill-opacity", 0.6);
        paint.put("fill-outline-color", theme.halo());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", layer.id() + AGG_SUFFIX);
        out.put("type", "fill");
        out.put("source", layer.id() + AGG_SUFFIX);
        out.put("source-layer", layer.id());
        out.put("minzoom", 0);
        out.put("maxzoom", agg);
        out.put("paint", paint);
        return out;
    }

    private static Map<String, Object> layer(String id, String type, String source, Object filter, int minzoom,
            Map<String, Object> layout, Map<String, Object> paint) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", id);
        out.put("type", type);
        out.put("source", source);
        out.put("source-layer", source);
        if (minzoom > 0) {
            out.put("minzoom", minzoom);
        }
        out.put("filter", filter);
        if (layout != null) {
            out.put("layout", layout);
        }
        out.put("paint", paint);
        return out;
    }
}
