package ai.foresignal.security.quota;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ProblemWriter;
import ai.foresignal.security.TokenFilter;
import ai.foresignal.security.ratelimit.ClientRateLimitFilter;
import ai.foresignal.security.user.AuthenticatedSession;
import ai.foresignal.security.user.TokenOwner;
import ai.foresignal.system.OperationalAlertService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * The per-user API rate limit (prompt R2-026).
 *
 * <p>Runs after {@link TokenFilter}, which binds the user: a session ({@link AuthenticatedSession}) or a user's own API
 * token ({@link TokenOwner}, R2-025). Every {@code /v1} and {@code /mcp} request of that user is charged to one bucket
 * of the user's effective {@code apiRateLimitPerMinute}, whichever session or token it came through; past it the
 * request is {@code 429 RATE_LIMITED} with {@code Retry-After} in whole seconds and {@code class: user}. A user whose
 * effective limit is unset is not limited here, and a request with no user (an API token without one, a feed key,
 * an anonymous caller) is left to the per-client limits of {@link ClientRateLimitFilter}, which still apply to the
 * user's requests too: a token's own {@code maxRequestsPerMinute} keeps limiting that token.
 *
 * <p>This limit does not follow {@code foresignal.ratelimit.enabled}: it is set per user by an admin, so it is on
 * exactly when a quota sets it.
 *
 * <p>When a user keeps meeting the limit for {@value #SUSTAINED_MINUTES} minutes (refused at least once in every
 * minute), one R2-014 operational alert {@code system.user_rate_limited} is raised, and
 * {@code system.user_rate_limit_cleared} once a minute passes without a refusal.
 */
@Component
public class UserRateLimitFilter extends OncePerRequestFilter implements Ordered {

    /** After the per-client limits ({@code TokenFilter.ORDER + 5}), before the MCP caller filter ({@code + 10}). */
    public static final int ORDER = TokenFilter.ORDER + 6;

    /** Minutes of refusals that raise the operational alert. */
    public static final int SUSTAINED_MINUTES = 5;

    /** The {@code class} of a refusal. */
    public static final String RATE_CLASS = "user";

    private static final Duration GAP = Duration.ofMinutes(1);

    private record Streak(Instant since, Instant last) {}

    private final QuotaService quotas;
    private final UserRateLimiter limiter;
    private final ProblemWriter problems;
    private final ObjectProvider<OperationalAlertService> alerts;
    private final Clock clock;
    private final Map<UUID, Streak> streaks = new ConcurrentHashMap<>();

    /**
     * Creates the filter.
     *
     * @param quotas the effective quotas
     * @param limiter the per-user buckets
     * @param problems writes the refusal
     * @param alerts the operational alerts, when present
     * @param clock the clock
     */
    public UserRateLimitFilter(QuotaService quotas, UserRateLimiter limiter, ProblemWriter problems,
            ObjectProvider<OperationalAlertService> alerts, Clock clock) {
        this.quotas = quotas;
        this.limiter = limiter;
        this.problems = problems;
        this.alerts = alerts;
        this.clock = clock;
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
        String path = request.getRequestURI();
        return path == null || !(path.equals("/v1") || path.startsWith("/v1/") || path.equals("/mcp")
                || path.startsWith("/mcp/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Optional<UUID> user = user(request);
        if (user.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }
        UUID id = user.get();
        limiter.record(id);
        Integer perMinute = quotas.effective(id).apiRateLimitPerMinute();
        if (perMinute != null) {
            Duration wait = limiter.tryAcquire(id, perMinute);
            if (!wait.isZero()) {
                refused(id, perMinute);
                refuse(response, wait, perMinute);
                return;
            }
        }
        allowed(id);
        chain.doFilter(request, response);
    }

    /** The user behind the request: the session's, or the owner of a user's own API token. */
    static Optional<UUID> user(HttpServletRequest request) {
        Optional<AuthenticatedSession> session = AuthenticatedSession.of(request);
        if (session.isPresent()) {
            return Optional.of(session.get().userId());
        }
        return TokenOwner.of(request).map(TokenOwner::userId);
    }

    private void refuse(HttpServletResponse response, Duration wait, int perMinute) throws IOException {
        long retryAfter = Math.max(1, (wait.toMillis() + 999) / 1000);
        response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(retryAfter));
        ProblemDetail problem = problems.problem(HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.RATE_LIMITED,
                "Your quota allows " + perMinute + " requests a minute (apiRateLimitPerMinute); retry after "
                        + retryAfter + " s.");
        problem.setProperty("class", RATE_CLASS);
        problem.setProperty("limit", "apiRateLimitPerMinute");
        problem.setProperty("quota", perMinute);
        problem.setProperty("retryAfterSeconds", retryAfter);
        problems.write(response, problem);
    }

    private void refused(UUID user, int perMinute) {
        Instant now = clock.instant();
        Streak streak = streaks.compute(user, (id, s) -> s == null || Duration.between(s.last(), now).compareTo(GAP) > 0
                ? new Streak(now, now) : new Streak(s.since(), now));
        if (Duration.between(streak.since(), now).compareTo(Duration.ofMinutes(SUSTAINED_MINUTES)) >= 0) {
            report(user, "warn", perMinute, streak.since());
        }
    }

    private void allowed(UUID user) {
        Streak streak = streaks.get(user);
        if (streak != null && Duration.between(streak.last(), clock.instant()).compareTo(GAP) > 0) {
            streaks.remove(user);
            report(user, "ok", null, streak.since());
        }
    }

    private void report(UUID user, String level, Integer perMinute, Instant since) {
        OperationalAlertService service = alerts.getIfAvailable();
        if (service == null) {
            return;
        }
        service.report("quota.rate:" + user, level, (current, previous) -> {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("userId", user.toString());
            data.put("apiRateLimitPerMinute", perMinute);
            data.put("since", since.toString());
            String path = "/v1/users/" + user + "/quota";
            if ("ok".equals(current)) {
                return new OperationalAlertService.Alert("system.user_rate_limit_cleared", "info",
                        "A user is back under their API rate limit",
                        "User " + user + " has made no refused request for a minute.", path, data);
            }
            return new OperationalAlertService.Alert("system.user_rate_limited", "warning",
                    "A user is held at their API rate limit",
                    "Since " + since + ", user " + user + " has been refused at " + perMinute
                            + " requests a minute in every minute.", path, data);
        });
    }
}
