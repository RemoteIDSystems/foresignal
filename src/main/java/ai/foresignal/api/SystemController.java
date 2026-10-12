package ai.foresignal.api;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.system.SystemInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code getSystem} and {@code rotateAdminToken} (prompt 019).
 *
 * <p>{@code GET /v1/system} is open: an anonymous caller gets the subset {@code version},
 * {@code apiVersion}, {@code startedAt}, {@code uptimeSeconds}, {@code status}, {@code features} and
 * {@code limits}; a caller presenting any valid token gets the full document. Until prompt 023 the
 * admin token is the only token, and 023 sets the minimum scope to {@code read}. An invalid token is
 * treated as anonymous rather than rejected, because the endpoint is open.
 */
@RestController
@Tag(name = "System")
public class SystemController {

    private static final String BEARER = "Bearer ";

    private final SystemInfoService system;
    private final AdminTokenService adminTokens;

    /** Creates the controller. */
    public SystemController(SystemInfoService system, AdminTokenService adminTokens) {
        this.system = system;
        this.adminTokens = adminTokens;
    }

    /** {@code getSystem}. */
    @GetMapping("/v1/system")
    @Operation(operationId = "getSystem", tags = "System")
    public Map<String, Object> getSystem(HttpServletRequest request) {
        return system.build(hasValidToken(request));
    }

    /** {@code rotateAdminToken}: the new token, shown once; the old one stops working immediately. */
    @PostMapping("/v1/system/admin-token/rotate")
    @Operation(operationId = "rotateAdminToken", tags = "System")
    public Map<String, String> rotateAdminToken() {
        return Map.of("token", adminTokens.rotate());
    }

    private boolean hasValidToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.regionMatches(true, 0, BEARER, 0, BEARER.length())) {
            return false;
        }
        return adminTokens.verify(header.substring(BEARER.length()).strip());
    }
}
