package ai.foresignal.source.adapters.internet;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.MultiPoint;

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
        assertThat(record.payload()).containsEntry("leak_count", 4);
    }

    @Test
    @DisplayName("the documented shape: numeric ids, AS names from asn_info, hijacker and victim geometry, volatile counts")
    void documentedShape() {
        String body = "{\"result\":{\"asn_info\":[{\"asn\":64500,\"country_code\":\"US\",\"org_name\":\"HIJACK-NET\"},"
                + "{\"asn\":64501,\"country_code\":\"DE\",\"org_name\":\"VICTIM-NET\"}],\"events\":[{\"id\":42,"
                + "\"confidence_score\":9,\"duration\":1800,\"event_type\":1,\"hijack_msgs_count\":3,"
                + "\"hijacker_asn\":64500,\"hijacker_country\":\"US\",\"is_stale\":false,"
                + "\"max_hijack_ts\":\"2026-10-13T10:30:00Z\",\"max_msg_ts\":\"2026-10-13T10:35:00Z\","
                + "\"min_hijack_ts\":\"2026-10-13T10:00:00Z\",\"on_going_count\":1,\"peer_ip_count\":5,"
                + "\"prefixes\":[\"203.0.113.0/24\"],\"tags\":[{\"name\":\"rpki\",\"score\":2}],"
                + "\"victim_asns\":[64501],\"victim_countries\":[\"DE\",\"FR\"]}]},\"result_info\":{\"total_count\":1}}";
        NormalizedRecord record = ADAPTER.parse(body.getBytes(StandardCharsets.UTF_8), true).get(0);
        assertThat(record.externalId()).isEqualTo("hijack:42");
        assertThat(record.observedAt()).isEqualTo(Instant.parse("2026-10-13T10:35:00Z"));
        assertThat(record.payload()).containsEntry("hijacker_asn_name", "HIJACK-NET")
                .containsEntry("victim_asn_names", List.of("VICTIM-NET")).containsEntry("duration", 1800)
                .containsEntry("hijack_msgs_count", 3).containsEntry("on_going_count", 1)
                .containsEntry("peer_ip_count", 5).containsEntry("geometryApproximate", true);
        assertThat(record.geometry()).isInstanceOf(MultiPoint.class);
        assertThat(record.geometry().getNumGeometries()).as("US, DE and FR").isEqualTo(3);

        String leak = "{\"result\":{\"asn_info\":[{\"asn\":64502,\"org_name\":\"LEAKY\"}],\"events\":[{\"id\":7,"
                + "\"countries\":[\"FR\",\"BE\"],\"finished\":false,\"leak_asn\":64502,\"leak_count\":9,"
                + "\"leak_seg\":[64510,64502,64520],\"leak_type\":1,\"max_ts\":\"2026-10-13T11:10:00Z\","
                + "\"min_ts\":\"2026-10-13T11:00:00Z\",\"origin_count\":1,\"peer_count\":2,\"prefix_count\":3}]}}";
        NormalizedRecord leakRecord = ADAPTER.parse(leak.getBytes(StandardCharsets.UTF_8), false).get(0);
        assertThat(leakRecord.payload()).containsEntry("leak_asn_name", "LEAKY")
                .containsEntry("leak_seg", List.of(64510, 64502, 64520));
        assertThat(leakRecord.geometry().getNumGeometries()).as("every leak country").isEqualTo(2);
    }

    @Test
    @DisplayName("a response with no max_msg_ts at all falls back to max_hijack_ts for every event")
    void observedAtFallback() {
        String body = "{\"result\":{\"events\":[{\"id\":1,\"hijacker_asn\":64500,"
                + "\"max_hijack_ts\":\"2026-10-13T10:30:00Z\",\"min_hijack_ts\":\"2026-10-13T10:00:00Z\"}]}}";
        NormalizedRecord record = ADAPTER.parse(body.getBytes(StandardCharsets.UTF_8), true).get(0);
        assertThat(record.observedAt()).isEqualTo(Instant.parse("2026-10-13T10:30:00Z"));
    }

    @Test
    @DisplayName("the local involved filter matches hijacker or victim ASNs and countries")
    void involves() {
        NormalizedRecord record = ADAPTER.parse(AdapterFixtures.bytes("cloudflare-radar-bgp", "events.json"), true)
                .get(0);
        assertThat(CloudflareBgpAdapter.involves(record, List.of(64501, 1), List.of())).isTrue();
        assertThat(CloudflareBgpAdapter.involves(record, List.of(1, 2), List.of())).isFalse();
        assertThat(CloudflareBgpAdapter.involves(record, List.of(), List.of("DE", "JP"))).isTrue();
        assertThat(CloudflareBgpAdapter.involves(record, List.of(), List.of("JP", "KR"))).isFalse();
    }
}
