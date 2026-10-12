package ai.foresignal.mcp;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * MCP server mechanics ({@code foresignal.mcp.*}, prompt 025).
 *
 * @param enabled whether the {@code /mcp} servlet is registered at all
 * @param legacySse the older HTTP+SSE transport (protocol 2024-11-05) for clients that predate
 *     streamable HTTP
 * @param requestTimeout how long a session waits for a client's answer to a server request
 * @param maxRequestBytes the largest JSON-RPC message accepted
 */
@ConfigurationProperties("foresignal.mcp")
public record McpProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue LegacySse legacySse,
        @DefaultValue("60s") Duration requestTimeout,
        @DefaultValue("1048576") int maxRequestBytes) {

    /** Fills missing values with the defaults. */
    public McpProperties {
        legacySse = legacySse == null ? new LegacySse(false) : legacySse;
        requestTimeout = requestTimeout == null ? Duration.ofSeconds(60) : requestTimeout;
        maxRequestBytes = maxRequestBytes <= 0 ? 1_048_576 : maxRequestBytes;
    }

    /** The shipped defaults: streamable HTTP only. */
    public static McpProperties defaults() {
        return new McpProperties(true, new LegacySse(false), Duration.ofSeconds(60), 1_048_576);
    }

    /**
     * {@code foresignal.mcp.legacy-sse.*}.
     *
     * @param enabled when true, {@code /mcp/sse} and {@code /mcp/message} are registered next to
     *     {@code /mcp}, sharing its tools and authentication; for older clients only
     */
    public record LegacySse(@DefaultValue("false") boolean enabled) {}
}
