package ai.foresignal.mcp.oauth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Settings {@code mcp.auth} (prompt 141, OpenAPI schema {@code McpAuthSettings}): remote MCP authorization delegated to
 * the operator's identity provider. Foresignal runs no authorization server and no device grant; it only verifies the
 * access tokens the identity provider issues.
 *
 * @param mode {@code token} (default), {@code oauth} or {@code both}
 * @param issuer the identity provider's issuer identifier, compared exactly with {@code iss}
 * @param audience the canonical resource URI tokens must name in {@code aud} (RFC 8707); null for
 *     {@code {server.publicBaseUrl}/mcp}
 * @param jwksUri the JWKS location; null to discover it from the issuer (RFC 8414, then OpenID Connect discovery)
 * @param scopeMap identity-provider scope to Foresignal scope ({@code read} or {@code agent}; {@code admin} is never
 *     mappable and ignored if it is ever stored)
 * @param scopeClaim the claim carrying the scopes: {@code scope} (space-separated, RFC 9068) or {@code scp}
 * @param allowedAlgs the JWS algorithms accepted: {@code RS256}, {@code ES256}, {@code EdDSA}, {@code Ed25519}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record McpAuthSettings(String mode, String issuer, String audience, String jwksUri,
        Map<String, String> scopeMap, String scopeClaim, List<String> allowedAlgs) {

    /** The default scope map: {@code foresignal:read} to {@code read}, {@code foresignal:agent} to {@code agent}. */
    public static final Map<String, String> DEFAULT_SCOPE_MAP = defaultScopeMap();

    /** The supported (and default) algorithms. */
    public static final List<String> SUPPORTED_ALGS = List.of("RS256", "ES256", "EdDSA", "Ed25519");

    /** The supported scope claims. */
    public static final List<String> SCOPE_CLAIMS = List.of("scope", "scp");

    /** Fills missing members with the defaults. */
    public McpAuthSettings {
        mode = mode == null ? McpAuthMode.TOKEN.wireValue() : mode;
        scopeMap = scopeMap == null ? DEFAULT_SCOPE_MAP : java.util.Collections.unmodifiableMap(
                new LinkedHashMap<>(scopeMap));
        scopeClaim = scopeClaim == null ? "scope" : scopeClaim;
        allowedAlgs = allowedAlgs == null || allowedAlgs.isEmpty() ? SUPPORTED_ALGS : List.copyOf(allowedAlgs);
    }

    /** The shipped value: {@code mode: token}, nothing configured. */
    public static McpAuthSettings defaults() {
        return new McpAuthSettings(McpAuthMode.TOKEN.wireValue(), null, null, null, null, null, null);
    }

    /** The mode; an unknown stored value counts as {@code token}. */
    public McpAuthMode authMode() {
        return McpAuthMode.fromWireValue(mode).orElse(McpAuthMode.TOKEN);
    }

    private static Map<String, String> defaultScopeMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("foresignal:read", "read");
        map.put("foresignal:agent", "agent");
        return java.util.Collections.unmodifiableMap(map);
    }
}
