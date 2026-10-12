package ai.foresignal.security.restrict;

import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_A;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_B;
import static ai.foresignal.testsupport.records.RecordsTestSources.SCHEMA_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.stream.SseTestClient;
import ai.foresignal.testsupport.stream.StreamTestSources;
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

/**
 * Feed keys over a real socket (prompt 035): the admin API, {@code /v1/k/{key}/…} for a valid
 * prefix, a wrong prefix, POST, an expired and a revoked key, a browser {@code EventSource} stream,
 * the hygiene headers and that the key never appears in a response body. {@code openReads} is off,
 * so the key is what authenticates. Every HTTP call and stream read has a timeout.
 */
@WebIntegrationTest
@Import(StreamTestSources.class)
class PathKeyFilterIT {

    private static final Instant T0 = Instant.parse("2026-10-12T12:00:00Z");
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @LocalServerPort int port;
    @Autowired MutableClock clock;
    @Autowired AdminTokenService adminTokens;
    @Autowired AppSettingService settings;
    @Autowired TestRecords records;
    @Autowired SseHub hub;
    @Autowired JsonMapper json;

    private final List<SseTestClient> streams = new ArrayList<>();
    private String admin;

    @BeforeEach
    void setUp() {
        clock.setInstant(T0);
        settings.invalidateCache();
        settings.put(SettingKeys.SECURITY, new SecuritySettings(false));
        admin = adminTokens.rotate();
    }

    @AfterEach
    void tearDown() {
        streams.forEach(SseTestClient::close);
        hub.clients().forEach(SseClient::close);
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
    }

    private String base() {
        return "http://localhost:" + port;
    }

    private HttpResponse<String> send(HttpRequest.Builder request) throws Exception {
        return HTTP.send(request.timeout(TIMEOUT).build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws Exception {
        return send(HttpRequest.newBuilder(URI.create(base() + path)).GET());
    }

    private HttpResponse<String> admin(String method, String path, String body) throws Exception {
        return send(HttpRequest.newBuilder(URI.create(base() + path)).header("Authorization", "Bearer " + admin)
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body)));
    }

    private static String input(String prefixes, String restrictions, Instant expiresAt) {
        return "{\"name\":\"wall\",\"prefixes\":" + prefixes + ",\"restrictions\":" + restrictions
                + ",\"expiresAt\":\"" + expiresAt + "\"}";
    }

    private JsonNode create() throws Exception {
        HttpResponse<String> response = admin("POST", "/v1/feed-keys", input(
                "[\"/v1/records\",\"/v1/changes/stream\"]", "{\"sources\":[\"" + SCHEMA_A + "\"]}",
                T0.plus(Duration.ofDays(30))));
        assertThat(response.statusCode()).as(response.body()).isEqualTo(201);
        return json.readTree(response.body());
    }

    @Test
    @DisplayName("creation returns the key and ready-made URLs once; listings never carry the key")
    void createAndList() throws Exception {
        JsonNode created = create();
        String key = created.get("key").asString();
        assertThat(key).matches("^fsf_[A-Za-z0-9_-]{43}$");
        assertThat(created.get("keyPrefix").asString()).isEqualTo(key.substring(0, 8));
        assertThat(created.get("urls")).hasSize(2);
        assertThat(created.get("urls").get(0).get("url").asString()).endsWith("/v1/k/" + key + "/records");
        assertThat(created.get("restrictions").get("excludePrivate").asBoolean()).isTrue();
        assertThat(created.get("privateAccess").asBoolean()).isFalse();
        HttpResponse<String> list = admin("GET", "/v1/feed-keys", null);
        assertThat(list.statusCode()).isEqualTo(200);
        assertThat(list.body()).contains(created.get("id").asString()).doesNotContain(key);
        assertThat(admin("GET", "/v1/tokens", null).body()).as("feed keys are not tokens")
                .doesNotContain(created.get("id").asString());
    }

    @Test
    @DisplayName("invalid inputs are 400: no restriction, endpoints, too long a life, an unreadable prefix")
    void validation() throws Exception {
        Instant month = T0.plus(Duration.ofDays(30));
        for (String bad : List.of(
                input("[\"/v1/records\"]", "{}", month),
                input("[\"/v1/records\"]", "{\"sources\":[\"" + SCHEMA_A + "\"],\"endpoints\":[\"GET /v1/records\"]}",
                        month),
                input("[\"/v1/records\"]", "{\"sources\":[\"" + SCHEMA_A + "\"]}", T0.plus(Duration.ofDays(366))),
                input("[\"/v1/records\"]", "{\"sources\":[\"" + SCHEMA_A + "\"]}", T0.minusSeconds(1)),
                input("[\"/v1/tokens\"]", "{\"sources\":[\"" + SCHEMA_A + "\"]}", month),
                input("[\"/v1/records/../tokens\"]", "{\"sources\":[\"" + SCHEMA_A + "\"]}", month),
                input("[]", "{\"sources\":[\"" + SCHEMA_A + "\"]}", month))) {
            assertThat(admin("POST", "/v1/feed-keys", bad).statusCode()).as(bad).isEqualTo(400);
        }
    }

    @Test
    @DisplayName("a valid prefix reads with hygiene headers and only the key's sources; others are refused")
    void readsThroughTheKey() throws Exception {
        records.ingestAt(SCHEMA_A, T0, NormalizedRecord.builder("a1", SCHEMA_KIND).observedAt(T0).point(1, 1).build());
        records.ingestAt(SCHEMA_B, T0, NormalizedRecord.builder("b1", SCHEMA_KIND).observedAt(T0).point(1, 1).build());
        String key = create().get("key").asString();

        HttpResponse<String> ok = get("/v1/k/" + key + "/records?order=id");
        assertThat(ok.statusCode()).as(ok.body()).isEqualTo(200);
        assertThat(ok.body()).contains("a1").doesNotContain("b1").doesNotContain(key);
        assertThat(ok.headers().firstValue("Referrer-Policy")).hasValue("no-referrer");
        assertThat(ok.headers().firstValue("X-Robots-Tag")).hasValue("noindex");
        assertThat(ok.headers().firstValue("Cache-Control").orElse("")).startsWith("private")
                .doesNotContain("public");

        HttpResponse<String> wrong = get("/v1/k/" + key + "/alerts");
        assertThat(wrong.statusCode()).isEqualTo(403);
        assertThat(wrong.body()).contains("RESTRICTED").doesNotContain(key);
        HttpResponse<String> post = send(HttpRequest.newBuilder(URI.create(base() + "/v1/k/" + key + "/records"))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString("{}")));
        assertThat(post.statusCode()).isEqualTo(405);
        HttpResponse<String> invalid = get("/v1/k/fsf_unknown/records");
        assertThat(invalid.statusCode()).isEqualTo(401);
        assertThat(invalid.body()).contains("INVALID_FEED_KEY").doesNotContain("fsf_unknown");
        assertThat(get("/v1/records").statusCode()).as("openReads is off").isEqualTo(401);
    }

    @Test
    @DisplayName("039: privateAccess is accepted and reflected; excludePrivate:false needs it")
    void privateAccess() throws Exception {
        Instant month = T0.plus(Duration.ofDays(30));
        String granted = "{\"name\":\"family-kiosk\",\"prefixes\":[\"/v1/records\"],\"restrictions\":{\"sources\":[\""
                + SCHEMA_A + "\"]},\"expiresAt\":\"" + month + "\",\"privateAccess\":true}";
        HttpResponse<String> ok = admin("POST", "/v1/feed-keys", granted);
        assertThat(ok.statusCode()).as(ok.body()).isEqualTo(201);
        JsonNode key = json.readTree(ok.body());
        assertThat(key.get("privateAccess").asBoolean()).isTrue();
        assertThat(key.get("restrictions").get("excludePrivate").asBoolean()).isFalse();

        String withoutGrant = "{\"name\":\"kiosk\",\"prefixes\":[\"/v1/records\"],\"restrictions\":{\"sources\":[\""
                + SCHEMA_A + "\"],\"excludePrivate\":false},\"expiresAt\":\"" + month + "\"}";
        assertThat(admin("POST", "/v1/feed-keys", withoutGrant).statusCode()).isEqualTo(400);
        assertThat(admin("POST", "/v1/feed-keys", withoutGrant.substring(0, withoutGrant.length() - 1) + ",\"privateAccess\":\"yes\"}")
                .statusCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("an expired key and a revoked key answer 410")
    void expiredAndRevoked() throws Exception {
        JsonNode created = create();
        String key = created.get("key").asString();
        assertThat(get("/v1/k/" + key + "/records").statusCode()).isEqualTo(200);
        assertThat(admin("DELETE", "/v1/feed-keys/" + created.get("id").asString(), null).statusCode()).isEqualTo(204);
        HttpResponse<String> revoked = get("/v1/k/" + key + "/records");
        assertThat(revoked.statusCode()).isEqualTo(410);
        assertThat(revoked.body()).contains("FEED_KEY_GONE");

        String other = create().get("key").asString();
        clock.advance(Duration.ofDays(31));
        assertThat(get("/v1/k/" + other + "/records").statusCode()).isEqualTo(410);
    }

    @Test
    @DisplayName("a browser EventSource streams changes through the key, inside its restriction only")
    void eventSource() throws Exception {
        String key = create().get("key").asString();
        SseTestClient client = SseTestClient.connect(base(), "/v1/k/" + key + "/changes/stream", Map.of());
        streams.add(client);
        assertThat(client.status()).isEqualTo(200);
        assertThat(client.header("Referrer-Policy")).isEqualTo("no-referrer");
        records.ingestAt(SCHEMA_B, T0, NormalizedRecord.builder("b9", SCHEMA_KIND).observedAt(T0).point(1, 1).build());
        records.ingestAt(SCHEMA_A, T0, NormalizedRecord.builder("a9", SCHEMA_KIND).observedAt(T0).point(1, 1).build());
        SseTestClient.Event change = client.next("change", TIMEOUT);
        assertThat(change).isNotNull();
        assertThat(change.data()).contains("a9").doesNotContain("b9");
        assertThat(client.received()).noneSatisfy(e -> assertThat(e.data()).contains("b9"));
        assertThat(hub.clients()).anySatisfy(c -> assertThat(c.principal().id()).startsWith("feed:"));
    }
}
