package ai.foresignal.api.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.locationtech.jts.geom.Envelope;

class NearParserTest {

    @Test
    void absentMeansNoFilter() {
        assertThat(NearParser.parse(null, null)).isNull();
        assertThat(NearParser.parse("", " ")).isNull();
    }

    @Test
    void parsesLonLatInGeoJsonOrder() {
        NearFilter near = NearParser.parse("-96.80,32.78", "25");
        assertThat(near.lon()).isEqualTo(-96.80);
        assertThat(near.lat()).isEqualTo(32.78);
        assertThat(near.radiusKm()).isEqualTo(25);
        assertThat(near.radiusMeters()).isEqualTo(25_000);
        assertThat(near.canonical()).isEqualTo("-96.8,32.78,25.0");
    }

    @ParameterizedTest
    @CsvSource(value = {
        "-96.8,32.7 | | radiusKm",
        " | 10 | near",
        "1 | 10 | near",
        "1,2,3 | 10 | near",
        "x,1 | 10 | near",
        "-181,0 | 10 | near",
        "0,91 | 10 | near",
        "0,0 | 0 | radiusKm",
        "0,0 | -1 | radiusKm",
        "0,0 | 2000.1 | radiusKm",
        "0,0 | abc | radiusKm",
        "0,0 | NaN | radiusKm"
    }, delimiter = '|')
    void rejects(String near, String radius, String field) {
        assertThatThrownBy(() -> NearParser.parse(near, radius))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors().getFirst().field()).isEqualTo(field));
    }

    @Test
    void acceptsTheMaximumRadius() {
        assertThat(NearParser.parse("0,0", "2000").radiusKm()).isEqualTo(2000);
    }

    @Test
    @DisplayName("the prefilter envelope contains the whole circle")
    void prefilterIsConservative() {
        NearFilter near = new NearFilter(10, 60, 100);
        List<Envelope> envelopes = near.prefilter();
        assertThat(envelopes).hasSize(1);
        Envelope e = envelopes.getFirst();
        // 100 km north is about 0.9 degrees of latitude; at 61 degrees, 100 km east is about 1.86 degrees.
        assertThat(e.getMaxY()).isGreaterThan(60.9);
        assertThat(e.getMinY()).isLessThan(59.1);
        assertThat(e.getMaxX()).isGreaterThan(11.86);
        assertThat(e.getMinX()).isLessThan(8.14);
    }

    @Test
    @DisplayName("a circle across the antimeridian yields two envelopes")
    void prefilterSplitsAtAntimeridian() {
        List<Envelope> east = new NearFilter(179.9, 0, 50).prefilter();
        assertThat(east).hasSize(2);
        assertThat(east.get(0).getMaxX()).isEqualTo(180.0);
        assertThat(east.get(1).getMinX()).isEqualTo(-180.0);
        assertThat(east.get(1).getMaxX()).isGreaterThan(-180.0);

        List<Envelope> west = new NearFilter(-179.9, 0, 50).prefilter();
        assertThat(west).hasSize(2);
        assertThat(west.get(0).getMinX()).isEqualTo(-180.0);
        assertThat(west.get(1).getMaxX()).isEqualTo(180.0);
    }

    @Test
    @DisplayName("near a pole the envelope covers every longitude")
    void prefilterNearPole() {
        List<Envelope> envelopes = new NearFilter(0, 89.5, 100).prefilter();
        assertThat(envelopes).hasSize(1);
        assertThat(envelopes.getFirst().getMinX()).isEqualTo(-180.0);
        assertThat(envelopes.getFirst().getMaxX()).isEqualTo(180.0);
        assertThat(envelopes.getFirst().getMaxY()).isEqualTo(90.0);
    }

    @Test
    void wideRadiusCoversEveryLongitude() {
        List<Envelope> envelopes = new NearFilter(0, 70, 2000).prefilter();
        assertThat(envelopes).hasSize(1);
        assertThat(envelopes.getFirst().getWidth()).isEqualTo(360.0);
    }
}
