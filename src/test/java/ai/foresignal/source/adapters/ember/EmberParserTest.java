package ai.foresignal.source.adapters.ember;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link EmberParser} (prompt 093): monthly generation rows, the generation-mix share, the carbon-intensity and
 * demand datasets, and the options month.
 */
class EmberParserTest {

    @Test
    @DisplayName("four generation rows with the share in extra")
    void generation() {
        List<NormalizedRecord> records = EmberParser.parse(
                AdapterFixtures.bytes("ember-electricity", "generation.json"), EmberParser.GENERATION);
        assertThat(records).hasSize(4);
        NormalizedRecord usaWind = records.get(0);
        assertThat(usaWind.externalId()).isEqualTo("ember:USA:Wind:2026-08");
        assertThat(usaWind.payload()).containsEntry("series", "ember:USA:Wind").containsEntry("value", 45.2)
                .containsEntry("unit", "TW.h").containsEntry("frequency", "P1M");
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> extra = (java.util.Map<String, Object>) usaWind.payload().get("extra");
        assertThat(extra).containsEntry("sharePct", 10.1);
        FieldSpecContract.assertPayloadKeysDeclared(new EmberAdapter("").descriptor(), records);
    }

    @Test
    @DisplayName("the carbon-intensity and demand datasets map their own value field")
    void otherDatasets() {
        String carbon = "{\"data\":[{\"entity\":\"United States\",\"entity_code\":\"USA\",\"date\":\"2026-08\","
                + "\"carbon_intensity_g_kwh\":380.5}]}";
        NormalizedRecord c = EmberParser.parse(carbon.getBytes(StandardCharsets.UTF_8),
                EmberParser.CARBON_INTENSITY).get(0);
        assertThat(c.externalId()).isEqualTo("ember:USA:carbon-intensity:2026-08");
        assertThat(c.payload()).containsEntry("value", 380.5).containsEntry("unit", "g/(kW.h)");
        String demand = "{\"data\":[{\"entity\":\"USA\",\"entity_code\":\"USA\",\"date\":\"2026-08\","
                + "\"electricity_demand_twh\":420.0}]}";
        NormalizedRecord d = EmberParser.parse(demand.getBytes(StandardCharsets.UTF_8), EmberParser.DEMAND).get(0);
        assertThat(d.payload()).containsEntry("value", 420.0).containsEntry("unit", "TW.h");
    }

    @Test
    @DisplayName("the options answer's newest month is the change marker")
    void options() {
        assertThat(EmberParser.newestMonth(AdapterFixtures.bytes("ember-electricity", "options.json")))
                .isEqualTo("2026-08");
    }

    @Test
    @DisplayName("a body without a data array is a format error; the descriptor needs a key")
    void errorsAndDescriptor() {
        assertThatThrownBy(() -> EmberParser.parse("{}".getBytes(StandardCharsets.UTF_8),
                EmberParser.GENERATION)).isInstanceOf(UpstreamFormatException.class);
        EmberAdapter adapter = new EmberAdapter("");
        SourceDescriptor descriptor = adapter.descriptor();
        assertThat(DescriptorValidator.problems(descriptor, adapter)).isEmpty();
        assertThat(descriptor.requiredCredentialFields()).extracting(c -> c.name()).containsExactly("apiKey");
        assertThat(descriptor.licence()).isEqualTo("CC BY 4.0");
    }
}
