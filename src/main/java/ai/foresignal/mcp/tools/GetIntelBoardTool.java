package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.cycle.IntelBoardService;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code get_intel_board} (read, prompt 128): the ≤ 8 KB summary of the intelligence layer. */
@RestrictedRead("the board carries no record content; private objects need private access")
@Component
public class GetIntelBoardTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props());

    private final IntelBoardService board;
    private final IntelVisibility visibility;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetIntelBoardTool(IntelBoardService board, IntelVisibility visibility, JsonMapper json) {
        this.board = board;
        this.visibility = visibility;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_intel_board";
    }

    @Override
    public String title() {
        return "Get intel board";
    }

    @Override
    public String description() {
        return "Returns the intel board: the top situations, hypothesis band changes and observed indicators of the"
                + " last 24 hours, forecasts due in 7 days, the latest assessment of each cadence, the ensemble's"
                + " 90-day calibration next to the operator's, and benchmark status. Read it before answering"
                + " questions about the current situation picture.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The intel board");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        return ToolResult.json(json.readTree(board.board(visibility.seesPrivate(context.caller())).body()));
    }
}
