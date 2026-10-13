package ai.foresignal.source.adapters.celestrak;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code celestrak-gp} through the real fetch path and ingest writer (prompt 064, Core-Data-Model §17): identical
 * elements are touches with no version, a new EPOCH is version 2, an older EPOCH is LATE, and the "not updated"
 * 403 is a NOT_MODIFIED run.
 */
class SpaceIngestIT extends WeatherIngestSupport {

    private static final String ID = CelestrakGpAdapter.ID;
    private static final String ACTIVE = "/NORAD/elements/gp.php?GROUP=active&FORMAT=csv";
    private static final Instant NOW = Instant.parse("2026-10-13T13:00:00Z");

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        configure(ID, false, Map.of(CelestrakGpAdapter.MEMBERS_PARAM, List.of()));
    }

    private static void serve(String body) {
        UPSTREAM.stubFor(get(urlEqualTo(ACTIVE)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/plain; charset=UTF-8").withBody(body)));
    }

    private static String stations() {
        return AdapterFixtures.text(ID, "stations.csv");
    }

    @Test
    @DisplayName("first fetch: one FIRST version per object; identical elements again: touches, no versions")
    void identicalIsTouch() {
        serve(stations());
        FetchOutcome first = fetch(ID);
        assertThat(first.success()).isTrue();
        assertThat(versions(ID, PersistReason.FIRST)).isEqualTo(21);

        clock.setInstant(NOW.plusSeconds(7200));
        FetchOutcome again = fetch(ID);
        assertThat(again.success()).isTrue();
        assertThat(again.persisted()).isZero();
        assertThat(versions(ID, (PersistReason) null)).isEqualTo(21);
        String title = jdbc.queryForObject("select title from record_head where source_id = ? and external_id = '25544'",
                String.class, ID);
        assertThat(title).isEqualTo("ISS (ZARYA)");
        assertThat(jdbc.queryForObject("select count(*) from record_head where source_id = ? and external_id = '100882'",
                Integer.class, ID)).isEqualTo(1);
    }

    @Test
    @DisplayName("a new EPOCH is version 2 of that object only; an older EPOCH is dropped as LATE")
    void newEpochIsVersionTwo() {
        serve(stations());
        fetch(ID);
        String newer = stations().replace("ISS (ZARYA),1998-067A,2026-10-13T02:41:20.970528,15.48846742",
                "ISS (ZARYA),1998-067A,2026-10-13T11:02:44.123456,15.48850001");
        assertThat(newer).isNotEqualTo(stations());
        serve(newer);
        clock.setInstant(NOW.plusSeconds(7200));
        FetchOutcome outcome = fetch(ID);
        assertThat(outcome.persisted()).isEqualTo(1);
        assertThat(versions(ID, "25544")).isEqualTo(2);
        assertThat(versions(ID, (PersistReason) null)).isEqualTo(22);

        String older = stations().replace("ISS (ZARYA),1998-067A,2026-10-13T02:41:20.970528,15.48846742",
                "ISS (ZARYA),1998-067A,2026-10-12T20:00:00.000000,15.48840000");
        serve(older);
        clock.setInstant(NOW.plusSeconds(14400));
        fetch(ID);
        assertThat(versions(ID, "25544")).isEqualTo(2);
    }

    @Test
    @DisplayName("CelesTrak's 'not updated' 403 is a NOT_MODIFIED run, not a failure")
    void notUpdated() {
        UPSTREAM.stubFor(get(urlEqualTo(ACTIVE)).willReturn(aResponse().withStatus(403)
                .withBody("GP data has not updated since your last successful download".getBytes(StandardCharsets.UTF_8))));
        FetchOutcome outcome = fetch(ID);
        assertThat(outcome.status()).isEqualTo(FetchStatus.NOT_MODIFIED);
        assertThat(backoff.isInBackoff(ID)).isFalse();
    }
}
