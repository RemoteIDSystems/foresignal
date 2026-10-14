package ai.foresignal.source.adapters.wikidata;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link WikidataNuclearParser} contract (prompt 094). */
class WikidataNuclearParserTest {

    private final SourceDescriptor descriptor = new NuclearPlantsWikidataAdapter("").descriptor();

    @Test
    @DisplayName("parses bindings, extracts the Qid and the WKT point, and omits missing optionals")
    void parses() {
        List<NormalizedRecord> records = WikidataNuclearParser.parse(
                AdapterFixtures.bytes(NuclearPlantsWikidataAdapter.ID, "sparql.json"));
        assertThat(records).extracting(NormalizedRecord::externalId)
                .containsExactly("wikidata:Q2351124", "wikidata:Q1000000");
        FieldSpecContract.assertPayloadKeysDeclared(descriptor, records);

        NormalizedRecord cattenom = records.get(0);
        assertThat(cattenom.payload()).containsEntry("name", "Cattenom Nuclear Power Plant")
                .containsEntry("country", "France")
                .containsEntry("capacityMw", 5448.0)
                .containsEntry("stateOfUse", "in use")
                .containsEntry("inception", "1979-01-01T00:00:00Z");
        assertThat(cattenom.geometry().getCoordinate().x).isCloseTo(6.2178,
                org.assertj.core.data.Offset.offset(1e-4));
        assertThat(cattenom.geometry().getCoordinate().y).isCloseTo(49.4158,
                org.assertj.core.data.Offset.offset(1e-4));

        NormalizedRecord fukushima = records.get(1);
        assertThat(fukushima.payload()).doesNotContainKey("capacityMw");
        assertThat(fukushima.payload()).doesNotContainKey("inception");
    }

    @Test
    @DisplayName("the query pins the property ids and no operator input changes it")
    void queryPinned() {
        assertThat(WikidataNuclearParser.QUERY).contains("wd:Q134447").contains("wdt:P625")
                .contains("wdt:P17").contains("wdt:P2109").contains("wdt:P5817").contains("wdt:P571");
    }
}
