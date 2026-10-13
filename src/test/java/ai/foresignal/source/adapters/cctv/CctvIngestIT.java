package ai.foresignal.source.adapters.cctv;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code cctv-caltrans-d4} end to end through the real fetch path and ingest writer (prompt 072): enabling it and
 * fetching writes one FIRST version per camera; an identical re-fetch writes no new version; a camera removed from
 * the catalog is tombstoned exactly once, after its second missing fetch.
 *
 * <p>The camera codes ({@code TVIT…}) are used by no other test, so the per-entity history holds in the shared
 * context.
 */
class CctvIngestIT extends WeatherIngestSupport {

    static final String ID = "cctv-caltrans-d4";
    static final String PATH = "/cwwp2.dot.ca.gov/data/d4/cctv/cctvStatusD04.json";
    static final List<String> CAMERAS = List.of("TVIT1", "TVIT2", "TVIT3");

    @AfterEach
    void disable() {
        configure(ID, false, Map.of());
    }

    /** Serves a district file listing {@code codes}. */
    private void serve(List<String> codes) {
        List<String> rows = new ArrayList<>();
        int i = 0;
        for (String code : codes) {
            i++;
            rows.add("""
                    {"cctv": {"index": "%d", "location": {"district": "4", "locationName": "%s -- I-80 : Test %d",
                     "nearbyPlace": "Berkeley", "longitude": "-122.3%d", "latitude": "37.8%d", "direction": "East",
                     "route": "I-80"}, "inService": "true", "imageData": {"streamingVideoURL": "", "static": {
                     "currentImageUpdateFrequency": "5", "currentImageURL":
                     "https://cwwp2.dot.ca.gov/data/d4/cctv/image/%s/%s.jpg"}}}}"""
                    .formatted(i, code, i, i, i, code.toLowerCase(), code.toLowerCase()));
        }
        UPSTREAM.stubFor(get(urlEqualTo(PATH)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody("{\"data\": [" + String.join(",", rows)
                        + "]}")));
    }

    private int total() {
        int sum = 0;
        for (String id : CAMERAS) {
            sum += versions(ID, id);
        }
        return sum;
    }

    @Test
    @DisplayName("enable and fetch: N FIRST versions; identical re-fetch: none; a camera missing twice: one tombstone")
    void ingest() {
        clock.setInstant(Instant.parse("2026-10-13T19:00:00Z"));
        configure(ID, true, Map.of());
        int before = total();
        serve(CAMERAS);
        FetchOutcome first = fetch(ID);
        assertThat(first.success()).isTrue();
        assertThat(total() - before).isEqualTo(3);
        for (String id : CAMERAS) {
            assertThat(jdbc.queryForObject("select persist_reason from record_version where source_id = ? and"
                    + " external_id = ? order by seq desc limit 1", Integer.class, ID, id))
                    .isEqualTo(PersistReason.FIRST.code());
            assertThat(tombstoned(ID, id)).isFalse();
        }
        assertThat(jdbc.queryForObject("select payload->>'frameAccess' from record_head where source_id = ? and"
                + " external_id = 'TVIT1'", String.class, ID)).isEqualTo("direct");

        clock.advance(Duration.ofMinutes(15));
        FetchOutcome same = fetch(ID);
        assertThat(same.success()).isTrue();
        assertThat(same.persisted()).isZero();
        assertThat(total() - before).as("identical re-fetch").isEqualTo(3);

        clock.advance(Duration.ofMinutes(15));
        serve(List.of("TVIT1", "TVIT2"));
        assertThat(fetch(ID).success()).isTrue();
        assertThat(tombstoned(ID, "TVIT3")).as("one miss is not enough").isFalse();
        assertThat(total() - before).isEqualTo(3);

        clock.advance(Duration.ofMinutes(15));
        assertThat(fetch(ID).success()).isTrue();
        assertThat(tombstoned(ID, "TVIT3")).isTrue();
        assertThat(total() - before).isEqualTo(4);

        clock.advance(Duration.ofMinutes(15));
        assertThat(fetch(ID).success()).isTrue();
        assertThat(total() - before).as("tombstoned once").isEqualTo(4);
        assertThat(tombstoned(ID, "TVIT1")).isFalse();
    }
}
