package ai.foresignal.source.adapters.fema;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link FemaReactorParser} contract (prompt 094). */
class FemaReactorParserTest {

    private final SourceDescriptor descriptor = new NuclearReactorsUsAdapter("").descriptor();

    @Test
    @DisplayName("parses reactors with a prefixed id and the power percentage")
    void parses() {
        List<NormalizedRecord> records =
                FemaReactorParser.parse(AdapterFixtures.bytes(NuclearReactorsUsAdapter.ID, "reactors.geojson"));
        assertThat(records).extracting(NormalizedRecord::externalId)
                .containsExactly("reactor:05000266", "reactor:05000528");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor, records);

        NormalizedRecord seabrook = records.get(0);
        assertThat(seabrook.payload()).containsEntry("reactorName", "Seabrook")
                .containsEntry("powerPlant", "Seabrook Station")
                .containsEntry("state", "NH")
                .containsEntry("reactorType", "PWR")
                .containsEntry("status", "Power Operation")
                .containsEntry("powerPct", 100.0)
                .containsEntry("mwe", 1246.0)
                .containsEntry("nrcRegion", "Region I");
        assertThat(seabrook.geometry()).isNotNull();
        assertThat(records.get(1).payload()).containsEntry("powerPct", 85.0);
    }
}
