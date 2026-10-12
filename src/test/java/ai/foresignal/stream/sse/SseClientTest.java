package ai.foresignal.stream.sse;

import static ai.foresignal.stream.sse.SseFixtures.T;
import static ai.foresignal.stream.sse.SseFixtures.box;
import static ai.foresignal.stream.sse.SseFixtures.fifo;
import static ai.foresignal.stream.sse.SseFixtures.limits;
import static ai.foresignal.stream.sse.SseFixtures.live;
import static ai.foresignal.stream.sse.SseFixtures.touch;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/** The client's two lanes, replay-then-live, limits and framing, driven through {@link SseClient#pump()}. */
class SseClientTest {

    private static JsonNode data(SseFrame frame) {
        return SseFixtures.JSON.readTree(frame.data());
    }

    private static void advance(SseFixtures.ClientBuilder b, long millis) {
        b.clock.advance(Duration.ofMillis(millis));
    }

    @Test
    @DisplayName("a burst of 1,000 live updates for 10 entities sends at most 10 per drain")
    void conflationUnderBurst() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        SseClient client = b.build();
        client.pump();
        for (int i = 0; i < 1000; i++) {
            client.offerLive(live("e" + (i % 10), T.plusMillis(i), box(i % 10, 1), box(i % 10, 1)));
        }
        advance(b, 1000);
        client.pump();
        List<SseFrame> lives = b.sender.named("live");
        assertThat(lives).hasSizeLessThanOrEqualTo(10).hasSize(10);
        // The newest update of each entity wins.
        assertThat(data(lives.getFirst()).get("observedAt").asString()).isEqualTo(T.plusMillis(990).toString());
        advance(b, 1000);
        client.pump();
        assertThat(b.sender.named("live")).hasSize(10);
    }

    @Test
    @DisplayName("a touch updates observedAt, validUntil and bbox of a pending live and sends no touch")
    void touchMergesIntoPendingLive() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        SseClient client = b.build();
        client.pump();
        client.offerLive(live("a", T, box(1, 1), box(1, 1)));
        client.offerTouch(touch("a", T.plusSeconds(5), box(1, 1)), List.of("aircraft"));
        advance(b, 1000);
        client.pump();
        assertThat(b.sender.named("touch")).isEmpty();
        List<SseFrame> lives = b.sender.named("live");
        assertThat(lives).hasSize(1);
        JsonNode live = data(lives.getFirst());
        assertThat(live.get("observedAt").asString()).isEqualTo(T.plusSeconds(5).toString());
        assertThat(live.get("validUntil").asString()).isEqualTo(T.plusSeconds(305).toString());
        assertThat(live.get("properties").get("gs").asInt()).isEqualTo(120);
    }

    @Test
    @DisplayName("standalone touches are sent only with touches=true, and a newer touch replaces an older one")
    void standaloneTouches() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        SseClient client = b.build();
        client.pump();
        client.offerTouch(touch("a", T, box(1, 1)), List.of("aircraft"));
        advance(b, 1000);
        client.pump();
        assertThat(b.sender.named("touch")).isEmpty();

        SseFixtures.ClientBuilder b2 = new SseFixtures.ClientBuilder();
        b2.filter = StreamFilter.changesOnly(new ChangeFilter(List.of(), List.of(), List.of(), null, null, false, true,
                true, Set.of()));
        SseClient touching = b2.build();
        touching.pump();
        touching.offerTouch(touch("a", T, box(1, 1)), List.of("aircraft"));
        touching.offerTouch(touch("a", T.plusSeconds(9), box(1, 1)), List.of("aircraft"));
        advance(b2, 1000);
        touching.pump();
        List<SseFrame> touches = b2.sender.named("touch");
        assertThat(touches).hasSize(1);
        assertThat(data(touches.getFirst()).get("observedAt").asString()).isEqualTo(T.plusSeconds(9).toString());
    }

    @Test
    @DisplayName("with live=false no live events are queued")
    void liveFalse() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        b.filter = StreamFilter.changesOnly(new ChangeFilter(List.of(), List.of(), List.of(), null, null, false, false,
                false, Set.of()));
        SseClient client = b.build();
        client.pump();
        client.offerLive(live("a", T, box(1, 1), null));
        advance(b, 1000);
        client.pump();
        assertThat(b.sender.named("live")).isEmpty();
    }

    @Test
    @DisplayName("live updates arriving during a replay wait in the map and are sent after the replayed changes")
    void liveBufferedDuringReplay() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        b.replays.put(ChangesTopic.NAME, new ReplayRequest(0, true));
        SseClient[] holder = new SseClient[1];
        List<Integer> framesWhenLiveArrived = new ArrayList<>();
        b.driver = (topic, request, filter, sink) -> {
            sink.emit(fifo(1));
            holder[0].offerLive(live("a", T, box(1, 1), null));
            framesWhenLiveArrived.add(b.sender.frames.size());
            sink.emit(fifo(2));
            return new ReplayOutcome.Done(2);
        };
        SseClient client = b.build();
        holder[0] = client;
        client.pump();
        assertThat(b.sender.events()).startsWith("change", "change");
        assertThat(framesWhenLiveArrived).containsExactly(1);
        advance(b, 1000);
        client.pump();
        assertThat(b.sender.events()).containsExactly("change", "change", "live");
    }

    @Test
    @DisplayName("buffered FIFO events at or below the replayed cursor are dropped on the replay/live boundary")
    void fifoDedupeOnBoundary() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        b.replays.put(ChangesTopic.NAME, new ReplayRequest(0, true));
        b.driver = (topic, request, filter, sink) -> {
            sink.emit(fifo(1));
            sink.emit(fifo(2));
            sink.emit(fifo(3));
            return new ReplayOutcome.Done(5);
        };
        SseClient client = b.build();
        for (long seq : new long[] {2, 4, 5, 6, 7}) {
            client.offerFifo(ChangesTopic.NAME, f -> fifo(seq));
        }
        client.pump();
        assertThat(b.sender.frames).extracting(SseFrame::id).containsExactly("1", "2", "3", "6", "7");
        assertThat(client.cursor(ChangesTopic.NAME)).isEqualTo(7L);
    }

    @Test
    @DisplayName("a full FIFO buffer sends reset {buffer_overflow} and completes the stream")
    void overflowResets() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        b.limits = limits(2, 0, 0);
        SseClient client = b.build();
        client.pump();
        client.offerFifo(ChangesTopic.NAME, f -> fifo(1));
        client.offerFifo(ChangesTopic.NAME, f -> fifo(2));
        client.offerFifo(ChangesTopic.NAME, f -> fifo(3));
        assertThat(client.pump()).isFalse();
        assertThat(b.sender.events()).containsExactly("reset");
        JsonNode reset = data(b.sender.frames.getFirst());
        assertThat(reset.get("reason").asString()).isEqualTo("buffer_overflow");
        assertThat(reset.get("resumeAfter").asLong()).isZero();
        assertThat(reset.get("watermarkSeq").asLong()).isEqualTo(SseFixtures.WATERMARK);
        assertThat(b.sender.completed).isTrue();
        assertThat(client.isClosed()).isTrue();
        assertThat(b.closedCallback).isTrue();
    }

    @Test
    @DisplayName("growing the conflation map past liveMaxEntities clears it and sends resync {live_cap}")
    void liveCapResyncs() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        b.limits = limits(0, 0, 2);
        SseClient client = b.build();
        client.pump();
        client.offerLive(live("a", T, box(1, 1), null));
        client.offerLive(live("b", T, box(1, 1), null));
        client.offerLive(live("a", T.plusSeconds(1), box(1, 1), null));
        client.offerLive(live("c", T, box(1, 1), null));
        advance(b, 1000);
        client.pump();
        assertThat(b.sender.events()).containsExactly("resync");
        JsonNode resync = data(b.sender.frames.getFirst());
        assertThat(resync.get("reason").asString()).isEqualTo("live_cap");
        assertThat(resync.get("watermarkSeq").asLong()).isEqualTo(SseFixtures.WATERMARK);
        assertThat(client.isClosed()).isFalse();
    }

    @Test
    @DisplayName("a named heartbeat with {} is sent every heartbeatSeconds")
    void heartbeatScheduling() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        SseClient client = b.build();
        client.pump();
        advance(b, 14_000);
        client.pump();
        assertThat(b.sender.named("heartbeat")).isEmpty();
        advance(b, 1_000);
        client.pump();
        assertThat(b.sender.named("heartbeat")).hasSize(1);
        assertThat(b.sender.named("heartbeat").getFirst().data()).isEqualTo("{}");
        advance(b, 5_000);
        client.pump();
        assertThat(b.sender.named("heartbeat")).hasSize(1);
        advance(b, 10_000);
        client.pump();
        assertThat(b.sender.named("heartbeat")).hasSize(2);
    }

    @Test
    @DisplayName("the replay cap sends reset {replay_cap, resumeAfter} after exactly replayCap events")
    void replayCap() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        b.limits = limits(0, 2, 0);
        b.replays.put(ChangesTopic.NAME, new ReplayRequest(0, true));
        b.driver = (topic, request, filter, sink) -> {
            for (long seq = 1; seq <= 3; seq++) {
                if (!sink.emit(fifo(seq))) {
                    return new ReplayOutcome.CapReached();
                }
            }
            return new ReplayOutcome.Done(3);
        };
        SseClient client = b.build();
        assertThat(client.pump()).isFalse();
        assertThat(b.sender.events()).containsExactly("change", "change", "reset");
        JsonNode reset = data(b.sender.frames.getLast());
        assertThat(reset.get("reason").asString()).isEqualTo("replay_cap");
        assertThat(reset.get("resumeAfter").asLong()).isEqualTo(2L);
        assertThat(b.sender.completed).isTrue();
    }

    @Test
    @DisplayName("a resume below the floor sends reset {resync_required} with a null resumeAfter and closes")
    void resyncRequired() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        b.replays.put(ChangesTopic.NAME, new ReplayRequest(3, true));
        b.driver = (topic, request, filter, sink) -> new ReplayOutcome.ResyncRequired(77);
        SseClient client = b.build();
        assertThat(client.pump()).isFalse();
        JsonNode reset = data(b.sender.frames.getFirst());
        assertThat(reset.get("reason").asString()).isEqualTo("resync_required");
        assertThat(reset.get("resumeAfter").isNull()).isTrue();
        assertThat(reset.get("watermarkSeq").asLong()).isEqualTo(77L);
        assertThat(client.isClosed()).isTrue();
    }

    @Test
    @DisplayName("a gap replays from the last delivered seq")
    void gapReplaysFromLastDelivered() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        List<Long> afters = new ArrayList<>();
        b.driver = (topic, request, filter, sink) -> {
            afters.add(request.after());
            assertThat(request.checkFloor()).isTrue();
            sink.emit(fifo(request.after() + 1));
            return new ReplayOutcome.Done(request.after() + 1);
        };
        SseClient client = b.build();
        client.pump();
        client.offerFifo(ChangesTopic.NAME, f -> fifo(4));
        client.pump();
        client.requestReplay(ChangesTopic.NAME);
        client.pump();
        assertThat(afters).containsExactly(4L);
        assertThat(b.sender.frames).extracting(SseFrame::id).containsExactly("4", "5");
    }

    @Test
    @DisplayName("deferred payloads are loaded in a batch and set on the event")
    void payloadsLoaded() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        List<List<Long>> calls = new ArrayList<>();
        b.payloads = (topic, seqs) -> {
            calls.add(List.copyOf(seqs));
            return Map.of(1L, "{\"mag\":4.5}", 2L, "{\"mag\":5.1}");
        };
        SseClient client = b.build();
        client.pump();
        for (long seq = 1; seq <= 2; seq++) {
            long s = seq;
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("seq", s);
            client.offerFifo(ChangesTopic.NAME, f -> new FifoEvent(ChangesTopic.NAME, s, "change", data, s));
        }
        client.pump();
        assertThat(calls).containsExactly(List.of(1L, 2L));
        assertThat(data(b.sender.frames.get(1)).get("payload").get("mag").asDouble()).isEqualTo(5.1);
    }

    @Test
    @DisplayName("composite ids carry the cursor of every subscribed topic, in key order")
    void compositeIds() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        b.mode = SseClient.CursorMode.COMPOSITE;
        b.cursorKeys = new LinkedHashMap<>();
        b.cursorKeys.put(ChangesTopic.NAME, "c");
        b.cursorKeys.put("alerts", "a");
        b.cursors = new LinkedHashMap<>(Map.of(ChangesTopic.NAME, 10L, "alerts", 3L));
        b.filter = new StreamFilter(List.of("alerts", ChangesTopic.NAME),
                Map.of(ChangesTopic.NAME, ChangeFilter.all(Set.of()), "alerts", "any"), 1);
        SseClient client = b.build();
        client.pump();
        client.offerFifo(ChangesTopic.NAME, f -> fifo(12));
        client.offerFifo("alerts", f -> new FifoEvent("alerts", 4, "alert", new LinkedHashMap<>(), null));
        client.pump();
        assertThat(b.sender.frames).extracting(SseFrame::id).containsExactly("c=12;a=3", "c=12;a=4");
    }

    @Test
    @DisplayName("live, touch, resync, heartbeat, reset, hello and filter-applied never carry an id line")
    void controlEventsHaveNoId() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        b.limits = limits(1, 0, 0);
        b.hello = Map.of("streamId", "stream-1", "topics", List.of(ChangesTopic.NAME), "watermarkSeq", 99,
                "filterVersion", 1);
        b.filter = StreamFilter.changesOnly(new ChangeFilter(List.of(), List.of(), List.of(), null, null, false, true,
                true, Set.of()));
        SseClient client = b.build();
        client.pump();
        client.offerLive(live("a", T, box(1, 1), null));
        client.offerTouch(touch("b", T, box(1, 1)), List.of("aircraft"));
        client.resync("gap");
        client.applyFilter(client.filter().next(List.of(ChangesTopic.NAME),
                Map.of(ChangesTopic.NAME, client.filter().changes())), Map.of());
        advance(b, 15_000);
        client.pump();
        client.offerFifo(ChangesTopic.NAME, f -> fifo(1));
        client.offerFifo(ChangesTopic.NAME, f -> fifo(2));
        client.pump();
        assertThat(b.sender.events())
                .contains("hello", "live", "touch", "resync", "heartbeat", "filter-applied", "reset");
        for (SseFrame frame : b.sender.frames) {
            assertThat(frame.hasId()).as(frame.event()).isFalse();
            assertThat(frame.wire()).as(frame.event()).doesNotContain("id:");
        }
    }

    @Test
    @DisplayName("change events carry id: <seq> on the wire")
    void changeEventsHaveIds() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        SseClient client = b.build();
        client.pump();
        client.offerFifo(ChangesTopic.NAME, f -> fifo(42));
        client.pump();
        assertThat(b.sender.frames.getFirst().wire()).startsWith("id:42\nevent:change\ndata:{");
    }

    @Test
    @DisplayName("a failed write closes the client")
    void failedWriteCloses() {
        SseFixtures.ClientBuilder b = new SseFixtures.ClientBuilder();
        SseClient client = b.build();
        client.pump();
        b.sender.failing = true;
        client.offerFifo(ChangesTopic.NAME, f -> fifo(1));
        assertThat(client.pump()).isFalse();
        assertThat(client.isClosed()).isTrue();
        assertThat(b.closedCallback).isTrue();
    }
}
