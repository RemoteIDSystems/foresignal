package ai.foresignal.map;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.api.support.PublicUrls;
import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.tiles.TileLayer;
import ai.foresignal.store.tiles.TileLayerResolver;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The generated MapLibre overlay style (prompt 058) for a fixture registry. */
class MapStyleServiceTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant T = Instant.parse("2026-10-13T00:00:00Z");
    static final PublicUrls URLS = new PublicUrls("http://fs.lan:8080", PublicUrls.V1);
    static final PublicUrls KEYED = new PublicUrls("http://fs.lan:8080", "/v1/k/fsk_abcdefghijklmnop");

    /** Aircraft: an icon rotated by {@code track}, aggregated below zoom 6. */
    static final SourceDescriptor PLANES = base("map-planes", "aircraft")
            .tileProperties("track", "gs")
            .aggBelowZoom(6)
            .kindDisplay("aircraft", new KindDisplay("{callsign}", "plane", "#112233", null, "track", null, null))
            .build();

    /** Fires: circles sized by {@code frp}. */
    static final SourceDescriptor FIRES = base("map-fires", "hotspot")
            .tileProperties("frp")
            .kindDisplay("hotspot", new KindDisplay(null, null, "#e8590c", null, null, "frp", null))
            .build();

    /** Two kinds in one source: polygon warnings with a severity match, and plain points. */
    static final SourceDescriptor WARNINGS = base("map-warnings", "warning-area")
            .kind("warning-point")
            .tileProperties("severity", "event")
            .kindDisplay("warning-area", new KindDisplay(null, "warning", "#C92A2A", null))
            .build();

    /** Airports: the icon and color chosen by value, as the sectional chart symbols are. */
    static final SourceDescriptor FIELDS = base("map-fields", "airport")
            .tileProperties("chartSymbol", "towered", "runwayHeading")
            .kindDisplay("airport", new KindDisplay("{name}", "airport-soft", "#d04fb4", null, "runwayHeading", null,
                    "towered", "chartSymbol", Map.of("major", "airport-major", "hard", "airport-hard"),
                    Map.of("true", "#4f8fe8")))
            .build();

    /** A private source. */
    static final SourceDescriptor SECRET = base("map-secret", "secret").privateSource(true).build();

    static final List<SourceDescriptor> REGISTRY = List.of(PLANES, FIRES, WARNINGS, FIELDS, SECRET);

    private final SpriteCatalog sprite = new SpriteCatalog(JSON);
    private final MapStyleService service = new MapStyleService(sprite, Clock.fixed(T, ZoneOffset.UTC));

    private static SourceDescriptor.Builder base(String id, String kind) {
        return SourceDescriptor.builder(id)
                .name("Map test " + id)
                .attribution("Credit " + id)
                .kind(kind)
                .mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 10_000));
    }

    private static TileLayer layer(String id, Set<String> excluded) {
        return TileLayerResolver.resolve(id, REGISTRY, excluded).orElseThrow();
    }

    private JsonNode style(List<TileLayer> layers, MapStyleService.Theme theme, PublicUrls urls) {
        return JSON.valueToTree(service.style(layers, theme, urls));
    }

    private static JsonNode layerById(JsonNode style, String id) {
        for (JsonNode l : style.get("layers")) {
            if (id.equals(l.get("id").asString())) {
                return l;
            }
        }
        throw new AssertionError("no layer " + id);
    }

    private static List<String> layerIds(JsonNode style) {
        List<String> ids = new ArrayList<>();
        style.get("layers").forEach(l -> ids.add(l.get("id").asString()));
        return ids;
    }

    @Test
    @DisplayName("version 8, sprite at the content-addressed URL, no glyphs and no text layers")
    void document() {
        JsonNode style = style(List.of(layer("map-planes", Set.of()), layer("map-fires", Set.of())),
                MapStyleService.Theme.LIGHT, URLS);
        assertThat(style.get("version").asInt()).isEqualTo(8);
        assertThat(style.get("name").asString()).isEqualTo(MapStyleService.NAME);
        assertThat(style.has("glyphs")).isFalse();
        assertThat(style.get("sprite").asString())
                .isEqualTo("http://fs.lan:8080/v1/map/sprites/foresignal-" + sprite.sha8());
        style.get("layers").forEach(l -> {
            assertThat(l.get("type").asString()).isIn("symbol", "circle", "line", "fill");
            JsonNode layout = l.get("layout");
            if (layout != null) {
                assertThat(layout.has("text-field")).isFalse();
            }
        });
        JsonNode meta = style.get("metadata").get("foresignal");
        assertThat(meta.get("generatedAt").asString()).isEqualTo(T.toString());
        assertThat(meta.get("theme").asString()).isEqualTo("light");
        assertThat(meta.get("layers").get(0).get("id").asString()).isEqualTo("map-planes");
        assertThat(meta.get("layers").get(0).get("kinds").get(0).asString()).isEqualTo("aircraft");
        assertThat(meta.get("layers").get(0).get("attribution").asString()).isEqualTo("Credit map-planes");
        assertThat(meta.get("layers").get(0).get("aggBelowZoom").asInt()).isEqualTo(6);
        assertThat(meta.get("layers").get(1).get("aggBelowZoom").isNull()).isTrue();
    }

    @Test
    @DisplayName("a point icon: symbol layer, SDF icon-color, rotation from rotateField aligned to the map")
    void iconWithRotation() {
        JsonNode style = style(List.of(layer("map-planes", Set.of())), MapStyleService.Theme.LIGHT, URLS);
        JsonNode point = layerById(style, "map-planes--aircraft--point");
        assertThat(point.get("type").asString()).isEqualTo("symbol");
        assertThat(point.get("source").asString()).isEqualTo("map-planes");
        assertThat(point.get("source-layer").asString()).isEqualTo("map-planes");
        assertThat(point.get("layout").get("icon-image").asString()).isEqualTo("plane");
        assertThat(point.get("layout").get("icon-allow-overlap").asBoolean()).isTrue();
        assertThat(point.get("layout").get("icon-rotation-alignment").asString()).isEqualTo("map");
        assertThat(point.get("layout").get("icon-rotate").toString())
                .isEqualTo("[\"coalesce\",[\"to-number\",[\"get\",\"track\"]],0]");
        assertThat(point.get("paint").get("icon-color").asString()).isEqualTo("#112233");
        assertThat(point.get("paint").get("icon-halo-color").asString()).isEqualTo("#FFFFFF");
        // A single-kind layer is filtered by geometry only.
        assertThat(point.get("filter").toString())
                .isEqualTo("[\"match\",[\"geometry-type\"],[\"Point\",\"MultiPoint\"],true,false]");
        assertThat(sprite.contains("plane")).isTrue();
    }

    @Test
    @DisplayName("a point without an icon: a circle whose radius follows the zoom and sizeField")
    void circleWithSize() {
        JsonNode style = style(List.of(layer("map-fires", Set.of())), MapStyleService.Theme.DARK, URLS);
        JsonNode point = layerById(style, "map-fires--hotspot--point");
        assertThat(point.get("type").asString()).isEqualTo("circle");
        String radius = point.get("paint").get("circle-radius").toString();
        assertThat(radius).startsWith("[\"interpolate\",[\"linear\"],[\"zoom\"]").contains("[\"get\",\"frp\"]")
                .contains("\"sqrt\"");
        assertThat(point.get("paint").get("circle-color").asString()).isEqualTo("#E8590C");
        assertThat(point.get("paint").get("circle-stroke-color").asString()).isEqualTo("#1D1F23");
        assertThat(point.has("minzoom")).isFalse();
    }

    @Test
    @DisplayName("lines and polygons: a line layer, a fill at 0.25 and its outline, each filtered by geometry type")
    void linesAndPolygons() {
        JsonNode style = style(List.of(layer("map-fires", Set.of())), MapStyleService.Theme.LIGHT, URLS);
        assertThat(layerIds(style)).containsExactly("map-fires--hotspot--point", "map-fires--hotspot--line",
                "map-fires--hotspot--fill", "map-fires--hotspot--outline");
        JsonNode fill = layerById(style, "map-fires--hotspot--fill");
        assertThat(fill.get("type").asString()).isEqualTo("fill");
        assertThat(fill.get("paint").get("fill-opacity").asDouble()).isEqualTo(0.25);
        assertThat(fill.get("filter").toString()).contains("\"Polygon\",\"MultiPolygon\"");
        JsonNode outline = layerById(style, "map-fires--hotspot--outline");
        assertThat(outline.get("type").asString()).isEqualTo("line");
        assertThat(layerById(style, "map-fires--hotspot--line").get("filter").toString())
                .contains("\"LineString\",\"MultiLineString\"");
    }

    @Test
    @DisplayName("several kinds in one layer are filtered by kind, and severity drives a color match")
    void multiKindAndSeverity() {
        JsonNode style = style(List.of(layer("map-warnings", Set.of())), MapStyleService.Theme.LIGHT, URLS);
        JsonNode area = layerById(style, "map-warnings--warning-area--fill");
        assertThat(area.get("filter").toString())
                .startsWith("[\"all\",[\"==\",[\"get\",\"kind\"],\"warning-area\"]");
        String color = area.get("paint").get("fill-color").toString();
        assertThat(color).startsWith("[\"match\",[\"downcase\",[\"to-string\",[\"coalesce\",[\"get\",\"severity\"],\"\"]]]")
                .contains("[\"extreme\",\"critical\"],\"#C92A2A\"").endsWith(",\"#C92A2A\"]");
        // A kind without display hints falls back to the theme color, still matched on severity.
        assertThat(layerById(style, "map-warnings--warning-point--point").get("paint").get("circle-color").toString())
                .endsWith(",\"#1971C2\"]");
        assertThat(layerById(style, "map-warnings--warning-area--point").get("type").asString()).isEqualTo("symbol");
    }

    @Test
    @DisplayName("below aggBelowZoom a hex source and fill ramp hand over to the raw layers at that zoom")
    void aggregationHandover() {
        JsonNode style = style(List.of(layer("map-planes", Set.of())), MapStyleService.Theme.LIGHT, URLS);
        JsonNode sources = style.get("sources");
        assertThat(sources.get("map-planes").get("type").asString()).isEqualTo("vector");
        assertThat(sources.get("map-planes").get("url").asString())
                .isEqualTo("http://fs.lan:8080/v1/tiles/map-planes.json");
        JsonNode agg = sources.get("map-planes--agg");
        assertThat(agg.get("tiles").get(0).asString())
                .isEqualTo("http://fs.lan:8080/v1/tiles/map-planes/{z}/{x}/{y}.mvt?agg=hex");
        assertThat(agg.get("maxzoom").asInt()).isEqualTo(6);
        JsonNode aggLayer = layerById(style, "map-planes--agg");
        assertThat(aggLayer.get("type").asString()).isEqualTo("fill");
        assertThat(aggLayer.get("maxzoom").asInt()).isEqualTo(6);
        assertThat(aggLayer.get("paint").get("fill-color").toString()).contains("[\"get\",\"count\"]");
        assertThat(layerById(style, "map-planes--aircraft--point").get("minzoom").asInt()).isEqualTo(6);
        assertThat(layerIds(style).getFirst()).isEqualTo("map-planes--agg");
    }

    @Test
    @DisplayName("through a tile key every generated URL carries the key prefix")
    void tileKeyPrefix() {
        JsonNode style = style(List.of(layer("map-planes", Set.of())), MapStyleService.Theme.LIGHT, KEYED);
        assertThat(style.get("sources").get("map-planes").get("url").asString())
                .isEqualTo("http://fs.lan:8080/v1/k/fsk_abcdefghijklmnop/tiles/map-planes.json");
        assertThat(style.get("sources").get("map-planes--agg").get("tiles").get(0).asString())
                .startsWith("http://fs.lan:8080/v1/k/fsk_abcdefghijklmnop/tiles/map-planes/");
        assertThat(style.get("sprite").asString())
                .isEqualTo("http://fs.lan:8080/v1/k/fsk_abcdefghijklmnop/map/sprites/" + sprite.spriteName());
        assertThat(KEYED.keyed()).isTrue();
        assertThat(URLS.keyed()).isFalse();
    }

    @Test
    @DisplayName("a private source is absent for a caller that may not see it")
    void privateSourceOmitted() {
        List<TileLayer> anonymous = TileLayerResolver.layers(REGISTRY, Set.of("map-secret"));
        JsonNode style = style(anonymous, MapStyleService.Theme.LIGHT, URLS);
        assertThat(style.get("sources").has("map-secret")).isFalse();
        assertThat(layerIds(style)).noneMatch(id -> id.startsWith("map-secret"));
        List<TileLayer> admin = TileLayerResolver.layers(REGISTRY, Set.of());
        assertThat(style(admin, MapStyleService.Theme.LIGHT, URLS).get("sources").has("map-secret")).isTrue();
    }

    @Test
    @DisplayName("icon and color by value: a match on iconField over iconCases, and on colorField over colorCases")
    void iconAndColorByValue() {
        JsonNode style = style(List.of(layer("map-fields", Set.of())), MapStyleService.Theme.DARK, URLS);
        JsonNode point = layerById(style, "map-fields--airport--point");
        String value = "[\"downcase\",[\"to-string\",[\"coalesce\",[\"get\",\"%s\"],\"\"]]]";
        assertThat(point.get("layout").get("icon-image").toString()).isEqualTo("[\"match\"," + value.formatted("chartSymbol")
                + ",\"hard\",\"airport-hard\",\"major\",\"airport-major\",\"airport-soft\"]");
        assertThat(point.get("paint").get("icon-color").toString())
                .isEqualTo("[\"match\"," + value.formatted("towered") + ",\"true\",\"#4F8FE8\",\"#D04FB4\"]");
        assertThat(point.get("layout").get("icon-rotate").toString()).contains("runwayHeading");
        assertThat(DescriptorValidator.problems(FIELDS, null)).isEmpty();
        // A kind without cases keeps its constant icon.
        assertThat(MapStyleService.iconImage(new KindDisplay(null, "plane", null, null))).isEqualTo("plane");
    }

    @Test
    @DisplayName("value cases need their field and fallback, sprite icons, #RRGGBB colors and lowercase keys")
    void caseValidation() {
        SourceDescriptor bad = base("map-bad-cases", "airport")
                .tileProperties("chartSymbol")
                .kindDisplay("airport", new KindDisplay(null, null, "#112233", null, null, null, null, "chartSymbol",
                        Map.of("Major", "no-such-icon"), Map.of("true", "blue")))
                .build();
        assertThat(DescriptorValidator.problems(bad, null))
                .anySatisfy(p -> assertThat(p).contains("iconCases needs iconField and a fallback icon"))
                .anySatisfy(p -> assertThat(p).contains("iconCases key 'Major' must be lowercase"))
                .anySatisfy(p -> assertThat(p).contains("iconCases icon 'no-such-icon'"))
                .anySatisfy(p -> assertThat(p).contains("colorCases needs colorField"))
                .anySatisfy(p -> assertThat(p).contains("colorCases color 'blue' must be #RRGGBB"));
        SourceDescriptor noCases = base("map-bad-field", "airport")
                .tileProperties("chartSymbol")
                .kindDisplay("airport", new KindDisplay(null, "airport-soft", null, null, null, null, null,
                        "chartSymbol", null, null))
                .build();
        assertThat(DescriptorValidator.problems(noCases, null))
                .anySatisfy(p -> assertThat(p).contains("iconField needs iconCases"));
    }

    @Test
    @DisplayName("a style field outside tileProperties, or an icon outside the sprite, fails validation")
    void validation() {
        SourceDescriptor missingRotate = base("map-bad", "aircraft")
                .tileProperties("gs")
                .kindDisplay("aircraft", new KindDisplay(null, "plane", "#112233", null, "track", null, null))
                .build();
        assertThat(DescriptorValidator.problems(missingRotate, null))
                .anySatisfy(p -> assertThat(p).contains("rotateField 'track'").contains("tileProperties"));
        SourceDescriptor missingSize = base("map-bad2", "hotspot")
                .kindDisplay("hotspot", new KindDisplay(null, null, null, null, null, "frp", "level"))
                .build();
        assertThat(DescriptorValidator.problems(missingSize, null))
                .anySatisfy(p -> assertThat(p).contains("sizeField 'frp'"))
                .anySatisfy(p -> assertThat(p).contains("colorField 'level'"));
        SourceDescriptor unknownIcon = base("map-bad3", "thing")
                .kindDisplay("thing", new KindDisplay(null, "no-such-icon", null, null))
                .build();
        assertThat(DescriptorValidator.problems(unknownIcon, null))
                .anySatisfy(p -> assertThat(p).contains("icon 'no-such-icon'").contains("valid names").contains("volcano"));
        assertThat(DescriptorValidator.problems(PLANES, null)).isEmpty();
        assertThat(DescriptorValidator.problems(FIRES, null)).isEmpty();
    }

    @Test
    @DisplayName("the color helper returns the constant without a color field, and the theme parses")
    void helpers() {
        TileLayer fires = layer("map-fires", Set.of());
        assertThat(MapStyleService.color(fires, null, "#000000")).isEqualTo("#000000");
        assertThat(MapStyleService.kinds(layer("map-warnings", Set.of()))).containsExactly("warning-area", "warning-point");
        assertThat(MapStyleService.Theme.parse(null)).isEqualTo(MapStyleService.Theme.LIGHT);
        assertThat(MapStyleService.Theme.parse("dark")).isEqualTo(MapStyleService.Theme.DARK);
        assertThat(MapStyleService.radius(null).toString()).doesNotContain("get");
        assertThat(service.generatedAt()).isEqualTo(T);
        Map<String, Object> empty = service.style(List.of(), MapStyleService.Theme.LIGHT, URLS);
        assertThat((Map<?, ?>) empty.get("sources")).isEmpty();
    }
}
