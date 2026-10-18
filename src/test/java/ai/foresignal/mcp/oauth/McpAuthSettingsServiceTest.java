package ai.foresignal.mcp.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.net.OutboundUrlPolicy;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link McpAuthSettingsService} (prompt 141): defaults, every validation rule of {@code PUT /v1/settings/mcp-auth}
 * (admin never mappable, outbound URL policy, required members per mode), the effective audience and the metadata
 * URLs.
 */
class McpAuthSettingsServiceTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    AppSettingService settings;
    McpAuthSettingsService service;
    ServerSettings server = ServerSettings.defaults();

    @BeforeEach
    void setUp() {
        settings = mock(AppSettingService.class);
        when(settings.getOrDefault(eq(SettingKeys.SERVER), eq(ServerSettings.class), any())).thenAnswer(i -> server);
        when(settings.getOrDefault(eq(SettingKeys.MCP_AUTH), eq(McpAuthSettings.class), any()))
                .thenReturn(McpAuthSettings.defaults());
        // idp.example is public, kc.lan private, nowhere.example does not resolve, metadata.example is link-local.
        OutboundUrlPolicy policy = OutboundUrlPolicy.withoutOverrides(host -> switch (host) {
            case "idp.example" -> new InetAddress[] {InetAddress.getByName("93.184.216.34")};
            case "kc.lan" -> new InetAddress[] {InetAddress.getByName("192.168.1.20")};
            case "metadata.example" -> new InetAddress[] {InetAddress.getByName("169.254.169.254")};
            case "localhost" -> new InetAddress[] {InetAddress.getByName("127.0.0.1")};
            default -> throw new UnknownHostException(host);
        });
        service = new McpAuthSettingsService(settings, policy);
    }

    static JsonNode body(String json) {
        return JSON.readTree(json);
    }

    List<FieldError> errors(String json) {
        try {
            service.parse(body(json));
        } catch (ValidationException e) {
            return e.getErrors();
        }
        throw new AssertionError("expected a validation error for " + json);
    }

    static List<String> fields(List<FieldError> errors) {
        return errors.stream().map(FieldError::field).toList();
    }

    @Test
    @DisplayName("the defaults: mode token, the default scope map, every supported algorithm, the scope claim")
    void defaults() {
        McpAuthSettings value = service.get();
        assertThat(value.authMode()).isEqualTo(McpAuthMode.TOKEN);
        assertThat(value.scopeMap()).containsEntry("foresignal:read", "read").containsEntry("foresignal:agent", "agent");
        assertThat(value.allowedAlgs()).containsExactly("RS256", "ES256", "EdDSA", "Ed25519");
        assertThat(value.scopeClaim()).isEqualTo("scope");
        assertThat(new McpAuthSettings("weird", null, null, null, null, null, null).authMode())
                .isEqualTo(McpAuthMode.TOKEN);
    }

    @Test
    @DisplayName("a full oauth body is accepted and stored")
    void validBody() {
        McpAuthSettings value = service.update(body("""
                {"mode":"oauth","issuer":"https://idp.example/realms/home","audience":"https://fs.example/mcp",
                 "jwksUri":"https://idp.example/realms/home/certs","scopeMap":{"mcp:read":"read","mcp:agent":"AGENT"},
                 "scopeClaim":"scp","allowedAlgs":["ES256","ES256","EdDSA"]}"""));
        assertThat(value.authMode()).isEqualTo(McpAuthMode.OAUTH);
        assertThat(value.scopeMap()).containsEntry("mcp:agent", "agent");
        assertThat(value.allowedAlgs()).containsExactly("ES256", "EdDSA");
        assertThat(value.scopeClaim()).isEqualTo("scp");
        verify(settings).put(SettingKeys.MCP_AUTH, value);
    }

    @Test
    @DisplayName("admin is never mappable: 400 naming the entry")
    void adminRefused() {
        List<FieldError> errors = errors("""
                {"mode":"oauth","issuer":"https://idp.example","audience":"https://fs.example/mcp",
                 "scopeMap":{"foresignal:admin":"admin","x":"Admin"}}""");
        assertThat(fields(errors)).containsExactlyInAnyOrder("scopeMap.foresignal:admin", "scopeMap.x");
        assertThat(errors.getFirst().message()).contains("admin is never mappable");
        verify(settings, never()).put(any(), any());
    }

    @Test
    @DisplayName("mode is required and must be token, oauth or both")
    void mode() {
        assertThat(fields(errors("{}"))).contains("mode");
        assertThat(fields(errors("{\"mode\":\"sometimes\"}"))).contains("mode");
        assertThat(fields(errors("{\"mode\":3}"))).contains("mode");
        assertThat(service.parse(body("{\"mode\":\"token\"}")).authMode()).isEqualTo(McpAuthMode.TOKEN);
    }

    @Test
    @DisplayName("oauth and both need an issuer, and an audience while server.publicBaseUrl is unset")
    void requiredMembers() {
        assertThat(fields(errors("{\"mode\":\"oauth\"}"))).containsExactlyInAnyOrder("issuer", "audience");
        assertThat(fields(errors("{\"mode\":\"both\",\"issuer\":\"https://idp.example\"}"))).containsExactly("audience");
        server = new ServerSettings("https://fs.example");
        McpAuthSettings value = service.parse(body("{\"mode\":\"both\",\"issuer\":\"https://idp.example\"}"));
        assertThat(value.audience()).isNull();
        assertThat(service.effectiveAudience(value)).isEqualTo("https://fs.example/mcp");
    }

    @Test
    @DisplayName("issuer and jwksUri pass the outbound URL policy: URL_NOT_ALLOWED when refused")
    void outboundPolicy() {
        assertThatThrownBy(() -> service.parse(body("""
                {"mode":"oauth","issuer":"https://metadata.example","audience":"https://fs.example/mcp"}""")))
                .isInstanceOfSatisfying(ValidationException.class, e -> {
                    assertThat(e.getCode()).isEqualTo(ErrorCodes.URL_NOT_ALLOWED);
                    assertThat(fields(e.getErrors())).containsExactly("issuer");
                });
        assertThatThrownBy(() -> service.parse(body("""
                {"mode":"oauth","issuer":"https://idp.example","jwksUri":"http://idp.example/certs",
                 "audience":"https://fs.example/mcp"}""")))
                .isInstanceOfSatisfying(ValidationException.class, e -> {
                    assertThat(e.getCode()).isEqualTo(ErrorCodes.URL_NOT_ALLOWED);
                    assertThat(e.getErrors().getFirst().message()).contains("plain http");
                });
    }

    @Test
    @DisplayName("plain http is fine for a LAN identity provider; a host that does not resolve yet is accepted")
    void lanAndUnresolved() {
        assertThat(service.parse(body("""
                {"mode":"oauth","issuer":"http://kc.lan:8080/realms/home","audience":"https://fs.example/mcp"}"""))
                .issuer()).isEqualTo("http://kc.lan:8080/realms/home");
        assertThat(service.parse(body("""
                {"mode":"oauth","issuer":"https://nowhere.example","audience":"https://fs.example/mcp"}"""))
                .issuer()).isEqualTo("https://nowhere.example");
    }

    @Test
    @DisplayName("URLs must be absolute http(s) without query or fragment")
    void urlShapes() {
        assertThat(fields(errors("""
                {"mode":"oauth","issuer":"ftp://idp.example","audience":"mcp","jwksUri":"https://idp.example/k?x=1"}
                """))).containsExactlyInAnyOrder("issuer", "audience", "jwksUri");
        assertThat(fields(errors("{\"mode\":\"token\",\"audience\":\"https://fs.example/mcp#x\"}")))
                .containsExactly("audience");
        assertThat(fields(errors("{\"mode\":\"token\",\"issuer\":42}"))).containsExactly("issuer");
        assertThat(fields(errors("{\"mode\":\"token\",\"issuer\":\"https://idp.example/" + "a".repeat(2050) + "\"}")))
                .containsExactly("issuer");
        assertThat(service.parse(body("{\"mode\":\"token\",\"issuer\":\"  \"}")).issuer()).isNull();
    }

    @Test
    @DisplayName("scopeMap, scopeClaim, allowedAlgs and unknown members are validated")
    void otherMembers() {
        assertThat(fields(errors("{\"mode\":\"token\",\"scopeMap\":[1]}"))).containsExactly("scopeMap");
        assertThat(fields(errors("{\"mode\":\"token\",\"scopeMap\":{\"has space\":\"read\",\"ok\":\"write\",\"n\":1}}")))
                .containsExactlyInAnyOrder("scopeMap.has space", "scopeMap.ok", "scopeMap.n");
        StringBuilder many = new StringBuilder("{\"mode\":\"token\",\"scopeMap\":{");
        for (int i = 0; i < 51; i++) {
            many.append(i == 0 ? "" : ",").append("\"s").append(i).append("\":\"read\"");
        }
        assertThat(fields(errors(many.append("}}").toString()))).containsExactly("scopeMap");
        assertThat(fields(errors("{\"mode\":\"token\",\"scopeClaim\":\"roles\"}"))).containsExactly("scopeClaim");
        assertThat(fields(errors("{\"mode\":\"token\",\"allowedAlgs\":[]}"))).containsExactly("allowedAlgs");
        assertThat(fields(errors("{\"mode\":\"token\",\"allowedAlgs\":\"RS256\"}"))).containsExactly("allowedAlgs");
        assertThat(fields(errors("{\"mode\":\"token\",\"allowedAlgs\":[\"HS256\",\"none\"]}")))
                .containsExactly("allowedAlgs[0]", "allowedAlgs[1]");
        assertThat(fields(errors("{\"mode\":\"token\",\"extra\":true}"))).containsExactly("extra");
        assertThatThrownBy(() -> service.parse(body("[]"))).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.parse(null)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("the base URL is server.publicBaseUrl, else the request's own origin; metadata and resource follow")
    void urls() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mcp");
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        assertThat(service.baseUrl(request)).isEqualTo("http://localhost:8080");
        assertThat(service.metadataUrl(request)).isEqualTo("http://localhost:8080/.well-known/oauth-protected-resource/mcp");
        McpAuthSettings value = new McpAuthSettings("oauth", "https://idp.example", null, null, null, null, null);
        assertThat(service.effectiveAudience(value)).isNull();
        assertThat(service.resource(value, request)).isEqualTo("http://localhost:8080/mcp");

        request.setScheme("https");
        request.setServerPort(443);
        assertThat(service.baseUrl(request)).isEqualTo("https://localhost");

        server = new ServerSettings("https://fs.example/");
        assertThat(service.metadataUrl(request)).isEqualTo("https://fs.example/.well-known/oauth-protected-resource/mcp");
        assertThat(service.resource(value, request)).isEqualTo("https://fs.example/mcp");
        McpAuthSettings explicit = new McpAuthSettings("oauth", "https://idp.example", " https://api.fs.example/mcp ",
                null, null, null, null);
        assertThat(service.effectiveAudience(explicit)).isEqualTo("https://api.fs.example/mcp");
    }
}
