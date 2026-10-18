package ai.foresignal.mcp.oauth;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.McpCallerFilter;
import ai.foresignal.mcp.McpProperties;
import ai.foresignal.mcp.McpToolRegistry;
import ai.foresignal.security.McpOAuthGate;
import ai.foresignal.security.Scope;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * The MCP authorization specification's step-up answer for identity-provider tokens (prompt 141): a
 * {@code tools/call} for a tool whose scope is above the token's mapped scope is refused at the HTTP layer with
 * {@code 403} and {@code WWW-Authenticate: Bearer error="insufficient_scope", scope="…", resource_metadata="…"}, so
 * the client can ask the identity provider for the wider scope and retry.
 *
 * <p>Applies only to {@code POST /mcp} requests that {@code TokenFilter} accepted with an identity-provider token
 * ({@link McpOAuthGate#ATTRIBUTE}). The JSON-RPC body is read once (up to {@code foresignal.mcp.max-request-bytes})
 * and handed on unchanged; a single message or a batch is inspected, and a body that is not JSON passes through for
 * the transport to answer. Foresignal API tokens keep the 025 behavior (a tool error naming the scope), because a
 * Foresignal token cannot be stepped up; {@code McpToolRegistry} still checks every call either way.
 */
@Component
public class McpStepUpFilter extends OncePerRequestFilter implements Ordered {

    /** After {@link McpCallerFilter} (Origin and session checks first). */
    public static final int ORDER = McpCallerFilter.ORDER + 10;

    /** The JSON-RPC error code of the refusal body (MCP clients read the HTTP status and header). */
    static final int INVALID_REQUEST = -32600;

    private final ObjectProvider<McpToolRegistry> registry;
    private final ObjectProvider<McpProperties> properties;
    private final McpOAuthGate gate;
    private final JsonMapper json;

    /** Creates the filter. */
    public McpStepUpFilter(ObjectProvider<McpToolRegistry> registry, ObjectProvider<McpProperties> properties,
            McpOAuthGate gate, JsonMapper json) {
        this.registry = registry;
        this.properties = properties;
        this.gate = gate;
        this.json = json;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod()) || !McpCallerFilter.isMcpPath(request.getRequestURI())
                || !(request.getAttribute(McpOAuthGate.ATTRIBUTE) instanceof McpOAuthGate.Accepted);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        McpOAuthGate.Accepted accepted = (McpOAuthGate.Accepted) request.getAttribute(McpOAuthGate.ATTRIBUTE);
        int max = properties.getIfAvailable(McpProperties::defaults).maxRequestBytes();
        byte[] body = request.getInputStream().readNBytes(max + 1);
        boolean complete = body.length <= max;
        CachedBodyRequest wrapped = new CachedBodyRequest(request, body, complete);
        if (complete) {
            Optional<Refusal> refusal = refusal(body, accepted.token().scope());
            if (refusal.isPresent()) {
                refuse(request, response, refusal.get());
                return;
            }
        }
        chain.doFilter(wrapped, response);
    }

    /**
     * A tool call in {@code body} needing more than {@code scope}.
     *
     * @param tool the tool
     * @param required its scope
     * @param id the JSON-RPC id of the call, or null
     */
    record Refusal(String tool, Scope required, JsonNode id) {}

    /** The first {@code tools/call} in {@code body} above {@code scope}, or empty. */
    Optional<Refusal> refusal(byte[] body, Scope scope) {
        McpToolRegistry tools = registry.getIfAvailable();
        if (tools == null || body.length == 0) {
            return Optional.empty();
        }
        JsonNode root;
        try {
            root = json.readTree(body);
        } catch (JacksonException e) {
            return Optional.empty();
        }
        if (root == null) {
            return Optional.empty();
        }
        if (root.isArray()) {
            for (JsonNode message : root) {
                Optional<Refusal> found = check(message, scope, tools);
                if (found.isPresent()) {
                    return found;
                }
            }
            return Optional.empty();
        }
        return check(root, scope, tools);
    }

    private static Optional<Refusal> check(JsonNode message, Scope scope, McpToolRegistry tools) {
        if (message == null || !message.isObject()) {
            return Optional.empty();
        }
        JsonNode method = message.get("method");
        if (method == null || !method.isString() || !"tools/call".equals(method.asString())) {
            return Optional.empty();
        }
        JsonNode params = message.get("params");
        JsonNode name = params == null ? null : params.get("name");
        if (name == null || !name.isString()) {
            return Optional.empty();
        }
        Optional<ForesignalMcpTool> tool = tools.tool(name.asString());
        if (tool.isEmpty() || scope.includes(tool.get().requiredScope())) {
            return Optional.empty();
        }
        return Optional.of(new Refusal(tool.get().name(), tool.get().requiredScope(), message.get("id")));
    }

    private void refuse(HttpServletRequest request, HttpServletResponse response, Refusal refusal) throws IOException {
        ObjectNode error = json.createObjectNode();
        error.put("code", INVALID_REQUEST);
        error.put("message", "The tool " + refusal.tool() + " needs the " + refusal.required().wireValue()
                + " scope (insufficient_scope); ask the identity provider for a token with the scope named in"
                + " WWW-Authenticate and retry.");
        ObjectNode body = json.createObjectNode();
        body.put("jsonrpc", "2.0");
        if (refusal.id() == null) {
            body.putNull("id");
        } else {
            body.set("id", refusal.id());
        }
        body.set("error", error);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, gate.insufficientScope(request, refusal.required()));
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(body.toString());
    }

    /**
     * A request whose body has already been read (in part), served again: the bytes read from memory, then, for a
     * body longer than the inspection limit, the rest of the original stream (so the transport applies its own
     * size limit to the whole body).
     */
    static final class CachedBodyRequest extends HttpServletRequestWrapper {

        private final byte[] body;
        private final boolean complete;
        private final java.io.InputStream stream;

        CachedBodyRequest(HttpServletRequest request, byte[] body, boolean complete) throws IOException {
            super(request);
            this.body = body;
            this.complete = complete;
            this.stream = complete ? new ByteArrayInputStream(body)
                    : new java.io.SequenceInputStream(new ByteArrayInputStream(body), request.getInputStream());
        }

        @Override
        public ServletInputStream getInputStream() {
            java.io.InputStream in = stream;
            return new ServletInputStream() {
                private boolean finished;

                @Override
                public boolean isFinished() {
                    return finished;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener listener) {
                    try {
                        listener.onDataAvailable();
                        listener.onAllDataRead();
                    } catch (IOException e) {
                        listener.onError(e);
                    }
                }

                @Override
                public int read() throws IOException {
                    int b = in.read();
                    finished = b < 0;
                    return b;
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    int n = in.read(b, off, len);
                    finished = n < 0;
                    return n;
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            String encoding = getCharacterEncoding();
            Charset charset = encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding);
            return new BufferedReader(new InputStreamReader(stream, charset));
        }

        @Override
        public int getContentLength() {
            return complete ? body.length : super.getContentLength();
        }

        @Override
        public long getContentLengthLong() {
            return complete ? body.length : super.getContentLengthLong();
        }
    }
}
