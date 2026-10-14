package ai.foresignal.source.adapters.fingrid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.series.FrequencyExcursionDetector;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link FingridFrequencyParser} and the Fingrid grid-frequency descriptor (prompt 093). */
class FingridFrequencyParserTest {

    @Test
    @DisplayName("five samples with their start times")
    void samples() {
        List<FrequencyExcursionDetector.Sample> samples =
                FingridFrequencyParser.parse(AdapterFixtures.bytes("grid-frequency-fingrid", "frequency.json"));
        assertThat(samples).hasSize(5);
        assertThat(samples.get(0).time()).isEqualTo(Instant.parse("2026-10-13T00:00:00Z"));
        assertThat(samples.get(1).hz()).isEqualTo(49.91);
    }

    @Test
    @DisplayName("a body without a data array is a format error")
    void errors() {
        assertThatThrownBy(() -> FingridFrequencyParser.parse("{}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("descriptor: key required, 3 min, 600/day (disabled by default)")
    void descriptor() {
        GridFrequencyFingridAdapter adapter = new GridFrequencyFingridAdapter("");
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.requiredCredentialFields()).extracting(c -> c.name()).containsExactly("apiKey");
        assertThat(d.defaultInterval()).isEqualTo(java.time.Duration.ofMinutes(3));
        assertThat(d.defaultBudget().perDay()).isEqualTo(600);
        assertThat(d.licence()).isEqualTo("CC BY 4.0");
    }
}
