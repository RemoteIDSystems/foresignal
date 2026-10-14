package ai.foresignal.source.adapters.eia;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link EiaParser} and the EIA adapters' descriptors (prompt 093): the envelope, numeric-string values, the
 * incomplete-return warning, the units mapping and the descriptor defaults.
 */
class EiaParserTest {

    @Test
    @DisplayName("spot envelope: three rows, string values parsed, frequency and total")
    void spots() {
        EiaParser.Envelope envelope = EiaParser.parse(AdapterFixtures.bytes("eia-petroleum", "spot.json"));
        assertThat(envelope.data()).hasSize(3);
        assertThat(envelope.frequency()).isEqualTo("daily");
        assertThat(envelope.total()).isEqualTo(3);
        assertThat(EiaParser.value(envelope.data().get(0))).isEqualTo(61.23);
        assertThat(EiaParser.units(envelope.data().get(0))).isEqualTo("$ per barrel");
        assertThat(EiaApi.ucum(EiaParser.units(envelope.data().get(0)))).isEqualTo("[USD]/[bbl_us]");
    }

    @Test
    @DisplayName("an incomplete return is a format error, never a silently truncated window")
    void incomplete() {
        assertThatThrownBy(() -> EiaParser.parse(
                AdapterFixtures.bytes("eia-petroleum", "incomplete.json")))
                .isInstanceOf(UpstreamFormatException.class)
                .hasMessageContaining("5,000-row cap");
    }

    @Test
    @DisplayName("value-units wins over units (EIA-930), and a bad body is a format error")
    void unitsAndErrors() {
        String body = "{\"response\":{\"data\":[{\"period\":\"x\",\"value\":\"1.5\",\"value-units\":\"megawatthours\"}]}}";
        EiaParser.Envelope envelope = EiaParser.parse(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertThat(EiaParser.units(envelope.data().get(0))).isEqualTo("megawatthours");
        assertThat(EiaApi.ucum("megawatthours")).isEqualTo("MW.h");
        assertThatThrownBy(() -> EiaParser.parse("not json".getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> EiaParser.parse("{\"error\":{\"code\":\"API_KEY_MISSING\"}}"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .isInstanceOf(UpstreamFormatException.class);
    }

    @Test
    @DisplayName("numeric strings: a blank, non-numeric or NaN value is null, never a poisoned number")
    void numbers() {
        assertThat(ai.foresignal.source.series.SeriesPoint.number("61.23")).isEqualTo(61.23);
        assertThat(ai.foresignal.source.series.SeriesPoint.number("")).isNull();
        assertThat(ai.foresignal.source.series.SeriesPoint.number("NaN")).isNull();
        assertThat(ai.foresignal.source.series.SeriesPoint.number("Infinity")).isNull();
        assertThat(ai.foresignal.source.series.SeriesPoint.number(Double.NaN)).isNull();
        assertThat(ai.foresignal.source.series.SeriesPoint.number(null)).isNull();
    }

    @Test
    @DisplayName("petroleum descriptor: LOCAL_ONLY (LSEG spot), key required, daily/weekly series kinds")
    void petroleumDescriptor() {
        EiaPetroleumAdapter adapter = new EiaPetroleumAdapter("");
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(d.licence()).isEqualTo("US-PD");
        assertThat(d.requiredCredentialFields()).extracting(c -> c.name()).containsExactly("apiKey");
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
        assertThat(d.kindRetention()).isEmpty();
        List<String> kinds = List.copyOf(d.kinds());
        assertThat(kinds).containsExactlyInAnyOrder("series-point", "series-latest");
    }

    @Test
    @DisplayName("eia-930 descriptor: RECEIVED time base, fuel mix on by default")
    void eia930Descriptor() {
        Eia930Adapter adapter = new Eia930Adapter("");
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).isEmpty();
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.RECEIVED);
        assertThat(d.requiredCredentialFields()).extracting(c -> c.name()).containsExactly("apiKey");
        assertThat(d.params()).extracting(p -> p.name()).contains("respondents", "fuelMix");
    }
}
