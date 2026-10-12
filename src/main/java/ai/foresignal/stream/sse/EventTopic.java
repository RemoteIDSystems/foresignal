package ai.foresignal.stream.sse;

import ai.foresignal.security.Scope;
import ai.foresignal.stream.Subscription;
import java.util.Collection;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * The SPI through which a topic joins the SSE machinery (prompt 015). A topic is a Spring bean;
 * {@link SseHub} discovers every bean of this type at start and needs no change to carry a new one.
 * Prompt 015 registers {@code changes} ({@link ChangesTopic}); prompt 017 registers {@code alerts}
 * and prompt 018 {@code notifications} and {@code system}.
 *
 * <h2>What a topic provides</h2>
 *
 * <ul>
 *   <li><b>Identity:</b> {@link #name()} (the {@code topics=} value and the PATCH block name),
 *       {@link #cursorKey()} (its key in the composite {@code id:}, e.g. {@code c}, {@code a},
 *       {@code n}) and {@link #cursorOrder()} (its position in that id).
 *   <li><b>Access:</b> {@link #requiredScope()}; a caller without it gets {@code 403} when it asks
 *       for the topic. Anonymous {@code openReads} callers count as {@code read}.
 *   <li><b>Filter:</b> {@link #filterType()}, an immutable value; {@link #parseQuery} builds it
 *       from the connect parameters ({@code <topic>.<name>} on {@code /v1/events/stream}) and
 *       {@link #parseJson} from a PATCH block. Both validate fully and throw
 *       {@code ValidationException} ({@code 400}); nothing is applied when either throws.
 *   <li><b>Resume:</b> {@link #head()} is the current cursor (where "live from now" starts and
 *       where a topic added by PATCH starts); {@link #replay} sends the events after a cursor from
 *       the database, on the client's sender thread, in cursor order.
 *   <li><b>Live events:</b> {@link #subscribe} attaches the topic to its own bus once, at start.
 *       For each event it calls {@link SseHub#publish(String, java.util.function.Function)} with a
 *       function from a client's filter (of {@link #filterType()}) to that client's
 *       {@link FifoEvent}, or null when the filter rejects it. Cursors must increase strictly per
 *       topic; the client drops anything at or below the cursor it already has, which is what makes
 *       replay-then-live gap-free and duplicate-free. On a bus gap the topic calls
 *       {@link SseHub#gap(String)} and every subscribed client replays from its last cursor.
 * </ul>
 *
 * <p>Live-lane (conflated, id-less) updates are a {@code changes} concern and go through
 * {@link SseHub#publishLive} and {@link SseHub#publishTouch}.
 *
 * @param <F> the topic's filter type
 */
public interface EventTopic<F> {

    /** The topic name: lowercase, {@code [a-z][a-z0-9-]*}. */
    String name();

    /** The topic's key in the composite cursor: lowercase, {@code [a-z][a-z0-9.-]*}, unique. */
    String cursorKey();

    /** The topic's position in the composite cursor; lower first. {@code changes} is 0. */
    default int cursorOrder() {
        return 100;
    }

    /** The scope a caller needs to subscribe. */
    Scope requiredScope();

    /** The class of the topic's filter values. */
    Class<F> filterType();

    /**
     * The filter from connect parameters.
     *
     * @param params the topic's parameters, prefix removed
     * @param principal the caller (for example, to exclude private sources from anonymous callers)
     */
    F parseQuery(TopicParams params, StreamPrincipal principal);

    /**
     * The filter from a PATCH block, which replaces the topic's filter whole (omitted fields take
     * their defaults).
     *
     * @param block the topic's JSON object
     * @param principal the caller
     */
    F parseJson(JsonNode block, StreamPrincipal principal);

    /** The current head cursor. */
    long head();

    /**
     * Sends every event after {@code request.after()} that {@code filter} accepts, in cursor order,
     * stopping when {@code sink} refuses one.
     */
    ReplayOutcome replay(ReplayRequest request, F filter, ReplaySink sink);

    /** Attaches the topic to its bus; called once when the hub starts. */
    Subscription subscribe(SseHub hub);

    /**
     * Payloads for {@link FifoEvent#payloadSeq()}s, as JSON text keyed by seq. Topics without
     * deferred payloads keep the default.
     */
    default Map<Long, String> loadPayloads(Collection<Long> seqs) {
        return Map.of();
    }
}
