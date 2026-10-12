package ai.foresignal.api;

import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.Scope;
import ai.foresignal.system.SystemInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code getSystem} and {@code rotateAdminToken} (prompt 019).
 *
 * <p>{@code GET /v1/system} is open: an anonymous caller gets the subset {@code version},
 * {@code apiVersion}, {@code startedAt}, {@code uptimeSeconds}, {@code status}, {@code features} and
 * {@code limits}; a caller presenting a valid token of at least the {@code read} scope gets the full
 * document (prompt 023). The token filter resolves the token on this open path without rejecting an
 * invalid one, which is treated as anonymous.
 */
@RestController
@Tag(name = "System")
public class SystemController {

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
        boolean full = CurrentToken.of(request).map(t -> t.scope().includes(Scope.READ)).orElse(false);
        return system.build(full);
    }

    /** {@code rotateAdminToken}: the new token, shown once; the old one stops working immediately. */
    @PostMapping("/v1/system/admin-token/rotate")
    @Operation(operationId = "rotateAdminToken", tags = "System")
    public Map<String, String> rotateAdminToken() {
        return Map.of("token", adminTokens.rotate());
    }
}
