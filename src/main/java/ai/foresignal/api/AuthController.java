package ai.foresignal.api;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.api.support.QueryParams;
import ai.foresignal.security.Scope;
import ai.foresignal.security.ratelimit.AuthFailureBackoff;
import ai.foresignal.security.ratelimit.ClientRateLimiter;
import ai.foresignal.security.ratelimit.RateClass;
import ai.foresignal.security.user.AccountMailer;
import ai.foresignal.security.user.AuthenticatedSession;
import ai.foresignal.security.user.SessionService;
import ai.foresignal.security.user.UserLifecycleService;
import ai.foresignal.security.user.UserTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

/**
 * The {@code Auth} operations (prompt R2-003; the MFA step of {@code login} is R2-024): {@code login}, {@code logout}, {@code requestPasswordReset},
 * {@code resetPassword}, {@code activateAccount}, {@code listSessions}, {@code revokeSession} and
 * {@code revokeAllSessions}.
 *
 * <p>The four credential operations are open and limited to {@value #AUTH_PER_MINUTE} requests a minute per client
 * address ({@code 429 RATE_LIMITED} with {@code Retry-After}); a failed sign-in also counts toward the address's
 * {@link AuthFailureBackoff}. The session operations need a signed-in session (an API token has no user to list
 * sessions for): {@code 401} otherwise. {@code revokeAllSessions} always keeps the calling session; {@code logout}
 * ends it.
 */
@RestController
@Tag(name = "Auth")
public class AuthController {

    /** Credential requests a client address may make per minute. */
    public static final int AUTH_PER_MINUTE = 10;

    private final SessionService sessions;
    private final UserLifecycleService lifecycle;
    private final UserTokenService userTokens;
    private final AccountMailer mailer;
    private final ObjectProvider<ClientRateLimiter> limiter;
    private final ObjectProvider<AuthFailureBackoff> backoff;

    /**
     * Creates the controller.
     *
     * @param sessions sign-in and sessions
     * @param lifecycle activation and password setting
     * @param userTokens reset tokens
     * @param mailer the account email sender
     * @param limiter the per-client rate limiter, when enabled
     * @param backoff the auth-failure backoff, when enabled
     */
    public AuthController(SessionService sessions, UserLifecycleService lifecycle, UserTokenService userTokens,
            AccountMailer mailer, ObjectProvider<ClientRateLimiter> limiter, ObjectProvider<AuthFailureBackoff> backoff) {
        this.sessions = sessions;
        this.lifecycle = lifecycle;
        this.userTokens = userTokens;
        this.mailer = mailer;
        this.limiter = limiter;
        this.backoff = backoff;
    }

    /** {@code login}: email and password to a {@code SessionToken}. */
    @PostMapping("/v1/auth/login")
    @Operation(operationId = "login", tags = "Auth")
    public Map<String, Object> login(@RequestBody JsonNode body, HttpServletRequest request) {
        limit(request);
        String email = required(body, "email");
        String password = required(body, "password");
        JsonNode code = body.get("mfaCode");
        String mfaCode = code == null || !code.isString() ? null : code.asString();
        SessionService.Login login;
        try {
            login = sessions.login(email, password, mfaCode, request.getHeader(HttpHeaders.USER_AGENT),
                    request.getRemoteAddr());
        } catch (ForesignalException e) {
            if (ErrorCodes.INVALID_CREDENTIALS.equals(e.getCode())) {
                failed(request);
            }
            throw e;
        }
        AuthFailureBackoff b = backoff.getIfAvailable();
        if (b != null) {
            b.recordSuccess(request.getRemoteAddr());
        }
        Map<String, Object> out = new LinkedHashMap<>();
        if (login.mfaRequired()) {
            // R2-024: the password was right, but MFA is active and no code came with it. No session exists; the
            // client asks for a code and signs in again with mfaCode.
            out.put("token", "");
            out.put("tokenPrefix", null);
            out.put("userId", login.user().getId().toString());
            out.put("issuedAt", null);
            out.put("expiresAt", null);
            out.put("idleExpiresAt", null);
            out.put("mfaRequired", true);
            out.put("scopes", List.of());
            out.put("permissions", List.of());
            return out;
        }
        return sessionToken(login);
    }

    /**
     * The {@code SessionToken} of a sign-in that issued a session ({@code login}, and {@code runSetup} for the first
     * admin, R2-027).
     *
     * @param login the sign-in; its session must exist
     * @return the {@code SessionToken} object
     */
    public static Map<String, Object> sessionToken(SessionService.Login login) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("token", login.token());
        out.put("tokenPrefix", login.session().getTokenPrefix());
        out.put("userId", login.user().getId().toString());
        out.put("issuedAt", login.session().getCreatedAt());
        out.put("expiresAt", login.session().getExpiresAt());
        out.put("idleExpiresAt", login.session().getIdleExpiresAt());
        out.put("mfaRequired", false);
        out.put("scopes", scopes(login.permissions().scope()));
        out.put("permissions", login.permissions().asList());
        return out;
    }

    /** {@code logout}: ends the calling session. */
    @PostMapping("/v1/auth/logout")
    @Operation(operationId = "logout", tags = "Auth")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        sessions.logout(session(request), request.getRemoteAddr());
    }

    /** {@code requestPasswordReset}: always 202, whether or not the email belongs to an account. */
    @PostMapping("/v1/auth/password/reset-request")
    @Operation(operationId = "requestPasswordReset", tags = "Auth")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void requestPasswordReset(@RequestBody JsonNode body, HttpServletRequest request) {
        limit(request);
        String email = required(body, "email");
        var issued = userTokens.requestReset(email);
        var user = issued.flatMap(i -> sessions.user(i.token().getUserId()));
        user.ifPresent(u -> mailer.sendPasswordReset(u, issued.get()));
        // R2-013: audited with the user when one matched; the email asked for is not stored.
        sessions.auditAuth("requestPasswordReset", user.orElse(null), 202, request.getRemoteAddr(),
                user.isPresent() ? Map.of("event", "password_reset_requested")
                        : Map.of("event", "password_reset_requested", "reason", "no_active_account"));
    }

    /** {@code resetPassword}: consumes a reset token and sets the new password; every session of the user ends. */
    @PostMapping("/v1/auth/password/reset")
    @Operation(operationId = "resetPassword", tags = "Auth")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@RequestBody JsonNode body, HttpServletRequest request) {
        limit(request);
        String token = required(body, "token");
        String password = required(body, "newPassword");
        try {
            var user = lifecycle.resetWithToken(token, password, "newPassword");
            sessions.auditAuth("resetPassword", user, 204, request.getRemoteAddr(), Map.of("event", "password_reset"));
        } catch (ValidationException e) {
            throw audited(gone(e, ErrorCodes.RESET_TOKEN_GONE, request), "resetPassword", "password_reset_failed",
                    request);
        }
    }

    /** {@code activateAccount}: consumes an activation token, sets the first password and activates the account. */
    @PostMapping("/v1/auth/activate")
    @Operation(operationId = "activateAccount", tags = "Auth")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activateAccount(@RequestBody JsonNode body, HttpServletRequest request) {
        limit(request);
        String token = required(body, "token");
        String password = required(body, "password");
        try {
            var user = lifecycle.activateWithToken(token, password);
            sessions.auditAuth("activateAccount", user, 204, request.getRemoteAddr(),
                    Map.of("event", "account_activated"));
        } catch (ValidationException e) {
            throw audited(gone(e, ErrorCodes.ACTIVATION_TOKEN_GONE, request), "activateAccount",
                    "activation_failed", request);
        } catch (ConflictException e) {
            // The account is no longer invited: the link no longer applies.
            throw audited(new ForesignalException(HttpStatus.GONE, ErrorCodes.ACTIVATION_TOKEN_GONE,
                    "The activation link is not valid. It may have expired or been used already."), "activateAccount",
                    "activation_failed", request);
        }
    }

    /** {@code listSessions}: the caller's live sessions; the calling one is marked {@code current}. */
    @GetMapping("/v1/auth/sessions")
    @Operation(operationId = "listSessions", tags = "Auth")
    public Map<String, Object> listSessions(@RequestParam(required = false) String cursor,
            @RequestParam(required = false) String limit, HttpServletRequest request) {
        AuthenticatedSession current = session(request);
        return sessions.list(current.sessionId(), current.userId(), cursor,
                QueryParams.intInRange("limit", limit, 1, 500, 100));
    }

    /** {@code revokeAllSessions}: revokes every other session of the caller, keeping the calling one. */
    @DeleteMapping("/v1/auth/sessions")
    @Operation(operationId = "revokeAllSessions", tags = "Auth")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeAllSessions(HttpServletRequest request) {
        AuthenticatedSession current = session(request);
        sessions.revokeAll(current.userId(), current.sessionId(), request.getRemoteAddr());
    }

    /** {@code revokeSession}: revokes one of the caller's own sessions; another user's is {@code 404}. */
    @DeleteMapping("/v1/auth/sessions/{id}")
    @Operation(operationId = "revokeSession", tags = "Auth")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeSession(@PathVariable String id, HttpServletRequest request) {
        AuthenticatedSession current = session(request);
        UUID sessionId;
        try {
            sessionId = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException(ErrorCodes.UNKNOWN_SESSION, "No such session.");
        }
        sessions.revoke(current.userId(), sessionId, request.getRemoteAddr());
    }

    /** The scopes a session of {@code scope} may call (the hierarchy expanded, as {@code Me.scopes}). */
    static List<String> scopes(Scope scope) {
        List<String> out = new ArrayList<>();
        for (Scope s : List.of(Scope.READ, Scope.AGENT, Scope.ADMIN)) {
            if (scope.includes(s)) {
                out.add(s.wireValue());
            }
        }
        return out;
    }

    private static AuthenticatedSession session(HttpServletRequest request) {
        return AuthenticatedSession.of(request).orElseThrow(() -> new ForesignalException(HttpStatus.UNAUTHORIZED,
                ErrorCodes.UNAUTHORIZED, "This operation needs a signed-in session, not an API token."));
    }

    private static String required(JsonNode body, String field) {
        JsonNode value = body == null ? null : body.get(field);
        if (value == null || !value.isString() || value.asString().isEmpty()) {
            throw ValidationException.forField(field, "is required");
        }
        return value.asString();
    }

    /** Charges the client address for one credential request; {@code 429} past {@value #AUTH_PER_MINUTE}/min. */
    private void limit(HttpServletRequest request) {
        ClientRateLimiter l = limiter.getIfAvailable();
        if (l == null || !l.enabled()) {
            return;
        }
        ClientRateLimiter.Decision decision = l.tryConsume("auth:" + ClientRateLimiter.ipKey(request.getRemoteAddr()),
                RateClass.WRITE, AUTH_PER_MINUTE);
        if (!decision.allowed()) {
            throw new ForesignalException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.RATE_LIMITED,
                    "Too many sign-in requests from this address; retry later.")
                    .withHeader(HttpHeaders.RETRY_AFTER, Long.toString(decision.retryAfterSeconds()));
        }
    }

    /** Counts a failed credential toward the address's backoff; past the threshold the answer becomes 429. */
    private void failed(HttpServletRequest request) {
        AuthFailureBackoff b = backoff.getIfAvailable();
        if (b == null) {
            return;
        }
        Optional<Duration> retry = b.recordFailure(request.getRemoteAddr());
        if (retry.isPresent()) {
            throw new ForesignalException(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.RATE_LIMITED,
                    "Too many failed sign-ins from this address; retry later.")
                    .withHeader(HttpHeaders.RETRY_AFTER, Long.toString(AuthFailureBackoff.seconds(retry.get())));
        }
    }

    /**
     * A refused reset or activation: a policy refusal stays {@code 400}; every token failure becomes {@code 410} with
     * the operation's code, and counts as a failed credential.
     */
    /**
     * Audits a refused activation or reset when the link was gone (410); a password-policy refusal (400) is the
     * user's typing, not an event, and is not audited (prompt R2-013). No user is named: the link did not resolve.
     */
    private RuntimeException audited(RuntimeException refusal, String operationId, String event,
            HttpServletRequest request) {
        if (refusal instanceof ForesignalException f && f.getStatus() == HttpStatus.GONE) {
            sessions.auditAuth(operationId, null, HttpStatus.GONE.value(), request.getRemoteAddr(),
                    Map.of("event", event, "code", f.getCode()));
        }
        return refusal;
    }

    private RuntimeException gone(ValidationException e, String code, HttpServletRequest request) {
        if (!ErrorCodes.USER_TOKEN_INVALID.equals(e.getCode())) {
            return e;
        }
        failed(request);
        return new ForesignalException(HttpStatus.GONE, code,
                "The link is not valid. It may have expired or been used already.");
    }
}
