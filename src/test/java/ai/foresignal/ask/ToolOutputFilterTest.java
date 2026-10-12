package ai.foresignal.ask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.api.support.BboxParser;
import ai.foresignal.source.SourceRegistry;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.StringNode;

/** {@link ToolOutputFilter}: withheld sources and out-of-area items are removed before the model sees them. */
class ToolOutputFilterTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    ToolOutputFilter filter;

    @BeforeEach
    void setUp() {
        SourceRegistry registry = mock(SourceRegistry.class);
        when(registry.contains(anyString())).thenAnswer(i -> Set.of("pub", "priv").contains(i.<String>getArgument(0)));
        filter = new ToolOutputFilter(registry, JSON);
    }

    @Test
    @DisplayName("list items of a withheld source are dropped, by source, id, docId or record")
    void dropsWithheldSources() {
        JsonNode output = JSON.readTree("""
                {"items":[{"id":"pub:1","seq":1},{"id":"priv:2","seq":2},{"source":"priv","externalId":"3"},
                 {"docType":"record","docId":"priv:4","seq":4},{"id":7,"ruleId":"r","record":"priv:5"},
                 {"docType":"briefing","docId":"b1"}]}""");

        ToolOutputFilter.Filtered out = filter.filter(output, source -> !source.equals("priv"), null);

        assertThat(out.removed()).isEqualTo(4);
        assertThat(out.output().get("items")).hasSize(2);
        assertThat(out.output().toString()).doesNotContain("priv");
    }

    @Test
    @DisplayName("items outside the area are dropped; a withheld top-level object becomes a notice")
    void areaAndTopLevel() {
        JsonNode list = JSON.readTree("""
                {"items":[{"id":"pub:1","lon":139.8,"lat":35.6},{"id":"pub:2","lon":-122.4,"lat":37.8},
                 {"id":"pub:3","geometry":{"type":"Point","coordinates":[-122.4,37.8]}}]}""");
        ToolOutputFilter.Filtered out = filter.filter(list, source -> true, BboxParser.parse("130,30,150,40"));
        assertThat(out.output().get("items")).hasSize(1);

        JsonNode single = JSON.readTree("{\"id\":\"priv:9\",\"seq\":9,\"title\":\"Hidden\"}");
        ToolOutputFilter.Filtered withheld = filter.filter(single, source -> !source.equals("priv"), null);
        assertThat(withheld.output().get("withheld").asBoolean()).isTrue();
        assertThat(withheld.output().toString()).doesNotContain("Hidden");
    }

    @Test
    @DisplayName("nested objects of a withheld source become null; text output passes unchanged")
    void nestedAndText() {
        JsonNode alert = JSON.readTree("{\"id\":1,\"ruleId\":\"r\",\"record\":{\"id\":\"priv:1\",\"title\":\"x\"}}");
        JsonNode out = filter.filter(alert, source -> !source.equals("priv"), null).output();
        assertThat(out.get("record").isNull()).isTrue();

        JsonNode text = StringNode.valueOf("plain");
        assertThat(filter.filter(text, source -> false, null).output()).isEqualTo(text);
    }
}
