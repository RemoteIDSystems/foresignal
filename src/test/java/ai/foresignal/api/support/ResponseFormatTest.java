package ai.foresignal.api.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.ValidationException;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResponseFormatTest {

    private static final List<ResponseFormat> ALL = List.of(ResponseFormat.values());

    @Test
    void formatParameterWinsOverAccept() {
        assertThat(ResponseFormat.negotiate("json", "application/geo+json", ALL)).isEqualTo(ResponseFormat.JSON);
        assertThat(ResponseFormat.negotiate("GeoJSON", null, ALL)).isEqualTo(ResponseFormat.GEOJSON);
    }

    @Test
    void acceptSelectsWhenNoFormat() {
        assertThat(ResponseFormat.negotiate(null, "application/geo+json", ALL)).isEqualTo(ResponseFormat.GEOJSON);
        assertThat(ResponseFormat.negotiate(null, "application/x-ndjson", ALL)).isEqualTo(ResponseFormat.NDJSON);
        assertThat(ResponseFormat.negotiate(null, "text/csv;q=0.5, application/geo+json-seq", ALL))
                .isEqualTo(ResponseFormat.GEOJSONSEQ);
        assertThat(ResponseFormat.negotiate(null, "*/*", ALL)).isEqualTo(ResponseFormat.JSON);
        assertThat(ResponseFormat.negotiate(null, "not a media type", ALL)).isEqualTo(ResponseFormat.JSON);
        assertThat(ResponseFormat.negotiate(null, null, ALL)).isEqualTo(ResponseFormat.JSON);
    }

    @Test
    void rejectsAFormatTheEndpointLacks() {
        assertThatThrownBy(() -> ResponseFormat.negotiate("csv", null, List.of(ResponseFormat.JSON, ResponseFormat.GEOJSON)))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ResponseFormat.negotiate("xml", null, ALL)).isInstanceOf(ValidationException.class);
    }

    @Test
    void streamingFlags() {
        assertThat(ResponseFormat.NDJSON.streaming()).isTrue();
        assertThat(ResponseFormat.GEOJSONSEQ.mediaType()).isEqualTo("application/geo+json-seq");
        assertThat(ResponseFormat.JSON.streaming()).isFalse();
        assertThat(ResponseFormat.CSV.wireValue()).isEqualTo("csv");
    }
}
