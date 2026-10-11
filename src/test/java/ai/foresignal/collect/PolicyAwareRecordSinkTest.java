package ai.foresignal.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.ingest.FetchLogEntry;
import ai.foresignal.ingest.IngestBackpressureException;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.IngestRecord;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.FetchStatus;
import ai.foresignal.store.FetchTrigger;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.adapters.FakeStreamingAdapter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * The streaming sink on its own, with a recording submitter in place of the ingest queue
 * (Core-Data-Model §7).
 */
class PolicyAwareRecordSinkTest {

    private static final Instant T0 = Instant.parse("2026-05-01T00:00:00Z");
    private static final String SOURCE = "sink-test";

    private final MutableClock clock = new MutableClock(T0);
    private final SimpleMeterRegistry meters = new SimpleMeterRegistry();
    private final Submitter submitter = new Submitter();
    private final Listener listener = new Listener();
    private PolicyAwareRecordSink sink;

    @AfterEach
    void close() {
        if (sink != null) {
            submitter.completeAll();
            sink.flushAndClose(Duration.ofSeconds(2));
        }
    }

    // ------------------------------------------------------------------ harness

    /** Records each submitted batch; futures complete immediately unless {@link #hold} is set. */
    static final class Submitter implements PolicyAwareRecordSink.BatchSubmitter {
        final List<List<IngestRecord>> batches = new CopyOnWriteArrayList<>();
        final List<FetchLogEntry> logs = new CopyOnWriteArrayList<>();
        final List<CompletableFuture<IngestOutcome>> futures = new CopyOnWriteArrayList<>();
        final AtomicInteger refuseNext = new AtomicInteger();
        volatile boolean hold;
        volatile RuntimeException failWith;

        @Override
        public CompletableFuture<IngestOutcome> submit(
                String sourceId, FetchLogEntry log, List<IngestRecord> records, Duration timeout) {
            if (refuseNext.getAndUpdate(n -> Math.max(0, n - 1)) > 0) {
                throw new IngestBackpressureException("full");
            }
            batches.add(List.copyOf(records));
            logs.add(log);
            CompletableFuture<IngestOutcome> future = new CompletableFuture<>();
            futures.add(future);
            if (failWith != null) {
                future.completeExceptionally(failWith);
            } else if (!hold) {
                future.complete(IngestOutcome.of(records.size(), records.size()));
            }
            return future;
        }

        void completeAll() {
            futures.forEach(f -> f.complete(IngestOutcome.of(0, 0)));
        }
    }

    static final class Listener implements PolicyAwareRecordSink.FlushListener {
        final List<IngestOutcome> flushed = new CopyOnWriteArrayList<>();
        final List<Throwable> failed = new CopyOnWriteArrayList<>();

        @Override
        public void flushed(FetchLogEntry log, int records, IngestOutcome outcome) {
            flushed.add(outcome);
        }

        @Override
        public void failed(FetchLogEntry log, int records, Throwable error) {
            failed.add(error);
        }
    }

    private static CollectorProperties.Stream settings(
            Duration flushInterval, int flushMax, int allMax, Duration blockTimeout) {
        return new CollectorProperties.Stream(
                flushInterval, flushMax, allMax, blockTimeout, Duration.ofMillis(200), null, null, null);
    }

    private PolicyAwareRecordSink sink(IngestPolicy.PolicyMode mode, CollectorProperties.Stream settings) {
        return sink(FakeStreamingAdapter.defaultDescriptor(SOURCE).licence("TEST-L").build(), mode, settings);
    }

    private PolicyAwareRecordSink sink(
            SourceDescriptor descriptor, IngestPolicy.PolicyMode mode, CollectorProperties.Stream settings) {
        sink = new PolicyAwareRecordSink(descriptor, mode, submitter, listener, clock, settings, "1.0-test", meters);
        sink.start();
        return sink;
    }

    private static NormalizedRecord record(String id, Instant observedAt, String value) {
        return NormalizedRecord.builder(id, "fake-record").observedAt(observedAt).put("v", value).build();
    }

    // ------------------------------------------------------------------ ALL

    @Nested
    @DisplayName("ALL")
    class All {

        @Test
        @DisplayName("keeps every record in arrival order, duplicates included")
        void preservesArrivalOrder() {
            PolicyAwareRecordSink s = sink(IngestPolicy.PolicyMode.ALL,
                    settings(Duration.ofMillis(100), 1000, 100, Duration.ofMillis(100)));
            s.accept(record("b", T0, "1"));
            s.accept(record("a", T0, "2"));
            s.accept(record("b", T0.minusSeconds(5), "3"));

            await().atMost(Duration.ofSeconds(3)).until(() -> submitter.batches.size() == 1);
            List<IngestRecord> batch = submitter.batches.get(0);
            assertThat(batch).extracting(r -> r.record().payload().get("v")).containsExactly("1", "2", "3");
            assertThat(batch).extracting(IngestRecord::arrival).isSorted();
            assertThat(s.isKeyed()).isFalse();
        }

        @Test
        @DisplayName("blocks at the cap, then drops after the timeout and counts it")
        void blocksThenDrops() {
            PolicyAwareRecordSink s = sink(IngestPolicy.PolicyMode.ALL,
                    settings(Duration.ofSeconds(30), 1000, 3, Duration.ofMillis(300)));
            for (int i = 0; i < 3; i++) {
                s.accept(record("r" + i, T0, "x"));
            }

            long start = System.nanoTime();
            s.accept(record("r3", T0, "x"));
            long blockedMs = Duration.ofNanos(System.nanoTime() - start).toMillis();

            assertThat(blockedMs).isGreaterThanOrEqualTo(250);
            assertThat(s.droppedCount()).isEqualTo(1);
            assertThat(s.buffered()).isEqualTo(3);
            assertThat(meters.find(PolicyAwareRecordSink.DROPPED_COUNTER).tag("source", SOURCE).counter().count())
                    .isEqualTo(1.0);
        }

        @Test
        @DisplayName("a blocked accept proceeds once a flush makes room")
        void unblocksOnFlush() {
            PolicyAwareRecordSink s = sink(IngestPolicy.PolicyMode.ALL,
                    settings(Duration.ofMillis(300), 1000, 3, Duration.ofSeconds(10)));
            for (int i = 0; i < 3; i++) {
                s.accept(record("r" + i, T0, "x"));
            }

            s.accept(record("r3", T0, "x"));

            assertThat(s.droppedCount()).isZero();
            await().atMost(Duration.ofSeconds(3)).until(() -> submitter.batches.stream().mapToInt(List::size).sum() == 4);
        }
    }

    // ------------------------------------------------------------------ keyed

    @Nested
    @DisplayName("SAMPLED and LATEST")
    class Keyed {

        @Test
        @DisplayName("the greatest observedAt wins and a tie goes to the later arrival")
        void latestWins() {
            PolicyAwareRecordSink s = sink(IngestPolicy.PolicyMode.SAMPLED,
                    settings(Duration.ofSeconds(30), 1000, 100, Duration.ofMillis(100)));
            Instant t1 = T0.minusSeconds(20);
            Instant t2 = T0.minusSeconds(10);

            s.accept(record("x", t2, "newest"));
            s.accept(record("x", t1, "older"));
            assertThat(s.keyedSnapshot().get("x").record().payload().get("v")).isEqualTo("newest");

            s.accept(record("x", t2, "tie-later"));
            assertThat(s.keyedSnapshot().get("x").record().payload().get("v")).isEqualTo("tie-later");

            s.accept(record("y", t1, "other"));
            assertThat(s.buffered()).isEqualTo(2);
            assertThat(s.isKeyed()).isTrue();

            s.flushAndClose(Duration.ofSeconds(2));
            List<IngestRecord> batch = submitter.batches.get(0);
            assertThat(batch).extracting(r -> r.record().payload().get("v")).containsExactlyInAnyOrder("tie-later", "other");
        }

        @Test
        @DisplayName("LATEST is keyed too")
        void latestIsKeyed() {
            PolicyAwareRecordSink s = sink(IngestPolicy.PolicyMode.LATEST,
                    settings(Duration.ofSeconds(30), 1000, 100, Duration.ofMillis(100)));
            s.accept(record("x", T0, "1"));
            s.accept(record("x", T0, "2"));
            assertThat(s.buffered()).isEqualTo(1);
        }

        @Test
        @DisplayName("under observedAtMode=RECEIVED the later receipt wins")
        void receivedMode() {
            SourceDescriptor descriptor = FakeStreamingAdapter.defaultDescriptor(SOURCE)
                    .observedAtMode(ObservedAtMode.RECEIVED)
                    .build();
            PolicyAwareRecordSink s = sink(descriptor, IngestPolicy.PolicyMode.SAMPLED,
                    settings(Duration.ofSeconds(30), 1000, 100, Duration.ofMillis(100)));

            s.accept(record("x", T0.plusSeconds(100), "first"));
            clock.advance(Duration.ofSeconds(1));
            s.accept(record("x", T0.minusSeconds(100), "second"));

            assertThat(s.keyedSnapshot().get("x").record().payload().get("v")).isEqualTo("second");
        }
    }

    // ------------------------------------------------------------------ flushing

    @Nested
    @DisplayName("flushing")
    class Flushing {

        @Test
        @DisplayName("never sends a second batch while one is outstanding")
        void oneOutstanding() {
            submitter.hold = true;
            PolicyAwareRecordSink s = sink(IngestPolicy.PolicyMode.ALL,
                    settings(Duration.ofMillis(50), 2, 100, Duration.ofMillis(100)));
            s.accept(record("a", T0, "1"));
            s.accept(record("b", T0, "2"));
            await().atMost(Duration.ofSeconds(3)).until(() -> submitter.batches.size() == 1);
            assertThat(s.hasOutstanding()).isTrue();

            s.accept(record("c", T0, "3"));
            s.accept(record("d", T0, "4"));
            s.accept(record("e", T0, "5"));
            await().during(Duration.ofMillis(400)).atMost(Duration.ofSeconds(2)).until(() -> submitter.batches.size() == 1);
            assertThat(s.buffered()).isEqualTo(3);

            submitter.futures.get(0).complete(IngestOutcome.of(2, 2));
            await().atMost(Duration.ofSeconds(3)).until(() -> submitter.batches.size() == 2);
            assertThat(submitter.batches.get(1)).hasSize(3);
        }

        @Test
        @DisplayName("flushes as soon as the buffer reaches flushMaxEntries")
        void sizeTriggered() {
            PolicyAwareRecordSink s = sink(IngestPolicy.PolicyMode.ALL,
                    settings(Duration.ofSeconds(30), 3, 100, Duration.ofMillis(100)));
            s.accept(record("a", T0, "1"));
            s.accept(record("b", T0, "2"));
            await().during(Duration.ofMillis(300)).atMost(Duration.ofSeconds(1)).until(submitter.batches::isEmpty);

            s.accept(record("c", T0, "3"));

            await().atMost(Duration.ofSeconds(2)).until(() -> submitter.batches.size() == 1);
            assertThat(submitter.batches.get(0)).hasSize(3);
        }

        @Test
        @DisplayName("flushes once the oldest record has waited flushInterval")
        void timeTriggered() {
            PolicyAwareRecordSink s = sink(IngestPolicy.PolicyMode.ALL,
                    settings(Duration.ofMillis(500), 1000, 100, Duration.ofMillis(100)));
            long start = System.nanoTime();
            s.accept(record("a", T0, "1"));

            await().atMost(Duration.ofSeconds(3)).until(() -> submitter.batches.size() == 1);
            assertThat(Duration.ofNanos(System.nanoTime() - start).toMillis()).isGreaterThanOrEqualTo(400);
        }

        @Test
        @DisplayName("flushes what is buffered on close, then drops anything accepted afterwards")
        void flushOnStop() {
            PolicyAwareRecordSink s = sink(IngestPolicy.PolicyMode.ALL,
                    settings(Duration.ofSeconds(30), 1000, 100, Duration.ofMillis(100)));
            s.accept(record("a", T0, "1"));
            s.accept(record("b", T0, "2"));

            assertThat(s.flushAndClose(Duration.ofSeconds(2))).isTrue();

            assertThat(submitter.batches).hasSize(1);
            assertThat(submitter.batches.get(0)).hasSize(2);
            assertThat(s.isFinished()).isTrue();
            s.accept(record("c", T0, "3"));
            assertThat(s.droppedCount()).isEqualTo(1);
            assertThat(s.buffered()).isZero();
        }

        @Test
        @DisplayName("close waits for the outstanding batch")
        void closeWaitsForOutstanding() {
            submitter.hold = true;
            PolicyAwareRecordSink s = sink(IngestPolicy.PolicyMode.ALL,
                    settings(Duration.ofSeconds(30), 1000, 100, Duration.ofMillis(100)));
            s.accept(record("a", T0, "1"));

            assertThat(s.flushAndClose(Duration.ofMillis(300))).isFalse();
            submitter.completeAll();
            await().atMost(Duration.ofSeconds(2)).until(s::isFinished);
        }

        @Test
        @DisplayName("each flush is one STREAM row's provenance, stamped on accept")
        void flushProvenance() {
            PolicyAwareRecordSink s = sink(IngestPolicy.PolicyMode.ALL,
                    settings(Duration.ofSeconds(30), 1000, 100, Duration.ofMillis(100)));
            s.accept(record("a", T0, "1"));
            clock.advance(Duration.ofSeconds(1));
            s.accept(record("b", T0, "2"));
            clock.advance(Duration.ofSeconds(1));

            s.flushAndClose(Duration.ofSeconds(2));

            FetchLogEntry entry = submitter.logs.get(0);
            assertThat(entry.trigger()).isEqualTo(FetchTrigger.STREAM);
            assertThat(entry.status()).isEqualTo(FetchStatus.OK);
            assertThat(entry.startedAt()).isEqualTo(T0);
            assertThat(entry.receivedAt()).isEqualTo(T0.plusSeconds(2));
            assertThat(entry.licence()).isEqualTo("TEST-L");
            assertThat(entry.adapterVersion()).isEqualTo("1.0-test");
            assertThat(submitter.batches.get(0)).extracting(IngestRecord::receivedAt)
                    .containsExactly(T0, T0.plusSeconds(1));
            assertThat(listener.flushed).hasSize(1);
        }

        @Test
        @DisplayName("a batch the writer failed reaches the listener")
        void failureReported() {
            submitter.failWith = new IllegalStateException("boom");
            PolicyAwareRecordSink s = sink(IngestPolicy.PolicyMode.ALL,
                    settings(Duration.ofSeconds(30), 1000, 100, Duration.ofMillis(100)));
            s.accept(record("a", T0, "1"));

            s.flushAndClose(Duration.ofSeconds(2));

            assertThat(listener.failed).hasSize(1);
            assertThat(listener.flushed).isEmpty();
        }

        @Test
        @DisplayName("a full queue keeps the records buffered and retries them")
        void backpressureRetries() {
            submitter.refuseNext.set(1);
            PolicyAwareRecordSink s = sink(IngestPolicy.PolicyMode.ALL,
                    settings(Duration.ofMillis(100), 1000, 100, Duration.ofMillis(100)));
            s.accept(record("a", T0, "1"));
            s.accept(record("b", T0, "2"));

            await().atMost(Duration.ofSeconds(3)).until(() -> submitter.batches.size() == 1);
            assertThat(submitter.batches.get(0)).extracting(r -> r.record().externalId()).containsExactly("a", "b");
        }

        @Test
        @DisplayName("an unstarted sink still flushes on close")
        void unstarted() {
            sink = new PolicyAwareRecordSink(
                    FakeStreamingAdapter.defaultDescriptor(SOURCE).build(), IngestPolicy.PolicyMode.ALL, submitter,
                    listener, clock, settings(Duration.ofSeconds(30), 1000, 100, Duration.ofMillis(100)), "v", meters);
            sink.accept(record("a", T0, "1"));

            assertThat(sink.flushAndClose(Duration.ofSeconds(2))).isTrue();
            assertThat(submitter.batches).hasSize(1);
        }
    }
}
