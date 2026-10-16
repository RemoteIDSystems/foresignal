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

    @Test
    @DisplayName("streams into a sink, counting a feature without a number and a malformed geometry kept bare")
    void streamsAndCounts() {
        String body = """
                {"type":"FeatureCollection","features":[
                  {"type":"Feature","properties":{"Volcano_Number":"1","Volcano_Name":"Good"},
                   "geometry":{"type":"Point","coordinates":[15.0,37.7]}},
                  {"type":"Feature","properties":{"Volcano_Number":"2","Volcano_Name":"Bad geometry"},
                   "geometry":{"type":"Point","coordinates":"nowhere"}},
                  {"type":"Feature","properties":{"Volcano_Name":"No number"},"geometry":null}
                ]}
                """;
        List<NormalizedRecord> out = new java.util.ArrayList<>();
        ai.foresignal.source.support.ReferenceSupport.ParseCounts counts =
                GvpWfsParser.parse(body.getBytes(java.nio.charset.StandardCharsets.UTF_8), out::add);

        assertThat(out).extracting(NormalizedRecord::externalId).containsExactly("1", "2");
        assertThat(out.get(0).geometry()).isNotNull();
        assertThat(out.get(1).geometry()).isNull();
        assertThat(counts.records()).isEqualTo(2);
        assertThat(counts.skipped()).isEqualTo(1);
        assertThat(counts.badGeometry()).isEqualTo(1);
    }
}
