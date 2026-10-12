package ai.foresignal.mcp;

import ai.foresignal.config.ForesignalVersion;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.json.schema.JsonSchemaValidator;
import io.modelcontextprotocol.json.schema.jackson3.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.server.McpTransportContextExtractor;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpStreamableServerSession;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

/**
 * Wires the MCP server (Architecture §18, prompt 025) from the MCP Java SDK core: the SDK's servlet
 * transports carry the protocol, {@link McpProtocol} answers it, and every session the SDK opens is
 * bound to its creator through {@link McpSessions}.
 *
 * <ul>
 *   <li><strong>Streamable HTTP</strong> at {@code /mcp} ({@code HttpServletStreamableServerTransportProvider};
 *       protocols 2024-11-05 to 2025-11-25), unless {@code foresignal.mcp.enabled=false}.
 *   <li><strong>Legacy HTTP+SSE</strong> ({@code HttpServletSseServerTransportProvider}, protocol
 *       2024-11-05) at {@code /mcp/sse} with messages at {@code /mcp/message}, only with
 *       {@code foresignal.mcp.legacy-sse.enabled=true}; for older clients.
 * </ul>
 *
 * <p>The SDK's Spring transports moved to Spring AI 2.x, which Foresignal does not use; the servlets
 * are registered directly with {@link ServletRegistrationBean}s, behind the same filters as the
 * API ({@code TokenFilter}, then {@link McpCallerFilter}).
 */
@Configuration
public class McpServerConfiguration {

    /** The streamable endpoint. */
    public static final String STREAMABLE_PATH = "/mcp";

    /** The legacy event-stream endpoint. */
    public static final String LEGACY_SSE_PATH = "/mcp/sse";

    /** The legacy message endpoint. */
    public static final String LEGACY_MESSAGE_PATH = "/mcp/message";

    /** The SDK's JSON binding over Foresignal's Jackson 3 mapper. */
    @Bean
    public McpJsonMapper mcpJsonMapper(JsonMapper json) {
        return new JacksonMcpJsonMapper(json);
    }

    /** The JSON Schema validator for tool arguments (networknt, 2020-12). */
    @Bean
    public JsonSchemaValidator mcpJsonSchemaValidator(JsonMapper json) {
        return new DefaultJsonSchemaValidator(json);
    }

    /** The protocol handlers shared by both transports. */
    @Bean
    public McpProtocol mcpProtocol(McpToolRegistry tools, List<ForesignalMcpResource> resources,
            List<ForesignalMcpPrompt> prompts, McpSessions sessions, McpJsonMapper mcpJsonMapper,
            ForesignalVersion version) {
        return new McpProtocol(tools, resources, prompts, sessions, mcpJsonMapper, version);
    }

    /** Puts the request's {@link McpCaller} into the SDK transport context. */
    static McpTransportContextExtractor<HttpServletRequest> contextExtractor(McpCallerResolver callers) {
        return request -> McpTransportContext.create(Map.of(McpCaller.CONTEXT_KEY, callers.resolve(request)));
    }

    /** The streamable HTTP transport, with Foresignal's session factory. */
    @Bean(destroyMethod = "")
    @ConditionalOnProperty(prefix = "foresignal.mcp", name = "enabled", havingValue = "true", matchIfMissing = true)
    public HttpServletStreamableServerTransportProvider mcpStreamableTransport(McpProtocol protocol,
            McpSessions sessions, McpCallerResolver callers, McpJsonMapper mcpJsonMapper,
            JsonSchemaValidator mcpJsonSchemaValidator, McpProperties properties) {
        HttpServletStreamableServerTransportProvider provider = HttpServletStreamableServerTransportProvider.builder()
                .jsonMapper(mcpJsonMapper)
                .mcpEndpoint(STREAMABLE_PATH)
                .contextExtractor(contextExtractor(callers))
                .maxRequestSize(properties.maxRequestBytes())
                .build();
        List<String> versions = provider.protocolVersions();
        provider.setSessionFactory(initialize -> {
            String id = UUID.randomUUID().toString();
            sessions.bind(id, McpSessions.STREAMABLE);
            McpStreamableServerSession session = new McpStreamableServerSession(id, initialize.capabilities(),
                    initialize.clientInfo(), properties.requestTimeout(),
                    protocol.requestHandlers(McpSessions.STREAMABLE), protocol.notificationHandlers(),
                    () -> Mono.fromRunnable(() -> sessions.remove(id)), mcpJsonSchemaValidator);
            return new McpStreamableServerSession.McpStreamableServerSessionInit(session,
                    protocol.initialize(initialize, versions));
        });
        return provider;
    }

    /** Registers the streamable transport at {@code /mcp}. */
    @Bean
    @ConditionalOnProperty(prefix = "foresignal.mcp", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ServletRegistrationBean<HttpServletStreamableServerTransportProvider> mcpStreamableServlet(
            HttpServletStreamableServerTransportProvider mcpStreamableTransport) {
        ServletRegistrationBean<HttpServletStreamableServerTransportProvider> registration =
                new ServletRegistrationBean<>(mcpStreamableTransport, STREAMABLE_PATH);
        registration.setName("mcpStreamable");
        registration.setAsyncSupported(true);
        registration.setLoadOnStartup(1);
        return registration;
    }

    /** The legacy HTTP+SSE transport, only when {@code foresignal.mcp.legacy-sse.enabled=true}. */
    @Bean(destroyMethod = "")
    @ConditionalOnProperty(prefix = "foresignal.mcp.legacy-sse", name = "enabled", havingValue = "true")
    public HttpServletSseServerTransportProvider mcpLegacySseTransport(McpProtocol protocol, McpSessions sessions,
            McpCallerResolver callers, McpJsonMapper mcpJsonMapper, JsonSchemaValidator mcpJsonSchemaValidator,
            McpProperties properties) {
        if (!properties.enabled()) {
            throw new IllegalStateException(
                    "foresignal.mcp.legacy-sse.enabled=true needs the MCP server (foresignal.mcp.enabled=true)");
        }
        HttpServletSseServerTransportProvider provider = HttpServletSseServerTransportProvider.builder()
                .jsonMapper(mcpJsonMapper)
                .sseEndpoint(LEGACY_SSE_PATH)
                .messageEndpoint(LEGACY_MESSAGE_PATH)
                .contextExtractor(contextExtractor(callers))
                .maxRequestSize(properties.maxRequestBytes())
                .build();
        List<String> versions = provider.protocolVersions();
        provider.setSessionFactory(transport -> {
            String id = UUID.randomUUID().toString();
            sessions.bind(id, McpSessions.LEGACY_SSE);
            return new McpServerSession(id, properties.requestTimeout(), transport,
                    initialize -> protocol.initialize(initialize, versions),
                    protocol.requestHandlers(McpSessions.LEGACY_SSE), protocol.notificationHandlers(),
                    () -> Mono.fromRunnable(() -> sessions.remove(id)), mcpJsonSchemaValidator);
        });
        return provider;
    }

    /** Registers the legacy transport at {@code /mcp/sse} and {@code /mcp/message}. */
    @Bean
    @ConditionalOnProperty(prefix = "foresignal.mcp.legacy-sse", name = "enabled", havingValue = "true")
    public ServletRegistrationBean<HttpServletSseServerTransportProvider> mcpLegacySseServlet(
            HttpServletSseServerTransportProvider mcpLegacySseTransport) {
        ServletRegistrationBean<HttpServletSseServerTransportProvider> registration =
                new ServletRegistrationBean<>(mcpLegacySseTransport, LEGACY_SSE_PATH, LEGACY_MESSAGE_PATH);
        registration.setName("mcpLegacySse");
        registration.setAsyncSupported(true);
        registration.setLoadOnStartup(1);
        return registration;
    }
}
