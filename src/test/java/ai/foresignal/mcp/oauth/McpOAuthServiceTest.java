package ai.foresignal.mcp.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.security.McpOAuthGate;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.ServerSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.mcp.TestIdp;
import ai.foresignal.testsupport.net.TestOutbound;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockHttpServletRequest;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link McpOAuthService} (prompt 141): the synthetic token of an accepted subject, the mode switches, the
 * {@code NoScope} outcome (admin never mappable at runtime), a disabled principal, and the two
 * {@code WWW-Authenticate} challenges.
 */
class McpOAuthServiceTest {

    static final Instant T = Instant.parse("2026-10-18T12:00:00Z");
    static final String ISSUER = "https://idp.example/realms/home";
    static final String JWKS = "https://idp.example/certs";
    static final String BASE = "https://fs.example";
    static final TestIdp IDP = TestIdp.create();

    final AtomicReference<McpAuthSettings> stored = new AtomicReference<>();
    PrincipalService principals;
    McpOAuthService service;
    JwksCache cache;
    FakeIdpFetcher fetcher;

    @BeforeEach
    void setUp() {
        AppSettingService settings = mock(AppSettingService.class);
        when(settings.getOrDefault(eq(SettingKeys.SERVER), eq(ServerSettings.class), any()))
                .thenReturn(new ServerSettings(BASE));
        when(settings.getOrDefault(eq(SettingKeys.MCP_AUTH), eq(McpAuthSettings.class), any()))
                .thenAnswer(i -> stored.get());
        stored.set(new McpAuthSettings("oauth", ISSUER, null, JWKS, null, null, null));
        MutableClock clock = new MutableClock(T);
        fetcher = new FakeIdpFetcher().serve(JWKS, IDP.jwks());
        cache = new JwksCache(fetcher, JsonMapper.builder().build(), clock);
        principals = mock(PrincipalService.class);
        when(principals.forExternalSubject(anyString(), anyString(), any())).thenReturn("01JPRINCIPAL0000000000000A");
        McpAuthSettingsService settingsService = new McpAuthSettingsService(settings, TestOutbound.offlinePolicy());
        service = new McpOAuthService(settingsService, new McpTokenValidator(cache, clock), cache, principals);
    }

    String token(String scope) {
        return IDP.token(ISSUER, BASE + "/mcp").expiresAt(T.plusSeconds(300)).scope(scope)
                .claim("name", "Ada Lovelace").sign();
    }

    @Test
    @DisplayName("an accepted token becomes a synthetic token: stable id, oauth: name, mapped scope, the principal")
    void accepted() {
        McpOAuthGate.Outcome outcome = service.authenticate(token("openid foresignal:agent"));
        assertThat(outcome).isInstanceOfSatisfying(McpOAuthGate.Accepted.class, a -> {
            assertThat(a.token().scope()).isEqualTo(Scope.AGENT);
            assertThat(a.token().name()).isEqualTo("oauth:Ada Lovelace");
            assertThat(a.token().id()).isEqualTo(McpOAuthService.tokenId(ISSUER, "user-1"));
            assertThat(a.token().principalId()).isEqualTo("01JPRINCIPAL0000000000000A");
            assertThat(a.token().restrictions().hasEndpointRestrictions()).isFalse();
            assertThat(a.token().privateAccess()).isFalse();
            assertThat(a.token().bootstrap()).isFalse();
            assertThat(a.issuer()).isEqualTo(ISSUER);
            assertThat(a.subject()).isEqualTo("user-1");
            assertThat(a.idpScopes()).containsExactlyInAnyOrder("openid", "foresignal:agent");
        });
        service.authenticate(token("foresignal:read"));
        verify(principals, times(1)).forExternalSubject(ISSUER, "user-1", "Ada Lovelace");
        assertThat(McpOAuthService.tokenId(ISSUER, "user-1")).isNotEqualTo(McpOAuthService.tokenId(ISSUER, "user-2"));
    }

    @Test
    @DisplayName("a raced first principal creation retries once")
    void racedPrincipal() {
        when(principals.forExternalSubject(anyString(), anyString(), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate handle"))
                .thenReturn("01JPRINCIPAL0000000000000B");
        assertThat(service.authenticate(token("foresignal:read"))).isInstanceOfSatisfying(McpOAuthGate.Accepted.class,
                a -> assertThat(a.token().principalId()).isEqualTo("01JPRINCIPAL0000000000000B"));
    }

    @Test
    @DisplayName("no mapped scope is NoScope; admin mapped in a stored row is ignored")
    void noScope() {
        assertThat(service.authenticate(token("openid profile"))).isInstanceOf(McpOAuthGate.NoScope.class);
        stored.set(new McpAuthSettings("oauth", ISSUER, null, JWKS, Map.of("foresignal:admin", "admin"), null, null));
        assertThat(service.authenticate(token("foresignal:admin")))
                .isInstanceOfSatisfying(McpOAuthGate.NoScope.class,
                        n -> assertThat(n.reason()).contains("foresignal:read"));
    }

    @Test
    @DisplayName("an invalid token is Rejected with the reason; mode token rejects every token")
    void rejected() {
        String wrongAudience = IDP.token(ISSUER, "https://other.example/mcp").expiresAt(T.plusSeconds(300))
                .scope("foresignal:read").sign();
        assertThat(service.authenticate(wrongAudience)).isInstanceOfSatisfying(McpOAuthGate.Rejected.class,
                r -> assertThat(r.reason()).contains("not for this resource").doesNotContain(wrongAudience));
        stored.set(McpAuthSettings.defaults());
        assertThat(service.authenticate(token("foresignal:read"))).isInstanceOf(McpOAuthGate.Rejected.class);
        assertThat(service.active()).isFalse();
        assertThat(service.acceptsApiTokens()).isTrue();
    }

    @Test
    @DisplayName("a disabled principal is Rejected")
    void disabledPrincipal() {
        when(principals.isDisabled("01JPRINCIPAL0000000000000A")).thenReturn(true);
        assertThat(service.authenticate(token("foresignal:read"))).isInstanceOfSatisfying(McpOAuthGate.Rejected.class,
                r -> assertThat(r.reason()).contains("disabled"));
    }

    @Test
    @DisplayName("modes: oauth refuses API tokens, both accepts them")
    void modes() {
        assertThat(service.active()).isTrue();
        assertThat(service.acceptsApiTokens()).isFalse();
        stored.set(new McpAuthSettings("both", ISSUER, null, JWKS, null, null, null));
        assertThat(service.active()).isTrue();
        assertThat(service.acceptsApiTokens()).isTrue();
    }

    @Test
    @DisplayName("the 401 challenge names resource_metadata and scope; invalid_token only when a token was presented")
    void challenge() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mcp");
        assertThat(service.challenge(request, false)).isEqualTo("Bearer resource_metadata=\"" + BASE
                + "/.well-known/oauth-protected-resource/mcp\", scope=\"foresignal:read\"");
        assertThat(service.challenge(request, true)).startsWith("Bearer error=\"invalid_token\", resource_metadata=");
    }

    @Test
    @DisplayName("the 403 challenge names insufficient_scope, the identity-provider scope needed and resource_metadata")
    void insufficientScope() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mcp");
        assertThat(service.insufficientScope(request, Scope.AGENT)).isEqualTo("Bearer error=\"insufficient_scope\","
                + " scope=\"foresignal:agent\", resource_metadata=\"" + BASE
                + "/.well-known/oauth-protected-resource/mcp\"");
        stored.set(new McpAuthSettings("oauth", ISSUER, null, JWKS, Map.of("mcp.write", "agent", "mcp.all", "agent"),
                null, null));
        assertThat(service.insufficientScope(request, Scope.AGENT)).contains("scope=\"mcp.all mcp.write\"");
    }

    @Test
    @DisplayName("quoted strips quotes, backslashes and control characters")
    void quoted() {
        assertThat(McpOAuthService.quoted("a\"b\\c\nd\u007fe")).isEqualTo("abcde");
    }

    @Test
    @DisplayName("a change of mcp.auth drops the cached keys")
    void settingChange() throws Exception {
        service.authenticate(token("foresignal:read"));
        service.onSettingChanged(new AppSettingChangedEvent(SettingKeys.SERVER));
        service.authenticate(token("foresignal:read"));
        assertThat(fetcher.count(JWKS)).isEqualTo(1);
        service.onSettingChanged(new AppSettingChangedEvent(SettingKeys.MCP_AUTH));
        service.authenticate(token("foresignal:read"));
        assertThat(fetcher.count(JWKS)).isEqualTo(2);
    }
}
