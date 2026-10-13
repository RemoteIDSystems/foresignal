package ai.foresignal.source.adapters.opensky;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PositionSource;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Point;

/**
 * {@link OpenSkyStatesParser} (prompt 062): the captured fixture, every index of the positional array, the
 * category mapping, {@code states: null} and malformed documents.
 */
class OpenSkyStatesParserTest {

    private static final String ID = OpenSkyStatesAdapter.ID;

    private static List<NormalizedRecord> edge() {
        return OpenSkyStatesParser.parse(AdapterFixtures.bytes(ID, "states-edge.json"));
    }

    private static NormalizedRecord byId(List<NormalizedRecord> records, String hex) {
        return records.stream().filter(r -> r.externalId().equals(hex)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("the captured bbox fixture parses to 30 aircraft, with the response time")
    void fixtureCount() {
        OpenSkyStatesParser.ParseResult result =
                OpenSkyStatesParser.parseWithDiagnostics(AdapterFixtures.bytes(ID, "states-bbox.json"));
        assertThat(result.records()).hasSize(30);
        assertThat(result.states()).isEqualTo(30);
        assertThat(result.skipped()).isZero();
        assertThat(result.time()).isEqualTo(Instant.ofEpochSecond(1791894198L));
        NormalizedRecord first = result.records().getFirst();
        assertThat(first.externalId()).isEqualTo("4b406a");
        assertThat(first.observedAt()).isEqualTo(Instant.ofEpochSecond(1791894197L));
        assertThat(first.payload()).containsEntry("flight", "HBYKG").containsEntry("originCountry", "Switzerland")
                .containsEntry("alt_baro", 876).containsEntry("squawk", "7000").containsEntry("posSource", "adsb")
                .doesNotContainKey("category");
        NormalizedRecord ground = byId(result.records(), "502db2");
        assertThat(ground.payload()).containsEntry("onGround", true).containsEntry("gs", 0.0)
                .doesNotContainKey("alt_baro");
    }

    @Test
    @DisplayName("every index: icao24, callsign, country, times, lon/lat, altitudes, ground, speed, track, rate, squawk, source")
    void everyIndex() {
        NormalizedRecord r = byId(edge(), "4b406a");
        assertThat(r.kind()).isEqualTo("aircraft");
        assertThat(r.eventTime()).isNull();
        assertThat(r.observedAt()).isEqualTo(Instant.ofEpochSecond(1791894298L));
        assertThat(r.quality().extra())
                .containsEntry(OpenSkyStatesParser.TIME_POSITION_EXTRA, Instant.ofEpochSecond(1791894297L).toString());
        Point p = (Point) r.geometry();
        assertThat(p.getX()).isEqualTo(7.0843);
        assertThat(p.getY()).isEqualTo(46.7357);
        assertThat(r.payload()).isEqualTo(Map.ofEntries(
                Map.entry("hex", "4b406a"),
                Map.entry("flight", "HBYKG"),
                Map.entry("originCountry", "Switzerland"),
                Map.entry("onGround", false),
                Map.entry("alt_baro", 876),
                Map.entry("alt_geom", 905),
                Map.entry("gs", 110.1),
                Map.entry("track", 109.1),
                Map.entry("baro_rate", -1.3),
                Map.entry("squawk", "7000"),
                Map.entry("posSource", "adsb"),
                Map.entry("category", "A1")));
        assertThat(r.quality().positionSource()).isEqualTo(PositionSource.REPORTED);
    }

    @Test
    @DisplayName("rows without lat/lon have null geometry; time_position null leaves no extra")
    void noPosition() {
        NormalizedRecord r = byId(edge(), "3c6444");
        assertThat(r.geometry()).isNull();
        assertThat(r.quality().extra()).doesNotContainKey(OpenSkyStatesParser.TIME_POSITION_EXTRA);
        assertThat(r.payload()).containsEntry("category", "B1").doesNotContainKeys("alt_baro", "gs", "track");
    }

    @Test
    @DisplayName("on the ground: alt_baro omitted, knots from m/s; position sources asterix, mlat and flarm")
    void groundAndSources() {
        List<NormalizedRecord> records = edge();
        NormalizedRecord ground = byId(records, "a1b2c3");
        assertThat(ground.payload()).containsEntry("onGround", true).containsEntry("gs", 8.2)
                .containsEntry("posSource", "asterix").doesNotContainKeys("alt_baro", "category");
        assertThat(ground.quality().positionSource()).isEqualTo(PositionSource.UNKNOWN);
        NormalizedRecord mlat = byId(records, "e48d4c");
        assertThat(mlat.payload()).containsEntry("posSource", "mlat").doesNotContainKeys("flight", "squawk");
        assertThat(mlat.quality().positionSource()).isEqualTo(PositionSource.CALCULATED);
        NormalizedRecord flarm = byId(records, "4ca7b3");
        assertThat(flarm.payload()).containsEntry("posSource", "flarm").containsEntry("baro_rate", 0.0);
        assertThat(flarm.quality().positionSource()).isEqualTo(PositionSource.REPORTED);
    }

    @Test
    @DisplayName("category integers map to ADS-B codes; 0, 1 and 13 are omitted")
    void categories() {
        List<NormalizedRecord> records = edge();
        assertThat(byId(records, "4b406a").payload()).containsEntry("category", "A1");
        assertThat(byId(records, "3c4b28").payload()).containsEntry("category", "A7");
        assertThat(byId(records, "3c6444").payload()).containsEntry("category", "B1");
        assertThat(byId(records, "3c4b29").payload()).containsEntry("category", "B2");
        assertThat(byId(records, "e48d4c").payload()).containsEntry("category", "B6");
        assertThat(byId(records, "3c4b2a").payload()).containsEntry("category", "C1");
        assertThat(byId(records, "4ca7b3").payload()).containsEntry("category", "C5");
        assertThat(byId(records, "3c4b26").payload()).doesNotContainKeys("category", "flight");
        assertThat(byId(records, "3c4b27").payload()).doesNotContainKey("category");
        assertThat(byId(records, "a1b2c3").payload()).doesNotContainKey("category");
    }

    @Test
    @DisplayName("states: null is zero aircraft and valid")
    void statesNull() {
        OpenSkyStatesParser.ParseResult result =
                OpenSkyStatesParser.parseWithDiagnostics(AdapterFixtures.bytes(ID, "states-null.json"));
        assertThat(result.records()).isEmpty();
        assertThat(result.time()).isEqualTo(Instant.ofEpochSecond(1791894198L));
    }

    @ParameterizedTest
    @ValueSource(strings = {"[]", "{\"states\":[]}", "{\"time\":1}", "{\"time\":1,\"states\":{}}",
            "{\"time\":1,\"states\":[{\"a\":1}]}", "<html></html>", ""})
    @DisplayName("anything other than {time, states:[…]|null} is a format change")
    void malformed(String body) {
        assertThatThrownBy(() -> OpenSkyStatesParser.parse(body.getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("a state without icao24 or last_contact is skipped and counted")
    void skipped() {
        OpenSkyStatesParser.ParseResult result = OpenSkyStatesParser.parseWithDiagnostics(
                "{\"time\":1,\"states\":[[null,\"X\"],[\"abc\",\"Y\",\"Z\",null,null],[\"abd\",null,null,null,5]]}"
                        .getBytes(StandardCharsets.UTF_8));
        assertThat(result.skipped()).isEqualTo(2);
        assertThat(result.records()).extracting(NormalizedRecord::externalId).containsExactly("abd");
    }

    @Test
    @DisplayName("every payload key is declared in the descriptor's fieldSpecs")
    void fieldSpecs() {
        var descriptor = new OpenSkyStatesAdapter("", "").descriptor();
        FieldSpecContract.assertPayloadKeysDeclared(descriptor, edge());
        FieldSpecContract.assertPayloadKeysDeclared(descriptor,
                OpenSkyStatesParser.parse(AdapterFixtures.bytes(ID, "states-bbox.json")));
    }
}
