package ai.foresignal.source.adapters.wikidata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.ReferenceSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
                .contains("wdt:P17").contains("p:P2109").contains("psn:P2109").contains("wdt:P5817")
                .contains("wdt:P571");
    }

    @Test
    @DisplayName("the query aggregates one row per item with fixed rules and best-rank normalized capacity")
    void queryAggregates() {
        assertThat(WikidataNuclearParser.QUERY).contains("GROUP BY ?item").contains("ORDER BY ?item")
                .contains("MAX(?capacityW)").contains("MIN(?inceptionDate)").contains("wikibase:BestRank")
                .doesNotContain("SAMPLE(");
    }

    @Test
    @DisplayName("an aggregated row's capacity in watts becomes megawatts, and the location key is read")
    void aggregatedRow() {
        String body = """
                {"results":{"bindings":[
                  {"item":{"type":"uri","value":"http://www.wikidata.org/entity/Q42"},
                   "itemLabel":{"type":"literal","value":"Test Plant"},
                   "location":{"type":"literal","value":"Point(10.5 50.25)"},
                   "capacityWatts":{"type":"literal","value":"1400000000"},
                   "countryLabel":{"type":"literal","value":"Germany"}}
                ]}}
                """;
        List<NormalizedRecord> records = WikidataNuclearParser.parse(body.getBytes(StandardCharsets.UTF_8));
        assertThat(records).hasSize(1);
        assertThat(records.get(0).payload()).containsEntry("capacityMw", 1400.0).containsEntry("country", "Germany");
        assertThat(records.get(0).geometry().getCoordinate().x).isEqualTo(10.5);
        FieldSpecContract.assertPayloadKeysDeclared(descriptor, records);
    }

    @Test
    @DisplayName("a repeated Qid is dropped, keeping the first binding")
    void duplicateQid() {
        String body = """
                {"results":{"bindings":[
                  {"item":{"value":"http://www.wikidata.org/entity/Q7"},"itemLabel":{"value":"First"}},
                  {"item":{"value":"http://www.wikidata.org/entity/Q7"},"itemLabel":{"value":"Second"}},
                  {"itemLabel":{"value":"No item"}}
                ]}}
                """;
        List<NormalizedRecord> out = new ArrayList<>();
        ReferenceSupport.ParseCounts counts =
                WikidataNuclearParser.parse(body.getBytes(StandardCharsets.UTF_8), out::add);
        assertThat(out).extracting(r -> r.payload().get("name")).containsExactly("First");
        assertThat(counts.records()).isEqualTo(1);
        assertThat(counts.skipped()).isEqualTo(2);
    }

    @Test
    @DisplayName("a response without results.bindings is a format change")
    void noBindings() {
        assertThatThrownBy(() -> WikidataNuclearParser.parse("{\"head\":{}}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }
}
