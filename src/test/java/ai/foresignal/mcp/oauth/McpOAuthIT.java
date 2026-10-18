package ai.foresignal.mcp.oauth;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenService;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.mcp.McpTestSources;
import ai.foresignal.testsupport.mcp.TestIdp;
import ai.foresignal.testsupport.security.TestTokens;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.nimbusds.jose.JWSAlgorithm;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
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
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * MCP remote authorization end to end (prompt 141): an in-test identity provider (keys and signer from
 * {@link TestIdp}, JWKS and discovery served by WireMock) against {@code /mcp} over a real socket.
 *
 * <p>Covers: a valid token's scopes mapped to the tools it sees and calls; wrong {@code aud}, wrong {@code iss}, a
 * refused algorithm and an expired token answered {@code 401} with {@code resource_metadata}; a missing token
 * answered {@code 401} even under open reads; a tool above the mapped scope answered {@code 403
 * insufficient_scope}; {@code admin} never mappable (refused at PUT, ignored at runtime); an unknown {@code kid}
 * triggering exactly one JWKS refresh; discovery through OpenID Connect; REST refusing identity-provider tokens; the
 * {@code oauth} and {@code both} modes for Foresignal tokens; the subject's principal; and the token never sent to
 * the identity provider.
 */
@WebIntegrationTest
@Import(McpTestSources.class)
class McpOAuthIT {

    @RegisterExtension
    static final WireMockExtension IDP_SERVER = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().gzipDisabled(true))
            .resetOnEachTest(true)
            .build();

    private static final Instant T = Instant.parse("2026-10-18T12:00:00Z");
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private static final TestIdp IDP = TestIdp.create();
    private static final String CERTS_PATH = "/realms/test/protocol/openid-connect/certs";
    private static final String INITIALIZE = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":"
            + "{\"protocolVersion\":\"2025-06-18\",\"capabilities\":{},\"clientInfo\":{\"name\":\"it\",\"version\":\"1\"}}}";

    @LocalServerPort int port;
    @Autowired MutableClock clock;
    @Autowired AppSettingService settings;
    @Autowired TokenService tokens;
    @Autowired JwksCache jwksCache;
    @Autowired JdbcTemplate jdbc;

    private final List<McpSyncClient> clients = new ArrayList<>();

    @BeforeEach
    void setUp() {
        clock.setInstant(T);
        settings.invalidateCache();
        settings.put(SettingKeys.SECURITY, SecuritySettings.defaults());
        settings.put(SettingKeys.SERVER, new ServerSettings(base()));
        jwksCache.invalidate();
        IDP_SERVER.stubFor(get(urlEqualTo(CERTS_PATH)).willReturn(json(IDP.jwks())));
        configure("oauth", IDP_SERVER.baseUrl() + CERTS_PATH, null, null);
    }

    @AfterEach
    void tearDown() {
        for (McpSyncClient client : clients) {
            try {
                client.closeGracefully();
            } catch (RuntimeException e) {
                // the server may already have refused the session
            }
        }
        settings.remove(SettingKeys.MCP_AUTH);
        settings.put(SettingKeys.SERVER, ServerSettings.defaults());
        jwksCache.invalidate();
    }

    private String base() {
        return "http://localhost:" + port;
    }

    private static String issuer() {
        return IDP_SERVER.baseUrl() + "/realms/test";
    }

    private String metadataUrl() {
        return base() + "/.well-known/oauth-protected-resource/mcp";
    }

    private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder json(String body) {
        return aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(body);
    }

    /** Stores {@code mcp.auth} directly (bypassing PUT validation, as an imported row would). */
    private void configure(String mode, String jwksUri, Map<String, String> scopeMap, List<String> algs) {
        settings.put(SettingKeys.MCP_AUTH, new McpAuthSettings(mode, issuer(), null, jwksUri, scopeMap, null, algs));
    }

    private TestIdp.Token token(String scope) {
        return IDP.token(issuer(), base() + "/mcp").expiresAt(clock.instant().plusSeconds(300)).scope(scope);
    }

    private static String bearer(String jwt) {
        return "Bearer " + jwt;
    }

    private HttpResponse<String> post(String authorization, String sessionId, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base() + "/mcp"))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (authorization != null) {
            request.header("Authorization", authorization);
        }
        if (sessionId != null) {
            request.header("Mcp-Session-Id", sessionId);
        }
        return HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getRest(String path, String authorization) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base() + path)).timeout(TIMEOUT).GET();
        if (authorization != null) {
            request.header("Authorization", authorization);
        }
        return HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    /** An initialized SDK client presenting {@code authorization}. */
    private McpSyncClient client(String authorization) {
        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder(base())
                .endpoint("/mcp")
                .connectTimeout(Duration.ofSeconds(5))
                .httpRequestCustomizer((builder, method, uri, body, context) ->
                        builder.header("Authorization", authorization))
                .build();
        McpSyncClient client = McpClient.sync(transport).requestTimeout(TIMEOUT).initializationTimeout(TIMEOUT).build();
        clients.add(client);
        client.initialize();
        return client;
    }

    private static List<String> toolNames(McpSyncClient client) {
        return client.listTools().tools().stream().map(McpSchema.Tool::name).toList();
    }

    private void assertChallenge401(HttpResponse<String> response, boolean tokenPresented) {
        assertThat(response.statusCode()).as(response.body()).isEqualTo(401);
        String challenge = response.headers().firstValue("WWW-Authenticate").orElse("");
        assertThat(challenge).startsWith("Bearer ")
                .contains("resource_metadata=\"" + metadataUrl() + "\"")
                .contains("scope=\"foresignal:read\"");
        if (tokenPresented) {
            assertThat(challenge).contains("error=\"invalid_token\"");
        } else {
            assertThat(challenge).doesNotContain("error=");
        }
    }

    @Test
    @DisplayName("a valid read token maps to read: read tools listed and callable, write tools hidden")
    void readTokenMapsToRead() {
        McpSyncClient client = client(bearer(token("openid foresignal:read").sign()));
        List<String> names = toolNames(client);
        assertThat(names).contains("query_records", "system_status").doesNotContain("create_watch", "delete_watch");
        McpSchema.CallToolResult result = client.callTool(new McpSchema.CallToolRequest("system_status", Map.of()));
        assertThat(result.isError()).as(result.content().toString()).isFalse();
    }

    @Test
    @DisplayName("a valid agent token (scp array, EdDSA) maps to agent: write tools listed")
    void agentTokenMapsToAgent() {
        settings.put(SettingKeys.MCP_AUTH, new McpAuthSettings("oauth", issuer(), null, IDP_SERVER.baseUrl() + CERTS_PATH, null,
                "scp", null));
        String jwt = IDP.token(issuer(), base() + "/mcp").expiresAt(clock.instant().plusSeconds(300))
                .scp("foresignal:read", "foresignal:agent").signWith(IDP.ed, JWSAlgorithm.EdDSA).sign();
        assertThat(toolNames(client(bearer(jwt)))).contains("create_watch", "query_records");
    }

    @Test
    @DisplayName("no token: 401 with resource_metadata and scope, even while open reads are on")
    void missingToken() throws Exception {
        assertChallenge401(post(null, null, INITIALIZE), false);
    }

    @Test
    @DisplayName("wrong aud, wrong iss, a refused algorithm and an expired token: 401 with resource_metadata")
    void invalidTokens() throws Exception {
        assertChallenge401(post(bearer(token("foresignal:read").audience("https://other.example/mcp").sign()), null,
                INITIALIZE), true);
        assertChallenge401(post(bearer(token("foresignal:read").issuer("https://evil.example/realms/test").sign()),
                null, INITIALIZE), true);
        assertChallenge401(post(bearer(token("foresignal:read").expiresAt(clock.instant().minusSeconds(120)).sign()),
                null, INITIALIZE), true);
        assertChallenge401(post(bearer(token("foresignal:read").hmac(new byte[32]).sign()), null, INITIALIZE), true);
        configure("oauth", IDP_SERVER.baseUrl() + CERTS_PATH, null, List.of("ES256"));
        assertChallenge401(post(bearer(token("foresignal:read").sign()), null, INITIALIZE), true);
    }

    @Test
    @DisplayName("a tool above the mapped scope: 403 with error=insufficient_scope, the scope needed and resource_metadata")
    void insufficientScope() throws Exception {
        String authorization = bearer(token("foresignal:read").sign());
        HttpResponse<String> init = post(authorization, null, INITIALIZE);
        assertThat(init.statusCode()).as(init.body()).isEqualTo(200);
        String session = init.headers().firstValue("Mcp-Session-Id").orElse(null);
        post(authorization, session, "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");

        HttpResponse<String> call = post(authorization, session, "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":"
                + "\"tools/call\",\"params\":{\"name\":\"create_watch\",\"arguments\":{}}}");
        assertThat(call.statusCode()).as(call.body()).isEqualTo(403);
        assertThat(call.headers().firstValue("WWW-Authenticate").orElse(""))
                .startsWith("Bearer error=\"insufficient_scope\"")
                .contains("scope=\"foresignal:agent\"")
                .contains("resource_metadata=\"" + metadataUrl() + "\"");
        assertThat(call.body()).contains("\"id\":2").contains("create_watch");
    }

    @Test
    @DisplayName("a valid token whose scopes map to nothing: 403 insufficient_scope naming foresignal:read")
    void noMappedScope() throws Exception {
        HttpResponse<String> response = post(bearer(token("openid profile").sign()), null, INITIALIZE);
        assertThat(response.statusCode()).isEqualTo(403);
        assertThat(response.headers().firstValue("WWW-Authenticate").orElse(""))
                .contains("error=\"insufficient_scope\"").contains("scope=\"foresignal:read\"");
        assertThat(response.body()).contains("INSUFFICIENT_SCOPE");
    }

    @Test
    @DisplayName("admin is never mappable: a stored admin mapping is ignored at runtime")
    void adminNeverMappable() throws Exception {
        configure("oauth", IDP_SERVER.baseUrl() + CERTS_PATH, Map.of("foresignal:admin", "admin", "foresignal:read", "read"), null);
        HttpResponse<String> adminOnly = post(bearer(token("foresignal:admin").sign()), null, INITIALIZE);
        assertThat(adminOnly.statusCode()).isEqualTo(403);
        assertThat(adminOnly.headers().firstValue("WWW-Authenticate").orElse("")).contains("insufficient_scope");

        McpSyncClient client = client(bearer(token("foresignal:admin foresignal:read").sign()));
        assertThat(toolNames(client)).doesNotContain("create_watch");
        HttpResponse<String> settingsRead = getRest("/v1/settings/mcp-auth",
                bearer(token("foresignal:admin").sign()));
        assertThat(settingsRead.statusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("an unknown kid triggers exactly one JWKS refresh; made-up kids cannot trigger more")
    void unknownKidRefreshesOnce() throws Exception {
        assertThat(post(bearer(token("foresignal:read").sign()), null, INITIALIZE).statusCode()).isEqualTo(200);
        IDP_SERVER.verify(1, getRequestedFor(urlEqualTo(CERTS_PATH)));

        IDP_SERVER.stubFor(get(urlEqualTo(CERTS_PATH)).willReturn(json(IDP.jwksWithRotated())));
        String rotated = token("foresignal:read").signWith(IDP.rotated, JWSAlgorithm.RS256).sign();
        assertThat(post(bearer(rotated), null, INITIALIZE).statusCode()).isEqualTo(200);
        IDP_SERVER.verify(2, getRequestedFor(urlEqualTo(CERTS_PATH)));

        assertChallenge401(post(bearer(token("foresignal:read").kid("made-up").sign()), null, INITIALIZE), true);
        assertChallenge401(post(bearer(token("foresignal:read").kid("made-up-2").sign()), null, INITIALIZE), true);
        IDP_SERVER.verify(2, getRequestedFor(urlEqualTo(CERTS_PATH)));
    }

    @Test
    @DisplayName("without jwksUri the keys are discovered (RFC 8414 absent, OpenID Connect present)")
    void discovery() throws Exception {
        IDP_SERVER.stubFor(get(urlEqualTo("/realms/test/.well-known/openid-configuration"))
                .willReturn(json("{\"issuer\":\"" + issuer() + "\",\"jwks_uri\":\"" + IDP_SERVER.baseUrl()
                        + CERTS_PATH + "\"}")));
        configure("oauth", null, null, null);
        assertThat(post(bearer(token("foresignal:read").sign()), null, INITIALIZE).statusCode()).isEqualTo(200);
        IDP_SERVER.verify(1, getRequestedFor(urlEqualTo("/.well-known/oauth-authorization-server/realms/test")));
        IDP_SERVER.verify(1, getRequestedFor(urlEqualTo("/realms/test/.well-known/openid-configuration")));
        IDP_SERVER.verify(1, getRequestedFor(urlEqualTo(CERTS_PATH)));
    }

    @Test
    @DisplayName("REST rejects identity-provider tokens with the plain challenge")
    void restRejectsOAuthTokens() throws Exception {
        String authorization = bearer(token("foresignal:agent").sign());
        HttpResponse<String> response = getRest("/v1/records", authorization);
        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.headers().firstValue("WWW-Authenticate").orElse(""))
                .isEqualTo("Bearer realm=\"foresignal\"");
        assertThat(getRest("/v1/triggers", authorization).statusCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("mode oauth refuses Foresignal tokens on /mcp; mode both accepts them; mode token ignores the IdP")
    void modes() throws Exception {
        String apiToken = TestTokens.bearer(TestTokens.create(tokens, "mcp-oauth-reader", Scope.READ));
        assertChallenge401(post(apiToken, null, INITIALIZE), true);

        configure("both", IDP_SERVER.baseUrl() + CERTS_PATH, null, null);
        assertThat(post(apiToken, null, INITIALIZE).statusCode()).isEqualTo(200);
        assertThat(post(bearer(token("foresignal:read").sign()), null, INITIALIZE).statusCode()).isEqualTo(200);
        assertChallenge401(post(null, null, INITIALIZE), false);

        configure("token", IDP_SERVER.baseUrl() + CERTS_PATH, null, null);
        assertThat(post(null, null, INITIALIZE).statusCode()).as("open reads apply again").isEqualTo(200);
        HttpResponse<String> jwt = post(bearer(token("foresignal:read").sign()), null, INITIALIZE);
        assertThat(jwt.statusCode()).isEqualTo(401);
        assertThat(jwt.headers().firstValue("WWW-Authenticate").orElse("")).isEqualTo("Bearer realm=\"foresignal\"");
    }

    @Test
    @DisplayName("the subject gets one principal across tokens; the token is never sent to the identity provider")
    void principalAndNoPassthrough() throws Exception {
        String jwt = token("foresignal:read").subject("principal-oauth").claim("preferred_username", "ada").sign();
        assertThat(post(bearer(jwt), null, INITIALIZE).statusCode()).isEqualTo(200);
        assertThat(post(bearer(token("foresignal:read").subject("principal-oauth").sign()), null, INITIALIZE)
                .statusCode()).isEqualTo(200);
        Integer principals = jdbc.queryForObject("SELECT count(*) FROM principal WHERE handle = ?", Integer.class,
                PrincipalService.externalHandle(issuer(), "principal-oauth"));
        assertThat(principals).isEqualTo(1);
        IDP_SERVER.verify(0, anyRequestedFor(anyUrl()).withHeader("Authorization", matching(".*")));
    }

    @Test
    @DisplayName("an MCP session opened with one token of a subject continues with a refreshed token of the same subject")
    void sessionSurvivesRefresh() throws Exception {
        HttpResponse<String> init = post(bearer(token("foresignal:read").sign()), null, INITIALIZE);
        String session = init.headers().firstValue("Mcp-Session-Id").orElse(null);
        clock.advance(Duration.ofSeconds(30));
        HttpResponse<String> next = post(bearer(token("foresignal:read").sign()), session,
                "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"ping\"}");
        assertThat(next.statusCode()).as(next.body()).isIn(200, 202);
        HttpResponse<String> other = post(bearer(token("foresignal:read").subject("user-2").sign()), session,
                "{\"jsonrpc\":\"2.0\",\"id\":4,\"method\":\"ping\"}");
        assertThat(other.statusCode()).isEqualTo(403);
    }
}
