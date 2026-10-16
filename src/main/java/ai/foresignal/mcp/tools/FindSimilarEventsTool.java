package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.intel.IntelEventMapper;
import ai.foresignal.intel.IntelligenceEventEntity;
import ai.foresignal.intel.IntelligenceEventRepository;
import ai.foresignal.intel.IntelVisibility;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.security.Scope;
import ai.foresignal.security.restrict.RestrictedRead;
import ai.foresignal.source.spi.Geo;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code find_similar_events} (read, prompt 102): deterministic — the intelligence events of the same
 * type within {@code withinKm} of one event over the history window, newest first.
 */
@RestrictedRead
@Component
public class FindSimilarEventsTool implements ForesignalMcpTool {

    /** The default radius, km. */
    static final double DEFAULT_KM = 200;

    /** The default window, days. */
    static final int DEFAULT_DAYS = 30;

    static final Map<String, Object> INPUT = object(props(
            "eventId", string("The event to compare against.", 1, 26),
            "withinKm", integer("The radius, km.", 1, 2_000, 200L),
            "windowDays", integer("How far back to look.", 1, 90, 30L)));

    private final IntelligenceEventRepository events;
    private final McpOutputShaper shaper;
    private final IntelVisibility visibility;

    /** Creates the tool. */
    public FindSimilarEventsTool(IntelligenceEventRepository events, McpOutputShaper shaper,
            IntelVisibility visibility) {
        this.events = events;
        this.shaper = shaper;
        this.visibility = visibility;
    }

    @Override
    public String name() {
        return "find_similar_events";
    }

    @Override
    public String title() {
        return "Find similar intelligence events";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Finds intelligence events of the same type within a radius over the recent"
                + " history. Deterministic: distance and type only.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Similar intelligence events");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        String id = McpArgs.requiredText(args, "eventId");
        // Private events (EGR-1): invisible to a caller without private access, as target and as result.
        boolean privateAccess = visibility.seesPrivate(context.caller());
        IntelligenceEventEntity target = events.findById(id).orElse(null);
        if (target == null || !IntelVisibility.visible(target, privateAccess)) {
            return ToolResult.error("No such event: " + id);
        }
        double withinKm = McpArgs.integer(args, "withinKm", (int) DEFAULT_KM);
        int days = McpArgs.integer(args, "windowDays", DEFAULT_DAYS);
        Instant since = Instant.now().minus(Duration.ofDays(days));
        Geometry targetGeom = target.getGeom();
        List<ObjectNode> items = new ArrayList<>();
        for (IntelligenceEventEntity event : events.findByTypeAndStatusInOrderByUpdatedAtDesc(target.getType(),
                java.util.List.of("emerging", "active", "reopened"))) {
            if (event.getId().equals(id) || event.getUpdatedAt() == null || event.getUpdatedAt().isBefore(since)
                    || !IntelVisibility.visible(event, privateAccess)) {
                continue;
            }
            if (withinKm < 2_000 && targetGeom != null && event.getGeom() != null
                    && Geo.haversineKm(targetGeom.getCentroid().getY(), targetGeom.getCentroid().getX(),
                            event.getGeom().getCentroid().getY(), event.getGeom().getCentroid().getX()) > withinKm) {
                continue;
            }
            items.add(shaper.objectTree(IntelEventMapper.summary(event)));
            if (items.size() >= 25) {
                break;
            }
        }
        return ToolResult.json(shaper.list(items, items.size(), false, Detail.SHORT, null));
    }
}
