package ai.foresignal.source.adapters.humanitarian;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code geonet-volcanoes} parsing (prompt 090). */
class GeoNetParserTest {

    @Test
    @DisplayName("a GeoJSON feature maps to a volcano-alert record with its level and color")
    void volcano() {
        List<NormalizedRecord> records = new GeoNetVolcanoAdapter("")
                .parse(AdapterFixtures.bytes("geonet-volcanoes", "volcano-val.json"));
        assertThat(records).isNotEmpty();
        NormalizedRecord taupo = records.stream().filter(r -> r.externalId().equals("geonet:taupo")).findFirst()
                .orElseThrow();
        assertThat(taupo.kind()).isEqualTo("volcano-alert");
        assertThat(taupo.observedAt()).as("RECEIVED").isNull();
        assertThat(taupo.geometry()).isNotNull();
        assertThat(taupo.payload()).containsEntry("nzLevel", 0).containsEntry("aviationColor", "Green");
    }
}
