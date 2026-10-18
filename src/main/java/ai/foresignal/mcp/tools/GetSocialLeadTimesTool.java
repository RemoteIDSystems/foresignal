package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.social.correlation.LeadTimeStatistics;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code get_social_lead_times} (read; prompt 137): median and interquartile lead of social first observation over
 * physical, news and official first observation for corroborated comparisons, with the training cut-off; only the
 * out-of-sample summaries support "leads by" statements.
 */
@Component
@RestrictedRead("aggregate statistics over comparisons; no post or record is returned")
public class GetSocialLeadTimesTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "claimType", JsonSchemas.string("Lexicon claim type, e.g. explosion, outage"),
            "platform", JsonSchemas.string("Network, e.g. bluesky, x"),
            "region", JsonSchemas.string("040 region code (its admin-1 regions included)"),
            "foundBy", JsonSchemas.enumeration("Organic or cue-found narratives", null, "organic", "cue"),
            "since", JsonSchemas.dateTime("Comparisons evaluated at or after this time")));

    private final LeadTimeStatistics stats;
    private final JsonMapper json;

    /** Creates the tool. */
    public GetSocialLeadTimesTool(LeadTimeStatistics stats, JsonMapper json) {
        this.stats = stats;
        this.json = json;
    }

    @Override
    public String name() {
        return "get_social_lead_times";
    }

    @Override
    public String title() {
        return "Get social lead-time statistics";
    }

    @Override
    public String description() {
        return "Returns how far social discussion preceded independent physical, news and official first observation"
                + " for corroborated claims, grouped by claim type, platform, region and whether the narrative was"
                + " found by a cue. Only outOfSampleOverPhysical (comparisons after the training cut-off) may support a"
                + " statement that a kind of social signal leads; single cases are observed timing, never foreknowledge.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("LeadTimeStats");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public boolean idempotentHint() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        try {
            String since = ListNewsStoriesTool.text(args, "since");
            return ToolResult.json(json.valueToTree(stats.stats(new LeadTimeStatistics.Filters(
                    ListNewsStoriesTool.text(args, "claimType"), ListNewsStoriesTool.text(args, "platform"),
                    ListNewsStoriesTool.text(args, "region"), ListNewsStoriesTool.text(args, "foundBy"),
                    since == null ? null : Instant.parse(since)))));
        } catch (DateTimeParseException e) {
            return ToolResult.error("since must be an ISO-8601 instant");
        } catch (RuntimeException e) {
            return ToolResult.error(e.getMessage());
        }
    }
}
