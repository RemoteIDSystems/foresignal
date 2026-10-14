package ai.foresignal.source.adapters.sanctions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** OFAC SDN parsing (prompt 091): identifiers, entity keys, caps and the XXE guard. */
class OfacSdnParserTest {

    private static List<NormalizedRecord> parse(String fixture) throws IOException, XMLStreamException {
        Path file = Xml.temporary(AdapterFixtures.bytes("ofac-sdn", fixture));
        return new OfacSdnAdapter(null).parse(file, OfacSdnAdapter.SDN);
    }

    @Test
    @DisplayName("entries map to sanction records with a publication observedAt")
    void entries() throws Exception {
        List<NormalizedRecord> records = parse("sdn.xml");
        assertThat(records).isNotEmpty();
        assertThat(records).extracting(NormalizedRecord::kind).containsOnly("sanction");
        assertThat(records).allMatch(r -> r.observedAt() != null, "observedAt from Publish_Date");
        assertThat(records).allMatch(r -> r.externalId().startsWith("SDN:"));
        NormalizedRecord first = records.get(0);
        assertThat(first.payload()).containsEntry("list", "SDN");
        assertThat((List<?>) first.payload().get("aliases")).hasSizeLessThanOrEqualTo(Sanctions.MAX_ALIASES);
    }

    @Test
    @DisplayName("at least one vessel carries a validated IMO, and an aircraft its tail")
    void entityKeys() throws Exception {
        List<NormalizedRecord> records = parse("sdn.xml");
        assertThat(records).anyMatch(r -> r.payload().get("imo") != null);
        assertThat(records).anyMatch(r -> "aircraft".equals(r.payload().get("partyType")));
    }

    @Test
    @DisplayName("a DOCTYPE/XXE document is refused")
    void xxe() {
        Path file;
        try {
            file = Xml.temporary(AdapterFixtures.bytes("ofac-sdn", "xxe.xml"));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertThatThrownBy(() -> new OfacSdnAdapter(null).parse(file, OfacSdnAdapter.SDN))
                .isInstanceOf(XMLStreamException.class);
    }

    @Test
    @DisplayName("the IMO check digit and the nine-digit MMSI are validated")
    void validators() {
        assertThat(Sanctions.isImo("IMO 9074729")).isTrue();
        assertThat(Sanctions.isImo("9074728")).isFalse();
        assertThat(Sanctions.isMmsi("123456789")).isTrue();
        assertThat(Sanctions.isMmsi("12345")).isFalse();
    }
}
