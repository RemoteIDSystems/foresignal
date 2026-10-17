package ai.foresignal.quality;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.backtest.AlertQualityService;
import ai.foresignal.backtest.TuningSuggester;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * The 115 suggestion kinds of {@link TuningSuggester} 1.1.0: {@code ACTIVE_WINDOW}, {@code SEVERITY_UP} and
 * {@code SEVERITY_DOWN} fire on their fixtures, stay silent below the sample minimums, and are deterministic.
 */
class TuningSuggesterExtensionTest {

    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Instant DAY = Instant.parse("2026-09-01T00:00:00Z");

    static AlertQualityService.Labeled labeled(String feedback, int hour) {
        String cls = AlertQualityService.FEEDBACK_CLASSES.get(feedback);
        return new AlertQualityService.Labeled(cls, null, feedback, DAY.plusSeconds(hour * 3600L + 60));
    }

    static List<Map<String, Object>> suggest(List<AlertQualityService.Labeled> labeled, String severity) {
        return TuningSuggester.suggest(new TuningSuggester.Inputs(Map.of(), List.of(), false, false, null, labeled,
                severity), JSON);
    }

    static List<String> kinds(List<Map<String, Object>> suggestions) {
        return suggestions.stream().map(s -> String.valueOf(s.get("kind"))).toList();
    }

    /** Positives in office hours (09–16 UTC), negatives at night (01–04 UTC). */
    static List<AlertQualityService.Labeled> dayAndNight(int positives, int negatives) {
        List<AlertQualityService.Labeled> out = new ArrayList<>();
        for (int i = 0; i < positives; i++) {
            out.add(labeled("useful", 9 + i % 8));
        }
        for (int i = 0; i < negatives; i++) {
            out.add(labeled("noise", 1 + i % 4));
        }
        return out;
    }

    @Test
    @DisplayName("ACTIVE_WINDOW: negatives in hours without positives give an active window over the other hours")
    @SuppressWarnings("unchecked")
    void activeWindow() {
        List<Map<String, Object>> suggestions = suggest(dayAndNight(8, 8), "warning");
        Map<String, Object> window = suggestions.stream().filter(s -> "ACTIVE_WINDOW".equals(s.get("kind")))
                .findFirst().orElseThrow();
        Map<String, Object> patch = (Map<String, Object>) window.get("patch");
        Map<String, Object> active = (Map<String, Object>) patch.get("activeWindow");
        assertThat(active).containsEntry("timezone", "UTC");
        List<Map<String, Object>> windows = (List<Map<String, Object>>) active.get("windows");
        assertThat(windows).isNotEmpty();
        assertThat(windows).allSatisfy(w -> assertThat((List<String>) w.get("days")).hasSize(7));
        Map<String, Object> effect = (Map<String, Object>) window.get("expectedEffect");
        assertThat(effect).containsEntry("removedNegatives", 8).containsEntry("keptPositives", 8);
    }

    @Test
    @DisplayName("ACTIVE_WINDOW stays silent below five positives or negatives, or when negatives share the positives' hours")
    void activeWindowSilent() {
        assertThat(kinds(suggest(dayAndNight(4, 8), "warning"))).doesNotContain("ACTIVE_WINDOW");
        assertThat(kinds(suggest(dayAndNight(8, 4), "warning"))).doesNotContain("ACTIVE_WINDOW");
        List<AlertQualityService.Labeled> mixed = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            mixed.add(labeled("useful", 9 + i % 8));
            mixed.add(labeled("noise", 9 + i % 8));
        }
        assertThat(kinds(suggest(mixed, "warning"))).doesNotContain("ACTIVE_WINDOW");
    }

    @Test
    @DisplayName("SEVERITY_UP: late ≥ 30% of ≥ 10 labels on info or warning; not on critical; not below 10 labels")
    @SuppressWarnings("unchecked")
    void severityUp() {
        List<AlertQualityService.Labeled> labels = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            labels.add(labeled("useful", 12));
        }
        for (int i = 0; i < 3; i++) {
            labels.add(labeled("late", 12));
        }
        Map<String, Object> up = suggest(labels, "info").stream().filter(s -> "SEVERITY_UP".equals(s.get("kind")))
                .findFirst().orElseThrow();
        assertThat((Map<String, Object>) up.get("patch")).containsEntry("severity", "warning");
        assertThat(suggest(labels, "warning").stream().filter(s -> "SEVERITY_UP".equals(s.get("kind")))
                .map(s -> ((Map<String, Object>) s.get("patch")).get("severity")).toList()).containsExactly("critical");
        assertThat(kinds(suggest(labels, "critical"))).doesNotContain("SEVERITY_UP");
        assertThat(kinds(suggest(labels.subList(0, 9), "info"))).doesNotContain("SEVERITY_UP");
    }

    @Test
    @DisplayName("SEVERITY_DOWN: noise ≥ 50% and no useful on a critical rule proposes warning")
    @SuppressWarnings("unchecked")
    void severityDown() {
        List<AlertQualityService.Labeled> labels = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            labels.add(labeled("noise", 12));
        }
        for (int i = 0; i < 4; i++) {
            labels.add(labeled("false_positive", 12));
        }
        Map<String, Object> down = suggest(labels, "critical").stream()
                .filter(s -> "SEVERITY_DOWN".equals(s.get("kind"))).findFirst().orElseThrow();
        assertThat((Map<String, Object>) down.get("patch")).containsEntry("severity", "warning");
        List<AlertQualityService.Labeled> withUseful = new ArrayList<>(labels);
        withUseful.add(labeled("useful", 12));
        assertThat(kinds(suggest(withUseful, "critical"))).doesNotContain("SEVERITY_DOWN");
        assertThat(kinds(suggest(labels, "warning"))).doesNotContain("SEVERITY_DOWN");
    }

    @Test
    @DisplayName("deterministic: the same inputs give the same suggestions; the version is 1.1.0")
    void deterministic() {
        List<AlertQualityService.Labeled> labels = dayAndNight(9, 9);
        assertThat(suggest(labels, "critical")).isEqualTo(suggest(new ArrayList<>(labels), "critical"));
        assertThat(TuningSuggester.VERSION).isEqualTo("1.1.0");
        assertThat(suggest(List.of(), null)).isEmpty();
    }
}
