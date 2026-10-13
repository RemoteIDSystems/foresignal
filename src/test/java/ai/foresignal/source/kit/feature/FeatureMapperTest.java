package ai.foresignal.source.kit.feature;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.kit.KitTestSpecs;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.ingest.SnapshotContract;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link FeatureMapper} (prompt 073): allowlisted and renamed fields, typing, value maps, constants, sentinels
 * removed, date formats, the fixed time base, a missing id rejected, and a stable static hash across re-fetches.
 */
class FeatureMapperTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static final String SPEC = """
            {
              "id": "feature-test-mapper",
              "name": "Mapper test",
              "kind": "road-restriction",
              "protocol": "arcgis",
              "url": "https://a.example/FeatureServer/0",
              "idField": "id",
              "titleField": "label",
              "fields": {
                "OBJ": {"target": "id", "type": "integer"},
                "Label": {"target": "label", "type": "string"},
                "Width": {"target": "widthM", "type": "number", "unit": "m"},
                "Lanes": {"target": "lanes", "type": "integer"},
                "Open": {"target": "open", "type": "boolean"},
                "Level": {"target": "level", "type": "integer", "valueMap": {"1": "low", "2": "high"}},
                "Updated": {"target": "updatedAt", "type": "timestamp"},
                "Starts": {"target": "startsAt", "type": "timestamp"},
                "Ends": {"target": "endsAt", "type": "timestamp"},
                "Secret": {"target": "secretNote", "type": "string"}
              },
              "constants": {"restrictionType": "closure", "source": "test"},
              "time": {"observedAt": "updatedAt", "validFrom": "startsAt", "validUntil": "endsAt", "format": "auto",
                       "zone": "America/Denver"},
              "budget": {"perMinute": 1, "perDay": 10},
              "license": "Test",
              "attribution": "Test",
              "homepage": "https://example.org/"
            }
            """;

    static JsonNode feature(String properties, String geometry) {
        return JSON.readTree("{\"type\": \"Feature\", \"geometry\": " + geometry + ", \"properties\": " + properties
                + "}");
    }

    static FeatureMapper.Mapped map(String properties) {
        return FeatureMapper.map(KitTestSpecs.feature(SPEC), feature(properties, "{\"type\": \"Point\","
                + " \"coordinates\": [-105.0, 39.7]}"));
    }

    @Test
    @DisplayName("fields are allowlisted, renamed and typed; constants are added; unknown attributes are dropped")
    void typing() {
        FeatureMapper.Mapped m = map("""
                {"OBJ": "17", "Label": "  Main St  ", "Width": "3.5", "Lanes": 2.0, "Open": "Y", "Level": 2,
                 "Updated": 1791913696835, "Starts": "2026-10-13 08:00:00", "Ends": "2026-10-14T08:00:00Z",
                 "Unlisted": "dropped", "Secret": null}
                """);
        assertThat(m.accepted()).isTrue();
        NormalizedRecord r = m.record();
        assertThat(r.externalId()).isEqualTo("17");
        assertThat(r.kind()).isEqualTo("road-restriction");
        assertThat(r.payload()).containsEntry("id", 17L).containsEntry("label", "Main St").containsEntry("widthM", 3.5)
                .containsEntry("lanes", 2L).containsEntry("open", true).containsEntry("level", "high")
                .containsEntry("restrictionType", "closure").containsEntry("source", "test")
                .containsEntry("updatedAt", Instant.ofEpochMilli(1791913696835L).toString())
                .doesNotContainKeys("Unlisted", "OBJ", "secretNote");
        assertThat(r.observedAt()).isEqualTo(Instant.ofEpochMilli(1791913696835L));
        // A local date-time is read in the spec's zone (America/Denver, UTC-6 in October).
        assertThat(r.validFrom()).isEqualTo(Instant.parse("2026-10-13T14:00:00Z"));
        assertThat(r.validUntil()).isEqualTo(Instant.parse("2026-10-14T08:00:00Z"));
        assertThat(r.eventTime()).isNull();
        assertThat(r.geometry().getSRID()).isEqualTo(4326);
        assertThat(r.geometry().getCoordinate().x).isEqualTo(-105.0);
    }

    @Test
    @DisplayName("nulls, blanks and sentinels (-999, -9999, 0001-01-01, year-1 epoch) are removed")
    void sentinels() {
        NormalizedRecord r = map("""
                {"OBJ": 1, "Label": "   ", "Width": -999, "Lanes": "-9999", "Open": null,
                 "Updated": 1791913696835, "Starts": "0001-01-01T00:00:00Z", "Ends": -62135596800000}
                """).record();
        assertThat(r.payload()).doesNotContainKeys("label", "widthM", "lanes", "open", "startsAt", "endsAt");
        assertThat(r.validFrom()).isNull();
        assertThat(r.validUntil()).isNull();
    }

    @Test
    @DisplayName("a value map applies by text; an unmapped code keeps its typed value")
    void valueMap() {
        assertThat(map("{\"OBJ\": 1, \"Level\": \"1\", \"Updated\": 1}").record().payload())
                .containsEntry("level", "low");
        assertThat(map("{\"OBJ\": 1, \"Level\": 3, \"Updated\": 1791913696835}").record().payload())
                .containsEntry("level", 3L);
    }

    @Test
    @DisplayName("date formats: epoch-ms, epoch-s, ISO with and without an offset, a pattern, unreadable dropped")
    void dates() {
        java.time.ZoneId utc = java.time.ZoneOffset.UTC;
        JsonNode n = JSON.readTree("1791913696");
        assertThat(FeatureMapper.timestamp(n, "1791913696", "epoch-s", utc))
                .isEqualTo(Instant.ofEpochSecond(1791913696L));
        assertThat(FeatureMapper.timestamp(JSON.readTree("1791913696835"), "1791913696835", "epoch-ms", utc))
                .isEqualTo(Instant.ofEpochMilli(1791913696835L));
        JsonNode text = JSON.readTree("\"x\"");
        assertThat(FeatureMapper.timestamp(text, "2026-10-13T10:00:00+02:00", "iso", utc))
                .isEqualTo(Instant.parse("2026-10-13T08:00:00Z"));
        assertThat(FeatureMapper.timestamp(text, "2026-10-13T10:00:00", "iso", utc))
                .isEqualTo(Instant.parse("2026-10-13T10:00:00Z"));
        assertThat(FeatureMapper.timestamp(text, "2026-10-13", "iso", utc)).isEqualTo(Instant.parse(
                "2026-10-13T00:00:00Z"));
        assertThat(FeatureMapper.timestamp(text, "13/10/2026 10:00", "dd/MM/yyyy HH:mm", utc))
                .isEqualTo(Instant.parse("2026-10-13T10:00:00Z"));
        assertThat(FeatureMapper.timestamp(text, "2026-10-13 10:00:00", "auto", utc))
                .isEqualTo(Instant.parse("2026-10-13T10:00:00Z"));
        assertThat(FeatureMapper.timestamp(text, "soon", "auto", utc)).isNull();
        assertThat(FeatureMapper.timestamp(text, "2026-10-13", "epoch-ms", utc)).isNull();
    }

    @Test
    @DisplayName("a missing id is rejected; a missing observedAt field is rejected, never replaced by another field")
    void rejections() {
        assertThat(map("{\"Label\": \"x\", \"Updated\": 1791913696835}").reason())
                .isEqualTo(FeatureMapper.REJECT_MISSING_ID);
        assertThat(map("{\"OBJ\": -999, \"Updated\": 1791913696835}").reason())
                .isEqualTo(FeatureMapper.REJECT_MISSING_ID);
        assertThat(map("{\"OBJ\": 5, \"Starts\": \"2026-10-13T00:00:00Z\"}").reason())
                .isEqualTo(FeatureMapper.REJECT_MISSING_OBSERVED_AT);
        assertThat(FeatureMapper.map(KitTestSpecs.feature(SPEC), JSON.readTree("[1]")).reason())
                .isEqualTo(FeatureMapper.REJECT_NOT_A_FEATURE);
        assertThat(FeatureMapper.map(KitTestSpecs.feature(SPEC), feature("{\"OBJ\": 1, \"Updated\": 1791913696835}",
                "{\"type\": \"Point\", \"coordinates\": []}")).reason()).isEqualTo(FeatureMapper.REJECT_BAD_GEOMETRY);
    }

    @Test
    @DisplayName("a feature without geometry keeps a null geometry; observedAt RECEIVED leaves it to the framework")
    void noGeometryAndReceived() {
        FeatureSourceSpec received = KitTestSpecs.feature(KitTestSpecs.with(SPEC, Map.of("time",
                Map.of("observedAt", "RECEIVED"))));
        FeatureMapper.Mapped m = FeatureMapper.map(received, feature("{\"OBJ\": 9}", "null"));
        assertThat(m.accepted()).isTrue();
        assertThat(m.record().geometry()).isNull();
        assertThat(m.record().observedAt()).isNull();
    }

    @Test
    @DisplayName("the WFIGS fixture maps every feature; two parses of one incident share a static hash")
    void wfigsSnapshot() {
        FeatureSourceSpec spec = new ai.foresignal.source.kit.KitSpecs(ai.foresignal.source.kit.KitOverlay.disabled(),
                "").features().stream().filter(s -> s.id().equals("feature-wfigs-incidents")).findFirst()
                .orElseThrow();
        JsonNode page = JSON.readTree(AdapterFixtures.bytes("feature-wfigs-incidents", "query-page-1.json"));
        List<NormalizedRecord> first = new ArrayList<>();
        for (JsonNode f : page.get("features")) {
            FeatureMapper.Mapped m = FeatureMapper.map(spec, f);
            assertThat(m.accepted()).as(String.valueOf(m.reason())).isTrue();
            first.add(m.record());
        }
        assertThat(first).hasSize(5);
        NormalizedRecord pondo = first.get(0);
        assertThat(pondo.externalId()).isEqualTo("{1B4FF51E-95DF-4E25-A871-BB406059EEA2}");
        assertThat(pondo.payload()).containsEntry("name", "D1 Pondo RX").containsEntry("state", "US-UT")
                .containsEntry("sizeAcres", 532.0).containsEntry("incidentType", "RX")
                .doesNotContainKey("percentContained");
        assertThat(pondo.eventTime()).isEqualTo(Instant.ofEpochMilli(1767644640000L));
        assertThat(pondo.observedAt()).isEqualTo(Instant.ofEpochMilli(1791582216973L));
        NormalizedRecord again = FeatureMapper.map(spec, JSON.readTree(AdapterFixtures.bytes(
                "feature-wfigs-incidents", "query-page-1.json")).get("features").get(0)).record();
        SnapshotContract.assertStableStaticHash(FeatureDescriptors.descriptor(spec), List.of(pondo, again));
    }
}
