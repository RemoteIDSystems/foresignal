package ai.foresignal.mcp.tools;

import ai.foresignal.mcp.ForesignalMcpResource;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.ToolContext;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * The resource {@code foresignal://products} (prompt 052; Weather-Imagery-Design §6.9): the raster products the
 * caller may see, as {@code list_products} long. Returns no records.
 */
@Component
public class ProductsResource implements ForesignalMcpResource {

    private final ListProductsTool products;
    private final JsonMapper json;

    /** Creates the resource. */
    public ProductsResource(ListProductsTool products, JsonMapper json) {
        this.products = products;
        this.json = json;
    }

    @Override
    public String uri() {
        return "foresignal://products";
    }

    @Override
    public String name() {
        return "products";
    }

    @Override
    public String description() {
        return "The weather and imagery products you may sample: id, title, kind, units, coverage, newest frame or run,"
                + " health and legend summary.";
    }

    @Override
    public String read(ToolContext context) {
        ObjectNode out = json.createObjectNode();
        out.set("items", json.valueToTree(products.items(context.excludedSources(), null, Detail.LONG)));
        out.put("note", "Use sample_weather to read values at a point.");
        return json.writeValueAsString(out);
    }
}
