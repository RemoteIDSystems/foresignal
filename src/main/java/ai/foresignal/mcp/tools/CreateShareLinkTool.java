package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.raster.ProductUrls;
import ai.foresignal.security.Scope;
import ai.foresignal.security.TokenRestrictions;
import ai.foresignal.security.visibility.Viewer;
import ai.foresignal.share.ShareLinkService;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code create_share_link} (agent; prompt 112): creates an expiring, path-bound share link to a record, alert,
 * view, track, briefing run or export with the caller's rights, and returns it with its {@code fss_} key and URL
 * (shown once). Private targets need admin; non-redistributable targets are refused.
 */
@Component
public class CreateShareLinkTool implements ForesignalMcpTool {

    static final Map<String, Object> INPUT = object(props(
            "targetType", enumeration("What to share", null, "record", "alert", "view", "track", "briefing-run",
                    "export"),
            "targetRef", string("Record or track {sourceId}/{externalId}, alert id, view id or slug,"
                    + " {briefingId}/{runId}, or export job id", 1, 600),
            "fieldMask", stringArray("Payload keys to keep (allowlist), applied after license filtering", 100),
            "precisionM", JsonSchemas.integer("Coordinate precision cap in meters", 1, 1_000_000, null),
            "expiresAt", dateTime("Expiry (default 7 days, at most 30 days ahead)"),
            "maxUses", JsonSchemas.integer("Maximum number of uses", 1, 1_000_000, null)), "targetType", "targetRef");

    private final ShareLinkService links;
    private final ProductUrls urls;
    private final JsonMapper json;
    private final Clock clock;

    /** Creates the tool. */
    public CreateShareLinkTool(ShareLinkService links, ProductUrls urls, JsonMapper json, Clock clock) {
        this.links = links;
        this.urls = urls;
        this.json = json;
        this.clock = clock;
    }

    @Override
    public String name() {
        return "create_share_link";
    }

    @Override
    public String title() {
        return "Create a share link";
    }

    @Override
    public String description() {
        return "Creates an expiring, path-bound share link (/v1/k/{key}) to one record, alert, view, track, briefing"
                + " run or export, readable without a token. The key is returned once. Records whose license does not"
                + " allow redistribution are removed (a target that is one is refused); private sources never leave."
                + " Needs an agent token.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.objectOutput("The created share link with its key and url");
    }

    @Override
    public Scope requiredScope() {
        return Scope.AGENT;
    }

    @Override
    public boolean idempotentHint() {
        return false;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        List<String> mask = null;
        if (args.has("fieldMask") && args.get("fieldMask").isArray()) {
            mask = new ArrayList<>();
            for (JsonNode f : args.get("fieldMask")) {
                mask.add(f.asString());
            }
        }
        Instant expiresAt = null;
        if (args.hasNonNull("expiresAt")) {
            try {
                expiresAt = OffsetDateTime.parse(args.get("expiresAt").asString()).toInstant();
            } catch (DateTimeParseException e) {
                return ToolResult.error("expiresAt must be an RFC 3339 date-time");
            }
        }
        ShareLinkService.Create create = new ShareLinkService.Create(text(args, "targetType"), text(args, "targetRef"),
                mask, integer(args, "precisionM"), expiresAt, integer(args, "maxUses"));
        ShareLinkService.Created created = links.create(create, caller(context));
        Map<String, Object> out = ShareLinkService.json(created.link(), clock.instant());
        out.put("key", created.key());
        out.put("url", urls.storedBase() + "/v1/k/" + created.key());
        return ToolResult.json(json.valueToTree(out));
    }

    /** The share-link caller of an MCP session. */
    static ShareLinkService.Caller caller(ToolContext context) {
        Scope scope = context.readCaller().scope();
        boolean admin = scope != null && scope.includes(Scope.ADMIN);
        var token = context.caller().token();
        String id = token == null || token.id() == null ? null : token.id().toString();
        Viewer viewer = new Viewer(scope, context.readCaller().privateAccess(), TokenRestrictions.NONE);
        return new ShareLinkService.Caller(id, token == null ? null : token.name(), admin, viewer,
                token == null ? null : token.restrictions(), context.restriction(), context.excludedSources());
    }

    private static String text(JsonNode args, String name) {
        JsonNode v = args.get(name);
        return v == null || v.isNull() ? null : v.asString();
    }

    private static Integer integer(JsonNode args, String name) {
        JsonNode v = args.get(name);
        return v == null || v.isNull() || !v.canConvertToInt() ? null : v.intValue();
    }
}
