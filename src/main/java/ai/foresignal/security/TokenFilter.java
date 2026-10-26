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
import ai.foresignal.security.user.AuthenticatedSession;
import ai.foresignal.security.user.SessionService;
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
import java.util.List;
import ai.foresignal.security.ratelimit.AuthFailureBackoff;
import ai.foresignal.security.restrict.ReadRestriction;
import ai.foresignal.security.restrict.RequestPrincipal;
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
 *   <li>Scope too low: {@code 403 INSUFFICIENT_SCOPE} with {@code requiredScope}. For a signed-in user's session on
 *       an operation with an {@code x-foresignal-permission} (R2-004), the permission replaces the scope check:
 *       {@code 403 PERMISSION_DENIED} with {@code requiredPermission} when the user's roles lack it.
 *   <li>An endpoint restriction with no matching pattern: {@code 403 ENDPOINT_NOT_ALLOWED}.
 *   <li>Data restrictions: {@link RestrictionEnforcer}; from prompt 035 data restrictions are applied
 *       by {@code RestrictionClamp} on every read path, so the enforcer refuses nothing.
 * </ol>
 *
 * <p>Prompt 035 adds the perimeter:
 *
 * <ul>
 *   <li>Every accepted request carries a {@link RequestPrincipal}: the token with its effective
 *       {@link ReadRestriction}, the anonymous profile ({@code security.anonymous}) for tokenless
 *       data reads, or a feed key bound by {@link ai.foresignal.security.restrict.PathKeyFilter}.
 *       A feed key reaches only {@code read} operations that are not {@code alwaysAuthenticated};
 *       an anonymous profile with {@code endpoints} reaches only those ({@code 403 RESTRICTED}).
 *   <li>Each refused credential (bearer token, ticket, ingest key) counts for
 *       {@link AuthFailureBackoff}; an address in backoff gets {@code 429 RATE_LIMITED} with
 *       {@code Retry-After} instead of the 401. A correct credential is always evaluated and
 *       accepted, and resets the address.
 *   <li>{@code security.metricsOpen=false} makes {@code /actuator/prometheus} admin-only.
 * </ul>
 *
 * <p>On ingest paths ({@link AccessRequirement.IngestKeyOrAdmin}, prompt 024) a request without an
 * API token, or with an {@code eik_} bearer, goes to the {@link IngestKeyAuthenticator}: an ingest
 * key in {@code X-Foresignal-Ingest-Key}, {@code Authorization: Bearer eik_…}, Basic (the key as the
 * password), or the ntfy/Gotify compatibility credentials. An accepted caller is set as
 * {@link IngestCaller}; an admin API token becomes an {@link IngestCaller#ADMIN_TOKEN} caller.
 * Ingest-key writes are audited with actor kind {@code ingest_key}.
 *
 * <p>An accepted request carries {@link CurrentToken} (and the scope under
 * {@link #SCOPE_ATTRIBUTE}). The {@code ticket} parameter is stripped from the request before it
 * reaches anything else. Every authenticated write and every 401/403 on a write path is audited,
 * except accepted requests on {@code /mcp/**}, whose tool calls the MCP server audits one by one
 * (prompt 025).
 *
 * <p>Prompt 141 adds MCP remote authorization ({@link McpOAuthGate}): while {@code mcp.auth.mode} is
 * {@code oauth} or {@code both}, {@code /mcp} always needs a credential (open reads do not apply, so an MCP
 * client gets the {@code 401} that leads it to the identity provider), a JWT-shaped bearer there is verified as
 * an identity-provider access token, and every {@code 401} on {@code /mcp} carries
 * {@code WWW-Authenticate: Bearer resource_metadata="…", scope="…"}. A valid token whose scopes map to nothing
 * gets {@code 403} with {@code error="insufficient_scope"}. In {@code oauth} mode a Foresignal token is refused on
 * {@code /mcp}; REST paths never accept identity-provider tokens.
 *
 * <p>Release 2 (prompt R2-003) adds login sessions: a bearer starting {@code fss_} is resolved through
 * {@link SessionService} before the API-token lookup, to a {@link CurrentToken} whose token is the session's
 * {@linkplain AuthenticatedSession#asToken() token view} (scope derived from the user's effective permissions), with
 * the {@link AuthenticatedSession} itself under {@link AuthenticatedSession#ATTRIBUTE}. A revoked, expired or
 * idle-expired session, or one whose user is no longer active, is a {@code 401}. A session is never accepted on ingest
 * paths, peer paths (which take only {@code fss_}-free peer keys) or as a feed key.
 *
 * <p>Deliberately a plain servlet filter: a small static policy table and one credential family do
 * not need Spring Security. That choice is why {@code management.endpoint.health.show-details} must
 * be {@code never}. Runs after {@link CorsFilter} so a preflight gets its CORS headers even when the
 * request itself would be rejected. {@code /actuator/prometheus} stays open on the assumption that
 * Foresignal is reachable only from a trusted network unless {@code security.metricsOpen} is false.
 */
@Component
public class TokenFilter extends OncePerRequestFilter implements Ordered {

    /** Deliberately after {@link CorsFilter}, so preflights are answered before any rejection. */
    public static final int ORDER = CorsFilter.ORDER + 10;

    static final String BEARER_PREFIX = "Bearer ";

    /** Ingest keys start with this (prompt 024); such a bearer is never an API token. */
    static final String INGEST_KEY_PREFIX = "eik_";

    /** Device-scoped ingest keys (prompt 039) also go to the ingest-key authenticator. */
    static final String DEVICE_KEY_PREFIX = "eid_";
    static final String WWW_AUTHENTICATE_VALUE = "Bearer realm=\"foresignal\"";

    /** The Prometheus scrape endpoint, admin-only while {@code security.metricsOpen} is false. */
    static final String METRICS_PATH = "/actuator/prometheus";

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
    private final org.springframework.beans.factory.ObjectProvider<IngestKeyAuthenticator> ingestKeys;
    private final org.springframework.beans.factory.ObjectProvider<AuthFailureBackoff> backoff;
    private final AntPathMatcher matcher = new AntPathMatcher();

    private volatile SecuritySettings securitySettings;

    /** Creates the filter. */
    public TokenFilter(AccessPolicy policy, TokenService tokens, TicketStore tickets, AppSettingService settings,
            ProblemWriter problems, RestrictionEnforcer restrictions, AuditService audit, SpecOperationIndex spec,
            SecureRequestMonitor secureRequests, Clock clock,
            org.springframework.beans.factory.ObjectProvider<IngestKeyAuthenticator> ingestKeys,
            org.springframework.beans.factory.ObjectProvider<AuthFailureBackoff> backoff) {
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
        this.ingestKeys = ingestKeys;
        this.backoff = backoff;
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
        if (requirement instanceof Open && METRICS_PATH.equals(AccessPolicy.normalize(path)) && !metricsOpen()) {
            requirement = AccessRequirement.ADMIN;
        }
        String bearer = bearerToken(request);

        // A feed key or tile key (050) bound by PathKeyFilter: no bearer parsing, READ operations only.
        Optional<RequestPrincipal> pathKey = RequestPrincipal.of(request).filter(RequestPrincipal::isPathKey);
        if (pathKey.isPresent()) {
            feedKey(request, response, chain, requirement, pathKey.get());
            return;
        }

        if (requirement instanceof Open) {
            // Open endpoints never reject a credential, but a valid token is still noted (getSystem).
            if (bearer != null) {
                resolveBearer(request, bearer).ifPresent(t -> accept(request, t));
            }
            if (RequestPrincipal.of(request).isEmpty()) {
                anonymousPrincipal().bind(request);
            }
            chain.doFilter(request, response);
            return;
        }

        if (requirement instanceof AccessRequirement.PeerKey) {
            peerByKey(request, response, chain, method, path, bearer);
            return;
        }

        if (requirement instanceof IngestKeyOrAdmin
                && (bearer == null || bearer.startsWith(INGEST_KEY_PREFIX)
                        || bearer.startsWith(DEVICE_KEY_PREFIX))) {
            ingestByKey(request, response, chain, method, path);
            return;
        }

        if (mcpOAuthActive(path)) {
            if (bearer == null) {
                reject(request, response, method, "This MCP server requires an access token from its identity"
                        + " provider; see the resource_metadata in WWW-Authenticate.", false);
                return;
            }
            if (McpOAuthGate.looksLikeJwt(bearer)) {
                mcpOAuthToken(request, response, chain, method, path, bearer);
                return;
            }
            if (!mcpOAuth.acceptsApiTokens()) {
                reject(request, response, method, "Only access tokens from the identity provider are accepted on"
                        + " /mcp (mcp.auth.mode is oauth).");
                return;
            }
            // mode both: a Foresignal token continues below; its 401s carry the OAuth challenge.
        }

        // ---- resolve the caller ----
        CurrentToken current = null;
        Ticket streamTicket = null;
        if (bearer != null) {
            if (bearer.startsWith(TicketStore.TICKET_PREFIX)) {
                reject(request, response, method, "Tickets are accepted only as the ?ticket= query parameter.");
                return;
            }
            Optional<CurrentToken> resolved = resolveBearer(request, bearer);
            if (resolved.isEmpty()) {
                reject(request, response, method, bearer.startsWith(SessionService.PREFIX)
                        ? "The session is not valid; sign in again."
                        : "The presented token is not valid.");
                return;
            }
            if (requirement instanceof IngestKeyOrAdmin && AuthenticatedSession.of(request).isPresent()) {
                reject(request, response, method, "Ingest requires an ingest key or an admin API token; a session is"
                        + " not accepted.");
                return;
            }
            current = resolved.get();
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
            Optional<ProblemDetail> refusal = authorize(requirement, current, method, path,
                    AuthenticatedSession.of(request).orElse(null));
            if (refusal.isPresent()) {
                release(streamTicket);
                streamTicket = null;
                ProblemDetail problem = refusal.get();
                request.setAttribute(REJECTION_ATTRIBUTE, problem.getProperties() == null ? null
                        : problem.getProperties().get("code"));
                if (problem.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
                    response.setHeader(HttpHeaders.WWW_AUTHENTICATE, challengeFor(request, current != null));
                }
                problems.write(response, problem);
                auditIfNeeded(request, response, method, path, current);
                return;
            }
            if (current == null) {
                anonymousPrincipal().bind(request);
            } else {
                accept(request, current);
                if (requirement instanceof IngestKeyOrAdmin) {
                    request.setAttribute(IngestCaller.ATTRIBUTE, new IngestCaller(
                            IngestCaller.ADMIN_TOKEN, null, "token:" + current.name(), null));
                }
            }
            chain.doFilter(request, response);
            auditIfNeeded(request, response, method, path, current);
        } finally {
            holdUntilStreamEnds(request, streamTicket);
        }
    }

    /**
     * An ingest path without an API token: the ingest key decides (prompt 024). No credential, or a
     * refused one, is a 401; an accepted caller proceeds with {@link IngestCaller} set, and its
     * writes are audited as {@code ingest_key}.
     */
    private void ingestByKey(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            HttpMethod method, String path) throws IOException, ServletException {
        IngestKeyAuthenticator authenticator = ingestKeys.getIfAvailable();
        IngestKeyAuthenticator.Result result = authenticator == null
                ? IngestKeyAuthenticator.Result.none()
                : authenticator.authenticate(request);
        if (!result.accepted()) {
            reject(request, response, method, result.refused()
                    ? result.rejection()
                    : "Ingest requires an ingest key or an admin token.", result.refused());
            return;
        }
        IngestCaller caller = result.caller();
        request.setAttribute(IngestCaller.ATTRIBUTE, caller);
        AuthFailureBackoff b = backoff.getIfAvailable();
        if (b != null) {
            b.recordSuccess(request.getRemoteAddr());
        }
        chain.doFilter(request, response);
        if (isWrite(method)) {
            String normalized = AccessPolicy.normalize(path);
            String[] segments = normalized.split("/");
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("via", caller.via());
            if (caller.sourceId() != null) {
                detail.put("sourceId", caller.sourceId());
            }
            audit.record(new AuditRecord(
                    clock.instant(),
                    caller.device() ? AuditRecord.INGEST_DEVICE : AuditRecord.INGEST_KEY,
                    null,
                    caller.principal(),
                    null,
                    null,
                    method.name(),
                    normalized,
                    spec.operationId(method, normalized),
                    response.getStatus(),
                    segments.length > 2 ? segments[2] : null,
                    segments.length > 3 ? segments[3] : null,
                    request.getRemoteAddr(),
                    detail));
        }
    }

    /** Resolves federation peer keys (prompt 113); absent in contexts without the federation package. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private PeerKeyAuthenticator peerKeys;

    /**
     * A federation peer path (prompt 113): only a peer ingest key ({@code fsp_…} as a Bearer token) is accepted;
     * an API token, even admin, is refused with 401. An accepted request carries {@link PeerCaller}, and its
     * writes are audited as {@code ingest_key} with {@code via: peer_key}.
     */
    private void peerByKey(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            HttpMethod method, String path, String bearer) throws IOException, ServletException {
        if (bearer == null || !bearer.startsWith(PeerKeyAuthenticator.PREFIX) || peerKeys == null) {
            reject(request, response, method, "This endpoint requires a peer ingest key (fsp_) as a Bearer token.",
                    bearer != null);
            return;
        }
        Optional<String> peer = peerKeys.authenticate(bearer);
        if (peer.isEmpty()) {
            reject(request, response, method, "The presented peer ingest key is not valid.", true);
            return;
        }
        request.setAttribute(PeerCaller.ATTRIBUTE, new PeerCaller(peer.get()));
        AuthFailureBackoff b = backoff.getIfAvailable();
        if (b != null) {
            b.recordSuccess(request.getRemoteAddr());
        }
        chain.doFilter(request, response);
        if (isWrite(method)) {
            String normalized = AccessPolicy.normalize(path);
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("via", "peer_key");
            detail.put("peerId", peer.get());
            audit.record(new AuditRecord(clock.instant(), AuditRecord.INGEST_KEY, null, "peer:" + peer.get(), null,
                    null, method.name(), normalized, spec.operationId(method, normalized), response.getStatus(),
                    "federation", peer.get(), request.getRemoteAddr(), detail));
        }
    }

    /** Resolves login sessions (R2-003); absent in slices without the identity model. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private SessionService sessions;

    /** Sets the {@link SessionService} (unit tests that build the filter by hand). */
    void setSessionService(SessionService sessions) {
        this.sessions = sessions;
    }

    /**
     * Resolves a bearer: a session ({@code fss_}, also binding {@link AuthenticatedSession}) or an API token.
     *
     * @param request the request
     * @param bearer the bearer
     * @return the current token, or empty when the bearer is not accepted
     */
    private Optional<CurrentToken> resolveBearer(HttpServletRequest request, String bearer) {
        if (bearer.startsWith(SessionService.PREFIX)) {
            SessionService s = sessions;
            if (s == null) {
                return Optional.empty();
            }
            return s.authenticate(bearer).map(session -> {
                request.setAttribute(AuthenticatedSession.ATTRIBUTE, session);
                return new CurrentToken(session.asToken(), false);
            });
        }
        return tokens.authenticate(bearer).map(t -> new CurrentToken(t, false));
    }

    /** Verifies identity-provider access tokens on {@code /mcp} (prompt 141); absent in slices without it. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private McpOAuthGate mcpOAuth;

    /** Sets the {@link McpOAuthGate} (unit tests that build the filter by hand). */
    void setMcpOAuthGate(McpOAuthGate gate) {
        this.mcpOAuth = gate;
    }

    /** True when {@code path} is the MCP server's and {@code mcp.auth.mode} is {@code oauth} or {@code both}. */
    private boolean mcpOAuthActive(String path) {
        McpOAuthGate gate = mcpOAuth;
        return gate != null && isMcp(AccessPolicy.normalize(path)) && gate.active();
    }

    /**
     * The {@code WWW-Authenticate} value of a {@code 401} on this request: the RFC 9728 challenge on
     * {@code /mcp} while MCP remote authorization is on (prompt 141), else {@value #WWW_AUTHENTICATE_VALUE}.
     */
    private String challengeFor(HttpServletRequest request, boolean presented) {
        if (mcpOAuthActive(request.getRequestURI())) {
            return mcpOAuth.challenge(request, presented);
        }
        return WWW_AUTHENTICATE_VALUE;
    }

    /**
     * An identity-provider access token on {@code /mcp} (prompt 141). Accepted: the request proceeds with the
     * synthetic {@link CurrentToken} and {@link McpOAuthGate#ATTRIBUTE}; refused: {@code 401} with the RFC 9728
     * challenge (counted by the failure backoff); valid without a mapped scope: {@code 403 INSUFFICIENT_SCOPE}
     * with {@code error="insufficient_scope"}. The token itself is never forwarded or logged.
     */
    private void mcpOAuthToken(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            HttpMethod method, String path, String bearer) throws IOException, ServletException {
        McpOAuthGate.Outcome outcome = mcpOAuth.authenticate(bearer);
        switch (outcome) {
            case McpOAuthGate.Accepted accepted -> {
                CurrentToken current = new CurrentToken(accepted.token(), false);
                accept(request, current);
                request.setAttribute(McpOAuthGate.ATTRIBUTE, accepted);
                chain.doFilter(request, response);
                auditIfNeeded(request, response, method, path, current);
            }
            case McpOAuthGate.Rejected refused -> reject(request, response, method,
                    "The access token is not valid here: " + refused.reason());
            case McpOAuthGate.NoScope noScope -> {
                ProblemDetail problem = problems.problem(HttpStatus.FORBIDDEN, ErrorCodes.INSUFFICIENT_SCOPE,
                        noScope.reason());
                problem.setProperty("requiredScope", Scope.READ.wireValue());
                request.setAttribute(REJECTION_ATTRIBUTE, ErrorCodes.INSUFFICIENT_SCOPE);
                response.setHeader(HttpHeaders.WWW_AUTHENTICATE, mcpOAuth.insufficientScope(request, Scope.READ));
                problems.write(response, problem);
                auditIfNeeded(request, response, method, path, null);
            }
        }
    }

    /**
     * The refusal for {@code current} on this request, or empty when it may proceed. {@code session} is the signed-in
     * user behind {@code current} when the bearer was a session (R2-004 gates it on permissions), else null.
     */
    private Optional<ProblemDetail> authorize(AccessRequirement requirement, CurrentToken current, HttpMethod method,
            String path, AuthenticatedSession session) {
        if (requirement instanceof RequiresScope required) {
            if (current == null) {
                if (required.scope() == Scope.READ && !required.alwaysAuthenticated() && openReads()) {
                    RequestPrincipal anonymous = anonymousPrincipal();
                    List<String> endpoints = anonymous.restriction().endpoints();
                    if (endpoints != null && !endpointAllowed(endpoints, method, path)) {
                        return Optional.of(problems.problem(HttpStatus.FORBIDDEN, ErrorCodes.RESTRICTED,
                                "Anonymous reads are restricted to other endpoints; present a token."));
                    }
                    return Optional.empty();
                }
                return Optional.of(unauthorized(required.alwaysAuthenticated() && required.scope() == Scope.READ
                        ? "This endpoint always requires a token, even while open reads are on."
                        : "This endpoint requires a bearer token."));
            }
            return checkToken(required.scope(), requirement, current, method, path, session);
        }
        if (requirement instanceof IngestKeyOrAdmin) {
            // Ingest keys are handled before this point (ingestByKey); an API token must be admin.
            if (current == null) {
                return Optional.of(unauthorized("Ingest requires an ingest key or an admin token."));
            }
            return checkToken(Scope.ADMIN, requirement, current, method, path, session);
        }
        // AccessRequirement is sealed, so this is unreachable; fail closed if a case is ever added.
        return Optional.of(unauthorized("This endpoint requires a bearer token."));
    }

    /**
     * The scope, endpoint and data checks for an accepted caller.
     *
     * <p>Prompt R2-004: for a session on an operation that carries an {@code x-foresignal-permission}, the permission
     * alone decides ({@code 403 PERMISSION_DENIED} with {@code requiredPermission} when the user's roles lack it), in
     * place of the scope derived from those same permissions; that is what lets an Auditor read the admin-scoped
     * audit log and keeps an Operator, whose derived scope is admin, out of user management. A session on an
     * operation without one is gated on its derived scope. An API token is gated on scope only, as before: an admin
     * token satisfies every permission, because machines are not permission-scoped.
     */
    private Optional<ProblemDetail> checkToken(Scope required, AccessRequirement requirement, CurrentToken current,
            HttpMethod method, String path, AuthenticatedSession session) {
        Optional<String> permission = session == null ? Optional.empty() : policy.permissionFor(method, path);
        if (permission.isPresent()) {
            if (!session.permissions().keys().contains(permission.get())) {
                ProblemDetail problem = problems.problem(HttpStatus.FORBIDDEN, ErrorCodes.PERMISSION_DENIED,
                        "This operation needs the " + permission.get() + " permission, which none of your roles"
                                + " grants.");
                problem.setProperty("requiredPermission", permission.get());
                return Optional.of(problem);
            }
        } else if (!current.scope().includes(required)) {
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
        return endpointAllowed(restrictions.endpoints(), method, path);
    }

    /** True when one of the {@code METHOD /path} patterns in {@code endpoints} matches the request. */
    boolean endpointAllowed(List<String> endpoints, HttpMethod method, String path) {
        String normalized = AccessPolicy.normalize(path);
        for (String endpoint : endpoints) {
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
        AuthenticatedToken t = current.token();
        new RequestPrincipal(RequestPrincipal.Kind.TOKEN, t.id(), t.name(), t.scope(), restrictionOf(t),
                t.privateAccess()).bind(request);
        AuthFailureBackoff b = backoff.getIfAvailable();
        if (b != null) {
            b.recordSuccess(request.getRemoteAddr());
        }
    }

    /**
     * The effective read restriction of a token (prompt 035). Private sources stay governed by the
     * 023 gate unless the token sets {@code excludePrivate} itself.
     */
    public static ReadRestriction restrictionOf(AuthenticatedToken token) {
        return ReadRestriction.of(token.restrictions(), false);
    }

    /** The anonymous principal under the {@code security.anonymous} profile in force. */
    RequestPrincipal anonymousPrincipal() {
        // Sensitive alerts (rules referencing a watchlist, prompt 036) are never shown anonymously.
        return RequestPrincipal.anonymous(ReadRestriction.of(security().effectiveAnonymous(), true)
                .withExcludeSensitive(true));
    }

    /**
     * A request a {@code PathKeyFilter} bound to a feed key or a tile key (prompt 050): allowed only on
     * {@code read} operations that are not {@code alwaysAuthenticated} (and on open ones), evaluated on
     * the canonical path; {@code 403 RESTRICTED} otherwise.
     */
    private void feedKey(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            AccessRequirement requirement, RequestPrincipal principal) throws IOException, ServletException {
        boolean allowed = requirement instanceof Open
                || (requirement instanceof RequiresScope required && required.scope() == Scope.READ
                        && !required.alwaysAuthenticated());
        if (!allowed) {
            request.setAttribute(REJECTION_ATTRIBUTE, ErrorCodes.RESTRICTED);
            problems.write(response, problems.problem(HttpStatus.FORBIDDEN, ErrorCodes.RESTRICTED,
                    (principal.isTileKey() ? "A tile key" : "A feed key") + " reads only data endpoints."));
            return;
        }
        request.setAttribute(SCOPE_ATTRIBUTE, Scope.READ);
        chain.doFilter(request, response);
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
        if (!refused && isMcp(normalized)) {
            // Every MCP message is a POST; the MCP server audits each tool call itself (prompt 025).
            return;
        }
        if (status >= 200 && status < 300 && isShareLinkWrite(normalized)) {
            // ShareLinkService audits a created or revoked link itself, with the link id (prompt 112); a second
            // row here would duplicate it (checkpoint 124). Failed attempts are still audited below.
            return;
        }
        String[] segments = normalized.split("/");
        Map<String, Object> detail = new LinkedHashMap<>();
        Object code = request.getAttribute(REJECTION_ATTRIBUTE);
        if (code != null) {
            detail.put("code", code);
        }
        if (current != null && current.viaTicket()) {
            detail.put("viaTicket", true);
        }
        if (current != null && AuthenticatedSession.of(request).isPresent()) {
            detail.put("via", "session");
            detail.put("userId", AuthenticatedSession.of(request).get().userId().toString());
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

    /** True for the share-link writes, which {@code ShareLinkService} audits itself (prompt 112). */
    static boolean isShareLinkWrite(String normalizedPath) {
        return normalizedPath.equals("/v1/share-links") || normalizedPath.startsWith("/v1/share-links/");
    }

    /** True for the MCP server's paths, whose tool calls are audited per call (prompt 025). */
    static boolean isMcp(String normalizedPath) {
        return normalizedPath.equals("/mcp") || normalizedPath.startsWith("/mcp/");
    }

    static boolean isWrite(HttpMethod method) {
        return !HttpMethod.GET.equals(method) && !HttpMethod.HEAD.equals(method) && !HttpMethod.OPTIONS.equals(method);
    }

    // ------------------------------------------------------------------------- helpers

    private boolean openReads() {
        return security().openReads();
    }

    private boolean metricsOpen() {
        return !Boolean.FALSE.equals(security().metricsOpen());
    }

    private SecuritySettings security() {
        SecuritySettings cached = securitySettings;
        if (cached == null) {
            cached = settings.getOrDefault(SettingKeys.SECURITY, SecuritySettings.class, SecuritySettings::defaults);
            securitySettings = cached;
        }
        return cached;
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
        reject(request, response, method, detail, true);
    }

    /**
     * {@link #reject(HttpServletRequest, HttpServletResponse, HttpMethod, String)}; {@code presented}
     * is false when no credential was presented at all, which never counts as a failure.
     */
    private void reject(HttpServletRequest request, HttpServletResponse response, HttpMethod method, String detail,
            boolean presented) throws IOException {
        AuthFailureBackoff b = presented ? backoff.getIfAvailable() : null;
        Optional<java.time.Duration> retry = b == null ? Optional.empty() : b.recordFailure(request.getRemoteAddr());
        if (retry.isPresent()) {
            // Too many failed credentials from this address: back off instead of answering 401 (035).
            request.setAttribute(REJECTION_ATTRIBUTE, ErrorCodes.RATE_LIMITED);
            response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(AuthFailureBackoff.seconds(retry.get())));
            problems.write(response, problems.problem(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.RATE_LIMITED,
                    "Too many failed credentials from this address; retry later."));
            auditIfNeeded(request, response, method, request.getRequestURI(), null);
            return;
        }
        request.setAttribute(REJECTION_ATTRIBUTE, ErrorCodes.UNAUTHORIZED);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, challengeFor(request, presented));
        problems.write(response, unauthorized(detail));
        auditIfNeeded(request, response, method, request.getRequestURI(), null);
    }

    private void tooMany(HttpServletResponse response) throws IOException {
        response.setHeader(HttpHeaders.RETRY_AFTER, "5");
        problems.write(response, problems.problem(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.STREAM_LIMIT,
                "This stream ticket is already in use by an open stream; a ticket serves one stream at a time."));
    }
}
