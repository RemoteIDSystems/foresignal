package ai.foresignal.source.adapters.gdacs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import tools.jackson.databind.json.JsonMapper;

/** {@link GdacsEventParser} against the captured MAP responses (prompt 067). */
class GdacsEventParserTest {

    private static GdacsEventParser.ParseResult parse(String fixture, String type) {
        return GdacsEventParser.parse(AdapterFixtures.bytes(GdacsAdapter.ID, fixture), type);
    }

    private static NormalizedRecord byId(List<NormalizedRecord> records, String id) {
        return records.stream().filter(r -> r.externalId().equals(id)).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("EQ: one record per event from the centroid points, polygons skipped, Green kept, the full payload")
    void earthquakes() {
        GdacsEventParser.ParseResult parsed = parse("MAP-EQ.json", "EQ");

        assertThat(parsed.features()).isEqualTo(10);
        assertThat(parsed.nonPoint()).isEqualTo(3);
        assertThat(parsed.records()).hasSize(7);
        assertThat(parsed.records()).filteredOn(r -> "Green".equals(r.payload().get("alertLevel"))).hasSize(6);
        NormalizedRecord orange = byId(parsed.records(), "EQ:1570339");
        assertThat(orange.kind()).isEqualTo("disaster");
        assertThat(orange.eventTime()).isEqualTo(Instant.parse("2026-10-09T17:56:06Z"));
        assertThat(orange.observedAt()).isEqualTo(Instant.parse("2026-10-12T20:01:14Z"));
        assertThat(orange.validUntil()).isNull();
        assertThat(((Point) orange.geometry()).getX()).isEqualTo(-80.769);
        assertThat(((Point) orange.geometry()).getY()).isEqualTo(7.5868);
        assertThat(orange.payload())
                .containsEntry("eventType", "EQ")
                .containsEntry("eventId", 1570339L)
                .containsEntry("episodeId", 1738684L)
                .containsEntry("name", "Earthquake in Panama")
                .containsEntry("description", "Earthquake in Panama")
                .containsEntry("alertLevel", "Orange")
                .containsEntry("alertScore", 2.0)
                .containsEntry("severity", 7.7)
                .containsEntry("severityText", "Magnitude 7.7M, Depth:12.647km")
                .containsEntry("severityUnit", "M")
                .containsEntry("country", "Panama")
                .containsEntry("iso3", "PAN")
                .containsEntry("affectedCountries", List.of("PAN"))
                .containsEntry("fromDate", "2026-10-09T17:56:06Z")
                .containsEntry("isCurrent", true)
                .containsEntry("isTemporary", false)
                .containsEntry("glide", "EQ-2026-000188-PAN")
                .containsEntry("automated", true)
                .containsKey("reportUrl");
        NormalizedRecord macquarie = byId(parsed.records(), "EQ:1570318");
        assertThat(macquarie.payload()).doesNotContainKeys("iso3", "affectedCountries", "glide", "country");
        FieldSpecContract.assertPayloadKeysDeclared(new GdacsAdapter("").descriptor(), parsed.records());
    }

    @Test
    @DisplayName("TC: track lines are skipped, five events, two Orange")
    void cyclones() {
        GdacsEventParser.ParseResult parsed = parse("MAP-TC.json", "TC");

        assertThat(parsed.nonPoint()).isEqualTo(5);
        assertThat(parsed.records()).extracting(NormalizedRecord::externalId)
                .containsExactly("TC:1001336", "TC:1001329", "TC:1001333", "TC:1001334", "TC:1001335");
        assertThat(byId(parsed.records(), "TC:1001335").payload()).containsEntry("severityUnit", "km/h")
                .containsEntry("alertLevel", "Orange");
        FieldSpecContract.assertPayloadKeysDeclared(new GdacsAdapter("").descriptor(), parsed.records());
    }

    @Test
    @DisplayName("features of another type are skipped; the newest episode and the centroid point win")
    void typeAndEpisodes() {
        assertThat(parse("MAP-EQ.json", "TC").records()).isEmpty();
        String json = "{\"type\":\"FeatureCollection\",\"features\":["
                + feature(5, 1, "Point_Affected", "2026-10-01T00:00:00", 1.0)
                + "," + feature(5, 2, "Point_Global", "2026-10-02T00:00:00", 2.0)
                + "," + feature(5, 2, "Point_Centroid", "2026-10-02T00:00:00", 3.0)
                + "," + feature(5, 1, "Point_Centroid", "2026-10-01T00:00:00", 4.0) + "]}";

        List<NormalizedRecord> records = GdacsEventParser.parse(json.getBytes(StandardCharsets.UTF_8), "FL").records();

        assertThat(records).singleElement().satisfies(r -> {
            assertThat(r.externalId()).isEqualTo("FL:5");
            assertThat(r.payload()).containsEntry("episodeId", 2L);
            assertThat(((Point) r.geometry()).getX()).isEqualTo(3.0);
        });
    }

    @Test
    @DisplayName("times without an offset are UTC; booleans come as strings; links must be on www.gdacs.org")
    void helpers() {
        JsonMapper json = JsonMapper.builder().build();
        assertThat(GdacsEventParser.time(json.readTree("\"2026-10-09T18:22:15\""))).isEqualTo(Instant.parse("2026-10-09T18:22:15Z"));
        assertThat(GdacsEventParser.time(json.readTree("\"2026-10-09T18:22:15+02:00\""))).isEqualTo(Instant.parse("2026-10-09T16:22:15Z"));
        assertThat(GdacsEventParser.time(json.readTree("\"soon\""))).isNull();
        assertThat(GdacsEventParser.bool(json.readTree("\"true\""))).isTrue();
        assertThat(GdacsEventParser.bool(json.readTree("false"))).isFalse();
        assertThat(GdacsEventParser.bool(json.readTree("\"maybe\""))).isNull();
        assertThat(GdacsEventParser.gdacsLink(json.readTree("\"https://www.gdacs.org/report.aspx?eventid=1\""))).isNotNull();
        assertThat(GdacsEventParser.gdacsLink(json.readTree("\"https://evil.example/report\""))).isNull();
    }

    @Test
    @DisplayName("an event without datemodified is skipped; a body that is not a FeatureCollection is a format error")
    void malformed() {
        String json = "{\"type\":\"FeatureCollection\",\"features\":[" + feature(9, 1, "Point_Centroid", null, 1.0) + "]}";
        GdacsEventParser.ParseResult parsed = GdacsEventParser.parse(json.getBytes(StandardCharsets.UTF_8), "FL");
        assertThat(parsed.records()).isEmpty();
        assertThat(parsed.skipped()).isEqualTo(1);

        assertThatThrownBy(() -> GdacsEventParser.parse(AdapterFixtures.bytes(GdacsAdapter.ID, "400-no-eventtype.json"), "EQ"))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> GdacsEventParser.parse("<html>".getBytes(StandardCharsets.UTF_8), "EQ"))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> GdacsEventParser.parse(new byte[0], "EQ")).isInstanceOf(UpstreamFormatException.class);
    }

    private static String feature(long eventId, long episodeId, String cls, String modified, double lon) {
        return "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[" + lon + ",10.0]},"
                + "\"properties\":{\"eventtype\":\"FL\",\"eventid\":" + eventId + ",\"episodeid\":" + episodeId
                + ",\"Class\":\"" + cls + "\",\"name\":\"Flood\",\"alertlevel\":\"Green\",\"alertscore\":1,"
                + "\"fromdate\":\"2026-10-01T00:00:00\""
                + (modified == null ? "" : ",\"datemodified\":\"" + modified + "\"") + "}}";
    }
}
