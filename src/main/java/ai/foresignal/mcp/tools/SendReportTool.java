package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.object.ObjectCaller;
import ai.foresignal.object.ObjectCallers;
import ai.foresignal.report.ReportSendEntity;
import ai.foresignal.report.ReportSender;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code send_report} (agent, prompt 132): sends a published report to principals and groups on this instance, as
 * {@code POST /v1/reports/{id}/send}; TLP is checked per recipient.
 */
@RestrictedRead("returns the recorded send (grantees and outcomes), never records")
@Component
public class SendReportTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "reportId", string("The report id or display id (RPT-yyyy-nnnn)."),
            "principals", JsonSchemas.stringArray("Principal ids or handles.", 100),
            "groups", JsonSchemas.stringArray("Group ids.", 100),
            "channels", JsonSchemas.stringArray("Recipients' own personal channel ids for HTML-link delivery.", 50),
            "message", string("A message for the recipients.", 0, 2000),
            "revision", JsonSchemas.integer("The revision to send (default: the latest).", 1, Integer.MAX_VALUE,
                    null)), "reportId");

    private final ReportSender sender;
    private final ObjectCallers callers;
    private final JsonMapper json;

    /** Creates the tool. */
    public SendReportTool(ReportSender sender, ObjectCallers callers, JsonMapper json) {
        this.sender = sender;
        this.callers = callers;
        this.json = json;
    }

    @Override
    public String name() {
        return "send_report";
    }

    @Override
    public String title() {
        return "Send a report";
    }

    @Override
    public String description() {
        return "Sends a published report to principals and groups on this instance: each gets viewer access and a"
                + " report.received notification through their own preferences. The report's TLP label is checked for"
                + " every recipient and a send it forbids is refused with TLP_VIOLATION naming the rule. To reach"
                + " another instance, build a signed package with POST /v1/exchange/packages instead.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The recorded ReportSend");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        ObjectCaller caller = callers.of(context.caller());
        if (caller.anonymous() || caller.principal() == null) {
            throw new McpToolException("Sending a report needs a token.");
        }
        String reportId = McpArgs.requiredText(args, "reportId");
        tools.jackson.databind.node.ObjectNode body = json.createObjectNode();
        for (String field : new String[] {"principals", "groups", "channels", "message", "revision"}) {
            if (args != null && args.has(field) && !args.get(field).isNull()) {
                body.set(field, args.get(field));
            }
        }
        ReportSendEntity send = sender.send(reportId, body, caller);
        return ToolResult.json(json.valueToTree(sender.view(send)));
    }
}
