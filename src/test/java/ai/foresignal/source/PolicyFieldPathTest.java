package ai.foresignal.source;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** How {@link SourceService} turns an {@code IngestPolicyValidator} message into an errors[] field. */
class PolicyFieldPathTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource(
            delimiter = '|',
            value = {
                "ingestPolicy.keyframe is required because x has a recordTtl | ingestPolicy.keyframe",
                "ingestPolicy.minInterval (PT1S) must not exceed ingestPolicy.keyframe (PT1M) | ingestPolicy.minInterval",
                "ingestPolicy.minInterval must be at least 2s, but was PT1S | ingestPolicy.minInterval",
                "ingestPolicy.edgeMinInterval must not be negative | ingestPolicy.edgeMinInterval",
                "ingestPolicy.mode SAMPLED requires the descriptor to declare significantFields | ingestPolicy.mode",
                "ingestPolicy.minDeviationMeters must be greater than zero | ingestPolicy.minDeviationMeters",
                "The recordTtl of x (PT10S) is too short for its poll interval | ingestPolicy.keyframe",
                "ingestPolicy.keyframe. trailing dot | ingestPolicy.keyframe",
            })
    @DisplayName("the leading path is the field; messages without one map to ingestPolicy.keyframe")
    void fieldOf(String message, String expected) {
        assertThat(SourceService.policyFieldOf(message.trim())).isEqualTo(expected);
    }

    @org.junit.jupiter.api.Test
    @DisplayName("null maps to ingestPolicy.keyframe")
    void nullMessage() {
        assertThat(SourceService.policyFieldOf(null)).isEqualTo("ingestPolicy.keyframe");
    }
}
