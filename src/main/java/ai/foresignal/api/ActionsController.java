package ai.foresignal.api;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ProblemWriter;
import ai.foresignal.notify.action.ActionPages;
import ai.foresignal.notify.action.ActionPaths;
import ai.foresignal.notify.action.ActionRateLimiter;
import ai.foresignal.notify.action.ActionService;
import ai.foresignal.security.ratelimit.AuthFailureBackoff;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

/**
 * Notification action capabilities (prompt 033): {@code getActionConfirm} and {@code executeAction}
 * at {@code /v1/actions/{token}}. Open in {@code AccessPolicy}: the capability is the credential.
 *
 * <ul>
 *   <li>{@code GET} <b>never acts</b>: it returns a confirm page with one POST button, or a
 *       {@code 410} page, so link previews, mail scanners and prefetchers are harmless.
 *   <li>{@code POST} executes once. {@code Accept: application/json} gets {@code ActionResult};
 *       anything else an HTML result page. A request whose {@code Origin} is neither the
 *       {@code publicBaseUrl} origin nor the request's own origin is refused ({@code 403}), which
 *       blocks cross-site form posts; service workers and ntfy send no Origin or the right one.
 * </ul>
 * Every response carries {@code Cache-Control: no-store}, {@code Referrer-Policy: no-referrer},
 * {@code X-Robots-Tag: noindex} and the strict {@link ActionPages#CONTENT_SECURITY_POLICY}; both
 * methods share a limit of 30 requests per minute per client IP. The token never appears in a
 * problem's {@code instance}, the audit log or a log line ({@link ActionPaths}).
 *
 * <p>Prompt 035: an unknown, expired or used capability is a failed credential check for
 * {@link AuthFailureBackoff}; past its threshold the refusal becomes {@code 429 RATE_LIMITED} with a
 * doubling {@code Retry-After}, and a valid capability resets the address. The 30-per-minute limit
 * stays here, on top of the perimeter's {@code anonymous} class (120 per minute per IP): it is stricter,
 * specific to capability links, and answers browsers with an HTML page.
 */
@RestController
@Tag(name = "Notifications")
@RequestMapping("/v1/actions")
public class ActionsController {

    private final ActionService actions;
    private final ActionRateLimiter limiter;
    private final ProblemWriter problems;
    private final JsonMapper json;
    private final AuthFailureBackoff backoff;

    /** Creates the controller. */
    public ActionsController(ActionService actions, ActionRateLimiter limiter, ProblemWriter problems,
            JsonMapper json, AuthFailureBackoff backoff) {
        this.actions = actions;
        this.limiter = limiter;
        this.problems = problems;
        this.json = json;
        this.backoff = backoff;
    }

    /** {@code getActionConfirm}: the confirm page; never acts. */
    @GetMapping("/{token}")
    @Operation(operationId = "getActionConfirm", tags = "Notifications")
    public ResponseEntity<String> getActionConfirm(@PathVariable String token, HttpServletRequest request) {
        ResponseEntity<String> limited = limited(request);
        if (limited != null) {
            return limited;
        }
        ActionService.Confirm confirm;
        try {
            confirm = actions.peek(token);
        } catch (ForesignalException e) {
            return failedCapability(request, e);
        }
        backoff.recordSuccess(request.getRemoteAddr());
        return html(HttpStatus.OK, ActionPages.confirm(confirm));
    }

    /** {@code executeAction}: executes the capability once. */
    @PostMapping("/{token}")
    @Operation(operationId = "executeAction", tags = "Notifications")
    public ResponseEntity<String> executeAction(@PathVariable String token, HttpServletRequest request) {
        ResponseEntity<String> limited = limited(request);
        if (limited != null) {
            return limited;
        }
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (origin != null && !originAllowed(origin, actions.baseUrl(), request)) {
            return refused(request, HttpStatus.FORBIDDEN, ErrorCodes.FORBIDDEN,
                    "Cross-site requests may not execute actions.", ActionPages.forbidden(), null);
        }
        ActionService.Result result;
        try {
            result = actions.execute(token, new ActionService.Via("http", null, request.getRemoteAddr()));
        } catch (ForesignalException e) {
            return failedCapability(request, e);
        }
        backoff.recordSuccess(request.getRemoteAddr());
        if (wantsJson(request)) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("action", result.action());
            body.put("alertId", result.alertId());
            body.put("result", result.result());
            return ResponseEntity.ok().headers(securityHeaders()).contentType(MediaType.APPLICATION_JSON)
                    .body(json.writeValueAsString(body));
        }
        return html(HttpStatus.OK, ActionPages.result(result));
    }

    /**
     * True when {@code origin} is the {@code publicBaseUrl} origin or the request's own origin
     * (scheme and {@code Host} as Tomcat resolved them behind trusted proxies).
     */
    static boolean originAllowed(String origin, String publicBaseUrl, HttpServletRequest request) {
        String given = normalizeOrigin(origin);
        if (given == null) {
            return false;
        }
        String configured = normalizeOrigin(publicBaseUrl);
        if (given.equals(configured)) {
            return true;
        }
        // The server name and port as Tomcat resolved them (behind trusted proxies, the forwarded host), not the raw
        // Host header, which a forwarding proxy leaves as its own address (checkpoint 041: the documented rule).
        String host = request.getServerName();
        if (host == null || host.isBlank()) {
            return false;
        }
        String authority = host.contains(":") && !host.startsWith("[") ? "[" + host + "]" : host;
        return given.equals(normalizeOrigin(request.getScheme() + "://" + authority + ":" + request.getServerPort()));
    }

    /** {@code scheme://host[:port]} in lowercase, default ports dropped; null when not a URL origin. */
    static String normalizeOrigin(String url) {
        if (url == null || url.isBlank() || "null".equals(url.strip())) {
            return null;
        }
        try {
            URI uri = URI.create(url.strip());
            if (uri.getScheme() == null || uri.getHost() == null) {
                return null;
            }
            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            int port = uri.getPort();
            boolean defaultPort = port == -1 || ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
            return scheme + "://" + uri.getHost().toLowerCase(Locale.ROOT) + (defaultPort ? "" : ":" + port);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * The refusal of a capability that did not verify: a failed credential check for
     * {@link AuthFailureBackoff}, answered {@code 410} (or the exception's own status) until the
     * address is in backoff, then {@code 429 RATE_LIMITED} with {@code Retry-After}.
     */
    private ResponseEntity<String> failedCapability(HttpServletRequest request, ForesignalException e) {
        Optional<Duration> retryAfter = backoff.recordFailure(request.getRemoteAddr());
        if (retryAfter.isPresent()) {
            long seconds = AuthFailureBackoff.seconds(retryAfter.get());
            return refused(request, HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.RATE_LIMITED,
                    "Too many invalid action links from this address; try again later.",
                    ActionPages.tooMany(seconds), Long.toString(seconds));
        }
        return refused(request, e.getStatus(), e.getCode(), e.getMessage(), ActionPages.gone(), null);
    }

    private ResponseEntity<String> limited(HttpServletRequest request) {
        ActionRateLimiter.Decision decision = limiter.attempt(request.getRemoteAddr());
        if (decision.allowed()) {
            return null;
        }
        return refused(request, HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.RATE_LIMITED,
                "Too many action requests; try again later.", ActionPages.tooMany(decision.retryAfterSeconds()),
                Long.toString(decision.retryAfterSeconds()));
    }

    /** A refusal as a problem document (JSON callers) or an HTML page, with the token redacted. */
    private ResponseEntity<String> refused(HttpServletRequest request, HttpStatus status, String code, String detail,
            String page, String retryAfter) {
        HttpHeaders headers = securityHeaders();
        if (retryAfter != null) {
            headers.set(HttpHeaders.RETRY_AFTER, retryAfter);
        }
        if (wantsJson(request) || status == HttpStatus.TOO_MANY_REQUESTS && !wantsHtml(request)) {
            ProblemDetail problem = problems.problem(status, code, detail);
            problem.setInstance(URI.create(ActionPaths.redact(request.getRequestURI()).replace("…", "%E2%80%A6")));
            return ResponseEntity.status(status).headers(headers).contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(json.writeValueAsString(problem));
        }
        return ResponseEntity.status(status).headers(headers).contentType(MediaType.TEXT_HTML).body(page);
    }

    private static ResponseEntity<String> html(HttpStatus status, String page) {
        return ResponseEntity.status(status).headers(securityHeaders())
                .contentType(new MediaType(MediaType.TEXT_HTML, java.nio.charset.StandardCharsets.UTF_8)).body(page);
    }

    /** The headers of every action response. */
    static HttpHeaders securityHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CACHE_CONTROL, "no-store");
        headers.set("Referrer-Policy", "no-referrer");
        headers.set("X-Robots-Tag", "noindex");
        headers.set("Content-Security-Policy", ActionPages.CONTENT_SECURITY_POLICY);
        return headers;
    }

    private static boolean wantsJson(HttpServletRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        return accept != null && accept.toLowerCase(Locale.ROOT).contains("json") && !wantsHtml(request);
    }

    private static boolean wantsHtml(HttpServletRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        return accept != null && accept.toLowerCase(Locale.ROOT).contains("text/html");
    }
}
