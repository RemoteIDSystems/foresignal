package ai.foresignal.source.adapters.sanctions;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
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
}
