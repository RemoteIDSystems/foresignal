package ai.foresignal.api;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.support.Cursor;
import ai.foresignal.api.support.CursorCodec;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.api.support.ReadCallerResolver;
import ai.foresignal.notify.NotificationAction;
import ai.foresignal.notify.action.ActionService;
import ai.foresignal.notify.action.ActionsConfig;
import ai.foresignal.notify.inbox.DeviceKeys;
import ai.foresignal.notify.inbox.InboxFilter;
import ai.foresignal.notify.inbox.InboxService;
import ai.foresignal.notify.inbox.InboxTopic;
import ai.foresignal.notify.inbox.PresenceService;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.stream.sse.EmitterSseSender;
import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.stream.sse.StreamFilter;
import ai.foresignal.stream.sse.StreamPrincipal;
import ai.foresignal.stream.sse.StreamPrincipalResolver;
import ai.foresignal.trigger.AlertState;
import ai.foresignal.trigger.AlertStore;
import ai.foresignal.trigger.AlertView;
import ai.foresignal.trigger.Severity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.JsonNode;

/**
 * The notification inbox of the {@code Notifications} tag (prompt 033): {@code listNotifications},
 * {@code getNotification}, {@code markNotificationsRead}, {@code getNotificationBadge} and
 * {@code streamNotifications}. All need a {@code read} token (always authenticated) and a device:
 * token device metadata, else the {@code Foresignal-Device-Id} header ({@link DeviceKeys}).
 * <b>Reading never acknowledges an alert.</b>
 */
@RestController
@Tag(name = "Notifications")
@RequestMapping("/v1/notifications")
public class NotificationsController {

    private final InboxService inbox;
    private final DeviceKeys devices;
    private final ReadCallerResolver callers;
    private final ActionService actions;
    private final AlertStore alerts;
    private final PresenceService presence;
    private final SseHub hub;
    private final StreamPrincipalResolver principals;

    /** Creates the controller. */
    public NotificationsController(InboxService inbox, DeviceKeys devices, ReadCallerResolver callers,
            ActionService actions, AlertStore alerts, PresenceService presence, SseHub hub,
            StreamPrincipalResolver principals) {
        this.inbox = inbox;
        this.devices = devices;
        this.callers = callers;
        this.actions = actions;
        this.alerts = alerts;
        this.presence = presence;
        this.hub = hub;
        this.principals = principals;
    }

    /** {@code listNotifications}: newest first, keyset on id. */
    @GetMapping
    @Operation(operationId = "listNotifications", tags = "Notifications")
    @ai.foresignal.security.restrict.RestrictedRead
    public Map<String, Object> listNotifications(
            @RequestParam(required = false) String after,
            @RequestParam(required = false) String before,
            @RequestParam(required = false) List<String> events,
            @RequestParam(required = false) String minSeverity,
            @RequestParam(required = false) String unread,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit,
            HttpServletRequest request) {
        String device = devices.resolve(request);
        Long afterId = QueryParams.nonNegativeLong("after", after);
        Long beforeId = QueryParams.nonNegativeLong("before", before);
        List<String> eventList = List.copyOf(QueryParams.enumSet("events", events, InboxService.EVENTS));
        Severity min = minSeverity == null ? null
                : Severity.fromWire(QueryParams.oneOf("minSeverity", minSeverity, Severity.WIRE_VALUES, null));
        boolean unreadOnly = QueryParams.bool("unread", unread, false);
        int size = QueryParams.intInRange("limit", limit, 1, InboxService.MAX_LIMIT, InboxService.DEFAULT_LIMIT);
        String fingerprint = CursorCodec.fingerprint(String.join("|", "inbox", device, String.valueOf(afterId),
                eventList.stream().sorted().toList().toString(), String.valueOf(min), Boolean.toString(unreadOnly),
                Integer.toString(size)));
        Cursor.Seq position = CursorCodec.decode(cursor, Cursor.Seq.class, fingerprint);
        Long upper = beforeId;
        if (position != null) {
            upper = upper == null ? position.q() : Math.min(upper, position.q());
        }
        List<InboxService.Item> page = inbox.list(device,
                new InboxService.Query(afterId, upper, eventList, min, unreadOnly, size + 1),
                callers.excludedSources(request), callers.restriction(request));
        String next = null;
        if (page.size() > size) {
            page = page.subList(0, size);
            next = CursorCodec.encode(new Cursor.Seq(page.getLast().id(), null), fingerprint);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("items", page.stream().map(InboxService.Item::toMap).toList());
        out.put("nextCursor", next);
        return out;
    }

    /** {@code getNotificationBadge}: unread counts for this device. */
    @GetMapping("/badge")
    @Operation(operationId = "getNotificationBadge", tags = "Notifications")
    @ai.foresignal.security.restrict.RestrictedRead
    public Map<String, Object> getNotificationBadge(HttpServletRequest request) {
        return inbox.badge(devices.resolve(request), callers.excludedSources(request), callers.restriction(request))
                .toMap();
    }

    /** {@code markNotificationsRead}: moves the cursor or adds sparse reads; never acknowledges. */
    @PostMapping("/read")
    @Operation(operationId = "markNotificationsRead", tags = "Notifications")
    public Map<String, Object> markNotificationsRead(@RequestBody JsonNode body, HttpServletRequest request) {
        String device = devices.resolve(request);
        return inbox.markRead(device, InboxService.parseRead(body)).toMap();
    }

    /** {@code streamNotifications}: SSE with {@code id:} = notification id, plus {@code badge}. */
    @GetMapping("/stream")
    @Operation(operationId = "streamNotifications", tags = "Notifications")
    @ai.foresignal.security.restrict.RestrictedRead
    public SseEmitter streamNotifications(
            @RequestHeader(value = StreamController.LAST_EVENT_ID, required = false) String lastEventId,
            HttpServletRequest request,
            HttpServletResponse response) {
        String device = devices.resolve(request);
        StreamPrincipal principal = principals.resolve(request);
        Long resume = AlertsController.rev(lastEventId);
        InboxFilter filter = new InboxFilter(device, callers.excludedSources(request), callers.restriction(request));
        StreamFilter streamFilter = new StreamFilter(List.of(InboxTopic.NAME), Map.of(InboxTopic.NAME, filter), 1);
        Map<String, Long> resumes = resume == null ? Map.of() : Map.of(InboxTopic.NAME, resume);
        String kind = CurrentToken.of(request).map(CurrentToken::device)
                .map(d -> PresenceService.kindOf(d.platform())).orElse("web");
        presence.background(device, kind);
        inbox.touch(device);
        SseEmitter emitter = EmitterSseSender.newCommittingEmitter();
        SseClient client = hub.open(new SseHub.OpenRequest(principal, SseClient.CursorMode.SINGLE, streamFilter,
                resumes, false), new EmitterSseSender(emitter));
        emitter.onCompletion(client::close);
        emitter.onTimeout(client::close);
        emitter.onError(e -> client.close());
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        return emitter;
    }

    /** {@code getNotification}: the item with links and fresh action capabilities for this device. */
    @GetMapping("/{notificationId}")
    @Operation(operationId = "getNotification", tags = "Notifications")
    @ai.foresignal.security.restrict.RestrictedRead
    public Map<String, Object> getNotification(@PathVariable String notificationId, HttpServletRequest request) {
        String device = devices.resolve(request);
        long id = id(notificationId);
        InboxService.Item item = inbox.get(device, id, callers.excludedSources(request), callers.restriction(request));
        Map<String, Object> out = new LinkedHashMap<>(item.toMap());
        out.put("links", item.links());
        List<Map<String, Object>> list = new ArrayList<>();
        if ("alert".equals(item.event()) && item.alertId() != null
                && AlertState.FIRING.wire().equals(item.alertState())) {
            Optional<AlertView> alert = alerts.find(item.alertId());
            if (alert.isPresent()) {
                for (NotificationAction action : actions.mint(alert.get(), null, null, device,
                        ActionsConfig.DEFAULT_LIST)) {
                    list.add(action(action));
                }
            }
        }
        out.put("actions", list);
        return out;
    }

    /** The {@code NotificationAction} JSON. */
    static Map<String, Object> action(NotificationAction action) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("action", action.action());
        out.put("label", action.label());
        out.put("method", "POST");
        out.put("url", action.url());
        out.put("confirmUrl", action.url());
        out.put("params", action.duration() == null ? Map.of() : Map.of("duration", action.duration()));
        out.put("expiresAt", action.expiresAt() == null ? null : action.expiresAt().toString());
        return out;
    }

    private static long id(String text) {
        try {
            long id = Long.parseLong(text);
            if (id > 0) {
                return id;
            }
        } catch (NumberFormatException e) {
            // reported below
        }
        throw NotFoundException.of("Notification", text);
    }
}
