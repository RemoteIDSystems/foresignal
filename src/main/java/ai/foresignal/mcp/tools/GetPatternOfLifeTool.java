package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.pol.PolService;
import ai.foresignal.pol.PolSubjectEntity;
import ai.foresignal.pol.PolViews;
import ai.foresignal.security.Scope;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_pattern_of_life} (agent; prompt 114): an opted-in subject with its learned places (label, visits,
 * dwell, typical hours, radius), compact. Without {@code subjectId}, lists the subjects. Private subjects are
 * visible to admin tokens only.
 */
@Component
@ai.foresignal.security.restrict.RestrictedRead
public class GetPatternOfLifeTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "subjectId", string("The pattern-of-life subject id; omit to list the subjects", 26, 26)));

    private final PolService pol;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetPatternOfLifeTool(PolService pol, JsonMapper json) {
        this.pol = pol;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_pattern_of_life";
    }

    @Override
    public String title() {
        return "Pattern of life";
    }

    @Override
    public String description() {
        return "Learned places of an opted-in pattern-of-life subject (a record, entity or watchlist): label, visits,"
                + " total dwell, typical hours of week and radius. Omit subjectId to list the subjects. Deviations are"
                + " pol-deviation records of the pattern-of-life source. Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A subject with its places, or the subjects");
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
        boolean admin = context.caller().scope() == Scope.ADMIN;
        JsonNode id = args == null ? null : args.get("subjectId");
        try {
            if (id == null || id.isNull()) {
                return ToolResult.json(json.valueToTree(Map.of("items", pol.list(null, 500, admin).items())));
            }
            PolSubjectEntity subject = pol.get(id.asString(), admin);
            Map<String, Object> out = new LinkedHashMap<>(pol.view(subject));
            List<Map<String, Object>> places = new ArrayList<>();
            for (Map<String, Object> place : pol.places(subject.getId(), null, 500, admin).items()) {
                Map<String, Object> compact = new LinkedHashMap<>();
                compact.put("id", place.get("id"));
                compact.put("label", place.get("label"));
                compact.put("centroid", place.get("centroid"));
                compact.put("radiusM", place.get("radiusM"));
                compact.put("visits", place.get("visits"));
                compact.put("totalDwellMinutes", place.get("totalDwellMinutes"));
                @SuppressWarnings("unchecked")
                List<Integer> histogram = (List<Integer>) place.get("hourOfWeek");
                compact.put("typicalHours", PolViews.typicalHours(histogram.stream().mapToInt(Integer::intValue).toArray()));
                compact.put("lastSeen", place.get("lastSeen"));
                places.add(compact);
            }
            out.put("places", places);
            return ToolResult.json(json.valueToTree(out));
        } catch (ForesignalException e) {
            return ToolResult.error(e.getMessage());
        }
    }
}
