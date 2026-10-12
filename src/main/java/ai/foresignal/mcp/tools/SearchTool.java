package ai.foresignal.mcp.tools;

import static ai.foresignal.mcp.JsonSchemas.dateTime;
import static ai.foresignal.mcp.JsonSchemas.detail;
import static ai.foresignal.mcp.JsonSchemas.enumArray;
import static ai.foresignal.mcp.JsonSchemas.enumeration;
import static ai.foresignal.mcp.JsonSchemas.integer;
import static ai.foresignal.mcp.JsonSchemas.object;
import static ai.foresignal.mcp.JsonSchemas.props;
import static ai.foresignal.mcp.JsonSchemas.string;
import static ai.foresignal.mcp.JsonSchemas.stringArray;

import ai.foresignal.api.support.BboxParser;
import ai.foresignal.mcp.ForesignalMcpTool;
import ai.foresignal.mcp.JsonSchemas;
import ai.foresignal.mcp.McpArgs;
import ai.foresignal.mcp.McpOutputShaper;
import ai.foresignal.mcp.McpOutputShaper.Detail;
import ai.foresignal.mcp.McpToolException;
import ai.foresignal.mcp.ToolContext;
import ai.foresignal.mcp.ToolResult;
import ai.foresignal.search.SearchRequest;
import ai.foresignal.search.SearchService;
import ai.foresignal.security.Scope;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code search} (read, cost-bearing; prompt 027): the same search as {@code GET /v1/search}, with
 * the caller's private sources excluded. Semantic and hybrid queries spend the embedding budget, so
 * a token is always required. Short items carry the persisted {@code seq} of every record hit, to
 * cite.
 */
@ai.foresignal.security.restrict.RestrictedRead
@Component
public class SearchTool implements ForesignalMcpTool {

    /** The largest {@code limit}. */
    public static final int MAX_LIMIT = 50;

    /** The default {@code limit}. */
    public static final int DEFAULT_LIMIT = 10;

    static final Map<String, Object> INPUT = object(props(
            "q", string("What to look for, in plain words, for example 'flooding near the river' or 'port strike'."
                    + " Semantic and hybrid modes match meaning, text mode matches words.", 1, 1000),
            "mode", enumeration("hybrid (default) fuses word and meaning matches; semantic matches meaning only;"
                    + " text matches words only and works without an embedding model.", "hybrid",
                    "hybrid", "semantic", "text"),
            "docType", enumArray("Document types to search: record, briefing, alert; omit for all.",
                    "record", "briefing", "alert"),
            "source", stringArray("Source ids to search (from list_sources); omit for every source.", 50),
            "kind", stringArray("Record kinds to include (from list_kinds); omit for every kind.", 50),
            "bbox", string("Area filter as minLon,minLat,maxLon,maxLat in WGS84 degrees, for example -125,24,-66,50."),
            "since", dateTime("Only documents whose timeField is at or after this RFC 3339 time."),
            "until", dateTime("Only documents whose timeField is before this RFC 3339 time."),
            "timeField", enumeration("The time axis of since/until: effective (default: event time when there is"
                    + " one, else observation time) or observed.", "effective", "effective", "observed"),
            "limit", integer("Most results to return (1–" + MAX_LIMIT + ").", 1, MAX_LIMIT, (long) DEFAULT_LIMIT),
            "detail", detail()),
            "q");

    private final SearchService search;
    private final McpOutputShaper shaper;

    /** Creates the tool. */
    public SearchTool(SearchService search, McpOutputShaper shaper) {
        this.search = search;
        this.shaper = shaper;
    }

    @Override
    public String name() {
        return "search";
    }

    @Override
    public String title() {
        return "Search records, briefings and alerts";
    }

    @Override
    public String description() {
        return UNTRUSTED_NOTE + " Finds records (and briefings and enriched alerts) by meaning and words, ranked by"
                + " relevance, filtered by source, kind, area and time window. Each item has docType, docId"
                + " (source:externalId for records), score, title, snippet, seq (the persisted version to cite),"
                + " source, kind and effectiveTime. degraded=true means semantic search was unavailable and only word"
                + " matches were used. Use get_record for a record in full.";
    }

    @Override
    public Map<String, Object> inputSchema() {
        return INPUT;
    }

    @Override
    public Map<String, Object> outputSchema() {
        return JsonSchemas.listOutput("Search results, most relevant first; mode and degraded say how they were found");
    }

    @Override
    public Scope requiredScope() {
        return Scope.READ;
    }

    @Override
    public boolean costBearing() {
        return true;
    }

    @Override
    public ToolResult call(ToolContext context, JsonNode args) {
        Detail detail = Detail.of(args);
        String q = McpArgs.requiredText(args, "q");
        Instant since = McpArgs.instant(args, "since");
        Instant until = McpArgs.instant(args, "until");
        if (since != null && until != null && !since.isBefore(until)) {
            throw new McpToolException("until must be after since.");
        }
        String mode = McpArgs.text(args, "mode");
        String timeField = McpArgs.text(args, "timeField");
        SearchRequest request = new SearchRequest(
                q.strip(),
                SearchRequest.Mode.of(mode == null ? "hybrid" : mode),
                new HashSet<>(McpArgs.strings(args, "docType")),
                visible(McpArgs.strings(args, "source"), context),
                McpArgs.strings(args, "kind"),
                BboxParser.parse(McpArgs.text(args, "bbox")),
                since,
                until,
                "observed".equals(timeField),
                McpArgs.integer(args, "limit", DEFAULT_LIMIT));
        Map<String, Object> results = search.search(request, context.excludedSources(), context.restriction());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) results.get("items");
        List<ObjectNode> out = new ArrayList<>(items.size());
        for (Map<String, Object> item : items) {
            out.add(detail == Detail.LONG ? shaper.objectTree(item) : shortItem(item));
        }
        Map<String, Object> extra = new java.util.LinkedHashMap<>();
        extra.put("mode", results.get("mode"));
        extra.put("degraded", results.get("degraded"));
        if (results.get("reason") != null) {
            extra.put("reason", results.get("reason"));
        }
        return ToolResult.json(shaper.list(out, out.size(), false, detail, extra));
    }

    private ObjectNode shortItem(Map<String, Object> item) {
        ObjectNode out = shaper.object();
        for (String key : List.of("docType", "docId", "score", "title", "snippet", "seq", "source", "kind",
                "effectiveTime")) {
            Object value = item.get(key);
            if (value != null) {
                out.set(key, shaper.tree(value));
            }
        }
        return out;
    }

    private static List<String> visible(List<String> sources, ToolContext context) {
        for (String id : sources) {
            if (context.excludedSources().contains(id)) {
                throw new McpToolException("Unknown source '" + id + "'.");
            }
        }
        return sources;
    }
}
