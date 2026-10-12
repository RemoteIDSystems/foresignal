package ai.foresignal.perf;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ingest.IngestBackpressureException;
import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.IngestQueue;
import ai.foresignal.ingest.IngestWriter;
import ai.foresignal.retention.RetentionJob;
import ai.foresignal.retention.RetentionRunResult;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.store.history.LeafInfo;
import ai.foresignal.store.history.PartitionManager;
import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.stream.sse.StreamLimits;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.stream.StreamTestSources;
import ai.foresignal.trigger.TriggerEngine;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The performance smoke of prompt 022, a gate (Roadmap §2, 000 stop conditions): synthetic ingest
 * through the real writer at the NFR-2 rate (20,000 versions/min) for 10 minutes and at 2× for 2
 * minutes, with 20 SSE subscribers and one asOf reader running; meanwhile a partition is created and
 * one is dropped (DETACH CONCURRENTLY, the retention path), and the retention job runs.
 *
 * <p>It records sustained throughput, commit latency p50/p95/p99 (submit to committed outcome,
 * queueing included), change-to-SSE delay p95 (NFR-3: ≤ 2 s; from the committed outcome to the
 * subscriber reading the event, so it is a slight under-estimate by the publish-to-complete gap of
 * microseconds), the heap high-water mark per SSE emitter, and writer back-pressure. It fails only
 * when the NFR-2 rate is not sustained, NFR-3 is missed, a subscriber loses or duplicates a change,
 * or an SSE emitter's FIFO lane grows past {@code clientBuffer} (the SSE memory bound). The numbers are printed as {@code PERF ...} lines.
 *
 * <p>Run with {@code ./gradlew perfTest}; {@code ./gradlew test} excludes the {@code perf} tag.
 */
@Tag("perf")
@WebIntegrationTest
@Import(StreamTestSources.class)
class PerformanceSmokeIT {

    /** NFR-2: persisted versions per minute. */
    static final int NFR2_PER_MINUTE = 20_000;

    /** NFR-3: change visible on the SSE stream within this many milliseconds of commit. */
    static final long NFR3_MILLIS = 2_000;

    static final Duration BASE_PHASE = Duration.ofMinutes(Integer.getInteger("foresignal.perf.baseMinutes", 10));
    static final Duration DOUBLE_PHASE = Duration.ofMinutes(Integer.getInteger("foresignal.perf.doubleMinutes", 2));
    static final int SUBSCRIBERS = 20;
    static final int ENTITIES = 5_000;
    static final long TICK_MILLIS = 500;

    /**
     * NFR-1's heap (512 MB), against which the SSE worst case is reported. The SSE memory budget is
     * stated only as a bound, "clientBuffer × maxClients plus the conflation maps" (prompt 015 report,
     * note 6), with no number: the gate is that no emitter's FIFO lane grows past {@code clientBuffer}
     * and none overflows; the worst case in bytes is reported for the owner.
     */
    static final long NFR1_HEAP_BYTES = 512L * 1024 * 1024;

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @LocalServerPort int port;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired IngestQueue queue;
    @Autowired SseHub hub;
    @Autowired AppSettingService settings;
    @Autowired PartitionManager partitions;
    @Autowired RetentionJob retentionJob;
    @Autowired IngestWriter writer;
    @Autowired TriggerEngine triggerEngine;
    @Autowired JdbcTemplate jdbc;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final List<Thread> threads = new CopyOnWriteArrayList<>();
    private final List<String> failures = new CopyOnWriteArrayList<>();
    private final List<Subscriber> subscribers = new ArrayList<>();

    /** Batch fromSeq → {toSeq, completion nanos}, for the change-to-SSE delay. */
    private final ConcurrentSkipListMap<Long, long[]> commits = new ConcurrentSkipListMap<>();

    @AfterEach
    void tearDown() throws InterruptedException {
        running.set(false);
        subscribers.forEach(Subscriber::close);
        hub.clients().forEach(SseClient::close);
        for (Thread thread : threads) {
            thread.join(10_000);
        }
        settings.invalidateCache();
        partitions.refresh();
        partitions.ensureAhead();
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.MINUTES)
    @DisplayName("NFR-2 for 10 min and 2× for 2 min, with 20 SSE subscribers, an asOf reader and partition maintenance")
    void smoke() throws Exception {
        report("host", System.getProperty("os.name") + " " + System.getProperty("os.arch") + ", "
                + Runtime.getRuntime().availableProcessors() + " cpus, max heap "
                + mb(Runtime.getRuntime().maxMemory()) + " MB, java " + System.getProperty("java.version"));
        settings.put(SettingKeys.STREAM_LIMITS, new StreamLimits(0, 0, 0, 0, 0, 0, 0, SUBSCRIBERS + 4));
        // Move the allocator past the first TRACK block, so rv_t_0 is wholly behind it and the
        // retention drop below is legal (PartitionManager refuses leaves the allocator has not passed).
        long jump = partitions.block(HistoryTier.TRACK);
        jdbc.queryForObject("SELECT setval('record_seq', ?)", Long.class, jump);
        settings.put(SettingKeys.TRIGGER_CURSOR, jump);
        triggerEngine.resetState();
        writer.resetState();
        partitions.refresh();
        partitions.ensureAhead();
        StreamLimits limits = hub.limits();

        // A ticking clock: the application clock follows real time, so ingested_at, seq_clock and
        // asOf behave as in production.
        clock.setInstant(Instant.now());
        daemon("perf-clock", () -> {
            while (running.get()) {
                clock.setInstant(Instant.now());
                sleep(20);
            }
        });

        // Heap before and after connecting the subscribers (post-GC), for the static footprint.
        long heapBefore = gcUsedHeap();
        for (int i = 0; i < SUBSCRIBERS; i++) {
            subscribers.add(new Subscriber(i));
        }
        awaitClients(SUBSCRIBERS);
        long heapWithClients = gcUsedHeap();
        long staticPerEmitter = Math.max(0, (heapWithClients - heapBefore) / SUBSCRIBERS);
        report("sse.static-per-emitter", kb(staticPerEmitter) + " KB (post-GC heap delta of " + SUBSCRIBERS
                + " idle streams, server and test-client sides together)");

        // Samplers: FIFO depth per emitter (reflection on SseClient's private counter; test-only),
        // post-GC live heap, and the ingest queue depth.
        AtomicInteger maxFifo = new AtomicInteger();
        AtomicInteger maxQueue = new AtomicInteger();
        AtomicLong maxLiveHeap = new AtomicLong();
        Field fifoEvents = SseClient.class.getDeclaredField("fifoEvents");
        fifoEvents.setAccessible(true);
        daemon("perf-sampler", () -> {
            while (running.get()) {
                for (SseClient client : hub.clients()) {
                    try {
                        maxFifo.accumulateAndGet(fifoEvents.getInt(client), Math::max);
                    } catch (IllegalAccessException e) {
                        failures.add("sampler: " + e);
                    }
                }
                maxQueue.accumulateAndGet(queue.depth(), Math::max);
                maxLiveHeap.accumulateAndGet(postGcLiveHeap(), Math::max);
                sleep(100);
            }
        });

        // The asOf reader.
        List<Long> asOfMicros = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger asOfErrors = new AtomicInteger();
        daemon("perf-asof", () -> {
            while (running.get()) {
                Instant t = clock.instant().minusSeconds(30);
                long start = System.nanoTime();
                try {
                    HttpResponse<byte[]> response = HTTP.send(HttpRequest.newBuilder(URI.create(base()
                                    + "/v1/records?source=" + STANDARD + "&limit=200&asOf="
                                    + URLEncoder.encode(t.toString(), StandardCharsets.UTF_8)))
                            .timeout(Duration.ofSeconds(60)).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
                    if (response.statusCode() != 200) {
                        asOfErrors.incrementAndGet();
                    }
                } catch (Exception e) {
                    asOfErrors.incrementAndGet();
                }
                asOfMicros.add((System.nanoTime() - start) / 1_000);
                sleep(1_000);
            }
        });

        // Phase 1: NFR-2 for 10 minutes, with partition maintenance and a retention run halfway.
        Phase base = new Phase("1x", NFR2_PER_MINUTE, BASE_PHASE);
        Thread maintenance = daemon("perf-maintenance", () -> {
            sleep(BASE_PHASE.toMillis() / 2);
            maintenanceUnderLoad();
        });
        base.run();
        maintenance.join(120_000);

        // Phase 2: 2× for 2 minutes.
        Phase doubled = new Phase("2x", 2 * NFR2_PER_MINUTE, DOUBLE_PHASE);
        doubled.run();

        // Let the subscribers drain, then stop everything.
        long expected = base.persisted.get() + doubled.persisted.get();
        long drainDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (System.nanoTime() < drainDeadline && subscribers.stream().anyMatch(s -> s.changes.get() < expected)) {
            sleep(100);
        }
        running.set(false);

        // ---- Numbers.
        base.print();
        doubled.print();
        List<Long> commitAll = new ArrayList<>(base.latencies);
        commitAll.addAll(doubled.latencies);
        report("commit-latency.all", percentiles(commitAll));

        long[] delay = delayPercentiles();
        report("change-to-sse", "p50 " + delay[0] + " ms, p95 " + delay[1] + " ms, p99 " + delay[2] + " ms, max "
                + delay[3] + " ms over " + delay[4] + " events (" + SUBSCRIBERS + " subscribers)");
        for (Subscriber s : subscribers) {
            if (s.changes.get() != expected || s.resets.get() > 0 || s.duplicates.get() > 0) {
                failures.add("subscriber " + s.index + ": " + s.changes.get() + " of " + expected + " changes, "
                        + s.resets.get() + " reset/resync, " + s.duplicates.get() + " out-of-order or duplicate");
            }
        }
        report("sse.subscribers", subscribers.stream().map(s -> Long.toString(s.changes.get())).toList()
                + " changes each, expected " + expected);

        List<Long> asOf = new ArrayList<>(asOfMicros);
        report("asof-reader", asOf.size() + " queries, " + asOfErrors.get() + " errors, latency "
                + percentiles(asOf));
        report("ingest-queue.max-depth", maxQueue.get() + " of " + queue.capacity());
        report("backpressure", "submits blocked > 100 ms: " + (base.blocked.get() + doubled.blocked.get())
                + ", rejected: " + (base.rejected.get() + doubled.rejected.get())
                + ", failed batches: " + (base.failed.get() + doubled.failed.get()));

        long eventBytes = bytesPerQueuedEvent();
        long perEmitterHighWater = staticPerEmitter + (long) maxFifo.get() * eventBytes;
        long perEmitterWorst = staticPerEmitter + (long) limits.clientBuffer() * eventBytes;
        long worstAll = perEmitterWorst * limits.maxClients();
        report("sse.fifo-high-water", maxFifo.get() + " events (clientBuffer " + limits.clientBuffer() + ")");
        report("sse.bytes-per-queued-event", eventBytes + " B (post-GC heap of 20,000 parsed change events / 20,000)");
        report("sse.heap-per-emitter.high-water", kb(perEmitterHighWater) + " KB");
        report("sse.heap-per-emitter.worst-case", kb(perEmitterWorst) + " KB; x maxClients " + limits.maxClients()
                + " = " + mb(worstAll) + " MB (" + (100 * worstAll / NFR1_HEAP_BYTES) + "% of NFR-1's 512 MB heap)");
        report("heap.post-gc-live-high-water", mb(maxLiveHeap.get()) + " MB (whole JVM: server, database client, "
                + SUBSCRIBERS + " test readers)");

        // ---- Gates.
        assertThat(failures).as("subscriber, sampler and maintenance failures").isEmpty();
        assertThat(base.failed.get() + doubled.failed.get()).as("failed batches").isZero();
        assertThat(base.achievedPerMinute()).as("NFR-2: sustained versions/min at 1x").isGreaterThanOrEqualTo(NFR2_PER_MINUTE);
        assertThat(doubled.achievedPerMinute()).as("sustained versions/min at 2x").isGreaterThanOrEqualTo(NFR2_PER_MINUTE);
        assertThat(delay[1]).as("NFR-3: change-to-SSE p95 (ms)").isLessThanOrEqualTo(NFR3_MILLIS);
        assertThat(maxFifo.get()).as("SSE memory bound: FIFO lane high-water within clientBuffer")
                .isLessThanOrEqualTo(limits.clientBuffer());
    }

    // ------------------------------------------------------------------------- the load

    /** One paced ingest phase: a batch every {@value #TICK_MILLIS} ms at absolute deadlines. */
    private final class Phase {

        final String name;
        final int perMinute;
        final Duration length;
        final AtomicLong persisted = new AtomicLong();
        final AtomicInteger failed = new AtomicInteger();
        final AtomicInteger rejected = new AtomicInteger();
        final AtomicInteger blocked = new AtomicInteger();
        final List<Long> latencies = Collections.synchronizedList(new ArrayList<>());
        long firstSubmit;
        final AtomicLong lastCompletion = new AtomicLong();
        final AtomicInteger round = new AtomicInteger();

        Phase(String name, int perMinute, Duration length) {
            this.name = name;
            this.perMinute = perMinute;
            this.length = length;
        }

        void run() throws Exception {
            int ticks = (int) (length.toMillis() / TICK_MILLIS);
            double perTick = perMinute * (TICK_MILLIS / 60_000.0);
            List<CompletableFuture<?>> pending = new ArrayList<>();
            firstSubmit = System.nanoTime();
            double carry = 0;
            for (int tick = 0; tick < ticks; tick++) {
                long deadline = firstSubmit + TimeUnit.MILLISECONDS.toNanos(tick * TICK_MILLIS);
                long wait = deadline - System.nanoTime();
                if (wait > 0) {
                    TimeUnit.NANOSECONDS.sleep(wait);
                }
                carry += perTick;
                int n = (int) carry;
                carry -= n;
                Instant now = clock.instant();
                int r = round.getAndIncrement();
                List<NormalizedRecord> batch = new ArrayList<>(n);
                for (int i = 0; i < n; i++) {
                    int entity = (int) ((((long) r) * n + i) % ENTITIES);
                    batch.add(NormalizedRecord.builder(name + "-" + entity, STANDARD_KIND).observedAt(now)
                            .point(-179 + (entity % 358), -80 + (entity % 160)).put("v", r).put("n", entity).build());
                }
                long submit = System.nanoTime();
                CompletableFuture<IngestOutcome> future;
                try {
                    future = records.submitAt(STANDARD, now, batch);
                } catch (IngestBackpressureException e) {
                    rejected.incrementAndGet();
                    continue;
                }
                if (System.nanoTime() - submit > TimeUnit.MILLISECONDS.toNanos(100)) {
                    blocked.incrementAndGet();
                }
                pending.add(future.whenComplete((outcome, error) -> {
                    long done = System.nanoTime();
                    if (error != null) {
                        failed.incrementAndGet();
                        return;
                    }
                    if (outcome.persisted() > 0) {
                        commits.put(outcome.fromSeq(), new long[] {outcome.toSeq(), done});
                    }
                    persisted.addAndGet(outcome.persisted());
                    latencies.add((done - submit) / 1_000);
                    lastCompletion.accumulateAndGet(done, Math::max);
                }));
            }
            CompletableFuture.allOf(pending.toArray(CompletableFuture[]::new)).exceptionally(e -> null)
                    .get(2, TimeUnit.MINUTES);
        }

        double achievedPerMinute() {
            double minutes = (lastCompletion.get() - firstSubmit) / 60e9;
            return minutes <= 0 ? 0 : persisted.get() / minutes;
        }

        void print() {
            report("throughput." + name, String.format(Locale.ROOT, "%,d versions in %.1f min = %,.0f versions/min"
                            + " (offered %,d/min; NFR-2 %,d/min, headroom %+.1f%%)",
                    persisted.get(), (lastCompletion.get() - firstSubmit) / 60e9, achievedPerMinute(), perMinute,
                    NFR2_PER_MINUTE, 100.0 * (achievedPerMinute() / NFR2_PER_MINUTE - 1)));
            report("commit-latency." + name, percentiles(latencies));
        }
    }

    /** Halfway through phase 1: create a partition, drop one (DETACH CONCURRENTLY), run retention. */
    private void maintenanceUnderLoad() {
        try {
            long start = System.nanoTime();
            long covered = partitions.coveredUpTo(HistoryTier.STANDARD, 1);
            partitions.ensureFor(HistoryTier.STANDARD, covered + partitions.block(HistoryTier.STANDARD));
            report("maintenance.create", "STANDARD leaf beyond " + covered + " in "
                    + (System.nanoTime() - start) / 1_000_000 + " ms");

            partitions.refresh();
            List<LeafInfo> track = partitions.leaves(HistoryTier.TRACK).stream()
                    .sorted((a, b) -> Long.compare(a.fromSeq(), b.fromSeq())).toList();
            LeafInfo oldest = track.getFirst();
            long dropStart = System.nanoTime();
            partitions.drop(HistoryTier.TRACK, oldest.name());
            report("maintenance.drop", oldest.name() + " [" + oldest.fromSeq() + ", " + oldest.toSeq()
                    + ") detached concurrently and dropped in " + (System.nanoTime() - dropStart) / 1_000_000 + " ms");

            long retentionStart = System.nanoTime();
            RetentionRunResult result = retentionJob.run();
            report("maintenance.retention", "run in " + (System.nanoTime() - retentionStart) / 1_000_000
                    + " ms, versionsDeleted " + result.versionsDeleted() + ", skipped " + result.skippedSources());
        } catch (RuntimeException e) {
            failures.add("maintenance: " + e);
        }
    }

    // ------------------------------------------------------------------------- subscribers

    /** A lean SSE reader: parses frames, counts changes, records the delay, keeps nothing else. */
    private final class Subscriber {

        final int index;
        final AtomicLong changes = new AtomicLong();
        final AtomicInteger resets = new AtomicInteger();
        final AtomicInteger duplicates = new AtomicInteger();
        final AtomicLongArray histogram = DELAYS;
        volatile InputStream body;

        Subscriber(int index) {
            this.index = index;
            daemon("perf-sse-" + index, this::read);
        }

        private void read() {
            try {
                HttpResponse<InputStream> response = HTTP.send(HttpRequest.newBuilder(
                                URI.create(base() + "/v1/changes/stream?source=" + STANDARD + "&live=false"))
                        .header("Accept", "text/event-stream").GET().build(), HttpResponse.BodyHandlers.ofInputStream());
                if (response.statusCode() != 200) {
                    failures.add("subscriber " + index + ": status " + response.statusCode());
                    return;
                }
                body = response.body();
                long lastSeq = 0;
                String id = null;
                String event = null;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.isEmpty()) {
                            if ("change".equals(event) && id != null) {
                                long seq = Long.parseLong(id);
                                record(seq, System.nanoTime());
                                if (seq <= lastSeq) {
                                    duplicates.incrementAndGet();
                                }
                                lastSeq = seq;
                                changes.incrementAndGet();
                            } else if ("reset".equals(event) || "resync".equals(event)) {
                                resets.incrementAndGet();
                            }
                            id = null;
                            event = null;
                        } else if (line.startsWith("id:")) {
                            id = line.substring(3).strip();
                        } else if (line.startsWith("event:")) {
                            event = line.substring(6).strip();
                        }
                    }
                }
            } catch (Exception e) {
                if (running.get()) {
                    failures.add("subscriber " + index + ": " + e);
                }
            }
        }

        private void record(long seq, long arrival) {
            Map.Entry<Long, long[]> batch = commits.floorEntry(seq);
            long millis = 0;
            if (batch != null && batch.getValue()[0] >= seq) {
                millis = Math.max(0, (arrival - batch.getValue()[1]) / 1_000_000);
            }
            // No entry yet: the event arrived before the batch's future completed, a delay of 0.
            histogram.incrementAndGet((int) Math.min(millis, DELAYS.length() - 1));
        }

        void close() {
            try {
                InputStream stream = body;
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
                // closing
            }
        }
    }

    /** Change-to-SSE delays in 1 ms buckets up to 60 s, over every subscriber. */
    static final AtomicLongArray DELAYS = new AtomicLongArray(60_001);

    /** {p50, p95, p99, max, count} of {@link #DELAYS}. */
    private static long[] delayPercentiles() {
        long total = 0;
        long max = 0;
        for (int i = 0; i < DELAYS.length(); i++) {
            long c = DELAYS.get(i);
            total += c;
            if (c > 0) {
                max = i;
            }
        }
        long[] out = new long[] {0, 0, 0, max, total};
        double[] q = {0.50, 0.95, 0.99};
        for (int k = 0; k < q.length; k++) {
            long rank = (long) Math.ceil(q[k] * total);
            long seen = 0;
            for (int i = 0; i < DELAYS.length(); i++) {
                seen += DELAYS.get(i);
                if (seen >= rank && rank > 0) {
                    out[k] = i;
                    break;
                }
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- measurement helpers

    /** Bytes of heap one queued change event takes, measured on 20,000 parsed copies of a real one. */
    private long bytesPerQueuedEvent() throws Exception {
        String sample = sampleChangeData();
        long before = gcUsedHeap();
        List<Object> held = new ArrayList<>(20_000);
        for (int i = 0; i < 20_000; i++) {
            held.add(JSON.readValue(sample, LinkedHashMap.class));
        }
        long after = gcUsedHeap();
        long per = Math.max(1, (after - before) / held.size());
        held.clear();
        return per;
    }

    private String sampleChangeData() throws Exception {
        HttpResponse<String> page = HTTP.send(HttpRequest.newBuilder(URI.create(base()
                        + "/v1/changes?source=" + STANDARD + "&limit=1")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        JsonNode item = JSON.readTree(page.body()).get("items").get(0);
        return JSON.writeValueAsString(item);
    }

    private static long gcUsedHeap() {
        for (int i = 0; i < 3; i++) {
            System.gc();
            sleep(200);
        }
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    /** The live heap after the most recent collection of each heap pool. */
    private static long postGcLiveHeap() {
        long sum = 0;
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getType() == MemoryType.HEAP) {
                MemoryUsage usage = pool.getCollectionUsage();
                if (usage != null) {
                    sum += usage.getUsed();
                }
            }
        }
        return sum;
    }

    private void awaitClients(int count) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (hub.clients().size() < count && System.nanoTime() < deadline) {
            sleep(50);
        }
        assertThat(hub.clients()).as("connected subscribers").hasSize(count);
    }

    private static String percentiles(List<Long> micros) {
        if (micros.isEmpty()) {
            return "no samples";
        }
        List<Long> sorted = new ArrayList<>(micros);
        Collections.sort(sorted);
        return String.format(Locale.ROOT, "p50 %.1f ms, p95 %.1f ms, p99 %.1f ms, max %.1f ms (n=%d)",
                at(sorted, 0.50) / 1000.0, at(sorted, 0.95) / 1000.0, at(sorted, 0.99) / 1000.0,
                sorted.getLast() / 1000.0, sorted.size());
    }

    private static long at(List<Long> sorted, double q) {
        int index = (int) Math.ceil(q * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }

    private String base() {
        return "http://localhost:" + port;
    }

    private Thread daemon(String name, Runnable body) {
        Thread thread = Thread.ofPlatform().daemon().name(name).start(body);
        threads.add(thread);
        return thread;
    }

    private static void report(String key, String value) {
        System.out.println("PERF " + key + ": " + value);
    }

    private static long kb(long bytes) {
        return bytes / 1024;
    }

    private static long mb(long bytes) {
        return bytes / (1024 * 1024);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
