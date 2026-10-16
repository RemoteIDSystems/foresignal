package ai.foresignal.source.adapters.heatrisk;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** HeatRisk data-page link discovery (prompt 106). */
class HeatRiskLinksTest {

    @Test
    @DisplayName("finds the 7 daily GeoTIFF links, ignoring duplicates")
    void links() {
        StringBuilder html = new StringBuilder();
        for (int d = 1; d <= 7; d++) {
            html.append("<a href=\"data/HeatRisk_").append(d).append("_Mercator.tif\">day ").append(d).append("</a>\n");
        }
        html.append("<a href=\"data/HeatRisk_3_Mercator.tif\">dup</a>\n");
        Map<Integer, String> links = HeatRiskAdapter.links(html.toString());
        assertThat(links).hasSize(7);
        assertThat(links.get(1)).endsWith("HeatRisk_1_Mercator.tif");
        assertThat(links.get(7)).endsWith("HeatRisk_7_Mercator.tif");
    }

    @Test
    @DisplayName("a page with no links is empty")
    void none() {
        assertThat(HeatRiskAdapter.links("<html/>")).isEmpty();
    }
}
