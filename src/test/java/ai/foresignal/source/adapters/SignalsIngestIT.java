package ai.foresignal.source.adapters;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.collect.BackoffRegistry;
import ai.foresignal.collect.CollectorScheduler;
import ai.foresignal.collect.FetchExecutor;
import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.adapters.cdc.NwssWvalAdapter;
import ai.foresignal.source.adapters.cdc.NwssWvalParser;
import ai.foresignal.source.adapters.inat.InatObservationsAdapter;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Duration;
import java.time.Instant;
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
import org.springframework.test.context.TestPropertySource;

/**
 * The citizen-science and signal sources end to end on the v1.2 pipeline (prompt 095): real fetches through the
 * scheduler, the fetch executor and the writer, against WireMock fixtures (Core-Data-Model §17: records are
 * never seeded with raw SQL).
 */
@IntegrationTest
@Import(MutableClock.TestConfig.class)
@TestPropertySource(properties = "foresignal.collector.scheduled=true")
class SignalsIngestIT extends WireMockSupport {

    private static final String INAT = InatObservationsAdapter.ID;
    private static final String WVAL = NwssWvalAdapter.ID;
    private static final Instant NOW = Instant.parse("2026-10-13T12:00:00Z");

    @DynamicPropertySource
    static void upstreams(DynamicPropertyRegistry registry) {
        registry.add(InatObservationsAdapter.BASE_URL_PROPERTY, WIREMOCK::baseUrl);
        registry.add(NwssWvalAdapter.BASE_URL_PROPERTY, WIREMOCK::baseUrl);
    }

    @Autowired private CollectorScheduler scheduler;
    @Autowired private FetchExecutor executor;
    @Autowired private SourceService sources;
    @Autowired private BackoffRegistry backoff;
    @Autowired private MutableClock clock;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        backoff.onSuccess(INAT);
        backoff.onSuccess(WVAL);
        if (!scheduler.isRunning()) {
            scheduler.start();
        }
        scheduler.reconcileNow();
        await().atMost(Duration.ofSeconds(10)).until(() -> executor.activeFetches() == 0);
        jdbc.update("delete from fetch_log");
    }

    @AfterEach
    void tearDown() {
        backoff.onSuccess(INAT);
        backoff.onSuccess(WVAL);
    }

    private static void serveInat(String fixture) {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo("/v1/observations")).willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(AdapterFixtures.bytes(INAT, fixture))));
    }

    private static void serveWval() {
        WIREMOCK.stubFor(WireMock.get(urlPathEqualTo("/resource/" + NwssWvalParser.DATASET + ".json"))
                .willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody(AdapterFixtures.bytes(WVAL, "wval.json"))));
    }

    private void enable(String id, Map<String, Object> params) {
        sources.update(id, new SourceSettingsUpdate(true, null, params, Budget.of(600, 100_000), null, false, null,
                false));
    }

    private void disable(String id) {
        sources.update(id, SourceSettingsUpdate.enabled(false));
        await().atMost(Duration.ofSeconds(10)).until(() -> executor.activeFetches() == 0);
    }

    private int fetchRows(String id) {
        Integer count = jdbc.queryForObject("select count(*) from fetch_log where source_id = ?", Integer.class, id);
        return count == null ? 0 : count;
    }

    private int versions(String id, PersistReason reason) {
        Integer count = reason == null
                ? jdbc.queryForObject("select count(*) from record_version where source_id = ?", Integer.class, id)
                : jdbc.queryForObject("select count(*) from record_version where source_id = ? and persist_reason = ?",
                        Integer.class, id, reason.code());
        return count == null ? 0 : count;
    }

    @Test
    @DisplayName("an iNaturalist revision creates a SIGNIFICANT version and no payload stores a username")
    void inatRevision() {
        serveInat("observations.json");
        enable(INAT, Map.of(InatObservationsAdapter.AOI_PARAM, "30,-98,31,-97"));
        await().atMost(Duration.ofSeconds(15)).until(() -> fetchRows(INAT) >= 1);
        disable(INAT);
        int firstVersions = versions(INAT, null);
        assertThat(firstVersions).isGreaterThan(0);

        serveInat("observations-revised.json");
        FetchOutcome outcome = executor.execute(INAT, FetchTrigger.MANUAL);

        assertThat(outcome.success()).isTrue();
        assertThat(versions(INAT, PersistReason.SIGNIFICANT)).isGreaterThanOrEqualTo(1);
        assertThat(versions(INAT, null)).isGreaterThan(firstVersions);

        String iNatPayloads = jdbc.queryForObject(
                "select coalesce(string_agg(payload::text, ' '), '') from record_version where source_id = ?",
                String.class, INAT);
        assertThat(iNatPayloads).doesNotContain("\"user\"").doesNotContain("\"login\"").doesNotContain("\"description\"");
    }

    @Test
    @DisplayName("an unchanged CDC NWSS re-poll writes no version")
    void nwssUnchanged() {
        serveWval();
        enable(WVAL, Map.of());
        await().atMost(Duration.ofSeconds(15)).until(() -> fetchRows(WVAL) >= 1);
        disable(WVAL);
        int firstVersions = versions(WVAL, null);
        assertThat(firstVersions).isGreaterThan(0);

        FetchOutcome outcome = executor.execute(WVAL, FetchTrigger.MANUAL);

        assertThat(outcome.success()).isTrue();
        assertThat(outcome.persisted()).isZero();
        assertThat(versions(WVAL, null)).isEqualTo(firstVersions);
    }
}
