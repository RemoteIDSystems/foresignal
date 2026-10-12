package ai.foresignal.mcp;

import ai.foresignal.system.FeatureContributor;
import java.util.Map;
import org.springframework.stereotype.Component;

/** {@code /v1/system.features.mcp} (prompt 025): true while the MCP server is enabled. */
@Component
public class McpFeature implements FeatureContributor {

    private final McpProperties properties;

    /** Creates the contributor. */
    public McpFeature(McpProperties properties) {
        this.properties = properties;
    }

    @Override
    public Map<String, Boolean> features() {
        return Map.of("mcp", properties.enabled());
    }
}
