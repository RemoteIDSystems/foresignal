package ai.foresignal.source.adapters.humanitarian;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code usgs-hans-volcanoes} parsing (prompt 090): the space-separated {@code sent_utc} and cached coords. */
class HansParserTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static Map<String, JsonNode> coords() {
        Map<String, JsonNode> coords = new HashMap<>();
        JsonNode us = JSON.readTree(AdapterFixtures.bytes("usgs-hans-volcanoes", "us-volcanoes.json"));
        for (JsonNode volcano : us) {
            if (volcano.has("vnum")) {
                coords.put(volcano.get("vnum").asString(), volcano);
            }
        }
        return coords;
    }

    @Test
    @DisplayName("a monitored volcano maps with a parsed observed time and, when known, its coordinates")
    void monitored() {
        List<NormalizedRecord> records = new UsgsHansVolcanoAdapter("")
                .parse(AdapterFixtures.bytes("usgs-hans-volcanoes", "monitored.json"), coords());
        assertThat(records).isNotEmpty();
        assertThat(records).allMatch(r -> r.observedAt() != null, "observedAt parsed");
        assertThat(records).extracting(NormalizedRecord::kind).containsOnly("volcano-alert");
        NormalizedRecord first = records.get(0);
        assertThat(first.externalId()).isEqualTo("usgs:311320");
        assertThat(first.payload()).containsEntry("alertLevel", "NORMAL").containsEntry("aviationColor", "GREEN");
        assertThat(records).anyMatch(r -> r.geometry() != null);
    }
}
