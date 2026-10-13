package ai.foresignal.source.adapters;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.FetchExecutor;
import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.adsblol.AdsbLolMilAdapter;
import ai.foresignal.source.adapters.opensky.OpenSkyStatesAdapter;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * The aircraft sources end to end on the v1.2 pipeline (prompt 062): real fetches through the fetch executor
 * and the real ingest writer, against WireMock, under the SAMPLED TRACK policy (Core-Data-Model §5.2).
 *
 * <p>Fetches are manual ({@link FetchExecutor#execute(String, FetchTrigger) execute(id, MANUAL)} runs while the
 * source is disabled); the budget is raised for the test. adsb.lol bodies are built per poll so {@code now}
 * follows the {@link MutableClock}; no record is seeded with SQL (Core-Data-Model §17).
 */
@IntegrationTest
@Import(MutableClock.TestConfig.class)
class AircraftIngestIT extends WireMockSupport {

    private static final String MIL = AdsbLolMilAdapter.ID;
    private static final String OPENSKY = OpenSkyStatesAdapter.ID;
    private static final Instant T0 = Instant.parse("2026-10-13T12:00:00Z");

    @DynamicPropertySource
    static void upstream(DynamicPropertyRegistry registry) {
        registry.add(AdsbLolMilAdapter.BASE_URL_PROPERTY, WIREMOCK::baseUrl);
        registry.add(OpenSkyStatesAdapter.BASE_URL_PROPERTY, WIREMOCK::baseUrl);
        registry.add(OpenSkyStatesAdapter.AUTH_URL_PROPERTY, WIREMOCK::baseUrl);
    }

    @Autowired private FetchExecutor executor;
    @Autowired private SourceService sources;
    @Autowired private BackoffRegistry backoff;
    @Autowired private MutableClock clock;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        backoff.onSuccess(MIL);
        backoff.onSuccess(OPENSKY);
        sources.update(MIL, new SourceSettingsUpdate(false, null, null, Budget.of(600, 100_000), null, false, null, false));
        sources.update(OPENSKY, new SourceSettingsUpdate(false, null, null, Budget.of(600, 100_000), null, false, null,
                false));
        jdbc.update("delete from fetch_log");
    }

    @AfterEach
    void tearDown() {
        backoff.onSuccess(MIL);
        backoff.onSuccess(OPENSKY);
    }

    // ---------- harness ----------

    /** One readsb aircraft entry, seen just now. */
    private record Ac(String hex, double lat, double lon, String squawk) {

        String json() {
            return String.format(Locale.ROOT,
                    "{\"hex\":\"%s\",\"type\":\"adsb_icao\",\"flight\":\"%s  \",\"r\":\"%s\",\"t\":\"C17\","
                            + "\"dbFlags\":1,\"alt_baro\":24000,\"gs\":0.0,\"track\":90.0,\"squawk\":\"%s\","
                            + "\"category\":\"A5\",\"nac_p\":10,\"nic\":8,\"lat\":%.6f,\"lon\":%.6f,"
                            + "\"seen_pos\":0.0,\"seen\":0.0,\"messages\":%d,\"rssi\":-20.1}",
                    hex, "RCH" + hex.substring(0, 3).toUpperCase(Locale.ROOT), "N" + hex, squawk, lat, lon,
                    Math.abs(hex.hashCode()) % 100_000);
        }
    }

    /** Serves a /v2/mil document whose {@code now} is the clock's instant. */
    private void serveMil(List<Ac> aircraft) {
        StringBuilder body = new StringBuilder("{\"ac\":[");
        for (int i = 0; i < aircraft.size(); i++) {
            body.append(i == 0 ? "" : ",").append(aircraft.get(i).json());
        }
        long now = clock.instant().toEpochMilli();
        body.append("],\"msg\":\"No error\",\"now\":").append(now).append(",\"total\":").append(aircraft.size())
                .append(",\"ctime\":").append(now).append(",\"ptime\":0}");
        WIREMOCK.stubFor(WireMock.get(urlEqualTo("/v2/mil")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json").withBody(body.toString())));
    }

    private FetchOutcome fetch(String id) {
        return executor.execute(id, FetchTrigger.MANUAL);
    }

    private int versions(String id, PersistReason reason) {
        Integer count = reason == null
                ? jdbc.queryForObject("select count(*) from record_version where source_id = ?", Integer.class, id)
                : jdbc.queryForObject("select count(*) from record_version where source_id = ? and persist_reason = ?",
                        Integer.class, id, reason.code());
        return count == null ? 0 : count;
    }

    private int versionsOf(String id, String externalId, PersistReason reason) {
        Integer count = jdbc.queryForObject(
                "select count(*) from record_version where source_id = ? and external_id = ? and persist_reason = ?",
                Integer.class, id, externalId, reason.code());
        return count == null ? 0 : count;
    }

    /** {@code km} kilometers north of {@code lat}. */
    private static double north(double lat, double km) {
        return lat + km / 111.195;
    }

    private static final Ac A = new Ac("ae1234", 40.0, -100.0, "1000");
    private static final Ac B = new Ac("ae5678", 41.0, -101.0, "2000");
    private static final Ac C = new Ac("ae9abc", 42.0, -102.0, "3000");

    // ---------- tests ----------

    @Test
    @DisplayName("the first poll writes FIRST versions for every aircraft")
    void firstPoll() {
        serveMil(List.of(A, B, C));

        FetchOutcome outcome = fetch(MIL);

        assertThat(outcome.success()).isTrue();
        assertThat(outcome.byReason().get(PersistReason.FIRST)).isEqualTo(3);
        assertThat(versions(MIL, PersistReason.FIRST)).isEqualTo(3);
        Map<String, Object> row = jdbc.queryForMap(
                "select status, licence, records_seen from fetch_log where source_id = ?", MIL);
        assertThat(row.get("status")).isEqualTo("OK");
        assertThat(row.get("licence")).isEqualTo("ODbL-1.0");
        String title = jdbc.queryForObject("select title from record_head where source_id = ? and external_id = ?",
                String.class, MIL, "ae1234");
        assertThat(title).isEqualTo("RCHAE1");
        Instant observed = jdbc.queryForObject(
                "select observed_at from record_live where source_id = ? and external_id = ?",
                java.sql.Timestamp.class, MIL, "ae1234").toInstant();
        assertThat(observed).isEqualTo(T0);
    }

    @Test
    @DisplayName("an identical second poll writes no version (TOUCH or LIVE)")
    void identicalPoll() {
        serveMil(List.of(A, B));
        fetch(MIL);

        FetchOutcome second = fetch(MIL);

        assertThat(second.success()).isTrue();
        assertThat(second.persisted()).isZero();
        assertThat(versions(MIL, null)).isEqualTo(2);
    }

    @Test
    @DisplayName("a 2 km move after 60 s is a SAMPLE; the unmoved aircraft writes nothing")
    void sample() {
        serveMil(List.of(A, B));
        fetch(MIL);

        clock.advance(Duration.ofSeconds(60));
        serveMil(List.of(new Ac(A.hex(), north(A.lat(), 2.0), A.lon(), A.squawk()), B));
        FetchOutcome outcome = fetch(MIL);

        assertThat(outcome.persisted()).isEqualTo(1);
        assertThat(outcome.byReason().get(PersistReason.SAMPLE)).isEqualTo(1);
        assertThat(versionsOf(MIL, A.hex(), PersistReason.SAMPLE)).isEqualTo(1);
        assertThat(versions(MIL, null)).isEqualTo(3);
    }

    @Test
    @DisplayName("a squawk change is SIGNIFICANT at once")
    void significant() {
        serveMil(List.of(A));
        fetch(MIL);

        clock.advance(Duration.ofSeconds(5));
        serveMil(List.of(new Ac(A.hex(), A.lat(), A.lon(), "7700")));
        FetchOutcome outcome = fetch(MIL);

        assertThat(outcome.byReason().get(PersistReason.SIGNIFICANT)).isEqualTo(1);
        assertThat(versionsOf(MIL, A.hex(), PersistReason.SIGNIFICANT)).isEqualTo(1);
    }

    @Test
    @DisplayName("an aircraft absent past its 6-minute TTL and then back is REAPPEAR")
    void reappear() {
        serveMil(List.of(A, C));
        fetch(MIL);

        clock.advance(Duration.ofSeconds(60));
        serveMil(List.of(A));
        fetch(MIL);

        clock.advance(Duration.ofMinutes(7));
        serveMil(List.of(A, C));
        FetchOutcome outcome = fetch(MIL);

        assertThat(versionsOf(MIL, C.hex(), PersistReason.REAPPEAR)).isEqualTo(1);
        assertThat(outcome.byReason().get(PersistReason.REAPPEAR)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("an OpenSky quota deferral writes a REFUSED fetch_log row and sends no states request")
    void quotaDeferredRow() {
        WIREMOCK.stubFor(WireMock.get(urlEqualTo("/api/states/all?extended=1")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withHeader("X-Rate-Limit-Remaining", "43")
                .withBody(AdapterFixtures.bytes(OPENSKY, "states-bbox.json"))));
        clock.setInstant(Instant.parse("2026-10-13T12:23:30Z"));

        FetchOutcome first = fetch(OPENSKY);
        assertThat(first.success()).isTrue();
        assertThat(versions(OPENSKY, PersistReason.FIRST)).isEqualTo(30);

        clock.advance(Duration.ofMinutes(5));
        FetchOutcome deferred = fetch(OPENSKY);

        assertThat(deferred.success()).isFalse();
        assertThat(deferred.status()).isEqualTo(FetchStatus.REFUSED);
        Map<String, Object> row = jdbc.queryForMap(
                "select status, error from fetch_log where source_id = ? order by id desc limit 1", OPENSKY);
        assertThat(row.get("status")).isEqualTo("REFUSED");
        assertThat((String) row.get("error")).startsWith(FetchExecutor.QUOTA_DEFERRED_ERROR)
                .contains("2026-10-14T00:00:00Z");
        WIREMOCK.verify(1, getRequestedFor(urlPathEqualTo("/api/states/all")));
        assertThat(jdbc.queryForObject("select consecutive_failures from source_health where source_id = ?",
                Integer.class, OPENSKY)).isZero();
    }
}
