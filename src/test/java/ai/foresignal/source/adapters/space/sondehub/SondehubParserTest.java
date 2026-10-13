package ai.foresignal.source.adapters.space.sondehub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.formats.sonde.RadiosondeRecords;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link SondehubParser} on 11 real sondes of 2026-10-13 (uploader identities and positions replaced) and hand-made
 * edge cases: the uploader position is never stored, sentinel sensor values are dropped, a sonde without a position
 * has no geometry.
 */
class SondehubParserTest {

    static NormalizedRecord byId(List<NormalizedRecord> records, String id) {
        return records.stream().filter(r -> r.externalId().equals(id)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("the fixture: 11 sondes with points, the shared payload, the uploader as observer and nothing else")
    void fixture() {
        SondehubParser.Parsed p = SondehubParser.parse(AdapterFixtures.bytes("sondehub", "sondes.json"));
        assertThat(p.records()).hasSize(11);
        assertThat(p.skipped()).isEmpty();
        assertThat(p.records()).allSatisfy(r -> {
            assertThat(r.kind()).isEqualTo("radiosonde");
            assertThat(r.geometry()).isNotNull();
            assertThat(r.payload().keySet()).isSubsetOf(RadiosondeRecords.PAYLOAD_FIELDS);
            assertThat(r.payload().toString()).doesNotContain("51.0000,10.0000").doesNotContain("redacted antenna");
            assertThat(r.observers()).hasSize(1);
            assertThat(r.observers().getFirst().receiverId()).startsWith("UPLOADER-");
        });
        NormalizedRecord rs41 = byId(p.records(), "N3850104");
        assertThat(rs41.observedAt()).isEqualTo(Instant.parse("2026-10-13T20:49:31.001Z"));
        assertThat(rs41.geometry().getCoordinate().x).isEqualTo(14.12191);
        assertThat(rs41.geometry().getCoordinate().y).isEqualTo(52.20925);
        assertThat(Double.isNaN(rs41.geometry().getCoordinate().getZ())).as("altitude is not geometry Z").isTrue();
        assertThat(rs41.payload()).containsEntry("altitudeM", 171.1).containsEntry("type", "RS41")
                .containsEntry("subtype", "RS41-SGP").containsEntry("manufacturer", "Vaisala")
                .containsEntry("verticalSpeedMs", -0.3).containsEntry("horizontalSpeedMs", 0.1)
                .containsEntry("headingDeg", 357.0).containsEntry("tempC", 11.7).containsEntry("humidityPct", 68.8)
                .containsEntry("pressureHpa", 1004.76).containsEntry("frequencyMHz", 404.8)
                .doesNotContainKeys("frame", "batt", "serial", "uploader_position", "uploader_antenna");
        assertThat(rs41.observers().getFirst()).isEqualTo(new Observer("UPLOADER-5", null, null, 6.9, null));
        assertThat(byId(p.records(), "X3712180").observers().getFirst().rssi()).isEqualTo(-122.5);
        FieldSpecContract.assertPayloadKeysDeclared(new SondehubAdapter(() -> null, "").descriptor(), p.records());
    }

    @Test
    @DisplayName("edge cases: no position means no geometry; sentinels dropped; no datetime or no serial skipped")
    void edges() {
        SondehubParser.Parsed p = SondehubParser.parse(AdapterFixtures.bytes("sondehub", "sondes-edge.json"));
        assertThat(p.records()).hasSize(1);
        assertThat(p.skipped()).containsEntry(MappedRecord.MISSING_ID, 1)
                .containsEntry(MappedRecord.MISSING_OBSERVED_AT, 1);
        NormalizedRecord r = p.records().getFirst();
        assertThat(r.externalId()).isEqualTo("T0000001");
        assertThat(r.geometry()).isNull();
        assertThat(r.payload()).doesNotContainKeys("tempC", "humidityPct", "pressureHpa")
                .containsEntry("headingDeg", 1.0).containsEntry("altitudeM", 1200.5);
        assertThat(r.payload().toString()).doesNotContain("51.0000");
        assertThatThrownBy(() -> SondehubParser.parse("[]".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> SondehubParser.parse("nope".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThat(SondehubParser.parse("{}".getBytes(StandardCharsets.UTF_8)).records()).isEmpty();
    }

    @Test
    @DisplayName("params and the request: center lat,lon or the first place, radius in meters, the window")
    void request() {
        SondehubAdapter adapter = new SondehubAdapter(() -> null, "");
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
        assertThat(adapter.descriptor().defaultInterval()).isEqualTo(Duration.ofMinutes(5));
        assertThat(adapter.descriptor().minInterval()).isEqualTo(Duration.ofMinutes(2));
        assertThat(adapter.descriptor().licence()).contains("CC BY-SA 2.0");
        assertThat(adapter.descriptor().description()).contains("uncalibrated");
        assertThat(adapter.uri(52.2, 13.62, 500, 600).toString())
                .isEqualTo("https://api.v2.sondehub.org/sondes?lat=52.2&lon=13.62&distance=500000&last=600");
        assertThat(SondehubAdapter.center("52.2, 13.6")).containsExactly(52.2, 13.6);
        assertThat(SondehubAdapter.center("95,0")).isNull();
        assertThat(SondehubAdapter.center("52.2")).isNull();
        assertThat(SondehubAdapter.center("a,b")).isNull();
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("center", "x", "radiusKm", 2000, "windowS", 10))))
                .containsOnlyKeys("center", "radiusKm", "windowS");
        assertThat(adapter.checkParams(ValidatedParams.of(Map.of("center", "52.2,13.6", "radiusKm", 1500)))).isEmpty();
        assertThat(SondehubAdapter.coordinate(-0.00001)).isEqualTo("0");
    }

    @Test
    @DisplayName("the same telemetry read twice keeps the static hash")
    void snapshot() {
        byte[] body = AdapterFixtures.bytes("sondehub", "sondes.json");
        NormalizedRecord a = byId(SondehubParser.parse(body).records(), "N3850104");
        NormalizedRecord b = byId(SondehubParser.parse(body).records(), "N3850104");
        SnapshotContract.assertStableStaticHash(new SondehubAdapter(() -> null, "").descriptor(), List.of(a, b));
    }
}
