package ai.foresignal.mcp.tools;

import ai.foresignal.mcp.ForesignalMcpResource;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import org.springframework.stereotype.Component;

/** The resource {@code foresignal://kinds}: the kinds currently stored, with counts, as {@code list_kinds}. */
@Component
public class KindsResource implements ForesignalMcpResource {

    private final ListKindsTool kinds;

    /** Creates the resource. */
    public KindsResource(ListKindsTool kinds) {
        this.kinds = kinds;
    }

    @Override
    public String uri() {
        return "foresignal://kinds";
    }

    @Override
    public String name() {
        return "kinds";
    }

    @Override
    public String description() {
        return "The kinds of record currently stored, each with its count of current records and its sources.";
    }

    @Override
    public String read(ToolContext context) {
        return kinds.list(context.excludedSources(), Detail.LONG).toString();
    }
}
