package ai.foresignal.source.adapters.passes;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.derive.DerivedRunner;
import ai.foresignal.place.PlaceService;
import ai.foresignal.source.adapters.celestrak.CelestrakGpAdapter;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code satellite-passes} on the real DERIVED runtime over {@code celestrak-gp} heads from a real fetch
 * (prompt 064): passes emitted for a place, unchanged by an element refresh within rounding, tombstoned after LOS.
 */
class SatellitePassesDerivedIT extends WeatherIngestSupport {

    private static final Duration WAIT = Duration.ofSeconds(60);
    private static final String GP = CelestrakGpAdapter.ID;
    private static final String ID = SatellitePassesGenerator.ID;
    private static final String ACTIVE = "/NORAD/elements/gp.php?GROUP=active&FORMAT=csv";
    private static final String PLACE = "passes-austin";
    private static final Instant T = Instant.parse("2026-10-13T06:00:00Z");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired private DerivedRunner runner;
    @Autowired private PlaceService places;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        places.create(JSON.readTree("{\"id\":\"" + PLACE + "\",\"name\":\"Passes Austin\",\"shape\":\"point\","
                + "\"geometry\":{\"type\":\"Point\",\"coordinates\":[-97.7431,30.2672]},\"radiusKm\":10,"
                + "\"timezone\":\"America/Chicago\",\"attributes\":{\"altitudeM\":150}}"));
        configure(GP, false, Map.of(CelestrakGpAdapter.MEMBERS_PARAM, List.of()));
        serve(AdapterFixtures.text(GP, "stations.csv"));
        assertThat(fetch(GP).success()).isTrue();
    }

    @AfterEach
    void tearDown() {
        configure(ID, false, Map.of());
        await().atMost(WAIT).until(() -> !runner.running().contains(ID));
    }

    private static void serve(String body) {
        UPSTREAM.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlEqualTo(ACTIVE))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "text/plain").withBody(body)));
    }

    private void enable() {
        configure(ID, true, Map.of(SatellitePassesGenerator.NORAD_PARAM, List.of("25544"),
                SatellitePassesGenerator.PLACES_PARAM, List.of(PLACE)));
        await().atMost(WAIT).until(() -> runner.running().contains(ID));
        idle();
    }

    private void idle() {
        assertThat(runner.awaitIdle(ID, WAIT)).isTrue();
    }

    private List<String> live() {
        return jdbc.queryForList("select external_id from record_head where source_id = ? and not tombstone"
                + " order by event_time", String.class, ID);
    }

    @Test
    @DisplayName("enabling it predicts the ISS passes over the place for 48 h as status predicted")
    void passesForPlace() {
        enable();
        List<String> ids = live();
        assertThat(ids).hasSize(6).allMatch(id -> id.startsWith("place:" + PLACE + ":25544:"));
        JsonNode first = JSON.readTree(jdbc.queryForObject("select payload::text from record_head where source_id = ?"
                + " and external_id = ?", String.class, ID, ids.get(0)));
        assertThat(first.get("status").asString()).isEqualTo("predicted");
        assertThat(first.get("satellite").asString()).isEqualTo("ISS (ZARYA)");
        assertThat(first.get("aos").asString()).isEqualTo("2026-10-13T12:06:30Z");
        assertThat(first.get("maxElevationDeg").asDouble()).isBetween(50.6, 51.0);
        assertThat(first.get("imminent").asBoolean()).isFalse();
        String geomType = jdbc.queryForObject("select GeometryType(geom) from record_live where source_id = ? and"
                + " external_id = ?", String.class, ID, ids.get(0));
        assertThat(geomType).isIn("LINESTRING", "MULTILINESTRING");
    }

    @Test
    @DisplayName("an element refresh within rounding makes no new pass version")
    void refreshWithinRounding() {
        enable();
        int before = versions(ID, (ai.foresignal.ingest.PersistReason) null);
        String refreshed = AdapterFixtures.text(GP, "stations.csv")
                .replace("ISS (ZARYA),1998-067A,2026-10-13T02:41:20.970528,", "ISS (ZARYA),1998-067A,2026-10-13T02:41:20.970529,");
        serve(refreshed);
        clock.setInstant(T.plusSeconds(60));
        assertThat(fetch(GP).persisted()).isEqualTo(1);
        idle();
        assertThat(versions(ID, (ai.foresignal.ingest.PersistReason) null)).isEqualTo(before);
    }

    @Test
    @DisplayName("a pass is tombstoned 10 minutes after its LOS; it turns imminent 15 minutes before AOS")
    void tombstonedAfterLos() {
        enable();
        String first = live().get(0);
        clock.setInstant(Instant.parse("2026-10-13T11:55:00Z"));
        runner.tickNow(ID);
        idle();
        JsonNode payload = JSON.readTree(jdbc.queryForObject("select payload::text from record_head where source_id = ?"
                + " and external_id = ?", String.class, ID, first));
        assertThat(payload.get("imminent").asBoolean()).isTrue();
        assertThat(versions(ID, first)).isEqualTo(2);

        clock.setInstant(Instant.parse("2026-10-13T12:24:00Z"));
        runner.tickNow(ID);
        idle();
        assertThat(tombstoned(ID, first)).isTrue();
        // The 11:55 full recompute re-anchors the 48 h horizon at 11:55, so one more future pass enters
        // the window than the 06:00 set: six live passes, the tombstoned first one gone.
        assertThat(live()).doesNotContain(first).hasSize(6);
    }
}
