package ai.foresignal.security;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ProblemWriter;
import ai.foresignal.config.CorsFilter;
import ai.foresignal.security.AccessRequirement.IngestKeyOrAdmin;
import ai.foresignal.security.AccessRequirement.Open;
import ai.foresignal.security.AccessRequirement.RequiresScope;
import ai.foresignal.security.audit.AuditRecord;
import ai.foresignal.security.audit.AuditService;
import ai.foresignal.security.ticket.Ticket;
import ai.foresignal.security.ticket.TicketAcceptance;
import ai.foresignal.security.ticket.TicketStore;
import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SecuritySettings;
import ai.foresignal.settings.SettingKeys;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Enforces {@link AccessPolicy} on every request with scoped API tokens (Architecture §10, §15;
 * FR-31; prompt 023).
 *
 * <p>Order of decisions:
 *
 * <ol>
 *   <li>The caller is resolved from {@code Authorization: Bearer fsr_…} through
 *       {@link TokenService}, or from {@code ?ticket=fst_…} through {@link TicketStore} on the paths
 *       a ticket purpose covers. A ticket is never accepted in the header.
 *   <li>Missing or invalid credential where one is needed: {@code 401}. Data reads ({@code read}
 *       rules) need none while {@code security.openReads} is on, unless the rule is
 *       {@code alwaysAuthenticated}.
 *   <li>Scope too low: {@code 403 INSUFFICIENT_SCOPE} with {@code requiredScope}.
 *   <li>An endpoint restriction with no matching pattern: {@code 403 ENDPOINT_NOT_ALLOWED}.
 *   <li>Data restrictions: {@link RestrictionEnforcer} ({@code 403 RESTRICTION_NOT_ENFORCED} until
 *       prompt 035).
 * </ol>
 *
 * <p>An accepted request carries {@link CurrentToken} (and the scope under
 * {@link #SCOPE_ATTRIBUTE}). The {@code ticket} parameter is stripped from the request before it
 * reaches anything else. Every authenticated write and every 401/403 on a write path is audited.
 *
 * <p>Deliberately a plain servlet filter: a small static policy table and one credential family do
 * not need Spring Security. That choice is why {@code management.endpoint.health.show-details} must
 * be {@code never}. Runs after {@link CorsFilter} so a preflight gets its CORS headers even when the
 * request itself would be rejected. {@code /actuator/prometheus} stays open on the assumption that
 * Foresignal is reachable only from a trusted network (prompt 035 adds {@code metricsOpen}).
 */
@Component
public class TokenFilter extends OncePerRequestFilter implements Ordered {

    /** Deliberately after {@link CorsFilter}, so preflights are answered before any rejection. */
    public static final int ORDER = CorsFilter.ORDER + 10;

    static final String BEARER_PREFIX = "Bearer ";
    static final String WWW_AUTHENTICATE_VALUE = "Bearer realm=\"foresignal\"";

    /** Set on the request so later components can tell the caller's scope. */
    public static final String SCOPE_ATTRIBUTE = "ai.foresignal.scope";

    /** The problem code of a rejection, for the audit entry. */
    static final String REJECTION_ATTRIBUTE = "ai.foresignal.rejection";

    private final AccessPolicy policy;
    private final TokenService tokens;
    private final TicketStore tickets;
    private final AppSettingService settings;
    private final ProblemWriter problems;
    private final RestrictionEnforcer restrictions;
    private final AuditService audit;
    private final SpecOperationIndex spec;
    private final SecureRequestMonitor secureRequests;
    private final Clock clock;
    private final AntPathMatcher matcher = new AntPathMatcher();

    private volatile SecuritySettings securitySettings;

    /** Creates the filter. */
    public TokenFilter(AccessPolicy policy, TokenService tokens, TicketStore tickets, AppSettingService settings,
            ProblemWriter problems, RestrictionEnforcer restrictions, AuditService audit, SpecOperationIndex spec,
            SecureRequestMonitor secureRequests, Clock clock) {
        this.policy = policy;
        this.tokens = tokens;
        this.tickets = tickets;
        this.settings = settings;
        this.problems = problems;
        this.restrictions = restrictions;
        this.audit = audit;
        this.spec = spec;
        this.secureRequests = secureRequests;
        this.clock = clock;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    /** Picks up a changed {@code security} setting without a restart. */
    @EventListener
    public void onSettingChanged(AppSettingChangedEvent event) {
        if (SettingKeys.SECURITY.equals(event.key())) {
            securitySettings = null;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest original, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (original.isSecure()) {
            secureRequests.secureRequest();
        }
        String ticketValue = TicketStrippingRequest.ticket(original.getQueryString());
        HttpServletRequest request = ticketValue == null ? original : new TicketStrippingRequest(original);
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        String path = request.getRequestURI();
        AccessRequirement requirement = policy.requirementFor(method, path);
        String bearer = bearerToken(request);

        if (requirement instanceof Open) {
            // Open endpoints never reject a credential, but a valid token is still noted (getSystem).
            if (bearer != null) {
                tokens.authenticate(bearer).ifPresent(t -> accept(request, new CurrentToken(t, false)));
            }
            chain.doFilter(request, response);
            return;
        }

        // ---- resolve the caller ----
        CurrentToken current = null;
        Ticket streamTicket = null;
        if (bearer != null) {
            if (bearer.startsWith(TicketStore.TICKET_PREFIX)) {
                reject(request, response, method, "Tickets are accepted only as the ?ticket= query parameter.");
                return;
            }
            Optional<AuthenticatedToken> token = tokens.authenticate(bearer);
            if (token.isEmpty()) {
                reject(request, response, method, "The presented token is not valid.");
                return;
            }
            current = new CurrentToken(token.get(), false);
        } else if (ticketValue != null) {
            TicketAcceptance acceptance = tickets.accept(ticketValue, method, path);
            switch (acceptance) {
                case TicketAcceptance.Accepted accepted -> {
                    current = new CurrentToken(accepted.ticket().parent(), true);
                    streamTicket = accepted.ticket().purpose() == ai.foresignal.security.ticket.TicketPurpose.STREAM
                            ? accepted.ticket()
                            : null;
                }
                case TicketAcceptance.Busy busy -> {
                    tooMany(response);
                    return;
                }
                case TicketAcceptance.Rejected rejected -> {
                    reject(request, response, method, rejected.reason());
                    return;
                }
            }
        }

        try {
            Optional<ProblemDetail> refusal = authorize(requirement, current, method, path);
            if (refusal.isPresent()) {
                release(streamTicket);
                streamTicket = null;
                ProblemDetail problem = refusal.get();
                request.setAttribute(REJECTION_ATTRIBUTE, problem.getProperties() == null ? null
                        : problem.getProperties().get("code"));
                if (problem.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
                    response.setHeader(HttpHeaders.WWW_AUTHENTICATE, WWW_AUTHENTICATE_VALUE);
                }
                problems.write(response, problem);
                auditIfNeeded(request, response, method, path, current);
                return;
            }
            if (current != null) {
                accept(request, current);
            }
            chain.doFilter(request, response);
            auditIfNeeded(request, response, method, path, current);
        } finally {
            holdUntilStreamEnds(request, streamTicket);
        }
    }

    /** The refusal for {@code current} on this request, or empty when it may proceed. */
    private Optional<ProblemDetail> authorize(AccessRequirement requirement, CurrentToken current, HttpMethod method,
            String path) {
        if (requirement instanceof RequiresScope required) {
            if (current == null) {
                if (required.scope() == Scope.READ && !required.alwaysAuthenticated() && openReads()) {
                    return Optional.empty();
                }
                return Optional.of(unauthorized(required.alwaysAuthenticated() && required.scope() == Scope.READ
                        ? "This endpoint always requires a token, even while open reads are on."
                        : "This endpoint requires a bearer token."));
            }
            return checkToken(required.scope(), requirement, current, method, path);
        }
        if (requirement instanceof IngestKeyOrAdmin) {
            // Per-source ingest keys arrive with custom sources in prompt 024. Until then an admin
            // token is the only way in, which keeps the endpoint closed rather than open.
            if (current == null) {
                return Optional.of(unauthorized("Ingest requires an ingest key or an admin token."));
            }
            return checkToken(Scope.ADMIN, requirement, current, method, path);
        }
        // AccessRequirement is sealed, so this is unreachable; fail closed if a case is ever added.
        return Optional.of(unauthorized("This endpoint requires a bearer token."));
    }

    private Optional<ProblemDetail> checkToken(Scope required, AccessRequirement requirement, CurrentToken current,
            HttpMethod method, String path) {
        if (!current.scope().includes(required)) {
            ProblemDetail problem = problems.problem(HttpStatus.FORBIDDEN, ErrorCodes.INSUFFICIENT_SCOPE,
                    "This operation needs the " + required.wireValue() + " scope; the token has "
                            + current.scope().wireValue() + ".");
            problem.setProperty("requiredScope", required.wireValue());
            return Optional.of(problem);
        }
        TokenRestrictions r = current.restrictions();
        if (r.hasEndpointRestrictions() && !endpointAllowed(r, method, path)) {
            return Optional.of(problems.problem(HttpStatus.FORBIDDEN, ErrorCodes.ENDPOINT_NOT_ALLOWED,
                    "This token is restricted to other endpoints."));
        }
        return restrictions.check(method, path, requirement, current.token())
                .map(refusal -> problems.problem(HttpStatus.FORBIDDEN, refusal.code(), refusal.detail()));
    }

    /** True when one of the token's {@code METHOD /path} patterns matches the request. */
    boolean endpointAllowed(TokenRestrictions restrictions, HttpMethod method, String path) {
        String normalized = AccessPolicy.normalize(path);
        for (String endpoint : restrictions.endpoints()) {
            int space = endpoint.indexOf(' ');
            if (space <= 0) {
                continue;
            }
            String m = endpoint.substring(0, space);
            String pattern = endpoint.substring(space + 1).strip();
            boolean methodOk = "*".equals(m) || m.equalsIgnoreCase(method.name())
                    || (HttpMethod.HEAD.equals(method) && "GET".equalsIgnoreCase(m));
            if (methodOk && matcher.match(pattern, normalized)) {
                return true;
            }
        }
        return false;
    }

    private void accept(HttpServletRequest request, CurrentToken current) {
        request.setAttribute(CurrentToken.ATTRIBUTE, current);
        request.setAttribute(SCOPE_ATTRIBUTE, current.scope());
    }

    /**
     * Keeps a stream ticket claimed while its SSE response is open: released when the async
     * response completes, fails or times out, or at once when the request did not go async.
     */
    private void holdUntilStreamEnds(HttpServletRequest request, Ticket ticket) {
        if (ticket == null) {
            return;
        }
        if (!request.isAsyncStarted()) {
            release(ticket);
            return;
        }
        AtomicBoolean released = new AtomicBoolean();
        Runnable once = () -> {
            if (released.compareAndSet(false, true)) {
                release(ticket);
            }
        };
        request.getAsyncContext().addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent event) {
                once.run();
            }

            @Override
            public void onTimeout(AsyncEvent event) {
                once.run();
            }

            @Override
            public void onError(AsyncEvent event) {
                once.run();
            }

            @Override
            public void onStartAsync(AsyncEvent event) {
                // A re-dispatch keeps the claim.
            }
        });
    }

    private void release(Ticket ticket) {
        if (ticket != null) {
            tickets.release(ticket);
        }
    }

    // ------------------------------------------------------------------------- audit

    /** Audits an authenticated write, or a 401/403 on a write path. */
    private void auditIfNeeded(HttpServletRequest request, HttpServletResponse response, HttpMethod method,
            String path, CurrentToken current) {
        if (!isWrite(method)) {
            return;
        }
        int status = response.getStatus();
        boolean refused = status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value();
        if (current == null && !refused) {
            return;
        }
        String normalized = AccessPolicy.normalize(path);
        String[] segments = normalized.split("/");
        Map<String, Object> detail = new LinkedHashMap<>();
        Object code = request.getAttribute(REJECTION_ATTRIBUTE);
        if (code != null) {
            detail.put("code", code);
        }
        if (current != null && current.viaTicket()) {
            detail.put("viaTicket", true);
        }
        audit.record(new AuditRecord(
                clock.instant(),
                AuditRecord.TOKEN,
                current == null ? null : current.id(),
                current == null ? null : current.name(),
                current == null ? null : current.principalId(),
                current == null ? null : current.scope().wireValue(),
                method.name(),
                normalized,
                spec.operationId(method, normalized),
                status,
                segments.length > 2 ? segments[2] : null,
                segments.length > 3 ? segments[3] : null,
                request.getRemoteAddr(),
                detail));
    }

    static boolean isWrite(HttpMethod method) {
        return !HttpMethod.GET.equals(method) && !HttpMethod.HEAD.equals(method) && !HttpMethod.OPTIONS.equals(method);
    }

    // ------------------------------------------------------------------------- helpers

    private boolean openReads() {
        SecuritySettings cached = securitySettings;
        if (cached == null) {
            cached = settings.getOrDefault(SettingKeys.SECURITY, SecuritySettings.class, SecuritySettings::defaults);
            securitySettings = cached;
        }
        return cached.openReads();
    }

    /** The token from {@code Authorization: Bearer <token>}, or null when absent or malformed. */
    private static String bearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private ProblemDetail unauthorized(String detail) {
        return problems.problem(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, detail);
    }

    /**
     * Answers 401 with a problem document and audits it on a write path.
     *
     * <p>The detail never contains the presented token or ticket: a 401 often ends up in a log or a
     * bug report, and echoing the credential would leak it.
     */
    private void reject(HttpServletRequest request, HttpServletResponse response, HttpMethod method, String detail)
            throws IOException {
        request.setAttribute(REJECTION_ATTRIBUTE, ErrorCodes.UNAUTHORIZED);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, WWW_AUTHENTICATE_VALUE);
        problems.write(response, unauthorized(detail));
        auditIfNeeded(request, response, method, request.getRequestURI(), null);
    }

    private void tooMany(HttpServletResponse response) throws IOException {
        response.setHeader(HttpHeaders.RETRY_AFTER, "5");
        problems.write(response, problems.problem(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.STREAM_LIMIT,
                "This stream ticket is already in use by an open stream; a ticket serves one stream at a time."));
    }
}
