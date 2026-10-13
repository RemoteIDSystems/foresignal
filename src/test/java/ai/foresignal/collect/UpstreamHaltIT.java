package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.notify.SystemEvent;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.UpstreamHaltException;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.collect.ControllablePollingAdapter;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

/**
 * The upstream halt end to end (prompt 064): {@code FetchExecutor} holds the source for 24 h, records the failure
 * with the halt reason, publishes {@code system.source_halted}; scheduled runs are skipped, a later ordinary
 * backoff is not released by fetch-now, and fetch-now releases the halt.
 */
@IntegrationTest
@Import(CollectorTestSources.class)
@RecordApplicationEvents
class UpstreamHaltIT {

    private static final String POLL = CollectorTestSources.POLL;
    private static final Instant T0 = Instant.parse("2026-10-13T12:00:00Z");
    private static final String REASON =
            "CelesTrak returned 404; automatic retries stopped per CelesTrak usage policy — investigate, then use fetch-now";

    @Autowired private FetchExecutor executor;
    @Autowired private ManualFetchService manual;
    @Autowired private ControllablePollingAdapter collectPollAdapter;
    @Autowired private BackoffRegistry backoff;
    @Autowired private MutableClock clock;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ApplicationEvents events;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        collectPollAdapter.reset();
        backoff.onSuccess(POLL);
    }

    @AfterEach
    void tearDown() {
        backoff.onSuccess(POLL);
    }

    private void halt() {
        collectPollAdapter.then(ctx -> {
            throw new UpstreamHaltException(404, REASON);
        });
        FetchOutcome outcome = executor.execute(POLL, FetchTrigger.MANUAL);
        assertThat(outcome.status()).isEqualTo(FetchStatus.FAILED);
    }

    @Test
    @DisplayName("a halt holds the source 24 h (beyond the 1 h ceiling), persisted, with the reason and a notification")
    void holds() {
        halt();

        assertThat(backoff.backoffUntil(POLL)).isEqualTo(T0.plus(Duration.ofHours(24)));
        assertThat(backoff.isHalted(POLL)).isTrue();
        assertThat(jdbc.queryForObject("select backoff_halted from source_health where source_id = ?", Boolean.class,
                POLL)).isTrue();
        Timestamp until = jdbc.queryForObject("select backoff_until from source_health where source_id = ?",
                Timestamp.class, POLL);
        assertThat(until.toInstant()).isEqualTo(T0.plus(Duration.ofHours(24)));
        assertThat(jdbc.queryForObject("select last_error from source_health where source_id = ?", String.class, POLL))
                .contains(REASON);
        List<SystemEvent> halted = events.stream(SystemEvent.class)
                .filter(e -> FetchExecutor.HALT_EVENT.equals(e.subtype())).toList();
        assertThat(halted).singleElement().satisfies(e -> {
            assertThat(e.severity()).isEqualTo("critical");
            assertThat(e.data()).containsEntry("sourceId", POLL).containsEntry("httpStatus", 404);
        });
    }

    @Test
    @DisplayName("while halted, scheduled runs are skipped without an upstream request, even 23 h later")
    void noRetry() {
        halt();
        int fetches = collectPollAdapter.fetchCount();
        clock.advance(Duration.ofHours(23));
        FetchOutcome skipped = executor.execute(POLL, FetchTrigger.SCHEDULED);
        assertThat(skipped.wasSkipped()).isTrue();
        assertThat(skipped.skipReason()).isEqualTo(FetchOutcome.SKIP_BACKOFF);
        assertThat(collectPollAdapter.fetchCount()).isEqualTo(fetches);
    }

    @Test
    @DisplayName("fetch-now releases the halt and fetches; an ordinary backoff still answers IN_BACKOFF")
    void fetchNowReleases() {
        halt();
        collectPollAdapter.then(ctx -> FetchResult.complete(List.of()));

        FetchOutcome outcome = manual.fetchNow(POLL);

        assertThat(outcome.success()).isTrue();
        assertThat(backoff.isInBackoff(POLL)).isFalse();
        assertThat(backoff.isHalted(POLL)).isFalse();
        assertThat(jdbc.queryForObject("select backoff_halted from source_health where source_id = ?", Boolean.class,
                POLL)).isFalse();

        backoff.onThrottled(POLL, Optional.of(Duration.ofMinutes(10)));
        assertThat(backoff.clearHalt(POLL)).isFalse();
        assertThatThrownBy(() -> manual.fetchNow(POLL))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((ForesignalException) e).getCode()).isEqualTo(ConflictException.IN_BACKOFF));
    }

    @Test
    @DisplayName("a halt survives a cache reset (restart): the flag is read back from source_health")
    void survivesRestart() {
        halt();
        backoff.clearCache();
        assertThat(backoff.isHalted(POLL)).isTrue();
        clock.advance(Duration.ofHours(25));
        assertThat(backoff.isHalted(POLL)).isFalse();
    }
}
