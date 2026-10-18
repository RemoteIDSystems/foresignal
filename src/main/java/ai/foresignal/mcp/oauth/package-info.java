/**
 * MCP remote authorization (prompt 141): {@code /mcp} accepts access tokens from the operator's own identity
 * provider (Keycloak, Authentik, Pocket ID, Auth0 and others), following the MCP specification 2025-11-25
 * "Authorization".
 *
 * <ul>
 *   <li>{@link ai.foresignal.mcp.oauth.ProtectedResourceMetadataController}: RFC 9728 protected resource metadata
 *       at {@code /.well-known/oauth-protected-resource} and {@code /.well-known/oauth-protected-resource/mcp}.
 *   <li>{@link ai.foresignal.mcp.oauth.McpTokenValidator} and {@link ai.foresignal.mcp.oauth.JwksCache}: JWT
 *       verification with Nimbus JOSE+JWT against the identity provider's JWKS (issuer, RFC 8707 audience,
 *       lifetime with 60 s of skew, algorithm allowlist).
 *   <li>{@link ai.foresignal.mcp.oauth.McpScopeMapper}: identity-provider scopes to {@code read} or {@code agent};
 *       {@code admin} is never mappable.
 *   <li>{@link ai.foresignal.mcp.oauth.McpOAuthService}: the {@code McpOAuthGate} the token filter consults, and the
 *       {@code WWW-Authenticate} challenges; {@link ai.foresignal.mcp.oauth.McpStepUpFilter}: {@code 403
 *       insufficient_scope} for tool calls above the token's scope.
 *   <li>{@link ai.foresignal.mcp.oauth.McpAuthSettingsService}: settings {@code mcp.auth}.
 * </ul>
 *
 * <p>Foresignal runs no authorization server and no device grant, and never forwards a presented token. REST keeps
 * Foresignal tokens.
 */
package ai.foresignal.mcp.oauth;
