package ai.foresignal.source.adapters.humanitarian;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.CountyGeometries;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code openfema-declarations} parsing (prompt 090): one record per designated area. */
class OpenFemaParserTest {

    private static final OpenFemaDeclarationsAdapter ADAPTER =
            new OpenFemaDeclarationsAdapter("", CountyGeometries.none());

    @Test
    @DisplayName("the captured declarations map to disaster-declaration records")
    void declarations() {
        List<NormalizedRecord> records = ADAPTER.parse(
                AdapterFixtures.bytes("openfema-declarations", "declarations.json"));
        assertThat(records).isNotEmpty();
        NormalizedRecord first = records.get(0);
        assertThat(first.kind()).isEqualTo("disaster-declaration");
        assertThat(first.externalId()).isNotBlank();
        assertThat(first.payload()).containsKeys("declarationTitle", "declarationType", "state", "designatedArea");
    }

    @Test
    @DisplayName("a hand-made 000 county row parses (state polygon; null without county geometries)")
    void stateCounty() {
        List<NormalizedRecord> records = ADAPTER.parse(
                AdapterFixtures.bytes("openfema-declarations", "state-000.json"));
        assertThat(records).hasSize(1);
        assertThat(records.get(0).externalId()).isEqualTo("fema-state-1");
        assertThat(records.get(0).geometry()).as("none() has no polygons").isNull();
    }
}
