package ai.foresignal.source.adapters.sanctions;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** UN consolidated list parsing (prompt 091): individuals and entities, documents, reference ids. */
class UnSanctionsParserTest {

    private static List<NormalizedRecord> parse() throws Exception {
        return new UnSanctionsAdapter(null)
                .parse(Xml.temporary(AdapterFixtures.bytes("un-sanctions", "consolidated.xml")));
    }

    @Test
    @DisplayName("individuals and entities map to UN sanction records")
    void entries() throws Exception {
        List<NormalizedRecord> records = parse();
        assertThat(records).isNotEmpty();
        assertThat(records).allMatch(r -> r.externalId().startsWith("un:"));
        assertThat(records).allMatch(r -> r.observedAt() != null);
        assertThat(records).extracting(r -> r.payload().get("partyType")).contains("individual", "entity");
        NormalizedRecord first = records.get(0);
        assertThat(first.payload()).containsEntry("list", "UN");
        assertThat(first.payload()).containsKey("unReference");
    }

    @Test
    @DisplayName("LISTED_ON (a bare date) is the eventTime and listedOn; lastUpdated is the latest VALUE")
    void datesAndUpdates() throws Exception {
        List<NormalizedRecord> records = parse();
        NormalizedRecord first = byId(records, "un:CDi.001");
        assertThat(first.eventTime()).isEqualTo(Instant.parse("2012-12-31T00:00:00Z"));
        assertThat(first.payload()).containsEntry("listedOn", "2012-12-31").containsEntry("dateOfBirth", "1971")
                .containsEntry("addresses", List.of(Map.of("country", "Rwanda")));
        assertThat(byId(records, "un:CDi.030").payload()).containsEntry("lastUpdated", "2023-03-01");
    }

    @Test
    @DisplayName("documents become identifiers and entity addresses keep city and country")
    void documentsAndAddresses() throws Exception {
        List<NormalizedRecord> records = parse();
        assertThat(byId(records, "un:CDi.019").payload().get("identifiers")).isEqualTo(List.of(
                Map.of("type", "National Identification Number", "value", "1-78-09-44621-80")));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> addresses =
                (List<Map<String, Object>>) byId(records, "un:CDe.002").payload().get("addresses");
        assertThat(addresses).contains(Map.of("city", "Butembo", "country", "Democratic Republic of the Congo"));
    }

    private static NormalizedRecord byId(List<NormalizedRecord> records, String externalId) {
        return records.stream().filter(r -> r.externalId().equals(externalId)).findFirst()
                .orElseThrow(() -> new AssertionError("no " + externalId));
    }
}
