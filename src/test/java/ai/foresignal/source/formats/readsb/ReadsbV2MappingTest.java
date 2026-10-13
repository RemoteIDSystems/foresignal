package ai.foresignal.source.formats.readsb;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.Observer;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.time.Instant;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.databind.json.JsonMapper;

/**
 * The readsb v2 specifics a local receiver adds to the shared mapper (prompt 069): every {@code type} mapped
 * to {@code posSource}, the integrity values in {@code quality.extra["adsb.*"]}, {@code uat_version} and
 * {@code band} at 978 MHz, and one observer per record with the receiver's {@code rssi}.
 */
class ReadsbV2MappingTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static Map<String, NormalizedRecord> map(String fixture, MapperOptions options) {
        ReadsbDocument doc = ReadsbDocument.parse(AdapterFixtures.bytes("local-receivers", fixture));
        return ReadsbAircraftMapper.mapAll(doc, options).records().stream()
                .collect(Collectors.toMap(NormalizedRecord::externalId, Function.identity()));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        "adsb_icao, adsb", "adsb_icao_nt, adsb", "adsr_icao, adsr", "tisb_icao, tisb", "adsc, adsc", "mlat, mlat",
        "other, other", "mode_s, modes", "adsb_other, adsb", "adsr_other, adsr", "tisb_other, tisb",
        "tisb_trackfile, tisb", "something_new, other"
    })
    @DisplayName("every readsb v2 type maps to a posSource")
    void typeToPosSource(String type, String posSource) {
        assertThat(ReadsbAircraftMapper.posSource(JSON.readTree("{\"type\":\"" + type + "\"}"))).isEqualTo(posSource);
    }

    @Test
    @DisplayName("ADS-C positions are reported, MLAT and TIS-B calculated")
    void positionSources() {
        assertThat(ReadsbAircraftMapper.positionSource("adsc")).isEqualTo(PositionSource.REPORTED);
        assertThat(ReadsbAircraftMapper.positionSource("mlat")).isEqualTo(PositionSource.CALCULATED);
        assertThat(ReadsbAircraftMapper.positionSource("modes")).isEqualTo(PositionSource.UNKNOWN);
    }

    @Test
    @DisplayName("the 1090 fixture: twelve records (the entry without hex skipped), posSource for every type")
    void fixture1090() {
        Map<String, NormalizedRecord> records = map("readsb-aircraft-1090.json",
                MapperOptions.localReceiver("local-adsb-1", "1090"));
        assertThat(records).hasSize(12);
        assertThat(records.get("a6b7c8").payload()).containsEntry(AircraftFields.POS_SOURCE, "mlat");
        assertThat(records.get("a4b5c6").payload()).containsEntry(AircraftFields.POS_SOURCE, "tisb");
        assertThat(records.get("a5b6c7").payload()).containsEntry(AircraftFields.POS_SOURCE, "adsc");
        assertThat(records.get("~2f0004").payload()).containsEntry(AircraftFields.POS_SOURCE, "tisb");
        assertThat(records.get("a2b3c4").payload()).containsEntry(AircraftFields.ON_GROUND, true)
                .doesNotContainKey(AircraftFields.ALT_BARO);
        assertThat(records.values()).allSatisfy(r -> assertThat(r.payload()).doesNotContainKey(AircraftFields.BAND));
    }

    @Test
    @DisplayName("nac_p and nic stay in the payload; nic_baro, sil, gva and sda go to quality.extra")
    void integrityExtras() {
        NormalizedRecord r = map("readsb-aircraft-1090.json", MapperOptions.localReceiver("local-adsb-1", "1090"))
                .get("a1b2c3");
        assertThat(r.payload()).containsEntry(AircraftFields.NAC_P, 10).containsEntry(AircraftFields.NIC, 8)
                .doesNotContainKeys("sil", "gva", "sda", "nic_baro", "rssi");
        assertThat(r.quality().extra()).containsEntry("adsb.nic_baro", 1L).containsEntry("adsb.sil", 3L)
                .containsEntry("adsb.gva", 2L).containsEntry("adsb.sda", 2L)
                .doesNotContainKey("adsb.uat_version");
    }

    @Test
    @DisplayName("each record has one observer: the receiver, with rssi in dBFS")
    void observer() {
        NormalizedRecord r = map("readsb-aircraft-1090.json", MapperOptions.localReceiver("local-adsb-1", "1090"))
                .get("a1b2c3");
        assertThat(r.observers()).containsExactly(new Observer("local-adsb-1", null, -12.4, null, null));
    }

    @Test
    @DisplayName("978 MHz: band 978 in the payload and uat_version in quality.extra")
    void uat() {
        Map<String, NormalizedRecord> records = map("skyaware978-aircraft.json",
                MapperOptions.localReceiver("local-adsb-2", "978"));
        assertThat(records).hasSize(2);
        NormalizedRecord r = records.get("a9c1d2");
        assertThat(r.payload()).containsEntry(AircraftFields.BAND, "978");
        assertThat(r.quality().extra()).containsEntry("adsb.uat_version", 2);
        assertThat(r.observedAt()).isEqualTo(Instant.ofEpochMilli(1791900010000L - 600));
    }

    @Test
    @DisplayName("the global sources are unchanged: no observer, no band, no integrity extras")
    void globalDefaults() {
        NormalizedRecord r = map("readsb-aircraft-1090.json", MapperOptions.DEFAULTS).get("a1b2c3");
        assertThat(r.observers()).isEmpty();
        assertThat(r.payload()).doesNotContainKey(AircraftFields.BAND);
        assertThat(r.quality().extra()).doesNotContainKeys("adsb.sil", "adsb.nic_baro");
    }
}
