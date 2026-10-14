package ai.foresignal.source.adapters.humanitarian;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code who-don} parsing (prompt 090): the title split, multi-country MultiPoint, override title. */
class WhoDonParserTest {

    private static final WhoDonAdapter ADAPTER = new WhoDonAdapter("", new CountryCentroids());

    @Test
    @DisplayName("the title splits into disease and country, resolved through the gazetteer")
    void split() {
        List<NormalizedRecord> records = ADAPTER.parse(AdapterFixtures.bytes("who-don", "diseaseoutbreaknews.json"));
        assertThat(records).isNotEmpty();
        NormalizedRecord first = records.get(0);
        assertThat(first.kind()).isEqualTo("disease-outbreak");
        assertThat(first.eventTime()).isNotNull();
        assertThat(first.observedAt()).isNotNull();
        assertThat(first.payload()).containsKey("disease").containsKey("summary");
        assertThat((String) first.payload().get("disease"))
                .isEqualTo("Pneumonia of undetermined etiology");
        assertThat(first.payload().get("countries")).isEqualTo(List.of("RU"));
    }

    @Test
    @DisplayName("a two-country title is a MultiPoint; an override title wins")
    void multiCountry() {
        List<NormalizedRecord> records = ADAPTER.parse(AdapterFixtures.bytes("who-don", "multi-country.json"));
        NormalizedRecord two = records.get(0);
        assertThat(two.payload().get("countries")).isEqualTo(List.of("CD", "UG"));
        assertThat(two.geometry().getGeometryType()).isEqualTo("MultiPoint");
        NormalizedRecord override = records.get(1);
        assertThat(override.payload()).containsEntry("title", "Cholera – Haiti").containsEntry("disease", "Cholera");
    }
}
