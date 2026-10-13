package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.raster.FrameKeys;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.RasterFrameRow;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.security.Scope;
import ai.foresignal.source.spi.ProductSpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code list_products} (read, prompt 052; Weather-Imagery-Design §6.9): the raster products the caller may
 * see, with id, title, kind, units, coverage, the newest frame or complete run, and health. Compact output of
 * 4 KB or less, {@code detail=long} up to 50 KB. Returns no records.
 */
@Component
public class ListProductsTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "category", enumeration("Only products of this category.", null, "radar", "satellite", "model",
                    "lightning", "imagery"),
            "detail", detail()));

    private final ProductRegistry registry;
    private final SampleService samples;
    private final McpOutputShaper shaper;
    private final JsonMapper json;

    /** Creates the tool. */
    public ListProductsTool(ProductRegistry registry, SampleService samples, McpOutputShaper shaper, JsonMapper json) {
        this.registry = registry;
        this.samples = samples;
        this.shaper = shaper;
        this.json = json;
    }

    @Override
    public String name() {
        return "list_products";
    }

    @Override
    public String title() {
        return "List weather and imagery products";
    }

    @Override
    public String description() {
        return "Lists the raster products (radar, satellite, model, lightning, imagery) you may sample: id, title, kind,"
                + " units, coverage (CONUS, global, …), the newest frame time (latest) or forecast run, and health"
                + " (ok, or unavailable with the reason). Use the ids with sample_weather, weather_forecast and"
                + " area_weather_stats.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Products");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        String category = McpArgs.text(args, "category");
        List<ObjectNode> items = items(context.excludedSources(), category, detail);
        return ToolResult.json(shaper.list(items, items.size(), false, detail, Map.of()));
    }

    /** The product items, ordered by id. */
    List<ObjectNode> items(Set<String> excluded, String category, Detail detail) {
        List<ObjectNode> items = new ArrayList<>();
        List<ProductRegistry.Registered> all = new ArrayList<>(registry.all());
        all.sort(Comparator.comparing(ProductRegistry.Registered::id));
        for (ProductRegistry.Registered product : all) {
            ProductSpec spec = product.spec();
            if (excluded.contains(product.sourceId()) || (category != null && !category.equals(spec.category()))) {
                continue;
            }
            items.add(item(product, detail));
        }
        return items;
    }

    /** One product item. */
    ObjectNode item(ProductRegistry.Registered product, Detail detail) {
        ProductSpec spec = product.spec();
        ObjectNode n = json.createObjectNode();
        n.put("id", spec.id());
        n.put("title", spec.title());
        n.put("kind", spec.kind());
        n.put("units", spec.units());
        n.put("coverage", spec.spatial() == null ? null : spec.spatial().coverage());
        if (!spec.isExternal()) {
            if (spec.isForecast()) {
                Optional<Instant> run = samples.latestCompleteRun(spec.id());
                n.put("latest", run.map(FrameKeys::runIso).orElse(null));
            } else {
                n.put("latest", samples.newestFrame(spec.id()).map(RasterFrameRow::validTime).map(Instant::toString)
                        .orElse(null));
            }
        }
        Optional<String> unavailable = samples.unavailableReason(product);
        n.put("health", unavailable.isPresent() ? "unavailable" : "ok");
        unavailable.ifPresent(r -> n.put("healthReason", r));
        if (detail == Detail.LONG) {
            n.put("category", spec.category());
            n.put("source", product.sourceId());
            n.put("temporal", spec.temporal() == null ? null : spec.temporal().type());
            n.put("method", spec.bilinear() ? ProductSpec.RESAMPLE_BILINEAR : ProductSpec.RESAMPLE_NEAREST);
            if (spec.legendSummary() != null) {
                n.put("legend", spec.legendSummary());
            }
        }
        return n;
    }
}
