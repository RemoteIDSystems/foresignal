package ai.foresignal.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** {@code OutboundSettings} parsing and validation (prompt 035). */
class OutboundSettingsServiceTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static OutboundSettings parse(String json) {
        return OutboundSettingsService.parse(JSON.readTree(json.replace('\'', '"')));
    }

    static List<String> fields(String json) {
        try {
            parse(json);
        } catch (ValidationException e) {
            assertThat(e.getCode()).isEqualTo("VALIDATION_FAILED");
            return e.getErrors().stream().map(FieldError::field).toList();
        }
        throw new AssertionError("expected a validation failure for " + json);
    }

    @Test
    @DisplayName("an empty body is no overrides; missing and null lists are empty")
    void defaults() {
        assertThat(parse("{}")).isEqualTo(OutboundSettings.defaults());
        assertThat(parse("{'allowHosts':null}").allowHosts()).isEmpty();
        assertThat(new OutboundSettings(null, null, null, null)).isEqualTo(OutboundSettings.defaults());
    }

    @Test
    @DisplayName("a full body round-trips; CIDRs are normalized to their network address")
    void full() {
        OutboundSettings value = parse("{'allowHosts':['nas.lan','*.home.arpa','192.168.1.5','[fd00::5]'],"
                + "'allowCidrs':['192.168.1.77/24','fd00::/8','10.0.0.1'],'denyHosts':['evil.example.org'],"
                + "'denyCidrs':['203.0.113.0/24']}");
        assertThat(value.allowHosts()).containsExactly("nas.lan", "*.home.arpa", "192.168.1.5", "[fd00::5]");
        assertThat(value.allowCidrs()).containsExactly("192.168.1.0/24", "fd00:0:0:0:0:0:0:0/8", "10.0.0.1/32");
        assertThat(value.denyHosts()).containsExactly("evil.example.org");
        assertThat(value.denyCidrs()).containsExactly("203.0.113.0/24");
    }

    @Test
    @DisplayName("every problem is reported with its field")
    void problems() {
        assertThat(fields("{'allowHosts':[' ','bad host',5],'allowCidrs':['10.0.0.0/33','nas.lan'],"
                + "'denyHosts':'x','denyCidrs':[null],'other':1}"))
                .containsExactlyInAnyOrder("allowHosts[0]", "allowHosts[1]", "allowHosts[2]", "allowCidrs[0]",
                        "allowCidrs[1]", "denyHosts", "denyCidrs[0]", "other");
        assertThatThrownBy(() -> OutboundSettingsService.parse(JSON.readTree("[]")))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> OutboundSettingsService.parse(null)).isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("at most 500 entries per list")
    void maxEntries() {
        StringBuilder many = new StringBuilder("{'denyHosts':[");
        for (int i = 0; i <= OutboundSettingsService.MAX_ENTRIES; i++) {
            many.append(i == 0 ? "" : ",").append("'h").append(i).append(".example.org'");
        }
        many.append("]}");
        assertThat(fields(many.toString())).containsExactly("denyHosts");
    }
}
