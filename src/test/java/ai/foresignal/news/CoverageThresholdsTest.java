package ai.foresignal.news;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.media.outlet.MediaVocabulary;
import ai.foresignal.media.outlet.OutletRegistry;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

/**
 * The coverage skew/blindspot thresholds and the news reliability numbers are settings (prompt 157): the
 * analyzer honors the thresholds it is given, and {@link NewsReliability} reads its numbers from the setting.
 */
class CoverageThresholdsTest {

    private List<CoverageAnalyzer.GroupInfo> leanRight(int count) {
        List<CoverageAnalyzer.GroupInfo> groups = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            groups.add(new CoverageAnalyzer.GroupInfo(MediaVocabulary.RIGHT, "US", "newspaper", "4", false, 1));
        }
        return groups;
    }

    @Test
    @DisplayName("a lower skew threshold flips a borderline coverage to skewed")
    void thresholdChangesSkewVerdict() {
        // Three right-leaning groups out of four: a 0.75 share.
        List<CoverageAnalyzer.GroupInfo> groups = leanRight(3);
        groups.add(new CoverageAnalyzer.GroupInfo(MediaVocabulary.LEFT, "US", "newspaper", "4", false, 1));

        boolean defaultSkew = CoverageAnalyzer.analyze("story", groups, false, null, Instant.now()).skewed();
        boolean lowered = CoverageAnalyzer.analyze("story", groups, false, null, Instant.now(),
                new CoverageAnalyzer.Thresholds(0.6, 3, 0.6, 8)).skewed();

        assertThat(defaultSkew).isTrue(); // 0.75 meets the default 0.75
        assertThat(lowered).isTrue();     // and still meets 0.6

        // A 0.6 share (3 of 5) is skewed only under the lowered threshold.
        List<CoverageAnalyzer.GroupInfo> five = leanRight(3);
        five.add(new CoverageAnalyzer.GroupInfo(MediaVocabulary.LEFT, "US", "newspaper", "4", false, 1));
        five.add(new CoverageAnalyzer.GroupInfo(MediaVocabulary.CENTER, "US", "newspaper", "4", false, 1));
        assertThat(CoverageAnalyzer.analyze("story", five, false, null, Instant.now()).skewed()).isFalse();
        assertThat(CoverageAnalyzer.analyze("story", five, false, null, Instant.now(),
                new CoverageAnalyzer.Thresholds(0.6, 3, 0.6, 8)).skewed()).isTrue();
    }

    @Test
    @DisplayName("NewsReliability reads its grade numbers from the setting, over the defaults")
    void reliabilityNumbersFromSettings() {
        AppSettingService service = mock(AppSettingService.class);
        when(service.get(eq(SettingKeys.MEDIA_RELIABILITY_NUMBERS), eq(Map.class)))
                .thenReturn(Optional.of(Map.of("A", 0.95, "E", -1.0, "Z", 0.5)));
        @SuppressWarnings("unchecked")
        ObjectProvider<AppSettingService> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(service);
        NewsReliability reliability = new NewsReliability(mock(NewsStoryStore.class), mock(OutletRegistry.class),
                provider);

        Map<String, Double> numbers = reliability.numbers();

        assertThat(numbers.get("A")).isEqualTo(0.95);   // overridden
        assertThat(numbers.get("E")).isEqualTo(0.3);    // out-of-range entry ignored, default kept
        assertThat(numbers).doesNotContainKey("Z");     // unknown grade ignored
        assertThat(reliability.numberOf("A")).isEqualTo(0.95);
    }
}
