package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.stream.sse.ChangeFilter;
import ai.foresignal.stream.sse.ChangesTopic;
import ai.foresignal.stream.sse.CompositeCursor;
import ai.foresignal.stream.sse.EmitterSseSender;
import ai.foresignal.stream.sse.EventTopic;
import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.stream.sse.StreamFilter;
import ai.foresignal.stream.sse.StreamPrincipal;
import ai.foresignal.stream.sse.StreamPrincipalResolver;
import ai.foresignal.stream.sse.TopicParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.JsonNode;

/**
 * The SSE endpoints (prompt 015): {@code GET /v1/changes/stream} ({@code streamChanges}),
 * {@code GET /v1/events/stream} ({@code streamEvents}) and
 * {@code PATCH /v1/events/stream/{streamId}/filter} ({@code updateEventStreamFilter}).
 *
 * <p>Everything is validated before a stream opens, so a bad request is a problem response, not a
 * stream that closes at once. The caller is resolved once, at connect, by the
 * {@link StreamPrincipalResolver}. Responses carry {@code Content-Type: text/event-stream},
 * {@code Cache-Control: no-cache} and {@code X-Accel-Buffering: no}; {@code text/event-stream} is
 * not in {@code server.compression.mime-types}, so streams are never gzip-buffered.
 *
 * <p>Needs the {@code read} scope, open while {@code security.openReads} is on.
 */
@RestController
@Tag(name = "Events")
public class StreamController {

    /** Header naming the anonymous stream key on a PATCH. */
    public static final String STREAM_KEY_HEADER = "Foresignal-Stream-Key";

    /** The SSE resume header. */
    public static final String LAST_EVENT_ID = "Last-Event-ID";

    static final Pattern TOPIC = Pattern.compile("^[a-z][a-z0-9-]*$");

    private final SseHub hub;
    private final ChangesTopic changes;
    private final StreamPrincipalResolver principals;

    /** Creates the controller. */
    public StreamController(SseHub hub, ChangesTopic changes, StreamPrincipalResolver principals) {
        this.hub = hub;
        this.changes = changes;
        this.principals = principals;
    }

    /** {@code streamChanges}. */
    @GetMapping("/v1/changes/stream")
    @Operation(operationId = "streamChanges", tags = "Changes")
    @ai.foresignal.security.restrict.RestrictedRead("ChangeFilter tests the restriction at publish time")
    public SseEmitter streamChanges(
            @RequestHeader(value = LAST_EVENT_ID, required = false) String lastEventId,
            @RequestParam(required = false) String after,
            HttpServletRequest request,
            HttpServletResponse response) {
        StreamPrincipal principal = principals.resolve(request);
        ChangeFilter filter = changes.parseQuery(new TopicParams("", parameters(request, "")), principal);
        Long resume = CompositeCursor.parseSeq(LAST_EVENT_ID, lastEventId);
        if (resume == null) {
            resume = QueryParams.nonNegativeLong("after", after);
        }
        Map<String, Long> resumes = resume == null ? Map.of() : Map.of(ChangesTopic.NAME, resume);
        return open(new SseHub.OpenRequest(
                principal, SseClient.CursorMode.SINGLE, StreamFilter.changesOnly(filter), resumes, false), response);
    }

    /** {@code streamEvents}. */
    @GetMapping("/v1/events/stream")
    @Operation(operationId = "streamEvents", tags = "Events")
    @ai.foresignal.security.restrict.RestrictedRead("every topic's filter carries the principal's restriction")
    public SseEmitter streamEvents(
            @RequestHeader(value = LAST_EVENT_ID, required = false) String lastEventIdHeader,
            @RequestParam(required = false) List<String> topics,
            @RequestParam(required = false) String lastEventId,
            HttpServletRequest request,
            HttpServletResponse response) {
        StreamPrincipal principal = principals.resolve(request);
        List<String> names = QueryParams.multi("topics", topics);
        if (names.isEmpty()) {
            names = List.of(ChangesTopic.NAME);
        }
        Map<String, Object> filters = new LinkedHashMap<>();
        for (String name : names) {
            EventTopic<?> topic = topic(name, "topics", principal);
            filters.put(name, topic.parseQuery(new TopicParams(name + ".", parameters(request, name + ".")), principal));
        }
        boolean fromHeader = lastEventIdHeader != null && !lastEventIdHeader.isBlank();
        Map<String, Long> cursors = CompositeCursor.parse(
                fromHeader ? LAST_EVENT_ID : "lastEventId", fromHeader ? lastEventIdHeader : lastEventId);
        Map<String, Long> resumes = new HashMap<>();
        for (Map.Entry<String, String> key : hub.cursorKeys().entrySet()) {
            Long cursor = cursors.get(key.getValue());
            if (cursor != null && filters.containsKey(key.getKey())) {
                resumes.put(key.getKey(), cursor);
            }
        }
        StreamFilter filter = new StreamFilter(names, filters, 1);
        return open(new SseHub.OpenRequest(principal, SseClient.CursorMode.COMPOSITE, filter, resumes, true), response);
    }

    /** {@code updateEventStreamFilter}. */
    @PatchMapping(value = "/v1/events/stream/{streamId}/filter", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "updateEventStreamFilter", tags = "Events")
    @ai.foresignal.security.restrict.RestrictedRead("the new filter is parsed for the owner, keeping its restriction")
    public ResponseEntity<JsonNode> updateEventStreamFilter(
            @PathVariable String streamId,
            @RequestHeader(value = STREAM_KEY_HEADER, required = false) String streamKey,
            @RequestBody JsonNode body,
            HttpServletRequest request) {
        StreamPrincipal caller = principals.resolve(request);
        SseClient client = hub.owned(streamId, caller, streamKey)
                .orElseThrow(() -> new NotFoundException("No open event stream " + streamId + " for this caller."));
        if (body == null || !body.isObject()) {
            throw new ValidationException("The filter must be a JSON object.");
        }
        StreamPrincipal owner = client.principal();
        StreamFilter current = client.filter();

        List<String> names = current.topics();
        JsonNode topicsNode = body.get("topics");
        if (topicsNode != null && !topicsNode.isNull()) {
            names = topicList(topicsNode);
            for (String name : names) {
                topic(name, "topics", caller);
            }
        }
        Map<String, Object> parsed = new HashMap<>();
        for (String field : body.propertyNames()) {
            if ("topics".equals(field)) {
                continue;
            }
            EventTopic<?> topic = hub.topic(field).orElseThrow(() -> unknownTopic(field, field));
            parsed.put(field, topic.parseJson(body.get(field), owner));
        }
        Map<String, Object> filters = new LinkedHashMap<>();
        Map<String, Long> addedHeads = new HashMap<>();
        for (String name : names) {
            EventTopic<?> topic = hub.topic(name).orElseThrow(() -> unknownTopic("topics", name));
            Object filter = parsed.get(name);
            if (filter == null) {
                filter = current.filterFor(name);
            }
            if (filter == null) {
                filter = topic.parseQuery(new TopicParams(name + ".", Map.of()), owner);
            }
            filters.put(name, filter);
            if (!current.subscribes(name)) {
                addedHeads.put(name, topic.head());
            }
        }
        client.applyFilter(current.next(names, filters), addedHeads);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    // ------------------------------------------------------------------------- helpers

    /**
     * Opens a single-topic {@code changes} stream for {@code principal} with {@code filter}, resuming
     * from {@code Last-Event-ID} or {@code after} (prompt 036: {@code streamView}).
     */
    public SseEmitter openChanges(StreamPrincipal principal, ChangeFilter filter, String lastEventId, String after,
            HttpServletResponse response) {
        Long resume = CompositeCursor.parseSeq(LAST_EVENT_ID, lastEventId);
        if (resume == null) {
            resume = QueryParams.nonNegativeLong("after", after);
        }
        Map<String, Long> resumes = resume == null ? Map.of() : Map.of(ChangesTopic.NAME, resume);
        return open(new SseHub.OpenRequest(
                principal, SseClient.CursorMode.SINGLE, StreamFilter.changesOnly(filter), resumes, false), response);
    }

    /** The request's parameters, unprefixed, for parsing a {@code changes} filter. */
    public static Map<String, List<String>> parameters(HttpServletRequest request) {
        return parameters(request, "");
    }

    private SseEmitter open(SseHub.OpenRequest open, HttpServletResponse response) {
        SseEmitter emitter = EmitterSseSender.newCommittingEmitter();
        SseClient client = hub.open(open, new EmitterSseSender(emitter));
        emitter.onCompletion(client::close);
        emitter.onTimeout(client::close);
        emitter.onError(e -> client.close());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        return emitter;
    }

    private EventTopic<?> topic(String name, String field, StreamPrincipal principal) {
        if (!TOPIC.matcher(name).matches()) {
            throw unknownTopic(field, name);
        }
        EventTopic<?> topic = hub.topic(name).orElseThrow(() -> unknownTopic(field, name));
        if (!principal.allows(topic.requiredScope())) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN,
                    "The topic '" + name + "' needs the " + topic.requiredScope().wireValue() + " scope.");
        }
        return topic;
    }

    private static List<String> topicList(JsonNode node) {
        if (!node.isArray() || node.isEmpty()) {
            throw ValidationException.forField("topics", "must be a non-empty array of topic names");
        }
        List<String> names = new ArrayList<>();
        for (JsonNode item : node) {
            if (!item.isString()) {
                throw ValidationException.forField("topics", "must be an array of strings");
            }
            String name = item.asString().strip();
            if (!names.contains(name)) {
                names.add(name);
            }
        }
        return names;
    }

    private static ValidationException unknownTopic(String field, String name) {
        return new ValidationException(ErrorCodes.UNKNOWN_TOPIC, "Unknown event topic '" + name + "'.",
                List.of(new FieldError(field, "'" + name + "' is not a registered topic")));
    }

    /** The request's parameters starting with {@code prefix}, with the prefix removed. */
    private static Map<String, List<String>> parameters(HttpServletRequest request, String prefix) {
        Map<String, List<String>> out = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            String name = entry.getKey();
            if (prefix.isEmpty()) {
                out.put(name, Arrays.asList(entry.getValue()));
            } else if (name.startsWith(prefix) && name.length() > prefix.length()) {
                out.put(name.substring(prefix.length()), Arrays.asList(entry.getValue()));
            }
        }
        return out;
    }
}
