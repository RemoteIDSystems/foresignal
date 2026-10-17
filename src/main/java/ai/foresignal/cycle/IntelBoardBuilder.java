package ai.foresignal.cycle;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.json.JsonMapper;

/**
 * Assembles the intel board (prompt 128 §6, {@code IntelBoard}) and keeps it within {@value #MAX_BYTES} bytes of JSON.
 * Pure. The truncation order, applied until the document fits, is:
 *
 * <ol>
 *   <li>every text (titles, questions, labels, descriptions, headlines) cut to {@value #TEXT} characters;</li>
 *   <li>items dropped from the end of {@code indicatorsObserved}, then {@code bandChanges}, then
 *       {@code forecastsDue}, then {@code situations} (down to one each);</li>
 *   <li>{@code benchmarks} reduced to the components that are not {@code pass}, then emptied;</li>
 *   <li>the headlines of {@code latestAssessments} removed;</li>
 *   <li>the remaining items of the four lists dropped, in the same order.</li>
 * </ol>
 *
 * Each section shortened is named once in {@code truncated}.
 */
public final class IntelBoardBuilder {

    /** The size limit, bytes of UTF-8 JSON. */
    public static final int MAX_BYTES = 8 * 1024;

    /** The longest text kept once truncation starts. */
    static final int TEXT = 120;

    /** The most situations. */
    public static final int MAX_SITUATIONS = 5;

    private static final List<String> LISTS = List.of("indicatorsObserved", "bandChanges", "forecastsDue",
            "situations");

    private IntelBoardBuilder() {}

    /**
     * The board's inputs.
     *
     * @param generatedAt when it was built
     * @param situations the top situations (at most five used)
     * @param bandChanges band changes of the last 24 h, newest first
     * @param indicatorsObserved indicators observed in the last 24 h, newest first
     * @param forecastsDue questions due in the next 7 days, soonest first
     * @param latestAssessments the latest assessment per cadence
     * @param calibration the 90-day calibration (ensemble and operator), or null
     * @param benchmarks the latest benchmark status per component
     */
    public record Inputs(Instant generatedAt, List<Map<String, Object>> situations,
            List<Map<String, Object>> bandChanges, List<Map<String, Object>> indicatorsObserved,
            List<Map<String, Object>> forecastsDue, List<Map<String, Object>> latestAssessments,
            Map<String, Object> calibration, List<Map<String, Object>> benchmarks) {}

    /** The board, within {@link #MAX_BYTES}. */
    public static Map<String, Object> build(Inputs in, JsonMapper json) {
        Map<String, Object> board = new LinkedHashMap<>();
        board.put("generatedAt", in.generatedAt().toString());
        board.put("situations", copy(in.situations(), MAX_SITUATIONS));
        board.put("bandChanges", copy(in.bandChanges(), Integer.MAX_VALUE));
        board.put("indicatorsObserved", copy(in.indicatorsObserved(), Integer.MAX_VALUE));
        board.put("forecastsDue", copy(in.forecastsDue(), Integer.MAX_VALUE));
        board.put("latestAssessments", copy(in.latestAssessments(), Integer.MAX_VALUE));
        board.put("calibration", in.calibration());
        board.put("benchmarks", copy(in.benchmarks(), Integer.MAX_VALUE));
        List<String> truncated = new ArrayList<>();
        board.put("truncated", truncated);
        if (fits(board, json)) {
            return board;
        }
        for (String key : List.of("situations", "bandChanges", "indicatorsObserved", "forecastsDue",
                "latestAssessments")) {
            if (shortenTexts(list(board, key))) {
                note(truncated, key);
            }
        }
        for (String key : LISTS) {
            List<Map<String, Object>> items = list(board, key);
            while (items.size() > 1 && !fits(board, json)) {
                items.remove(items.size() - 1);
                note(truncated, key);
            }
        }
        List<Map<String, Object>> benchmarks = list(board, "benchmarks");
        if (!fits(board, json) && benchmarks.removeIf(b -> "pass".equals(b.get("status")))) {
            note(truncated, "benchmarks");
        }
        if (!fits(board, json) && !benchmarks.isEmpty()) {
            benchmarks.clear();
            note(truncated, "benchmarks");
        }
        if (!fits(board, json)) {
            for (Map<String, Object> a : list(board, "latestAssessments")) {
                a.put("headline", null);
            }
            note(truncated, "latestAssessments");
        }
        for (String key : LISTS) {
            List<Map<String, Object>> items = list(board, key);
            while (!items.isEmpty() && !fits(board, json)) {
                items.remove(items.size() - 1);
                note(truncated, key);
            }
        }
        return board;
    }

    /** The UTF-8 JSON size of {@code board}. */
    public static int size(Map<String, Object> board, JsonMapper json) {
        return json.writeValueAsString(board).getBytes(StandardCharsets.UTF_8).length;
    }

    private static boolean fits(Map<String, Object> board, JsonMapper json) {
        return size(board, json) <= MAX_BYTES;
    }

    private static boolean shortenTexts(List<Map<String, Object>> items) {
        boolean changed = false;
        for (Map<String, Object> item : items) {
            for (Map.Entry<String, Object> e : item.entrySet()) {
                if (e.getValue() instanceof String s && s.length() > TEXT && !e.getKey().endsWith("Id")
                        && !e.getKey().endsWith("At")) {
                    e.setValue(s.substring(0, TEXT - 1) + "…");
                    changed = true;
                }
            }
        }
        return changed;
    }

    private static void note(List<String> truncated, String key) {
        if (!truncated.contains(key)) {
            truncated.add(key);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> list(Map<String, Object> board, String key) {
        return (List<Map<String, Object>>) board.get(key);
    }

    private static List<Map<String, Object>> copy(List<Map<String, Object>> in, int max) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (in == null) {
            return out;
        }
        for (Map<String, Object> m : in) {
            if (out.size() >= max) {
                break;
            }
            out.add(new LinkedHashMap<>(m));
        }
        return out;
    }
}
