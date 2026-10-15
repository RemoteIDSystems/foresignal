package ai.foresignal.backtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The deterministic tuning suggester (prompt 103). */
class TuningSuggesterTest {

    private static TuningSuggester suggester(Map<String, Object> report) {
        AlertQualityService quality = mock(AlertQualityService.class);
        when(quality.forRule(any(UUID.class), any(Instant.class))).thenReturn(report);
        return new TuningSuggester(quality);
    }

    @Test
    @DisplayName("duplicates suggest a cooldown")
    void cooldown() {
        var suggestions = suggester(Map.of("duplicates", 4L, "flapping", 0L)).forRule(UUID.randomUUID(), Instant.now());
        assertThat(suggestions).anyMatch(s -> s.get("kind").equals("add_cooldown") && s.containsKey("patch"));
    }

    @Test
    @DisplayName("flapping suggests enter mode")
    void flapping() {
        var suggestions = suggester(Map.of("duplicates", 0L, "flapping", 3L)).forRule(UUID.randomUUID(), Instant.now());
        assertThat(suggestions).anyMatch(s -> s.get("kind").equals("switch_to_enter"));
    }

    @Test
    @DisplayName("low precision suggests raising the threshold")
    void precision() {
        var suggestions = suggester(Map.of("duplicates", 0L, "flapping", 0L, "precision", 0.3))
                .forRule(UUID.randomUUID(), Instant.now());
        assertThat(suggestions).anyMatch(s -> s.get("kind").equals("raise_threshold"));
        assertThat(suggestions).allMatch(s -> s.containsKey("caveats"));
    }

    @Test
    @DisplayName("a clean rule has no suggestions")
    void clean() {
        var suggestions = suggester(Map.of("duplicates", 0L, "flapping", 0L, "precision", 0.9))
                .forRule(UUID.randomUUID(), Instant.now());
        assertThat(suggestions).isEmpty();
    }
}
