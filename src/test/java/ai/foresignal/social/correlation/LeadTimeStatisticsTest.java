package ai.foresignal.social.correlation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link LeadTimeStatistics#compute}: the training cut-off as the contract gives {@code LeadTimeStats}. */
class LeadTimeStatisticsTest {

    @Test
    @DisplayName("without a comparison the training cut-off is null, not the epoch")
    void noComparisonNoCutoff() {
        Map<String, Object> stats = LeadTimeStatistics.compute(List.of(),
                new LeadTimeStatistics.Filters(null, null, null, null, null), 2.0 / 3.0,
                Instant.parse("2026-10-23T19:00:00Z"));
        assertThat(stats).containsKey("trainingCutoff");
        assertThat(stats.get("trainingCutoff")).isNull();
        assertThat(stats.get("groups")).isEqualTo(List.of());
    }
}
