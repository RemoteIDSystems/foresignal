package ai.foresignal.inbound.format;

import static ai.foresignal.inbound.format.FormatTestSupport.json;
import static ai.foresignal.inbound.format.FormatTestSupport.options;
import static ai.foresignal.inbound.format.FormatTestSupport.source;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.InvalidInputException;
import ai.foresignal.source.spi.NormalizedRecord;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link AlertmanagerFormat} (prompt 024). */
class AlertmanagerFormatTest {

    private static final String PAYLOAD = """
            {"version":"4","groupKey":"{}:{alertname=\\"HighTemp\\"}","truncatedAlerts":0,"status":"firing",
             "receiver":"foresignal","groupLabels":{"alertname":"HighTemp"},"commonLabels":{},"commonAnnotations":{},
             "externalURL":"http://alertmanager:9093",
             "alerts":[
               {"status":"firing","labels":{"alertname":"HighTemp","severity":"page","latitude":"52.37","longitude":"4.89"},
                "annotations":{"summary":"Server room hot"},"startsAt":"2026-10-12T07:00:00Z","endsAt":"0001-01-01T00:00:00Z",
                "generatorURL":"http://prometheus/graph","fingerprint":"a1b2c3"},
               {"status":"resolved","labels":{"alertname":"DiskFull","lat":"x","lon":"4"},
                "annotations":{},"startsAt":"2026-10-12T06:00:00Z","endsAt":"2026-10-12T07:30:00Z",
                "generatorURL":"http://prometheus/graph","fingerprint":"d4e5f6"}
             ]}""";

    private final AlertmanagerFormat format = new AlertmanagerFormat(FormatTestSupport.JSON, FormatTestSupport.mapper());

    @Test
    @DisplayName("firing alerts become records keyed by fingerprint with geometry from labels; resolved ones tombstone")
    void firingAndResolved() {
        ParsedIngest parsed = format.parse(json(PAYLOAD), source("prom-alert"), FormatOptions.empty());

        assertThat(parsed.rejections()).isEmpty();
        NormalizedRecord firing = parsed.records().get(0);
        assertThat(firing.externalId()).isEqualTo("a1b2c3");
        assertThat(firing.kind()).isEqualTo("prom-alert");
        assertThat(firing.observedAt()).isEqualTo(Instant.parse("2026-10-12T07:00:00Z"));
        assertThat(firing.eventTime()).isEqualTo(Instant.parse("2026-10-12T07:00:00Z"));
        assertThat(firing.geometry().getCoordinate().x).isEqualTo(4.89);
        assertThat(firing.geometry().getCoordinate().y).isEqualTo(52.37);
        assertThat(firing.payload()).containsEntry("status", "firing").containsEntry("receiver", "foresignal")
                .containsKeys("labels", "annotations", "generatorURL", "groupKey");
        assertThat(firing.payload().get("annotations")).isEqualTo(Map.of("summary", "Server room hot"));

        NormalizedRecord resolved = parsed.records().get(1);
        assertThat(resolved.tombstone()).isTrue();
        assertThat(resolved.observedAt()).isEqualTo(Instant.parse("2026-10-12T07:30:00Z"));
    }

    @Test
    @DisplayName("resolved: record keeps a resolved alert as a record observed at endsAt, without geometry for bad labels")
    void resolvedAsRecord() {
        ParsedIngest parsed = format.parse(json(PAYLOAD), source("prom-alert"), options("{\"resolved\":\"record\"}"));

        NormalizedRecord resolved = parsed.records().get(1);
        assertThat(resolved.tombstone()).isFalse();
        assertThat(resolved.payload()).containsEntry("status", "resolved");
        assertThat(resolved.observedAt()).isEqualTo(Instant.parse("2026-10-12T07:30:00Z"));
        assertThat(resolved.geometry()).isNull();
    }

    @Test
    @DisplayName("kindLabel picks the kind from a label; an undeclared value is UNKNOWN_KIND")
    void kindLabel() {
        ParsedIngest parsed = format.parse(json(PAYLOAD), source("page", "ticket"), options("{\"kindLabel\":\"severity\"}"));

        assertThat(parsed.records()).extracting(NormalizedRecord::kind).containsExactly("page");
        assertThat(parsed.rejections()).singleElement().satisfies(r -> {
            assertThat(r.index()).isEqualTo(1);
            assertThat(r.reason()).isEqualTo("UNKNOWN_KIND");
        });
    }

    @Test
    @DisplayName("several kinds without kindLabel reject every alert; a wrong version is a 400")
    void kindsAndVersion() {
        ParsedIngest parsed = format.parse(json(PAYLOAD), source("a", "b"), FormatOptions.empty());
        assertThat(parsed.records()).isEmpty();
        assertThat(parsed.rejections()).hasSize(2);

        assertThatThrownBy(() -> format.parse(json("{\"version\":\"3\",\"alerts\":[]}"), source("a"), FormatOptions.empty()))
                .isInstanceOf(InvalidInputException.class);
        assertThat(format.options()).extracting(FormatOptionSpec::name).containsExactly("kindLabel", "resolved");
    }
}
