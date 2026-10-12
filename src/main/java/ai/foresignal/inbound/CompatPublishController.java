package ai.foresignal.inbound;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.security.IngestCaller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * ntfy- and Gotify-compatible publish outside {@code /v1} (prompt 024): {@code ntfyPublish},
 * {@code ntfyPublishPut}, {@code ntfyPublishViaGet}, {@code ntfyPublishViaPost},
 * {@code ntfyPublishJson} and {@code gotifyPublish}.
 *
 * <p>The ntfy topic is the custom source id; the Gotify key identifies the source. Credentials are
 * checked by the token filter's ingest authenticator, including ntfy's {@code ?auth=} and Gotify's
 * {@code X-Gotify-Key} and {@code ?token=}; their values are redacted from logs and from
 * {@code fetch_log}.
 */
@RestController
@Tag(name = "Ingest")
public class CompatPublishController {

    private static final Set<String> NTFY_ACTIONS = Set.of("publish", "send", "trigger");
    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private final CompatPublishService service;
    private final LoopGuard loopGuard;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the controller. */
    public CompatPublishController(CompatPublishService service, LoopGuard loopGuard, JsonMapper json, Clock clock) {
        this.service = service;
        this.loopGuard = loopGuard;
        this.json = json;
        this.clock = clock;
    }

    /** {@code ntfyPublish}: POST, body = message. */
    @PostMapping("/compat/ntfy/{topic}")
    @Operation(operationId = "ntfyPublish", tags = "Ingest")
    public Map<String, Object> ntfyPublish(@PathVariable String topic, HttpServletRequest request) {
        return ntfyBody(topic, request);
    }

    /** {@code ntfyPublishPut}: PUT, body = message. */
    @PutMapping("/compat/ntfy/{topic}")
    @Operation(operationId = "ntfyPublishPut", tags = "Ingest")
    public Map<String, Object> ntfyPublishPut(@PathVariable String topic, HttpServletRequest request) {
        return ntfyBody(topic, request);
    }

    /** {@code ntfyPublishViaGet}: fields from the query string. */
    @GetMapping("/compat/ntfy/{topic}/{action}")
    @Operation(operationId = "ntfyPublishViaGet", tags = "Ingest")
    public Map<String, Object> ntfyPublishViaGet(@PathVariable String topic, @PathVariable String action,
            HttpServletRequest request) {
        return ntfyQuery(topic, action, request);
    }

    /** {@code ntfyPublishViaPost}: fields from the query string; the body is ignored. */
    @PostMapping("/compat/ntfy/{topic}/{action}")
    @Operation(operationId = "ntfyPublishViaPost", tags = "Ingest")
    public Map<String, Object> ntfyPublishViaPost(@PathVariable String topic, @PathVariable String action,
            HttpServletRequest request) {
        return ntfyQuery(topic, action, request);
    }

    /** {@code ntfyPublishJson}: {@code {topic, message, title, priority, tags, click}} (Uptime Kuma). */
    @PostMapping("/compat/ntfy")
    @Operation(operationId = "ntfyPublishJson", tags = "Ingest")
    public Map<String, Object> ntfyPublishJson(HttpServletRequest request) {
        Instant receivedAt = clock.instant();
        byte[] body = IngestBodyReader.read(request);
        JsonNode node;
        try {
            node = json.readTree(body);
        } catch (RuntimeException e) {
            throw new InvalidInputException("The body must be a JSON object.");
        }
        if (node == null || !node.isObject()) {
            throw new InvalidInputException("The body must be a JSON object.");
        }
        String topic = text(node, "topic");
        if (topic == null || topic.isBlank()) {
            throw new InvalidInputException("topic is required.");
        }
        IngestCaller caller = InboundRequests.caller(request, topic);
        List<String> tags = new ArrayList<>();
        String id = header(request, "X-Foresignal-External-Id");
        JsonNode tagNode = node.get("tags");
        if (tagNode != null && tagNode.isArray()) {
            for (JsonNode tag : tagNode) {
                CompatPublishService.Tags parsed = CompatPublishService.ntfyTags(tag.asString());
                tags.addAll(parsed.tags());
                if (id == null && parsed.externalId() != null) {
                    id = parsed.externalId();
                }
            }
        }
        JsonNode priority = node.get("priority");
        Integer prio = priority == null || priority.isNull() ? null
                : CompatPublishService.ntfyPriority(priority.asString());
        CompatPublishService.Message message = new CompatPublishService.Message(topic, text(node, "title"),
                text(node, "message"), prio, tags, text(node, "click"), id, null);
        if (loopGuard.isOwn(request, body, rawTags(node.get("tags")), null)) {
            return CompatPublishService.ntfyResponse(service.dropped(message, receivedAt), message, topic);
        }
        CompatPublishService.Published published = service.publish(topic, caller, message, receivedAt, body.length,
                InboundRequests.sha256(body), IngestUrls.redacted(request));
        return CompatPublishService.ntfyResponse(published, message, topic);
    }

    /** {@code gotifyPublish}: JSON or form {@code title, message, priority, extras}; the key picks the source. */
    @PostMapping("/compat/gotify/message")
    @Operation(operationId = "gotifyPublish", tags = "Ingest")
    public Map<String, Object> gotifyPublish(HttpServletRequest request) {
        IngestCaller caller = InboundRequests.caller(request, null);
        if (caller.sourceId() == null) {
            throw new InvalidInputException(
                    "Gotify publish is identified by the source's ingest key (X-Gotify-Key or ?token=).");
        }
        Instant receivedAt = clock.instant();
        String title;
        String text;
        Integer priority = null;
        Map<String, Object> extras = null;
        byte[] body;
        if (isJson(request.getContentType())) {
            body = IngestBodyReader.read(request);
            JsonNode node;
            try {
                node = json.readTree(body);
            } catch (RuntimeException e) {
                throw new InvalidInputException("The body must be a JSON object.");
            }
            if (node == null || !node.isObject()) {
                throw new InvalidInputException("The body must be a JSON object.");
            }
            title = text(node, "title");
            text = text(node, "message");
            JsonNode p = node.get("priority");
            if (p != null && !p.isNull()) {
                if (!p.isIntegralNumber() || p.asInt() < 0 || p.asInt() > 10) {
                    throw new InvalidInputException("priority must be an integer from 0 to 10.");
                }
                priority = p.asInt();
            }
            JsonNode e = node.get("extras");
            if (e != null && e.isObject()) {
                extras = json.convertValue(e, MAP);
            }
        } else {
            title = request.getParameter("title");
            text = request.getParameter("message");
            String p = request.getParameter("priority");
            if (p != null && !p.isBlank()) {
                try {
                    priority = Integer.parseInt(p.strip());
                } catch (NumberFormatException ex) {
                    throw new InvalidInputException("priority must be an integer from 0 to 10.");
                }
                if (priority < 0 || priority > 10) {
                    throw new InvalidInputException("priority must be an integer from 0 to 10.");
                }
            }
            body = (title == null ? "" : title).concat("\n").concat(text == null ? "" : text)
                    .getBytes(StandardCharsets.UTF_8);
        }
        if (text == null) {
            throw new InvalidInputException("message is required.");
        }
        String id = header(request, "X-Foresignal-External-Id");
        if (id == null && extras != null && extras.get("foresignal::record") instanceof Map<?, ?> record
                && record.get("externalId") != null) {
            id = record.get("externalId").toString();
        }
        CompatPublishService.Message message = new CompatPublishService.Message(null, title, text, priority, List.of(),
                null, id, extras);
        if (loopGuard.isOwn(request, null, null, extras)) {
            return CompatPublishService.gotifyResponse(service.dropped(message, receivedAt), message);
        }
        CompatPublishService.Published published = service.publish(caller.sourceId(), caller, message, receivedAt,
                body.length, InboundRequests.sha256(body), IngestUrls.redacted(request));
        return CompatPublishService.gotifyResponse(published, message);
    }

    // ---------- ntfy helpers ----------

    private Map<String, Object> ntfyBody(String topic, HttpServletRequest request) {
        IngestCaller caller = InboundRequests.caller(request, topic);
        Instant receivedAt = clock.instant();
        byte[] body = IngestBodyReader.read(request);
        String text = body.length == 0 ? first(request, "message", "m") : new String(body, StandardCharsets.UTF_8);
        CompatPublishService.Tags tags = CompatPublishService.ntfyTags(
                firstNonNull(header(request, "X-Tags", "Tags", "ta"), first(request, "tags", "ta")));
        String id = header(request, "X-Foresignal-External-Id");
        CompatPublishService.Message message = new CompatPublishService.Message(topic,
                firstNonNull(header(request, "X-Title", "Title", "t"), first(request, "title", "t")),
                text,
                CompatPublishService.ntfyPriority(
                        firstNonNull(header(request, "X-Priority", "Priority", "p"), first(request, "priority", "p"))),
                tags.tags(),
                firstNonNull(header(request, "X-Click", "Click"), first(request, "click")),
                id != null ? id : tags.externalId(),
                null);
        if (loopGuard.isOwn(request, null, rawTagList(firstNonNull(header(request, "X-Tags", "Tags", "ta"),
                first(request, "tags", "ta"))), null)) {
            return CompatPublishService.ntfyResponse(service.dropped(message, receivedAt), message, topic);
        }
        CompatPublishService.Published published = service.publish(topic, caller, message, receivedAt, body.length,
                InboundRequests.sha256(body), IngestUrls.redacted(request));
        return CompatPublishService.ntfyResponse(published, message, topic);
    }

    private Map<String, Object> ntfyQuery(String topic, String action, HttpServletRequest request) {
        if (!NTFY_ACTIONS.contains(action)) {
            throw new NotFoundException("Unknown ntfy action '" + action + "'.");
        }
        IngestCaller caller = InboundRequests.caller(request, topic);
        Instant receivedAt = clock.instant();
        CompatPublishService.Tags tags = CompatPublishService.ntfyTags(first(request, "tags", "ta"));
        String id = header(request, "X-Foresignal-External-Id");
        String text = first(request, "message", "m");
        CompatPublishService.Message message = new CompatPublishService.Message(topic, first(request, "title", "t"), text,
                CompatPublishService.ntfyPriority(first(request, "priority", "p")), tags.tags(),
                first(request, "click"), id != null ? id : tags.externalId(), null);
        byte[] body = (text == null ? "" : text).getBytes(StandardCharsets.UTF_8);
        if (loopGuard.isOwn(request, null, rawTagList(first(request, "tags", "ta")), null)) {
            return CompatPublishService.ntfyResponse(service.dropped(message, receivedAt), message, topic);
        }
        CompatPublishService.Published published = service.publish(topic, caller, message, receivedAt, body.length,
                InboundRequests.sha256(body), IngestUrls.redacted(request));
        return CompatPublishService.ntfyResponse(published, message, topic);
    }

    /** Every tag as sent, including an {@code id=} tag, for the loop guard. */
    private static List<String> rawTagList(String value) {
        List<String> out = new ArrayList<>();
        if (value != null) {
            for (String part : value.split(",")) {
                if (!part.isBlank()) {
                    out.add(part.strip());
                }
            }
        }
        return out;
    }

    private static List<String> rawTags(JsonNode tags) {
        List<String> out = new ArrayList<>();
        if (tags != null && tags.isArray()) {
            tags.forEach(t -> out.addAll(rawTagList(t.asString())));
        }
        return out;
    }

    private static String header(HttpServletRequest request, String... names) {
        for (String name : names) {
            String value = request.getHeader(name);
            if (value != null && !value.isBlank()) {
                return value.strip();
            }
        }
        return null;
    }

    private static String first(HttpServletRequest request, String... names) {
        for (String name : names) {
            String value = queryParameter(request.getQueryString(), name);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /** A query-string parameter, read without touching the body. */
    static String queryParameter(String query, String name) {
        if (query == null) {
            return null;
        }
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            String key = java.net.URLDecoder.decode(eq < 0 ? pair : pair.substring(0, eq), StandardCharsets.UTF_8);
            if (key.equals(name)) {
                return eq < 0 ? "" : java.net.URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private static String firstNonNull(String a, String b) {
        return a != null ? a : b;
    }

    private static String text(JsonNode node, String name) {
        JsonNode value = node.get(name);
        return value == null || value.isNull() || !value.isValueNode() ? null : value.asString();
    }

    private static boolean isJson(String contentType) {
        if (contentType == null) {
            return false;
        }
        try {
            return MediaType.parseMediaType(contentType).isCompatibleWith(MediaType.APPLICATION_JSON);
        } catch (org.springframework.util.InvalidMimeTypeException e) {
            return false;
        }
    }
}
