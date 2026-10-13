package ai.foresignal.map;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The shipped sprite (prompt 058): sheets, SDF flags, the content hash and the icon names adapters use. */
class SpriteCatalogTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    /** {@code new KindDisplay("template", "icon", …)} in adapter sources. */
    static final Pattern ADAPTER_ICON = Pattern.compile("new KindDisplay\\(\\s*(?:\"[^\"]*\"|null)\\s*,\\s*\"([^\"]+)\"");

    private final SpriteCatalog catalog = new SpriteCatalog(JSON);

    @Test
    @DisplayName("both sheets parse: PNG bytes, the same names, every entry SDF, pixel ratios 1 and 2")
    void sheets() {
        SpriteCatalog.Sheet standard = catalog.sheet(false);
        SpriteCatalog.Sheet retina = catalog.sheet(true);
        assertThat(standard.png()).startsWith((byte) 0x89, (byte) 'P', (byte) 'N', (byte) 'G');
        assertThat(retina.png()).startsWith((byte) 0x89, (byte) 'P', (byte) 'N', (byte) 'G');
        assertThat(standard.entries()).isNotEmpty();
        assertThat(retina.entries().keySet()).isEqualTo(standard.entries().keySet());
        assertThat(standard.entries().values()).allSatisfy(e -> {
            assertThat(e.sdf()).isTrue();
            assertThat(e.pixelRatio()).isEqualTo(1.0);
            assertThat(e.width()).isPositive();
        });
        assertThat(retina.entries().values()).allSatisfy(e -> assertThat(e.pixelRatio()).isEqualTo(2.0));
        JsonNode index = JSON.readTree(new String(standard.index(), java.nio.charset.StandardCharsets.UTF_8));
        assertThat(index.get("volcano").get("sdf").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("sha8 is eight hex digits of SHA-256 over both PNGs, and stable across loads")
    void sha8() {
        assertThat(catalog.sha8()).matches("^[0-9a-f]{8}$");
        assertThat(new SpriteCatalog(JSON).sha8()).isEqualTo(catalog.sha8());
        assertThat(catalog.sha8())
                .isEqualTo(SpriteCatalog.sha8(catalog.sheet(false).png(), catalog.sheet(true).png()));
        assertThat(catalog.spriteName()).isEqualTo("foresignal-" + catalog.sha8());
        assertThat(SpriteCatalog.SPRITE_FILE.matcher(catalog.spriteName() + "@2x.png").matches()).isTrue();
    }

    @Test
    @DisplayName("the default set and the adapter aliases are present; names are kebab-case")
    void names() {
        assertThat(catalog.names()).contains("marker", "circle", "triangle", "airport", "ferry", "fire-station",
                "danger", "rail", "bicycle", "communications-tower", "volcano", "earthquake");
        assertThat(catalog.names()).allSatisfy(n -> assertThat(n).matches(SpriteCatalog.NAME));
        assertThat(SpriteCatalog.iconNames()).isEqualTo(catalog.names());
    }

    @Test
    @DisplayName("each icon has a 32 px KML PNG; unknown and unsafe names have none")
    void icons() {
        assertThat(catalog.names()).allSatisfy(name -> assertThat(catalog.icon(name)).as(name).isPresent());
        byte[] png = catalog.icon("volcano").orElseThrow();
        assertThat(png).startsWith((byte) 0x89, (byte) 'P', (byte) 'N', (byte) 'G');
        // IHDR width and height (big-endian at offsets 16 and 20).
        assertThat(((png[18] & 0xff) << 8) | (png[19] & 0xff)).isEqualTo(32);
        assertThat(((png[22] & 0xff) << 8) | (png[23] & 0xff)).isEqualTo(32);
        assertThat(catalog.icon("no-such-icon")).isEmpty();
        assertThat(catalog.icon("../sprite/foresignal")).isEmpty();
        assertThat(catalog.icon(null)).isEmpty();
    }

    @Test
    @DisplayName("every kindDisplay.icon declared by a built-in adapter exists in the sprite")
    void adapterIcons() throws IOException {
        Set<String> used = new TreeSet<>();
        Path adapters = Path.of("src/main/java/ai/foresignal/source/adapters");
        try (Stream<Path> files = Files.walk(adapters)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                Matcher m = ADAPTER_ICON.matcher(Files.readString(file));
                while (m.find()) {
                    used.add(m.group(1));
                }
            }
        }
        assertThat(used).isNotEmpty();
        assertThat(catalog.names()).containsAll(used);
    }

    @Test
    @DisplayName("the index entries carry their positions")
    void entries() {
        Map<String, SpriteCatalog.Entry> entries = catalog.sheet(false).entries();
        SpriteCatalog.Entry volcano = entries.get("volcano");
        assertThat(volcano.x()).isNotNegative();
        assertThat(volcano.y()).isNotNegative();
        assertThat(catalog.contains("volcano")).isTrue();
        assertThat(catalog.contains("nope")).isFalse();
        assertThat(catalog.contains(null)).isFalse();
    }
}
