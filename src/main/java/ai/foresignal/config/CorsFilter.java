package ai.foresignal.config;

import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.CorsSettings;
import ai.foresignal.settings.CorsSettingsService;
import ai.foresignal.settings.SettingKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Applies the CORS rules stored in {@code app_setting.cors} (FR-33, Architecture §10).
 *
 * <p>Hand-written rather than Spring's {@code CorsConfigurationSource} because the rule is not one
 * policy per path: reads and writes have different origin lists, and both are editable at runtime.
 * Expressing "GET from anywhere, POST from these three origins" through the standard machinery
 * would need two overlapping registrations plus a refresh mechanism.
 *
 * <p>Runs before {@link ai.foresignal.security.TokenFilter} so a preflight is answered even when the
 * real request would be rejected — otherwise a browser reports an opaque CORS error and hides the
 * 401 that explains the problem.
 */
@Component
public class CorsFilter extends OncePerRequestFilter implements Ordered {

    /** Lower than {@link ai.foresignal.security.TokenFilter#ORDER}. */
    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

    static final String ALLOWED_HEADERS = "Authorization, Content-Type, Accept, Last-Event-ID";
    static final String EXPOSED_HEADERS = "Location";
    static final String MAX_AGE_SECONDS = "600";

    private final CorsSettingsService corsSettings;
    private volatile CorsSettings cached;

    public CorsFilter(CorsSettingsService corsSettings) {
        this.corsSettings = corsSettings;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    /** Applies an updated origin list without a restart. */
    @EventListener
    public void onSettingChanged(AppSettingChangedEvent event) {
        if (SettingKeys.CORS.equals(event.key())) {
            cached = null;
        }
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (origin == null) {
            // Not a cross-origin request; CORS says nothing about it.
            chain.doFilter(request, response);
            return;
        }

        boolean preflight = isPreflight(request);
        HttpMethod effectiveMethod = effectiveMethod(request, preflight);
        boolean readOnly = isReadMethod(effectiveMethod);
        List<String> allowed = readOnly ? settings().readOrigins() : settings().writeOrigins();

        if (isAllowed(origin, allowed)) {
            applyHeaders(response, origin, preflight);
        }

        if (preflight) {
            // Answer the preflight here either way. With the headers it is an approval; without
            // them the browser sees a 200 that grants nothing, which is the correct refusal.
            response.setStatus(HttpStatus.OK.value());
            return;
        }
        chain.doFilter(request, response);
    }

    private void applyHeaders(HttpServletResponse response, String origin, boolean preflight) {
        // Echo the caller's origin rather than "*", so credentialed requests work and the Vary
        // header keeps caches honest.
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        response.addHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN);
        response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
        response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, EXPOSED_HEADERS);
        if (preflight) {
            response.setHeader(
                    HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                    "GET, HEAD, POST, PUT, DELETE, OPTIONS");
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
            response.setHeader(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE_SECONDS);
        }
    }

    /** True when {@code origin} is permitted by {@code allowed}, where {@code *} means any. */
    static boolean isAllowed(String origin, List<String> allowed) {
        for (String candidate : allowed) {
            if (CorsSettingsService.WILDCARD.equals(candidate) || candidate.equalsIgnoreCase(origin)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPreflight(HttpServletRequest request) {
        return HttpMethod.OPTIONS.matches(request.getMethod())
                && request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD) != null;
    }

    /**
     * For a preflight, the method the browser intends to use; otherwise the request's own method.
     *
     * <p>Checking {@code Access-Control-Request-Method} is what stops a preflight for a {@code POST}
     * being judged against the read origins just because the preflight itself is an {@code OPTIONS}.
     */
    private static HttpMethod effectiveMethod(HttpServletRequest request, boolean preflight) {
        if (preflight) {
            String requested = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
            return HttpMethod.valueOf(requested.trim().toUpperCase());
        }
        return HttpMethod.valueOf(request.getMethod());
    }

    private static boolean isReadMethod(HttpMethod method) {
        return HttpMethod.GET.equals(method)
                || HttpMethod.HEAD.equals(method)
                || HttpMethod.OPTIONS.equals(method);
    }

    private CorsSettings settings() {
        CorsSettings current = cached;
        if (current == null) {
            current = corsSettings.get();
            cached = current;
        }
        return current;
    }
}
