package ai.foresignal.mcp.oauth;

import ai.foresignal.security.AuthenticatedToken;
import ai.foresignal.security.McpOAuthGate;
import ai.foresignal.security.PrincipalService;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.SettingKeys;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * MCP remote authorization (prompt 141): the {@link McpOAuthGate} {@code TokenFilter} consults on {@code /mcp}.
 *
 * <p>A verified identity-provider token ({@link McpTokenValidator}) becomes a synthetic {@link AuthenticatedToken}:
 *
 * <ul>
 *   <li>id: a name-based UUID of the issuer and subject, so MCP sessions stay bound to the same person across token
 *       refreshes ({@code McpSessions});
 *   <li>name: {@code oauth:} plus the display name or subject (the audit actor is {@code token:oauth:…});
 *   <li>scope: the most privileged mapped scope ({@code read} or {@code agent}, never {@code admin});
 *   <li>no restrictions, no private grant;
 *   <li>principal: the subject's own principal ({@link PrincipalService#forExternalSubject}), so a disabled
 *       principal is refused like a disabled token owner.
 * </ul>
 *
 * <p>Foresignal runs no authorization server and no device grant; the token is verified locally and never
 * forwarded.
 */
@Service
public class McpOAuthService implements McpOAuthGate {

    /** The prefix of a synthetic token's name. */
    public static final String NAME_PREFIX = "oauth:";

    private final McpAuthSettingsService settings;
    private final McpTokenValidator validator;
    private final JwksCache jwks;
    private final PrincipalService principals;
    private final Cache<String, String> principalIds = Caffeine.newBuilder().maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(10)).build();

    /** Creates the service. */
    public McpOAuthService(McpAuthSettingsService settings, McpTokenValidator validator, JwksCache jwks,
            PrincipalService principals) {
        this.settings = settings;
        this.validator = validator;
        this.jwks = jwks;
        this.principals = principals;
    }

    /** Drops the cached keys when {@code mcp.auth} changes. */
    @EventListener
    public void onSettingChanged(AppSettingChangedEvent event) {
        if (SettingKeys.MCP_AUTH.equals(event.key())) {
            jwks.invalidate();
        }
    }

    @Override
    public boolean active() {
        return settings.get().authMode().oauth();
    }

    @Override
    public boolean acceptsApiTokens() {
        return settings.get().authMode().apiTokens();
    }

    @Override
    public Outcome authenticate(String bearer) {
        McpAuthSettings value = settings.get();
        if (!value.authMode().oauth()) {
            return new Rejected("identity-provider tokens are not accepted (mcp.auth.mode is token)");
        }
        McpTokenValidator.Verified verified;
        try {
            verified = validator.verify(bearer, value, settings.effectiveAudience(value));
        } catch (McpTokenValidator.InvalidTokenException e) {
            return new Rejected(e.getMessage());
        }
        Optional<Scope> scope = McpScopeMapper.map(verified.scopes(), value.scopeMap());
        if (scope.isEmpty()) {
            return new NoScope("The access token carries no scope that maps to a Foresignal scope; request "
                    + String.join(" or ", McpScopeMapper.scopesFor(Scope.READ, value.scopeMap())) + ".");
        }
        String principalId = principalId(verified);
        if (principals.isDisabled(principalId)) {
            return new Rejected("the principal of this subject is disabled");
        }
        AuthenticatedToken token = new AuthenticatedToken(tokenId(verified.issuer(), verified.subject()),
                tokenName(verified), scope.get(), TokenRestrictions.NONE, null, principalId, false, false, List.of());
        return new Accepted(token, verified.issuer(), verified.subject(), verified.scopes());
    }

    @Override
    public String challenge(HttpServletRequest request, boolean tokenPresented) {
        McpAuthSettings value = settings.get();
        StringBuilder header = new StringBuilder("Bearer ");
        if (tokenPresented) {
            header.append("error=\"invalid_token\", ");
        }
        header.append("resource_metadata=\"").append(quoted(settings.metadataUrl(request))).append("\", scope=\"")
                .append(quoted(String.join(" ", McpScopeMapper.scopesFor(Scope.READ, value.scopeMap())))).append('"');
        return header.toString();
    }

    @Override
    public String insufficientScope(HttpServletRequest request, Scope required) {
        McpAuthSettings value = settings.get();
        return "Bearer error=\"insufficient_scope\", scope=\""
                + quoted(String.join(" ", McpScopeMapper.scopesFor(required, value.scopeMap())))
                + "\", resource_metadata=\"" + quoted(settings.metadataUrl(request)) + "\"";
    }

    /** The synthetic token id of a subject: a name-based UUID of the issuer and subject. */
    public static UUID tokenId(String issuer, String subject) {
        return UUID.nameUUIDFromBytes(("mcp-oauth\n" + issuer + "\n" + subject).getBytes(StandardCharsets.UTF_8));
    }

    private static String tokenName(McpTokenValidator.Verified verified) {
        String name = NAME_PREFIX + (verified.displayName() == null ? verified.subject() : verified.displayName());
        return name.length() > 200 ? name.substring(0, 200) : name;
    }

    private String principalId(McpTokenValidator.Verified verified) {
        String key = verified.issuer() + "\n" + verified.subject();
        return principalIds.get(key, ignored -> {
            try {
                return principals.forExternalSubject(verified.issuer(), verified.subject(), verified.displayName());
            } catch (DataIntegrityViolationException raced) {
                // Two first requests of one subject at once: the other created it.
                return principals.forExternalSubject(verified.issuer(), verified.subject(), verified.displayName());
            }
        });
    }

    /** {@code value} safe inside a quoted-string: no quotes, backslashes or control characters. */
    static String quoted(String value) {
        StringBuilder out = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            if (c != '"' && c != '\\' && c >= 0x20 && c != 0x7f) {
                out.append(c);
            }
        }
        return out.toString();
    }
}
