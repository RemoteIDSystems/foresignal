package ai.foresignal.testsupport.mcp;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.testsupport.trigger.TriggerTestSources;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * The MCP test context (prompt 025): the trigger sources of {@link TriggerTestSources} (records,
 * private source, trigger engine) on a real port, plus two test tool beans, so the registry picks
 * up beans declared anywhere: {@code test_cost} (cost-bearing) and {@code test_image} (an image
 * result). Every MCP integration test imports this configuration.
 */
@TestConfiguration
@Import(TriggerTestSources.class)
public class McpTestSources {

    /** {@code test_cost}. */
    @Bean
    public ForesignalMcpTool testCostTool() {
        return McpTestTools.costBearing();
    }

    /** {@code test_image}. */
    @Bean
    public ForesignalMcpTool testImageTool() {
        return McpTestTools.image();
    }
}
