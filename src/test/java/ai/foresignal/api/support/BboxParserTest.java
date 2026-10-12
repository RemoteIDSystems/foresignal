package ai.foresignal.api.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Envelope;

class BboxParserTest {

    @Test
    void absentMeansNoFilter() {
        assertThat(BboxParser.parse(null)).isNull();
        assertThat(BboxParser.parse("  ")).isNull();
    }

    @Test
    void parsesOneBox() {
        BboxFilter filter = BboxParser.parse("-97.5, 32.5,-96.5,33.5");
        assertThat(filter.envelopes()).containsExactly(new Envelope(-97.5, -96.5, 32.5, 33.5));
        assertThat(filter.crossesAntimeridian()).isFalse();
        assertThat(filter.contains(-97, 33)).isTrue();
        assertThat(filter.contains(-95, 33)).isFalse();
    }

    @Test
    @DisplayName("minLon > maxLon crosses the antimeridian and is split into two boxes")
    void splitsAtAntimeridian() {
        BboxFilter filter = BboxParser.parse("170,-10,-170,10");
        assertThat(filter.crossesAntimeridian()).isTrue();
        assertThat(filter.envelopes()).containsExactly(
                new Envelope(170, 180, -10, 10), new Envelope(-180, -170, -10, 10));
        assertThat(filter.contains(175, 0)).isTrue();
        assertThat(filter.contains(-175, 0)).isTrue();
        assertThat(filter.contains(0, 0)).isFalse();
        assertThat(filter.canonical()).contains("|");
    }

    @Test
    void acceptsTheWholeWorld() {
        assertThat(BboxParser.parse("-180,-90,180,90").envelopes()).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1,2,3", "1,2,3,4,5", "a,2,3,4", "-181,0,10,10", "0,0,181,10", "0,-91,10,10", "0,0,10,91",
        "0,10,10,10", "0,20,10,10", "NaN,0,1,1", "0,0,Infinity,1", ",,,"
    })
    void rejectsMalformedOrOutOfRange(String value) {
        assertThatThrownBy(() -> BboxParser.parse(value))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors().getFirst().field()).isEqualTo("bbox"));
    }
}
