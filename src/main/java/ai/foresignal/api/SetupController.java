package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.security.AdminTokenService;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.setup.FirstRunService;
import ai.foresignal.setup.SetupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * First-run setup (prompt R2-027): {@code getFirstRunStatus}, open, and {@code runSetup}, which only the bootstrap
 * admin token may call ({@code 403} for any other caller, an admin API token or a session included) and only while no
 * user exists ({@code 409 ALREADY_SETUP} after).
 */
@RestController
@Tag(name = "System")
public class SetupController {

    private final FirstRunService firstRun;
    private final SetupService setup;
    private final AdminTokenService adminTokens;

    /**
     * Creates the controller.
     *
     * @param firstRun the first-run status
     * @param setup the setup
     * @param adminTokens identifies the bootstrap admin token
     */
    public SetupController(FirstRunService firstRun, SetupService setup, AdminTokenService adminTokens) {
        this.firstRun = firstRun;
        this.setup = setup;
        this.adminTokens = adminTokens;
    }

    /** {@code getFirstRunStatus}: whether setup is still needed. */
    @GetMapping("/v1/system/first-run")
    @Operation(operationId = "getFirstRunStatus", tags = "System")
    public Map<String, Object> getFirstRunStatus() {
        return firstRun.status();
    }

    /** {@code runSetup}: creates the first admin and seeds the optional settings. */
    @PostMapping("/v1/setup")
    @Operation(operationId = "runSetup", tags = "System")
    public Map<String, Object> runSetup(@RequestBody JsonNode body, HttpServletRequest request) {
        Optional<UUID> caller = CurrentToken.of(request).filter(t -> !t.viaTicket()).map(CurrentToken::id);
        Optional<UUID> bootstrap = adminTokens.bootstrapTokenId();
        if (caller.isEmpty() || bootstrap.isEmpty() || !caller.get().equals(bootstrap.get())) {
            throw new ForesignalException(HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN,
                    "Setup is run with the bootstrap admin token printed at first start.");
        }
        return setup.run(body, request.getHeader(HttpHeaders.USER_AGENT), request.getRemoteAddr());
    }
}
