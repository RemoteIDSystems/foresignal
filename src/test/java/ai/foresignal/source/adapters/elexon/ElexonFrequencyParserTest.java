package ai.foresignal.source.adapters.elexon;

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

/** {@link ElexonFrequencyParser} and the GB grid-frequency descriptor (prompt 093). */
class ElexonFrequencyParserTest {

    @Test
    @DisplayName("eight 15-second samples with their measurement times")
    void samples() {
        List<FrequencyExcursionDetector.Sample> samples =
                ElexonFrequencyParser.parse(AdapterFixtures.bytes("grid-frequency-gb", "frequency.json"));
        assertThat(samples).hasSize(8);
        assertThat(samples.get(0).time()).isEqualTo(Instant.parse("2026-10-13T00:00:00Z"));
        assertThat(samples.get(0).hz()).isEqualTo(50.062);
    }

    @Test
    @DisplayName("a body without a data array is a format error")
    void errors() {
        assertThatThrownBy(() -> ElexonFrequencyParser.parse("{}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("descriptor: no credential, 2 min, 1,500/day, the two grid-frequency kinds")
    void descriptor() {
        GridFrequencyGbAdapter adapter = new GridFrequencyGbAdapter("");
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.credentialFields()).isEmpty();
        assertThat(d.defaultInterval()).isEqualTo(java.time.Duration.ofMinutes(2));
        assertThat(d.defaultBudget().perDay()).isEqualTo(1500);
        assertThat(d.kinds()).containsExactlyInAnyOrder("frequency-excursion", "grid-frequency");
    }
}
