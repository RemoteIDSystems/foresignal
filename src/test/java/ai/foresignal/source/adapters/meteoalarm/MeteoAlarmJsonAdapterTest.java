package ai.foresignal.source.adapters.meteoalarm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.adapters.cap.CapAlertRecords;
import ai.foresignal.source.formats.cap.CapAlert;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.testsupport.adapters.AdapterFixtures;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@link MeteoAlarmJsonAdapter} (prompt 089): the JSON alert converts to the CAP model with fidelity, and a
 * malformed node is refused.
 */
class MeteoAlarmJsonAdapterTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    private static JsonNode firstAlert() {
        return JSON.readTree(AdapterFixtures.bytes("meteoalarm-greece", "warnings.json"))
                .get("warnings").get(0).get("alert");
    }

    @Test
    @DisplayName("the real fixture converts to a CAP alert with identifier, sender, times and info")
    void converts() {
        CapAlert alert = MeteoAlarmJsonAdapter.convert(firstAlert());
        assertThat(alert.capVersion()).isEqualTo("1.2");
        assertThat(alert.identifier()).isNotBlank();
        assertThat(alert.sender()).isNotBlank();
        assertThat(alert.sent()).isNotNull();
        assertThat(alert.msgType()).isNotBlank();
        assertThat(alert.status()).isNotBlank();
        assertThat(alert.infos()).isNotEmpty();
        CapAlert.Info info = alert.firstInfo();
        assertThat(info.event()).isNotBlank();
        assertThat(info.language()).isNotBlank();
        assertThat(info.areas()).isNotEmpty();
    }

    @Test
    @DisplayName("a converted alert maps to a cap-alert record")
    void maps() {
        CapAlert alert = MeteoAlarmJsonAdapter.convert(firstAlert());
        NormalizedRecord record = CapAlertRecords.map(alert, "thread-1", List.of("en-GB", "el-GR"), false);
        assertThat(record).isNotNull();
        assertThat(record.kind()).isEqualTo("cap-alert");
        assertThat(record.externalId()).isEqualTo("thread-1");
        assertThat(record.observedAt()).isNotNull();
        assertThat(record.payload()).containsKey("event");
    }

    @Test
    @DisplayName("references: every space-separated sender,identifier,sent triplet is read, in a string or an array")
    void references() {
        String two = "a@x.org,id-1,2026-10-14T10:00:00+00:00 b@y.org,id-2,2026-10-14T11:00:00+00:00";
        List<CapAlert.Reference> parsed = MeteoAlarmJsonAdapter.triplets(two);
        assertThat(parsed).hasSize(2);
        assertThat(parsed.get(0).sender()).isEqualTo("a@x.org");
        assertThat(parsed.get(1).identifier()).isEqualTo("id-2");
        assertThat(parsed.get(1).sent()).isNotNull();
        CapAlert alert = MeteoAlarmJsonAdapter.convert(JSON.readTree("{\"identifier\":\"id-3\",\"sender\":\"a@x.org\","
                + "\"sent\":\"2026-10-14T12:00:00+00:00\",\"status\":\"Actual\",\"msgType\":\"Update\","
                + "\"references\":\"" + two + "\"}"));
        assertThat(alert.references()).extracting(CapAlert.Reference::identifier).containsExactly("id-1", "id-2");
        CapAlert array = MeteoAlarmJsonAdapter.convert(JSON.readTree("{\"identifier\":\"id-4\",\"sender\":\"a@x.org\","
                + "\"sent\":\"2026-10-14T12:00:00+00:00\",\"references\":[\"" + two + "\",\"c@z.org,id-0,"
                + "2026-10-14T09:00:00+00:00\"]}"));
        assertThat(array.references()).extracting(CapAlert.Reference::identifier)
                .containsExactly("id-1", "id-2", "id-0");
        assertThat(MeteoAlarmJsonAdapter.triplets("  ")).isEmpty();
    }

    @Test
    @DisplayName("a node that is not an alert, or has no sent time, is refused")
    void refuses() {
        assertThatThrownBy(() -> MeteoAlarmJsonAdapter.convert(null))
                .isInstanceOf(UpstreamFormatException.class);
        assertThatThrownBy(() -> MeteoAlarmJsonAdapter.convert(JSON.readTree("{\"identifier\":\"x\"}")))
                .isInstanceOf(UpstreamFormatException.class);
    }
}
