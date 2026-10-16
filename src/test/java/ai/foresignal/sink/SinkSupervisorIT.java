package ai.foresignal.sink;

import static ai.foresignal.testsupport.sink.SinkTestConfig.FEED;
import static ai.foresignal.testsupport.sink.SinkTestConfig.FEED_TYPE;
import static ai.foresignal.testsupport.sink.SinkTestConfig.FORBIDDEN;
import static ai.foresignal.testsupport.sink.SinkTestConfig.KIND;
import static ai.foresignal.testsupport.sink.SinkTestConfig.MIRROR;
import static ai.foresignal.testsupport.sink.SinkTestConfig.MIRROR_TYPE;
import static ai.foresignal.testsupport.sink.SinkTestConfig.OPEN;
import static ai.foresignal.testsupport.sink.SinkTestConfig.SYSTEM_EVENTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.sink.SinkTestConfig;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * The sink supervisor (prompt 110): enable/disable lifecycle, restart with backoff while the target is down, one
 * {@code system.sink_failing} notification per failing episode, and a STATE_MIRROR that snapshots, tails new
 * persisted changes, refuses what the gate refuses and resyncs on request.
 */
@IntegrationTest
@Import(SinkTestConfig.class)
class SinkSupervisorIT {

    static final Duration WAIT = Duration.ofSeconds(30);
    static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired SinkService sinks;
    @Autowired SinkSupervisor supervisor;
    @Autowired SinkCursorRepository cursors;
    @Autowired TestRecords records;

    @BeforeEach
    void setUp() {
        FEED.reset();
        MIRROR.reset();
        SYSTEM_EVENTS.clear();
        supervisor.timings(Duration.ofSeconds(1), Duration.ofMillis(200));
    }

    @AfterEach
    void tearDown() {
        supervisor.stopAll();
        supervisor.timings(null, null);
    }

    void point(String source, String id) {
        records.ingest(source, NormalizedRecord.builder(id, KIND).observedAt(Instant.now()).point(10, 20).build());
    }

    void create(String id, String type, boolean enabled) {
        sinks.create(JSON.readTree("{\"id\":\"" + id + "\",\"type\":\"" + type + "\",\"name\":\"S " + id + "\","
                + "\"enabled\":" + enabled + ",\"config\":{},\"filter\":{\"sources\":[\"" + OPEN + "\",\""
                + FORBIDDEN + "\"]}}"));
    }

    @Test
    @DisplayName("a sink is created disabled by default; enabling starts it, disabling and deleting stop it")
    void lifecycle() {
        sinks.create(JSON.readTree("{\"id\":\"life\",\"type\":\"" + FEED_TYPE + "\",\"name\":\"Life\"}"));
        assertThat(supervisor.run("life")).isNull();
        sinks.update("life", JSON.readTree("{\"enabled\":true}"));
        await().atMost(WAIT).until(() -> supervisor.run("life") != null && supervisor.run("life").instance() != null);
        int running = supervisor.running();
        sinks.update("life", JSON.readTree("{\"enabled\":false}"));
        assertThat(supervisor.run("life")).isNull();
        assertThat(supervisor.running()).isEqualTo(running - 1);
        assertThat(cursors.findById("life").orElseThrow().getState()).isEqualTo(SinkCursorEntity.STOPPED);
        sinks.update("life", JSON.readTree("{\"enabled\":true}"));
        await().atMost(WAIT).until(() -> supervisor.run("life") != null);
        sinks.delete("life");
        assertThat(supervisor.run("life")).isNull();
        assertThat(cursors.findById("life")).as("the stopped run does not resurrect the deleted cursor").isEmpty();
    }

    @Test
    @DisplayName("a target that is down is retried with backoff; one system notification per failing episode")
    void backoffAndNotification() {
        FEED.failOpen.set(true);
        create("down", FEED_TYPE, true);
        await().atMost(WAIT).until(() -> FEED.opens.get() >= 3);
        assertThat(supervisor.failing()).isGreaterThanOrEqualTo(1);
        assertThat(cursors.findById("down").orElseThrow().getState()).isEqualTo(SinkCursorEntity.FAILING);
        await().atMost(WAIT).until(() -> SYSTEM_EVENTS.stream()
                .anyMatch(e -> "system.sink_failing".equals(e.subtype()) && "down".equals(e.data().get("sinkId"))));
        int opens = FEED.opens.get();
        await().atMost(WAIT).until(() -> FEED.opens.get() >= opens + 2);
        assertThat(SYSTEM_EVENTS.stream().filter(e -> "down".equals(e.data().get("sinkId"))).count())
                .as("not repeated on every backoff").isEqualTo(1);

        FEED.failOpen.set(false);
        await().atMost(Duration.ofMinutes(6)).until(() -> supervisor.failing() == 0);
        assertThat(cursors.findById("down").orElseThrow().getFailingSince()).isNull();
    }

    @Test
    @DisplayName("STATE_MIRROR: snapshot, then tail of new changes; refused records never reach it; resync re-snapshots")
    void mirror() {
        point(OPEN, "m1");
        point(FORBIDDEN, "m2");
        create("mirror", MIRROR_TYPE, true);
        await().atMost(WAIT).until(() -> MIRROR.externalIds().contains("m1"));
        assertThat(MIRROR.externalIds()).doesNotContain("m2");

        point(OPEN, "m3");
        await().atMost(WAIT).until(() -> MIRROR.externalIds().contains("m3"));
        assertThat(MIRROR.externalIds()).doesNotContain("m2");

        int resyncs = MIRROR.resyncs.get();
        int received = MIRROR.received.size();
        sinks.resync("mirror");
        await().atMost(WAIT).until(() -> MIRROR.resyncs.get() > resyncs);
        await().atMost(WAIT).until(() -> MIRROR.received.size() >= received + 2);
        assertThat(MIRROR.received.subList(received, MIRROR.received.size()))
                .extracting(SinkRecord::externalId).contains("m1", "m3").doesNotContain("m2");
    }
}
