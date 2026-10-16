package ai.foresignal.source.adapters.sanctions;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** UK Sanctions List parsing (prompt 091): name types and the UN cross-reference. */
class UkSanctionsParserTest {

    private static List<NormalizedRecord> parse() throws Exception {
        return new UkSanctionsAdapter(null)
                .parse(Xml.temporary(AdapterFixtures.bytes("uk-sanctions-list", "uk-sanctions-list.xml")));
    }

    @Test
    @DisplayName("entries map to UK sanction records with the primary name and aliases")
    void entries() throws Exception {
        List<NormalizedRecord> records = parse();
        assertThat(records).isNotEmpty();
        assertThat(records).allMatch(r -> r.externalId().startsWith("uk:"));
        assertThat(records).allMatch(r -> r.observedAt() != null);
        NormalizedRecord first = records.get(0);
        assertThat(first.payload()).containsEntry("list", "UK").containsEntry("name",
                "HAJI KHAIRULLAH HAJI SATTAR MONEY EXCHANGE");
        assertThat((List<?>) first.payload().get("aliases")).isNotEmpty();
    }

    @Test
    @DisplayName("a UN reference becomes the unReference entity key")
    void unReference() throws Exception {
        List<NormalizedRecord> records = parse();
        assertThat(records).anyMatch(r -> r.payload().get("unReference") != null);
    }

    @Test
    @DisplayName("a ship (real DPR0075): IMO key and identifier, flag, aliases, sanctions imposed")
    @SuppressWarnings("unchecked")
    void ship() throws Exception {
        NormalizedRecord ship = byId(parse(), "uk:DPR0075");
        assertThat(ship.payload()).containsEntry("partyType", "vessel").containsEntry("name", "Petrel 8")
                .containsEntry("imo", "9562233").containsEntry("vesselFlag", "Comoros")
                .containsEntry("listedOn", "2017-10-03").containsEntry("lastUpdated", "18/12/2021");
        assertThat((List<Object>) ship.payload().get("aliases")).contains("Jal Vahini", "Tian Tai 36");
        assertThat(ship.payload().get("identifiers")).isEqualTo(
                List.of(Map.of("type", "IMO", "value", "IMO9562233")));
        assertThat(ship.payload().get("sanctionsImposed")).isEqualTo(List.of("Prohibition of port entry"));
        assertThat(ship.payload().get("designationSource")).isEqualTo(List.of("UN"));
    }

    @Test
    @DisplayName("the entity with a UN reference carries crossRefs, addresses as city and country, and listedOn")
    void entityDetails() throws Exception {
        NormalizedRecord entity = byId(parse(), "uk:AFG0001");
        assertThat(entity.payload()).containsEntry("unReference", "TAe.010")
                .containsEntry("crossRefs", List.of("TAe.010")).containsEntry("listedOn", "2012-06-29");
        assertThat(entity.eventTime()).isEqualTo(java.time.Instant.parse("2012-06-29T00:00:00Z"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> addresses = (List<Map<String, Object>>) entity.payload().get("addresses");
        assertThat(addresses).isNotEmpty().hasSizeLessThanOrEqualTo(Sanctions.MAX_ADDRESSES)
                .allSatisfy(a -> assertThat(a.keySet()).isSubsetOf("city", "country"));
    }

    @Test
    @DisplayName("an individual carries a date of birth as published and passport identifiers")
    void individualDetails() throws Exception {
        List<NormalizedRecord> records = parse();
        assertThat(records).anyMatch(r -> "individual".equals(r.payload().get("partyType"))
                && r.payload().get("dateOfBirth") != null);
        assertThat(records).anyMatch(r -> r.payload().get("identifiers") instanceof List<?> ids
                && ids.stream().anyMatch(id -> ((Map<?, ?>) id).get("type").equals("Passport")));
    }

    private static NormalizedRecord byId(List<NormalizedRecord> records, String externalId) {
        return records.stream().filter(r -> r.externalId().equals(externalId)).findFirst()
                .orElseThrow(() -> new AssertionError("no " + externalId));
    }
}
