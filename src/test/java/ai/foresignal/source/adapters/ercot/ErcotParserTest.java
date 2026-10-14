package ai.foresignal.source.adapters.ercot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link ErcotParser} (prompt 093): the report envelope mapped by {@code fields[].name}, never by position; object
 * rows accepted; a body without fields is a format error.
 */
class ErcotParserTest {

    @Test
    @DisplayName("load report: columns mapped by fields[].name")
    void load() {
        ErcotParser.Envelope envelope = ErcotParser.parse(AdapterFixtures.bytes("ercot-public", "load.json"));
        assertThat(envelope.fields()).contains("operatingDay", "hourEnding", "coast", "total");
        assertThat(envelope.rows()).hasSize(2);
        Map<String, Object> row = envelope.rows().get(0);
        assertThat(row).containsEntry("operatingDay", "2026-10-12").containsEntry("hourEnding", 1.0)
                .containsEntry("coast", 3740.0).containsEntry("total", 33020.0);
    }

    @Test
    @DisplayName("spp report: settlement point and price mapped by name")
    void spp() {
        ErcotParser.Envelope envelope = ErcotParser.parse(AdapterFixtures.bytes("ercot-public", "spp.json"));
        assertThat(envelope.rows().get(0)).containsEntry("settlementPoint", "HB_HOUSTON")
                .containsEntry("settlementPointPrice", 24.51);
    }

    @Test
    @DisplayName("object rows are accepted, and a missing fields array is a format error")
    void objectsAndErrors() {
        ErcotParser.Envelope envelope = ErcotParser.parse(
                "{\"fields\":[{\"name\":\"a\"}],\"data\":[{\"a\":1}]}".getBytes(StandardCharsets.UTF_8));
        assertThat(envelope.rows().get(0)).containsEntry("a", 1.0);
        assertThatThrownBy(() -> ErcotParser.parse("{\"data\":[]}".getBytes(StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("descriptor: three credentials required, 15-minute interval, series kinds")
    void descriptor() {
        ErcotAdapter adapter = new ErcotAdapter("", "");
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.requiredCredentialFields()).extracting(c -> c.name())
                .containsExactlyInAnyOrder("username", "password", "subscriptionKey");
        assertThat(d.defaultInterval()).isEqualTo(java.time.Duration.ofMinutes(15));
    }
}
