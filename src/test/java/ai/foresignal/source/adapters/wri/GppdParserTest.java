package ai.foresignal.source.adapters.wri;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link GppdParser} contract on the WRI GPPD fixture (prompt 094). */
class GppdParserTest {

    private final SourceDescriptor descriptor = new WriPowerPlantsAdapter("").descriptor();

    @Test
    @DisplayName("parses each row, skips one without an id, and declares every payload key")
    void parses() {
        GppdParser.Parsed parsed = GppdParser.parseAll(AdapterFixtures.bytes(WriPowerPlantsAdapter.ID, "gppd.csv"));

        assertThat(parsed.records()).hasSize(3);
        assertThat(parsed.skipped()).isEqualTo(1);
        FieldSpecContract.assertPayloadKeysDeclared(descriptor, parsed.records());

        NormalizedRecord gorgas = record(parsed.records(), "USA0000001");
        assertThat(gorgas.kind()).isEqualTo(GppdParser.KIND);
        assertThat(gorgas.payload()).containsEntry("name", "Alabama Power Company, Gorgas")
                .containsEntry("country", "United States of America")
                .containsEntry("capacityMw", 1000.0)
                .containsEntry("primaryFuel", "Coal")
                .containsEntry("commissioningYear", 1951L);
        assertThat((List<String>) gorgas.payload().get("otherFuels")).containsExactly("Biomass");
        assertThat(gorgas.geometry().getSRID()).isEqualTo(4326);
        assertThat(gorgas.geometry().getCoordinate().x).isCloseTo(-87.2, org.assertj.core.data.Offset.offset(1e-6));
        assertThat(gorgas.geometry().getCoordinate().y).isCloseTo(33.7, org.assertj.core.data.Offset.offset(1e-6));

        NormalizedRecord greifswald = record(parsed.records(), "DEU0000003");
        assertThat(greifswald.payload()).doesNotContainKey("capacityMw");
        assertThat((List<String>) greifswald.payload().get("otherFuels")).isEmpty();
    }

    @Test
    @DisplayName("descriptor declares the frozen version and CC BY 4.0")
    void descriptor() {
        assertThat(descriptor.licence()).isEqualTo("CC BY 4.0");
        assertThat(descriptor.kinds()).contains(GppdParser.KIND, "dataset-release");
        assertThat(descriptor.attribution()).contains("World Resources Institute");
    }

    @SuppressWarnings("unchecked")
    private static NormalizedRecord record(List<NormalizedRecord> records, String id) {
        return records.stream().filter(r -> r.externalId().equals(id)).findFirst().orElseThrow();
    }
}
