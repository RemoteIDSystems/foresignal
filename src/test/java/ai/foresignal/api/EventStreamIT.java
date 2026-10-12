package ai.foresignal.api;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.stream.SseTestClient;
import ai.foresignal.testsupport.stream.SseTestClient.Event;
import ai.foresignal.testsupport.stream.StreamTestSources;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The multiplexed {@code GET /v1/events/stream} and its filter PATCH (prompt 015). */
@WebIntegrationTest
@Import(StreamTestSources.class)
class EventStreamIT {

    static final Instant T = Instant.parse("2026-03-01T00:00:00Z");
    static final Duration WAIT = Duration.ofSeconds(5);
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final HttpClient HTTP = HttpClient.newHttpClient();

    @LocalServerPort int port;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired SseHub hub;
    @Autowired AppSettingService settings;
    @Autowired AdminTokenService adminTokens;

    private final List<SseTestClient> streams = new ArrayList<>();
    private Instant now = T;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
    }

    @AfterEach
    void tearDown() {
        streams.forEach(SseTestClient::close);
        hub.clients().forEach(SseClient::close);
    }

    private String base() {
        return "http://localhost:" + port;
    }

    private SseTestClient open(String query, Map<String, String> headers) {
        SseTestClient stream = SseTestClient.connect(base(), "/v1/events/stream" + query, headers);
        streams.add(stream);
        return stream;
    }

    private void ingest(String id, double lon, double lat) {
        now = now.plusSeconds(10);
        clock.setInstant(now);
        records.ingestAt(STANDARD, now,
                NormalizedRecord.builder(id, STANDARD_KIND).observedAt(now).point(lon, lat).build());
    }

    private HttpResponse<String> patch(String streamId, String body, Map<String, String> headers) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base() + "/v1/events/stream/" + streamId + "/filter"))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body));
        headers.forEach(request::header);
        try {
            return HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private static JsonNode data(Event event) {
        return JSON.readTree(event.data());
    }

    @Test
    @DisplayName("hello comes first, then changes with composite ids")
    void helloThenChanges() {
        SseTestClient stream = open("?topics=changes&changes.source=" + STANDARD, Map.of());
        assertThat(stream.status()).isEqualTo(200);
        Event hello = stream.next(WAIT);
        assertThat(hello.event()).isEqualTo("hello");
        assertThat(hello.id()).isNull();
        JsonNode json = data(hello);
        assertThat(json.get("streamId").asString()).matches("^[A-Za-z0-9_-]{22}$");
        assertThat(json.get("topics").get(0).asString()).isEqualTo("changes");
        assertThat(json.get("filterVersion").asInt()).isEqualTo(1);
        assertThat(json.get("watermarkSeq").isNumber()).isTrue();
        assertThat(json.get("streamKey").asString()).isNotBlank();

        ingest("a", 5, 5);
        Event change = stream.next("change", WAIT);
        assertThat(change.id()).isEqualTo("c=" + data(change).get("seq").asString());
    }

    @Test
    @DisplayName("token streams get no streamKey; topics defaults to changes")
    void tokenHello() {
        String token = adminTokens.rotate();
        SseTestClient stream = open("", Map.of("Authorization", "Bearer " + token));
        JsonNode hello = data(stream.next("hello", WAIT));
        assertThat(hello.has("streamKey")).isFalse();
        assertThat(hello.get("topics").get(0).asString()).isEqualTo("changes");
    }

    @Test
    @DisplayName("a composite Last-Event-ID (or ?lastEventId=) resumes the changes topic")
    void compositeResume() {
        SseTestClient first = open("?changes.source=" + STANDARD, Map.of());
        ingest("a", 1, 1);
        ingest("b", 1, 1);
        ingest("c", 1, 1);
        List<Event> all = first.collect("change", Duration.ofSeconds(2));
        assertThat(all).hasSize(3);

        SseTestClient resumed = open("?changes.source=" + STANDARD, Map.of("Last-Event-ID", all.getFirst().id()));
        assertThat(resumed.next(WAIT).event()).isEqualTo("hello");
        assertThat(resumed.collect("change", Duration.ofSeconds(2))).extracting(Event::id)
                .containsExactly(all.get(1).id(), all.get(2).id());

        SseTestClient byParam = open("?changes.source=" + STANDARD + "&lastEventId=" + all.get(1).id(), Map.of());
        assertThat(byParam.collect("change", Duration.ofSeconds(2))).extracting(Event::id)
                .containsExactly(all.get(2).id());

        // The header wins over the parameter.
        SseTestClient both = open("?changes.source=" + STANDARD + "&lastEventId=" + all.get(1).id(),
                Map.of("Last-Event-ID", all.getFirst().id()));
        assertThat(both.collect("change", Duration.ofSeconds(2))).hasSize(2);
    }

    @Test
    @DisplayName("PATCH swaps the bbox: filter-applied {atSeq} precedes the first event under the new filter")
    void patchSwapsFilter() {
        SseTestClient stream = open("?changes.bbox=0,0,10,10&changes.live=false", Map.of());
        JsonNode hello = data(stream.next("hello", WAIT));
        ingest("inside", 5, 5);
        Event first = stream.next("change", WAIT);
        long firstSeq = data(first).get("seq").asLong();

        String body = "{\"changes\":{\"bbox\":\"20,20,30,30\",\"live\":false}}";
        HttpResponse<String> response = patch(hello.get("streamId").asString(), body,
                Map.of("Foresignal-Stream-Key", hello.get("streamKey").asString()));
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(JSON.readTree(response.body())).isEqualTo(JSON.readTree(body));

        Event applied = stream.next(WAIT);
        while (applied != null && applied.event().equals("heartbeat")) {
            applied = stream.next(WAIT);
        }
        assertThat(applied).isNotNull();
        assertThat(applied.event()).isEqualTo("filter-applied");
        assertThat(applied.id()).isNull();
        assertThat(data(applied).get("atSeq").asLong()).isEqualTo(firstSeq);
        assertThat(data(applied).get("filterVersion").asInt()).isEqualTo(2);

        ingest("old-area", 5, 5);
        ingest("new-area", 25, 25);
        List<Event> changes = stream.collect("change", Duration.ofSeconds(2));
        assertThat(changes).extracting(e -> data(e).get("externalId").asString()).containsExactly("new-area");
    }

    @Test
    @DisplayName("PATCH from another principal, with a wrong key or for an unknown stream is 404")
    void patchOwnership() {
        String token = adminTokens.rotate();
        SseTestClient owned = open("", Map.of("Authorization", "Bearer " + token));
        String tokenStream = data(owned.next("hello", WAIT)).get("streamId").asString();
        String body = "{\"changes\":{\"live\":false}}";
        assertThat(patch(tokenStream, body, Map.of()).statusCode()).isEqualTo(404);
        assertThat(patch(tokenStream, body, Map.of("Authorization", "Bearer " + token)).statusCode()).isEqualTo(200);

        SseTestClient anonymous = open("", Map.of());
        JsonNode hello = data(anonymous.next("hello", WAIT));
        String anonStream = hello.get("streamId").asString();
        assertThat(patch(anonStream, body, Map.of()).statusCode()).isEqualTo(404);
        assertThat(patch(anonStream, body, Map.of("Foresignal-Stream-Key", "wrong")).statusCode()).isEqualTo(404);
        assertThat(patch(anonStream, body, Map.of("Authorization", "Bearer " + token)).statusCode()).isEqualTo(404);
        assertThat(patch("AAAAAAAAAAAAAAAAAAAAAA", body, Map.of()).statusCode()).isEqualTo(404);
    }

    @Test
    @DisplayName("an unknown topic is 400 UNKNOWN_TOPIC on connect and on PATCH; nothing is applied")
    void unknownTopic() {
        SseTestClient refused = open("?topics=changes,nope", Map.of());
        assertThat(refused.status()).isEqualTo(400);
        // alerts (017), notifications and system (018) are registered; workspace arrives with 129.
        assertThat(open("?topics=workspace", Map.of()).status()).isEqualTo(400);

        SseTestClient stream = open("", Map.of());
        JsonNode hello = data(stream.next("hello", WAIT));
        Map<String, String> key = Map.of("Foresignal-Stream-Key", hello.get("streamKey").asString());
        HttpResponse<String> response = patch(hello.get("streamId").asString(), "{\"topics\":[\"nope\"]}", key);
        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(JSON.readTree(response.body()).get("code").asString()).isEqualTo("UNKNOWN_TOPIC");
        assertThat(patch(hello.get("streamId").asString(), "{\"workspace\":{}}", key).statusCode()).isEqualTo(400);
        assertThat(patch(hello.get("streamId").asString(), "{\"changes\":{\"bbox\":\"1,2\"}}", key).statusCode())
                .isEqualTo(400);
        assertThat(patch(hello.get("streamId").asString(), "{\"changes\":{\"color\":1}}", key).statusCode())
                .isEqualTo(400);
        assertThat(hub.clients()).singleElement().satisfies(c -> assertThat(c.filter().version()).isEqualTo(1));
    }

    @Test
    @DisplayName("a malformed composite cursor is 400 INVALID_CURSOR")
    void malformedCursor() {
        assertThat(open("", Map.of("Last-Event-ID", "c=x")).status()).isEqualTo(400);
        assertThat(open("?lastEventId=12", Map.of()).status()).isEqualTo(400);
    }
}
