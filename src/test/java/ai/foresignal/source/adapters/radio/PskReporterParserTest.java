package ai.foresignal.source.adapters.radio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

/**
 * {@link PskReporterParser} (prompt 082) on the real 2026-10-14 captures (a busy call sign's reports with
 * {@code rronly=1}, and a query without it that carries {@code activeReceiver} elements) and the hand-made edge and
 * XXE documents.
 */
class PskReporterParserTest {

    private static PskReporterParser.Result parse(String name, int cap) {
        return PskReporterParser.parse(AdapterFixtures.bytes("pskreporter-ft8", name), cap);
    }

    @Test
    @DisplayName("the busy call sign's capture: 50 spots, the receiver as observer, no receiver call sign in the payload")
    void realFixture() {
        PskReporterParser.Result result = parse("callsign-N6AD.xml", 2000);

        assertThat(result.records()).hasSize(50);
        assertThat(result.reports()).isEqualTo(50);
        assertThat(result.activeReceivers()).isZero();
        NormalizedRecord first = result.records().getFirst();
        assertThat(first.externalId()).isEqualTo("K3UA|N6AD|14075|1791937846");
        assertThat(first.observedAt()).isEqualTo(Instant.ofEpochSecond(1791937846L)).isEqualTo(first.eventTime());
        assertThat(first.payload()).containsEntry("sender", "K3UA").containsEntry("senderLocator", "EN90vm82jf")
                .containsEntry("mode", "FT8").containsEntry("frequencyHz", 14075125L).containsEntry("band", "20m")
                .containsEntry("snr", -14).containsEntry("dxcc", "United States")
                .containsEntry("receiverLocator", "CM88qg60fk");
        assertThat(first.observers()).containsExactly(new Observer("N6AD", 14075125L, null, -14.0, null));
        assertThat(((Point) first.geometry()).getX()).isEqualTo(-80.179167);
        assertThat(first.quality().accuracyMeters()).isEqualTo(422.0);
        for (NormalizedRecord r : result.records()) {
            String receiver = r.observers().getFirst().receiverId();
            assertThat(r.payload().values()).as(r.externalId()).doesNotContain(receiver);
            assertThat(r.payload()).doesNotContainKeys("receiver", "receiverCallsign");
        }
        FieldSpecContract.assertPayloadKeysDeclared(RadioTestAdapters.psk().descriptor(),
                result.records());
    }

    @Test
    @DisplayName("activeReceiver elements are counted for coverage")
    void activeReceivers() {
        PskReporterParser.Result result = parse("active-receivers.xml", 2000);

        assertThat(result.activeReceivers()).isEqualTo(25);
        assertThat(result.records()).hasSize(49);
    }

    @Test
    @DisplayName("records are capped at the limit")
    void cap() {
        PskReporterParser.Result result = parse("callsign-N6AD.xml", 10);

        assertThat(result.records()).hasSize(10);
        assertThat(result.capped()).isTrue();
        assertThat(result.reports()).isEqualTo(50);
    }

    @Test
    @DisplayName("edge cases: repeated id, missing time, bad number skipped; no or invalid locator means no geometry")
    void edge() {
        PskReporterParser.Result result = parse("edge.xml", 2000);

        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly(
                "N0CALL-2|N0CALL|14074|1791937439", "N0CALL-3|N0CALL|7074|1791937440", "N0CALL-4|N0CALL|7074|1791937441");
        assertThat(result.skipped()).containsEntry(PskReporterParser.DUPLICATE, 1)
                .containsEntry(PskReporterParser.MISSING_FIELDS, 1).containsEntry(PskReporterParser.BAD_NUMBER, 1);
        assertThat(result.records().get(1).geometry()).isNull();
        assertThat(result.records().get(2).geometry()).isNull();
        assertThat(result.records().get(2).quality().accuracyMeters()).isNull();
        assertThat(result.records().get(1).payload()).containsEntry("band", "40m").doesNotContainKey("snr");
        assertThat(result.activeReceivers()).isEqualTo(2);
    }

    @Test
    @DisplayName("a document with a DOCTYPE and an external entity throws UpstreamFormatException, nothing is resolved")
    void xxe() {
        assertThatThrownBy(() -> parse("doctype-xxe.xml", 10)).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("DOCTYPE");
    }

    @Test
    @DisplayName("a document that is not receptionReports, or not XML, throws")
    void formatErrors() {
        assertThatThrownBy(() -> PskReporterParser.parse("<html/>".getBytes(StandardCharsets.UTF_8), 10))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> PskReporterParser.parse("not xml".getBytes(StandardCharsets.UTF_8), 10))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> PskReporterParser.parse(new byte[0], 10)).isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("bands: the common amateur bands, nothing outside them")
    void bands() {
        assertThat(PskReporterParser.band(14_074_000)).isEqualTo("20m");
        assertThat(PskReporterParser.band(7_074_000)).isEqualTo("40m");
        assertThat(PskReporterParser.band(50_313_000)).isEqualTo("6m");
        assertThat(PskReporterParser.band(474_200)).isEqualTo("630m");
        assertThat(PskReporterParser.band(9_000_000)).isNull();
    }

    @Test
    @DisplayName("the same spot fetched twice hashes equal")
    void stableHash() {
        List<NormalizedRecord> a = parse("callsign-N6AD.xml", 1).records();
        List<NormalizedRecord> b = parse("callsign-N6AD.xml", 1).records();

        SnapshotContract.assertStableStaticHash(RadioTestAdapters.psk().descriptor(),
                List.of(a.getFirst(), b.getFirst()));
    }
}
