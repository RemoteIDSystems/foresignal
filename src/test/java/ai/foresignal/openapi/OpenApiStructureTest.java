package ai.foresignal.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.testsupport.openapi.OpenApiSpec;
import ai.foresignal.testsupport.openapi.OpenApiSpec.SpecOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

/**
 * The shape of {@code docs/OpenAPI.json} that the conformance machinery relies on (prompt 020):
 * an OAS 3.1 document, the outbound {@code webhooks}, the SSE event map on every streaming
 * operation, and a scope on every operation.
 */
class OpenApiStructureTest {

    private static final JsonNode DOC = OpenApiSpec.document();

    @Test
    @DisplayName("the document parses as OpenAPI 3.1 with info, paths and components")
    void isOpenApi31() {
        assertThat(DOC.get("openapi").asString()).startsWith("3.1.");
        assertThat(DOC.get("info").get("version").asString()).matches("\\d+\\.\\d+\\.\\d+");
        assertThat(DOC.get("paths").isObject()).isTrue();
        assertThat(DOC.get("components").get("schemas").isObject()).isTrue();
        assertThat(DOC.get("info").get("x-foresignal-changelog").isArray()).isTrue();
    }

    @Test
    @DisplayName("webhooks.notification is a POST with the signature headers and a WebhookEnvelope body")
    void notificationWebhook() {
        JsonNode post = DOC.get("webhooks").get("notification").get("post");
        assertThat(post).as("webhooks.notification.post").isNotNull();
        List<String> headers = StreamSupport.stream(post.get("parameters").spliterator(), false)
                .filter(p -> "header".equals(p.get("in").asString()))
                .map(p -> p.get("name").asString())
                .toList();
        assertThat(headers).contains("X-Foresignal-Event", "X-Foresignal-Delivery", "X-Foresignal-Timestamp",
                "X-Foresignal-Signature", "X-Foresignal-Origin");
        assertThat(post.get("requestBody").get("content").get("application/json").get("schema").get("$ref").asString())
                .isEqualTo("#/components/schemas/WebhookEnvelope");
        assertThat(post.get("description").asString())
                .as("signature recipe and retry ladder")
                .contains("sha256=HMAC", "<timestamp>.<body>", "retried");
        assertThat(post.get("responses").propertyNames()).anyMatch(code -> code.startsWith("2"));
        assertThat(post.get("x-foresignal-scope").asString()).isEqualTo("open");
    }

    @Test
    @DisplayName("webhooks.watchdogHeartbeat is a bodiless GET and POST")
    void watchdogWebhook() {
        JsonNode item = DOC.get("webhooks").get("watchdogHeartbeat");
        for (String method : List.of("get", "post")) {
            JsonNode op = item.get(method);
            assertThat(op).as(method).isNotNull();
            assertThat(op.has("requestBody")).as(method + " has no body").isFalse();
            assertThat(op.get("x-foresignal-scope").asString()).isEqualTo("open");
        }
    }

    @Test
    @DisplayName("every webhook is open, declares security and documents a response")
    void webhooksAreOutbound() {
        for (SpecOperation op : OpenApiSpec.webhooks()) {
            assertThat(op.scope()).as(op.label()).isEqualTo("open");
            assertThat(op.node().has("security")).as(op.label() + " security").isTrue();
            assertThat(op.node().get("responses").isObject()).as(op.label() + " responses").isTrue();
        }
    }

    @Test
    @DisplayName("every SSE operation has x-foresignal-sse-events whose schemas exist, and its stream schema lists them")
    void sseOperationsMapTheirEvents() {
        List<String> problems = new ArrayList<>();
        int sse = 0;
        for (SpecOperation op : OpenApiSpec.routes()) {
            JsonNode stream = eventStream(op.node());
            if (stream == null) {
                continue;
            }
            sse++;
            JsonNode events = op.node().get("x-foresignal-sse-events");
            if (events == null || !events.isObject() || events.isEmpty()) {
                problems.add(op.label() + ": no x-foresignal-sse-events");
                continue;
            }
            Set<String> listed = refs(stream.get("schema"));
            for (String name : events.propertyNames()) {
                JsonNode event = events.get(name);
                String ref = event.path("schema").path("$ref").asString();
                if (!ref.startsWith("#/components/schemas/")
                        || !DOC.get("components").get("schemas").has(ref.substring("#/components/schemas/".length()))) {
                    problems.add(op.label() + ": event " + name + " has no component schema (" + ref + ")");
                } else if (!listed.contains(ref)) {
                    problems.add(op.label() + ": event " + name + " schema " + ref + " missing from the text/event-stream anyOf");
                }
                // Events owned by later prompts declare their id line when they are built.
                boolean later = event.has("prompt") && Integer.parseInt(event.get("prompt").asString()) > OpenApiSpec.currentPrompt();
                if (!later && (event.get("id") == null || !event.get("id").isBoolean())) {
                    problems.add(op.label() + ": event " + name + " does not state whether it carries an id: line");
                }
            }
        }
        assertThat(sse).as("SSE operations found").isGreaterThanOrEqualTo(3);
        assertThat(problems).isEmpty();
    }

    @Test
    @DisplayName("only change carries an id: line on /v1/changes/stream")
    void changeStreamIds() {
        JsonNode events = DOC.get("paths").get("/v1/changes/stream").get("get").get("x-foresignal-sse-events");
        for (String name : events.propertyNames()) {
            assertThat(events.get(name).get("id").asBoolean()).as(name).isEqualTo("change".equals(name));
        }
    }

    @Test
    @DisplayName("the event payload schemas of 015, 017 and 018 exist")
    void eventSchemasExist() {
        JsonNode schemas = DOC.get("components").get("schemas");
        for (String name : List.of("Change", "ChangeEvent", "LiveEvent", "TouchEvent", "ResyncEvent", "ResetEvent",
                "HeartbeatEvent", "HelloEvent", "FilterAppliedEvent", "AlertEvent", "NotificationMessage",
                "WebhookEnvelope")) {
            assertThat(schemas.has(name)).as(name).isTrue();
        }
        JsonNode alertType = schemas.get("AlertEvent").get("properties").get("type").get("enum");
        List<String> types = StreamSupport.stream(alertType.spliterator(), false).map(JsonNode::asString).toList();
        assertThat(types).contains("fired", "acknowledged", "snoozed", "resolved");
    }

    @Test
    @DisplayName("every operation has x-foresignal-scope")
    void everyOperationHasAScope() {
        List<String> missing = OpenApiSpec.allOperations().stream()
                .filter(op -> op.scope() == null)
                .map(SpecOperation::label)
                .toList();
        assertThat(missing).isEmpty();
    }

    /** The {@code text/event-stream} media type object of an operation's 2xx response, or null. */
    private static JsonNode eventStream(JsonNode operation) {
        JsonNode responses = operation.get("responses");
        for (String code : responses.propertyNames()) {
            if (code.startsWith("2")) {
                JsonNode stream = responses.get(code).path("content").get("text/event-stream");
                if (stream != null) {
                    return stream;
                }
            }
        }
        return null;
    }

    /** The {@code $ref}s listed in a schema's {@code anyOf} or {@code oneOf}. */
    private static Set<String> refs(JsonNode schema) {
        Set<String> out = new java.util.HashSet<>();
        if (schema == null) {
            return out;
        }
        for (String keyword : List.of("anyOf", "oneOf")) {
            JsonNode list = schema.get(keyword);
            if (list != null) {
                list.forEach(s -> out.add(s.path("$ref").asString()));
            }
        }
        return out;
    }
}
