package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.mfa.MfaService;
import ai.foresignal.security.ratelimit.ClientRateLimiter;
import ai.foresignal.security.ratelimit.RateClass;
import ai.foresignal.security.user.AuthenticatedSession;
import ai.foresignal.security.user.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The {@code MFA} operations (prompt R2-024): {@code enrollMfa}, {@code verifyMfa}, {@code disableMfa} and
 * {@code getMfaRecoveryCodes}, each for the signed-in user's own account ({@code 401} for an API token, which has no
 * user).
 *
 * <p>A code-checking call ({@code verifyMfa}, {@code disableMfa}) is limited to {@value #CODE_CHECKS_PER_MINUTE} a
 * minute per user ({@code 429 RATE_LIMITED} with {@code Retry-After}), so six digits cannot be guessed through them.
 * Every change and every refused code is audited with the user; a secret or code never is.
 */
@RestController
@Tag(name = "MFA")
public class MfaController {

    /** Code checks a user may make per minute. */
    public static final int CODE_CHECKS_PER_MINUTE = 10;

    private final MfaService mfa;
    private final SessionService sessions;
    private final ObjectProvider<ClientRateLimiter> limiter;
    private final Clock clock;

    /**
     * Creates the controller.
     *
     * @param mfa the second factor
     * @param sessions the account audit
     * @param limiter the per-client rate limiter, when enabled
     * @param clock the clock
     */
    public MfaController(MfaService mfa, SessionService sessions, ObjectProvider<ClientRateLimiter> limiter,
            Clock clock) {
        this.mfa = mfa;
        this.sessions = sessions;
        this.limiter = limiter;
        this.clock = clock;
    }

    /** {@code enrollMfa}: a new pending TOTP secret. */
    @PostMapping("/v1/me/mfa/enroll")
    @Operation(operationId = "enrollMfa", tags = "MFA")
    public Map<String, Object> enroll(HttpServletRequest request) {
        AuthenticatedSession session = session(request);
        Map<String, Object> out = mfa.enroll(session.userId());
        audit("enrollMfa", session, 200, request, "mfa_enrollment_started");
        return out;
    }

    /**
     * {@code verifyMfa}: activates a pending enrollment (the answer then carries the first recovery codes, shown once)
     * or, once active, checks a step-up code.
     */
    @PostMapping("/v1/me/mfa/verify")
    @Operation(operationId = "verifyMfa", tags = "MFA")
    public Map<String, Object> verify(@RequestBody JsonNode body, HttpServletRequest request) {
        AuthenticatedSession session = session(request);
        limit(session);
        String code = text(body, "code");
        if (code == null || code.isBlank()) {
            throw ValidationException.forField("code", "is required");
        }
        MfaService.Verified verified;
        try {
            verified = mfa.verify(session.userId(), code);
        } catch (ValidationException e) {
            audit("verifyMfa", session, 400, request, "mfa_code_refused");
            throw e;
        }
        Map<String, Object> out = new LinkedHashMap<>(verified.status());
        if (verified.activated()) {
            Map<String, Object> codes = new LinkedHashMap<>();
            codes.put("codes", verified.recoveryCodes());
            codes.put("generatedAt", verified.generatedAt());
            out.put("recoveryCodes", codes);
        }
        audit("verifyMfa", session, 200, request, verified.activated() ? "mfa_enabled" : "mfa_step_up");
        return out;
    }

    /** {@code disableMfa}: re-authenticates with a code or the password, then turns MFA off. */
    @PostMapping("/v1/me/mfa/disable")
    @Operation(operationId = "disableMfa", tags = "MFA")
    public Map<String, Object> disable(@RequestBody JsonNode body, HttpServletRequest request) {
        AuthenticatedSession session = session(request);
        limit(session);
        Map<String, Object> out;
        try {
            out = mfa.disable(session.userId(), text(body, "code"), text(body, "password"));
        } catch (ValidationException e) {
            audit("disableMfa", session, 400, request, "mfa_disable_refused");
            throw e;
        }
        audit("disableMfa", session, 200, request, "mfa_disabled");
        return out;
    }

    /** {@code getMfaRecoveryCodes}: a fresh set of recovery codes, the old set invalidated. */
    @GetMapping("/v1/me/mfa/recovery-codes")
    @Operation(operationId = "getMfaRecoveryCodes", tags = "MFA")
    public Map<String, Object> recoveryCodes(HttpServletRequest request) {
        AuthenticatedSession session = session(request);
        Map<String, Object> out = mfa.regenerateRecoveryCodes(session.userId());
        audit("getMfaRecoveryCodes", session, 200, request, "mfa_recovery_codes_regenerated");
        return out;
    }

    private void audit(String operationId, AuthenticatedSession session, int status, HttpServletRequest request,
            String event) {
        sessions.user(session.userId()).ifPresent(user -> sessions.auditAuth(clock.instant(), operationId,
                user, session.sessionId(), status, request.getRemoteAddr(), Map.of("event", event)));
    }

    /** Charges the user for one code check; {@code 429} past {@value #CODE_CHECKS_PER_MINUTE}/min. */
    private void limit(AuthenticatedSession session) {
        ClientRateLimiter l = limiter.getIfAvailable();
        if (l == null || !l.enabled()) {
            return;
        }
        ClientRateLimiter.Decision decision = l.tryConsume("mfa:" + session.userId(), RateClass.WRITE,
                CODE_CHECKS_PER_MINUTE);
        if (!decision.allowed()) {
            throw new ForesignalException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.RATE_LIMITED,
                    "Too many code checks; retry later.")
                    .withHeader(HttpHeaders.RETRY_AFTER, Long.toString(decision.retryAfterSeconds()));
        }
    }

    private static String text(JsonNode body, String field) {
        JsonNode value = body == null ? null : body.get(field);
        return value == null || !value.isString() ? null : value.asString();
    }

    private static AuthenticatedSession session(HttpServletRequest request) {
        return AuthenticatedSession.of(request).orElseThrow(() -> new ForesignalException(HttpStatus.UNAUTHORIZED,
                ErrorCodes.UNAUTHORIZED, "This operation needs a signed-in session, not an API token."));
    }
}
