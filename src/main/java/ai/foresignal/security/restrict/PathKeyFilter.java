package ai.foresignal.security.restrict;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ProblemWriter;
import ai.foresignal.config.CorsFilter;
import ai.foresignal.security.ratelimit.AuthFailureBackoff;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.UrlPathHelper;

/**
 * Resolves keys carried in the path, {@code /v1/k/{key}/<rest>} (prompt 035; Conventions "Key-in-path
 * URLs"), for header-less clients: KML NetworkLinks, iCal subscriptions, feed readers, Home
 * Assistant {@code geo_json_events} and browser {@code EventSource}.
 *
 * <p>Runs before {@code TokenFilter}. For a {@code /v1/k/…} request it:
 *
 * <ol>
 *   <li>picks the {@link PathKeyResolver} by key prefix ({@code fsf_} feed keys, {@code fsk_} tile keys
 *       from prompt 050; 112 adds {@code fss_}) and resolves the key in constant time. Unknown →
 *       {@code 401} with the resolver's {@link PathKeyResolver#invalidCode()} ({@code INVALID_FEED_KEY},
 *       {@code INVALID_TILE_KEY}); expired or revoked → its {@link PathKeyResolver#goneStatus()} and
 *       {@link PathKeyResolver#goneCode()} ({@code 410 FEED_KEY_GONE} for feed keys, {@code 401
 *       INVALID_TILE_KEY} for tile keys). Each refusal counts as a failed credential for
 *       {@link AuthFailureBackoff};
 *   <li>allows only {@code GET} and {@code HEAD} ({@code 405} otherwise);
 *   <li>requires the canonical path {@code /v1/<rest>} to start with one of the key's prefixes on a
 *       segment boundary ({@code 403} with {@link PathKeyResolver#outsidePrefixCode()} otherwise:
 *       {@code RESTRICTED} for feed keys, {@code INSUFFICIENT_SCOPE} for tile keys); dot segments, empty segments and
 *       encoded separators are refused ({@code 400});
 *   <li>binds the key's {@link RequestPrincipal} and forwards the request, unchanged otherwise, to the
 *       canonical path within the same filter chain, so the token filter, the rate limiter and the
 *       controllers see {@code /v1/<rest>}.
 * </ol>
 *
 * <p>Prompt 064: the CelesTrak-compatible output ({@value #QUERY_KEY_PREFIX}…) is read by Gpredict and Look4Sat,
 * which can send neither headers nor a {@code /v1/k/} path, so there a feed key ({@code fsf_}) is accepted as the
 * query parameter {@value #QUERY_KEY_PARAM}. The same rules apply (unknown 401, gone 410, GET and HEAD only, the
 * path must lie under one of the key's prefixes, failures count for {@link AuthFailureBackoff}); the request is
 * not forwarded, and {@value #QUERY_KEY_ATTRIBUTE} is set so the response stays {@code Cache-Control: private}.
 * The access log never contains the query string.
 *
 * <p>Every {@code /v1/k/…} response carries {@code Referrer-Policy: no-referrer},
 * {@code X-Robots-Tag: noindex} and {@code Cache-Control: private} (controllers may refine the
 * latter: a feed key is always restricted so stays {@code private}; a tile key without data
 * restrictions gets the canonical path's own rule, {@code public} only while {@code openReads} covers
 * it, prompt 050). The key never appears in a response body, except in the follow-up URLs of the
 * documents prompt 058 generates for header-less clients (the MapLibre style, KML NetworkLink and icon
 * hrefs, a feed's own self link), which repeat the caller's own key ({@link #keyBase}) and are always
 * {@code Cache-Control: private}. For every request the filter also sets the request attribute {@value #LOG_URI_ATTRIBUTE}: the request URI with any path key replaced by its
 * first eight characters and {@code …} ({@link #redact}), which the Tomcat access-log pattern in
 * {@code application.yml} logs instead of the raw request line.
 */
@Component
public class PathKeyFilter extends OncePerRequestFilter implements Ordered {

    /** After CORS (preflights first), before {@code TokenFilter} ({@code CorsFilter.ORDER + 10}). */
    public static final int ORDER = CorsFilter.ORDER + 5;

    /** The path prefix of key-in-path URLs. */
    public static final String PREFIX = "/v1/k/";

    /** The path prefix where a feed key may arrive as {@code ?key=} (prompt 064). */
    public static final String QUERY_KEY_PREFIX = "/compat/celestrak/";

    /** The query parameter carrying a feed key under {@link #QUERY_KEY_PREFIX}. */
    public static final String QUERY_KEY_PARAM = "key";

    /** Set (to {@code true}) on a request authenticated by a {@code ?key=} feed key (prompt 064). */
    public static final String QUERY_KEY_ATTRIBUTE = "ai.foresignal.pathKey.query";

    /** The request attribute holding the redacted request URI, for the access log. */
    public static final String LOG_URI_ATTRIBUTE = "ai.foresignal.logUri";

    /** The request attribute holding the original (key-bearing) URI; never logged. */
    static final String ORIGINAL_URI_ATTRIBUTE = "ai.foresignal.pathKey.originalUri";

    /**
     * The request attribute holding {@code /v1/k/{key}} of a forwarded key-in-path request (prompt 058),
     * so documents that generate follow-up URLs (the MapLibre style, KML NetworkLinks) keep the caller on
     * the same key. Never logged; read through {@link #keyBase(HttpServletRequest)}.
     */
    public static final String KEY_BASE_ATTRIBUTE = "ai.foresignal.pathKey.base";

    private static final Pattern KEY_SEGMENT = Pattern.compile("(/v1/k/)([^/?#\\s]+)");

    /** Characters of a key kept by {@link #redact} (the same as the stored, listed key prefix). */
    public static final int REDACTED_KEEP = 8;

    /** Keys shorter than this are replaced entirely, so the kept characters never reveal most of one. */
    static final int REDACT_MIN_LENGTH = 16;

    /** The refusal codes used when no resolver owns the key's prefix (the feed-key defaults). */
    private static final PathKeyResolver DEFAULT_CODES = new PathKeyResolver() {
        @Override
        public String prefix() {
            return "";
        }

        @Override
        public Resolution resolve(String key) {
            return new Invalid();
        }
    };

    private final List<PathKeyResolver> resolvers;
    private final ProblemWriter problems;
    private final org.springframework.beans.factory.ObjectProvider<AuthFailureBackoff> backoff;

    /** Creates the filter. */
    public PathKeyFilter(List<PathKeyResolver> resolvers, ProblemWriter problems,
            org.springframework.beans.factory.ObjectProvider<AuthFailureBackoff> backoff) {
        this.resolvers = List.copyOf(resolvers);
        this.problems = problems;
        this.backoff = backoff;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    /**
     * {@code path} with any {@code /v1/k/{key}} segment replaced by the key's first eight characters
     * and {@code …} ({@code /v1/k/fsk_AbCd…/products/…}, prompt 050), or by {@code …} alone when the key
     * is shorter than {@value #REDACT_MIN_LENGTH} characters; null stays null.
     */
    public static String redact(String path) {
        if (path == null || !path.contains(PREFIX)) {
            return path;
        }
        return KEY_SEGMENT.matcher(path).replaceAll(m -> java.util.regex.Matcher.quoteReplacement(
                m.group(1) + keyHint(m.group(2))));
    }

    /** The redacted form of one key: its first eight characters and {@code …}, or {@code …} for a short key. */
    public static String keyHint(String key) {
        if (key == null || key.length() < REDACT_MIN_LENGTH) {
            return "…";
        }
        return key.substring(0, REDACTED_KEEP) + "…";
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
        String path = uri.substring(Math.min(contextPath.length(), uri.length()));
        request.setAttribute(LOG_URI_ATTRIBUTE, redact(uri));
        if (!path.startsWith(PREFIX)) {
            String queryKey = path.startsWith(QUERY_KEY_PREFIX) ? request.getParameter(QUERY_KEY_PARAM) : null;
            if (queryKey != null && !queryKey.isBlank()) {
                queryKey(request, response, chain, queryKey.strip(), path);
                return;
            }
            chain.doFilter(request, response);
            return;
        }
        hygiene(response);
        String afterPrefix = path.substring(PREFIX.length());
        int slash = afterPrefix.indexOf('/');
        String key = slash < 0 ? afterPrefix : afterPrefix.substring(0, slash);
        String rest = slash < 0 ? "" : afterPrefix.substring(slash);

        Optional<PathKeyResolver> resolver = resolvers.stream()
                .filter(r -> key.startsWith(r.prefix()))
                .findFirst();
        PathKeyResolver.Resolution resolution = resolver.isEmpty() || key.isEmpty()
                ? new PathKeyResolver.Invalid()
                : resolver.get().resolve(key);
        PathKeyResolver codes = resolver.orElse(DEFAULT_CODES);
        String ip = request.getRemoteAddr();
        switch (resolution) {
            case PathKeyResolver.Invalid invalid -> {
                if (!backedOff(ip, response)) {
                    refuse(response, HttpStatus.UNAUTHORIZED, codes.invalidCode(),
                            "The key in this URL is not valid.");
                }
                return;
            }
            case PathKeyResolver.Gone gone -> {
                if (!backedOff(ip, response)) {
                    refuse(response, codes.goneStatus(), codes.goneCode(),
                            "The key in this URL has expired or was revoked.");
                }
                return;
            }
            case PathKeyResolver.Valid valid ->
                    forward(request, response, chain, valid, codes, contextPath, rest, ip);
        }
    }

    private void forward(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            PathKeyResolver.Valid valid, PathKeyResolver codes, String contextPath, String rest, String ip)
            throws IOException, ServletException {
        AuthFailureBackoff b = backoff.getIfAvailable();
        if (b != null) {
            b.recordSuccess(ip);
        }
        String method = request.getMethod().toUpperCase(Locale.ROOT);
        if (!"GET".equals(method) && !"HEAD".equals(method)) {
            response.setHeader(HttpHeaders.ALLOW, "GET, HEAD");
            refuse(response, HttpStatus.METHOD_NOT_ALLOWED, ErrorCodes.METHOD_NOT_ALLOWED,
                    "Key-in-path URLs are read-only: GET and HEAD only.");
            return;
        }
        if (rest.isEmpty() || !safe(rest)) {
            refuse(response, HttpStatus.BAD_REQUEST, ErrorCodes.MALFORMED_REQUEST,
                    "The path after the key must be a plain API path.");
            return;
        }
        String canonical = "/v1" + rest;
        if (!allowed(canonical, valid.prefixes())) {
            refuse(response, HttpStatus.FORBIDDEN, codes.outsidePrefixCode(),
                    "This key is restricted to other paths.");
            return;
        }
        valid.principal().bind(request);
        request.setAttribute(KEY_BASE_ATTRIBUTE, PREFIX + key(request, contextPath));
        HttpServletRequest forwarded = new CanonicalPathRequest(request, contextPath, canonical);
        ServletRequestPathUtils.clearParsedRequestPath(forwarded);
        forwarded.removeAttribute(UrlPathHelper.PATH_ATTRIBUTE);
        chain.doFilter(forwarded, response);
    }

    /** Resolves a {@code ?key=} feed key under {@link #QUERY_KEY_PREFIX} (prompt 064) and continues unforwarded. */
    private void queryKey(HttpServletRequest request, HttpServletResponse response, FilterChain chain, String key,
            String path) throws IOException, ServletException {
        hygiene(response);
        Optional<PathKeyResolver> resolver = resolvers.stream()
                .filter(r -> !r.prefix().isEmpty() && key.startsWith(r.prefix()))
                .findFirst();
        PathKeyResolver.Resolution resolution = resolver.isEmpty()
                || !key.startsWith(ai.foresignal.security.feed.FeedKeyService.KEY_PREFIX)
                ? new PathKeyResolver.Invalid()
                : resolver.get().resolve(key);
        PathKeyResolver codes = resolver.orElse(DEFAULT_CODES);
        String ip = request.getRemoteAddr();
        switch (resolution) {
            case PathKeyResolver.Invalid invalid -> {
                if (!backedOff(ip, response)) {
                    refuse(response, HttpStatus.UNAUTHORIZED, codes.invalidCode(), "The key in this URL is not valid.");
                }
            }
            case PathKeyResolver.Gone gone -> {
                if (!backedOff(ip, response)) {
                    refuse(response, codes.goneStatus(), codes.goneCode(),
                            "The key in this URL has expired or was revoked.");
                }
            }
            case PathKeyResolver.Valid valid -> {
                AuthFailureBackoff b = backoff.getIfAvailable();
                if (b != null) {
                    b.recordSuccess(ip);
                }
                String method = request.getMethod().toUpperCase(Locale.ROOT);
                if (!"GET".equals(method) && !"HEAD".equals(method)) {
                    response.setHeader(HttpHeaders.ALLOW, "GET, HEAD");
                    refuse(response, HttpStatus.METHOD_NOT_ALLOWED, ErrorCodes.METHOD_NOT_ALLOWED,
                            "Key URLs are read-only: GET and HEAD only.");
                    return;
                }
                if (!safe(path) || !allowed(path, valid.prefixes())) {
                    refuse(response, HttpStatus.FORBIDDEN, codes.outsidePrefixCode(),
                            "This key is restricted to other paths.");
                    return;
                }
                valid.principal().bind(request);
                request.setAttribute(QUERY_KEY_ATTRIBUTE, Boolean.TRUE);
                chain.doFilter(request, response);
            }
        }
    }

    /**
     * {@code /v1/k/{key}} when {@code request} came in through a key-in-path URL (prompt 058), else null.
     * Generated URLs append the canonical path without its {@code /v1} ({@code /v1/k/{key}/tiles/…}).
     */
    public static String keyBase(HttpServletRequest request) {
        Object value = request == null ? null : request.getAttribute(KEY_BASE_ATTRIBUTE);
        return value instanceof String s ? s : null;
    }

    /** The key segment of a {@code /v1/k/{key}/…} request URI. */
    private static String key(HttpServletRequest request, String contextPath) {
        String uri = request.getRequestURI();
        String afterPrefix = uri.substring(Math.min(contextPath.length() + PREFIX.length(), uri.length()));
        int slash = afterPrefix.indexOf('/');
        return slash < 0 ? afterPrefix : afterPrefix.substring(0, slash);
    }

    /** True when {@code canonical} starts with one of {@code prefixes} on a segment boundary. */
    static boolean allowed(String canonical, List<String> prefixes) {
        for (String prefix : prefixes) {
            if (prefix.endsWith("/")) {
                if (canonical.startsWith(prefix)) {
                    return true;
                }
            } else if (canonical.equals(prefix) || canonical.startsWith(prefix + "/")
                    || canonical.startsWith(prefix + ".")) {
                // "/v1/tiles/quakes" also covers "/v1/tiles/quakes.json" and "/v1/tiles/quakes/…".
                return true;
            }
        }
        return false;
    }

    /** False for dot segments, empty segments, backslashes and encoded separators or dots. */
    static boolean safe(String rest) {
        String lower = rest.toLowerCase(Locale.ROOT);
        if (lower.contains("%2f") || lower.contains("%5c") || lower.contains("%2e") || lower.contains("\\")
                || lower.contains(";")) {
            return false;
        }
        for (String segment : rest.substring(1).split("/", -1)) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Counts a failed key for the client address; true when the address is in backoff and a
     * {@code 429} was written instead of the normal refusal.
     */
    private boolean backedOff(String ip, HttpServletResponse response) throws IOException {
        AuthFailureBackoff b = backoff.getIfAvailable();
        if (b == null) {
            return false;
        }
        Optional<Duration> retry = b.recordFailure(ip);
        if (retry.isEmpty()) {
            return false;
        }
        response.setHeader(HttpHeaders.RETRY_AFTER, Long.toString(AuthFailureBackoff.seconds(retry.get())));
        refuse(response, HttpStatus.TOO_MANY_REQUESTS, ErrorCodes.RATE_LIMITED,
                "Too many failed credentials from this address; retry later.");
        return true;
    }

    private void refuse(HttpServletResponse response, HttpStatus status, String code, String detail)
            throws IOException {
        problems.write(response, problems.problem(status, code, detail));
    }

    /** The headers every key-in-path response carries. */
    static void hygiene(HttpServletResponse response) {
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("X-Robots-Tag", "noindex");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "private");
    }

    /** The request as seen at its canonical path; everything else (query, headers, body) unchanged. */
    static final class CanonicalPathRequest extends HttpServletRequestWrapper {

        private final String contextPath;
        private final String canonical;

        CanonicalPathRequest(HttpServletRequest request, String contextPath, String canonical) {
            super(request);
            this.contextPath = contextPath;
            this.canonical = canonical;
        }

        @Override
        public String getRequestURI() {
            return contextPath + canonical;
        }

        @Override
        public StringBuffer getRequestURL() {
            HttpServletRequest r = (HttpServletRequest) getRequest();
            StringBuffer url = new StringBuffer();
            url.append(r.getScheme()).append("://").append(r.getServerName());
            int port = r.getServerPort();
            boolean defaultPort = ("http".equalsIgnoreCase(r.getScheme()) && port == 80)
                    || ("https".equalsIgnoreCase(r.getScheme()) && port == 443);
            if (!defaultPort && port > 0) {
                url.append(':').append(port);
            }
            return url.append(getRequestURI());
        }

        @Override
        public String getServletPath() {
            return canonical;
        }

        @Override
        public String getPathInfo() {
            return null;
        }
    }
}
