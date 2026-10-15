package ai.foresignal.analyst;

import ai.foresignal.llm.client.ToolSpec;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The analyst's output tools (prompt 102): {@code record_note}, {@code propose_watch},
 * {@code propose_intel_event}, {@code propose_investigation_item} and {@code annotate_event}. These
 * are loop-only tools: they never execute anything, they record validated proposals the runner
 * persists. The specs are stable for prompt caching.
 */
public final class AnalystOutputTools {

    public static final String RECORD_NOTE = "record_note";
    public static final String PROPOSE_WATCH = "propose_watch";
    public static final String PROPOSE_INTEL_EVENT = "propose_intel_event";
    public static final String PROPOSE_INVESTIGATION_ITEM = "propose_investigation_item";
    public static final String ANNOTATE_EVENT = "annotate_event";

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private AnalystOutputTools() {}

    /** True when {@code name} is one of the analyst's output tools. */
    public static boolean isOutputTool(String name) {
        return RECORD_NOTE.equals(name) || isProposalTool(name);
    }

    /** True when {@code name} is a proposal tool. */
    public static boolean isProposalTool(String name) {
        return PROPOSE_WATCH.equals(name) || PROPOSE_INTEL_EVENT.equals(name)
                || PROPOSE_INVESTIGATION_ITEM.equals(name) || ANNOTATE_EVENT.equals(name);
    }

    /** The output tool specs. */
    public static List<ToolSpec> specs(JsonMapper json) {
        List<ToolSpec> out = new ArrayList<>();
        out.add(new ToolSpec(RECORD_NOTE, "Write an analyst note with typed, cited claims.",
                schema(json, "title", "body", "priority", "claims")));
        out.add(new ToolSpec(PROPOSE_WATCH, "Propose a watch (a rule) to create.",
                schema(json, "rule", "rationale", "citations")));
        out.add(new ToolSpec(PROPOSE_INTEL_EVENT, "Propose an intelligence event from existing members.",
                schema(json, "type", "title", "members", "rationale", "citations")));
        out.add(new ToolSpec(PROPOSE_INVESTIGATION_ITEM, "Propose adding an item to an open investigation.",
                schema(json, "investigationId", "itemType", "ref", "note", "citations")));
        out.add(new ToolSpec(ANNOTATE_EVENT, "Propose a speculation note on an existing event's narrative.",
                schema(json, "eventId", "note", "citations")));
        return out;
    }

    /** A captured output. */
    public record Captured(String noteTitle, String noteBody, String priority, List<AnalystOutputValidator.RawClaim> claims,
            List<AnalystOutputValidator.RawProposal> proposals) {}

    /** Parses one output tool call into the capture. */
    public static void capture(String name, JsonNode input, JsonMapper json, List<AnalystOutputValidator.RawClaim> claims,
            List<AnalystOutputValidator.RawProposal> proposals, String[] note) {
        Map<String, Object> args = json.readValue(json.writeValueAsString(input), MAP);
        if (RECORD_NOTE.equals(name)) {
            note[0] = text(args.get("title"));
            note[1] = text(args.get("body"));
            note[2] = args.get("priority") == null ? "normal" : text(args.get("priority"));
            for (Object raw : list(args.get("claims"))) {
                if (raw instanceof Map<?, ?> map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> claim = (Map<String, Object>) map;
                    claims.add(new AnalystOutputValidator.RawClaim(text(claim.get("text")), text(claim.get("basis")),
                            AnalystClaim.strings(claim.get("citations"))));
                }
            }
            return;
        }
        Map<String, Object> payload;
        if (PROPOSE_WATCH.equals(name)) {
            payload = map(args.get("rule"));
            payload.put("citations", list(args.get("citations")));
        } else if (PROPOSE_INTEL_EVENT.equals(name)) {
            payload = new LinkedHashMap<>();
            payload.put("type", text(args.get("type")));
            payload.put("title", text(args.get("title")));
            payload.put("members", list(args.get("members")));
        } else if (PROPOSE_INVESTIGATION_ITEM.equals(name)) {
            payload = new LinkedHashMap<>();
            payload.put("investigationId", text(args.get("investigationId")));
            payload.put("itemType", text(args.get("itemType")));
            payload.put("ref", text(args.get("ref")));
            payload.put("note", text(args.get("note")));
        } else {
            payload = new LinkedHashMap<>();
            payload.put("eventId", text(args.get("eventId")));
            payload.put("note", text(args.get("note")));
        }
        proposals.add(new AnalystOutputValidator.RawProposal(name, payload, text(args.get("rationale")),
                AnalystClaim.strings(args.get("citations"))));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object raw) {
        return raw instanceof Map<?, ?> value ? (Map<String, Object>) value : new LinkedHashMap<>();
    }

    private static List<Object> list(Object raw) {
        return raw instanceof List<?> value ? new ArrayList<>(value) : List.of();
    }

    private static String text(Object raw) {
        return raw == null ? null : String.valueOf(raw);
    }

    private static JsonNode schema(JsonMapper json, String... fields) {
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        for (String field : fields) {
            properties.put(field, Map.of("description", field));
            required.add(field);
        }
        return json.valueToTree(Map.of("type", "object", "properties", properties, "required", List.of(fields[0])));
    }
}
