package ai.foresignal.mcp.oauth;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code getProtectedResourceMetadata} and {@code getMcpProtectedResourceMetadata} (prompt 141): the RFC 9728
 * metadata an MCP client reads, from the {@code resource_metadata} of a {@code 401} on {@code /mcp}, to find the
 * identity provider. Open (under {@code /.well-known/**} in {@code AccessPolicy}); {@code 404 MCP_OAUTH_DISABLED}
 * while {@code mcp.auth.mode} is {@code token}.
 */
@RestController
@Tag(name = "MCP")
public class ProtectedResourceMetadataController {

    private final McpAuthSettingsService settings;

    /** Creates the controller. */
    public ProtectedResourceMetadataController(McpAuthSettingsService settings) {
        this.settings = settings;
    }

    /** {@code getProtectedResourceMetadata}: the same document as the {@code /mcp} path variant. */
    @GetMapping(value = "/.well-known/oauth-protected-resource", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getProtectedResourceMetadata", tags = "MCP")
    public ResponseEntity<ProtectedResourceMetadata> getProtectedResourceMetadata(HttpServletRequest request) {
        return document(request);
    }

    /** {@code getMcpProtectedResourceMetadata}: the path-suffixed location of the {@code /mcp} resource. */
    @GetMapping(value = "/.well-known/oauth-protected-resource/mcp", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getMcpProtectedResourceMetadata", tags = "MCP")
    public ResponseEntity<ProtectedResourceMetadata> getMcpProtectedResourceMetadata(HttpServletRequest request) {
        return document(request);
    }

    /** The metadata document, or {@code 404} while MCP remote authorization is off. */
    ResponseEntity<ProtectedResourceMetadata> document(HttpServletRequest request) {
        McpAuthSettings value = settings.get();
        if (!value.authMode().oauth()) {
            throw new NotFoundException(ErrorCodes.MCP_OAUTH_DISABLED, "MCP remote authorization is off"
                    + " (mcp.auth.mode is token); /mcp accepts Foresignal tokens only.");
        }
        ProtectedResourceMetadata body = new ProtectedResourceMetadata(
                settings.resource(value, request),
                value.issuer() == null ? List.of() : List.of(value.issuer()),
                McpScopeMapper.supported(value.scopeMap()),
                List.of("header"),
                ProtectedResourceMetadata.RESOURCE_NAME,
                settings.baseUrl(request) + "/docs");
        return ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(body);
    }
}
