package ai.foresignal.analyst;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.ask.CitableExtractor;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Exact citation matching (prompt 102, principle 1): a citation is valid only when it names exactly an
 * item a tool returned, never because its text occurs somewhere in tool output.
 */
class AnalystCitationsTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** A record and alert extractor for the shapes used here (the 028 extractors need a source registry). */
    private static final CitableExtractor EXTRACTOR = (tool, output, citable) -> walk(output, citable);

    private static void walk(JsonNode node, ai.foresignal.ask.CitationSet citable) {
        if (node.isArray()) {
            node.forEach(child -> walk(child, citable));
            return;
        }
        if (!node.isObject()) {
            return;
        }
        JsonNode id = node.get("id");
        if (id != null && id.isString() && id.asString().contains(":") && node.path("seq").isIntegralNumber()) {
            citable.addRecord(id.asString(), node.get("seq").asLong(), null);
        }
        if (id != null && id.isIntegralNumber() && node.has("ruleId")) {
            citable.addAlert(id.asString(), null);
        }
        node.forEach(child -> {
            if (child.isContainer()) {
                walk(child, citable);
            }
        });
    }

    private static AnalystCitations seeded() {
        AnalystCitations citations = new AnalystCitations(List.of(EXTRACTOR));
        citations.accept("query_records", JSON.readTree("{\"items\":[{\"id\":\"usgs:us1\",\"seq\":42,"
                + "\"title\":\"quake near usgs:us2 and 01EVENTNOTRETURNED\"}]}"));
        citations.accept("list_alerts", JSON.readTree("{\"items\":[{\"id\":7,\"ruleId\":\"r\"}]}"));
        citations.accept("list_intel_events", JSON.readTree("{\"items\":[{\"id\":\"01EVENTRETURNED\"}]}"));
        citations.accept("list_anomalies", JSON.readTree("{\"items\":[{\"id\":13}]}"));
        return citations;
    }

    @Test
    @DisplayName("returned records, alerts, events and anomalies are citable in every documented form")
    void exactForms() {
        AnalystCitations citations = seeded();
        assertThat(citations.valid("usgs:us1@42")).isTrue();
        assertThat(citations.valid("[usgs:us1@42]")).isTrue();
        assertThat(citations.valid("usgs:us1")).isTrue();
        assertThat(citations.valid("alert:7")).isTrue();
        assertThat(citations.valid("event:01EVENTRETURNED")).isTrue();
        assertThat(citations.valid("01EVENTRETURNED")).isTrue();
        assertThat(citations.valid("anomaly:13")).isTrue();
    }

    @Test
    @DisplayName("a substring of tool output, another version or an unreturned id is not citable")
    void substringsRejected() {
        AnalystCitations citations = seeded();
        assertThat(citations.valid("usgs:us1@43")).isFalse();
        assertThat(citations.valid("usgs:us2")).isFalse();
        assertThat(citations.valid("01EVENTNOTRETURNED")).isFalse();
        assertThat(citations.valid("usgs")).isFalse();
        assertThat(citations.valid("us1")).isFalse();
        assertThat(citations.valid("alert:8")).isFalse();
        assertThat(citations.valid("4")).isFalse();
        assertThat(citations.valid("")).isFalse();
        assertThat(citations.valid(null)).isFalse();
    }
}
