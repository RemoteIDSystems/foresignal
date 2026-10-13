package ai.foresignal.source.adapters.rail;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.collect.UpstreamClient;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.adapters.amtraker.AmtrakerAdapter;
import ai.foresignal.source.adapters.digitraffic.rail.DigitrafficRailAdapter;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The prompt 080 rail sources end to end through the real fetch path and ingest writer, against the shared stub
 * server ({@link WeatherIngestSupport}): every prompt 080 source registers disabled; an Amtraker train that moves less
 * than 150 m within {@code minInterval} writes no version, a heading change of at least 30° (Amtraker's compass moves
 * in 45° steps) writes a SAMPLE version, and an older position is LATE; a Digitraffic run stores every train and looks
 * one up.
 *
 * <p>Each test uses its own train id, so versions written by earlier tests in the shared database do not interfere.
 */
class RailIngestIT extends WeatherIngestSupport {

    static final String AMTRAKER = "/v3/trains";
    static final String LATEST = "/api/v1/train-locations/latest";

    @Autowired UpstreamClient client;

    @AfterEach
    void disable() {
        configure(AmtrakerAdapter.ID, false, Map.of());
        configure(DigitrafficRailAdapter.ID, false, Map.of());
    }

    /** One Amtraker train, stopped (no dead reckoning), at a position, heading and time. */
    private static String train(String id, double lat, double lon, String heading, String lastValTs) {
        return """
                {"9001":[{"dataSource":"amtraker-v3","routeName":"Sunset Limited","trainNum":"9001","trainID":"%s",\
                "lat":%s,"lon":%s,"trainTimely":"","heading":"%s","eventCode":"DEM","origCode":"NOL","destCode":"LAX",\
                "trainState":"Active","velocity":0,"lastValTS":"%s","provider":"Amtrak","providerShort":"AMTK"}]}
                """.formatted(id, lat, lon, heading, lastValTs);
    }

    private static void serveAmtraker(String body) {
        UPSTREAM.stubFor(get(urlPathEqualTo(AMTRAKER)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body.getBytes(StandardCharsets.UTF_8))));
    }

    private int samples(String externalId) {
        Integer count = jdbc.queryForObject("select count(*) from record_version where source_id = ? and external_id = ?"
                + " and persist_reason = ?", Integer.class, AmtrakerAdapter.ID, externalId, PersistReason.SAMPLE.code());
        return count == null ? 0 : count;
    }

    @Test
    @DisplayName("every prompt 080 source is registered and disabled")
    void registered() {
        List<String> ids = List.of("airplaneslive-mil", "adsbfi-mil", "adsbfi-region-1", "adsbfi-region-2",
                "adsbfi-region-3", "adsbfi-region-4", "digitraffic-rail", "amtraker");
        assertThat(sources.list()).filteredOn(s -> ids.contains(s.id())).hasSize(ids.size())
                .allSatisfy(s -> assertThat(s.enabled()).as(s.id()).isFalse());
    }

    @Test
    @DisplayName("amtraker: < 150 m within minInterval writes nothing; a 45° heading change is SAMPLE; older is LATE")
    void sampling() {
        String id = "9001-13";
        configure(AmtrakerAdapter.ID, false, Map.of());

        clock.setInstant(Instant.parse("2026-10-13T22:45:45Z"));
        serveAmtraker(train(id, 32.303674, -108.590358, "NW", "2026-10-13T16:45:40-06:00"));
        assertThat(fetch(AmtrakerAdapter.ID).success()).isTrue();
        assertThat(versions(AmtrakerAdapter.ID, id)).isEqualTo(1);

        // 10 s later, about 33 m on, same heading: inside minInterval and under 150 m, so no version.
        clock.setInstant(Instant.parse("2026-10-13T22:45:55Z"));
        serveAmtraker(train(id, 32.303974, -108.590358, "NW", "2026-10-13T16:45:50-06:00"));
        FetchOutcome small = fetch(AmtrakerAdapter.ID);
        assertThat(small.success()).isTrue();
        assertThat(versions(AmtrakerAdapter.ID, id)).isEqualTo(1);

        // 30 s after the first fix, NW (315°) to W (270°): the headingDeg threshold of 30° is exceeded.
        clock.setInstant(Instant.parse("2026-10-13T22:46:15Z"));
        serveAmtraker(train(id, 32.304074, -108.590458, "W", "2026-10-13T16:46:10-06:00"));
        assertThat(fetch(AmtrakerAdapter.ID).success()).isTrue();
        assertThat(versions(AmtrakerAdapter.ID, id)).isEqualTo(2);
        assertThat(samples(id)).isEqualTo(1);
        assertThat(jdbc.queryForObject("select (payload->>'headingDeg')::float from record_head where source_id = ?"
                + " and external_id = ?", Double.class, AmtrakerAdapter.ID, id)).isEqualTo(270.0);

        // A fix older than the head is LATE and changes nothing.
        clock.setInstant(Instant.parse("2026-10-13T22:46:20Z"));
        serveAmtraker(train(id, 32.31, -108.6, "S", "2026-10-13T16:46:00-06:00"));
        FetchOutcome late = fetch(AmtrakerAdapter.ID);
        assertThat(late.success()).isTrue();
        assertThat(late.late()).isEqualTo(1);
        assertThat(versions(AmtrakerAdapter.ID, id)).isEqualTo(2);
    }

    @Test
    @DisplayName("digitraffic-rail: every train is stored, one train is looked up, with the Digitraffic-User header")
    void digitraffic() {
        var state = client.stateStore(DigitrafficRailAdapter.ID);
        state.remove(DigitrafficRailAdapter.CACHE_KEY);
        state.remove(DigitrafficRailAdapter.LOOKUPS_KEY);
        configure(DigitrafficRailAdapter.ID, false, Map.of());
        clock.setInstant(Instant.parse("2026-10-13T22:50:46Z"));
        UPSTREAM.stubFor(get(urlPathEqualTo(LATEST)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("digitraffic-rail", "latest.json"))));
        UPSTREAM.stubFor(get(urlPathEqualTo("/api/v1/trains/2026-10-13/2210")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes("digitraffic-rail", "train-2026-10-14-2241.json"))));

        FetchOutcome outcome = fetch(DigitrafficRailAdapter.ID);

        assertThat(outcome.success()).isTrue();
        assertThat(outcome.recordsSeen()).isEqualTo(32);
        assertThat(jdbc.queryForObject("select payload->>'trainType' from record_head where source_id = ? and"
                + " external_id = ?", String.class, DigitrafficRailAdapter.ID, "2026-10-13:2210")).isEqualTo("T");
        assertThat(state.get(DigitrafficRailAdapter.LOOKUPS_KEY)).contains("2026-10-13:1");
        UPSTREAM.verify(1, getRequestedFor(urlPathEqualTo("/api/v1/trains/2026-10-13/2210"))
                .withHeader("Digitraffic-User", matching("Foresignal/.+")));
        UPSTREAM.verify(1, getRequestedFor(urlPathEqualTo(LATEST))
                .withHeader("Accept-Encoding", equalTo("gzip")));
    }
}
