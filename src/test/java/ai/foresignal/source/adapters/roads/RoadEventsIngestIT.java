package ai.foresignal.source.adapters.roads;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
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
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The road-event sources end to end through the real fetch path and ingest writer (prompt 074): the Digitraffic
 * road-work fixture ingests one version per current situation with {@code validFrom}/{@code validUntil} from its
 * schedule; a situation absent from two consecutive fetches is tombstoned once; an Open511 {@code ARCHIVED} event is
 * tombstoned at once; every road-event source is registered, and disabled under the test profile.
 *
 * <p>The fixture's situation ids and the {@code drivebc.ca/IT-…} ids are used by no other test, so per-entity history
 * holds in the shared context.
 */
class RoadEventsIngestIT extends WeatherIngestSupport {

    static final String DT = DigitrafficRoadAdapter.ID;
    static final String DT_PATH = "/tie.digitraffic.fi/api/traffic-message/v2/roadworks";
    static final String OPEN511 = "open511-drivebc";
    static final String OPEN511_PATH = "/api.open511.gov.bc.ca/events";
    static final String FIRST = "GUID50472288";
    static final JsonMapper JSON = JsonMapper.builder().build();

    @AfterEach
    void disable() {
        configure(DT, false, Map.of());
        configure(OPEN511, false, Map.of());
    }

    private void serve(String path, byte[] body) {
        UPSTREAM.stubFor(get(urlPathEqualTo(path)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body)));
    }

    /** The road-work fixture without the situation {@code id}. */
    private static byte[] roadworksWithout(String id) {
        ObjectNode root = (ObjectNode) JSON.readTree(AdapterFixtures.bytes("digitraffic-road", "roadworks.json"));
        ArrayNode features = (ArrayNode) root.get("features");
        for (int i = features.size() - 1; i >= 0; i--) {
            if (id.equals(features.get(i).path("properties").path("situationId").asString())) {
                features.remove(i);
            }
        }
        return JSON.writeValueAsBytes(root);
    }

    @Test
    @DisplayName("every road-event source is registered, disabled under the test profile, and emits road-event")
    void registered() {
        // The test profile seeds every source disabled (foresignal.sources.default-enable=false, prompt 146); in
        // production the ones needing no key or parameter start enabled (prompt 145).
        assertThat(sources.list()).filteredOn(s -> s.id().startsWith("wzdx-") || s.id().startsWith("open511-")
                        || List.of("digitraffic-road", "ndw-situations", "ndw-srti").contains(s.id()))
                .hasSize(35)
                .allSatisfy(s -> assertThat(s.enabled()).as(s.id()).isFalse());
    }

    @Test
    @DisplayName("Digitraffic road works: 14 versions with schedules; absent twice → one tombstone")
    void digitraffic() {
        clock.setInstant(Instant.parse("2026-10-13T19:00:00Z"));
        configure(DT, true, Map.of(DigitrafficRoadAdapter.TYPES_PARAM, List.of("ROAD_WORK")));
        int before = versions(DT, (ai.foresignal.ingest.PersistReason) null);
        serve(DT_PATH, AdapterFixtures.bytes("digitraffic-road", "roadworks.json"));
        FetchOutcome first = fetch(DT);
        assertThat(first.success()).isTrue();
        assertThat(versions(DT, (ai.foresignal.ingest.PersistReason) null) - before).isEqualTo(14);
        assertThat(head("valid_from", DT, FIRST)).isEqualTo(Instant.parse("2026-10-13T17:30:00Z"));
        assertThat(head("valid_until_explicit", DT, FIRST)).isEqualTo(Instant.parse("2026-10-14T03:00:00Z"));
        assertThat(jdbc.queryForObject("select payload->>'category' from record_head where source_id = ? and"
                + " external_id = ?", String.class, DT, FIRST)).isEqualTo("roadwork");
        assertThat(jdbc.queryForObject("select count(*) from record_head where source_id = ?"
                + " and (payload -> 'contact') is not null", Integer.class, DT)).isZero();
        assertThat(tombstoned(DT, "GUID50472247")).as("closed early: never stored").isNull();

        clock.advance(Duration.ofMinutes(5));
        serve(DT_PATH, roadworksWithout(FIRST));
        assertThat(fetch(DT).success()).isTrue();
        assertThat(tombstoned(DT, FIRST)).as("one miss is not enough").isFalse();

        clock.advance(Duration.ofMinutes(5));
        assertThat(fetch(DT).success()).isTrue();
        assertThat(tombstoned(DT, FIRST)).isTrue();
        int afterTombstone = versions(DT, FIRST);

        clock.advance(Duration.ofMinutes(5));
        assertThat(fetch(DT).success()).isTrue();
        assertThat(versions(DT, FIRST)).as("tombstoned once").isEqualTo(afterTombstone);
        assertThat(tombstoned(DT, "GUID50472287")).isFalse();
    }

    @Test
    @DisplayName("Open511: an event that turns ARCHIVED is tombstoned at once; schedules fill validFrom/validUntil")
    void open511Archived() {
        clock.setInstant(Instant.parse("2026-10-13T19:00:00Z"));
        configure(OPEN511, true, Map.of());
        String active = """
                {"events": [{"id": "drivebc.ca/IT-1", "status": "%s", "event_type": "INCIDENT", "severity": "MAJOR",
                 "headline": "INCIDENT", "description": "Crash on Highway 1. Expect delays.",
                 "updated": "%s", "roads": [{"name": "Highway 1", "direction": "BOTH", "state": "CLOSED"}],
                 "schedule": {"intervals": ["2026-10-13T18:00/2026-10-13T23:00"]},
                 "geography": {"type": "Point", "coordinates": [-123.1, 49.2]}},
                 {"id": "drivebc.ca/IT-2", "status": "ACTIVE", "event_type": "CONSTRUCTION",
                 "updated": "2026-10-13T10:00:00-07:00",
                 "geography": {"type": "Point", "coordinates": [-123.2, 49.3]}}], "pagination": {"offset": "0"}}""";
        serve(OPEN511_PATH, active.formatted("ACTIVE", "2026-10-13T11:00:00-07:00").getBytes(StandardCharsets.UTF_8));
        assertThat(fetch(OPEN511).success()).isTrue();
        assertThat(tombstoned(OPEN511, "drivebc.ca/IT-1")).isFalse();
        assertThat(head("valid_from", OPEN511, "drivebc.ca/IT-1")).isEqualTo(Instant.parse("2026-10-13T18:00:00Z"));
        assertThat(head("valid_until_explicit", OPEN511, "drivebc.ca/IT-1"))
                .isEqualTo(Instant.parse("2026-10-13T23:00:00Z"));
        assertThat(jdbc.queryForObject("select payload->>'isClosure' from record_head where source_id = ? and"
                + " external_id = 'drivebc.ca/IT-1'", String.class, OPEN511)).isEqualTo("true");

        clock.advance(Duration.ofMinutes(5));
        serve(OPEN511_PATH, active.formatted("ARCHIVED", "2026-10-13T11:30:00-07:00")
                .getBytes(StandardCharsets.UTF_8));
        assertThat(fetch(OPEN511).success()).isTrue();
        assertThat(tombstoned(OPEN511, "drivebc.ca/IT-1")).as("immediately, without waiting for absence").isTrue();
        assertThat(tombstoned(OPEN511, "drivebc.ca/IT-2")).isFalse();
    }
}
