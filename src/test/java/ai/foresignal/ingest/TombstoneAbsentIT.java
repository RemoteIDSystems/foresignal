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
    }

    @Autowired private ReferenceTestAdapter adapter;
    @Autowired private FetchExecutor executor;
    @Autowired private MutableClock clock;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        adapter.set(List.of());
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
        return jdbc.queryForObject(
                "select adapter_state->>'health.degraded' from source_health where source_id = ?",
                String.class, ID);
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

        clock.advance(Duration.ofMinutes(1));
        FetchOutcome overridden = executor.execute(ID, FetchTrigger.MANUAL, true);
        assertThat(overridden.success()).isTrue();
        assertThat(versions(PersistReason.TOMBSTONE, "e5")).isEqualTo(1);
        assertThat(versions(PersistReason.TOMBSTONE, "e10")).isEqualTo(1);
        assertThat(releaseDisappeared()).isEqualTo("6");
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
