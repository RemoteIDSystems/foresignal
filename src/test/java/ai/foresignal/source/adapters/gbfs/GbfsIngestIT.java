package ai.foresignal.source.adapters.gbfs;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.testsupport.weather.WeatherIngestSupport;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * {@code gbfs-boulder-bcycle} end to end through the real fetch path and ingest writer (prompt 071): the first poll
 * writes FIRST versions; a bike count from 5 to 4 is a live update with no version; 3 to 2 ({@code ok} to
 * {@code low}) and {@code isRenting} false are SIGNIFICANT; unchanged polls are touches, and a keyframe follows after
 * 20 minutes.
 *
 * <p>Each test uses its own station, so the per-entity {@code observedAt} order holds across tests that share the
 * cached context; every status document has {@code ttl 0}, so no poll is skipped by the GBFS ttl.
 */
class GbfsIngestIT extends WeatherIngestSupport {

    static final String ID = "gbfs-boulder-bcycle";
    static final String INFO = "/gbfs.bcycle.com/bcycle_boulder/station_information.json";
    static final String STATUS = "/gbfs.bcycle.com/bcycle_boulder/station_status.json";

    static final String INFORMATION = """
            {"last_updated": 1791910000, "ttl": 60, "version": "1.1", "data": {"stations": [
             {"station_id": "L1", "name": "Live Station", "address": "1 Pearl St", "lat": 40.018, "lon": -105.28, "capacity": 12},
             {"station_id": "R1", "name": "Renting Station", "lat": 40.019, "lon": -105.27, "capacity": 10},
             {"station_id": "K1", "name": "Keyframe Station", "lat": 40.02, "lon": -105.26, "capacity": 8}]}}""";

    @Autowired private SourceRegistry registry;

    @BeforeEach
    void setUp() {
        configure(ID, false, Map.of());
        UPSTREAM.stubFor(get(urlEqualTo(INFO)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(INFORMATION)));
    }

    /** Serves one station's status, reported at the clock's current second. */
    private void serve(String station, int bikes, int docks, boolean renting) {
        long now = clock.instant().getEpochSecond();
        UPSTREAM.stubFor(get(urlEqualTo(STATUS)).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody("""
                        {"last_updated": %d, "ttl": 0, "version": "1.1", "data": {"stations": [
                         {"station_id": "%s", "num_bikes_available": %d, "num_docks_available": %d,
                          "is_installed": 1, "is_renting": %d, "is_returning": 1, "last_reported": %d}]}}"""
                        .formatted(now, station, bikes, docks, renting ? 1 : 0, now))));
    }

    private List<Integer> reasons(String externalId) {
        return jdbc.queryForList("select persist_reason from record_version where source_id = ? and external_id = ?"
                + " order by seq", Integer.class, ID, externalId);
    }

    private String headPayload(String externalId, String key) {
        return jdbc.queryForObject("select payload->>'" + key + "' from record_head where source_id = ? and"
                + " external_id = ?", String.class, ID, externalId);
    }

    private String livePayload(String externalId, String key) {
        // A live-only (kinematic) update lives in record_live.kinematic; record_live has no payload column.
        return jdbc.queryForObject("select kinematic->>'" + key + "' from record_live where source_id = ? and"
                + " external_id = ?", String.class, ID, externalId);
    }

    private static int code(PersistReason reason) {
        return reason.code();
    }

    @Test
    @DisplayName("FIRST; 5 -> 4 bikes is live only; 3 -> 2 (ok -> low) is SIGNIFICANT")
    void liveAndSignificant() {
        clock.setInstant(Instant.parse("2026-10-13T18:00:00Z"));
        int before = versions(ID, "L1");
        serve("L1", 5, 7, true);
        FetchOutcome first = fetch(ID);
        assertThat(first.success()).isTrue();
        assertThat(reasons("L1")).last().isEqualTo(code(PersistReason.FIRST));
        assertThat(headPayload("L1", "availability")).isEqualTo("ok");
        assertThat(headPayload("L1", "name")).isEqualTo("Live Station");

        clock.advance(Duration.ofSeconds(60));
        serve("L1", 4, 8, true);
        FetchOutcome live = fetch(ID);
        assertThat(live.success()).isTrue();
        assertThat(live.persisted()).isZero();
        assertThat(live.live()).isEqualTo(1);
        assertThat(versions(ID, "L1") - before).isEqualTo(1);
        assertThat(livePayload("L1", "bikesAvailable")).isEqualTo("4");
        assertThat(headPayload("L1", "bikesAvailable")).as("the version keeps the first counts").isEqualTo("5");

        clock.advance(Duration.ofSeconds(60));
        serve("L1", 3, 9, true);
        assertThat(fetch(ID).persisted()).isZero();

        clock.advance(Duration.ofSeconds(60));
        serve("L1", 2, 10, true);
        FetchOutcome low = fetch(ID);
        assertThat(low.persisted()).isEqualTo(1);
        assertThat(reasons("L1")).last().isEqualTo(code(PersistReason.SIGNIFICANT));
        assertThat(headPayload("L1", "availability")).isEqualTo("low");
        assertThat(versions(ID, "L1") - before).isEqualTo(2);
    }

    @Test
    @DisplayName("isRenting false is SIGNIFICANT (out-of-service)")
    void rentingStops() {
        clock.setInstant(Instant.parse("2026-10-13T19:00:00Z"));
        serve("R1", 6, 4, true);
        assertThat(fetch(ID).success()).isTrue();

        clock.advance(Duration.ofSeconds(60));
        serve("R1", 6, 4, false);
        FetchOutcome stopped = fetch(ID);
        assertThat(stopped.persisted()).isEqualTo(1);
        assertThat(reasons("R1")).last().isEqualTo(code(PersistReason.SIGNIFICANT));
        assertThat(headPayload("R1", "isRenting")).isEqualTo("false");
        assertThat(headPayload("R1", "availability")).isEqualTo("out-of-service");
    }

    @Test
    @DisplayName("unchanged polls are touches; a keyframe is written after 20 minutes")
    void touchesAndKeyframe() {
        clock.setInstant(Instant.parse("2026-10-13T20:00:00Z"));
        serve("K1", 6, 2, true);
        assertThat(fetch(ID).success()).isTrue();

        // A count change after the version: the keyframe then carries the newer counts.
        clock.advance(Duration.ofSeconds(60));
        serve("K1", 5, 3, true);
        assertThat(fetch(ID).live()).isEqualTo(1);

        int touches = 0;
        for (int i = 0; i < 3; i++) {
            clock.advance(Duration.ofSeconds(60));
            serve("K1", 5, 3, true);
            FetchOutcome same = fetch(ID);
            assertThat(same.persisted()).isZero();
            touches += same.touched() + same.touchesThrottled();
        }
        assertThat(touches).isEqualTo(3);
        assertThat(reasons("K1")).containsExactly(code(PersistReason.FIRST));

        clock.setInstant(Instant.parse("2026-10-13T20:20:30Z"));
        serve("K1", 5, 3, true);
        FetchOutcome keyframe = fetch(ID);
        assertThat(keyframe.persisted()).isEqualTo(1);
        List<Integer> reasons = reasons("K1");
        assertThat(reasons).hasSize(2).first().isEqualTo(code(PersistReason.FIRST));
        assertThat(reasons.get(1)).as("the keyframe row: KEYFRAME, or HEARTBEAT when the live row is clean")
                .isIn(code(PersistReason.KEYFRAME), code(PersistReason.HEARTBEAT));
        assertThat(headPayload("K1", "bikesAvailable")).isEqualTo("5");
    }

    @Test
    @DisplayName("the registry holds every reviewed system as STANDARD + LATEST")
    void registered() {
        for (GbfsSystem system : GbfsSystems.REVIEWED) {
            assertThat(registry.find(system.sourceId())).as(system.sourceId()).isPresent();
        }
        var descriptor = registry.find(ID).orElseThrow().descriptor();
        assertThat(descriptor.historyTier()).isEqualTo(HistoryTier.STANDARD);
        assertThat(descriptor.defaultIngestPolicy()).isInstanceOf(IngestPolicy.Latest.class);
        assertThat(descriptor.followRedirects()).isFalse();
    }
}
