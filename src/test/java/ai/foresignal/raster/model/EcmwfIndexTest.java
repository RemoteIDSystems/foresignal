package ai.foresignal.raster.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.raster.RasterFormatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The ECMWF {@code .index} parser (prompt 105). */
class EcmwfIndexTest {

    private static final String BODY = """
            {"domain": "g", "date": "20261015", "time": "0000", "stream": "oper", "step": "6", "levtype": "sfc", "param": "tp", "_offset": 0, "_length": 596597}
            {"domain": "g", "date": "20261015", "time": "0000", "stream": "oper", "step": "6", "levtype": "sfc", "param": "10u", "_offset": 9967974, "_length": 869184}
            {"domain": "g", "date": "20261015", "time": "0000", "stream": "oper", "step": "6", "levtype": "sfc", "param": "10v", "_offset": 12250976, "_length": 863820}
            {"domain": "g", "date": "20261015", "time": "0000", "stream": "oper", "step": "6", "levelist": "500", "levtype": "pl", "param": "t", "_offset": 20000000, "_length": 1000}
            not json at all
            {"domain": "g", "param": "no-offset"}
            """;

    @Test
    @DisplayName("parses the valid JSON lines and skips malformed ones")
    void parses() {
        EcmwfIndex index = EcmwfIndex.parse(BODY);
        assertThat(index.entries()).hasSize(4);
        assertThat(index.entries().get(0).param()).isEqualTo("tp");
        assertThat(index.entries().get(0).offset()).isZero();
        assertThat(index.entries().get(0).length()).isEqualTo(596597);
        assertThat(index.entries().get(0).end()).isEqualTo(596596);
        assertThat(index.entries().get(3).levelist()).isEqualTo("500");
        assertThat(index.entries().get(3).levtype()).isEqualTo("pl");
    }

    @Test
    @DisplayName("finds a field by param, levtype and levelist, the first match winning")
    void finds() {
        EcmwfIndex index = EcmwfIndex.parse(BODY);
        assertThat(index.find("10u", "sfc", null)).get().extracting(EcmwfIndex.Entry::offset).isEqualTo(9967974);
        assertThat(index.find("t", "pl", "500")).isPresent();
        assertThat(index.find("t", "pl", "850")).isEmpty();
        assertThat(index.find("nope", "sfc", null)).isEmpty();
    }

    @Test
    @DisplayName("folds the step-qualified gust names onto 10fg")
    void normalizesGust() {
        assertThat(EcmwfIndex.normalizeParam("10fg")).isEqualTo("10fg");
        assertThat(EcmwfIndex.normalizeParam("10fg3")).isEqualTo("10fg");
        assertThat(EcmwfIndex.normalizeParam("10fg6")).isEqualTo("10fg");
        assertThat(EcmwfIndex.normalizeParam("2t")).isEqualTo("2t");
        String body = "{\"param\": \"10fg3\", \"levtype\": \"sfc\", \"step\": \"144\", \"_offset\": 10, \"_length\": 20}";
        assertThat(EcmwfIndex.parse(body).find("10fg", "sfc", null)).isPresent();
    }

    @Test
    @DisplayName("refuses a body with no valid line")
    void refusesEmpty() {
        assertThatThrownBy(() -> EcmwfIndex.parse("an HTML error page"))
                .isInstanceOf(RasterFormatException.class)
                .hasMessageContaining("no valid JSON entry");
        assertThatThrownBy(() -> EcmwfIndex.parse(""))
                .isInstanceOf(RasterFormatException.class);
    }

    @Test
    @DisplayName("labels an entry as PARAM:LEVTYPE[:LEVELIST]")
    void labels() {
        EcmwfIndex index = EcmwfIndex.parse(BODY);
        assertThat(index.entries().get(0).label()).isEqualTo("tp:sfc");
        assertThat(index.entries().get(3).label()).isEqualTo("t:pl:500");
    }
}
