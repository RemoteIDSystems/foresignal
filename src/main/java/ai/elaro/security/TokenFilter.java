package ai.elaro.security;

import ai.elaro.api.error.ErrorCodes;
import ai.elaro.api.error.ProblemWriter;
import ai.elaro.config.CorsFilter;
import ai.elaro.security.AccessRequirement.IngestKeyOrAdmin;
import ai.elaro.security.AccessRequirement.Open;
import ai.elaro.security.AccessRequirement.RequiresScope;
import ai.elaro.settings.AppSettingChangedEvent;
import ai.elaro.settings.AppSettingService;
import ai.elaro.settings.SecuritySettings;
import ai.elaro.settings.SettingKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Enforces {@link AccessPolicy} on every request (Architecture §10, §15; FR-31).
 *
 * <p>Deliberately a plain servlet filter: Elaro has one credential type and a small static policy
 * table, so Spring Security would add a large amount of machinery for no benefit. That choice is
 * why {@code management.endpoint.health.show-details} must be {@code never} — {@code when-authorized}
 * only means anything with Spring Security present.
 *
 * <p>Runs after {@link ai.elaro.config.CorsFilter} so a cross-origin preflight gets its CORS
 * headers even when the actual request would be rejected; a browser that never sees the preflight
 * answer reports an opaque CORS failure instead of the 401 the developer needs to see.
 *
 * <p>{@code /actuator/prometheus} is intentionally open. Scraping it needs no credential, on the
 * assumption that Elaro is reachable only from a trusted network; anyone exposing it to the
 * internet should put a reverse proxy in front.
 */
@Component
public class TokenFilter extends OncePerRequestFilter implements Ordered {

    /** Deliberately after {@link CorsFilter}, so preflights are answered before any rejection. */
    public static final int ORDER = CorsFilter.ORDER + 10;

    static final String BEARER_PREFIX = "Bearer ";
    static final String WWW_AUTHENTICATE_VALUE = "Bearer realm=\"elaro\"";

    /** Set on the request so later components can tell how the caller authenticated. */
    public static final String SCOPE_ATTRIBUTE = "ai.elaro.scope";

    private final AccessPolicy policy;
    private final AdminTokenService adminTokens;
    private final AppSettingService settings;
    private final ProblemWriter problems;

    private volatile SecuritySettings securitySettings;

    public TokenFilter(
            AccessPolicy policy,
            AdminTokenService adminTokens,
            AppSettingService settings,
            ProblemWriter problems) {
        this.policy = policy;
        this.adminTokens = adminTokens;
        this.settings = settings;
        this.problems = problems;
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
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        AccessRequirement requirement =
                policy.requirementFor(method(request), request.getRequestURI());

        if (requirement instanceof Open) {
            chain.doFilter(request, response);
            return;
        }

        String presented = bearerToken(request);

        if (requirement instanceof RequiresScope required) {
            if (presented == null && required.scope() == Scope.READ && openReads()) {
                // Data reads are open by default; no credential, no scope attribute.
                chain.doFilter(request, response);
                return;
            }
            if (presented == null) {
                reject(response, "This endpoint requires a bearer token.");
                return;
            }
            if (!adminTokens.verify(presented)) {
                reject(response, "The presented token is not valid.");
                return;
            }
            // Until prompt 019 the only token is the bootstrap admin token, which satisfies every
            // scope. The policy already records the final scopes, so 019 changes this lookup only.
            request.setAttribute(SCOPE_ATTRIBUTE, Scope.ADMIN);
            chain.doFilter(request, response);
            return;
        }

        if (requirement instanceof IngestKeyOrAdmin) {
            // Per-source ingest keys arrive with custom sources in prompt 020. Until then an admin
            // token is the only way in, which keeps the endpoint closed rather than open.
            if (presented != null && adminTokens.verify(presented)) {
                request.setAttribute(SCOPE_ATTRIBUTE, Scope.ADMIN);
                chain.doFilter(request, response);
                return;
            }
            reject(response, "Ingest requires an ingest key or an admin token.");
            return;
        }

        // AccessRequirement is sealed, so this is unreachable; fail closed if a case is ever added.
        reject(response, "This endpoint requires a bearer token.");
    }

    private boolean openReads() {
        SecuritySettings cached = securitySettings;
        if (cached == null) {
            cached =
                    settings.getOrDefault(
                            SettingKeys.SECURITY, SecuritySettings.class, SecuritySettings::defaults);
            securitySettings = cached;
        }
        return cached.openReads();
    }

    private static HttpMethod method(HttpServletRequest request) {
        return HttpMethod.valueOf(request.getMethod());
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

    /**
     * Answers 401 with a problem document.
     *
     * <p>The detail never contains the presented token: a 401 often ends up in a log or a bug
     * report, and echoing the credential would leak it.
     */
    private void reject(HttpServletResponse response, String detail) throws IOException {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, WWW_AUTHENTICATE_VALUE);
        problems.write(
                response,
                problems.problem(HttpStatus.UNAUTHORIZED, ErrorCodes.UNAUTHORIZED, detail));
    }
}
