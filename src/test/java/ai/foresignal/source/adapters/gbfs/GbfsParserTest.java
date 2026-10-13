package ai.foresignal.source.adapters.gbfs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link GbfsParser} on the captured Citi Bike, BCycle and publicbikesystem.net feeds and the hand-made fixtures
 * (prompt 071): v2.3 and v3.0 produce the same payload shape, localized names and the language fallback, boolean
 * normalization, the {@code availability} boundaries, and stations without a usable {@code last_reported} dropped.
 */
class GbfsParserTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static byte[] fixture(String dir, String name) {
        return AdapterFixtures.bytes("gbfs/" + dir, name);
    }

    static GbfsParser.Snapshot snapshot(String dir, String language) {
        GbfsParser.Information info = GbfsParser.parseInformation(fixture(dir, "station_information.json"), language);
        GbfsParser.Status status = GbfsParser.parseStatus(fixture(dir, "station_status.json"));
        return GbfsParser.merge("test", info.stations(), status);
    }

    static NormalizedRecord record(GbfsParser.Snapshot snapshot, String id) {
        return snapshot.records().stream().filter(r -> r.externalId().equals(id)).findFirst().orElseThrow();
    }

    static GbfsParser.Status status(String json) {
        return GbfsParser.parseStatus(json.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("the v2.3 and v3.0 feeds of one system produce identical payload shapes and values")
    void v2AndV3Identical() {
        GbfsParser.Snapshot v2 = snapshot("austin-capmetro", null);
        GbfsParser.Snapshot v3 = snapshot("austin-capmetro-v3", null);
        assertThat(v2.records()).hasSize(25);
        assertThat(v3.records()).hasSize(25);

        NormalizedRecord a = record(v2, "1");
        NormalizedRecord b = record(v3, "1");
        assertThat(a.payload().keySet()).isEqualTo(b.payload().keySet());
        assertThat(a.payload()).containsEntry("name", "E 10th/Red River").containsEntry("address", "E 10th/Red River")
                .containsEntry("capacity", 15).containsEntry("bikesAvailable", 3).containsEntry("docksAvailable", 12)
                .containsEntry("disabledBikes", 0).containsEntry("disabledDocks", 0).containsEntry("isInstalled", true)
                .containsEntry("isRenting", true).containsEntry("isReturning", true).containsEntry("availability", "ok")
                .containsEntry("system", "test").doesNotContainKey("ebikesAvailable").doesNotContainKey("regionId");
        assertThat(b.payload()).containsEntry("name", "E 10th/Red River").containsEntry("bikesAvailable", 3)
                .containsEntry("availability", "ok");
        assertThat(a.observedAt()).isEqualTo(Instant.ofEpochSecond(1_791_909_991L));
        assertThat(b.observedAt()).isEqualTo(Instant.parse("2026-10-13T16:50:39Z"));
        assertThat(a.kind()).isEqualTo(GbfsParser.KIND);
        Point point = (Point) a.geometry();
        assertThat(point.getX()).isEqualTo(-97.735364);
        assertThat(point.getY()).isEqualTo(30.270155);
        assertThat(v2.records()).allSatisfy(r -> assertThat(r.payload().keySet())
                .isSubsetOf(GbfsDescriptors.descriptor(GbfsSystems.reviewed("austin-capmetro")).fieldSpecs().keySet()));
    }

    @Test
    @DisplayName("envelopes: version, ttl and last_updated in both time formats")
    void envelopes() {
        GbfsParser.Information v2 = GbfsParser.parseInformation(fixture("citibike", "station_information.json"), "en");
        assertThat(v2.version()).isEqualTo("2.3");
        assertThat(v2.ttlSeconds()).isEqualTo(60);
        assertThat(v2.lastUpdated()).isEqualTo(Instant.ofEpochSecond(1_791_910_007L));
        assertThat(v2.stations()).hasSize(25);

        GbfsParser.Status v3 = GbfsParser.parseStatus(fixture("austin-capmetro-v3", "station_status.json"));
        assertThat(v3.version()).isEqualTo("3.0");
        assertThat(v3.ttlSeconds()).isEqualTo(18);
        assertThat(v3.lastUpdated()).isEqualTo(Instant.parse("2026-10-13T16:51:23Z"));

        GbfsParser.Status bcycle = GbfsParser.parseStatus(fixture("boulder-bcycle", "station_status.json"));
        assertThat(bcycle.version()).isEqualTo("1.1");
        assertThat(bcycle.stations()).hasSize(25);
    }

    @Test
    @DisplayName("Citi Bike: the 86400 placeholders are dropped and counted; the rest carry e-bike counts")
    void citiBike() {
        GbfsParser.Snapshot snapshot = snapshot("citibike", "en");
        assertThat(snapshot.untimed()).isEqualTo(11);
        assertThat(snapshot.unmatched()).isZero();
        assertThat(snapshot.records()).hasSize(14);
        NormalizedRecord full = record(snapshot, "66db408f-0aca-11e7-82f6-3863bb44ef7c");
        assertThat(full.payload()).containsEntry("bikesAvailable", 37).containsEntry("ebikesAvailable", 11)
                .containsEntry("docksAvailable", 0).containsEntry("availability", "full");
        assertThat(full.observedAt()).isEqualTo(Instant.ofEpochSecond(1_791_909_859L));
        assertThat(record(snapshot, "545a32d3-b74c-4dc2-b82c-a6d185dd4666").payload())
                .containsEntry("availability", "empty");
        assertThat(record(snapshot, "98ebf9c6-1d94-4e21-9007-c623875fdb5e").payload())
                .containsEntry("availability", "low");
        assertThat(full.payload()).containsKey("regionId");
    }

    @Test
    @DisplayName("BCycle 1.1: num_bikes_available_types.electric is the e-bike count; 0/1 booleans")
    void bcycle() {
        GbfsParser.Snapshot snapshot = snapshot("boulder-bcycle", "en");
        assertThat(snapshot.records()).hasSize(25);
        NormalizedRecord first = record(snapshot, "bcycle_boulder_1855");
        assertThat(first.payload()).containsEntry("name", "Folsom & Colorado")
                .containsEntry("address", "SE corner of Folsom & Colorado").containsEntry("bikesAvailable", 1)
                .containsEntry("ebikesAvailable", 1).containsEntry("docksAvailable", 14)
                .containsEntry("isInstalled", true).containsEntry("isRenting", true)
                .containsEntry("availability", "low").doesNotContainKey("capacity");
    }

    @Test
    @DisplayName("localized names: the requested language, its primary subtag, else the first entry")
    void localizedNames() {
        byte[] json = fixture("synthetic", "localized-name-v3.json");
        Map<String, GbfsParser.StationInfo> en = GbfsParser.parseInformation(json, "en").stations();
        assertThat(en.get("V1").name()).isEqualTo("Central Station");
        assertThat(en.get("V2").name()).as("no English name: the first").isEqualTo("Parc du Mont-Royal");
        assertThat(en.get("V3").name()).as("en matches en-CA by primary subtag").isEqualTo("Old Port");

        Map<String, GbfsParser.StationInfo> fr = GbfsParser.parseInformation(json, "fr").stations();
        assertThat(fr.get("V1").name()).isEqualTo("Gare Centrale");
        assertThat(fr.get("V3").name()).isEqualTo("Vieux-Port");

        Map<String, GbfsParser.StationInfo> frCa = GbfsParser.parseInformation(json, "fr-CA").stations();
        assertThat(frCa.get("V3").name()).isEqualTo("Vieux-Port");
        assertThat(frCa.get("V1").name()).as("fr-CA falls back to fr").isEqualTo("Gare Centrale");

        Map<String, GbfsParser.StationInfo> defaulted = GbfsParser.parseInformation(json, null).stations();
        assertThat(defaulted.get("V1").name()).isEqualTo("Central Station");
        assertThat(GbfsParser.parseInformation(json, "en").lastUpdated()).isEqualTo(Instant.parse("2026-10-13T16:46:40Z"));
        assertThat(GbfsParser.parseInformation(json, "en").ttlSeconds()).isZero();

        GbfsParser.Information austin = GbfsParser.parseInformation(fixture("austin-capmetro-v3",
                "station_information.json"), "nl");
        assertThat(austin.stations().get("1").name()).isEqualTo("E 10th/Red River");
    }

    @Test
    @DisplayName("booleans: true/false, 0/1, \"true\"/\"yes\"/\"0\", and the fallback")
    void booleans() throws Exception {
        GbfsParser.Information info = GbfsParser.parseInformation(fixture("synthetic", "information.json"), "en");
        GbfsParser.Snapshot snapshot = GbfsParser.merge("s", info.stations(),
                GbfsParser.parseStatus(fixture("synthetic", "bool-as-int.json")));
        NormalizedRecord s1 = record(snapshot, "S1");
        assertThat(s1.payload()).containsEntry("isInstalled", true).containsEntry("isRenting", false)
                .containsEntry("isReturning", true).containsEntry("availability", "out-of-service")
                .containsEntry("ebikesAvailable", 2);
        NormalizedRecord s2 = record(snapshot, "S2");
        assertThat(s2.payload()).containsEntry("isInstalled", true).containsEntry("isRenting", true)
                .containsEntry("isReturning", false).containsEntry("availability", "ok");
        assertThat(s2.observedAt()).as("a numeric-string last_reported").isEqualTo(Instant.ofEpochSecond(1_791_909_990L));

        assertThat(GbfsParser.normalizeBool(JSON.readTree("true"), false)).isTrue();
        assertThat(GbfsParser.normalizeBool(JSON.readTree("0"), true)).isFalse();
        assertThat(GbfsParser.normalizeBool(JSON.readTree("2"), false)).isTrue();
        assertThat(GbfsParser.normalizeBool(JSON.readTree("\"no\""), true)).isFalse();
        assertThat(GbfsParser.normalizeBool(JSON.readTree("\"maybe\""), true)).isTrue();
        assertThat(GbfsParser.normalizeBool(JSON.readTree("\"maybe\""), false)).isFalse();
        assertThat(GbfsParser.normalizeBool(JSON.readTree("null"), true)).isTrue();
        assertThat(GbfsParser.normalizeBool(null, false)).isFalse();
        assertThat(GbfsParser.normalizeBool(JSON.readTree("[]"), true)).isTrue();
    }

    @Test
    @DisplayName("availability boundaries: 0, 2 and 3 bikes; 0 docks; not renting; not installed")
    void availability() {
        assertThat(GbfsParser.availability(true, true, 0, 10)).isEqualTo("empty");
        assertThat(GbfsParser.availability(true, true, 2, 10)).isEqualTo("low");
        assertThat(GbfsParser.availability(true, true, 3, 10)).isEqualTo("ok");
        assertThat(GbfsParser.availability(true, true, 5, 0)).isEqualTo("full");
        assertThat(GbfsParser.availability(true, true, 5, null)).as("unknown docks are never full").isEqualTo("ok");
        assertThat(GbfsParser.availability(true, false, 5, 5)).isEqualTo("out-of-service");
        assertThat(GbfsParser.availability(false, true, 5, 5)).isEqualTo("out-of-service");
        assertThat(GbfsParser.availability(true, true, 0, 0)).as("empty before full").isEqualTo("empty");
        assertThat(GbfsParser.availability(true, true, 1, 0)).as("low before full").isEqualTo("low");
    }

    @Test
    @DisplayName("a station without last_reported, or with the 86400 placeholder, is dropped and counted")
    void missingLastReported() {
        GbfsParser.Information info = GbfsParser.parseInformation(fixture("synthetic", "information.json"), "en");
        GbfsParser.Snapshot snapshot = GbfsParser.merge("s", info.stations(),
                GbfsParser.parseStatus(fixture("synthetic", "missing-last-reported.json")));
        assertThat(snapshot.records()).isEmpty();
        assertThat(snapshot.untimed()).isEqualTo(2);
    }

    @Test
    @DisplayName("a status-only station is skipped and counted; information without a position is skipped")
    void statusOnly() {
        GbfsParser.Information info = GbfsParser.parseInformation(fixture("synthetic", "information.json"), "en");
        assertThat(info.stations()).containsOnlyKeys("S1", "S2");
        assertThat(info.skipped()).isEqualTo(2);
        GbfsParser.Snapshot snapshot = GbfsParser.merge("s", info.stations(),
                GbfsParser.parseStatus(fixture("synthetic", "status-only-station.json")));
        assertThat(snapshot.records()).extracting(NormalizedRecord::externalId).containsExactly("S1");
        assertThat(snapshot.unmatched()).isEqualTo(1);
        assertThat(record(snapshot, "S1").payload()).containsEntry("regionId", "r1").containsEntry("capacity", 10)
                .containsEntry("address", "1 Main St");
        assertThat(GbfsParser.merge("s", null, GbfsParser.parseStatus(fixture("synthetic", "status-only-station.json")))
                .unmatched()).isEqualTo(2);
    }

    @Test
    @DisplayName("a duplicate station keeps its newest report; an entry without a bike count is skipped")
    void duplicatesAndMalformed() {
        GbfsParser.Status status = status("""
                {"last_updated": 1791910000, "ttl": 60, "data": {"stations": [
                 {"station_id": "S1", "num_bikes_available": 1, "num_docks_available": 9, "last_reported": 1791909900},
                 {"station_id": "S1", "num_bikes_available": 7, "num_docks_available": 3, "last_reported": 1791909990},
                 {"station_id": "S1", "num_bikes_available": 2, "num_docks_available": 8, "last_reported": 1791909800},
                 {"station_id": "S2", "num_docks_available": 8, "last_reported": 1791909800},
                 {"num_bikes_available": 2, "last_reported": 1791909800}]}}""");
        assertThat(status.stations()).hasSize(1);
        assertThat(status.stations().get(0).bikesAvailable()).isEqualTo(7);
        assertThat(status.skipped()).isEqualTo(2);
        assertThat(status.version()).isNull();
    }

    @Test
    @DisplayName("GEV's locale wrapper under data is read; non-GBFS bodies are format errors")
    void envelopeErrors() {
        GbfsParser.Status wrapped = status("""
                {"last_updated": 1791910000, "ttl": 60, "data": {"en": {"stations": [
                 {"station_id": "S1", "num_bikes_available": 1, "num_docks_available": 9, "last_reported": 1791909900}]}}}""");
        assertThat(wrapped.stations()).hasSize(1);

        assertThatThrownBy(() -> status("")).isInstanceOf(UpstreamFormatException.class).hasMessageContaining("empty");
        assertThatThrownBy(() -> status("<html>")).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("not valid JSON");
        assertThatThrownBy(() -> status("[1]")).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("not a JSON object");
        assertThatThrownBy(() -> status("{\"data\": {\"feeds\": []}}")).isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("no data.stations");
        assertThatThrownBy(() -> GbfsParser.parseInformation(new byte[0], "en"))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("times: epoch seconds, numeric strings, RFC 3339 with offsets; placeholders and garbage are null")
    void times() throws Exception {
        assertThat(GbfsParser.time(JSON.readTree("1791909990"))).isEqualTo(Instant.ofEpochSecond(1_791_909_990L));
        assertThat(GbfsParser.time(JSON.readTree("1791909990.7"))).isEqualTo(Instant.ofEpochSecond(1_791_909_990L));
        assertThat(GbfsParser.time(JSON.readTree("\"2026-10-13T09:46:30-07:00\"")))
                .isEqualTo(Instant.parse("2026-10-13T16:46:30Z"));
        assertThat(GbfsParser.time(JSON.readTree("86400"))).isNull();
        assertThat(GbfsParser.time(JSON.readTree("-5"))).isNull();
        assertThat(GbfsParser.time(JSON.readTree("\"yesterday\""))).isNull();
        assertThat(GbfsParser.time(JSON.readTree("true"))).isNull();
        assertThat(GbfsParser.time(null)).isNull();
    }

    @Test
    @DisplayName("names fall back to short_name; numeric ids are read as text")
    void fallbacks() {
        GbfsParser.Information info = GbfsParser.parseInformation("""
                {"last_updated": 1791910000, "ttl": 7200, "data": {"stations": [
                 {"station_id": 42, "short_name": "42", "lat": "40.1", "lon": "-105.2", "capacity": -3}]}}"""
                .getBytes(StandardCharsets.UTF_8), "en");
        GbfsParser.StationInfo station = info.stations().get("42");
        assertThat(station.name()).isEqualTo("42");
        assertThat(station.lat()).isEqualTo(40.1);
        assertThat(station.capacity()).as("negative capacity is not a capacity").isNull();
        assertThat(info.ttlSeconds()).isEqualTo(7200);
        assertThat(List.of(info.version() == null)).containsExactly(true);
    }
}
