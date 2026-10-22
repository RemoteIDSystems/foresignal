package ai.foresignal.security.ratelimit;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ProblemWriter;
import ai.foresignal.security.CurrentToken;
import ai.foresignal.security.IngestCaller;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenFilter;
import ai.foresignal.security.restrict.RequestPrincipal;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Per-client rate limits of the access perimeter (prompt 035; OWASP API4:2023).
 *
 * <p>Runs after {@link TokenFilter}, so the caller is known: the client key is the
 * {@link RequestPrincipal}'s token or feed-key id, an ingest caller's principal, or otherwise the
 * client IP that Tomcat's {@code RemoteIpValve} resolved through the trusted proxies of prompt 023
 * ({@code request.getRemoteAddr()}; a spoofed {@code X-Forwarded-For} from an untrusted peer changes
 * nothing). Requests the token filter refused never get here; repeated bad credentials are slowed by
 * {@link AuthFailureBackoff} instead.
 *
 * <p>Each request is charged to one {@linkplain #classify class}:
 *
 * <ul>
 *   <li>{@code tiles}: GET of {@code /v1/tiles…} and every other tile or TileJSON path;
 *   <li>{@code stream}: GET of a path ending in {@code /stream} (and the legacy {@code /mcp/sse}),
 *       one open per request plus a concurrent slot held until the async response completes, times
 *       out or fails;
 *   <li>{@code expensive}: {@code /v1/search}, POST {@code /v1/ask} and {@code /v1/ask/stream},
 *       {@code /v1/sample…};
 *   <li>{@code write}: any other method than GET, HEAD and OPTIONS;
 *   <li>{@code read}: everything else.
 * </ul>
 *
 * A caller without a credential is also charged to {@code anonymous} per IP for every request except
 * {@code tiles}; when the anonymous profile sets its own {@code maxRequestsPerMinute}, the stricter of
 * the two applies (both are caps on the same callers). Tiles are left out because a map draws far more
 * of them than the anonymous budget allows: one map view used it up within seconds and every other read
 * the page made (a track, a summary) was refused for the rest of the minute. They stay limited by their
 * own class, per IP for an anonymous caller. A token's {@code maxRequestsPerMinute} overrides {@code read}, up or down. Admin tokens are exempt from {@code read} and {@code tiles} only.
 * {@code /actuator/prometheus} is limited per IP when {@code metricsPerMinute} is set. Preflights and
 * paths outside {@code /v1}, {@code /mcp} and {@code /actuator/prometheus} are never limited.
 *
 * <p>A refusal is {@code 429 RATE_LIMITED} with {@code Retry-After} in whole seconds (at least 1) and
 * the refusing {@code class} in the problem. Paths of key-in-path URLs ({@code /v1/k/{key}/…}) are
 * classified by their canonical path and the key never appears in a refusal.
 */
@Component
public class ClientRateLimitFilter extends OncePerRequestFilter implements Ordered {

    /**
     * After {@link TokenFilter}, which binds the principal, and before the MCP caller filter (at
     * {@code TokenFilter.ORDER + 10}), so an MCP flood is refused before any session work.
     */
    public static final int ORDER = TokenFilter.ORDER + 5;

    /** The scraped metrics endpoint. */
    static final String PROMETHEUS = "/actuator/prometheus";

    /** The prefix of key-in-path URLs (Conventions: {@code /v1/k/{key}/…}). */
    static final String KEY_PATH_PREFIX = "/v1/k/";

    private final ClientRateLimiter limiter;
    private final ProblemWriter problems;

    /** Creates the filter. */
    public ClientRateLimitFilter(ClientRateLimiter limiter, ProblemWriter problems) {
        this.limiter = limiter;
        this.problems = problems;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = canonicalPath(request);
        return !(isApi(path) || isMcp(path) || PROMETHEUS.equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!limiter.enabled()) {
            chain.doFilter(request, response);
            return;
        }
        RateLimitSettings settings = limiter.settings();
        String path = canonicalPath(request);
        String ipKey = ClientRateLimiter.ipKey(request.getRemoteAddr());

        if (PROMETHEUS.equals(path)) {
            ClientRateLimiter.Decision decision = limiter.tryConsume(ipKey, RateClass.METRICS);
            if (!decision.allowed()) {
                refuse(response, decision);
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        Caller caller = caller(request, ipKey);
        RateClass rateClass = classify(request.getMethod(), path);

        if (caller.anonymous() && rateClass != RateClass.TILES) {
            int limit = caller.anonymousOverride() != null
                    ? Math.min(caller.anonymousOverride(), settings.anonymous())
                    : settings.anonymous();
            ClientRateLimiter.Decision decision = limiter.tryConsume(ipKey, RateClass.ANONYMOUS, limit);
            if (!decision.allowed()) {
                refuse(response, decision);
                return;
            }
        }
        boolean exempt = caller.admin() && (rateClass == RateClass.READ || rateClass == RateClass.TILES);
        if (!exempt) {
            int limit = rateClass == RateClass.READ && caller.readOverride() != null
                    ? caller.readOverride()
                    : settings.perMinute(rateClass);
            ClientRateLimiter.Decision decision = limiter.tryConsume(caller.key(), rateClass, limit);
            if (!decision.allowed()) {
                refuse(response, decision);
                return;
            }
        }
        ClientRateLimiter.StreamSlot slot = null;
        if (rateClass == RateClass.STREAM) {
            slot = limiter.tryOpenStream(caller.key(), settings.stream().concurrent());
            if (slot == null) {
                refuse(response, limiter.concurrentStreamsRefused());
                return;
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            holdUntilStreamEnds(request, slot);
        }
    }

    /**
     * The class of a request.
     *
     * @param method the HTTP method
     * @param path the canonical path (no query, no key-in-path prefix)
     */
    static RateClass classify(String method, String path) {
        boolean read = "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method);
        if (isExpensive(method, path)) {
            return RateClass.EXPENSIVE;
        }
        if (!read) {
            return RateClass.WRITE;
        }
        if (isTiles(path)) {
            return RateClass.TILES;
        }
        if (path.endsWith("/stream") || path.equals("/mcp/sse")) {
            return RateClass.STREAM;
        }
        return RateClass.READ;
    }

    /** Search, Ask (both forms) and sampling. */
    static boolean isExpensive(String method, String path) {
        if (path.equals("/v1/search")) {
            return true;
        }
        if ("POST".equalsIgnoreCase(method) && (path.equals("/v1/ask") || path.equals("/v1/ask/stream"))) {
            return true;
        }
        return path.equals("/v1/sample") || path.startsWith("/v1/sample/");
    }

    /** Map tiles and TileJSON wherever they are served. */
    static boolean isTiles(String path) {
        return path.equals("/v1/tiles.json")
                || path.startsWith("/v1/tiles/")
                || path.contains("/tiles/")
                || path.endsWith("/tiles.json")
                || path.endsWith("/tiles");
    }

    /**
     * The path to classify: Tomcat's decoded and normalized servlet path (so {@code %2F} or
     * {@code ;param} spellings cannot move a request into a cheaper class), without a trailing slash
     * and with a key-in-path prefix {@code /v1/k/{key}} replaced by {@code /v1}.
     */
    static String canonicalPath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();
        String path = (servletPath == null ? "" : servletPath) + (pathInfo == null ? "" : pathInfo);
        if (path.isEmpty()) {
            path = request.getRequestURI();
            int query = path == null ? -1 : path.indexOf('?');
            path = query >= 0 ? path.substring(0, query) : path;
        }
        if (path == null || path.isEmpty()) {
            return "/";
        }
        if (path.startsWith(KEY_PATH_PREFIX)) {
            int next = path.indexOf('/', KEY_PATH_PREFIX.length());
            path = next < 0 ? "/v1" : "/v1" + path.substring(next);
        }
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private static boolean isApi(String path) {
        return path.equals("/v1") || path.startsWith("/v1/");
    }

    private static boolean isMcp(String path) {
        return path.equals("/mcp") || path.startsWith("/mcp/");
    }

    /**
     * Who is charged.
     *
     * @param key the bucket key
     * @param anonymous true when the caller presented no credential
     * @param admin true for an admin token
     * @param readOverride a token's {@code maxRequestsPerMinute}, or null
     * @param anonymousOverride the anonymous profile's {@code maxRequestsPerMinute}, or null; the
     *     stricter of it and the {@code anonymous} setting applies
     */
    record Caller(String key, boolean anonymous, boolean admin, Integer readOverride, Integer anonymousOverride) {}

    /** The caller of {@code request}, from the principal, the accepted token or the ingest caller. */
    static Caller caller(HttpServletRequest request, String ipKey) {
        Optional<RequestPrincipal> principal = RequestPrincipal.of(request);
        if (principal.isPresent()) {
            RequestPrincipal p = principal.get();
            Integer override = positive(p.restriction().maxRequestsPerMinute());
            if (p.isAnonymous()) {
                return new Caller(ipKey, true, false, null, override);
            }
            return new Caller(p.clientKey(), false, p.scope() == Scope.ADMIN, override, null);
        }
        Optional<CurrentToken> token = CurrentToken.of(request);
        if (token.isPresent()) {
            CurrentToken t = token.get();
            Integer override = t.restrictions() == null ? null : positive(t.restrictions().maxRequestsPerMinute());
            return new Caller("token:" + t.id(), false, t.scope() == Scope.ADMIN, override, null);
        }
        Object ingest = request.getAttribute(IngestCaller.ATTRIBUTE);
        if (ingest instanceof IngestCaller caller && !caller.signatureOnly()) {
            return new Caller("ingest:" + caller.principal(), false, false, null, null);
        }
        return new Caller(ipKey, true, false, null, null);
    }

    private static Integer positive(Integer value) {
        return value == null || value <= 0 ? null : value;
    }

    /** {@code 429 RATE_LIMITED} with {@code Retry-After} and the refusing class. */
    private void refuse(HttpServletResponse response, ClientRateLimiter.Decision decision) throws IOException {
        long retryAfter = Math.max(1, decision.retryAfterSeconds());
        response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(retryAfter));
        ProblemDetail problem = problems.problem(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.RATE_LIMITED,
                "Too many " + decision.rateClass().wireValue() + " requests from this client; retry after "
                        + retryAfter + " s.");
        problem.setProperty("class", decision.rateClass().wireValue());
        problem.setProperty("retryAfterSeconds", retryAfter);
        problems.write(response, problem);
    }

    /**
     * Keeps a stream slot claimed while its SSE response is open: released when the async response
     * completes, fails or times out, or at once when the request did not go async (a refusal, an
     * error).
     */
    private static void holdUntilStreamEnds(HttpServletRequest request, ClientRateLimiter.StreamSlot slot) {
        if (slot == null) {
            return;
        }
        if (!request.isAsyncStarted()) {
            slot.release();
            return;
        }
        try {
            request.getAsyncContext().addListener(new AsyncListener() {
                @Override
                public void onComplete(AsyncEvent event) {
                    slot.release();
                }

                @Override
                public void onTimeout(AsyncEvent event) {
                    slot.release();
                }

                @Override
                public void onError(AsyncEvent event) {
                    slot.release();
                }

                @Override
                public void onStartAsync(AsyncEvent event) {
                    // A re-dispatch keeps the slot.
                }
            });
        } catch (IllegalStateException e) {
            // The async context already completed between the check and the listener.
            slot.release();
        }
    }
}
