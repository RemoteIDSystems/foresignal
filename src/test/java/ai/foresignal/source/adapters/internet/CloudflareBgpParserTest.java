package ai.foresignal.source.adapters.internet;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code cloudflare-radar-bgp} parsing (prompt 092): hijack and leak mapping, AS names, volatile fields. */
class CloudflareBgpParserTest {

    private static final CloudflareBgpAdapter ADAPTER = new CloudflareBgpAdapter(null, new CountryCentroids());

    @Test
    @DisplayName("a hijack maps with confidence, victim ASNs and the volatile fields declared")
    void hijack() {
        List<NormalizedRecord> records = ADAPTER.parse(
                AdapterFixtures.bytes("cloudflare-radar-bgp", "events.json"), true);
        assertThat(records).hasSize(1);
        NormalizedRecord record = records.get(0);
        assertThat(record.externalId()).isEqualTo("hijack:HJ1");
        assertThat(record.kind()).isEqualTo("bgp-hijack");
        assertThat(record.payload()).containsEntry("confidence_score", 9.0).containsEntry("hijacker_asn", 64500);
        assertThat(record.payload().get("victim_asns")).isEqualTo(List.of(64501));
        assertThat(record.payload().get("prefixes")).isEqualTo(List.of("203.0.113.0/24"));
        assertThat(record.geometry()).isNotNull();
        assertThat(ADAPTER.descriptor().volatileFields()).contains("max_msg_ts", "on_going_count");
    }

    @Test
    @DisplayName("a leak maps with its ASN, countries and finished flag")
    void leak() {
        List<NormalizedRecord> records = ADAPTER.parse(
                AdapterFixtures.bytes("cloudflare-radar-bgp", "events.json"), false);
        assertThat(records).hasSize(1);
        NormalizedRecord record = records.get(0);
        assertThat(record.externalId()).isEqualTo("leak:LK1");
        assertThat(record.kind()).isEqualTo("bgp-leak");
        assertThat(record.payload()).containsEntry("leak_asn", 64502).containsEntry("finished", true);
        assertThat(record.payload().get("countries")).isEqualTo(List.of("FR"));
    }
}
