package ai.foresignal.source.adapters.carbon;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import ai.foresignal.testsupport.adapters.FieldSpecContract;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link CarbonIntensityParser} and the GB carbon-intensity descriptor (prompt 093): the national actual, the
 * regional forecasts (no actual) and the generation mix.
 */
class CarbonIntensityParserTest {

    @Test
    @DisplayName("the national point uses the actual when present, with the forecast and index in extra")
    void national() {
        List<NormalizedRecord> records =
                CarbonIntensityParser.national(AdapterFixtures.bytes("gb-carbon-intensity", "intensity.json"));
        assertThat(records).hasSize(1);
        NormalizedRecord point = records.get(0);
        assertThat(point.externalId()).isEqualTo("carbon:gb:national:2026-10-14T20:00:00Z");
        assertThat(point.payload()).containsEntry("value", 97.0).containsEntry("status", "final")
                .containsEntry("unit", "g/(kW.h)").containsEntry("series", "carbon:gb:national");
        assertThat(point.eventTime()).isEqualTo(Instant.parse("2026-10-14T20:00:00Z"));
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> extra = (java.util.Map<String, Object>) point.payload().get("extra");
        assertThat(extra).containsEntry("forecast", 103.0).containsEntry("index", "moderate");
        FieldSpecContract.assertPayloadKeysDeclared(new CarbonIntensityAdapter("").descriptor(), records);
    }

    @Test
    @DisplayName("regional points carry the forecast only and the shortname in extra")
    void regional() {
        List<NormalizedRecord> records =
                CarbonIntensityParser.regional(AdapterFixtures.bytes("gb-carbon-intensity", "regional.json"));
        assertThat(records).hasSize(2);
        NormalizedRecord region = records.get(0);
        assertThat(region.externalId()).isEqualTo("carbon:gb:region:1:2026-10-14T20:00:00Z");
        assertThat(region.payload()).containsEntry("value", 0.0).containsEntry("status", "forecast");
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> extra = (java.util.Map<String, Object>) region.payload().get("extra");
        assertThat(extra).containsEntry("shortname", "North Scotland");
    }

    @Test
    @DisplayName("the generation mix is one point whose extra holds the fuel percentages")
    void generation() {
        List<NormalizedRecord> records =
                CarbonIntensityParser.generation(AdapterFixtures.bytes("gb-carbon-intensity", "generation.json"));
        assertThat(records).hasSize(1);
        NormalizedRecord mix = records.get(0);
        assertThat(mix.externalId()).isEqualTo("genmix:gb:2026-10-14T20:00:00Z");
        assertThat(mix.payload()).doesNotContainKey("value");
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> extra = (java.util.Map<String, Object>) mix.payload().get("extra");
        assertThat(extra).containsEntry("wind", 52.5).containsEntry("gas", 23.6);
    }

    @Test
    @DisplayName("descriptor: no key, RECEIVED time base, half-hourly")
    void descriptor() {
        CarbonIntensityAdapter adapter = new CarbonIntensityAdapter("");
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.credentialFields()).isEmpty();
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.RECEIVED);
        assertThat(d.defaultInterval()).isEqualTo(java.time.Duration.ofMinutes(30));
        assertThat(d.licence()).isEqualTo("CC BY 4.0");
    }
}
