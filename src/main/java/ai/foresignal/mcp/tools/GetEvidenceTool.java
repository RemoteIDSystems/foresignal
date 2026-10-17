package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.enumArray;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.SeriesEgress;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.evidence.EvidenceQueries;
import ai.foresignal.evidence.QualityFlags;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.EgressPath;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_evidence} (read, prompt 116): a canonical event with its observations, originators, evidence groups
 * and quality flags; the evidence identity of one record; or a list of canonical events. Independence is counted
 * in evidence groups, never in records or sources.
 */
@Component
@RestrictedRead("observations of hidden sources are left out")
public class GetEvidenceTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "canonicalEventId", string("A canonical event id: returns the event with its observations and groups.", 26, 26),
            "source", string("With externalId: the evidence identity of this record.", 1, 64),
            "externalId", string("With source: the record's external id.", 1, 512),
            "family", string("When listing: only this family, e.g. seismic, fire, conflict, outage.", 1, 64),
            "region", string("When listing: only this region code.", 1, 16),
            "since", dateTime("When listing: only events at or after this time."),
            "flags", enumArray("When listing: only events with all these quality flags.",
                    QualityFlags.ALL.toArray(String[]::new)),
            "limit", integer("When listing: how many events at most.", 1, 100, 20L)));

    private final EvidenceQueries queries;
    private final SeriesEgress egress;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetEvidenceTool(EvidenceQueries queries, SeriesEgress egress, JsonMapper json) {
        this.queries = queries;
        this.egress = egress;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_evidence";
    }

    @Override
    public String title() {
        return "Evidence identity";
    }

    @Override
    public String description() {
        return "Evidence identity of events: canonicalEventId returns one real-world event with every observation"
                + " that describes it, who originated each one, the evidence groups (observations that trace back to"
                + " one originator) and quality flags (ECHO: many re-reports of one origin; SINGLE_ORIGINATOR;"
                + " TIMESTAMP_CONFLICT; LATE_REVISION). source and externalId return one record's originator and"
                + " canonical events. With neither, lists canonical events. Count independent confirmation in"
                + " evidence groups, never in records or sources; a flag describes the evidence, it never says a"
                + " report is false.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("A CanonicalEvent, a RecordEvidence or a CanonicalEventPage");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public boolean readOnlyHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Set<String> excluded = egress.excluded(context.readCaller(), context.excludedSources(), EgressPath.MCP);
        try {
            String id = McpArgs.text(args, "canonicalEventId");
            if (id != null) {
                return ToolResult.json(json.valueToTree(queries.detail(id, excluded)));
            }
            String source = McpArgs.text(args, "source");
            String externalId = McpArgs.text(args, "externalId");
            if (source != null || externalId != null) {
                if (source == null || externalId == null) {
                    return ToolResult.error("Give both source and externalId.");
                }
                return ToolResult.json(json.valueToTree(queries.ofRecord(source, externalId, excluded)));
            }
            return ToolResult.json(json.valueToTree(queries.list(McpArgs.text(args, "family"),
                    McpArgs.text(args, "region"), McpArgs.instant(args, "since"), McpArgs.strings(args, "flags"), null,
                    McpArgs.integer(args, "limit", 20), excluded)));
        } catch (ForesignalException e) {
            return ToolResult.error(e.getMessage());
        }
    }
}
