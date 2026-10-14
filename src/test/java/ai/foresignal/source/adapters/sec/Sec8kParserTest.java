package ai.foresignal.source.adapters.sec;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** SEC 8-K parsing (prompt 091): the accession id, item codes, 8-K/A and the ISO-8859-1 declaration. */
class Sec8kParserTest {

    private static final Sec8kAdapter ADAPTER = new Sec8kAdapter(null, null);

    @Test
    @DisplayName("the current feed maps entries to filing records with item codes")
    void current() {
        List<NormalizedRecord> records = ADAPTER.parse(AdapterFixtures.bytes("sec-8k", "current-8k.xml"));
        assertThat(records).isNotEmpty();
        assertThat(records).extracting(NormalizedRecord::kind).containsOnly("filing");
        NormalizedRecord first = records.get(0);
        assertThat(first.externalId()).matches("[\\d-]+");
        assertThat(first.payload()).containsKey("company").containsKey("items").containsKey("url");
        assertThat(first.eventTime()).isNotNull();
        assertThat(first.observedAt()).isNotNull();
    }

    @Test
    @DisplayName("an ISO-8859-1 8-K/A with three items keeps all of them")
    void eightKa() {
        List<NormalizedRecord> records = ADAPTER.parse(AdapterFixtures.bytes("sec-8k", "8k-a.xml"));
        assertThat(records).hasSize(1);
        NormalizedRecord record = records.get(0);
        assertThat(record.externalId()).isEqualTo("0001234567-26-000001");
        assertThat(record.payload()).containsEntry("form", "8-K/A")
                .containsEntry("company", "EXAMPLE CORP")
                .containsEntry("cik", "0001234567");
        assertThat(record.payload().get("items")).isEqualTo(List.of("1.05", "2.02", "9.01"));
        assertThat(record.payload()).containsEntry("sizeKb", 1024);
    }
}
