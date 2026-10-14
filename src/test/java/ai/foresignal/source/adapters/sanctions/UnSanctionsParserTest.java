package ai.foresignal.source.adapters.sanctions;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
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
}
