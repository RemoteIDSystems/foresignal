package ai.foresignal.source.adapters.gvp;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link GvpWfsParser} contract (prompt 094). */
class GvpWfsParserTest {

    private final SourceDescriptor descriptor = new GvpVolcanoesAdapter("").descriptor();

    @Test
    @DisplayName("parses features, keeps BCE years negative and omits a missing eruption year")
    void parses() {
        List<NormalizedRecord> records =
                GvpWfsParser.parse(AdapterFixtures.bytes(GvpVolcanoesAdapter.ID, "volcanoes.geojson"));
        assertThat(records).hasSize(3);
        assertThat(records).extracting(NormalizedRecord::externalId).containsExactly("210010", "210020", "210030");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor, records);

        NormalizedRecord etna = records.get(0);
        assertThat(etna.payload()).containsEntry("name", "Etna")
                .containsEntry("primaryType", "Stratovolcano")
                .containsEntry("lastEruptionYear", 2021L)
                .containsEntry("country", "Italy")
                .containsEntry("elevationM", 3357.0);
        assertThat(etna.geometry()).isNotNull();

        NormalizedRecord vesuvius = records.get(1);
        assertThat(vesuvius.payload()).containsEntry("lastEruptionYear", 1944L);
        assertThat(vesuvius.payload()).doesNotContainKey("summary");

        NormalizedRecord ancient = records.get(2);
        assertThat(ancient.payload()).containsEntry("lastEruptionYear", -1200L);
    }
}
