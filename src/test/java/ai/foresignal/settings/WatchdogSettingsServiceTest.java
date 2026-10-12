package ai.foresignal.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** {@code WatchdogSettings} parsing and validation (prompt 019). */
class WatchdogSettingsServiceTest {

    static final JsonMapper JSON = JsonMapper.builder().build();

    static WatchdogSettings parse(String json) {
        return WatchdogSettingsService.parse(JSON.readTree(json.replace('\'', '"')));
    }

    @Test
    @DisplayName("an empty body is the defaults: no URLs, 60s, GET, canary off hourly")
    void defaults() {
        WatchdogSettings value = parse("{}");
        assertThat(value).isEqualTo(WatchdogSettings.defaults());
        assertThat(value.interval()).isEqualTo(Duration.ofSeconds(60));
        assertThat(value.method()).isEqualTo("GET");
        assertThat(value.canary().enabled()).isFalse();
        assertThat(value.configured()).isFalse();
    }

    @Test
    @DisplayName("a full body round-trips")
    void full() {
        WatchdogSettings value = parse("{'heartbeatUrl':'https://hc-ping.com/abc','failUrl':'https://hc-ping.com/abc/fail',"
                + "'interval':'2m','method':'post','canary':{'enabled':true,'interval':'30m'}}");
        assertThat(value.heartbeatUrl()).isEqualTo("https://hc-ping.com/abc");
        assertThat(value.interval()).isEqualTo(Duration.ofMinutes(2));
        assertThat(value.method()).isEqualTo("POST");
        assertThat(value.canary()).isEqualTo(new WatchdogSettings.Canary(true, Duration.ofMinutes(30)));
        assertThat(value.configured()).isTrue();
    }

    @Test
    @DisplayName("every problem is reported at once")
    void errors() {
        assertThatThrownBy(() -> parse("{'heartbeatUrl':'ftp://x','failUrl':'nope','interval':'10s','method':'PUT',"
                + "'canary':{'enabled':'yes','interval':'1s','x':1},'extra':true}"))
                .isInstanceOf(ValidationException.class)
                .satisfies(e -> assertThat(((ValidationException) e).getErrors()).extracting(FieldError::field)
                        .contains("heartbeatUrl", "failUrl", "interval", "method", "canary.enabled", "canary.interval",
                                "canary.x", "extra"));
    }
}
