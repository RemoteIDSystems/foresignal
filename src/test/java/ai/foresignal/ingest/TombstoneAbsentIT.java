package ai.foresignal.ingest;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.collect.FetchExecutor;
import ai.foresignal.collect.FetchOutcome;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.ConditionalReferenceTestAdapter;
import ai.foresignal.testsupport.adapters.ReferenceTestAdapter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Complete-set reconciliation (prompt 094): a record the run did not see is tombstoned, a mass absence
 * trips the shrink guard, forced touches protect unchanged records, a 304 never reconciles, and a record
 * that comes back is a REAPPEAR. Drives the real executor, writer and {@link TombstoneAbsent}.
 */
@IntegrationTest
@Import({TombstoneAbsentIT.Config.class, MutableClock.TestConfig.class})
class TombstoneAbsentIT extends WireMockSupport {

    private static final String ID = ReferenceTestAdapter.ID;
    private static final String KIND = ReferenceTestAdapter.KIND;
    private static final Instant NOW = Instant.parse("2026-10-14T12:00:00Z");

    /** Registers the reference test adapter. */
    @TestConfiguration
    static class Config {
        @Bean
        ReferenceTestAdapter referenceTestAdapter() {
            return new ReferenceTestAdapter();
        }

        @Bean
        ConditionalReferenceTestAdapter conditionalReferenceTestAdapter() {
            return new ConditionalReferenceTestAdapter();
        }
    }

    @Autowired private ReferenceTestAdapter adapter;
    @Autowired private ConditionalReferenceTestAdapter conditional;
    @Autowired private FetchExecutor executor;
    @Autowired private MutableClock clock;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        adapter.set(List.of());
        conditional.baseUrl(baseUrl());
    }

    private static List<NormalizedRecord> records(String... ids) {
        List<NormalizedRecord> out = new ArrayList<>(ids.length);
        for (String id : ids) {
            out.add(NormalizedRecord.builder(id, KIND).put("name", id).build());
        }
        return out;
    }

    private static List<NormalizedRecord> e1to10() {
        String[] ids = new String[10];
        for (int i = 0; i < 10; i++) {
            ids[i] = "e" + (i + 1);
        }
        return records(ids);
    }

    private int versions(PersistReason reason, String externalId) {
        Integer count = jdbc.queryForObject(
                "select count(*) from record_version where source_id = ? and external_id = ? and persist_reason = ?",
                Integer.class, ID, externalId, reason.code());
        return count == null ? 0 : count;
    }

    private boolean headTombstoned(String externalId) {
        Boolean tombstone = jdbc.queryForObject(
                "select tombstone from record_head where source_id = ? and external_id = ?",
                Boolean.class, ID, externalId);
        return Boolean.TRUE.equals(tombstone);
    }

    private String releaseDisappeared() {
        return jdbc.queryForObject(
                "select payload->>'disappeared' from record_version where source_id = ? and external_id = ?"
                        + " order by version desc limit 1",
                String.class, ID, ReferenceTestAdapter.RELEASE);
    }

    private String degradedReason() {
        return degradedReason(ID);
    }

    private String degradedReason(String sourceId) {
        List<String> rows = jdbc.queryForList(
                "select adapter_state->>'health.degraded' from source_health where source_id = ?",
                String.class, sourceId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private int versions(String sourceId, PersistReason reason, String externalId) {
        Integer count = jdbc.queryForObject(
                "select count(*) from record_version where source_id = ? and external_id = ? and persist_reason = ?",
                Integer.class, sourceId, externalId, reason.code());
        return count == null ? 0 : count;
    }

    private FetchOutcome fetch() {
        return executor.execute(ID, FetchTrigger.MANUAL);
    }

    @Test
    @DisplayName("a record absent from a complete run gets exactly one TOMBSTONE and a dataset-release")
    void tombstonesAbsent() {
        adapter.set(e1to10());
        fetch();
        assertThat(versions(PersistReason.FIRST, "e5")).isEqualTo(1);

        clock.advance(Duration.ofMinutes(1));
        List<NormalizedRecord> nine = e1to10();
        nine.removeIf(r -> r.externalId().equals("e5"));
        adapter.set(nine);
        FetchOutcome outcome = fetch();

        assertThat(outcome.success()).isTrue();
        assertThat(versions(PersistReason.TOMBSTONE, "e5")).isEqualTo(1);
        assertThat(headTombstoned("e5")).isTrue();
        assertThat(versions(PersistReason.TOMBSTONE, "e1")).isZero();
        assertThat(releaseDisappeared()).isEqualTo("1");

        // The tombstone batch's fetch_log row joins the run's chunk rows through run_id.
        List<String> reconcileRuns = jdbc.queryForList(
                "select run_id from fetch_log where source_id = ? and principal = 'reconcile'", String.class, ID);
        assertThat(reconcileRuns).hasSize(1).doesNotContainNull();
        Integer chunkRows = jdbc.queryForObject(
                "select count(*) from fetch_log where source_id = ? and run_id = ? and principal is null",
                Integer.class, ID, reconcileRuns.get(0));
        assertThat(chunkRows).isPositive();
    }

    @Test
    @DisplayName("a truncated dataset tripping the shrink guard writes nothing; allowShrink overrides once")
    void shrinkGuard() {
        adapter.set(e1to10());
        fetch();

        clock.advance(Duration.ofMinutes(1));
        adapter.set(records("e1", "e2", "e3", "e4"));
        FetchOutcome blocked = fetch();

        assertThat(blocked.success()).isTrue();
        assertThat(versions(PersistReason.TOMBSTONE, "e5")).isZero();
        assertThat(degradedReason()).contains("shrank");

        // The skipped reconciliation is recorded as a DISCARDED row carrying the run id and the reason.
        List<java.util.Map<String, Object>> discarded = jdbc.queryForList(
                "select run_id, error from fetch_log where source_id = ? and status = 'DISCARDED'", ID);
        assertThat(discarded).hasSize(1);
        assertThat(discarded.get(0).get("run_id")).isNotNull();
        assertThat((String) discarded.get(0).get("error")).startsWith(TombstoneOutcome.SHRINK_REASON_PREFIX);
        Integer sameRun = jdbc.queryForObject(
                "select count(*) from fetch_log where source_id = ? and run_id = ? and status = 'OK'",
                Integer.class, ID, discarded.get(0).get("run_id"));
        assertThat(sameRun).isPositive();

        clock.advance(Duration.ofMinutes(1));
        FetchOutcome overridden = executor.execute(ID, FetchTrigger.MANUAL, true);
        assertThat(overridden.success()).isTrue();
        assertThat(versions(PersistReason.TOMBSTONE, "e5")).isEqualTo(1);
        assertThat(versions(PersistReason.TOMBSTONE, "e10")).isEqualTo(1);
        assertThat(releaseDisappeared()).isEqualTo("6");
        // A reconciliation that ran clears the guard's degradation.
        assertThat(degradedReason()).isNull();
    }

    @Test
    @DisplayName("the shrink-guard degradation clears on the next clean complete run")
    void shrinkGuardRecovers() {
        adapter.set(e1to10());
        fetch();

        clock.advance(Duration.ofMinutes(1));
        adapter.set(records("e1", "e2"));
        fetch();
        assertThat(degradedReason()).contains("shrank");

        clock.advance(Duration.ofMinutes(1));
        adapter.set(e1to10());
        fetch();

        assertThat(degradedReason()).isNull();
        assertThat(versions(PersistReason.TOMBSTONE, "e5")).isZero();
    }

    @Test
    @DisplayName("on a conditional-GET source, allowShrink re-reads the dataset despite the stored validator")
    void allowShrinkOnConditionalSource() {
        String id = ConditionalReferenceTestAdapter.ID;
        String path = ConditionalReferenceTestAdapter.PATH;
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(
                        com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo(path))
                .willReturn(com.github.tomakehurst.wiremock.client.WireMock.aResponse().withStatus(200)
                        .withHeader("ETag", "\"v1\"").withBody("e1,e2,e3,e4,e5,e6,e7,e8,e9,e10")));
        executor.execute(id, FetchTrigger.MANUAL);
        assertThat(versions(id, PersistReason.FIRST, "e10")).isEqualTo(1);

        // The upstream now publishes a much smaller release; a request with its validator gets a 304.
        WIREMOCK.resetAll();
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(
                        com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo(path))
                .withHeader("If-None-Match", com.github.tomakehurst.wiremock.client.WireMock.equalTo("\"v2\""))
                .willReturn(com.github.tomakehurst.wiremock.client.WireMock.aResponse().withStatus(304)));
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(
                        com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo(path))
                .withHeader("If-None-Match", com.github.tomakehurst.wiremock.client.WireMock.notMatching("\"v2\""))
                .willReturn(com.github.tomakehurst.wiremock.client.WireMock.aResponse().withStatus(200)
                        .withHeader("ETag", "\"v2\"").withBody("e1,e2,e3,e4")));
        WIREMOCK.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(
                        com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo(path))
                .withHeader("If-None-Match", com.github.tomakehurst.wiremock.client.WireMock.absent())
                .willReturn(com.github.tomakehurst.wiremock.client.WireMock.aResponse().withStatus(200)
                        .withHeader("ETag", "\"v2\"").withBody("e1,e2,e3,e4")));

        clock.advance(Duration.ofMinutes(1));
        executor.execute(id, FetchTrigger.MANUAL);
        assertThat(versions(id, PersistReason.TOMBSTONE, "e5")).isZero();
        assertThat(degradedReason(id)).contains("shrank");

        // A plain re-run would send "v2" and get a 304; the override must not.
        clock.advance(Duration.ofMinutes(1));
        FetchOutcome overridden = executor.execute(id, FetchTrigger.MANUAL, true);

        assertThat(overridden.status()).isNotEqualTo(FetchStatus.NOT_MODIFIED);
        assertThat(versions(id, PersistReason.TOMBSTONE, "e5")).isEqualTo(1);
        assertThat(versions(id, PersistReason.TOMBSTONE, "e10")).isEqualTo(1);
        assertThat(degradedReason(id)).isNull();
        WIREMOCK.verify(com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor(
                        com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo(path))
                .withoutHeader("If-None-Match"));

        // The validator of the re-read release is kept: the next scheduled run is a 304.
        clock.advance(Duration.ofMinutes(1));
        assertThat(executor.execute(id, FetchTrigger.MANUAL).status()).isEqualTo(FetchStatus.NOT_MODIFIED);
    }

    @Test
    @DisplayName("a re-run within touchMin writes no tombstone and touches every record")
    void forcedTouches() {
        adapter.set(e1to10());
        fetch();
        Instant firstReceived = lastReceived("e1");

        clock.advance(Duration.ofMinutes(1));
        adapter.set(e1to10());
        FetchOutcome outcome = fetch();

        assertThat(outcome.touchesThrottled()).isZero();
        assertThat(outcome.touched()).isEqualTo(10);
        assertThat(versions(PersistReason.TOMBSTONE, "e1")).isZero();
        assertThat(lastReceived("e1")).isAfter(firstReceived);
    }

    @Test
    @DisplayName("a 304 run never reconciles")
    void notModified() {
        adapter.set(e1to10());
        fetch();

        clock.advance(Duration.ofMinutes(1));
        adapter.unchanged();
        FetchOutcome outcome = fetch();

        assertThat(outcome.status()).isEqualTo(FetchStatus.NOT_MODIFIED);
        assertThat(versions(PersistReason.TOMBSTONE, "e1")).isZero();
        Integer releases = jdbc.queryForObject(
                "select count(*) from record_version where source_id = ? and external_id = ?",
                Integer.class, ID, ReferenceTestAdapter.RELEASE);
        assertThat(releases).isEqualTo(1);
    }

    @Test
    @DisplayName("a reappearing record is a REAPPEAR")
    void reappear() {
        adapter.set(e1to10());
        fetch();

        clock.advance(Duration.ofMinutes(1));
        List<NormalizedRecord> nine = e1to10();
        nine.removeIf(r -> r.externalId().equals("e5"));
        adapter.set(nine);
        fetch();
        assertThat(headTombstoned("e5")).isTrue();

        clock.advance(Duration.ofMinutes(1));
        adapter.set(e1to10());
        fetch();

        assertThat(versions(PersistReason.REAPPEAR, "e5")).isEqualTo(1);
        assertThat(headTombstoned("e5")).isFalse();
    }

    private Instant lastReceived(String externalId) {
        java.sql.Timestamp value = jdbc.queryForObject(
                "select last_received_at from record_live where source_id = ? and external_id = ?",
                java.sql.Timestamp.class, ID, externalId);
        return value == null ? null : value.toInstant();
    }
}
