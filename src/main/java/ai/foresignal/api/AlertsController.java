package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.mapper.AlertMapper;
import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.Actors;
import ai.foresignal.api.support.Cursor;
import ai.foresignal.api.support.CursorCodec;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import ai.foresignal.stream.sse.AlertsTopic;
import ai.foresignal.stream.sse.EmitterSseSender;
import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.stream.sse.StreamFilter;
import ai.foresignal.stream.sse.StreamPrincipal;
import ai.foresignal.stream.sse.StreamPrincipalResolver;
import ai.foresignal.stream.sse.TopicParams;
import ai.foresignal.trigger.AlertQuery;
import ai.foresignal.trigger.AlertService;
import ai.foresignal.trigger.AlertState;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.Feedback;
import ai.foresignal.trigger.Severity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.JsonNode;

/**
 * The {@code Alerts} operations (prompt 017): {@code listAlerts}, {@code getAlert},
 * {@code streamAlerts}, {@code acknowledgeAlert}, {@code resolveAlert}, {@code snoozeAlert},
 * {@code unsnoozeAlert} and {@code setAlertFeedback}.
 *
 * <p>Reads need {@code read} (open while {@code security.openReads} is on); lifecycle writes need
 * {@code agent}. Anonymous callers never see alerts of private sources: those are filtered from
 * lists and streams and are {@code 404} by id. An alert id that is not a number is not found.
 */
@RestController
@Tag(name = "Alerts")
@RequestMapping("/v1/alerts")
public class AlertsController {

    /** The largest page. */
    static final int MAX_LIMIT = 5_000;

    /** The default page. */
    static final int DEFAULT_LIMIT = 500;

    private final AlertService alerts;
    private final AlertMapper mapper;
    private final RecordMapper records;
    private final RecordQueryService queries;
    private final ReadCallerResolver callers;
    private final SseHub hub;
    private final AlertsTopic topic;
    private final StreamPrincipalResolver principals;

    /** Creates the controller. */
    public AlertsController(
            AlertService alerts,
            AlertMapper mapper,
            RecordMapper records,
            RecordQueryService queries,
            ReadCallerResolver callers,
            SseHub hub,
            AlertsTopic topic,
            StreamPrincipalResolver principals) {
        this.alerts = alerts;
        this.mapper = mapper;
        this.records = records;
        this.queries = queries;
        this.callers = callers;
        this.hub = hub;
        this.topic = topic;
        this.principals = principals;
    }

    /** {@code listAlerts}: newest first, keyset on id. */
    @GetMapping
    @Operation(operationId = "listAlerts", tags = "Alerts")
    public Map<String, Object> listAlerts(
            @RequestParam(required = false) String ruleId,
            @RequestParam(required = false) List<String> source,
            @RequestParam(required = false) List<String> kind,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String acknowledged,
            @RequestParam(required = false) List<String> state,
            @RequestParam(required = false) List<String> feedback,
            @RequestParam(required = false) String minSeverity,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String includeSuppressed,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        Set<UUID> rules = ruleId == null ? Set.of() : Set.of(uuid("ruleId", ruleId));
        Instant sinceValue = QueryParams.instant("since", since);
        Boolean ack = acknowledged == null ? null : QueryParams.bool("acknowledged", acknowledged, false);
        List<String> states = List.copyOf(QueryParams.enumSet("state", state, AlertState.WIRE_VALUES));
        List<String> feedbacks = List.copyOf(QueryParams.enumSet("feedback", feedback, Feedback.WIRE_VALUES));
        Severity min = minSeverity == null ? null
                : Severity.fromWire(QueryParams.oneOf("minSeverity", minSeverity, Severity.WIRE_VALUES, null));
        boolean suppressed = QueryParams.bool("includeSuppressed", includeSuppressed, false);
        int size = QueryParams.intInRange("limit", limit, 1, MAX_LIMIT, DEFAULT_LIMIT);
        Set<String> excluded = callers.excludedSources(callers.resolve(request));
        AlertQuery query = new AlertQuery(rules, QueryParams.multi("source", source), QueryParams.multi("kind", kind),
                sinceValue, ack, states, feedbacks, min, tag, suppressed, excluded);
        String fingerprint = CursorCodec.fingerprint(canonical(query, size));
        Cursor.Seq position = CursorCodec.decode(cursor, Cursor.Seq.class, fingerprint);
        List<AlertView> page = alerts.list(query, position == null ? null : position.q(), size + 1);
        String next = null;
        if (page.size() > size) {
            page = page.subList(0, size);
            next = CursorCodec.encode(new Cursor.Seq(page.getLast().id(), null), fingerprint);
        }
        List<Map<String, Object>> items = new ArrayList<>();
        for (AlertView alert : page) {
            items.add(mapper.alert(alert, null));
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", items);
        out.put("nextCursor", next);
        return out;
    }

    /** {@code getAlert}: with {@code record} loaded by seq while the version is retained. */
    @GetMapping("/{alertId}")
    @Operation(operationId = "getAlert", tags = "Alerts")
    public Map<String, Object> getAlert(@PathVariable String alertId, HttpServletRequest request) {
        AlertView alert = visible(alertId, request);
        RecordRow row = queries.versionsBySeq(List.of(alert.seq())).get(alert.seq());
        Map<String, Object> record = row == null ? null : records.record(row, FieldSelection.ALL, RecordMapper.Shape.VERSION);
        return mapper.alert(alert, record);
    }

    /** {@code acknowledgeAlert}: optional note and feedback; idempotent; a no-op on resolved alerts. */
    @PostMapping("/{alertId}/ack")
    @Operation(operationId = "acknowledgeAlert", tags = "Alerts")
    public Map<String, Object> acknowledgeAlert(
            @PathVariable String alertId, @RequestBody(required = false) JsonNode body, HttpServletRequest request) {
        AlertService.Transition transition = AlertService.transition(body, false);
        return mapper.alert(alerts.acknowledge(id(alertId), transition, Actors.of(request)), null);
    }

    /** {@code resolveAlert}: optional note and feedback; idempotent on a resolved alert. */
    @PostMapping("/{alertId}/resolve")
    @Operation(operationId = "resolveAlert", tags = "Alerts")
    public Map<String, Object> resolveAlert(
            @PathVariable String alertId, @RequestBody(required = false) JsonNode body, HttpServletRequest request) {
        AlertService.Transition transition = AlertService.transition(body, false);
        return mapper.alert(alerts.resolve(id(alertId), transition, Actors.of(request)), null);
    }

    /** {@code snoozeAlert}: {@code until} or {@code for}. */
    @PostMapping("/{alertId}/snooze")
    @Operation(operationId = "snoozeAlert", tags = "Alerts")
    public Map<String, Object> snoozeAlert(@PathVariable String alertId, @RequestBody JsonNode body) {
        return mapper.alert(alerts.snooze(id(alertId), body), null);
    }

    /** {@code unsnoozeAlert}: idempotent. */
    @DeleteMapping("/{alertId}/snooze")
    @Operation(operationId = "unsnoozeAlert", tags = "Alerts")
    public Map<String, Object> unsnoozeAlert(@PathVariable String alertId) {
        return mapper.alert(alerts.unsnooze(id(alertId)), null);
    }

    /** {@code setAlertFeedback}: in any state. */
    @PutMapping("/{alertId}/feedback")
    @Operation(operationId = "setAlertFeedback", tags = "Alerts")
    public Map<String, Object> setAlertFeedback(@PathVariable String alertId, @RequestBody(required = false) JsonNode body) {
        AlertService.Transition transition = AlertService.transition(body, true);
        return mapper.alert(alerts.setFeedback(id(alertId), transition), null);
    }

    /** {@code streamAlerts}: SSE with {@code id:} = alert rev. */
    @GetMapping("/stream")
    @Operation(operationId = "streamAlerts", tags = "Alerts")
    public SseEmitter streamAlerts(
            @RequestHeader(value = StreamController.LAST_EVENT_ID, required = false) String lastEventId,
            HttpServletRequest request,
            HttpServletResponse response) {
        StreamPrincipal principal = principals.resolve(request);
        AlertQuery filter = topic.parseQuery(new TopicParams("", parameters(request)), principal);
        Long resume = rev(lastEventId);
        Map<String, Long> resumes = resume == null ? Map.of() : Map.of(AlertsTopic.NAME, resume);
        StreamFilter streamFilter = new StreamFilter(List.of(AlertsTopic.NAME), Map.of(AlertsTopic.NAME, filter), 1);
        SseEmitter emitter = new SseEmitter(0L);
        SseClient client = hub.open(new SseHub.OpenRequest(principal, SseClient.CursorMode.SINGLE, streamFilter, resumes,
                false), new EmitterSseSender(emitter));
        emitter.onCompletion(client::close);
        emitter.onTimeout(client::close);
        emitter.onError(e -> client.close());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        return emitter;
    }

    // ------------------------------------------------------------------------- helpers

    private AlertView visible(String alertId, HttpServletRequest request) {
        AlertView alert = alerts.get(id(alertId));
        if (callers.excludedSources(callers.resolve(request)).contains(alert.sourceId())) {
            throw NotFoundException.of("Alert", alertId);
        }
        return alert;
    }

    /** A stable description of a listing, for its cursor fingerprint. */
    static String canonical(AlertQuery q, int size) {
        return String.join("|", "alerts",
                q.ruleIds().stream().map(UUID::toString).sorted().toList().toString(),
                q.sources().stream().sorted().toList().toString(),
                q.kinds().stream().sorted().toList().toString(),
                String.valueOf(q.since()), String.valueOf(q.acknowledged()),
                q.states().stream().sorted().toList().toString(),
                q.feedback().stream().sorted().toList().toString(),
                String.valueOf(q.minSeverity()), String.valueOf(q.tag()), String.valueOf(q.includeSuppressed()),
                q.excludedSources().stream().sorted().toList().toString(), Integer.toString(size));
    }

    static long id(String alertId) {
        try {
            return Long.parseLong(alertId);
        } catch (NumberFormatException e) {
            throw NotFoundException.of("Alert", alertId);
        }
    }

    private static UUID uuid(String field, String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField(field, "must be a rule id (UUID)");
        }
    }

    /** The resume rev from {@code Last-Event-ID}, or null. */
    static Long rev(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            long rev = Long.parseLong(text.strip());
            if (rev >= 0) {
                return rev;
            }
        } catch (NumberFormatException e) {
            // reported below
        }
        throw new ValidationException(ErrorCodes.INVALID_CURSOR, "The resume cursor is not valid.",
                List.of(new FieldError(StreamController.LAST_EVENT_ID, "must be a non-negative alert rev")));
    }

    private static Map<String, List<String>> parameters(HttpServletRequest request) {
        Map<String, List<String>> out = new HashMap<>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            out.put(entry.getKey(), Arrays.asList(entry.getValue()));
        }
        return out;
    }
}
