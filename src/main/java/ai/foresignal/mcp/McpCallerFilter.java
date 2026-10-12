package ai.foresignal.mcp;

import ai.foresignal.config.CorsFilter;
import ai.foresignal.security.TokenFilter;
import ai.foresignal.settings.CorsSettingsService;
import io.modelcontextprotocol.spec.HttpHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Runs in front of the MCP transports, after {@link TokenFilter} has authenticated the request
 * (prompt 025):
 *
 * <ol>
 *   <li>refuses a browser {@code Origin} that is neither this host nor a CORS write origin (the MCP
 *       specification's DNS-rebinding protection), with {@code 403};
 *   <li>resolves the {@link McpCaller} and stores it as a request attribute, where the transports'
 *       context extractor finds it;
 *   <li>refuses a streamable {@code Mcp-Session-Id} that was created by a different caller, with
 *       {@code 403};
 *   <li>runs the request inside {@link McpSessions#open}, so a session the SDK opens is bound to
 *       this caller.
 * </ol>
 *
 * <p>Refusals carry a JSON-RPC error body, the shape MCP clients read.
 */
@Component
public class McpCallerFilter extends OncePerRequestFilter implements Ordered {

    /** After the token filter. */
    public static final int ORDER = TokenFilter.ORDER + 10;

    /** The JSON-RPC "invalid request" code. */
    static final int INVALID_REQUEST = -32600;

    private final McpCallerResolver callers;
    private final McpSessions sessions;
    private final CorsSettingsService cors;
    private final JsonMapper json;

    /** Creates the filter. */
    public McpCallerFilter(McpCallerResolver callers, McpSessions sessions, CorsSettingsService cors,
            JsonMapper json) {
        this.callers = callers;
        this.sessions = sessions;
        this.cors = cors;
        this.json = json;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    /** True for {@code /mcp} and everything below it. */
    public static boolean isMcpPath(String path) {
        return path != null && (path.equals("/mcp") || path.equals("/mcp/") || path.startsWith("/mcp/"));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !isMcpPath(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String origin = request.getHeader(org.springframework.http.HttpHeaders.ORIGIN);
        if (origin != null && !originAllowed(origin, request)) {
            refuse(response, "The Origin " + origin + " may not use this MCP server. Add it to the CORS write"
                    + " origins (PUT /v1/settings/cors) if this browser client is yours.");
            return;
        }
        McpCaller caller = callers.resolve(request);
        request.setAttribute(McpCaller.CONTEXT_KEY, caller);
        String sessionId = request.getHeader(HttpHeaders.MCP_SESSION_ID);
        Optional<McpSessions.Binding> binding = sessions.binding(sessionId);
        if (binding.isPresent() && !binding.get().callerKey().equals(caller.key())) {
            refuse(response, "This MCP session was opened with a different token. Initialize a new session with"
                    + " the token you are presenting.");
            return;
        }
        try (McpSessions.CallerScope ignored = sessions.open(caller)) {
            chain.doFilter(request, response);
        }
    }

    /** Same host as the request, or listed among the CORS write origins. */
    boolean originAllowed(String origin, HttpServletRequest request) {
        String host = request.getHeader(org.springframework.http.HttpHeaders.HOST);
        try {
            URI uri = URI.create(origin.strip());
            String authority = uri.getRawAuthority();
            if (host != null && authority != null && authority.equalsIgnoreCase(host.strip())) {
                return true;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        return CorsFilter.isAllowed(origin, cors.get().writeOrigins());
    }

    private void refuse(HttpServletResponse response, String message) throws IOException {
        ObjectNode error = json.createObjectNode();
        error.put("code", INVALID_REQUEST);
        error.put("message", message);
        ObjectNode body = json.createObjectNode();
        body.put("jsonrpc", "2.0");
        body.putNull("id");
        body.set("error", error);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(body.toString());
    }
}
