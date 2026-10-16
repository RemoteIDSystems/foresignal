package ai.foresignal.source.adapters.awc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The TAF change-group tokenizer (prompt 106). */
class TafGroupsTest {

    @Test
    @DisplayName("splits a TAF into initial and change groups with wind, visibility, wx and clouds")
    @SuppressWarnings("unchecked")
    void parses() {
        String raw = "TAF KFWD 152300Z 1600/1706 18012KT P6SM SCT040 "
                + "FM160300 20015G25KT 4SM -RA BKN015 "
                + "TEMPO 1606/1610 2SM TSRA OVC008CB";
        List<Map<String, Object>> groups = TafGroups.parse(raw);
        assertThat(groups).hasSize(3);
        assertThat(groups.get(0)).containsEntry("changeIndicator", "INITIAL");
        assertThat((Map<String, Object>) groups.get(0).get("wind")).containsEntry("speed", 12);
        assertThat(groups.get(1)).containsEntry("changeIndicator", "FM160300");
        assertThat(((Map<String, Object>) groups.get(1).get("wind")).get("gust")).isEqualTo(25);
        assertThat(groups.get(1).get("visibility")).isEqualTo("4SM");
        assertThat((List<Object>) groups.get(1).get("wx")).contains("-RA");
        assertThat((List<Object>) groups.get(2).get("clouds")).contains("OVC008CB");
        assertThat(groups.get(2)).containsEntry("from", "1606").containsEntry("to", "1610");
    }

    @Test
    @DisplayName("an empty or null body has no groups")
    void empty() {
        assertThat(TafGroups.parse(null)).isEmpty();
        assertThat(TafGroups.parse("  ")).isEmpty();
    }
}
