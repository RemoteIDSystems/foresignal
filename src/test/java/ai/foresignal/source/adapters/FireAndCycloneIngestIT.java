package ai.foresignal.source.adapters;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.HealthService;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.adapters.fire.NifcPerimetersAdapter;
import ai.foresignal.source.adapters.nhc.NhcCycloneConesAdapter;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The prompt 075 sources end to end through the real fetch path and ingest writer: every source registers disabled
 * with a license; NIFC perimeters ingest and one absent from two consecutive complete fetches is tombstoned; an NHC
 * advisory change writes version 2 of the cone; an out-of-season (empty) NHC service keeps the source healthy.
 *
 * <p>The NIFC fixture ids and the NHC source are used by no other test, so per-entity history holds in the shared
 * context.
 */
class FireAndCycloneIngestIT extends WeatherIngestSupport {

    static final String NIFC = NifcPerimetersAdapter.ID;
    static final String NIFC_LAYER = "/services3.arcgis.com/T4QMspbfLg3qTGWY/arcgis/rest/services/"
            + "WFIGS_Interagency_Perimeters_Current/FeatureServer/0";
    static final String NHC = NhcCycloneConesAdapter.ID;
    static final String NHC_SERVICE = "/mapservices.weather.noaa.gov/tropical/rest/services/tropical/"
            + "NHC_tropical_weather_summary/MapServer";
    static final Map<String, Integer> NHC_LAYERS = Map.of("points", 5, "track", 6, "cone", 7, "warning", 8);
    static final String FIRST_FIRE = "2026-FLFNF-000419";
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired HealthService health;

    @AfterEach
    void disable() {
        configure(NIFC, false, Map.of());
        configure(NHC, false, Map.of());
    }

    private static void serve(String path, Map<String, String> query, byte[] body) {
        var stub = get(urlPathEqualTo(path));
        for (Map.Entry<String, String> q : query.entrySet()) {
            stub = stub.withQueryParam(q.getKey(), equalTo(q.getValue()));
        }
        UPSTREAM.stubFor(stub.willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                .withBody(body)));
    }

    /** The NIFC layer document with {@code editDate} as every edit date. */
    private static byte[] nifcLayer(long editDate) {
        ObjectNode layer = (ObjectNode) JSON.readTree(AdapterFixtures.bytes("nifc-perimeters", "layer.json"));
        ObjectNode editing = (ObjectNode) layer.get("editingInfo");
        editing.put("lastEditDate", editDate);
        editing.put("dataLastEditDate", editDate);
        return JSON.writeValueAsBytes(layer);
    }

    /** The NIFC page without the perimeter {@code uniqueId}. */
    private static byte[] nifcPageWithout(String uniqueId) {
        ObjectNode page = (ObjectNode) JSON.readTree(AdapterFixtures.bytes("nifc-perimeters", "query-page-1.json"));
        ArrayNode features = (ArrayNode) page.get("features");
        for (int i = features.size() - 1; i >= 0; i--) {
            if (uniqueId.equals(features.get(i).path("properties").path("attr_UniqueFireIdentifier").asString())) {
                features.remove(i);
            }
        }
        return JSON.writeValueAsBytes(page);
    }

    private void serveNhc(Map<String, byte[]> queries) {
        serve(NHC_SERVICE, Map.of("f", "json"), AdapterFixtures.bytes("nhc-cyclone-cones", "service.json"));
        NHC_LAYERS.forEach((key, id) -> {
            serve(NHC_SERVICE + "/" + id, Map.of("f", "json"), AdapterFixtures.bytes("nhc-cyclone-cones",
                    "layer-" + key + ".json"));
            serve(NHC_SERVICE + "/" + id + "/query", Map.of(), queries.get(key));
        });
    }

    private static Map<String, byte[]> nhcFixtures() {
        return Map.of("points", AdapterFixtures.bytes("nhc-cyclone-cones", "query-points.json"),
                "track", AdapterFixtures.bytes("nhc-cyclone-cones", "query-track.json"),
                "cone", AdapterFixtures.bytes("nhc-cyclone-cones", "query-cone.json"),
                "warning", AdapterFixtures.bytes("nhc-cyclone-cones", "query-warning.json"));
    }

    @Test
    @DisplayName("every 075 source is registered, disabled by default, with a license; no HMS source is added")
    void registered() {
        List<String> ids = List.of("nifc-perimeters", "inciweb-incidents", "cwfis-perimeters", "cwfis-fwi-stations",
                "cwfis-hotspots", "radio-browser", "nhc-cyclone-cones");
        assertThat(sources.list()).filteredOn(s -> ids.contains(s.id())).hasSize(ids.size())
                .allSatisfy(s -> assertThat(s.enabled()).as(s.id()).isFalse());
        assertThat(sources.list()).filteredOn(s -> s.id().startsWith("hms-")).extracting(s -> s.id())
                .containsExactlyInAnyOrder("hms-smoke", "hms-fire");
    }

    @Test
    @DisplayName("NIFC: 25 perimeters ingest; one absent from two consecutive complete fetches is tombstoned once")
    void nifcPerimeters() {
        clock.setInstant(Instant.parse("2026-10-13T19:30:00Z"));
        configure(NIFC, true, Map.of());
        serve(NIFC_LAYER, Map.of("f", "json"), nifcLayer(1_791_919_634_361L));
        serve(NIFC_LAYER + "/query", Map.of(), AdapterFixtures.bytes("nifc-perimeters", "query-page-1.json"));
        assertThat(fetch(NIFC).success()).isTrue();
        assertThat(versions(NIFC, FIRST_FIRE)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from record_head where source_id = ? and not tombstone",
                Integer.class, NIFC)).isEqualTo(25);
        assertThat(jdbc.queryForObject("select payload->>'geometrySimplified' from record_head where source_id = ?"
                + " and external_id = '2026-ORFWF-260286'", String.class, NIFC)).isEqualTo("true");

        clock.advance(Duration.ofMinutes(5));
        serve(NIFC_LAYER, Map.of("f", "json"), nifcLayer(1_791_919_934_361L));
        serve(NIFC_LAYER + "/query", Map.of(), nifcPageWithout(FIRST_FIRE));
        assertThat(fetch(NIFC).success()).isTrue();
        assertThat(tombstoned(NIFC, FIRST_FIRE)).as("one miss is not enough").isFalse();

        clock.advance(Duration.ofMinutes(5));
        serve(NIFC_LAYER, Map.of("f", "json"), nifcLayer(1_791_920_234_361L));
        assertThat(fetch(NIFC).success()).isTrue();
        assertThat(tombstoned(NIFC, FIRST_FIRE)).isTrue();
        int afterTombstone = versions(NIFC, FIRST_FIRE);

        clock.advance(Duration.ofMinutes(5));
        serve(NIFC_LAYER, Map.of("f", "json"), nifcLayer(1_791_920_534_361L));
        assertThat(fetch(NIFC).success()).isTrue();
        assertThat(versions(NIFC, FIRST_FIRE)).as("tombstoned once").isEqualTo(afterTombstone);
    }

    @Test
    @DisplayName("NHC: the four layers ingest; a new advisory writes version 2 of the cone; empty keeps it healthy")
    void nhcAdvisoryAndOffSeason() {
        clock.setInstant(Instant.parse("2026-10-13T19:30:00Z"));
        configure(NHC, true, Map.of());
        serveNhc(nhcFixtures());
        assertThat(fetch(NHC).success()).isTrue();
        assertThat(versions(NHC, "ep202026")).as("the cone").isEqualTo(1);
        assertThat(versions(NHC, "ep202026:track")).as("the track").isEqualTo(1);
        assertThat(jdbc.queryForObject("select count(*) from record_head where source_id = ? and kind ="
                + " 'cyclone-cone'", Integer.class, NHC)).isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from record_head where source_id = ? and kind ="
                + " 'cyclone-forecast-point'", Integer.class, NHC)).isEqualTo(18);
        assertThat(jdbc.queryForObject("select payload->>'stormId' from record_head where source_id = ? and"
                + " external_id = 'ep202026:HWA:2'", String.class, NHC)).isEqualTo("ep202026");

        clock.advance(Duration.ofHours(6));
        Map<String, byte[]> next = new java.util.HashMap<>(nhcFixtures());
        String cone = new String(next.get("cone"), StandardCharsets.UTF_8).replace("\"advisnum\":\"25A\"",
                "\"advisnum\":\"26\"").replace("1791914547000", "1791936147000");
        next.put("cone", cone.getBytes(StandardCharsets.UTF_8));
        serveNhc(next);
        assertThat(fetch(NHC).success()).isTrue();
        assertThat(versions(NHC, "ep202026")).as("version 2 of the cone").isEqualTo(2);
        assertThat(versions(NHC, "ep202026:track")).as("the unchanged track").isEqualTo(1);
        assertThat(jdbc.queryForObject("select payload->>'advisoryNumber' from record_head where source_id = ? and"
                + " external_id = 'ep202026'", String.class, NHC)).isEqualTo("26");

        clock.advance(Duration.ofMinutes(10));
        byte[] empty = "{\"type\": \"FeatureCollection\", \"features\": []}".getBytes(StandardCharsets.UTF_8);
        serveNhc(Map.of("points", empty, "track", empty, "cone", empty, "warning", empty));
        assertThat(fetch(NHC).success()).isTrue();
        assertThat(health.health(NHC).status()).as("zeroResultsNormal out of season").isEqualTo(HealthStatus.HEALTHY);
    }
}
