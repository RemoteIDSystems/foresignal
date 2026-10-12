package ai.foresignal.raster;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.ProductSpec;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamingAdapter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The external catalog of prompt 045: the entries load, every product passes the registry's rules,
 * the GIBS matrix sets agree with the native zooms, and the owning source is DERIVED with no way to
 * fetch anything (no polling or streaming implementation, no HTTP client field).
 */
class ExternalCatalogTest {

    private final ExternalCatalog catalog = new ExternalCatalog();

    @Test
    @DisplayName("loads the nine GIBS and five nowCOAST entries with every required member")
    void entriesLoad() {
        List<ExternalCatalog.Entry> entries = catalog.entries();
        assertThat(entries).hasSize(14);
        assertThat(entries.stream().filter(e -> "gibs".equals(e.document().get("provider")))).hasSize(9);
        assertThat(entries.stream().filter(e -> "nowcoast".equals(e.document().get("provider")))).hasSize(5);
        for (ExternalCatalog.Entry e : entries) {
            Map<String, Object> d = e.document();
            assertThat(d).containsKeys("externalTemplate", "tilingScheme", "tileSize", "maxNativeZoom", "bounds",
                    "attribution");
            String template = (String) d.get("externalTemplate");
            assertThat(template).startsWith("https://");
            assertThat(template.contains("{z}/{y}/{x}") || template.contains("{bbox-epsg-3857}")).as(e.id()).isTrue();
            assertThat(d.get("cors")).isEqualTo("*");
            assertThat(catalog.find(e.id())).contains(e);
        }
        assertThat(catalog.find("gibs-goes-east-geocolor").orElseThrow().document().get("externalTemplate"))
                .isEqualTo("https://gibs.earthdata.nasa.gov/wmts/epsg3857/best/GOES-East_ABI_GeoColor/default/{time}/"
                        + "GoogleMapsCompatible_Level7/{z}/{y}/{x}.png");
    }

    @Test
    @DisplayName("each GIBS template's matrix set level is its maxNativeZoom")
    void gibsLevels() {
        Pattern level = Pattern.compile("GoogleMapsCompatible_Level(\\d+)/");
        for (ExternalCatalog.Entry e : catalog.entries()) {
            if (!"gibs".equals(e.document().get("provider"))) {
                continue;
            }
            Matcher m = level.matcher((String) e.document().get("externalTemplate"));
            assertThat(m.find()).as(e.id()).isTrue();
            assertThat(Integer.parseInt(m.group(1))).as(e.id()).isEqualTo(e.document().get("maxNativeZoom"));
        }
    }

    @Test
    @DisplayName("every product is kind=external and passes the registry's rules under the DERIVED source")
    void productsAreValid() {
        ExternalCatalogSource source = new ExternalCatalogSource(catalog);
        SourceDescriptor descriptor = source.descriptor();
        assertThat(descriptor.id()).isEqualTo(ExternalCatalogSource.SOURCE_ID);
        assertThat(descriptor.mode()).isEqualTo(Mode.DERIVED);
        assertThat(descriptor.products()).hasSize(14);
        for (ProductSpec spec : descriptor.products()) {
            assertThat(spec.isExternal()).isTrue();
            assertThat(ProductRegistry.problems(spec, ExternalCatalogSource.SOURCE_ID, descriptor)).as(spec.id())
                    .isEmpty();
        }
        assertThat(ExternalCatalog.period(Map.of("period", "PT10M"))).hasMinutes(10);
        assertThat(ExternalCatalog.period(Map.of("period", "P1Y"))).as("calendar periods are not durations").isNull();
        assertThat(ExternalCatalog.period(null)).isNull();
    }

    @Test
    @DisplayName("nothing can fetch: the source is neither polling nor streaming and holds no HTTP client")
    void noHttpClient() {
        ExternalCatalogSource source = new ExternalCatalogSource(catalog);
        assertThat(source).isNotInstanceOf(PollingAdapter.class).isNotInstanceOf(StreamingAdapter.class);
        for (Class<?> type : List.of(ExternalCatalog.class, ExternalCatalogSource.class)) {
            for (Field field : type.getDeclaredFields()) {
                String name = field.getType().getName();
                assertThat(name).as(type.getSimpleName() + "." + field.getName())
                        .doesNotContain("Http").doesNotContain("UpstreamClient").doesNotContain("SourceHttp");
            }
        }
    }
}
