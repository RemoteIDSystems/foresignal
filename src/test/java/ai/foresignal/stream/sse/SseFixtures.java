package ai.foresignal.stream.sse;

import ai.foresignal.api.support.BboxParser;
import ai.foresignal.stream.Bbox;
import ai.foresignal.stream.ChangeSummary;
import ai.foresignal.stream.LiveUpdate;
import ai.foresignal.stream.Touch;
import ai.foresignal.ingest.PersistReason;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.test.MutableClock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.json.JsonMapper;

/** Builders shared by the SSE unit tests. */
final class SseFixtures {

    static final Instant T = Instant.parse("2026-03-01T12:00:00Z");
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final long WATERMARK = 99L;

    private SseFixtures() {}

    /** A client builder with no replay, the default limits and a no-op replay driver. */
    static final class ClientBuilder {
        final MutableClock clock = new MutableClock(T);
        final FakeSender sender = new FakeSender();
        StreamLimits limits = StreamLimits.defaults();
        SseClient.CursorMode mode = SseClient.CursorMode.SINGLE;
        Map<String, String> cursorKeys = new LinkedHashMap<>(Map.of(ChangesTopic.NAME, "c"));
        Map<String, Long> cursors = new LinkedHashMap<>(Map.of(ChangesTopic.NAME, 0L));
        Map<String, ReplayRequest> replays = new LinkedHashMap<>();
        Map<String, Object> hello;
        SseClient.ReplayDriver driver = (topic, request, filter, sink) -> new ReplayOutcome.Done(request.after());
        SseClient.PayloadLoader payloads = (topic, seqs) -> Map.of();
        StreamFilter filter = StreamFilter.changesOnly(ChangeFilter.all(Set.of()));
        boolean closedCallback;

        SseClient build() {
            SseClient.Settings settings = new SseClient.Settings("stream-1", StreamPrincipal.anonymous("127.0.0.1"),
                    "key", mode, cursorKeys, cursors, replays, hello, limits, clock, JSON, driver, payloads,
                    () -> WATERMARK, c -> closedCallback = true);
            return new SseClient(settings, filter, sender);
        }
    }

    static StreamLimits limits(int clientBuffer, int replayCap, int liveMaxEntities) {
        return new StreamLimits(0, clientBuffer, replayCap, 0, 0, liveMaxEntities, 0, 0);
    }

    static Bbox box(double x, double y) {
        return new Bbox(x, y, x, y);
    }

    static LiveUpdate live(String id, Instant observedAt, Bbox bbox, Bbox prevBbox) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("gs", 120);
        return new LiveUpdate("src", id, "aircraft", observedAt, null, 1, null, bbox, prevBbox, properties, null);
    }

    static Touch touch(String id, Instant observedAt, Bbox bbox) {
        return new Touch("src", id, observedAt, observedAt.plusSeconds(300), bbox);
    }

    static ChangeSummary change(long seq, String source, String kind, Bbox bbox, Bbox prevPersisted, Bbox prevLive,
            boolean tombstone) {
        return new ChangeSummary(seq, seq > 1 ? seq - 1 : null, HistoryTier.STANDARD, source, "e" + seq, kind, 1,
                tombstone ? PersistReason.TOMBSTONE : PersistReason.FIRST, T, null, null, tombstone, 0, bbox,
                prevPersisted, prevLive);
    }

    static FifoEvent fifo(long seq) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("seq", seq);
        return new FifoEvent(ChangesTopic.NAME, seq, "change", data, null);
    }

    static ChangeFilter boxFilter(String bbox) {
        return new ChangeFilter(List.of(), List.of(), List.of(), BboxParser.parse(bbox), null, false, true, true,
                Set.of());
    }
}
