package ai.foresignal.cycle;

import ai.foresignal.analyst.AnalystActions;
import ai.foresignal.llm.client.ToolSpec;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The loop-only output tools of the cycle roles (prompt 128): {@code record_judgments} (analyst),
 * {@code record_red_team} (red team), {@code record_outside_view} and {@code record_forecast} (forecaster), and the
 * proposal tools (proposer). They never execute anything: the runner captures their input, validates it and decides.
 * The specs are fixed, so the tool prefix stays cacheable.
 */
public final class CycleOutputTools {

    /** The analyst's output. */
    public static final String RECORD_JUDGMENTS = "record_judgments";

    /** The red team's output (one call per set). */
    public static final String RECORD_RED_TEAM = "record_red_team";

    /** The forecaster's outside view. */
    public static final String RECORD_OUTSIDE_VIEW = "record_outside_view";

    /** The forecaster's final forecast. */
    public static final String RECORD_FORECAST = "record_forecast";

    /** The proposal types the proposer may record (102 pipeline). */
    public static final List<String> PROPOSALS = List.of(AnalystActions.PROPOSE_HYPOTHESIS_SET,
            AnalystActions.PROPOSE_INDICATOR, AnalystActions.PROPOSE_WATCH, AnalystActions.PROPOSE_QUESTION,
            AnalystActions.PROPOSE_SITUATION_MERGE, AnalystActions.PROPOSE_CHRONICLE);

    private static final TypeReference<LinkedHashMap<String, Object>> MAP = new TypeReference<>() {};

    private CycleOutputTools() {}

    /** The analyst's tools. */
    public static List<ToolSpec> analyst(JsonMapper json) {
        Map<String, Object> judgment = object(Map.of(
                "text", Map.of("type", "string", "description", "The judgment, one or two sentences"),
                "probability", Map.of("type", "number", "minimum", 0, "maximum", 1,
                        "description", "Only with questionId or hypothesisSetId"),
                "confidence", Map.of("type", "string", "enum", List.of("low", "moderate", "high")),
                "confidenceStatement", Map.of("type", "string", "description", "A separate sentence on confidence"),
                "citations", Map.of("type", "array", "items", Map.of("type", "string")),
                "hypothesisSetId", Map.of("type", "string"),
                "questionId", Map.of("type", "string"),
                "changeExplanation", Map.of("type", "string",
                        "description", "What moved since the previous assessment and why")), List.of("text", "citations"));
        return List.of(new ToolSpec(RECORD_JUDGMENTS, "Record the 3 to 7 key judgments of this assessment.",
                json.valueToTree(object(Map.of(
                        "judgments", Map.of("type", "array", "items", judgment),
                        "whatWeCannotSee", Map.of("type", "string",
                                "description", "Optional comment on the collection gaps")), List.of("judgments")))));
    }

    /** The red team's tools. */
    public static List<ToolSpec> redTeam(JsonMapper json) {
        return List.of(new ToolSpec(RECORD_RED_TEAM, "Record the red-team challenge of one hypothesis set.",
                json.valueToTree(object(Map.of(
                        "setId", Map.of("type", "string"),
                        "runnerUpHypothesisId", Map.of("type", "string"),
                        "strongestCase", Map.of("type", "string"),
                        "disconfirmingEvidence", Map.of("type", "array", "items", Map.of("type", "object")),
                        "assumptionsAtRisk", Map.of("type", "array", "items", Map.of("type", "string")),
                        "premortem", Map.of("type", "string"),
                        "probability", Map.of("type", "number", "minimum", 0, "maximum", 1),
                        "distribution", Map.of("type", "object"),
                        "citations", Map.of("type", "array", "items", Map.of("type", "string"))),
                        List.of("setId", "strongestCase", "premortem")))));
    }

    /** The forecaster's tools. */
    public static List<ToolSpec> forecaster(JsonMapper json) {
        List<ToolSpec> out = new ArrayList<>();
        out.add(new ToolSpec(RECORD_OUTSIDE_VIEW, "Record the outside view: criteria quoted verbatim, the reference"
                + " class and its rate.", json.valueToTree(object(Map.of(
                        "criteriaQuoted", Map.of("type", "string"),
                        "referenceClass", Map.of("type", "string"),
                        "baseRate", Map.of("type", "number", "minimum", 0, "maximum", 1),
                        "probability", Map.of("type", "number", "minimum", 0, "maximum", 1),
                        "citations", Map.of("type", "array", "items", Map.of("type", "string"))),
                        List.of("criteriaQuoted", "referenceClass")))));
        out.add(new ToolSpec(RECORD_FORECAST, "Record the final forecast after the inside-view adjustments.",
                json.valueToTree(object(Map.of(
                        "adjustments", Map.of("type", "array", "items", Map.of("type", "object")),
                        "probability", Map.of("type", "number", "minimum", 0, "maximum", 1),
                        "distribution", Map.of("type", "object"),
                        "quantiles", Map.of("type", "object"),
                        "claims", Map.of("type", "array", "items", Map.of("type", "object")),
                        "citations", Map.of("type", "array", "items", Map.of("type", "string"))),
                        List.of("adjustments")))));
        return out;
    }

    /** The proposer's tools. */
    public static List<ToolSpec> proposer(JsonMapper json) {
        List<ToolSpec> out = new ArrayList<>();
        Map<String, Object> common = Map.of("rationale", Map.of("type", "string"),
                "citations", Map.of("type", "array", "items", Map.of("type", "string")));
        out.add(new ToolSpec(AnalystActions.PROPOSE_HYPOTHESIS_SET, "Propose a hypothesis set (HypothesisSetInput).",
                json.valueToTree(object(merge(common, Map.of("set", Map.of("type", "object"))), List.of("set")))));
        out.add(new ToolSpec(AnalystActions.PROPOSE_INDICATOR, "Propose an indicator for an existing set.",
                json.valueToTree(object(merge(common, Map.of("setId", Map.of("type", "string"),
                        "indicator", Map.of("type", "object"))), List.of("setId", "indicator")))));
        out.add(new ToolSpec(AnalystActions.PROPOSE_WATCH, "Propose a watch (a rule) to create.",
                json.valueToTree(object(merge(common, Map.of("rule", Map.of("type", "object"))), List.of("rule")))));
        out.add(new ToolSpec(AnalystActions.PROPOSE_QUESTION, "Propose a ledger question (ForecastQuestionInput).",
                json.valueToTree(object(merge(common, Map.of("question", Map.of("type", "object"))),
                        List.of("question")))));
        out.add(new ToolSpec(AnalystActions.PROPOSE_SITUATION_MERGE, "Propose merging one situation into another.",
                json.valueToTree(object(merge(common, Map.of("situationId", Map.of("type", "string"),
                        "intoId", Map.of("type", "string"), "reason", Map.of("type", "string"))),
                        List.of("situationId", "intoId")))));
        out.add(new ToolSpec(AnalystActions.PROPOSE_CHRONICLE, "Propose a chronicle (a monitoring thread that records its"
                + " own history) for an emerging situation (situationId) or an area with a topic; Foresignal builds the"
                + " layers, capture modes and storage estimate.", json.valueToTree(object(merge(common, Map.of(
                        "situationId", Map.of("type", "string"),
                        "area", Map.of("type", "object", "description", "ChronicleArea: geometry, placeIds, regionCodes,"
                                + " bufferKm"),
                        "topic", Map.of("type", "string", "description", "An attention topic id or keywords"),
                        "name", Map.of("type", "string"))), List.of()))));
        return out;
    }

    /** The arguments of a tool call as a map. */
    public static Map<String, Object> args(JsonNode input, JsonMapper json) {
        if (input == null || input.isNull()) {
            return new LinkedHashMap<>();
        }
        try {
            return json.readValue(json.writeValueAsString(input), MAP);
        } catch (RuntimeException e) {
            return new LinkedHashMap<>();
        }
    }

    /** True when {@code name} is one of the output tools of any role. */
    public static boolean isOutputTool(String name) {
        return RECORD_JUDGMENTS.equals(name) || RECORD_RED_TEAM.equals(name) || RECORD_OUTSIDE_VIEW.equals(name)
                || RECORD_FORECAST.equals(name) || PROPOSALS.contains(name);
    }

    private static Map<String, Object> merge(Map<String, Object> a, Map<String, Object> b) {
        Map<String, Object> out = new LinkedHashMap<>(new java.util.TreeMap<>(a));
        out.putAll(new java.util.TreeMap<>(b));
        return out;
    }

    private static Map<String, Object> object(Map<String, Object> properties, List<String> required) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", "object");
        out.put("properties", new java.util.TreeMap<>(properties));
        out.put("required", required);
        return out;
    }
}
