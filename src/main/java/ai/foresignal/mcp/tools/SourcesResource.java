package ai.foresignal.mcp.tools;

import ai.foresignal.mcp.ForesignalMcpResource;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import org.springframework.stereotype.Component;

/** The resource {@code foresignal://sources}: the sources the caller may see, as {@code list_sources} short. */
@Component
public class SourcesResource implements ForesignalMcpResource {

    private final ListSourcesTool sources;

    /** Creates the resource. */
    public SourcesResource(ListSourcesTool sources) {
        this.sources = sources;
    }

    @Override
    public String uri() {
        return "foresignal://sources";
    }

    @Override
    public String name() {
        return "sources";
    }

    @Override
    public String description() {
        return "Foresignal's data sources with id, name, kinds, enabled flag, health status and last data time.";
    }

    @Override
    public String read(ToolContext context) {
        return sources.list(context.caller(), Detail.LONG).toString();
    }
}
