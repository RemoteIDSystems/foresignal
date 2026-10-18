package ai.foresignal.mcp.oauth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * OAuth 2.0 Protected Resource Metadata (RFC 9728) for {@code /mcp} (prompt 141, OpenAPI schema
 * {@code ProtectedResourceMetadata}); member names follow the RFC.
 *
 * @param resource the canonical resource URI (the audience tokens must name)
 * @param authorizationServers the identity provider's issuer
 * @param scopesSupported the identity-provider scopes that map to a Foresignal scope
 * @param bearerMethodsSupported always {@code ["header"]}
 * @param resourceName always {@code Foresignal}
 * @param resourceDocumentation where a person reads about the API
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record ProtectedResourceMetadata(
        @JsonProperty("resource") String resource,
        @JsonProperty("authorization_servers") List<String> authorizationServers,
        @JsonProperty("scopes_supported") List<String> scopesSupported,
        @JsonProperty("bearer_methods_supported") List<String> bearerMethodsSupported,
        @JsonProperty("resource_name") String resourceName,
        @JsonProperty("resource_documentation") String resourceDocumentation) {

    /** The {@code resource_name}. */
    public static final String RESOURCE_NAME = "Foresignal";

    /** Copies the lists. */
    public ProtectedResourceMetadata {
        authorizationServers = authorizationServers == null ? List.of() : List.copyOf(authorizationServers);
        scopesSupported = scopesSupported == null ? List.of() : List.copyOf(scopesSupported);
        bearerMethodsSupported = bearerMethodsSupported == null ? List.of("header") : List.copyOf(bearerMethodsSupported);
    }
}
