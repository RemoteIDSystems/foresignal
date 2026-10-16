package ai.foresignal.source.adapters.humanitarian;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code reliefweb-disasters} parsing (prompt 090). */
class ReliefWebParserTest {

    @Test
    @DisplayName("the hand-made body maps to disaster records with the primary-country point")
    void disasters() {
        List<NormalizedRecord> records = new ReliefWebDisastersAdapter("")
                .parse(AdapterFixtures.bytes("reliefweb-disasters", "disasters.json"));
        assertThat(records).hasSize(2);
        NormalizedRecord first = records.get(0);
        assertThat(first.externalId()).isEqualTo("rw:100001");
        assertThat(first.kind()).isEqualTo("disaster");
        assertThat(first.geometry()).isNotNull();
        assertThat(first.payload()).as("primary_country.location is a country point")
                .containsEntry("geometryApproximate", true);
        assertThat(first.payload()).containsEntry("name", "Example flood 2026")
                .containsEntry("status", "current");
        assertThat(first.payload().get("countries")).isEqualTo(List.of("PAK"));
        assertThat((String) first.payload().get("description")).doesNotContain("<p>");
    }
}
