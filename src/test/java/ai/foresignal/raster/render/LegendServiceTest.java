package ai.foresignal.raster.render;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.testsupport.openapi.SpecSchemas;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Legends of prompt 045: the JSON shape (validated against the spec's {@code Legend}), imperial and
 * metric unit conversion, and the PNG dimensions per orientation and theme.
 */
class LegendServiceTest {

    private static final StyleRegistry STYLES = new StyleRegistry();
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private final LegendService legends = new LegendService();

    @Test
    @DisplayName("the reflectivity legend has stops, ticks, specials and the 50 dBZ meaning, and validates as Legend")
    @SuppressWarnings("unchecked")
    void reflectivityShape() {
        Map<String, Object> legend = legends.legend(RenderFixtures.reflectivity(),
                STYLES.resolve("nws-reflectivity@1").style(), false);
        assertThat(legend).containsEntry("styleId", "nws-reflectivity").containsEntry("styleVersion", 1)
                .containsEntry("type", "discrete").containsEntry("unitsLabel", "dBZ");
        List<Map<String, Object>> stops = (List<Map<String, Object>>) legend.get("stops");
        assertThat(stops).hasSize(15);
        assertThat(stops.get(0)).containsEntry("value", 5.0).containsEntry("color", "#04e9e7");
        assertThat((List<?>) legend.get("ticks")).hasSize(15);
        assertThat((List<Map<String, Object>>) legend.get("specials")).extracting(m -> m.get("kind"))
                .containsExactly("noCoverage", "noEcho", "noData");
        assertThat((List<Map<String, Object>>) legend.get("meaning")).extracting(m -> m.get("text"))
                .contains("≥ 50 dBZ: heavy rain, hail possible");
        JsonNode node = JSON.readTree(JSON.writeValueAsString(legend));
        assertThat(new SpecSchemas().validateComponent("Legend", node)).isEmpty();
    }

    @Test
    @DisplayName("imperial: K to °F, mm/h to in/h, m/s to mph, mm to in; metric: K to °C")
    @SuppressWarnings("unchecked")
    void conversions() {
        Map<String, Object> f = legends.legend(RenderFixtures.temperature(false), STYLES.resolve("temperature@1").style(),
                true);
        assertThat(f).containsEntry("units", "degF").containsEntry("unitsLabel", "°F");
        List<Map<String, Object>> stops = (List<Map<String, Object>>) f.get("stops");
        assertThat((Double) stops.get(0).get("value")).isCloseTo(-40.0, within(1e-6));
        assertThat((Double) stops.get(stops.size() - 1).get("value")).isCloseTo(122.0, within(1e-6));
        Map<String, Object> c = legends.legend(RenderFixtures.temperature(false), STYLES.resolve("temperature@1").style(),
                false);
        assertThat(c).containsEntry("unitsLabel", "°C");
        assertThat((Double) ((List<Map<String, Object>>) c.get("stops")).get(0).get("value")).isCloseTo(-40.0,
                within(1e-6));
        assertThat(((List<Map<String, Object>>) c.get("meaning")).get(0).get("max")).isEqualTo(0.0);
        assertThat(UnitConversion.of("mm/h", true).apply(25.4)).isCloseTo(1.0, within(1e-12));
        assertThat(UnitConversion.of("mm", true).units()).isEqualTo("in");
        assertThat(UnitConversion.of("m/s", true).apply(10)).isCloseTo(22.369, within(1e-3));
        assertThat(UnitConversion.of("m/s", false).converts()).isFalse();
        assertThat(UnitConversion.of("dBZ", true).converts()).isFalse();
        assertThat(UnitConversion.of(null, true).label()).isNull();
        assertThat(UnitConversion.format(0.123)).isEqualTo("0.12");
        assertThat(UnitConversion.format(2.25)).isEqualTo("2.3");
        assertThat(UnitConversion.format(-0.001)).isEqualTo("0");
        assertThat(UnitConversion.format(122.0)).isEqualTo("122");
    }

    @Test
    @DisplayName("continuous ramps get five evenly spaced ticks")
    @SuppressWarnings("unchecked")
    void continuousTicks() {
        Map<String, Object> legend = legends.legend(RenderFixtures.temperature(false),
                STYLES.resolve("wind-speed@1").style(), false);
        List<Map<String, Object>> ticks = (List<Map<String, Object>>) legend.get("ticks");
        assertThat(ticks).extracting(t -> t.get("label")).containsExactly("0", "10", "20", "30", "40");
    }

    @Test
    @DisplayName("PNG: width × 48 horizontal, width × 256 vertical, in both themes")
    void pngDimensions() throws Exception {
        StyleDefinition style = STYLES.resolve("precip-rate@1").style();
        BufferedImage horizontal = ImageIO.read(new ByteArrayInputStream(
                legends.png(RenderFixtures.reflectivity(), style, false, 300, false, true)));
        assertThat(horizontal.getWidth()).isEqualTo(300);
        assertThat(horizontal.getHeight()).isEqualTo(LegendService.HORIZONTAL_HEIGHT);
        assertThat(horizontal.getRGB(0, 0) & 0xFFFFFF).as("dark background").isEqualTo(0x1E1E1E);
        BufferedImage vertical = ImageIO.read(new ByteArrayInputStream(
                legends.png(RenderFixtures.reflectivity(), style, true, 120, true, false)));
        assertThat(vertical.getWidth()).isEqualTo(120);
        assertThat(vertical.getHeight()).isEqualTo(LegendService.VERTICAL_HEIGHT);
        assertThat(vertical.getRGB(119, 255) & 0xFFFFFF).as("light background").isEqualTo(0xFFFFFF);
    }
}
