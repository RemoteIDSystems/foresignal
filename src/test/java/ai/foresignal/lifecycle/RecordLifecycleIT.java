package ai.foresignal.lifecycle;

import static ai.foresignal.testsupport.ingest.TestIngestSources.LATEST;
import static ai.foresignal.testsupport.ingest.TestIngestSources.LATEST_KIND;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.ingest.IngestOutcome;
import ai.foresignal.ingest.IngestWriter;
import ai.foresignal.retention.RetentionJob;
import ai.foresignal.retention.RetentionRunResult;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.RecordsTestSources.StubRetentionState;
import ai.foresignal.testsupport.stream.SseTestClient;
import ai.foresignal.testsupport.stream.SseTestClient.Event;
import ai.foresignal.testsupport.stream.StreamTestSources;
import ai.foresignal.trigger.TriggerEngine;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * One entity through its whole life (prompt 022, task 5), asserted over the public API at every
 * step: ingest; a live-only update and a touch; a significant revision; a tombstone; reintroduction
 * (REAPPEAR, enter semantics per Core-Data-Model §11); asOf and version history at each transition
 * (§12); an SSE client that resumes from a cursor taken before the revision and receives every
 * change once, in order (§10.2); and two retention runs (§13), after which every historical query and
 * the old cursor either return correct results or the documented explicit errors
 * ({@code 400 ASOF_BEYOND_RETENTION}, an SSE {@code reset}, {@code 410 RESYNC_REQUIRED}), never
 * silently wrong data.
 *
 * <p>Three variants run the same scenario: plain; with the writer's database session killed in the
 * middle of the revision batch and the writer restarted (the transaction rolls back, seqs may gap, and
 * no version is duplicated or lost); and with two readers paging snapshots ({@code order=id}) and a
 * background producer ingesting throughout.
 *
 * <p>The entity lives on {@code test-latest} (LATEST, significant {@code name}, STANDARD tier, the
 * default 7 d window), so a {@code level} change is live-only and an identical observation after
 * {@code touchMin} (1 h) is a touch.
 */
@WebIntegrationTest
@Import(StreamTestSources.class)
class RecordLifecycleIT {

    static final Instant T0 = Instant.parse("2026-03-01T00:00:00Z");
    static final String ID = "life";
    static final Duration WAIT = Duration.ofSeconds(10);
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @LocalServerPort int port;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired StubRetentionState retention;
    @Autowired RetentionJob retentionJob;
    @Autowired TriggerEngine engine;
    @Autowired ai.foresignal.reference.RegionTagWorker regionWorker;
    @Autowired ChangeBus changeBus;
    @Autowired IngestWriter writer;
    @Autowired SseHub hub;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;

    private final List<SseTestClient> streams = new ArrayList<>();
    private String token;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        retention.reset();
        settings.invalidateCache();
        token = adminTokens.rotate();
    }

    @AfterEach
    void tearDown() {
        streams.forEach(SseTestClient::close);
        hub.clients().forEach(SseClient::close);
        retention.reset();
        writer.start();
    }

    @Test
    @DisplayName("the whole life of one entity, asserted at every transition")
    void plain() throws Exception {
        scenario(false);
    }

    @Test
    @DisplayName("the writer's session is killed mid-batch and the writer restarted: no duplicate or lost version")
    void writerKilledMidBatch() throws Exception {
        scenario(true);
    }

    @Test
    @DisplayName("two readers page stable snapshots while a producer ingests throughout the life")
    void concurrentReaders() throws Exception {
        Background background = new Background();
        background.start();
        try {
            scenario(false);
        } finally {
            background.stop();
        }
        assertThat(background.failures).as("background ingest and snapshot readers").isEmpty();
        assertThat(background.passes.get()).as("complete snapshot passes").isGreaterThanOrEqualTo(2);
        assertThat(background.batches.get()).as("background batches").isPositive();
    }

    // ------------------------------------------------------------------------- the scenario

    private void scenario(boolean killWriter) throws Exception {
        String ruleId = post("/v1/triggers", """
                {"name":"Named","sources":["%s"],"mode":"enter",
                 "conditions":[{"path":"name","op":"exists","value":true}]}""".formatted(LATEST)).get("id").asString();

        // (1) Ingest.
        Instant t1 = T0;
        IngestOutcome first = ingest(t1, rec(t1, "A", 1));
        assertThat(first.persisted()).isEqualTo(1);
        long seq1 = first.toSeq();
        assertCurrent("A", 1, 1);

        // A live client subscribed before the revision; its cursor and the resume cursor are seq1.
        SseTestClient live = sse("?source=" + LATEST);

        // (2a) A live-only update: no version, the live row moves.
        Instant tLive = T0.plus(Duration.ofMinutes(10));
        IngestOutcome liveOnly = ingest(tLive, rec(tLive, "A", 2));
        assertThat(liveOnly.persisted()).isZero();
        assertThat(liveOnly.live()).isEqualTo(1);
        JsonNode current = get("/v1/records/" + LATEST + "/" + ID);
        assertThat(current.get("version").asInt()).isEqualTo(1);
        assertThat(current.get("payload").get("level").asInt()).isEqualTo(2);
        assertThat(current.get("live").asBoolean()).isTrue();

        // (2b) An identical observation after touchMin: a touch, still no version.
        Instant tTouch = T0.plus(Duration.ofMinutes(80));
        IngestOutcome touch = ingest(tTouch, rec(tTouch, "A", 2));
        assertThat(touch.persisted()).isZero();
        assertThat(touch.touched()).isEqualTo(1);

        // (2c) A significant revision: version 2 (optionally with the writer killed mid-batch).
        Instant t2 = T0.plus(Duration.ofDays(1));
        IngestOutcome second = killWriter ? ingestKillingWriter(t2, rec(t2, "B", 3)) : ingest(t2, rec(t2, "B", 3));
        assertThat(second.persisted()).isEqualTo(1);
        long seq2 = second.toSeq();

        // (3) A tombstone.
        Instant t3 = T0.plus(Duration.ofDays(2));
        IngestOutcome third = ingest(t3, NormalizedRecord.tombstone(ID, LATEST_KIND, t3));
        assertThat(third.persisted()).isEqualTo(1);
        long seq3 = third.toSeq();

        // (4) Reintroduction: REAPPEAR, which counts as entering.
        Instant t4 = T0.plus(Duration.ofDays(3));
        IngestOutcome fourth = ingest(t4, rec(t4, "C", 4));
        assertThat(fourth.persisted()).isEqualTo(1);
        long seq4 = fourth.toSeq();
        assertThat(List.of(seq1, seq2, seq3, seq4)).isSorted().doesNotHaveDuplicates();
        assertCurrent("C", 4, 4);
        // asOf may not be in the future: move a minute past the last transition before reading back.
        clock.setInstant(t4.plus(Duration.ofMinutes(1)));

        // Version history: exactly four versions, contiguous version numbers, in seq order.
        JsonNode versions = get("/v1/records/" + LATEST + "/" + ID + "/versions").get("items");
        assertThat(versions).hasSize(4);
        assertThat(texts(versions, "persistReason")).containsExactly("FIRST", "SIGNIFICANT", "TOMBSTONE", "REAPPEAR");
        assertThat(ints(versions, "version")).containsExactly(1, 2, 3, 4);
        assertThat(longs(versions, "seq")).containsExactly(seq1, seq2, seq3, seq4);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM record_version WHERE source_id = ? AND external_id = ?",
                Long.class, LATEST, ID)).isEqualTo(4);

        // (5) asOf at every transition returns exactly what was known then.
        assertThat(asOf(t1.minusSeconds(1)).get("items")).isEmpty();
        assertAsOf(t1.plusSeconds(1), "A", 1);
        // asOf reads versions: after the live-only update and the touch it is still version 1 (level 1).
        JsonNode afterTouch = assertAsOf(tTouch.plusSeconds(1), "A", 1);
        assertThat(afterTouch.get("payload").get("level").asInt()).isEqualTo(1);
        assertAsOf(t2.plusSeconds(1), "B", 2);
        assertThat(asOf(t3.plusSeconds(1)).get("items")).as("tombstoned at t3").isEmpty();
        assertAsOf(t4.plusSeconds(1), "C", 4);

        // (6) The live client and a resumed client see every change after seq1 once, in order.
        assertThat(changes(live, 3)).containsExactly(seq2, seq3, seq4);
        SseTestClient resumed = sse("?source=" + LATEST + "&after=" + seq1);
        assertThat(changes(resumed, 3)).containsExactly(seq2, seq3, seq4);
        assertThat(resumed.collect("change", Duration.ofSeconds(1))).as("no duplicate after the replay").isEmpty();

        // The rule fired on FIRST and on REAPPEAR (enter), and on nothing else.
        await().atMost(WAIT).untilAsserted(() -> assertThat(get("/v1/alerts?ruleId=" + ruleId).get("items")).hasSize(2));

        // (7) Retention with the floor between the revision and the tombstone. The window is 7 d,
        // so at t4 + 6.5 d the cutoff time is t3 + 12 h, and cutoffSeq is the first seq of the latest
        // seq_clock minute at or before it (Core-Data-Model §8): the tombstone's minute, so v1 and v2
        // go and v3, v4 stay.
        RetentionRunResult firstRun = runRetention(t4.plus(Duration.ofHours(6 * 24 + 12)));
        assertThat(firstRun.skippedSources()).isEmpty();
        assertThat(firstRun.headsPurged()).isZero();
        assertThat(longs(get("/v1/records/" + LATEST + "/" + ID + "/versions").get("items"), "seq"))
                .containsExactly(seq3, seq4);
        assertCurrent("C", 4, 4);

        // (8) The historical queries again: correct results or the documented explicit errors.
        for (Instant t : List.of(t1.plusSeconds(1), t2.plusSeconds(1), t3.plusSeconds(1), t4.plusSeconds(1))) {
            Reply reply = send("GET", "/v1/records?source=" + LATEST + "&asOf=" + enc(t.toString()), null);
            assertThat(reply.status()).as("asOf %s below the horizon", t).isEqualTo(400);
            assertThat(reply.body().get("code").asString()).isEqualTo("ASOF_BEYOND_RETENTION");
        }
        assertAsOf(clock.instant(), "C", 4);
        // The old SSE cursor is below the lowest retained seq: an explicit reset, never a silent gap.
        SseTestClient stale = sse("?source=" + LATEST + "&after=" + seq1);
        Event reset = stale.next("reset", WAIT);
        assertThat(reset).as("reset for a cursor below the retention floor").isNotNull();
        assertThat(JSON.readTree(reset.data()).get("reason").asString()).isEqualTo("resync_required");
        assertThat(stale.received()).noneMatch(e -> e.event().equals("change"));
        // The change feed: routine version deletes do not advance purgeSeq (Conventions), so the old
        // cursor still pages, and what it returns is the retained truth (v3 and v4).
        assertThat(longs(get("/v1/changes?source=" + LATEST + "&after=" + seq1).get("items"), "seq"))
                .containsExactly(seq3, seq4);

        // Second run: a later batch (another entity, a day after t4) gives seq_clock a minute past
        // t4, so the cutoff can pass v4; v3 and v4 age out, the head is purged (I7: every version
        // gone) and purgeSeq advances.
        Instant marker = t4.plus(Duration.ofDays(1));
        clock.setInstant(marker);
        records.ingestAt(STANDARD, marker, NormalizedRecord.builder("marker", STANDARD_KIND).observedAt(marker)
                .point(0, 0).put("v", 1).build());
        RetentionRunResult secondRun = runRetention(marker.plus(Duration.ofDays(7)).plus(Duration.ofMinutes(1)));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM record_version WHERE source_id = ? AND external_id = ?",
                Long.class, LATEST, ID)).isZero();
        assertThat(secondRun.headsPurged()).isGreaterThanOrEqualTo(1);
        assertThat(send("GET", "/v1/records/" + LATEST + "/" + ID, null).status()).isEqualTo(404);
        Reply gone = send("GET", "/v1/changes?source=" + LATEST + "&after=" + seq1, null);
        assertThat(gone.status()).as("a cursor below purgeSeq").isEqualTo(410);
        assertThat(gone.body().get("code").asString()).isEqualTo("RESYNC_REQUIRED");
        assertThat(asOf(clock.instant()).get("items")).isEmpty();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM record_head h FULL JOIN record_live l"
                + " ON l.source_id = h.source_id AND l.external_id = h.external_id"
                + " WHERE h.source_id IS NULL OR l.source_id IS NULL", Long.class)).as("I10").isZero();
    }

    // ------------------------------------------------------------------------- the writer kill

    /**
     * Ingests {@code record} at {@code at} while the entity's head row is locked by another
     * transaction, so the writer blocks inside its batch transaction after allocating seqs and
     * inserting the version; then terminates the writer's database session, releases the lock,
     * restarts the writer and re-submits the batch if it failed (as a producer would). Exactly one
     * new version must result.
     */
    private IngestOutcome ingestKillingWriter(Instant at, NormalizedRecord record) throws Exception {
        clock.setInstant(at);
        long before = writer.lastAllocatedSeq();
        CompletableFuture<Void> locked = new CompletableFuture<>();
        CompletableFuture<Void> release = new CompletableFuture<>();
        TransactionTemplate tx = new TransactionTemplate(transactions);
        Thread holder = Thread.ofVirtual().start(() -> tx.executeWithoutResult(status -> {
            jdbc.queryForList("SELECT 1 FROM record_head WHERE source_id = ? AND external_id = ? FOR UPDATE", LATEST, ID);
            locked.complete(null);
            release.join();
        }));
        locked.get(10, TimeUnit.SECONDS);
        CompletableFuture<IngestOutcome> pending = records.submitAt(LATEST, at, List.of(record));
        Integer pid = null;
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (pid == null && System.nanoTime() < deadline) {
            List<Integer> blocked = jdbc.queryForList("SELECT pid FROM pg_stat_activity WHERE wait_event_type = 'Lock'"
                    + " AND query ILIKE '%record_head%' AND pid <> pg_backend_pid()", Integer.class);
            pid = blocked.isEmpty() ? null : blocked.getFirst();
            if (pid == null) {
                Thread.sleep(5);
            }
        }
        assertThat(pid).as("the writer blocked on the locked head inside its batch").isNotNull();
        jdbc.queryForObject("SELECT pg_terminate_backend(?)", Boolean.class, pid);
        release.complete(null);
        holder.join(10_000);

        IngestOutcome outcome;
        try {
            outcome = pending.get(30, TimeUnit.SECONDS);
        } catch (ExecutionException killed) {
            outcome = null;
        }
        // Restart: stop drains and fails nothing left; start reloads state from the database.
        writer.stop();
        writer.start();
        if (outcome == null) {
            outcome = records.ingestAt(LATEST, at, record);
        }
        assertThat(writer.lastAllocatedSeq()).as("the killed attempt's seqs are not reused").isGreaterThan(before);
        return outcome;
    }

    // ------------------------------------------------------------------------- background load

    /**
     * A producer revising 30 {@code test-standard} entities, and two readers paging the current
     * snapshot of both sources with {@code order=id}: every pass must be complete, duplicate-free and
     * pinned (no item newer than the page's {@code asOfSeq}).
     */
    private final class Background {

        final List<String> failures = new CopyOnWriteArrayList<>();
        final AtomicInteger passes = new AtomicInteger();
        final AtomicInteger batches = new AtomicInteger();
        final AtomicBoolean running = new AtomicBoolean(true);
        final List<Thread> threads = new ArrayList<>();

        void start() {
            threads.add(Thread.ofVirtual().name("lifecycle-producer").start(this::produce));
            threads.add(Thread.ofVirtual().name("lifecycle-reader-1").start(this::read));
            threads.add(Thread.ofVirtual().name("lifecycle-reader-2").start(this::read));
        }

        void stop() throws InterruptedException {
            running.set(false);
            for (Thread thread : threads) {
                thread.join(30_000);
            }
        }

        private void produce() {
            int round = 0;
            while (running.get()) {
                Instant now = clock.instant();
                List<NormalizedRecord> batch = new ArrayList<>();
                for (int i = 0; i < 30; i++) {
                    batch.add(NormalizedRecord.builder("bg-" + i, STANDARD_KIND).observedAt(now).point(i, 1)
                            .put("round", round).build());
                }
                try {
                    TestRecords.await(records.submitAt(STANDARD, now, batch));
                    batches.incrementAndGet();
                } catch (RuntimeException e) {
                    failures.add("producer: " + e);
                }
                round++;
                sleep(20);
            }
        }

        private void read() {
            while (running.get()) {
                try {
                    Set<String> seen = new HashSet<>();
                    String cursor = null;
                    Long pinned = null;
                    do {
                        Reply page = send("GET", "/v1/records?source=" + LATEST + "&source=" + STANDARD
                                + "&order=id&limit=7" + (cursor == null ? "" : "&cursor=" + enc(cursor)), null);
                        if (page.status() != 200) {
                            failures.add("reader: status " + page.status() + " " + new String(page.bytes(), StandardCharsets.UTF_8));
                            break;
                        }
                        JsonNode body = page.body();
                        long asOfSeq = body.get("asOfSeq").asLong();
                        if (pinned == null) {
                            pinned = asOfSeq;
                        } else if (pinned != asOfSeq) {
                            failures.add("reader: asOfSeq moved within a pass " + pinned + " -> " + asOfSeq);
                        }
                        for (JsonNode item : body.get("items")) {
                            String key = item.get("source").asString() + "/" + item.get("externalId").asString();
                            if (!seen.add(key)) {
                                failures.add("reader: duplicate " + key + " within one pass");
                            }
                            if (item.get("seq").asLong() > asOfSeq) {
                                failures.add("reader: " + key + " seq " + item.get("seq").asLong() + " above asOfSeq " + asOfSeq);
                            }
                        }
                        JsonNode next = body.get("nextCursor");
                        cursor = next == null || next.isNull() ? null : next.asString();
                    } while (cursor != null && running.get());
                    if (cursor == null) {
                        passes.incrementAndGet();
                    }
                } catch (RuntimeException e) {
                    failures.add("reader: " + e);
                }
                sleep(10);
            }
        }
    }

    // ------------------------------------------------------------------------- helpers

    private static NormalizedRecord rec(Instant observedAt, String name, int level) {
        return NormalizedRecord.builder(ID, LATEST_KIND).observedAt(observedAt).point(10, 20)
                .put("name", name).put("level", level).build();
    }

    private IngestOutcome ingest(Instant at, NormalizedRecord record) {
        clock.setInstant(at);
        return records.ingestAt(LATEST, at, record);
    }

    private RetentionRunResult runRetention(Instant now) {
        await().atMost(Duration.ofSeconds(30)).until(() -> engine.cursor() >= changeBus.lastPublishedSeq());
        // The region tag worker's cursor joins the clamp too (prompt 040).
        await().atMost(Duration.ofSeconds(30)).until(() -> !regionWorker.running()
                || regionWorker.cursor() >= changeBus.lastPublishedSeq());
        clock.setInstant(now);
        return retentionJob.run();
    }

    private void assertCurrent(String name, int level, int version) {
        JsonNode current = get("/v1/records/" + LATEST + "/" + ID);
        assertThat(current.get("payload").get("name").asString()).isEqualTo(name);
        assertThat(current.get("payload").get("level").asInt()).isEqualTo(level);
        assertThat(current.get("version").asInt()).isEqualTo(version);
        assertThat(current.get("tombstone").asBoolean()).isFalse();
    }

    private JsonNode asOf(Instant t) {
        JsonNode page = get("/v1/records?source=" + LATEST + "&asOf=" + enc(t.toString()));
        assertThat(page.get("asOf").asString()).isEqualTo(t.toString());
        return page;
    }

    private JsonNode assertAsOf(Instant t, String name, int version) {
        JsonNode items = asOf(t).get("items");
        assertThat(items).as("asOf %s", t).hasSize(1);
        JsonNode item = items.get(0);
        assertThat(item.get("externalId").asString()).isEqualTo(ID);
        assertThat(item.get("version").asInt()).as("asOf %s version", t).isEqualTo(version);
        assertThat(item.get("payload").get("name").asString()).isEqualTo(name);
        return item;
    }

    private SseTestClient sse(String query) {
        SseTestClient client = SseTestClient.connect("http://localhost:" + port, "/v1/changes/stream" + query,
                Map.of("Authorization", "Bearer " + token));
        assertThat(client.status()).isEqualTo(200);
        streams.add(client);
        return client;
    }

    /** The seqs of the next {@code count} change events (each waited for), by their {@code id:} lines. */
    private static List<Long> changes(SseTestClient client, int count) {
        List<Long> seqs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Event change = client.next("change", WAIT);
            assertThat(change).as("change %d of %d", i + 1, count).isNotNull();
            assertThat(JSON.readTree(change.data()).get("externalId").asString()).isEqualTo(ID);
            seqs.add(Long.parseLong(change.id()));
        }
        return seqs;
    }

    private static List<String> texts(JsonNode array, String field) {
        List<String> out = new ArrayList<>();
        array.forEach(n -> out.add(n.get(field).asString()));
        return out;
    }

    private static List<Integer> ints(JsonNode array, String field) {
        List<Integer> out = new ArrayList<>();
        array.forEach(n -> out.add(n.get(field).asInt()));
        return out;
    }

    private static List<Long> longs(JsonNode array, String field) {
        List<Long> out = new ArrayList<>();
        array.forEach(n -> out.add(n.get(field).asLong()));
        return out;
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private record Reply(int status, byte[] bytes) {
        JsonNode body() {
            return bytes.length == 0 ? JSON.nullNode() : JSON.readTree(bytes);
        }
    }

    private JsonNode get(String path) {
        Reply reply = send("GET", path, null);
        assertThat(reply.status()).as("GET %s: %s", path, new String(reply.bytes(), StandardCharsets.UTF_8)).isEqualTo(200);
        return reply.body();
    }

    private JsonNode post(String path, String body) {
        Reply reply = send("POST", path, body);
        assertThat(reply.status()).as("POST %s: %s", path, new String(reply.bytes(), StandardCharsets.UTF_8)).isEqualTo(201);
        return reply.body();
    }

    private Reply send(String method, String path, String body) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + token);
        if (body == null) {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            request.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        }
        try {
            HttpResponse<byte[]> response = HTTP.send(request.build(), HttpResponse.BodyHandlers.ofByteArray());
            return new Reply(response.statusCode(), response.body());
        } catch (IOException e) {
            throw new IllegalStateException(method + " " + path + " failed", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(method + " " + path + " interrupted", e);
        }
    }

    @SuppressWarnings("unused")
    private static Map<String, Object> map(Object... pairs) {
        Map<String, Object> out = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            out.put((String) pairs[i], pairs[i + 1]);
        }
        return out;
    }
}
