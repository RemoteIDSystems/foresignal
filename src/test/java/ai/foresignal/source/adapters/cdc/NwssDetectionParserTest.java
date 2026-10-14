package ai.foresignal.source.adapters.cdc;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.CountyGeometries;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link NwssDetectionParser} against a trimmed detections response (prompt 095). */
class NwssDetectionParserTest {

    private static final byte[] FIXTURE = AdapterFixtures.bytes("cdc-nwss-detections", "detections.json");

    private static List<NormalizedRecord> records(boolean includeWastewaterScan) {
        return NwssDetectionParser.parse(FIXTURE, new NwssDetectionParser.Options(includeWastewaterScan))
                .detections().stream().map(d -> NwssDetectionParser.toRecord(d, null)).toList();
    }

    @Test
    @DisplayName("the latest sample per site and target wins, and WastewaterSCAN rows are dropped by default")
    void foldsLatest() {
        NwssDetectionParser.Result result = NwssDetectionParser.parse(FIXTURE,
                new NwssDetectionParser.Options(false));
        assertThat(result.rows()).isEqualTo(4);
        assertThat(result.detections()).extracting(NwssDetectionParser.Detection::key)
                .containsExactlyInAnyOrder("CA-1:h5", "TX-1:measles");
        assertThat(result.wastewaterScanSkipped()).isEqualTo(1);
        FieldSpecContract.assertPayloadKeysDeclared(
                new NwssDetectionAdapter("", CountyGeometries.none()).descriptor(), records(false));
    }

    @Test
    @DisplayName("the newer CA-1 sample is a detection and the string 'No' reads as false")
    void detectionFlip() {
        List<NormalizedRecord> records = records(false);
        NormalizedRecord ca = records.stream().filter(r -> r.externalId().equals("CA-1:h5")).findFirst()
                .orElseThrow();
        assertThat(ca.payload().get("detected")).isEqualTo(true);
        assertThat(ca.eventTime()).isEqualTo(Instant.parse("2026-09-08T00:00:00Z"));
        NormalizedRecord tx = records.stream().filter(r -> r.externalId().equals("TX-1:measles")).findFirst()
                .orElseThrow();
        assertThat(tx.payload().get("detected")).isEqualTo(false);
    }

    @Test
    @DisplayName("an included WastewaterSCAN detection carries the CC BY-NC override")
    void wastewaterScanOverride() {
        List<NormalizedRecord> all = records(true);
        assertThat(all).hasSize(3);
        NormalizedRecord scan = all.stream().filter(r -> r.externalId().equals("WA-1:mpox")).findFirst()
                .orElseThrow();
        assertThat(scan.payload().get("licenceOverride")).isEqualTo(NwssWvalParser.WASTEWATER_SCAN_LICENCE);
    }
}
