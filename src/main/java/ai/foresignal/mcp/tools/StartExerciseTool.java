package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.bool;
import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.exercise.ExerciseRunEntity;
import ai.foresignal.exercise.ExerciseService;
import ai.foresignal.exercise.ExerciseViews;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code start_exercise} (agent; prompt 115): starts an exercise replay, or projects one with {@code dryRun}, with
 * the validation of {@code createExercise}. Exercise alerts reach only channels that opted in or are named.
 */
@Component
@ai.foresignal.security.restrict.RestrictedRead
public class StartExerciseTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "name", string("A name for the run", 1, ExerciseService.MAX_NAME),
            "ruleIds", stringArray("Rules to replay (or give tag)", 100),
            "tag", string("Replay every rule with this tag (or give ruleIds)", 1, 64),
            "from", dateTime("Start of the replayed window (with to)"),
            "to", dateTime("End of the replayed window (with from); at most 7 days after from"),
            "speed", enumeration("Pacing between matches", "x60", "realtime", "x10", "x60", "max"),
            "channelIds", stringArray("Channels to deliver to, besides those with acceptExercise", 50),
            "enrich", bool("Allow alert enrichment of the exercise alerts", Boolean.FALSE),
            "dryRun", bool("Return projected counts per rule and channel without delivering", Boolean.FALSE)),
            "name", "from", "to");

    private final ExerciseService exercises;
    private final JsonMapper json;

    /** Creates the tool. */
    public StartExerciseTool(ExerciseService exercises, JsonMapper json) {
        this.exercises = exercises;
        this.json = json;
    }

    @Override
    public String name() {
        return "start_exercise";
    }

    @Override
    public String title() {
        return "Start an exercise replay";
    }

    @Override
    public String description() {
        return "Replays a retained window (at most 7 days) through selected rules as [EXERCISE] alerts delivered only"
                + " to channels that opted in (acceptExercise) or are named; real alert state and statistics are never"
                + " touched. dryRun returns projected counts. One run at a time. Needs an agent token.";
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
    public boolean alwaysAuthenticated() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        try {
            ExerciseRunEntity run = exercises.create(args, context.actor());
            boolean dryRun = args != null && args.path("dryRun").asBoolean(false);
            return ToolResult.json(json.valueToTree(ExerciseViews.run(run, dryRun)));
        } catch (ForesignalException e) {
            return ToolResult.error(e.getMessage());
        }
    }
}
