package ai.foresignal.source.adapters.space.donki;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link DonkiParser} on real DONKI answers of 2026-10-13, one per type (the 7-day window; HSS and WSA-ENLIL from a
 * 60-day window), including the real empty SEP answer.
 */
class DonkiParserTest {

    static SourceDescriptor descriptor() {
        return new NasaDonkiAdapter("").descriptor();
    }

    static DonkiParser.Parsed parse(String type) {
        return DonkiParser.parse(type, AdapterFixtures.bytes("nasa-donki", type + ".json"));
    }

    @ParameterizedTest(name = "{0}: {1} records, first {2}")
    @CsvSource(delimiter = '|', value = {
        "FLR|7|2026-10-06T05:38:00-FLR-001|2026-10-06T11:21:00Z|2026-10-06T05:38:00Z",
        "CME|25|2026-10-06T00:00:00-CME-001|2026-10-06T13:02:00Z|2026-10-06T00:00:00Z",
        "CMEAnalysis|24|CMEAnalysis/48991|2026-10-06T12:04:00Z|2026-10-06T06:23:00Z",
        "GST|1|2026-10-10T15:00:00-GST-001|2026-10-10T18:03:00Z|2026-10-10T15:00:00Z",
        "IPS|5|2026-10-09T10:41:00-IPS-001|2026-10-09T21:42:00Z|2026-10-09T10:41:00Z",
        "MPC|1|2026-10-10T15:33:00-MPC-001|2026-10-10T15:41:00Z|2026-10-10T15:33:00Z",
        "RBE|1|2026-10-10T14:35:00-RBE-001|2026-10-10T15:05:00Z|2026-10-10T14:35:00Z",
        "HSS|3|2026-08-18T12:40:00-HSS-001|2026-08-19T16:55:00Z|2026-08-18T12:40:00Z",
        "WSAEnlilSimulations|3|WSA-ENLIL/48102/1|2026-08-15T01:02:00Z|2026-08-14T22:24:00Z"})
    @DisplayName("each event type: ids, submission time as observedAt, begin/start/event time as eventTime")
    void eachType(String type, int count, String firstId, String observed, String event) {
        DonkiParser.Parsed p = parse(type);
        assertThat(p.records()).hasSize(count);
        assertThat(p.skipped()).isEmpty();
        NormalizedRecord first = p.records().getFirst();
        assertThat(first.externalId()).isEqualTo(firstId);
        assertThat(first.kind()).isEqualTo(DonkiParser.EVENT_KIND);
        assertThat(first.observedAt()).isEqualTo(Instant.parse(observed));
        assertThat(first.eventTime()).isEqualTo(Instant.parse(event));
        assertThat(first.payload()).containsEntry("type", type).containsEntry("beginTime", event);
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), p.records());
    }

    @Test
    @DisplayName("payloads: flare classes and links, CME most-accurate analysis, GST kpMax, simulation inputs")
    void payloads() {
        NormalizedRecord flare = parse("FLR").records().getFirst();
        assertThat(flare.payload()).containsEntry("classType", "M1.4").containsEntry("sourceLocation", "N10E12")
                .containsEntry("activeRegionNum", 14549L).containsEntry("peakTime", "2026-10-06T06:10:00Z")
                .containsEntry("endTime", "2026-10-06T06:51:00Z").containsEntry("catalog", "M2M_CATALOG")
                .containsEntry("instruments", List.of("GOES-P: EXIS 1.0-8.0"))
                .containsEntry("linkedEvents", List.of("2026-10-06T06:23:00-CME-001")).containsEntry("versionId", 1L)
                .containsEntry("link", "https://ccmc.gsfc.nasa.gov/DONKI/view/FLR/48986/-1");
        assertThat(parse("CME").records().getFirst().payload()).containsEntry("cmeSpeedKms", 325.0)
                .containsEntry("cmeHalfAngle", 17.0).containsEntry("sourceLocation", "S19W88");
        assertThat(parse("CMEAnalysis").records().getFirst().payload()).containsEntry("cmeSpeedKms", 570.0)
                .containsEntry("linkedEvents", List.of("2026-10-06T06:23:00-CME-001"));
        assertThat(parse("GST").records().getFirst().payload()).containsEntry("kpMax", 7.0).containsEntry("linkedEvents",
                List.of("2026-10-10T14:33:00-IPS-001", "2026-10-10T15:33:00-MPC-001"));
        assertThat(parse("WSAEnlilSimulations").records().getFirst().payload()).containsEntry("cmeSpeedKms", 530.0)
                .containsEntry("cmeHalfAngle", 35.0).containsEntry("linkedEvents", List.of("2026-08-14T22:24:00-CME-001"));
    }

    @Test
    @DisplayName("notifications: notice kind, issue time, type and link; the empty \"## \" body is left out")
    void notifications() {
        DonkiParser.Parsed p = parse("notifications");
        assertThat(p.records()).hasSize(21).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo(DonkiParser.NOTICE_KIND);
            assertThat(r.payload()).containsEntry("type", "notifications").doesNotContainKey("messageBody");
        });
        NormalizedRecord first = p.records().getFirst();
        assertThat(first.externalId()).isEqualTo("20261006-AL-001");
        assertThat(first.observedAt()).isEqualTo(Instant.parse("2026-10-06T12:46:00Z"));
        assertThat(first.payload()).containsEntry("messageType", "CME")
                .containsEntry("link", "https://ccmc.gsfc.nasa.gov/DONKI/view/Alert/48995/1");
        assertThat(DonkiParser.body("## Message Type: Space Weather Notification")).startsWith("## Message");
        assertThat(DonkiParser.body("x".repeat(5000))).hasSize(DonkiParser.MAX_BODY);
        FieldSpecContract.assertPayloadKeysDeclared(descriptor(), p.records());
    }

    @Test
    @DisplayName("the empty SEP array is a valid empty result; an API error text or an object is a format error")
    void emptyAndErrors() {
        assertThat(parse("SEP").records()).isEmpty();
        assertThatThrownBy(() -> DonkiParser.parse("FLR", "API Error: Date range cannot exceed 60 days."
                .getBytes(StandardCharsets.UTF_8))).isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> DonkiParser.parse("FLR", "{}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        DonkiParser.Parsed p = DonkiParser.parse("FLR", ("[{\"beginTime\": \"2026-10-06T05:38Z\"},"
                + " {\"flrID\": \"a\"}, {\"flrID\": \"b\", \"submissionTime\": \"2026-10-06T11:21Z\"},"
                + " {\"flrID\": \"b\", \"submissionTime\": \"2026-10-06T11:21Z\"}, 3]").getBytes(StandardCharsets.UTF_8));
        assertThat(p.records()).extracting(NormalizedRecord::externalId).containsExactly("b");
        assertThat(p.skipped()).containsEntry(MappedRecord.MISSING_ID, 2)
                .containsEntry(MappedRecord.MISSING_OBSERVED_AT, 1).containsEntry(MappedRecord.DUPLICATE_ID, 1);
    }

    @Test
    @DisplayName("the descriptor and the request: one URL per type, lookbackDays 1 to 60, types filtered to known ones")
    void descriptorAndRequest() {
        NasaDonkiAdapter adapter = new NasaDonkiAdapter("");
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofMinutes(30));
        assertThat(d.defaultBudget().perDay()).isEqualTo(400);
        assertThat(d.homepage()).doesNotContain("kauai");
        assertThat(adapter.uri("GST", LocalDate.parse("2026-10-06"), LocalDate.parse("2026-10-13")).toString())
                .isEqualTo("https://ccmc.gsfc.nasa.gov/DONKI-API/get/GST?startDate=2026-10-06&endDate=2026-10-13");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("lookbackDays", 61)))).containsKey("lookbackDays");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("lookbackDays", 60)))).isEmpty();
        assertThat(NasaDonkiAdapter.types(ValidatedParams.empty())).containsExactly("FLR", "CME", "GST", "IPS", "SEP");
        assertThat(NasaDonkiAdapter.types(ValidatedParams.of(Map.of("types", List.of("notifications", "BOGUS", "FLR",
                "FLR"))))).containsExactly("notifications", "FLR");
    }

    @Test
    @DisplayName("the same event read again keeps the static hash")
    void snapshot() {
        NormalizedRecord a = parse("FLR").records().getFirst();
        NormalizedRecord b = parse("FLR").records().getFirst();
        SnapshotContract.assertStableStaticHash(descriptor(), List.of(a, b));
    }
}
