package ai.foresignal.raster.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.RasterDtype;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The style registry of prompt 045: every shipped style compiles within 256 colors, the MRMS code
 * mapping (0 and 1 transparent, code 166 = 50 dBZ), versioned and unversioned names, and unknown
 * names.
 */
class StyleRegistryTest {

    private final StyleRegistry styles = new StyleRegistry();

    @Test
    @DisplayName("ships the nine prompt 045 styles, each compiling to at most 256 colors with entry 0 transparent")
    void shippedStyles() {
        assertThat(styles.all()).extracting(StyleDefinition::ref).containsExactly("ir-enhanced@1", "ir-gray@1",
                "mesh@1", "nws-reflectivity@1", "precip-rate@1", "rgb@1", "smoke@1", "temperature@1", "wind-speed@1");
        ProductSpec u8 = ProductSpec.builder("p").dtype(RasterDtype.U8).build();
        ProductSpec u16 = ProductSpec.builder("q").dtype(RasterDtype.U16).build();
        ProductSpec f32 = ProductSpec.builder("r").dtype(RasterDtype.F32).build();
        for (StyleDefinition style : styles.all()) {
            for (ProductSpec spec : List.of(u8, u16, f32)) {
                CompiledStyle compiled = styles.compiled(style, spec);
                assertThat(compiled.palette().length).isBetween(1, 256);
                assertThat(compiled.color(0)).isZero();
                assertThat(compiled.colorModel().getMapSize()).isLessThanOrEqualTo(256);
            }
            assertThat(styles.isCurrent(style)).isTrue();
            assertThat(style.toMap(true)).containsKeys("id", "version", "ref", "current", "type", "stops", "notice");
        }
        assertThat(styles.compiled(styles.resolve("precip-rate").style(), u16).table()).hasSize(65_536);
        assertThat(styles.compiled(styles.resolve("mesh").style(), u8).table()).hasSize(256);
        assertThat(styles.compiled(styles.resolve("temperature").style(), f32).table()).isNull();
    }

    @Test
    @DisplayName("MRMS codes: 0 no coverage and 1 no echo are transparent, 166 is 50 dBZ, 2 (−32 dBZ) is transparent")
    void mrmsCodes() {
        ProductSpec spec = RenderFixtures.reflectivity();
        CompiledStyle style = styles.compiled(styles.resolve("nws-reflectivity@1").style(), spec);
        byte[] table = style.table();
        assertThat(style.color(table[0] & 0xFF)).isZero();
        assertThat(style.color(table[1] & 0xFF)).isZero();
        assertThat(spec.physical(166)).isEqualTo(50.0);
        assertThat(style.color(table[166] & 0xFF)).isEqualTo(0xFFFD0000);
        assertThat(style.color(table[2] & 0xFF) >>> 24).isZero();
        assertThat(style.color(table[76] & 0xFF)).as("5 dBZ, the first step").isEqualTo(0xFF04E9E7);
        assertThat(style.color(table[255] & 0xFF)).as("94.5 dBZ, the top step").isEqualTo(0xFFFDFDFD);
        assertThat(style.isSpecial(0)).isTrue();
        assertThat(style.isSpecial(66)).isFalse();
    }

    @Test
    @DisplayName("name@v is explicit; a bare name resolves to the current version and is not; versioned() fills it in")
    void versions() {
        StyleRegistry.Resolved explicit = styles.resolve("nws-reflectivity@1");
        assertThat(explicit.explicitVersion()).isTrue();
        StyleRegistry.Resolved bare = styles.resolve("nws-reflectivity");
        assertThat(bare.explicitVersion()).isFalse();
        assertThat(bare.ref()).isEqualTo("nws-reflectivity@1");
        assertThat(styles.versioned("ir-gray")).isEqualTo("ir-gray@1");
        assertThat(styles.versioned("not-a-style")).isEqualTo("not-a-style");
        StyleRegistry two = new StyleRegistry(List.of(def("ramp", 1), def("ramp", 2)));
        assertThat(two.resolve("ramp").ref()).isEqualTo("ramp@2");
        assertThat(two.resolve("ramp@1").ref()).isEqualTo("ramp@1");
        assertThat(two.isCurrent(two.resolve("ramp@1").style())).isFalse();
    }

    @Test
    @DisplayName("unknown names, unknown versions and malformed names are 400 UNKNOWN_STYLE")
    void unknown() {
        for (String ref : List.of("viridis", "nws-reflectivity@9", "NWS", "a b", "x@y")) {
            assertThatThrownBy(() -> styles.resolve(ref))
                    .isInstanceOfSatisfying(ForesignalException.class, e -> {
                        assertThat(e.getCode()).isEqualTo("UNKNOWN_STYLE");
                        assertThat(e.getStatus().value()).isEqualTo(400);
                    });
        }
        assertThat(styles.find(null)).isEmpty();
    }

    @Test
    @DisplayName("duplicate versions, bad colors and more than 256 colors are refused")
    void invalidDefinitions() {
        assertThatThrownBy(() -> new StyleRegistry(List.of(def("d", 1), def("d", 1))))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> StyleDefinition.argb("red")).isInstanceOf(IllegalArgumentException.class);
        assertThat(StyleDefinition.argb("#00000000")).isZero();
        assertThat(StyleDefinition.argb("transparent")).isZero();
        java.util.List<StyleDefinition.Category> many = new java.util.ArrayList<>();
        for (int i = 0; i < 300; i++) {
            many.add(new StyleDefinition.Category(i, "#123456", "c" + i));
        }
        StyleDefinition big = new StyleDefinition("big", 1, StyleDefinition.CATEGORICAL, null, null, many, null, null,
                null, null);
        assertThatThrownBy(() -> CompiledStyle.compile(big, ProductSpec.builder("p").dtype(RasterDtype.U16).build()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> StyleRegistry.parse(Map.of("id", "x", "version", 1, "type", "rainbow")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("categorical styles map raw codes; continuous styles clamp at both ends")
    void categoricalAndContinuous() {
        StyleDefinition flags = new StyleDefinition("flags", 1, StyleDefinition.CATEGORICAL, null, null,
                List.of(new StyleDefinition.Category(3, "#ff0000", "Snow"), new StyleDefinition.Category(6, "#00ff00",
                        "Hail")), null, null, null, null);
        CompiledStyle compiled = CompiledStyle.compile(flags, ProductSpec.builder("f").dtype(RasterDtype.U8).build());
        assertThat(compiled.color(compiled.table()[3] & 0xFF)).isEqualTo(0xFFFF0000);
        assertThat(compiled.color(compiled.table()[6] & 0xFF)).isEqualTo(0xFF00FF00);
        assertThat(compiled.table()[4]).isZero();
        CompiledStyle gray = styles.compiled(styles.resolve("ir-gray@1").style(),
                ProductSpec.builder("ir").dtype(RasterDtype.F32).build());
        assertThat(gray.color(gray.indexOfValue(100))).isEqualTo(0xFFFFFFFF);
        assertThat(gray.color(gray.indexOfValue(400))).isEqualTo(0xFF000000);
        assertThat(gray.indexOfValue(Double.NaN)).isZero();
    }

    private static StyleDefinition def(String id, int version) {
        return new StyleDefinition(id, version, StyleDefinition.CONTINUOUS, "K",
                List.of(new StyleDefinition.Stop(0, "#000000", null), new StyleDefinition.Stop(1, "#ffffff", null)),
                null, null, null, null, null);
    }
}
