package ai.foresignal.source.formats.readsb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;

/**
 * Prompt 062's shared readsb mapper reads the adsb.fi v2 and v3 envelopes and the airplanes.live envelope unchanged
 * (prompt 080): {@code ac[]}, {@code now} in milliseconds, {@code total}, {@code ctime}, plus the {@code msg} and
 * {@code ptime} it ignores. The existing mapper tests are untouched; this class only adds the new envelopes and
 * {@link ReadsbResponses}.
 */
class ReadsbEnvelopeTest {

    /** {@code now} of the adsb.fi {@code /v2/mil} capture. */
    static final Instant MIL_NOW = Instant.ofEpochMilli(1791931814001L);

    private static ReadsbDocument doc(String dir, String file) {
        return ReadsbDocument.parse(AdapterFixtures.bytes(dir, file));
    }

    private static Map<String, NormalizedRecord> byId(List<NormalizedRecord> records) {
        return records.stream().collect(Collectors.toMap(NormalizedRecord::externalId, Function.identity()));
    }

    @Test
    @DisplayName("adsb.fi /v2/mil: ac[], now in milliseconds, total and ctime")
    void adsbfiMilEnvelope() {
        ReadsbDocument doc = doc("adsbfi-mil", "mil.json");
        assertThat(doc.aircraft()).hasSize(30);
        assertThat(doc.now()).isEqualTo(MIL_NOW);
        assertThat(doc.total()).isEqualTo(30);
        assertThat(doc.ctime()).isEqualTo(MIL_NOW);
    }

    @Test
    @DisplayName("adsb.fi /v2/mil maps every entry: lastPosition, no position, MLAT, ground and seen_pos")
    void adsbfiMilMapping() {
        ReadsbAircraftMapper.MapResult result = ReadsbAircraftMapper.mapAll(doc("adsbfi-mil", "mil.json"),
                MapperOptions.DEFAULTS);
        assertThat(result.skipped()).isZero();
        Map<String, NormalizedRecord> records = byId(result.records());
        assertThat(records).hasSize(30);
        assertThat(result.records()).extracting(r -> r.payload().get(AircraftFields.MILITARY)).containsOnly(true);

        // Only lastPosition: its point, flagged, with its own seen_pos (761 s old); observedAt = now - seen.
        NormalizedRecord last = records.get("ae4ff0");
        Point point = (Point) last.geometry();
        assertThat(point.getX()).isEqualTo(-117.211028);
        assertThat(point.getY()).isEqualTo(32.696347);
        assertThat(last.quality().extra()).containsEntry(ReadsbAircraftMapper.LAST_POSITION_EXTRA, true)
                .containsEntry(ReadsbAircraftMapper.SEEN_POS_EXTRA, 761.286);
        assertThat(last.observedAt()).isEqualTo(MIL_NOW.minusMillis(800));
        assertThat(last.payload()).containsEntry(AircraftFields.ON_GROUND, true)
                .doesNotContainKey(AircraftFields.ALT_BARO);

        // No position at all: no geometry (the TRACK carry-forward keeps the previous one).
        assertThat(records.get("3ea3b0").geometry()).isNull();
        assertThat(records.get("40003e").geometry()).isNull();

        // MLAT: the mlat list names lat, so posSource mlat and a calculated position.
        NormalizedRecord mlat = records.get("ae05ac");
        assertThat(mlat.payload()).containsEntry(AircraftFields.POS_SOURCE, "mlat");
        assertThat(mlat.quality().positionSource()).isEqualTo(PositionSource.CALCULATED);

        // A large seen_pos is kept as the position's age.
        assertThat(records.get("43c93a").quality().extra()).containsEntry(ReadsbAircraftMapper.SEEN_POS_EXTRA, 46.854);

        // Ground, emergency "none" omitted.
        assertThat(records.get("aeeb9d").payload()).containsEntry(AircraftFields.ON_GROUND, true)
                .doesNotContainKey(AircraftFields.EMERGENCY);
        assertThat(records.get("ae57d5").payload()).containsEntry(AircraftFields.FLIGHT, "C2011")
                .containsEntry(AircraftFields.POS_SOURCE, "adsb");
    }

    @Test
    @DisplayName("adsb.fi /v3 region: the same envelope; the query-relative dst and dir never reach the payload")
    void adsbfiRegion() {
        ReadsbDocument doc = doc("adsbfi-region", "region-39.86--104.67-25.json");
        assertThat(doc.now()).isEqualTo(Instant.ofEpochMilli(1791931836001L));
        assertThat(doc.total()).isEqualTo(25);
        Map<String, NormalizedRecord> records = byId(ReadsbAircraftMapper.mapAll(doc, MapperOptions.DEFAULTS).records());
        assertThat(records).hasSize(25);
        assertThat(records.get("a832c2").payload()).containsEntry(AircraftFields.POS_SOURCE, "adsr");
        assertThat(records.get("a832c2").quality().positionSource()).isEqualTo(PositionSource.REPORTED);
        assertThat(records.values()).allSatisfy(r -> assertThat(r.payload()).doesNotContainKeys("dst", "dir",
                "rr_lat", "rr_lon", "messages", "rssi"));
    }

    @Test
    @DisplayName("the airplanes.live envelope (synthetic until access is granted) parses the same way")
    void airplanesLive() {
        ReadsbDocument doc = doc("airplaneslive-mil", "mil-synthetic.json");
        assertThat(doc.now()).isEqualTo(MIL_NOW);
        assertThat(ReadsbAircraftMapper.mapAll(doc, MapperOptions.DEFAULTS).records())
                .extracting(NormalizedRecord::externalId).containsExactly("ae57d5", "ae05ac", "ae4ff0");
    }

    @Test
    @DisplayName("edge cases: a ~ (non-ICAO) TIS-B address, only lastPosition, an upper-case hex with a large seen_pos")
    void edges() {
        Map<String, NormalizedRecord> records = byId(ReadsbAircraftMapper.mapAll(doc("adsbfi-mil", "edge.json"),
                MapperOptions.DEFAULTS).records());
        assertThat(records).containsOnlyKeys("~a1b2c3", "ae0001", "ae0002");
        assertThat(records.get("~a1b2c3").payload()).containsEntry(AircraftFields.POS_SOURCE, "tisb");
        assertThat(records.get("~a1b2c3").quality().positionSource()).isEqualTo(PositionSource.CALCULATED);
        assertThat(records.get("ae0001").quality().extra()).containsEntry(ReadsbAircraftMapper.SEEN_POS_EXTRA, 350.4)
                .containsEntry(ReadsbAircraftMapper.LAST_POSITION_EXTRA, true);
        assertThat(((Point) records.get("ae0001").geometry()).getY()).isEqualTo(38.5);
        assertThat(records.get("ae0002").quality().extra()).containsEntry(ReadsbAircraftMapper.SEEN_POS_EXTRA, 58.9);
    }

    @Test
    @DisplayName("ReadsbResponses refuses a truncated body, an HTML 200 and a JSON type with an HTML body")
    void responseCheck() {
        byte[] json = AdapterFixtures.bytes("adsbfi-mil", "edge.json");
        assertThat(ReadsbResponses.map(response(json, "application/json", false), "adsb.fi").records()).hasSize(3);

        assertThatThrownBy(() -> ReadsbResponses.map(response(json, "application/json", true), "adsb.fi"))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("truncated");
        byte[] html = "<!DOCTYPE html><p>wait</p>".getBytes(StandardCharsets.UTF_8);
        assertThatThrownBy(() -> ReadsbResponses.map(response(html, "text/html", false), "adsb.fi"))
                .isInstanceOf(UpstreamFormatException.class)
                .hasMessage(ReadsbResponses.nonJsonMessage("adsb.fi"));
        assertThatThrownBy(() -> ReadsbResponses.map(response(html, "application/json", false), "airplanes.live"))
                .isInstanceOf(UpstreamFormatException.class).hasMessageContaining("non-JSON 200");
        assertThat(ReadsbResponses.startsWithBrace(" \n{".getBytes(StandardCharsets.UTF_8))).isTrue();
        assertThat(ReadsbResponses.startsWithBrace(new byte[0])).isFalse();
    }

    private static UpstreamResponse response(byte[] body, String contentType, boolean truncated) {
        return new UpstreamResponse(200, Map.of(), body, contentType, truncated, false);
    }
}
