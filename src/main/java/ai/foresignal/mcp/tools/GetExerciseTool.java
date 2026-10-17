package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.exercise.ExerciseService;
import ai.foresignal.exercise.ExerciseViews;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** {@code get_exercise} (agent; prompt 115): the status and statistics of an exercise run. */
@Component
@ai.foresignal.security.restrict.RestrictedRead
public class GetExerciseTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props("exerciseId", string("The exercise run id", 26, 26)),
            "exerciseId");

    private final ExerciseService exercises;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetExerciseTool(ExerciseService exercises, JsonMapper json) {
        this.exercises = exercises;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_exercise";
    }

    @Override
    public String title() {
        return "Exercise run status";
    }

    @Override
    public String description() {
        return "The status and statistics of an exercise replay: versions scanned, alerts by rule, notifications by"
                + " channel, matches suppressed by cooldown or rate cap, skipped rules and caveats. Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("An ExerciseRun");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public boolean readOnlyHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        try {
            return ToolResult.json(json.valueToTree(ExerciseViews.run(
                    exercises.get(McpArgs.requiredText(args, "exerciseId")), false)));
        } catch (ForesignalException e) {
            return ToolResult.error(e.getMessage());
        }
    }
}
