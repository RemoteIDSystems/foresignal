package ai.foresignal.object;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.stream.sse.SseClient;
import ai.foresignal.stream.sse.SseHub;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.security.TestTokens;
import ai.foresignal.testsupport.stream.SseTestClient;
import ai.foresignal.testsupport.stream.StreamTestSources;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
 * Workspace context synchronization over SSE (prompt 129, invariant OBJ-1): two clients follow one workspace, a patch
 * with the right {@code If-Match} reaches the other with {@code originClientId}, a stale one is 412, presence events
 * announce joins and leaves, and member roles are enforced. Runs on a real port, which also proves that an encoded
 * slash in an object reference reaches the controller.
 */
@WebIntegrationTest
@Import(StreamTestSources.class)
class WorkspaceSyncIT {

    static final Instant T = Instant.parse("2026-10-12T12:00:00Z");
    static final Duration WAIT = Duration.ofSeconds(5);
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final HttpClient HTTP = HttpClient.newHttpClient();

    @LocalServerPort int port;
    @Autowired TokenService tokens;
    @Autowired MutableClock clock;
    @Autowired SseHub hub;
    @Autowired AppSettingService settings;
    @Autowired WorkspacePresence presence;
    @Autowired TestRecords records;

    private final List<SseTestClient> streams = new ArrayList<>();
    private String owner;
    private String editor;
    private String viewer;
    private String stranger;
    private String editorPrincipal;
    private String viewerPrincipal;

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        TokenService.CreatedToken o = TestTokens.create(tokens, "ws-owner", Scope.AGENT);
        TokenService.CreatedToken e = TestTokens.create(tokens, "ws-editor", Scope.READ);
        TokenService.CreatedToken v = TestTokens.create(tokens, "ws-viewer", Scope.READ);
        TokenService.CreatedToken s = TestTokens.create(tokens, "ws-stranger", Scope.AGENT);
        owner = TestTokens.bearer(o);
        editor = TestTokens.bearer(e);
        viewer = TestTokens.bearer(v);
        stranger = TestTokens.bearer(s);
        editorPrincipal = e.entity().getPrincipalId();
        viewerPrincipal = v.entity().getPrincipalId();
    }

    @AfterEach
    void tearDown() {
        streams.forEach(SseTestClient::close);
        hub.clients().forEach(SseClient::close);
    }

    private String base() {
        return "http://localhost:" + port;
    }

    private HttpResponse<String> send(String method, String path, String bearer, String contentType, String body,
            Map<String, String> headers) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base() + path)).method(method,
                body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (bearer != null) {
            request.header("Authorization", bearer);
        }
        if (contentType != null) {
            request.header("Content-Type", contentType);
        }
        headers.forEach(request::header);
        try {
            return HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

    private SseTestClient follow(String bearer, String workspaceId, String clientId, String kind) {
        SseTestClient stream = SseTestClient.connect(base(), "/v1/events/stream?topics=workspace"
                + "&workspace.workspaceId=" + workspaceId + "&workspace.clientId=" + clientId
                + "&workspace.clientKind=" + kind, Map.of("Authorization", bearer));
        streams.add(stream);
        return stream;
    }

    private String createWorkspace() {
        HttpResponse<String> created = send("POST", "/v1/workspaces", owner, "application/json",
                ("{'name':'Red Sea watch','members':{'" + editorPrincipal + "':'editor','" + viewerPrincipal
                        + "':'viewer'},'context':{'timeRange':{'start':'2026-10-10T00:00:00Z'},'viewport':{'bbox':"
                        + "[32,11,45,21]}}}").replace('\'', '"'), Map.of());
        assertThat(created.statusCode()).isEqualTo(201);
        JsonNode body = JSON.readTree(created.body());
        assertThat(body.get("contextVersion").asLong()).isEqualTo(1);
        assertThat(created.headers().firstValue("ETag")).contains("\"1\"");
        return body.get("id").asString();
    }

    @Test
    @DisplayName("a patch with the right If-Match reaches the other client with originClientId; a stale one is 412")
    void contextSync() {
        String id = createWorkspace();
        SseTestClient globe = follow(owner, id, "globe-1", "globe");
        SseTestClient analyst = follow(editor, id, "analyst-1", "analyst");
        assertThat(globe.next("hello", WAIT)).isNotNull();
        assertThat(analyst.next("hello", WAIT)).isNotNull();

        String patch = "{\"focusRef\":\"situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C\",\"selectedRefs\":"
                + "[\"situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C\"]}";
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("If-Match", "\"1\"");
        headers.put("Foresignal-Client-Id", "analyst-1");
        HttpResponse<String> ok = send("PATCH", "/v1/workspaces/" + id + "/context", editor,
                "application/merge-patch+json", patch, headers);
        assertThat(ok.statusCode()).isEqualTo(200);
        assertThat(JSON.readTree(ok.body()).get("contextVersion").asLong()).isEqualTo(2);

        SseTestClient.Event event = globe.next("workspace.context", WAIT);
        assertThat(event).isNotNull();
        JsonNode data = JSON.readTree(event.data());
        assertThat(data.get("workspaceId").asString()).isEqualTo(id);
        assertThat(data.get("version").asLong()).isEqualTo(2);
        assertThat(data.get("originClientId").asString()).isEqualTo("analyst-1");
        assertThat(data.get("patch").get("focusRef").asString()).isEqualTo("situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C");
        // The sender receives its own patch too and ignores it by originClientId.
        assertThat(JSON.readTree(analyst.next("workspace.context", WAIT).data()).get("originClientId").asString())
                .isEqualTo("analyst-1");

        HttpResponse<String> stale = send("PATCH", "/v1/workspaces/" + id + "/context", owner,
                "application/merge-patch+json", "{\"focusRef\":null}", Map.of("If-Match", "1"));
        assertThat(stale.statusCode()).isEqualTo(412);
        JsonNode problem = JSON.readTree(stale.body());
        assertThat(problem.get("code").asString()).isEqualTo("WORKSPACE_CONTEXT_STALE");
        assertThat(problem.get("contextVersion").asLong()).isEqualTo(2);

        HttpResponse<String> missing = send("PATCH", "/v1/workspaces/" + id + "/context", owner,
                "application/merge-patch+json", "{\"focusRef\":null}", Map.of());
        assertThat(missing.statusCode()).isEqualTo(400);
        HttpResponse<String> invalid = send("PATCH", "/v1/workspaces/" + id + "/context", owner,
                "application/merge-patch+json", "{\"focusRef\":\"nope\"}", Map.of("If-Match", "2"));
        assertThat(invalid.statusCode()).isEqualTo(400);
        assertThat(JSON.readTree(invalid.body()).get("code").asString()).isEqualTo("OBJECT_REF_INVALID");
    }

    @Test
    @DisplayName("presence: joins and leaves are announced with the connected clients")
    void presenceEvents() {
        String id = createWorkspace();
        SseTestClient globe = follow(owner, id, "globe-1", "globe");
        assertThat(globe.next("hello", WAIT)).isNotNull();
        presence.refresh();
        JsonNode joined = JSON.readTree(globe.next("workspace.presence", WAIT).data());
        assertThat(joined.get("change").get("type").asString()).isEqualTo("joined");
        assertThat(joined.get("clients").findValuesAsString("clientKind")).containsExactly("globe");

        SseTestClient phone = follow(editor, id, "phone-1", "mobile");
        assertThat(phone.next("hello", WAIT)).isNotNull();
        presence.refresh();
        JsonNode second = JSON.readTree(globe.next("workspace.presence", WAIT).data());
        assertThat(second.get("change").get("clientId").asString()).isEqualTo("phone-1");
        assertThat(second.get("clients").findValuesAsString("clientId")).containsExactlyInAnyOrder("globe-1",
                "phone-1");

        phone.close();
        hub.clients().stream().filter(c -> c.filter().filterFor(WorkspaceTopic.NAME) instanceof WorkspaceFilter f
                && f.clientId().equals("phone-1")).forEach(SseClient::close);
        presence.refresh();
        JsonNode left = JSON.readTree(globe.next("workspace.presence", WAIT).data());
        assertThat(left.get("change").get("type").asString()).isEqualTo("left");
        assertThat(left.get("clients").findValuesAsString("clientId")).containsExactly("globe-1");
    }

    @Test
    @DisplayName("roles: viewers cannot patch, editors need agent scope for metadata, strangers see nothing")
    void rolesEnforced() {
        String id = createWorkspace();
        HttpResponse<String> viewerPatch = send("PATCH", "/v1/workspaces/" + id + "/context", viewer,
                "application/merge-patch+json", "{\"layers\":[\"vessel\"]}", Map.of("If-Match", "1"));
        assertThat(viewerPatch.statusCode()).isEqualTo(403);
        assertThat(JSON.readTree(viewerPatch.body()).get("code").asString()).isEqualTo("WORKSPACE_FORBIDDEN");
        assertThat(send("GET", "/v1/workspaces/" + id, viewer, null, null, Map.of()).statusCode()).isEqualTo(200);

        HttpResponse<String> editorMeta = send("PATCH", "/v1/workspaces/" + id, editor,
                "application/merge-patch+json", "{\"name\":\"Renamed\"}", Map.of());
        assertThat(editorMeta.statusCode()).isEqualTo(403);
        HttpResponse<String> ownerMeta = send("PATCH", "/v1/workspaces/" + id, owner, "application/merge-patch+json",
                "{\"name\":\"Renamed\"}", Map.of());
        assertThat(ownerMeta.statusCode()).isEqualTo(200);
        assertThat(JSON.readTree(ownerMeta.body()).get("name").asString()).isEqualTo("Renamed");

        HttpResponse<String> layout = send("PUT", "/v1/workspaces/" + id + "/layout", editor, "application/json",
                "{\"panels\":[\"globe\"]}", Map.of());
        assertThat(layout.statusCode()).isEqualTo(200);

        assertThat(send("GET", "/v1/workspaces/" + id, stranger, null, null, Map.of()).statusCode()).isEqualTo(404);
        SseTestClient denied = SseTestClient.connect(base(), "/v1/events/stream?topics=workspace&workspace.workspaceId="
                + id, Map.of("Authorization", stranger));
        streams.add(denied);
        assertThat(denied.status()).isEqualTo(404);
        assertThat(send("GET", "/v1/workspaces", null, null, null, Map.of()).statusCode()).isEqualTo(401);
        assertThat(send("DELETE", "/v1/workspaces/" + id, editor, null, null, Map.of()).statusCode()).isEqualTo(403);
        assertThat(send("DELETE", "/v1/workspaces/" + id, owner, null, null, Map.of()).statusCode()).isEqualTo(204);
    }

    @Test
    @DisplayName("deep links and summaries come from the workspace's current context")
    void contextLinkAndSummary() {
        String id = createWorkspace();
        HttpResponse<String> link = send("GET", "/v1/workspaces/" + id + "/context-link", viewer, null, null, Map.of());
        assertThat(link.statusCode()).isEqualTo(200);
        ContextLinks.Decoded decoded = ContextLinks.decode(JSON.readTree(link.body()).get("payload").asString());
        assertThat(decoded.workspaceId()).isEqualTo(id);
        assertThat(decoded.context().get("viewport").get("bbox")).hasSize(4);
        HttpResponse<String> summary = send("GET", "/v1/workspaces/" + id + "/summary", viewer, null, null, Map.of());
        assertThat(summary.statusCode()).isEqualTo(200);
        assertThat(JSON.readTree(summary.body()).has("layerCounts")).isTrue();
    }

    @Test
    @DisplayName("an object reference with an encoded slash reaches the controller through the servlet container")
    void encodedSlashInReference() {
        records.ingest(STANDARD, NormalizedRecord.builder("a/b", STANDARD_KIND).observedAt(T).point(1.0, 2.0).build());
        HttpResponse<String> response = send("GET", "/v1/objects/record:" + STANDARD + "%2Fa%252Fb", owner, null,
                null, Map.of());
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode envelope = JSON.readTree(response.body());
        assertThat(envelope.get("ref").asString()).isEqualTo("record:" + STANDARD + "/a%2Fb");
        assertThat(envelope.get("type").asString()).isEqualTo("record");
    }
}
