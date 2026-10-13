package ai.foresignal.source.formats.readsb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.adapters.adsblol.AdsbLolMilAdapter;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Point;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The shared readsb mapper (prompt 062): the Core-Data-Model §2 time base with {@code now} in seconds and in
 * milliseconds, geometry and {@code lastPosition}, unit conversions, {@code posSource}, {@code navDegraded}
 * boundaries, {@code dbFlags}, the allowlist, and the snapshot contract.
 */
class ReadsbAircraftMapperTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static JsonNode node(String json) {
        return JSON.readTree(json);
    }

    private static NormalizedRecord map(String entry, Instant now) {
        return ReadsbAircraftMapper.map(node(entry), now, MapperOptions.DEFAULTS).orElseThrow();
    }

    private static ReadsbDocument edge() {
        return ReadsbDocument.parse(AdapterFixtures.bytes("readsb", "readsb-edge.json"));
    }

    @Nested
    @DisplayName("documents and the time base")
    class TimeBase {

        @Test
        @DisplayName("now in seconds: observedAt = now - seen")
        void seconds() {
            ReadsbDocument doc = ReadsbDocument.parse(
                    "{\"now\":1791894243.5,\"aircraft\":[{\"hex\":\"abc123\",\"seen\":2.25}]}".getBytes(StandardCharsets.UTF_8));
            assertThat(doc.now()).isEqualTo(Instant.ofEpochMilli(1791894243500L));
            NormalizedRecord r = ReadsbAircraftMapper.mapAll(doc, MapperOptions.DEFAULTS).records().getFirst();
            assertThat(r.observedAt()).isEqualTo(Instant.ofEpochMilli(1791894241250L));
            assertThat(r.eventTime()).isNull();
        }

        @Test
        @DisplayName("now in milliseconds (adsb.lol, readsb-edge): observedAt = now - seen")
        void milliseconds() {
            ReadsbDocument doc = edge();
            assertThat(doc.now()).isEqualTo(Instant.ofEpochMilli(1791894243001L));
            NormalizedRecord ground = ReadsbAircraftMapper.mapAll(doc, MapperOptions.DEFAULTS).records().getFirst();
            assertThat(ground.observedAt()).isEqualTo(Instant.ofEpochMilli(1791894243001L - 200));
        }

        @Test
        @DisplayName("the adsb.lol capture sends ac, total and ctime in milliseconds")
        void adsbLolTopLevel() {
            ReadsbDocument doc = ReadsbDocument.parse(AdapterFixtures.bytes("adsblol-mil", "mil.json"));
            assertThat(doc.aircraft()).hasSize(30);
            assertThat(doc.total()).isEqualTo(30);
            assertThat(doc.now()).isEqualTo(Instant.ofEpochMilli(1791894193500L));
            assertThat(doc.ctime()).isEqualTo(Instant.ofEpochMilli(1791894193500L));
        }

        @Test
        @DisplayName("seen absent means observedAt = now; seen_pos goes to quality.extra")
        void seenPos() {
            Instant now = Instant.parse("2026-10-13T12:00:00Z");
            NormalizedRecord r = map("{\"hex\":\"abc123\",\"lat\":1,\"lon\":2,\"seen_pos\":3.5}", now);
            assertThat(r.observedAt()).isEqualTo(now);
            assertThat(r.quality().extra()).containsEntry(ReadsbAircraftMapper.SEEN_POS_EXTRA, 3.5);
        }

        @Test
        @DisplayName("a document without the array or a numeric now is a format change")
        void malformed() {
            assertThatThrownBy(() -> ReadsbDocument.parse("{\"now\":1}".getBytes(StandardCharsets.UTF_8)))
                    .isInstanceOf(UpstreamFormatException.class);
            assertThatThrownBy(() -> ReadsbDocument.parse("{\"ac\":[]}".getBytes(StandardCharsets.UTF_8)))
                    .isInstanceOf(UpstreamFormatException.class);
            assertThatThrownBy(() -> ReadsbDocument.parse("<html>".getBytes(StandardCharsets.UTF_8)))
                    .isInstanceOf(UpstreamFormatException.class);
            assertThatThrownBy(() -> ReadsbDocument.parse(new byte[0])).isInstanceOf(UpstreamFormatException.class);
        }
    }

    @Nested
    @DisplayName("identity and geometry")
    class Geometry {

        @Test
        @DisplayName("an entry without hex is skipped and counted")
        void missingHex() {
            ReadsbAircraftMapper.MapResult result = ReadsbAircraftMapper.mapAll(edge(), MapperOptions.DEFAULTS);
            assertThat(result.skipped()).isEqualTo(1);
            assertThat(result.records()).hasSize(6);
            assertThat(result.records()).extracting(NormalizedRecord::externalId).doesNotContain("nohex1");
        }

        @Test
        @DisplayName("hex is lower-cased, ~ kept, kind aircraft")
        void hex() {
            List<NormalizedRecord> records = ReadsbAircraftMapper.mapAll(edge(), MapperOptions.DEFAULTS).records();
            assertThat(records.get(0).externalId()).isEqualTo("a1b2c3");
            assertThat(records.get(0).payload()).containsEntry("hex", "a1b2c3");
            assertThat(records.get(1).externalId()).isEqualTo("~2ab098");
            assertThat(records).allSatisfy(r -> assertThat(r.kind()).isEqualTo("aircraft"));
        }

        @Test
        @DisplayName("lat/lon is a 2D point in SRID 4326")
        void point() {
            NormalizedRecord r = ReadsbAircraftMapper.mapAll(edge(), MapperOptions.DEFAULTS).records().getFirst();
            Point p = (Point) r.geometry();
            assertThat(p.getX()).isEqualTo(-87.904722);
            assertThat(p.getY()).isEqualTo(41.978611);
            assertThat(p.getSRID()).isEqualTo(4326);
            assertThat(Double.isNaN(p.getCoordinate().getZ())).isTrue();
            assertThat(r.quality().extra()).doesNotContainKey(ReadsbAircraftMapper.LAST_POSITION_EXTRA);
        }

        @Test
        @DisplayName("lastPosition is the fallback, flagged in quality.extra with its seen_pos")
        void lastPosition() {
            NormalizedRecord r = ReadsbAircraftMapper.mapAll(edge(), MapperOptions.DEFAULTS).records().get(3);
            assertThat(r.externalId()).isEqualTo("4b7fcc");
            Point p = (Point) r.geometry();
            assertThat(p.getX()).isEqualTo(11.645753);
            assertThat(p.getY()).isEqualTo(48.593681);
            assertThat(r.quality().extra())
                    .containsEntry(ReadsbAircraftMapper.LAST_POSITION_EXTRA, true)
                    .containsEntry(ReadsbAircraftMapper.SEEN_POS_EXTRA, 2620.732);
        }

        @Test
        @DisplayName("without lat/lon and lastPosition the geometry is null; the fallback can be turned off")
        void noPosition() {
            List<NormalizedRecord> records = ReadsbAircraftMapper.mapAll(edge(), MapperOptions.DEFAULTS).records();
            assertThat(records.get(4).geometry()).isNull();
            NormalizedRecord noFallback = ReadsbAircraftMapper.mapAll(edge(), new MapperOptions("aircraft", false))
                    .records().get(3);
            assertThat(noFallback.geometry()).isNull();
        }

        @Test
        @DisplayName("on the ground: onGround true, alt_baro omitted")
        void ground() {
            NormalizedRecord r = ReadsbAircraftMapper.mapAll(edge(), MapperOptions.DEFAULTS).records().getFirst();
            assertThat(r.payload()).containsEntry("onGround", true).doesNotContainKey("alt_baro");
            assertThat(r.payload()).containsEntry("alt_geom", 198);
        }
    }

    @Nested
    @DisplayName("units")
    class Units {

        @Test
        @DisplayName("feet to meters (rounded to 1 m), knots and degrees to 0.1, ft/min to m/s")
        void conversions() {
            NormalizedRecord r = map("{\"hex\":\"abc123\",\"alt_baro\":35000,\"alt_geom\":36125,\"gs\":451.26,"
                    + "\"track\":359.96,\"baro_rate\":-1088}", Instant.EPOCH.plusSeconds(1_800_000_000L));
            assertThat(r.payload())
                    .containsEntry("alt_baro", 10668)
                    .containsEntry("alt_geom", 11011)
                    .containsEntry("gs", 451.3)
                    .containsEntry("track", 360.0)
                    .containsEntry("baro_rate", -5.5)
                    .containsEntry("onGround", false);
        }

        @Test
        @DisplayName("the conversion helpers")
        void helpers() {
            assertThat(AircraftFields.feetToMeters(1000)).isEqualTo(305);
            assertThat(AircraftFields.mpsToKnots(56.62)).isEqualTo(110.1);
            assertThat(AircraftFields.fpmToMps(640)).isEqualTo(3.3);
            assertThat(AircraftFields.tenths(-0.04)).isEqualTo(0.0);
            assertThat(AircraftFields.wholeMeters(876.3)).isEqualTo(876);
        }
    }

    @Nested
    @DisplayName("posSource and quality")
    class PosSource {

        @Test
        @DisplayName("mlat: the mlat list names lat; quality CALCULATED")
        void mlat() {
            NormalizedRecord r = ReadsbAircraftMapper.mapAll(edge(), MapperOptions.DEFAULTS).records().get(2);
            assertThat(r.payload()).containsEntry("posSource", "mlat");
            assertThat(r.quality().positionSource()).isEqualTo(PositionSource.CALCULATED);
        }

        @Test
        @DisplayName("tisb: the tisb list names lat; quality CALCULATED")
        void tisb() {
            NormalizedRecord r = ReadsbAircraftMapper.mapAll(edge(), MapperOptions.DEFAULTS).records().get(1);
            assertThat(r.payload()).containsEntry("posSource", "tisb");
            assertThat(r.quality().positionSource()).isEqualTo(PositionSource.CALCULATED);
        }

        @ParameterizedTest
        @CsvSource({
            "adsb_icao,adsb,REPORTED", "adsb_icao_nt,adsb,REPORTED", "adsb_other,adsb,REPORTED",
            "adsr_icao,adsr,REPORTED", "mlat,mlat,CALCULATED", "tisb_trackfile,tisb,CALCULATED",
            "mode_s,modes,UNKNOWN", "adsc,other,UNKNOWN", "unknown,other,UNKNOWN"
        })
        @DisplayName("posSource from type")
        void fromType(String type, String expected, PositionSource quality) {
            NormalizedRecord r = map("{\"hex\":\"abc123\",\"type\":\"" + type + "\"}", Instant.EPOCH.plusSeconds(1));
            assertThat(r.payload()).containsEntry("posSource", expected);
            assertThat(r.quality().positionSource()).isEqualTo(quality);
        }

        @Test
        @DisplayName("no type and no lists: no posSource, quality UNKNOWN")
        void none() {
            NormalizedRecord r = map("{\"hex\":\"abc123\"}", Instant.EPOCH.plusSeconds(1));
            assertThat(r.payload()).doesNotContainKey("posSource");
            assertThat(r.quality().positionSource()).isEqualTo(PositionSource.UNKNOWN);
        }
    }

    @Nested
    @DisplayName("navDegraded")
    class NavDegraded {

        @ParameterizedTest
        @CsvSource({"7,7,true", "8,7,false", "8,6,true", "11,11,false", "0,8,true"})
        @DisplayName("nac_p 7/8 and nic 6/7 boundaries")
        void boundaries(int nacP, int nic, boolean degraded) {
            NormalizedRecord r = map("{\"hex\":\"abc123\",\"nac_p\":" + nacP + ",\"nic\":" + nic + "}",
                    Instant.EPOCH.plusSeconds(1));
            assertThat(r.payload()).containsEntry("navDegraded", degraded)
                    .containsEntry("nac_p", nacP).containsEntry("nic", nic);
        }

        @Test
        @DisplayName("only one present decides alone; neither present means no key")
        void partial() {
            assertThat(map("{\"hex\":\"a\",\"nic\":6}", Instant.EPOCH.plusSeconds(1)).payload())
                    .containsEntry("navDegraded", true);
            assertThat(map("{\"hex\":\"a\",\"nac_p\":9}", Instant.EPOCH.plusSeconds(1)).payload())
                    .containsEntry("navDegraded", false);
            assertThat(map("{\"hex\":\"a\"}", Instant.EPOCH.plusSeconds(1)).payload()).doesNotContainKey("navDegraded");
        }
    }

    @Nested
    @DisplayName("payload allowlist")
    class Payload {

        @Test
        @DisplayName("dbFlags bits 1, 4 and 8 become mil, pia and ladd, only when set")
        void dbFlags() {
            List<NormalizedRecord> records = ReadsbAircraftMapper.mapAll(edge(), MapperOptions.DEFAULTS).records();
            assertThat(records.get(2).payload()).containsEntry("ladd", true).doesNotContainKeys("mil", "pia");
            assertThat(records.get(3).payload()).containsEntry("mil", true).containsEntry("pia", true)
                    .doesNotContainKey("ladd");
            assertThat(records.get(4).payload()).doesNotContainKeys("mil", "pia", "ladd");
            assertThat(records.get(5).payload()).containsEntry("ladd", true).containsEntry("navDegraded", true);
        }

        @Test
        @DisplayName("strings trimmed, emergency none omitted, other emergencies kept")
        void strings() {
            List<NormalizedRecord> records = ReadsbAircraftMapper.mapAll(edge(), MapperOptions.DEFAULTS).records();
            Map<String, Object> ground = records.getFirst().payload();
            assertThat(ground).containsEntry("flight", "UAL123").containsEntry("r", "N12345")
                    .containsEntry("t", "B738").containsEntry("desc", "BOEING 737-800")
                    .containsEntry("squawk", "1200").containsEntry("category", "A3")
                    .doesNotContainKey("emergency");
            assertThat(records.get(2).payload()).containsEntry("emergency", "general").containsEntry("squawk", "7700");
        }

        @Test
        @DisplayName("raw counters and query-relative fields are excluded, and nothing is null")
        void excluded() {
            for (NormalizedRecord r : ReadsbAircraftMapper.mapAll(edge(), MapperOptions.DEFAULTS).records()) {
                assertThat(r.payload()).doesNotContainKeys("messages", "rssi", "seen", "seen_pos", "rc", "version",
                        "sil", "gva", "sda", "nav_qnh", "type", "mlat", "tisb", "lastPosition", "lat", "lon",
                        "dbFlags", "geom_rate");
                assertThat(r.payload().values()).doesNotContainNull();
                assertThat(r.payload().keySet()).isSubsetOf(AircraftDescriptorsKeys.ALL);
            }
        }

        @Test
        @DisplayName("every fixture's payload keys are declared with the right types (fieldSpecs contract)")
        void fieldSpecs() {
            var descriptor = new AdsbLolMilAdapter("").descriptor();
            FieldSpecContract.assertPayloadKeysDeclared(descriptor,
                    ReadsbAircraftMapper.mapAll(edge(), MapperOptions.DEFAULTS).records());
            FieldSpecContract.assertPayloadKeysDeclared(descriptor, ReadsbAircraftMapper.mapAll(
                    ReadsbDocument.parse(AdapterFixtures.bytes("adsblol-mil", "mil.json")), MapperOptions.DEFAULTS)
                    .records());
            FieldSpecContract.assertPayloadKeysDeclared(descriptor, ReadsbAircraftMapper.mapAll(
                    ReadsbDocument.parse(AdapterFixtures.bytes("adsblol-area", "point-41.98--87.90-250.json")),
                    MapperOptions.DEFAULTS).records());
        }
    }

    @Test
    @DisplayName("two polls that differ only in kinematics keep one static hash (adapter rule 7)")
    void stableStaticHash() {
        Instant first = Instant.parse("2026-10-13T12:00:00Z");
        NormalizedRecord a = map("{\"hex\":\"abc123\",\"flight\":\"UAL1  \",\"r\":\"N1\",\"t\":\"B738\","
                + "\"squawk\":\"1200\",\"category\":\"A3\",\"alt_baro\":35000,\"gs\":450.0,\"track\":90.0,"
                + "\"nac_p\":10,\"nic\":8,\"lat\":41.0,\"lon\":-87.0,\"seen\":0.5}", first);
        NormalizedRecord b = map("{\"hex\":\"abc123\",\"flight\":\"UAL1\",\"r\":\"N1\",\"t\":\"B738\","
                + "\"squawk\":\"1200\",\"category\":\"A3\",\"alt_baro\":35100,\"gs\":455.0,\"track\":91.0,"
                + "\"nac_p\":10,\"nic\":8,\"lat\":41.05,\"lon\":-86.9,\"seen\":0.2,\"messages\":99}",
                first.plusSeconds(30));
        SnapshotContract.assertStableStaticHash(new AdsbLolMilAdapter("").descriptor(), List.of(a, b));
    }

    @Test
    @DisplayName("a null or non-object entry, or no now, maps to nothing")
    void nothing() {
        assertThat(ReadsbAircraftMapper.map(null, Instant.EPOCH, null)).isEqualTo(Optional.empty());
        assertThat(ReadsbAircraftMapper.map(node("[1]"), Instant.EPOCH, null)).isEmpty();
        assertThat(ReadsbAircraftMapper.map(node("{\"hex\":\"a\"}"), null, null)).isEmpty();
        assertThat(ReadsbAircraftMapper.map(node("{\"hex\":\"  \"}"), Instant.EPOCH, null)).isEmpty();
    }

    /** Every key {@link AircraftFields} defines. */
    static final class AircraftDescriptorsKeys {
        static final java.util.Set<String> ALL = java.util.Set.of(
                AircraftFields.HEX, AircraftFields.FLIGHT, AircraftFields.REGISTRATION, AircraftFields.TYPE,
                AircraftFields.DESCRIPTION, AircraftFields.SQUAWK, AircraftFields.EMERGENCY, AircraftFields.CATEGORY,
                AircraftFields.ON_GROUND, AircraftFields.ALT_BARO, AircraftFields.ALT_GEOM,
                AircraftFields.GROUND_SPEED, AircraftFields.TRACK, AircraftFields.BARO_RATE, AircraftFields.POS_SOURCE,
                AircraftFields.NAC_P, AircraftFields.NIC, AircraftFields.NAV_DEGRADED, AircraftFields.MILITARY,
                AircraftFields.PIA, AircraftFields.LADD);
    }
}
