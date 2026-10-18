package ai.foresignal.doctor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * The model's answer, parsed as data (prompt 140, task 5): {@code {summary, likelyCause, confidence, evidence[],
 * suggestedActions[], patch?: {file, unifiedDiff}}}. Lenient about where the JSON object sits (the first balanced object
 * in the text is taken, so prose or a code fence around it is ignored) and strict about what is kept: only these members,
 * each with its type and size enforced, so nothing in the answer can add fields or change the shape.
 *
 * @param summary at most 80 words
 * @param likelyCause one sentence, capped
 * @param confidence 0..1, or null
 * @param evidence up to 10 strings
 * @param suggestedActions up to 10 strings
 * @param patchFile the file a proposed patch targets, or null
 * @param patchDiff the proposed unified diff, or null
 */
public record DoctorLlmOutput(String summary, String likelyCause, Double confidence, List<String> evidence,
        List<String> suggestedActions, String patchFile, String patchDiff) {

    /** The most words a summary keeps. */
    public static final int MAX_SUMMARY_WORDS = 80;

    /** The most items per list. */
    public static final int MAX_ITEMS = 10;

    /** The longest list item kept. */
    public static final int MAX_ITEM_LENGTH = 400;

    /** The longest patch kept. */
    public static final int MAX_PATCH_LENGTH = 20_000;

    /** Copies the lists. */
    public DoctorLlmOutput {
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
        suggestedActions = suggestedActions == null ? List.of() : List.copyOf(suggestedActions);
    }

    /**
     * Parses the first JSON object in {@code content}.
     *
     * @return the output, or null when the text holds no JSON object
     */
    public static DoctorLlmOutput parse(String content, JsonMapper json) {
        String object = firstObject(content);
        if (object == null) {
            return null;
        }
        JsonNode root;
        try {
            root = json.readTree(object);
        } catch (JacksonException e) {
            return null;
        }
        if (root == null || !root.isObject()) {
            return null;
        }
        String summary = words(text(root.get("summary"), 2_000), MAX_SUMMARY_WORDS);
        String cause = StructureExtractor.cap(text(root.get("likelyCause"), 2_000), 600);
        Double confidence = null;
        JsonNode c = root.get("confidence");
        if (c != null && c.isNumber()) {
            confidence = Math.max(0.0, Math.min(1.0, c.asDouble()));
        }
        String file = null;
        String diff = null;
        JsonNode patch = root.get("patch");
        if (patch != null && patch.isObject()) {
            file = StructureExtractor.cap(text(patch.get("file"), 400), 400);
            diff = text(patch.get("unifiedDiff"), MAX_PATCH_LENGTH);
            if (file == null || diff == null || diff.isBlank()) {
                file = null;
                diff = null;
            }
        }
        return new DoctorLlmOutput(summary, cause, confidence, strings(root.get("evidence")),
                strings(root.get("suggestedActions")), file, diff);
    }

    /** The first balanced {@code {…}} in {@code text}, honoring JSON strings; null when there is none. */
    static String firstObject(String text) {
        if (text == null) {
            return null;
        }
        int start = text.indexOf('{');
        while (start >= 0) {
            int depth = 0;
            boolean inString = false;
            for (int i = start; i < text.length(); i++) {
                char ch = text.charAt(i);
                if (inString) {
                    if (ch == '\\') {
                        i++;
                    } else if (ch == '"') {
                        inString = false;
                    }
                    continue;
                }
                if (ch == '"') {
                    inString = true;
                } else if (ch == '{') {
                    depth++;
                } else if (ch == '}') {
                    depth--;
                    if (depth == 0) {
                        return text.substring(start, i + 1);
                    }
                }
            }
            start = text.indexOf('{', start + 1);
        }
        return null;
    }

    private static String text(JsonNode node, int max) {
        if (node == null || node.isNull() || node.isContainer()) {
            return null;
        }
        String value = node.isString() ? node.asString() : node.toString();
        value = value.strip();
        return value.isEmpty() ? null : (value.length() > max ? value.substring(0, max) : value);
    }

    private static List<String> strings(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node == null || !node.isArray()) {
            return out;
        }
        for (JsonNode item : node) {
            String value = text(item, MAX_ITEM_LENGTH * 2);
            if (value != null) {
                out.add(StructureExtractor.cap(value, MAX_ITEM_LENGTH));
            }
            if (out.size() >= MAX_ITEMS) {
                break;
            }
        }
        return out;
    }

    /** {@code text} cut to {@code max} words. */
    static String words(String text, int max) {
        if (text == null) {
            return null;
        }
        String[] parts = text.strip().split("\\s+");
        if (parts.length <= max) {
            return text.strip();
        }
        return String.join(" ", java.util.Arrays.copyOf(parts, max)) + "…";
    }

    /** The members as a map, for the report's {@code llm} block (the patch is added by the caller). */
    public Map<String, Object> asMap() {
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("summary", summary);
        out.put("likelyCause", likelyCause);
        out.put("confidence", confidence);
        out.put("evidence", evidence);
        out.put("suggestedActions", suggestedActions);
        return out;
    }
}
