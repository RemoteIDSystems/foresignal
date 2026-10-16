package ai.foresignal.sink;

import static ai.foresignal.testsupport.sink.SinkTestConfig.FEED;
import static ai.foresignal.testsupport.sink.SinkTestConfig.FEED_TYPE;
import static ai.foresignal.testsupport.sink.SinkTestConfig.FORBIDDEN;
import static ai.foresignal.testsupport.sink.SinkTestConfig.KIND;
import static ai.foresignal.testsupport.sink.SinkTestConfig.OPEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.records.RecordsTestSources.StubRetentionState;
import ai.foresignal.testsupport.sink.SinkTestConfig;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * CHANGE_FEED cursors (prompt 110): at-least-once delivery in seq order; the cursor advances only after an
 * acknowledgement and is persisted; a crash mid-page resumes without loss; a record the gate refuses is skipped
 * and counted and the cursor moves past it (a denied record never stalls the sink or holds back retention); a new
 * cursor and the purge horizon both snapshot current state as {@code SNAPSHOT} events and then tail; the 019
 * retention clamp includes the sink's cursor.
 */
@IntegrationTest
@Import(SinkTestConfig.class)
class ChangeFeedCursorIT {

    static final Duration WAIT = Duration.ofSeconds(30);
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired SinkService sinks;
    @Autowired SinkSupervisor supervisor;
    @Autowired SinkCursorRepository cursors;
    @Autowired SinkCursorConsumer consumer;
    @Autowired TestRecords records;
    @Autowired StubRetentionState retention;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        FEED.reset();
        retention.reset();
        supervisor.timings(null, Duration.ofMillis(200));
    }

    @AfterEach
    void tearDown() {
        supervisor.stopAll();
        supervisor.timings(null, null);
        retention.reset();
    }

    void point(String source, String id) {
        records.ingest(source, NormalizedRecord.builder(id, KIND).observedAt(Instant.now()).point(10, 20)
                .put("n", id).build());
    }

    long maxSeq() {
        Long seq = jdbc.queryForObject("SELECT max(seq) FROM record_version", Long.class);
        return seq == null ? 0 : seq;
    }

    void create(String id) {
        sinks.create(JSON.readTree("{\"id\":\"" + id + "\",\"type\":\"" + FEED_TYPE + "\",\"name\":\"Feed\","
                + "\"enabled\":true,\"config\":{},\"filter\":{\"sources\":[\"" + OPEN + "\",\"" + FORBIDDEN + "\"]}}"));
    }

    SinkCursorEntity cursor(String id) {
        return cursors.findById(id).orElseThrow();
    }

    @Test
    @DisplayName("a new cursor snapshots current state as SNAPSHOT events, then tails new changes in seq order")
    void snapshotThenTail() {
        point(OPEN, "a");
        point(OPEN, "b");
        create("feed-new");
        await().atMost(WAIT).until(() -> FEED.received.size() == 2);
        assertThat(FEED.received).allMatch(SinkRecord::snapshot);
        assertThat(FEED.externalIds()).containsExactlyInAnyOrder("a", "b");
        await().atMost(WAIT).until(() -> cursor("feed-new").getLastSeq() >= maxSeq());

        point(OPEN, "c");
        point(OPEN, "d");
        await().atMost(WAIT).until(() -> FEED.received.size() == 4);
        List<SinkRecord> tail = FEED.received.subList(2, 4);
        assertThat(tail).noneMatch(SinkRecord::snapshot);
        assertThat(tail.get(0).seq()).isLessThan(tail.get(1).seq());
        assertThat(tail).extracting(SinkRecord::externalId).containsExactly("c", "d");
        await().atMost(WAIT).until(() -> cursor("feed-new").getLastSeq() == maxSeq());
        assertThat(cursor("feed-new").getResyncCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("a denied record is skipped and counted, and the cursor moves past it")
    void deniedRecordsDoNotStall() {
        create("feed-denied");
        await().atMost(WAIT).until(() -> cursor("feed-denied").getState().equals(SinkCursorEntity.RUNNING));
        point(OPEN, "x");
        point(FORBIDDEN, "y");
        point(OPEN, "z");
        await().atMost(WAIT).until(() -> FEED.externalIds().contains("z"));
        assertThat(FEED.externalIds()).containsExactly("x", "z");
        await().atMost(WAIT).until(() -> cursor("feed-denied").getLastSeq() == maxSeq());
        supervisor.stop("feed-denied");
        Map<String, Object> denied = cursor("feed-denied").getDenied();
        assertThat(((Number) denied.get("total")).longValue()).isEqualTo(1);
        assertThat(((Map<?, ?>) denied.get("byReason")).containsKey(SinkRecordGate.REASON_LICENCE_FORBIDDEN)).isTrue();
        assertThat(((Map<?, ?>) denied.get("bySource")).containsKey(FORBIDDEN)).isTrue();
        assertThat(consumer.cursorSeq()).as("the denied record does not hold back retention")
                .hasValue(maxSeq());
    }

    @Test
    @DisplayName("the cursor advances only after an ack; refused deliveries are retried in order")
    void advancesOnlyOnAck() {
        create("feed-ack");
        await().atMost(WAIT).until(() -> cursor("feed-ack").getState().equals(SinkCursorEntity.RUNNING));
        long before = cursor("feed-ack").getLastSeq();
        FEED.refuse.set(true);
        point(OPEN, "p");
        point(OPEN, "q");
        await().atMost(WAIT).until(() -> cursor("feed-ack").getState().equals(SinkCursorEntity.FAILING));
        assertThat(cursor("feed-ack").getLastSeq()).isEqualTo(before);
        assertThat(FEED.received).isEmpty();

        FEED.refuse.set(false);
        await().atMost(WAIT).until(() -> FEED.externalIds().containsAll(List.of("p", "q")));
        assertThat(FEED.externalIds()).containsExactly("p", "q");
        await().atMost(WAIT).until(() -> cursor("feed-ack").getLastSeq() == maxSeq());
    }

    @Test
    @DisplayName("a crash mid-page resumes from the last acknowledged record without loss")
    void crashMidPageResumes() {
        create("feed-crash");
        await().atMost(WAIT).until(() -> cursor("feed-crash").getState().equals(SinkCursorEntity.RUNNING));
        FEED.failOnceAt.set("2");
        point(OPEN, "1");
        point(OPEN, "2");
        point(OPEN, "3");
        await().atMost(WAIT).until(() -> FEED.externalIds().contains("3"));
        assertThat(FEED.externalIds()).containsSubsequence("1", "2", "3");
        assertThat(FEED.externalIds()).containsOnlyOnce("2", "3");
        await().atMost(WAIT).until(() -> cursor("feed-crash").getLastSeq() == maxSeq());
    }

    @Test
    @DisplayName("below the purge horizon the sink resyncs (snapshot) and then continues from the watermark")
    void purgeHorizonResync() {
        create("feed-purge");
        await().atMost(WAIT).until(() -> cursor("feed-purge").getState().equals(SinkCursorEntity.RUNNING));
        supervisor.stop("feed-purge");
        point(OPEN, "kept");
        long resyncsBefore = cursor("feed-purge").getResyncCount();
        retention.setPurgeSeq(OPEN, maxSeq());
        FEED.received.clear();
        supervisor.start("feed-purge");
        await().atMost(WAIT).until(() -> FEED.externalIds().contains("kept"));
        assertThat(FEED.received.getFirst().snapshot()).isTrue();
        await().atMost(WAIT).until(() -> cursor("feed-purge").getResyncCount() > resyncsBefore);
        point(OPEN, "after");
        await().atMost(WAIT).until(() -> FEED.externalIds().contains("after"));
        assertThat(FEED.received.getLast().snapshot()).isFalse();
    }

    @Test
    @DisplayName("the retention clamp includes enabled CHANGE_FEED cursors")
    void clamp() {
        point(OPEN, "a");
        create("feed-clamp");
        await().atMost(WAIT).until(() -> cursor("feed-clamp").getLastSeq() >= maxSeq());
        assertThat(consumer.cursorSeq()).hasValue(cursor("feed-clamp").getLastSeq());
        assertThat(consumer.name()).isEqualTo(SinkCursorConsumer.NAME);
    }
}
