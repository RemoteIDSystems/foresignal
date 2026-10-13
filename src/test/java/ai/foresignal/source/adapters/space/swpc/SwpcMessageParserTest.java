package ai.foresignal.source.adapters.space.swpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link SwpcMessageParser} on the real {@code alerts.json} of 2026-10-13 (85 messages, with a real cancel) and on
 * hand-made messages: codes, serials, categories, validity windows, the cancel tombstone and reused serials.
 */
class SwpcMessageParserTest {

    static SourceDescriptor descriptor() {
        return new SwpcAlertsAdapter("").descriptor();
    }

    static NormalizedRecord byId(List<NormalizedRecord> records, String id) {
        return records.stream().filter(r -> r.externalId().equals(id) && !r.tombstone()).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("the descriptor validates: two kinds, 5 min, 2/min and 600/day, TTL 7 d on receipt, event retention")
    void descriptorShape() {
        SourceDescriptor d = descriptor();
        assertThat(DescriptorValidator.problems(d, new SwpcAlertsAdapter(""))).isEmpty();
        assertThat(d.kinds()).containsExactlyInAnyOrder("space-weather-alert", "space-weather-scales");
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(5));
        assertThat(d.defaultBudget().perMinute()).isEqualTo(2);
        assertThat(d.defaultBudget().perDay()).isEqualTo(600);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofDays(7));
        assertThat(d.ttlAnchor()).isEqualTo(ai.foresignal.source.spi.TtlAnchor.RECEIVED);
        assertThat(d.embedFields()).containsExactly("message");
        assertThat(d.kindRetention()).containsEntry("space-weather-alert", Duration.ofDays(1825));
        assertThat(d.licence()).isEqualTo("US-PD");
    }

    @Test
    @DisplayName("the fixture: 82 records, categories from the code, one cancel tombstone, reused serials counted")
    void fixture() {
        SwpcMessageParser.Messages m = SwpcMessageParser.parse(AdapterFixtures.bytes("swpc-alerts", "alerts.json"));
        assertThat(m.records()).hasSize(82);
        assertThat(m.skipped()).containsEntry(MappedRecord.DUPLICATE_ID, 3).containsEntry("canceled", 1);
        assertThat(m.canceled()).containsExactly("WATA30:285");
        assertThat(m.records()).filteredOn(NormalizedRecord::tombstone).extracting(NormalizedRecord::externalId)
                .containsExactly("WATA30:285");
        NormalizedRecord tomb = m.records().stream().filter(NormalizedRecord::tombstone).findFirst().orElseThrow();
        assertThat(tomb.observedAt()).isEqualTo(Instant.parse("2026-10-11T17:02:59.390Z"));
        assertThat(m.records()).filteredOn(r -> !r.tombstone()).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo(SwpcMessageParser.KIND);
            assertThat(r.externalId()).isEqualTo(r.payload().get("code") + ":" + r.payload().get("serial"));
            assertThat(r.geometry()).isNull();
            assertThat((String) r.payload().get("message")).doesNotContain("\r").hasSizeLessThanOrEqualTo(4096);
        });

        NormalizedRecord warning = byId(m.records(), "WARK04:5434");
        assertThat(warning.payload()).containsEntry("category", "warning").containsEntry("kIndex", 4)
                .doesNotContainKey("noaaScale");
        assertThat(warning.observedAt()).isEqualTo(Instant.parse("2026-10-13T14:31:37.343Z"));
        assertThat(warning.validFrom()).isEqualTo(Instant.parse("2026-10-13T01:15:00Z"));
        assertThat(warning.validUntil()).as("Now Valid Until").isEqualTo(Instant.parse("2026-10-13T21:00:00Z"));

        NormalizedRecord alert = byId(m.records(), "ALTK05:2057");
        assertThat(alert.payload()).containsEntry("category", "alert").containsEntry("noaaScale", "G1")
                .containsEntry("kIndex", 5).containsEntry("serial", 2057);
        assertThat(alert.eventTime()).as("Threshold Reached").isEqualTo(Instant.parse("2026-10-10T21:45:00Z"));

        NormalizedRecord summary = byId(m.records(), "SUMXM5:328");
        assertThat(summary.payload()).containsEntry("category", "summary").containsEntry("noaaScale", "R2");
        assertThat(summary.eventTime()).as("Begin Time").isEqualTo(Instant.parse("2026-10-10T14:58:00Z"));

        assertThat(byId(m.records(), "WARK07:153").validUntil()).isEqualTo(Instant.parse("2026-10-10T23:59:00Z"));
        assertThat(byId(m.records(), "WATA30:286").payload()).containsEntry("category", "cancel");
        assertThat(byId(m.records(), "ALTEF3:3752").observedAt()).as("the newest of the reused serial")
                .isEqualTo(Instant.parse("2026-10-08T05:01:00.663Z"));
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), m.records());
    }

    @Test
    @DisplayName("headers: code prefixes, scale codes, message times, a cancel line, an inverted window")
    void headers() {
        assertThat(SwpcMessageParser.category("ALTK05")).isEqualTo("alert");
        assertThat(SwpcMessageParser.category("WARK04")).isEqualTo("warning");
        assertThat(SwpcMessageParser.category("WATA20")).isEqualTo("watch");
        assertThat(SwpcMessageParser.category("SUMX01")).isEqualTo("summary");
        assertThat(SwpcMessageParser.category("CANX01")).isEqualTo("cancel");
        assertThat(SwpcMessageParser.category("XYZ")).isEqualTo("other");
        assertThat(SwpcMessageParser.category(null)).isEqualTo("other");
        assertThat(SwpcMessageParser.scale("G3 - Strong")).isEqualTo("G3");
        assertThat(SwpcMessageParser.scale("none")).isNull();
        assertThat(SwpcMessageParser.time("2026 Oct 13 1431 UTC")).isEqualTo(Instant.parse("2026-10-13T14:31:00Z"));
        assertThat(SwpcMessageParser.time("2026 Oct  3 0901 UTC")).isEqualTo(Instant.parse("2026-10-03T09:01:00Z"));
        assertThat(SwpcMessageParser.time("soon")).isNull();

        SwpcMessageParser.Header h = SwpcMessageParser.header("Space Weather Message Code: WARK05\r\nSerial Number: 7\r\n"
                + "Valid From: 2026 Oct 13 1200 UTC\r\nValid To: 2026 Oct 13 1000 UTC\r\n");
        assertThat(h.externalId()).isEqualTo("WARK05:7");
        NormalizedRecord r = SwpcMessageParser.record(h, Instant.parse("2026-10-13T09:00:00Z"));
        assertThat(r.validFrom()).as("an inverted window keeps only its end").isNull();
        assertThat(r.validUntil()).isEqualTo(Instant.parse("2026-10-13T10:00:00Z"));

        SwpcMessageParser.Header cancel = SwpcMessageParser.header("Space Weather Message Code: CANK04\n"
                + "Serial Number: 12\nCANCEL WARNING: Geomagnetic K-index of 4 expected\nCancel Serial Number: 11\n");
        assertThat(cancel.cancel()).isTrue();
        assertThat(cancel.cancelSerial()).isEqualTo(11);
        assertThat(SwpcMessageParser.header(null).externalId()).isNull();
    }

    @Test
    @DisplayName("a cancel tombstones its target in the same file; messages without a code or a time are skipped")
    void cancelAndSkips() {
        String body = """
                [{"product_id": "K04W", "issue_datetime": "2026-10-13 12:00:00.000",
                  "message": "Space Weather Message Code: WARK04\\r\\nSerial Number: 11\\r\\nIssue Time: 2026 Oct 13 1200 UTC"},
                 {"product_id": "K04W", "issue_datetime": "2026-10-13 13:00:00.000",
                  "message": "Space Weather Message Code: WARK04\\r\\nSerial Number: 12\\r\\nCANCEL WARNING: K-index of 4\\nCancel Serial Number: 11"},
                 {"product_id": "X", "issue_datetime": "2026-10-13 13:00:00.000", "message": "no header"},
                 {"product_id": "Y", "message": "Space Weather Message Code: ALTK04\\nSerial Number: 1"}]""";
        SwpcMessageParser.Messages m = SwpcMessageParser.parse(body.getBytes(StandardCharsets.UTF_8));
        assertThat(m.records()).extracting(NormalizedRecord::externalId, NormalizedRecord::tombstone)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("WARK04:12", false),
                        org.assertj.core.groups.Tuple.tuple("WARK04:11", true));
        assertThat(m.skipped()).containsEntry(MappedRecord.MISSING_ID, 1)
                .containsEntry(MappedRecord.MISSING_OBSERVED_AT, 1).containsEntry("canceled", 1);
        assertThat(m.records().get(0).payload()).containsEntry("category", "cancel").containsEntry("kIndex", 4);

        assertThatThrownBy(() -> SwpcMessageParser.parse("{}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> SwpcMessageParser.parse("<html>".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("the same message received again later keeps the static hash")
    void snapshot() {
        SwpcMessageParser.Header h = SwpcMessageParser.header("Space Weather Message Code: ALTK05\nSerial Number: 2057\n"
                + "Threshold Reached: 2026 Oct 10 2145 UTC\nNOAA Scale: G1 - Minor\n");
        Instant issued = Instant.parse("2026-10-10T21:45:58Z");
        SnapshotContract.assertStableStaticHash(descriptor(), List.of(SwpcMessageParser.record(h, issued),
                SwpcMessageParser.record(h, issued)));
    }
}
