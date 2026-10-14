package ai.foresignal.source.adapters.humanitarian;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code unhcr-population} parsing (prompt 090): string numbers, the {@code -} sentinel, label points. */
class UnhcrParserTest {

    private static List<NormalizedRecord> cooRecords() {
        return new UnhcrPopulationAdapter("", new CountryCentroids())
                .parse(AdapterFixtures.bytes("unhcr-population", "population-2024.json"), 2024, "coo", "coo_iso");
    }

    @Test
    @DisplayName("a country-of-origin row becomes a displacement record at its label point")
    void coo() {
        List<NormalizedRecord> records = cooRecords();
        assertThat(records).isNotEmpty();
        NormalizedRecord afg = records.stream().filter(r -> r.externalId().equals("coo:AFG:2024")).findFirst()
                .orElseThrow();
        assertThat(afg.kind()).isEqualTo("displacement");
        assertThat(afg.observedAt()).as("RECEIVED").isNull();
        assertThat(afg.eventTime()).isNotNull();
        assertThat(afg.geometry()).isNotNull();
        assertThat(afg.payload()).containsEntry("year", 2024).containsEntry("refugees", 5766586.0)
                .containsEntry("geometryApproximate", true);
    }

    @Test
    @DisplayName("the '-' sentinel country of asylum is skipped, and a numeric string is read as a number")
    void sentinels() {
        assertThat(UnhcrPopulationAdapter.number(
                tools.jackson.databind.json.JsonMapper.builder().build().readTree("\"1234\"")).doubleValue())
                .isEqualTo(1234.0);
        assertThat(UnhcrPopulationAdapter.number(
                tools.jackson.databind.json.JsonMapper.builder().build().readTree("\"-\""))).isNull();
        List<NormalizedRecord> coa = new UnhcrPopulationAdapter("", new CountryCentroids())
                .parse(AdapterFixtures.bytes("unhcr-population", "population-2024.json"), 2024, "coa", "coa_iso");
        assertThat(coa).allMatch(r -> !r.externalId().contains(":-:"));
    }
}
