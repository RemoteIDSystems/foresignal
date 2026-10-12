package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.enumArray;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.error.NotFoundException;
import ai.foresignal.api.mapper.HealthMapper;
import ai.foresignal.collect.HealthService;
import ai.foresignal.collect.SourceHealthView;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.SourceVisibilityGate;
import ai.foresignal.source.HealthStatus;
import ai.foresignal.source.SourceRegistry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code source_health} (read): health of one source or of all, optionally by status, as
 * {@code getSourceHealth} and {@code listSourceHealth}.
 */
@Component
public class SourceHealthTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "source", string("One source id; omit for every source.", 1, 200),
            "status", enumArray("Only sources in these states.", "healthy", "degraded", "failing", "idle", "disabled"),
            "detail", detail()));

    /** Members of a short health item. */
    static final List<String> SHORT_MEMBERS = List.of("sourceId", "status", "statusReason", "lastSuccessAt",
            "lastErrorAt", "lastError", "consecutiveFailures", "lastDataAt", "backoffUntil", "budgetRemainingToday");

    private final HealthService health;
    private final SourceRegistry registry;
    private final SourceVisibilityGate visibility;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public SourceHealthTool(HealthService health, SourceRegistry registry, SourceVisibilityGate visibility,
            McpOutputShaper shaper) {
        this.health = health;
        this.registry = registry;
        this.visibility = visibility;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "source_health";
    }

    @Override
    public String title() {
        return "Source health";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Reports the health of one data source or all of them: status (healthy, degraded,"
                + " failing, idle, disabled), the reason, last success and error, consecutive failures, budget left"
                + " today and when data last arrived. Check it before concluding that nothing happened: a failing"
                + " source cannot report events.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Health per source");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        String source = McpArgs.text(args, "source");
        Set<HealthStatus> wanted = EnumSet.noneOf(HealthStatus.class);
        for (String status : McpArgs.strings(args, "status")) {
            wanted.add(HealthStatus.fromValue(status));
        }
        List<SourceHealthView> views;
        if (source != null) {
            registry.descriptor(source);
            if (!visibility.visible(source, context.caller().currentToken())) {
                throw NotFoundException.of("Source", source);
            }
            SourceHealthView view = health.health(source);
            views = wanted.isEmpty() || wanted.contains(view.status()) ? List.of(view) : List.of();
        } else {
            views = health.all(wanted.isEmpty() ? Optional.empty() : Optional.of(wanted)).stream()
                    .filter(v -> visibility.visible(v.sourceId(), context.caller().currentToken()))
                    .sorted(Comparator.comparing(SourceHealthView::sourceId))
                    .toList();
        }
        List<ObjectNode> items = new ArrayList<>();
        for (SourceHealthView view : views) {
            ObjectNode full = shaper.objectTree(HealthMapper.health(view));
            if (detail == Detail.LONG) {
                items.add(full);
            } else {
                ObjectNode item = shaper.object();
                for (String member : SHORT_MEMBERS) {
                    if (full.hasNonNull(member)) {
                        item.set(member, full.get(member));
                    }
                }
                items.add(item);
            }
        }
        return ToolResult.json(shaper.list(items, items.size(), false, detail, null));
    }
}
