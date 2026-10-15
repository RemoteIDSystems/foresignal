package ai.foresignal.collect;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestBackpressureException;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Coverage;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WireMockSupport;
import ai.foresignal.testsupport.adapters.FakeAdapters;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import ai.foresignal.testsupport.collect.ControllablePollingAdapter;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The fetch path end to end: adapter, traces, the real writer and the real {@code fetch_log} and
 * {@code source_health} tables.
 *
 * <p>The scheduler is off in the test profile, so nothing fetches behind these tests' backs; each
 * test calls {@link FetchExecutor#execute} itself.
 */
@IntegrationTest
@Import(CollectorTestSources.class)
class FetchExecutorIT extends WireMockSupport {

    private static final String POLL = CollectorTestSources.POLL;
    private static final String KEYED = CollectorTestSources.KEYED;
    private static final Instant T0 = Instant.parse("2026-04-01T12:00:00Z");
    private static final String SECRET = "s3cr3t-key-0123456789";

    @Autowired private FetchExecutor executor;
    @Autowired private ControllablePollingAdapter collectPollAdapter;
    @Autowired private ControllablePollingAdapter collectKeyedAdapter;
    @Autowired private SourceService sources;
    @Autowired private SourceRegistry registry;
    @Autowired private BackoffRegistry backoff;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private MutableClock clock;
    @Autowired private MeterRegistry meters;
    @Autowired private ForesignalVersion version;
    @Autowired private FetchContextFactory contexts;
    @Autowired private FetchDispatcher dispatcher;
    @Autowired private HealthTracker health;
    @Autowired private FetchLogWriter fetchLogWriter;
    @Autowired private CollectorProperties properties;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        collectPollAdapter.reset();
        collectKeyedAdapter.reset();
        backoff.onSuccess(POLL);
        backoff.onSuccess(KEYED);
    }

    @AfterEach
    void tearDown() {
        backoff.onSuccess(POLL);
        collectPollAdapter.reset();
    }

    // ------------------------------------------------------------------ helpers

    private List<Map<String, Object>> fetchRows(String sourceId) {
        return jdbc.queryForList("select * from fetch_log where source_id = ? order by id", sourceId);
    }

    private Map<String, Object> onlyRow(String sourceId) {
        List<Map<String, Object>> rows = fetchRows(sourceId);
        assertThat(rows).hasSize(1);
        return rows.get(0);
    }

    private Map<String, Object> healthRow(String sourceId) {
        List<Map<String, Object>> rows = jdbc.queryForList("select * from source_health where source_id = ?", sourceId);
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    private int heads(String sourceId) {
        Integer count = jdbc.queryForObject("select count(*) from record_head where source_id = ?", Integer.class, sourceId);
        return count == null ? 0 : count;
    }

    private static FetchResult twoRecords() {
        Instant observed = T0.minusSeconds(60);
        return FetchResult.complete(List.of(
                CollectorTestSources.record("a", observed), CollectorTestSources.record("b", observed)));
    }

    private static String sha256(String text) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)));
    }

    private double counter(String name, String source, String reason) {
        Counter counter = meters.find(name).tag("source", source).tag("reason", reason).counter();
        return counter == null ? 0 : counter.count();
    }

    // ------------------------------------------------------------------ tests

    @Nested
    @DisplayName("records returned")
    class Records {

        @Test
        @DisplayName("are ingested through the writer, which writes the OK row with counts and seqs")
        void ingestsAndWritesOkRow() {
            collectPollAdapter.then(ctx -> twoRecords().withCoverage(Coverage.of(2, 2)));

            FetchOutcome outcome = executor.execute(POLL, FetchTrigger.MANUAL);

            assertThat(outcome.success()).isTrue();
            assertThat(outcome.status()).isEqualTo(FetchStatus.OK);
            assertThat(outcome.recordsSeen()).isEqualTo(2);
            assertThat(outcome.persisted()).isEqualTo(2);
            assertThat(outcome.recordsChanged()).isEqualTo(2);
            assertThat(outcome.byReason().get(PersistReason.FIRST)).isEqualTo(2);
            assertThat(outcome.fromSeq()).isNotNull();
            assertThat(outcome.toSeq()).isEqualTo(outcome.fromSeq() + 1);
            assertThat(outcome.error()).isNull();

            Map<String, Object> row = onlyRow(POLL);
            assertThat(row.get("status")).isEqualTo("OK");
            assertThat(row.get("trigger")).isEqualTo("MANUAL");
            assertThat(row.get("ingested_at")).isNotNull();
            assertThat(row.get("records_seen")).isEqualTo(2);
            assertThat(row.get("versions_written")).isEqualTo(2);
            assertThat(((Number) row.get("from_seq")).longValue()).isEqualTo(outcome.fromSeq());
            assertThat(((Number) row.get("to_seq")).longValue()).isEqualTo(outcome.toSeq());
            assertThat(row.get("licence")).isEqualTo(CollectorTestSources.LICENCE);
            assertThat(row.get("adapter_version")).isEqualTo(version.version());
            assertThat(row.get("principal")).isNull();
            assertThat(row.get("coverage").toString()).contains("\"expected\"");
            assertThat(outcome.fetchId()).isEqualTo(((Number) row.get("id")).longValue());

            assertThat(heads(POLL)).isEqualTo(2);
        }

        @Test
        @DisplayName("update health and set last_data_at")
        void updatesHealth() {
            collectPollAdapter.then(ctx -> twoRecords());

            executor.execute(POLL, FetchTrigger.MANUAL);

            Map<String, Object> health = healthRow(POLL);
            assertThat(health.get("last_success_at")).isNotNull();
            assertThat(health.get("last_attempt_at")).isNotNull();
            assertThat(((Timestamp) health.get("last_data_at")).toInstant()).isEqualTo(T0);
            assertThat(health.get("last_records_seen")).isEqualTo(2);
            assertThat(health.get("consecutive_failures")).isEqualTo(0);
        }

        @Test
        @DisplayName("are stamped with receivedAt when fetch returns")
        void stampsReceivedAt() {
            collectPollAdapter.then(ctx -> {
                clock.setInstant(T0.plusSeconds(5));
                return twoRecords();
            });

            executor.execute(POLL, FetchTrigger.MANUAL);

            List<Timestamp> received = jdbc.queryForList(
                    "select received_at from record_version where source_id = ?", Timestamp.class, POLL);
            assertThat(received).hasSize(2).allSatisfy(ts -> assertThat(ts.toInstant()).isEqualTo(T0.plusSeconds(5)));
        }

        @Test
        @DisplayName("carry the last trace's URL, status, ETag and body hash into the row")
        void rowFromTraces() throws Exception {
            stubFor(get(urlPathEqualTo("/feed"))
                    .willReturn(aResponse().withStatus(200).withHeader("ETag", "\"v1\"").withBody("hello")));
            collectPollAdapter.then(ctx -> {
                ctx.http().get(URI.create(url("/feed")));
                return twoRecords();
            });

            executor.execute(POLL, FetchTrigger.MANUAL);

            Map<String, Object> row = onlyRow(POLL);
            assertThat(row.get("request_count")).isEqualTo(1);
            assertThat(row.get("http_status")).isEqualTo(200);
            assertThat(row.get("etag")).isEqualTo("\"v1\"");
            assertThat(((Number) row.get("bytes")).longValue()).isEqualTo(5L);
            assertThat(row.get("body_sha256")).isEqualTo(sha256("hello"));
            assertThat((String) row.get("url_redacted")).endsWith("/feed");
            assertThat(((Timestamp) row.get("received_at")).toInstant()).isEqualTo(T0);
        }
    }

    @Nested
    @DisplayName("no records")
    class NoRecords {

        @Test
        @DisplayName("a 304 writes NOT_MODIFIED and leaves last_data_at untouched")
        void notModified() {
            collectPollAdapter.then(ctx -> twoRecords()).then(ctx -> FetchResult.unchanged());
            executor.execute(POLL, FetchTrigger.MANUAL);
            clock.advance(Duration.ofMinutes(5));

            FetchOutcome outcome = executor.execute(POLL, FetchTrigger.MANUAL);

            assertThat(outcome.success()).isTrue();
            assertThat(outcome.status()).isEqualTo(FetchStatus.NOT_MODIFIED);
            List<Map<String, Object>> rows = fetchRows(POLL);
            assertThat(rows).hasSize(2);
            assertThat(rows.get(1).get("status")).isEqualTo("NOT_MODIFIED");
            assertThat(rows.get(1).get("ingested_at")).isNull();
            assertThat(outcome.fetchId()).isEqualTo(((Number) rows.get(1).get("id")).longValue());

            Map<String, Object> health = healthRow(POLL);
            assertThat(((Timestamp) health.get("last_data_at")).toInstant()).isEqualTo(T0);
            assertThat(((Timestamp) health.get("last_success_at")).toInstant()).isEqualTo(T0.plus(Duration.ofMinutes(5)));
        }

        @Test
        @DisplayName("an empty result writes OK with zero counts and leaves last_data_at untouched")
        void empty() {
            collectPollAdapter.then(ctx -> FetchResult.complete(List.of()));

            FetchOutcome outcome = executor.execute(POLL, FetchTrigger.MANUAL);

            assertThat(outcome.success()).isTrue();
            assertThat(outcome.recordsSeen()).isZero();
            Map<String, Object> row = onlyRow(POLL);
            assertThat(row.get("status")).isEqualTo("OK");
            assertThat(row.get("records_seen")).isEqualTo(0);
            assertThat(row.get("versions_written")).isEqualTo(0);
            assertThat(row.get("ingested_at")).isNull();
            assertThat(row.get("licence")).isEqualTo(CollectorTestSources.LICENCE);

            Map<String, Object> health = healthRow(POLL);
            assertThat(health.get("last_success_at")).isNotNull();
            assertThat(health.get("last_data_at")).isNull();
        }
    }

    @Nested
    @DisplayName("failures")
    class Failures {

        @Test
        @DisplayName("an HTTP error writes FAILED with the status and a redacted error, and counts a failure")
        void httpFailure() {
            sources.setCredentials(KEYED, Map.of("apiKey", SECRET));
            stubFor(get(urlPathEqualTo("/keyed"))
                    .willReturn(aResponse().withStatus(500).withBody("server rejected " + SECRET)));
            collectKeyedAdapter.then(ctx -> {
                ctx.http().get(URI.create(url("/keyed?key=" + ctx.credentials().get("apiKey"))));
                return twoRecords();
            });

            FetchOutcome outcome = executor.execute(KEYED, FetchTrigger.MANUAL);

            assertThat(outcome.success()).isFalse();
            assertThat(outcome.status()).isEqualTo(FetchStatus.FAILED);
            assertThat(outcome.error()).contains("UpstreamHttpException").doesNotContain(SECRET);

            Map<String, Object> row = onlyRow(KEYED);
            assertThat(row.get("status")).isEqualTo("FAILED");
            assertThat(row.get("http_status")).isEqualTo(500);
            assertThat((String) row.get("error")).contains("UpstreamHttpException").doesNotContain(SECRET);
            assertThat((String) row.get("url_redacted")).doesNotContain(SECRET);
            assertThat(row.get("request_count")).isEqualTo(1);

            Map<String, Object> health = healthRow(KEYED);
            assertThat(health.get("consecutive_failures")).isEqualTo(1);
            assertThat((String) health.get("last_error")).doesNotContain(SECRET);
            assertThat(heads(KEYED)).isZero();
        }

        @Test
        @DisplayName("an error longer than 2,000 characters is truncated")
        void longErrorTruncated() {
            collectPollAdapter.then(ctx -> {
                throw new IllegalStateException("x".repeat(5000));
            });

            executor.execute(POLL, FetchTrigger.MANUAL);

            assertThat(((String) onlyRow(POLL).get("error")).length()).isLessThanOrEqualTo(HealthTracker.MAX_ERROR_LENGTH);
        }

        @Test
        @DisplayName("a timeout interrupts the adapter and fails the fetch")
        void timeout() {
            AtomicBoolean interrupted = new AtomicBoolean();
            collectPollAdapter.then(ctx -> {
                try {
                    Thread.sleep(30_000);
                } catch (InterruptedException e) {
                    interrupted.set(true);
                    throw e;
                }
                return twoRecords();
            });

            FetchOutcome outcome = executor.execute(POLL, FetchTrigger.MANUAL);

            assertThat(outcome.status()).isEqualTo(FetchStatus.FAILED);
            assertThat(outcome.error()).contains("AdapterTimeoutException");
            assertThat(outcome.durationMs()).isLessThan(10_000);
            await().atMost(Duration.ofSeconds(5)).untilTrue(interrupted);
            assertThat(onlyRow(POLL).get("status")).isEqualTo("FAILED");
            assertThat(healthRow(POLL).get("consecutive_failures")).isEqualTo(1);
        }

        @Test
        @DisplayName("a budget refusal writes REFUSED and is not a health failure")
        void refused() {
            collectPollAdapter.then(ctx -> {
                throw new BudgetExceededException(POLL, "spent");
            });

            FetchOutcome outcome = executor.execute(POLL, FetchTrigger.MANUAL);

            assertThat(outcome.success()).isFalse();
            assertThat(outcome.status()).isEqualTo(FetchStatus.REFUSED);
            assertThat(outcome.error()).isEqualTo(FetchExecutor.BUDGET_EXCEEDED_ERROR);
            Map<String, Object> row = onlyRow(POLL);
            assertThat(row.get("status")).isEqualTo("REFUSED");
            assertThat(row.get("error")).isEqualTo("budget exceeded");
            Map<String, Object> health = healthRow(POLL);
            assertThat(health.get("consecutive_failures")).isEqualTo(0);
            assertThat(health.get("last_error")).isNull();
        }

        @Test
        @DisplayName("a refusal wrapped in an adapter exception is still a refusal")
        void wrappedRefusal() {
            collectPollAdapter.then(ctx -> {
                throw new ai.foresignal.source.spi.AdapterException("wrapped", new BudgetExceededException(POLL, "spent"));
            });

            assertThat(executor.execute(POLL, FetchTrigger.MANUAL).status()).isEqualTo(FetchStatus.REFUSED);
        }
    }

    @Nested
    @DisplayName("skips")
    class Skips {

        @Test
        @DisplayName("a source in backoff is skipped with no row, no fetch and no health change")
        void inBackoff() {
            backoff.onThrottled(POLL, Optional.of(Duration.ofMinutes(5)));

            FetchOutcome outcome = executor.execute(POLL, FetchTrigger.MANUAL);

            assertThat(outcome.wasSkipped()).isTrue();
            assertThat(outcome.skipReason()).isEqualTo(FetchOutcome.SKIP_BACKOFF);
            assertThat(collectPollAdapter.fetchCount()).isZero();
            assertThat(fetchRows(POLL)).isEmpty();
            Object lastAttempt = healthRow(POLL).get("last_attempt_at");
            assertThat(lastAttempt).isNull();
        }

        @Test
        @DisplayName("an adapter that hits backoff mid-fetch leaves no row")
        void backoffMidFetch() {
            collectPollAdapter.then(ctx -> {
                throw new InBackoffException(POLL, T0.plusSeconds(60));
            });

            FetchOutcome outcome = executor.execute(POLL, FetchTrigger.MANUAL);

            assertThat(outcome.skipReason()).isEqualTo(FetchOutcome.SKIP_BACKOFF);
            assertThat(fetchRows(POLL)).isEmpty();
            assertThat(healthRow(POLL).get("consecutive_failures")).isEqualTo(0);
        }

        @Test
        @DisplayName("an overlapping run is skipped and counted")
        void overlap() throws Exception {
            CountDownLatch release = new CountDownLatch(1);
            collectPollAdapter.then(ctx -> {
                release.await(10, TimeUnit.SECONDS);
                return FetchResult.complete(List.of());
            });
            double before = counter("foresignal.fetch.skipped", POLL, "overlap");

            CompletableFuture<FetchOutcome> first =
                    CompletableFuture.supplyAsync(() -> executor.execute(POLL, FetchTrigger.SCHEDULED));
            await().atMost(Duration.ofSeconds(5)).until(() -> collectPollAdapter.fetchCount() == 1);
            assertThat(executor.isRunning(POLL)).isTrue();
            assertThat(executor.activeFetches()).isGreaterThanOrEqualTo(1);

            FetchOutcome second = executor.execute(POLL, FetchTrigger.SCHEDULED);
            release.countDown();
            first.get(10, TimeUnit.SECONDS);

            assertThat(second.skipReason()).isEqualTo(FetchOutcome.SKIP_OVERLAP);
            assertThat(counter("foresignal.fetch.skipped", POLL, "overlap")).isEqualTo(before + 1);
            assertThat(collectPollAdapter.fetchCount()).isEqualTo(1);
            assertThat(fetchRows(POLL)).hasSize(1);
            assertThat(executor.isRunning(POLL)).isFalse();
        }
    }

    @Nested
    @DisplayName("disabled in flight")
    class Discarded {

        @Test
        @DisplayName("a scheduled run of a source disabled in flight is DISCARDED and nothing is ingested")
        void discarded() {
            sources.update(POLL, SourceSettingsUpdate.enabled(true));
            collectPollAdapter.then(ctx -> {
                sources.update(POLL, SourceSettingsUpdate.enabled(false));
                return twoRecords();
            });

            FetchOutcome outcome = executor.execute(POLL, FetchTrigger.SCHEDULED);

            assertThat(outcome.status()).isEqualTo(FetchStatus.DISCARDED);
            assertThat(outcome.recordsSeen()).isEqualTo(2);
            Map<String, Object> row = onlyRow(POLL);
            assertThat(row.get("status")).isEqualTo("DISCARDED");
            assertThat(row.get("trigger")).isEqualTo("SCHEDULED");
            assertThat(row.get("records_seen")).isEqualTo(2);
            assertThat(heads(POLL)).isZero();
        }

        @Test
        @DisplayName("a manual run of a disabled source still ingests")
        void manualWhileDisabled() {
            collectPollAdapter.then(ctx -> twoRecords());

            FetchOutcome outcome = executor.execute(POLL, FetchTrigger.MANUAL);

            assertThat(outcome.status()).isEqualTo(FetchStatus.OK);
            assertThat(heads(POLL)).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("ingest trouble")
    class IngestTrouble {

        private FetchExecutor executorWith(IngestQueue queue) {
            return new FetchExecutor(
                    registry, contexts, dispatcher, health, backoff, queue, fetchLogWriter, version, jdbc, meters,
                    properties, clock);
        }

        @Test
        @DisplayName("a full queue writes FAILED 'ingest queue full' and is not an upstream failure")
        void backpressure() {
            IngestQueue queue = mock(IngestQueue.class);
            when(queue.submit(anyString(), any(FetchLogEntry.class), anyList(), any(Duration.class), anyBoolean()))
                    .thenThrow(new IngestBackpressureException("full"));
            collectPollAdapter.then(ctx -> twoRecords());

            FetchOutcome outcome = executorWith(queue).execute(POLL, FetchTrigger.MANUAL);

            assertThat(outcome.status()).isEqualTo(FetchStatus.FAILED);
            assertThat(outcome.error()).isEqualTo(FetchExecutor.QUEUE_FULL_ERROR);
            Map<String, Object> row = onlyRow(POLL);
            assertThat(row.get("error")).isEqualTo("ingest queue full");
            assertThat(row.get("records_seen")).isEqualTo(2);
            assertThat(healthRow(POLL).get("consecutive_failures")).isEqualTo(0);
            assertThat(healthRow(POLL).get("last_error")).isNull();
        }

        @Test
        @DisplayName("a batch the writer failed writes FAILED and is not an upstream failure")
        void writerFailure() {
            IngestQueue queue = mock(IngestQueue.class);
            when(queue.submit(anyString(), any(FetchLogEntry.class), anyList(), any(Duration.class), anyBoolean()))
                    .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("constraint violated")));
            collectPollAdapter.then(ctx -> twoRecords());

            FetchOutcome outcome = executorWith(queue).execute(POLL, FetchTrigger.MANUAL);

            assertThat(outcome.status()).isEqualTo(FetchStatus.FAILED);
            assertThat(outcome.error()).startsWith("ingest failed:").contains("constraint violated");
            assertThat(onlyRow(POLL).get("status")).isEqualTo("FAILED");
            assertThat(healthRow(POLL).get("consecutive_failures")).isEqualTo(0);
        }

        @Test
        @DisplayName("the submit timeout is the remaining fetch time plus the grace period")
        void submitTimeout() {
            IngestQueue queue = mock(IngestQueue.class);
            AtomicReference<Duration> timeout = new AtomicReference<>();
            when(queue.submit(anyString(), any(FetchLogEntry.class), anyList(), any(Duration.class), anyBoolean()))
                    .thenAnswer(invocation -> {
                        timeout.set(invocation.getArgument(3));
                        throw new IngestBackpressureException("full");
                    });
            collectPollAdapter.then(ctx -> twoRecords());

            executorWith(queue).execute(POLL, FetchTrigger.MANUAL);

            assertThat(timeout.get())
                    .isGreaterThan(properties.ingestGrace())
                    .isLessThanOrEqualTo(properties.ingestGrace().plusSeconds(2));
        }
    }

    @Nested
    @DisplayName("arguments")
    class Arguments {

        @Test
        @DisplayName("an unknown source is not found")
        void unknown() {
            assertThatThrownBy(() -> executor.execute("no-such-source", FetchTrigger.MANUAL))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("only polling sources are fetched")
        void notPolling() {
            assertThatThrownBy(() -> executor.execute(FakeAdapters.STREAMING_ID, FetchTrigger.MANUAL))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> executor.execute(FakeAdapters.PUSH_ID, FetchTrigger.MANUAL))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("STREAM and PUSH are not fetch triggers")
        void wrongTrigger() {
            assertThatThrownBy(() -> executor.execute(POLL, FetchTrigger.STREAM))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
