package ai.foresignal.mcp;

import ai.foresignal.system.SystemBlockContributor;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The MCP server's state for {@code /v1/system} (prompt 025): the {@code mcp} block
 * ({@code SystemInfo.mcp}: {@code enabled}, {@code transports.streamable},
 * {@code transports.legacySse}, active {@code sessions}, registered {@code tools}).
 *
 * <p>The {@code features.mcp} flag is {@link McpFeature}, kept separate because feature
 * contributors are injected eagerly into {@code SystemInfoService}, which the {@code system_status}
 * tool depends on.
 */
@Component
public class McpStatus implements SystemBlockContributor {

    private final McpProperties properties;
    private final McpSessions sessions;
    private final McpToolRegistry tools;

    /** Creates the contributor. */
    public McpStatus(McpProperties properties, McpSessions sessions, McpToolRegistry tools) {
        this.properties = properties;
        this.sessions = sessions;
        this.tools = tools;
    }

    @Override
    public String key() {
        return "mcp";
    }

    @Override
    public Map<String, Object> block() {
        Map<String, Object> transports = new LinkedHashMap<>();
        transports.put("streamable", properties.enabled());
        transports.put("legacySse", properties.enabled() && properties.legacySse().enabled());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("enabled", properties.enabled());
        out.put("transports", transports);
        out.put("sessions", sessions.count());
        out.put("tools", tools.size());
        return out;
    }
}
