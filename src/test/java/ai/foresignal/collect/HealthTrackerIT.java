package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.source.CredentialsAccess;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.store.CoverageSample;
import ai.foresignal.store.SourceHealthEntity;
import ai.foresignal.store.SourceHealthRepository;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Envelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import tools.jackson.databind.json.JsonMapper;

/**
 * The tracker against the real {@code source_health} table.
 *
 * <p>What matters most here cannot be checked without a database: that the tracker writes only its
 * own columns of a row other components share, that the rings survive a round trip through
 * {@code jsonb}, and that the error stored is the redacted, truncated one.
 */
@IntegrationTest
class HealthTrackerIT {

    private static final String SOURCE = "health-test";
    private static final String SECRET = "s3cr3t-api-key-value";
    private static final Instant START = Instant.parse("2026-03-01T12:00:00Z");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private SourceHealthRepository rows;

    private MutableClock clock;
    private CredentialsAccess credentials;
    private BackoffRegistry backoff;
    private HealthTracker tracker;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(START);
        credentials = mock(CredentialsAccess.class);
        when(credentials.forSource(anyString())).thenReturn(Map.of("apiKey", SECRET));
        backoff = new BackoffRegistry(jdbc, clock);
        tracker = new HealthTracker(jdbc, jsonMapper, transactionManager, backoff, credentials, clock);
    }

    private SourceHealthEntity row() {
        return rows.findById(SOURCE).orElseThrow();
    }

    private static Instant millis(Instant instant) {
        return instant.truncatedTo(ChronoUnit.MILLIS);
    }

    @Nested
    @DisplayName("attempts and successes")
    class Successes {

        @Test
        @DisplayName("an attempt creates the row and sets last_attempt_at")
        void attempt() {
            tracker.recordAttempt(SOURCE);

            assertThat(row().getLastAttemptAt()).isEqualTo(START);
            assertThat(row().getConsecutiveFailures()).isZero();
        }

        @Test
        @DisplayName("a success sets the counts and timestamps and resets the failure streak")
        void success() {
            tracker.recordFailure(SOURCE, new UpstreamIOException("connection refused", null));
            tracker.recordFailure(SOURCE, new UpstreamIOException("connection refused", null));
            clock.advance(Duration.ofMinutes(1));

            tracker.recordSuccess(SOURCE, 42, 7, false);

            SourceHealthEntity row = row();
            assertThat(row.getLastSuccessAt()).isEqualTo(START.plusSeconds(60));
            assertThat(row.getLastRecordsSeen()).isEqualTo(42);
            assertThat(row.getLastRecordsChanged()).isEqualTo(7);
            assertThat(row.getConsecutiveFailures()).isZero();
            assertThat(row.getRecentRecordCounts()).containsExactly(42);
            assertThat(row.getLastDataAt()).isEqualTo(START.plusSeconds(60));
            // The earlier failure stays visible as history.
            assertThat(row.getLastError()).contains("connection refused");
        }

        @Test
        @DisplayName("the record-count ring keeps the last five, newest last")
        void ringOfFive() {
            for (int seen = 1; seen <= 7; seen++) {
                tracker.recordSuccess(SOURCE, seen, 0, false);
            }

            assertThat(row().getRecentRecordCounts()).containsExactly(3, 4, 5, 6, 7);
        }

        @Test
        @DisplayName("a 304 counts as a success but stays out of the ring and never sets last_data_at")
        void notModified() {
            tracker.recordSuccess(SOURCE, 10, 1, false);
            Instant dataAt = row().getLastDataAt();
            clock.advance(Duration.ofMinutes(5));

            tracker.recordSuccess(SOURCE, 0, 0, true);

            SourceHealthEntity row = row();
            assertThat(row.getRecentRecordCounts()).containsExactly(10);
            assertThat(row.getLastSuccessAt()).isEqualTo(START.plus(Duration.ofMinutes(5)));
            assertThat(row.getLastDataAt()).isEqualTo(dataAt);
        }

        @Test
        @DisplayName("a success with zero records enters the ring but does not set last_data_at")
        void zeroRecords() {
            tracker.recordSuccess(SOURCE, 0, 0, false);

            assertThat(row().getRecentRecordCounts()).containsExactly(0);
            assertThat(row().getLastDataAt()).isNull();
        }

        @Test
        @DisplayName("an ingest outcome reports persisted as changed, and coverage enters its ring")
        void ingestOutcome() {
            Coverage coverage = new Coverage(12, 300, 280, new Envelope(-98, -96, 30, 31));

            tracker.recordSuccess(SOURCE, new IngestOutcome(300, 25, 0, 275, 0, 1, 0, 2, 3, 10, 34, Map.of(), 0, List.of()), false, coverage);

            SourceHealthEntity row = row();
            assertThat(row.getLastRecordsSeen()).isEqualTo(300);
            assertThat(row.getLastRecordsChanged()).isEqualTo(25);
            assertThat(row.getLastCoverage()).isEqualTo(new CoverageSample(12, 300, 280, List.of(-98.0, 30.0, -96.0, 31.0)));
            assertThat(row.getRecentCoverage()).hasSize(1);
        }

        @Test
        @DisplayName("the coverage ring keeps the last twenty, and an empty report leaves it alone")
        void coverageRing() {
            for (int receivers = 1; receivers <= 25; receivers++) {
                tracker.recordSuccess(SOURCE, IngestOutcome.of(5, 1), false, new Coverage(receivers, null, null, null));
            }
            tracker.recordSuccess(SOURCE, IngestOutcome.of(5, 1), false, Coverage.none());
            tracker.recordSuccess(SOURCE, IngestOutcome.of(5, 1), true, new Coverage(99, null, null, null));

            SourceHealthEntity row = row();
            assertThat(row.getRecentCoverage()).hasSize(SourceHealthEntity.RECENT_COVERAGE_LIMIT);
            assertThat(row.getRecentCoverage().get(0).receivers()).isEqualTo(6);
            assertThat(row.getLastCoverage().receivers()).isEqualTo(25);
        }

        @Test
        @DisplayName("a success clears an active backoff")
        void clearsBackoff() {
            backoff.onThrottled(SOURCE, Optional.of(Duration.ofMinutes(10)));
            assertThat(backoff.isInBackoff(SOURCE)).isTrue();

            tracker.recordSuccess(SOURCE, 3, 1, false);

            assertThat(backoff.isInBackoff(SOURCE)).isFalse();
            assertThat(row().getBackoffUntil()).isNull();
        }

        @Test
        @DisplayName("a success with no prior attempt still records an attempt time")
        void successImpliesAttempt() {
            tracker.recordSuccess(SOURCE, 1, 1, false);

            assertThat(row().getLastAttemptAt()).isEqualTo(START);
        }
    }

    @Nested
    @DisplayName("failures")
    class Failures {

        @Test
        @DisplayName("each failure increments the streak and records when and why")
        void streak() {
            tracker.recordFailure(SOURCE, new UpstreamHttpException(500, "Internal"));
            clock.advance(Duration.ofSeconds(30));
            tracker.recordFailure(SOURCE, new UpstreamHttpException(502, "Bad gateway"));

            SourceHealthEntity row = row();
            assertThat(row.getConsecutiveFailures()).isEqualTo(2);
            assertThat(row.getLastErrorAt()).isEqualTo(START.plusSeconds(30));
            assertThat(row.getLastError()).isEqualTo("UpstreamHttpException: Upstream returned HTTP 502: Bad gateway");
            assertThat(row.getLastHttpStatus()).isEqualTo(502);
        }

        @Test
        @DisplayName("an HTTP status is captured through a wrapping cause, and cleared by a non-HTTP error")
        void httpStatus() {
            tracker.recordFailure(SOURCE, new RuntimeException("wrapped", new UpstreamHttpException(403, "")));
            assertThat(row().getLastHttpStatus()).isEqualTo(403);

            tracker.recordFailure(SOURCE, new UpstreamIOException("timed out", new IOException("timeout")));
            assertThat(row().getLastHttpStatus()).isNull();
        }

        @Test
        @DisplayName("credential values are masked in the stored message")
        void redaction() {
            tracker.recordFailure(
                    SOURCE, new UpstreamIOException("GET https://api.example/feed?key=" + SECRET + " failed", null));

            assertThat(row().getLastError())
                    .doesNotContain(SECRET)
                    .contains("key=" + UrlRedactor.MASK);
        }

        @Test
        @DisplayName("the message is withheld when credentials cannot be loaded to mask it")
        void withheldWithoutCredentials() {
            when(credentials.forSource(SOURCE)).thenThrow(new IllegalStateException("cannot decrypt"));

            tracker.recordFailure(SOURCE, new UpstreamIOException("key=" + SECRET, null));

            assertThat(row().getLastError())
                    .doesNotContain(SECRET)
                    .startsWith("UpstreamIOException: (message withheld");
        }

        @Test
        @DisplayName("a long message is cut to 2000 characters")
        void truncation() {
            tracker.recordFailure(SOURCE, new IllegalStateException("x".repeat(5000)));

            String stored = row().getLastError();
            assertThat(stored).hasSize(HealthTracker.MAX_ERROR_LENGTH).endsWith(HealthTracker.TRUNCATION_MARKER);
        }

        @Test
        @DisplayName("an exception with no message stores its class, or its cause")
        void noMessage() {
            tracker.recordFailure(SOURCE, new IllegalStateException());
            assertThat(row().getLastError()).isEqualTo("IllegalStateException");

            tracker.recordFailure(SOURCE, new RuntimeException((String) null, new IOException("reset")));
            assertThat(row().getLastError()).isEqualTo("RuntimeException: IOException: reset");
        }

        @Test
        @DisplayName("budget refusals and backoff skips are not failures")
        void notFailures() {
            tracker.recordFailure(SOURCE, new BudgetExceededException(SOURCE, "daily budget spent"));
            tracker.recordFailure(SOURCE, new InBackoffException(SOURCE, START.plusSeconds(60)));
            tracker.recordRefused(SOURCE);

            assertThat(rows.findById(SOURCE)).isEmpty();
        }
    }

    @Test
    @DisplayName("backoff_until and adapter_state belong to other components and are never touched")
    void leavesOtherColumnsAlone() {
        Instant deadline = START.plus(Duration.ofHours(1));
        jdbc.update(
                """
                insert into source_health (source_id, consecutive_failures, recent_record_counts, adapter_state, backoff_until)
                values (?, 0, '[]'::jsonb, '{"etag":"\\"abc\\""}'::jsonb, ?)
                """,
                SOURCE,
                Timestamp.from(deadline));

        tracker.recordAttempt(SOURCE);
        tracker.recordFailure(SOURCE, new UpstreamIOException("down", null));
        tracker.recordSuccess(SOURCE, 5, 5, false);
        tracker.recordSuccess(SOURCE, IngestOutcome.of(5, 5), false, new Coverage(3, null, null, null));

        SourceHealthEntity row = row();
        assertThat(row.getBackoffUntil()).isEqualTo(millis(deadline));
        assertThat(row.getAdapterState()).containsEntry("etag", "\"abc\"");
    }

    @Nested
    @DisplayName("streaming hooks")
    class Streaming {

        @Test
        @DisplayName("a stream with no events is disconnected since the tracker started")
        void defaultState() {
            assertThat(tracker.streamState(SOURCE)).isEqualTo(StreamState.disconnectedAt(START));
        }

        @Test
        @DisplayName("connecting is an attempt that clears the failure streak but adds nothing to the ring")
        void connected() {
            tracker.recordFailure(SOURCE, new UpstreamIOException("refused", null));
            clock.advance(Duration.ofSeconds(10));

            tracker.recordStreamConnected(SOURCE);

            SourceHealthEntity row = row();
            assertThat(tracker.streamState(SOURCE)).isEqualTo(StreamState.connectedAt(START.plusSeconds(10)));
            assertThat(row.getLastAttemptAt()).isEqualTo(START.plusSeconds(10));
            assertThat(row.getConsecutiveFailures()).isZero();
            assertThat(row.getRecentRecordCounts()).isEmpty();
        }

        @Test
        @DisplayName("a disconnect with an error is a failure; a clean close is not")
        void disconnected() {
            tracker.recordStreamConnected(SOURCE);
            clock.advance(Duration.ofSeconds(5));

            tracker.recordStreamDisconnected(SOURCE, null);
            assertThat(row().getConsecutiveFailures()).isZero();
            assertThat(tracker.streamState(SOURCE)).isEqualTo(StreamState.disconnectedAt(START.plusSeconds(5)));

            tracker.recordStreamDisconnected(SOURCE, new UpstreamIOException("socket closed", null));
            assertThat(row().getConsecutiveFailures()).isEqualTo(1);
        }

        @Test
        @DisplayName("a flush is a success that sets last_data_at")
        void flush() {
            tracker.recordStreamFlush(SOURCE, 120, 30);

            SourceHealthEntity row = row();
            assertThat(row.getLastRecordsSeen()).isEqualTo(120);
            assertThat(row.getLastRecordsChanged()).isEqualTo(30);
            assertThat(row.getLastDataAt()).isEqualTo(START);
            assertThat(row.getRecentRecordCounts()).containsExactly(120);
        }
    }
}
