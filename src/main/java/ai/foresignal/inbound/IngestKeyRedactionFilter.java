package ai.foresignal.inbound;

import ai.foresignal.security.restrict.PathKeyFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Keeps keys carried in ingest paths ({@code /v1/ingest/{sourceId}/k/{key}/{format}}, prompt 039) out
 * of every log, audit entry, problem {@code instance} and fetch log.
 *
 * <p>Runs right after {@link PathKeyFilter} and before {@code TokenFilter}. On a key-in-path ingest
 * URL it stores the key under {@link #KEY_ATTRIBUTE} (read only by {@code IngestAuthenticator}),
 * overwrites the access-log URI attribute ({@link PathKeyFilter#LOG_URI_ATTRIBUTE}) with the redacted
 * path, and hands the rest of the chain a request whose URI, URL and servlet path carry
 * {@link IngestUrls#REDACTED_KEY} instead of the key. Routing is unaffected: the {@code {key}} path
 * variable simply reads {@code redacted}. The query string is never part of the access log.
 */
@Component
public class IngestKeyRedactionFilter extends OncePerRequestFilter implements Ordered {

    /** Right after {@link PathKeyFilter}, before {@code TokenFilter}. */
    public static final int ORDER = PathKeyFilter.ORDER + 1;

    /** The request attribute holding the key taken from the path; never logged. */
    public static final String KEY_ATTRIBUTE = "ai.foresignal.ingest.pathKey";

    @Override
    public int getOrder() {
        return ORDER;
    }

    /** The key a request carried in its ingest path, or null. */
    public static String pathKey(HttpServletRequest request) {
        Object value = request.getAttribute(KEY_ATTRIBUTE);
        return value instanceof String key ? key : null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        String key = IngestUrls.pathKey(uri);
        if (key == null) {
            chain.doFilter(request, response);
            return;
        }
        request.setAttribute(KEY_ATTRIBUTE, key);
        String redacted = IngestUrls.redactPath(uri);
        request.setAttribute(PathKeyFilter.LOG_URI_ATTRIBUTE, redacted);
        chain.doFilter(new Redacted(request), response);
    }

    /** A request whose path no longer carries the key. */
    static final class Redacted extends HttpServletRequestWrapper {

        Redacted(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getRequestURI() {
            return IngestUrls.redactPath(super.getRequestURI());
        }

        @Override
        public StringBuffer getRequestURL() {
            StringBuffer url = super.getRequestURL();
            return url == null ? null : new StringBuffer(IngestUrls.redactPath(url.toString()));
        }

        @Override
        public String getServletPath() {
            return IngestUrls.redactPath(super.getServletPath());
        }

        @Override
        public String getPathInfo() {
            return IngestUrls.redactPath(super.getPathInfo());
        }
    }
}
