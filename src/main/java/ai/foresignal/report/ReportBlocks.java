package ai.foresignal.report;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.forecast.language.EstimativeLanguage;
import ai.foresignal.object.ObjectRef;
import ai.foresignal.object.ObjectTypes;
import ai.foresignal.security.Ulids;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/**
 * Parses and checks report blocks (prompt 132, OpenAPI {@code ReportBlock}, one variant per {@code type}).
 *
 * <p>Fields the server owns are never taken from a request: {@code id} (assigned), {@code updatedAt} and
 * {@code updatedBy}, envelopes and snapshots (resolved by {@link ReportSnapshots}), the ICD 203 term of a key judgment
 * (derived from its probability with {@link EstimativeLanguage}), a map's attachment and an evidence item's grade.
 * A block's {@code textOrigin} is {@code author} unless the drafter or the narrative writer set it; an edit keeps the
 * origin, so a labeled LLM narrative stays labeled. A probability-bearing key judgment must reference a ledger
 * question or a hypothesis set.
 */
public final class ReportBlocks {

    /** The block id pattern. */
    public static final Pattern BLOCK_ID = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    /** The fields every block may carry. */
    static final Set<String> COMMON = Set.of("id", "type", "title", "textOrigin", "updatedAt", "updatedBy");

    /** The type-specific fields. */
    static final Map<String, Set<String>> FIELDS = Map.ofEntries(
            Map.entry("markdown", Set.of("markdown", "citations")),
            Map.entry("bluf", Set.of("judgments")),
            Map.entry("objects", Set.of("items")),
            Map.entry("map", Set.of("layers", "area", "bbox", "timeRange", "attachmentId", "mediaId", "caveat")),
            Map.entry("timeline", Set.of("sourceRef", "items")),
            Map.entry("hypotheses", Set.of("hypothesisSetRef", "snapshot", "revisionIds")),
            Map.entry("forecasts", Set.of("questions")),
            Map.entry("evidence", Set.of("items")),
            Map.entry("coverage", Set.of("caveats", "coverage")),
            Map.entry("attachments", Set.of("attachmentIds")),
            Map.entry("sources", Set.of("attributions")));

    /** The confidence levels of a key judgment. */
    static final List<String> CONFIDENCE = List.of("low", "moderate", "high");

    /** The most products a map block draws (the 055 limit). */
    static final int MAX_MAP_LAYERS = 4;

    private ReportBlocks() {}

    /** A new block id. */
    public static String newId(Instant now) {
        return "b" + Ulids.next(now).toLowerCase(java.util.Locale.ROOT);
    }

    /**
     * Parses a {@code ReportBlock} from a request.
     *
     * @param node the block
     * @param field the request field, for problems
     * @param origin the text origin to set ({@code author} for API callers)
     * @return the checked block without server-owned fields
     * @throws ValidationException when the block is malformed
     */
    public static Map<String, Object> parse(JsonNode node, String field, String origin) {
        if (node == null || !node.isObject()) {
            throw ValidationException.forField(field, "must be a ReportBlock object");
        }
        String type = node.hasNonNull("type") && node.get("type").isString() ? node.get("type").asString() : null;
        if (type == null || !ReportVocabulary.BLOCK_TYPES.contains(type)) {
            throw ValidationException.forField(field + ".type", "must be one of " + ReportVocabulary.BLOCK_TYPES);
        }
        Set<String> allowed = FIELDS.get(type);
        for (String member : node.propertyNames()) {
            if (!COMMON.contains(member) && !allowed.contains(member)) {
                throw ValidationException.forField(field + "." + member, "is not a field of a " + type + " block");
            }
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", type);
        out.put("title", text(node, "title", field, 300, false));
        out.put("textOrigin", origin);
        switch (type) {
            case "markdown" -> {
                String markdown = text(node, "markdown", field, 100_000, true);
                out.put("markdown", markdown);
                out.put("citations", refs(node.get("citations"), field + ".citations", 200, null));
            }
            case "bluf" -> out.put("judgments", judgments(node.get("judgments"), field + ".judgments"));
            case "objects" -> out.put("items", refItems(node.get("items"), field + ".items", "ref", 500, null));
            case "map" -> {
                List<String> layers = strings(node.get("layers"), field + ".layers", MAX_MAP_LAYERS);
                out.put("layers", layers);
                out.put("area", geometry(node.get("area"), field + ".area"));
                out.put("bbox", bbox(node.get("bbox"), field + ".bbox"));
                out.put("timeRange", timeRange(node.get("timeRange"), field + ".timeRange"));
                out.put("attachmentId", null);
                out.put("mediaId", null);
            }
            case "timeline" -> {
                String sourceRef = node.hasNonNull("sourceRef") ? ref(node.get("sourceRef"), field + ".sourceRef",
                        null) : null;
                out.put("sourceRef", sourceRef);
                out.put("items", timelineItems(node.get("items"), field + ".items"));
            }
            case "hypotheses" -> {
                out.put("hypothesisSetRef", ref(node.get("hypothesisSetRef"), field + ".hypothesisSetRef",
                        ObjectTypes.HYPOTHESIS_SET));
                out.put("snapshot", null);
                out.put("revisionIds", List.of());
            }
            case "forecasts" -> out.put("questions", refItems(node.get("questions"), field + ".questions",
                    "questionRef", 100, ObjectTypes.QUESTION));
            case "evidence" -> out.put("items", evidenceItems(node.get("items"), field + ".items"));
            case "coverage" -> {
                out.put("caveats", caveats(node.get("caveats"), field + ".caveats"));
                out.put("coverage", list(node.get("coverage"), field + ".coverage", 50));
            }
            case "attachments" -> out.put("attachmentIds", ulids(node.get("attachmentIds"), field + ".attachmentIds",
                    100));
            case "sources" -> out.put("attributions", attributions(node.get("attributions"), field + ".attributions"));
            default -> throw new IllegalStateException(type);
        }
        return out;
    }

    /** True when {@code block} was written by a machine ({@code deterministic} or {@code llm}). */
    public static boolean machineGenerated(Map<String, Object> block) {
        Object origin = block.get("textOrigin");
        return ReportVocabulary.DETERMINISTIC.equals(origin) || ReportVocabulary.LLM.equals(origin);
    }

    /** True when any block was written by a machine. */
    public static boolean machineGenerated(List<Map<String, Object>> blocks) {
        for (Map<String, Object> b : blocks) {
            if (machineGenerated(b)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Every object reference a block cites (objects, timeline items and source, hypothesis sets, questions, key
     * judgment references, citations, evidence items and attachments), in order and without duplicates.
     */
    @SuppressWarnings("unchecked")
    public static List<String> citedRefs(Map<String, Object> block) {
        Set<String> out = new LinkedHashSet<>();
        String type = String.valueOf(block.get("type"));
        switch (type) {
            case "markdown" -> addAll(out, block.get("citations"));
            case "bluf" -> {
                for (Object j : asList(block.get("judgments"))) {
                    if (j instanceof Map<?, ?> m) {
                        add(out, m.get("questionRef"));
                        add(out, m.get("hypothesisSetRef"));
                    }
                }
            }
            case "objects" -> {
                for (Object i : asList(block.get("items"))) {
                    if (i instanceof Map<?, ?> m) {
                        add(out, m.get("ref"));
                    }
                }
            }
            case "timeline" -> {
                add(out, block.get("sourceRef"));
                for (Object i : asList(block.get("items"))) {
                    if (i instanceof Map<?, ?> m) {
                        add(out, m.get("ref"));
                    }
                }
            }
            case "hypotheses" -> add(out, block.get("hypothesisSetRef"));
            case "forecasts" -> {
                for (Object q : asList(block.get("questions"))) {
                    if (q instanceof Map<?, ?> m) {
                        add(out, m.get("questionRef"));
                    }
                }
            }
            case "evidence" -> {
                for (Object i : asList(block.get("items"))) {
                    if (i instanceof Map<?, ?> m && m.get("evidenceItemId") != null) {
                        out.add(ObjectTypes.EVIDENCE_ITEM + ":" + m.get("evidenceItemId"));
                    }
                }
            }
            case "attachments" -> {
                for (Object id : asList(block.get("attachmentIds"))) {
                    out.add(ObjectTypes.ATTACHMENT + ":" + id);
                }
            }
            default -> {
                // map, coverage and sources cite no objects
            }
        }
        return new ArrayList<>(out);
    }

    /** The {@code [[ref]]} object links of a markdown text. */
    public static List<String> inlineLinks(String markdown) {
        List<String> out = new ArrayList<>();
        if (markdown == null) {
            return out;
        }
        java.util.regex.Matcher m = Pattern.compile("\\[\\[([^\\[\\]\\s]{3,1024})]]").matcher(markdown);
        while (m.find()) {
            if (ObjectRef.isValid(m.group(1)) && !out.contains(m.group(1))) {
                out.add(m.group(1));
            }
        }
        return out;
    }

    // ------------------------------------------------------------------------- helpers

    @SuppressWarnings("unchecked")
    static List<Object> asList(Object value) {
        return value instanceof List<?> l ? (List<Object>) l : List.of();
    }

    private static void add(Set<String> out, Object value) {
        if (value instanceof String s && !s.isBlank()) {
            out.add(s);
        }
    }

    private static void addAll(Set<String> out, Object value) {
        for (Object v : asList(value)) {
            add(out, v);
        }
    }

    private static String text(JsonNode node, String name, String field, int max, boolean required) {
        JsonNode v = node.get(name);
        if (v == null || v.isNull()) {
            if (required) {
                throw ValidationException.forField(field + "." + name, "is required");
            }
            return null;
        }
        if (!v.isString() || v.asString().length() > max || (required && v.asString().isBlank())) {
            throw ValidationException.forField(field + "." + name, "must be a string of at most " + max
                    + " characters");
        }
        return v.asString();
    }

    private static String ref(JsonNode node, String field, String type) {
        if (node == null || node.isNull() || !node.isString()) {
            throw ValidationException.forField(field, "must be an object reference");
        }
        ObjectRef ref = ObjectRef.parse(node.asString(), field);
        if (type != null && !type.equals(ref.type())) {
            throw ValidationException.forField(field, "must be a " + type + " reference");
        }
        return ref.format();
    }

    private static List<String> refs(JsonNode node, String field, int max, String type) {
        List<String> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isArray() || node.size() > max) {
            throw ValidationException.forField(field, "must be an array of at most " + max + " references");
        }
        int i = 0;
        for (JsonNode n : node) {
            String r = ref(n, field + "[" + i++ + "]", type);
            if (!out.contains(r)) {
                out.add(r);
            }
        }
        return out;
    }

    private static List<Map<String, Object>> refItems(JsonNode node, String field, String key, int max, String type) {
        if (node == null || !node.isArray()) {
            throw ValidationException.forField(field, "is required: an array of items with " + key);
        }
        if (node.size() > max) {
            throw ValidationException.forField(field, "holds more than " + max + " items");
        }
        List<Map<String, Object>> out = new ArrayList<>();
        int i = 0;
        for (JsonNode n : node) {
            String f = field + "[" + i++ + "]";
            JsonNode refNode = n != null && n.isObject() ? n.get(key) : n;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put(key, ref(refNode, f + (n != null && n.isObject() ? "." + key : ""), type));
            out.add(item);
        }
        return out;
    }

    private static List<Map<String, Object>> judgments(JsonNode node, String field) {
        if (node == null || !node.isArray()) {
            throw ValidationException.forField(field, "is required: an array of key judgments");
        }
        if (node.size() > 50) {
            throw ValidationException.forField(field, "holds more than 50 judgments");
        }
        List<Map<String, Object>> out = new ArrayList<>();
        int i = 0;
        for (JsonNode n : node) {
            String f = field + "[" + i++ + "]";
            if (n == null || !n.isObject()) {
                throw ValidationException.forField(f, "must be a key judgment object");
            }
            for (String member : n.propertyNames()) {
                if (!Set.of("text", "probability", "icdTerm", "confidence", "questionRef", "hypothesisSetRef")
                        .contains(member)) {
                    throw ValidationException.forField(f + "." + member, "is not a key judgment field");
                }
            }
            String text = text(n, "text", f, 2000, true);
            Double p = null;
            if (n.hasNonNull("probability")) {
                if (!n.get("probability").isNumber() || n.get("probability").asDouble() < 0
                        || n.get("probability").asDouble() > 1) {
                    throw ValidationException.forField(f + ".probability", "must be a probability in [0, 1]");
                }
                p = n.get("probability").asDouble();
            }
            String confidence = n.hasNonNull("confidence") ? n.get("confidence").asString() : null;
            if (confidence != null && !CONFIDENCE.contains(confidence)) {
                throw ValidationException.forField(f + ".confidence", "must be low, moderate or high");
            }
            String question = n.hasNonNull("questionRef") ? ref(n.get("questionRef"), f + ".questionRef",
                    ObjectTypes.QUESTION) : null;
            String set = n.hasNonNull("hypothesisSetRef") ? ref(n.get("hypothesisSetRef"), f + ".hypothesisSetRef",
                    ObjectTypes.HYPOTHESIS_SET) : null;
            if (p != null && question == null && set == null) {
                throw new ValidationException("A probability-bearing key judgment must reference a ledger question"
                        + " or a hypothesis set.", List.of(new FieldError(f + ".probability",
                                "needs questionRef or hypothesisSetRef")));
            }
            out.add(judgment(text, p, confidence, question, set));
        }
        return out;
    }

    /**
     * A key judgment with its ICD 203 term derived from the probability.
     *
     * @param text the judgment
     * @param probability the probability, or null
     * @param confidence {@code low}, {@code moderate}, {@code high} or null
     * @param questionRef the ledger question, or null
     * @param hypothesisSetRef the hypothesis set, or null
     * @return the {@code KeyJudgment} map
     */
    public static Map<String, Object> judgment(String text, Double probability, String confidence, String questionRef,
            String hypothesisSetRef) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("text", text);
        out.put("probability", probability);
        out.put("icdTerm", probability == null ? null : EstimativeLanguage.term(probability));
        out.put("confidence", confidence);
        out.put("questionRef", questionRef);
        out.put("hypothesisSetRef", hypothesisSetRef);
        return out;
    }

    private static List<Map<String, Object>> timelineItems(JsonNode node, String field) {
        if (node == null || !node.isArray()) {
            throw ValidationException.forField(field, "is required: an array of timeline items");
        }
        if (node.size() > 1000) {
            throw ValidationException.forField(field, "holds more than 1000 items");
        }
        List<Map<String, Object>> out = new ArrayList<>();
        int i = 0;
        for (JsonNode n : node) {
            String f = field + "[" + i++ + "]";
            if (n == null || !n.isObject()) {
                throw ValidationException.forField(f, "must be a timeline item object");
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("at", instant(n.get("at"), f + ".at", true).toString());
            item.put("title", text(n, "title", f, 500, true));
            item.put("ref", n.hasNonNull("ref") ? ref(n.get("ref"), f + ".ref", null) : null);
            out.add(item);
        }
        return out;
    }

    private static List<Map<String, Object>> evidenceItems(JsonNode node, String field) {
        if (node == null || !node.isArray()) {
            throw ValidationException.forField(field, "is required: an array of evidence items");
        }
        if (node.size() > 200) {
            throw ValidationException.forField(field, "holds more than 200 items");
        }
        List<Map<String, Object>> out = new ArrayList<>();
        int i = 0;
        for (JsonNode n : node) {
            String f = field + "[" + i++ + "]";
            JsonNode id = n != null && n.isObject() ? n.get("evidenceItemId") : n;
            if (id == null || !id.isString() || !Ulids.isUlid(id.asString())) {
                throw ValidationException.forField(f + ".evidenceItemId", "must be an evidence item ULID");
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("evidenceItemId", id.asString());
            out.add(item);
        }
        return out;
    }

    private static List<Map<String, Object>> caveats(JsonNode node, String field) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isArray() || node.size() > 100) {
            throw ValidationException.forField(field, "must be an array of at most 100 caveats");
        }
        int i = 0;
        for (JsonNode n : node) {
            String f = field + "[" + i++ + "]";
            if (n == null || !n.isObject()) {
                throw ValidationException.forField(f, "must be a Caveat object");
            }
            Map<String, Object> c = new LinkedHashMap<>();
            c.put("code", text(n, "code", f, 64, true));
            c.put("text", text(n, "text", f, 2000, true));
            if (n.hasNonNull("data")) {
                c.put("data", plain(n.get("data")));
            }
            out.add(c);
        }
        return out;
    }

    private static List<Map<String, Object>> attributions(JsonNode node, String field) {
        if (node == null || !node.isArray()) {
            throw ValidationException.forField(field, "is required: an array of attributions");
        }
        List<Map<String, Object>> out = new ArrayList<>();
        int i = 0;
        for (JsonNode n : node) {
            String f = field + "[" + i++ + "]";
            if (n == null || !n.isObject()) {
                throw ValidationException.forField(f, "must be an attribution object");
            }
            Map<String, Object> a = new LinkedHashMap<>();
            a.put("sourceId", text(n, "sourceId", f, 200, true));
            a.put("licence", text(n, "licence", f, 200, false));
            a.put("attribution", text(n, "attribution", f, 2000, true));
            a.put("url", text(n, "url", f, 2048, false));
            out.add(a);
        }
        return out;
    }

    private static List<String> strings(JsonNode node, String field, int max) {
        List<String> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isArray() || node.size() > max) {
            throw ValidationException.forField(field, "must be an array of at most " + max + " strings");
        }
        for (JsonNode n : node) {
            if (!n.isString() || n.asString().isBlank() || n.asString().length() > 200) {
                throw ValidationException.forField(field, "must hold non-empty strings");
            }
            out.add(n.asString());
        }
        return out;
    }

    private static List<String> ulids(JsonNode node, String field, int max) {
        if (node == null || !node.isArray()) {
            throw ValidationException.forField(field, "is required: an array of ULIDs");
        }
        if (node.size() > max) {
            throw ValidationException.forField(field, "holds more than " + max + " ids");
        }
        List<String> out = new ArrayList<>();
        for (JsonNode n : node) {
            if (!n.isString() || !Ulids.isUlid(n.asString())) {
                throw ValidationException.forField(field, "must hold ULIDs");
            }
            if (!out.contains(n.asString())) {
                out.add(n.asString());
            }
        }
        return out;
    }

    private static List<Object> list(JsonNode node, String field, int max) {
        List<Object> out = new ArrayList<>();
        if (node == null || node.isNull()) {
            return out;
        }
        if (!node.isArray() || node.size() > max) {
            throw ValidationException.forField(field, "must be an array of at most " + max + " entries");
        }
        for (JsonNode n : node) {
            out.add(plain(n));
        }
        return out;
    }

    /**
     * Checks a GeoJSON geometry (an object with a {@code type} and {@code coordinates} or {@code geometries}).
     *
     * @return the geometry as a map, or null
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> geometry(JsonNode node, String field) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isObject() || !node.hasNonNull("type") || !(node.has("coordinates") || node.has("geometries"))) {
            throw ValidationException.forField(field, "must be a GeoJSON geometry");
        }
        Object value = plain(node);
        return value instanceof Map<?, ?> m ? (Map<String, Object>) m : null;
    }

    private static List<Double> bbox(JsonNode node, String field) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isArray() || node.size() != 4) {
            throw ValidationException.forField(field, "must be [minLon, minLat, maxLon, maxLat]");
        }
        List<Double> out = new ArrayList<>();
        for (JsonNode n : node) {
            if (!n.isNumber()) {
                throw ValidationException.forField(field, "must hold numbers");
            }
            out.add(n.asDouble());
        }
        if (out.get(0) < -180 || out.get(2) > 180 || out.get(1) < -90 || out.get(3) > 90 || out.get(0) >= out.get(2)
                || out.get(1) >= out.get(3)) {
            throw ValidationException.forField(field, "must be a WGS84 box with min below max");
        }
        return out;
    }

    /**
     * Checks a {@code TimeRange} ({@code start} required, {@code end} optional and after it).
     *
     * @return the range as a map, or null
     */
    public static Map<String, Object> timeRange(JsonNode node, String field) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isObject()) {
            throw ValidationException.forField(field, "must be a TimeRange object");
        }
        Instant start = instant(node.get("start"), field + ".start", true);
        Instant end = instant(node.get("end"), field + ".end", false);
        if (end != null && !end.isAfter(start)) {
            throw ValidationException.forField(field + ".end", "must be after start");
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("start", start.toString());
        out.put("end", end == null ? null : end.toString());
        return out;
    }

    private static Instant instant(JsonNode node, String field, boolean required) {
        if (node == null || node.isNull()) {
            if (required) {
                throw ValidationException.forField(field, "is required");
            }
            return null;
        }
        try {
            return Instant.parse(node.asString());
        } catch (DateTimeParseException e) {
            throw ValidationException.forField(field, "must be an ISO-8601 instant");
        }
    }

    /** A JSON value as plain Java (maps, lists, strings, numbers, booleans, null). */
    static Object plain(JsonNode n) {
        if (n == null || n.isNull()) {
            return null;
        }
        if (n.isObject()) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (String k : n.propertyNames()) {
                out.put(k, plain(n.get(k)));
            }
            return out;
        }
        if (n.isArray()) {
            List<Object> out = new ArrayList<>();
            for (JsonNode e : n) {
                out.add(plain(e));
            }
            return out;
        }
        if (n.isBoolean()) {
            return n.asBoolean();
        }
        if (n.isIntegralNumber()) {
            return n.asLong();
        }
        if (n.isNumber()) {
            return n.asDouble();
        }
        return n.asString();
    }
}
