package ai.foresignal.api.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class StreamWriterTest {

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void ndjsonWritesOneObjectPerLine() {
        String body = write(ResponseFormat.NDJSON, List.of(Map.of("a", 1), Map.of("a", 2)), List.of());
        assertThat(body).isEqualTo("{\"a\":1}\n{\"a\":2}\n");
    }

    @Test
    void geojsonSeqPrefixesTheRecordSeparator() {
        Map<String, Object> feature = new LinkedHashMap<>();
        feature.put("type", "Feature");
        feature.put("geometry", RawJson.of("{\"type\":\"Point\",\"coordinates\":[1,2]}"));
        String body = write(ResponseFormat.GEOJSONSEQ, List.of(feature), List.of());
        assertThat(body).isEqualTo("\u001e{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[1,2]}}\n");
    }

    @Test
    void csvWritesAHeaderAndRows() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", "s:1");
        row.put("payload.mag", "4.5");
        row.put("geometry", "POINT(1 2)");
        String body = write(ResponseFormat.CSV, List.of(row), List.of("id", "payload.mag", "geometry"));
        List<String> lines = body.lines().toList();
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).isEqualTo("id,payload.mag,geometry");
        assertThat(lines.get(1)).startsWith("s:1,4.5,").contains("POINT(1 2)");
    }

    @Test
    void refusesPagedFormats() {
        assertThatThrownBy(() -> new StreamWriter(ResponseFormat.JSON, new ByteArrayOutputStream(), json, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private String write(ResponseFormat format, List<Map<String, Object>> items, List<String> columns) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (StreamWriter writer = new StreamWriter(format, out, json, columns)) {
            items.forEach(writer::write);
        }
        return out.toString(StandardCharsets.UTF_8);
    }
}
