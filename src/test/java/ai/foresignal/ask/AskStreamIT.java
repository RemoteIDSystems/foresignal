package ai.foresignal.ask;

import static ai.foresignal.testsupport.llm.FakeLlmClient.call;
import static ai.foresignal.testsupport.llm.FakeLlmClient.text;
import static ai.foresignal.testsupport.llm.FakeLlmClient.toolUse;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.llm.LlmSettings;
import ai.foresignal.llm.client.LlmProviderException;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ask.AskItSupport;
import ai.foresignal.testsupport.ask.AskTestSources;
import ai.foresignal.testsupport.stream.SseTestClient;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;

/**
 * {@code askStream} over a real socket (prompt 028): event order, warnings, the {@code done} payload
 * equal to the stored message, the {@code error} event, and problems answered before a stream opens.
 * Every connection and read has a timeout.
 */
@WebIntegrationTest
@Import(AskTestSources.class)
class AskStreamIT extends AskItSupport {

    static final Duration TIMEOUT = Duration.ofSeconds(20);
    static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    static final String Q1 = AskTestSources.QUAKES + ":q1";

    @LocalServerPort int port;

    String base() {
        return "http://localhost:" + port;
    }

    SseTestClient stream(String bearer, String body) {
        Map<String, String> headers = bearer == null ? Map.of() : Map.of("Authorization", bearer);
        return SseTestClient.post(base(), "/v1/ask/stream", headers, body);
    }

    /** Every non-heartbeat event until {@code last} arrives (or the timeout passes). */
    static List<SseTestClient.Event> until(SseTestClient client, String last) {
        List<SseTestClient.Event> events = new ArrayList<>();
        long deadline = System.nanoTime() + TIMEOUT.toNanos();
        while (System.nanoTime() < deadline) {
            SseTestClient.Event event = client.next(Duration.ofSeconds(2));
            if (event == null) {
                continue;
            }
            if (!event.event().equals("heartbeat")) {
                events.add(event);
            }
            if (event.event().equals(last)) {
                return events;
            }
        }
        throw new AssertionError("no " + last + " event within " + TIMEOUT + "; got " + events);
    }

    static int indexOf(List<SseTestClient.Event> events, String name) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).event().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    @Test
    @DisplayName("events arrive in order: progress, delta, citation, warning, done; done equals the stored message")
    void eventOrderAndDone() throws Exception {
        quake("q1", "Tokyo Bay", 5.5, 139.8, 35.6);
        chat().then(toolUse(call("c1", "query_records", "{}")))
                .then(text("Tokyo Bay shook [" + Q1 + "].\nIt measured 5.5 on the scale."));

        try (SseTestClient client = stream(reader, "{\"question\":\"Quakes?\"}")) {
            assertThat(client.status()).isEqualTo(200);
            assertThat(client.header("Content-Type")).startsWith("text/event-stream");
            List<SseTestClient.Event> events = until(client, "done");

            List<String> names = events.stream().map(SseTestClient.Event::event).toList();
            assertThat(names.getFirst()).isEqualTo("progress");
            int progressDone = lastIndex(names, "progress");
            int delta = indexOf(events, "delta");
            int citation = indexOf(events, "citation");
            int warning = indexOf(events, "warning");
            int done = indexOf(events, "done");
            assertThat(progressDone).isLessThan(delta);
            assertThat(delta).isLessThan(citation);
            assertThat(citation).isLessThan(warning);
            assertThat(warning).isLessThan(done);
            assertThat(done).isEqualTo(events.size() - 1);

            JsonNode calling = JSON.readTree(events.get(0).data());
            assertThat(calling.get("tool").asString()).isEqualTo("query_records");
            assertThat(calling.get("status").asString()).isEqualTo("calling");
            assertThat(JSON.readTree(events.get(warning).data()).get("message").asString())
                    .startsWith("uncited claim: It measured 5.5");
            assertThat(JSON.readTree(events.get(citation).data()).get("id").asString()).isEqualTo(Q1);

            JsonNode response = JSON.readTree(events.get(done).data());
            JsonNode thread = getThread(response.get("threadId").asString());
            JsonNode stored = thread.get("messages").get(1);
            assertThat(stored.get("content").asString()).isEqualTo(response.get("answer").asString());
            assertThat(stored.get("citations")).isEqualTo(response.get("citations"));
            assertThat(stored.get("warnings")).isEqualTo(response.get("warnings"));
            assertThat(client.awaitEnd(TIMEOUT)).isTrue();
        }
    }

    private static int lastIndex(List<String> names, String name) {
        return names.lastIndexOf(name);
    }

    @Test
    @DisplayName("a provider failure after the stream opened is an error event, then the stream ends")
    void errorEvent() {
        chat().then(request -> {
            throw new LlmProviderException(LlmProviderException.Kind.CONNECTION, CHAT, "fake provider is down");
        });

        try (SseTestClient client = stream(reader, "{\"question\":\"Quakes?\"}")) {
            assertThat(client.status()).isEqualTo(200);
            List<SseTestClient.Event> events = until(client, "error");
            JsonNode problem = JSON.readTree(events.getLast().data());
            assertThat(problem.get("status").asInt()).isGreaterThanOrEqualTo(400);
            assertThat(problem.hasNonNull("code")).isTrue();
            assertThat(indexOf(events, "done")).isEqualTo(-1);
            assertThat(client.awaitEnd(TIMEOUT)).isTrue();
        }
    }

    @Test
    @DisplayName("problems found before the model runs are statuses, not streams: 401, 409, 400")
    void problemsBeforeTheStream() {
        try (SseTestClient client = stream(null, "{\"question\":\"Quakes?\"}")) {
            assertThat(client.status()).isEqualTo(401);
        }
        try (SseTestClient client = stream(reader, "{\"nope\":1}")) {
            assertThat(client.status()).isEqualTo(400);
        }
        llmSettings.update(new LlmSettings(null, null, true, true, false, 200_000, false));
        try (SseTestClient client = stream(reader, "{\"question\":\"Quakes?\"}")) {
            assertThat(client.status()).isEqualTo(409);
        }
        assertThat(chat().requests()).isEmpty();
    }

    private JsonNode getThread(String threadId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(base() + "/v1/ask/threads/" + threadId))
                .header("Authorization", reader)
                .timeout(TIMEOUT)
                .GET()
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        return JSON.readTree(response.body());
    }
}
