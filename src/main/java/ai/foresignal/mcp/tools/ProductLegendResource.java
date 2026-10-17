package ai.foresignal.mcp.tools;

import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.mcp.ForesignalMcpResourceTemplate;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.raster.ProductRegistry;
import ai.foresignal.raster.render.LegendService;
import ai.foresignal.raster.render.RasterRenderService;
import ai.foresignal.raster.render.StyleRegistry;
import ai.foresignal.raster.sample.SampleService;
import ai.foresignal.source.spi.ProductSpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * The resource template {@code foresignal://products/{id}/legend} (prompt 052; Weather-Imagery-Design §6.9):
 * a product's JSON legend in its default style and metric units, as {@code getProductLegend} returns it.
 */
@ai.foresignal.security.restrict.RestrictedRead("products of excluded sources are hidden; a legend holds no record content")
@Component
public class ProductLegendResource implements ForesignalMcpResourceTemplate {

    private static final Pattern URI = Pattern.compile("^foresignal://products/([a-z0-9][a-z0-9-]{0,63})/legend$");

    private final SampleService samples;
    private final RasterRenderService renderer;
    private final LegendService legends;
    private final JsonMapper json;

    /** Creates the template. */
    public ProductLegendResource(SampleService samples, RasterRenderService renderer, LegendService legends,
            JsonMapper json) {
        this.samples = samples;
        this.renderer = renderer;
        this.legends = legends;
        this.json = json;
    }

    @Override
    public String uriTemplate() {
        return "foresignal://products/{id}/legend";
    }

    @Override
    public String name() {
        return "product-legend";
    }

    @Override
    public String description() {
        return "A product's legend (stops, colors, labels and units) in its default style, metric units.";
    }

    @Override
    public boolean matches(String uri) {
        return uri != null && URI.matcher(uri).matches();
    }

    @Override
    public String read(ToolContext context, String uri) {
        Matcher m = URI.matcher(uri == null ? "" : uri);
        if (!m.matches()) {
            throw new McpToolException("Not a product legend URI: " + uri);
        }
        try {
            ProductRegistry.Registered product = samples.visible(m.group(1), context.excludedSources());
            ProductSpec spec = product.spec();
            RasterRenderService.requireRendered(spec);
            StyleRegistry.Resolved style = renderer.style(spec, null);
            return json.writeValueAsString(legends.legend(spec, style.style(), false));
        } catch (ForesignalException e) {
            throw new McpToolException(e.getMessage());
        }
    }
}
