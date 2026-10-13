package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;

import ai.foresignal.api.mapper.RecordMapper;
import ai.foresignal.api.support.FieldSelection;
import ai.foresignal.api.support.GeometryOptions;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.media.MediaRow;
import ai.foresignal.media.MediaService;
import ai.foresignal.security.Scope;
import ai.foresignal.store.query.RecordQueryService;
import ai.foresignal.store.query.RecordRow;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code get_satellite_pass} (read, prompt 049, Weather-Imagery-Design §6.9): one pass of the operator's
 * SatDump stations with its metadata, plus its 256 px thumbnail as image content when it has one. The
 * image always travels with a text caption, so a model without vision still gets the facts.
 */
@ai.foresignal.security.restrict.RestrictedRead
@Component
public class GetSatellitePassTool implements ForesignalMcpTool {

    /** The largest thumbnail attached, bytes. */
    static final long MAX_THUMBNAIL_BYTES = 200 * 1024;

    static final Map<String, Object> INPUT = object(props(
            "id", string("The pass id from list_satellite_passes: {station}:{satKey}:{aosEpochMinute}, for example"
                    + " home:meteor-m2-3:29467234, or the item id satdump-home:home:meteor-m2-3:29467234.", 3, 300),
            "detail", detail()), "id");

    private final RecordQueryService queries;
    private final RecordItems items;
    private final McpOutputShaper shaper;
    private final SatellitePasses passes;
    private final MediaService media;

    /** Creates the tool. */
    public GetSatellitePassTool(RecordQueryService queries, RecordItems items, McpOutputShaper shaper,
            SatellitePasses passes, MediaService media) {
        this.queries = queries;
        this.items = items;
        this.shaper = shaper;
        this.passes = passes;
        this.media = media;
    }

    @Override
    public String name() {
        return "get_satellite_pass";
    }

    @Override
    public String title() {
        return "Get one satellite pass";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Returns one satellite pass received by the operator's SatDump ground station:"
                + " satellite, AOS, LOS, max elevation, status, RF statistics (SNR, BER, lock times) and decoded"
                + " products, plus the first composite's 256 px thumbnail as an image when there is one.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The pass");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        String[] id = SatellitePasses.split(McpArgs.requiredText(args, "id"));
        items.visibleSources(List.of(id[0]), context.excludedSources());
        RecordRow row = queries.get(id[0], id[1], false, FieldSelection.ALL, GeometryOptions.DEFAULT,
                        context.excludedSources(), context.restriction())
                .filter(r -> SatellitePasses.KIND.equals(r.kind()))
                .orElseThrow(() -> new McpToolException("No satellite pass " + id[1] + " at " + id[0]
                        + ". Check the id with list_satellite_passes."));
        ObjectNode item = shaper.object(items.item(row, RecordMapper.Shape.CURRENT, detail), detail);
        String thumbnail = passes.thumbnail(row);
        if (thumbnail == null) {
            return ToolResult.json(item);
        }
        try {
            MediaRow thumb = media.visible(thumbnail, context.excludedSources());
            if (thumb.bytes() > MAX_THUMBNAIL_BYTES) {
                return ToolResult.json(item);
            }
            byte[] bytes = Files.readAllBytes(media.file(thumb));
            String caption = caption(row, passes.payload(row));
            return ToolResult.image(Base64.getEncoder().encodeToString(bytes), thumb.contentType(), caption, item);
        } catch (IOException | RuntimeException e) {
            // The thumbnail expired or its file is gone: the metadata still answers the question.
            return ToolResult.json(item);
        }
    }

    /** What the thumbnail shows, for a model without vision. */
    static String caption(RecordRow row, JsonNode payload) {
        String title = row.title() != null ? row.title() : payload.path("title").asString("Satellite pass");
        String composite = "";
        for (JsonNode product : payload.path("products")) {
            if (product.path("composite").isString()) {
                composite = " Thumbnail of " + product.path("composite").asString() + ".";
                break;
            }
        }
        return title + " (" + payload.path("status").asString("unknown") + ", AOS "
                + payload.path("aos").asString("unknown") + ")." + composite;
    }
}
