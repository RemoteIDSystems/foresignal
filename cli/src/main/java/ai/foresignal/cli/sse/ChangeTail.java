package ai.foresignal.cli.sse;

import ai.foresignal.cli.http.ApiClient;
import ai.foresignal.cli.http.ApiResponse;
import ai.foresignal.cli.http.Query;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.MissingNode;

/**
 * {@code foresignal changes tail}: follows {@code GET /v1/changes/stream} with the client contract of
 * Core-Data-Model §10.2 and the {@code streamChanges} operation:
 *
 * <ul>
 *   <li>tracks the id (seq) of the last {@code change} event and resumes with {@code Last-Event-ID} (and
 *       {@code after}) after any disconnect; {@code live}, {@code touch}, {@code resync}, {@code reset} and
 *       {@code heartbeat} carry no id and never move the resume position;
 *   <li>on {@code reset} with a {@code resumeAfter} ({@code buffer_overflow}, {@code replay_cap}) reconnects at once
 *       from that seq; on {@code reset} without one ({@code resync_required}: the resume point fell below the
 *       retention floor) pages the snapshot {@code GET /v1/records?order=id} with the same filters, keeps its
 *       {@code watermarkSeq}, and reopens the stream at that watermark;
 *   <li>on {@code resync} (live updates were lost) re-fetches the current state the same way and keeps the stream
 *       open;
 *   <li>emits a {@code live} or {@code touch} event only when its {@code observedAt} is not older than the one
 *       already seen for that entity;
 *   <li>reconnects through {@link ResilientSseStream} (heartbeat watchdog at 2.5× the interval, jittered backoff
 *       1 s → 60 s honoring {@code retry:} and {@code Retry-After}).
 * </ul>
 */
public final class ChangeTail {

    /** The page size of snapshot reads. */
    public static final int SNAPSHOT_PAGE = 5000;

    /**
     * What to follow.
     *
     * @param sources       {@code source} filters
     * @param kinds         {@code kind} filters
     * @param bbox          the {@code bbox} filter ({@code minLon,minLat,maxLon,maxLat}), or null
     * @param live          also receive the conflated {@code live} lane
     * @param touches       also receive {@code touch} events
     * @param after         the seq to start after, or null for "live from now"
     * @param snapshotFirst page the snapshot first and open the stream at its watermark
     * @param maxEvents     stop after this many emitted events and snapshot records (0 = never)
     */
    public record Options(List<String> sources, List<String> kinds, String bbox, boolean live, boolean touches,
            Long after, boolean snapshotFirst, long maxEvents) {
    }

    /** Receives what the tail emits, in order. */
    public interface Sink {

        /**
         * A stream event to show.
         *
         * @param name the event name ({@code change}, {@code live}, {@code touch})
         * @param id   its id, or null
         * @param data its parsed data
         */
        void event(String name, String id, JsonNode data);

        /**
         * One record of a snapshot (initial, after {@code reset}, or after {@code resync}).
         *
         * @param record the record
         */
        void snapshot(JsonNode record);

        /**
         * A notice for standard error ({@code reset}, {@code resync}, snapshot boundaries).
         *
         * @param message the notice
         */
        void notice(String message);
    }

    /** Thrown inside the handler to end the run once {@code maxEvents} is reached. */
    private static final class Done extends RuntimeException {
        private static final long serialVersionUID = 1L;

        Done() {
            super(null, null, false, false);
        }
    }

    private final ApiClient api;
    private final Options options;
    private final Sink sink;
    private final ResilientSseStream stream;
    private final Map<String, Instant> observed = new HashMap<>();
    private Long lastId;
    private long emitted;

    /**
     * Creates a tail.
     *
     * @param api     the API client
     * @param options what to follow
     * @param sink    where events go
     * @param stream  the reconnecting stream
     */
    public ChangeTail(ApiClient api, Options options, Sink sink, ResilientSseStream stream) {
        this.api = api;
        this.options = options;
        this.sink = sink;
        this.stream = stream;
        this.lastId = options.after();
    }

    /**
     * Convenience factory with the default backoff.
     *
     * @param api        the API client
     * @param options    what to follow
     * @param sink       where events go
     * @param heartbeat  the server's heartbeat interval
     * @param backoff    the backoff
     * @param sleeper    how to wait
     * @param err        where reconnect notices go
     * @param maxRetries consecutive failed attempts before giving up (negative = forever)
     * @return the tail
     */
    public static ChangeTail create(ApiClient api, Options options, Sink sink, Duration heartbeat, Backoff backoff,
            Sleeper sleeper, PrintWriter err, int maxRetries) {
        return new ChangeTail(api, options, sink,
                new ResilientSseStream(api, heartbeat, backoff, sleeper, err, maxRetries));
    }

    /**
     * The resume position.
     *
     * @return the seq of the last {@code change} (or the watermark of the last snapshot), or null
     */
    public Long lastId() {
        return lastId;
    }

    /**
     * Follows the stream until {@code maxEvents} is reached, forever otherwise.
     *
     * @throws ai.foresignal.cli.http.ApiException     on a non-retryable API error
     * @throws ai.foresignal.cli.http.NetworkException when reconnecting gives up
     */
    public void run() {
        try {
            if (options.snapshotFirst()) {
                lastId = snapshot("initial snapshot");
            }
            stream.run(this::request, this::onEvent);
        } catch (Done done) {
            // maxEvents reached.
        }
    }

    /**
     * The next connection attempt, resuming from {@link #lastId()}.
     *
     * @return the request
     */
    StreamRequest request() {
        Query query = filters();
        query.add("live", Boolean.toString(options.live()));
        query.flag("touches", options.touches());
        Map<String, String> headers = new LinkedHashMap<>();
        if (lastId != null) {
            query.add("after", lastId);
            headers.put("Last-Event-ID", Long.toString(lastId));
        }
        return new StreamRequest("/v1/changes/stream", query, headers);
    }

    private Query filters() {
        return Query.of().addAll("source", options.sources()).addAll("kind", options.kinds())
                .add("bbox", options.bbox());
    }

    private ResilientSseStream.Action onEvent(SseEvent event) {
        switch (event.event()) {
            case "change" -> {
                JsonNode data = parse(event.data());
                Long seq = parseLong(event.id());
                if (seq == null && data.path("seq").canConvertToLong()) {
                    seq = data.path("seq").asLong();
                }
                if (seq != null) {
                    lastId = seq;
                }
                remember(data.path("source").asString(null), data.path("externalId").asString(null),
                        data.path("observedAt").asString(null), true);
                emit(event.event(), event.id(), data);
            }
            case "live", "touch" -> {
                JsonNode data = parse(event.data());
                if (remember(data.path("sourceId").asString(null), data.path("externalId").asString(null),
                        data.path("observedAt").asString(null), false)) {
                    emit(event.event(), null, data);
                }
            }
            case "resync" -> {
                JsonNode data = parse(event.data());
                sink.notice("resync (" + data.path("reason").asString("unknown") + "): re-fetching current state");
                snapshot("resync snapshot");
            }
            case "reset" -> {
                JsonNode data = parse(event.data());
                String reason = data.path("reason").asString("unknown");
                JsonNode resumeAfter = data.path("resumeAfter");
                if (resumeAfter.canConvertToLong() && !resumeAfter.isNull()) {
                    lastId = resumeAfter.asLong();
                    sink.notice("reset (" + reason + "): resuming after " + lastId);
                } else {
                    sink.notice("reset (" + reason + "): re-snapshotting");
                    lastId = snapshot("reset snapshot");
                }
                return ResilientSseStream.Action.RECONNECT_NOW;
            }
            default -> {
                // heartbeat, hello and unknown events: nothing to show (the watchdog already saw them).
            }
        }
        return ResilientSseStream.Action.CONTINUE;
    }

    /**
     * Pages {@code GET /v1/records?order=id} with the tail's filters, emitting every record.
     *
     * @param label what the snapshot is for (notices)
     * @return the snapshot's {@code watermarkSeq} (the seq to reopen the stream after), or the current resume
     *         position when the server sent none
     */
    Long snapshot(String label) {
        Query base = filters().add("order", "id").add("limit", SNAPSHOT_PAGE);
        Long watermark = null;
        String cursor = null;
        long count = 0;
        do {
            Query query = base.copy().add("cursor", cursor);
            ApiResponse response = api.checked(api.send("GET", "/v1/records", query, null, Map.of(),
                    Duration.ofMinutes(5)));
            JsonNode page = api.parse(response);
            if (watermark == null) {
                JsonNode w = page.path("watermarkSeq");
                if (w.canConvertToLong() && !w.isNull()) {
                    watermark = w.asLong();
                } else {
                    watermark = response.header("Foresignal-Watermark-Seq").map(ChangeTail::parseLong).orElse(null);
                }
            }
            for (JsonNode record : page.path("items")) {
                remember(record.path("source").asString(null), record.path("externalId").asString(null),
                        record.path("observedAt").asString(null), true);
                count++;
                sink.snapshot(record);
                countEmitted();
            }
            JsonNode next = page.path("nextCursor");
            cursor = next.isString() && !next.asString().isEmpty() ? next.asString() : null;
        } while (cursor != null);
        sink.notice(label + ": " + count + " records, watermarkSeq " + (watermark == null ? "unknown" : watermark));
        return watermark != null ? watermark : lastId;
    }

    /**
     * Records the newest {@code observedAt} of an entity.
     *
     * @param source     the source id
     * @param externalId the external id
     * @param observedAt the event's {@code observedAt}
     * @param always     true for persisted changes and snapshot records (always stored)
     * @return false when the event is older than the stored value and must not be applied
     */
    private boolean remember(String source, String externalId, String observedAt, boolean always) {
        if (source == null || externalId == null || observedAt == null) {
            return true;
        }
        Instant at;
        try {
            at = Instant.parse(observedAt);
        } catch (DateTimeParseException e) {
            return true;
        }
        String key = source + '\u0000' + externalId;
        Instant stored = observed.get(key);
        if (!always && stored != null && at.isBefore(stored)) {
            return false;
        }
        if (stored == null || !at.isBefore(stored) || always) {
            observed.put(key, at);
        }
        return true;
    }

    private void emit(String name, String id, JsonNode data) {
        sink.event(name, id, data);
        countEmitted();
    }

    private void countEmitted() {
        emitted++;
        if (options.maxEvents() > 0 && emitted >= options.maxEvents()) {
            throw new Done();
        }
    }

    private JsonNode parse(String data) {
        if (data == null || data.isEmpty()) {
            return MissingNode.getInstance();
        }
        try {
            return api.json().readTree(data);
        } catch (JacksonException e) {
            sink.notice("ignoring an event whose data is not JSON: " + e.getOriginalMessage());
            return MissingNode.getInstance();
        }
    }

    private static Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
