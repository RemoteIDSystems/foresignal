package ai.foresignal.raster.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * One registered style version (prompt 045; Weather-Imagery-Design §6.3), as read from
 * {@code src/main/resources/raster/styles/{name}@{version}.json}.
 *
 * <ul>
 *   <li>{@value #CONTINUOUS}: colors interpolate between {@code stops} (values outside clamp to the
 *       end colors);
 *   <li>{@value #DISCRETE}: each stop colors values from its value up to the next stop; values below
 *       the first stop are transparent;
 *   <li>{@value #CATEGORICAL}: {@code categories} color raw codes.
 * </ul>
 * {@code specials} color the product's special codes ({@code noCoverage}, {@code noEcho},
 * {@code noData}); a special the style does not list is transparent. Values are physical, in the
 * style's {@code units}: physical = {@code code × scale + offset}. A style whose {@code passthrough}
 * is {@code rgba8} draws an rgba8 frame's own colors ({@code rgb@1}).
 *
 * @param id the style name, kebab-case
 * @param version the version, at least 1
 * @param type continuous, discrete or categorical
 * @param units physical units of the stop values, or null
 * @param stops color stops, ascending by value
 * @param categories categories by raw code
 * @param specials special-code renderings
 * @param meaning plain-language meanings of value ranges
 * @param notice ramp origin and license, or null
 * @param passthrough {@code rgba8} for a passthrough style, else null
 */
public record StyleDefinition(String id, int version, String type, String units, List<Stop> stops,
        List<Category> categories, List<Special> specials, List<Meaning> meaning, String notice, String passthrough) {

    /** Interpolated stops. */
    public static final String CONTINUOUS = "continuous";

    /** Stepped stops. */
    public static final String DISCRETE = "discrete";

    /** Colors by raw code. */
    public static final String CATEGORICAL = "categorical";

    /** Every type. */
    public static final Set<String> TYPES = Set.of(CONTINUOUS, DISCRETE, CATEGORICAL);

    /** Every special kind. */
    public static final Set<String> SPECIAL_KINDS = Set.of("noCoverage", "noEcho", "noData");

    /** Validates the definition and sorts the stops. */
    public StyleDefinition {
        if (id == null || !id.matches("^[a-z0-9-]+$")) {
            throw new IllegalArgumentException("Style id must be kebab-case, was " + id);
        }
        if (version < 1) {
            throw new IllegalArgumentException("Style " + id + " version must be at least 1");
        }
        if (type == null || !TYPES.contains(type)) {
            throw new IllegalArgumentException("Style " + id + " type must be one of " + TYPES);
        }
        stops = stops == null ? List.of() : stops.stream().sorted(Comparator.comparingDouble(Stop::value)).toList();
        categories = categories == null ? List.of() : List.copyOf(categories);
        specials = specials == null ? List.of() : List.copyOf(specials);
        meaning = meaning == null ? List.of() : List.copyOf(meaning);
        for (Special s : specials) {
            if (!SPECIAL_KINDS.contains(s.kind())) {
                throw new IllegalArgumentException("Style " + id + " special kind must be one of " + SPECIAL_KINDS);
            }
        }
        if (passthrough == null && !CATEGORICAL.equals(type) && stops.isEmpty()) {
            throw new IllegalArgumentException("Style " + id + " needs at least one stop");
        }
        if (passthrough != null && !passthrough.equals("rgba8")) {
            throw new IllegalArgumentException("Style " + id + " passthrough must be rgba8");
        }
    }

    /** {@code id@version}. */
    public String ref() {
        return id + "@" + version;
    }

    /** True for a passthrough style. */
    public boolean isPassthrough() {
        return passthrough != null;
    }

    /**
     * A color stop.
     *
     * @param value physical value
     * @param color {@code #RRGGBB}, {@code #RRGGBBAA} or {@code transparent}
     * @param label legend label, or null
     */
    public record Stop(double value, String color, String label) {}

    /**
     * A category.
     *
     * @param code the raw code
     * @param color its color
     * @param label its label
     */
    public record Category(int code, String color, String label) {}

    /**
     * A special-code rendering.
     *
     * @param kind noCoverage, noEcho or noData
     * @param color {@code transparent} or a color
     * @param label legend label
     */
    public record Special(String kind, String color, String label) {}

    /**
     * The meaning of a value range.
     *
     * @param min inclusive lower bound, or null
     * @param max exclusive upper bound, or null
     * @param text the plain-language meaning
     */
    public record Meaning(Double min, Double max, String text) {}

    /** The color as ARGB; {@code transparent} is 0. */
    public static int argb(String color) {
        if (color == null) {
            throw new IllegalArgumentException("A color is required");
        }
        String c = color.strip().toLowerCase(Locale.ROOT);
        if (c.equals("transparent")) {
            return 0;
        }
        if (!c.matches("^#[0-9a-f]{6}([0-9a-f]{2})?$")) {
            throw new IllegalArgumentException("Color must be #RRGGBB, #RRGGBBAA or transparent, was " + color);
        }
        int rgb = Integer.parseInt(c.substring(1, 7), 16);
        int alpha = c.length() == 9 ? Integer.parseInt(c.substring(7, 9), 16) : 0xFF;
        return alpha << 24 | rgb;
    }

    /** The definition as the API's {@code Style} object. */
    public Map<String, Object> toMap(boolean current) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", id);
        out.put("version", version);
        out.put("ref", ref());
        out.put("current", current);
        out.put("type", type);
        out.put("units", units);
        List<Map<String, Object>> s = new ArrayList<>();
        for (Stop stop : stops) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("value", stop.value());
            m.put("color", stop.color());
            m.put("label", stop.label());
            s.add(m);
        }
        out.put("stops", s);
        List<Map<String, Object>> c = new ArrayList<>();
        for (Category cat : categories) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", cat.code());
            m.put("color", cat.color());
            m.put("label", cat.label());
            c.add(m);
        }
        out.put("categories", c);
        List<Map<String, Object>> sp = new ArrayList<>();
        for (Special special : specials) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("kind", special.kind());
            m.put("color", special.color());
            m.put("label", special.label());
            sp.add(m);
        }
        out.put("specials", sp);
        List<Map<String, Object>> me = new ArrayList<>();
        for (Meaning mn : meaning) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("min", mn.min());
            m.put("max", mn.max());
            m.put("text", mn.text());
            me.add(m);
        }
        out.put("meaning", me);
        out.put("notice", notice);
        return out;
    }
}
