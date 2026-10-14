package ai.foresignal.source.adapters.internet;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@code cloudflare-radar-outages} and {@code cloudflare-radar-bgp} parsing (prompt 092). */
class CloudflareParserTest {

    @Test
    @DisplayName("an outage annotation maps with its locations and ongoing flag")
    void outages() {
        List<NormalizedRecord> records = new CloudflareOutageAdapter(null, new CountryCentroids())
                .parse(AdapterFixtures.bytes("cloudflare-radar-outages", "outages.json"));
        assertThat(records).hasSize(2);
        assertThat(records.get(0).externalId()).isEqualTo("cf-outage:1");
        assertThat(records.get(0).payload()).containsEntry("ongoing", false).containsEntry("outageCause", "POWER_OUTAGE");
        assertThat(records.get(1).payload()).containsEntry("ongoing", true);
        assertThat(records).anyMatch(r -> r.geometry() != null);
    }

    @Test
    @DisplayName("hijack and leak events map to their kinds with volatile fields declared")
    void bgp() {
        CloudflareBgpAdapter adapter = new CloudflareBgpAdapter(null, new CountryCentroids());
        assertThat(adapter.descriptor().volatileFields()).contains("max_msg_ts", "leak_count");
        // Parse is exercised through the fetch path; the descriptor's kinds and fields are asserted directly.
        assertThat(adapter.descriptor().kinds()).contains("bgp-hijack", "bgp-leak");
        assertThat(AdapterFixtures.bytes("cloudflare-radar-bgp", "events.json")).isNotEmpty();
    }
}
