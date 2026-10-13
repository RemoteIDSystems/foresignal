package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.adapters.FakeAdapters;
import ai.foresignal.testsupport.adapters.FakeStreamingAdapter;
import ai.foresignal.testsupport.collect.CollectorTestSources;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The supervisor against the real writer, with short ladders so reconnects are observable.
 *
 * <p>The supervisor under test is built here with test settings rather than autowired, so a
 * reconnect ladder of milliseconds does not leak into other tests' context.
 */
@IntegrationTest
@Import(CollectorTestSources.class)
class StreamingSupervisorIT {

    private static final String STREAM = FakeAdapters.STREAMING_ID;
    private static final Instant T0 = Instant.parse("2026-04-01T12:00:00Z");

    @Autowired private SourceRegistry registry;
    @Autowired private SourceService sources;
    @Autowired private FetchContextFactory contexts;
    @Autowired private HealthTracker health;
    @Autowired private BackoffRegistry backoff;
    @Autowired private IngestQueue queue;
    @Autowired private FetchLogWriter fetchLog;
    @Autowired private ForesignalVersion version;
    @Autowired private MeterRegistry meters;
    @Autowired private MutableClock clock;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private FakeStreamingAdapter fakeStreamingAdapter;

    private StreamingSupervisor supervisor;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        fakeStreamingAdapter.reset();
        backoff.onSuccess(STREAM);
    }

    @AfterEach
    void tearDown() {
        if (supervisor != null) {
            supervisor.stopAll(Duration.ofSeconds(5));
        }
        fakeStreamingAdapter.reset();
    }

    private StreamingSupervisor supervisor(
            Duration flushInterval, List<Duration> ladder, Duration resetAfter, Duration credentialsRetry) {
        CollectorProperties.Stream stream = new CollectorProperties.Stream(
                flushInterval, 2000, 50_000, Duration.ofSeconds(1), Duration.ofSeconds(1), ladder, resetAfter,
                credentialsRetry);
        CollectorProperties properties = new CollectorProperties(
                false, Duration.ofSeconds(2), Duration.ofSeconds(10), Duration.ofSeconds(30), stream);
        supervisor = new StreamingSupervisor(
                registry, sources, contexts, health, queue, fetchLog, version, meters, properties, clock);
        return supervisor;
    }

    private StreamingSupervisor defaultTestSupervisor() {
        return supervisor(Duration.ofMillis(200), List.of(Duration.ofMillis(100)), Duration.ofMinutes(10),
                Duration.ofHours(1));
    }

    private static NormalizedRecord record(String id) {
        return NormalizedRecord.builder(id, "fake-record").observedAt(T0.minusSeconds(5)).point(1, 2).put("n", id).build();
    }

    private List<Map<String, Object>> streamRows() {
        return jdbc.queryForList("select * from fetch_log where source_id = ? and trigger = 'STREAM' order by id", STREAM);
    }

    @Test
    @DisplayName("flushes reach the writer as STREAM rows and set last_data_at")
    void flushesReachWriter() {
        fakeStreamingAdapter.scriptOnStart(record("s1"), record("s2"));
        StreamingSupervisor s = defaultTestSupervisor();

        s.start(STREAM);

        await().atMost(Duration.ofSeconds(10)).until(() -> !streamRows().isEmpty() && streamRows().get(0).get("ingested_at") != null);
        Map<String, Object> row = streamRows().get(0);
        assertThat(row.get("status")).isEqualTo("OK");
        assertThat(row.get("records_seen")).isEqualTo(2);
        assertThat(row.get("versions_written")).isEqualTo(2);
        await().atMost(Duration.ofSeconds(5)).until(() -> lastDataAt() != null);
        assertThat(s.isConnected(STREAM)).isTrue();
        assertThat(s.activeStreams()).isEqualTo(1);
        assertThat(s.running()).containsExactly(STREAM);
        assertThat(health.streamState(STREAM).connected()).isTrue();
    }

    private Instant lastDataAt() {
        List<Timestamp> rows = jdbc.queryForList(
                "select last_data_at from source_health where source_id = ?", Timestamp.class, STREAM);
        return rows.isEmpty() || rows.get(0) == null ? null : rows.get(0).toInstant();
    }

    @Test
    @DisplayName("stop closes the handle and flushes what was buffered")
    void stopFlushes() {
        StreamingSupervisor s = supervisor(Duration.ofSeconds(60), List.of(Duration.ofMillis(100)),
                Duration.ofMinutes(10), Duration.ofHours(1));
        s.start(STREAM);
        await().atMost(Duration.ofSeconds(5)).until(() -> fakeStreamingAdapter.startCount() == 1 && s.isConnected(STREAM));
        fakeStreamingAdapter.emit(record("late-1"));
        fakeStreamingAdapter.emit(record("late-2"));
        assertThat(streamRows()).isEmpty();

        assertThat(s.stop(STREAM, Duration.ofSeconds(10))).isTrue();

        assertThat(fakeStreamingAdapter.allClosed()).isTrue();
        List<Map<String, Object>> rows = streamRows();
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("records_seen")).isEqualTo(2);
        assertThat(s.running()).isEmpty();
        assertThat(health.streamState(STREAM).connected()).isFalse();
    }

    @Test
    @DisplayName("reconnects on the ladder and stops at the long step")
    void reconnectLadder() {
        StreamingSupervisor s = supervisor(Duration.ofMillis(200),
                List.of(Duration.ofMillis(100), Duration.ofMillis(200), Duration.ofSeconds(60)), Duration.ofMinutes(10),
                Duration.ofHours(1));
        s.start(STREAM);
        await().atMost(Duration.ofSeconds(5)).until(() -> fakeStreamingAdapter.startCount() == 1);

        fakeStreamingAdapter.lastHandle().drop(new IOException("connection reset"));
        await().atMost(Duration.ofSeconds(5)).until(() -> fakeStreamingAdapter.startCount() == 2);
        assertThat(s.attempts(STREAM)).isEqualTo(1);

        await().atMost(Duration.ofSeconds(5)).until(() -> s.isConnected(STREAM));
        fakeStreamingAdapter.lastHandle().drop(new IOException("again"));
        await().atMost(Duration.ofSeconds(5)).until(() -> fakeStreamingAdapter.startCount() == 3);
        assertThat(s.attempts(STREAM)).isEqualTo(2);

        await().atMost(Duration.ofSeconds(5)).until(() -> s.isConnected(STREAM));
        fakeStreamingAdapter.lastHandle().drop(new IOException("third"));
        await().during(Duration.ofSeconds(1)).atMost(Duration.ofSeconds(2))
                .until(() -> fakeStreamingAdapter.startCount() == 3);
        assertThat(s.isConnected(STREAM)).isFalse();

        Integer failures = jdbc.queryForObject(
                "select consecutive_failures from source_health where source_id = ?", Integer.class, STREAM);
        assertThat(failures).isEqualTo(1);
    }

    @Test
    @DisplayName("the ladder starts again after a connection held for ladderResetAfter")
    void ladderResets() throws Exception {
        StreamingSupervisor s = supervisor(Duration.ofMillis(200),
                List.of(Duration.ofMillis(50), Duration.ofMillis(100), Duration.ofMillis(150)), Duration.ofMillis(800),
                Duration.ofHours(1));
        s.start(STREAM);
        await().atMost(Duration.ofSeconds(5)).until(() -> s.isConnected(STREAM));
        fakeStreamingAdapter.lastHandle().drop(null);
        await().atMost(Duration.ofSeconds(5)).until(() -> fakeStreamingAdapter.startCount() == 2 && s.isConnected(STREAM));
        fakeStreamingAdapter.lastHandle().drop(null);
        await().atMost(Duration.ofSeconds(5)).until(() -> fakeStreamingAdapter.startCount() == 3 && s.isConnected(STREAM));
        assertThat(s.attempts(STREAM)).isEqualTo(2);

        Thread.sleep(1200);
        fakeStreamingAdapter.lastHandle().drop(null);
        await().atMost(Duration.ofSeconds(5)).until(() -> fakeStreamingAdapter.startCount() == 4);

        assertThat(s.attempts(STREAM)).isEqualTo(1);
    }

    @Test
    @DisplayName("rejected credentials retry on the credentials interval, not the ladder")
    void credentialsRejected() {
        StreamingSupervisor s = supervisor(Duration.ofMillis(200), List.of(Duration.ofSeconds(60)),
                Duration.ofMinutes(10), Duration.ofMillis(200));
        fakeStreamingAdapter.failOnStart(new CredentialsRejectedException("key refused"));

        s.start(STREAM);

        await().atMost(Duration.ofSeconds(5)).until(() -> fakeStreamingAdapter.startCount() == 2);
        assertThat(s.attempts(STREAM)).isZero();
        await().atMost(Duration.ofSeconds(5)).until(() -> s.isConnected(STREAM));
        String error = jdbc.queryForObject("select last_error from source_health where source_id = ?", String.class, STREAM);
        assertThat(error).contains("CredentialsRejectedException");
    }

    @Test
    @DisplayName("the default credentials retry is hourly")
    void credentialsHourly() {
        StreamingSupervisor s = new StreamingSupervisor(
                registry, sources, contexts, health, queue, fetchLog, version, meters, CollectorProperties.defaults(),
                clock);
        supervisor = s;

        assertThat(s.nextDelay(STREAM, 3, new CredentialsRejectedException("no"))).isEqualTo(Duration.ofHours(1));
        assertThat(s.nextDelay(STREAM, 0, new IOException("x"))).isEqualTo(Duration.ofSeconds(5));
        assertThat(s.nextDelay(STREAM, 1, null)).isEqualTo(Duration.ofSeconds(15));
        assertThat(s.nextDelay(STREAM, 2, null)).isEqualTo(Duration.ofSeconds(60));
        assertThat(s.nextDelay(STREAM, 3, null)).isEqualTo(Duration.ofSeconds(300));
        assertThat(s.nextDelay(STREAM, 4, null)).isEqualTo(Duration.ofSeconds(900));
        assertThat(s.nextDelay(STREAM, 9, null)).isEqualTo(Duration.ofSeconds(900));
    }

    @Test
    @DisplayName("a budget refusal waits until the next minute instead of spinning")
    void budgetWaitsForNextMinute() {
        clock.setInstant(T0.plusSeconds(10));
        StreamingSupervisor s = defaultTestSupervisor();

        Duration delay = s.nextDelay(STREAM, 0, new BudgetExceededException(STREAM, "spent"));

        assertThat(delay).isBetween(Duration.ofSeconds(50), Duration.ofSeconds(51));
    }

    @Test
    @DisplayName("a source in backoff waits out its backoff")
    void backoffWaits() {
        StreamingSupervisor s = defaultTestSupervisor();

        Duration delay = s.nextDelay(STREAM, 0, new InBackoffException(STREAM, T0.plusSeconds(90)));

        assertThat(delay).isEqualTo(Duration.ofSeconds(90));
    }

    @Test
    @DisplayName("reconcile starts and stops streams to match the desired set")
    void reconcile() {
        StreamingSupervisor s = defaultTestSupervisor();

        s.reconcile(Set.of(STREAM));
        await().atMost(Duration.ofSeconds(5)).until(() -> fakeStreamingAdapter.startCount() == 1);
        assertThat(s.running()).containsExactly(STREAM);

        s.reconcile(Set.of());

        assertThat(s.running()).isEmpty();
        assertThat(fakeStreamingAdapter.allClosed()).isTrue();
    }

    @Test
    @DisplayName("only streaming sources can be started")
    void notStreaming() {
        StreamingSupervisor s = defaultTestSupervisor();

        assertThatThrownBy(() -> s.start(CollectorTestSources.POLL)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> s.start(FakeAdapters.PUSH_ID)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("063: changed parameters go to the handle's update(); a handle that applies them is not restarted")
    void paramsAppliedInPlace() {
        fakeStreamingAdapter.acceptUpdates(true);
        StreamingSupervisor s = defaultTestSupervisor();
        s.start(STREAM);
        await().atMost(Duration.ofSeconds(5)).until(() -> s.isConnected(STREAM));

        assertThat(s.paramsChanged(STREAM)).isTrue();

        assertThat(fakeStreamingAdapter.startCount()).isEqualTo(1);
        assertThat(fakeStreamingAdapter.lastHandle().updates()).hasSize(1);
        assertThat(fakeStreamingAdapter.lastHandle().isRunning()).isTrue();
    }

    @Test
    @DisplayName("063: a handle that cannot apply new parameters is restarted; a stopped stream is left alone")
    void paramsRestart() {
        StreamingSupervisor s = defaultTestSupervisor();
        assertThat(s.paramsChanged(STREAM)).as("not running").isFalse();
        assertThat(fakeStreamingAdapter.startCount()).isZero();

        s.start(STREAM);
        await().atMost(Duration.ofSeconds(5)).until(() -> s.isConnected(STREAM));
        FakeStreamingAdapter.Handle first = fakeStreamingAdapter.lastHandle();

        assertThat(s.paramsChanged(STREAM)).isFalse();

        await().atMost(Duration.ofSeconds(5)).until(() -> fakeStreamingAdapter.startCount() == 2);
        assertThat(first.updates()).hasSize(1);
        assertThat(first.isRunning()).isFalse();
        assertThat(s.running()).containsExactly(STREAM);
    }

    @Test
    @DisplayName("063: restart() closes and reopens a running stream at once")
    void restart() {
        StreamingSupervisor s = defaultTestSupervisor();
        s.restart(STREAM);
        assertThat(fakeStreamingAdapter.startCount()).as("not running: nothing to restart").isZero();

        s.start(STREAM);
        await().atMost(Duration.ofSeconds(5)).until(() -> fakeStreamingAdapter.startCount() == 1);
        s.restart(STREAM);

        await().atMost(Duration.ofSeconds(5)).until(() -> fakeStreamingAdapter.startCount() == 2);
        assertThat(fakeStreamingAdapter.openHandles()).isEqualTo(1);
    }
}
