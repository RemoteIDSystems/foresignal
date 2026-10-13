package ai.foresignal.map;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The shipped MapLibre sprite and the KML icons (prompt 058), committed under
 * {@code src/main/resources/map/} by {@code scripts/build-sprite.sh} from Maki and Temaki (CC0) with
 * {@code spreet --sdf}.
 *
 * <ul>
 *   <li>{@code map/sprite/foresignal.{json,png}} and {@code foresignal@2x.{json,png}}: the 1x and 2x
 *       sheets; every entry is a signed distance field ({@code "sdf": true}), so a style colors it with
 *       {@code icon-color}.
 *   <li>{@code map/icons/<name>.png}: one white 32 px PNG per icon for KML {@code IconStyle} (Google
 *       Earth multiplies it by the style color).
 * </ul>
 *
 * <p>The sheets are content-addressed: {@link #sha8()} is the first eight hex digits of the SHA-256 of
 * the 1x PNG followed by the 2x PNG, and the style's {@code sprite} URL is
 * {@code <base>/v1/map/sprites/foresignal-{sha8}}, served {@code immutable}. A rebuilt sprite gets a
 * new hash, so a cached old one is never mixed with a new index.
 *
 * <p>The icon names are also read statically ({@link #iconNames()}) by {@code DescriptorValidator}, so
 * a {@code kindDisplay.icon} outside the sprite fails startup with the list of valid names.
 */
@Component
public class SpriteCatalog {

    /** The classpath folder of the sheets. */
    public static final String SPRITE_PATH = "map/sprite/";

    /** The classpath folder of the 32 px KML icons. */
    public static final String ICON_PATH = "map/icons/";

    /** The base name of every sheet file. */
    public static final String BASE_NAME = "foresignal";

    /** A valid icon name (getMapIcon's path pattern). */
    public static final Pattern NAME = Pattern.compile("^[a-z0-9-]+$");

    /** A sprite file name: {@code foresignal-{sha8}[@2x].{json|png}} (getMapSprite's path pattern). */
    public static final Pattern SPRITE_FILE = Pattern.compile("^foresignal-([0-9a-f]{8})(@2x)?\\.(json|png)$");

    private static volatile Set<String> staticNames;

    private final Sheet standard;
    private final Sheet retina;
    private final String sha8;
    private final Map<String, Optional<byte[]>> icons = new ConcurrentHashMap<>();

    /**
     * Loads both sheets from the classpath.
     *
     * @throws IllegalStateException when a sheet is missing or its index does not parse
     */
    public SpriteCatalog(JsonMapper json) {
        this.standard = sheet(json, BASE_NAME);
        this.retina = sheet(json, BASE_NAME + "@2x");
        this.sha8 = sha8(standard.png(), retina.png());
    }

    /**
     * One sprite sheet.
     *
     * @param index the index JSON bytes as shipped
     * @param png the sheet PNG bytes
     * @param entries icon name to its index entry
     */
    public record Sheet(byte[] index, byte[] png, Map<String, Entry> entries) {}

    /**
     * One index entry of a sheet.
     *
     * @param x left offset in the sheet
     * @param y top offset in the sheet
     * @param width width in pixels
     * @param height height in pixels
     * @param pixelRatio 1 or 2
     * @param sdf true for a signed distance field
     */
    public record Entry(int x, int y, int width, int height, double pixelRatio, boolean sdf) {}

    /** The content hash of the sheets: eight lowercase hex digits. */
    public String sha8() {
        return sha8;
    }

    /** {@code foresignal-{sha8}}: the sprite name the style references. */
    public String spriteName() {
        return BASE_NAME + "-" + sha8;
    }

    /** The 1x sheet ({@code retina} false) or the 2x sheet. */
    public Sheet sheet(boolean retinaSheet) {
        return retinaSheet ? retina : standard;
    }

    /** The icon names of the 1x sheet, sorted. */
    public Set<String> names() {
        return Collections.unmodifiableSet(new TreeSet<>(standard.entries().keySet()));
    }

    /** True when {@code name} is an icon of the sprite. */
    public boolean contains(String name) {
        return name != null && standard.entries().containsKey(name);
    }

    /** The 32 px KML icon PNG of {@code name}, when the sprite has it. */
    public Optional<byte[]> icon(String name) {
        if (name == null || !NAME.matcher(name).matches() || !contains(name)) {
            return Optional.empty();
        }
        return icons.computeIfAbsent(name, n -> Optional.ofNullable(resource(ICON_PATH + n + ".png")));
    }

    /**
     * The icon names of the shipped 1x sheet, read once from the classpath without Spring (for
     * {@code DescriptorValidator}, which is static).
     *
     * @throws IllegalStateException when the index is missing or does not parse
     */
    public static Set<String> iconNames() {
        Set<String> names = staticNames;
        if (names == null) {
            synchronized (SpriteCatalog.class) {
                names = staticNames;
                if (names == null) {
                    byte[] index = resource(SPRITE_PATH + BASE_NAME + ".json");
                    if (index == null) {
                        throw new IllegalStateException("The sprite index " + SPRITE_PATH + BASE_NAME
                                + ".json is missing; run scripts/build-sprite.sh");
                    }
                    names = Collections.unmodifiableSet(
                            new TreeSet<>(parse(JsonMapper.builder().build(), index, BASE_NAME).keySet()));
                    staticNames = names;
                }
            }
        }
        return names;
    }

    /** The first eight hex digits of the SHA-256 over {@code parts}, in order. */
    static String sha8(byte[]... parts) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (byte[] part : parts) {
                digest.update(part);
            }
            return HexFormat.of().formatHex(digest.digest()).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static Sheet sheet(JsonMapper json, String name) {
        byte[] index = resource(SPRITE_PATH + name + ".json");
        byte[] png = resource(SPRITE_PATH + name + ".png");
        if (index == null || png == null) {
            throw new IllegalStateException("The sprite sheet " + SPRITE_PATH + name
                    + ".{json,png} is missing; run scripts/build-sprite.sh");
        }
        return new Sheet(index, png, parse(json, index, name));
    }

    private static Map<String, Entry> parse(JsonMapper json, byte[] index, String name) {
        JsonNode root;
        try {
            root = json.readTree(new String(index, StandardCharsets.UTF_8));
        } catch (RuntimeException e) {
            throw new IllegalStateException("The sprite index " + name + ".json does not parse", e);
        }
        if (root == null || !root.isObject()) {
            throw new IllegalStateException("The sprite index " + name + ".json is not a JSON object");
        }
        Map<String, Entry> entries = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> e : root.properties()) {
            JsonNode v = e.getValue();
            entries.put(e.getKey(), new Entry(v.path("x").asInt(), v.path("y").asInt(), v.path("width").asInt(),
                    v.path("height").asInt(), v.path("pixelRatio").asDouble(1), v.path("sdf").asBoolean(false)));
        }
        return Collections.unmodifiableMap(entries);
    }

    private static byte[] resource(String path) {
        try (InputStream in = SpriteCatalog.class.getClassLoader().getResourceAsStream(path)) {
            return in == null ? null : in.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + path, e);
        }
    }
}
