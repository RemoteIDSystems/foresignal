package ai.foresignal.mcp.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.mcp.McpTestSources;
import ai.foresignal.testsupport.security.TestTokens;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
 * RFC 9728 protected resource metadata over a real socket (prompt 141): both well-known paths, open, with every
 * member; {@code 404 MCP_OAUTH_DISABLED} in {@code token} mode; and {@code GET/PUT /v1/settings/mcp-auth} (admin
 * only, {@code admin} never mappable, the outbound URL policy on the issuer).
 */
@WebIntegrationTest
@Import(McpTestSources.class)
class ProtectedResourceMetadataIT {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String ISSUER = "https://idp.example/realms/home";
    private static final List<String> PATHS = List.of("/.well-known/oauth-protected-resource",
            "/.well-known/oauth-protected-resource/mcp");

    @LocalServerPort int port;
    @Autowired AppSettingService settings;
    @Autowired TokenService tokens;

    private String admin;
    private String reader;

    @BeforeEach
    void setUp() {
        settings.invalidateCache();
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
        settings.remove(SettingKeys.MCP_AUTH);
        settings.put(SettingKeys.SERVER, ServerSettings.defaults());
        admin = TestTokens.bearer(TestTokens.create(tokens, "prm-admin", Scope.ADMIN));
        reader = TestTokens.bearer(TestTokens.create(tokens, "prm-reader", Scope.READ));
    }

    @AfterEach
    void tearDown() {
        settings.remove(SettingKeys.MCP_AUTH);
        settings.put(SettingKeys.SERVER, ServerSettings.defaults());
    }

    private String base() {
        return "http://localhost:" + port;
    }

    private HttpResponse<String> send(String method, String path, String bearer, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base() + path)).timeout(TIMEOUT)
                .header("Accept", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(body));
        if (body != null) {
            request.header("Content-Type", "application/json");
        }
        if (bearer != null) {
            request.header("Authorization", bearer);
        }
        return HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static List<String> strings(JsonNode array) {
        List<String> out = new ArrayList<>();
        array.forEach(item -> out.add(item.asString()));
        return out;
    }

    @Test
    @DisplayName("mode token (the default): both well-known paths answer 404 MCP_OAUTH_DISABLED, without a token")
    void disabledByDefault() throws Exception {
        for (String path : PATHS) {
            HttpResponse<String> response = send("GET", path, null, null);
            assertThat(response.statusCode()).as(path).isEqualTo(404);
            assertThat(JSON.readTree(response.body()).get("code").asString()).isEqualTo("MCP_OAUTH_DISABLED");
        }
    }

    @Test
    @DisplayName("mode oauth: both paths serve the same document with every RFC 9728 member, no token needed")
    void metadata() throws Exception {
        settings.put(SettingKeys.SERVER, new ServerSettings(base()));
        HttpResponse<String> put = send("PUT", "/v1/settings/mcp-auth", admin,
                "{\"mode\":\"oauth\",\"issuer\":\"" + ISSUER + "\"}");
        assertThat(put.statusCode()).as(put.body()).isEqualTo(200);

        List<JsonNode> documents = new ArrayList<>();
        for (String path : PATHS) {
            HttpResponse<String> response = send("GET", path, null, null);
            assertThat(response.statusCode()).as(path).isEqualTo(200);
            assertThat(response.headers().firstValue("Content-Type").orElse("")).startsWith("application/json");
            JsonNode body = JSON.readTree(response.body());
            assertThat(body.get("resource").asString()).isEqualTo(base() + "/mcp");
            assertThat(strings(body.get("authorization_servers"))).containsExactly(ISSUER);
            assertThat(strings(body.get("scopes_supported"))).containsExactly("foresignal:agent", "foresignal:read");
            assertThat(strings(body.get("bearer_methods_supported"))).containsExactly("header");
            assertThat(body.get("resource_name").asString()).isEqualTo("Foresignal");
            assertThat(body.get("resource_documentation").asString()).isEqualTo(base() + "/docs");
            documents.add(body);
        }
        assertThat(documents.get(0)).isEqualTo(documents.get(1));
    }

    @Test
    @DisplayName("an explicit audience is the resource; a custom scope map is what scopes_supported lists")
    void explicitAudienceAndScopes() throws Exception {
        HttpResponse<String> put = send("PUT", "/v1/settings/mcp-auth", admin, """
                {"mode":"both","issuer":"%s","audience":"https://foresignal.example/mcp",
                 "scopeMap":{"mcp:read":"read","mcp:write":"agent"}}""".formatted(ISSUER));
        assertThat(put.statusCode()).as(put.body()).isEqualTo(200);
        JsonNode body = JSON.readTree(send("GET", PATHS.get(1), null, null).body());
        assertThat(body.get("resource").asString()).isEqualTo("https://foresignal.example/mcp");
        assertThat(strings(body.get("scopes_supported"))).containsExactly("mcp:read", "mcp:write");
    }

    @Test
    @DisplayName("GET /v1/settings/mcp-auth is admin only and returns the defaults when never set")
    void settingsAdminOnly() throws Exception {
        assertThat(send("GET", "/v1/settings/mcp-auth", null, null).statusCode()).isEqualTo(401);
        assertThat(send("GET", "/v1/settings/mcp-auth", reader, null).statusCode()).isEqualTo(403);
        assertThat(send("PUT", "/v1/settings/mcp-auth", reader, "{\"mode\":\"token\"}").statusCode()).isEqualTo(403);
        HttpResponse<String> response = send("GET", "/v1/settings/mcp-auth", admin, null);
        assertThat(response.statusCode()).isEqualTo(200);
        JsonNode body = JSON.readTree(response.body());
        assertThat(body.get("mode").asString()).isEqualTo("token");
        assertThat(body.get("scopeClaim").asString()).isEqualTo("scope");
        assertThat(body.get("scopeMap").get("foresignal:read").asString()).isEqualTo("read");
        assertThat(strings(body.get("allowedAlgs"))).containsExactly("RS256", "ES256", "EdDSA", "Ed25519");
    }

    @Test
    @DisplayName("PUT: admin is never mappable (400 VALIDATION_FAILED naming the entry); nothing is stored")
    void adminNotMappable() throws Exception {
        HttpResponse<String> response = send("PUT", "/v1/settings/mcp-auth", admin, """
                {"mode":"oauth","issuer":"%s","audience":"https://foresignal.example/mcp",
                 "scopeMap":{"foresignal:admin":"admin"}}""".formatted(ISSUER));
        assertThat(response.statusCode()).isEqualTo(400);
        JsonNode body = JSON.readTree(response.body());
        assertThat(body.get("code").asString()).isEqualTo("VALIDATION_FAILED");
        assertThat(body.toString()).contains("scopeMap.foresignal:admin").contains("never mappable");
        assertThat(JSON.readTree(send("GET", "/v1/settings/mcp-auth", admin, null).body()).get("mode").asString())
                .isEqualTo("token");
    }

    @Test
    @DisplayName("PUT: an issuer the outbound URL policy refuses is 400 URL_NOT_ALLOWED; oauth needs an issuer")
    void issuerChecked() throws Exception {
        HttpResponse<String> refused = send("PUT", "/v1/settings/mcp-auth", admin, """
                {"mode":"oauth","issuer":"http://169.254.169.254/latest","audience":"https://foresignal.example/mcp"}""");
        assertThat(refused.statusCode()).isEqualTo(400);
        assertThat(JSON.readTree(refused.body()).get("code").asString()).isEqualTo("URL_NOT_ALLOWED");
        HttpResponse<String> missing = send("PUT", "/v1/settings/mcp-auth", admin, "{\"mode\":\"oauth\"}");
        assertThat(missing.statusCode()).isEqualTo(400);
        assertThat(missing.body()).contains("issuer").contains("audience");
    }
}
